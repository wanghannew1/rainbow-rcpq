package com.jlrc.rcpq.service.td

import com.jlrc.rcpq.service.calc.InsuranceCalcEngine
import com.jlrc.rcpq.service.proc.ProcHandler
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal

/**
 * W4d-2② 补缴情况×4（旧 PKG_派遣保险月情况, 2026-11-17）。
 * 独立新文件(W4b 教训)。候选=已参保且当月有回盘成果(AAA603/903/503<ym AND AAA608/908/508=ym),
 * ATJ000='2', 逐人调 BbfCalc(LB='1') 取补缴明细; 旧单位循环内 COMMIT→单事务(结构性修正)。
 *
 * 源: PACKAGE_BODY_PKG_派遣保险月情况.sql
 *   - PRC_省医保补缴情况 L636-810（TJ0B/B1; TC9W_BP01 ABO004→ATJ0B6/ABO003→DAA001;
 *     hack 220105199206272626→医疗清零; head 含 ATJ0B6 滞纳金）
 *   - PRC_工伤补缴情况   L3047-3211（TJ0F/F1; TC9W_BP09 同款 ATJ0F6/DAA001;
 *     hack 22010319930822063X→BBA004=16.29; ATJ0F3/F5=BBA004; 34560/34889 no-op 忠实）
 *   - PRC_市公积金补缴情况 L2469-2623（TJ0E/E1; AAA501<ym+AAA508=ym+NOT EXISTS TC0A;
 *     hack ym=202411×4 人固定基数/缴存额 忠实）
 *   - PRC_省公积金补缴情况 L1941-2107（TJ0D/D1; 活码=小写重写版: TC9U_BP01 联查,
 *     CAA001/002=回盘桶 abp005/006, 保险当月补缴调用已被注释→不调(忠实), 无大病/滞纳金）
 */
@Component
class Yueqk5Cases(
    private val jdbc: JdbcTemplate,
    private val engine: InsuranceCalcEngine,
    private val bbf: BbfCases,
) {
    private val ok = 1
    private val Z = BigDecimal.ZERO

    private fun pct(v: BigDecimal?): String? = v?.stripTrailingZeros()?.toPlainString() + "%"
    private fun bd(v: Any?): BigDecimal = (v as Number?)?.let { BigDecimal(it.toString()) } ?: Z
    private fun nm(v: Any?): Long? = (v as Number?)?.toLong()

    private fun lookupBp(table: String, ym: Long, atc900: Long): Pair<BigDecimal?, BigDecimal?>? =
        jdbc.query(
            """SELECT abo004::numeric, abo003::numeric FROM $table
               WHERE aae002=? AND atc900=? LIMIT 2""",
            { rs, _ -> Pair(rs.getBigDecimal(1), rs.getBigDecimal(2)) }, ym, atc900)
            .takeIf { it.size == 1 }?.firstOrNull()

    private fun entry(aae002: String, aaa020: String, aaa030: String, aae011: String): Triple<Long, Array<Any?>, Array<String?>> {
        val ym = aae002.toLongOrNull() ?: throw YueErr5("年月格式不正确|")
        jdbc.query("SELECT id FROM aa12 WHERE id=?", { rs, _ -> rs.getString("id") }, aaa020)
            .firstOrNull() ?: throw YueErr5("参保地区划信息不正确")
        val aa03 = jdbc.query("SELECT aaa030, aaa031 FROM aa03 WHERE aaa030=?",
            { rs, _ -> arrayOf(rs.getObject("aaa030"), rs.getString("aaa031")) }, aaa030.toLong())
            .firstOrNull() ?: throw YueErr5("险金户头信息不正确")
        val user = jdbc.query(
            "SELECT aae011, aae017, aae019, aae020, aae022 FROM sys_org_user_unit WHERE aae011=?",
            { rs, _ -> arrayOf(rs.getString("aae011"), rs.getString("aae017"), rs.getString("aae019"),
                rs.getString("aae020"), rs.getString("aae022")) }, aae011).firstOrNull()
            ?: throw YueErr5("经办人信息不正确")
        return Triple(ym, aa03, user)
    }

    /** 省医保补缴情况(TJ0B/B1) */
    @Transactional
    fun sbBujiao(aae002: String, aaa020: String, aaa030: String, aae011: String): Map<String, Any?> {
        val (ym, aa03, user) = entry(aae002, aaa020, aaa030, aae011)
        val filter = """aaa402='1' AND atc90pa IN ('0','1') AND aaa110=?
                       AND aaa030=? AND aaa603<? AND aaa608=?"""
        val detailSql = """INSERT INTO tj0b1 (aae002, atc900, atj000, atb930, atb931, aab001, aab004,
            aaa030, aaa031, aaa020, aaa021, aaa001, aac001, aac003, aac002, aac009,
            aaa302, aaa305, aaa306, atb90l, atb90n, atb90o, atb90q, aaa399,
            baa002, bba002, atb90t, bba004, bba005, daa001, atj0b3, atj0b5, atj0b6, aac001)
            VALUES (?, ?, '2', ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?,
            ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)""" // 35 参
        val headSql = """INSERT INTO tj0b (aae002, xh, atb930, atb931, aab001, aab004, aaa030, aaa031,
            aaa020, aaa021, atj000, atj0b2, atb90l, atb90n, atb90o, atb90q, aaa399,
            baa002, bba002, atb90t, bba004, bba005, atj0b3, daa001, atj0b5, atj0b6,
            aae011, aae017, aae036, aae019, aae020, aae022, aaezzz)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, '2', ?, ?, ?, ?, ?, ?,
            ?, ?, ?, ?, ?, ?, ?, ?, ?, now(), ?, ?, ?, ?, '0')""" // 30 参
        var xh = 0
        var rows = 0
        val units = jdbc.query(
            """SELECT DISTINCT a.atb930, b.atb931, c.aab001, c.aab004
               FROM tc90 a, tb93 b, ab01 c
               WHERE a.atb930=b.atb930 AND b.aab001=c.aab001
                 AND $filter ORDER BY b.atb931""",
            { rs, _ -> arrayOf(rs.getObject("atb930"), rs.getString("atb931"),
                rs.getObject("aab001"), rs.getString("aab004")) },
            aaa020, aaa030.toLong(), ym, ym)
        for (qy in units) {
            var rs = 0
            var baa002 = Z; var bba002 = Z; var atb90t = Z; var bba004 = Z; var bba005 = Z
            var daa001 = Z; var atj0b6 = Z
            var lLast: String? = null; var nLast: String? = null; var oLast: String? = null
            var qLast: String? = null; var g399Last: String? = null
            val emps = jdbc.queryForList(
                """SELECT a.* FROM tc90 a, tb93 b, ab01 c
                   WHERE a.atb930=b.atb930 AND b.aab001=c.aab001
                     AND $filter AND a.atb930=?""",
                aaa020, aaa030.toLong(), ym, ym, qy[0])
            for (yg in emps) {
                val atc900 = nm(yg["atc900"])!!
                val bfr = try {
                    bbf.bf(atc900, ym.toString(), "1")
                } catch (e: BbfErr) {
                    throw YueErr5(e.m)
                }
                @Suppress("UNCHECKED_CAST")
                val c = bfr["bf"] as Map<String, Any?>
                fun bd5(k: String): BigDecimal = c[k] as BigDecimal
                var baa002R = bd5("baa002")
                var bba002R = bd5("bba002")
                var bba004R = bd5("bba004")
                // 忠实: 硬编码清零 hack(数据域限定)
                if (yg["aac002"]?.toString() == "220105199206272626") { baa002R = Z; bba002R = Z }
                val atb90tR = bd5("atb90t")
                val bba005R = bd5("bba005")
                val (atj0b6R, daa001R) = lookupBp("tc9w_bp01", ym, atc900)
                    ?: Pair(null, null)
                val atj0b3 = bba002R.add(atb90tR).add(bba005R)
                val atj0b5 = baa002R.add(bba002R).add(atb90tR).add(bba005R)
                    .add(daa001R ?: Z).add(atj0b6R ?: Z)
                if (atj0b5 > Z) {
                    rs++
                    try {
                        jdbc.update(detailSql, ym, atc900, qy[0], qy[1], qy[2], qy[3],
                            aa03[0], aa03[1], yg["aaa110"], yg["aaa111"], yg["aaa005"],
                            nm(yg["aac001"]), yg["aac003"], yg["aac002"], yg["aac009"],
                            bd5("aaa302"), bd5("aaa305"), bd5("aaa306"),
                            pct(bd5("atb90l") ?: null), pct(bd5("atb90n")), pct(bd5("atb90o")),
                            pct(bd5("atb90q")), pct(bd5("aaa399")),
                            baa002R, bba002R, atb90tR, bba004R, bba005R, daa001R, atj0b3, atj0b5,
                            atj0b6R, nm(yg["aac001"]))
                    } catch (e: Exception) {
                        throw YueErr5("记录转款明细信息出错${e.message ?: ""}")
                    }
                    rows++
                }
                baa002 = baa002.add(baa002R); bba002 = bba002.add(bba002R)
                atb90t = atb90t.add(atb90tR); bba004 = bba004.add(bba004R)
                bba005 = bba005.add(bba005R); daa001 = daa001.add(daa001R ?: Z)
                atj0b6 = atj0b6.add(atj0b6R ?: Z)
                bd5("atb90l"); lLast = pct(bd5("atb90l")); nLast = pct(bd5("atb90n"))
                oLast = pct(bd5("atb90o")); qLast = pct(bd5("atb90q")); g399Last = pct(bd5("aaa399"))
            }
            val atj0b3 = bba002.add(atb90t).add(bba005)
            val a5 = baa002.add(bba002).add(atb90t).add(bba005).add(daa001).add(atj0b6)
            if (a5 > Z) {
                xh++
                try {
                    jdbc.update(headSql, ym, xh, qy[0], qy[1], qy[2], qy[3], aa03[0], aa03[1],
                        aaa020, jdbc.queryForObject("SELECT label FROM aa12 WHERE id=?",
                            String::class.java, aaa020),
                        rs, lLast, nLast, oLast, qLast, g399Last,
                        baa002, bba002, atb90t, bba004, bba005, atj0b3, daa001, a5, atj0b6,
                        user[0], user[1], user[2], user[3], user[4])
                } catch (e: Exception) {
                    throw YueErr5("记录补收汇总信息出错${e.message ?: ""}")
                }
            }
        }
        return mapOf("code" to ok, "msg" to null as String?, "units" to units.size, "rows" to rows)
    }

    /** 市公积金补缴情况(TJ0E/E1) */
    @Transactional
    fun shiGjjBujiao(aae002: String, aaa020: String, aaa030: String, aae011: String): Map<String, Any?> {
        val (ym, aa03, user) = entry(aae002, aaa020, aaa030, aae011)
        val detailSql = """INSERT INTO tj0e1 (aae002, atc900, atj000, atb930, atb931, aab001, aab004,
            aaa030, aaa031, aaa020, aaa021, aaa001, aac001, aac003, aac002, aac009,
            aaa303, atb90s_dw, atb90s_gr, caa001, caa002, atj0e5)
            VALUES (?, ?, '2', ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)""" // 22 参
        val headSql = """INSERT INTO tj0e (aae002, xh, atb930, atb931, aab001, aab004, aaa030,
            aaa031, aaa020, aaa021, atj000, atj0e2, atb90s_dw, atb90s_gr, caa001, caa002,
            atj0e5, aae011, aae017, aae036, aae019, aae020, aae022, aaezzz)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, '2', ?, ?, ?, ?, ?, ?,
            ?, ?, now(), ?, ?, ?, '0')""" // 21 参
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
            val emps = jdbc.queryForList(
                """SELECT a.* FROM tc90 a, tb93 b, ab01 c
                   WHERE a.atb930=b.atb930 AND b.aab001=c.aab001 AND c.aaa030=?
                     AND a.aaa120=? AND a.aaa407='1' AND atc90w IN ('0','1')
                     AND aaa501<? AND aaa508=? AND a.atb930=?
                     AND NOT EXISTS (SELECT 1 FROM tc0a WHERE atc900=a.atc900 AND aae002=?)""",
                aaa030.toLong(), aaa020, ym, ym, qy[0], ym)
            for (yg in emps) {
                val atc900 = nm(yg["atc900"])!!
                rs++ // 忠实: N_RS 无条件自增(插/不插都算)
                val bfr = try {
                    bbf.bf(atc900, ym.toString(), "1")
                } catch (e: BbfErr) {
                    throw YueErr5(e.m)
                }
                @Suppress("UNCHECKED_CAST")
                val c = bfr["bf"] as Map<String, Any?>
                var aaa303 = c["aaa303"] as BigDecimal
                var caa1 = c["caa001"] as BigDecimal
                var caa2 = c["caa002"] as BigDecimal
                // 忠实: hack ym=202411 × 4 人固定基数/缴存额(数据域限定)
                if (ym == 202411L) {
                    val fix = mapOf(
                        "220402199304063622" to listOf("1136", "568", "568"),
                        "220182198909160653" to listOf("760", "480", "280"),
                        "220822199709093421" to listOf("560", "280", "280"),
                        "220106199005318428" to listOf("760", "480", "280"))
                    val idc = yg["aac002"]?.toString()
                    fix[idc]?.let { f -> aaa303 = BigDecimal(f[0]); caa1 = BigDecimal(f[1]); caa2 = BigDecimal(f[2]) }
                }
                val a5 = caa1.add(caa2)
                if (a5 > Z) {
                    try {
                        jdbc.update(detailSql, ym, atc900, qy[0], qy[1], qy[2], qy[3],
                            aa03[0], aa03[1], yg["aaa120"], yg["aaa121"], yg["aaa004"],
                            nm(yg["aac001"]), yg["aac003"], yg["aac002"], yg["aac009"],
                            aaa303, pct(c["atb90s_dw"] as BigDecimal), pct(c["atb90s_gr"] as BigDecimal),
                            caa1, caa2, a5)
                    } catch (e: Exception) {
                        throw YueErr5("记录转款明细信息出错${e.message ?: ""}${atc900}")
                    }
                    rows++
                }
                caa001 = caa001.add(caa1); caa002 = caa002.add(caa2)
            }
            val a5 = caa001.add(caa002)
            if (a5 > Z) {
                xh++
                try {
                    jdbc.update(headSql, ym, xh, qy[0], qy[1], qy[2], qy[3], aa03[0], aa03[1],
                        aaa020, jdbc.queryForObject("SELECT label FROM aa12 WHERE id=?",
                            String::class.java, aaa020),
                        rs, caa001, caa002, a5,
                        user[0], user[1], user[2], user[3], user[4])
                } catch (e: Exception) {
                    throw YueErr5("记录转款汇总信息出错${e.message ?: ""}")
                }
            }
        }
        return mapOf("code" to ok, "msg" to null as String?, "units" to units.size, "rows" to rows)
    }

    /** 工伤补缴情况(TJ0F/F1) */
    @Transactional
    fun gsBujiao(aae002: String, aaa020: String, aaa030: String, aae011: String): Map<String, Any?> {
        val (ym, aa03, user) = entry(aae002, aaa020, aaa030, aae011)
        val filter = """aaa404='1' AND atc90pb IN ('0','1') AND aaa130=?
                       AND aaa030=? AND aaa903<? AND aaa908=?"""
        val detailSql = """INSERT INTO tj0f1 (aae002, atc900, atj000, atb930, atb931, aab001, aab004,
            aaa030, aaa031, aaa020, aaa021, aaa001, aac003, aac002, aac009, aac008,
            aaa302, aaa305, aaa306, atb90l, atb90n, atb90o, atb90q, aaa399,
            baa002, bba002, atb90t, bba004, bba005, daa001, atj0f3, atj0f5, atj0f6, aac001)
            VALUES (?, ?, '2', ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?,
            ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)""" // 34 参
        val headSql = """INSERT INTO tj0f (aae002, xh, atb930, atb931, aab001, aab004, aaa030, aaa031,
            aaa020, aaa021, atj000, atj0f2, atb90l, atb90n, atb90o, atb90q, aaa399,
            baa002, bba002, atb90t, bba004, bba005, atj0f3, daa001, atj0f5, atj0f6,
            aae011, aae017, aae036, aae019, aae020, aae022, aaezzz)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, '2', ?, ?, ?, ?, ?, ?,
            ?, ?, ?, ?, ?, ?, ?, ?, ?, now(), ?, ?, ?, ?, '0')""" // 30 参
        var xh = 0
        var rows = 0
        val units = jdbc.query(
            """SELECT DISTINCT a.atb930, b.atb931, c.aab001, c.aab004
               FROM tc90 a, tb93 b, ab01 c
               WHERE a.atb930=b.atb930 AND b.aab001=c.aab001
                 AND $filter ORDER BY b.atb931""",
            { rs, _ -> arrayOf(rs.getObject("atb930"), rs.getString("atb931"),
                rs.getObject("aab001"), rs.getString("aab004")) },
            aaa020, aaa030.toLong(), ym, ym)
        for (qy in units) {
            var rs = 0
            var bba004 = Z; var daa001 = Z; var atj0f6 = Z
            var lLast: String? = null; var nLast: String? = null; var oLast: String? = null
            var qLast: String? = null; var g399Last: String? = null
            val emps = jdbc.queryForList(
                """SELECT a.* FROM tc90 a, tb93 b, ab01 c
                   WHERE a.atb930=b.atb930 AND b.aab001=c.aab001
                     AND $filter AND a.atb930=?""",
                aaa020, aaa030.toLong(), ym, ym, qy[0])
            for (yg in emps) {
                val atc900 = nm(yg["atc900"])!!
                val bfr = try {
                    bbf.bf(atc900, ym.toString(), "1")
                } catch (e: BbfErr) {
                    throw YueErr5(e.m)
                }
                @Suppress("UNCHECKED_CAST")
                val c = bfr["bf"] as Map<String, Any?>
                fun bd5(k: String): BigDecimal = c[k] as BigDecimal
                var bba004R = bd5("bba004")
                // 忠实: TC9W_BP09 单行取 ABO004/ABO003, 多行/无行 → NULL(忠实 SELECT INTO 语义)
                val bp09 = jdbc.query(
                    """SELECT abo004::numeric, abo003::numeric FROM tc9w_bp09
                       WHERE aae002=? AND atc900=? LIMIT 2""",
                    { rs, _ -> Pair(rs.getBigDecimal(1), rs.getBigDecimal(2)) }, ym, atc900)
                val atj0f6R: BigDecimal? = bp09.singleOrNull()?.first
                val daa001R: BigDecimal? = bp09.singleOrNull()?.second
                // 忠实: 硬编码 hack(数据域限定)
                if (yg["aac002"]?.toString() == "22010319930822063X") bba004R = BigDecimal("16.29")
                if (atc900 == 34560L) { /* 忠实: 旧源码 no-op */ }
                if (atc900 == 34889L) { /* 忠实: 旧源码 no-op */ }
                val atj0f3 = bba004R
                val atj0f5 = bba004R
                if (atj0f5 > Z) {
                    try {
                        jdbc.update(detailSql, ym, atc900, qy[0], qy[1], qy[2], qy[3],
                            aa03[0], aa03[1], yg["aaa130"], yg["aaa131"], yg["aaa005"],
                            yg["aac003"], yg["aac002"], yg["aac009"], yg["aac008"],
                            bd5("aaa302"), bd5("aaa305"), bd5("aaa306"),
                            pct(bd5("atb90l")), pct(bd5("atb90n")), pct(bd5("atb90o")),
                            pct(bd5("atb90q")), pct(bd5("aaa399")),
                            Z, Z, Z, bba004R, Z, daa001R, atj0f3, atj0f5, atj0f6R,
                            nm(yg["aac001"]))
                    } catch (e: Exception) {
                        throw YueErr5("记录转款明细信息出错${e.message ?: ""}")
                    }
                }
                bba004 = bba004.add(bba004R); daa001 = daa001.add(daa001R ?: Z)
                atj0f6 = atj0f6.add(atj0f6R ?: Z)
                lLast = pct(bd5("atb90l")); nLast = pct(bd5("atb90n")); oLast = pct(bd5("atb90o"))
                qLast = pct(bd5("atb90q")); g399Last = pct(bd5("aaa399"))
            }
            val atj0f3 = bba004
            val a5 = bba004
            if (a5 > Z) {
                xh++
                try {
                    jdbc.update(headSql, ym, xh, qy[0], qy[1], qy[2], qy[3], aa03[0], aa03[1],
                        aaa020, jdbc.queryForObject("SELECT label FROM aa12 WHERE id=?",
                            String::class.java, aaa020),
                        rs, lLast, nLast, oLast, qLast, g399Last,
                        Z, Z, Z, bba004, Z, atj0f3, daa001, a5, atj0f6,
                        user[0], user[1], user[2], user[3], user[4])
                } catch (e: Exception) {
                    throw YueErr5("记录补收汇总信息出错${e.message ?: ""}")
                }
            }
        }
        return mapOf("code" to ok, "msg" to null as String?, "units" to units.size, "rows" to rows)
    }

    /** 省公积金补缴情况(TJ0D/D1): 活码=小写重写版 — TC9U_BP01 联查, CAA001/002=回盘桶
     *  abp005/006, 保险当月补缴调用已被注释→不调(忠实); 无大病/滞纳金。 */
    @Transactional
    fun shengGjjBujiao(aae002: String, aaa020: String, aaa030: String, aae011: String): Map<String, Any?> {
        val (ym, aa03, user) = entry(aae002, aaa020, aaa030, aae011)
        val detailSql = """INSERT INTO tj0d1 (aae002, atc900, atj000, atb930, atb931, aab001, aab004,
            aaa030, aaa031, aaa020, aaa021, aaa001, aac001, aac003, aac002, aac009,
            aaa303, atb90s_dw, atb90s_gr, caa001, caa002, atj0d5)
            VALUES (?, ?, '2', ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)""" // 22 参
        val headSql = """INSERT INTO tj0d (aae002, xh, atb930, atb931, aab001, aab004, aaa030,
            aaa031, aaa020, aaa021, atj000, atj0d2, atb90s_dw, atb90s_gr, caa001, caa002,
            atj0d5, aae011, aae017, aae036, aae019, aae020, aae022, aaezzz)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, '2', ?, ?, ?, ?, ?, ?,
            ?, ?, now(), ?, ?, ?, '0')""" // 21 参
        var xh = 0
        var rows = 0
        val units = jdbc.query(
            """SELECT DISTINCT a.atb930, b.atb931, c.aab001, c.aab004
               FROM tc90 a, tb93 b, ab01 c, tc9u_bp01 d
               WHERE a.atb930=b.atb930 AND b.aab001=c.aab001 AND a.atc900=d.atc900
                 AND c.aaa030=? AND a.aaa120=? AND d.aae002=? ORDER BY b.atb931""",
            { rs, _ -> arrayOf(rs.getObject("atb930"), rs.getString("atb931"),
                rs.getObject("aab001"), rs.getString("aab004")) },
            aaa030.toLong(), aaa020, ym)
        for (qy in units) {
            var rs = 0
            var caa001 = Z; var caa002 = Z
            var dwLast: String? = null; var grLast: String? = null
            val emps = jdbc.queryForList(
                """SELECT a.*, d.abp005, d.abp006 FROM tc90 a, tb93 b, ab01 c, tc9u_bp01 d
                   WHERE a.atb930=b.atb930 AND b.aab001=c.aab001 AND a.atc900=d.atc900
                     AND c.aaa030=? AND a.aaa120=? AND a.aaa407='1' AND d.aae002=?
                     AND a.atb930=?""",
                aaa030.toLong(), aaa020, ym, qy[0])
            for (yg in emps) {
                val atc900 = nm(yg["atc900"])!!
                rs++
                val aaa303 = yg["aaa303"] as BigDecimal?
                val atb90s = yg["atb90s"]?.toString()
                val caa1 = yg["abp005"]?.toString()?.toBigDecimalOrNull()
                val caa2 = yg["abp006"]?.toString()?.toBigDecimalOrNull()
                val a5 = (caa1 ?: Z).add(caa2 ?: Z)
                if (a5 > Z) {
                    try {
                        jdbc.update(detailSql, ym, atc900, qy[0], qy[1], qy[2], qy[3],
                            aa03[0], aa03[1], yg["aaa120"], yg["aaa121"], yg["aaa004"],
                            nm(yg["aac001"]), yg["aac003"], yg["aac002"], yg["aac009"],
                            aaa303,
                            atb90s?.let { it.substringBefore(':') + "%" },
                            atb90s?.let { it.substringAfter(':', "") + "%" },
                            caa1, caa2, a5)
                    } catch (e: Exception) {
                        throw YueErr5("记录转款明细信息出错${e.message ?: ""}")
                    }
                    rows++
                }
                caa001 = caa001.add(caa1 ?: Z); caa002 = caa002.add(caa2 ?: Z)
            }
            val a5 = caa001.add(caa002)
            if (a5 > Z) {
                xh++
                try {
                    jdbc.update(headSql, ym, xh, qy[0], qy[1], qy[2], qy[3], aa03[0], aa03[1],
                        aaa020, jdbc.queryForObject("SELECT label FROM aa12 WHERE id=?",
                            String::class.java, aaa020),
                        rs, null, null, caa001, caa002, a5,
                        user[0], user[1], user[2], user[3], user[4])
                } catch (e: Exception) {
                    throw YueErr5("记录转款汇总信息出错${e.message ?: ""}")
                }
            }
        }
        return mapOf("code" to ok, "msg" to null as String?, "units" to units.size, "rows" to rows)
    }
}

class YueErr5(val m: String) : RuntimeException(m)

@Component
class Yueqk5SbProc(private val c: Yueqk5Cases) : ProcHandler {
    override fun procName() = "PKG_派遣保险月情况.PRC_省医保补缴情况"
    override fun handle(params: Map<String, Any?>): Map<String, Any?> {
        for (k in listOf("aae002", "aaa020", "aaa030", "aae011"))
            if (params[k] == null) return mapOf("appCode" to -1, "errMsg" to "缺少 $k")
        val r = try {
            c.sbBujiao(params["aae002"]!!.toString(), params["aaa020"]!!.toString(),
                params["aaa030"]!!.toString(), params["aae011"]!!.toString())
        } catch (e: YueErr5) {
            mapOf("code" to -1, "msg" to e.m, "units" to 0, "rows" to 0, "skipped" to null)
        }
        return mapOf("appCode" to r["code"], "errMsg" to r["msg"],
            "units" to r["units"], "rows" to r["rows"], "skipped" to r["skipped"])
    }
}

@Component
class Yueqk5ShiGjjProc(private val c: Yueqk5Cases) : ProcHandler {
    override fun procName() = "PKG_派遣保险月情况.PRC_市公积金补缴情况"
    override fun handle(params: Map<String, Any?>): Map<String, Any?> {
        for (k in listOf("aae002", "aaa020", "aaa030", "aae011"))
            if (params[k] == null) return mapOf("appCode" to -1, "errMsg" to "缺少 $k")
        val r = try {
            c.shiGjjBujiao(params["aae002"]!!.toString(), params["aaa020"]!!.toString(),
                params["aaa030"]!!.toString(), params["aae011"]!!.toString())
        } catch (e: YueErr5) {
            mapOf("code" to -1, "msg" to e.m, "units" to 0, "rows" to 0, "skipped" to null)
        }
        return mapOf("appCode" to r["code"], "errMsg" to r["msg"],
            "units" to r["units"], "rows" to r["rows"], "skipped" to r["skipped"])
    }
}

@Component
class Yueqk5GsProc(private val c: Yueqk5Cases) : ProcHandler {
    override fun procName() = "PKG_派遣保险月情况.PRC_工伤补缴情况"
    override fun handle(params: Map<String, Any?>): Map<String, Any?> {
        for (k in listOf("aae002", "aaa020", "aaa030", "aae011"))
            if (params[k] == null) return mapOf("appCode" to -1, "errMsg" to "缺少 $k")
        val r = try {
            c.gsBujiao(params["aae002"]!!.toString(), params["aaa020"]!!.toString(),
                params["aaa030"]!!.toString(), params["aae011"]!!.toString())
        } catch (e: YueErr5) {
            mapOf("code" to -1, "msg" to e.m, "units" to 0, "rows" to 0, "skipped" to null)
        }
        return mapOf("appCode" to r["code"], "errMsg" to r["msg"],
            "units" to r["units"], "rows" to r["rows"], "skipped" to r["skipped"])
    }
}

@Component
class Yueqk5ShengGjjProc(private val c: Yueqk5Cases) : ProcHandler {
    override fun procName() = "PKG_派遣保险月情况.PRC_省公积金补缴情况"
    override fun handle(params: Map<String, Any?>): Map<String, Any?> {
        for (k in listOf("aae002", "aaa020", "aaa030", "aae011"))
            if (params[k] == null) return mapOf("appCode" to -1, "errMsg" to "缺少 $k")
        val r = try {
            c.shengGjjBujiao(params["aae002"]!!.toString(), params["aaa020"]!!.toString(),
                params["aaa030"]!!.toString(), params["aae011"]!!.toString())
        } catch (e: YueErr5) {
            mapOf("code" to -1, "msg" to e.m, "units" to 0, "rows" to 0, "skipped" to null)
        }
        return mapOf("appCode" to r["code"], "errMsg" to r["msg"],
            "units" to r["units"], "rows" to r["rows"], "skipped" to r["skipped"])
    }
}
