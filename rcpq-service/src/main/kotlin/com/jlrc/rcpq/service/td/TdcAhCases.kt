package com.jlrc.rcpq.service.td

import com.fasterxml.jackson.databind.ObjectMapper
import com.jlrc.rcpq.entity.TC9W1
import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

/**
 * td/c 7c: 三份复制的 tdcahService(ah/ay/bp 目录, 各 67-68L)合并为参数化实现。
 *
 * 页面→表映射: tdcah→TC9W_BP01, tdcay→TC9W_BP04, tdcbp→TC9W_BP09。
 * - delete: 按(年月, 户头, 区划)整批删
 * - delete1: ah/bp 按(年月, 户头)删; tdcay 原拼串 bug(缺'=')运行时必抛——保留报错行为
 * - doSave: TC9W1.aaa005 与库中不一致时联动 TC90.AAA005(工伤户头编号变更)
 */
@Component
class TdcAhCases(
    private val jdbcTemplate: JdbcTemplate,
    private val objectMapper: ObjectMapper,
) {
    @PersistenceContext
    lateinit var em: EntityManager

    fun tableFor(page: String): String = when {
        page.startsWith("tdcay") -> "tc9w_bp04"
        page.startsWith("tdcbp") -> "tc9w_bp09"
        else -> "tc9w_bp01" // tdcah
    }

    @Transactional
    fun delete(page: String, aae002: String, aaa030: String, aaa020: String): Map<String, Any?> {
        val table = tableFor(page)
        val n = jdbcTemplate.update(
            "DELETE FROM $table WHERE aae002=? AND aaa030=? AND aaa020=?",
            aae002.toLong(), aaa030.toLong(), aaa020)
        return mapOf("table" to table, "deleted" to n)
    }

    @Transactional
    fun delete1(page: String, aae002: String, aaa030: String, aaa020: String): Map<String, Any?> {
        val table = tableFor(page)
        if (page.startsWith("tdcay")) {
            // 旧 ay 副本拼串 bug: HQL "and aaa020" + 值(缺'=')→ 运行时 QueryException, 页面收 -3000
            throw IllegalStateException("tdcay delete1 旧拼串 bug(HQL 'and aaa020'缺'='), 与旧系统一致报错")
        }
        val n = jdbcTemplate.update(
            "DELETE FROM $table WHERE aae002=? AND aaa030=?", aae002.toLong(), aaa030.toLong())
        return mapOf("table" to table, "deleted" to n)
    }

    @Transactional
    fun doSave(tc9w1Row: Map<String, Any?>): Map<String, Any?> {
        val tc9w1 = objectMapper.convertValue(
            tc9w1Row.filterKeys { it != "__state" }, TC9W1::class.java)
        val atc9w0 = tc9w1.atc9w0 ?: throw IllegalArgumentException("缺少 atc9w0")
        val before = jdbcTemplate.queryForObject(
            "SELECT aaa005 FROM tc9w1 WHERE atc9w0=?", String::class.java, atc9w0)
        var synced = 0
        if (before != tc9w1.aaa005) {
            synced = jdbcTemplate.update(
                "UPDATE tc90 SET aaa005=? WHERE atc900=?", tc9w1.aaa005, tc9w1.atc900)
        }
        em.merge(tc9w1)
        return mapOf("atc9w0" to atc9w0, "tc90Synced" to synced)
    }
}
