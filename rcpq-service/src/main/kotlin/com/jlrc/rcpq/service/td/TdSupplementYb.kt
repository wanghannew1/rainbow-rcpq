package com.jlrc.rcpq.service.td

import java.math.BigDecimal

/**
 * PRC_医保补退明细(L3775-5358) 移植 — 桥单 20261127-041。
 * 省医保(substr(AAA110,3,2)='99'): '20'参保差异(AC10 aac101='2', BBA005 不冲只置零)/
 * '21'参保异议(TJ0B1×TC93, ZJHJ/GZHJ 含 202105 前后 CASE, DAA001_GZ=ATC93BC+BD)/
 * '27'补发(门+AAA208>=202202)/'22'省停保(BBA004 按 AAE002_GZ<202105 条件)/
 * '23'省征缴差异×2(SUM(DAA001) 子查询, 无零和门, 计代理费)。
 * 市医保(≠'99'): '21'/'22' TJ0C1 版(BBA200 无 ATB90T; BBA004 按 AAE003>=202107→0)/
 * '23'市×2(GZHJ 双计 ATB90T 源码笔误忠实; ATC9EH≠0 门; 不计代理费)。
 * 基数补差: '24'TJ0B2 / '25'TJ0B3(ATJ0B5) / '26'TC09 减免(负冲)。
 * 离职未参保'29'(AC10 aac101='2' + TC93 退按月收取含 DAA002-=BC/DAA001-=BD, 无零和门)。
 * 大病承担侧(AAA803): TC90 空→看 TB93, '1'=单位承担(DAA002) 否则个人(DAA001)。
 */
fun TdSupplementCases.yiBao(aae002: String, tc90: Map<String, Any?>, t: MutableMap<String, Any?>): Int {
    val nAae002 = s.monthAdd(aae002, -1)
    val atc900 = s.long(tc90["atc900"])!!
    val atb930 = s.long(tc90["atb930"])!!
    val aac001 = s.long(tc90["aac001"])!!
    val nBjBegin = bjBegin("yb", atc900, atb930, aac001, aae002)
    val nBj24 = bjN("24", atb930, aac001)
    val nBj25 = bjN("25", atb930, aac001)
    val nBj26 = bjN("26", atb930, aac001)
    var dlf = 0
    val a110 = s.str(tc90["aaa110"]) ?: ""
    val prov99 = a110.length >= 4 && a110.substring(2, 4) == "99"

    fun daa(r: MutableMap<String, Any?>, zDaa001: BigDecimal, gDaa001: BigDecimal) {
        val tb93Flag = s.str(tc90["aaa803_tb93"])
        val tc90Flag = s.str(tc90["aaa803_tc90"])
        val unit = if (tc90Flag == null) tb93Flag == "1" else tc90Flag == "1"
        if (unit) {
            r["daa001"] = BigDecimal.ZERO
            r["daa002"] = s.nz(r["daa002"]).add(zDaa001).subtract(gDaa001)
        } else {
            r["daa001"] = s.nz(r["daa001"]).add(zDaa001).subtract(gDaa001)
            r["daa002"] = BigDecimal.ZERO
        }
    }
    fun sumSb(r: MutableMap<String, Any?>) { // 省版 BBA200 含 ATB90T
        r["bba200"] = s.nz(r["bba002"]).add(s.nz(r["bba004"])).add(s.nz(r["bba005"]))
            .add(s.nz(r["atb90t"])).add(s.nz(r["daa002"]))
        r["baa200"] = s.nz(r["baa002"]).add(s.nz(r["daa001"]))
        r["atc9eh"] = s.nz(r["bba200"]).add(s.nz(r["baa200"]))
    }
    fun sumShi(r: MutableMap<String, Any?>) { // 市版 BBA200 无 ATB90T
        r["bba200"] = s.nz(r["bba002"]).add(s.nz(r["bba004"])).add(s.nz(r["bba005"]))
            .add(s.nz(r["daa002"]))
        r["baa200"] = s.nz(r["baa002"]).add(s.nz(r["daa001"]))
        r["atc9eh"] = s.nz(r["bba200"]).add(s.nz(r["baa200"]))
    }
    /** 省版 202105 前后 CASE 合计: 前(含 BBA004)/后(不含) */
    fun caseHj(ym: Any?, b2: Any?, b4: Any?, b5: Any?, b0: Any?, t0: Any?, d1: Any?): BigDecimal {
        val before = ym != null && s.nvl(ym).toLong() < 202105
        var v = s.nz(b2).add(s.nz(b5)).add(s.nz(b0)).add(s.nz(t0)).add(s.nz(d1))
        if (before) v = v.add(s.nz(b4))
        return v
    }

    val gateProv = (s.str(tc90["atc90pa"]) == "0" || s.str(tc90["atc90pa"]) == "1") &&
        s.nvl(tc90["aaa608"]).toLong() >= nBjBegin && nBjBegin <= aae002.toLong() && prov99
    if (gateProv) {
        // '20' 参保年月差异(L3902-3962)
        val a601 = s.str(tc90["aaa601"]); val a603 = s.str(tc90["aaa603"])
        if (a601 != null && a603 != null && a601 < a603 && a601 <= aae002) {
            val ksny = a601; val jsny = monthOrErr(a603, -1)
            val ac10s = s.jdbc.queryForList(
                "SELECT * FROM ac10 WHERE aac101='2' AND atc932>=?::numeric AND atc932<=?::numeric AND atc900=?",
                ksny.toBigDecimal(), jsny.toBigDecimal(), atc900)
            for (a in ac10s) {
                val r = HashMap(t)
                r["aae003"] = a["atc932"]; r["atc072"] = "20"
                r["bba002"] = s.nz(r["bba002"]).subtract(s.nz(a["bba002"]))
                r["bba004"] = s.nz(r["bba004"]).subtract(s.nz(a["bba004"]))
                r["bba005"] = BigDecimal.ZERO // 源: 生育只置零不冲
                r["atb90t"] = s.nz(r["atb90t"]).subtract(s.nz(a["atb90t"]))
                r["baa002"] = s.nz(r["baa002"]).subtract(s.nz(a["baa002"]))
                r["daa001"] = s.nz(r["daa001"]).subtract(s.nz(a["daa001"]))
                r["daa002"] = s.nz(t["daa002"]).subtract(s.nz(a["daa002"]))
                sumSb(r); ins07(r)
            }
        }
        // '21' 省 参保异议退发(L3964-4078): AAA603>=AAE002, 窗下界=合同 AAE002
        if (a603 != null && a603 >= aae002) {
            val win = monthOrErr(a603, -1)
            val lower = s.nvl(tc90["aae002"])
            val bRows = s.jdbc.queryForList(
                """SELECT atc931 aae002_gz, bba002, bba004, bba005, atb90t, baa002,
                          COALESCE(atc93bc,0)+COALESCE(atc93bd,0) daa001_gz
                   FROM tc93 b WHERE b.atc937='1' AND b.aae214='1'
                   AND b.atc931>=?::numeric AND b.atc931<=?::numeric AND b.atc900=?""",
                lower, win.toBigDecimal(), atc900)
            for (b in bRows) {
                val gzHj = caseHj(b["aae002_gz"], b["bba002"], b["bba004"], b["bba005"],
                    b["baa002"], b["atb90t"], b["daa001_gz"])
                val zs = s.jdbc.queryForList(
                    """SELECT bba002, bba004, bba005, atb90t, baa002, daa001 FROM tj0b1
                       WHERE aae002=?::numeric AND atj000='1' AND aae013='1' AND atc900=?""",
                    b["aae002_gz"], atc900)
                val pairs = if (zs.isEmpty()) listOf(mapOf<String, Any?>(
                    "bba002" to null, "bba004" to null, "bba005" to null,
                    "atb90t" to null, "baa002" to null, "daa001" to null)) else zs
                for (z in pairs) {
                    val zjHj = caseHj(b["aae002_gz"], z["bba002"], z["bba004"],
                        z["bba005"], z["baa002"], z["atb90t"], z["daa001"])
                    if (BigDecimal.ZERO.compareTo(zjHj) == 0 && zjHj.compareTo(gzHj) != 0) {
                        val r = HashMap(t)
                        r["aae003"] = b["aae002_gz"]; r["atc072"] = "21"
                        r["bba002"] = s.nz(r["bba002"]).add(s.nz(z["bba002"])).subtract(s.nz(b["bba002"]))
                        r["bba004"] = s.nz(r["bba004"]).add(s.nz(z["bba004"])).subtract(s.nz(b["bba004"]))
                        r["bba005"] = s.nz(r["bba005"]).add(s.nz(z["bba005"])).subtract(s.nz(b["bba005"]))
                        r["atb90t"] = s.nz(r["atb90t"]).add(s.nz(z["atb90t"])).subtract(s.nz(b["atb90t"]))
                        r["baa002"] = s.nz(r["baa002"]).add(s.nz(z["baa002"])).subtract(s.nz(b["baa002"]))
                        daa(r, s.nz(z["daa001"]), s.nz(b["daa001_gz"]))
                        sumSb(r); ins07(r)
                    }
                }
            }
        }
    }
    // '27' 省 补发(L4090-4192)
    if (gateProv && s.nvl(tc90["aaa208"]).toLong() >= 202202) {
        val a601 = s.str(tc90["aaa601"]); val a603 = s.str(tc90["aaa603"])
        if (a601 != null && a603 != null && a601 > a603) {
            val months = buFaYue(tc90, aae002, a603, monthOrErr(a601, -1))
            var ym = a603!!
            for (tc01 in months) {
                val r = HashMap(t)
                r["aae003"] = ym.toBigDecimal(); r["atc072"] = "27"
                r["bba002"] = tc01.bba002; r["bba004"] = tc01.bba004
                r["bba005"] = BigDecimal.ZERO; r["atb90t"] = tc01.atb90t
                r["baa002"] = tc01.baa002
                r["daa001"] = tc01.daa001
                r["daa002"] = tc01.daa002 ?: BigDecimal.ZERO
                sumSb(r); ins07(r)
                ym = s.monthAdd(ym, 1)
            }
        }
    }
    // '22' 省 停保(L4196-4286): ATC90PA='1' 且 AAA609 达窗
    if (s.str(tc90["atc90pa"]) == "1" && s.nvl(tc90["aaa609"]).toLong() >= nBjBegin &&
        nBjBegin <= aae002.toLong() && prov99) {
        val a609 = s.str(tc90["aaa609"])!!
        ybTingBui22(t, tc90, a609, nAae002, prov = true, atc900 = atc900)
    }
    // '23' 省 征缴与工资差异×2(L4289-4478)
    dlf += ybCha23(t, tc90, aae002, nBjBegin, nAae002, prov = true, keyAtb930 = true)
    dlf += ybCha23(t, tc90, aae002, nBjBegin, nAae002, prov = true, keyAtb930 = false)
    // 市医保 '21'(L4482-4600): 门≠'99' 且 AAA603>AAE002(严格大于)
    val gateShi = (s.str(tc90["atc90pa"]) == "0" || s.str(tc90["atc90pa"]) == "1") &&
        s.nvl(tc90["aaa608"]).toLong() >= nBjBegin && nBjBegin <= aae002.toLong() && !prov99
    if (gateShi) {
        val a603 = s.str(tc90["aaa603"])
        if (a603 != null && a603 > aae002) {
            val win = monthOrErr(a603, -1)
            val lower = s.nvl(tc90["aae002"])
            val bRows = s.jdbc.queryForList(
                """SELECT atc931 aae002_gz, bba002, bba004, bba005, atb90t, baa002,
                          COALESCE(atc93bc,0)+COALESCE(atc93bd,0) daa001_gz
                   FROM tc93 b WHERE b.atc937='1' AND b.aae214='1'
                   AND b.atc931>=?::numeric AND b.atc931<=?::numeric AND b.atc900=?""",
                lower, win.toBigDecimal(), atc900)
            for (b in bRows) {
                val gzHj = s.nz(b["bba002"]).add(s.nz(b["bba004"])).add(s.nz(b["bba005"]))
                    .add(s.nz(b["baa002"])).add(s.nz(b["atb90t"])).add(s.nz(b["daa001_gz"]))
                val zs = s.jdbc.queryForList(
                    """SELECT bba002, bba004, bba005, baa002, daa001 FROM tj0c1
                       WHERE aae002=?::numeric AND atj000='1' AND aae013='1' AND atc900=?""",
                    b["aae002_gz"], atc900)
                val pairs = if (zs.isEmpty()) listOf(mapOf<String, Any?>(
                    "bba002" to null, "bba004" to null, "bba005" to null,
                    "baa002" to null, "daa001" to null)) else zs
                for (z in pairs) {
                    val zjHj = s.nz(z["bba002"]).add(s.nz(z["bba004"])).add(s.nz(z["bba005"]))
                        .add(s.nz(z["baa002"])).add(s.nz(z["daa001"]))
                    if (BigDecimal.ZERO.compareTo(zjHj) == 0 && zjHj.compareTo(gzHj) != 0) {
                        val r = HashMap(t)
                        r["aae003"] = b["aae002_gz"]; r["atc072"] = "21"
                        r["bba002"] = s.nz(r["bba002"]).add(s.nz(z["bba002"])).subtract(s.nz(b["bba002"]))
                        if (s.nvl(b["aae002_gz"]).toLong() >= 202107) r["bba004"] = BigDecimal.ZERO
                        else r["bba004"] = s.nz(r["bba004"]).add(s.nz(z["bba004"])).subtract(s.nz(b["bba004"]))
                        r["bba005"] = s.nz(r["bba005"]).add(s.nz(z["bba005"])).subtract(s.nz(b["bba005"]))
                        r["baa002"] = s.nz(r["baa002"]).add(s.nz(z["baa002"])).subtract(s.nz(b["baa002"]))
                        daa(r, s.nz(z["daa001"]), s.nz(b["daa001_gz"]))
                        sumShi(r); ins07(r)
                    }
                }
            }
        }
    }
    // '22' 市 停保(L4700-4806)
    if (s.str(tc90["atc90pa"]) == "1" && s.nvl(tc90["aaa609"]).toLong() >= nBjBegin &&
        nBjBegin <= aae002.toLong() && !prov99) {
        val a609 = s.str(tc90["aaa609"])!!
        ybTingBui22(t, tc90, a609, nAae002, prov = false, atc900 = atc900)
    }
    // '23' 市 征缴与工资金额差异×2(L4809-5147): GZHJ 双计 ATB90T 源码笔误
    ybCha23(t, tc90, aae002, nBjBegin, nAae002, prov = false, keyAtb930 = true)
    ybCha23(t, tc90, aae002, nBjBegin, nAae002, prov = false, keyAtb930 = false)
    // '24' 医保基数补差 / '25' 工伤基数补差 / '26' 省工伤减免 — 三段同构
    val ygRows = s.jdbc.queryForList("SELECT * FROM tc90 WHERE atb930=? AND aac001=?", atb930, aac001)
    fun reassign(r: MutableMap<String, Any?>) {
        if (s.long(r["atc900"]) != s.long(t["atc900"])) {
            try {
                s.jdbc.update("UPDATE tc07 SET atc900=? WHERE atc070=?", t["atc900"], r["atc070"])
            } catch (e: Exception) {
                s.jdbc.update("DELETE FROM tc07 WHERE atc070=?", r["atc070"])
            }
        }
    }
    for (yg in ygRows) {
        val zRows = s.jdbc.queryForList(
            """SELECT * FROM tj0b2 WHERE aaezzz='1' AND aae003>?::numeric
               AND aae002>=202201 AND atc900=?""", nBj24.toBigDecimal(), s.long(yg["atc900"]))
        for (z in zRows) {
            val r = HashMap(t)
            r["atc900"] = yg["atc900"]; r["aae003"] = z["aae003"]; r["atc072"] = "24"
            r["bba002"] = s.nz(r["bba002"]).add(s.nz(z["bba002"]))
            r["bba004"] = s.nz(r["bba004"]).add(s.nz(z["bba004"]))
            r["bba005"] = s.nz(r["bba005"]).add(s.nz(z["bba005"]))
            r["atb90t"] = s.nz(r["atb90t"]).add(s.nz(z["atb90t"]))
            r["baa002"] = s.nz(r["baa002"]).add(s.nz(z["baa002"]))
            sumSb(r)
            if (ins07(r)) reassign(r)
        }
    }
    for (yg in ygRows) {
        val zRows = s.jdbc.queryForList(
            """SELECT * FROM tj0b3 WHERE aaezzz='1' AND aae003>?::numeric
               AND aae002>=202201 AND atc900=?""", nBj25.toBigDecimal(), s.long(yg["atc900"]))
        for (z in zRows) {
            val r = HashMap(t)
            r["aae003"] = z["aae003"]; r["atc900"] = yg["atc900"]; r["atc072"] = "25"
            r["bba002"] = BigDecimal.ZERO
            r["bba004"] = s.nz(r["bba004"]).add(s.nz(z["atj0b5"]))
            r["bba005"] = BigDecimal.ZERO; r["atb90t"] = BigDecimal.ZERO
            r["baa002"] = BigDecimal.ZERO
            sumSb(r)
            if (ins07(r)) reassign(r)
        }
    }
    for (yg in ygRows) {
        val zRows = s.jdbc.queryForList(
            """SELECT * FROM tc09 WHERE aae003>?::numeric AND aae002>=202201 AND atc900=?""",
            nBj26.toBigDecimal(), s.long(yg["atc900"]))
        for (z in zRows) {
            val r = HashMap(t)
            r["atc900"] = yg["atc900"]; r["aae003"] = z["aae003"]; r["atc072"] = "26"
            r["bba002"] = BigDecimal.ZERO
            r["bba004"] = s.nz(r["bba004"]).subtract(s.nz(z["bba004"]))
            r["bba005"] = BigDecimal.ZERO; r["atb90t"] = BigDecimal.ZERO
            r["baa002"] = BigDecimal.ZERO
            sumSb(r)
            if (ins07(r)) reassign(r)
        }
    }
    // 离职未参保 '29'(L5317-5357): ATB90R='5' 且 ATC90PA∈('2',NULL)
    if (s.str(tc90["atb90r"]) == "5" && s.nvl(tc90["aae003"]).toLong() <= aae002.toLong() &&
        (s.str(tc90["atc90pa"]) == "2" || s.str(tc90["atc90pa"]) == null)) {
        val ac10s = s.jdbc.queryForList(
            "SELECT * FROM ac10 WHERE aac101='2' AND atc900=?", atc900)
        for (a in ac10s) {
            val r = HashMap(t)
            r["atc072"] = "29"; r["aae003"] = a["atc932"]
            r["bba002"] = s.nz(r["bba002"]).subtract(s.nz(a["bba002"]))
            r["bba004"] = s.nz(r["bba004"]).subtract(s.nz(a["bba004"]))
            r["bba005"] = s.nz(r["bba005"]).subtract(s.nz(a["bba005"]))
            r["atb90t"] = s.nz(r["atb90t"]).subtract(s.nz(a["atb90t"]))
            r["baa002"] = s.nz(r["baa002"]).subtract(s.nz(a["baa002"]))
            sumShi(r); ins07(r)
        }
        val tRows = s.jdbc.queryForList(
            """SELECT * FROM tc93 WHERE (COALESCE(baa200,0)+COALESCE(bba200,0)) <> 0
               AND atc931>=202012 AND atc900=?""", atc900)
        for (x in tRows) {
            val r = HashMap(t)
            r["atc072"] = "29"; r["aae003"] = x["atc931"]
            r["bba002"] = s.nz(r["bba002"]).subtract(s.nz(x["bba002"]))
            r["bba004"] = s.nz(r["bba004"]).subtract(s.nz(x["bba004"]))
            r["bba005"] = s.nz(r["bba005"]).subtract(s.nz(x["bba005"]))
            r["atb90t"] = s.nz(r["atb90t"]).subtract(s.nz(x["atb90t"]))
            r["baa002"] = s.nz(r["baa002"]).subtract(s.nz(x["baa002"]))
            r["daa002"] = s.nz(r["daa002"]).subtract(s.nz(x["atc93bc"]))
            r["daa001"] = s.nz(r["daa001"]).subtract(s.nz(x["atc93bd"]))
            sumShi(r); ins07(r)
        }
    }
    return dlf
}
