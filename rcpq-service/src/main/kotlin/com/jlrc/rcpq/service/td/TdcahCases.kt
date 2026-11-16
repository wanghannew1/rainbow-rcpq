package com.jlrc.rcpq.service.td

import com.jlrc.rcpq.service.calc.InsuranceCalcEngine
import com.jlrc.rcpq.service.proc.ProcHandler
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

/**
 * W2 省医保链（旧 PKG_保险人员增减 省医保五过程规格重写, 2026-11-17）。
 *
 * 源: rainbowpq/work/pkg_all/PACKAGE_BODY_PKG_保险人员增减.sql
 *   - PRC_省医保异动回盘校验     L2840-2998（TC9W_BP01 桶; TC9W2 置终态'4' **未注释=活**）
 *   - PRC_省医保异动回盘提交     L2999-3149（参保→AAA603/ATC90PA/AAA005/AAA608; 异议→SAFESIGN='2';
 *                                  停保消费 **TC9W2 名单表**(atc9w1='3')→AAA604/AAA609/ATC90PA='1'; 全批盖章）
 *   - PRC_省医保基数补差回盘校验 L3150-3232（TC9W_BP06 桶, 按 **AAA005 医保个人编号** 匹配+当月 TC93 校验）
 *   - PRC_省医保基数补差回盘提交 L3233-3371（重建 TJ0B2 工伤基数补差台账: 物理DELETE+DUP→UPDATE）
 *   - PRC_省医保工资表           L3682-3790（TC9W_BP07 重建, 调计算险金引擎; 回盘后报错守卫）
 *
 * 忠实点:
 *   - 校验内 TC9W2.ATC9W8='4' 置终态在旧系统**未被注释**（与公积金 W0#1 相反），原样保留在校验里;
 *   - 停保循环旧版从 TC90 扫描的实现被注释禁用, 活代码改为消费 TC9W2 名单（忠实采用活代码版）;
 *   - 补差提交开头的 DELETE TJ0B2 为物理删重建语义（铁律 8 改造清单, 不顺手改）;
 *   - 工资表守卫「当月数据已经回盘」与生成端共用（TC9W2 有 '4' 即拒绝）。
 */
@Component
class TdcahCases(
    private val jdbc: JdbcTemplate,
    private val tdck: TdckCases,
    private val engine: InsuranceCalcEngine,
    private val calcProcs: com.jlrc.rcpq.service.calc.CalcProcs,
) {
    private val ok = 1
    private val err = -1

    private fun err(msg: String) = mapOf("code" to err, "msg" to msg)

    private val normId = "replace(replace(replace(replace(replace(aac002, 'x', 'X'), chr(13), ''), chr(10), ''), chr(9), ''), ' ', '')"
    private val normName = "replace(replace(replace(replace(aac003, chr(13), ''), chr(10), ''), chr(9), ''), ' ', '')"

    /** 匹配省医保在保员工: max(atc900) of aaa402='1' aae00p 1/5/7 同医保属地(aaa110)同证件 */
    private fun matchSm(aaa020: String, idCard: String?): Map<String, Any?>? =
        jdbc.query(
            """SELECT atc900, atb930, aab001, aab004, atc90pa, aaa601, aac001 FROM tc90
               WHERE atc900 = (SELECT max(atc900) FROM tc90
                               WHERE aaa402='1' AND aae00p IN ('1','5','7')
                                 AND aaa110=? AND aac002=?)""",
            { rs, _ -> mapOf("atc900" to (rs.getObject("atc900") as Number).toLong(),
                "atb930" to (rs.getObject("atb930") as Number?)?.toLong(),
                "aab001" to (rs.getObject("aab001") as Number?)?.toLong(),
                "aab004" to rs.getString("aab004"), "atc90pa" to rs.getString("atc90pa"),
                "aaa601" to rs.getObject("aaa601"), "aac001" to rs.getString("aac001")) },
            aaa020, idCard).firstOrNull()?.takeIf { it["atc900"] != null }

    // ---------- PRC_省医保异动回盘校验 ----------

    @Transactional
    fun smCheck(aae002: String, aaa030: String, aaa020: String): Map<String, Any?> {
        val aae002N = aae002.toLongOrNull() ?: return err("省医保年月格式不正确|")
        jdbc.update(
            """UPDATE tc9w_bp01 SET aac002 = $normId, aac003 = $normName
               WHERE aaa030=? AND aaa020=? AND aae002=?""",
            aaa030.toLong(), aaa020, aae002N)
        // 忠实: 旧系统此处置终态语句未被注释(与公积金 W0#1 相反)——校验即整批置 '4'
        jdbc.update(
            "UPDATE tc9w2 SET atc9w8='4' WHERE atc9w8='1' AND aaa030=? AND aae002=?",
            aaa030.toLong(), aae002N)
        data class Row(val id: Long, val aac002: String?, val aaa90w: String?, val aaa001: String?,
                       val abp001: String?, val abo003: String?, val aae002r: String?)
        val rows = jdbc.query(
            """SELECT id, aac002, aaa90w, aaa001, abp001, abo003, aae002
               FROM tc9w_bp01 WHERE aaa030=? AND aaa020=? AND aae002=? ORDER BY id""",
            { rs, _ -> Row(rs.getLong("id"), rs.getString("aac002"), rs.getString("aaa90w"),
                rs.getString("aaa001"), rs.getString("abp001"), rs.getString("abo003"),
                rs.getString("aae002")) },
            aaa030.toLong(), aaa020, aae002N)
        var pass = 0
        var reject = 0
        for (rec in rows) {
            var nOk = "3"
            var msg: String? = null
            var atc900: Long? = null
            var atb930: Long? = null
            var aab001: Long? = null
            var aab004: String? = null
            var aae001: Long? = null
            var v: String? = null
            var bd1: java.math.BigDecimal? = null
            var bd2: java.math.BigDecimal? = null
            class Ck(m: String) : Exception(m)
            try {
                if (rec.aaa90w != "1" && rec.aaa90w != "2") throw Ck("申报结果格式不正确|")
                val t = matchSm(aaa020, rec.aac002) ?: throw Ck("匹配员工信息出错|")
                atc900 = t["atc900"] as Long
                atb930 = t["atb930"] as Long?
                aab001 = t["aab001"] as Long?
                aab004 = t["aab004"] as String?
                if (rec.aaa90w == "1") {
                    if (rec.aaa001.isNullOrBlank()) throw Ck("参保成功的人员必须填写保险个人编号|")
                    if (rec.aaa001.contains('.')) throw Ck("保险个人编号格式不正确|")
                    runCatching { java.time.YearMonth.parse(rec.abp001, java.time.format.DateTimeFormatter.ofPattern("yyyyMM")) }
                        .getOrElse { throw Ck("参保年月格式不正确|") }
                    // 是否退缴纳大额: 医保未参保且大病险=0 → 记 '14' + AA94 大病参数
                    // (忠实三值逻辑: TO_NUMBER(NULL)=NULL, NULL=0 为假 → abo003 空时不进大额分支)
                    val abo003N = runCatching { rec.abo003?.trim()?.toBigDecimal() }.getOrElse {
                        return err("异动回盘校验出错，错误信息：ORA-01722: invalid number，请将错误信息发给维护工程师") }
                    if (t["atc90pa"] == null && abo003N != null && abo003N.compareTo(java.math.BigDecimal.ZERO) == 0) {
                        v = "14"
                        bd1 = jdbc.query(
                            """SELECT aaa945 FROM aa94 WHERE aaa100=? AND aaa941='11'
                               AND aaa946<=? AND aaa947>=?""",
                            { rs, _ -> rs.getObject("aaa945") as java.math.BigDecimal },
                            aaa020, t["aaa601"], t["aaa601"]).firstOrNull()
                            ?: throw Ck("大病险参数设置有误|")
                        bd2 = bd1
                    }
                    // 本月新增: 医保未参保 → aae001=业务年月
                    if (t["atc90pa"] == null) aae001 = rec.aae002r?.toLongOrNull()
                }
                jdbc.update(
                    """UPDATE tc9w_bp01 SET ace10a=?, ace10b=?, aae001=?, atc900=?, atb930=?, atb931=null,
                       aab001=?, aab004=?, aaa90v=?, atc93bd_1=?, atc93bd_2=?, aaa201_1=null, aaa201_2=null
                       WHERE id=?""",
                    nOk, msg, aae001, atc900, atb930, aab001, aab004, v, bd1, bd2, rec.id)
            } catch (ck: Ck) {
                nOk = "2"; msg = ck.message
                jdbc.update("UPDATE tc9w_bp01 SET ace10a=?, ace10b=? WHERE id=?", nOk, msg, rec.id)
            }
            if (nOk == "3") pass++ else reject++
        }
        return mapOf("code" to ok, "msg" to null as String?, "rows" to rows.size,
            "pass" to pass, "reject" to reject)
    }

    // ---------- PRC_省医保异动回盘提交 ----------

    @Transactional
    fun smCommit(aae002: String, aaa030: String, aaa020: String, aae011: String): Map<String, Any?> {
        val aae002N = aae002.toLongOrNull() ?: return err("省医保年月格式不正确|")
        val user = jdbc.query(
            "SELECT aae011, aae017, aae019, aae020, aae022 FROM sys_org_user_unit WHERE aae011=?",
            { rs, _ -> arrayOf(rs.getString("aae011"), rs.getString("aae017"), rs.getString("aae019"),
                rs.getString("aae020"), rs.getString("aae022")) },
            aae011).firstOrNull() ?: return err("获取经办人信息出错")
        data class CbRow(val atc900: Long?, val aac003: String?, val aaa001: String?, val abp001: String?)
        val cbRows = jdbc.query(
            """SELECT atc900, aac003, aaa001, abp001 FROM tc9w_bp01
               WHERE ace10a='3' AND aaa90w='1' AND aaa030=? AND aaa020=? AND aae002=? ORDER BY id""",
            { rs, _ -> CbRow((rs.getObject("atc900") as Number?)?.toLong(), rs.getString("aac003"),
                rs.getString("aaa001"), rs.getString("abp001")) },
            aaa030.toLong(), aaa020, aae002N)
        var cbApplied = 0
        for (cb in cbRows) {
            val atc900 = cb.atc900 ?: return err("【${cb.aac003}】新参保更新出错")
            // V_AAE002=参保年月(abp001 yyyymm)→ AAA603/AAA203 numeric 列; 非数值=旧系统 ORA-01722
            val ym = cb.abp001?.toLongOrNull()
                ?: return err("异动回盘提交出错，错误信息：ORA-01722: invalid number，请将错误信息发给维护工程师")
            val nSpecial = jdbc.queryForObject(
                """SELECT count(*) FROM tc06 WHERE aae100='1' AND atc064 IN ('1','3')
                   AND (aae002=? OR (aae002<=? AND atc06c>=?))
                   AND aac001=(SELECT aac001 FROM tc90 WHERE atc900=?)""",
                Int::class.java, aae002N, aae002N, aae002N, atc900)
            if (nSpecial == 0) {
                val n = jdbc.update(
                    "UPDATE tc90 SET aaa603=?, atc90pa='0', aaa005=?, aaa608=? WHERE atc900=?",
                    ym, cb.aaa001, aae002N, atc900)
                if (n == 0) return err("【${cb.aac003}】新参保更新出错")
            }
            jdbc.update(
                "UPDATE tc90_yy SET cbsign='0', aaa203=? WHERE safesign='2' AND atc900=?", ym, atc900)
            cbApplied++
        }
        // 处理异议
        data class YyRow(val atc900: Long?, val aae013: String?)
        val yyRows = jdbc.query(
            """SELECT atc900, aae013 FROM tc9w_bp01
               WHERE ace10a='3' AND aaa90w='2' AND aaa030=? AND aaa020=? AND aae002=? ORDER BY id""",
            { rs, _ -> YyRow((rs.getObject("atc900") as Number?)?.toLong(), rs.getString("aae013")) },
            aaa030.toLong(), aaa020, aae002N)
        var yyApplied = 0
        for (yy in yyRows) {
            val atc900 = yy.atc900 ?: continue
            val r = tdck.objectionAssign(atc900.toString(), "2", aae002, yy.aae013 ?: "")
            if ((r["code"] as Int) != ok) return r
            jdbc.update("UPDATE tc90 SET atc90pa='2', aaa608=? WHERE atc900=?", aae002N, atc900)
            yyApplied++
        }
        // 处理停保(忠实活代码版: 消费 TC9W2 名单表 atc9w1='3', 旧 TC90 扫描版被注释禁用)
        data class TzRow(val atc900: Long?, val aac003: String?)
        val tzRows = jdbc.query(
            """SELECT atc900, aac003 FROM tc9w2
               WHERE atc9w1='3' AND aaa030=? AND aaa100=? AND aae002=?""",
            { rs, _ -> TzRow((rs.getObject("atc900") as Number?)?.toLong(), rs.getString("aac003")) },
            aaa030.toLong(), aaa020, aae002N)
        var tzApplied = 0
        for (tb in tzRows) {
            val atc900 = tb.atc900 ?: return err("【${tb.aac003}】新参保更新出错")
            val nSpecial = jdbc.queryForObject(
                """SELECT count(*) FROM tc06 WHERE aae100='1' AND atc064 IN ('1','2')
                   AND (aae002=? OR (aae002<=? AND atc06c>=?))
                   AND aac001=(SELECT aac001 FROM tc90 WHERE atc900=?)""",
                Int::class.java, aae002N, aae002N, aae002N, atc900)
            if (nSpecial == 0) {
                jdbc.update(
                    "UPDATE tc90 SET aaa604=?, aaa609=?, atc90pa='1' WHERE atc900=?", aae002N, aae002N, atc900)
                tzApplied++
            }
        }
        // 全批盖章经办人
        jdbc.update(
            """UPDATE tc9w_bp01 SET aae211=?, aae217=?, aae236=now(), aae219=?, aae220=?, aae222=?
               WHERE aaa030=? AND aaa020=? AND aae002=?""",
            user[0], user[1], user[2], user[3], user[4], aaa030.toLong(), aaa020, aae002N)
        return mapOf("code" to ok, "msg" to null as String?,
            "cbApplied" to cbApplied, "yyApplied" to yyApplied, "tzApplied" to tzApplied)
    }

    // ---------- PRC_省医保基数补差回盘校验 ----------

    @Transactional
    fun bjdcCheck(ace100: String): Map<String, Any?> {
        val ace100N = ace100.toLongOrNull() ?: return err("批次号格式不正确|")
        jdbc.update(
            """UPDATE tc9w_bp06 SET
               aaa005 = replace(replace(replace(replace(aaa005, chr(13), ''), chr(10), ''), chr(9), ''), ' ', ''),
               aae002 = replace(aae002, '.0', ''), aae003 = replace(aae003, '.0', '')
               WHERE ace100=?""", ace100N)
        data class Row(val id: Long, val aaa005: String?, val aae003: String?, val ace10a: String?)
        val rows = jdbc.query(
            """SELECT id, aaa005, aae003, ace10a FROM tc9w_bp06
               WHERE (ace10a IS NULL OR ace10a IN ('0','2','3','4')) AND ace100=? ORDER BY id""",
            { rs, _ -> Row(rs.getLong("id"), rs.getString("aaa005"), rs.getString("aae003"),
                rs.getString("ace10a")) }, ace100N)
        var pass = 0
        var reject = 0
        for (rec in rows) {
            var nOk = "3"
            var msg: String? = null
            class Ck(m: String) : Exception(m)
            try {
                val t = jdbc.query(
                    """SELECT atc900, atb930, aac001 FROM tc90
                       WHERE atc900 = (SELECT max(atc900) FROM tc90
                                       WHERE aaa402='1' AND aae00p IN ('1','5','7') AND aaa005=?)""",
                    { rs, _ -> arrayOf((rs.getObject("atc900") as Number).toLong(),
                        rs.getObject("atb930"), rs.getString("aac001")) },
                    rec.aaa005).firstOrNull() ?: throw Ck("根据医保个人编号未匹配到员工信息|")
                val atb930 = (t[1] as Number?)?.toLong()
                val aae003N = rec.aae003?.toLongOrNull()
                    ?: return err("异动回盘校验出错，错误信息：ORA-01722: invalid number，请将错误信息发给维护工程师")
                // tc93.aac001 为 numeric 簇(与 tc90.aac001 varchar 不同簇) — 铁律: 分簇绑定
                val aac001N = (t[2] as String?)?.toLongOrNull()
                    ?: return err("异动回盘校验出错，错误信息：ORA-01722: invalid number，请将错误信息发给维护工程师")
                jdbc.query(
                    "SELECT atc900 FROM tc93 WHERE atc931=? AND atc937='1' AND atb930=? AND aac001=?",
                    { rs, _ -> rs.getLong("atc900") }, aae003N, atb930, aac001N).firstOrNull()
                    ?: throw Ck("当月工资未查询到|")
                jdbc.update("UPDATE tc9w_bp06 SET ace10a=?, ace10b=? WHERE id=?", nOk, msg, rec.id)
            } catch (ck: Ck) {
                nOk = "2"; msg = ck.message
                jdbc.update("UPDATE tc9w_bp06 SET ace10a=?, ace10b=? WHERE id=?", nOk, msg, rec.id)
            }
            if (nOk == "3") pass++ else reject++
        }
        return mapOf("code" to ok, "msg" to null as String?, "rows" to rows.size,
            "pass" to pass, "reject" to reject)
    }

    // ---------- PRC_省医保基数补差回盘提交 ----------

    @Transactional
    fun bjdcCommit(ace100: String, aae002: String, aaa020: String, aaa030: String, aae011: String): Map<String, Any?> {
        val ace100N = ace100.toLongOrNull() ?: return err("批次号格式不正确|")
        val aae002N = aae002.toLongOrNull() ?: return err("省医保年月格式不正确|")
        val user = jdbc.query(
            "SELECT aae011, aae017, aae019, aae020, aae022 FROM sys_org_user_unit WHERE aae011=?",
            { rs, _ -> arrayOf(rs.getString("aae011"), rs.getString("aae017"), rs.getString("aae019"),
                rs.getString("aae020"), rs.getString("aae022")) },
            aae011).firstOrNull() ?: return err("获取经办人信息出错")
        // 重建语义: 先物理删该属地+户头+年月旧台账(忠实, 铁律 8 改造清单)
        jdbc.update(
            "DELETE FROM tj0b2 WHERE aaa020=? AND aaa030=? AND aae002=?", aaa020, aaa030.toLong(), aae002N)
        data class Row(val id: Long, val aaa005: String?, val aae003: String?, val aaa020r: String?,
                       val aaa021r: String?, val aaa030r: Long?, val aaa031r: String?,
                       val abp002: String?, val abp003: String?, val abp004: String?, val abp005: String?,
                       val abp006: String?, val abp007: String?, val abp008: String?)
        val rows = jdbc.query(
            """SELECT id, aaa005, aae003, aaa020, aaa021, aaa030, aaa031,
               abp002, abp003, abp004, abp005, abp006, abp007, abp008
               FROM tc9w_bp06 WHERE ace10a='3' AND ace100=? ORDER BY id""",
            { rs, _ -> Row(rs.getLong("id"), rs.getString("aaa005"), rs.getString("aae003"),
                rs.getString("aaa020"), rs.getString("aaa021"),
                (rs.getObject("aaa030") as Number?)?.toLong(), rs.getString("aaa031"),
                rs.getString("abp002"), rs.getString("abp003"), rs.getString("abp004"),
                rs.getString("abp005"), rs.getString("abp006"), rs.getString("abp007"),
                rs.getString("abp008")) }, ace100N)
        var applied = 0
        for (r in rows) {
            var nOk = "1"
            var msg: String? = null
            class Ck(m: String) : Exception(m)
            try {
                val t = jdbc.query(
                    """SELECT atc900, atb930, aab001, aab004, aac001, aac003, aac002, aaa005 FROM tc90
                       WHERE atc900 = (SELECT max(atc900) FROM tc90
                                       WHERE aaa402='1' AND aae00p IN ('1','5','7') AND aaa005=?)""",
                    { rs, _ -> arrayOf((rs.getObject("atc900") as Number).toLong(),
                        rs.getObject("atb930"), rs.getObject("aab001"), rs.getString("aab004"),
                        rs.getString("aac001"), rs.getString("aac003"), rs.getString("aac002"),
                        rs.getString("aaa005")) }, r.aaa005).firstOrNull()
                    ?: throw Ck("匹配员工信息出错|")
                val atb930 = (t[1] as Number?)?.toLong()
                val aae003N = r.aae003?.toLongOrNull()
                    ?: return err("异动回盘提交出错，错误信息：ORA-01722: invalid number，请将错误信息发给维护工程师")
                // tj0b2.aac001/tc93.aac001 同 numeric 簇 — 转换后绑定
                val aac001N = (t[4] as String?)?.toLongOrNull()
                    ?: return err("异动回盘提交出错，错误信息：ORA-01722: invalid number，请将错误信息发给维护工程师")
                val tc93 = jdbc.query(
                    """SELECT atc900, atb930, atb931 FROM tc93
                       WHERE atc931=? AND atc937='1' AND atb930=? AND aac001=?""",
                    { rs, _ -> arrayOf((rs.getObject("atc900") as Number).toLong(),
                        rs.getObject("atb930"), rs.getString("atb931")) }, aae003N, atb930, aac001N).firstOrNull()
                    ?: throw Ck("当月工资未查询到|")
                // bp06 金额列 varchar → tj0b2 numeric(旧系统 INSERT 隐式 TO_NUMBER, 失败=ORA-01722)
                fun dec(s: String?): java.math.BigDecimal? = s?.trim()?.toBigDecimalOrNull()
                val n302 = dec(r.abp002) ?: return err("异动回盘提交出错，错误信息：ORA-01722: invalid number，请将错误信息发给维护工程师")
                val n002 = dec(r.abp004) ?: return err("异动回盘提交出错，错误信息：ORA-01722: invalid number，请将错误信息发给维护工程师")
                val n006 = dec(r.abp006) ?: return err("异动回盘提交出错，错误信息：ORA-01722: invalid number，请将错误信息发给维护工程师")
                val n007 = dec(r.abp007) ?: return err("异动回盘提交出错，错误信息：ORA-01722: invalid number，请将错误信息发给维护工程师")
                val n003 = dec(r.abp003) ?: return err("异动回盘提交出错，错误信息：ORA-01722: invalid number，请将错误信息发给维护工程师")
                val n005 = dec(r.abp005) ?: return err("异动回盘提交出错，错误信息：ORA-01722: invalid number，请将错误信息发给维护工程师")
                val n008 = dec(r.abp008) ?: return err("异动回盘提交出错，错误信息：ORA-01722: invalid number，请将错误信息发给维护工程师")
                // DUP→UPDATE 等价: PK(tj0b2)=aae002+aae003+atc900, ON CONFLICT 更新 9 个金额列
                jdbc.update(
                    """INSERT INTO tj0b2 (aae002, aae003, atj000, atc900, aaa001, aac001, aac003, aac002,
                       aaa020, aaa021, aab001, aab004, atb930, atb931, aaa030, aaa031,
                       aaa302, aaa305, aaa306, bba002, bba004, bba005, baa002, atb90t, atj0b5,
                       aae011, aae017, aae036, aae019, aae020, aae022)
                       VALUES (?, ?, '1', ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?,
                       ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, now(), ?, ?, ?)
                       ON CONFLICT (aae002, aae003, atc900) DO UPDATE SET
                       aaa302=EXCLUDED.aaa302, aaa305=EXCLUDED.aaa305, aaa306=EXCLUDED.aaa306,
                       bba002=EXCLUDED.bba002, bba004=EXCLUDED.bba004, bba005=EXCLUDED.bba005,
                       baa002=EXCLUDED.baa002, atb90t=EXCLUDED.atb90t, atj0b5=EXCLUDED.atj0b5""",
                    aae002N, aae003N, tc93[0], t[7], aac001N, t[5], t[6],
                    r.aaa020r, r.aaa021r, t[2], t[3], tc93[1], tc93[2],
                    r.aaa030r, r.aaa031r,
                    n302, n302, n302, n002, n006, n007, n003,
                    n005, n008,
                    user[0], user[1], user[2], user[3], user[4])
                jdbc.update("UPDATE tc9w_bp06 SET ace10a=?, ace10b=? WHERE id=?", nOk, msg, r.id)
                applied++
            } catch (ck: Ck) {
                nOk = "2"; msg = ck.message
                jdbc.update("UPDATE tc9w_bp06 SET ace10a=?, ace10b=? WHERE id=?", nOk, msg, r.id)
            }
        }
        return mapOf("code" to ok, "msg" to null as String?, "applied" to applied)
    }

    // ---------- PRC_省医保工资表 ----------

    @Transactional
    fun gzbiao(aaa030: String, aae002: String, aae011: String, aaa110: String): Map<String, Any?> {
        val aae002N = aae002.toLongOrNull() ?: return err("省医保年月格式不正确|")
        val aa03 = jdbc.query(
            "SELECT aaa030, aaa031 FROM aa03 WHERE aaa030=?",
            { rs, _ -> arrayOf((rs.getObject("aaa030") as Number).toLong(), rs.getString("aaa031")) },
            aaa030.toLong()).firstOrNull() ?: return err("查询险金户头名称出错")
        val user = jdbc.query(
            "SELECT aae011, aae017, aae019, aae020, aae022 FROM sys_org_user_unit WHERE aae011=?",
            { rs, _ -> arrayOf(rs.getString("aae011"), rs.getString("aae017"), rs.getString("aae019"),
                rs.getString("aae020"), rs.getString("aae022")) },
            aae011).firstOrNull() ?: return err("查询经办用户出错")
        val done = jdbc.queryForObject(
            "SELECT count(*) FROM tc9w2 WHERE atc9w8='4' AND aaa030=? AND aae002=?",
            Int::class.java, aaa030.toLong(), aae002N)
        if (done > 0) return err("当月数据已经回盘，不能重新生成，请点击查询查看历史数据")
        jdbc.update(
            "DELETE FROM tc9w_bp07 WHERE aaa030=? AND aaa020=? AND aae002=?",
            aaa030.toLong(), aaa110, aae002N)
        data class R(val atc900: Long, val aac003: String?, val aaa110r: String?, val aaa111r: String?,
                     val atc905: String?)
        val list = jdbc.query(
            """SELECT a.atc900, a.aac003, a.aaa110, a.aaa111, a.atc905
               FROM tc90 a JOIN ab01 b ON a.aab001=b.aab001
               WHERE b.aaa030=? AND a.atb90r='3' AND a.aaa110=? AND a.atc90pa IS NULL
                 AND a.aae002=? AND a.aaa402='1' ORDER BY a.atc900""",
            { rs, _ -> R((rs.getObject("atc900") as Number).toLong(), rs.getString("aac003"),
                rs.getString("aaa110"), rs.getString("aaa111"), rs.getString("atc905")) },
            aaa030.toLong(), aaa110, aae002N)
        var n = 0
        for (r in list) {
            val row = calcProcs.loadTc90(r.atc900)
                ?: return err("计算险金信息出错：合同不存在")
            val (code, result) = engine.calc(row, aae002, InsuranceCalcEngine.Tc01())
            if (code == -1) return err("计算险金信息出错：$result")
            val tc01 = result as InsuranceCalcEngine.Tc01
            fun nv(v: java.math.BigDecimal?) = v ?: java.math.BigDecimal.ZERO
            val abp003 = nv(tc01.baa001)
            val abp004 = nv(tc01.baa003)
            val abp005 = nv(tc01.baa002)
            val abp006 = nv(tc01.caa002)
            val abp007 = abp003.add(abp004).add(abp005).add(abp006)
            val abp001 = nv(r.atc905?.toBigDecimalOrNull())
            val abp008 = abp001.subtract(abp007)
            jdbc.update(
                """INSERT INTO tc9w_bp07 (baz001, baz002, id, aae002, aaa030, aaa031, aaa020, aaa021,
                   aac003, abp001, abp002, abp003, abp004, abp005, abp006, abp007, abp008,
                   aae011, aae017, aae036, aae019, aae020, aae022)
                   VALUES (nextval('seq_sys_baz001'), nextval('seq_sys_baz002'), nextval('seq_tc9w_bp07_id'),
                   ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, now(), ?, ?, ?)""",
                aae002N, aa03[0], aa03[1], r.aaa110r, r.aaa111r, r.aac003,
                r.atc905, r.atc905,
                abp003, abp004, abp005, abp006, abp007, abp008,
                user[0], user[1], user[2], user[3], user[4])
            n++
        }
        return mapOf("code" to ok, "msg" to null as String?, "rows" to n)
    }
}

/** ---------- /proc 注册面（PKG_保险人员增减 省医保五过程, W2） ---------- */

@Component
class TdcahCheckProc(private val c: TdcahCases) : ProcHandler {
    override fun procName() = "PKG_保险人员增减.PRC_省医保异动回盘校验"
    override fun handle(params: Map<String, Any?>): Map<String, Any?> {
        val aae002 = params["aae002"]?.toString() ?: return mapOf("appCode" to -1, "errMsg" to "缺少 aae002")
        val aaa030 = params["aaa030"]?.toString() ?: return mapOf("appCode" to -1, "errMsg" to "缺少 aaa030")
        val aaa020 = params["aaa020"]?.toString() ?: return mapOf("appCode" to -1, "errMsg" to "缺少 aaa020")
        val r = c.smCheck(aae002, aaa030, aaa020)
        return mapOf("appCode" to r["code"], "errMsg" to r["msg"],
            "rows" to r["rows"], "pass" to r["pass"], "reject" to r["reject"])
    }
}

@Component
class TdcahCommitProc(private val c: TdcahCases) : ProcHandler {
    override fun procName() = "PKG_保险人员增减.PRC_省医保异动回盘提交"
    override fun handle(params: Map<String, Any?>): Map<String, Any?> {
        val aae002 = params["aae002"]?.toString() ?: return mapOf("appCode" to -1, "errMsg" to "缺少 aae002")
        val aaa030 = params["aaa030"]?.toString() ?: return mapOf("appCode" to -1, "errMsg" to "缺少 aaa030")
        val aaa020 = params["aaa020"]?.toString() ?: return mapOf("appCode" to -1, "errMsg" to "缺少 aaa020")
        val aae011 = params["aae011"]?.toString() ?: return mapOf("appCode" to -1, "errMsg" to "缺少 aae011")
        val r = c.smCommit(aae002, aaa030, aaa020, aae011)
        return mapOf("appCode" to r["code"], "errMsg" to r["msg"],
            "cbApplied" to r["cbApplied"], "yyApplied" to r["yyApplied"], "tzApplied" to r["tzApplied"])
    }
}

@Component
class TdcahBjCheckProc(private val c: TdcahCases) : ProcHandler {
    override fun procName() = "PKG_保险人员增减.PRC_省医保基数补差回盘校验"
    override fun handle(params: Map<String, Any?>): Map<String, Any?> {
        val ace100 = params["ace100"]?.toString() ?: return mapOf("appCode" to -1, "errMsg" to "缺少 ace100")
        val r = c.bjdcCheck(ace100)
        return mapOf("appCode" to r["code"], "errMsg" to r["msg"],
            "rows" to r["rows"], "pass" to r["pass"], "reject" to r["reject"])
    }
}

@Component
class TdcahBjCommitProc(private val c: TdcahCases) : ProcHandler {
    override fun procName() = "PKG_保险人员增减.PRC_省医保基数补差回盘提交"
    override fun handle(params: Map<String, Any?>): Map<String, Any?> {
        for (k in listOf("ace100", "aae002", "aaa020", "aaa030", "aae011"))
            if (params[k] == null) return mapOf("appCode" to -1, "errMsg" to "缺少 $k")
        val r = c.bjdcCommit(params["ace100"]!!.toString(), params["aae002"]!!.toString(),
            params["aaa020"]!!.toString(), params["aaa030"]!!.toString(), params["aae011"]!!.toString())
        return mapOf("appCode" to r["code"], "errMsg" to r["msg"], "applied" to r["applied"])
    }
}

@Component
class TdcahGzbProc(private val c: TdcahCases) : ProcHandler {
    override fun procName() = "PKG_保险人员增减.PRC_省医保工资表"
    override fun handle(params: Map<String, Any?>): Map<String, Any?> {
        for (k in listOf("aaa030", "aae002", "aae011", "aaa110"))
            if (params[k] == null) return mapOf("appCode" to -1, "errMsg" to "缺少 $k")
        val r = c.gzbiao(params["aaa030"]!!.toString(), params["aae002"]!!.toString(),
            params["aae011"]!!.toString(), params["aaa110"]!!.toString())
        return mapOf("appCode" to r["code"], "errMsg" to r["msg"], "rows" to r["rows"])
    }
}
