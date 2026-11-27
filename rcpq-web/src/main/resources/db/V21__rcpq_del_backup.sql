-- V21 (桥单 20261127-029): PRC_删除员工(BC-06 清场腿) W-A 删前备份表
-- 用户裁定 W-A(2026-11-26): 忠实物理删保留, 但删前把将删行整行备份到新侧专表;
-- 本表无 Oracle 对应 → 夜间全量同步(~172 表自动发现)永不触碰。
CREATE TABLE IF NOT EXISTS rcpq_del_backup (
  id         bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  table_name varchar(64) NOT NULL,
  atc900     bigint      NOT NULL,
  row_data   jsonb       NOT NULL,
  backed_at  timestamptz NOT NULL DEFAULT now()
);
COMMENT ON TABLE rcpq_del_backup IS
  'PRC_删除员工 删前行态备份(W-A 裁定, 桥单 029): 物理删前 tc90_list/tc90/tc93/tc08/aa97/tc90_yy 六表按 ATC900 整行 jsonb 存档; 新侧专表无 Oracle 对应, 同步不覆盖';
CREATE INDEX IF NOT EXISTS idx_rcpq_del_backup_atc900 ON rcpq_del_backup (atc900);
