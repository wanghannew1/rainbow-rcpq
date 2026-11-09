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

    /** 表名 → 实体类。@Entity 全量扫描(519 实体与表同名生成；视图实体仅查询通道使用) */
    private val entityClassByTable: Map<String, Class<*>> by lazy {
        val scanner = org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider(false)
        scanner.addIncludeFilter(
            org.springframework.core.type.filter.AnnotationTypeFilter(jakarta.persistence.Entity::class.java))
        val map = mutableMapOf<String, Class<*>>()
        for (bd in scanner.findCandidateComponents("com.jlrc.rcpq.entity")) {
            val cls = Class.forName(bd.beanClassName)
            val table = cls.getAnnotation(jakarta.persistence.Table::class.java)?.name ?: cls.simpleName
            map[table.uppercase()] = cls
        }
        map
    }

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
                    val id = pkValue(entityClass, fields, pkNames)
                        ?: throw IllegalArgumentException("修改行缺少主键: $fields")
                    val current = em.find(entityClass, id)
                        ?: throw IllegalArgumentException("修改行不存在: $id")
                    objectMapper.updateValue(current, objectMapper.convertValue(fields, Map::class.java))
                    em.merge(current)
                    result.updated++
                }
                "deleted" -> {
                    val id = pkValue(entityClass, fields, pkNames)
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

    /** 从行数据中提取主键值（单主键直接取；复合主键转实体 @IdClass，按元数据顺序） */
    private fun pkValue(entityClass: Class<*>, fields: Map<String, Any?>, pkNames: List<String>): Any? {
        if (pkNames.isEmpty()) return null
        val vals = pkNames.mapNotNull { fields[it] }
        if (vals.size != pkNames.size) return null
        if (pkNames.size == 1) return vals[0]
        val idCls = entityClass.getAnnotation(jakarta.persistence.IdClass::class.java)?.value
            ?: throw IllegalArgumentException("复合主键但实体无 @IdClass: ${entityClass.simpleName}")
        val ctor = idCls.java.constructors.first()
        return ctor.newInstance(*vals.toTypedArray())
    }
}
