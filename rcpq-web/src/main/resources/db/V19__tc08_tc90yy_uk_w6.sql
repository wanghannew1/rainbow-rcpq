-- V19__tc08_tc90yy_uk_w6.sql — W6 散件收口配套 (2026-11-18)
-- 1) 复刻 Oracle UK_TC08 UNIQUE(AAE000, ATC900, AAA000) — 备份库 user_indexes 实证(DbW6Idx)。
--    PRC_保险补收基数_个人 的 DUP_VAL_ON_INDEX→静默幂等分支以此为触发键(同人同险种同补收
--    年份一条 TC08; PG 快照违例预检 0 行 2026-11-18; tc08 不在同步集合)。
CREATE UNIQUE INDEX IF NOT EXISTS uk_tc08 ON tc08(aae000, atc900, aaa000);

-- 2) 复刻 Oracle UK_TC90_YY UNIQUE(SAFESIGN, ATC900) — 参保异议赋值先 DELETE 后 INSERT,
--    正常路径不触发, 复刻为并发防御。启用前数据修复: PG 侧历史会话产生的 4 组重复异议行
--    (safesign='2', 8 行完全同质 back=0/ins=0/cb=2/aae002=202611, tc90_yy 不在同步集合),
--    保留后插 tc90yyid 较大者, 物理删较早 4 行(一次性修复, 幂等; 非业务删除)。
DELETE FROM tc90_yy WHERE tc90yyid IN (19506, 19507, 19508, 19509);
CREATE UNIQUE INDEX IF NOT EXISTS uk_tc90_yy ON tc90_yy(safesign, atc900);

-- 3) seq_tc08_atc080 首次启用(W6 补收基数发号; PG 快照 last=131053 为迁移值),
--    按并行期约定抬段 1e7 (seq_offset.sql 同约定)
SELECT setval('seq_tc08_atc080',
              GREATEST(COALESCE((SELECT max(atc080) FROM tc08), 0)::bigint + 1, 10000001));
