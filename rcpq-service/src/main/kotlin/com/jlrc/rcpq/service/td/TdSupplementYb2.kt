package com.jlrc.rcpq.service.td

import java.math.BigDecimal

/**
 * 医保补退明细拆分件 — '22'停保体 + '23'征缴与工资差异四循环(桥单 20261127-041)。
 * '22': B(TC93) 驱动外连 TJ0B1(省)/TJ0C1(市); 省 BBA004 按 GZ 月<202105 条件,
 *       市 BBA004 按 GZ 月>=202107 置零; 省 ATB90T 参与, 市不动 ATB90T(BBA200 亦不含)。
 * '23': A(征缴明细) 驱动外连 TC93; 省 ZJHJ 含 202105 CASE+月内 SUM(DAA001) 子查询,
 *       DAA001_ZJ=SUM 子查询; 市 ZJHJ 无 CASE 无 ATB90T, DAA001_ZJ=行值,
 *       **GZHJ 双计 ATB90T(源码笔误忠实)**; 省 BBA004 按 GZ 月<202105(GZ NULL→NULL 比较
 *       非真→置零), 市 BBA004 按行月>=202107 置零; 省 ATB90T_ZJ=A 值/市 ATB90T_ZJ=0;
 *       省: 无零和门+计代理费; 市: ATC9EH≠0 门+不计数。
 */
internal fun TdSupplementCases.ybTingBui22(
    t: MutableMap<String, Any?>, tc90: Map<String, Any?>,
    a609: String, nAae002: String, prov: Boolean, atc900: Long,
) {
    fun caseHj(ym: Any?, b2: Any?, b4: Any?, b5: Any?, b0: Any?, t0: Any?, d1: Any?): BigDecimal {
        val before = ym != null && s.nvl(ym).toLong() < 202105
        var v = s.nz(b2).add(s.nz(b5)).add(s.nz(b0)).add(s.nz(t0)).add(s.nz(d1))
        if (before) v = v.add(s.nz(b4))
        return v
    }
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
    val zTable = if (prov) "tj0b1" else "tj0c1"
    val zCols = if (prov) "bba002, bba004, bba005, atb90t, baa002, daa001"
                else "bba002, bba004, bba005, baa002, daa001"
    val bRows = s.jdbc.queryForList(
        """SELECT atc931 aae002_gz, bba002, bba004, bba005, atb90t, baa002,
                  COALESCE(atc93bc,0)+COALESCE(atc93bd,0) daa001_gz
           FROM tc93 b WHERE b.atc937='1' AND b.aae214='1'
           AND b.atc931>=?::numeric AND b.atc931<=?::numeric AND b.atc900=?""",
        a609.toBigDecimal(), nAae002.toBigDecimal(), atc900)
    for (b in bRows) {
        val gzHj = if (prov) caseHj(b["aae002_gz"], b["bba002"], b["bba004"], b["bba005"],
            b["baa002"], b["atb90t"], b["daa001_gz"])
        else s.nz(b["bba002"]).add(s.nz(b["bba004"])).add(s.nz(b["bba005"]))
            .add(s.nz(b["baa002"])).add(s.nz(b["atb90t"])).add(s.nz(b["daa001_gz"]))
        val zs = s.jdbc.queryForList(
            "SELECT $zCols FROM $zTable WHERE aae002=?::numeric AND atj000='1' AND aae013='1' AND atc900=?",
            b["aae002_gz"], atc900)
        val pairs = if (zs.isEmpty()) listOf(mapOf<String, Any?>(
            "bba002" to null, "bba004" to null, "bba005" to null,
            "atb90t" to null, "baa002" to null, "daa001" to null)) else zs
        for (z in pairs) {
            val zjHj = if (prov) caseHj(b["aae002_gz"], z["bba002"], z["bba004"], z["bba005"],
                z["baa002"], z["atb90t"], z["daa001"])
            else s.nz(z["bba002"]).add(s.nz(z["bba004"])).add(s.nz(z["bba005"]))
                .add(s.nz(z["baa002"])).add(s.nz(z["daa001"]))
            if (BigDecimal.ZERO.compareTo(zjHj) == 0 && zjHj.compareTo(gzHj) != 0) {
                val r = HashMap(t)
                r["aae003"] = b["aae002_gz"]; r["atc072"] = "22"
                r["bba002"] = s.nz(r["bba002"]).add(s.nz(z["bba002"])).subtract(s.nz(b["bba002"]))
                r["bba004"] = if (prov) {
                    if (s.nvl(b["aae002_gz"]).toLong() < 202105)
                        s.nz(r["bba004"]).add(s.nz(z["bba004"])).subtract(s.nz(b["bba004"]))
                    else BigDecimal.ZERO
                } else {
                    if (s.nvl(b["aae002_gz"]).toLong() >= 202107) BigDecimal.ZERO
                    else s.nz(r["bba004"]).add(s.nz(z["bba004"])).subtract(s.nz(b["bba004"]))
                }
                r["bba005"] = s.nz(r["bba005"]).add(s.nz(z["bba005"])).subtract(s.nz(b["bba005"]))
                r["baa002"] = s.nz(r["baa002"]).add(s.nz(z["baa002"])).subtract(s.nz(b["baa002"]))
                if (prov) r["atb90t"] = s.nz(r["atb90t"]).add(s.nz(z["atb90t"])).subtract(s.nz(b["atb90t"]))
                daa(r, s.nz(z["daa001"]), s.nz(b["daa001_gz"]))
                if (prov) {
                    r["bba200"] = s.nz(r["bba002"]).add(s.nz(r["bba004"])).add(s.nz(r["bba005"]))
                        .add(s.nz(r["atb90t"])).add(s.nz(r["daa002"]))
                } else {
                    r["bba200"] = s.nz(r["bba002"]).add(s.nz(r["bba004"])).add(s.nz(r["bba005"]))
                        .add(s.nz(r["daa002"]))
                }
                r["baa200"] = s.nz(r["baa002"]).add(s.nz(r["daa001"]))
                r["atc9eh"] = s.nz(r["bba200"]).add(s.nz(r["baa200"]))
                ins07(r)
            }
        }
    }
}

/** '23' 征缴与工资差异单循环 — prov(省,TJ0B1)/市(TJ0C1) × keyAtb930(键 ATB930+AAC001/ATC900) */
internal fun TdSupplementCases.ybCha23(
    t: MutableMap<String, Any?>, tc90: Map<String, Any?>, aae002: String,
    nBjBegin: Long, nAae002: String, prov: Boolean, keyAtb930: Boolean,
): Int {
    var dlf = 0
    val atb930 = s.long(tc90["atb930"])!!
    val aac001 = s.long(tc90["aac001"])!!
    val zTable = if (prov) "tj0b1" else "tj0c1"
    fun caseHj(ym: Any?, b2: Any?, b4: Any?, b5: Any?, b0: Any?, t0: Any?, d1: Any?): BigDecimal {
        val before = ym != null && s.nvl(ym).toLong() < 202105
        var v = s.nz(b2).add(s.nz(b5)).add(s.nz(b0)).add(s.nz(t0)).add(s.nz(d1))
        if (before) v = v.add(s.nz(b4))
        return v
    }
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
    val ygRows = s.jdbc.queryForList("SELECT * FROM tc90 WHERE atb930=? AND aac001=?", atb930, aac001)
    for (yg in ygRows) {
        val ygAtc900 = s.long(yg["atc900"])!!
        val aKey = if (keyAtb930) "atb930=? AND aac001=?" else "atc900=?"
        val aArgs = if (keyAtb930) arrayOf<Any?>(yg["atb930"], yg["aac001"]) else arrayOf<Any?>(ygAtc900)
        val zCols = if (prov) "bba002, bba004, bba005, atb90t, baa002"
                    else "bba002, bba004, bba005, baa002, daa001"
        val aRows = s.jdbc.queryForList(
            """SELECT aae002 aae002_zj, $zCols FROM $zTable
               WHERE aae002>=?::numeric AND aae002<=?::numeric AND atj000='1' AND aae013='1' AND $aKey""",
            *arrayOf<Any?>(nBjBegin.toBigDecimal(), nAae002.toBigDecimal()), *aArgs)
        for (a in aRows) {
            // 省 ZJ 月内 SUM(DAA001) 子查询(无 atj000/aae013 过滤)
            val sumSub = if (prov) s.jdbc.queryForObject(
                """SELECT COALESCE(SUM(daa001),0) FROM tj0b1
                   WHERE aae002=?::numeric AND ${if (keyAtb930) "atb930=? AND aac001=?" else "atc900=?"}""",
                BigDecimal::class.java,
                *arrayOf<Any?>(a["aae002_zj"]), *aArgs) else BigDecimal.ZERO
            val daa001Zj = if (prov) s.nz(sumSub) else s.nz(a["daa001"])
            val zjHj = if (prov) caseHj(a["aae002_zj"], a["bba002"], a["bba004"], a["bba005"],
                a["baa002"], a["atb90t"], sumSub)
            else s.nz(a["bba002"]).add(s.nz(a["bba004"])).add(s.nz(a["bba005"]))
                .add(s.nz(a["baa002"])).add(s.nz(a["daa001"]))
            val bRows = s.jdbc.queryForList(
                """SELECT atc931 aae002_gz, bba002, bba004, bba005, atb90t, baa002,
                          COALESCE(atc93bc,0)+COALESCE(atc93bd,0) daa001_gz
                   FROM tc93 b WHERE b.atc937='1' AND b.aae214='1'
                   AND b.atc931>=?::numeric AND b.atc931<=?::numeric AND ${if (keyAtb930) "b.atb930=? AND b.aac001=?" else "b.atc900=?"}""",
                *arrayOf<Any?>(nBjBegin.toBigDecimal(), nAae002.toBigDecimal()), *aArgs)
            val matches = bRows.filter { s.nvl(it["aae002_gz"]).compareTo(s.nvl(a["aae002_zj"])) == 0 }
            val diffs = if (matches.isEmpty()) listOf(mapOf<String, Any?>(
                "aae002_gz" to null, "bba002" to null, "bba004" to null, "bba005" to null,
                "atb90t" to null, "baa002" to null, "daa001_gz" to null)) else matches.filter { m ->
                val gzHj = if (prov) caseHj(m["aae002_gz"], m["bba002"], m["bba004"], m["bba005"],
                    m["baa002"], m["atb90t"], m["daa001_gz"])
                else s.nz(m["bba002"]).add(s.nz(m["bba004"])).add(s.nz(m["bba005"]))
                    .add(s.nz(m["baa002"])).add(s.nz(m["atb90t"])).add(s.nz(m["daa001_gz"]))
                    .add(s.nz(m["atb90t"])) // 源码笔误: ATB90T 双计
                zjHj.compareTo(gzHj) != 0
            }
            for (m in diffs) {
                var nJsq = 1
                val r = HashMap(t)
                r["atc900"] = yg["atc900"]; r["aae003"] = a["aae002_zj"]; r["atc072"] = "23"
                r["bba002"] = s.nz(r["bba002"]).add(s.nz(a["bba002"])).subtract(s.nz(m["bba002"]))
                r["bba004"] = if (prov) {
                    // 源: IF R.AAE002_GZ<202055... <202105 — GZ NULL→比较非真→置零
                    if (m["aae002_gz"] != null && s.nvl(m["aae002_gz"]).toLong() < 202105)
                        s.nz(r["bba004"]).add(s.nz(a["bba004"])).subtract(s.nz(m["bba004"]))
                    else BigDecimal.ZERO
                } else {
                    if (s.nvl(a["aae002_zj"]).toLong() >= 202107) BigDecimal.ZERO
                    else s.nz(r["bba004"]).add(s.nz(a["bba004"])).subtract(s.nz(m["bba004"]))
                }
                r["bba005"] = s.nz(r["bba005"]).add(s.nz(a["bba005"])).subtract(s.nz(m["bba005"]))
                r["baa002"] = s.nz(r["baa002"]).add(s.nz(a["baa002"])).subtract(s.nz(m["baa002"]))
                r["atb90t"] = if (prov)
                    s.nz(r["atb90t"]).add(s.nz(a["atb90t"])).subtract(s.nz(m["atb90t"]))
                else BigDecimal.ZERO.subtract(s.nz(m["atb90t"])) // 市: ATB90T_ZJ=0 显式
                daa(r, daa001Zj, s.nz(m["daa001_gz"]))
                if (prov) {
                    r["bba200"] = s.nz(r["bba002"]).add(s.nz(r["bba004"])).add(s.nz(r["bba005"]))
                        .add(s.nz(r["atb90t"])).add(s.nz(r["daa002"]))
                } else {
                    r["bba200"] = s.nz(r["bba002"]).add(s.nz(r["bba004"])).add(s.nz(r["bba005"]))
                        .add(s.nz(r["daa002"])).add(s.nz(r["atb90t"]))
                }
                r["baa200"] = s.nz(r["baa002"]).add(s.nz(r["daa001"]))
                r["atc9eh"] = s.nz(r["bba200"]).add(s.nz(r["baa200"]))
                val cnt = s.jdbc.queryForObject(
                    "SELECT COUNT(*) FROM ac10 WHERE aac101='2' AND atc932=?::numeric AND atc900=?",
                    Int::class.java, r["aae003"], ygAtc900) ?: 0
                if (cnt == 0) {
                    if (prov) {
                        if (!ins07(r)) nJsq = 0 // 源 DUP→N_JSQ:=0(实践不可达: PK)
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
                    } else {
                        // 市 '23': ATC9EH≠0 门, 无代理费计数
                        if (s.nz(r["atc9eh"]).compareTo(BigDecimal.ZERO) != 0) {
                            if (ins07(r) && s.long(r["atc900"]) != s.long(t["atc900"])) {
                                try {
                                    s.jdbc.update("UPDATE tc07 SET atc900=? WHERE atc070=?",
                                        t["atc900"], r["atc070"])
                                } catch (e: Exception) {
                                    s.jdbc.update("DELETE FROM tc07 WHERE atc070=?", r["atc070"])
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    return dlf
}
