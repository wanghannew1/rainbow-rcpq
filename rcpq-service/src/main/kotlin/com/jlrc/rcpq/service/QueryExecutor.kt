package com.jlrc.rcpq.service

import com.jlrc.rcpq.service.model.ColumnMeta
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Component

/**
 * 标准查询执行器（原 PojoMgr.query 的 PG 实现）。
 *
 * 协议决策（docs/xa-module-spec.md §5）：新前端不使用旧 unieap JS 框架，
 * 线格式重新设计为 JSON；服务端语义保真——同 store + 同过滤条件 + 同分页 → 同结果行。
 *
 * 过滤参数（在交互码解析出的 store 上）：
 *   { "<列名>": 值 }                      → 等值
 *   { "<列名>__like": "xx" }              → 前缀模糊(LIKE 'xx%')
 *   { "page": 1 }                         → 页码(1起, 默认1)
 * 列名以元数据白名单校验（防注入）；排序 = 主键升序（与旧系统一致）。
 */
@Component
class QueryExecutor(private val jdbcTemplate: JdbcTemplate) {

    data class QueryResult(val rows: List<Map<String, Any?>>, val totalCount: Long, val page: Int, val pageSize: Int)

    fun query(table: String, columns: List<ColumnMeta>, pks: List<String>, params: Map<String, String>): QueryResult {
        val validCols = columns.associateBy { it.name }
        val where = mutableListOf<String>()
        val args = mutableListOf<Any>()
        var page = 1
        val pageSize = DEFAULT_PAGE_SIZE

        for ((key, value) in params) {
            when {
                key == "page" -> page = value.toIntOrNull()?.coerceAtLeast(1) ?: 1
                key.endsWith("__like") -> {
                    val col = key.removeSuffix("__like")
                    if (validCols.containsKey(col) && isStringType(validCols[col]!!.jdbcType) && value.isNotEmpty()) {
                        where += "${col} LIKE ?"
                        args += "$value%"
                    }
                }
                validCols.containsKey(key) && value.isNotEmpty() -> {
                    where += "$key = ?"
                    args += value
                }
            }
        }

        val whereSql = if (where.isEmpty()) "" else " WHERE " + where.joinToString(" AND ")
        val orderSql = if (pks.isEmpty()) "" else " ORDER BY " + pks.joinToString(", ")
        val total = jdbcTemplate.queryForObject("SELECT count(*) FROM $table$whereSql", Long::class.java, *args.toTypedArray())
        val rows = jdbcTemplate.queryForList(
            "SELECT * FROM $table$whereSql$orderSql LIMIT $pageSize OFFSET ${(page - 1) * pageSize}",
            *args.toTypedArray(),
        )
        return QueryResult(rows, total, page, pageSize)
    }

    companion object {
        /** 旧系统 datastore pagesize 常见为 10；xa 元数据里 pagesize 属性待逐 store 接入 */
        const val DEFAULT_PAGE_SIZE = 10

        private fun isStringType(jdbcType: Int): Boolean =
            jdbcType == java.sql.Types.VARCHAR || jdbcType == java.sql.Types.CHAR
    }
}
