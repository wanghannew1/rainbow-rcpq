package com.jlrc.rcpq.web.sync

import org.springframework.beans.factory.annotation.Value
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Component
import java.math.BigDecimal
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

    /** 执行同步。mode: FULL=全量 UPSERT(夜间); INCR=白天增量——两库按 PK 排序归并比对,
     *  跳过未变行, 只写新增/修改键(Oracle 侧删除不传播: PG 自建行保留, 与全量同口径)。
     *  filter=null 全部表, 否则只跑指定表(大小写不敏感)。 */
    fun syncTables(filter: List<String>? = null, mode: String = "FULL"): Map<String, Any?> {
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
                        val r = if (mode.equals("INCR", true)) incrOne(ora, spec) else syncOne(ora, spec)
                        logRun(spec.table, mode, r.read, r.upsert, r.skipped, System.currentTimeMillis() - t0, "OK", null)
                        mapOf("table" to spec.table, "status" to "OK", "mode" to mode, "read" to r.read,
                            "upsert" to r.upsert, "skipped" to r.skipped,
                            "ms" to System.currentTimeMillis() - t0)
                    } catch (e: Exception) {
                        val msg = e.toString() + "\n" + (e.stackTrace.firstOrNull()?.toString() ?: "")
                        logRun(spec.table, mode, 0, 0, 0, System.currentTimeMillis() - t0, "ERROR", msg)
                        mapOf("table" to spec.table, "status" to "ERROR", "mode" to mode, "error" to msg)
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

    private data class Result(val read: Long, val upsert: Long, val ins: Long, val upd: Long, val skipped: Long = 0)

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
            "ORDER BY ${spec.latestBy} DESC NULLS LAST, ROWID) rn FROM ${spec.table} t) WHERE rn = 1"
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

    /** INCR 模式: 两库同列按 PK 排序流式归并, 规范化指纹比对——
     *  未变行跳过, 新增/修改键回查 Oracle 批量 UPSERT; PG 独有键跳过(自建行保留)。 */
    private fun incrOne(ora: Connection, spec: Spec): Result {
        if (DEBUG) System.err.println("DBG incrOne 进入 " + spec.table)
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
        val pkIdx = spec.pk.map { p -> cols.indexOfFirst { it.equals(p, true) } }
        val pkList = spec.pk.joinToString(",")
        val colList = cols.joinToString(",")
        val selectSql = if (spec.latestBy == null) "SELECT $colList FROM ${spec.table} ORDER BY $pkList"
        else "SELECT $colList FROM (SELECT t.*, ROW_NUMBER() OVER (PARTITION BY $pkList " +
            "ORDER BY ${spec.latestBy} DESC NULLS LAST, ROWID) rn FROM ${spec.table} t) WHERE rn = 1 ORDER BY $pkList"

        var read = 0L
        var skipped = 0L
        val changedKeys = mutableListOf<Array<Any?>>()
        val upsertSql =
            "INSERT INTO ${spec.table.lowercase()} ($colList) VALUES (${cols.joinToString(",") { "?" }}) " +
            "ON CONFLICT ($pkList) DO UPDATE SET " +
            cols.filter { c -> spec.pk.none { p -> p.equals(c, true) } }
                .joinToString(",") { "${it}=EXCLUDED.${it}" }

        fun canon(v: Any?): String = when (v) {
            null -> "\u0000"
            is BigDecimal -> v.stripTrailingZeros().toPlainString()
            is Timestamp -> v.time.toString()
            is java.sql.Date -> v.time.toString()
            is String -> "s:" + v
            is Number -> "n:" + v.toString()
            else -> v.toString()
        }

        ora.prepareStatement(selectSql).use { po ->
            po.fetchSize = 2000
            val rsOra = po.executeQuery()
            requireNotNull(pg.dataSource).connection.use { conn ->
                conn.autoCommit = false
                conn.prepareStatement(
                    "SELECT $colList FROM ${spec.table.lowercase()} ORDER BY $pkList").use { pi ->
                    pi.fetchSize = 2000
                    val rsPg = pi.executeQuery()
                    val rowOra = arrayOfNulls<Any?>(cols.size)
                    val rowPg = arrayOfNulls<Any?>(cols.size)
                    val hasOra = rsOra.next()
                    val hasPg = rsPg.next()
                    fun pullOra() {
                        for (i in cols.indices) rowOra[i] = rsOra.getObject(i + 1)
                    }
                    fun pullPg() {
                        for (i in cols.indices) rowPg[i] = rsPg.getObject(i + 1)
                    }
                    fun keyOf(row: Array<Any?>): String = pkIdx.joinToString("|") { canon(row[it]) }
                    // PG 侧 date 列(迁移语义截断时间)与 Oracle DATE 带时间的值: 只比日期部分,
                    // 时间差异不判改(比对容忍); 两侧同为 timestamp 时仍按毫秒精确比
                    fun eqCol(i: Int): Boolean {
                        val a = rowOra[i]; val b = rowPg[i]
                        if (b is java.sql.Date && a is Timestamp) {
                            val dA = a.toLocalDateTime().toLocalDate()
                            return dA == b.toLocalDate()
                        }
                        return canon(a) == canon(b)
                    }
                    fun cmp(rowA: Array<Any?>, rowB: Array<Any?>): Int =
                        keyOf(rowA).compareTo(keyOf(rowB))
                    val dbg = DEBUG
                    var o = hasOra; var g = hasPg
                    if (o) pullOra()
                    if (g) pullPg()
                    // 归并比对(两流均按 PK 升序); 修改判定=规范化指纹不等(铁律3: BigDecimal 归一后比)
                    while (o || g) {
                        when {
                            o && (!g || cmp(rowOra, rowPg) < 0) -> {  // Oracle 新增
                                if (DEBUG && changedKeys.size < 3)
                                    System.err.println("DBG 新增分支 keyOra=" + keyOf(rowOra) +
                                        (if (g) " keyPg=" + keyOf(rowPg) else " (PG流已尽)"))
                                changedKeys.add(rowOra.clone()); read++
                                o = rsOra.next(); if (o) pullOra()
                            }
                            o && g && cmp(rowOra, rowPg) == 0 -> {
                                read++
                                if (cols.indices.any { !eqCol(it) }) {
                                    if (DEBUG && changedKeys.isEmpty()) {
                                        System.err.println("DBG key=" + keyOf(rowOra))
                                        for (i in cols.indices) if (!eqCol(i))
                                            System.err.println("  DBG col=" + cols[i] + " oraClass=" +
                                                (rowOra[i]?.javaClass?.simpleName ?: "null") + " ora=" + rowOra[i] +
                                                " pgClass=" + (rowPg[i]?.javaClass?.simpleName ?: "null") + " pg=" + rowPg[i])
                                    }
                                    changedKeys.add(rowOra.clone())
                                } else skipped++
                                o = rsOra.next(); if (o) pullOra()
                                g = rsPg.next(); if (g) pullPg()
                            }
                            else -> {  // PG 独有 → 自建行/Oracle 已删, 不动
                                g = rsPg.next(); if (g) pullPg()
                            }
                        }
                    }
                }
            }
        }
        // 回查 Oracle 批量 UPSERT 差异键(分块 IN 条件; 单键组兼容复合 PK)
        var written = 0L
        if (changedKeys.isNotEmpty()) {
            val conn = requireNotNull(pg.dataSource).connection
            conn.autoCommit = false
            try {
                conn.prepareStatement(upsertSql).use { batch ->
                    // 回查同样套 latestBy 去重(源表 PK 失效有重复, 如 TC90_LIST.IKEY);
                    // 内层只生成子查询(WHERE rn=1 由统一外层带过滤拼入, 避免双 WHERE)
                    val innerDedupe = if (spec.latestBy == null) "SELECT $colList FROM ${spec.table}"
                    else "SELECT t.* FROM (SELECT t.*, ROW_NUMBER() OVER (PARTITION BY $pkList " +
                        "ORDER BY ${spec.latestBy} DESC NULLS LAST, ROWID) rn FROM ${spec.table} t)"
                    for (keys in changedKeys.chunked(300)) {
                        val ors = (0 until keys.size).joinToString(" OR ") {
                            "(" + spec.pk.mapIndexed { j, p -> "$p = ?" }.joinToString(" AND ") + ")"
                        }
                        ora.prepareStatement(
                            if (spec.latestBy == null) "$innerDedupe WHERE $ors"
                            else "SELECT * FROM ($innerDedupe WHERE rn = 1 AND ($ors)) t2").use { ps ->
                            var pi = 1
                            for (k in keys) for (j in pkIdx) ps.setObject(pi++, k[j])
                            ps.executeQuery().use { rs ->
                                while (rs.next()) {
                                    for (i in cols.indices) batch.setObject(i + 1, rs.getObject(i + 1))
                                    batch.addBatch()
                                }
                            }
                        }
                        written += batch.executeBatch().sumOf { if (it == Statement_SUCCESS_NO_INFO) 1L else it.coerceAtLeast(0).toLong() }
                        conn.commit()
                    }
                }
            } finally {
                conn.autoCommit = true
                conn.close()
            }
        }
        return Result(read, written, 0, 0, skipped)
    }

    companion object {
        /** 调试开关: SyncMain --debug 置 true, 打印首个差异行明细 */
        var DEBUG = false
        private const val Statement_SUCCESS_NO_INFO = -2 // java.sql.Statement.SUCCESS_NO_INFO
    }

    private fun logRun(table: String, mode: String, read: Long, upsert: Long, skipped: Long, ms: Long, status: String, err: String?) {
        runCatching {
            pg.update(
                "INSERT INTO wb_sync_log (table_name, mode, read_rows, upsert_rows, skipped_rows, " +
                    "duration_ms, status, error) VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
                table, mode, read, upsert, skipped, ms, status, err)
        }
    }
}
