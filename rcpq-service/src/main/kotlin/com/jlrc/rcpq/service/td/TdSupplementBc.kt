package com.jlrc.rcpq.service.td

import java.math.BigDecimal

/**
 * PRC_保险补差明细(L7506-7823) 移植 — 桥单 20261127-041。
 * 四段基数补差(逐 R_YG 合同×源表, NOT EXISTS TC07 同码同月幂等闸):
 * '16'TJ0A2(社保四列)/'24'TJ0B2(医疗五列)/'25'TJ0B3(ATJ0B5→BBA004)/
 * '26'TC09(省工伤减免, BBA004 负冲)。
 * 源死句 IF ATC900=33810 THEN PRM_CODE:=DEF_OK 无操作不移植; 离职未参保注释块后
 * 无数据循环; PRM_DLFYS 恒 0(编排体 N_DLF_3 复写源笔误, 返回值被编排体弃用)。
 */
fun TdSupplementCases.buCha(aae002: String, tc90: Map<String, Any?>, t: MutableMap<String, Any?>): Int {
    val atb930 = s.long(tc90["atb930"])!!
    val aac001 = s.long(tc90["aac001"])!!
    val nBj16 = bjN("16", atb930, aac001)
    val nBj24 = bjN("24", atb930, aac001)
    val nBj25 = bjN("25", atb930, aac001)
    val nBj26 = bjN("26", atb930, aac001)
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
            """SELECT * FROM tj0a2 a WHERE aaezzz='1' AND aae003>?::numeric
               AND aae002>=202201 AND atc900=?
               AND NOT EXISTS (SELECT 1 FROM tc07 WHERE atc072='16'
                     AND aae003=a.aae003 AND atc900=?)""",
            nBj16.toBigDecimal(), s.long(yg["atc900"]), s.long(yg["atc900"]))
        for (z in zRows) {
            val r = HashMap(t)
            r["atc900"] = yg["atc900"]; r["aae003"] = z["aae003"]; r["atc072"] = "16"
            r["bba001"] = s.nz(r["bba001"]).add(s.nz(z["bba001"]))
            r["baa001"] = s.nz(r["baa001"]).add(s.nz(z["baa001"]))
            r["bba003"] = s.nz(r["bba003"]).add(s.nz(z["bba003"]))
            r["baa003"] = s.nz(r["baa003"]).add(s.nz(z["baa003"]))
            r["bba200"] = s.nz(r["bba001"]).add(s.nz(r["bba003"]))
            r["baa200"] = s.nz(r["baa001"]).add(s.nz(r["baa003"]))
            r["atc9eh"] = s.nz(r["bba200"]).add(s.nz(r["baa200"]))
            if (ins07(r)) reassign(r)
        }
    }
    for (yg in ygRows) {
        val zRows = s.jdbc.queryForList(
            """SELECT * FROM tj0b2 a WHERE aaezzz='1' AND aae003>?::numeric
               AND aae002>=202201 AND atc900=?
               AND NOT EXISTS (SELECT 1 FROM tc07 WHERE atc072='24'
                     AND aae003=a.aae003 AND atc900=?)""",
            nBj24.toBigDecimal(), s.long(yg["atc900"]), s.long(yg["atc900"]))
        for (z in zRows) {
            val r = HashMap(t)
            r["atc900"] = yg["atc900"]; r["aae003"] = z["aae003"]; r["atc072"] = "24"
            r["bba002"] = s.nz(r["bba002"]).add(s.nz(z["bba002"]))
            r["bba004"] = s.nz(r["bba004"]).add(s.nz(z["bba004"]))
            r["bba005"] = s.nz(r["bba005"]).add(s.nz(z["bba005"]))
            r["atb90t"] = s.nz(r["atb90t"]).add(s.nz(z["atb90t"]))
            r["baa002"] = s.nz(r["baa002"]).add(s.nz(z["baa002"]))
            r["bba200"] = s.nz(r["bba002"]).add(s.nz(r["bba004"])).add(s.nz(r["bba005"]))
                .add(s.nz(r["atb90t"])).add(s.nz(r["daa002"]))
            r["baa200"] = s.nz(r["baa002"]).add(s.nz(r["daa001"]))
            r["atc9eh"] = s.nz(r["bba200"]).add(s.nz(r["baa200"]))
            if (ins07(r)) reassign(r)
        }
    }
    for (yg in ygRows) {
        val zRows = s.jdbc.queryForList(
            """SELECT * FROM tj0b3 a WHERE aaezzz='1' AND aae003>?::numeric
               AND aae002>=202201 AND atc900=?
               AND NOT EXISTS (SELECT 1 FROM tc07 WHERE atc072='25'
                     AND aae003=a.aae003 AND atc900=?)""",
            nBj25.toBigDecimal(), s.long(yg["atc900"]), s.long(yg["atc900"]))
        for (z in zRows) {
            val r = HashMap(t)
            r["aae003"] = z["aae003"]; r["atc900"] = yg["atc900"]; r["atc072"] = "25"
            r["bba002"] = BigDecimal.ZERO
            r["bba004"] = BigDecimal.ZERO +
                s.nz(r["bba004"]).add(s.nz(z["atj0b5"]))
            r["bba005"] = BigDecimal.ZERO
            r["atb90t"] = BigDecimal.ZERO
            r["baa002"] = BigDecimal.ZERO
            r["bba200"] = s.nz(r["bba002"]).add(s.nz(r["bba004"])).add(s.nz(r["bba005"]))
                .add(s.nz(r["atb90t"])).add(s.nz(r["daa002"]))
            r["baa200"] = s.nz(r["baa002"]).add(s.nz(r["daa001"]))
            r["atc9eh"] = s.nz(r["bba200"]).add(s.nz(r["baa200"]))
            if (ins07(r)) reassign(r)
        }
    }
    for (yg in ygRows) {
        val zRows = s.jdbc.queryForList(
            """SELECT * FROM tc09 a WHERE aae003>?::numeric AND aae002>=202201 AND atc900=?
               AND NOT EXISTS (SELECT 1 FROM tc07 WHERE atc072='26'
                     AND aae003=a.aae003 AND atc900=?)""",
            nBj26.toBigDecimal(), s.long(yg["atc900"]), s.long(yg["atc900"]))
        for (z in zRows) {
            val r = HashMap(t)
            r["atc900"] = yg["atc900"]; r["aae003"] = z["aae003"]; r["atc072"] = "26"
            r["bba002"] = BigDecimal.ZERO
            r["bba004"] = s.nz(r["bba004"]).subtract(s.nz(z["bba004"]))
            r["bba005"] = BigDecimal.ZERO
            r["atb90t"] = BigDecimal.ZERO
            r["baa002"] = BigDecimal.ZERO
            r["bba200"] = s.nz(r["bba002"]).add(s.nz(r["bba004"])).add(s.nz(r["bba005"]))
                .add(s.nz(r["atb90t"])).add(s.nz(r["daa002"]))
            r["baa200"] = s.nz(r["baa002"]).add(s.nz(r["daa001"]))
            r["atc9eh"] = s.nz(r["bba200"]).add(s.nz(r["baa200"]))
            if (ins07(r)) reassign(r)
        }
    }
    return 0 // 源 N_DLF 恒 0
}
