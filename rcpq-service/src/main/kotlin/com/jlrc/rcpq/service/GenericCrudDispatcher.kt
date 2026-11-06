package com.jlrc.rcpq.service

import com.jlrc.rcpq.service.model.ColumnMeta

/**
 * 通用 CRUD 分发器（对应原 LemisBusinessMgr 契约）。
 *
 * 必须保留的语义不变式（docs/LemisBusinessMgr-contract.md）：
 * 1. update 主从表保存：主表先存，从表（store 名含"."）随后，外键回填两种形态（SAVENOM2O 切换）；
 *    SQL 模式禁止 update。
 * 2. validate：非 nullable 非空校验（新记录跳过主键）；长度按 precision，非 ASCII 字符计 2。
 * 3. 双模式：SQL datastore（JDBC）与 POJO datastore（ORM）分别路由。
 */
interface GenericCrudDispatcher {
    fun query(context: InteractionContext): InteractionContext
    fun update(context: InteractionContext): InteractionContext
    fun count(context: InteractionContext): InteractionContext
    fun validate(context: InteractionContext, meta: List<ColumnMeta>): ValidationResult
}
