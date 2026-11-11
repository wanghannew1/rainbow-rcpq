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
class HzController(private val jdbc: JdbcTemplate, private val tdck: TdckCases) {

    /** 名册推送请求: 年月 + 可选单位过滤 + 经办人 */
    data class PushReq(val ym: String, val aab001: Long? = null, val aae011: String = "")

    /** 名册推送: 当月合同开始且生效(atb90r='3') → TC9W 工作表。
     *  同合同同月旧行先删(重推覆盖); atc9w1='1' 报增, atc9w8='1' 待校验。 */
    @PostMapping("/push")
    fun push(@RequestBody r: PushReq): Map<String, Any?> {
        if (!Regex("^\\d{6}$").matches(r.ym)) return mapOf("appCode" to -1, "errMsg" to "年月格式不正确")
        val unitFilter = if (r.aab001 != null) " AND t.aab001=${r.aab001}" else ""
        // 险金户头(aaa030/aaa031)在 ab01, 按单位取(与 PRC_参保异议赋值 同源)
        val rows = jdbc.queryForList(
            """SELECT t.atc900, t.aac002, t.aac003, t.aab001, t.aab004,
               u.aaa030, u.aaa031, t.aaa100, t.aaa101
               FROM tc90 t LEFT JOIN ab01 u ON u.aab001 = t.aab001
               WHERE t.atb90r='3' AND to_char(t.atc90c,'YYYYMM')='${r.ym}'$unitFilter
               ORDER BY t.atc900""")
        val ymN = r.ym.toLong()
        var pushed = 0
        val skipped = mutableListOf<String>()
        for (row in rows) {
            val aaa030 = (row["aaa030"] as Number?)?.toLong()
            if (aaa030 == null) { skipped.add("${row["aac003"]}(无险金户头)"); continue }
            jdbc.update("DELETE FROM tc9w WHERE aae002=? AND atc900=? AND atc9w1='1'",
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

    /** 报增查询: TC9W 名册工作表(按年月/险金户头/参保属地过滤) */
    @GetMapping("/roster")
    fun roster(@RequestParam ym: String,
               @RequestParam(required = false) aaa030: Long?,
               @RequestParam(required = false) aaa020: String?): List<Map<String, Any?>> {
        if (!Regex("^\\d{6}$").matches(ym)) return emptyList()
        var sql = """SELECT w.atc9w0, w.aae002, w.atc900, w.aac001, w.aab001, w.aab004,
            w.atc9w1, w.aaa030, w.aaa031, w.aaa100, w.aaa101, w.aac002, w.aac003,
            w.atc9w8, w.aae011,
            EXISTS(SELECT 1 FROM tc9w_bp b WHERE b.aae002=w.aae002 AND b.atc900=w.atc900
                   AND b.aaa90w='1') AS confirmed_ok,
            EXISTS(SELECT 1 FROM tc9w_bp b WHERE b.aae002=w.aae002 AND b.atc900=w.atc900
                   AND b.aaa90w='2') AS confirmed_no
            FROM tc9w w WHERE w.aae002=${ym.toLong()} AND w.atc9w1='1'"""
        if (aaa030 != null) sql += " AND w.aaa030=$aaa030"
        if (!aaa020.isNullOrBlank()) sql += " AND w.aaa100='${aaa020.replace("'", "''")}'"
        return jdbc.queryForList("$sql ORDER BY w.atc9w0")
    }

    /** 已推送名册的 户头/区划 组合(查询条件下拉) */
    @GetMapping("/heads")
    fun heads(@RequestParam ym: String): List<Map<String, Any?>> {
        if (!Regex("^\\d{6}$").matches(ym)) return emptyList()
        return jdbc.queryForList(
            """SELECT DISTINCT aaa030, aaa031, aaa100, aaa101 FROM tc9w
               WHERE aae002=? AND atc9w1='1' AND aaa100 IS NOT NULL ORDER BY aaa030""", ym.toLong())
    }

    /** 成果确认请求: atc9w0 + 成功(参保日期 abp001 + 个人编号 aaa001) 或 异议(原因 aae013) */
    data class ConfirmReq(val atc9w0: Long, val ok: Boolean, val abp001: String? = null,
                          val aaa001: String? = null, val aae013: String? = null,
                          val aae011: String = "")

    /** 成果确认: 写 TC9W_BP(ace10a='3', aaa90w 1=成功/2=异议; 重确认覆盖同键旧行)。
     *  abp001 存 'yyyy-MM-dd' — 回盘提交 PRC 按前 10 位 ISO 解析。 */
    @PostMapping("/confirm")
    fun confirm(@RequestBody r: ConfirmReq): Map<String, Any?> {
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
        val aaa90w = if (r.ok) "1" else "2"
        jdbc.update(
            "DELETE FROM tc9w_bp WHERE aae002=? AND atc900=? AND aaa90w=?",
            ymN, (w["atc900"] as Number).toLong(), aaa90w)
        jdbc.update(
            """INSERT INTO tc9w_bp (id, aae002, aaa030, aaa031, aaa020, aaa021, aaa001,
               aac002, aac003, abp001, ace10a, aaa90w, atc900, aab001, aab004, aae011,
               aae036, aae013)
               VALUES (nextval('seq_tc9w_id'), ?, ?, ?, ?, ?, ?, ?, ?, ?, '3', ?, ?, ?, ?, ?,
               now(), ?)""",
            ymN, (w["aaa030"] as Number?)?.toLong(), w["aaa031"],
            w["aaa100"], w["aaa101"], if (r.ok) r.aaa001 else null,
            w["aac002"], w["aac003"], if (r.ok) r.abp001 else null, aaa90w,
            (w["atc900"] as Number).toLong(), (w["aab001"] as Number?)?.toLong(), w["aab004"],
            r.aae011, r.aae013)
        if (r.ok) jdbc.update("UPDATE tc9w SET aaa001=? WHERE atc9w0=?", r.aaa001, r.atc9w0)
        return mapOf("appCode" to 1,
            "msg" to (if (r.ok) "已登记报增成功(待成功校验)" else "已登记异议(待回盘提交)"))
    }

    /** 批量操作请求(atc900 列表; pass 另带对应 atc9w0) */
    data class BatchReq(val atc900: List<Long>, val atc9w0: List<Long> = emptyList())

    /** 延续至下月: 复用 doAjax 续月模式(tc90.aaa201 社保参保年月 +1 月)。
     *  旧系统行为: TC90 延续生效后 TC9W 更新必然报错(忠实保留) — 以 msg 透出。 */
    @PostMapping("/next")
    fun next(@RequestBody r: BatchReq): Map<String, Any?> {
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

    /** 回盘批次查询: TC9W_BP(成果确认/导入落地) */
    @GetMapping("/bp")
    fun bp(@RequestParam ym: String, @RequestParam aaa030: Long,
           @RequestParam aaa020: String): List<Map<String, Any?>> {
        if (!Regex("^\\d{6}$").matches(ym)) return emptyList()
        return jdbc.queryForList(
            """SELECT id, aaa90w, aaa001, abp001, aac002, aac003, aae013, ace10a, atc900, aab004
               FROM tc9w_bp WHERE aae002=? AND aaa030=? AND aaa020=? ORDER BY id""",
            ym.toLong(), aaa030, aaa020)
    }
}
