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
 * (同步集合=全量自动发现 ~172 表, sync_daily.sh 无参模式: Oracle 有数据+PG 有同主键+非死表
 * 即纳入, **含 TC9W_BP 系列/TC90_BP 族等全部 bp 批次表**——sync.log read=523368 实证;
 * 本文件静态 specs 仅为主要表子集, GET /sync/tables 同; wb_audit_log 等纯新系统表不在其中)。
 * 撞号后果实证(2026-11-26): 同步行零消耗 PG 序列, 段内首传即 pk 冲突(seq_offset.sql
 * 2026-11-26 块)。
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
        Spec("TC90_YY", listOf("TC90YYID"), dedupeKey = listOf("SAFESIGN", "ATC900")), // 历史重复组保留最大主键(W6 uk_tc90_yy)
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
        // 名单表族(2026-11-21 用户拍板): 原封不动全量同步, 无月份界限
        Spec("TC9W",  listOf("ATC9W0")),
        Spec("TC9W1", listOf("ATC9W0")),
        Spec("TC9W2", listOf("ATC9W0")),
        Spec("TC9W3", listOf("ATC9W0")),
        Spec("TC9W4", listOf("ATC9W0")),
        Spec("TC9U",  listOf("ATC9U0")),
    )

    /** latestBy 非空时源侧按 PK 去重取该列最新行(源表主键失效的兜底, 如 TC90_LIST.IKEY)
     *  dedupeKey 非空时按业务键去重保留 PK 最大行(W6 "保留后插"语义), 影子 PG 行先删后插;
     *  dedupeKey 任一列为 NULL 的行不参与去重(=PG 唯一约束 NULL 相异语义)
     *  maxAae002 非空时源侧限定 aae002<=该值(名单表族历史月只读: <=202610 为东软的账,
     *  202611+ 为新系统自留地, Oracle 不得写入防新旧两套当月名单混账) */
    data class Spec(val table: String, val pk: List<String>, val latestBy: String? = null,
                    val dedupeKey: List<String>? = null, val maxAae002: Long? = null)

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
            DriverManager.getConnection(oraUrl, oraUser, oraPass).use { ora ->
                // 自动发现: Oracle∩PG 双方带主键的共有表全量纳入(用户拍板: 东软有的表原封不动全同步;
                // 新系统独有的表 PG 独有, 天然被交集排除=自留地)。人工 specs 保留为特殊钩子覆盖项。
                val manual = if (filter.isNullOrEmpty()) specs
                else specs.filter { s -> filter.any { it.equals(s.table, ignoreCase = true) } }
                val chosen = if (filter.isNullOrEmpty()) {
                    val byName = manual.associateBy { it.table.uppercase() }
                    val discovered = discoverSpecs(ora).filter { it.table.uppercase() !in byName }
                    manual + discovered
                } else {
                    val auto = discoverSpecs(ora).filter { s -> filter.any { it.equals(s.table, ignoreCase = true) } }
                    val byName = manual.associateBy { it.table.uppercase() }
                    (manual + auto.filter { it.table.uppercase() !in byName })
                }
                if (chosen.isEmpty()) return mapOf("appCode" to -1, "errMsg" to "无匹配的同步表")
                val results = chosen.map { spec ->
                    val t0 = System.currentTimeMillis()
                    try {
                        // dedupeKey 表走 FULL(W6 去重语义需全量分组, INCR 流式合并不适用)
                        val r = if (spec.dedupeKey != null) syncOne(ora, spec)
                        else if (mode.equals("INCR", true)) incrOne(ora, spec) else syncOne(ora, spec)
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

    /** 死表排除清单(2026-11-21 全源交叉体检: 应用SQL未引用+过程/页面源码无引用+数据陈旧,
     *  工具 work/AppSqlProbe.java + unref/proc_ref 交叉; 含日期戳备份残表正则二次兜底) */
    val deadTables = setOf( // v7 定版: 并集 236 张(含 CA05 郑州村组残留)
        "AA05",
        "AA10_XAEG_CONT",
        "AA13",
        "AA73",
        "AA74",
        "AA82",
        "AA96",
        "AC07",
        "AT70",
        "AT71",
        "CA05",
        "CA11",
        "CA111",
        "CA112",
        "CA13",
        "CA16",
        "CA18",
        "CA20",
        "CA21",
        "CC78",
        "CLIENT_PAD",
        "JFSJ01",
        "JFSJ02",
        "MAP_CONFIG",
        "MAP_ICON",
        "MAP_LAYER",
        "MAP_PIC",
        "SYSLOB",
        "SYS_MSG",
        "SYS_NOTE",
        "SYS_USER",
        "TC11",
        "TC12",
        "TC13",
        "TC13_BP",
        "TC14",
        "TC15",
        "TC16",
        "TC17",
        "TC20",
        "TC21",
        "TC70",
        "TC71",
        "TC72",
        "TC73",
        "TC74",
        "TC75",
        "TC76",
        "TC78",
        "TC79",
        "TC7A",
        "TC7D",
        "TC7E",
        "TC7F",
        "TC7G",
        "TC7H",
        "TC7I",
        "TC7J",
        "TC7K",
        "TC7L",
        "TC7M",
        "TC7N",
        "TC7O",
        "TC7P",
        "TC7Q",
        "TC7R",
        "TC7S",
        "TC7V",
        "TC7W",
        "TC7X",
        "TC7Y",
        "TC7Z",
        "TC81",
        "TC83",
        "TC84",
        "TC85",
        "TC86",
        "TC87",
        "TC88",
        "TC89",
        "TC8A",
        "TC8B",
        "TC8C",
        "TC8D",
        "TC8E",
        "TC8F",
        "TC90_ZJBG",
        "TC93_INFO",
        "TC96",
        "TC96_TMP",
        "TC9B",
        "TC9C",
        "TC9D",
        "TC9D_FILE",
        "TC9G",
        "TC9H",
        "TC9I",
        "TC9J",
        "TC9K",
        "TC9M",
        "TC9N",
        "TC9S",
        "TC9Z",
        "TD02",
        "TJ013",
        "TJ014",
        "TJ02_MSG",
        "TJ031",
        "TJ032",
        "TJ071",
        "TJ072",
        "TJ0A1_20260725",
        "TJ0B1_20260725",
        "TJ0C1_20260725",
        "TJ0D1_20260725",
        "TJ0E1_20260725",
        "TJ0F1_20260725",
        "TJ101",
        "TJ201",
        "TJ203",
        "TJ204",
        "TJ205",
        "TJ206",
        "TJ207",
        "TJ209",
        "TJ210",
        "TJ211",
        "TJ212",
        "TJ213",
        "TJ214",
        "TJ215",
        "TJ216",
        "TJ841",
        "TJ842",
        "TJ843",
        "TJ844",
        "TJAA1",
        "TJAA2",
        "TJAB01",
        "TJAC1",
        "TJAC2",
        "TJAG1",
        "TJAJ1",
        "TJAM1",
        "TJAM2",
        "TJAM3",
        "TJAM4",
        "TJBB5",
        "TJBB6",
        "TJBB7",
        "TJBH4",
        "TJBH5",
        "TJB_WORK",
        "TJCA1",
        "TJCA3",
        "TJCC4",
        "TJDA1",
        "TJDA2",
        "TJDA3",
        "TJDA4",
        "TJDA5",
        "TJDB1",
        "TJDB2",
        "TJDB3",
        "TJDC1",
        "TJDC2",
        "TJDC3",
        "TJDC4",
        "TJQC3",
        "TJQC4",
        "TJQC5",
        "TJTK1",
        "TJTK2",
        "TJTK3",
        "USER_INFO",
        "VB01",
        "VB40",
        "VB42",
        "VB45",
        "VB46",
        "VC01",
        "VC02",
        "VC03_BP",
        "VC0M",
        "VD20",
        "VD23",
        "VD24",
        "VD25",
        "VV91",
        "VV92",
        "V_AA12",
        "V_AB01_TREE",
        "V_AC01_TC97",
        "V_AC10",
        "V_AC10_ZCXJ",
        "V_CBJ",
        "V_CC78_TC9J",
        "V_DW_EXPIRE",
        "V_DW_EXPIRE_TX",
        "V_INVOICE_TC93",
        "V_LAST_HT",
        "V_TB93_AB07",
        "V_TB93_TC93_ZCXJ",
        "V_TB93_TC9T",
        "V_TC8F",
        "V_TC8I_LIST",
        "V_TC8T_TC8V",
        "V_TC90_ADW",
        "V_TC90_DFGZ",
        "V_TC90_MAX",
        "V_TC90_TC97",
        "V_TC91_TC90",
        "V_TC93_BALANCE",
        "V_TC97_DA",
        "V_TC97_DSH",
        "V_TC98_AC01",
        "V_TC9F",
        "V_TC9V",
        "V_TJ02",
        "V_ZDGZFFQD",
        "WBS_SCREEN",
        "WTS_CODE",
        "WTS_FUNCTION",
        "WTS_LAYERS",
        "WTS_MACHINE",
        "WTS_NAVIGATION",
        "WTS_POLICY",
        "WTS_RESOURCE",
        "WTS_STYLE_TEMPLATE",
        "ZE09",
        "ZE10",
        "ZE12",
        "ZE30",
        "ZE32",
        "ZE33",
        "ZH_CH_DWHZ",
    )

    /** 自动发现共有表(双方带主键): Oracle user_constraints(P) ∩ PG information_schema PK。
     *  新系统独有的表(PG 独有)=交集自动排除=自留地; 东软独有(Oracle 独有)亦排除(无 PG 落点)。 */
    private fun discoverSpecs(ora: Connection): List<Spec> {
        val pgPks = HashMap<String, String>()
        pg.query(
            "SELECT tc.table_name, string_agg(kcu.column_name, ',' ORDER BY kcu.ordinal_position) pk " +
            "FROM information_schema.table_constraints tc " +
            "JOIN information_schema.key_column_usage kcu " +
            "  ON kcu.constraint_name=tc.constraint_name AND kcu.table_name=tc.table_name " +
            "WHERE tc.constraint_type='PRIMARY KEY' AND tc.table_schema='public' " +
            "GROUP BY tc.table_name") { rs, _ ->
            pgPks[rs.getString(1).uppercase()] = rs.getString(2)
        }
        val out = mutableListOf<Spec>()
        val seen = HashSet<String>()
        ora.prepareStatement(
            "SELECT cc.table_name, LISTAGG(cc.column_name, ',') WITHIN GROUP (ORDER BY cc.position) pk " +
            "FROM user_constraints k JOIN user_cons_columns cc ON k.constraint_name=cc.constraint_name " +
            "WHERE k.constraint_type='P' GROUP BY cc.table_name ORDER BY cc.table_name").use { ps ->
            val rs = ps.executeQuery()
            while (rs.next()) {
                val tab = rs.getString(1)
                val pk = rs.getString(2) ?: continue
                if (pgPks[tab.uppercase()] == null) continue
                if (tab.uppercase() in deadTables) continue
                if (seen.add(tab.uppercase())) out.add(Spec(tab, pk.split(",").map { it.trim() }))
            }
        }
        // 无用表过滤: Oracle 空表(从未使用)不同步; 带日期戳的备份残表(TC9W_BP04_20260725 类)不同步
        // 游标纪律: 每张表 count(*) 的 Statement/ResultSet 必须 close(泄漏 → ORA-01000 全程崩)
        val useful = out.filter { s ->
            if (s.table.matches(Regex(".*_20\\d{6}$"))) return@filter false
            try {
                ora.createStatement().use { st ->
                    st.executeQuery("SELECT count(*) FROM ${s.table}").use { r ->
                        r.next(); r.getLong(1) > 0
                    }
                }
            } catch (e: Exception) { false }
        }
        return useful
    }

    /** dedupeKey 路径: 全量读源→按业务键分组保留 PK 最大行(W6 "保留后插"语义)→组内删 PG 旧行→upsert 保留行。
     *  调和 uk_tc90_yy(SAFESIGN,ATC900) 与 Oracle 历史重复组; dedupeKey 含 NULL 的行不分组(=PG NULL 相异语义)。 */
    private fun syncOneDedupe(ora: Connection, spec: Spec, cols: List<String>): Result {
        val pkIdx = spec.pk.map { p -> cols.indexOfFirst { it.equals(p, true) } }
        val dkIdx = spec.dedupeKey!!.map { d -> cols.indexOfFirst { it.equals(d, true) } }
        require(pkIdx.all { it >= 0 } && dkIdx.all { it >= 0 }) { "${spec.table} dedupeKey/PK 列缺失: ${cols}" }
        val colList = cols.joinToString(",")
        val ph = cols.joinToString(",") { "?" }
        val pkList = spec.pk.joinToString(",")
        val nonPk = cols.filter { c -> spec.pk.none { p -> p.equals(c, true) } }
        val upsertSql = if (nonPk.isEmpty())
            "INSERT INTO ${spec.table.lowercase()} ($colList) VALUES ($ph) ON CONFLICT ($pkList) DO NOTHING"
        else "INSERT INTO ${spec.table.lowercase()} ($colList) VALUES ($ph) ON CONFLICT ($pkList) DO UPDATE SET " +
            nonPk.joinToString(",") { "${it}=EXCLUDED.${it}" }
        val delSql = "DELETE FROM ${spec.table.lowercase()} WHERE " +
            spec.dedupeKey.joinToString(" AND ") { "${it}=?" }

        fun pkVal(v: Any?): Double = (v as? Number)?.toDouble()
            ?: v?.toString()?.toDoubleOrNull() ?: 0.0

        var read = 0L
        val kept = LinkedHashMap<String, Triple<Array<Any?>, Double, Array<Any?>>>() // 键串→(行值, 保留PK值, 业务键原值)
        val solo = mutableListOf<Array<Any?>>()
        ora.prepareStatement("SELECT $colList FROM ${spec.table}").use { ps ->
            ps.fetchSize = 2000
            val rs = ps.executeQuery()
            while (rs.next()) {
                read++
                val vals = Array(cols.size) { i -> rs.getObject(i + 1) }
                val dkVals = Array(dkIdx.size) { i -> vals[dkIdx[i]] }
                if (dkVals.any { it == null }) { solo.add(vals); continue }
                val key = dkVals.joinToString("\u0001")
                val pkMax = pkVal(vals[pkIdx[0]])
                val cur = kept[key]
                if (cur == null || pkMax > cur.second) kept[key] = Triple(vals, pkMax, dkVals)
            }
        }
        val conn = requireNotNull(pg.dataSource).connection
        conn.autoCommit = false
        var deleted = 0L
        var written = 0L
        try {
            conn.prepareStatement(delSql).use { del ->
                for ((_, t) in kept) {
                    for (i in dkIdx.indices) del.setObject(i + 1, t.third[i])
                    deleted += del.executeUpdate().toLong()
                }
            }
            conn.prepareStatement(upsertSql).use { batch ->
                for ((_, t) in kept) {
                    for (i in cols.indices) batch.setObject(i + 1, t.first[i])
                    batch.addBatch()
                }
                // dedupeKey 含 NULL 的行: PG 唯一约束视 NULL 相异, 直接 upsert
                for (vals in solo) {
                    for (i in cols.indices) batch.setObject(i + 1, vals[i])
                    batch.addBatch()
                }
                written += batch.executeBatch().sumOf { if (it == Statement_SUCCESS_NO_INFO) 1L else it.coerceAtLeast(0).toLong() }
            }
            conn.commit() // 小表单事务: 删+插全量原子, 不留半组状态
        } finally {
            conn.autoCommit = true
            conn.close()
        }
        return Result(read, written, 0, 0, deleted)
    }

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
        if (spec.dedupeKey != null) return syncOneDedupe(ora, spec, cols)
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
        val srcWhere = if (spec.maxAae002 != null) " WHERE aae002<=" + spec.maxAae002 else ""
        val selectSql = if (spec.latestBy == null) "SELECT $colList FROM ${spec.table}$srcWhere"
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
        val srcWhere = if (spec.maxAae002 != null) " WHERE aae002<=" + spec.maxAae002 else ""
        val selectSql = if (spec.latestBy == null) "SELECT $colList FROM ${spec.table}$srcWhere ORDER BY $pkList"
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
