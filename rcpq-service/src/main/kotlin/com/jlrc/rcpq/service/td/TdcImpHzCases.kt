package com.jlrc.rcpq.service.td

import com.jlrc.rcpq.service.proc.ProcHandler
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal

/**
 * td/c/k 社保异动汇总导入族（旧 PKG_IMP_FZ 的 TC9W_HZ 族规格重写，导入基建波首割）。
 *
 * - PRC_PARSE_AAC002: GB11643 身份证校验(15/18 位; 加权 [7,9,10,5,8,4,2,1,6,3,7,9,10,5,8,4,2,1],
 *   校验码 '10X98765432'; 15→18 升级; -2 位数/-3 出生日期/-4 校验位[-4 仍回填性别/生日]/-1 系统错)
 * - CHECK_TC9W_HZ_SUCCESS_IMP / IMP2: 批次行证件清洗→身份证校验→员工 max 匹配
 *   (IMP2 加: 审核状态 atb90r='3'/报增 atc90p='0'/基数 aaa301=atc9w2/参保时间 atc904=atc9w1/
 *   户口 aa10 字典核对)→ ACE10A=3 回写(失败 ACE10A=2+ACE10B 原因)
 * - COMMIT_TC9W_HZ_SUCCESS_IMP: ACE10A='3' 行 → TC90.aaa001←REC.KEY, ACE10A='1'
 *   (原 TC9W 联动分支整块被注释, 忠实只保留 TC90 更新)
 * - COMMIT_AAC001: 单条 TC90.aaa001 回写
 * 均注册 /proc(PKG_IMP_FZ.*), flag 非'bz' 时校验循环不执行(原样)。
 */
@Component
class TdcImpHzCases(
    private val jdbcTemplate: JdbcTemplate,
) {
    val ok = 1

    /** GB11643 校验位(等价 PRC_PARSE_AAC002): 返回 (appcode, 性别, 出生日期, 18 位) */
    fun parseAac002(id: String): Array<Any?> {
        val weights = intArrayOf(7, 9, 10, 5, 8, 4, 2, 1, 6, 3, 7, 9, 10, 5, 8, 4, 2, 1)
        val checks = "10X98765432"
        try {
            when (id.length) {
                15 -> {
                    val tmp = id.substring(0, 6) + "19" + id.substring(6)
                    var sum = 0
                    for (i in 0 until 17) sum += tmp[i].digitToInt() * weights[i]
                    val mod = checks[sum % 11]
                    val aac004 = if (id[14].digitToInt() % 2 == 0) "2" else "1"
                    val aac006 = try {
                        java.time.LocalDate.parse("19" + id.substring(6, 12), java.time.format.DateTimeFormatter.BASIC_ISO_DATE)
                            .toString()
                    } catch (e: Exception) { return arrayOf<Any?>(-3, aac004, null, null) }
                    return arrayOf<Any?>(1, aac004, aac006, tmp + mod)
                }
                18 -> {
                    var sum = 0
                    for (i in 0 until 17) sum += id[i].digitToInt() * weights[i]
                    val mod = checks[sum % 11]
                    var code = 1
                    if (id.uppercase().last() != mod) code = -4 // 原 -4 仍继续回填
                    val aac004 = if (id[16].digitToInt() % 2 == 0) "2" else "1"
                    val aac006 = try {
                        java.time.LocalDate.parse(id.substring(6, 14), java.time.format.DateTimeFormatter.BASIC_ISO_DATE)
                            .toString()
                    } catch (e: Exception) { return arrayOf<Any?>(-3, aac004, null, null) }
                    return arrayOf<Any?>(code, aac004, aac006, id.uppercase())
                }
                else -> return arrayOf<Any?>(-2, null, null, null)
            }
        } catch (e: Exception) {
            return arrayOf<Any?>(-1, null, null, null)
        }
    }

    /** SUCCESS 校验(imp2=false: V1 仅证件+匹配; true: IMP2 加四项一致性核对) */
    @Transactional
    fun checkHzSuccess(ace100: String, flag: String, imp2: Boolean): Map<String, Any?> {
        if (flag != "bz") return mapOf("code" to ok, "rows" to 0, "pass" to 0, "reject" to 0)
        val rows = jdbcTemplate.query(
            "SELECT id, aac002, aac003, atc9w2::text AS w2, atc9w1::text AS w1, aac009 FROM tc9w_hz WHERE ace100=?",
            { rs, _ -> arrayOf<Any?>(rs.getLong("id"), rs.getString("aac002"), rs.getString("aac003"),
                rs.getString("w2"), rs.getString("w1"), rs.getString("aac009")) },
            ace100.toLongOrNull() ?: return mapOf("code" to 1, "rows" to 0, "pass" to 0, "reject" to 0))
        var pass = 0
        var reject = 0
        for (rec in rows) {
            var nOk = "3"
            var msg: String? = null
            val aac002 = clean(rec[1] as String?)
            val aac003 = clean(rec[2] as String?)
            // 身份证校验(原先清洗后校验; 清洗结果落回行为原 UPDATE ROW 语义的一部分)
            jdbcTemplate.update("UPDATE tc9w_hz SET aac002=?, aac003=? WHERE id=?", aac002, aac003, rec[0])
            val parsed = parseAac002(aac002 ?: "")
            var valid = (parsed[0] as Int) == 1
            if (!valid) { nOk = "2"; msg = (msg ?: "") + "公民身份证号码不合法|" }
            var tc90: Array<Any?>? = null
            if (valid) {
                val found = jdbcTemplate.query(
                    """SELECT atc900, aac001, atb90r, atc90p::text p, aaa301::text b301, atc904::text c904, aac009
                       FROM tc90 WHERE atc900=(SELECT max(atc900) FROM tc90 WHERE aac002=? AND aac003=?)
                       AND aac002=? AND aac003=?""",
                    { rs, _ -> arrayOf<Any?>(rs.getLong("atc900"), rs.getLong("aac001"), rs.getString("atb90r"),
                        rs.getString("p"), rs.getString("b301"), rs.getString("c904"), rs.getString("aac009")) },
                    aac002, aac003, aac002, aac003).firstOrNull()
                if (found == null) {
                    nOk = "2"; msg = (msg ?: "") + "系统不存在该员工信息|"; valid = false
                } else tc90 = found
            }
            if (valid && imp2) {
                val f = tc90!!
                if (f[2] != "3") { nOk = "2"; msg = (msg ?: "") + "当前员工当前已失效|" }
                val p = f[3]
                if (p != "0") { nOk = "2"; msg = (msg ?: "") + "当前员工申报报增未成功|" }
                if (p == null) { nOk = "2"; msg = (msg ?: "") + "当前员工申报报增未成功|" }
                if (f[4] != rec[3]) { nOk = "2"; msg = (msg ?: "") + "当前员工缴费基数不一致|" }
                if (f[5] != rec[4]) { nOk = "2"; msg = (msg ?: "") + "当前员工参保时间不一致|" }
                val aa10 = jdbcTemplate.query(
                    "SELECT aaa102 FROM aa10 WHERE aaa100='AAC009' AND aaa103=?",
                    { rs, _ -> rs.getString("aaa102") }, (rec[5] as String?)?.trim() ?: "")
                    .firstOrNull()
                if (aa10 == null) { nOk = "2"; msg = (msg ?: "") + "户口性质格式不正确|" }
                else if (aa10 != f[6]) { nOk = "2"; msg = (msg ?: "") + "当前员工户口性质不一致|" }
            }
            if (valid) {
                jdbcTemplate.update(
                    if (imp2) "UPDATE tc9w_hz SET ace10a=3, aac001=? WHERE id=?"
                    else "UPDATE tc9w_hz SET ace10a=3, aac001=?, atc900=? WHERE id=?",
                    (tc90?.get(1) as Long?), tc90?.get(0) as Long?, rec[0])
                pass++
            } else {
                jdbcTemplate.update(
                    "UPDATE tc9w_hz SET ace10a=?, ace10b=? WHERE id=?", nOk, msg, rec[0])
                reject++
            }
        }
        return mapOf("code" to ok, "rows" to rows.size, "pass" to pass, "reject" to reject)
    }

    /** COMMIT_TC9W_HZ_SUCCESS_IMP: ACE10A='3' 行回写 TC90.aaa001(原 TC9W 联动分支整块被注释) */
    @Transactional
    fun commitHzSuccess(ace100: String): Map<String, Any?> {
        val rows = jdbcTemplate.query(
            "SELECT id, key, aac002 FROM tc9w_hz WHERE ace10a='3' AND ace100=?",
            { rs, _ -> arrayOf<Any?>(rs.getLong("id"), rs.getString("key"), rs.getString("aac002")) },
            ace100.toLongOrNull() ?: return mapOf("code" to 1, "committed" to 0))
        var n = 0
        for (rec in rows) {
            n += jdbcTemplate.update(
                "UPDATE tc90 SET aaa001=? WHERE aac002=?", rec[1], rec[2])
            jdbcTemplate.update("UPDATE tc9w_hz SET ace10a='1' WHERE id=?", rec[0])
        }
        return mapOf("code" to ok, "committed" to n)
    }

    /** COMMIT_AAC001: 单条编号回写 */
    @Transactional
    fun commitAac001(aaa001: String, aac002: String): Map<String, Any?> {
        val n = jdbcTemplate.update("UPDATE tc90 SET aaa001=? WHERE aac002=?", aaa001, aac002)
        return mapOf("code" to ok, "updated" to n)
    }

    private fun clean(v: String?): String? =
        v?.replace("\r", "")?.replace("\n", "")?.replace("\t", "")?.replace("x", "X")?.replace(" ", "")
}

/** /proc 注册面(PKG_IMP_FZ.* 四个) */
@Component
class TdcImpHzCheckProc(private val c: TdcImpHzCases) : ProcHandler {
    override fun procName() = "PKG_IMP_FZ.PRC_CHECK_TC9W_HZ_SUCCESS_IMP"
    override fun handle(params: Map<String, Any?>): Map<String, Any?> {
        val r = c.checkHzSuccess(
            params["ace100"]?.toString() ?: return mapOf("appCode" to -1, "errMsg" to "缺少 ace100"),
            params["flag"]?.toString() ?: "bz", imp2 = false)
        return mapOf("appCode" to r["code"], "errMsg" to null as String?,
            "rows" to r["rows"], "pass" to r["pass"], "reject" to r["reject"])
    }
}

@Component
class TdcImpHzCheck2Proc(private val c: TdcImpHzCases) : ProcHandler {
    override fun procName() = "PKG_IMP_FZ.PRC_CHECK_TC9W_HZ_SUCCESS_IMP2"
    override fun handle(params: Map<String, Any?>): Map<String, Any?> {
        val r = c.checkHzSuccess(
            params["ace100"]?.toString() ?: return mapOf("appCode" to -1, "errMsg" to "缺少 ace100"),
            params["flag"]?.toString() ?: "bz", imp2 = true)
        return mapOf("appCode" to r["code"], "errMsg" to null as String?,
            "rows" to r["rows"], "pass" to r["pass"], "reject" to r["reject"])
    }
}

@Component
class TdcImpHzCommitProc(private val c: TdcImpHzCases) : ProcHandler {
    override fun procName() = "PKG_IMP_FZ.PRC_COMMIT_TC9W_HZ_SUCCESS_IMP"
    override fun handle(params: Map<String, Any?>): Map<String, Any?> {
        val r = c.commitHzSuccess(params["ace100"]?.toString() ?: return mapOf("appCode" to -1, "errMsg" to "缺少 ace100"))
        return mapOf("appCode" to r["code"], "errMsg" to null as String?, "committed" to r["committed"])
    }
}

@Component
class TdcImpAac001Proc(private val c: TdcImpHzCases) : ProcHandler {
    override fun procName() = "PKG_IMP_FZ.PRC_COMMIT_AAC001"
    override fun handle(params: Map<String, Any?>): Map<String, Any?> {
        val r = c.commitAac001(
            params["aaa001"]?.toString() ?: return mapOf("appCode" to -1, "errMsg" to "缺少 aaa001"),
            params["aac002"]?.toString() ?: return mapOf("appCode" to -1, "errMsg" to "缺少 aac002"))
        return mapOf("appCode" to r["code"], "errMsg" to null as String?, "updated" to r["updated"])
    }
}
