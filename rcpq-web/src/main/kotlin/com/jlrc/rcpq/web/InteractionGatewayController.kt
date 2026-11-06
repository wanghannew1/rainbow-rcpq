package com.jlrc.rcpq.web

import com.jlrc.rcpq.service.DefaultCrudDispatcher
import com.jlrc.rcpq.service.InteractionContext
import org.springframework.web.bind.annotation.*

/**
 * 交互网关（对应原 EAPActionServlet 的交互分发层）。
 * GET /interaction/{code} —— 按 REQ-* 交互码路由到通用分发器或自定义用例。
 * TODO-BEHAVIOR: 返回结构需与原 dataStore JSON 对齐（前端依赖），先回显路由结果。
 */
@RestController
@RequestMapping("/interaction")
class InteractionGatewayController(
    private val registry: InteractionRegistry,
    private val dispatcher: DefaultCrudDispatcher,
) {
    @GetMapping("/{code}")
    fun dispatch(
        @PathVariable code: String,
        @RequestParam params: Map<String, String>,
    ): Map<String, Any?> {
        val spec = registry.resolve(code)
            ?: return mapOf("error" to "未知交互码: $code", "code" to -30010)
        val ctx = InteractionContext(params.toMutableMap())
        val result = when (spec.method) {
            "query" -> dispatcher.query(ctx)
            "update" -> dispatcher.update(ctx)
            "count" -> dispatcher.count(ctx)
            else -> ctx // TODO: 自定义用例路由(XabUnitService.doDelete 等)接 Spring bean 名映射
        }
        return mapOf(
            "interaction" to spec.code,
            "routedTo" to "${spec.bean}.${spec.method}",
            "params" to result.params,
        )
    }

    @GetMapping("/registry")
    fun registry(): Map<String, Any> = mapOf(
        "count" to registry.size(),
        "interactions" to registry.all().map { mapOf("code" to it.code, "page" to it.page, "method" to it.method) },
    )
}
