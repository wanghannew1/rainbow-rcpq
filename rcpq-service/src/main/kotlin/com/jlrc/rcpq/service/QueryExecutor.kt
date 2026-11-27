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
                    args += typedValue(validCols[key]!!, value)
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

        /**
         * 等值参数按列 jdbcType 分型绑定(2026-11-27, 桥单 013 BC-04 对比中发现的移植缺陷)：
         * 全 String 绑定在 PG 报 "operator does not exist: bigint = character varying"
         * (Oracle 隐式转换宽容, PG 不行——PG 类型漂移铁律同族)。非数值/日期列维持 String;
         * 解析失败回退原串(与 SysImportController.typed 同保守策略)。
         */
        private fun typedValue(meta: ColumnMeta, value: String): Any =
            when (meta.jdbcType) {
                java.sql.Types.BIGINT, java.sql.Types.INTEGER, java.sql.Types.SMALLINT,
                java.sql.Types.TINYINT -> value.toLongOrNull() ?: value
                java.sql.Types.NUMERIC, java.sql.Types.DECIMAL, java.sql.Types.FLOAT,
                java.sql.Types.REAL, java.sql.Types.DOUBLE ->
                    value.toBigDecimalOrNull() ?: value
                java.sql.Types.DATE, java.sql.Types.TIMESTAMP, java.sql.Types.TIME ->
                    runCatching { java.sql.Date.valueOf(value) }.getOrDefault(value)
                else -> value
            }
    }
}
