-- V13__ora_sync_widen.sql — 同步表列宽对齐 Oracle(2026-11-12, 迁移时缩列致回盘写入超长)
-- 生成器: scripts/db/DbColDiff.java; 规则: varchar 长度/numeric 精度取 Oracle 侧
ALTER TABLE tc90_list ALTER COLUMN atc9wc TYPE varchar(60);
