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
-- 2026-11-21 W7: 通用导入通道(/import/sys)接入时发现 bp 族批次表序列漏抬——
-- 六序列全停在迁移数据段内(seq_tc90_id=174407 撞 tc90_bp 迁移行, bp2 序列 51119 < 表内 max 51222),
-- 首次 Excel 上传即主键冲突。bp 批次表 id 为 varchar, 需 ::bigint 转。逐条已跑(2026-11-21):
SELECT setval('seq_tc90_id',     GREATEST((SELECT COALESCE(max(id),0)::bigint+1 FROM tc90_bp),  10000001));
SELECT setval('seq_tc90_bp1_id', GREATEST((SELECT COALESCE(max(id),0)::bigint+1 FROM tc90_bp1), 10000001));
SELECT setval('seq_tc90_bp2_id', GREATEST((SELECT COALESCE(max(id),0)::bigint+1 FROM tc90_bp2), 10000001));
SELECT setval('seq_tc90_bp3_id', GREATEST((SELECT COALESCE(max(id),0)::bigint+1 FROM tc90_bp3), 10000001));
SELECT setval('seq_tc90_bp4_id', GREATEST((SELECT COALESCE(max(id),0)::bigint+1 FROM tc90_bp4), 10000001));
SELECT setval('seq_ac01_gx_id',  GREATEST((SELECT COALESCE(max(id),0)::bigint+1 FROM ac01_gx),  10000001));
-- 同坑未接: tc90_imp 模板(→TC90_IMP 表)无 ID 序列配置, 导入报"未配置目标表ID序列"(见 import-channel.md)
-- 2026-11-21 W7 重大修正: seq_tb91_atb910 抬段窗口失效——tb91 在每日同步集合, Oracle 侧键
-- 已发到 27,152,330(远超 2026-11-12 设想的"5 万级"), PG 从 1e7 发号直接撞同步行
-- (2026-11-21 06:22 同步行 27152330 > PG seq 27151966, /proc 员工导入提交 500 实证)。
-- 修正: PG 键段抬到 5e7(Oracle 27M 之上的不相交段, Oracle ~1.5 万/日, 余量数年)。
-- 其余 11 序列复核(2026-11-21): 表内 max 全部 ≤1e7+82(PG 自发号), 无同病。
-- 教训: 凡在同步集合的表, 抬段前必查 Oracle 侧实际键域(备份库 max), 不能按 1e7 一刀切。
SELECT setval('seq_tb91_atb910', 50000001);
-- 2026-11-21 W7 续: seq_tc90_qdls_id 同病(续签提交写 TC90_QDLS 主键撞迁移行, appCode=-1
-- "记录合同签订历史出错" 实证), 抬段同规则。逐条已跑(2026-11-21):
SELECT setval('seq_tc90_qdls_id', GREATEST((SELECT COALESCE(max(id),0)::bigint+1 FROM tc90_qdls), 10000001));
-- 2026-11-21 W7 专项五套批次表序列接入(/import/sys tc04/05/06/07/td04_bp, hbm 权威=SEQ_TC0x_ID):
-- seq_tc05_id(34317<max34360 落后)/seq_tc06_id(从未调用, next=1 撞 max102)/seq_tc07_id(65663<max70638)
-- 三例同病, 统一抬段(逐条已跑):
SELECT setval('seq_tc04_id', GREATEST((SELECT COALESCE(max(id),0)::bigint+1 FROM tc04_bp), 10000001));
SELECT setval('seq_tc05_id', GREATEST((SELECT COALESCE(max(id),0)::bigint+1 FROM tc05_bp), 10000001));
SELECT setval('seq_tc06_id', GREATEST((SELECT COALESCE(max(id),0)::bigint+1 FROM tc06_bp), 10000001));
SELECT setval('seq_tc07_id', GREATEST((SELECT COALESCE(max(id),0)::bigint+1 FROM tc07_bp), 10000001));
SELECT setval('seq_td04_id', GREATEST((SELECT COALESCE(max(id),0)::bigint+1 FROM td04_bp), 10000001));
-- 2026-11-21 W7 三银行到账批次表接入(/import/sys tb97_bp/bp01/bp02, hbm 权威=SEQ_TB97_BP*_ID):
-- bp 4824<max4843 / bp01 33142<max33429 均落后(撞迁移行), bp02 恰好齐平。统一抬段(已跑):
SELECT setval('seq_tb97_bp_id',   GREATEST((SELECT COALESCE(max(id),0)::bigint+1 FROM tb97_bp),  10000001));
SELECT setval('seq_tb97_bp01_id', GREATEST((SELECT COALESCE(max(id),0)::bigint+1 FROM tb97_bp01), 10000001));
SELECT setval('seq_tb97_bp02_id', GREATEST((SELECT COALESCE(max(id),0)::bigint+1 FROM tb97_bp02), 10000001));
-- 2026-11-26 §5.1 tc9w_bp04 市医保回盘线接入(/import/sys tc9w_bp04 统一引擎 + /wb/hz/import-bp
-- 定制通道, 两通道共用同一对序列, 都是 nextval)。两序列停在 Oracle 同步行段内:
--   seq_tc9w_bp04_id=965239 < 表内 max(id) 971840, seq_sys_baz002=9761839 < tc9w 族 baz002
--   全局 max 9824802(tc9w; tc9w_bp04 自身 9818010/tc9w_bp 9815785/tc9u 9815215/tc9w2 9804766),
--   首传即主键/批次键冲突(2026-11-26 e2e 实证 duplicate key pk_tc9w_bp04)。
-- 同步归属勘正(2026-11-26, work/sync.log 实证 TC9W_BP04 read=523368 upsert=523368): 每晚同步
-- 为全量自动发现 ~172 表(sync_daily.sh 无参模式), 含全部 bp 批次表族——OracleSyncJob 头注释
-- "TC9W_BP 系列不在同步集合"过时, GET /sync/tables 的 21 表仅是静态 specs 子集。TC9W_BP04
-- 202611 域的 6605 行即 Oracle 生产中月状态(ace10a='3')经同步进来, 零消耗 PG 序列——
-- 同步表抬段铁律(先查 Oracle 键域)适用:
--   id 序列: Oracle 本表键域 ~97 万, 月增 ~7 千(月度回盘批量) → 1e7 地板余量百月级, 沿用 1e7;
--   baz002 序列: 多表共享发号器, Oracle SYS_BAZ002 现 ~985 万且距 1e7 仅 ~18 万(月增数万,
--   数月即到) → 按 tb91 先例(Oracle 27M→5e7)抬 5e7 不相交段。
-- 形态=不回落式抬段(GREATEST 含序列现值, 幂等重跑不倒退)。
SELECT setval('seq_tc9w_bp04_id', GREATEST((SELECT last_value FROM seq_tc9w_bp04_id),
    (SELECT COALESCE(max(id),0)::bigint+1 FROM tc9w_bp04), 10000001));
SELECT setval('seq_sys_baz002', GREATEST((SELECT last_value FROM seq_sys_baz002),
    (SELECT COALESCE(max(baz002),0)::bigint+1 FROM tc9w), 50000001));
