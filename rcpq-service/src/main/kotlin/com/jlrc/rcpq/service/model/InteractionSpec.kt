package com.jlrc.rcpq.service.model

/**
 * 交互规格（对应原 InteractionBean）。
 * 路由键 = 交互码（REQ-*），由 docs/xa-module-spec.md §2 的清单生成。
 */
data class InteractionSpec(
    val code: String,
    val page: String,
    /** Q=查询 M=修改 */
    val type: String,
    /** 处理 bean：generic=通用分发器，或自定义服务名 */
    val bean: String,
    /** 方法名：query/update/count 或自定义用例名 */
    val method: String,
    /** 是否启用元数据校验（对应 metadata_validate） */
    val metadataValidate: Boolean = false,
    /** 页面携带的 store 清单（旧页面 XML datastores 权威回填，scripts/patch_interaction_stores.py）。
     *  哈希交互码无码内 store 段，通用分发按此解析。 */
    val stores: List<String> = emptyList(),
)
