-- 全序列巡检(幂等, 只抬不降): 对每个形如 seq_<表>_<列> 且表有该列的序列,
-- setval 到 表内该列 max(若更大)。执行: psql -f scripts/db/seq_sweep.sql
DO $$
DECLARE
  r RECORD;
  mx BIGINT;
  cur BIGINT;
BEGIN
  FOR r IN
    SELECT s.sequencename,substring(s.sequencename from 5) AS rest
    FROM pg_sequences s
    WHERE s.sequencename LIKE 'seq\_%'
  LOOP
    -- rest 形如 '<table>_<col>' 或 '<table>_id'; 逐表试探
    BEGIN
      EXECUTE format('SELECT max(%I) FROM %I',
        CASE WHEN r.rest LIKE '%\_id' THEN 'id'
             WHEN r.rest LIKE '%\_atc9w0' THEN 'atc9w0'
             WHEN r.rest LIKE '%\_atc9u0' THEN 'atc9u0'
             WHEN r.rest LIKE '%\_atc090' THEN 'atc090'
             WHEN r.rest LIKE '%\_atc930' THEN 'atc930'
             WHEN r.rest LIKE '%\_tc90yyid' THEN 'tc90yyid'
             ELSE regexp_replace(r.rest, '^.*_', '')
        END, substring(r.rest from 5)) INTO mx;
      EXECUTE format('SELECT last_value FROM %I', r.sequencename) INTO cur;
      IF mx IS NOT NULL AND (cur IS NULL OR cur < mx) THEN
        EXECUTE format('SELECT setval(%L, %s)', r.sequencename, mx);
        RAISE NOTICE 'setval % -> %', r.sequencename, mx;
      END IF;
    EXCEPTION WHEN OTHERS THEN
      NULL; -- 列不存在/非数字: 跳过
    END;
  END LOOP;
END $$;

-- 已知跨表命名序列(启发式覆盖不到, 人工维护):
-- seq_tc09_id      → tc09_bp.id
-- seq_tb91_atb910  → tb91.atb910
-- 首次修正(2026-11-17):
-- SELECT setval('seq_tb91_atb910', (SELECT max(atb910) FROM tb91));
-- SELECT setval('seq_tc09_id', (SELECT max(id) FROM tc09_bp));
-- 修正语句(带 ::bigint 显式转换):
-- SELECT setval('seq_tb91_atb910', (SELECT max(atb910)::bigint FROM tb91));
-- SELECT setval('seq_tc09_id',      (SELECT max(id)::bigint      FROM tc09_bp));
