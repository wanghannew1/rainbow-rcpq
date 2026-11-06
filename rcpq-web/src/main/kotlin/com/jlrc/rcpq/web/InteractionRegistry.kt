package com.jlrc.rcpq.web

import com.fasterxml.jackson.databind.ObjectMapper
import com.jlrc.rcpq.service.model.InteractionSpec
import org.springframework.core.io.ClassPathResource
import org.springframework.stereotype.Component
import jakarta.annotation.PostConstruct

/**
 * 交互注册表：从 resources/interactions.json 加载（由 xa-module-spec §2 清单生成）。
 * 覆盖两条通道：页面交互（REQ-*）与 Ajax 直调（bean 名）。
 */
@Component
class InteractionRegistry(private val objectMapper: ObjectMapper) {

    private val byCode = mutableMapOf<String, InteractionSpec>()

    @PostConstruct
    fun load() {
        val json = ClassPathResource("interactions.json").inputStream.readBytes()
        val specs: List<InteractionSpec> =
            objectMapper.readerForListOf(InteractionSpec::class.java).readValue(json)
        specs.forEach { byCode[it.code] = it }
    }

    fun resolve(code: String): InteractionSpec? = byCode[code]
    fun all(): List<InteractionSpec> = byCode.values.sortedBy { it.code }
    fun size(): Int = byCode.size
}
