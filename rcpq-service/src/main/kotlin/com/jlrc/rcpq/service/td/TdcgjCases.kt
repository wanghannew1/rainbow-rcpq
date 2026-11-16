package com.jlrc.rcpq.service.td

import com.jlrc.rcpq.service.proc.ProcHandler
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal

/**
 * W1 公积金链（旧 PKG_保险人员增减 公积金六过程规格重写, 2026-11-16）。
 *
 * 源: rainbowpq/work/pkg_all/PACKAGE_BODY_PKG_保险人员增减.sql
 *   - PRC_公积金异动回盘校验   L6510-6639（TC9W_BP02 桶）
 *   - PRC_公积金异动回盘提交   L6640-6766（参保成功/停保成功 两循环回写 TC90）
 *   - PRC_公积金封存回盘校验   L6767-6857（TC9U_BP02 桶, 逐行结果回写被注释→净效果=文本清洗）
 *   - PRC_公积金封存回盘提交   L6858-6952（ABP002='封存' 行回写 TC90.AAA004）
 *   - PRC_公积金异议回盘校验   L6346-6443（TC9U_BP03 桶, ABP003 含'100066835'+'状态封存'→启封）
 *   - PRC_公积金异议回盘提交   L6444-6509（启封→TC9U 置 ATC9U1='2'/ATC9UY='2'; 异议→参保异议赋值 SAFESIGN='4'）
 *
 * W0#1 裁决(2026-11-16): 原校验尾整批 `UPDATE TC9U SET ATC9U8='4'` 被注释禁用（L6555-6560,
 * 提交过程对 TC90 的业务回写全部健在、生产活跃）——新系统在**异动回盘提交成功路径末尾**
 * 恢复终态置位（与社保线 atc9w8='4' 对称）。
 *
 * 忠实保留的旧缺陷:
 *   - 异动校验对非'正常'行回写 atc900 时沿用上一'正常'行匹配值（PL/SQL 局部记录变量不清零）;
 *   - 异议校验不持久化启封查得的 AAA004（提交读列, 导入侧负责写入）;
 *   - 异议提交删 TC9U_BP01 为物理 DELETE（铁律 8 改造清单, 不顺手改）;
 *   - 封存校验逐行校验结果两处回写均被注释（净效果=清洗）。
 */
@Component
class TdcgjCases(
    private val jdbc: JdbcTemplate,
    private val tdck: TdckCases,
) {
    private val ok = 1
    private val err = -1

    private fun err(msg: String) = mapOf("code" to err, "msg" to msg)

    /** 文本清洗片段(原 REPLACE 链: 回车/换行/制表符; 身份证 x→X; 去空格) */
    private fun norm(col: String, x2x: Boolean) =
        "replace(replace(replace(replace(" + (if (x2x) "replace($col, 'x', 'X'), " else "$col, ") +
            "chr(13), ''), chr(10), ''), chr(9), ''), ' ', '')"

    private val normId = norm("aac002", true)
    private val normName = norm("aac003", false)
    private val normAcct = norm("aaa001", false)
    private val normBp2 = norm("abp002", true)

    /** 公积金员工匹配(异议校验用): max(atc900) of 在保公积金合同(aaa407='1', aae00p 1/5/7) 同属地同证件 */
    private fun matchHf(aaa020: String, idCard: String?): Map<String, Any?>? =
        jdbc.query(
            """SELECT atc900, aac002, aac003, aaa303, aaa503 FROM tc90
               WHERE atc900 = (SELECT max(atc900) FROM tc90
                               WHERE aaa407='1' AND aae00p IN ('1','5','7')
                                 AND aaa120=? AND aac002=?)""",
            { rs, _ -> mapOf("atc900" to (rs.getObject("atc900") as Number).toLong(),
                "aac002" to rs.getString("aac002"), "aac003" to rs.getString("aac003"),
                "aaa303" to (rs.getObject("aaa303") as BigDecimal?),
                "aaa503" to rs.getObject("aaa503")) },
            aaa020, idCard).firstOrNull()?.takeIf { it["atc900"] != null }

    private fun blKey(abp004: String?, abp005: String?) =
        (abp004 ?: "").replace(".0", "") + ":" + (abp005 ?: "").replace(".0", "")

    private fun blExists(abp004: String?, abp005: String?): Boolean {
        val key = blKey(abp004, abp005)
        return jdbc.queryForObject(
            "SELECT count(*) FROM aa10 WHERE aaa100='ATB90S' AND aaa102=?", Int::class.java, key) > 0
    }

    private fun tc06Special(aae002N: Long, atc900: Long, kinds: String): Int =
        jdbc.queryForObject(
            """SELECT count(*) FROM tc06 WHERE aae100='1' AND atc067 IN ($kinds)
               AND (aae002=? OR (aae002<=? AND atc06f>=?))
               AND aac001=(SELECT aac001 FROM tc90 WHERE atc900=?)""",
            Int::class.java, aae002N, aae002N, aae002N, atc900)

    // ---------- PRC_公积金异动回盘校验 ----------

    @Transactional
    fun gjCheck(aae002: String, aaa030: String, aaa020: String): Map<String, Any?> {
        val aae002N = aae002.toLongOrNull() ?: return err("公积金年月格式不正确|")
        jdbc.update(
            """UPDATE tc9w_bp02 SET
               aac002 = $normId, aac003 = $normName, aaa001 = $normAcct,
               aaa90z = null, aaa303_1 = 0, aaa303_2 = 0
               WHERE aaa030=? AND aaa020=? AND aae002=?""",
            aaa030.toLong(), aaa020, aae002N)
        data class Row(val id: Long, val aac002: String?, val aaa001: String?,
                       val abp002: String?, val abp003: String?, val abp004: String?, val abp005: String?)
        val rows = jdbc.query(
            """SELECT id, aac002, aaa001, abp002, abp003, abp004, abp005
               FROM tc9w_bp02 WHERE aaa030=? AND aaa020=? AND aae002=? ORDER BY id""",
            { rs, _ -> Row(rs.getLong("id"), rs.getString("aac002"), rs.getString("aaa001"),
                rs.getString("abp002"), rs.getString("abp003"), rs.getString("abp004"),
                rs.getString("abp005")) },
            aaa030.toLong(), aaa020, aae002N)
        var pass = 0
        var reject = 0
        var lastMatchedAtc900: Long? = null // 忠实: PL/SQL 局部记录变量循环间不清零, 非'正常'行沿用
        for (rec in rows) {
            var nOk = "3"
            var msg: String? = null
            var atc900: Long? = null
            var z: String? = null
            var d1: BigDecimal? = BigDecimal.ZERO
            var d2: BigDecimal? = BigDecimal.ZERO
            class Ck(m: String) : Exception(m)
            try {
                if (rec.abp002 == "正常") {
                    val t = jdbc.query(
                        """SELECT atc900, aaa303 FROM tc90
                           WHERE atc900 = (SELECT max(atc900) FROM tc90
                                           WHERE aaa407='1' AND aae00p IN ('1','5','7')
                                             AND aaa120=? AND aac002=?)""",
                        { rs, _ -> arrayOf((rs.getObject("atc900") as Number).toLong(), rs.getObject("aaa303") as BigDecimal?) },
                        aaa020, rec.aac002).firstOrNull()
                    if (t == null) throw Ck("匹配员工信息出错|")
                    atc900 = t[0] as Long
                    lastMatchedAtc900 = atc900
                    if (rec.aaa001.isNullOrBlank()) throw Ck("个人账号不能为空|")
                    if (rec.aaa001.contains('.')) throw Ck("个人编号格式不正确|")
                    // 基数不符(非失败, 记差异 aaa90z='10'); TO_NUMBER(abp003) 失败→原过程外层 OTHERS
                    val abp003N = runCatching {
                        rec.abp003?.trim()?.toBigDecimal() ?: BigDecimal.ZERO
                    }.getOrElse {
                        return err("异动回盘校验出错，错误信息：ORA-01722: invalid number，请将错误信息发给维护工程师")
                    }
                    val base = (t[1] as BigDecimal?) ?: BigDecimal.ZERO
                    if (abp003N.compareTo(base) != 0) { z = "10"; d1 = base; d2 = abp003N }
                    if (!blExists(rec.abp004, rec.abp005)) throw Ck("缴存比例不正确|")
                    jdbc.update(
                        """UPDATE tc9w_bp02 SET ace10a=?, ace10b=?, atc900=?, aaa90z=?, aaa303_1=?, aaa303_2=?
                           WHERE id=?""", nOk, msg, atc900, z, d1, d2, rec.id)
                } else {
                    // 忠实: 非'正常'行走成功路径写 ACE10A='3', atc900=上一'正常'行匹配值(变量不清零)
                    jdbc.update(
                        "UPDATE tc9w_bp02 SET ace10a='3', ace10b=null, atc900=? WHERE id=?",
                        lastMatchedAtc900, rec.id)
                }
            } catch (ck: Ck) {
                nOk = "2"; msg = ck.message
                jdbc.update(
                    "UPDATE tc9w_bp02 SET ace10a=?, ace10b=? WHERE id=?", nOk, msg, rec.id)
            }
            if (nOk == "3") pass++ else reject++
        }
        return mapOf("code" to ok, "msg" to null as String?, "rows" to rows.size,
            "pass" to pass, "reject" to reject)
    }

    // ---------- PRC_公积金异动回盘提交 ----------

    @Transactional
    fun gjCommit(aae002: String, aaa030: String, aaa020: String, aae011: String): Map<String, Any?> {
        val aae002N = aae002.toLongOrNull() ?: return err("公积金年月格式不正确|")
        val user = jdbc.query(
            "SELECT aae011 FROM sys_org_user_unit WHERE aae011=?",
            { rs, _ -> rs.getString("aae011") }, aae011).firstOrNull()
            ?: return err("获取经办人信息出错")
        // 处理参保成功(ACE10A='3' 且 ABP002='正常')
        data class CbRow(val id: Long, val atc900: Long?, val aac003: String?, val aaa001: String?,
                         val abp004: String?, val abp005: String?, val aaa90z: String?, val aaa303_2: BigDecimal?)
        val cbRows = jdbc.query(
            """SELECT id, atc900, aac003, aaa001, abp004, abp005, aaa90z, aaa303_2
               FROM tc9w_bp02 WHERE ace10a='3' AND abp002='正常'
                 AND aaa030=? AND aaa020=? AND aae002=? ORDER BY id""",
            { rs, _ -> CbRow(rs.getLong("id"), (rs.getObject("atc900") as Number?)?.toLong(),
                rs.getString("aac003"), rs.getString("aaa001"), rs.getString("abp004"),
                rs.getString("abp005"), rs.getString("aaa90z"), rs.getObject("aaa303_2") as BigDecimal?) },
            aaa030.toLong(), aaa020, aae002N)
        var cbApplied = 0
        for (cb in cbRows) {
            val atc900 = cb.atc900 ?: return err("【${cb.aac003}】新参保更新出错")
            val t = jdbc.query(
                "SELECT aac001, aaa503 FROM tc90 WHERE atc900=?",
                { rs, _ -> arrayOf(rs.getString("aac001"), rs.getObject("aaa503")) }, atc900).firstOrNull()
                ?: return err("【${cb.aac003}】新参保更新出错")
            if (tc06Special(aae002N, atc900, "'1','3'") == 0) {
                if (!blExists(cb.abp004, cb.abp005)) {
                    return err("【${cb.aac003}】新参保更新出错，原因是比例不正确")
                }
                val n = jdbc.update(
                    """UPDATE tc90 SET
                       aaa508 = CASE WHEN atc90w IS NULL OR atc90w='2' THEN ? ELSE aaa508 END,
                       atc90w = '0',
                       aaa004 = ?,
                       atb90s = ?,
                       aaa303 = CASE WHEN ? = '10' THEN ? ELSE aaa303 END
                       WHERE atc900=?""",
                    aae002N, cb.aaa001, blKey(cb.abp004, cb.abp005), cb.aaa90z, cb.aaa303_2, atc900)
                if (n == 0) return err("【${cb.aac003}】新参保更新出错")
                jdbc.update(
                    "UPDATE tc90_yy SET cbsign='0', aaa203=? WHERE safesign='4' AND atc900=?",
                    t[1], atc900)
                jdbc.update(
                    "UPDATE tc9u_bp01 SET aaa004=? WHERE aae002=? AND atc900=?", cb.aaa001, aae002N, atc900)
                jdbc.update("UPDATE tc9w_bp02 SET ace10a='1' WHERE id=?", cb.id)
                cbApplied++
            }
        }
        // 处理停保成功的(ATC90W='0' 且 AAA504<=年月 且(终止生效或'16'异动) 同户头同属地)
        data class TzRow(val atc900: Long)
        val tzRows = jdbc.query(
            """SELECT a.atc900 FROM tc90 a JOIN ab01 b ON a.aab001=b.aab001
               WHERE b.aaa030=? AND a.aaa407='1' AND a.atc90w='0' AND a.aaa504<=?
                 AND (a.atb90r='5' OR strpos(a.atc9wc, '16') > 0) AND a.aaa120=?""",
            { rs, _ -> TzRow((rs.getObject("atc900") as Number).toLong()) },
            aaa030.toLong(), aae002N, aaa020)
        var tzApplied = 0
        for (tb in tzRows) {
            if (tc06Special(aae002N, tb.atc900, "'1','2'") == 0) {
                jdbc.update(
                    "UPDATE tc90 SET aaa504=?, atc90w='1', aaa509=? WHERE atc900=?",
                    aae002N, aae002N, tb.atc900)
                tzApplied++
            }
        }
        // W0#1 裁决: 恢复终态置位(原校验尾 L6555-6560 被注释, 移至提交成功后; 范围同原 SQL)
        val terminal = jdbc.update(
            "UPDATE tc9u SET atc9u8='4' WHERE atc9u8='1' AND aaa030=? AND aae002=?",
            aaa030.toLong(), aae002N)
        return mapOf("code" to ok, "msg" to null as String?,
            "cbApplied" to cbApplied, "tzApplied" to tzApplied, "terminalApplied" to terminal)
    }

    // ---------- PRC_公积金封存回盘校验 ----------

    @Transactional
    fun fengcunCheck(aae002: String, aaa030: String, aaa020: String): Map<String, Any?> {
        val aae002N = aae002.toLongOrNull() ?: return err("公积金年月格式不正确|")
        // 原过程逐行校验(ABP002='封存' 匹配/账号非空)的 ACE10A 回写两处均被注释 → 净效果=文本清洗(忠实)
        val n = jdbc.update(
            """UPDATE tc9u_bp02 SET aac002 = $normId, aac003 = $normName, aaa001 = $normAcct
               WHERE aaa030=? AND aaa100=? AND aae002=?""",
            aaa030.toLong(), aaa020, aae002N)
        return mapOf("code" to ok, "msg" to null as String?, "cleaned" to n)
    }

    // ---------- PRC_公积金封存回盘提交 ----------

    @Transactional
    fun fengcunCommit(aae002: String, aaa030: String, aaa020: String): Map<String, Any?> {
        val aae002N = aae002.toLongOrNull() ?: return err("公积金年月格式不正确|")
        jdbc.update(
            """UPDATE tc9u_bp02 SET aac002 = $normId, aac003 = $normName, aaa001 = $normAcct, ace10a = null
               WHERE aaa030=? AND aaa100=? AND aae002=?""",
            aaa030.toLong(), aaa020, aae002N)
        data class Row(val atc9u0: Long, val aac002: String?, val aac003: String?,
                       val aaa001: String?, val abp002: String?)
        val rows = jdbc.query(
            """SELECT atc9u0, aac002, aac003, aaa001, abp002 FROM tc9u_bp02
               WHERE aaa030=? AND aaa100=? AND aae002=? ORDER BY atc9u0""",
            { rs, _ -> Row(rs.getLong("atc9u0"), rs.getString("aac002"), rs.getString("aac003"),
                rs.getString("aaa001"), rs.getString("abp002")) },
            aaa030.toLong(), aaa020, aae002N)
        var applied = 0
        var skipped = 0
        for (rec in rows) {
            if (rec.abp002 != "封存") continue
            class Skip : Exception()
            try {
                val t = jdbc.query(
                    """SELECT atc900, atc90w FROM tc90
                       WHERE atc900 = (SELECT max(a.atc900) FROM tc90 a JOIN ab01 b ON a.aab001=b.aab001
                                       WHERE a.aaa407='1' AND a.aae00p IN ('1','5','7')
                                         AND a.aaa120=? AND b.aaa030=? AND a.aac003=? AND a.aac002=?)""",
                    { rs, _ -> arrayOf((rs.getObject("atc900") as Number).toLong(), rs.getString("atc90w")) },
                    aaa020, aaa030.toLong(), rec.aac003, rec.aac002).firstOrNull()
                    ?: throw Skip()
                if (rec.aaa001.isNullOrBlank()) throw Skip()
                if (t[1] == "2") {
                    jdbc.update(
                        "UPDATE tc90 SET aaa004=?, atc90w=null WHERE atc900=?", rec.aaa001, t[0])
                } else {
                    jdbc.update("UPDATE tc90 SET aaa004=? WHERE atc900=?", rec.aaa001, t[0])
                }
                jdbc.update(
                    "UPDATE tc9u_bp02 SET ace10a='1', ace10b='新开户' WHERE atc9u0=?", rec.atc9u0)
                applied++
            } catch (e: Skip) {
                skipped++ // 原异常路径回写被注释 → 行保持 ACE10A=NULL(清洗后), 继续下一行
            }
        }
        return mapOf("code" to ok, "msg" to null as String?,
            "applied" to applied, "skipped" to skipped)
    }

    // ---------- PRC_公积金异议回盘校验 ----------

    @Transactional
    fun yiyiCheck(aae002: String, aaa030: String, aaa020: String): Map<String, Any?> {
        val aae002N = aae002.toLongOrNull() ?: return err("公积金年月格式不正确|")
        jdbc.update(
            """UPDATE tc9u_bp03 SET abp002 = $normBp2
               WHERE aaa030=? AND aaa020=? AND aae002=?""",
            aaa030.toLong(), aaa020, aae002N)
        data class Row(val atc9u0: Long, val abp002: String?, val abp003: String?)
        val rows = jdbc.query(
            "SELECT atc9u0, abp002, abp003 FROM tc9u_bp03 WHERE aaa030=? AND aaa020=? AND aae002=? ORDER BY atc9u0",
            { rs, _ -> Row(rs.getLong("atc9u0"), rs.getString("abp002"), rs.getString("abp003")) },
            aaa030.toLong(), aaa020, aae002N)
        var pass = 0
        var reject = 0
        for (rec in rows) {
            var nOk = "3"
            var msg: String? = null
            class Ck(m: String) : Exception(m)
            try {
                val t = matchHf(aaa020, rec.abp002)
                    ?: throw Ck("匹配员工信息出错|")
                var abp004 = "异议"
                if ((rec.abp003 ?: "").contains("100066835") && (rec.abp003 ?: "").contains("状态封存")) {
                    abp004 = "启封"
                    // 启封须查到封存信息(原: SELECT AAA001 INTO REC.AAA004 FROM TC9U_BP02,
                    // 源列=aaa001; 忠实: 查得值不在此持久化, 提交读 bp03.AAA004 由导入侧写入)
                    jdbc.query(
                        """SELECT aaa001 FROM tc9u_bp02
                           WHERE aaa001 IS NOT NULL AND aae002=? AND aac003=? AND aac002=? LIMIT 1""",
                        { rs, _ -> rs.getString("aaa001") }, aae002N, t["aac003"], t["aac002"]).firstOrNull()
                        ?: throw Ck("启封状态，但未查询到封存信息|")
                }
                jdbc.update(
                    """UPDATE tc9u_bp03 SET ace10a=?, atc900=?, aac002=?, aac003=?, abp004=?
                       WHERE atc9u0=?""",
                    nOk, t["atc900"], t["aac002"], t["aac003"], abp004, rec.atc9u0)
            } catch (ck: Ck) {
                nOk = "2"; msg = ck.message
                jdbc.update(
                    "UPDATE tc9u_bp03 SET ace10a=?, ace10b=? WHERE atc9u0=?", nOk, msg, rec.atc9u0)
            }
            if (nOk == "3") pass++ else reject++
        }
        return mapOf("code" to ok, "msg" to null as String?, "rows" to rows.size,
            "pass" to pass, "reject" to reject)
    }

    // ---------- PRC_公积金异议回盘提交 ----------

    @Transactional
    fun yiyiCommit(aae002: String, aaa030: String, aaa020: String, aae011: String): Map<String, Any?> {
        val aae002N = aae002.toLongOrNull() ?: return err("公积金年月格式不正确|")
        val user = jdbc.query(
            "SELECT aae011 FROM sys_org_user_unit WHERE aae011=?",
            { rs, _ -> rs.getString("aae011") }, aae011).firstOrNull()
            ?: return err("获取经办人信息出错")
        data class Row(val atc9u0: Long, val atc900: Long?, val abp003: String?,
                       val abp004: String?, val aaa004: String?)
        val rows = jdbc.query(
            """SELECT atc9u0, atc900, abp003, abp004, aaa004 FROM tc9u_bp03
               WHERE ace10a='3' AND aaa030=? AND aaa020=? AND aae002=? ORDER BY atc9u0""",
            { rs, _ -> Row(rs.getLong("atc9u0"), (rs.getObject("atc900") as Number?)?.toLong(),
                rs.getString("abp003"), rs.getString("abp004"), rs.getString("aaa004")) },
            aaa030.toLong(), aaa020, aae002N)
        var hfApplied = 0
        var yyApplied = 0
        for (cb in rows) {
            if (cb.abp004 == "启封") {
                if (cb.atc900 != null) {
                    jdbc.update(
                        "UPDATE tc9u SET atc9u1='2', atc9uy='2' WHERE aae002=? AND atc900=?", aae002N, cb.atc900)
                    jdbc.update("UPDATE tc90 SET aaa004=? WHERE atc900=?", cb.aaa004, cb.atc900)
                    hfApplied++
                }
            } else if (cb.abp004 == "异议") {
                val atc900 = cb.atc900
                    ?: return err("未查询到员工信息")
                val exists = jdbc.queryForObject(
                    "SELECT count(*) FROM tc90 WHERE atc900=?", Int::class.java, atc900) > 0
                if (!exists) return err("未查询到员工信息")
                val r = tdck.objectionAssign(atc900.toString(), "4", aae002, cb.abp003 ?: "")
                if ((r["code"] as Int) != ok) return r
                jdbc.update(
                    "UPDATE tc90 SET atc90w='2', aaa508=? WHERE atc900=?", aae002N, atc900)
                // 忠实物理 DELETE(旧系统同款); 铁律 8 改造清单项, 不顺手改
                jdbc.update("DELETE FROM tc9u_bp01 WHERE aae002=? AND atc900=?", aae002N, atc900)
                yyApplied++
            }
        }
        return mapOf("code" to ok, "msg" to null as String?,
            "hfApplied" to hfApplied, "yyApplied" to yyApplied)
    }
}

/** ---------- /proc 注册面（PKG_保险人员增减 公积金六过程, W1） ---------- */

@Component
class TdcgjCheckProc(private val c: TdcgjCases) : ProcHandler {
    override fun procName() = "PKG_保险人员增减.PRC_公积金异动回盘校验"
    override fun handle(params: Map<String, Any?>): Map<String, Any?> {
        val aae002 = params["aae002"]?.toString() ?: return mapOf("appCode" to -1, "errMsg" to "缺少 aae002")
        val aaa030 = params["aaa030"]?.toString() ?: return mapOf("appCode" to -1, "errMsg" to "缺少 aaa030")
        val aaa020 = params["aaa020"]?.toString() ?: return mapOf("appCode" to -1, "errMsg" to "缺少 aaa020")
        val r = c.gjCheck(aae002, aaa030, aaa020)
        return mapOf("appCode" to r["code"], "errMsg" to r["msg"],
            "rows" to r["rows"], "pass" to r["pass"], "reject" to r["reject"])
    }
}

@Component
class TdcgjCommitProc(private val c: TdcgjCases) : ProcHandler {
    override fun procName() = "PKG_保险人员增减.PRC_公积金异动回盘提交"
    override fun handle(params: Map<String, Any?>): Map<String, Any?> {
        val aae002 = params["aae002"]?.toString() ?: return mapOf("appCode" to -1, "errMsg" to "缺少 aae002")
        val aaa030 = params["aaa030"]?.toString() ?: return mapOf("appCode" to -1, "errMsg" to "缺少 aaa030")
        val aaa020 = params["aaa020"]?.toString() ?: return mapOf("appCode" to -1, "errMsg" to "缺少 aaa020")
        val aae011 = params["aae011"]?.toString() ?: return mapOf("appCode" to -1, "errMsg" to "缺少 aae011")
        val r = c.gjCommit(aae002, aaa030, aaa020, aae011)
        return mapOf("appCode" to r["code"], "errMsg" to r["msg"],
            "cbApplied" to r["cbApplied"], "tzApplied" to r["tzApplied"],
            "terminalApplied" to r["terminalApplied"])
    }
}

@Component
class TdcgjFcCheckProc(private val c: TdcgjCases) : ProcHandler {
    override fun procName() = "PKG_保险人员增减.PRC_公积金封存回盘校验"
    override fun handle(params: Map<String, Any?>): Map<String, Any?> {
        val aae002 = params["aae002"]?.toString() ?: return mapOf("appCode" to -1, "errMsg" to "缺少 aae002")
        val aaa030 = params["aaa030"]?.toString() ?: return mapOf("appCode" to -1, "errMsg" to "缺少 aaa030")
        val aaa020 = params["aaa020"]?.toString() ?: return mapOf("appCode" to -1, "errMsg" to "缺少 aaa020")
        val r = c.fengcunCheck(aae002, aaa030, aaa020)
        return mapOf("appCode" to r["code"], "errMsg" to r["msg"], "cleaned" to r["cleaned"])
    }
}

@Component
class TdcgjFcCommitProc(private val c: TdcgjCases) : ProcHandler {
    override fun procName() = "PKG_保险人员增减.PRC_公积金封存回盘提交"
    override fun handle(params: Map<String, Any?>): Map<String, Any?> {
        val aae002 = params["aae002"]?.toString() ?: return mapOf("appCode" to -1, "errMsg" to "缺少 aae002")
        val aaa030 = params["aaa030"]?.toString() ?: return mapOf("appCode" to -1, "errMsg" to "缺少 aaa030")
        val aaa020 = params["aaa020"]?.toString() ?: return mapOf("appCode" to -1, "errMsg" to "缺少 aaa020")
        val r = c.fengcunCommit(aae002, aaa030, aaa020)
        return mapOf("appCode" to r["code"], "errMsg" to r["msg"],
            "applied" to r["applied"], "skipped" to r["skipped"])
    }
}

@Component
class TdcgjYyCheckProc(private val c: TdcgjCases) : ProcHandler {
    override fun procName() = "PKG_保险人员增减.PRC_公积金异议回盘校验"
    override fun handle(params: Map<String, Any?>): Map<String, Any?> {
        val aae002 = params["aae002"]?.toString() ?: return mapOf("appCode" to -1, "errMsg" to "缺少 aae002")
        val aaa030 = params["aaa030"]?.toString() ?: return mapOf("appCode" to -1, "errMsg" to "缺少 aaa030")
        val aaa020 = params["aaa020"]?.toString() ?: return mapOf("appCode" to -1, "errMsg" to "缺少 aaa020")
        val r = c.yiyiCheck(aae002, aaa030, aaa020)
        return mapOf("appCode" to r["code"], "errMsg" to r["msg"],
            "rows" to r["rows"], "pass" to r["pass"], "reject" to r["reject"])
    }
}

@Component
class TdcgjYyCommitProc(private val c: TdcgjCases) : ProcHandler {
    override fun procName() = "PKG_保险人员增减.PRC_公积金异议回盘提交"
    override fun handle(params: Map<String, Any?>): Map<String, Any?> {
        val aae002 = params["aae002"]?.toString() ?: return mapOf("appCode" to -1, "errMsg" to "缺少 aae002")
        val aaa030 = params["aaa030"]?.toString() ?: return mapOf("appCode" to -1, "errMsg" to "缺少 aaa030")
        val aaa020 = params["aaa020"]?.toString() ?: return mapOf("appCode" to -1, "errMsg" to "缺少 aaa020")
        val aae011 = params["aae011"]?.toString() ?: return mapOf("appCode" to -1, "errMsg" to "缺少 aae011")
        val r = c.yiyiCommit(aae002, aaa030, aaa020, aae011)
        return mapOf("appCode" to r["code"], "errMsg" to r["msg"],
            "hfApplied" to r["hfApplied"], "yyApplied" to r["yyApplied"])
    }
}
