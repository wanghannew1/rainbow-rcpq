-- seq_offset.sql — 并行运行期 PG 序列抬段(防两库独立发号撞主键)
-- 2026-11-12: 同步机制配套。两库序列各自发号, 同 PK 互覆是并行运行最大的覆盖风险。
-- 方案: PG 发号段抬到 10,000,000 以上(当前业务主键最大 ~10 万段, 1e7 段余量充足)。
-- 注意: 抬段后 PG 新建业务键从 1e7 起, 跳段是刻意的、可解释的(见 five-insurance-migration.md)。
-- 若 Oracle 侧业务键未来逼近 1e7(当前 5 万级, 数十年内不会), 再抬窗口。
-- 已核对这些序列名=对应实体 @SequenceGenerator / pg_sequences(2026-11-12):
--   TC90=SEQ_TC90_ATC900, AC01=SEQ_AC01_AAC001, TB90=SEQ_TB90_ATB900, TB91=SEQ_TB91_ATB910,
--   TB93=SEQ_TB93_ATB930, TC93=SEQ_TC93_ATC930, TB97=SEQ_TB97_ATB970, TB99=SEQ_TB99_ATB990,
--   AB01=SEQ_AB01_AAB001, TC06=SEQ_TC06_ATC060, TC92=SEQ_TC92_ATC920, CE10=SEQ_CE10_ACE100,
--   TC90_YY=SEQ_TC90_YY_TC90YYID, TC90_LIST IKEY 见表核对(如为 identity 则无需抬)
-- 用法(每序列一条, ORACLE 侧同步不加表则无需改动 Oracle):
--   SELECT setval('seq_tc90_atc900', GREATEST((SELECT COALESCE(max(atc900),0)+1 FROM tc90), 10000001));
--   ... 逐序列套用, 执行前先跑右侧 SELECT max 核对当前值
-- 2026-11-12 开发库执行记录: 见 git 提交说明(逐条已跑)
SELECT setval('seq_tc90_atc900',   GREATEST((SELECT COALESCE(max(atc900),0)::bigint+1 FROM tc90), 10000001));
SELECT setval('seq_ac01_aac001',   GREATEST((SELECT COALESCE(max(aac001),0)::bigint+1 FROM ac01), 10000001));
SELECT setval('seq_tb90_atb900',   GREATEST((SELECT COALESCE(max(atb900),0)::bigint+1 FROM tb90), 10000001));
SELECT setval('seq_tb91_atb910',   GREATEST((SELECT COALESCE(max(atb910),0)::bigint+1 FROM tb91), 10000001));
SELECT setval('seq_tb93_atb930',   GREATEST((SELECT COALESCE(max(atb930),0)::bigint+1 FROM tb93), 10000001));
SELECT setval('seq_tc93_atc930',   GREATEST((SELECT COALESCE(max(atc930),0)::bigint+1 FROM tc93), 10000001));
SELECT setval('seq_tb97_atb970',   GREATEST((SELECT COALESCE(max(atb970),0)::bigint+1 FROM tb97), 10000001));
SELECT setval('seq_tb99_atb990',   GREATEST((SELECT COALESCE(max(atb990),0)::bigint+1 FROM tb99), 10000001));
SELECT setval('seq_ab01_aab001',   GREATEST((SELECT COALESCE(max(aab001),0)::bigint+1 FROM ab01), 10000001));
SELECT setval('seq_tc06_atc060',   GREATEST((SELECT COALESCE(max(atc060),0)::bigint+1 FROM tc06), 10000001));
SELECT setval('seq_tc92_atc920',   GREATEST((SELECT COALESCE(max(atc920),0)::bigint+1 FROM tc92), 10000001));
SELECT setval('seq_ce10_ace100',   GREATEST((SELECT COALESCE(max(ace100),0)::bigint+1 FROM ce10), 10000001));
SELECT setval('seq_tc90_yy_tc90yyid', GREATEST((SELECT COALESCE(max(tc90yyid),0)::bigint+1 FROM tc90_yy), 10000001));
-- 2026-11-18 W5b: seq_aa97_aaa970 首次启用(基数导入提交发号), 由 V16__aa97_uk_w5b.sql 自动抬段
--   (aa97 不在同步集合无覆盖风险, 抬段仅为键空间可解释; aa97_bp.id 由导入面显式指定不走序列)
