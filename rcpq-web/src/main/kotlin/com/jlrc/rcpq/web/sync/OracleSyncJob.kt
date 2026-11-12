package com.jlrc.rcpq.web.sync

import org.springframework.beans.factory.annotation.Value
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Component
import java.sql.Connection
import java.sql.DriverManager
import java.sql.Timestamp
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Oracle→PG 每日同步引擎（并行运行期, 2026-11-12 定调）。
 *
 * 背景: 新旧系统并行一段时间, 新系统每日从 Oracle 旧库同步数据。同步表=旧库原始表,
 * PG 侧对这些表**只读不改造**——任何写在两次同步之间都会被下一次同步覆盖
 * (新系统自己的业务表如 TC9W/TC9W1/TC9W_BP 系列/wb_audit_log 不在同步集合, 不受影响)。
 *
 * 规则(确定性, 可复核):
 *  1) UPSERT by PK: Oracle 有 → PG 覆盖(Oracle 赢); Oracle 无的 PG 行不动(新系统自建行保留);
 *     只写两库共有列(自动取交集, PG 独有列不碰); 不执行删除。
 *  2) 每表每次一行 wb_sync_log(读/插/更新/耗时/状态)。
 *  3) 串行防重入(runAtomic)。分块批写(fetchSize 2000 / batch 500)。
 *
 * 并行期撞号: 两库序列独立, 同 PK 双方都发号会互覆 —— 须先把 PG 序列抬到
 * Oracle 之上(scripts/db/seq_offset.sql, setval 至 1e7 段), 本引擎不做主键改写。
 */
@Component
class OracleSyncJob(private val pg: JdbcTemplate) {

    @Value("\${ora.url:jdbc:oracle:thin:@172.16.0.60:1521:orcl}")
    lateinit var oraUrl: String

    @Value("\${ora.user:ccrcpq}")
    lateinit var oraUser: String

    @Value("\${ora.password:}")
    lateinit var oraPass: String

    /** 同步集合(表名→主键), 全部 UPSERT; 扩表时在此登记+跑 scripts/db 里的列注释脚本 */
    val specs: List<Spec> = listOf(
        Spec("TC90", listOf("ATC900")),
        Spec("TC90_LIST", listOf("IKEY"), latestBy = "AAE036"), // 源表 IKEY 有重复(旧库 PK 失效), 取最新行
        Spec("TC90_YY", listOf("TC90YYID")),
        Spec("AC01", listOf("AAC001")),
        Spec("TB90", listOf("ATB900")),
        Spec("TB91", listOf("ATB910")),
        Spec("TB93", listOf("ATB930")),
        Spec("TC93", listOf("ATC930")),
        Spec("TB97", listOf("ATB970")),
        Spec("TB99", listOf("ATB990")),
        Spec("AB01", listOf("AAB001")),
        Spec("AA10", listOf("AAA100", "AAA102")),
        Spec("TC06", listOf("ATC060")),
        Spec("TC92", listOf("ATC920")),
        Spec("CE10", listOf("ACE100")),
    )

    /** latestBy 非空时源侧按 PK 去重取该列最新行(源表主键失效的兜底, 如 TC90_LIST.IKEY) */
    data class Spec(val table: String, val pk: List<String>, val latestBy: String? = null)

    private val running = AtomicBoolean(false)

    fun isRunning(): Boolean = running.get()

    /** 注册表状态(表/主键/PG 行数) */
    fun tableStatus(): List<Map<String, Any?>> = specs.map { s ->
        val n = pg.queryForObject("SELECT count(*) FROM ${s.table.lowercase()}", Long::class.java)
        mapOf("table" to s.table, "pk" to s.pk, "pg_rows" to n)
    }

    /** 执行同步; filter=null 全量, 否则只跑指定表(大小写不敏感)。返回逐表结果汇总。 */
    fun syncTables(filter: List<String>? = null): Map<String, Any?> {
        if (!running.compareAndSet(false, true))
            return mapOf("appCode" to -1, "errMsg" to "同步进行中, 请稍后(防重入)")
        try {
            val chosen = if (filter.isNullOrEmpty()) specs
            else specs.filter { s -> filter.any { it.equals(s.table, ignoreCase = true) } }
            if (chosen.isEmpty()) return mapOf("appCode" to -1, "errMsg" to "无匹配的同步表")
            DriverManager.getConnection(oraUrl, oraUser, oraPass).use { ora ->
                val results = chosen.map { spec ->
                    val t0 = System.currentTimeMillis()
                    try {
                        val r = syncOne(ora, spec)
                        logRun(spec.table, r.read, r.upsert, r.ins, r.upd, System.currentTimeMillis() - t0, "OK", null)
                        mapOf("table" to spec.table, "status" to "OK", "read" to r.read,
                            "upsert" to r.upsert, "insert" to r.ins, "update" to r.upd,
                            "ms" to System.currentTimeMillis() - t0)
                    } catch (e: Exception) {
                        logRun(spec.table, 0, 0, 0, 0, System.currentTimeMillis() - t0, "ERROR", e.message)
                        mapOf("table" to spec.table, "status" to "ERROR", "error" to e.message)
                    }
                }
                return mapOf("appCode" to (if (results.all { it["status"] == "OK" }) 1 else -1),
                    "tables" to results,
                    "msg" to "同步完成: " + results.joinToString(", ") { "${it["table"]}=${it["status"]}" })
            }
        } finally {
            running.set(false)
        }
    }

    private data class Result(val read: Long, val upsert: Long, val ins: Long, val upd: Long)

    private fun syncOne(ora: Connection, spec: Spec): Result {
        // 列交集: Oracle user_tab_columns ∩ PG information_schema, 保持 Oracle 顺序
        val oraCols = mutableListOf<String>()
        ora.prepareStatement(
            "SELECT column_name FROM user_tab_columns WHERE table_name=? ORDER BY column_id").use { ps ->
            ps.setString(1, spec.table)
            val r = ps.executeQuery()
            while (r.next()) oraCols.add(r.getString(1))
        }
        val pgCols = pg.query(
            "SELECT upper(column_name) FROM information_schema.columns WHERE table_name=?",
            { rs, _ -> rs.getString(1) }, spec.table.lowercase()).toSet()
        val cols = oraCols.filter { it in pgCols }
        require(cols.containsAll(spec.pk)) { "${spec.table} 主键列缺失: ${spec.pk} ∩ $cols" }
        val pkUpper = spec.pk.map { it.uppercase() }
        val nonPk = cols.filter { it.uppercase() !in pkUpper }

        val colList = cols.joinToString(",")
        val ph = cols.joinToString(",") { "?" }
        val pkList = spec.pk.joinToString(",")
        val upsertSql =
            if (nonPk.isEmpty())
                "INSERT INTO ${spec.table.lowercase()} ($colList) VALUES ($ph) ON CONFLICT ($pkList) DO NOTHING"
            else
                "INSERT INTO ${spec.table.lowercase()} ($colList) VALUES ($ph) ON CONFLICT ($pkList) DO UPDATE SET " +
                    nonPk.joinToString(",") { "${it}=EXCLUDED.${it}" }

        var read = 0L
        var written = 0L
        val selectSql = if (spec.latestBy == null) "SELECT $colList FROM ${spec.table}"
        else "SELECT $colList FROM (SELECT t.*, ROW_NUMBER() OVER (PARTITION BY $pkList " +
            "ORDER BY ${spec.latestBy} DESC NULLS LAST) rn FROM ${spec.table} t) WHERE rn = 1"
        ora.prepareStatement(selectSql).use { ps ->
            ps.fetchSize = 2000
            val rs = ps.executeQuery()
            val conn = requireNotNull(pg.dataSource).connection
            conn.autoCommit = false
            try {
                conn.prepareStatement(upsertSql).use { batch ->
                    var n = 0
                    while (rs.next()) {
                        read++
                        for (i in cols.indices) batch.setObject(i + 1, rs.getObject(i + 1))
                        batch.addBatch()
                        if (++n >= 500) {
                            written += batch.executeBatch().sumOf { if (it == Statement_SUCCESS_NO_INFO) 1L else it.coerceAtLeast(0).toLong() }
                            conn.commit(); n = 0
                        }
                    }
                    if (n > 0) {
                        written += batch.executeBatch().sumOf { if (it == Statement_SUCCESS_NO_INFO) 1L else it.coerceAtLeast(0).toLong() }
                        conn.commit()
                    }
                }
            } finally {
                conn.autoCommit = true
                conn.close()
            }
        }
        // PG ON CONFLICT DO UPDATE 的返回计数不区分插入/更新, 统计入 upsert
        return Result(read, written, 0, 0)
    }

    companion object {
        private const val Statement_SUCCESS_NO_INFO = -2 // java.sql.Statement.SUCCESS_NO_INFO
    }

    private fun logRun(table: String, read: Long, upsert: Long, ins: Long, upd: Long, ms: Long, status: String, err: String?) {
        runCatching {
            pg.update(
                """INSERT INTO wb_sync_log (table_name, read_rows, upsert_rows, insert_rows,
                   update_rows, duration_ms, status, error) VALUES (?, ?, ?, ?, ?, ?, ?, ?)""",
                table, read, upsert, ins, upd, ms, status, err)
        }
    }
}
