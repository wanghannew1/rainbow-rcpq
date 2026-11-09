package com.jlrc.rcpq.web

import com.jlrc.rcpq.service.DefaultCrudDispatcher
import com.jlrc.rcpq.service.InteractionContext
import com.jlrc.rcpq.service.MetadataRegistry
import com.jlrc.rcpq.service.QueryExecutor
import com.jlrc.rcpq.service.MetadataValidator
import org.springframework.web.bind.annotation.*

/**
 * 交互网关（对应原 EAPActionServlet 的交互分发层）。
 * GET /interaction/{code} —— 按 REQ-* 交互码路由到通用分发器或自定义用例。
 * POST /interaction/{code}/validate —— 用真实列元数据校验行数据（行为对照观测面）。
 * TODO-BEHAVIOR: 返回结构需与原 dataStore JSON 对齐（前端依赖），先回显路由结果。
 */
@RestController
@RequestMapping("/interaction")
class InteractionGatewayController(
    private val registry: InteractionRegistry,
    private val dispatcher: DefaultCrudDispatcher,
    private val metadataRegistry: MetadataRegistry,
    private val queryExecutor: QueryExecutor,
) {
    @GetMapping("/{code}")
    fun dispatch(
        @PathVariable code: String,
        @RequestParam params: Map<String, String>,
    ): Map<String, Any?> {
        val spec = registry.resolve(code)
            ?: return mapOf("error" to "未知交互码: $code", "code" to -30010)
        val store = storeOf(code)
        return when (spec.method) {
            "query" -> {
                val meta = metadataRegistry.meta(store)
                    ?: return mapOf("error" to "无store元数据: $store", "code" to -30010)
                val r = queryExecutor.query(meta.table, meta.columns, pksOf(meta), params)
                mapOf(
                    "interaction" to spec.code,
                    "routedTo" to "${spec.bean}.${spec.method}",
                    "store" to store,
                    "table" to meta.table,
                    "rows" to r.rows,
                    "totalCount" to r.totalCount,
                    "page" to r.page,
                    "pageSize" to r.pageSize,
                )
            }
            else -> {
                val ctx = InteractionContext(params.toMutableMap())
                val result = when (spec.method) {
                    "update" -> dispatcher.update(ctx)
                    "count" -> dispatcher.count(ctx)
                    else -> ctx // TODO: 自定义用例路由(XabUnitService.doDelete 等)接 Spring bean 名映射
                }
                mapOf(
                    "interaction" to spec.code,
                    "routedTo" to "${spec.bean}.${spec.method}",
                    "params" to result.params,
                )
            }
        }
    }

    /** 交互码 REQ-<page>-<store>-<action> → store 名 */
    private fun storeOf(code: String): String =
        code.split("-").let { if (it.size >= 3) it[it.size - 2] else "" }

    private fun pksOf(meta: MetadataRegistry.StoreMeta): List<String> =
        meta.columns.filter { it.primaryKey }.map { it.name }

    @GetMapping("/registry")
    fun registry(): Map<String, Any> = mapOf(
        "count" to registry.size(),
        "interactions" to registry.all().map { mapOf("code" to it.code, "page" to it.page, "method" to it.method) },
    )

    /**
     * 行数据元数据校验。交互码形如 REQ-<page>-<store>-<action>，store 取倒数第二段。
     * body = 行数据（列名→值）；可选 "__isNew"=true 表示新增记录（主键非空校验跳过）。
     * 校验规则/错误码/文案与原系统一致（MetadataValidator）。
     */
    @PostMapping("/{code}/validate")
    fun validate(@PathVariable code: String, @RequestBody body: Map<String, Any?>): Map<String, Any?> {
        val spec = registry.resolve(code)
            ?: return mapOf("error" to "未知交互码: $code", "code" to -30010)
        val parts = code.split("-")
        val store = if (parts.size >= 3) parts[parts.size - 2] else ""
        val meta = metadataRegistry.meta(store)
            ?: return mapOf("error" to "无store元数据: $store", "code" to -30010)
        val result = MetadataValidator.validateRow(body, meta.columns, isNew = body["__isNew"] == true)
        return mapOf(
            "interaction" to code,
            "store" to store,
            "table" to meta.table,
            "valid" to result.valid,
            "code" to result.code,
            "message" to result.message,
        )
    }
}
