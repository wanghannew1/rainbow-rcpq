package com.jlrc.rcpq.service

import com.jlrc.rcpq.service.model.ColumnMeta

/**
 * 元数据校验（原 LemisBusinessMgr.validate 的语义移植，docs/LemisBusinessMgr-contract.md §2）：
 *
 * 规则（与旧系统逐条对齐，勿改数值/文案——前端与行为对照测试依赖）：
 * 1. 非 nullable 列值为 null 或 "" → 校验失败 code=-30027「[label]数据不可为空」；
 *    新增记录（isNew=true）跳过主键列。
 * 2. precision>=0 且类型为字符串/数值时做长度校验：
 *    数值长度 = 去掉 '-' 和 '.' 后的字符数；字符串长度 = ASCII 字符计 1、非 ASCII 计 2；
 *    超长 → code=-30027「[label]数据过长」。
 * 3. 列名含 "." 的为从表级联列，仅当对应从表参与持久化时校验（试点版：跳过）。
 */
object MetadataValidator {

    const val ERR_POSTDATA_RECHECK = -30027L

    /** VARCHAR / CHAR 类 jdbcType；NUMERIC 类单独判断 */
    private fun isStringType(jdbcType: Int): Boolean =
        jdbcType == java.sql.Types.VARCHAR || jdbcType == java.sql.Types.CHAR

    private fun isNumericType(jdbcType: Int): Boolean =
        jdbcType == java.sql.Types.NUMERIC || jdbcType == java.sql.Types.DECIMAL

    fun validateRow(
        row: Map<String, Any?>,
        meta: List<ColumnMeta>,
        isNew: Boolean = false,
    ): ValidationResult {
        val result = ValidationResult()
        for (col in meta) {
            if (col.name.contains('.')) continue // TODO-BEHAVIOR: 从表级联列按 SAVENOM2O 过滤
            val value = row[col.name]
            if (!col.nullable) {
                val skipPk = col.primaryKey && isNew
                if (!skipPk && (value == null || value == "")) {
                    return ValidationResult(
                        valid = false,
                        code = ERR_POSTDATA_RECHECK,
                        message = "[${col.label.ifEmpty { col.name }}]数据不可为空",
                    )
                }
            }
            if (value != null && col.precision >= 0 && (isStringType(col.jdbcType) || isNumericType(col.jdbcType))) {
                val len = if (isNumericType(col.jdbcType)) {
                    value.toString().replace("-", "").replace(".", "").length
                } else {
                    displayLength(value.toString())
                }
                if (len > col.precision) {
                    return ValidationResult(
                        valid = false,
                        code = ERR_POSTDATA_RECHECK,
                        message = "[${col.label.ifEmpty { col.name }}]数据过长",
                    )
                }
            }
        }
        return result
    }

    /** ASCII 计 1、非 ASCII 计 2（原系统对中文的长度口径） */
    fun displayLength(s: String): Int = s.fold(0) { acc, c -> acc + if (c.code < 128) 1 else 2 }
}
