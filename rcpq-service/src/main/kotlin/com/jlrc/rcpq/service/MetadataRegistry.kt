package com.jlrc.rcpq.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.jlrc.rcpq.service.model.ColumnMeta
import org.springframework.core.io.ClassPathResource
import org.springframework.stereotype.Component
import jakarta.annotation.PostConstruct

/**
 * 数据集元数据注册表：加载 xa-metadata.json（由原 PojoDataStoreBean/MetaDataBean XML 导出）。
 * store → (entity, table, columns) —— validate/query 的行为依据。
 * TODO-SCOPE: 全模块 568 个 store 的元数据按同法扩展（当前仅 xa 9 个）。
 */
@Component
class MetadataRegistry(private val objectMapper: ObjectMapper) {

    class StoreMeta(
        val store: String,
        val entity: String,
        val table: String,
        val columns: List<ColumnMeta>,
    )

    private val stores = mutableMapOf<String, StoreMeta>()

    @PostConstruct
    fun load() {
        @Suppress("UNCHECKED_CAST")
        val raw: Map<String, Map<String, Any>> =
            objectMapper.readValue(
                ClassPathResource("xa-metadata.json").inputStream,
                objectMapper.typeFactory.constructMapType(Map::class.java, String::class.java, Map::class.java),
            )
        for ((name, v) in raw) {
            @Suppress("UNCHECKED_CAST")
            val colsRaw = v["columns"] as List<Map<String, Any>>
            val cols = colsRaw.map { c ->
                ColumnMeta(
                    name = c["name"]?.toString() ?: "",
                    label = c["label"]?.toString() ?: "",
                    nullable = c["nullable"]?.toString() != "false",
                    primaryKey = c["primaryKey"]?.toString() == "true",
                    precision = (c["precision"] as? Number)?.toInt() ?: -1,
                    scale = (c["scale"] as? Number)?.toInt() ?: 0,
                    jdbcType = (c["jdbcType"] as? Number)?.toInt() ?: 0,
                )
            }
            stores[name] = StoreMeta(
                store = name,
                entity = v["entity"]?.toString() ?: "",
                table = v["table"]?.toString() ?: "",
                columns = cols,
            )
        }
    }

    fun meta(storeName: String): StoreMeta? = stores[storeName]
    fun size(): Int = stores.size
}
