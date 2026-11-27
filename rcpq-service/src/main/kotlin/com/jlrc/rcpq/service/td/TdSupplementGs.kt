package com.jlrc.rcpq.service.td

import java.math.BigDecimal

/**
 * PRC_工伤补退明细(L5360-6470)+PRC_公积金补退明细(L6472-7504) 移植 — 桥单 20261127-041。
 * 工伤: '40'参保差异(AC10 aac101='4', 仅 BBA004)/'41'异议(TJ0F1×TC93, ZJHJ=GZHJ=BBA004)/
 * '46'补发(门 AAA908>=202202 且 substr(AAA130,3,2)='99' 活性)/'42'停保(AAA909)/
 * '43'差异×2(TC18 202201 特例闸; AAE003=202107 且 ATC9EH=-0.01 → ATC07Z='9' 怪笔;
 * N_BEGIN('46' 水位)声明即查后无消费点=死变量, 查询保留)/'49'离职(AC10 无 aac101 过滤)。
 * 公积金: 省'30'/'31'/'36'/'32'/'33'×2(AAC101='3', TJ0D1)/市'30'(JSNY 仅两分支:
 * AAA503<N_AAE002→AAA503, =N_AAE002→prev, 其余 NULL→报错)/市'31'/'32'/'33'×2(TJ0E1)/
 * '39'离职/'35'TC0A 补差(NOT EXISTS 幂等闸, ABP00D/E)。
 */
fun TdSupplementCases.gongShang(aae002: String, tc90: Map<String, Any?>, t: MutableMap<String, Any?>): Int {
    val nAae002 = s.monthAdd(aae002, -1)
    val atc900 = s.long(tc90["atc900"])!!
    val atb930 = s.long(tc90["atb930"])!!
    val aac001 = s.long(tc90["aac001"])!!
    val nBjBegin = bjBegin("gs", atc900, atb930, aac001, aae002)
    // N_BEGIN('46' 水位查询, 源后无消费点=死变量, 保留查询面忠实)
    run {
        try {
            s.jdbc.queryForObject(
                """SELECT CASE WHEN MAX(aae003) IS NULL THEN 202107
                   ELSE (MAX(aae003)::bigint + 1) END FROM tc07 WHERE atc07z IN ('0','1')
                   AND atc072='46' AND datasource='30'
                   AND (COALESCE(bba002,0)+COALESCE(bba004,0)+COALESCE(bba005,0)
                        +COALESCE(atb90t,0)+COALESCE(baa002,0)) <> 0 AND atc900=?""",
                Long::class.java, atc900)
        } catch (e: Exception) { /* 源 EXCEPTION → N_BEGIN := N_AAE002(未消费) */ }
    }
    var dlf = 0
    val a130 = s.str(tc90["aaa130"]) ?: ""
    val prov130 = a130.length >= 4 && a130.substring(2, 4) == "99"

    fun sums(r: MutableMap<String, Any?>) { // 工伤版: BBA002/005/ATB90T/BAA002 恒零参与
        r["bba200"] = s.nz(r["bba002"]).add(s.nz(r["bba004"])).add(s.nz(r["bba005"]))
            .add(s.nz(r["atb90t"])).add(s.nz(r["daa002"]))
        r["baa200"] = s.nz(r["baa002"]).add(s.nz(r["daa001"]))
        r["atc9eh"] = s.nz(r["bba200"]).add(s.nz(r["baa200"]))
    }

    val gate40 = (s.str(tc90["atc90pb"]) == "0" || s.str(tc90["atc90pb"]) == "1") &&
        s.nvl(tc90["aaa908"]).toLong() >= nBjBegin && nBjBegin <= aae002.toLong()
    // 注: 此门 substr(AAA130,3,2)='99' 在源中被注释=不生效(活性), 仅 '46' 补发门生效
    if (gate40) {
        val a901 = s.str(tc90["aaa901"]); val a903 = s.str(tc90["aaa903"])
        // '40' 参保年月差异(L5430-5480)
        if (a901 != null && a903 != null && a901 < a903 && a901 < aae002) {
            val ksny = a901; val jsny = monthOrErr(a903, -1)
            val ac10s = s.jdbc.queryForList(
                "SELECT * FROM ac10 WHERE aac101='4' AND atc932>=?::numeric AND atc932<=?::numeric AND atc900=?",
                ksny.toBigDecimal(), jsny.toBigDecimal(), atc900)
            for (a in ac10s) {
                val r = HashMap(t)
                r["aae003"] = a["atc932"]; r["atc072"] = "40"
                r["bba002"] = BigDecimal.ZERO
                r["bba004"] = s.nz(r["bba004"]).subtract(s.nz(a["bba004"]))
                r["bba005"] = BigDecimal.ZERO; r["atb90t"] = BigDecimal.ZERO
                r["baa002"] = BigDecimal.ZERO
                r["daa001"] = BigDecimal.ZERO; r["daa002"] = BigDecimal.ZERO
                sums(r); ins07(r)
            }
        }
        // '41' 参保异议退发(L5482-5560): AAA903>=AAE002, TJ0F1×TC93, 窗下界=合同 AAE002
        if (a903 != null && a903 >= aae002) {
            val win = monthOrErr(a903, -1)
            val lower = s.nvl(tc90["aae002"])
            val bRows = s.jdbc.queryForList(
                """SELECT atc931 aae002_gz, bba004 FROM tc93 b
                   WHERE b.atc937='1' AND b.aae214='1'
                   AND b.atc931>=?::numeric AND b.atc931<=?::numeric AND b.atc900=?""",
                lower, win.toBigDecimal(), atc900)
            for (b in bRows) {
                val gzHj = s.nz(b["bba004"])
                val zs = s.jdbc.queryForList(
                    """SELECT bba004 FROM tj0f1 WHERE aae002=?::numeric AND atj000='1'
                       AND aae013='1' AND atc900=?""", b["aae002_gz"], atc900)
                val pairs = if (zs.isEmpty()) listOf(mapOf<String, Any?>("bba004" to null)) else zs
                for (z in pairs) {
                    val zjHj = s.nz(z["bba004"])
                    if (BigDecimal.ZERO.compareTo(zjHj) == 0 && zjHj.compareTo(gzHj) != 0) {
                        val r = HashMap(t)
                        r["aae003"] = b["aae002_gz"]; r["atc072"] = "41"
                        r["bba002"] = BigDecimal.ZERO
                        r["bba004"] = s.nz(r["bba004"]).add(s.nz(z["bba004"])).subtract(s.nz(b["bba004"]))
                        r["bba005"] = BigDecimal.ZERO; r["atb90t"] = BigDecimal.ZERO
                        r["baa002"] = BigDecimal.ZERO
                        sums(r); ins07(r)
                    }
                }
            }
        }
    }
    // '46' 省 补发(L5585-5683): 门+AAA908>=202202+substr(AAA130,3,2)='99'(此处活性)
    if (gate40 && s.nvl(tc90["aaa908"]).toLong() >= 202202 && prov130) {
        val a901 = s.str(tc90["aaa901"]); val a903 = s.str(tc90["aaa903"])
        if (a901 != null && a903 != null && a901 > a903) {
            val months = buFaYue(tc90, aae002, a903, monthOrErr(a901, -1))
            var ym = a903!!
            for (tc01 in months) {
                val r = HashMap(t)
                r["aae003"] = ym.toBigDecimal(); r["atc072"] = "46"
                r["bba002"] = BigDecimal.ZERO
                r["bba004"] = tc01.bba004
                r["bba005"] = BigDecimal.ZERO; r["atb90t"] = BigDecimal.ZERO
                r["baa002"] = BigDecimal.ZERO
                r["daa001"] = BigDecimal.ZERO; r["daa002"] = BigDecimal.ZERO
                sums(r); ins07(r)
                ym = s.monthAdd(ym, 1)
            }
        }
    }
    // '42' 省 停保(L5686-5775): ATC90PB='1' 且 AAA909 达窗
    if (s.str(tc90["atc90pb"]) == "1" && s.nvl(tc90["aaa909"]).toLong() >= nBjBegin &&
        nBjBegin <= aae002.toLong()) {
        val a909 = s.str(tc90["aaa909"])!!
        val bRows = s.jdbc.queryForList(
            """SELECT atc931 aae002_gz, bba004 FROM tc93 b
               WHERE b.atc937='1' AND b.aae214='1'
               AND b.atc931>=?::numeric AND b.atc931<=?::numeric AND b.atc900=?""",
            a909.toBigDecimal(), nAae002.toBigDecimal(), atc900)
        for (b in bRows) {
            val gzHj = s.nz(b["bba004"])
            val zs = s.jdbc.queryForList(
                """SELECT bba004 FROM tj0f1 WHERE aae002=?::numeric AND atj000='1'
                   AND aae013='1' AND atc900=?""", b["aae002_gz"], atc900)
            val pairs = if (zs.isEmpty()) listOf(mapOf<String, Any?>("bba004" to null)) else zs
            for (z in pairs) {
                val zjHj = s.nz(z["bba004"])
                if (BigDecimal.ZERO.compareTo(zjHj) == 0 && zjHj.compareTo(gzHj) != 0) {
                    val r = HashMap(t)
                    r["aae003"] = b["aae002_gz"]; r["atc072"] = "42"
                    r["bba002"] = BigDecimal.ZERO
                    r["bba004"] = s.nz(r["bba004"]).add(s.nz(z["bba004"])).subtract(s.nz(b["bba004"]))
                    r["bba005"] = BigDecimal.ZERO; r["atb90t"] = BigDecimal.ZERO
                    r["baa002"] = BigDecimal.ZERO
                    sums(r); ins07(r)
                }
            }
        }
    }
    // '43' 征缴与工资差异×2(L5777-5960): TC18 202201 特例+202107/-0.01→'9' 怪笔
    for (pass in 1..2) {
        val ygRows = s.jdbc.queryForList(
            "SELECT * FROM tc90 WHERE atb930=? AND aac001=? ORDER BY atc900", atb930, aac001)
        for (yg in ygRows) {
            val ygAtc900 = s.long(yg["atc900"])!!
            val aRows = s.jdbc.queryForList(
                """SELECT aae002 aae002_zj, bba004 FROM tj0f1
                   WHERE aae002>=?::numeric AND aae002<=?::numeric AND atj000='1' AND aae013='1'
                   AND ${if (pass == 1) "atb930=? AND aac001=?" else "atc900=?"}""",
                nBjBegin.toBigDecimal(), nAae002.toBigDecimal(),
                *(if (pass == 1) arrayOf(yg["atb930"], yg["aac001"]) else arrayOf<Any?>(ygAtc900)))
            for (a in aRows) {
                val zjHj = s.nz(a["bba004"])
                val bRows = s.jdbc.queryForList(
                    """SELECT atc931 aae002_gz, bba004 FROM tc93 b
                       WHERE b.atc937='1' AND b.aae214='1' AND b.atc931>=?::numeric
                         AND b.atc931<=?::numeric AND ${if (pass == 1) "b.atb930=? AND b.aac001=?" else "b.atc900=?"}""",
                    nBjBegin.toBigDecimal(), nAae002.toBigDecimal(),
                    *(if (pass == 1) arrayOf(yg["atb930"], yg["aac001"]) else arrayOf<Any?>(ygAtc900)))
                val matches = bRows.filter { s.nvl(it["aae002_gz"]).compareTo(s.nvl(a["aae002_zj"])) == 0 }
                val diffs = if (matches.isEmpty()) listOf(mapOf<String, Any?>(
                    "aae002_gz" to null, "bba004" to null)) else matches.filter {
                    zjHj.compareTo(s.nz(it["bba004"])) != 0
                }
                for (m in diffs) {
                    var nJsq = 1
                    var nCnt = 0
                    // TC18 202201 特例闸(两循环同): 当年 1 月有记录则跳过
                    if (s.nvl(a["aae002_zj"]).toLong() == 202201L) {
                        nCnt = s.jdbc.queryForObject(
                            "SELECT COUNT(*) FROM tc18 WHERE aae002=?::numeric AND aac002=?",
                            Int::class.java, a["aae002_zj"], yg["aac002"]) ?: 0
                    }
                    if (nCnt == 0) {
                        val r = HashMap(t)
                        r["atc900"] = yg["atc900"]; r["aae003"] = a["aae002_zj"]
                        r["atc072"] = "43"
                        r["bba002"] = BigDecimal.ZERO
                        r["bba004"] = s.nz(r["bba004"]).add(s.nz(a["bba004"])).subtract(s.nz(m["bba004"]))
                        r["bba005"] = BigDecimal.ZERO; r["atb90t"] = BigDecimal.ZERO
                        r["baa002"] = BigDecimal.ZERO
                        sums(r)
                        // 怪笔: 202107 月合计恰为 -0.01 → 置已确认态 '9'(源活性)
                        if (s.nvl(a["aae002_zj"]).toLong() == 202107L &&
                            s.nz(r["atc9eh"]).compareTo(BigDecimal("-0.01")) == 0) {
                            r["atc07z"] = "9"
                        }
                        nCnt = s.jdbc.queryForObject(
                            "SELECT COUNT(*) FROM ac10 WHERE aac101='4' AND atc932=?::numeric AND atc900=?",
                            Int::class.java, r["aae003"], ygAtc900) ?: 0
                        if (nCnt == 0) {
                            if (!ins07(r)) nJsq = 0
                            if (nJsq == 1 && s.long(r["atc900"]) != s.long(t["atc900"])) {
                                try {
                                    s.jdbc.update("UPDATE tc07 SET atc900=? WHERE atc070=?",
                                        t["atc900"], r["atc070"])
                                } catch (e: Exception) {
                                    s.jdbc.update("DELETE FROM tc07 WHERE atc070=?", r["atc070"])
                                }
                            }
                            if (m["aae002_gz"] == null && s.nvl(a["aae002_zj"]).toLong() > nBjBegin) {
                                dlf += nJsq
                            }
                        }
                    }
                }
            }
        }
    }
    // 离职未参保 '49'(L6385-6470): ATB90R='5' 且 ATC90PB∈('2',NULL) 且 AAA130 省99
    if (s.str(tc90["atb90r"]) == "5" && s.nvl(tc90["aae003"]).toLong() <= aae002.toLong() &&
        (s.str(tc90["atc90pb"]) == "2" || s.str(tc90["atc90pb"]) == null) && prov130) {
        val ac10s = s.jdbc.queryForList(
            "SELECT * FROM ac10 WHERE atc900=?", atc900) // 源无 aac101 过滤(活性)
        for (a in ac10s) {
            val r = HashMap(t)
            r["atc072"] = "49"; r["aae003"] = a["atc932"]
            r["bba002"] = BigDecimal.ZERO
            r["bba004"] = s.nz(r["bba004"]).subtract(s.nz(a["bba004"]))
            r["bba005"] = BigDecimal.ZERO; r["atb90t"] = BigDecimal.ZERO
            r["baa002"] = BigDecimal.ZERO
            sums(r); ins07(r)
        }
        val tRows = s.jdbc.queryForList(
            """SELECT * FROM tc93 WHERE (COALESCE(baa200,0)+COALESCE(bba200,0)) <> 0
               AND atc931>=202012 AND atc900=?""", atc900)
        for (x in tRows) {
            val r = HashMap(t)
            r["atc072"] = "49"; r["aae003"] = x["atc931"]
            r["bba002"] = BigDecimal.ZERO
            r["bba004"] = s.nz(r["bba004"]).subtract(s.nz(x["bba004"]))
            r["bba005"] = BigDecimal.ZERO; r["atb90t"] = BigDecimal.ZERO
            r["baa002"] = BigDecimal.ZERO
            sums(r); ins07(r)
        }
    }
    return dlf
}

/** PRC_公积金补退明细(L6472-7504) */
fun TdSupplementCases.gongJijin(aae002: String, tc90: Map<String, Any?>, t: MutableMap<String, Any?>): Int {
    val nAae002 = s.monthAdd(aae002, -1)
    val atc900 = s.long(tc90["atc900"])!!
    val atb930 = s.long(tc90["atb930"])!!
    val aac001 = s.long(tc90["aac001"])!!
    val nBjBegin = bjBegin("gjj", atc900, atb930, aac001, aae002)
    var dlf = 0
    val a120 = s.str(tc90["aaa120"]) ?: ""
    val prov120 = a120.length >= 4 && a120.substring(2, 4) == "99"

    fun sums(r: MutableMap<String, Any?>) {
        r["bba200"] = s.nz(r["caa001"])
        r["baa200"] = s.nz(r["caa002"])
        r["atc9eh"] = s.nz(r["bba200"]).add(s.nz(r["baa200"]))
    }
    /** '31'/'32' 共用体: B(TC93) 驱动外连 TJ0D1(省)/TJ0E1(市), 窗 [from, to] */
    fun tingYi(t072: String, from: BigDecimal, to: BigDecimal, prov: Boolean) {
        val zTable = if (prov) "tj0d1" else "tj0e1"
        val bRows = s.jdbc.queryForList(
            """SELECT atc931 aae002_gz, caa001, caa002 FROM tc93 b
               WHERE b.atc937='1' AND b.aae214='1'
               AND b.atc931>=?::numeric AND b.atc931<=?::numeric AND b.atc900=?""",
            from, to, atc900)
        for (b in bRows) {
            val gzHj = s.nz(b["caa001"]).add(s.nz(b["caa002"]))
            val zs = s.jdbc.queryForList(
                """SELECT caa001, caa002 FROM $zTable WHERE aae002=?::numeric AND atj000='1'
                   AND aae013='1' AND atc900=?""", b["aae002_gz"], atc900)
            val pairs = if (zs.isEmpty()) listOf(mapOf<String, Any?>(
                "caa001" to null, "caa002" to null)) else zs
            for (z in pairs) {
                val zjHj = s.nz(z["caa001"]).add(s.nz(z["caa002"]))
                if (BigDecimal.ZERO.compareTo(zjHj) == 0 && zjHj.compareTo(gzHj) != 0) {
                    val r = HashMap(t)
                    r["aae003"] = b["aae002_gz"]; r["atc072"] = t072
                    r["caa001"] = s.nz(r["caa001"]).add(s.nz(z["caa001"])).subtract(s.nz(b["caa001"]))
                    r["caa002"] = s.nz(r["caa002"]).add(s.nz(z["caa002"])).subtract(s.nz(b["caa002"]))
                    sums(r); ins07(r)
                }
            }
        }
    }
    /** '33' 征缴与工资金额差异单循环: A(TJ0D1/TJ0E1) 驱动外连 TC93 */
    fun cha33(prov: Boolean, keyAtb930: Boolean): Int {
        var acc = 0
        val zTable = if (prov) "tj0d1" else "tj0e1"
        val ygRows = s.jdbc.queryForList(
            "SELECT * FROM tc90 WHERE atb930=? AND aac001=?", atb930, aac001)
        for (yg in ygRows) {
            val ygAtc900 = s.long(yg["atc900"])!!
            val aKey = if (keyAtb930) "atb930=? AND aac001=?" else "atc900=?"
            val aArgs = if (keyAtb930) arrayOf<Any?>(yg["atb930"], yg["aac001"]) else arrayOf<Any?>(ygAtc900)
            val aRows = s.jdbc.queryForList(
                """SELECT aae002 aae002_zj, caa001, caa002 FROM $zTable
                   WHERE aae002>=?::numeric AND aae002<=?::numeric AND atj000='1' AND aae013='1' AND $aKey""",
                *arrayOf<Any?>(nBjBegin.toBigDecimal(), nAae002.toBigDecimal()), *aArgs)
            for (a in aRows) {
                val zjHj = s.nz(a["caa001"]).add(s.nz(a["caa002"]))
                val bRows = s.jdbc.queryForList(
                    """SELECT atc931 aae002_gz, caa001, caa002 FROM tc93 b
                       WHERE b.atc937='1' AND b.aae214='1' AND b.atc931>=?::numeric
                         AND b.atc931<=?::numeric AND ${if (keyAtb930) "b.atb930=? AND b.aac001=?" else "b.atc900=?"}""",
                    *arrayOf<Any?>(nBjBegin.toBigDecimal(), nAae002.toBigDecimal()), *aArgs)
                val matches = bRows.filter {
                    s.nvl(it["aae002_gz"]).compareTo(s.nvl(a["aae002_zj"])) == 0
                }
                val diffs = if (matches.isEmpty()) listOf(mapOf<String, Any?>(
                    "aae002_gz" to null, "caa001" to null, "caa002" to null)) else matches.filter {
                    zjHj.compareTo(s.nz(it["caa001"]).add(s.nz(it["caa002"]))) != 0
                }
                for (m in diffs) {
                    var nJsq = 1
                    val r = HashMap(t)
                    r["atc900"] = yg["atc900"]; r["aae003"] = a["aae002_zj"]; r["atc072"] = "33"
                    r["caa001"] = s.nz(r["caa001"]).add(s.nz(a["caa001"])).subtract(s.nz(m["caa001"]))
                    r["caa002"] = s.nz(r["caa002"]).add(s.nz(a["caa002"])).subtract(s.nz(m["caa002"]))
                    sums(r)
                    val cnt = s.jdbc.queryForObject(
                        "SELECT COUNT(*) FROM ac10 WHERE aac101='3' AND atc932=?::numeric AND atc900=?",
                        Int::class.java, r["aae003"], ygAtc900) ?: 0
                    if (cnt == 0) {
                        if (!ins07(r)) nJsq = 0
                        if (nJsq == 1 && s.long(r["atc900"]) != s.long(t["atc900"])) {
                            try {
                                s.jdbc.update("UPDATE tc07 SET atc900=? WHERE atc070=?",
                                    t["atc900"], r["atc070"])
                            } catch (e: Exception) {
                                s.jdbc.update("DELETE FROM tc07 WHERE atc070=?", r["atc070"])
                            }
                        }
                        if (m["aae002_gz"] == null && s.nvl(a["aae002_zj"]).toLong() > nBjBegin) {
                            acc += nJsq
                        }
                    }
                }
            }
        }
        return acc
    }

    val gateProv = (s.str(tc90["atc90w"]) == "0" || s.str(tc90["atc90w"]) == "1") &&
        s.nvl(tc90["aaa508"]).toLong() >= nBjBegin && nBjBegin <= aae002.toLong() && prov120
    if (gateProv) {
        val a501 = s.str(tc90["aaa501"]); val a503 = s.str(tc90["aaa503"])
        // '30' 省 参保年月差异(L6546-6595)
        if (a501 != null && a503 != null && a501 < a503 && a501 < aae002) {
            val ksny = a501; val jsny = monthOrErr(a503, -1)
            val ac10s = s.jdbc.queryForList(
                "SELECT * FROM ac10 WHERE aac101='3' AND atc932>=?::numeric AND atc932<=?::numeric AND atc900=?",
                ksny.toBigDecimal(), jsny.toBigDecimal(), atc900)
            for (a in ac10s) {
                val r = HashMap(t)
                r["aae003"] = a["atc932"]; r["atc072"] = "30"
                r["caa001"] = s.nz(r["caa001"]).subtract(s.nz(a["caa001"]))
                r["caa002"] = s.nz(r["caa002"]).subtract(s.nz(a["caa002"]))
                sums(r); ins07(r)
            }
        }
        // '31' 省 参保异议退发(L6633-6710): AAA503>=AAE002, 窗下界=合同 AAE002
        if (a503 != null && a503 >= aae002) {
            tingYi("31", s.nvl(tc90["aae002"]), monthOrErr(a503, -1).toBigDecimal(), prov = true)
        }
    }
    // '36' 补发(L6713-6795): 门+AAA508>=202202(无省99 条件)
    if ((s.str(tc90["atc90w"]) == "0" || s.str(tc90["atc90w"]) == "1") &&
        s.nvl(tc90["aaa508"]).toLong() >= nBjBegin && nBjBegin <= aae002.toLong() &&
        s.nvl(tc90["aaa508"]).toLong() >= 202202) {
        val a501 = s.str(tc90["aaa501"]); val a503 = s.str(tc90["aaa503"])
        if (a501 != null && a503 != null && a501 > a503) {
            val months = buFaYue(tc90, aae002, a503, monthOrErr(a501, -1))
            var ym = a503!!
            for (tc01 in months) {
                val r = HashMap(t)
                r["aae003"] = ym.toBigDecimal(); r["atc072"] = "36"
                r["caa001"] = tc01.caa001; r["caa002"] = tc01.caa002
                sums(r); ins07(r)
                ym = s.monthAdd(ym, 1)
            }
        }
    }
    // '32' 省 停保(L6798-6880)
    if (s.str(tc90["atc90w"]) == "1" && s.nvl(tc90["aaa509"]).toLong() >= nBjBegin &&
        nBjBegin <= aae002.toLong() && prov120) {
        tingYi("32", s.str(tc90["aaa509"])!!.toBigDecimal(), nAae002.toBigDecimal(), prov = true)
    }
    // '33' 省 ×2(L6883-6996)
    dlf += cha33(prov = true, keyAtb930 = true)
    dlf += cha33(prov = true, keyAtb930 = false)
    // 市公积金(L6998-7165): 门≠'99'
    val gateShi = (s.str(tc90["atc90w"]) == "0" || s.str(tc90["atc90w"]) == "1") &&
        s.nvl(tc90["aaa508"]).toLong() >= nBjBegin && nBjBegin <= aae002.toLong() && !prov120
    if (gateShi) {
        val a501 = s.str(tc90["aaa501"]); val a503 = s.str(tc90["aaa503"])
        // '30' 市: JSNY 仅两分支(AAA503<N_AAE002→AAA503; =N_AAE002→prev; 其余 NULL→报错)
        if (a501 != null && a503 != null && a501 < a503 && a501 < aae002) {
            val ksny = a501
            val jsny: String? = when {
                a503.toLong() < nAae002.toLong() -> a503
                a503.toLong() == nAae002.toLong() -> monthOrErr(a503, -1)
                else -> null // 源: N_JSNY 未赋值 → NULL 守卫报错
            }
            if (ksny == null || jsny == null)
                throw SettleRet(-1, "无法计算补退，原因为计算补退起止年月出错")
            val ac10s = s.jdbc.queryForList(
                "SELECT * FROM ac10 WHERE atc932>=?::numeric AND atc932<=?::numeric AND atc900=?",
                ksny.toBigDecimal(), jsny.toBigDecimal(), atc900) // 源无 aac101 过滤(活性)
            for (a in ac10s) {
                val r = HashMap(t)
                r["aae003"] = a["atc932"]; r["atc072"] = "30"
                r["caa001"] = s.nz(r["caa001"]).subtract(s.nz(a["caa001"]))
                r["caa002"] = s.nz(r["caa002"]).subtract(s.nz(a["caa002"]))
                sums(r); ins07(r)
            }
        }
        // '31' 市 参保异议退发(L7080-7165)
        if (a503 != null && a503 >= aae002) {
            tingYi("31", s.nvl(tc90["aae002"]), monthOrErr(a503, -1).toBigDecimal(), prov = false)
        }
    }
    // '32' 市 停保(L7167-7245)
    if (s.str(tc90["atc90w"]) == "1" && s.nvl(tc90["aaa509"]).toLong() >= nBjBegin &&
        nBjBegin <= aae002.toLong() && !prov120) {
        tingYi("32", s.str(tc90["aaa509"])!!.toBigDecimal(), nAae002.toBigDecimal(), prov = false)
    }
    // '33' 市 ×2(L7248-7355)
    cha33(prov = false, keyAtb930 = true)
    cha33(prov = false, keyAtb930 = false)
    // 离职未参保 '39'(L7398-7458): ATB90R='5' 且 ATC90W∈('2',NULL)
    if (s.str(tc90["atb90r"]) == "5" && s.nvl(tc90["aae003"]).toLong() <= aae002.toLong() &&
        (s.str(tc90["atc90w"]) == "2" || s.str(tc90["atc90w"]) == null)) {
        val ac10s = s.jdbc.queryForList(
            "SELECT * FROM ac10 WHERE aac101='3' AND atc900=?", atc900)
        for (a in ac10s) {
            val r = HashMap(t)
            r["atc072"] = "39"; r["aae003"] = a["atc932"]
            r["caa001"] = s.nz(r["caa001"]).subtract(s.nz(a["caa001"]))
            r["caa002"] = s.nz(r["caa002"]).subtract(s.nz(a["caa002"]))
            sums(r); ins07(r)
        }
        val tRows = s.jdbc.queryForList(
            """SELECT * FROM tc93 WHERE (COALESCE(baa200,0)+COALESCE(bba200,0)) <> 0
               AND atc931>=202012 AND atc900=?""", atc900)
        for (x in tRows) {
            val r = HashMap(t)
            r["atc072"] = "39"; r["aae003"] = x["atc931"]
            r["caa001"] = s.nz(r["caa001"]).subtract(s.nz(x["caa001"]))
            r["caa002"] = s.nz(r["caa002"]).subtract(s.nz(x["caa002"]))
            sums(r); ins07(r)
        }
    }
    // '35' TC0A 补差(L7360-7407): NOT EXISTS 幂等闸, ABP00D/E
    val ygRows = s.jdbc.queryForList(
        "SELECT * FROM tc0a WHERE aae114='1' AND atb930=? AND aac001=?", atb930, aac001)
    for (yg in ygRows) {
        val zRows = s.jdbc.queryForList(
            """SELECT * FROM tc0a t WHERE NOT EXISTS (SELECT 1 FROM tc07
               WHERE atc072='35' AND aae003=t.aae002 AND aac001=t.aac001 AND aac001=?)
               AND aae114='1' AND aac001=?""",
            aac001, yg["aac001"])
        for (z in zRows) {
            val r = HashMap(t)
            r["aae003"] = z["aae002"]; r["atc072"] = "35"
            r["caa001"] = s.nz(r["caa001"]).add(s.nz(z["abp00d"]))
            r["caa002"] = s.nz(r["caa002"]).add(s.nz(z["abp00e"]))
            sums(r); ins07(r)
        }
    }
    return dlf
}
