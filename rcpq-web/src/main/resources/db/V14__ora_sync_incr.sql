-- V14__ora_sync_incr.sql — 增量同步模式(2026-11-12 定调: 白天按需发起, 比对后只更新差异)
-- mode: FULL=全量 UPSERT(夜间) / INCR=归并比对(白天按需, 跳过未变行只写差异)
ALTER TABLE wb_sync_log ADD COLUMN IF NOT EXISTS mode varchar(8) NOT NULL DEFAULT 'FULL';
ALTER TABLE wb_sync_log ADD COLUMN IF NOT EXISTS skipped_rows bigint NOT NULL DEFAULT 0;
COMMENT ON COLUMN wb_sync_log.mode IS '同步模式: FULL=全量 UPSERT / INCR=增量比对(跳过未变)';
COMMENT ON COLUMN wb_sync_log.skipped_rows IS 'INCR: 比对后未变化跳过的行数';
