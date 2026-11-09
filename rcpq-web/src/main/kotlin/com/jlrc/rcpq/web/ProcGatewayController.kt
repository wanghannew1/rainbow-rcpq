package com.jlrc.rcpq.web

import com.jlrc.rcpq.service.proc.ProcHandler
import org.springframework.web.bind.annotation.*

/**
 * 过程网关（对应原 lemis.util.getProcedure → ProcedureDAOUtil.executeProcedure 通道）。
 * POST /proc/{pkg.proc} body={形参...} → 处理器结果 {appCode, errMsg, ...}。
 * GET /proc → 已注册过程清单（观测面）。
 * 未注册过程返回 appCode=-1 + 提示（等价原 appCode<1 抛 LemisException -30032 的调用侧行为）。
 */
@RestController
@RequestMapping("/proc")
class ProcGatewayController(private val handlers: List<ProcHandler>) {

    private val byName: Map<String, ProcHandler> by lazy {
        handlers.associateBy { it.procName() }
    }

    @PostMapping("/{name:.+}")
    fun call(@PathVariable name: String, @RequestBody params: Map<String, Any?>): Map<String, Any?> {
        val h = byName[name]
            ?: return mapOf("appCode" to -1, "errMsg" to "过程未实现或未注册: $name")
        return h.handle(params)
    }

    @GetMapping
    fun registry(): Map<String, Any?> = mapOf(
        "count" to byName.size,
        "procs" to byName.keys.sorted(),
    )
}
