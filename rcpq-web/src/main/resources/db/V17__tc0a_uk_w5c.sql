-- V17__tc0a_uk_w5c.sql — W5c 公积金上下限五过程配套 (2026-11-18)
-- 1) 复刻 Oracle UK_TC0A UNIQUE(AAE002, ATC900) — 备份库 user_indexes 实证(DbW5cIdx)。
--    PRC_公积金基数上下限调整提交/PRC_公积金基数下限调整 的 DUP_VAL_ON_INDEX→UPDATE
--    分支以此为触发键(同一调整年月+同一合同一行 TC0A; PG 快照违例预检 0 行 2026-11-18;
--    tc0a 不在同步集合, 无同步写入撞索引风险)。
CREATE UNIQUE INDEX IF NOT EXISTS uk_tc0a ON tc0a(aae002, atc900);

-- 2) seq_tc0a_atc0a0 首次启用(W5c 提交/下限托底发号; PG 快照存量 max=13679 为迁移值),
--    按并行期约定抬段 1e7 (seq_offset.sql 同约定; tc0a 不在同步集合无覆盖风险)
SELECT setval('seq_tc0a_atc0a0',
              GREATEST(COALESCE((SELECT max(atc0a0) FROM tc0a), 0)::bigint + 1, 10000001));
