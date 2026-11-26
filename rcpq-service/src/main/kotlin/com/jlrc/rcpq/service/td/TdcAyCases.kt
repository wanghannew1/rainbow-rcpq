package com.jlrc.rcpq.service.td

import com.jlrc.rcpq.service.proc.ProcHandler
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal

/**
 * td/c/ay 市医保异动回盘（M4）：PRC_市医保异动回盘校验(源 L4378-4662) +
 * PRC_市医保异动回盘提交(源 L4663-4943)，源=work/pkg_bxryzz.sql。
 *
 * 源包 PKG_保险人员增减 包体生产即 INVALID(铁律1, DbStatus 实测)，但本两过程
 * 主体逻辑为东软可读完整业务(坏块已被东软自行注释: TC9W1 匹配块/R_TC9W 未赋值
 * →最终 UPDATE ATC9W0 恒 NULL, 忠实写 NULL)。按社保双轨模式移植: 逐条转写
 * 可运行逻辑, 源中仅赋值不使用的变量(如校验后 TC06 特殊情况计数、提交里
 * V_AAE002)按"无可观测行为"处理并在行内注明。
 *
 * 涉表: TC9W_BP04(市医保回盘批次, tdcay)+TC9W1(市医保名册工作表)+TC90/AA94/AA97/
 * AA97_BG/TC90_YY/TC06/TC10/SYS_ORG_USER_UNIT。提交前置闸: 当月批次存在 ace10a='2'
 * 即拒绝。盖章五要素与社保回盘同款。
 */
@Component
class TdcAyCases(private val jdbcTemplate: JdbcTemplate) {

    private val ok = 1
    private val err = -1

    /** 文本清洗(与社保校验同款: 回车/换行/制表/空格, x→X) */
    private fun clean(v: String?): String? =
        v?.replace("\r", "")?.replace("\n", "")?.replace("\t", "")?.replace("x", "X")?.replace(" ", "")

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

    private fun num(v: String?): BigDecimal? =
        v?.trim()?.takeIf { it.isNotEmpty() }?.let {
            runCatching { BigDecimal(it) }.getOrElse { throw NumberFormatException("TO_NUMBER($it)") }
        }

    /** 码串追加(INSTR 去重语义): null→code, 不含则 ",code" 拼接 */
    private fun append(cur: String?, code: String): String = when {
        cur == null -> code
        !cur.contains(code) -> "$cur,$code"
        else -> cur
    }

    /** PRC_市医保异动回盘校验 */
    @Transactional
    fun checkAy(aae002: String, aaa030: String, aaa020: String): Map<String, Any?> = try {
        val ym = aae002.toLong()
        val head = aaa030.toLong()
        // 1) 文本清洗+清空校验结果(源: WHERE AAA030+AAE002, 无 AAA020 — 忠实)
        jdbcTemplate.update(
            """UPDATE tc9w_bp04 SET aac002=replace(replace(replace(replace(replace(aac002,chr(13),''),chr(10),''),chr(9),''),'x','X'),' ',''),
               aac003=replace(replace(replace(replace(aac003,chr(13),''),chr(10),''),chr(9),''),' ',''),
               aaa90y=NULL, aaa302_1=NULL, aaa302_2=NULL, atc93bd_1=NULL, atc93bd_2=NULL, ace10a=NULL
               WHERE aaa030=? AND aae002=?""", head, ym)
        // 2) 待审 TC9W1 置 '4'(源同款, 无 aaa020)
        jdbcTemplate.update(
            "UPDATE tc9w1 SET atc9w8='4' WHERE atc9w8='1' AND aaa030=? AND aae002=?", head, ym)
        // 3) 逐行校验(ace10a IS NULL OR '2')
        data class Row(val id: Long, val aac002: String?, val aac003: String?, val abp001: String?,
                       val abp004: String?, val abp006: String?, val abp007: String?, val abp008: String?,
                       val abp009: String?, val abp00a: String?, val abp00b: String?, val abp00c: String?,
                       val aae002: Any?)
        val rows = jdbcTemplate.query(
            """SELECT id, aac002, aac003, abp001, abp004, abp006, abp007, abp008, abp009,
               abp00a, abp00b, abp00c, aae002 FROM tc9w_bp04
               WHERE (ace10a IS NULL OR ace10a='2') AND aaa030=? AND aaa020=? AND aae002=?""",
            { rs, _ -> Row(rs.getLong("id"), rs.getString("aac002"), rs.getString("aac003"),
                rs.getString("abp001"), rs.getString("abp004"), rs.getString("abp006"),
                rs.getString("abp007"), rs.getString("abp008"), rs.getString("abp009"),
                rs.getString("abp00a"), rs.getString("abp00b"), rs.getString("abp00c"),
                rs.getObject("aae002")) },
            head, aaa020, ym)
        var pass = 0
        for (rec in rows) {
            var msg = ""
            var flags: String? = null
            var aaa302_1: BigDecimal? = null
            var aaa302_2: BigDecimal? = null
            var atc93bd_1: BigDecimal? = null
            var atc93bd_2: BigDecimal? = null
            try {
                val id18 = id15to18(clean(rec.aac002))
                // 匹配合同(源: MAX(ATC900), 医保参保+用工形式 1/5/7+属地+证件 15→18 兼容)
                val tc90: Map<String, Any?> = jdbcTemplate.query(
                    """SELECT atc900, aaa302, atb930, aab001, aab004, aac008, aaa601, aaa603, atc90pa
                       FROM tc90 WHERE atc900=(SELECT max(atc900) FROM tc90
                         WHERE aaa402='1' AND aae00p IN ('1','5','7') AND aaa110=?
                         AND (aac002=? OR aac002=?))""",
                    { rs, _ -> mapOf("atc900" to (rs.getObject("atc900") as Number).toLong(),
                        "aaa302" to rs.getBigDecimal("aaa302"), "atb930" to (rs.getObject("atb930") as Number?)?.toLong(),
                        "aab001" to (rs.getObject("aab001") as Number?), "aab004" to rs.getString("aab004"),
                        "aac008" to rs.getString("aac008"), "aaa601" to (rs.getObject("aaa601") as Number?),
                        "aaa603" to (rs.getObject("aaa603") as Number?), "atc90pa" to rs.getString("atc90pa")) },
                    aaa020, id18, id18).firstOrNull()
                    ?: throw AyBreak("匹配员工信息出错，原因为【姓名错误】或者【是否参加医保状态错误】|")
                val atb931 = tc90["atb930"]?.let { atb930 ->
                    jdbcTemplate.query(
                        "SELECT atb931 FROM tb93 WHERE atb930=?",
                        { rs, _ -> rs.getString("atb931") }, atb930).firstOrNull()
                } ?: throw AyBreak("匹配结算单元信息出错|")
                // 基数不符(11) — 源 L4503 IF R_TC90.AAA302 <> TO_NUMBER(REC.ABP004): PL/SQL 数值比较,
                // 任一侧 NULL=未知不旗标, 等值不看 scale(BigDecimal.compareTo, 铁律3; equals 曾把
                // PG numeric 存量 5000.00 与回盘 "5000"/"5000.0" 误标不符)
                val applyBase = num(rec.abp004)
                val tc90Base = tc90["aaa302"] as BigDecimal?
                if (tc90Base != null && applyBase != null && tc90Base.compareTo(applyBase) != 0) {
                    flags = append(flags, "11")
                    aaa302_1 = tc90Base
                    aaa302_2 = applyBase
                    msg += "【申报基数：${aaa302_1}；实际缴费基数：${aaa302_2}】"
                    // 源在此覆盖 aae013(异议原因), 循环末写入
                }
                // 灵活就业(12) — TO_NUMBER(ABP008)=0 数值等值(compareTo, 铁律3; "0.0" 曾漏旗标)
                if (rec.abp001 == "在职" && num(rec.abp008)?.compareTo(BigDecimal.ZERO) == 0)
                    flags = append(flags, "12")
                // 退出灵活就业(16)
                val aac008 = tc90["aac008"] as String?
                if (aac008 != null && aac008.contains("51") && rec.abp001 == "在职" && ((num(rec.abp008) ?: BigDecimal.ZERO) > BigDecimal.ZERO))
                    flags = append(flags, "16")
                // 大额(14): 退缴纳大额 / 退大病差额(源两分支共用 AA94 参数)
                // [卷66 忠实] 源 TO_NUMBER(ABP007)=0/>0 两分支: ABP007 列空→TO_NUMBER=NULL→两分支
                // 皆未知不进入——不查 AA94、不可能报"大病险参数设置有误"(空金额列天然惰性, 卷61)。
                // 曾用 ?:ZERO 把空值当 0 → 分支误激活 → AA94 查参失败误拒行(e2e 实测, 已修)。
                val daeParam = "SELECT aaa945 FROM aa94 WHERE aaa100=? AND aaa941='11' AND aaa946<=? AND aaa947>=?"
                if (tc90["atc90pa"] == null) {
                    val bd = num(rec.abp007)
                    if (bd != null) {
                        val a94 = jdbcTemplate.query(daeParam,
                            { rs, _ -> rs.getBigDecimal("aaa945") }, aaa020, tc90["aaa601"], tc90["aaa601"]).firstOrNull()
                            ?: throw AyBreak("大病险参数设置有误|")
                        if (bd.compareTo(BigDecimal.ZERO) == 0) {
                            flags = append(flags, "14")
                            atc93bd_1 = a94
                            atc93bd_2 = bd
                        } else if (a94.compareTo(bd) != 0) {
                            flags = append(flags, "14")
                            atc93bd_2 = bd - a94
                        }
                    }
                }
                // 失业代缴医疗(17) — 六项和=0 数值等值(compareTo, 铁律3; 和的 scale 随加数漂移)
                // [卷66 忠实] 源六项 TO_NUMBER 求和: 任一列空→和为 NULL→等值未知不旗标(空金额列
                // 天然惰性, 卷61); 曾用 ?:ZERO 补零 → 空行被误判和=0 而误旗标。
                val parts17 = listOf(num(rec.abp006), num(rec.abp007), num(rec.abp008),
                    num(rec.abp009), num(rec.abp00a), num(rec.abp00c))
                val abpSum = if (parts17.any { it == null }) null
                             else parts17.reduce { a, b -> a!!.add(b) }
                val b00b17 = num(rec.abp00b)
                if (abpSum != null && abpSum.compareTo(BigDecimal.ZERO) == 0 &&
                    b00b17 != null && b00b17.compareTo(BigDecimal.ZERO) > 0) flags = append(flags, "17")
                // 是否本月新增: aae001 = atc90pa null ? 行.aae002 : 合同.aaa603
                val aae001: Any? = if (tc90["atc90pa"] == null) rec.aae002 else tc90["aaa603"]
                // 行落地(源: ATC9W0=R_TC9W.ATC9W0, TC9W1 匹配块被东软注释→恒 NULL, 忠实)
                jdbcTemplate.update(
                    """UPDATE tc9w_bp04 SET atc9w0=NULL, atc900=?, ace10a=?, ace10b=?, aaa90y=?,
                       aaa302_1=?, aaa302_2=?, atc93bd_1=?, atc93bd_2=?, aae001=?, atb930=?, atb931=?,
                       aab001=?, aab004=?, aae013=? WHERE id=?""",
                    tc90["atc900"], "3", msg.ifEmpty { null }, flags, aaa302_1, aaa302_2,
                    atc93bd_1, atc93bd_2, aae001, tc90["atb930"], atb931,
                    tc90["aab001"], tc90["aab004"], msg.ifEmpty { null }, rec.id)
                pass++
            } catch (e: AyBreak) {
                jdbcTemplate.update(
                    "UPDATE tc9w_bp04 SET ace10a='2', ace10b=? WHERE id=?", e.message, rec.id)
            }
        }
        mapOf("code" to ok, "msg" to null as String?, "rows" to rows.size,
            "pass" to pass, "reject" to rows.size - pass)
    } catch (e: NumberFormatException) {
        mapOf("code" to err, "msg" to "医保异动回盘校验出错，错误信息：ORA-01722 无效数字(${e.message})，请将错误信息发给维护工程师")
    } catch (e: Exception) {
        mapOf("code" to err, "msg" to "医保异动回盘校验出错，错误信息：${e.message}，请将错误信息发给维护工程师")
    }

    private class AyBreak(val msg: String) : Exception(msg)

    /** PRC_市医保异动回盘提交 */
    @Transactional
    fun commitAy(aae002: String, aaa030: String, aaa020: String, aae011: String): Map<String, Any?> {
        return try {
        val ym = aae002.toLong()
        val head = aaa030.toLong()
        // 前置闸: 当月存在校验失败数据(源: 仅按 aae002, 无 aaa030/aaa020 — 忠实)
        val nFail = jdbcTemplate.queryForObject(
            "SELECT count(*) FROM tc9w_bp04 WHERE ace10a='2' AND aae002=?", Int::class.java, ym)
        if (nFail > 0) return mapOf("code" to err,
            "msg" to "该月份存在校验失败数据，不符合提交条件，请切换至【失败数据】页查看")
        val user = jdbcTemplate.query(
            "SELECT aae011, aae017, aae019, aae020, aae022 FROM sys_org_user_unit WHERE aae011=?",
            { rs, _ -> arrayOf(rs.getString("aae011"), rs.getString("aae017"), rs.getString("aae019"),
                rs.getString("aae020"), rs.getString("aae022")) }, aae011).firstOrNull()
            ?: return mapOf("code" to err, "msg" to "获取经办人信息出错")
        data class Row(val id: Long, val atc900: Long?, val aac003: String?, val aac000: String?,
                       val abp004: String?, val aaa90y: String?, val aae002: Any?,
                       val aae011: String?, val aae017: String?, val aae019: String?,
                       val aae020: String?, val aae022: String?)
        val rows = jdbcTemplate.query(
            """SELECT id, atc900, aac003, aac000, abp004, aaa90y, aae002,
               aae011, aae017, aae019, aae020, aae022
               FROM tc9w_bp04 WHERE ace10a IN ('1','3') AND aaa030=? AND aaa020=? AND aae002=?""",
            { rs, _ -> Row(rs.getLong("id"), (rs.getObject("atc900") as Number?)?.toLong(),
                rs.getString("aac003"), rs.getString("aac000"), rs.getString("abp004"),
                rs.getString("aaa90y"), rs.getObject("aae002"), rs.getString("aae011"),
                rs.getString("aae017"), rs.getString("aae019"), rs.getString("aae020"),
                rs.getString("aae022")) },
            head, aaa020, ym)
        var applied = 0
        var baseAdj = 0
        for (cb in rows) {
            val atc900 = cb.atc900 ?: return mapOf("code" to err, "msg" to "【${cb.aac003}】新参保更新出错")
            val tc90 = jdbcTemplate.queryForMap(
                "SELECT aaa302, atc90pa, aaa603 FROM tc90 WHERE atc900=?", atc900)
            val oldBase = tc90["aaa302"] as BigDecimal?
            val atc90pa = tc90["atc90pa"] as String?
            // 特殊情况 TC06 计数: 源计算后未使用(无可观测行为), 移植省略
            val has11 = cb.aaa90y?.contains("11") == true
            val newBase = if (has11) num(cb.abp004) else null
            // 参保成功: 医保实际参保/工资截止年月+标志清零+基数以申报为准+医保状态码串
            jdbcTemplate.update(
                """UPDATE tc90 SET
                   aaa603 = CASE WHEN atc90pa IS NULL OR atc90pa='2' THEN ?::numeric ELSE aaa603 END,
                   aaa608 = CASE WHEN atc90pa IS NULL OR atc90pa='2' THEN ?::numeric ELSE aaa608 END,
                   atc90pa = '0',
                   aaa005 = ?,
                   aaa302 = CASE WHEN ? THEN ? ELSE aaa302 END,
                   aaa306 = CASE WHEN ? THEN ? ELSE aaa306 END,
                   aac008 = CASE
                     WHEN ? AND (aac008 IS NULL OR aac008='51') THEN '51'
                     WHEN ? AND aac008 IS NOT NULL AND strpos(aac008,'51')=0 THEN aac008||',51'
                     WHEN ? AND (aac008 IS NULL OR aac008='80') THEN '80'
                     WHEN ? AND aac008 IS NOT NULL AND strpos(aac008,'80')=0 THEN aac008||',80'
                     WHEN ? AND (aac008 IS NULL OR aac008='81') THEN '81'
                     WHEN ? AND aac008 IS NOT NULL AND strpos(aac008,'81')=0 THEN aac008||',81'
                     WHEN ? AND aac008 IS NOT NULL AND strpos(aac008,'51')>0 THEN replace(aac008,'51','')
                     WHEN ? AND (aac008 IS NULL OR aac008='52') THEN '51'
                     WHEN ? AND aac008 IS NOT NULL AND strpos(aac008,'52')=0 THEN aac008||',52'
                     ELSE aac008 END
                   WHERE atc900=?""",
                ym, ym, cb.aac000,
                has11, newBase, has11, newBase,
                flag(cb.aaa90y, "12"), flag(cb.aaa90y, "12"),
                flag(cb.aaa90y, "13"), flag(cb.aaa90y, "13"),
                flag(cb.aaa90y, "15"), flag(cb.aaa90y, "15"),
                flag(cb.aaa90y, "16"),
                flag(cb.aaa90y, "17"), flag(cb.aaa90y, "17"),
                atc900)
            // 标记完成
            jdbcTemplate.update("UPDATE tc9w_bp04 SET ace10a='1' WHERE id=?", cb.id)
            // 异议表落地(safesign='2' 医保)
            jdbcTemplate.update(
                "UPDATE tc90_yy SET cbsign='0', aaa203=? WHERE safesign='2' AND atc900=?", ym, atc900)
            // 基数不符: AA97 调整 + AA97_BG 台账(源 aa97type='2', 注文案为源原文)
            if (has11) {
                val year = aae002.take(4).toLong()
                jdbcTemplate.update(
                    "UPDATE aa97 SET aaa302=?, aaa306=? WHERE aae001 >= ? AND atc900=?",
                    newBase, newBase, year, atc900)
                val bgId = jdbcTemplate.queryForObject("SELECT nextval('seq_aa97_bg_id')", Long::class.java)
                runCatching {
                    jdbcTemplate.update(
                        """INSERT INTO aa97_bg (id, tname, tkey, atc900, aa97type, aaa300_bgq, aaa300_bgh,
                           aae013, aae011, aae017, aae019, aae020, aae022, aae036)
                           VALUES (?, 'TC9W_BP04', ?, ?, '2', ?, ?, '公积金缴费基数上下限调整',
                           ?, ?, ?, ?, ?, now())""",
                        bgId, cb.id, atc900, oldBase, newBase,
                        cb.aae011, cb.aae017, cb.aae019, cb.aae020, cb.aae022)
                }.onFailure { e ->
                    if (!isDup(e)) return mapOf("code" to err, "msg" to "记录缴费基数变更信息出错")
                }
                baseAdj++
            }
            applied++
        }
        // 停保(源: atb90r='5' 且医保已参保 atc90pa='0' 且 aaa604<=当月, 无 TC10 在途; TC06 特殊情况跳过)
        val tb = jdbcTemplate.queryForList(
            """SELECT a.atc900, a.aac001 FROM tc90 a, ab01 b
               WHERE a.aab001=b.aab001 AND b.aaa030=? AND a.aaa110=? AND a.aaa402='1'
                 AND a.atc90pa='0' AND a.atb90r='5' AND a.aaa604<=?
                 AND NOT EXISTS (SELECT 1 FROM tc10 WHERE atc107='1' AND atc900=a.atc900)""",
            head, aaa020, ym)
        var stopApplied = 0
        for (r in tb) {
            val atc900 = (r["atc900"] as Number).toLong()
            val aac001 = (r["aac001"] as Number?)?.toLong()
            val nSpecial = if (aac001 == null) 0L else jdbcTemplate.queryForObject(
                """SELECT count(*) FROM tc06 WHERE atc064 IN ('1','2')
                   AND (aae002=? OR (aae002<=? AND atc06c>=?)) AND aac001=?""",
                Long::class.java, ym, ym, ym, aac001)
            if (nSpecial == 0L) {
                jdbcTemplate.update(
                    "UPDATE tc90 SET aaa604=?, aaa609=?, atc90pa='1' WHERE atc900=?", ym, ym, atc900)
                stopApplied++
            }
        }
        // 全批盖章
        jdbcTemplate.update(
            """UPDATE tc9w_bp04 SET aae211=?, aae217=?, aae236=now(), aae219=?, aae220=?, aae222=?
               WHERE aaa030=? AND aaa020=? AND aae002=?""",
            user[0], user[1], user[2], user[3], user[4], head, aaa020, ym)
        mapOf("code" to ok, "msg" to null as String?, "applied" to applied,
            "baseAdjusted" to baseAdj, "stopApplied" to stopApplied)
    } catch (e: NumberFormatException) {
        mapOf("code" to err, "msg" to "医保异动回盘提交出错，错误信息：ORA-01722 无效数字(${e.message})")
    } catch (e: Exception) {
        mapOf("code" to err, "msg" to "医保异动回盘提交出错，错误信息：${e.message}，请将错误信息发给维护工程师")
        }
    }

    private fun flag(y: String?, code: String): Boolean = y?.contains(code) == true

    private fun isDup(e: Throwable): Boolean {
        var t: Throwable? = e
        while (t != null) {
            if (t.message?.contains("duplicate key", ignoreCase = true) == true ||
                t.message?.contains("唯一", ignoreCase = true) == true) return true
            t = t.cause
        }
        return false
    }
}

/** /proc 注册面: PKG_保险人员增减.PRC_市医保异动回盘校验/提交 */
@org.springframework.stereotype.Component
class TdcAyCheckProc(private val ay: TdcAyCases) : ProcHandler {
    override fun procName() = "PKG_保险人员增减.PRC_市医保异动回盘校验"
    override fun handle(params: Map<String, Any?>): Map<String, Any?> {
        val r = ay.checkAy(
            params["aae002"]?.toString() ?: return mapOf("appCode" to -1, "errMsg" to "缺少 aae002"),
            params["aaa030"]?.toString() ?: return mapOf("appCode" to -1, "errMsg" to "缺少 aaa030"),
            params["aaa020"]?.toString() ?: return mapOf("appCode" to -1, "errMsg" to "缺少 aaa020"))
        return mapOf("appCode" to r["code"], "errMsg" to r["msg"],
            "rows" to r["rows"], "pass" to r["pass"], "reject" to r["reject"])
    }
}

@org.springframework.stereotype.Component
class TdcAyCommitProc(private val ay: TdcAyCases) : ProcHandler {
    override fun procName() = "PKG_保险人员增减.PRC_市医保异动回盘提交"
    override fun handle(params: Map<String, Any?>): Map<String, Any?> {
        val r = ay.commitAy(
            params["aae002"]?.toString() ?: return mapOf("appCode" to -1, "errMsg" to "缺少 aae002"),
            params["aaa030"]?.toString() ?: return mapOf("appCode" to -1, "errMsg" to "缺少 aaa030"),
            params["aaa020"]?.toString() ?: return mapOf("appCode" to -1, "errMsg" to "缺少 aaa020"),
            params["aae011"]?.toString() ?: return mapOf("appCode" to -1, "errMsg" to "缺少 aae011"))
        return mapOf("appCode" to r["code"], "errMsg" to r["msg"],
            "applied" to r["applied"], "baseAdjusted" to r["baseAdjusted"],
            "stopApplied" to r["stopApplied"])
    }
}
