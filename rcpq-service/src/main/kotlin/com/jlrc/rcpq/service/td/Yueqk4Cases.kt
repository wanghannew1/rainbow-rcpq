package com.jlrc.rcpq.service.td

import com.jlrc.rcpq.service.calc.InsuranceCalcEngine
import com.jlrc.rcpq.service.proc.ProcHandler
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal

/**
 * W4c-2 应缴费情况（旧 PRC_市医保应缴费情况 L1552-1700 规格重写, 2026-11-17, tdcbj 页）。
 *
 * 三态候选过滤(忠实): (参保中 ATC90PA 0/3 且 AAE002<=当月 且 AAA602 未到期) OR
 * (停保 ATC90PA='1' 且 AAA609=当月) OR (未参保 ATC90PA NULL 且 AAE002=当月);
 * 新参保行 ATJ0AA='1' 并查 TC9W1.ABP016 ('1','2' 跳过该行); 单元排除 37664(忠实);
 * 明细全量插 TJ0C2(无金额门槛); 旧尾部散 COMMIT 无实义不移植。
 */
@Component
class Yueqk4Cases(private val jdbc: JdbcTemplate, private val engine: InsuranceCalcEngine) {
    private val ok = 1
    private val Z = BigDecimal.ZERO

    private fun pct(v: BigDecimal?): String? = v?.stripTrailingZeros()?.toPlainString() + "%"

    @Transactional
    fun yingjiaofei(aae002: String, aaa020: String, aaa030: String, aae011: String): Map<String, Any?> {
        val ym = aae002.toLongOrNull() ?: throw YueErr4("年月格式不正确|")
        jdbc.query("SELECT id FROM aa12 WHERE id=?", { rs, _ -> rs.getString("id") }, aaa020)
            .firstOrNull() ?: throw YueErr4("参保地区划信息不正确")
        val aa03 = jdbc.query("SELECT aaa030, aaa031 FROM aa03 WHERE aaa030=?",
            { rs, _ -> arrayOf(rs.getObject("aaa030"), rs.getString("aaa031")) }, aaa030.toLong())
            .firstOrNull() ?: throw YueErr4("险金户头信息不正确")
        val user = jdbc.query(
            "SELECT aae011, aae017, aae019, aae020, aae022 FROM sys_org_user_unit WHERE aae011=?",
            { rs, _ -> arrayOf(rs.getString("aae011"), rs.getString("aae017"), rs.getString("aae019"),
                rs.getString("aae020"), rs.getString("aae022")) }, aae011).firstOrNull()
            ?: throw YueErr4("经办人信息不正确")
        jdbc.update("DELETE FROM tj0c2 WHERE aaa020=? AND aaa030=? AND aae002=?",
            aaa020, aaa030.toLong(), ym)
        val emps = jdbc.queryForList(
            """SELECT a.*, b.aaa030 AS u30, b.aaa031 AS u31, c.atb931
               FROM tc90 a, ab01 b, tb93 c
               WHERE a.aab001=b.aab001 AND a.atb930=c.atb930
                 AND aaa402='1' AND aaa110=? AND b.aaa030=? AND a.atb930<>37664
                 AND ((atc90pa IN ('0','3') AND a.aae002<=? AND (aaa602 IS NULL OR aaa602>?))
                      OR (atc90pa='1' AND aaa609=?)
                      OR (atc90pa IS NULL AND a.aae002=? AND aaa402='1'))
                 ORDER BY a.atc900, a.atb930""",
            aaa020, aaa030.toLong(), ym, ym, ym, ym)
        var rows = 0
        for (yg in emps) {
            val atc900 = (yg["atc900"] as Number).toLong()
            // 新参保行: ATJ0AA='1' + TC9W1 报增行 ABP016 ('1','2') → 跳过(忠实)
            var atj0aa = "0"
            if ((yg["aae002"] as Number?)?.toLong() == ym && yg["atc90pa"] == null) {
                atj0aa = "1"
                val abp016 = jdbc.query(
                    "SELECT abp016 FROM tc9w1 WHERE atc9w1='1' AND aae002=? AND atc900=?",
                    { rs, _ -> rs.getString("abp016") }, ym, atc900).firstOrNull()
                if (abp016 == "1" || abp016 == "2") continue
            }
            val row = jdbc.query(
                "SELECT * FROM tc90 WHERE atc900=?", { rs, _ ->
                val md = rs.metaData
                (1..md.columnCount).associate { md.getColumnLabel(it).lowercase() to rs.getObject(it) }
            }, atc900).firstOrNull()
            val (code, result) = engine.calc(row!!, ym.toString(), InsuranceCalcEngine.Tc01(jslb = "2"))
            if (code == -1) throw YueErr4("计算险金信息出错：$result")
            val c = result as InsuranceCalcEngine.Tc01
            val a3 = (c.bba002 ?: Z).add(c.bba004 ?: Z).add(c.bba005 ?: Z)
            val a5 = baa002Row(c).add(a3)
            try {
                jdbc.update(
                    """INSERT INTO tj0c2 (aae002, atc900, atb930, atb931, aab001, aab004, aaa030,
                       aaa031, aaa020, aaa021, aaa001, aac003, aac002, aaa603, aaa302, aaa305,
                       aaa306, atb90l, atb90n, atb90o, atb90q, baa002, bba002, bba004, bba005,
                       daa001, atj0c3, atj0c5, atj0aa, aac001)
                       VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?,
                       ?, ?, ?, ?, ?)""",
                    ym, atc900, yg["atb930"], yg["atb931"], yg["aab001"], yg["aab004"],
                    yg["u30"], yg["u31"], yg["aaa110"], yg["aaa111"], yg["aaa005"],
                    yg["aac003"], yg["aac002"], yg["aaa603"],
                    c.aaa302, c.aaa305, c.aaa306,
                    pct(c.atb90l), pct(c.atb90n), pct(c.atb90o), pct(c.atb90q),
                    c.baa002 ?: Z, c.bba002 ?: Z, c.bba004 ?: Z, c.bba005 ?: Z,
                    c.daa001 ?: Z, a3, a5, atj0aa, nm2(yg["aac001"]))
            } catch (e: Exception) {
                throw YueErr4("记录应缴费明细信息出错${e.message ?: ""}")
            }
            rows++
        }
        return mapOf("code" to ok, "msg" to null as String?, "rows" to rows,
            "operator" to (user[0] ?: ""))
    }

    private fun baa002Row(c: InsuranceCalcEngine.Tc01) = c.baa002 ?: Z
    private fun nm2(v: Any?): Long? = (v as Number?)?.toLong()
}

class YueErr4(val m: String) : RuntimeException(m)

@Component
class Yueqk4YjProc(private val c: Yueqk4Cases) : ProcHandler {
    override fun procName() = "PKG_派遣保险月情况.PRC_市医保应缴费情况"
    override fun handle(params: Map<String, Any?>): Map<String, Any?> {
        for (k in listOf("aae002", "aaa020", "aaa030", "aae011"))
            if (params[k] == null) return mapOf("appCode" to -1, "errMsg" to "缺少 $k")
        val r = try {
            c.yingjiaofei(params["aae002"]!!.toString(), params["aaa020"]!!.toString(),
                params["aaa030"]!!.toString(), params["aae011"]!!.toString())
        } catch (e: YueErr4) {
            mapOf("code" to -1, "msg" to e.m, "rows" to 0, "skipped" to null)
        }
        return mapOf("appCode" to r["code"], "errMsg" to r["msg"], "rows" to r["rows"])
    }
}
