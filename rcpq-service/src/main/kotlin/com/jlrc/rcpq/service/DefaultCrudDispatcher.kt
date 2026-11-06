package com.jlrc.rcpq.service

import com.jlrc.rcpq.service.model.ColumnMeta
import org.springframework.stereotype.Component

/**
 * 缺省实现：xa 试点的标准 CRUD 骨架。
 * 行为等价项逐条补齐（每项对应规格书/契约文档章节），标注 TODO-BEHAVIOR。
 */
@Component
class DefaultCrudDispatcher : GenericCrudDispatcher {

    override fun query(context: InteractionContext): InteractionContext {
        // TODO-BEHAVIOR(契约§2 query): 双模式分发——SQL datastore→SqlQueryExecutor,
        // POJO datastore→按 rowSet 实体查询; 结果 store 回填 context
        return context
    }

    override fun update(context: InteractionContext): InteractionContext {
        // TODO-BEHAVIOR(契约§2 update): 主从表保存顺序 + initPK/initPKNoM2O 外键回填;
        // SQL 模式直接抛 BizException(-30001, "SQLDS不能用于更新")
        return context
    }

    override fun count(context: InteractionContext): InteractionContext {
        // TODO-BEHAVIOR(契约§2 count): SQL→sqlMgr.doCount; POJO→空 store 逐个 doCount
        return context
    }

    override fun validate(context: InteractionContext, meta: List<ColumnMeta>): ValidationResult {
        // TODO-BEHAVIOR(契约§2 validate): 非空(新记录跳过主键) + 长度(非ASCII计2)
        for (col in meta) {
            val value = context.dataSet(context.params["storeName"]?.toString() ?: "")
                .firstOrNull()?.get(col.name)
            if (!col.nullable && !col.primaryKey && (value == null || value == "")) {
                return ValidationResult(
                    valid = false,
                    code = -30027, // 原 LEMIS_ERRNO_POSTDATA_RECHECK 数值
                    message = "[${col.label.ifEmpty { col.name }}]数据不可为空",
                )
            }
            // TODO-BEHAVIOR: precision 长度校验(非 ASCII 计 2)
        }
        return ValidationResult()
    }
}
