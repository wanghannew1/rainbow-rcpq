package com.jlrc.rcpq.service.td

import com.jlrc.rcpq.service.calc.InsuranceCalcEngine
import com.jlrc.rcpq.service.proc.ProcHandler
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal

/**
 * W4b 月情况·公积金/工伤线（旧 PKG_派遣保险月情况, 2026-11-17）。
 * 独立于 YueqkCases(社保线) — 修订历史教训: 占位符簿记独立成文件, 避免对已验证文件做行级手术。
 *
 * 源: PACKAGE_BODY_PKG_派遣保险月情况.sql
 *   - PRC_省公积金月情况/转款情况  L1701-1940（TJ0D/D1）
 *   - PRC_市公积金月情况/转款情况  L2231-2468（TJ0E/E1, 过滤无 atb90r 分支）
 *   - PRC_工伤月情况/转款情况      L2749-3046（TJ0F/F1, TC10 UNION + TC18=202201 专项）
 * 结构性修正: 旧单位循环内裸 COMMIT → 单事务(YueErr 回滚)。硬编码个人 hack 忠实保留。
 */
class YueErr2(val m: String) : RuntimeException(m)

@Component
class Yueqk2Cases(private val jdbc: JdbcTemplate, private val engine: InsuranceCalcEngine) {
    private val ok = 1
    private val Z = BigDecimal.ZERO

    /** 24 列公积金主表 + 23 列明细: 字面量 atj000='1'/aaezzz='0'/aae036=now() → head 21 参, detail 23 参 */
    @Transactional
    fun gjjYue(aae002: String, aaa020: String, aaa030: String, aae011: String, city: Boolean): Map<String, Any?> {
        val main = if (city) "tj0e" else "tj0d"
        val detail = if (city) "tj0e1" else "tj0d1"
        val sumCol = if (city) "atj0e5" else "atj0d5"
        val cntCol = if (city) "atj0e2" else "atj0d2"
        val ym = aae002.toLongOrNull() ?: throw YueErr2("年月格式不正确|")
        jdbc.query("SELECT id, label FROM aa12 WHERE id=?", { rs, _ -> rs.getString("id") }, aaa020)
            .firstOrNull() ?: throw YueErr2("参保地区划信息不正确")
        val aa03 = jdbc.query("SELECT aaa030, aaa031 FROM aa03 WHERE aaa030=?",
            { rs, _ -> arrayOf(rs.getObject("aaa030"), rs.getString("aaa031")) }, aaa030.toLong())
            .firstOrNull() ?: throw YueErr2("险金户头信息不正确")
        val user = jdbc.query(
            "SELECT aae011, aae017, aae019, aae020, aae022 FROM sys_org_user_unit WHERE aae011=?",
            { rs, _ -> arrayOf(rs.getString("aae011"), rs.getString("aae017"), rs.getString("aae019"),
                rs.getString("aae020"), rs.getString("aae022")) }, aae011).firstOrNull()
            ?: throw YueErr2("经办人信息不正确")
        val g = jdbc.queryForObject(
            """SELECT count(*) FROM tb91 a, tc90 b, ab01 c
               WHERE a.atb91z=b.atc900 AND b.aab001=c.aab001 AND a.atb911='23' AND a.atb912='2'
                 AND b.aae00p IN ('1','5') AND c.aaa030=? AND b.aae003=?""",
            Int::class.java, aaa030.toLong(), ym)
        if (g > 0) throw YueErr2("不符合条件，原因是存在参保人员合同减员信息未审核")
        if (jdbc.queryForObject(
                "SELECT count(*) FROM $main WHERE aaezzz='1' AND aaa020=? AND aaa030=? AND aae002=?",
                Int::class.java, aaa020, aaa030.toLong(), ym) > 0)
            return mapOf("code" to ok, "msg" to null as String?, "skipped" to "已确认月份不重建", "rows" to 0)
        jdbc.update("DELETE FROM $main WHERE aaa020=? AND aaa030=? AND aae002=?", aaa020, aaa030.toLong(), ym)
        jdbc.update("DELETE FROM $detail WHERE aaa020=? AND aaa030=? AND aae002=?", aaa020, aaa030.toLong(), ym)
        val detailSql = """INSERT INTO $detail (aae002, atc900, atj000, atb930, atb931, aab001, aab004,
            aaa030, aaa031, aaa020, aaa021, aaa001, aac001, aac003, aac002, aac009,
            aaa303, atb90s_dw, atb90s_gr, caa001, caa002, $sumCol, atj0aa)
            VALUES (?, ?, '1', ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)""" // 23 参
        val headSql = """INSERT INTO $main (aae002, xh, atb930, atb931, aab001, aab004, aaa030,
            aaa031, aaa020, aaa021, atj000, $cntCol, atb90s_dw, atb90s_gr, caa001, caa002,
            $sumCol, aae011, aae017, aae036, aae019, aae020, aae022, aaezzz)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)""" // 24 参(全绑定)
        var xh = 0
        var rows = 0
        val units = jdbc.query(
            """SELECT a.atb930, a.atb931, a.aab001, b.aab004 FROM tb93 a JOIN ab01 b ON a.aab001=b.aab001
               WHERE b.aaa030=? ORDER BY a.atb931""",
            { rs, _ -> arrayOf(rs.getObject("atb930"), rs.getString("atb931"),
                rs.getObject("aab001"), rs.getString("aab004")) }, aa03[0])
        for (qy in units) {
            var rs = 0
            var caa001 = Z; var caa002 = Z
            var dwLast: String? = null; var grLast: String? = null
            val emps = if (!city) jdbc.queryForList(
                """SELECT * FROM tc90 WHERE aaa407='1' AND aaa120=?
                   AND (atb90r='3' OR (atb90r='5' AND (aaa502>=? OR aaa502 IS NULL)))
                   AND ((atc90w='0' AND aaa503<=?) OR (atc90w IS NULL AND aae002<=?)) AND atb930=?""",
                aaa020, ym, ym, ym, qy[0])
            else jdbc.queryForList(
                """SELECT * FROM tc90 WHERE aaa407='1' AND aaa120=?
                   AND ((atc90w='0' AND aaa503<=?) OR (atc90w='1' AND aaa504>?)) AND atb930=?""",
                aaa020, ym, ym, qy[0])
            for (yg in emps) {
                val atc900 = (yg["atc900"] as Number).toLong()
                val (code, result) = engine.calc(yg, ym.toString(), InsuranceCalcEngine.Tc01(jslb = "2"))
                if (code == -1) throw YueErr2("计算险金信息出错：$result")
                val c = result as InsuranceCalcEngine.Tc01
                val a5 = (c.caa001 ?: Z).add(c.caa002 ?: Z)
                val atj0aa = if ((yg["aaa508"] as Number?)?.toLong() == ym) "1" else "0"
                if (a5 > Z) {
                    rs++
                    try {
                        jdbc.update(detailSql, ym, atc900, qy[0], qy[1], qy[2], qy[3],
                            aa03[0], aa03[1], yg["aaa120"], yg["aaa121"], yg["aaa004"],
                            (yg["aac001"] as Number?)?.toLong(), yg["aac003"], yg["aac002"], yg["aac009"],
                            yg["aaa303"] as BigDecimal?,
                            yg["atb90s"]?.toString()?.let { it.substringBefore(':') + "%" },
                            yg["atb90s"]?.toString()?.let { it.substringAfter(':', "") + "%" },
                            c.caa001 ?: Z, c.caa002 ?: Z, a5, atj0aa)
                    } catch (e: Exception) {
                        throw YueErr2("记录转款明细信息出错${e.message ?: ""}")
                    }
                    rows++
                }
                caa001 = caa001.add(c.caa001 ?: Z); caa002 = caa002.add(c.caa002 ?: Z)
                c.atb90sDw?.let { dwLast = it.stripTrailingZeros().toPlainString() + "%" }
                c.atb90sGr?.let { grLast = it.stripTrailingZeros().toPlainString() + "%" }
            }
            val a5 = caa001.add(caa002)
            if (a5 > Z) {
                xh++
                try {
                    jdbc.update(headSql, ym, xh, qy[0], qy[1], qy[2], qy[3], aa03[0], aa03[1],
                        aaa020, jdbc.queryForObject("SELECT label FROM aa12 WHERE id=?",
                            String::class.java, aaa020),
                        "1", rs, dwLast, grLast, caa001, caa002, a5,
                        user[0], user[1], java.time.LocalDateTime.now(), user[2], user[3], user[4], "0")
                } catch (e: Exception) {
                    throw YueErr2("记录转款汇总信息出错${e.message ?: ""} <- ${e.cause?.message ?: ""} <- ${e.cause?.cause?.message ?: ""}")
                }
            }
        }
        return mapOf("code" to ok, "msg" to null as String?, "units" to units.size, "rows" to rows)
    }

    /** 工伤月情况(TJ0F/F1): TC10(ATC105='2') UNION; TC18=202201 专项覆写; 硬编码个人归零 hack 忠实。
     *  明细 34 列 31 参(atj000='1'/aae036=now 字面量); 主表 32 列 29 参(atj000/aaezzz/aae036 字面量)。 */
    @Transactional
    fun gsYue(aae002: String, aaa020: String, aaa030: String, aae011: String): Map<String, Any?> {
        val ym = aae002.toLongOrNull() ?: throw YueErr2("年月格式不正确|")
        jdbc.query("SELECT id FROM aa12 WHERE id=?", { rs, _ -> rs.getString("id") }, aaa020)
            .firstOrNull() ?: throw YueErr2("参保地区划信息不正确")
        val aa03 = jdbc.query("SELECT aaa030, aaa031 FROM aa03 WHERE aaa030=?",
            { rs, _ -> arrayOf(rs.getObject("aaa030"), rs.getString("aaa031")) }, aaa030.toLong())
            .firstOrNull() ?: throw YueErr2("险金户头信息不正确")
        val user = jdbc.query(
            "SELECT aae011, aae017, aae019, aae020, aae022 FROM sys_org_user_unit WHERE aae011=?",
            { rs, _ -> arrayOf(rs.getString("aae011"), rs.getString("aae017"), rs.getString("aae019"),
                rs.getString("aae020"), rs.getString("aae022")) }, aae011).firstOrNull()
            ?: throw YueErr2("经办人信息不正确")
        val g = jdbc.queryForObject(
            """SELECT count(*) FROM tb91 a, tc90 b, ab01 c
               WHERE a.atb91z=b.atc900 AND b.aab001=c.aab001 AND a.atb911='23' AND a.atb912='2'
                 AND b.aae00p IN ('1','5') AND c.aaa030=? AND b.aae003=?""",
            Int::class.java, aaa030.toLong(), ym)
        if (g > 0) throw YueErr2("不符合条件，原因是存在参保人员合同减员信息未审核")
        if (jdbc.queryForObject(
                "SELECT count(*) FROM tj0f WHERE aaezzz='1' AND aaa020=? AND aaa030=? AND aae002=?",
                Int::class.java, aaa020, aaa030.toLong(), ym) > 0)
            return mapOf("code" to ok, "msg" to null as String?, "skipped" to "已确认月份不重建", "rows" to 0)
        jdbc.update("DELETE FROM tj0f WHERE aaa020=? AND aaa030=? AND aae002=?", aaa020, aaa030.toLong(), ym)
        jdbc.update("DELETE FROM tj0f1 WHERE aaa020=? AND aaa030=? AND aae002=?", aaa020, aaa030.toLong(), ym)
        val loc99 = aaa020.length >= 4 && aaa020.substring(2, 4) != "99"
        val detailSql = """INSERT INTO tj0f1 (aae002, atc900, atj000, atb930, atb931, aab001, aab004,
            aaa030, aaa031, aaa020, aaa021, aaa001, aac003, aac002, aac009, aac008,
            aaa302, aaa305, aaa306, atb90l, atb90n, atb90o, atb90q, aaa399,
            baa002, bba002, atb90t, bba004, bba005, daa001, atj0f3, atj0f5, atj0aa, aac001)
            VALUES (?, ?, '1', ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?,
            ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)""" // 34 列 -1 字面量 = 33 参
        val headSql = """INSERT INTO tj0f (aae002, xh, atb930, atb931, aab001, aab004, aaa030, aaa031,
            aaa020, aaa021, atj000, atj0f2, atb90l, atb90n, atb90o, atb90q, aaa399,
            baa002, bba002, atb90t, bba004, bba005, daa001, atj0f3, atj0f5,
            aae011, aae017, aae036, aae019, aae020, aae022, aaezzz)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, '1', ?, ?, ?, ?, ?, ?, ?, ?,
            ?, ?, ?, ?, ?, ?, ?, ?, now(), ?, ?, ?, '0')""" // 32 列 -3 字面量 = 29 参
        var xh = 0
        var rows = 0
        val units = jdbc.query(
            """SELECT a.atb930, a.atb931, a.aab001, b.aab004 FROM tb93 a JOIN ab01 b ON a.aab001=b.aab001
               WHERE b.aaa030=? ORDER BY a.atb931""",
            { rs, _ -> arrayOf(rs.getObject("atb930"), rs.getString("atb931"),
                rs.getObject("aab001"), rs.getString("aab004")) }, aa03[0])
        for (qy in units) {
            var rs = 0
            var bba004 = Z
            var lLast: String? = null; var nLast: String? = null; var oLast: String? = null
            var qLast: String? = null; var g399Last: String? = null
            val emps = jdbc.queryForList(
                """SELECT * FROM tc90 WHERE aaa404='1' AND aaa130=?
                   AND ((atc90pb='0' AND aaa903<=?) OR (atc90pb='1' AND aaa904>?)) AND atb930=?
                   UNION
                   SELECT a.* FROM tc90 a, tc10 b WHERE a.atc900=b.atc900 AND b.atc105='2'
                     AND a.aaa130=? AND a.atb930=?""",
                aaa020, ym, ym, qy[0], aaa020, qy[0])
            for (yg in emps) {
                val atc900 = (yg["atc900"] as Number).toLong()
                val (code, result) = engine.calc(yg, ym.toString(), InsuranceCalcEngine.Tc01(jslb = "2"))
                if (code == -1) throw YueErr2("计算险金信息出错：$result")
                val c = result as InsuranceCalcEngine.Tc01
                var r305 = Z; var rB4 = Z
                if (loc99 && ym == 202201L) {
                    val tc18 = runCatching {
                        jdbc.query("SELECT aaa305, aaa000 FROM tc18 WHERE aae002=? AND aac002=?",
                            { rs, _ -> arrayOf(rs.getBigDecimal(1), rs.getBigDecimal(2)) },
                            ym, yg["aac002"]).firstOrNull()
                    }.getOrNull()
                    if (tc18 != null) { r305 = tc18[0] as BigDecimal? ?: Z; rB4 = tc18[1] as BigDecimal? ?: Z }
                }
                val base4 = if (r305 > Z) rB4 else (c.bba004 ?: Z)
                val b4 = if (yg["aac002"] == "220282199106213512") Z else base4 // 旧硬编码 hack 忠实
                val atj0aa = if ((yg["aaa908"] as Number?)?.toLong() == ym) "1" else "0"
                if (b4 > Z) rs++
                if (b4 > Z) {
                    try {
                        jdbc.update(detailSql, ym, atc900, qy[0], qy[1], qy[2], qy[3],
                            aa03[0], aa03[1], yg["aaa130"], yg["aaa131"], yg["aaa009"],
                            yg["aac003"], yg["aac002"], yg["aac009"], yg["aac008"],
                            c.aaa302, c.aaa305, c.aaa306,
                            pct(c.atb90l), pct(c.atb90n), pct(c.atb90o), pct(c.atb90q), pct(c.aaa399),
                            Z, Z, Z, b4, Z, Z, b4, b4, atj0aa,
                            (yg["aac001"] as Number?)?.toLong())
                    } catch (e: Exception) {
                        throw YueErr2("记录转款明细信息出错${e.message ?: ""}")
                    }
                    rows++
                }
                bba004 = bba004.add(b4)
                c.atb90l?.let { lLast = it.stripTrailingZeros().toPlainString() + "%" }
                c.atb90n?.let { nLast = it.stripTrailingZeros().toPlainString() + "%" }
                c.atb90o?.let { oLast = it.stripTrailingZeros().toPlainString() + "%" }
                c.atb90q?.let { qLast = it.stripTrailingZeros().toPlainString() + "%" }
                c.aaa399?.let { g399Last = it.stripTrailingZeros().toPlainString() + "%" }
            }
            if (bba004 > Z) {
                xh++
                try {
                    jdbc.update(headSql, ym, xh, qy[0], qy[1], qy[2], qy[3], aa03[0], aa03[1],
                        aaa020, jdbc.queryForObject("SELECT label FROM aa12 WHERE id=?",
                            String::class.java, aaa020),
                        rs, lLast, nLast, oLast, qLast, g399Last,
                        Z, Z, Z, bba004, Z, Z, bba004, bba004,
                        user[0], user[1], user[2], user[3], user[4])
                } catch (e: Exception) {
                    throw YueErr2("记录转款汇总信息出错${e.message ?: ""} <- ${e.cause?.message ?: ""} <- ${e.cause?.cause?.message ?: ""}")
                }
            }
        }
        return mapOf("code" to ok, "msg" to null as String?, "units" to units.size, "rows" to rows)
    }

    private fun pct(v: BigDecimal?): String? = v?.stripTrailingZeros()?.toPlainString() + "%"
}

@Component
class Yueqk2ShengGjjProc(private val c: Yueqk2Cases) : ProcHandler {
    override fun procName() = "PKG_派遣保险月情况.PRC_省公积金月情况"
    override fun handle(params: Map<String, Any?>): Map<String, Any?> {
        for (k in listOf("aae002", "aaa020", "aaa030", "aae011"))
            if (params[k] == null) return mapOf("appCode" to -1, "errMsg" to "缺少 $k")
        val r = try {
            c.gjjYue(params["aae002"]!!.toString(), params["aaa020"]!!.toString(),
                params["aaa030"]!!.toString(), params["aae011"]!!.toString(), city = false)
        } catch (e: YueErr2) {
            mapOf("code" to -1, "msg" to e.m, "units" to 0, "rows" to 0, "skipped" to null)
        }
        return mapOf("appCode" to r["code"], "errMsg" to r["msg"],
            "units" to r["units"], "rows" to r["rows"], "skipped" to r["skipped"])
    }
}

@Component
class Yueqk2ShiGjjProc(private val c: Yueqk2Cases) : ProcHandler {
    override fun procName() = "PKG_派遣保险月情况.PRC_市公积金月情况"
    override fun handle(params: Map<String, Any?>): Map<String, Any?> {
        for (k in listOf("aae002", "aaa020", "aaa030", "aae011"))
            if (params[k] == null) return mapOf("appCode" to -1, "errMsg" to "缺少 $k")
        val r = try {
            c.gjjYue(params["aae002"]!!.toString(), params["aaa020"]!!.toString(),
                params["aaa030"]!!.toString(), params["aae011"]!!.toString(), city = true)
        } catch (e: YueErr2) {
            mapOf("code" to -1, "msg" to e.m, "units" to 0, "rows" to 0, "skipped" to null)
        }
        return mapOf("appCode" to r["code"], "errMsg" to r["msg"],
            "units" to r["units"], "rows" to r["rows"], "skipped" to r["skipped"])
    }
}

@Component
class Yueqk2GsProc(private val c: Yueqk2Cases) : ProcHandler {
    override fun procName() = "PKG_派遣保险月情况.PRC_工伤月情况"
    override fun handle(params: Map<String, Any?>): Map<String, Any?> {
        for (k in listOf("aae002", "aaa020", "aaa030", "aae011"))
            if (params[k] == null) return mapOf("appCode" to -1, "errMsg" to "缺少 $k")
        val r = try {
            c.gsYue(params["aae002"]!!.toString(), params["aaa020"]!!.toString(),
                params["aaa030"]!!.toString(), params["aae011"]!!.toString())
        } catch (e: YueErr2) {
            mapOf("code" to -1, "msg" to e.m, "units" to 0, "rows" to 0, "skipped" to null)
        }
        return mapOf("appCode" to r["code"], "errMsg" to r["msg"],
            "units" to r["units"], "rows" to r["rows"], "skipped" to r["skipped"])
    }
}
