package com.jlrc.rcpq.web.sync

import com.jlrc.rcpq.web.AuditLog
import org.springframework.beans.factory.annotation.Value
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * 同步触发面: POST /sync/run(手动全量/指定表) + GET /sync/status + GET /sync/tables。
 * 定时: sync.cron 属性(默认关闭, 生产建议每日 01:30 即 "0 30 1 * * *" 且 sync.enabled=true)。
 * 手动触发同样入 wb_audit_log 留痕。
 */
@RestController
@RequestMapping("/sync")
class SyncController(
    private val job: OracleSyncJob,
    private val pg: JdbcTemplate,
    private val audit: AuditLog,
    @Value("\${sync.enabled:false}") private val enabled: Boolean,
) {

    data class RunReq(val tables: List<String>? = null, val aae011: String? = null)

    @PostMapping("/run")
    fun run(@RequestBody(required = false) r: RunReq?): Map<String, Any?> {
        audit.log(r?.aae011, "sync:run", (r?.tables ?: emptyList()).joinToString(","), r)
        return job.syncTables(r?.tables)
    }

    @GetMapping("/status")
    fun status(): Map<String, Any?> = mapOf(
        "running" to job.isRunning(), "enabled" to enabled,
        "last" to pg.queryForList(
            """SELECT table_name, read_rows, upsert_rows, duration_ms, status, error, started
               FROM wb_sync_log ORDER BY id DESC LIMIT 30"""))

    @GetMapping("/tables")
    fun tables() = mapOf("specs" to job.tableStatus())

    /** 每日定时同步(sync.enabled=true 且 sync.cron 生效时); 每表结果落 wb_sync_log */
    @Scheduled(cron = "\${sync.cron:0 30 1 * * *}")
    fun daily() {
        if (enabled) job.syncTables(null)
    }
}
