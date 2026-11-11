package com.jlrc.rcpq.web

import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.web.bind.annotation.*

/**
 * 业务员工作台辅助端点(窄口径白名单, 供 static/index.html 使用; 无自由 SQL 面)。
 * 每个端点对应一个业务动作的批次准备/状态查询, 业务校验与提交仍走 /proc 与 /interaction。
 */
@RestController
@RequestMapping("/wb")
class WorkbenchController(private val jdbcTemplate: JdbcTemplate,
                          private val baopan: BaopanExporter) {

    private fun nextval(seq: String) = jdbcTemplate.queryForObject("SELECT nextval('$seq')", Long::class.java)!!

    @PostMapping("/next-batch")
    fun nextBatch(): Map<String, Any?> = mapOf("ace100" to nextval("seq_ce10_ace100"))

    /** 新签导入批次行(tc90_bp), 字段白名单逐一绑定(含校验必填: 身份证有效期/联系电话/协议首签日期/服务费标准) */
    data class HireRow(
        val aac002: String, val aac003: String, val aae00p: String,
        val atb930: Long, val atb900: Long, val aab001: Long,
        val atc90a: String, val atc90c: String, val atc90d: String?,
        val atc905: String, val aae002: String, val aaa401: String?,
        val aaa100: String?, val aaa203: String?, val aaa301: String?,
        val aab004: String?,
        val aae005: String, val atc90ax: String, val atc90b: String,
        val atb90a: String, val atb90b: String?,
    )

    @PostMapping("/hire-batch")
    fun hireBatch(@RequestBody r: HireRow): Map<String, Any?> {
        val ace = nextval("seq_ce10_ace100")
        jdbcTemplate.update(
            """INSERT INTO tc90_bp (id, ace100, aac002, aac003, aac005, aac012, aae00p, aac009, atc90a,
               atc90c, atc90d, atc905, aae002, aaa401, aaa100, aaa203, aaa301,
               aae005, atc90ax, atc90b, atb90a, atb90b, atb930, atb900, aab001, aab004)
               VALUES (nextval('seq_tc90_id'), ?, ?, ?, '01-汉族', '91-其他人员', ?, '10-城镇失业',
               ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?::numeric, ?::numeric, ?, ?, ?, ?)""",
            ace, r.aac002, r.aac003, r.aae00p, r.atc90a, r.atc90c, r.atc90d, r.atc905, r.aae002,
            r.aaa401, r.aaa100, r.aaa203, r.aaa301, r.aae005, r.atc90ax, r.atc90b,
            r.atb90a, r.atb90b ?: "0", r.atb930, r.atb900, r.aab001, r.aab004 ?: "单位名")
        return mapOf("ace100" to ace)
    }

    /** 审核台: 合同行 + 待审 TB91 行(20) */
    @GetMapping("/audit-row/{atc900}")
    fun auditRow(@PathVariable atc900: Long): Map<String, Any?> {
        val tc90 = jdbcTemplate.queryForMap(
            "SELECT atc900, aac003, atb90r, aac001 FROM tc90 WHERE atc900=?", atc900)
        val tb91 = jdbcTemplate.queryForMap(
            "SELECT atb910, atb91z, atb911, atb912 FROM tb91 WHERE atb91z=? AND atb911='20' " +
                "ORDER BY atb910 DESC LIMIT 1", atc900)
        return mapOf("tc90" to tc90, "tb91" to tb91)
    }

    /** 工资核对: 编制 TC93 当月行(从合同带单位/结算单元/经办信息) + 核对批次行 */
    data class SalarySetup(
        val atc900: Long, val ym: Long, val atc93aa: String, val atb930: Long,
    )

    @PostMapping("/salary-setup")
    fun salarySetup(@RequestBody r: SalarySetup): Map<String, Any?> {
        val exists = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM tc93 WHERE atc900=? AND atc931=?",
            Int::class.java, r.atc900, r.ym) ?: 0
        if (exists == 0) {
            jdbcTemplate.update(
                """INSERT INTO tc93 (atc900, atc930, baz001, baz002, atb900, aab001, aab004, atb930,
                   atb931, aac001, aac002, aac003, atc931, atc932, atc937, aae001, atc93aa, atc93x,
                   atc93p, aae011)
                   SELECT ?, nextval('seq_tc93_atc930'), nextval('seq_sys_baz001'), nextval('seq_sys_baz002'),
                   t.atb900, t.aab001, t.aab004, t.atb930, t.atc90x, t.aac001, t.aac002, t.aac003,
                   ?, ?, '1', ?, ?::numeric, ?::numeric, '1', 'jiaq' FROM tc90 t WHERE t.atc900=?""",
                r.atc900, r.ym, r.ym, r.ym, r.atc93aa, r.atc93aa, r.atc900)
        }
        val atc930 = jdbcTemplate.queryForObject(
            "SELECT atc930 FROM tc93 WHERE atc900=? AND atc931=? LIMIT 1",
            Long::class.java, r.atc900, r.ym)!!
        val aac002 = jdbcTemplate.queryForObject(
            "SELECT aac002 FROM tc93 WHERE atc930=?", String::class.java, atc930)
        val aac003 = jdbcTemplate.queryForObject(
            "SELECT aac003 FROM tc93 WHERE atc930=?", String::class.java, atc930)
        val ace = nextval("seq_ce10_ace100")
        val bid = jdbcTemplate.queryForObject("SELECT COALESCE(MAX(id)+1,1) FROM tc93_bp1", Long::class.java)
        jdbcTemplate.update(
            """INSERT INTO tc93_bp1 (id, ace100, aac002, aac003, atc93aa, atc93k, atc93m)
               VALUES (?, ?, ?, ?, ?, '0', '0')""",
            bid, ace, aac002, aac003, r.atc93aa)
        return mapOf("ace100" to ace, "atc930" to atc930)
    }

    /** 到账批次行(tb97_bp) */
    data class ArrivalRow(val abp001: String, val abp002: String, val abp005: String)

    @PostMapping("/arrival-batch")
    fun arrivalBatch(@RequestBody r: ArrivalRow): Map<String, Any?> {
        val ace = nextval("seq_ce10_ace100")
        jdbcTemplate.update(
            "INSERT INTO tb97_bp (id, ace100, abp001, abp002, abp005) VALUES (nextval('seq_tb97_bp_id'), ?, ?, ?, ?)",
            ace, r.abp001, r.abp002, r.abp005)
        return mapOf("ace100" to ace)
    }

    /** 发票批次行(tb99_bp): 固定借发票/普票 + 人事代理服务费一项 */
    data class InvoiceRow(val abp001: String, val abp004: String)

    @PostMapping("/invoice-batch")
    fun invoiceBatch(@RequestBody r: InvoiceRow): Map<String, Any?> {
        val ace = nextval("seq_ce10_ace100")
        jdbcTemplate.update(
            """INSERT INTO tb99_bp (id, ace100, aaa030, aaa031, abp001, abp002, abp003, abp004)
               VALUES (nextval('seq_tb99_id'), ?, 1, '工作台', ?, '1-借发票', '1-普票', ?)""",
            ace, r.abp001, r.abp004)
        return mapOf("ace100" to ace)
    }

    /** 续签批次行(tc90_bp3), 合同要素从生效合同带出 */
    data class RenewRow(
        val aac002: String, val aac003: String, val atc90c: String, val atc90d: String,
    )

    @PostMapping("/renew-batch")
    fun renewBatch(@RequestBody r: RenewRow): Map<String, Any?> {
        val ace = nextval("seq_ce10_ace100")
        jdbcTemplate.update(
            """INSERT INTO tc90_bp3 (id, ace100, aac002, aac003, atb930, aab001, aab004, aab004_1,
               aae00b, atc908, atc90a, atc90c, atc90d)
               SELECT nextval('seq_tc90_bp3_id'), ?, ?, ?, t.atb930, t.aab001, t.aab004,
               '续签', 'R1', '续签岗', '1-固定期限', ?, ? FROM tc90 t
               WHERE t.aac002=? AND t.aac003=? AND t.atb90r='3' LIMIT 1""",
            ace, r.aac002, r.aac003, r.atc90c, r.atc90d, r.aac002, r.aac003)
        return mapOf("ace100" to ace)
    }

    // ---------- 查询面(只读, 白名单列, LIMIT 封顶) ----------

    /** 待审队列: 新签审核流水 TB91(类型 20/标志 2 待审) + 合同要素 */
    @GetMapping("/audit-queue")
    fun auditQueue(): List<Map<String, Any?>> = jdbcTemplate.queryForList(
        """SELECT b.atb910, b.atb91z AS atc900, b.aae036,
           t.aac002, t.aac003, t.atc90a, t.atc90c, t.atc90d, t.atc905, t.aab004, t.atb90r
           FROM tb91 b JOIN tc90 t ON t.atc900 = b.atb91z
           WHERE b.atb911='20' AND b.atb912='2'
           ORDER BY b.atb910 DESC LIMIT 200""")

    /** 批次查询: 按白名单表序探测 ace100 所属批次表, 返回逐行 ace10a(3过/2拒/1完成)+ace10b(原因) */
    @GetMapping("/batch/{ace100}")
    fun batch(@PathVariable ace100: Long): Map<String, Any?> {
        val specs = listOf(
            "tc90_bp" to listOf("aac002", "aac003", "atc90a", "atc90c", "atc90d", "atc905", "ace10a", "ace10b"),
            "tc90_bp1" to listOf("aac002", "aac003", "atc90i", "aae003", "atc90k", "ace10a", "ace10b"),
            "tc90_bp3" to listOf("aac002", "aac003", "atc90a", "atc90c", "atc90d", "ace10a", "ace10b"),
            "tc93_bp1" to listOf("aac002", "aac003", "atc93aa", "ace10a", "ace10b"),
            "tb97_bp" to listOf("abp001", "abp002", "abp005", "ace10a", "ace10b"),
            "tb99_bp" to listOf("abp001", "abp002", "abp004", "ace10a", "ace10b"),
        )
        for ((table, cols) in specs) {
            val rows = jdbcTemplate.queryForList(
                "SELECT id, ${cols.joinToString(", ")} FROM $table WHERE ace100=? ORDER BY id", ace100)
            if (rows.isNotEmpty()) {
                val header = runCatching {
                    jdbcTemplate.queryForMap(
                        "SELECT aae019, aae036 FROM ce10 WHERE ace100=?", ace100)
                }.getOrNull()
                return mapOf("table" to table, "header" to header, "rows" to rows)
            }
        }
        return mapOf("table" to null, "header" to null, "rows" to emptyList<Map<String, Any?>>())
    }

    /** 合同搜索: 身份证/姓名模糊或合同号精确, 供各表单选择器 */
    @GetMapping("/contract-search")
    fun contractSearch(@RequestParam q: String): List<Map<String, Any?>> {
        val like = "%$q%"
        return jdbcTemplate.queryForList(
            """SELECT atc900, aac002, aac003, atb90r, atc90a, atc90c, atc90d, atc905,
               aab001, aab004, atb930, atb900 FROM tc90
               WHERE aac002 LIKE ? OR aac003 LIKE ? OR atc900::text = ?
               ORDER BY atc900 DESC LIMIT 50""", like, like, q)
    }

    /** 单位清单( distinct 单位编号+名称, 附协议数) */
    @GetMapping("/units")
    fun units(): List<Map<String, Any?>> = jdbcTemplate.queryForList(
        """SELECT aab001, aab004, count(*) AS agreements FROM tb90
           GROUP BY aab001, aab004 ORDER BY aab001 LIMIT 2000""")

    /** 派遣协议(TB90, 协议号倒序); 可按单位过滤, 不带参返回全部(带单位名做标签) */
    @GetMapping("/agreements")
    fun agreements(@RequestParam(required = false) aab001: Long?): List<Map<String, Any?>> =
        if (aab001 == null) jdbcTemplate.queryForList(
            "SELECT atb900, aab001, aab004 FROM tb90 ORDER BY atb900 DESC LIMIT 2000")
        else jdbcTemplate.queryForList(
            "SELECT atb900, aab001, aab004 FROM tb90 WHERE aab001=? ORDER BY atb900 DESC", aab001)

    /** 参保属地(区划): 从存量数据反向提取 12 位代码→名称映射(库内无区划字典表, 旧系统为手敲文本) */
    @GetMapping("/regions")
    fun regions(): List<Map<String, Any?>> {
        val raw = jdbcTemplate.queryForList(
            """SELECT DISTINCT aaa100 FROM tc90_bp WHERE aaa100 ~ '^[0-9]{12}' LIMIT 200""") +
            jdbcTemplate.queryForList(
                """SELECT DISTINCT aab301::text AS aaa100 FROM tb90
                   WHERE aab301 IS NOT NULL AND aab301 ~ '^[0-9]{12}' LIMIT 200""")
        val map = LinkedHashMap<String, String?>()
        for (row in raw) {
            val v = row["aaa100"]?.toString() ?: continue
            val code = v.substring(0, 12)
            val name = v.removePrefix(code).trimStart('-', '/', '　').ifEmpty { null }
            map.putIfAbsent(code, name)
        }
        return map.map { (code, name) -> mapOf("code" to code, "name" to (name ?: code)) }
    }

    /** 结算单元(TB93), 可按单位过滤; atb900=绑定的派遣协议(全量填充, 联动反填用) */
    @GetMapping("/settle-units")
    fun settleUnits(@RequestParam(required = false) aab001: Long?): List<Map<String, Any?>> =
        if (aab001 == null) jdbcTemplate.queryForList(
            "SELECT atb930, atb931, aab001, atb900 FROM tb93 ORDER BY atb930 LIMIT 2000")
        else jdbcTemplate.queryForList(
            "SELECT atb930, atb931, aab001, atb900 FROM tb93 WHERE aab001=? ORDER BY atb930", aab001)

    /** 经办人(SYS_ORG_USER_UNIT) */
    @GetMapping("/opers")
    fun opers(): List<Map<String, Any?>> = jdbcTemplate.queryForList(
        """SELECT account, aae011, aae019, aae017, aae020, aae022
           FROM sys_org_user_unit ORDER BY aae011""")

    /** 码表(AA10): 白名单类别 → "代码-名称"串(库内存储格式, 如 1-派遣) */
    @GetMapping("/codes")
    fun codes(@RequestParam category: String): List<Map<String, Any?>> {
        val allowed = setOf("AAE00P", "ATC90A", "ATC90K", "AAC005", "AAC009",
            "AAC011", "AAC012", "ATC909", "AAC013")
        if (category !in allowed) return emptyList()
        return jdbcTemplate.queryForList(
            """SELECT aaa102, aaa102||'-'||aaa103 AS item FROM aa10
               WHERE aaa100=? ORDER BY aaa102""", category)
    }

    /** 最近到账(TB97) */
    @GetMapping("/arrivals")
    fun arrivals(): List<Map<String, Any?>> = jdbcTemplate.queryForList(
        """SELECT atb970, atb974, aab004, atb931, atb971, atb972, atb973 FROM tb97
           ORDER BY atb970 DESC LIMIT 50""")

    /** 最近发票(TB99) */
    @GetMapping("/invoices")
    fun invoices(): List<Map<String, Any?>> = jdbcTemplate.queryForList(
        """SELECT atb990, atb99y, atb994, aab004, atb993 FROM tb99
           ORDER BY atb990 DESC LIMIT 50""")

    /** 终止批次行(tc90_bp1), 单位要素从合同带出 */
    data class TerminateRow(
        val aac002: String, val aac003: String, val atc90i: String, val atc90av: String,
    )

    @PostMapping("/terminate-batch")
    fun terminateBatch(@RequestBody r: TerminateRow): Map<String, Any?> {
        val ace = nextval("seq_ce10_ace100")
        val bid = jdbcTemplate.queryForObject("SELECT COALESCE(MAX(id)+1,1) FROM tc90_bp1", Long::class.java)
        jdbcTemplate.update(
            """INSERT INTO tc90_bp1 (id, ace100, aac002, aac003, atb930, aab001, aab004,
               atc90i, aae003, atc90k, atc90l, atc90n, atc90av)
               SELECT ?, ?, ?, ?, t.atb930, t.aab001, t.aab004, ?, ?::numeric,
               '09-职工个人原因', '0-否', '0-否', ?::numeric FROM tc90 t
               WHERE t.aac002=? AND t.aac003=? ORDER BY t.atc900 DESC LIMIT 1""",
            bid, ace, r.aac002, r.aac003, r.atc90i, r.atc90av.substring(0, 6),
            r.atc90av, r.aac002, r.aac003)
        return mapOf("ace100" to ace)
    }

    // ---------- 五险一金报盘名册 ----------
    // 列规范 = 东软名册网格(社保 tc9wStore_grid / 医保 tc9w1Store_grid, td/h/y 与 td/c/ay);
    // 旧 TC9W/TC9W1 工作表新库为空(报增导入包生产即 INVALID 未移植), 名册从合同+人口属性推导。

    private val rosterKinds = setOf("social", "medical")
    private val rosterFlows = setOf("add", "cut")

    /** 名册数据: 返回 (列[键,标签], 行)。异动类型/证件类型等常量列在 Kotlin 侧补。 */
    private fun rosterData(kind: String, flow: String, ym: String,
                           aab001: Long?): Pair<List<Pair<String, String>>, List<Map<String, Any?>>> {
        if (kind !in rosterKinds || flow !in rosterFlows || !Regex("^\\d{6}$").matches(ym)) {
            return listOf<Pair<String, String>>() to emptyList()
        }
        val unitFilter = if (aab001 != null) " AND t.aab001=$aab001" else ""
        val colsAndSql: Pair<List<Pair<String, String>>, String> = when (kind to flow) {
            "social" to "add" -> listOf(
                "ydlx" to "异动类型", "aac002" to "身份证号码", "aac003" to "姓名",
                "xb" to "性别", "mz" to "民族", "hjxz" to "户籍性质", "ygxs" to "用工形式",
                "sbgz" to "申报缴费工资(月)", "bcqdrq" to "本次劳动合同起始时间", "cbsj" to "参保时间",
                "gw" to "岗位", "lxdh" to "联系电话", "cscd" to "社保参保属地", "aab004" to "单位名称",
            ) to ("""SELECT t.aac002, t.aac003, a.aac004 AS xb,
                COALESCE(NULLIF(t.aac005,''), a.aac005) AS mz, t.aac009 AS hjxz,
                t.aae00p AS ygxs, t.atc905 AS sbgz, t.atc90c AS bcqdrq, t.atc90c AS cbsj,
                t.atc908 AS gw, t.aae005 AS lxdh, t.aaa100 AS cscd, t.aab004
                FROM tc90 t LEFT JOIN ac01 a ON a.aac002 = t.aac002
                WHERE t.atb90r='3' AND to_char(t.atc90c,'YYYYMM')='$ym'$unitFilter
                ORDER BY t.atc900""")
            "social" to "cut" -> listOf(
                "ydlx" to "异动类型", "aac002" to "身份证号码", "aac003" to "姓名",
                "jzyy" to "减少原因", "jssj" to "减少时间", "jfjzny" to "缴费截止年月",
                "aab004" to "单位名称",
            ) to ("""SELECT t.aac002, t.aac003, t.atc90k AS jzyy, t.atc90i AS jssj,
                t.atc90av AS jfjzny, t.aab004
                FROM tc90 t
                WHERE t.atc90i IS NOT NULL AND to_char(t.atc90i,'YYYYMM')='$ym'$unitFilter
                ORDER BY t.atc900""")
            "medical" to "add" -> listOf(
                "ybny" to "医保年月", "ydlx" to "异动类型", "sfzlx" to "身份证件类型",
                "aac002" to "身份证号码", "aac003" to "姓名", "csrq" to "出生日期", "xb" to "性别",
                "mz" to "民族", "cjgzrq" to "参加工作日期", "hjxz" to "户口性质",
                "lxdh" to "联系电话", "zjyxq" to "证件有效期", "gz" to "工资",
                "sbjs" to "申报缴费基数", "cscd" to "参保属地", "aab004" to "单位名称",
            ) to ("""SELECT t.aac002, t.aac003, a.aac006 AS csrq, a.aac004 AS xb,
                COALESCE(NULLIF(t.aac005,''), a.aac005) AS mz, t.atc90c AS cjgzrq,
                t.aac009 AS hjxz, t.aae005 AS lxdh, t.atc90ax AS zjyxq, t.atc905 AS gz,
                t.aaa302 AS sbjs, COALESCE(t.aaa110, t.aaa100) AS cscd, t.aab004
                FROM tc90 t LEFT JOIN ac01 a ON a.aac002 = t.aac002
                WHERE t.atb90r='3' AND to_char(t.atc90c,'YYYYMM')='$ym'$unitFilter
                ORDER BY t.atc900""")
            else -> listOf(
                "ybny" to "医保年月", "ydlx" to "异动类型", "aac002" to "身份证号码",
                "aac003" to "姓名", "bgyy" to "变更原因", "bgsj" to "变更时间",
                "jfjzny" to "缴费截止年月", "aab004" to "单位名称",
            ) to ("""SELECT t.aac002, t.aac003, t.atc90k AS bgyy, t.atc90i AS bgsj,
                t.atc90av AS jfjzny, t.aab004
                FROM tc90 t
                WHERE t.atc90i IS NOT NULL AND to_char(t.atc90i,'YYYYMM')='$ym'$unitFilter
                ORDER BY t.atc900""")
        }
        val (cols, sql) = colsAndSql
        val raw = jdbcTemplate.queryForList(sql)
        val constCol: Map<String, String> = when (kind to flow) {
            "social" to "add" -> mapOf("ydlx" to "新参保")
            "social" to "cut" -> mapOf("ydlx" to "减少")
            "medical" to "add" -> mapOf("ydlx" to "新参保", "sfzlx" to "居民身份证")
            else -> mapOf("ydlx" to "停保")
        }
        val rows = raw.mapIndexed { i, r ->
            val out = LinkedHashMap<String, Any?>()
            out["xh"] = i + 1
            for ((k, _) in cols) {
                out[k] = if (constCol.containsKey(k)) constCol[k] else r[k]
            }
            out
        }
        val fullCols = listOf("xh" to "序号") + cols
        return fullCols to rows
    }

    /** 名册查询(JSON): 供页面表格 */
    @GetMapping("/roster")
    fun roster(@RequestParam kind: String, @RequestParam flow: String, @RequestParam ym: String,
               @RequestParam(required = false) aab001: Long?): Map<String, Any?> {
        val (cols, rows) = rosterData(kind, flow, ym, aab001)
        return mapOf(
            "columns" to cols.map { mapOf("key" to it.first, "label" to it.second) },
            "rows" to rows, "count" to rows.size)
    }

    /** 报盘行: 与名册同口径(增=当月合同开始且生效; 减=当月合同终止), 供模板导出 */
    private fun baopanRows(flow: String, ym: String, aab001: Long?): List<BaopanExporter.Row> {
        if (flow !in rosterFlows || !Regex("^\\d{6}$").matches(ym)) return emptyList()
        val unitFilter = if (aab001 != null) " AND t.aab001=$aab001" else ""
        return if (flow == "add") jdbcTemplate.query(
            """SELECT t.aac002, t.aac003, t.aac005 AS mz, t.aac009 AS hjxz, t.aac012 AS aac012,
               t.atc905 AS salary, t.atc90c AS cstart, t.aae005 AS phone, t.atc902 AS addr,
               t.aac183 AS zip, t.aaa100 AS locs, t.aaa110 AS locm, t.aaa601 AS medym,
               t.aab001, t.aab004
               FROM tc90 t
               WHERE t.atb90r='3' AND to_char(t.atc90c,'YYYYMM')='$ym'$unitFilter
               ORDER BY t.atc900""") { rs, _ ->
            BaopanExporter.Row(
                aac002 = rs.getString("aac002") ?: "", aac003 = rs.getString("aac003") ?: "",
                mz = rs.getString("mz"), hjxz = rs.getString("hjxz"), rySf = rs.getString("aac012"),
                salary = rs.getBigDecimal("salary"),
                start = rs.getDate("cstart")?.toLocalDate(),
                phone = rs.getString("phone"), addr = rs.getString("addr"), zip = rs.getString("zip"),
                locSocial = rs.getString("locs"), locMedical = rs.getString("locm"),
                medYm = rs.getObject("medym")?.let { (it as Number).toLong() },
                aab001 = rs.getObject("aab001")?.let { (it as Number).toLong() },
                aab004 = rs.getString("aab004"))
        } else jdbcTemplate.query(
            """SELECT t.aac002, t.aac003, t.atc90k AS reason, t.atc90i AS cend,
               t.aaa110 AS locm, t.aab001, t.aab004
               FROM tc90 t
               WHERE t.atc90i IS NOT NULL AND to_char(t.atc90i,'YYYYMM')='$ym'$unitFilter
               ORDER BY t.atc900""") { rs, _ ->
            BaopanExporter.Row(
                aac002 = rs.getString("aac002") ?: "", aac003 = rs.getString("aac003") ?: "",
                end = rs.getDate("cend")?.toLocalDate(), reason = rs.getString("reason"),
                locMedical = rs.getString("locm"),
                aab001 = rs.getObject("aab001")?.let { (it as Number).toLong() },
                aab004 = rs.getString("aab004"))
        }
    }

    /** 名册报盘导出: 以 resources/baopan 局方模板为底稿 POI 生成。
     *  社保增/减 = 单文件 .xlsx(企业职工批量参保登记 / 单位职工停保模板);
     *  医保增/减 = 按医保参保属地(aaa110)拆 省(2299)/市 两文件打包 .zip。 */
    @GetMapping("/roster-export")
    fun rosterExport(@RequestParam kind: String, @RequestParam flow: String,
                     @RequestParam ym: String, @RequestParam(required = false) aab001: Long?,
                     response: jakarta.servlet.http.HttpServletResponse) {
        val rows = baopanRows(flow, ym, aab001)
        val xlsx = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
        when {
            kind == "social" && flow == "add" -> sendFile(response, "社保增员_$ym.xlsx", xlsx,
                baopan.socialAdd(rows, aab001?.toString(), rows.firstOrNull()?.aab004))
            kind == "social" -> sendFile(response, "社保减员_$ym.xlsx", xlsx,
                baopan.socialCut(rows))
            flow == "add" -> sendFile(response, "医保增员_$ym.zip", "application/zip",
                BaopanExporter.zipFiles(baopan.medicalAdd(rows, ym)))
            else -> sendFile(response, "医保减员_$ym.zip", "application/zip",
                BaopanExporter.zipFiles(baopan.medicalCut(rows, ym)))
        }
    }

    private fun sendFile(response: jakarta.servlet.http.HttpServletResponse,
                         filename: String, contentType: String, bytes: ByteArray) {
        response.contentType = contentType
        response.setHeader("Content-Disposition",
            "attachment; filename*=UTF-8''" + java.net.URLEncoder.encode(filename, "UTF-8"))
        response.outputStream.use { it.write(bytes) }
    }
}
