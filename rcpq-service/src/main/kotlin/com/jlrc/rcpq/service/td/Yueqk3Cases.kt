package com.jlrc.rcpq.service.td

import com.jlrc.rcpq.service.calc.InsuranceCalcEngine
import com.jlrc.rcpq.service.proc.ProcHandler
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal

/**
 * W4c-1 月情况·医保双线（旧 PKG_派遣保险月情况, 2026-11-17）。
 * 独立新文件(W4b 教训: 占位符簿记禁止行级手术)。
 *
 * 源: PACKAGE_BODY_PKG_派遣保险月情况.sql
 *   - PRC_省医保月情况/转款情况 L314-635（TJ0B/B1; 入口无减员守卫; TC10 UNION;
 *     TJ0B2 基数补差联算(aac001 numeric 簇转换); TC9W_BP01 大病 ABO003;
 *     AAE001=下月; 硬编码清零 hack 220105198711250028 忠实）
 *   - PRC_市医保月情况/转款情况 L1126-1551（TJ0C/C1; 入口有减员守卫; 无 UNION;
 *     单元排除 37664(硬编码忠实); 过滤排除退休(aac008 含 80);
 *     比例 ATB90L 剥 '*' 前缀; 大病=TC9W_BP04 sum(abp007)(姓名+证件匹配);
 *     ATJ0C5>=0 全插(与省 >0 不同); hack 220822199710080425+202509→100 忠实）
 */
class YueErr3(val m: String) : RuntimeException(m)

@Component
class Yueqk3Cases(private val jdbc: JdbcTemplate, private val engine: InsuranceCalcEngine) {
    private val ok = 1
    private val Z = BigDecimal.ZERO

    private fun pct(v: BigDecimal?): String? = v?.stripTrailingZeros()?.toPlainString() + "%"
    private fun nm(v: Any?): Long? = (v as Number?)?.toLong()

    /** 医保月情况(省=TJ0B/B1, 市=TJ0C/C1)。
     *  detail 省线 32 参(32 列 - atj000), head 省线 30 参(33 列 - atj000/aae036/aaezzz);
     *  detail 市线 29 参(30 列 - atj000), head 市线 28 参(31 列 - 3 字面量)。 */
    @Transactional
    fun ybYue(aae002: String, aaa020: String, aaa030: String, aae011: String, sheng: Boolean): Map<String, Any?> {
        val ym = aae002.toLongOrNull() ?: throw YueErr3("年月格式不正确|")
        val main = if (sheng) "tj0b" else "tj0c"
        val detail = if (sheng) "tj0b1" else "tj0c1"
        jdbc.query("SELECT id, label FROM aa12 WHERE id=?", { rs, _ -> rs.getString("id") }, aaa020)
            .firstOrNull() ?: throw YueErr3("参保地区划信息不正确")
        val aa03 = jdbc.query("SELECT aaa030, aaa031 FROM aa03 WHERE aaa030=?",
            { rs, _ -> arrayOf(rs.getObject("aaa030"), rs.getString("aaa031")) }, aaa030.toLong())
            .firstOrNull() ?: throw YueErr3("险金户头信息不正确")
        val user = jdbc.query(
            "SELECT aae011, aae017, aae019, aae020, aae022 FROM sys_org_user_unit WHERE aae011=?",
            { rs, _ -> arrayOf(rs.getString("aae011"), rs.getString("aae017"), rs.getString("aae019"),
                rs.getString("aae020"), rs.getString("aae022")) }, aae011).firstOrNull()
            ?: throw YueErr3("经办人信息不正确")
        if (!sheng) { // 忠实: 市医保入口有减员守卫, 省医保入口没有
            val g = jdbc.queryForObject(
                """SELECT count(*) FROM tb91 a, tc90 b, ab01 c
                   WHERE a.atb91z=b.atc900 AND b.aab001=c.aab001 AND a.atb911='23' AND a.atb912='2'
                     AND b.aae00p IN ('1','5') AND c.aaa030=? AND b.aae003=?""",
                Int::class.java, aaa030.toLong(), ym)
            if (g > 0) throw YueErr3("不符合条件，原因是存在参保人员合同减员信息未审核")
        }
        if (jdbc.queryForObject(
                "SELECT count(*) FROM $main WHERE aaezzz='1' AND aaa020=? AND aaa030=? AND aae002=?",
                Int::class.java, aaa020, aaa030.toLong(), ym) > 0)
            return mapOf("code" to ok, "msg" to null as String?, "skipped" to "已确认月份不重建", "rows" to 0)
        jdbc.update("DELETE FROM $main WHERE aaa020=? AND aaa030=? AND aae002=?", aaa020, aaa030.toLong(), ym)
        jdbc.update("DELETE FROM $detail WHERE aaa020=? AND aaa030=? AND aae002=?", aaa020, aaa030.toLong(), ym)
        val nextYm = java.time.YearMonth.parse(aae002, java.time.format.DateTimeFormatter.ofPattern("yyyyMM"))
            .plusMonths(1).format(java.time.format.DateTimeFormatter.ofPattern("yyyyMM"))
        // 单元循环: 市医保排除硬编码单元 37664(旧源码忠实)
        val units = jdbc.query(
            """SELECT a.atb930, a.atb931, a.aab001, b.aab004 FROM tb93 a JOIN ab01 b ON a.aab001=b.aab001
               WHERE b.aaa030=? ${if (!sheng) "AND a.atb930<>37664" else ""} ORDER BY a.atb931""",
            { rs, _ -> arrayOf(rs.getObject("atb930"), rs.getString("atb931"),
                rs.getObject("aab001"), rs.getString("aab004")) }, aa03[0])
        var xh = 0
        var rows = 0
        for (qy in units) {
            var rs = 0
            var baa002 = Z; var bba002 = Z; var atb90t = Z; var bba004 = Z; var bba005 = Z; var daa001 = Z
            var lLast: String? = null; var nLast: String? = null; var oLast: String? = null
            var qLast: String? = null; var g399Last: String? = null
            val emps = if (sheng) jdbc.queryForList(
                """SELECT * FROM tc90 WHERE aaa402='1' AND aaa110=?
                   AND ((atc90pa='0' AND aaa603<=?) OR (atc90pa='1' AND aaa604>?)) AND atb930=?
                   UNION
                   SELECT a.* FROM tc90 a, tc10 b WHERE a.atc900=b.atc900 AND b.atc105='2'
                     AND a.aaa110=? AND a.atb930=?""",
                aaa020, ym, ym, qy[0], aaa020, qy[0])
            else jdbc.queryForList(
                """SELECT * FROM tc90 WHERE aaa402='1' AND aaa110=?
                   AND ((atc90pa='0' AND aaa603<=?) OR (atc90pa='1' AND aaa604>?))
                   AND (strpos(coalesce(aac008,''), '80')=0) AND atb930=?""",
                aaa020, ym, ym, qy[0])
            for (yg in emps) {
                val atc900 = (yg["atc900"] as Number).toLong()
                val (code, result) = engine.calc(yg, ym.toString(), InsuranceCalcEngine.Tc01(jslb = "2"))
                if (code == -1) throw YueErr3("计算险金信息出错：$result")
                val c = result as InsuranceCalcEngine.Tc01
                val aac002 = yg["aac002"]?.toString()
                var baa002R = c.baa002 ?: Z
                var bba002R = (c.bba002 ?: Z).add(c.bba005 ?: Z) // 忠实: 省线医疗单位缴含公务员补助比例额
                if (!sheng) {
                    bba002R = c.bba002 ?: Z
                    // 灵活就业(51): 生育/医疗单位缴归零(忠实)
                    if (((yg["aac008"] as String?) ?: "").contains("51")) {
                        bba002R = Z
                    }
                }
                // 忠实: 硬编码清零 hack(省线一人)
                if (sheng && aac002 == "220105198711250028") { baa002R = Z; bba002R = Z }
                var atb90tR = c.atb90t ?: Z
                var bba004R = Z // 两线均置 0(忠实)
                var bba005R = if (!sheng && ((yg["aac008"] as String?) ?: "").contains("51")) Z else (c.bba005 ?: Z)
                var daa001R = if (sheng) (c.daa001 ?: Z).add(c.daa002 ?: Z) else (c.daa001 ?: Z)
                // 省线大病: AAA608=ym → TC9W_BP01 ABO003>0 取值; AAA603<>ym → 0
                if (sheng) {
                    if ((yg["aaa608"] as Number?)?.toLong() == ym) {
                        daa001R = jdbc.query(
                            """SELECT abo003::numeric FROM tc9w_bp01 WHERE abo003::numeric>0
                               AND aae002=? AND atc900=? LIMIT 1""",
                            { rs, _ -> rs.getBigDecimal(1) }, ym, atc900).firstOrNull() ?: Z
                    }
                    if ((yg["aaa603"] as Number?)?.toLong() != ym) daa001R = Z
                } else {
                    // 市线大病: TC9W_BP04 sum(abp007)(rownum=1, 姓名+证件匹配; SUM 恒返行=NVL 0)
                    daa001R = jdbc.queryForObject(
                        """SELECT coalesce(sum(to_number(abp007)),0) FROM (SELECT abp007 FROM tc9w_bp04
                           WHERE aaa030=? AND aae002=? AND aac003=? AND aac002=? LIMIT 1) t""",
                        BigDecimal::class.java, aaa030.toLong(), ym, yg["aac003"], yg["aac002"])
                    // 忠实: 硬编码 hack 220822199710080425+202509 → DAA001=100
                    if (aac002 == "220822199710080425" && ym == 202509L) daa001R = BigDecimal(100)
                }
                // 省线 TJ0B2 基数补差联算(tj0b2.aac001 numeric — tc90.aac001 转换)
                if (sheng) {
                    val aac001N = (yg["aac001"] as Number?)?.toLong()
                    val jsbc = jdbc.queryForList(
                        "SELECT * FROM tj0b2 WHERE aae002=? AND aac001=?", ym, aac001N)
                    for (j in jsbc) {
                        fun bd(k: String) = j[k] as BigDecimal?
                        baa002R = baa002R.add(bd("baa002") ?: Z)
                        bba002R = bba002R.add(bd("bba002") ?: Z).add(bd("bba005") ?: Z)
                        atb90tR = atb90tR.add(bd("atb90t") ?: Z)
                        bba004R = bba004R.add(bd("bba004") ?: Z)
                    }
                    bba005R = Z // 忠实: 联算循环内生育归零
                }
                val atj0b3c = bba002R.add(atb90tR).add(bba005R) // 省线口径(市线为 bba002+bba004+bba005)
                val atj0c3c = bba002R.add(bba004R).add(bba005R) // 市线口径
                val a5 = if (sheng) baa002R.add(bba002R).add(atb90tR).add(bba005R).add(daa001R)
                else baa002R.add(bba002R).add(bba004R).add(bba005R).add(daa001R)
                val atj0aa = if ((yg["aaa608"] as Number?)?.toLong() == ym) "1" else "0"
                // 省线 >0 才插; 市线 >=0 全插(忠实差异)
                val doInsert = if (sheng) a5 > Z else a5 >= Z
                if (doInsert) rs++
                if (doInsert) {
                    try {
                        if (sheng) jdbc.update(
                            """INSERT INTO tj0b1 (aae002, atc900, atj000, atb930, atb931, aab001, aab004,
                               aaa030, aaa031, aaa020, aaa021, aaa001, aac003, aac002, aac009, aac008,
                               aaa302, aaa305, aaa306, atb90l, atb90n, atb90o, atb90q, aaa399,
                               baa002, bba002, atb90t, bba004, bba005, daa001, atj0b3, atj0b5, atj0aa, aac001)
                               VALUES (?, ?, '1', ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?,
                               ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)""",
                            ym, atc900, qy[0], qy[1], qy[2], qy[3], aa03[0], aa03[1],
                            yg["aaa110"], yg["aaa111"], yg["aaa005"], yg["aac003"], yg["aac002"],
                            yg["aac009"], yg["aac008"],
                            c.aaa302, c.aaa305, c.aaa306,
                            pct(c.atb90l), pct(c.atb90n), pct(c.atb90o), pct(c.atb90q), pct(c.aaa399),
                            baa002R, bba002R, atb90tR, bba004R, bba005R, daa001R, atj0b3c, a5, atj0aa,
                            nm(yg["aac001"]))
                        else jdbc.update(
                            """INSERT INTO tj0c1 (aae002, atc900, atj000, atb930, atb931, aab001, aab004,
                               aaa030, aaa031, aaa020, aaa021, aaa001, aac003, aac002, aac009, aac008,
                               aaa302, aaa305, aaa306, atb90l, atb90n, atb90o, atb90q,
                               baa002, bba002, bba004, bba005, daa001, atj0c3, atj0c5, atj0aa, aac001)
                               VALUES (?, ?, '1', ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?,
                               ?, ?, ?, ?, ?, ?, ?, ?, ?)""",
                            ym, atc900, qy[0], qy[1], qy[2], qy[3], aa03[0], aa03[1],
                            yg["aaa110"], yg["aaa111"], yg["aaa005"], yg["aac003"], yg["aac002"],
                            yg["aac009"], yg["aac008"],
                            c.aaa302, c.aaa305, c.aaa306,
                            (c.atb90l?.toString() ?: "").replace("*", "") + "%",
                            pct(c.atb90n), pct(c.atb90o), pct(c.atb90q),
                            baa002R, bba002R, bba004R, bba005R, daa001R, atj0c3c, a5, atj0aa,
                            nm(yg["aac001"]))
                    } catch (e: Exception) {
                        throw YueErr3("记录转款明细信息出错${e.message ?: ""}")
                    }
                    rows++
                }
                baa002 = baa002.add(baa002R); bba002 = bba002.add(bba002R)
                atb90t = atb90t.add(atb90tR); bba004 = bba004.add(bba004R)
                bba005 = bba005.add(bba005R); daa001 = daa001.add(daa001R)
                lLast = pct(c.atb90l); nLast = pct(c.atb90n); oLast = pct(c.atb90o)
                qLast = pct(c.atb90q); g399Last = pct(c.aaa399)
            }
            val atj03 = if (sheng) bba002.add(atb90t).add(bba005) else bba002.add(bba004).add(bba005)
            val a5 = baa002.add(bba002).add(atb90t).add(bba005).add(daa001)
            if (a5 > Z) {
                xh++
                try {
                    if (sheng) jdbc.update(
                        """INSERT INTO tj0b (aae002, xh, atb930, atb931, aab001, aab004, aaa030, aaa031,
                           aaa020, aaa021, atj000, atj0b2, atb90l, atb90n, atb90o, atb90q, aaa399,
                           baa002, bba002, atb90t, bba004, bba005, atj0b3, daa001, atj0b5, aae001,
                           aae011, aae017, aae036, aae019, aae020, aae022, aaezzz)
                           VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, '1', ?, ?, ?, ?, ?, ?,
                           ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, now(), ?, ?, ?, '0')""",
                        ym, xh, qy[0], qy[1], qy[2], qy[3], aa03[0], aa03[1],
                        aaa020, jdbc.queryForObject("SELECT label FROM aa12 WHERE id=?",
                            String::class.java, aaa020),
                        rs, lLast, nLast, oLast, qLast, g399Last,
                        baa002, bba002, atb90t, bba004, bba005, atj03, daa001, a5,
                        nextYm.toLongOrNull(),
                        user[0], user[1], user[2], user[3], user[4])
                    else jdbc.update(
                        """INSERT INTO tj0c (aae002, xh, atb930, atb931, aab001, aab004, aaa030, aaa031,
                           aaa020, aaa021, atj000, atj0c2, atb90l, atb90n, atb90o, atb90q,
                           baa002, bba002, bba004, bba005, atj0c3, daa001, atj0c5, aae001,
                           aae011, aae017, aae036, aae019, aae020, aae022, aaezzz)
                           VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, '1', ?, ?, ?, ?, ?,
                           ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, now(), ?, ?, ?, '0')""",
                        ym, xh, qy[0], qy[1], qy[2], qy[3], aa03[0], aa03[1],
                        aaa020, jdbc.queryForObject("SELECT label FROM aa12 WHERE id=?",
                            String::class.java, aaa020),
                        rs, lLast, nLast, oLast, qLast,
                        baa002, bba002, bba004, bba005, atj03, daa001, a5,
                        nextYm.toLongOrNull(),
                        user[0], user[1], user[2], user[3], user[4])
                } catch (e: Exception) {
                    throw YueErr3("记录转款汇总信息出错${e.message ?: ""} <- ${e.cause?.message ?: ""}")
                }
            }
        }
        return mapOf("code" to ok, "msg" to null as String?, "units" to units.size, "rows" to rows)
    }
}

@Component
class Yueqk3ShengProc(private val c: Yueqk3Cases) : ProcHandler {
    override fun procName() = "PKG_派遣保险月情况.PRC_省医保月情况"
    override fun handle(params: Map<String, Any?>): Map<String, Any?> {
        for (k in listOf("aae002", "aaa020", "aaa030", "aae011"))
            if (params[k] == null) return mapOf("appCode" to -1, "errMsg" to "缺少 $k")
        val r = try {
            c.ybYue(params["aae002"]!!.toString(), params["aaa020"]!!.toString(),
                params["aaa030"]!!.toString(), params["aae011"]!!.toString(), sheng = true)
        } catch (e: YueErr3) {
            mapOf("code" to -1, "msg" to e.m, "units" to 0, "rows" to 0, "skipped" to null)
        }
        return mapOf("appCode" to r["code"], "errMsg" to r["msg"],
            "units" to r["units"], "rows" to r["rows"], "skipped" to r["skipped"])
    }
}

@Component
class Yueqk3ShiProc(private val c: Yueqk3Cases) : ProcHandler {
    override fun procName() = "PKG_派遣保险月情况.PRC_市医保月情况"
    override fun handle(params: Map<String, Any?>): Map<String, Any?> {
        for (k in listOf("aae002", "aaa020", "aaa030", "aae011"))
            if (params[k] == null) return mapOf("appCode" to -1, "errMsg" to "缺少 $k")
        val r = try {
            c.ybYue(params["aae002"]!!.toString(), params["aaa020"]!!.toString(),
                params["aaa030"]!!.toString(), params["aae011"]!!.toString(), sheng = false)
        } catch (e: YueErr3) {
            mapOf("code" to -1, "msg" to e.m, "units" to 0, "rows" to 0, "skipped" to null)
        }
        return mapOf("appCode" to r["code"], "errMsg" to r["msg"],
            "units" to r["units"], "rows" to r["rows"], "skipped" to r["skipped"])
    }
}
