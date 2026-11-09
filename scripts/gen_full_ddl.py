#!/usr/bin/env python3
"""全量 DDL: 剩余全部 hbm 表 + 82 视图 + 665 序列 → V6__full_schema.sql
视图原样移植(Oracle 简单 SELECT 视图 PG 兼容; 失败项在 apply 时收集后补)"""
import os
import re

RES = "/home/ubuntu/github/rainbowpq/src/main/resources"
WORK = "/home/ubuntu/github/rainbow-rcpq/work"
OUT = "/home/ubuntu/github/rainbow-rcpq/rcpq-web/src/main/resources/db/V6__full_schema.sql"
DONE = {"AA01", "AA10", "AA12", "SYS_QUESTION", "AB01", "AB06", "AB07", "SYS_FILE",
        "AC01", "AC01_LB", "AC06", "AC0B", "AC0D", "CC0P", "TC90", "TC90_LIST"}
TYPES = {1: "CHAR", 2: "NUMERIC", 3: "NUMERIC", 4: "INTEGER", 6: "REAL", 8: "DOUBLE PRECISION",
         12: "VARCHAR", 91: "DATE", 93: "TIMESTAMP", -1: "TEXT", 2004: "BYTEA", 2005: "TEXT"}

def coldef(cm):
    ty = int(cm.group(2))
    length, scale = cm.group(3), cm.group(4)
    base = TYPES.get(ty, "TEXT")
    if base == "NUMERIC":
        return f"{cm.group(1)} NUMERIC({length or 38},{scale or 0})"
    if base in ("VARCHAR", "CHAR"):
        return f"{cm.group(1)} VARCHAR({length})" if length else f"{cm.group(1)} TEXT"
    return f"{cm.group(1)} {base}"

def table_ddl(path):
    t = open(path, encoding="utf-8", errors="replace").read()
    m = re.search(r'<class\s+name="[^"]+"[^>]*?table="([^"]*)"', t)
    if not m:
        return None
    table = m.group(1)
    cols, pks = [], []
    m2 = re.search(r'<id\s+name="[^"]+"[^>]*?type="[^"]+"(.*?)</id>', t, re.S)
    if m2:
        cm = re.search(r'<column\s+name="([^"]+)"[^>]*?sql-type="(\d+)"(?:[^>]*?length="(\d+)")?(?:[^>]*?scale="(\d+)")?[^>]*?(not-null="true")?', m2.group(1), re.S)
        if cm:
            cols.append(coldef(cm) + (" NOT NULL" if cm.group(5) else "")); pks.append(cm.group(1))
    cid = re.search(r"<composite-id>(.*?)</composite-id>", t, re.S)
    if cid:
        for km in re.finditer(r'<key-property\s+name="[^"]+"[^>]*?type="[^"]+"[^>]*?>(.*?)</key-property>', cid.group(1), re.S):
            cm = re.search(r'<column\s+name="([^"]+)"[^>]*?sql-type="(\d+)"(?:[^>]*?length="(\d+)")?(?:[^>]*?scale="(\d+)")?[^>]*?(not-null="true")?', km.group(1), re.S)
            if cm:
                cols.append(coldef(cm) + " NOT NULL"); pks.append(cm.group(1))
    for pm in re.finditer(r'<property\s+name="[^"]+"[^>]*?type="[^"]+"[^>]*?>(.*?)</property>', t, re.S):
        cm = re.search(r'<column\s+name="([^"]+)"[^>]*?sql-type="(\d+)"(?:[^>]*?length="(\d+)")?(?:[^>]*?scale="(\d+)")?[^>]*?(not-null="true")?', pm.group(1), re.S)
        if cm:
            cols.append(coldef(cm) + (" NOT NULL" if cm.group(5) else ""))
    if not cols:
        return None
    out = [f"CREATE TABLE {table} (", "    " + ",\n    ".join(cols)]
    if pks:
        out.append(f"    , CONSTRAINT PK_{table} PRIMARY KEY ({', '.join(pks)})")
    out.append(");\n")
    return "\n".join(out)

def main():
    lines = ["-- V6: 全量 schema(剩余 hbm 表 + 视图 + 全部序列)", ""]
    n_t = 0
    for dp, _, fs in os.walk(RES):
        for fn in sorted(fs):
            if not fn.endswith(".hbm.xml"):
                continue
            t = open(os.path.join(dp, fn), encoding="utf-8", errors="replace").read()
            m = re.search(r'table="([^"]*)"', t)
            if not m or m.group(1) in DONE or m.group(1).upper().startswith("V_"):
                continue  # V_* 是视图, 由备份库视图定义段创建
            d = table_ddl(os.path.join(dp, fn))
            if d:
                lines.append(d)
                n_t += 1

    # 视图
    lines.append("-- ===== 视图(Oracle 原定义移植) =====")
    views = open(os.path.join(WORK, "schema_views.txt"), encoding="utf-8").read()
    n_v = 0
    for seg in views.split("==== ")[1:]:
        name = seg.split(" ====")[0]
        body = seg.split(" ====", 1)[1].strip()
        if not body or body == "(空)":
            continue
        sql = body.rstrip(";") + ";"
        lines.append(f"-- view: {name}")
        lines.append(f"CREATE OR REPLACE VIEW {name} AS\n{sql}\n")
        n_v += 1

    # 序列(全部, IF NOT EXISTS 避免与 V2/V4 冲突)
    lines.append("-- ===== 序列(值对齐备份库) =====")
    n_s = 0
    for row in open(os.path.join(WORK, "sequences.tsv"), encoding="utf-8"):
        nm, val = row.strip().split("\t")
        lines.append(f"CREATE SEQUENCE IF NOT EXISTS {nm.lower()} START {val};")
        n_s += 1

    os.makedirs(os.path.dirname(OUT), exist_ok=True)
    open(OUT, "w", encoding="utf-8").write("\n".join(lines))
    print(f"written: tables={n_t} views={n_v} sequences={n_s}, {os.path.getsize(OUT)//1024}KB")

if __name__ == "__main__":
    main()
