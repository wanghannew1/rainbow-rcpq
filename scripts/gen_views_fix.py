#!/usr/bin/env python3
"""V6 视图修复: 提取未建成视图 DDL，清洗 Oracle 工件后生成并应用 V8 迁移。

失败根因(2026-11-09 实测):
1. Oracle 导出 DDL 含全角标点(，) → CREATE 静默失败(灌库 ON_ERROR_STOP=0)
2. 视图内 --注释行带分号截断朴素切分 → 先剥注释再按 ; 切分
3. Oracle 方言: sysdate→now(), MINUS→EXCEPT
仍挂起: PKG_COMMON 包函数依赖 / decode() —— 随 td/c/td/d 波次补(清单 work/pending_views.txt)
"""
import re
import subprocess
import os
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
DDL = (ROOT / "rcpq-web/src/main/resources/db/V6__full_schema.sql").read_text(encoding="utf-8")
OUT = ROOT / "rcpq-web/src/main/resources/db/V8__views_fix.sql"
PENDING = ROOT / "work/pending_views.txt"
ENV = {**os.environ, "PGPASSWORD": "rcpq_dev"}

def pg(sql: str):
    r = subprocess.run(["psql", "-h", "localhost", "-U", "rcpq", "-d", "rainbow_rcpq",
                        "-tA", "-v", "ON_ERROR_STOP=1", "-c", sql],
                       capture_output=True, text=True, env=ENV)
    return (r.returncode, (r.stderr or "").strip().splitlines()[0:1])

def strip_comments(text: str) -> str:
    text = re.sub(r'/\*.*?\*/', ' ', text, flags=re.S)
    return re.sub(r'^\s*--.*$', '', text, flags=re.M)

def sanitize(stmt: str) -> str:
    s = (stmt.replace("，", ",").replace("（", "(").replace("）", ")").replace("；", ";"))
    s = re.sub(r'\bsysdate\b', 'now()', s, flags=re.I)
    s = re.sub(r'\bMINUS\b', 'EXCEPT', s, flags=re.I)
    return s

def main():
    r = subprocess.run(["psql", "-h", "localhost", "-U", "rcpq", "-d", "rainbow_rcpq", "-tA",
                        "-c", "select viewname from pg_views where schemaname='public'"],
                       capture_output=True, text=True, env=ENV)
    have = set(r.stdout.upper().split())
    clean = strip_comments(DDL)
    blocks = {}
    for m in re.finditer(r'CREATE OR REPLACE VIEW ([A-Za-z0-9_]+)(.*?);', clean, re.S | re.I):
        blocks.setdefault(m.group(1).upper(), m.group(0))
    missing = [v for v in blocks if v not in have]

    # Oracle 内建兼容垫片(decode/to_char(number)/to_number(text)); PKG_COMMON/业务函数不在垫片范围
    # decode 用 "any" 变参 + text 域比较(视图内均为状态码等值比较, 与 Oracle NULL=NULL 语义一致)
    shims = ["""CREATE OR REPLACE FUNCTION public.decode(VARIADIC p_args "any")
RETURNS text LANGUAGE plpgsql IMMUTABLE AS $fn$
DECLARE a text[]; n int;
BEGIN
  a := p_args::text[];
  n := array_length(a, 1);
  IF n < 3 THEN RETURN NULL; END IF;
  FOR i IN 2 .. n - 1 BY 2 LOOP
    IF a[1] IS NOT DISTINCT FROM a[i] THEN
      RETURN a[i + 1];
    END IF;
  END LOOP;
  IF n % 2 = 1 THEN RETURN a[n]; END IF;
  RETURN NULL;
END $fn$;""",
    "CREATE OR REPLACE FUNCTION public.to_char(n numeric) RETURNS text "
    "LANGUAGE sql IMMUTABLE AS $fn$ SELECT n::text $fn$;",
    "CREATE OR REPLACE FUNCTION public.to_number(s text) RETURNS numeric "
    "LANGUAGE sql IMMUTABLE AS $fn$ SELECT s::numeric $fn$;"]
    shim_applied = []
    for s in shims:
        rc, _ = pg(s)
        if rc == 0:
            shim_applied.append(s)

    fixed, pending = [], []
    for v in missing:
        stmt = sanitize(blocks[v])
        rc, err = pg(stmt)
        if rc == 0:
            fixed.append(v)
        else:
            reason = err[0] if err else "unknown"
            pending.append(f"{v}\t{reason[:120]}")
    OUT.write_text(
        "-- V8: V6 视图修复(Oracle 导出全角标点致 CREATE 静默失败; sysdate/MINUS/decode/to_char 方言清洗)\n"
        "-- 生成: scripts/gen_views_fix.py; 仍挂起视图见 work/pending_views.txt\n\n"
        + "".join(s + "\n\n" for s in shim_applied)
        + "".join("\n" + sanitize(blocks[v]) + "\n" for v in fixed),
        encoding="utf-8")
    PENDING.write_text(
        "# PG 未建成视图清单(2026-11-09 gen_views_fix.py 实测; 垫片后余量)\n"
        + "".join(l + "\n" for l in pending), encoding="utf-8")
    print(f"shims={len(shim_applied)} fixed={len(fixed)} pending={len(pending)}")
    for l in pending:
        print("  PENDING", l)

if __name__ == "__main__":
    main()
