package com.jlrc.rcpq.service.td

import com.jlrc.rcpq.service.calc.InsuranceCalcEngine
import com.jlrc.rcpq.service.proc.ProcHandler
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.math.RoundingMode
import java.sql.Timestamp
import java.time.YearMonth
import java.time.format.DateTimeFormatter

/**
 * W5a 缴费基数核对/确认×4（旧 PKG_缴费基数 L636-1755, 2026-11-17, tdcq/tdcab/tdcac/tdch 页）。
 * 独立新文件(W4b 教训)。
 *
 * 公共骨架: 已确认 guard(AAE214_k='1' 任一行→静默跳过) → AA97 逐人 → TC90 取合同 →
 * 资格判定(SYSDATE 年度动态, 核对仅在当年窗口有效=旧系统忠实) → UPDATE AA97 置 '3'/'0' →
 * (确认段)TC90 基数落库 + AA97 置 '1' + 经办六件套(_k 后缀列)。
 * 四线差异(源码逐条核对, 详见 docs/five-insurance-migration.md W5-0 差异表):
 *   社保: 资格含 P='3'&substr(202,1,4)=当年; 确认落 ATC905/AAA301/AAA304←301_1, TC90 取同人 MAX(ATC900)(AAE00P 157);
 *   医保: P='3' 无 substr 条件 + INSTR(aac008,'80') 退休旁路在外层(绕过 AAE00P);
 *         ATC90PA CASE 多两支(80+市→NULL/80+省→'0'); 确认落 AAA302/AAA306←302_1 by 自身 ATC900;
 *   公积金: 无 80 旁路; AAA303_1>0 时活跃调 clamp('3') 且 IN=OUT=303_1→夹取值写回申报基数,
 *         ABP00C/D/E 省市两分支同为 ROUND 整数化(注释称"市进位"按代码忠实 HALF_UP scale0),
 *         AAE214_3 按现基数<>申报 分 '3'/'0'(他线恒 '3'); ABP00C/D/E PG varchar 须串化;
 *         确认落 AAA303←303_1 by MAX; 源 L1261 调试 no-op 桩(530404)不移植;
 *   工伤: 主路径 '3' 项被注释; ELSIF PB='3' 分支: 同年同人 AA97 唯一才过 + 活跃调 clamp('2',
 *         OUT=305 随被 TC90.AAA305 覆盖=仅报错不落值); 确认落 AAA305←305_1 by 自身 ATC900。
 * clamp 复用引擎 clampBase(公共.fun_缴费基数)。结构性: 旧外层 WHEN OTHERS+指定回滚 →
 * JishuErr 穿越事务边界整批回滚; 逐行 '0'/'3' 标记随事务提交。
 */
class JishuErr(val m: String) : RuntimeException(m)

/** 行级不合格信号: 该人 AAE214_k 置 '0', 不中断批次(忠实 per-row BEGIN..EXCEPTION) */
private class Mark0 : Exception("mark0")

@Component
class Jishu5Cases(private val jdbc: JdbcTemplate, private val engine: InsuranceCalcEngine) {

    private val ok = 1
    private val Z = BigDecimal.ZERO

    private fun bd(v: Any?): BigDecimal? = when (v) {
        null -> null
        is Number -> BigDecimal(v.toString())
        is String -> v.trim().takeIf { it.isNotEmpty() }?.let { BigDecimal(it) }
        else -> BigDecimal(v.toString())
    }
    private fun bd0(v: Any?): BigDecimal = bd(v) ?: Z
    private fun nm(v: Any?): Long? = when (v) {
        null -> null
        is Number -> v.toLong()
        is String -> v.trim().takeIf { it.isNotEmpty() }?.let { BigDecimal(it).toLong() }
        else -> null
    }
    private fun s(v: Any?): String? = v?.toString()
    private fun nowY(): String = YearMonth.now().format(DateTimeFormatter.ofPattern("yyyy"))
    private fun nowYm(): String = YearMonth.now().format(DateTimeFormatter.ofPattern("yyyyMM"))
    private fun in157(v: String?): Boolean = v == "1" || v == "5" || v == "7"
    private fun is80(v: String?): Boolean = (v ?: "").contains("80")
    private fun substr34(v: String?): String? = v?.takeIf { it.length >= 4 }?.substring(2, 4)
    /** Oracle SUBSTR(num,1,4): 数值串化取前 4 位 */
    private fun numYm4(v: BigDecimal?): String? = v?.let { it.toPlainString().take(4) }

    // ==================== 核对×4 ====================

    /** PRC_社保缴费基数核对(AAE214_1) */
    @Transactional
    fun sbHedui(aae001: String, aaa030: String, aaa020: String): Map<String, Any?> = try {
        val y = nowY(); val y01 = (y + "01").toLong()
        heduiGuard(1, aaa030, aaa020, aae001)?.let { return it }
        val rows = jdbc.queryForList(
            "SELECT * FROM aa97 WHERE aaa030=? AND aaa100=? AND aae001=?",
            aaa030.toLong(), aaa020, aae001.toLong())
        var marked = 0
        for (r in rows) {
            val aaa970 = nm(r["aaa970"]) ?: continue
            try {
                val t = loadTc90(r)
                val p = s(t["atc90p"]); val rr = s(t["atb90r"]); val a202 = bd(t["aaa202"])
                val eligible = in157(s(t["aae00p"])) && (
                    (p == "0" && (a202 == null || a202.toLong() > y01)) ||
                        (s(t["aaa401"]) == "1" && p == null && rr == "3" &&
                            bd(t["aae002"])?.let { it.toLong() < y01 } == true) ||
                        (p == "3" && numYm4(a202) == y) ||
                        (p == "2" && rr == "3"))
                if (!eligible) throw Mark0()
                val caseP = when { p == null -> "2"; p == "3" -> "0"; else -> p }
                jdbc.update(
                    """UPDATE aa97 SET aaa301=?, atc90p=?, aac002=?, aae214_1='3', aaa001=?
                       WHERE (aae214_1 IS NULL OR aae214_1='0' OR aae214_1='3') AND aaa970=?""",
                    bd(t["atc905"]), caseP, s(t["aac002"]), s(t["aaa001"]), aaa970)
                marked++
            } catch (e: Mark0) {
                jdbc.update("UPDATE aa97 SET aae214_1='0' WHERE aaa970=?", aaa970)
            }
        }
        mapOf("code" to ok, "msg" to null as String?, "rows" to rows.size, "marked" to marked, "skipped" to null)
    } catch (e: JishuErr) {
        throw e
    } catch (e: Exception) {
        throw JishuErr("缴费基数核对出错，错误信息：${e.cause?.message ?: e.message}，请将错误信息发给维护工程师")
    }

    /** PRC_医保缴费基数核对(AAE214_2) */
    @Transactional
    fun ybHedui(aae001: String, aaa030: String, aaa020: String): Map<String, Any?> = try {
        val y = nowY(); val y01 = (y + "01").toLong()
        heduiGuard(2, aaa030, aaa020, aae001)?.let { return it }
        val rows = jdbc.queryForList(
            "SELECT * FROM aa97 WHERE aaa030=? AND aaa110=? AND aae001=?",
            aaa030.toLong(), aaa020, aae001.toLong())
        var marked = 0
        for (r in rows) {
            val aaa970 = nm(r["aaa970"]) ?: continue
            try {
                val t = loadTc90(r)
                val pa = s(t["atc90pa"]); val rr = s(t["atb90r"]); val a602 = bd(t["aaa602"])
                val e80 = is80(s(t["aac008"]))
                val base = (pa == "0" && (a602 == null || a602.toLong() > y01)) ||
                    (s(t["aaa402"]) == "1" && pa == null && rr == "3" &&
                        bd(t["aae002"])?.let { it.toLong() < y01 } == true) ||
                    pa == "3" ||
                    (pa == "2" && rr == "3")
                // 忠实: 80 旁路在外层, 连 AAE00P 都绕过
                val eligible = (base && in157(s(t["aae00p"]))) || e80
                if (!eligible) throw Mark0()
                val casePa = when {
                    pa == null -> "2"
                    pa == "3" -> "0"
                    e80 && substr34(s(t["aaa110"])) != "99" -> null
                    e80 -> "0"
                    else -> pa
                }
                jdbc.update(
                    """UPDATE aa97 SET atc90pa=?, aaa005=?, aaa302=?, aaa302_1=?, aae214_2='3'
                       WHERE (aae214_2 IS NULL OR aae214_2='0' OR aae214_2='3') AND aaa970=?""",
                    casePa, s(t["aaa005"]), bd0(t["aaa302"]), bd0(r["aaa302_1"]), aaa970)
                marked++
            } catch (e: Mark0) {
                jdbc.update("UPDATE aa97 SET aae214_2='0' WHERE aaa970=?", aaa970)
            }
        }
        mapOf("code" to ok, "msg" to null as String?, "rows" to rows.size, "marked" to marked, "skipped" to null)
    } catch (e: JishuErr) {
        throw e
    } catch (e: Exception) {
        throw JishuErr("缴费基数核对出错，错误信息：${e.cause?.message ?: e.message}，请将错误信息发给维护工程师")
    }

    /** PRC_公积金缴费基数核对(AAE214_3) — 四线中唯一活跃调 clamp 且夹取值写回申报基数 */
    @Transactional
    fun gjjHedui(aae001: String, aaa030: String, aaa020: String): Map<String, Any?> = try {
        val y = nowY(); val y01 = (y + "01").toLong()
        heduiGuard(3, aaa030, aaa020, aae001)?.let { return it }
        val rows = jdbc.queryForList(
            "SELECT * FROM aa97 WHERE aaa030=? AND aaa120=? AND aae001=?",
            aaa030.toLong(), aaa020, aae001.toLong())
        var marked = 0
        for (r in rows) {
            val aaa970 = nm(r["aaa970"]) ?: continue
            try {
                val t = loadTc90(r)
                val w = s(t["atc90w"]); val rr = s(t["atb90r"]); val a502 = bd(t["aaa502"])
                val eligible = in157(s(t["aae00p"])) && (
                    (w == "0" && (a502 == null || a502.toLong() > y01)) ||
                        (s(t["aaa407"]) == "1" && w == null && rr == "3" &&
                            bd(t["aae002"])?.let { it.toLong() < y01 } == true) ||
                        w == "3" ||
                        (w == "2" && rr == "3"))
                if (!eligible) throw Mark0()
                // 忠实: clamp IN=OUT=303_1 → 夹取值写回 R_AA97.AAA303_1
                var s3031 = bd(r["aaa303_1"])
                if (bd0(s3031) > Z) {
                    s3031 = bd(engine.clampBase("3", nowYm(), s(t["aaa120"]), s3031))
                    ?: throw JishuErr("缴费基数上下限设置不正确|")
                }
                // 省/市两分支同为 ROUND 整数化(源注释"市进位"与代码不符, 按代码忠实)
                fun round0(a: BigDecimal, p: BigDecimal): BigDecimal =
                    a.multiply(p).divide(BigDecimal(100), 0, RoundingMode.HALF_UP)
                val abpD = round0(bd0(s3031), bd0(r["atb90s_dw"]))
                val abpC = round0(bd0(s3031), bd0(r["atb90s_gr"]))
                val abpE = bd0(abpC).add(bd0(abpD))
                val a303 = bd0(t["aaa303"])
                // 铁律3: 金额比较 compareTo
                val case214 = if (a303.compareTo(bd0(s3031)) != 0) "3" else "0"
                val caseW = when { w == null -> "2"; w == "3" -> "0"; else -> w }
                jdbc.update(
                    """UPDATE aa97 SET atc90w=?, aaa303=?, aaa303_1=?, abp00d=?, abp00c=?, abp00e=?,
                       aae214_3=?, aaa004=?
                       WHERE (aae214_3 IS NULL OR aae214_3='0' OR aae214_3='3') AND aaa970=?""",
                    caseW, a303, bd0(s3031), abpD.toPlainString(), abpC.toPlainString(),
                    abpE.toPlainString(), case214, s(t["aaa004"]), aaa970)
                marked++
            } catch (e: Mark0) {
                jdbc.update("UPDATE aa97 SET aae214_3='0' WHERE aaa970=?", aaa970)
            }
        }
        mapOf("code" to ok, "msg" to null as String?, "rows" to rows.size, "marked" to marked, "skipped" to null)
    } catch (e: JishuErr) {
        throw e
    } catch (e: Exception) {
        throw JishuErr("缴费基数核对出错，错误信息：${e.cause?.message ?: e.message}，请将错误信息发给维护工程师")
    }

    /** PRC_工伤缴费基数核对(AAE214_4) — 主路径'3'项被注释 + ELSIF PB='3' 二次分支 */
    @Transactional
    fun gsHedui(aae001: String, aaa030: String, aaa020: String): Map<String, Any?> = try {
        val y = nowY(); val y01 = (y + "01").toLong()
        heduiGuard(4, aaa030, aaa020, aae001)?.let { return it }
        val rows = jdbc.queryForList(
            "SELECT * FROM aa97 WHERE aaa030=? AND aaa130=? AND aae001=?",
            aaa030.toLong(), aaa020, aae001.toLong())
        var marked = 0
        for (r in rows) {
            val aaa970 = nm(r["aaa970"]) ?: continue
            try {
                val t = loadTc90(r)
                val pb = s(t["atc90pb"]); val rr = s(t["atb90r"]); val a902 = bd(t["aaa902"])
                val e80 = is80(s(t["aac008"]))
                val base = (pb == "0" && (a902 == null || a902.toLong() > y01)) ||
                    (s(t["aaa404"]) == "1" && pb == null && rr == "3" &&
                        bd(t["aae002"])?.let { it.toLong() < y01 } == true) ||
                    (pb == "2" && rr == "3")
                val eligible = (base && in157(s(t["aae00p"]))) || e80
                if (eligible) {
                    gsUpdate(r, t, aaa970); marked++
                } else if (pb == "3") {
                    val cnt = jdbc.queryForObject(
                        "SELECT count(*) FROM aa97 WHERE aae001=? AND aac001=?",
                        Int::class.java, aae001.toLong(), nm(r["aac001"]))
                    if (cnt == 1) {
                        if (bd0(r["aaa305_1"]) > Z && !e80) {
                            // 忠实: OUT=AAA305 随即被 UPDATE 的 TC90.AAA305 覆盖, 仅报错不落值
                            try {
                                engine.clampBase("2", nowYm(), s(t["aaa110"]), bd(r["aaa305_1"]))
                            } catch (e: Exception) {
                                throw JishuErr("缴费基数上下限设置不正确|")
                            }
                        }
                        gsUpdate(r, t, aaa970); marked++
                    } else throw Mark0()
                } else throw Mark0()
            } catch (e: Mark0) {
                jdbc.update("UPDATE aa97 SET aae214_4='0' WHERE aaa970=?", aaa970)
            }
        }
        mapOf("code" to ok, "msg" to null as String?, "rows" to rows.size, "marked" to marked, "skipped" to null)
    } catch (e: JishuErr) {
        throw e
    } catch (e: Exception) {
        throw JishuErr("缴费基数核对出错，错误信息：${e.cause?.message ?: e.message}，请将错误信息发给维护工程师")
    }

    private fun gsUpdate(r: Map<String, Any?>, t: Map<String, Any?>, aaa970: Long) {
        val pb = s(t["atc90pb"])
        val casePb = when { pb == null -> "2"; pb == "3" -> "0"; else -> pb }
        jdbc.update(
            """UPDATE aa97 SET atc90pb=?, aaa009=?, aaa305=?, aaa305_1=?, aae214_4='3'
               WHERE (aae214_4 IS NULL OR aae214_4='0' OR aae214_4='3') AND aaa970=?""",
            casePb, s(t["aaa009"]), bd0(t["aaa305"]), bd0(r["aaa305_1"]), aaa970)
    }

    private fun loadTc90(r: Map<String, Any?>): Map<String, Any?> =
        jdbc.queryForList("SELECT * FROM tc90 WHERE atc900=?", nm(r["atc900"]))
            .firstOrNull() ?: throw Mark0()

    /** 已确认 guard: 本线任一 '1' 行 → 静默跳过(现行版, 源 SELECT COUNT 守卫已注释) */
    private fun heduiGuard(k: Int, aaa030: String, aaa020: String, aae001: String): Map<String, Any?>? {
        val col = "aaa100,aaa110,aaa120,aaa130".split(",")[k - 1]
        val flag = "aae214_$k"
        val n = jdbc.queryForObject(
            "SELECT count(*) FROM aa97 WHERE $flag='1' AND $col=? AND aaa030=? AND aae001=?",
            Int::class.java, aaa020, aaa030.toLong(), aae001.toLong())
        return if (n > 0) mapOf(
            "code" to ok, "msg" to null as String?, "skipped" to "已确认年度不重新核对",
            "rows" to 0, "marked" to 0) else null
    }

    // ==================== 确认×4 ====================

    /** PRC_社保缴费基数确认: TC90←301_1(ATC905/AAA301/AAA304) by 同人 MAX(ATC900) */
    @Transactional
    fun sbQueren(aae001: String, aaa030: String, aaa020: String, aae002: String, aae011: String): Map<String, Any?> {
        sbHedui(aae001, aaa030, aaa020)
        val user = loadUser(aae011)
        val rows = jdbc.queryForList(
            "SELECT * FROM aa97 WHERE aae214_1='3' AND aaa030=? AND aaa100=? AND aae001=?",
            aaa030.toLong(), aaa020, aae001.toLong())
        for (r in rows) {
            jdbc.update(
                """UPDATE tc90 SET atc905=?, aaa301=?, aaa304=?
                   WHERE atc900=(SELECT max(atc900) FROM tc90 WHERE aae00p IN ('1','5','7') AND aac001=?)""",
                bd0(r["aaa301_1"]), bd0(r["aaa301_1"]), bd0(r["aaa301_1"]), nm(r["aac001"]))
            querenMark(r, 1, aae002, user)
        }
        return mapOf("code" to ok, "msg" to null as String?, "confirmed" to rows.size)
    }

    /** PRC_医保缴费基数确认: TC90←302_1(AAA302/AAA306) by 自身 ATC900 */
    @Transactional
    fun ybQueren(aae001: String, aaa030: String, aaa020: String, aae002: String, aae011: String): Map<String, Any?> {
        ybHedui(aae001, aaa030, aaa020)
        val user = loadUser(aae011)
        val rows = jdbc.queryForList(
            "SELECT * FROM aa97 WHERE aae214_2='3' AND aaa030=? AND aaa110=? AND aae001=?",
            aaa030.toLong(), aaa020, aae001.toLong())
        for (r in rows) {
            jdbc.update(
                "UPDATE tc90 SET aaa302=?, aaa306=? WHERE atc900=?",
                bd0(r["aaa302_1"]), bd0(r["aaa302_1"]), nm(r["atc900"]))
            querenMark(r, 2, aae002, user)
        }
        return mapOf("code" to ok, "msg" to null as String?, "confirmed" to rows.size)
    }

    /** PRC_公积金缴费基数确认: TC90←303_1(AAA303) by 同人 MAX(ATC900) */
    @Transactional
    fun gjjQueren(aae001: String, aaa030: String, aaa020: String, aae002: String, aae011: String): Map<String, Any?> {
        gjjHedui(aae001, aaa030, aaa020)
        val user = loadUser(aae011)
        val rows = jdbc.queryForList(
            "SELECT * FROM aa97 WHERE aae214_3='3' AND aaa030=? AND aaa120=? AND aae001=?",
            aaa030.toLong(), aaa020, aae001.toLong())
        for (r in rows) {
            jdbc.update(
                """UPDATE tc90 SET aaa303=?
                   WHERE atc900=(SELECT max(atc900) FROM tc90 WHERE aae00p IN ('1','5','7') AND aac001=?)""",
                bd0(r["aaa303_1"]), nm(r["aac001"]))
            querenMark(r, 3, aae002, user)
        }
        return mapOf("code" to ok, "msg" to null as String?, "confirmed" to rows.size)
    }

    /** PRC_工伤缴费基数确认: TC90←305_1(AAA305) by 自身 ATC900 */
    @Transactional
    fun gsQueren(aae001: String, aaa030: String, aaa020: String, aae002: String, aae011: String): Map<String, Any?> {
        gsHedui(aae001, aaa030, aaa020)
        val user = loadUser(aae011)
        val rows = jdbc.queryForList(
            "SELECT * FROM aa97 WHERE aae214_4='3' AND aaa030=? AND aaa130=? AND aae001=?",
            aaa030.toLong(), aaa020, aae001.toLong())
        for (r in rows) {
            jdbc.update(
                "UPDATE tc90 SET aaa305=? WHERE atc900=?",
                bd0(r["aaa305_1"]), nm(r["atc900"]))
            querenMark(r, 4, aae002, user)
        }
        return mapOf("code" to ok, "msg" to null as String?, "confirmed" to rows.size)
    }

    private fun loadUser(aae011: String): Array<String?> =
        jdbc.query(
            "SELECT aae011, aae017, aae019, aae020, aae022 FROM sys_org_user_unit WHERE aae011=?",
            { rs, _ -> arrayOf(rs.getString("aae011"), rs.getString("aae017"), rs.getString("aae019"),
                rs.getString("aae020"), rs.getString("aae022")) }, aae011).firstOrNull()
            ?: throw JishuErr("查询经办用户出错")

    /** AA97 置 '1' + 生效年月 + 经办六件套(_k 后缀) */
    private fun querenMark(r: Map<String, Any?>, k: Int, aae002: String, user: Array<String?>) {
        val aaa970 = nm(r["aaa970"]) ?: return
        jdbc.update(
            """UPDATE aa97 SET aae002_$k=?, aae214_$k='1', aae211_$k=?, aae217_$k=?,
               aae236_$k=?, aae219_$k=?, aae220_$k=?, aae222_$k=? WHERE aaa970=?""",
            aae002.toLongOrNull(), user[0], user[1], Timestamp(System.currentTimeMillis()),
            user[2], user[3], user[4], aaa970)
    }
}

// ==================== /proc 注册面 ====================

private const val HD_PARAMS = "缺少参数(需 aae001/aaa030/aaa020)"
private const val QR_PARAMS = "缺少参数(需 aae001/aaa030/aaa020/aae002/aae011)"

private fun hdMap(e: Exception): Map<String, Any?> =
    if (e is JishuErr) mapOf("appCode" to -1, "errMsg" to e.m)
    else mapOf("appCode" to -1, "errMsg" to e.message)

@Component
class Jishu5SbHdProc(private val c: Jishu5Cases) : ProcHandler {
    override fun procName() = "PKG_缴费基数.PRC_社保缴费基数核对"
    override fun handle(params: Map<String, Any?>): Map<String, Any?> {
        if (params["aae001"] == null || params["aaa030"] == null || params["aaa020"] == null)
            return mapOf("appCode" to -1, "errMsg" to HD_PARAMS)
        return try {
            val r = c.sbHedui(params["aae001"]!!.toString(), params["aaa030"]!!.toString(),
                params["aaa020"]!!.toString())
            mapOf("appCode" to r["code"], "errMsg" to r["msg"], "rows" to r["rows"],
                "marked" to r["marked"], "skipped" to r["skipped"])
        } catch (e: Exception) { hdMap(e) }
    }
}

@Component
class Jishu5YbHdProc(private val c: Jishu5Cases) : ProcHandler {
    override fun procName() = "PKG_缴费基数.PRC_医保缴费基数核对"
    override fun handle(params: Map<String, Any?>): Map<String, Any?> {
        if (params["aae001"] == null || params["aaa030"] == null || params["aaa020"] == null)
            return mapOf("appCode" to -1, "errMsg" to HD_PARAMS)
        return try {
            val r = c.ybHedui(params["aae001"]!!.toString(), params["aaa030"]!!.toString(),
                params["aaa020"]!!.toString())
            mapOf("appCode" to r["code"], "errMsg" to r["msg"], "rows" to r["rows"],
                "marked" to r["marked"], "skipped" to r["skipped"])
        } catch (e: Exception) { hdMap(e) }
    }
}

@Component
class Jishu5GjjHdProc(private val c: Jishu5Cases) : ProcHandler {
    override fun procName() = "PKG_缴费基数.PRC_公积金缴费基数核对"
    override fun handle(params: Map<String, Any?>): Map<String, Any?> {
        if (params["aae001"] == null || params["aaa030"] == null || params["aaa020"] == null)
            return mapOf("appCode" to -1, "errMsg" to HD_PARAMS)
        return try {
            val r = c.gjjHedui(params["aae001"]!!.toString(), params["aaa030"]!!.toString(),
                params["aaa020"]!!.toString())
            mapOf("appCode" to r["code"], "errMsg" to r["msg"], "rows" to r["rows"],
                "marked" to r["marked"], "skipped" to r["skipped"])
        } catch (e: Exception) { hdMap(e) }
    }
}

@Component
class Jishu5GsHdProc(private val c: Jishu5Cases) : ProcHandler {
    override fun procName() = "PKG_缴费基数.PRC_工伤缴费基数核对"
    override fun handle(params: Map<String, Any?>): Map<String, Any?> {
        if (params["aae001"] == null || params["aaa030"] == null || params["aaa020"] == null)
            return mapOf("appCode" to -1, "errMsg" to HD_PARAMS)
        return try {
            val r = c.gsHedui(params["aae001"]!!.toString(), params["aaa030"]!!.toString(),
                params["aaa020"]!!.toString())
            mapOf("appCode" to r["code"], "errMsg" to r["msg"], "rows" to r["rows"],
                "marked" to r["marked"], "skipped" to r["skipped"])
        } catch (e: Exception) { hdMap(e) }
    }
}

private fun qrCall(c: Jishu5Cases, params: Map<String, Any?>, line: String): Map<String, Any?> {
    if (params["aae001"] == null || params["aaa030"] == null || params["aaa020"] == null ||
        params["aae002"] == null || params["aae011"] == null)
        return mapOf("appCode" to -1, "errMsg" to QR_PARAMS)
    return try {
        val r = when (line) {
            "sb" -> c.sbQueren(params["aae001"]!!.toString(), params["aaa030"]!!.toString(),
                params["aaa020"]!!.toString(), params["aae002"]!!.toString(), params["aae011"]!!.toString())
            "yb" -> c.ybQueren(params["aae001"]!!.toString(), params["aaa030"]!!.toString(),
                params["aaa020"]!!.toString(), params["aae002"]!!.toString(), params["aae011"]!!.toString())
            "gjj" -> c.gjjQueren(params["aae001"]!!.toString(), params["aaa030"]!!.toString(),
                params["aaa020"]!!.toString(), params["aae002"]!!.toString(), params["aae011"]!!.toString())
            else -> c.gsQueren(params["aae001"]!!.toString(), params["aaa030"]!!.toString(),
                params["aaa020"]!!.toString(), params["aae002"]!!.toString(), params["aae011"]!!.toString())
        }
        mapOf("appCode" to r["code"], "errMsg" to r["msg"], "confirmed" to r["confirmed"])
    } catch (e: Exception) { hdMap(e) }
}

@Component
class Jishu5SbQrProc(private val c: Jishu5Cases) : ProcHandler {
    override fun procName() = "PKG_缴费基数.PRC_社保缴费基数确认"
    override fun handle(params: Map<String, Any?>) = qrCall(c, params, "sb")
}

@Component
class Jishu5YbQrProc(private val c: Jishu5Cases) : ProcHandler {
    override fun procName() = "PKG_缴费基数.PRC_医保缴费基数确认"
    override fun handle(params: Map<String, Any?>) = qrCall(c, params, "yb")
}

@Component
class Jishu5GjjQrProc(private val c: Jishu5Cases) : ProcHandler {
    override fun procName() = "PKG_缴费基数.PRC_公积金缴费基数确认"
    override fun handle(params: Map<String, Any?>) = qrCall(c, params, "gjj")
}

@Component
class Jishu5GsQrProc(private val c: Jishu5Cases) : ProcHandler {
    override fun procName() = "PKG_缴费基数.PRC_工伤缴费基数确认"
    override fun handle(params: Map<String, Any?>) = qrCall(c, params, "gs")
}
