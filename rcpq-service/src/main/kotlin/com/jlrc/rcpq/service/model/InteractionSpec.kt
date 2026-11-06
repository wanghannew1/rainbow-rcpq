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
)
