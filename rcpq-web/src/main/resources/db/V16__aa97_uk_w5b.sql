-- V16__aa97_uk_w5b.sql — W5b 基数导入校验/提交配套 (2026-11-18)
-- 1) 复刻 Oracle UK_AA97 UNIQUE(AAE001,AAC001) — 备份库 user_indexes 实证(DbW5bIdx)。
--    PRC_基数导入提交 的 DUP_VAL_ON_INDEX→UPDATE 分支以此为触发键(调整年度+个人编号,
--    一人一年一行 AA97; 同人换合同重提=更新旧行, WHERE AAE001+ATC900 匹配 0 行静默=源忠实)。
--    PG 快照违例预检: 0 行 (2026-11-18); aa97 不在同步集合, 无同步写入撞索引风险。
CREATE UNIQUE INDEX IF NOT EXISTS uk_aa97 ON aa97(aae001, aac001);

-- 2) seq_aa97_aaa970 首次启用(W5b 导入提交发号), 按并行期约定抬段 1e7
--    (scripts/db/seq_offset.sql 同约定; aa97 不在同步集合无覆盖风险, 抬段仅为
--    PG 新发键空间可解释; seq_sweep.sql 巡检同覆盖此序列)
SELECT setval('seq_aa97_aaa970',
              GREATEST(COALESCE((SELECT max(aaa970) FROM aa97), 0)::bigint + 1, 10000001));
