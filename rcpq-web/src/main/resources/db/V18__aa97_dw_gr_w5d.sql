-- V18__aa97_dw_gr_w5d.sql — W5d 未申报清单/名册配套 (2026-11-18)
-- seq_aa97_dw_fid / seq_aa97_gr_cid 首次启用(W5d 未申报清单/名册发号), 按并行期约定抬段 1e7
-- (scripts/db/seq_offset.sql 同约定; aa97_dw/gr 不在同步集合无覆盖风险, 抬段仅为键空间可解释;
--  PG 快照存量 fid max=3363 / cid max=127364 为迁移值)
SELECT setval('seq_aa97_dw_fid',
              GREATEST(COALESCE((SELECT max(fid) FROM aa97_dw), 0)::bigint + 1, 10000001));
SELECT setval('seq_aa97_gr_cid',
              GREATEST(COALESCE((SELECT max(cid) FROM aa97_gr), 0)::bigint + 1, 10000001));
