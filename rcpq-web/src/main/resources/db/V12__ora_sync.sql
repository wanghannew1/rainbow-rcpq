-- V12__ora_sync.sql — Oracle→PG 每日同步(并行运行期, 2026-11-12 定调: 新旧系统并行,
-- 新系统每日从 Oracle 旧库同步; 同步表=Oracle 原始表, PG 侧只读不改造, 改动会被覆盖)
-- 引擎: com.jlrc.rcpq.web.sync.OracleSyncJob / 触发: POST /sync/run + 定时(sync.cron)

CREATE TABLE IF NOT EXISTS wb_sync_log (
  id          bigserial PRIMARY KEY,
  started     timestamptz NOT NULL DEFAULT now(),
  table_name  varchar(64) NOT NULL,
  read_rows   bigint NOT NULL DEFAULT 0,
  upsert_rows bigint NOT NULL DEFAULT 0,
  insert_rows bigint NOT NULL DEFAULT 0,
  update_rows bigint NOT NULL DEFAULT 0,
  duration_ms bigint NOT NULL DEFAULT 0,
  status      varchar(16) NOT NULL,   -- OK / ERROR
  error       text
);
CREATE INDEX IF NOT EXISTS idx_wb_sync_log_started ON wb_sync_log (started DESC);
COMMENT ON TABLE wb_sync_log IS 'Oracle→PG 每日同步运行日志(每表一行/次): 读行数/插入/更新/耗时/状态';
COMMENT ON COLUMN wb_sync_log.status IS '状态: OK=成功 ERROR=失败(error 带信息)';
