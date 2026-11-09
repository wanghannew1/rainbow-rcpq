package com.jlrc.rcpq.service.td

import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

/**
 * td/c/j 补贴确认（旧 tdcjService 118L 规格重写）。
 *
 * - saveNobt: 逐 id 置 TC93_DB.isbt='0'(标记不补贴)
 * - saveConfirm: 逐 id 确认——isbt 空→'1'; aae214='1'+经办人章;
 *   同步 TC93_SS.aae214='1'(按证件号+姓名+年度)
 * - AllConfirm: 按年度全量——isbt 空且 aae214='0' → isbt='1';
 *   aae214='0' → '1'+经办人章; TC93_SS 全年度 aae214='1'
 * 经办人: 会话用户(测试约定 9999)
 */
@Component
class TdcjCases(
    private val jdbcTemplate: JdbcTemplate,
) {
    private val user = arrayOf("9999", "测试用户", "00000000", "彩虹人才试点", "120114")

    @Transactional
    fun saveNobt(ids: List<Long>): Map<String, Any?> {
        var n = 0
        for (id in ids) {
            n += jdbcTemplate.update("UPDATE tc93_db SET isbt='0' WHERE id=?", id)
        }
        return mapOf("nobt" to n)
    }

    @Transactional
    fun saveConfirm(ids: List<Long>): Map<String, Any?> {
        var n = 0
        var ssSynced = 0
        for (id in ids) {
            val row = jdbcTemplate.query(
                "SELECT id, isbt, aac002, aac003, aae002::text y FROM tc93_db WHERE id=?",
                { rs, _ -> arrayOf(rs.getLong("id"), rs.getString("isbt"), rs.getString("aac002"),
                    rs.getString("aac003"), rs.getString("y")) }, id).firstOrNull() ?: continue
            val isbt = row[1] as String?
            val newIsbt = if (isbt.isNullOrBlank()) "1" else isbt
            n += jdbcTemplate.update(
                """UPDATE tc93_db SET isbt=?, aae214='1', aae211=?, aae217=?, aae236=now(),
                   aae219=?, aae220=?, aae222=? WHERE id=?""",
                newIsbt, user[0], user[1], user[2], user[3], user[4], id)
            ssSynced += jdbcTemplate.update(
                "UPDATE tc93_ss SET aae214='1' WHERE aac002=? AND aac003=? AND year=?",
                row[2], row[3], row[4])
        }
        return mapOf("confirmed" to n, "ssSynced" to ssSynced)
    }

    @Transactional
    fun allConfirm(year: String): Map<String, Any?> {
        // tc93_db.aae002 为 numeric, tc93_ss.year 为 varchar — 分表绑定
        val y = year.toLongOrNull() ?: return mapOf("error" to "year 非数值")
        val n1 = jdbcTemplate.update(
            "UPDATE tc93_db SET isbt='1' WHERE aae002=? AND aae214='0' AND isbt IS NULL", y)
        val n2 = jdbcTemplate.update(
            """UPDATE tc93_db SET aae214='1', aae211=?, aae217=?, aae236=now(),
               aae219=?, aae220=?, aae222=? WHERE aae002=? AND aae214='0'""",
            user[0], user[1], user[2], user[3], user[4], y)
        val n3 = jdbcTemplate.update("UPDATE tc93_ss SET aae214='1' WHERE year=?", year)
        return mapOf("isbtSet" to n1, "confirmed" to n2, "ssSynced" to n3)
    }
}
