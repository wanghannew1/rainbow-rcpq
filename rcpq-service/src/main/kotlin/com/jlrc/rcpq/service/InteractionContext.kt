package com.jlrc.rcpq.service

/**
 * 交互上下文（对应原 IDataCenter/IDataStore 的最小替代）。
 * 试点阶段仅承载参数与结果集占位；结构将随行为对照测试演进。
 */
class InteractionContext(
    val params: MutableMap<String, Any?> = mutableMapOf(),
    val dataSets: MutableMap<String, MutableList<Map<String, Any?>>> = mutableMapOf(),
) {
    fun param(key: String): Any? = params[key]
    fun setParam(key: String, value: Any?) {
        params[key] = value
    }

    fun dataSet(name: String): MutableList<Map<String, Any?>> =
        dataSets.getOrPut(name) { mutableListOf() }
}

/** 校验结果（对应原 ValidateResult；code 保留原错误码数值，前端依赖） */
data class ValidationResult(
    var valid: Boolean = true,
    var code: Long = 0,
    var message: String = "",
)
