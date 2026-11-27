package com.jlrc.rcpq.service.td

import com.jlrc.rcpq.service.proc.ProcHandler
import org.springframework.stereotype.Component
import java.math.BigDecimal

/** 12 比例出参束(源 PRM_ATB90K..PRM_AAA399 OUT VARCHAR2) */
data class SettleRates(
    val k: String?, val l: String?, val m: String?, val n: String?, val nJ: String?,
    val o: String?, val p: String?, val q: String?, val r: String?,
    val sDw: String?, val sGr: String?, val aaa399: String?,
)

@Component
class TdSettleRateCases(private val s: TdSettleSupport) {

    /** Oracle TO_CHAR(x) 整数/小数直排(16→'16'); TO_CHAR(x,'0.99')/'0.990' 两位/三位补零 */
    private fun fmt(v: BigDecimal, pat: Int?): String =
        if (pat == null) v.stripTrailingZeros().toPlainString()
        else String.format("%.${pat}f", v)

    /** 源逐值 CASE: AAA942<1 → TO_CHAR(x,'0.99'/'0.990')(smallPat 位小数), 否则 TO_CHAR(x) 直排 */
    private fun fmt94(v: BigDecimal, smallPat: Int?): String =
        if (v < BigDecimal.ONE) String.format("%.${smallPat ?: 2}f", v)
        else v.stripTrailingZeros().toPlainString()

    private fun fill94(tab: MutableMap<String, String>, codes: List<String>, region: String?, ym: Long, smallPat: Int?) {
        if (region == null) return
        val ph = codes.joinToString(",") { "?" }
        val rows = s.jdbc.queryForList(
            "SELECT aaa941, aaa942 FROM aa94 WHERE aaa941 IN ($ph) " +
                "AND aaa946 <= ? AND aaa947 >= ? AND aaa100=?",
            *codes.toTypedArray(), ym, ym, region)
        for (r in rows) {
            val code = s.str(r["aaa941"]) ?: continue
            tab[code] = fmt94(s.nvl(r["aaa942"]), smallPat)
        }
    }

    /** 取键(行内 BEGIN..EXCEPTION 缺键→'0' 同判) */
    private fun take(tab: Map<String, String>, code: String): String =
        try { tab[code]?.trim() ?: throw IndexOutOfBoundsException() } catch (e: Exception) { "0" }

    /** ①PRC_获取缴费比例_按员工(公共包 L161-446) */
    fun rates(tc90: Map<String, Any?>, aae002In: String?): SettleRates {
        val nAae002 = aae002In?.takeIf { it.isNotBlank() } ?: s.nowYm()
        val ym = nAae002.toLong()
        val tab = mutableMapOf<String, String>()
        // 社保(养老): AAA100 非空 + AAA401='1', 码 01(单位)/06(个人), <1 时 '0.99' 两位
        if (tc90["aaa100"] != null && s.str(tc90["aaa401"]) == "1") {
            fill94(tab, listOf("01", "06"), s.str(tc90["aaa100"]), ym, 2)
        }
        val k = if (tc90["aaa100"] != null && s.str(tc90["aaa401"]) == "1") take(tab, "01") else "0"
        val p = if (tc90["aaa100"] != null && s.str(tc90["aaa401"]) == "1") take(tab, "06") else "0"
        // 失业: 同门, 码 03(单位)/08(个人), <1 时 '0.99' 两位
        if (tc90["aaa100"] != null && s.str(tc90["aaa401"]) == "1") {
            fill94(tab, listOf("03", "08"), s.str(tc90["aaa100"]), ym, 2)
        }
        val m = if (tc90["aaa100"] != null && s.str(tc90["aaa401"]) == "1") take(tab, "03") else "0"
        val r = if (tc90["aaa100"] != null && s.str(tc90["aaa401"]) == "1") take(tab, "08") else "0"
        // 医保: AAA110 非空 + AAA402='1', 码 02/04/05/07/12 格式 '0.990';
        // 省医保(substr(3,2)='99') AAA399=12 码, 市医保 AAA399 恒 '0'
        val prov110 = s.str(tc90["aaa110"])?.let { it.length >= 4 && it.substring(2, 4) == "99" } ?: false
        if (tc90["aaa110"] != null && s.str(tc90["aaa402"]) == "1") {
            fill94(tab, listOf("02", "04", "05", "07", "12"), s.str(tc90["aaa110"]), ym, 3)
        }
        val medGate = tc90["aaa110"] != null && s.str(tc90["aaa402"]) == "1"
        val l = if (medGate) take(tab, "02") else "0"
        var n = if (medGate) take(tab, "04") else "0"
        val o = if (medGate) take(tab, "05") else "0"
        val q = if (medGate) take(tab, "07") else "0"
        val a399 = when {
            !medGate -> "0"
            prov110 -> take(tab, "12")
            else -> "0"
        }
        // 工伤(AA94 段): AAA130 非空 + AAA404='1', 码 04 覆写
        if (tc90["aaa130"] != null && s.str(tc90["aaa404"]) == "1") {
            fill94(tab, listOf("04"), s.str(tc90["aaa130"]), ym, 3)
            n = take(tab, "04")
        }
        // 公积金: ATB90S 非空 + AAA407='1'; 跨年先查 AA97 下年度已确认行, 无则回退本行
        var sDw: String?; var sGr: String?
        if (tc90["atb90s"] != null && s.str(tc90["aaa407"]) == "1") {
            var vAtb90s = s.str(tc90["atb90s"])
            if (nAae002.substring(0, 4) != s.nowYm().substring(0, 4)) {
                val nextYear = (nAae002.substring(0, 4).toInt() + 1).toString()
                vAtb90s = s.jdbc.queryForList(
                    "SELECT atb90s FROM aa97 WHERE aae214_3 IN ('1','3') AND aae001=? AND aac001=?",
                    nextYear.toLong(), s.long(tc90["aac001"])).singleOrNull()
                    ?.let { s.str(it["atb90s"]) } ?: s.str(tc90["atb90s"])
            }
            val idx = vAtb90s?.indexOf(':') ?: -1
            if (idx < 0) {
                // Oracle: SUBSTR(v,1,INSTR-1)=SUBSTR(v,1,-1)=NULL; SUBSTR(v,INSTR+1,LEN)=SUBSTR(v,1,LEN)=v
                sDw = null; sGr = vAtb90s
            } else {
                sDw = vAtb90s!!.substring(0, idx)
                sGr = vAtb90s.substring(idx + 1)
            }
        } else { sDw = "0"; sGr = "0" }
        // 工伤 legacy(AA03×AB01): 市医保(substr≠'99') 且 <202203 → AAA034/AAA035 覆写, 行内异常→0/0
        val city110 = !(s.str(tc90["aaa110"])?.let { it.length >= 4 && it.substring(2, 4) == "99" } ?: true)
        if (city110 && ym < 202203) {
            val rows = s.jdbc.queryForList(
                "SELECT a.aaa034, a.aaa035 FROM aa03 a, ab01 b WHERE a.aaa030=b.aaa030 AND b.aab001=?",
                s.long(tc90["aab001"]))
            val one = rows.singleOrNull()
            if (one == null) { n = "0" }
            else {
                n = s.nvl(one["aaa034"]).stripTrailingZeros().toPlainString()
                var nJ = s.nvl(one["aaa035"]).stripTrailingZeros().toPlainString()
                return SettleRates(k, l, m, n, nJ, o, p, q, r, sDw, sGr, a399)
            }
        }
        return SettleRates(k, l, m, n, "0", o, p, q, r, sDw, sGr, a399)
    }

    /**
     * PRC_保险补退明细 门控双轨(铁律1 先例; 源 保险补退包 L2906-3086):
     * DELETE TC07(datasource='30') 段忠实; 五子过程门条件逐字求值——
     *   社保 atc90p∈('0','1')∧aaa208≥N_BJ_BEGIN; 医保 atc90pa∧aaa608∧省99;
     *   工伤 atc90pb∧aaa908; 公积金 atc90w∧aaa508; 补差=基数补差历史行存在
     * 门关(如 BC-09 夹具旗标全'2')→与源一致的零操作; 门开→数据面循环(~4700 行)未移植,
     * 报错即止不静默(回执注明)。
     */
    fun insuranceDetail(atc900: Long, aae002: String, aae011: String) {
        try { s.userRow(aae011) } catch (e: Exception) {
            throw SettleRet(-1, "无法计算补退，原因为经办人信息不正确")
        }
        val tc90 = s.single("v_tc90_qpkg", "atc900=?", atc900)
            ?: throw SettleRet(-1, "无法计算补退，原因为查询合同信息出错")
        s.jdbc.update(
            "DELETE FROM tc07 WHERE atc07z='0' AND datasource='30' AND aae002=?::numeric " +
                "AND atb930=? AND aac001=?",
            aae002.toBigDecimalOrNull() ?: aae002, tc90["atb930"], tc90["aac001"])
        // N_BJ_BEGIN = TC07(datasource='30', 非零和) max(aae003)+1, 无行→202212
        val nBjBegin = s.jdbc.queryForObject(
            """SELECT CASE WHEN MAX(aae003) IS NULL THEN 202212 ELSE MAX(aae003)::bigint END
               FROM (SELECT aae003 FROM tc07 WHERE atc07z IN ('0','1','9') AND datasource='30'
                     AND (COALESCE(bba001,0)+COALESCE(bba003,0)+COALESCE(baa001,0)+COALESCE(baa003,0))
                         + COALESCE(aaa913,0) <> 0 AND aae002 < ?::numeric AND atc900=?) t""",
            Long::class.java, aae002.toBigDecimalOrNull() ?: aae002, atc900) ?: 202212L
        val ym = aae002.toBigDecimalOrNull() ?: BigDecimal.ZERO
        fun flagGate(flag: String?, stop: Any?, extra: Boolean = true): Boolean =
            (flag == "0" || flag == "1") && s.nvl(stop).toLong() >= nBjBegin &&
                nBjBegin <= ym.toLong() && extra
        val prov110 = s.str(tc90["aaa110"])?.let { it.length >= 4 && it.substring(2, 4) == "99" } ?: false
        val anyOpen =
            flagGate(s.str(tc90["atc90p"]), tc90["aaa208"]) ||
                flagGate(s.str(tc90["atc90pa"]), tc90["aaa608"], prov110) ||
                flagGate(s.str(tc90["atc90pb"]), tc90["aaa908"]) ||
                flagGate(s.str(tc90["atc90w"]), tc90["aaa508"]) ||
                (s.jdbc.queryForObject(
                    "SELECT COUNT(*) FROM tc07 WHERE atc07z IN ('0','1') " +
                        "AND atc072 IN ('16','24','25','26') AND atb930=? AND aac001=?",
                    Int::class.java, tc90["atb930"], tc90["aac001"]) ?: 0) > 0
        if (anyOpen) {
            throw SettleRet(-1,
                "无法计算补退，补退明细五子过程数据面循环未移植(桥单 20261127-034 门控双轨: 门开路径待补)")
        }
    }
}

/** /proc 注册面: PKG_人才派遣公共.PRC_获取缴费比例_按员工 */
@Component
class TdPayRateProc(private val c: TdSettleRateCases, private val s: TdSettleSupport) : ProcHandler {
    override fun procName() = "PKG_人才派遣公共.PRC_获取缴费比例_按员工"
    override fun handle(params: Map<String, Any?>): Map<String, Any?> {
        val atc900 = (params["atc900"] ?: params["prm_atc900"]
            ?: return mapOf("appCode" to -1, "errMsg" to "缺少 atc900")).toString().toLongOrNull()
            ?: return mapOf("appCode" to -1, "errMsg" to "atc900 非数值")
        val aae002 = params["aae002"]?.toString() ?: params["prm_aae002"]?.toString()
        val tc90 = s.single("tc90", "atc900=?", atc900)
            ?: return mapOf("appCode" to -1, "errMsg" to "获取缴费比例出错，错误信息：合同不存在")
        return try {
            val r = c.rates(tc90, aae002)
            mapOf("appCode" to 1, "errMsg" to null as String?,
                "atb90k" to r.k, "atb90l" to r.l, "atb90m" to r.m, "atb90n" to r.n,
                "atb90n_j" to r.nJ, "atb90o" to r.o, "atb90p" to r.p, "atb90q" to r.q,
                "atb90r" to r.r, "atb90s_dw" to r.sDw, "atb90s_gr" to r.sGr, "aaa399" to r.aaa399)
        } catch (e: Exception) {
            mapOf("appCode" to -1, "errMsg" to "测算出错，错误信息：${e.message}，请将错误信息发给维护工程师")
        }
    }
}
