package com.jlrc.rcpq.service.td

import java.math.BigDecimal

/**
 * PRC_社保补退明细(L3087-3773) 移植 — 桥单 20261127-041。
 * 分支: '10'参保年月差异(AC10 负冲) / '11'参保异议退发(TJ0A1×TC93 外连) /
 * '14'农民工(INSTR(AAC008,'27')) / '15'补发(获取补退基数+计算险金, 门 AAA208>=202202) /
 * '12'社保停保 / '13'工资与征缴差异(双循环: ATB930+AAC001 键×2 与 ATC900 键×2, 含
 * ATC900 重指派怪笔+代理费月数计数) / '19'离职未参保三段(AC10 负冲+TJ0A2 基数补差
 * (BBA200:=ATJ0A1 直取)+TC93 退按月收取)。
 * 外连语义(Oracle (+) 在 A 侧=保留 B): B 全行×按月找 A, A 缺=ZJ 全零; NVL(ZJHJ,0)=0 且
 * ≠NVL(GZHJ,0) 才产出('11'/'12'); '13' 反向(保留 A, B 缺=GZ 全零+计代理费月数)。
 */
fun TdSupplementCases.sheBao(aae002: String, tc90: Map<String, Any?>, t: MutableMap<String, Any?>): Int {
    val nAae002 = s.monthAdd(aae002, -1)
    val atc900 = s.long(tc90["atc900"])!!
    val atb930 = s.long(tc90["atb930"])!!
    val aac001 = s.long(tc90["aac001"])!!
    val nBjBegin = bjBegin("sb", atc900, atb930, aac001, aae002)
    val nBj16 = bjN("16", atb930, aac001)
    var dlf = 0
    val gate1 = (s.str(tc90["atc90p"]) == "0" || s.str(tc90["atc90p"]) == "1") &&
        s.nvl(tc90["aaa208"]).toLong() >= nBjBegin && nBjBegin <= aae002.toLong()

    fun sums(r: MutableMap<String, Any?>) {
        r["bba200"] = s.nz(r["bba001"]).add(s.nz(r["bba003"]))
        r["baa200"] = s.nz(r["baa001"]).add(s.nz(r["baa003"]))
        r["atc9eh"] = s.nz(r["bba200"]).add(s.nz(r["baa200"]))
    }

    if (gate1) {
        // '10' 参保年月差异(L3146-3200)
        val a201 = s.str(tc90["aaa201"]); val a203 = s.str(tc90["aaa203"])
        if (a201 != null && a203 != null && a201 < a203 && a201 < aae002) {
            val ksny = a201; val jsny = monthOrErr(a203, -1)
            val ac10s = s.jdbc.queryForList(
                "SELECT * FROM ac10 WHERE aac101='1' AND atc932>=?::numeric AND atc932<=?::numeric AND atc900=?",
                ksny.toBigDecimal(), jsny.toBigDecimal(), atc900)
            for (a in ac10s) {
                val r = HashMap(t)
                r["aae003"] = a["atc932"]; r["atc072"] = "10"
                r["bba001"] = s.nz(r["bba001"]).subtract(s.nz(a["bba001"]))
                r["bba003"] = s.nz(r["bba003"]).subtract(s.nz(a["bba003"]))
                r["baa001"] = s.nz(r["baa001"]).subtract(s.nz(a["baa001"]))
                r["baa003"] = s.nz(r["baa003"]).subtract(s.nz(a["baa003"]))
                sums(r); ins07(r)
            }
        }
        // '11' 参保异议退发(L3202-3288): AAA203>=AAE002, TJ0A1(缴)×TC93(发) 外连零缴非零发
        if (a203 != null && a203 >= aae002) {
            val win = s.monthAdd(a203, -1)
            val bRows = s.jdbc.queryForList(
                """SELECT atc931 aae002_gz, bba001, bba003, baa001, baa003 FROM tc93 b
                   WHERE b.atc937='1' AND b.aae214='1' AND b.atc931<=?::numeric AND b.atc900=?""",
                win.toBigDecimal(), atc900)
            for (b in bRows) {
                val gz = s.nz(b["bba001"]).add(s.nz(b["bba003"])).add(s.nz(b["baa001"])).add(s.nz(b["baa003"]))
                val zs = s.jdbc.queryForList(
                    """SELECT atj0a7, bba001, bba003, baa001, baa003 FROM tj0a1
                       WHERE aae002=?::numeric AND atj000='1' AND aae013='1' AND atc900=?""",
                    b["aae002_gz"], atc900)
                if (zs.isEmpty()) {
                    if (BigDecimal.ZERO.compareTo(gz) != 0) {
                        val r = HashMap(t)
                        r["aae003"] = b["aae002_gz"]; r["atc072"] = "11"
                        r["bba001"] = BigDecimal.ZERO.subtract(gz); r["bba003"] = BigDecimal.ZERO
                        r["baa001"] = BigDecimal.ZERO; r["baa003"] = BigDecimal.ZERO
                        sums(r); ins07(r)
                    }
                    continue
                }
                for (z in zs) {
                    val zj = s.nz(z["atj0a7"])
                    if (BigDecimal.ZERO.compareTo(zj) == 0 && zj.compareTo(gz) != 0) {
                        val r = HashMap(t)
                        r["aae003"] = b["aae002_gz"]; r["atc072"] = "11"
                        r["bba001"] = s.nz(z["bba001"]).subtract(s.nz(b["bba001"]))
                        r["bba003"] = s.nz(z["bba003"]).subtract(s.nz(b["bba003"]))
                        r["baa001"] = s.nz(z["baa001"]).subtract(s.nz(b["baa001"]))
                        r["baa003"] = s.nz(z["baa003"]).subtract(s.nz(b["baa003"]))
                        sums(r); ins07(r)
                    }
                }
            }
        }
        // '14' 农民工(L3290-3346)
        if ((s.str(tc90["aac008"]) ?: "").contains("27")) {
            val ksny = a203; val jsny = monthOrErr(nAae002, -1)
            if (ksny == null) throw SettleRet(-1, "无法计算补退，原因为计算补退起止年月出错")
            val ac10s = s.jdbc.queryForList(
                "SELECT * FROM ac10 WHERE atc932>=?::numeric AND atc932<=?::numeric AND atc900=?",
                ksny.toBigDecimal(), jsny.toBigDecimal(), atc900)
            for (a in ac10s) {
                val r = HashMap(t)
                r["aae003"] = a["atc932"]; r["atc072"] = "14"
                r["baa003"] = s.nz(a["baa003"]).negate()
                sums(r)
                if (s.nz(r["atc9eh"]).compareTo(BigDecimal.ZERO) != 0) ins07(r)
            }
        }
    }
    // '15' 补发(L3352-3448): 门+AAA208>=202202, AAA201>AAA203
    if (gate1 && s.nvl(tc90["aaa208"]).toLong() >= 202202) {
        val a201 = s.str(tc90["aaa201"]); val a203 = s.str(tc90["aaa203"])
        if (a201 != null && a203 != null && a201 > a203) {
            val months = buFaYue(tc90, aae002, a203, monthOrErr(a201, -1))
            var ym = a203!!
            for (tc01 in months) {
                val r = HashMap(t)
                r["aae003"] = ym.toBigDecimal(); r["atc072"] = "15"
                r["bba001"] = tc01.bba001; r["bba003"] = tc01.bba003
                r["baa001"] = tc01.baa001; r["baa003"] = tc01.baa003
                sums(r); ins07(r)
                ym = s.monthAdd(ym, 1)
            }
        }
    }
    // '12' 社保停保(L3452-3530): ATC90P='1' 且 AAA209 达窗
    if (s.str(tc90["atc90p"]) == "1" && s.nvl(tc90["aaa209"]).toLong() >= nBjBegin &&
        nBjBegin <= aae002.toLong()) {
        val a209 = s.str(tc90["aaa209"])!!
        val bRows = s.jdbc.queryForList(
            """SELECT atc931 aae002_gz, bba001, bba003, baa001, baa003 FROM tc93 b
               WHERE b.atc937='1' AND b.aae214='1' AND b.atc931>=?::numeric
                 AND b.atc931<=?::numeric AND b.atc900=?""",
            a209.toBigDecimal(), nAae002.toBigDecimal(), atc900)
        for (b in bRows) {
            val gz = s.nz(b["bba001"]).add(s.nz(b["bba003"])).add(s.nz(b["baa001"])).add(s.nz(b["baa003"]))
            val zs = s.jdbc.queryForList(
                """SELECT atj0a7, bba001, bba003, baa001, baa003 FROM tj0a1
                   WHERE aae002=?::numeric AND atj000='1' AND aae013='1' AND atc900=?""",
                b["aae002_gz"], atc900)
            val pairs = if (zs.isEmpty()) listOf(null to b) else zs.map { it to b }
            for ((z, b2) in pairs) {
                val zj = z?.let { s.nz(it["atj0a7"]) } ?: BigDecimal.ZERO
                if (BigDecimal.ZERO.compareTo(zj) == 0 && zj.compareTo(gz) != 0) {
                    val r = HashMap(t)
                    r["aae003"] = b2["aae002_gz"]; r["atc072"] = "12"
                    r["bba001"] = (z?.let { s.nz(it["bba001"]) } ?: BigDecimal.ZERO).subtract(s.nz(b2["bba001"]))
                    r["bba003"] = (z?.let { s.nz(it["bba003"]) } ?: BigDecimal.ZERO).subtract(s.nz(b2["bba003"]))
                    r["baa001"] = (z?.let { s.nz(it["baa001"]) } ?: BigDecimal.ZERO).subtract(s.nz(b2["baa001"]))
                    r["baa003"] = (z?.let { s.nz(it["baa003"]) } ?: BigDecimal.ZERO).subtract(s.nz(b2["baa003"]))
                    sums(r); ins07(r)
                }
            }
        }
    }
    // '13' 工资与征缴差异 — 双循环(L3450-3634): pass1 键 ATB930+AAC001, pass2 键 ATC900
    for (pass in 1..2) {
        val ygRows = s.jdbc.queryForList(
            "SELECT * FROM tc90 WHERE atb930=? AND aac001=? ORDER BY atc900", atb930, aac001)
        for (yg in ygRows) {
            val ygAtc900 = s.long(yg["atc900"])!!
            val aRows = s.jdbc.queryForList(
                """SELECT aae002 aae002_zj, atj0a7, atj0a5, atj0a6, bba001, bba003, baa001, baa003
                   FROM tj0a1 WHERE aae002>=?::numeric AND aae002<=?::numeric
                   AND atj000='1' AND aae013='1' AND ${if (pass == 1) "atb930=? AND aac001=?" else "atc900=?"}""",
                nBjBegin.toBigDecimal(), nAae002.toBigDecimal(),
                *(if (pass == 1) arrayOf(yg["atb930"], yg["aac001"]) else arrayOf(ygAtc900)))
            for (a in aRows) {
                val zj = s.nz(a["atj0a7"]).add(s.nz(a["atj0a5"])).add(s.nz(a["atj0a6"]))
                val zBba001 = s.nz(a["bba001"]).add(s.nz(a["atj0a5"]))
                val zBba003 = s.nz(a["bba003"]).add(s.nz(a["atj0a6"]))
                val bRows = s.jdbc.queryForList(
                    """SELECT atc931 aae002_gz, bba001, bba003, baa001, baa003 FROM tc93 b
                       WHERE b.atc937='1' AND b.aae214='1' AND b.atc931>=?::numeric
                         AND b.atc931<=?::numeric AND ${if (pass == 1) "b.atb930=? AND b.aac001=?" else "b.atc900=?"}""",
                    nBjBegin.toBigDecimal(), nAae002.toBigDecimal(),
                    *(if (pass == 1) arrayOf(yg["atb930"], yg["aac001"]) else arrayOf(ygAtc900)))
                val matches = bRows.filter {
                    s.nvl(it["aae002_gz"]).compareTo(s.nvl(a["aae002_zj"])) == 0
                }
                val diffs = if (matches.isEmpty()) listOf(mapOf<String, Any?>(
                    "aae002_gz" to null, "bba001" to null, "bba003" to null,
                    "baa001" to null, "baa003" to null)) else matches.filter { m ->
                    val gz = s.nz(m["bba001"]).add(s.nz(m["bba003"])).add(s.nz(m["baa001"])).add(s.nz(m["baa003"]))
                    zj.compareTo(gz) != 0
                }
                for (m in diffs) {
                    var nJsq = 1
                    val r = HashMap(t)
                    r["aae003"] = a["aae002_zj"]; r["atc900"] = yg["atc900"]
                    r["atc072"] = "13"
                    r["bba001"] = zBba001.subtract(s.nz(m["bba001"]))
                    r["bba003"] = zBba003.subtract(s.nz(m["bba003"]))
                    r["baa001"] = s.nz(a["baa001"]).subtract(s.nz(m["baa001"]))
                    r["baa003"] = s.nz(a["baa003"]).subtract(s.nz(m["baa003"]))
                    sums(r)
                    val cnt = s.jdbc.queryForObject(
                        "SELECT COUNT(*) FROM ac10 WHERE aac101='1' AND atc932=?::numeric AND atc900=?",
                        Int::class.java, r["aae003"], ygAtc900) ?: 0
                    if (cnt == 0) {
                        if (!ins07(r)) nJsq = 0 // 源 DUP→N_JSQ:=0(实践不可达: PK)
                        if (nJsq == 1 && s.long(r["atc900"]) != s.long(t["atc900"])) {
                            try {
                                s.jdbc.update("UPDATE tc07 SET atc900=? WHERE atc070=?",
                                    t["atc900"], r["atc070"])
                            } catch (e: Exception) {
                                // 源: UPDATE 失败→DELETE 该行+OK(实践不可达: PK 定位更新)
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
    // 离职未参保(L3636-3772): ATB90R='5' 且 AAE003<=结算月 且 ATC90P∈('2',NULL)
    if (s.str(tc90["atb90r"]) == "5" && s.nvl(tc90["aae003"]).toLong() <= aae002.toLong() &&
        (s.str(tc90["atc90p"]) == "2" || s.str(tc90["atc90p"]) == null)) {
        // '19' AC10 负冲
        val ac10s = s.jdbc.queryForList(
            "SELECT * FROM ac10 WHERE aac101='1' AND atc900=?", atc900)
        for (a in ac10s) {
            val r = HashMap(t)
            r["atc072"] = "19"; r["aae003"] = a["atc932"]
            r["bba001"] = s.nz(a["bba001"]).negate(); r["bba003"] = s.nz(a["bba003"]).negate()
            r["baa001"] = s.nz(a["baa001"]).negate(); r["baa003"] = s.nz(a["baa003"]).negate()
            sums(r); ins07(r)
        }
        // 社保基数补差(TJ0A2): BBA200:=R.ATJ0A1 直取(源活性)
        val ygRows = s.jdbc.queryForList(
            "SELECT * FROM tc90 WHERE atb930=? AND aac001=?", atb930, aac001)
        for (yg in ygRows) {
            val zRows = s.jdbc.queryForList(
                """SELECT * FROM tj0a2 WHERE aaezzz='1' AND aae003>?::numeric
                   AND aae002>=202201 AND atc900=?""",
                nBj16.toBigDecimal(), s.long(yg["atc900"]))
            for (z in zRows) {
                val r = HashMap(t)
                r["aae003"] = z["aae003"]; r["atc900"] = yg["atc900"]; r["atc072"] = "16"
                r["bba001"] = s.nz(z["bba001"]); r["baa001"] = s.nz(z["baa001"])
                r["bba003"] = s.nz(z["bba003"]); r["baa003"] = s.nz(z["baa003"])
                r["bba200"] = s.nz(z["atj0a1"]); r["baa200"] = s.nz(z["atj0a2"])
                r["atc9eh"] = s.nz(z["atj0a3"])
                ins07(r)
                if (s.long(r["atc900"]) != s.long(t["atc900"])) {
                    try {
                        s.jdbc.update("UPDATE tc07 SET atc900=? WHERE atc070=?", t["atc900"], r["atc070"])
                    } catch (e: Exception) {
                        s.jdbc.update("DELETE FROM tc07 WHERE atc070=?", r["atc070"])
                    }
                }
            }
        }
        // 退按月收取(TC93): 和≠0, >=202012, >=AAA201, AAC001 键
        val tRows = s.jdbc.queryForList(
            """SELECT * FROM tc93 WHERE (COALESCE(baa200,0)+COALESCE(bba200,0)) <> 0
               AND atc931>=202012 AND atc931>=?::numeric AND aac001=?""",
            s.nvl(tc90["aaa201"]), aac001)
        for (x in tRows) {
            val r = HashMap(t)
            r["atc072"] = "19"; r["aae003"] = x["atc931"]
            r["bba001"] = s.nz(x["bba001"]).negate(); r["bba003"] = s.nz(x["bba003"]).negate()
            r["baa001"] = s.nz(x["baa001"]).negate(); r["baa003"] = s.nz(x["baa003"]).negate()
            sums(r)
            if (s.nz(r["atc9eh"]).compareTo(BigDecimal.ZERO) != 0) ins07(r)
        }
    }
    return dlf
}
