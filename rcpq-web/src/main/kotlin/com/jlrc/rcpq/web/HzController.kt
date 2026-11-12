package com.jlrc.rcpq.web

import com.jlrc.rcpq.service.td.TdckCases
import org.springframework.jdbc.core.JdbcTemplate
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

    /** 名册推送请求: 年月 + 险种(social=TC9W/medical=TC9W1) + 可选单位过滤 + 经办人 */
    data class PushReq(val ym: String, val kind: String = "social",
                       val aab001: Long? = null, val aae011: String = "")

    /** 名册推送: 当月合同开始且生效(atb90r='3') → TC9W 工作表。
     *  同合同同月旧行置 atc9w8='9' 作废(逻辑删除, 不物理删; 重推可追溯),
     *  查询面一律排除 '9'; atc9w1='1' 报增, 新行 atc9w8='1' 待校验。 */
    @PostMapping("/push")
    fun push(@RequestBody r: PushReq): Map<String, Any?> {
        audit.log(r.aae011, "wb:hz/push", "ym=${r.ym}", r)
        if (!Regex("^\\d{6}$").matches(r.ym)) return mapOf("appCode" to -1, "errMsg" to "年月格式不正确")
        val unitFilter = if (r.aab001 != null) " AND t.aab001=${r.aab001}" else ""
        val medFilter = if (r.kind == "medical") " AND t.aaa402='1' AND t.aaa110 IS NOT NULL" else ""
        // 险金户头(aaa030/aaa031)在 ab01, 按单位取(与 PRC_参保异议赋值 同源)
        val rows = jdbc.queryForList(
            """SELECT t.atc900, t.aac002, t.aac003, t.aab001, t.aab004,
               u.aaa030, u.aaa031, t.aaa100, t.aaa101, t.aaa110, t.aaa111
               FROM tc90 t LEFT JOIN ab01 u ON u.aab001 = t.aab001
               WHERE t.atb90r='3' AND to_char(t.atc90c,'YYYYMM')='${r.ym}'$unitFilter$medFilter
               ORDER BY t.atc900""")
        val ymN = r.ym.toLong()
        if (r.kind == "medical") return pushMedical(r, rows, ymN)
        var pushed = 0
        val skipped = mutableListOf<String>()
        for (row in rows) {
            val aaa030 = (row["aaa030"] as Number?)?.toLong()
            if (aaa030 == null) { skipped.add("${row["aac003"]}(无险金户头)"); continue }
            jdbc.update(
                "UPDATE tc9w SET atc9w8='9' WHERE aae002=? AND atc900=? AND atc9w1='1' AND atc9w8<>'9'",
                ymN, (row["atc900"] as Number).toLong())
            jdbc.update(
                """INSERT INTO tc9w (atc9w0, aae002, atc900, aab001, aab004, atc9w1,
                   aaa030, aaa031, aaa100, aaa101, abp001, aac002, aac003, atc9w8, aae011, aae036)
                   VALUES (nextval('seq_tc9w_atc9w0'), ?, ?, ?, ?, '1', ?, ?, ?, ?,
                   '居民身份证（户口簿）', ?, ?, '1', ?, now())""",
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
                "UPDATE tc9w1 SET atc9w8='9' WHERE aae002=? AND atc900=? AND atc9w1='1' AND atc9w8<>'9'",
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

    /** 报增查询: TC9W/TC9W1 名册工作表(按年月/险金户头/参保属地过滤; kind=medical 读 TC9W1) */
    @GetMapping("/roster")
    fun roster(@RequestParam ym: String,
               @RequestParam(required = false) aaa030: Long?,
               @RequestParam(required = false) aaa020: String?,
               @RequestParam(required = false) kind: String?): List<Map<String, Any?>> {
        if (!Regex("^\\d{6}$").matches(ym)) return emptyList()
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
        val table = if (kind == "medical") "tc9w1" else "tc9w"
        return jdbc.queryForList(
            """SELECT DISTINCT aaa030, aaa031, aaa100, aaa101 FROM $table
               WHERE aae002=? AND atc9w1='1' AND atc9w8<>'9' AND aaa100 IS NOT NULL ORDER BY aaa030""", ym.toLong())
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
    fun confirm(@RequestBody r: ConfirmReq): Map<String, Any?> {
        audit.log(r.aae011, "wb:hz/confirm", "atc9w0=${r.atc9w0}", r)
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

    /** 回盘批次查询: TC9W_BP / TC9W_BP04(kind=medical) */
    @GetMapping("/bp")
    fun bp(@RequestParam ym: String, @RequestParam aaa030: Long,
           @RequestParam aaa020: String, @RequestParam(required = false) kind: String?): List<Map<String, Any?>> {
        if (!Regex("^\\d{6}$").matches(ym)) return emptyList()
        if (kind == "medical") return jdbc.queryForList(
            """SELECT id, ace10a AS aaa90w, aac002, aac003, coalesce(abp004,'') AS aaa001,
               coalesce(aaa90y,'') AS abp001, coalesce(aae013,'') AS aae013, ace10a, atc900, aab004
               FROM tc9w_bp04 WHERE aae002=? AND aaa030=? AND aaa020=? ORDER BY id""",
            ym.toLong(), aaa030, aaa020)
        return jdbc.queryForList(
            """SELECT id, aaa90w, aaa001, abp001, aac002, aac003, aae013, ace10a, atc900, aab004
               FROM tc9w_bp WHERE aae002=? AND aaa030=? AND aaa020=? ORDER BY id""",
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
    fun importBp(@RequestParam ym: String, @RequestParam kind: String,
                 @RequestParam aae011: String,
                 @RequestParam("file") file: org.springframework.web.multipart.MultipartFile): Map<String, Any?> {
        audit.log(aae011, "wb:hz/import-bp", "ym=$ym kind=$kind",
            mapOf("ym" to ym, "kind" to kind, "file" to file.originalFilename, "size" to file.size))
        if (!Regex("^\\d{6}$").matches(ym)) return mapOf("appCode" to -1, "errMsg" to "年月格式不正确")
        val wb = try {
            org.apache.poi.ss.usermodel.WorkbookFactory.create(file.inputStream)
        } catch (e: Exception) {
            return mapOf("appCode" to -1, "errMsg" to "文件解析失败: ${e.message}")
        }
        wb.use { book ->
            val sheet = book.getSheetAt(0)
            val ids = jdbc.query(
                """SELECT atc9w0, atc900, aaa030, aaa031, aaa100, aaa101, aac002, aac003,
                   aab001, aab004 FROM ${if (kind == "medical") "tc9w1" else "tc9w"}
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
            val rejects = mutableListOf<String>()
            var seq = 0 // 行序对齐游标(文件数据行 ↔ 工作表推送序)
            for (r in 1..sheet.lastRowNum) {
                val row = sheet.getRow(r) ?: continue
                if (kind == "medical") {
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
                    imported++
                } else {
                    val h = hdr(sheet, listOf("申报结果", "保险个人编号", "公民身份证号码", "姓名",
                        "户口性质", "业务年月", "备注", "养老滞纳金", "失业滞纳金"))
                    val result = cellStr(row, h["申报结果"])
                    if (result.isNullOrBlank()) continue
                    val idCard = cellStr(row, h["公民身份证号码"])?.let { id15to18(it) }
                    val w = (idCard?.let { byId[it] }) ?: ids.getOrNull(seq)
                    if (w == null) {
                        unmatched++; rejects.add("R${r + 1}: 无对应名册行"); seq++; continue
                    }
                    seq++
                    val aaa90w = when (result) {
                        "1" -> "1"; "2" -> "2"; else -> null
                    }
                    if (aaa90w == null) {
                        unmatched++; rejects.add("R${r + 1}: 申报结果 '$result' 非已知值"); continue
                    }
                    // 逻辑更新: 同键(aae002+atc900+aaa90w)已有行原地覆盖(不物理删)
                    val updSoc = jdbc.update(
                        """UPDATE tc9w_bp SET aaa001=?, aac002=?, aac003=?, aac009=?, abp001=?,
                           abp002=?, abp003=?, aab001=?, aab004=?, aae011=?, aae036=now(), aae013=?
                           WHERE aae002=? AND atc900=? AND aaa90w=?""",
                        cellStr(row, h["保险个人编号"]),
                        idCard ?: w["aac002"], w["aac003"] ?: cellStr(row, h["姓名"]),
                        cellStr(row, h["户口性质"]), toIsoDate(cellStr(row, h["业务年月"])),
                        cellStr(row, h["养老滞纳金"]), cellStr(row, h["失业滞纳金"]),
                        w["aab001"], w["aab004"], aae011, cellStr(row, h["备注"]),
                        ym.toLong(), w["atc900"], aaa90w)
                    if (updSoc == 0) jdbc.update(
                        """INSERT INTO tc9w_bp (id, aae002, aaa030, aaa031, aaa020, aaa021, aaa001,
                        aac002, aac003, aac009, abp001, abp002, abp003, ace10a, aaa90w, atc900,
                        aab001, aab004, aae011, aae036, aae013)
                        VALUES (nextval('seq_tc9w_id'), ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, NULL, ?, ?, ?, ?, ?, now(), ?)""",
                        ym.toLong(), w["aaa030"], w["aaa031"], w["aaa100"], w["aaa101"],
                        cellStr(row, h["保险个人编号"]),
                        idCard ?: w["aac002"], w["aac003"] ?: cellStr(row, h["姓名"]),
                        cellStr(row, h["户口性质"]), toIsoDate(cellStr(row, h["业务年月"])),
                        cellStr(row, h["养老滞纳金"]), cellStr(row, h["失业滞纳金"]),
                        aaa90w, w["atc900"], w["aab001"], w["aab004"], aae011,
                        cellStr(row, h["备注"]))
                    imported++
                }
            }
            return mapOf("appCode" to 1, "imported" to imported, "unmatched" to unmatched,
                "rosterRows" to ids.size, "rejects" to rejects,
                "msg" to "导入 $imported 行(待成功校验)" + if (unmatched > 0) "; 未匹配 $unmatched 行" else "")
        }
    }
}
