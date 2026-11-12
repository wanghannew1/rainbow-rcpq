-- V15__ora_sync_date_ts.sql — 同步表 date 列对齐 Oracle DATE 语义(2026-11-12, 已应用)
-- 原因: PG date 截断时间部分, Oracle DATE 存量含时间(如 tc90.atc90e 10:58:03),
--       增量比对按毫秒大面积误判; date→timestamp 无损扩展, 存量 00:00 不变
-- 注意: 25 列因被视图依赖(v_last_ht 等)无法 ALTER, 有意保留 date ——
--       增量比对对这批列按"日期部分"容忍时间差异(OracleSyncJob.eqCol), 列单:
--       tc90: aac006,aac007,aac181,aac184,aac185,aaa801,atc90ab,atc90ac,atc90ae,
--             atc90ak,atc90al,atc90au,atc90ax,atc90b,atc90c,atc90d,atc90e,atc90i,
--             atc90j,atc90q,bae036; ab01: aab00b,aab00c,aab010,aab011
ALTER TABLE tc90_list ALTER COLUMN aac006 TYPE timestamp;
ALTER TABLE tc90_list ALTER COLUMN aac181 TYPE timestamp;
ALTER TABLE tc90_list ALTER COLUMN aac007 TYPE timestamp;
ALTER TABLE tc90_list ALTER COLUMN aac184 TYPE timestamp;
ALTER TABLE tc90_list ALTER COLUMN aac185 TYPE timestamp;
ALTER TABLE tc90_list ALTER COLUMN atc90b TYPE timestamp;
ALTER TABLE tc90_list ALTER COLUMN atc90c TYPE timestamp;
ALTER TABLE tc90_list ALTER COLUMN atc90d TYPE timestamp;
ALTER TABLE tc90_list ALTER COLUMN atc90e TYPE timestamp;
ALTER TABLE tc90_list ALTER COLUMN atc90i TYPE timestamp;
ALTER TABLE tc90_list ALTER COLUMN atc90j TYPE timestamp;
ALTER TABLE tc90_list ALTER COLUMN bae036 TYPE timestamp;
ALTER TABLE tc90_list ALTER COLUMN atc90q TYPE timestamp;
ALTER TABLE tc90_list ALTER COLUMN atc90ab TYPE timestamp;
ALTER TABLE tc90_list ALTER COLUMN atc90ac TYPE timestamp;
ALTER TABLE tc90_list ALTER COLUMN atc90ae TYPE timestamp;
ALTER TABLE tc90_list ALTER COLUMN atc90ak TYPE timestamp;
ALTER TABLE tc90_list ALTER COLUMN atc90al TYPE timestamp;
ALTER TABLE tc90_list ALTER COLUMN atc90au TYPE timestamp;
ALTER TABLE tc90_list ALTER COLUMN atc90ax TYPE timestamp;
ALTER TABLE tc90_list ALTER COLUMN aaa801 TYPE timestamp;
ALTER TABLE ac01 ALTER COLUMN aac006 TYPE timestamp;
ALTER TABLE ac01 ALTER COLUMN aac181 TYPE timestamp;
ALTER TABLE ac01 ALTER COLUMN aac007 TYPE timestamp;
ALTER TABLE ac01 ALTER COLUMN aac184 TYPE timestamp;
ALTER TABLE ac01 ALTER COLUMN aac185 TYPE timestamp;
ALTER TABLE ab01 ALTER COLUMN bae036 TYPE timestamp;
