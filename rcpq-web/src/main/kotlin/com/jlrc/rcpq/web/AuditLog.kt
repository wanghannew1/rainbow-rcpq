package com.jlrc.rcpq.web

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional

/**
 * 操作留痕(2026-11-11 定调: 用户操作一律留痕, 避免纠纷不可追溯)。
 * 调用面: /proc 过程网关、/interaction 交互网关、/wb 工作台写操作、报盘导出。
 * 配套标准: 删除一律逻辑删除, 禁止物理 DELETE(本模块自身只 INSERT)。
 */
@Component
class AuditLog(private val jdbcTemplate: JdbcTemplate, private val om: ObjectMapper) {

    /** 记一条操作; detail 任意对象(序列化为 JSON, 截断 2000 字), target 为主对象标识。
     *  REQUIRES_NEW(2026-11-20): 留痕独立事务提交 — 业务端点补 @Transactional 后,
     *  失败回滚不再连带吞掉操作留痕(失败操作恰是最需要追溯的)。 */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    fun log(aae011: String?, action: String, target: String?, detail: Any?, ip: String? = null) {
        val json = runCatching {
            when (detail) {
                null -> null
                is String -> detail
                else -> om.writeValueAsString(detail)
            }
        }.getOrNull()?.take(2000)
        runCatching {
            jdbcTemplate.update(
                """INSERT INTO wb_audit_log (aae011, action, target, detail, app_ip)
                   VALUES (?, ?, ?, ?, ?)""",
                aae011?.take(32), action.take(64), target?.take(200), json, ip?.take(64))
        } // 留痕失败不阻断业务(审计写失败仅吞异常; 表缺失等由启动检查暴露)
    }
}
