package com.jlrc.rcpq.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.jlrc.rcpq.service.model.ColumnMeta
import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

/**
 * 标准保存/删除执行器（原 PojoMgr.update 的 PG 实现，xa 试点为单 store）。
 *
 * 协议（JSON 行数组，__state 标注行状态——对应原 PojoEntity 的
 * isNewModified/dataModified/deletedPojo 三态）：
 *   { "rows": [ {"__state":"new",      ...字段},      // 新增：序列主键由 JPA 生成，勿传主键；
 *               {"__state":"modified", ...字段, "主键":值},  // 修改：须带主键
 *               {"__state":"deleted",  "主键":值} ] }  // 删除：按主键
 *
 * assigned 主键表（AA12）新增时必须携带主键值。
 * 字段名 = 元数据列名（小写）= 实体属性名；值类型由实体定义转换（时间用 ISO 格式）。
 * TODO-BEHAVIOR(契约§2): 主从表（store 名含"."）保存顺序 + initPK/initPKNoM2O 外键回填——xa 无从表，随 dialog 页接入时补。
 */
@Component
class UpdateExecutor(
    private val objectMapper: ObjectMapper,
    private val metadataRegistry: MetadataRegistry,
) {
    @PersistenceContext
    lateinit var em: EntityManager

    /** store 名 → 实体类。试点 4 表硬映射；后续由生成器产出映射清单 */
    private val entityClassByTable = mapOf(
        "AA01" to com.jlrc.rcpq.entity.AA01::class.java,
        "AA10" to com.jlrc.rcpq.entity.AA10::class.java,
        "AA12" to com.jlrc.rcpq.entity.AA12::class.java,
        "SYS_QUESTION" to com.jlrc.rcpq.entity.SYS_QUESTION::class.java,
    )

    data class UpdateResult(var inserted: Int = 0, var updated: Int = 0, var deleted: Int = 0)

    @Transactional
    fun execute(store: String, rows: List<Map<String, Any?>>): UpdateResult {
        val meta = metadataRegistry.meta(store)
            ?: throw IllegalArgumentException("无store元数据: $store")
        val entityClass = entityClassByTable[meta.table]
            ?: throw IllegalArgumentException("表 ${meta.table} 无实体映射")
        val pkNames = meta.columns.filter { it.primaryKey }.map { it.name }
        val result = UpdateResult()
        for (row in rows) {
            val state = row["__state"]?.toString() ?: "modified"
            val fields = row.filterKeys { it != "__state" }
            when (state) {
                "new" -> {
                    val entity = objectMapper.convertValue(fields, entityClass)
                    em.persist(entity)
                    result.inserted++
                }
                "modified" -> {
                    val id = pkValue(fields, pkNames)
                        ?: throw IllegalArgumentException("修改行缺少主键: $fields")
                    val current = em.find(entityClass, id)
                        ?: throw IllegalArgumentException("修改行不存在: $id")
                    objectMapper.updateValue(current, objectMapper.convertValue(fields, Map::class.java))
                    em.merge(current)
                    result.updated++
                }
                "deleted" -> {
                    val id = pkValue(fields, pkNames)
                        ?: throw IllegalArgumentException("删除行缺少主键: $fields")
                    val current = em.find(entityClass, id)
                        ?: throw IllegalArgumentException("删除行不存在: $id")
                    em.remove(current)
                    result.deleted++
                }
                else -> throw IllegalArgumentException("未知行状态: $state")
            }
        }
        return result
    }

    /** 从行数据中提取主键值（单主键直接取；复合主键转 @IdClass） */
    private fun pkValue(fields: Map<String, Any?>, pkNames: List<String>): Any? {
        if (pkNames.isEmpty()) return null
        val vals = pkNames.mapNotNull { fields[it] }
        if (vals.size != pkNames.size) return null
        return if (pkNames.size == 1) vals[0] else newIdClass(vals)
    }

    private fun newIdClass(vals: List<Any?>): Any {
        // AA10Id(aaa100, aaa102) — 复合键构造按元数据顺序
        val cls = Class.forName("com.jlrc.rcpq.entity.AA10Id")
        val ctor = cls.constructors.first()
        return ctor.newInstance(*vals.toTypedArray())
    }
}
