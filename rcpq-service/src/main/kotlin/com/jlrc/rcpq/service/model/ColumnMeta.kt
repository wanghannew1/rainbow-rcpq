package com.jlrc.rcpq.service.model

/**
 * 列元数据（对应原 MetaDataBean）。
 * 行为语义来源：docs/LemisBusinessMgr-contract.md §2 validate 规则。
 */
data class ColumnMeta(
    val name: String,
    val label: String = "",
    val nullable: Boolean = true,
    val primaryKey: Boolean = false,
    val precision: Int = -1,
    val scale: Int = 0,
    /** java.sql.Types 原值，保持对旧行为可对照 */
    val jdbcType: Int = 0,
)
