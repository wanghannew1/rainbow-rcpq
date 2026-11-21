package com.jlrc.rcpq.web

import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

/**
 * 月转款工作台查询面（W4c-2, 配合六险种月情况 /proc + PRC_月转款确认）。
 * TABNAME 白名单 → 主表/明细表(与 PRC_月转款确认 同源), 只读查询。
 */
@RestController
@RequestMapping("/wb/yue")
class YueViewController(private val jdbc: JdbcTemplate) {

    // TABNAME → (主表, 明细表) — 与 PRC_月转款确认 白名单同源
    private val tabs = mapOf(
        "TJ0A" to ("tj0a" to "tj0a1"), "TJ0B" to ("tj0b" to "tj0b1"),
        "TJ0C" to ("tj0c" to "tj0c1"), "TJ0D" to ("tj0d" to "tj0d1"),
        "TJ0E" to ("tj0e" to "tj0e1"), "TJ0F" to ("tj0f" to "tj0f1"))

    /** 汇总查询 */
    @GetMapping("/main")
    fun main(@RequestParam ym: String, @RequestParam aaa030: Long,
             @RequestParam aaa020: String, @RequestParam tab: String): List<Map<String, Any?>> {
        if (!Regex("^\\d{6}$").matches(ym)) return emptyList()
        val t = tabs[tab.trim().uppercase()] ?: return emptyList()
        return jdbc.queryForList(
            """SELECT * FROM ${t.first} WHERE aae002=? AND aaa030=? AND aaa020=?
               ORDER BY xh NULLS LAST""",
            ym.toLong(), aaa030, aaa020)
    }

    /** 明细查询 */
    @GetMapping("/detail")
    fun detail(@RequestParam ym: String, @RequestParam aaa030: Long,
               @RequestParam aaa020: String, @RequestParam tab: String,
               @RequestParam(required = false) atb930: Long?): List<Map<String, Any?>> {
        if (!Regex("^\\d{6}$").matches(ym)) return emptyList()
        val t = tabs[tab.trim().uppercase()] ?: return emptyList()
        var sql = """SELECT * FROM ${t.second} WHERE aae002=? AND aaa030=? AND aaa020=?"""
        if (atb930 != null) sql += " AND atb930=$atb930"
        return jdbc.queryForList("$sql ORDER BY atc900 NULLS LAST LIMIT 500")
    }

    /** 月垫付台账 TJ0Z 查询(W4d-3, tdce:53 页读侧; 只读, 与 /proc PRC_月垫付情况 配套)。 */
    @GetMapping("/df")
    fun df(@RequestParam ym: String, @RequestParam aaa030: Long): List<Map<String, Any?>> {
        if (!Regex("^\\d{6}$").matches(ym)) return emptyList()
        return jdbc.queryForList(
            """SELECT * FROM tj0z WHERE aae002=? AND aaa030=?
               ORDER BY ttype, xh NULLS LAST LIMIT 500""",
            ym.toLong(), aaa030)
    }

    /** 市医保应缴费明细 TJ0C2 查询(2026-11-21, td/c/bj 页读侧 tj0c2Store 等价; 只读, 与
     *  /proc PRC_市医保应缴费情况 配套——该过程重建 tj0c2(aaa020+aaa030+aae002 键)。
     *  注: TJ06 是另一张历史表, 本页不用。 */
    @GetMapping("/tj0c2")
    fun tj0c2(@RequestParam ym: String, @RequestParam aaa030: Long,
              @RequestParam aaa020: String): List<Map<String, Any?>> {
        if (!Regex("^\\d{6}$").matches(ym)) return emptyList()
        return jdbc.queryForList(
            """SELECT * FROM tj0c2 WHERE aae002=? AND aaa030=? AND aaa020=?
               ORDER BY atc900 NULLS LAST LIMIT 500""",
            ym.toLong(), aaa030, aaa020)
    }
}
