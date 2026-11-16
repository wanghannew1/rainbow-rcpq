package com.jlrc.rcpq.service.td

import com.jlrc.rcpq.service.proc.ProcHandler
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

/**
 * W3a 省工伤链（旧 PKG_保险人员增减 省工伤六过程规格重写, 2026-11-17）。
 *
 * 源: rainbowpq/work/pkg_all/PACKAGE_BODY_PKG_保险人员增减.sql
 *   - PRC_省工伤异动回盘校验     L7809-7939（TC9W_BP09 桶; 清洗**无 WHERE=全表清洗**(忠实);
 *                                  TC9W3 置终态'4'未注释=活, 按 aaa100+aae002 范围）
 *   - PRC_省工伤异动回盘提交     L7940-8104（参保→AAA903/ATC90PB 状态机/AAA009/AAA305 覆写;
 *                                  bp09 **提交翻 ace10a='1'**(省医保不翻); 异议 SAFESIGN='3';
 *                                  停保消费 TC9W3(atc9w1='3')→AAA904/AAA909/ATC90PB='1'）
 *   - PRC_省工伤减免校验         L8898-9032（TC09_BP 桶, 按 **AAA009 编号+AAA402 医保旗标**匹配(旧复制粘贴系),
 *                                  姓名/额度 BBA004/合计 ATC9EH 校验; 无外层 OTHERS 包装(忠实)）
 *   - PRC_省工伤减免提交         L9033-9161（先调校验; 物理 DELETE TC09 重建→INSERT, DUP→跳过）
 *   - PRC_省工伤基数补差回盘校验 L3372-3502（TC9W_BP08 桶, AAA009 匹配; 单月所属期查**前一月**工资）
 *   - PRC_省工伤基数补差回盘提交 L3503-3681（TJ0B3 跨年桶: 所属期区间逐月摊分 BBA004/N_YS;
 *                                  旧 DELETE TJ0B3 被注释=累积模式, DUP→UPDATE(bba004/atj0b5);
 *                                  TC93 无月时以 TC90 合成占位(其 ATB931 误取单位名, 忠实)）
 *
 * W0#3 裁决落地: 人员增减报减/报增分支性别码 SELECT INTO 无兜底(民族/户口有) — 新系统推送侧
 * 行级兜底: 性别码取不到 → 跳过该行并在 skipped 报姓名, 其余行正常生成（见 HzController.pushGongshang）。
 */
@Component
class TdcbpCases(
    private val jdbc: JdbcTemplate,
    private val tdck: TdckCases,
) {
    private val ok = 1
    private val err = -1

    private fun err(msg: String) = mapOf("code" to err, "msg" to msg)

    private val normId = "replace(replace(replace(replace(replace(aac002, 'x', 'X'), chr(13), ''), chr(10), ''), chr(9), ''), ' ', '')"
    private val normName = "replace(replace(replace(replace(aac003, chr(13), ''), chr(10), ''), chr(9), ''), ' ', '')"

    private fun dec(s: String?, onFail: String): java.math.BigDecimal? =
        s?.trim()?.toBigDecimalOrNull() ?: run { null }

    /** 省工伤匹配: AAA404='1' aae00p 1/5/7 同工伤属地(aaa130) */
    private fun matchGs(aaa020: String, idCard: String?): Map<String, Any?>? =
        jdbc.query(
            """SELECT atc900, atb930, aab001, aab004, atc90pb, aac001, aac003, aac008 FROM tc90
               WHERE atc900 = (SELECT max(atc900) FROM tc90
                               WHERE aaa404='1' AND aae00p IN ('1','5','7')
                                 AND aaa130=? AND aac002=?)""",
            { rs, _ -> mapOf("atc900" to (rs.getObject("atc900") as Number).toLong(),
                "atb930" to (rs.getObject("atb930") as Number?)?.toLong(),
                "aab001" to (rs.getObject("aab001") as Number?)?.toLong(),
                "aab004" to rs.getString("aab004"), "atc90pb" to rs.getString("atc90pb"),
                "aac001" to rs.getString("aac001"), "aac003" to rs.getString("aac003"),
                "aac008" to rs.getString("aac008")) },
            aaa020, idCard).firstOrNull()?.takeIf { it["atc900"] != null }

    /** 费款所属期解析: 'yyyymm-yyyymm' 区间 / 单月 → 起止 LocalDate(singleAdj=true 时单月=前一月, 校验忠实) */
    private fun periodRange(raw: String?, singleAdj: Boolean): Pair<java.time.LocalDate, java.time.LocalDate>? {
        val s = raw?.trim() ?: return null
        val fmt = java.time.format.DateTimeFormatter.ofPattern("yyyyMM")
        return try {
            if (s.contains('-')) {
                val a = java.time.YearMonth.parse(s.substringBefore('-'), fmt).atDay(1)
                val b = java.time.YearMonth.parse(s.substringAfter('-'), fmt).atDay(1)
                a to b
            } else {
                val d = java.time.YearMonth.parse(s, fmt).atDay(1)
                if (singleAdj) d.minusMonths(1) to d.minusMonths(1) else d to d
            }
        } catch (e: Exception) { null }
    }

    // ---------- PRC_省工伤异动回盘校验 ----------

    @Transactional
    fun sbCheck(aae002: String, aaa030: String, aaa020: String): Map<String, Any?> {
        val aae002N = aae002.toLongOrNull() ?: return err("省工伤年月格式不正确|")
        // 忠实: 旧清洗 UPDATE 无 WHERE = 全表清洗(L7827-7841)
        jdbc.update(
            """UPDATE tc9w_bp09 SET aac002 = $normId, aac003 = $normName,
               abp001 = replace(abp001, '.0', '')""")
        // 忠实: 旧系统此置终态未注释(活), 范围含 aaa100(属地)
        jdbc.update(
            "UPDATE tc9w3 SET atc9w8='4' WHERE atc9w8='1' AND aaa030=? AND aaa100=? AND aae002=?",
            aaa030.toLong(), aaa020, aae002N)
        data class Row(val id: Long, val aac002: String?, val aaa90w: String?, val aaa001: String?,
                       val abp001: String?, val aae002r: String?)
        val rows = jdbc.query(
            """SELECT id, aac002, aaa90w, aaa001, abp001, aae002 FROM tc9w_bp09
               WHERE aaa030=? AND aaa020=? AND aae002=? ORDER BY id""",
            { rs, _ -> Row(rs.getLong("id"), rs.getString("aac002"), rs.getString("aaa90w"),
                rs.getString("aaa001"), rs.getString("abp001"), rs.getString("aae002")) },
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
            class Ck(m: String) : Exception(m)
            try {
                if (rec.aaa90w != "1" && rec.aaa90w != "2") throw Ck("申报结果格式不正确|")
                val t = matchGs(aaa020, rec.aac002) ?: throw Ck("匹配员工信息出错|")
                atc900 = t["atc900"] as Long
                atb930 = t["atb930"] as Long?
                aab001 = t["aab001"] as Long?
                aab004 = t["aab004"] as String?
                if (rec.aaa90w == "1") {
                    if (rec.aaa001.isNullOrBlank()) throw Ck("参保成功的人员必须填写保险个人编号|")
                    if (rec.aaa001.contains('.')) throw Ck("保险个人编号格式不正确|")
                    runCatching { java.time.YearMonth.parse(rec.abp001, java.time.format.DateTimeFormatter.ofPattern("yyyyMM")) }
                        .getOrElse { throw Ck("参保年月格式不正确|") }
                    if (t["atc90pb"] == null) aae001 = rec.aae002r?.toLongOrNull()
                }
                jdbc.update(
                    """UPDATE tc9w_bp09 SET ace10a=?, ace10b=?, aae001=?, atc900=?, atb930=?, atb931=null,
                       aab001=?, aab004=?, aaa90v=null, atc93bd_1=null, atc93bd_2=null,
                       aaa201_1=null, aaa201_2=null WHERE id=?""",
                    nOk, msg, aae001, atc900, atb930, aab001, aab004, rec.id)
            } catch (ck: Ck) {
                nOk = "2"; msg = ck.message
                jdbc.update("UPDATE tc9w_bp09 SET ace10a=?, ace10b=? WHERE id=?", nOk, msg, rec.id)
            }
            if (nOk == "3") pass++ else reject++
        }
        return mapOf("code" to ok, "msg" to null as String?, "rows" to rows.size,
            "pass" to pass, "reject" to reject)
    }

    // ---------- PRC_省工伤异动回盘提交 ----------

    @Transactional
    fun sbCommit(aae002: String, aaa030: String, aaa020: String, aae011: String): Map<String, Any?> {
        val aae002N = aae002.toLongOrNull() ?: return err("省工伤年月格式不正确|")
        val user = jdbc.query(
            "SELECT aae011, aae017, aae019, aae020, aae022 FROM sys_org_user_unit WHERE aae011=?",
            { rs, _ -> arrayOf(rs.getString("aae011"), rs.getString("aae017"), rs.getString("aae019"),
                rs.getString("aae020"), rs.getString("aae022")) },
            aae011).firstOrNull() ?: return err("获取经办人信息出错")
        data class CbRow(val id: Long, val atc900: Long?, val aac003: String?, val aaa001: String?,
                         val abp001: String?, val abo005: String?)
        val cbRows = jdbc.query(
            """SELECT id, atc900, aac003, aaa001, abp001, abo005 FROM tc9w_bp09
               WHERE ace10a='3' AND aaa90w='1' AND aaa030=? AND aaa020=? AND aae002=? ORDER BY id""",
            { rs, _ -> CbRow(rs.getLong("id"), (rs.getObject("atc900") as Number?)?.toLong(),
                rs.getString("aac003"), rs.getString("aaa001"), rs.getString("abp001"),
                rs.getString("abo005")) },
            aaa030.toLong(), aaa020, aae002N)
        var cbApplied = 0
        for (cb in cbRows) {
            val atc900 = cb.atc900 ?: return err("【${cb.aac003}】新参保更新出错")
            val ym = cb.abp001?.toLongOrNull()
                ?: return err("异动回盘提交出错，错误信息：ORA-01722: invalid number，请将错误信息发给维护工程师")
            val nSpecial = jdbc.queryForObject(
                """SELECT count(*) FROM tc06 WHERE aae100='1' AND atc064 IN ('1','3')
                   AND (aae002=? OR (aae002<=? AND atc06c>=?))
                   AND aac001=(SELECT aac001 FROM tc90 WHERE atc900=?)""",
                Int::class.java, aae002N, aae002N, aae002N, atc900)
            if (nSpecial == 0) {
                // AAA305 覆写: NVL(abo005,0)>0 → TO_NUMBER(abo005)（非数值=旧 ORA-01722 → 整批 -1）
                val abo005N = runCatching { cb.abo005?.trim()?.toBigDecimal() }.getOrElse {
                    return err("异动回盘提交出错，错误信息：ORA-01722: invalid number，请将错误信息发给维护工程师") }
                val n = jdbc.update(
                    """UPDATE tc90 SET
                       aaa903 = ?,
                       atc90pb = CASE
                         WHEN atc90pb IS NULL AND (aaa902 IS NULL OR aaa902 > ?) THEN '0'
                         WHEN atc90pb IS NULL AND (aaa902 = ?) THEN '1'
                         ELSE '0' END,
                       aaa909 = CASE
                         WHEN atc90pb IS NULL AND (aaa902 IS NULL OR aaa902 > ?) THEN NULL
                         WHEN atc90pb IS NULL AND (aaa902 = ?) THEN ?
                         ELSE NULL END,
                       aaa009 = ?, aaa908 = ?,
                       aaa305 = CASE WHEN coalesce(?, 0) > 0 THEN ? ELSE aaa305 END
                       WHERE atc900=?""",
                    ym, aae002N, aae002N, aae002N, aae002N, aae002N,
                    cb.aaa001, aae002N, abo005N, abo005N, atc900)
                if (n == 0) return err("【${cb.aac003}】新参保更新出错")
            }
            // 省工伤: 提交翻 ace10a='1'(省医保不翻 — 桶间差异忠实)
            jdbc.update("UPDATE tc9w_bp09 SET ace10a='1' WHERE id=?", cb.id)
            jdbc.update(
                "UPDATE tc90_yy SET cbsign='0', aaa203=? WHERE safesign='3' AND atc900=?", ym, atc900)
            cbApplied++
        }
        // 异议
        data class YyRow(val atc900: Long?, val aae013: String?)
        val yyRows = jdbc.query(
            """SELECT atc900, aae013 FROM tc9w_bp09
               WHERE ace10a='3' AND aaa90w='2' AND aaa030=? AND aaa020=? AND aae002=? ORDER BY id""",
            { rs, _ -> YyRow((rs.getObject("atc900") as Number?)?.toLong(), rs.getString("aae013")) },
            aaa030.toLong(), aaa020, aae002N)
        var yyApplied = 0
        for (yy in yyRows) {
            val atc900 = yy.atc900 ?: continue
            val r = tdck.objectionAssign(atc900.toString(), "3", aae002, yy.aae013 ?: "")
            if ((r["code"] as Int) != ok) return r
            jdbc.update("UPDATE tc90 SET atc90pb='2', aaa908=? WHERE atc900=?", aae002N, atc900)
            yyApplied++
        }
        // 停保(消费 TC9W3 报减行)
        data class TzRow(val atc900: Long?, val aac003: String?)
        val tzRows = jdbc.query(
            """SELECT atc900, aac003 FROM tc9w3
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
                    "UPDATE tc90 SET aaa904=?, aaa909=?, atc90pb='1' WHERE atc900=?",
                    aae002N, aae002N, atc900)
                tzApplied++
            }
        }
        jdbc.update(
            """UPDATE tc9w_bp09 SET aae211=?, aae217=?, aae236=now(), aae219=?, aae220=?, aae222=?
               WHERE aaa030=? AND aaa020=? AND aae002=?""",
            user[0], user[1], user[2], user[3], user[4], aaa030.toLong(), aaa020, aae002N)
        return mapOf("code" to ok, "msg" to null as String?,
            "cbApplied" to cbApplied, "yyApplied" to yyApplied, "tzApplied" to tzApplied)
    }

    // ---------- PRC_省工伤减免校验 ----------

    @Transactional
    fun jmCheck(ace100: String): Map<String, Any?> {
        val ace100N = ace100.toLongOrNull() ?: return err("批次号格式不正确|")
        jdbc.update(
            """UPDATE tc09_bp SET
               aac002 = $normId, aac003 = $normName,
               atb931 = replace(replace(replace(replace(atb931, chr(13), ''), chr(10), ''), chr(9), ''), ' ', ''),
               aaa005 = replace(replace(replace(replace(aaa005, chr(13), ''), chr(10), ''), chr(9), ''), ' ', ''),
               atc900 = NULL WHERE ace100=?""", ace100N)
        data class Row(val id: Long, val aaa005: String?, val aac003: String?, val aae002r: Long?,
                       val bba004: String?, val atc9eh: String?)
        val rows = jdbc.query(
            """SELECT id, aaa005, aac003, aae002, bba004, atc9eh FROM tc09_bp
               WHERE (ace10a IS NULL OR ace10a='2') AND ace100=? ORDER BY id""",
            { rs, _ -> Row(rs.getLong("id"), rs.getString("aaa005"), rs.getString("aac003"),
                (rs.getObject("aae002") as Number?)?.toLong(), rs.getString("bba004"),
                rs.getString("atc9eh")) }, ace100N)
        var pass = 0
        var reject = 0
        for (rec in rows) {
            var nOk = "3"
            var msg: String? = null
            var atc900: Long? = null
            class Ck(m: String) : Exception(m)
            try {
                // 忠实: 旧减免匹配用 AAA402(医保旗标)+AAA009(医保编号) — 复制粘贴系, 原样
                val t = jdbc.query(
                    """SELECT atc900, atb930, aac001, aac003 FROM tc90
                       WHERE atc900 = (SELECT max(atc900) FROM tc90
                                       WHERE aaa402='1' AND aae00p IN ('1','5','7') AND aaa009=?)""",
                    { rs, _ -> arrayOf((rs.getObject("atc900") as Number).toLong(),
                        rs.getObject("atb930"), rs.getString("aac001"), rs.getString("aac003")) },
                    rec.aaa005).firstOrNull() ?: throw Ck("根据个人编号未匹配到员工信息|")
                atc900 = t[0] as Long
                val atb930 = (t[1] as Number?)?.toLong()
                val aae002m = rec.aae002r
                    ?: return err("异动回盘校验出错，错误信息：ORA-01722: invalid number，请将错误信息发给维护工程师")
                val aac001N = (t[2] as String?)?.toLongOrNull()
                    ?: return err("异动回盘校验出错，错误信息：ORA-01722: invalid number，请将错误信息发给维护工程师")
                jdbc.query(
                    "SELECT atc900 FROM tc93 WHERE atc931=? AND atc937='1' AND atb930=? AND aac001=?",
                    { rs, _ -> rs.getLong("atc900") }, aae002m, atb930, aac001N).firstOrNull()
                    ?: throw Ck("当月工资未查询到|")
                if (t[3] != rec.aac003) throw Ck("姓名与系统不一致|")
                val bba004 = dec(rec.bba004, "工伤减免额度格式不正确|")
                    ?: throw Ck(if (rec.bba004.isNullOrBlank()) "工伤减免额度不能为零|" else "工伤减免额度格式不正确|")
                if (bba004.compareTo(java.math.BigDecimal.ZERO) == 0) throw Ck("工伤减免额度不能为零|")
                val atc9eh = dec(rec.atc9eh, "缴费合计格式不正确|")
                    ?: throw Ck(if (rec.atc9eh.isNullOrBlank()) "缴费合计不能为空或者零|" else "缴费合计格式不正确|")
                if (atc9eh.compareTo(java.math.BigDecimal.ZERO) == 0) throw Ck("缴费合计不能为空或者零|")
                jdbc.update(
                    "UPDATE tc09_bp SET ace10a=?, ace10b=?, atc900=? WHERE id=?",
                    nOk, msg, atc900, rec.id)
            } catch (ck: Ck) {
                nOk = "2"; msg = ck.message
                jdbc.update("UPDATE tc09_bp SET ace10a=?, ace10b=? WHERE id=?", nOk, msg, rec.id)
            }
            if (nOk == "3") pass++ else reject++
        }
        return mapOf("code" to ok, "msg" to null as String?, "rows" to rows.size,
            "pass" to pass, "reject" to reject)
    }

    // ---------- PRC_省工伤减免提交 ----------

    @Transactional
    fun jmCommit(ace100: String, aae002: String, aaa020: String, aaa030: String, aae011: String): Map<String, Any?> {
        val c = jmCheck(ace100)
        if ((c["code"] as Int) != ok) return c
        val ace100N = ace100.toLongOrNull()!!
        val aae002N = aae002.toLongOrNull() ?: return err("省工伤年月格式不正确|")
        val user = jdbc.query(
            "SELECT aae011, aae017, aae019, aae020, aae022 FROM sys_org_user_unit WHERE aae011=?",
            { rs, _ -> arrayOf(rs.getString("aae011"), rs.getString("aae017"), rs.getString("aae019"),
                rs.getString("aae020"), rs.getString("aae022")) },
            aae011).firstOrNull() ?: return err("获取经办人信息出错")
        // 重建语义: 物理删该属地+户头+年月旧减免账(忠实, 铁律 8 清单)
        jdbc.update(
            "DELETE FROM tc09 WHERE aaa110=? AND aaa030=? AND aae002=?",
            aaa020, aaa030.toLong(), aae002N)
        data class Row(val id: Long, val aaa005: String?, val aac003: String?, val aae002r: Long?,
                       val aaa020r: String?, val aaa021r: String?, val aaa030r: Long?, val aaa031r: String?,
                       val abp002: String?, val bba004: String?, val atc9eh: String?)
        val rows = jdbc.query(
            """SELECT id, aaa005, aac003, aae002, aaa020, aaa021, aaa030, aaa031, abp002, bba004, atc9eh
               FROM tc09_bp WHERE ace10a='3' AND ace100=? ORDER BY id""",
            { rs, _ -> Row(rs.getLong("id"), rs.getString("aaa005"), rs.getString("aac003"),
                (rs.getObject("aae002") as Number?)?.toLong(), rs.getString("aaa020"),
                rs.getString("aaa021"), (rs.getObject("aaa030") as Number?)?.toLong(),
                rs.getString("aaa031"), rs.getString("abp002"), rs.getString("bba004"),
                rs.getString("atc9eh")) }, ace100N)
        var applied = 0
        for (rec in rows) {
            var nOk = "1"
            var msg: String? = null
            class Ck(m: String) : Exception(m)
            try {
                val t = jdbc.query(
                    """SELECT atc900, atb930, aac001, aac003, aaa005 FROM tc90
                       WHERE atc900 = (SELECT max(atc900) FROM tc90
                                       WHERE aaa402='1' AND aae00p IN ('1','5','7') AND aaa009=?)""",
                    { rs, _ -> arrayOf((rs.getObject("atc900") as Number).toLong(),
                        rs.getObject("atb930"), rs.getString("aac001"), rs.getString("aac003"),
                        rs.getString("aaa005")) }, rec.aaa005).firstOrNull()
                    ?: throw Ck("根据个人编号未匹配到员工信息|")
                val atb930 = (t[1] as Number?)?.toLong()
                val aae002m = rec.aae002r
                    ?: return err("异动回盘提交出错，错误信息：ORA-01722: invalid number，请将错误信息发给维护工程师")
                val aac001N = (t[2] as String?)?.toLongOrNull()
                    ?: return err("异动回盘提交出错，错误信息：ORA-01722: invalid number，请将错误信息发给维护工程师")
                val tc93 = jdbc.query(
                    """SELECT atc900, aac001, aac003, aac002, atb930, atb931, atb900, aab001, aab004
                       FROM tc93 WHERE atc931=? AND atc937='1' AND atb930=? AND aac001=?""",
                    { rs, _ -> arrayOf((rs.getObject("atc900") as Number).toLong(),
                        (rs.getObject("aac001") as Number?), rs.getString("aac003"), rs.getString("aac002"),
                        rs.getObject("atb930"), rs.getString("atb931"), rs.getObject("atb900"),
                        rs.getObject("aab001"), rs.getString("aab004")) },
                    aae002m, atb930, aac001N).firstOrNull() ?: throw Ck("当月工资未查询到|")
                fun dec(s: String?): java.math.BigDecimal? = s?.trim()?.toBigDecimalOrNull()
                val baz = jdbc.queryForObject("SELECT nextval('seq_sys_baz001')", Long::class.java)
                val n = jdbc.update(
                    """INSERT INTO tc09 (baz001, baz002, atc090, aae002, aae003, atc900, aac001, aaa005,
                       aac003, aac002, atb930, atb931, atb900, aab001, aab004, aaa030, aaa031,
                       aaa110, aaa111, aaa302, bba004, atc9eh, atc091,
                       aae011, aae017, aae019, aae020, aae022, aae036)
                       VALUES (?, ?, nextval('seq_tc09_atc090'), ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?,
                       ?, ?, ?, ?, ?, '1', ?, ?, ?, ?, ?, now())""",
                    baz, baz, aae002N, aae002N, tc93[0], tc93[1], t[4], tc93[2], tc93[3],
                    tc93[4], tc93[5], tc93[6], tc93[7], tc93[8],
                    rec.aaa030r, rec.aaa031r, rec.aaa020r, rec.aaa021r,
                    dec(rec.abp002), dec(rec.bba004), dec(rec.atc9eh),
                    user[0], user[1], user[2], user[3], user[4])
                // DUP→跳过等价: 旧 DUP_VAL_ON_INDEX 仅置 PRM_CODE=OK; PG 侧靠重建 DELETE 前置, 冲突即 0 行
                jdbc.update("UPDATE tc09_bp SET ace10a=?, ace10b=? WHERE id=?", nOk, msg, rec.id)
                applied++
            } catch (ck: Ck) {
                nOk = "2"; msg = ck.message
                jdbc.update("UPDATE tc09_bp SET ace10a=?, ace10b=? WHERE id=?", nOk, msg, rec.id)
            }
        }
        return mapOf("code" to ok, "msg" to null as String?, "applied" to applied)
    }

    // ---------- PRC_省工伤基数补差回盘校验 ----------

    @Transactional
    fun bcCheck(ace100: String): Map<String, Any?> {
        val ace100N = ace100.toLongOrNull() ?: return err("批次号格式不正确|")
        jdbc.update(
            """UPDATE tc9w_bp08 SET
               aaa009 = replace(replace(replace(replace(aaa009, chr(13), ''), chr(10), ''), chr(9), ''), ' ', ''),
               aae003 = replace(aae003, '.0', '')
               WHERE ace100=?""", ace100N)
        data class Row(val id: Long, val aaa009: String?, val aae003: String?)
        val rows = jdbc.query(
            """SELECT id, aaa009, aae003 FROM tc9w_bp08
               WHERE (ace10a IS NULL OR ace10a IN ('0','2','3','4')) AND ace100=? ORDER BY id""",
            { rs, _ -> Row(rs.getLong("id"), rs.getString("aaa009"), rs.getString("aae003")) }, ace100N)
        var pass = 0
        var reject = 0
        for (rec in rows) {
            var nOk = "3"
            var msg: String? = null
            class Ck(m: String) : Exception(m)
            try {
                // 忠实: 校验单月所属期=查前一月(ADD_MONTHS -1), 区间=原样
                val range = periodRange(rec.aae003, singleAdj = true)
                    ?: throw Ck("费款所属期格式不正确|")
                val t = jdbc.query(
                    """SELECT atc900, aac001 FROM tc90
                       WHERE atc900 = (SELECT max(atc900) FROM tc90
                                       WHERE aaa404='1' AND aae00p IN ('1','5','7') AND aaa009=?)""",
                    { rs, _ -> arrayOf((rs.getObject("atc900") as Number).toLong(),
                        rs.getString("aac001")) }, rec.aaa009).firstOrNull()
                    ?: throw Ck("匹配员工信息出错|")
                var ks = range.first
                while (!ks.isAfter(range.second)) {
                    val ym = java.time.YearMonth.from(ks).format(java.time.format.DateTimeFormatter.ofPattern("yyyyMM"))
                    val aac001N = (t[1] as String?)?.toLongOrNull()
                        ?: return err("异动回盘校验出错，错误信息：ORA-01722: invalid number，请将错误信息发给维护工程师")
                    jdbc.query(
                        "SELECT atc900 FROM tc93 WHERE atc931=? AND atc937='1' AND aac001=?",
                        { rs, _ -> rs.getLong("atc900") }, ym.toLong(), aac001N).firstOrNull()
                        ?: throw Ck("未查询到${ym}工资发放信息|")
                    ks = ks.plusMonths(1)
                }
                jdbc.update("UPDATE tc9w_bp08 SET ace10a=?, ace10b=? WHERE id=?", nOk, msg, rec.id)
            } catch (ck: Ck) {
                nOk = "3" // 忠实: 旧缺月分支置 N_OK=3(仍算过) 且信息入 ace10b
                msg = ck.message
                jdbc.update("UPDATE tc9w_bp08 SET ace10a=?, ace10b=? WHERE id=?", nOk, msg, rec.id)
            }
            pass++ // 旧逻辑缺月也置 '3' → 全计入通过侧
        }
        return mapOf("code" to ok, "msg" to null as String?, "rows" to rows.size,
            "pass" to pass, "reject" to reject)
    }

    // ---------- PRC_省工伤基数补差回盘提交 ----------

    @Transactional
    fun bcCommit(ace100: String, aae002: String, aaa020: String, aaa030: String, aae011: String): Map<String, Any?> {
        val ace100N = ace100.toLongOrNull() ?: return err("批次号格式不正确|")
        val user = jdbc.query(
            "SELECT aae011, aae017, aae019, aae020, aae022 FROM sys_org_user_unit WHERE aae011=?",
            { rs, _ -> arrayOf(rs.getString("aae011"), rs.getString("aae017"), rs.getString("aae019"),
                rs.getString("aae020"), rs.getString("aae022")) },
            aae011).firstOrNull() ?: return err("获取经办人信息出错")
        // 忠实: 旧 DELETE TJ0B3 被注释 → 累积模式(DUP→UPDATE bba004/atj0b5), 不重建
        data class Row(val id: Long, val aaa009: String?, val aac003: String?, val aae002r: String?,
                       val aae003r: String?, val aaa020r: String?, val aaa021r: String?,
                       val aaa030r: Long?, val aaa031r: String?, val abp006: String?)
        val rows = jdbc.query(
            """SELECT id, aaa009, aac003, aae002, aae003, aaa020, aaa021, aaa030, aaa031, abp006
               FROM tc9w_bp08 WHERE ace10a='3' AND ace100=? ORDER BY id""",
            { rs, _ -> Row(rs.getLong("id"), rs.getString("aaa009"), rs.getString("aac003"),
                rs.getString("aae002"), rs.getString("aae003"), rs.getString("aaa020"),
                rs.getString("aaa021"), (rs.getObject("aaa030") as Number?)?.toLong(),
                rs.getString("aaa031"), rs.getString("abp006")) }, ace100N)
        var applied = 0
        for (rec in rows) {
            var nOk = "1"
            var msg: String? = null
            class Ck(m: String) : Exception(m)
            try {
                // 提交: 单月=当月本身(与校验查前一月不同 — 旧系统自体差异, 双忠实)
                val range = periodRange(rec.aae003r, singleAdj = false)
                    ?: throw Ck("费款所属期格式不正确|")
                val t = jdbc.query(
                    """SELECT atc900, aac001, aac003, aac002, atb930, aab004, atb900, aab001 FROM tc90
                       WHERE atc900 = (SELECT max(atc900) FROM tc90
                                       WHERE aaa404='1' AND aae00p IN ('1','5','7')
                                         AND aaa009=? AND aac003=?)""",
                    { rs, _ -> arrayOf((rs.getObject("atc900") as Number).toLong(),
                        rs.getString("aac001"), rs.getString("aac003"), rs.getString("aac002"),
                        rs.getObject("atb930"), rs.getString("aab004"), rs.getObject("atb900"),
                        rs.getObject("aab001")) },
                    rec.aaa009, rec.aac003).firstOrNull()
                    ?: throw Ck("匹配员工信息出错|")
                val aac001N = (t[1] as String?)?.toLongOrNull()
                    ?: return err("异动回盘提交出错，错误信息：ORA-01722: invalid number，请将错误信息发给维护工程师")
                // 旧 MONTHS_BETWEEN(止,起)+1 = 含首尾月数
                val nys = java.time.Period.between(range.first, range.second).toTotalMonths() + 1f
                val abp006N = runCatching { rec.abp006?.trim()?.toBigDecimal() }.getOrElse {
                    return err("提交出错，错误信息：ORA-01722: invalid number，请将错误信息发给维护工程师") }
                    ?: return err("提交出错，错误信息：ORA-01722: invalid number，请将错误信息发给维护工程师")
                val aae002N = rec.aae002r?.toLongOrNull()
                    ?: return err("提交出错，错误信息：ORA-01722: invalid number，请将错误信息发给维护工程师")
                var ks = range.first
                while (!ks.isAfter(range.second)) {
                    val ym = java.time.YearMonth.from(ks)
                        .format(java.time.format.DateTimeFormatter.ofPattern("yyyyMM")).toLong()
                    // TC93 无月时以 TC90 合成占位(ATB931 误取单位名 — 忠实)
                    val tc93 = jdbc.query(
                        """SELECT atc900, aac001, aac003, aac002, atb930, atb931, atb900, aab001, aab004
                           FROM tc93 WHERE atc931=? AND atc937='1' AND aac001=?""",
                        { rs, _ -> arrayOf((rs.getObject("atc900") as Number).toLong(),
                            rs.getObject("aac001"), rs.getString("aac003"), rs.getString("aac002"),
                            rs.getObject("atb930"), rs.getString("atb931"), rs.getObject("atb900"),
                            rs.getObject("aab001"), rs.getString("aab004")) }, ym, aac001N).firstOrNull()
                        ?: arrayOf(t[0], aac001N, t[2], t[3], t[4], t[5], t[6], t[7], t[5])
                    // 旧 NUMBER 除法无定标 → 高精度除(DECIMAL64), 不做 2 位舍入
                    val bba004 = abp006N.divide(java.math.BigDecimal(nys.toDouble()), java.math.MathContext.DECIMAL64)
                    jdbc.update(
                        """INSERT INTO tj0b3 (aae002, aae003, atj000, atc900, aaa001, aac001, aac003,
                           aac002, aaa020, aaa021, aab001, aab004, atb930, atb931, aaa030, aaa031,
                           bba004, atj0b5, aae011, aae017, aae036, aae019, aae020, aae022)
                           VALUES (?, ?, '1', ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, now(), ?, ?, ?)
                           ON CONFLICT (aae002, aae003, atc900) DO UPDATE SET
                           bba004=EXCLUDED.bba004, atj0b5=EXCLUDED.atj0b5""",
                        aae002N, ym, tc93[0], rec.aaa009, aac001N, tc93[2] ?: t[1], tc93[3],
                        rec.aaa020r, rec.aaa021r, tc93[7], tc93[8], tc93[4], tc93[5],
                        rec.aaa030r, rec.aaa031r, bba004, bba004,
                        user[0], user[1], user[2], user[3], user[4])
                    ks = ks.plusMonths(1)
                }
                jdbc.update("UPDATE tc9w_bp08 SET ace10a=?, ace10b=? WHERE id=?", nOk, msg, rec.id)
                applied++
            } catch (ck: Ck) {
                nOk = "2"; msg = ck.message
                jdbc.update("UPDATE tc9w_bp08 SET ace10a=?, ace10b=? WHERE id=?", nOk, msg, rec.id)
            }
        }
        return mapOf("code" to ok, "msg" to null as String?, "applied" to applied)
    }
}

/** ---------- /proc 注册面（PKG_保险人员增减 省工伤六过程, W3a） ---------- */

@Component
class TdcbpCheckProc(private val c: TdcbpCases) : ProcHandler {
    override fun procName() = "PKG_保险人员增减.PRC_省工伤异动回盘校验"
    override fun handle(params: Map<String, Any?>): Map<String, Any?> {
        for (k in listOf("aae002", "aaa030", "aaa020"))
            if (params[k] == null) return mapOf("appCode" to -1, "errMsg" to "缺少 $k")
        val r = c.sbCheck(params["aae002"]!!.toString(), params["aaa030"]!!.toString(), params["aaa020"]!!.toString())
        return mapOf("appCode" to r["code"], "errMsg" to r["msg"],
            "rows" to r["rows"], "pass" to r["pass"], "reject" to r["reject"])
    }
}

@Component
class TdcbpCommitProc(private val c: TdcbpCases) : ProcHandler {
    override fun procName() = "PKG_保险人员增减.PRC_省工伤异动回盘提交"
    override fun handle(params: Map<String, Any?>): Map<String, Any?> {
        for (k in listOf("aae002", "aaa030", "aaa020", "aae011"))
            if (params[k] == null) return mapOf("appCode" to -1, "errMsg" to "缺少 $k")
        val r = c.sbCommit(params["aae002"]!!.toString(), params["aaa030"]!!.toString(),
            params["aaa020"]!!.toString(), params["aae011"]!!.toString())
        return mapOf("appCode" to r["code"], "errMsg" to r["msg"],
            "cbApplied" to r["cbApplied"], "yyApplied" to r["yyApplied"], "tzApplied" to r["tzApplied"])
    }
}

@Component
class TdcbpJmCheckProc(private val c: TdcbpCases) : ProcHandler {
    override fun procName() = "PKG_保险人员增减.PRC_省工伤减免校验"
    override fun handle(params: Map<String, Any?>): Map<String, Any?> {
        val ace100 = params["ace100"]?.toString() ?: return mapOf("appCode" to -1, "errMsg" to "缺少 ace100")
        val r = c.jmCheck(ace100)
        return mapOf("appCode" to r["code"], "errMsg" to r["msg"],
            "rows" to r["rows"], "pass" to r["pass"], "reject" to r["reject"])
    }
}

@Component
class TdcbpJmCommitProc(private val c: TdcbpCases) : ProcHandler {
    override fun procName() = "PKG_保险人员增减.PRC_省工伤减免提交"
    override fun handle(params: Map<String, Any?>): Map<String, Any?> {
        for (k in listOf("ace100", "aae002", "aaa020", "aaa030", "aae011"))
            if (params[k] == null) return mapOf("appCode" to -1, "errMsg" to "缺少 $k")
        val r = c.jmCommit(params["ace100"]!!.toString(), params["aae002"]!!.toString(),
            params["aaa020"]!!.toString(), params["aaa030"]!!.toString(), params["aae011"]!!.toString())
        return mapOf("appCode" to r["code"], "errMsg" to r["msg"], "applied" to r["applied"])
    }
}

@Component
class TdcbpBcCheckProc(private val c: TdcbpCases) : ProcHandler {
    override fun procName() = "PKG_保险人员增减.PRC_省工伤基数补差回盘校验"
    override fun handle(params: Map<String, Any?>): Map<String, Any?> {
        val ace100 = params["ace100"]?.toString() ?: return mapOf("appCode" to -1, "errMsg" to "缺少 ace100")
        val r = c.bcCheck(ace100)
        return mapOf("appCode" to r["code"], "errMsg" to r["msg"],
            "rows" to r["rows"], "pass" to r["pass"], "reject" to r["reject"])
    }
}

@Component
class TdcbpBcCommitProc(private val c: TdcbpCases) : ProcHandler {
    override fun procName() = "PKG_保险人员增减.PRC_省工伤基数补差回盘提交"
    override fun handle(params: Map<String, Any?>): Map<String, Any?> {
        for (k in listOf("ace100", "aae002", "aaa020", "aaa030", "aae011"))
            if (params[k] == null) return mapOf("appCode" to -1, "errMsg" to "缺少 $k")
        val r = c.bcCommit(params["ace100"]!!.toString(), params["aae002"]!!.toString(),
            params["aaa020"]!!.toString(), params["aaa030"]!!.toString(), params["aae011"]!!.toString())
        return mapOf("appCode" to r["code"], "errMsg" to r["msg"], "applied" to r["applied"])
    }
}
