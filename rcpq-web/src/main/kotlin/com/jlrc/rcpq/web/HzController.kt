package com.jlrc.rcpq.web

import com.jlrc.rcpq.service.td.TdckCases
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

/**
 * 社保报增回盘(M2, 仿东软 tdck: 报增查询→结果→成果确认→延续至下月→回盘)。
 *
 * TC9W(名册工作表)/TC9W_BP(回盘批次表) 旧由"报增名册导入"(生产 INVALID 死包, 不移植)
 * 与"申报结果导入"填充; 新系统名册由合同推导推送(push), 成果先支持手工确认
 * (报增成功=写 TC9W_BP aaa90w='1'/报增失败异议='2'), 校验与提交复用已移植过程
 * /proc/PKG_保险人员增减.PRC_社保异动回盘校验|提交(前端直调)。
 * 局方回盘文件导入留 M5(TC9W_HZ 系列)。
 */
@RestController
@RequestMapping("/wb/hz")
class HzController(private val jdbc: JdbcTemplate, private val tdck: TdckCases,
                   private val audit: AuditLog) {

    /** 名册推送请求: 年月 + 险种(social/medical/smedical/housingfund/sgongshang) + 可选单位/属地过滤 + 经办人 */
    data class PushReq(val ym: String, val kind: String = "social",
                       val aab001: Long? = null, val aaa110: String? = null, val aae011: String = "")

    /** DATE 列经 PG JDBC 返回 java.sql.Date(Timestamp 不可强转) — 两型归一 LocalDate */
    private fun toDate(v: Any?): java.time.LocalDate? = when (v) {
        is java.sql.Timestamp -> v.toLocalDateTime().toLocalDate()
        is java.sql.Date -> v.toLocalDate()
        else -> null
    }

    /** LocalDate → yyyymmdd */
    private fun ymd8(v: Any?): String? = toDate(v)
        ?.format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd"))

    /** 名册推送: 当月合同开始且生效(atb90r='3') → TC9W 工作表。
     *  同合同同月旧行置 atc9w8='9' 作废(逻辑删除, 不物理删; 重推可追溯),
     *  查询面一律排除 '9'; atc9w1='1' 报增, 新行 atc9w8='1' 待校验。 */
    @PostMapping("/push")
    @Transactional
    fun push(@RequestBody r: PushReq): Map<String, Any?> {
        audit.log(r.aae011, "wb:hz/push", "ym=${r.ym}", r)
        if (!Regex("^\\d{6}$").matches(r.ym)) return mapOf("appCode" to -1, "errMsg" to "年月格式不正确")
        // ⭐回盘锁(P0 修正 2026-11-20): 东软 PRC_社保人员增减 L901-912 忠实兜底 — 目标名单表该年月
        // 已存在 atc9w8='4'(已回盘办结)行 → 拒绝重推; 带单位过滤则锁限该单位(推送粒度=月+单位,
        // 旧锁粒度=户头+区划, 随推送作用域对齐)。此前仅 2001a43 前端置灰为防线, 可被筛选/未加载/直调绕过。
        val ymN = r.ym.toLong()
        val lockTable = when (r.kind) {
            "medical" -> "tc9w1"; "housingfund" -> "tc9u"; "smedical" -> "tc9w2"
            "sgongshang" -> "tc9w3"; "mgongshang" -> "tc9w4"; else -> "tc9w"
        }
        val lockCol = if (r.kind == "housingfund") "atc9u8" else "atc9w8"
        val n4 = jdbc.queryForObject(
            "SELECT count(*) FROM $lockTable WHERE aae002=? AND $lockCol='4'" +
                (if (r.aab001 != null) " AND aab001=${r.aab001}" else ""), Long::class.java, ymN) ?: 0L
        if (n4 > 0) return mapOf("appCode" to -1,
            "errMsg" to "当月数据已经回盘，不能重新生成，请点击查询查看历史数据")
        val unitFilter = if (r.aab001 != null) " AND t.aab001=${r.aab001}" else ""
        val medFilter = if (r.kind == "medical") " AND t.aaa402='1' AND t.aaa110 IS NOT NULL" else ""
        val hfFilter = if (r.kind == "housingfund") " AND t.aaa407='1'" else ""
        val smFilter = if (r.kind == "smedical") " AND t.aaa402='1' AND t.aaa110 IS NOT NULL AND t.atc90pa IS NULL" else ""
        // 省工伤: 忠实人员增减报增候选过滤(L7520-7536), atb90r='5' 终止未生效也入;
        // 旧生成按 PRM_AAA110 属地参数范围 — 推送侧同款, 属地必传(防全库扩散)
        val gsFilter = if (r.kind == "sgongshang" || r.kind == "mgongshang") {
            val loc = r.aaa110?.replace("'", "''")
                ?: return mapOf("appCode" to -1, "errMsg" to "工伤推送须带 aaa110 属地过滤")
            // 市工伤候选=AAA903<=ym AND ATC90PB NULL(L8162-8166); 省工伤另有变更入队分支
            val cand = if (r.kind == "sgongshang")
                "((t.aaa903 <= ${r.ym.toLong()} AND t.atc90pb IS NULL)" +
                    " OR (t.aae002 = ${r.ym.toLong()} AND strpos(t.atc9wc, '12') > 0))"
            else "t.aaa903 <= ${r.ym.toLong()} AND t.atc90pb IS NULL"
            """ AND t.aaa130='$loc'
               AND (t.atb90r='3' OR (t.atb90r='5' AND t.aaa902 > ${r.ym.toLong()}))
               AND $cand
               AND t.aaa404='1' AND t.aae00p IN ('1','5','7')"""
        } else ""
        // 基础行集: 险种分流 WHERE — 工伤只走忠实候选分支(当月合同分支会漏入 aaa130 NULL 行)
        val where = if (r.kind == "sgongshang" || r.kind == "mgongshang")
            "t.atb90r IN ('3','5') AND t.aaa404='1'" + gsFilter
        else
            "t.atb90r='3' AND to_char(t.atc90c,'YYYYMM')='${r.ym}'$unitFilter$medFilter$hfFilter$smFilter"
        val rows = jdbc.queryForList(
            """SELECT t.atc900, t.aac002, t.aac003, t.aab001, t.aab004,
               u.aaa030, u.aaa031, t.aaa100, t.aaa101, t.aaa110, t.aaa111,
               t.aac001, t.atb900, t.aaa004, t.atb90s, t.aaa303, t.aaa120, t.aaa121,
               t.aae005, t.atc902, t.aac004, t.aac006, t.zynum, t.jynum,
               t.aac005, t.aac009, t.aac012, t.aaa005, t.atc905, t.atc90ax, t.atc90b, t.atc90pa, t.atc90c,
               t.aaa130, t.aaa131, t.atc90pb, t.aaa902, t.aaa903
               FROM tc90 t LEFT JOIN ab01 u ON u.aab001 = t.aab001
               WHERE $where
               ORDER BY t.atc900""")
        if (r.kind == "medical") return pushMedical(r, rows, ymN)
        if (r.kind == "housingfund") return pushHousingFund(r, rows, ymN)
        if (r.kind == "smedical") return pushSMedical(r, rows, ymN)
        if (r.kind == "sgongshang") return pushGongshang(r, rows, ymN, "tc9w3")
        if (r.kind == "mgongshang") return pushGongshang(r, rows, ymN, "tc9w4")
        var pushed = 0
        val skipped = mutableListOf<String>()
        for (row in rows) {
            val aaa030 = (row["aaa030"] as Number?)?.toLong()
            if (aaa030 == null) { skipped.add("${row["aac003"]}(无险金户头)"); continue }
            jdbc.update(
                // 重推清理收窄(P0 修正): 仅作废待校验行(atc9w8='1'), 忠实旧预删 ATC9W8='1' —
                // '4' 已回盘行不可被作废(旧 `<>'9'` 会把回盘历史一并置 9)
                "UPDATE tc9w SET atc9w8='9' WHERE aae002=? AND atc900=? AND atc9w1='1' AND atc9w8='1'",
                ymN, (row["atc900"] as Number).toLong())
            jdbc.update(
                """INSERT INTO tc9w (atc9w0, aae002, atc900, aab001, aab004, atc9w1,
                   aaa030, aaa031, aaa100, aaa101, abp001, aac002, aac003, atc9w8,
                   atc9w2, abpzzz, aaa90x, aae011, aae036)
                   VALUES (nextval('seq_tc9w_atc9w0'), ?, ?, ?, ?, '1', ?, ?, ?, ?,
                   '居民身份证（户口簿）', ?, ?, '1', '0', '0', '90', ?, now())""",
                ymN, (row["atc900"] as Number).toLong(),
                (row["aab001"] as Number?)?.toLong(), row["aab004"],
                aaa030, row["aaa031"], row["aaa100"], row["aaa101"],
                row["aac002"], row["aac003"], r.aae011)
            pushed++
        }
        return mapOf("appCode" to 1, "pushed" to pushed, "skipped" to skipped,
            "msg" to "推送 $pushed 人" + (if (skipped.isEmpty()) "" else "; 跳过 ${skipped.size}(险金户头缺失)"))
    }

    /** 医保名册推送: 参保合同(aaa402='1')且医保属地(aaa110)非空 → TC9W1;
     *  无属地行跳过(校验按 aaa110 匹配), 其余语义与社保同款(atc9w8 作废/重推)。 */
    private fun pushMedical(r: PushReq, rows: List<Map<String, Any?>>, ymN: Long): Map<String, Any?> {
        var pushed = 0
        val skipped = mutableListOf<String>()
        for (row in rows) {
            val aaa110 = row["aaa110"] as String?
            val aaa030 = (row["aaa030"] as Number?)?.toLong()
            if (aaa030 == null || aaa110.isNullOrBlank()) {
                skipped.add("${row["aac003"]}(无户头/医保属地)")
                continue
            }
            jdbc.update(
                "UPDATE tc9w1 SET atc9w8='9' WHERE aae002=? AND atc900=? AND atc9w1='1' AND atc9w8='1'",
                ymN, (row["atc900"] as Number).toLong())
            jdbc.update(
                """INSERT INTO tc9w1 (atc9w0, aae002, atc900, aab001, aab004, atc9w1,
                   aaa030, aaa031, aaa100, aaa101, abp001, aac002, aac003, atc9w8, aae011, aae036)
                   VALUES (nextval('seq_tc9w1_atc9w0'), ?, ?, ?, ?, '1', ?, ?, ?, ?,
                   '居民身份证（户口簿）', ?, ?, '1', ?, now())""",
                ymN, (row["atc900"] as Number).toLong(),
                (row["aab001"] as Number?)?.toLong(), row["aab004"],
                aaa030, row["aaa031"], row["aaa100"], row["aaa101"],
                row["aac002"], row["aac003"], r.aae011)
            pushed++
        }
        return mapOf("appCode" to 1, "pushed" to pushed, "skipped" to skipped,
            "msg" to "推送 $pushed 人" + (if (skipped.isEmpty()) "" else "; 跳过 ${skipped.size}(无户头/医保属地)"))
    }

    /** 公积金名册推送: 在保公积金合同(aaa407='1') → TC9U(开户行, atc9u1='1')。
     *  字段赋值对齐 PRC_省公积金开户赋值(保险人员增减 L5704-5772): 基数=aaa303, 比例=atb90s
     *  按':'拆分, 月缴存额=round(基数×比例/100), 证件类型'01'/账户状态'01'/学历'4'/职业'21' 等默认。 */
    private fun pushHousingFund(r: PushReq, rows: List<Map<String, Any?>>, ymN: Long): Map<String, Any?> {
        val user = jdbc.query(
            "SELECT aae011, aae017, aae019, aae020, aae022 FROM sys_org_user_unit WHERE aae011=?",
            { rs, _ -> arrayOf(rs.getString("aae011"), rs.getString("aae017"), rs.getString("aae019"),
                rs.getString("aae020"), rs.getString("aae022")) },
            r.aae011).firstOrNull()
        var pushed = 0
        val skipped = mutableListOf<String>()
        for (row in rows) {
            val aaa030 = (row["aaa030"] as Number?)?.toLong()
            if (aaa030 == null) { skipped.add("${row["aac003"]}(无险金户头)"); continue }
            val atb90s = row["atb90s"]?.toString()
            val dwbl = atb90s?.substringBefore(':')?.trim()?.toBigDecimalOrNull()
            val grbl = atb90s?.substringAfter(':', "")?.ifBlank { null }?.trim()?.toBigDecimalOrNull()
            val base = (row["aaa303"] as Number?)?.toString()?.toBigDecimal()
            fun amt(rate: java.math.BigDecimal?) =
                rate?.let { r -> base?.multiply(r)?.divide(java.math.BigDecimal(100), 0, java.math.RoundingMode.HALF_UP) }
            val d = amt(dwbl)
            val e = amt(grbl)
            val c = d?.add(e ?: java.math.BigDecimal.ZERO) ?: e
            val addr = row["atc902"]?.toString()?.let { a ->
                val bs = a.toByteArray(Charsets.UTF_8)
                if (bs.size <= 50) a else String(bs, 0, 50, Charsets.UTF_8) }
            // 出生 yyyymmdd(原 to_char(aac006,'yyyymmdd')); aac006 为 date/timestamp 列
            val birth = ymd8(row["aac006"])
            jdbc.update(
                "UPDATE tc9u SET atc9u8='9' WHERE aae002=? AND atc900=? AND atc9u1='1' AND atc9u8='1'",
                ymN, (row["atc900"] as Number).toLong())
            jdbc.update(
                """INSERT INTO tc9u (baz001, baz002, atc9u0, aae002, atc900, atb900, aab001, aab004,
                   atc9u1, aaa030, aaa031, aaa004, aac001, aac003, abp001, aac002,
                   abp008, abp009, abp00a, abp00b, abp00d, abp00e, abp00c, abp00f,
                   abp00x, abp00i, abp00j, abp00k, abp00l, abp004, abp005,
                   atc9u8, atc9u2, atc9uz, abpzzz, aaa100, aaa101, zynum, jynum,
                   aae011, aae017, aae019, aae020, aae022, aae036)
                   VALUES (nextval('seq_sys_baz001'), nextval('seq_sys_baz002'), nextval('seq_tc9u_atc9u0'),
                   ?, ?, ?, ?, ?, '1', ?, ?, ?, ?, ?, '01', ?, ?, ?, ?, ?, ?, ?, ?, '01',
                   '4', '21', ?, ?, '130000', ?, ?,
                   '1', '1', '10', '0', ?, ?, ?, ?, ?, ?, ?, ?, ?, now())""",
                ymN, (row["atc900"] as Number).toLong(),
                (row["atb900"] as Number?)?.toLong(), (row["aab001"] as Number?)?.toLong(), row["aab004"],
                aaa030, row["aaa031"], row["aaa004"], row["aac001"], row["aac003"], row["aac002"],
                row["aaa303"]?.toString(), row["aaa303"]?.toString(),
                dwbl?.toPlainString(), grbl?.toPlainString(),
                d?.toPlainString(), e?.toPlainString(), c?.toPlainString(),
                row["aae005"], addr,
                birth, row["aac004"],
                row["aaa120"], row["aaa121"], row["zynum"], row["jynum"],
                user?.get(0), user?.get(1), user?.get(2), user?.get(3), user?.get(4))
            pushed++
        }
        return mapOf("appCode" to 1, "pushed" to pushed, "skipped" to skipped,
            "msg" to "推送 $pushed 人" + (if (skipped.isEmpty()) "" else "; 跳过 ${skipped.size}(险金户头缺失)"))
    }

    /** 省医保名册推送: 在保未参保合同(aaa402='1', aaa110 非空, atc90pa NULL) → TC9W2 报增(atc9w1='1')。
     *  字段赋值对齐 PRC_省医保人员增减 报增分支(L2080-2184): 证件'1 居民身份证（户口簿）',
     *  性别/民族/户口取 AA10 拼接(民族/户口有兜底, 性别无), baz001=max+1 计数器(忠实,
     *  旧系统取号后被覆盖), atc9w2=1当月/2非当月(生成义, W0#2), aaa90y='90' 默认失败。 */
    private fun pushSMedical(r: PushReq, rows: List<Map<String, Any?>>, ymN: Long): Map<String, Any?> {
        val user = jdbc.query(
            "SELECT aae011, aae017, aae019, aae020, aae022 FROM sys_org_user_unit WHERE aae011=?",
            { rs, _ -> arrayOf(rs.getString("aae011"), rs.getString("aae017"), rs.getString("aae019"),
                rs.getString("aae020"), rs.getString("aae022")) },
            r.aae011).firstOrNull()
        fun aa10Label(col: String, code: String?): String? =
            code?.let { c -> jdbc.query(
                "SELECT aaa102 || ' ' || aaa103 FROM aa10 WHERE aaa102=? AND aaa100=?",
                { rs, _ -> rs.getString(1) }, c, col).firstOrNull() }
        fun bytes50(v: String?): String? = v?.let { a ->
            val bs = a.toByteArray(Charsets.UTF_8); if (bs.size <= 50) a else String(bs, 0, 50, Charsets.UTF_8) }
        fun ymd(col: Any?): String? = ymd8(col)
        val nowYm = java.time.YearMonth.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMM"))
        var pushed = 0
        val skipped = mutableListOf<String>()
        for (row in rows) {
            val aaa030 = (row["aaa030"] as Number?)?.toLong()
            if (aaa030 == null) { skipped.add("${row["aac003"]}(无险金户头)"); continue }
            var nXh = (jdbc.queryForObject(
                "SELECT coalesce(max(baz001),0) FROM tc9w2 WHERE atc9w1='1' AND aaa030=? AND aae002=?",
                Long::class.java, aaa030, ymN) ?: 0L) + 1
            val start = toDate(row["atc90c"])
            val isCurMonth = start != null &&
                java.time.YearMonth.from(start).format(java.time.format.DateTimeFormatter.ofPattern("yyyyMM")) == nowYm
            jdbc.update(
                "UPDATE tc9w2 SET atc9w8='9' WHERE aae002=? AND atc900=? AND atc9w1='1' AND atc9w8='1'",
                ymN, (row["atc900"] as Number).toLong())
            jdbc.update(
                """INSERT INTO tc9w2 (baz001, baz002, atc9w0, aae002, abpzzz, atc900, atb900, aab001,
                   aab004, aaa100, aaa101, aac001, atc9w1, aaa030, aaa031, abp001, aac002, aac003,
                   abp004, abp005, abp006, abp007, abp008, abp009, abp00a, abp00b, abp00c, abp00d,
                   abp00e, abp00f, abp00g, abp00h, abp00i, abp00j, abp00k, atc9w8, atc9w2,
                   aaa005, aaa302_1, aaa90y, zynum, jynum,
                   aae011, aae017, aae019, aae020, aae022, aae036)
                   VALUES (?, nextval('seq_sys_baz002'), nextval('seq_tc9w2_atc9w0'), ?, '0', ?, ?, ?,
                   ?, ?, ?, ?, '1', ?, ?, '1 居民身份证（户口簿）', ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?,
                   ?, '130000', ?, '4 其他', ?, ?, ?, '1', ?, ?, ?, '90', ?, ?,
                   ?, ?, ?, ?, ?, now())""",
                nXh, ymN, (row["atc900"] as Number).toLong(),
                (row["atb900"] as Number?)?.toLong(), (row["aab001"] as Number?)?.toLong(), row["aab004"],
                row["aaa110"], row["aaa111"], row["aac001"], aaa030, row["aaa031"],
                row["aac002"], row["aac003"],
                ymd(row["aac006"]), aa10Label("AAC004", row["aac004"]?.toString()),
                aa10Label("AAC005", row["aac005"]?.toString()) ?: "01 汉族",
                ymd(row["atc90c"]), "0 非公务员", "3 合同制",
                if (row["aac012"]?.toString() == "91") "1 干部" else "0 非干部", "0 非在编",
                aa10Label("AAC009", row["aac009"]?.toString()) ?: "11 本市城镇户口",
                row["aae005"], bytes50(row["atc902"]?.toString()),
                bytes50(row["atc902"]?.toString()),
                ymd(row["atc90ax"]), ymd(row["atc90b"]), row["atc905"]?.toString(),
                if (isCurMonth) "1" else "2",
                row["aaa005"], row["atc905"] as Number?,
                row["zynum"], row["jynum"],
                user?.get(0), user?.get(1), user?.get(2), user?.get(3), user?.get(4))
            pushed++
        }
        return mapOf("appCode" to 1, "pushed" to pushed, "skipped" to skipped,
            "msg" to "推送 $pushed 人" + (if (skipped.isEmpty()) "" else "; 跳过 ${skipped.size}(险金户头缺失)"))
    }

    /** 省工伤名册推送: 忠实人员增减报增候选 → TC9W3(atc9w1='1')。
     *  W0#3 裁决落地: 性别码 AA10 取不到 → **跳过该行并在 skipped 报姓名**(旧行为=整批 ORA-01403,
     *  候选中仅个别行异常); 民族/户口沿用旧兜底值。字段对齐报增分支 L7535-7808:
     *  baz001/002=序列原值(工伤不用计数器), atc9w0=seq_tc9w_atc9w0(与 TC9W 共享), abp00b=NULL,
     *  户口兜底='11 本地非农业户口（本地城镇）'。 */
    private fun pushGongshang(r: PushReq, rows: List<Map<String, Any?>>, ymN: Long, table: String): Map<String, Any?> {
        val user = jdbc.query(
            "SELECT aae011, aae017, aae019, aae020, aae022 FROM sys_org_user_unit WHERE aae011=?",
            { rs, _ -> arrayOf(rs.getString("aae011"), rs.getString("aae017"), rs.getString("aae019"),
                rs.getString("aae020"), rs.getString("aae022")) },
            r.aae011).firstOrNull()
        fun aa10Label(col: String, code: String?): String? =
            code?.let { c -> jdbc.query(
                "SELECT aaa102 || ' ' || aaa103 FROM aa10 WHERE aaa102=? AND aaa100=?",
                { rs, _ -> rs.getString(1) }, c, col).firstOrNull() }
        fun bytes50(v: String?): String? = v?.let { a ->
            val bs = a.toByteArray(Charsets.UTF_8); if (bs.size <= 50) a else String(bs, 0, 50, Charsets.UTF_8) }
        val nowYm = java.time.YearMonth.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMM"))
        var pushed = 0
        val skipped = mutableListOf<String>()
        for (row in rows) {
            val aaa030 = (row["aaa030"] as Number?)?.toLong()
            if (aaa030 == null) { skipped.add("${row["aac003"]}(无险金户头)"); continue }
            // W0#3: 性别码无兜底 → 行级兜底(跳行+报姓名), 不再毒整批
            val sex = aa10Label("AAC004", row["aac004"]?.toString())
            if (sex == null) { skipped.add("${row["aac003"]}(性别码异常, 行级兜底跳过)"); continue }
            val start = toDate(row["atc90c"])
            val isCurMonth = start != null &&
                java.time.YearMonth.from(start).format(java.time.format.DateTimeFormatter.ofPattern("yyyyMM")) == nowYm
            jdbc.update(
                "UPDATE $table SET atc9w8='9' WHERE aae002=? AND atc900=? AND atc9w1='1' AND atc9w8='1'",
                ymN, (row["atc900"] as Number).toLong())
            jdbc.update(
                """INSERT INTO $table (baz001, baz002, atc9w0, aae002, abpzzz, atc900, atb900, aab001,
                   aab004, aaa100, aaa101, aac001, atc9w1, aaa030, aaa031, abp001, aac002, aac003,
                   abp004, abp005, abp006, abp007, abp008, abp009, abp00a, abp00b, abp00c, abp00d,
                   abp00e, abp00f, abp00g, abp00h, abp00i, abp00j, abp00k, atc9w8, atc9w2,
                   aaa302_1, aaa90y, zynum, jynum,
                   aae011, aae017, aae019, aae020, aae022, aae036)
                   VALUES (nextval('seq_sys_baz001'), nextval('seq_sys_baz002'), nextval('seq_tc9w_atc9w0'),
                   ?, '0', ?, ?, ?, ?, ?, ?, ?, '1', ?, ?, '1 居民身份证（户口簿）', ?, ?,
                   ?, ?, ?, ?, '0 非公务员', '3 合同制', ?, NULL, ?, ?, ?,
                   '130000', ?, '4 其他', ?, ?, ?, '1', ?, ?, '90', ?, ?,
                   ?, ?, ?, ?, ?, now())""",
                ymN, (row["atc900"] as Number).toLong(),
                (row["atb900"] as Number?)?.toLong(), (row["aab001"] as Number?)?.toLong(), row["aab004"],
                row["aaa130"], row["aaa131"], row["aac001"], aaa030, row["aaa031"],
                row["aac002"], row["aac003"],
                ymd8(row["aac006"]), sex,
                aa10Label("AAC005", row["aac005"]?.toString()) ?: "01 汉族",
                ymd8(row["atc90c"]),
                if (row["aac012"]?.toString() == "91") "1 干部" else "0 非干部",
                aa10Label("AAC009", row["aac009"]?.toString()) ?: "11 本地非农业户口（本地城镇）",
                row["aae005"], bytes50(row["atc902"]?.toString()),
                bytes50(row["atc902"]?.toString()),
                ymd8(row["atc90ax"]), ymd8(row["atc90b"]), row["atc905"]?.toString(),
                if (isCurMonth) "1" else "2",
                row["atc905"] as Number?, // aaa302_1 numeric(申报基数)
                row["zynum"], row["jynum"],
                user?.get(0), user?.get(1), user?.get(2), user?.get(3), user?.get(4))
            pushed++
        }
        return mapOf("appCode" to 1, "pushed" to pushed, "skipped" to skipped,
            "msg" to "推送 $pushed 人" + (if (skipped.isEmpty()) "" else "; 跳过 ${skipped.size}(无户头/性别码兜底)"))
    }

    /** 报增查询: TC9W/TC9W1 名册工作表(按年月/险金户头/参保属地过滤; kind=medical 读 TC9W1) */
    @GetMapping("/roster")
    fun roster(@RequestParam ym: String,
               @RequestParam(required = false) aaa030: Long?,
               @RequestParam(required = false) aaa020: String?,
               @RequestParam(required = false) kind: String?): List<Map<String, Any?>> {
        if (!Regex("^\\d{6}$").matches(ym)) return emptyList()
        if (kind == "housingfund") {
            // atc9u8 别名 atc9w8(P1 修正 2026-11-20): 前端状态列/回盘防呆(pushLocked)统一按 atc9w8 取值
            var hsql = """SELECT w.atc9u0 AS atc9w0, w.aae002, w.atc900, w.aac001, w.aab001, w.aab004,
                w.atc9u1, w.aaa030, w.aaa031, w.aaa100, w.aaa101, w.aac002, w.aac003,
                w.atc9u8 AS atc9w8, w.aae011, w.aaa004, w.abp009,
                EXISTS(SELECT 1 FROM tc9w_bp02 b WHERE b.aae002=w.aae002 AND b.atc900=w.atc900
                       AND b.ace10a='1' AND b.abp002='正常') AS confirmed_ok,
                EXISTS(SELECT 1 FROM tc9u_bp03 y WHERE y.aae002=w.aae002 AND y.atc900=w.atc900
                       AND y.abp004='异议') AS confirmed_no
                FROM tc9u w WHERE w.aae002=${ym.toLong()}::numeric AND w.atc9u1='1' AND w.atc9u8<>'9'"""
            if (aaa030 != null) hsql += " AND w.aaa030=$aaa030"
            if (!aaa020.isNullOrBlank()) hsql += " AND w.aaa100='${aaa020.replace("'", "''")}'"
            return jdbc.queryForList("$hsql ORDER BY w.atc9u0")
        }
        if (kind == "mgongshang") {
            var m2sql = """SELECT w.atc9w0, w.aae002, w.atc900, w.aac001, w.aab001, w.aab004,
                w.atc9w1, w.aaa030, w.aaa031, w.aaa100, w.aaa101, w.aac002, w.aac003,
                w.atc9w8, w.aae011, w.aaa302_1,
                EXISTS(SELECT 1 FROM tc9w_bp10 b WHERE b.aae002=w.aae002 AND b.atc900=w.atc900
                       AND b.ace10a='1') AS confirmed_ok
                FROM tc9w4 w WHERE w.aae002=${ym.toLong()} AND w.atc9w1='1' AND w.atc9w8<>'9'"""
            if (aaa030 != null) m2sql += " AND w.aaa030=$aaa030"
            if (!aaa020.isNullOrBlank()) m2sql += " AND w.aaa100='${aaa020.replace("'", "''")}'"
            return jdbc.queryForList("$m2sql ORDER BY w.atc9w0")
        }
        if (kind == "sgongshang") {
            var gsql = """SELECT w.atc9w0, w.aae002, w.atc900, w.aac001, w.aab001, w.aab004,
                w.atc9w1, w.aaa030, w.aaa031, w.aaa100, w.aaa101, w.aac002, w.aac003,
                w.atc9w8, w.aae011, w.aaa302_1,
                EXISTS(SELECT 1 FROM tc9w_bp09 b WHERE b.aae002=w.aae002 AND b.atc900=w.atc900
                       AND b.aaa90w='1' AND b.ace10a='1') AS confirmed_ok,
                EXISTS(SELECT 1 FROM tc9w_bp09 b WHERE b.aae002=w.aae002 AND b.atc900=w.atc900
                       AND b.aaa90w='2' AND b.aae236 IS NOT NULL) AS confirmed_no
                FROM tc9w3 w WHERE w.aae002=${ym.toLong()} AND w.atc9w1='1' AND w.atc9w8<>'9'"""
            if (aaa030 != null) gsql += " AND w.aaa030=$aaa030"
            if (!aaa020.isNullOrBlank()) gsql += " AND w.aaa100='${aaa020.replace("'", "''")}'"
            return jdbc.queryForList("$gsql ORDER BY w.atc9w0")
        }
        if (kind == "smedical") {
            var ssql = """SELECT w.atc9w0, w.aae002, w.atc900, w.aac001, w.aab001, w.aab004,
                w.atc9w1, w.aaa030, w.aaa031, w.aaa100, w.aaa101, w.aac002, w.aac003,
                w.atc9w8, w.aae011, w.aaa005, w.aaa302_1,
                EXISTS(SELECT 1 FROM tc9w_bp01 b WHERE b.aae002=w.aae002 AND b.atc900=w.atc900
                       AND b.aaa90w='1' AND b.aae236 IS NOT NULL) AS confirmed_ok,
                EXISTS(SELECT 1 FROM tc9w_bp01 b WHERE b.aae002=w.aae002 AND b.atc900=w.atc900
                       AND b.aaa90w='2' AND b.aae236 IS NOT NULL) AS confirmed_no
                FROM tc9w2 w WHERE w.aae002=${ym.toLong()} AND w.atc9w1='1' AND w.atc9w8<>'9'"""
            if (aaa030 != null) ssql += " AND w.aaa030=$aaa030"
            if (!aaa020.isNullOrBlank()) ssql += " AND w.aaa100='${aaa020.replace("'", "''")}'"
            return jdbc.queryForList("$ssql ORDER BY w.atc9w0")
        }
        if (kind == "medical") {
            var msql = """SELECT w.atc9w0, w.aae002, w.atc900, w.aac001, w.aab001, w.aab004,
                w.atc9w1, w.aaa030, w.aaa031, w.aaa100, w.aaa101, w.aac002, w.aac003,
                w.atc9w8, w.aae011,
                EXISTS(SELECT 1 FROM tc9w_bp04 b WHERE b.aae002=w.aae002 AND b.atc900=w.atc900
                       AND b.ace10a='1') AS confirmed_ok,
                EXISTS(SELECT 1 FROM tc9w_bp04 b WHERE b.aae002=w.aae002 AND b.atc900=w.atc900
                       AND b.ace10a='2') AS confirmed_no
                FROM tc9w1 w WHERE w.aae002=${ym.toLong()} AND w.atc9w1='1' AND w.atc9w8<>'9'"""
            if (aaa030 != null) msql += " AND w.aaa030=$aaa030"
            if (!aaa020.isNullOrBlank()) msql += " AND w.aaa100='${aaa020.replace("'", "''")}'"
            return jdbc.queryForList("$msql ORDER BY w.atc9w0")
        }
        var sql = """SELECT w.atc9w0, w.aae002, w.atc900, w.aac001, w.aab001, w.aab004,
            w.atc9w1, w.aaa030, w.aaa031, w.aaa100, w.aaa101, w.aac002, w.aac003,
            w.atc9w8, w.aae011,
            EXISTS(SELECT 1 FROM tc9w_bp b WHERE b.aae002=w.aae002 AND b.atc900=w.atc900
                   AND b.aaa90w='1') AS confirmed_ok,
            EXISTS(SELECT 1 FROM tc9w_bp b WHERE b.aae002=w.aae002 AND b.atc900=w.atc900
                   AND b.aaa90w='2') AS confirmed_no
            FROM tc9w w WHERE w.aae002=${ym.toLong()} AND w.atc9w1='1' AND w.atc9w8<>'9'"""
        if (aaa030 != null) sql += " AND w.aaa030=$aaa030"
        if (!aaa020.isNullOrBlank()) sql += " AND w.aaa100='${aaa020.replace("'", "''")}'"
        return jdbc.queryForList("$sql ORDER BY w.atc9w0")
    }

    /** 已推送名册的 户头/区划 组合(查询条件下拉; kind=medical 读 TC9W1) */
    @GetMapping("/heads")
    fun heads(@RequestParam ym: String, @RequestParam(required = false) kind: String?): List<Map<String, Any?>> {
        if (!Regex("^\\d{6}$").matches(ym)) return emptyList()
        val table = when (kind) { "medical" -> "tc9w1"; "housingfund" -> "tc9u"; "smedical" -> "tc9w2"; "sgongshang" -> "tc9w3"; "mgongshang" -> "tc9w4"; else -> "tc9w" }
        return jdbc.queryForList(
            """SELECT t.aaa030,
                   (SELECT (array_agg(x.aaa031 ORDER BY x.aae036 DESC NULLS LAST))[1]
                      FROM ab01 x WHERE x.aaa030 = t.aaa030) AS aaa031,
                   t.aaa100, max(t.aaa101) AS aaa101
               FROM $table t
               WHERE t.aae002=? AND t.atc9w1='1' AND t.atc9w8<>'9' AND t.aaa100 IS NOT NULL
               GROUP BY t.aaa030, t.aaa100 ORDER BY t.aaa030""", ym.toLong())
    }

    /** 成果确认请求: atc9w0 + 险种 + 成功(参保日期 abp001 + 个人编号 aaa001; 医保另可带申报基数
     *  abp004, 缺省=合同基数) 或 异议(原因 aae013) */
    data class ConfirmReq(val atc9w0: Long, val ok: Boolean, val kind: String = "social",
                          val abp001: String? = null, val abp004: String? = null,
                          val aaa001: String? = null, val aae013: String? = null,
                          val aae011: String = "")

    /** 成果确认: 写 TC9W_BP(ace10a='3', aaa90w 1=成功/2=异议)。
     *  同键(aae002+atc900+aaa90w)已有行则原地 UPDATE 覆盖(逻辑更新, 不物理删, 重确认可追溯);
     *  abp001 存 'yyyy-MM-dd' — 回盘提交 PRC 按前 10 位 ISO 解析。 */
    @PostMapping("/confirm")
    @Transactional
    fun confirm(@RequestBody r: ConfirmReq): Map<String, Any?> {
        audit.log(r.aae011, "wb:hz/confirm", "atc9w0=${r.atc9w0}", r)
        if (r.kind == "housingfund") return confirmHousingFund(r)
        if (r.kind == "smedical") return confirmSMedical(r)
        if (r.kind == "sgongshang") return confirmSGongshang(r)
        if (r.kind == "mgongshang") return confirmMGongshang(r)
        if (r.kind == "medical") return confirmMedical(r)
        val w = runCatching {
            jdbc.queryForMap(
                """SELECT aae002, aaa030, aaa031, aaa100, aaa101, atc900, aac002, aac003,
                   aab001, aab004 FROM tc9w WHERE atc9w0=?""", r.atc9w0)
        }.getOrNull() ?: return mapOf("appCode" to -1, "errMsg" to "名册行不存在")
        if (r.ok && (r.abp001.isNullOrBlank() || r.aaa001.isNullOrBlank()))
            return mapOf("appCode" to -1, "errMsg" to "报增成功须填实际参保日期与个人编号")
        if (!r.ok && r.aae013.isNullOrBlank())
            return mapOf("appCode" to -1, "errMsg" to "报增失败须填原因")
        val ymN = (w["aae002"] as Number).toLong()
        val atc900N = (w["atc900"] as Number).toLong()
        val aaa90w = if (r.ok) "1" else "2"
        // 逻辑更新: 同键旧行原地覆盖(不物理删), 操作留痕见 wb_audit_log
        val updated = jdbc.update(
            """UPDATE tc9w_bp SET aaa001=?, abp001=?, aae013=?, aae011=?, aae036=now()
               WHERE aae002=? AND atc900=? AND aaa90w=?""",
            if (r.ok) r.aaa001 else null, if (r.ok) r.abp001 else null, r.aae013,
            r.aae011, ymN, atc900N, aaa90w)
        if (updated == 0) jdbc.update(
            """INSERT INTO tc9w_bp (id, aae002, aaa030, aaa031, aaa020, aaa021, aaa001,
               aac002, aac003, abp001, ace10a, aaa90w, atc900, aab001, aab004, aae011,
               aae036, aae013)
               VALUES (nextval('seq_tc9w_id'), ?, ?, ?, ?, ?, ?, ?, ?, ?, '3', ?, ?, ?, ?, ?,
               now(), ?)""",
            ymN, (w["aaa030"] as Number?)?.toLong(), w["aaa031"],
            w["aaa100"], w["aaa101"], if (r.ok) r.aaa001 else null,
            w["aac002"], w["aac003"], if (r.ok) r.abp001 else null, aaa90w,
            atc900N, (w["aab001"] as Number?)?.toLong(), w["aab004"],
            r.aae011, r.aae013)
        if (r.ok) jdbc.update("UPDATE tc9w SET aaa001=? WHERE atc9w0=?", r.aaa001, r.atc9w0)
        return mapOf("appCode" to 1,
            "msg" to (if (r.ok) "已登记报增成功(待成功校验)" else "已登记异议(待回盘提交)"))
    }

    /** 医保成果确认: 反馈行写入 TC9W_BP04(abp001 参保状态默认'在职', abp004 申报基数缺省=
     *  合同医保基数→校验无基数差异; ace10a 留空待校验; 重确认原地更新, 不物理删) */
    private fun confirmMedical(r: ConfirmReq): Map<String, Any?> {
        val w = runCatching {
            jdbc.queryForMap(
                """SELECT aae002, aaa030, aaa031, aaa100, aaa101, atc900, aac002, aac003,
                   aab001, aab004 FROM tc9w1 WHERE atc9w0=?""", r.atc9w0)
        }.getOrNull() ?: return mapOf("appCode" to -1, "errMsg" to "名册行不存在")
        if (!r.ok && r.aae013.isNullOrBlank())
            return mapOf("appCode" to -1, "errMsg" to "报增失败须填原因")
        val ymN = (w["aae002"] as Number).toLong()
        val atc900N = (w["atc900"] as Number).toLong()
        val base = r.abp004?.takeIf { it.isNotBlank() }
            ?: jdbc.queryForObject("SELECT aaa302::text FROM tc90 WHERE atc900=?", String::class.java, atc900N)
        // 原地更新已有反馈行, 无则插入(逻辑更新, 留痕见 wb_audit_log)
        val updated = jdbc.update(
            """UPDATE tc9w_bp04 SET aac002=?, aac003=?, abp001=?, abp004=?, aae013=?,
               aae011=?, aae036=now() WHERE aae002=? AND atc900=?""",
            w["aac002"], w["aac003"], r.abp001 ?: "在职", base, r.aae013,
            r.aae011, ymN, atc900N)
        if (updated == 0) jdbc.update(
            """INSERT INTO tc9w_bp04 (id, aae002, aaa030, aaa031, aaa020, aaa021, aac002,
               aac003, abp001, abp004, abp006, abp007, abp008, abp009, abp00a, abp00b,
               abp00c, aae013, aae011, aae036, atc900, aab001, aab004)
               VALUES (nextval('seq_tc9w_bp04_id'), ?, ?, ?, ?, ?, ?, ?, ?, ?, '0', '0', '0',
               '0', '0', '0', '0', ?, ?, now(), ?, ?, ?)""",
            ymN, (w["aaa030"] as Number?)?.toLong(), w["aaa031"],
            w["aaa100"], w["aaa101"], w["aac002"], w["aac003"],
            r.abp001 ?: "在职", base, r.aae013, r.aae011,
            atc900N, (w["aab001"] as Number?)?.toLong(), w["aab004"])
        if (r.ok && r.aaa001 != null)
            jdbc.update("UPDATE tc9w1 SET aac001=? WHERE atc9w0=?", r.aaa001, r.atc9w0)
        return mapOf("appCode" to 1, "msg" to "已登记医保反馈(申报基数 $base, 待成功校验)")
    }

    /** 公积金回盘结果手动登记(新系统辅助按键, 等价 局方回盘文件→TC9W_BP02 单行;
     *  公积金回盘文件实样未到位 W0#4 前的通道): 写 TC9W_BP02 ABP002='正常' 待校验行
     *  (基数缺省=名册 abp009, 比例缺省=名册 abp00a/b), 之后走 异动回盘校验→异动回盘提交
     *  (提交回写 TC90 + W0#1 终态置位)。公积金异议不经此键 — 走异议回盘桶(TC9U_BP03)。
     *  atc9w0 字段此处承载 TC9U.ATC9U0。 */
    private fun confirmHousingFund(r: ConfirmReq): Map<String, Any?> {
        if (!r.ok) return mapOf("appCode" to -1,
            "errMsg" to "公积金报增失败走异议回盘通道(异议回盘文件→TC9U_BP03)")
        if (r.aaa001.isNullOrBlank()) return mapOf("appCode" to -1, "errMsg" to "报增成功须填个人编号")
        val w = runCatching {
            jdbc.queryForMap(
                """SELECT aae002, aaa030, aaa031, aaa100, aaa101, atc900, aac002, aac003, aab001,
                   aab004, abp009, abp00a, abp00b FROM tc9u WHERE atc9u0=?""", r.atc9w0)
        }.getOrNull() ?: return mapOf("appCode" to -1, "errMsg" to "名册行不存在")
        val ymN = (w["aae002"] as Number).toLong()
        val atc900N = (w["atc900"] as Number).toLong()
        val base = r.abp004?.takeIf { it.isNotBlank() } ?: w["abp009"]?.toString()
        // 逻辑更新: 同键(aae002+atc900+abp002='正常')旧行原地覆盖(不物理删)
        val upd = jdbc.update(
            """UPDATE tc9w_bp02 SET aaa001=?, abp003=?, abp004=?, abp005=?, aae011=?, aae036=now()
               WHERE aae002=? AND atc900=? AND abp002='正常'""",
            r.aaa001, base, w["abp00a"], w["abp00b"], r.aae011, ymN, atc900N)
        if (upd == 0) jdbc.update(
            """INSERT INTO tc9w_bp02 (id, aae002, aaa030, aaa031, aaa020, aaa021, aaa001, aac002,
               aac003, abp002, abp003, abp004, abp005, atc900, aae011, aae036)
               VALUES (nextval('seq_tc9w_bp02_atc9u0'), ?, ?, ?, ?, ?, ?, ?, ?, '正常', ?, ?, ?, ?, ?, now())""",
            ymN, (w["aaa030"] as Number?)?.toLong(), w["aaa031"], w["aaa100"], w["aaa101"],
            r.aaa001, w["aac002"], w["aac003"], base, w["abp00a"], w["abp00b"], atc900N, r.aae011)
        return mapOf("appCode" to 1, "msg" to "已登记公积金回盘结果(待校验)")
    }

    /** 省医保回盘结果登记(手动, 等价局方回盘文件→TC9W_BP01 单行): 成功=aaa90w='1'(须个人编号,
     *  参保年月缺省=业务年月), 失败=aaa90w='2'(须原因 aae013)。之后走 异动回盘校验(整批置
     *  TC9W2 '4')→异动回盘提交(参保 AAA603/ATC90PA='0'/异议 SAFESIGN='2'/停保消费 TC9W2 报减行)。
     *  atc9w0 字段此处承载 TC9W2.ATC9W0。 */
    private fun confirmSMedical(r: ConfirmReq): Map<String, Any?> {
        val w = runCatching {
            jdbc.queryForMap(
                """SELECT aae002, aaa030, aaa031, aaa100, aaa101, atc900, aac002, aac003, aab001,
                   aab004 FROM tc9w2 WHERE atc9w0=?""", r.atc9w0)
        }.getOrNull() ?: return mapOf("appCode" to -1, "errMsg" to "名册行不存在")
        val ymN = (w["aae002"] as Number).toLong()
        val atc900N = (w["atc900"] as Number).toLong()
        val aaa90w = if (r.ok) "1" else "2"
        if (r.ok && r.aaa001.isNullOrBlank())
            return mapOf("appCode" to -1, "errMsg" to "参保成功须填保险个人编号")
        if (!r.ok && r.aae013.isNullOrBlank())
            return mapOf("appCode" to -1, "errMsg" to "报增失败须填原因")
        // 参保年月: 缺省=业务年月; 手输 yyyy-MM-dd 取前7位转 yyyymm
        val abp001 = r.abp001?.takeIf { it.isNotBlank() }?.let {
            if (it.length >= 7 && it.contains('-')) it.substring(0, 7).replace("-", "") else it
        } ?: ymN.toString()
        // 逻辑更新: 同键(aae002+atc900+aaa90w)旧行原地覆盖(不物理删)
        val upd = jdbc.update(
            """UPDATE tc9w_bp01 SET aaa001=?, abp001=?, aae013=?, aae011=?, aae036=now()
               WHERE aae002=? AND atc900=? AND aaa90w=?""",
            if (r.ok) r.aaa001 else null, abp001, r.aae013, r.aae011, ymN, atc900N, aaa90w)
        if (upd == 0) jdbc.update(
            """INSERT INTO tc9w_bp01 (id, aae002, atc9w1, aaa90w, aaa030, aaa031, aaa020, aaa021,
               aaa001, aac002, aac003, abp001, aae013, aae011, aae036, atc900, aab001, aab004)
               VALUES (nextval('seq_tc9w_bp01_atc9w0'), ?, '1', ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, now(), ?, ?, ?)""",
            ymN, aaa90w, (w["aaa030"] as Number?)?.toLong(), w["aaa031"], w["aaa100"], w["aaa101"],
            if (r.ok) r.aaa001 else null, w["aac002"], w["aac003"], abp001, r.aae013, r.aae011,
            atc900N, (w["aab001"] as Number?)?.toLong(), w["aab004"])
        return mapOf("appCode" to 1,
            "msg" to (if (r.ok) "已登记参保成功(待异动校验)" else "已登记异议(待异动校验/提交)"))
    }

    /** 省工伤回盘结果登记(手动, 等价局方回盘文件→TC9W_BP09 单行): 成功=aaa90w='1'(须个人编号,
     *  参保年月缺省=业务年月), 失败=aaa90w='2'(须原因)。之后走 异动回盘校验(整批置 TC9W3 '4')
     *  →异动回盘提交(ATC90PB 状态机/翻 ace10a='1'/异议 SAFESIGN='3'/停保消费 TC9W3 报减行)。
     *  atc9w0 字段此处承载 TC9W3.ATC9W0。 */
    private fun confirmSGongshang(r: ConfirmReq): Map<String, Any?> {
        val w = runCatching {
            jdbc.queryForMap(
                """SELECT aae002, aaa030, aaa031, aaa100, aaa101, atc900, aac002, aac003, aab001,
                   aab004 FROM tc9w3 WHERE atc9w0=?""", r.atc9w0)
        }.getOrNull() ?: return mapOf("appCode" to -1, "errMsg" to "名册行不存在")
        val ymN = (w["aae002"] as Number).toLong()
        val atc900N = (w["atc900"] as Number).toLong()
        val aaa90w = if (r.ok) "1" else "2"
        if (r.ok && r.aaa001.isNullOrBlank())
            return mapOf("appCode" to -1, "errMsg" to "参保成功须填保险个人编号")
        if (!r.ok && r.aae013.isNullOrBlank())
            return mapOf("appCode" to -1, "errMsg" to "报增失败须填原因")
        val abp001 = r.abp001?.takeIf { it.isNotBlank() }?.let {
            if (it.length >= 7 && it.contains('-')) it.substring(0, 7).replace("-", "") else it
        } ?: ymN.toString()
        val upd = jdbc.update(
            """UPDATE tc9w_bp09 SET aaa001=?, abp001=?, aae013=?, aae011=?, aae036=now()
               WHERE aae002=? AND atc900=? AND aaa90w=?""",
            if (r.ok) r.aaa001 else null, abp001, r.aae013, r.aae011, ymN, atc900N, aaa90w)
        if (upd == 0) jdbc.update(
            """INSERT INTO tc9w_bp09 (id, aae002, atc9w1, aaa90w, aaa030, aaa031, aaa020, aaa021,
               aaa001, aac002, aac003, abp001, aae013, aae011, aae036, atc900, aab001, aab004)
               VALUES (nextval('seq_tc9w_bp09_id'), ?, '1', ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, now(), ?, ?, ?)""",
            ymN, aaa90w, (w["aaa030"] as Number?)?.toLong(), w["aaa031"], w["aaa100"], w["aaa101"],
            if (r.ok) r.aaa001 else null, w["aac002"], w["aac003"], abp001, r.aae013, r.aae011,
            atc900N, (w["aab001"] as Number?)?.toLong(), w["aab004"])
        return mapOf("appCode" to 1,
            "msg" to (if (r.ok) "已登记参保成功(待异动校验)" else "已登记异议(待异动校验/提交)"))
    }

    /** 市工伤回盘结果登记(手动, 等价局方回盘文件→TC9W_BP10 单行): 桶无申报结果列
     *  (异议由提交的 TC90 扫描标记); 基数 abp004 必填, 缴费列缺省 '0'。
     *  atc9w0 字段此处承载 TC9W4.ATC9W0。 */
    private fun confirmMGongshang(r: ConfirmReq): Map<String, Any?> {
        if (r.abp004.isNullOrBlank())
            return mapOf("appCode" to -1, "errMsg" to "须填缴费基数")
        val w = runCatching {
            jdbc.queryForMap(
                """SELECT aae002, aaa030, aaa031, aaa100, aaa101, atc900, aac002, aac003, aab001,
                   aab004 FROM tc9w4 WHERE atc9w0=?""", r.atc9w0)
        }.getOrNull() ?: return mapOf("appCode" to -1, "errMsg" to "名册行不存在")
        val ymN = (w["aae002"] as Number).toLong()
        val atc900N = (w["atc900"] as Number).toLong()
        val upd = jdbc.update(
            """UPDATE tc9w_bp10 SET abp004=?, aae011=?, aae036=now()
               WHERE aae002=? AND atc900=? AND ace10a IS NULL""",
            r.abp004, r.aae011, ymN, atc900N)
        if (upd == 0) jdbc.update(
            """INSERT INTO tc9w_bp10 (id, aae002, aaa030, aaa031, aaa020, aaa021, aac002, aac003,
               abp001, abp004, abp006, abp007, abp008, abp009, abp00a, abp00b, abp00c, aae011,
               aae036, atc900, aab001, aab004)
               VALUES (nextval('seq_tc9w_bp10_id'), ?, ?, ?, ?, ?, ?, ?, '在职', ?,
               NULL, NULL, NULL, NULL, NULL, NULL, NULL, ?, now(), ?, ?, ?)""",
            ymN, (w["aaa030"] as Number?)?.toLong(), w["aaa031"], w["aaa100"], w["aaa101"],
            w["aac002"], w["aac003"], r.abp004, r.aae011,
            atc900N, (w["aab001"] as Number?)?.toLong(), w["aab004"])
        return mapOf("appCode" to 1, "msg" to "已登记市工伤回盘结果(待异动校验)")
    }

    /** 批量操作请求(atc900 列表; pass 另带对应 atc9w0) */
    data class BatchReq(val atc900: List<Long>, val atc9w0: List<Long> = emptyList())

    /** 延续至下月: 复用 doAjax 续月模式(tc90.aaa201 社保参保年月 +1 月)。
     *  旧系统行为: TC90 延续生效后 TC9W 更新必然报错(忠实保留) — 以 msg 透出。 */
    @PostMapping("/next")
    fun next(@RequestBody r: BatchReq): Map<String, Any?> {
        audit.log(null, "wb:hz/next", "atc900=${r.atc900}", r)
        if (r.atc900.isEmpty()) return mapOf("appCode" to -1, "errMsg" to "请选择名册行")
        return try {
            tdck.doAjax("", r.atc900.joinToString(","), "bz")
            mapOf("appCode" to 1)
        } catch (e: IllegalStateException) {
            mapOf("appCode" to 1, "msg" to "已延续至下月(tc90.aaa201+1月); ${e.message}")
        }
    }

    /** 审核通过(批量报增成功标记): 复用 doAjax 审核模式(tc90.atc90p='0')。
     *  同上: TC90 标记生效后按旧系统报错 — msg 透出。 */
    @PostMapping("/pass")
    fun pass(@RequestBody r: BatchReq): Map<String, Any?> {
        audit.log(null, "wb:hz/pass", "atc900=${r.atc900}", r)
        if (r.atc900.isEmpty()) return mapOf("appCode" to -1, "errMsg" to "请选择名册行")
        val atc9w0 = r.atc9w0.joinToString(",")
        val atc900 = r.atc900.joinToString(",")
        return try {
            tdck.doAjax(atc9w0, atc900, "bz")
            mapOf("appCode" to 1)
        } catch (e: IllegalStateException) {
            mapOf("appCode" to 1, "msg" to "已审核通过(tc90.atc90p='0'); ${e.message}")
        }
    }

    /** 回盘批次查询: TC9W_BP / TC9W_BP04(kind=medical) / 公积金三桶(kind=housingfund, bucket=hx|fc|yy) */
    @GetMapping("/bp")
    fun bp(@RequestParam ym: String, @RequestParam aaa030: Long,
           @RequestParam aaa020: String, @RequestParam(required = false) kind: String?,
           @RequestParam(required = false) bucket: String?): List<Map<String, Any?>> {
        if (!Regex("^\\d{6}$").matches(ym)) return emptyList()
        if (kind == "housingfund") return when (bucket ?: "hx") {
            // 封存桶(TC9U_BP02) 按 aaa100=属地 过滤(原过程同款); 异议桶(TC9U_BP03) 按 aaa020
            "fc" -> jdbc.queryForList(
                """SELECT atc9u0 AS id, abp002, aac002, aac003, aaa001, ace10a, ace10b
                   FROM tc9u_bp02 WHERE aae002=? AND aaa030=? AND aaa100=? ORDER BY atc9u0""",
                ym.toLong(), aaa030, aaa020)
            "yy" -> jdbc.queryForList(
                """SELECT atc9u0 AS id, abp002, abp003, abp004, aaa004, aac002, aac003,
                   ace10a, ace10b, atc900 FROM tc9u_bp03
                   WHERE aae002=? AND aaa030=? AND aaa020=? ORDER BY atc9u0""",
                ym.toLong(), aaa030, aaa020)
            else -> jdbc.queryForList(
                """SELECT id, abp002, aac002, aac003, aaa001, abp003, abp004, abp005,
                   aaa90z, aaa303_1, aaa303_2, ace10a, ace10b, atc900
                   FROM tc9w_bp02 WHERE aae002=? AND aaa030=? AND aaa020=? ORDER BY id""",
                ym.toLong(), aaa030, aaa020)
        }
        if (kind == "mgongshang") return jdbc.queryForList(
            """SELECT id, aac000, aac002, aac003, abp001, abp004, aaa90y, ace10a, ace10b, atc900
               FROM tc9w_bp10 WHERE aae002=? AND aaa030=? AND aaa020=? ORDER BY id""",
            ym.toLong(), aaa030, aaa020)
        if (kind == "sgongshang") return when (bucket ?: "hx") {
            // 补差桶(TC9W_BP08, ace100 批次)/减免桶(TC09_BP, ace100 批次); 异动桶=TC9W_BP09
            "bj" -> jdbc.queryForList(
                """SELECT id, ace100, aaa009, aac003, aae003, abp006, ace10a, ace10b
                   FROM tc9w_bp08 WHERE aaa030=? AND aaa020=? ORDER BY ace100 DESC, id LIMIT 200""",
                aaa030, aaa020)
            "jm" -> jdbc.queryForList(
                """SELECT id, ace100, aaa005, aac003, aae002, abp002, bba004, atc9eh, ace10a, ace10b
                   FROM tc09_bp WHERE aaa030=? AND aaa020=? ORDER BY ace100 DESC, id LIMIT 200""",
                aaa030, aaa020)
            else -> jdbc.queryForList(
                """SELECT id, aaa90w, aac002, aac003, aaa001, abp001, aae013, ace10a, ace10b, atc900
                   FROM tc9w_bp09 WHERE aae002=? AND aaa030=? AND aaa020=? ORDER BY id""",
                ym.toLong(), aaa030, aaa020)
        }
        if (kind == "smedical") return when (bucket ?: "hx") {
            // 补差桶(TC9W_BP06, 按 ace100 批次)/工资表桶(TC9W_BP07); 异动桶=TC9W_BP01
            "bj" -> jdbc.queryForList(
                """SELECT id, ace100, aaa005, aac003, aae002, aae003, abp002, abp003, abp004,
                   abp005, abp006, abp007, abp008, ace10a, ace10b
                   FROM tc9w_bp06 WHERE aaa030=? AND aaa020=? ORDER BY ace100 DESC, id LIMIT 200""",
                ym.toLong(), aaa030, aaa020)
            "gz" -> jdbc.queryForList(
                """SELECT id, aac003, abp001, abp002, abp003, abp004, abp005, abp006, abp007, abp008
                   FROM tc9w_bp07 WHERE aae002=? AND aaa030=? AND aaa020=? ORDER BY id""",
                ym.toLong(), aaa030, aaa020)
            else -> jdbc.queryForList(
                """SELECT id, aaa90w, aac002, aac003, aaa001, abp001, abo003, abo004, aae013,
                   ace10a, ace10b, aab004, atc900, aae211
                   FROM tc9w_bp01 WHERE aae002=? AND aaa030=? AND aaa020=? ORDER BY id""",
                ym.toLong(), aaa030, aaa020)
        }
        if (kind == "medical") return jdbc.queryForList(
            """SELECT id, ace10a AS aaa90w, aac002, aac003, coalesce(abp004,'') AS aaa001,
               coalesce(aaa90y,'') AS abp001, coalesce(aae013,'') AS aae013, ace10a, atc900, aab004
               FROM tc9w_bp04 WHERE aae002=? AND aaa030=? AND aaa020=? ORDER BY id""",
            ym.toLong(), aaa030, aaa020)
        // 回盘行 aaa020 可为 NULL(推送名册属地列空→导入落账 NULL) — NULL 属地行跟随户头展示
        return jdbc.queryForList(
            """SELECT id, aaa90w, aaa001, abp001, aac002, aac003, aae013, ace10a, atc900, aab004
               FROM tc9w_bp WHERE aae002=? AND aaa030=? AND (aaa020=? OR aaa020 IS NULL) ORDER BY id""",
            ym.toLong(), aaa030, aaa020)
    }

    /** 补缴明细查询: TC9W_BP05(先调 /proc/PKG_保险人员增减.PRC_社保补缴明细 重建) */
    @GetMapping("/bj")
    fun bujiao(@RequestParam ym: String, @RequestParam aaa030: Long,
               @RequestParam aaa020: String): List<Map<String, Any?>> {
        if (!Regex("^\\d{6}$").matches(ym)) return emptyList()
        return jdbc.queryForList(
            """SELECT atc900, aaa001, aac002, aac003, abp001, abp001_1, abp003, abp004,
               abp005, abp006, abp007, abp008, abp009, abp00a, abp00d
               FROM tc9w_bp05 WHERE aae002=? AND aaa030=? AND aaa020=? ORDER BY atc900""",
            ym.toLong(), aaa030, aaa020)
    }

    // ---------- M5: 局方申报结果(回盘)文件导入 ----------
    // 真实文件样本在 resources/baopan/huipan/:
    //  社保异动导入模板.xls  sheet '社保异动回盘' 9 列: 申报结果(1=成功/2=异议)/保险个人编号/
    //    公民身份证号码/姓名/户口性质/业务年月/备注/养老滞纳金/失业滞纳金 —— 实测局方返回文件
    //    编号/证件列常为空, 按报盘行序对齐工作表(可调口径; 有证件时按证件匹配优先)
    //  市医保回盘2026.04.xls sheet '个人缴费信息' 17 列: 单位编号/费款所属期/个人编号/身份证
    //    号码/姓名/人员类别(在职)/缴费标志/到账日期/缴费基数/缴费金额/基本医疗/个人缴纳大额/
    //    个人缴纳医疗/单位缴纳医疗/划个人帐户/单位缴纳工伤/单位缴纳生育
    //    —— 列→abp 映射与校验规则自洽: 缴费基数→abp004(基数不符 11), 个人缴纳大额→abp007
    //    (大额 14 vs AA94), 个人缴纳医疗→abp008(灵活就业 12), 划个人帐户→abp00b(失业代缴 17)
    // 省医保回盘文件格式已存档但暂不导入(省医保链路未建, 见 five-insurance-migration.md)

    /** 回盘文件导入请求路径参数: 表单字段 ym/kind/aae011 + file */
    private fun hdr(sheet: org.apache.poi.ss.usermodel.Sheet, names: List<String>): Map<String, Int> {
        val row = sheet.getRow(0) ?: return emptyMap()
        val out = mutableMapOf<String, Int>()
        for (c in 0 until row.lastCellNum.toInt()) {
            val t = row.getCell(c)?.toString()?.trim() ?: continue
            if (t in names && !out.containsKey(t)) out[t] = c
        }
        return out
    }

    private fun cellStr(row: org.apache.poi.ss.usermodel.Row?, col: Int?): String? {
        if (row == null || col == null) return null
        val c = row.getCell(col) ?: return null
        return when (c.cellType) {
            org.apache.poi.ss.usermodel.CellType.NUMERIC ->
                if (org.apache.poi.ss.usermodel.DateUtil.isCellDateFormatted(c))
                    c.getDateCellValue().toInstant()
                        .atZone(java.time.ZoneId.systemDefault()).toLocalDate().toString()
                else c.numericCellValue.toBigDecimal().stripTrailingZeros().toPlainString()
            else -> c.toString()?.trim()?.takeIf { it.isNotEmpty() }
        }
    }

    /** 15 位身份证 → 18 位(19 世纪补位+校验位), 非 15 位原样 — FUNC_AAC00215_18 等价 */
    private fun id15to18(v: String?): String? {
        val t = v?.trim() ?: return null
        if (t.length != 15 || !t.all { it.isDigit() }) return t
        val s17 = t.substring(0, 6) + "19" + t.substring(6)
        val w = intArrayOf(7, 9, 10, 5, 8, 4, 2, 1, 6, 3, 7, 9, 10, 5, 8, 4, 2)
        val sum = s17.mapIndexed { i, ch -> (ch - '0') * w[i] }.sum()
        val code = (12 - sum % 11) % 11
        return s17 + (if (code == 10) "X" else code.toString())
    }

    /** 业务年月列 → 'yyyy-MM-dd'(提交 PRC 按前 10 位 ISO 解析): 8 位 yyyymmdd/日期串均归一 */
    private fun toIsoDate(v: String?): String? {
        val t = v?.trim() ?: return null
        return when {
            Regex("^\\d{8}$").matches(t) -> t.substring(0, 4) + "-" + t.substring(4, 6) + "-" + t.substring(6)
            Regex("^\\d{4}-\\d{2}-\\d{2}").matches(t) -> t.substring(0, 10)
            Regex("^\\d{6}$").matches(t) -> t.substring(0, 4) + "-" + t.substring(4, 6) + "-01"
            else -> null
        }
    }

    /** 局方申报结果(回盘)文件导入: 解析 .xls → 按 证件/行序 对齐名册工作表 → 写
     *  TC9W_BP(社保)/TC9W_BP04(市医保) 待校验行 → 复用 /proc 校验/提交。
     *  东软对应: tdck 申报结果导入弹窗/tdcay 导入弹窗(通用导入通道写批次表)。 */
    @PostMapping("/import-bp")
    @Transactional
    fun importBp(@RequestParam ym: String, @RequestParam kind: String,
                 @RequestParam aae011: String,
                 @RequestParam aaa030: Long, @RequestParam aaa020: String,
                 @RequestParam("file") file: org.springframework.web.multipart.MultipartFile): Map<String, Any?> {
        audit.log(aae011, "wb:hz/import-bp", "ym=$ym kind=$kind aaa030=$aaa030",
            mapOf("ym" to ym, "kind" to kind, "file" to file.originalFilename, "size" to file.size))
        // P1 修正(2026-11-20): 公积金/工伤回盘文件实样未到位(W0#4) — 显式拒收,
        // 替代原先落入三分支全不匹配的静默 no-op(解析白做、0 行无提示)
        if (kind == "housingfund" || kind == "sgongshang" || kind == "mgongshang")
            return mapOf("appCode" to -1,
                "errMsg" to "该险种回盘文件导入待局方实样(W0#4), 未开放")
        // 源对话框必选户头/参保属地(AAE002/AAA030/AAA020 取自对话框参数, 导入文档 §2 ②)
        val headAaa031 = jdbc.queryForList(
            """SELECT (array_agg(aaa031 ORDER BY aae036 DESC NULLS LAST))[1] AS n
               FROM ab01 WHERE aaa030=?""", aaa030).firstOrNull()?.get("n")?.toString()
            ?: return mapOf("appCode" to -1, "errMsg" to "户头不存在")
        val headAaa021 = jdbc.queryForList("SELECT label FROM aa12 WHERE id=?", aaa020)
            .firstOrNull()?.get("label")?.toString() ?: aaa020
        if (!Regex("^\\d{6}$").matches(ym)) return mapOf("appCode" to -1, "errMsg" to "年月格式不正确")
        // 源对话框重导语义(导入文档 §6.3): "该月已存在数据，是否删除后重新导入？" →
        // 按 月+户头+区划 清暂存后重写; 同户头 aaa020=NULL 的遗留行一并清(源清不掉=记档差异)
        jdbc.update(
            """DELETE FROM tc9w_bp WHERE aae002=? AND aaa030=?
               AND (aaa020=? OR aaa020 IS NULL)""",
            ym.toLong(), aaa030, aaa020)
        val wb = try {
            org.apache.poi.ss.usermodel.WorkbookFactory.create(file.inputStream)
        } catch (e: Exception) {
            return mapOf("appCode" to -1, "errMsg" to "文件解析失败: ${e.message}")
        }
        wb.use { book ->
            val sheet = book.getSheetAt(0)
            val rosterTable = when (kind) { "medical" -> "tc9w1"; "smedical" -> "tc9w2"; else -> "tc9w" }
            val ids = jdbc.query(
                """SELECT atc9w0, atc900, aaa030, aaa031, aaa100, aaa101, aac002, aac003,
                   aab001, aab004 FROM $rosterTable
                   WHERE aae002=? AND atc9w1='1' AND atc9w8<>'9' ORDER BY atc9w0""",
                { rs, _ -> mapOf(
                    "atc9w0" to (rs.getObject("atc9w0") as Number).toLong(),
                    "atc900" to (rs.getObject("atc900") as Number?)?.toLong(),
                    "aaa030" to (rs.getObject("aaa030") as Number?)?.toLong(),
                    "aaa031" to rs.getString("aaa031"), "aaa100" to rs.getString("aaa100"),
                    "aaa101" to rs.getString("aaa101"), "aac002" to rs.getString("aac002"),
                    "aac003" to rs.getString("aac003"), "aab001" to (rs.getObject("aab001") as Number?)?.toLong(),
                    "aab004" to rs.getString("aab004")) },
                ym.toLong())
            val byId = ids.associateBy { it["aac002"] }
            var imported = 0
            var unmatched = 0
            val matchedKeys = mutableSetOf<Long?>()
            val rejects = mutableListOf<String>()
            var seq = 0 // 行序对齐游标(文件数据行 ↔ 工作表推送序)
            for (r in 1..sheet.lastRowNum) {
                val row = sheet.getRow(r) ?: continue
                if (kind == "smedical") {
                    // 省医保回盘(实样 baopan/huipan/1.回盘--2026.05省医保): sheet 首页 7 列,
                    // 数据自 R2 起(无隐藏元行); 按证件优先/报盘行序对齐 TC9W2 名册
                    val h = hdr(sheet, listOf("申报结果", "保险个人编号", "公民身份证号码", "姓名",
                        "参保年月", "大病险", "滞纳金"))
                    val result = cellStr(row, h["申报结果"])
                    if (result.isNullOrBlank()) continue
                    val idCard = cellStr(row, h["公民身份证号码"])?.let { id15to18(it) }
                    // 源行为=证件优先+报盘行序兜底(回盘文件行序=推送行序的前提);
                    // 新系统修正: 回盘文件自带证件号, 证件未命中推送名册=名册与回盘不一致,
                    // 行序硬对齐会把结果写到别人台账(错配比拒绝危险) → 未命中即拒(2026-11-19)
                    val w = idCard?.let { byId[it] }
                    if (w == null) {
                        unmatched++
                        rejects.add("R${r + 1}: 证件 ${idCard ?: "?"} 无对应推送名册行(该人可能不在本月报增名单)")
                        continue
                    }
                    val aaa90w = when (result) {
                        "1" -> "1"; "2" -> "2"; else -> null
                    }
                    if (aaa90w == null) {
                        unmatched++; rejects.add("R${r + 1}: 申报结果 '$result' 非已知值"); continue
                    }
                    val upd = jdbc.update(
                        """UPDATE tc9w_bp01 SET aaa001=?, aac002=?, aac003=?, abp001=?, abo003=?,
                           abo004=?, aab001=?, aab004=?, aae011=?, aae036=now()
                           WHERE aae002=? AND atc900=? AND aaa90w=?""",
                        cellStr(row, h["保险个人编号"]), idCard ?: w["aac002"],
                        w["aac003"] ?: cellStr(row, h["姓名"]),
                        cellStr(row, h["参保年月"]), cellStr(row, h["大病险"]), cellStr(row, h["滞纳金"]),
                        w["aab001"], w["aab004"], aae011, ym.toLong(), w["atc900"], aaa90w)
                    if (upd == 0) jdbc.update(
                        """INSERT INTO tc9w_bp01 (id, aae002, atc9w1, aaa90w, aaa030, aaa031, aaa020,
                           aaa021, aaa001, aac002, aac003, abp001, abo003, abo004, atc900, aab001,
                           aab004, aae011, aae036)
                           VALUES (nextval('seq_tc9w_bp01_atc9w0'), ?, '1', ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, now())""",
                        ym.toLong(), aaa90w, w["aaa030"], w["aaa031"], w["aaa100"], w["aaa101"],
                        cellStr(row, h["保险个人编号"]), idCard ?: w["aac002"],
                        w["aac003"] ?: cellStr(row, h["姓名"]),
                        cellStr(row, h["参保年月"]), cellStr(row, h["大病险"]), cellStr(row, h["滞纳金"]),
                        w["atc900"], w["aab001"], w["aab004"], aae011)
                    matchedKeys.add((w["atc900"] as Number?)?.toLong())
                    imported++
                } else if (kind == "medical") {
                    val h = hdr(sheet, listOf("身份证号码", "姓名", "人员类别", "缴费标志", "到账日期",
                        "缴费基数", "缴费金额", "基本医疗", "个人缴纳大额", "个人缴纳医疗",
                        "单位缴纳医疗", "划个人帐户", "单位缴纳工伤", "单位缴纳生育",
                        "费款所属期", "个人编号"))
                    val idCard = cellStr(row, h["身份证号码"])?.let { id15to18(it) }
                    val w = (idCard?.let { byId[it] }) ?: ids.getOrNull(seq)
                    if (w == null) {
                        unmatched++; rejects.add("R${r + 1}: 无对应名册行"); seq++; continue
                    }
                    seq++
                    // 逻辑更新: 已有反馈行原地覆盖(不物理删), ace10a 留空待校验
                    val updMed = jdbc.update(
                        """UPDATE tc9w_bp04 SET aac000=?, aac002=?, aac003=?, abp001=?, abp002=?,
                           abp003=?, abp004=?, abp005=?, abp006=?, abp007=?, abp008=?, abp009=?,
                           abp00a=?, abp00b=?, abp00c=?, aab001=?, aab004=?, aae011=?, aae036=now()
                           WHERE aae002=? AND atc900=?""",
                        cellStr(row, h["个人编号"]), w["aac002"], w["aac003"] ?: cellStr(row, h["姓名"]),
                        cellStr(row, h["人员类别"]) ?: "在职", cellStr(row, h["缴费标志"]),
                        cellStr(row, h["到账日期"]), cellStr(row, h["缴费基数"]),
                        cellStr(row, h["缴费金额"]), cellStr(row, h["基本医疗"]),
                        cellStr(row, h["个人缴纳大额"]), cellStr(row, h["个人缴纳医疗"]),
                        cellStr(row, h["单位缴纳医疗"]), cellStr(row, h["单位缴纳工伤"]),
                        cellStr(row, h["划个人帐户"]), cellStr(row, h["单位缴纳生育"]),
                        w["aab001"], w["aab004"], aae011, ym.toLong(), w["atc900"])
                    if (updMed == 0) jdbc.update(
                        """INSERT INTO tc9w_bp04 (id, aae002, aaa030, aaa031, aaa020, aaa021, aac000,
                        aac002, aac003, abp001, abp002, abp003, abp004, abp005, abp006, abp007,
                        abp008, abp009, abp00a, abp00b, abp00c, atc900, aab001, aab004, aae011, aae036)
                        VALUES (nextval('seq_tc9w_bp04_id'), ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, now())""",
                        ym.toLong(), w["aaa030"], w["aaa031"], w["aaa100"], w["aaa101"],
                        cellStr(row, h["个人编号"]), w["aac002"], w["aac003"] ?: cellStr(row, h["姓名"]),
                        cellStr(row, h["人员类别"]) ?: "在职", cellStr(row, h["缴费标志"]),
                        cellStr(row, h["到账日期"]), cellStr(row, h["缴费基数"]),
                        cellStr(row, h["缴费金额"]), cellStr(row, h["基本医疗"]),
                        cellStr(row, h["个人缴纳大额"]), cellStr(row, h["个人缴纳医疗"]),
                        cellStr(row, h["单位缴纳医疗"]), cellStr(row, h["单位缴纳工伤"]),
                        cellStr(row, h["划个人帐户"]), cellStr(row, h["单位缴纳生育"]),
                        w["atc900"], w["aab001"], w["aab004"], aae011)
                    matchedKeys.add((w["atc900"] as Number?)?.toLong())
                    imported++
                } else if (kind == "social") {
                    // 社保回盘: 源 ImportUtil 语义=Excel 原样落暂存表(不匹配不打标,
                    // AAE002/AAA030/AAA020/AAA031/AAA021 取对话框参数), 逐行证件匹配/
                    // 打标(ace10a 3=符合 2=不符合+ace10b 原因)由 PRC_社保异动回盘校验
                    // (W3b 已移 TdckCases.fallbackCheck, 按身份证查 TC90 取 MAX(ATC900))完成;
                    // 姓名不参与匹配(源 :1617-1623 注释禁用)=源行为忠实
                    val h = hdr(sheet, listOf("申报结果", "保险个人编号", "公民身份证号码", "姓名",
                        "户口性质", "业务年月", "备注", "养老滞纳金", "失业滞纳金"))
                    val result = cellStr(row, h["申报结果"])
                    if (result.isNullOrBlank()) continue
                    jdbc.update(
                        """INSERT INTO tc9w_bp (id, aae002, aaa030, aaa031, aaa020, aaa021,
                        aaa001, aac002, aac003, aac009, abp001, abp002, abp003, ace10a,
                        aaa90w, aae011, aae036, aae013)
                        VALUES (nextval('seq_tc9w_id'), ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?,
                        NULL, ?, ?, now(), ?)""",
                        ym.toLong(), aaa030, headAaa031, aaa020, headAaa021,
                        cellStr(row, h["保险个人编号"]), cellStr(row, h["公民身份证号码"]),
                        cellStr(row, h["姓名"]), cellStr(row, h["户口性质"]),
                        toIsoDate(cellStr(row, h["业务年月"])),
                        cellStr(row, h["养老滞纳金"]), cellStr(row, h["失业滞纳金"]),
                        result, aae011, cellStr(row, h["备注"]))
                    imported++
                }
            }
            // 导入后自动校验(源对话框 doImport 即调 CHECK proc; 逐行按身份证查 TC90
            // MAX(ATC900) 打标, 未命中行 ace10a='2' '匹配员工信息出错|' 留在不符合视图)
            if (kind == "social") tdck.fallbackCheck(ym, aaa030.toString(), aaa020)
            val stat = jdbc.queryForList(
                """SELECT ace10a, count(*) AS n FROM tc9w_bp
                   WHERE aae002=? AND aaa030=? AND aaa020=? GROUP BY ace10a""",
                ym.toLong(), aaa030, aaa020)
            val passed = (stat.firstOrNull { it["ace10a"] == "3" }?.get("n") as Number?)?.toInt() ?: 0
            val failed = (stat.firstOrNull { it["ace10a"] == "2" }?.get("n") as Number?)?.toInt() ?: 0
            val rejectList = jdbc.queryForList(
                """SELECT 'R' || row_number() OVER (ORDER BY id) || ': ' || ace10b AS why
                   FROM tc9w_bp WHERE aae002=? AND aaa030=? AND aaa020=? AND ace10a='2'
                   ORDER BY id""", ym.toLong(), aaa030, aaa020)
                .map { it["why"].toString() }
            return mapOf("appCode" to 1, "imported" to imported, "unmatched" to failed,
                "rosterRows" to imported, "rejects" to rejectList,
                "msg" to ("导入完成：成功 $passed 行，失败 $failed 行" +
                    (if (failed > 0) "（失败明细见弹窗/台账不符合视图）" else "")))
        }
    }
}
