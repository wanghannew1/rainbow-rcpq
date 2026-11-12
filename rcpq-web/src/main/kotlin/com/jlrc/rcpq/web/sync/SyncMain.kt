package com.jlrc.rcpq.web.sync

import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.datasource.DriverManagerDataSource
import kotlin.system.exitProcess

/**
 * 外挂同步入口(生产推荐方式): 独立 JVM 进程跑同步, 与业务服务进程隔离——
 * 同步失败/OOM 不影响线上业务; 并行期结束摘 crontab 即退役, 业务包无需改动。
 * 与进程内 /sync/run 复用同一 OracleSyncJob 引擎(单一实现, 不漂移)。
 *
 * 用法: java -Dloader.main=com.jlrc.rcpq.web.sync.SyncMain -jar rcpq-web.jar 表名...
 *   (无参=全量 15 表 FULL; --incr=增量比对, 白天业务期按需, 只写差异行;
 *    PG/Oracle 连接取环境变量, 见 sync_daily.sh)
 * 退出码: 0=全 OK, 1=有表失败/被防重入拒绝 — crontab 可据此告警。
 */
fun main(args: Array<String>) {
    val pgUrl = System.getenv("SYNC_PG_URL") ?: "jdbc:postgresql://localhost:5432/rainbow_rcpq?reWriteBatchedInserts=true"
    val pgUser = System.getenv("SYNC_PG_USER") ?: "rcpq"
    val pgPass = System.getenv("SYNC_PG_PASS") ?: "rcpq_dev"
    val ds = DriverManagerDataSource(pgUrl, pgUser, pgPass)
    // 胖 jar 合并会丢 SPI 注册文件, 显式加载两库驱动
    Class.forName("org.postgresql.Driver")
    Class.forName("oracle.jdbc.OracleDriver")
    val job = OracleSyncJob(JdbcTemplate(ds))
    // 连接参数从环境变量取(application.yml 只服务业务进程), Oracle 侧默认与 scripts/db 工具一致
    job.oraUrl = System.getenv("SYNC_ORA_URL") ?: "jdbc:oracle:thin:@172.16.0.60:1521:orcl"
    job.oraUser = System.getenv("SYNC_ORA_USER") ?: "ccrcpq"
    job.oraPass = System.getenv("SYNC_ORA_PASS") ?: "ccrc"

    val incr = args.any { it.equals("--incr", true) || it.equals("-i", true) }
    val tables = args.filterNot { it.startsWith("-") }.takeIf { it.isNotEmpty() }
    val r = job.syncTables(tables, if (incr) "INCR" else "FULL")
    println(r["msg"])
    (r["tables"] as? List<*>)?.forEach { t ->
        val m = t as Map<*, *>
        println("  ${m["table"]}: ${m["status"]} read=${m["read"]} upsert=${m["upsert"]} ${m["ms"]}ms" +
            (if (m["status"] == "ERROR") " err=${m["error"]}" else ""))
    }
    if ((r["appCode"] as Int) != 1) exitProcess(1)
}
