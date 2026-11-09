package com.jlrc.rcpq.service.td

import com.fasterxml.jackson.databind.ObjectMapper
import com.jlrc.rcpq.entity.TC9U
import jakarta.persistence.EntityManager
import java.util.concurrent.ConcurrentHashMap
import jakarta.persistence.PersistenceContext
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

/**
 * td/c 7d: 批删克隆家族参数化合并(tdca/tdcae/tdcaj) + tdcl AA97 修改保存。
 *
 * - tdca: TC9W_BP03 按 aae002 批删(delete/delete1 同语义)
 * - tdcae: TC9U_BP02 按(年月, aaa100, 户头) / TC9U_BP03 按(年月, 区划, 户头)
 * - tdcaj: TC9W_BP02 按(年月, 户头, 区划) + doSave TC9U.aaa004→TC90.AAA004 联动
 * - tdcl: AA97 整行修改保存(aa97Store_mod)
 * 键类型纪律: aae002/aaa030/aae001 为 numeric, aaa020/aaa100 为 varchar。
 */
@Component
class TdcBatchCases(
    private val jdbcTemplate: JdbcTemplate,
    private val objectMapper: ObjectMapper,
) {
    @PersistenceContext
    lateinit var em: EntityManager

    /** 列类型缓存(information_schema 权威, numeric→数值绑定避免 varchar=numeric 500) */
    private val colTypes = ConcurrentHashMap<String, String>()
    private fun typeOf(table: String, col: String): String = colTypes.computeIfAbsent("$table.$col") {
        jdbcTemplate.queryForObject(
            "SELECT data_type FROM information_schema.columns WHERE table_name=? AND column_name=?",
            String::class.java, table, col.lowercase()) ?: "character varying"
    }

    private fun bindCol(table: String, k: String, v: String): Any = when (typeOf(table, k)) {
        "numeric", "bigint", "integer", "smallint" -> v.toBigDecimalOrNull() ?: v.toLongOrNull() ?: 0L
        else -> v
    }

    fun delete(table: String, keys: Map<String, String>): Map<String, Any?> {
        val ks = keys.entries.joinToString(" AND ") { "${it.key}=?" }
        val args = keys.entries.map { bindCol(table, it.key, it.value) }.toTypedArray()
        val n = jdbcTemplate.update("DELETE FROM $table WHERE $ks", *args)
        return mapOf("table" to table, "deleted" to n)
    }

    /** tdcaj doSave: TC9U 公积金户头编号联动 */
    @Transactional
    fun doSaveTc9u(tc9uRow: Map<String, Any?>): Map<String, Any?> {
        val tc9u = objectMapper.convertValue(tc9uRow.filterKeys { it != "__state" }, TC9U::class.java)
        val atc9u0 = tc9u.atc9u0 ?: throw IllegalArgumentException("缺少 atc9u0")
        val before = jdbcTemplate.queryForObject(
            "SELECT aaa004 FROM tc9u WHERE atc9u0=?", String::class.java, atc9u0)
        var synced = 0
        if (before != tc9u.aaa004) {
            synced = jdbcTemplate.update(
                "UPDATE tc90 SET aaa004=? WHERE atc900=?", tc9u.aaa004, tc9u.atc900)
        }
        em.merge(tc9u)
        return mapOf("atc9u0" to atc9u0, "tc90Synced" to synced)
    }

    /** tdcl update: AA97 整行修改保存 */
    @Transactional
    fun saveAa97(aa97Row: Map<String, Any?>): Map<String, Any?> {
        val fields = aa97Row.filterKeys { it != "__state" }
        val keys = listOf("aae001", "aac001")
        for (k in keys) if (fields[k] == null) throw IllegalArgumentException("缺少 $k")
        val aae001 = fields["aae001"].toString().toLong()
        val aac001 = fields["aac001"].toString().toLong()
        val current = jdbcTemplate.query(
            "SELECT 1 FROM aa97 WHERE aae001=? AND aac001=?", { rs, _ -> rs.getInt(1) },
            aae001, aac001).firstOrNull()
        if (current == null) {
            // 无既有行: 按元数据列全量插入
            val cols = fields.keys.toList()
            val placeholders = cols.joinToString(",") { "?" }
            jdbcTemplate.update(
                "INSERT INTO aa97 (${cols.joinToString(",")}) VALUES ($placeholders)",
                *cols.map { bindCol("aa97", it, fields[it].toString()) }.toTypedArray())
            return mapOf("saved" to "inserted")
        }
        for ((k, v) in fields) {
            if (k in keys) continue
            jdbcTemplate.update("UPDATE aa97 SET $k=? WHERE aae001=? AND aac001=?",
                bindCol("aa97", k, v.toString()), aae001, aac001)
        }
        return mapOf("saved" to "updated")
    }
}
