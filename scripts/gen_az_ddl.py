#!/usr/bin/env python3
"""az 波次 DDL 生成: 12 表 + AB01_CHILD 索引表 + V_AB01_AB07 视图 + 6 序列 → rcpq-web/src/main/resources/db/V4__az_init.sql"""
import os
import re

RES = "/home/ubuntu/github/rainbowpq/src/main/resources"
BASE = f"{RES}/com/neusoft/lemis/apps/entities/baseDB"
OUT = "/home/ubuntu/github/rainbow-rcpq/rcpq-web/src/main/resources/db/V4__az_init.sql"

TABLES = {
    "AB01": f"{BASE}/A/AB01.hbm.xml", "AB06": f"{BASE}/A/AB06.hbm.xml",
    "AB07": f"{BASE}/A/AB07.hbm.xml", "SYS_FILE": f"{BASE}/S/SYS_FILE.hbm.xml",
    "AC01": f"{BASE}/A/AC01.hbm.xml", "AC01_LB": f"{BASE}/A/AC01_LB.hbm.xml",
    "AC06": f"{BASE}/A/AC06.hbm.xml", "AC0B": f"{BASE}/A/AC0B.hbm.xml",
    "AC0D": f"{BASE}/A/AC0D.hbm.xml", "CC0P": f"{BASE}/C/CC0P.hbm.xml",
    "TC90": f"{BASE}/T/TC90.hbm.xml", "TC90_LIST": f"{BASE}/T/TC90_LIST.hbm.xml",
}
TYPES = {1: "CHAR", 2: "NUMERIC", 3: "NUMERIC", 4: "INTEGER", 6: "REAL", 8: "DOUBLE PRECISION",
         12: "VARCHAR", 91: "DATE", 93: "TIMESTAMP", -1: "TEXT", 2004: "BYTEA", 2005: "TEXT"}

ID_SEQ = {  # 由序列生成主键的表(其余 assigned/客户端携带)
    "AC01": ("aac001", "SEQ_AC01_AAC001", 45073),
}

def coldef(cm):
    ty = int(cm.group(2))
    length, scale = cm.group(3), cm.group(4)
    base = TYPES.get(ty, "TEXT")
    if base == "NUMERIC":
        return f"{cm.group(1)} NUMERIC({length or 38},{scale or 0})"
    if base in ("VARCHAR", "CHAR"):
        # hbm 未声明长度的大文本列(如 base64 附件) → TEXT, 避免 255 截断
        return f"{cm.group(1)} VARCHAR({length})" if length else f"{cm.group(1)} TEXT"
    return f"{cm.group(1)} {base}"

lines = ["-- V4: az 波次 12 表 + AB01_CHILD 单位树索引 + V_AB01_AB07 视图 + 6 序列",
         "-- 由 hbm 元数据生成(2026-11-09); AB01_CHILD/视图/序列来自备份库原文语义", ""]
for tb, path in TABLES.items():
    t = open(path, encoding="utf-8", errors="replace").read()
    cols, pks = [], []
    m = re.search(r'<id\s+name="([^"]+)"[^>]*?type="([^"]+)"(.*?)</id>', t, re.S)
    if m:
        cm = re.search(r'<column\s+name="([^"]+)"[^>]*?sql-type="(\d+)"(?:[^>]*?length="(\d+)")?(?:[^>]*?scale="(\d+)")?[^>]*?(not-null="true")?', m.group(3), re.S)
        if cm:
            d = coldef(cm) + (" NOT NULL" if cm.group(5) else "")
            cols.append(d); pks.append(cm.group(1))
    cid = re.search(r"<composite-id>(.*?)</composite-id>", t, re.S)
    if cid:
        for km in re.finditer(r'<key-property\s+name="([^"]+)"[^>]*?type="([^"]+)"[^>]*?>(.*?)</key-property>', cid.group(1), re.S):
            cm = re.search(r'<column\s+name="([^"]+)"[^>]*?sql-type="(\d+)"(?:[^>]*?length="(\d+)")?(?:[^>]*?scale="(\d+)")?[^>]*?(not-null="true")?', km.group(3), re.S)
            if cm:
                cols.append(coldef(cm) + " NOT NULL"); pks.append(cm.group(1))
    for pm in re.finditer(r'<property\s+name="[^"]+"[^>]*?type="([^"]+)"[^>]*?>(.*?)</property>', t, re.S):
        cm = re.search(r'<column\s+name="([^"]+)"[^>]*?sql-type="(\d+)"(?:[^>]*?length="(\d+)")?(?:[^>]*?scale="(\d+)")?[^>]*?(not-null="true")?', pm.group(2), re.S)
        if cm:
            d = coldef(cm) + (" NOT NULL" if cm.group(5) else "")
            cols.append(d)
    lines.append(f"CREATE TABLE {tb} (")
    lines.append("    " + ",\n    ".join(cols))
    if pks:
        lines.append(f"    , CONSTRAINT PK_{tb} PRIMARY KEY ({', '.join(pks)})")
    lines.append(");\n")

# AC01 序列
lines.append("CREATE SEQUENCE SEQ_AC01_AAC001 START 45073;")
lines.append("CREATE SEQUENCE SEQ_AC06_AAC060 START 1;")
lines.append("CREATE SEQUENCE SEQ_AB01_CHILD_SEQ START 3438;")
lines.append("CREATE SEQUENCE SEQ_AB06_ID START 4470;")
lines.append("CREATE SEQUENCE SEQ_SYS_BAZ001 START 13325416;")
lines.append("CREATE SEQUENCE SEQ_SYS_BAZ002 START 9755372;")
for nm, v in (("SEQ_AC06_ID", 202), ("SEQ_AB01_AAB001", 3700), ("SEQ_AB07_ID", 21), ("SEQ_SYS_FILE_ID", 121),
              ("SEQ_AC0B_AAC0B0", 1), ("SEQ_AC0D_AAC0D0", 1), ("SEQ_CC0P_ACC0P0", 41),
              ("SEQ_TC90_ATC900", 70484), ("SEQ_TC90_LIST_IKEY", 83753)):
    lines.append(f"CREATE SEQUENCE {nm} START {v};")
lines.append("")

# AB01_CHILD 单位树展开索引(原 PRC_CREATE_CHILD 重建目标, 列取自过程 INSERT)
lines.append("""-- 单位树展开索引(根节点扁平化, 原 AB01_CHILD/CONNECT BY 语义)
CREATE TABLE AB01_CHILD (
    SEQ      NUMERIC(10,0),
    AAB001   NUMERIC(16,0),
    AAB004   VARCHAR(200),
    ID       VARCHAR(48),
    AAE011   VARCHAR(24),
    LVL      NUMERIC(4,0),
    ROOT     VARCHAR(48),
    CONSTRAINT PK_AB01_CHILD PRIMARY KEY (SEQ)
);

-- 单位主联系人视图(原 V_AB01_AB07 定义原文移植)
CREATE OR REPLACE VIEW v_ab01_ab07 AS
SELECT t.aab001,
       a.aab004,
       a.AAE007,
       a.AAE006,
       t.AAC003,
       t.id
FROM ab01 a, ab07 t
WHERE a.aab001 = t.aab001
  AND t.AAB078 = '1';
""")

os.makedirs(os.path.dirname(OUT), exist_ok=True)
open(OUT, "w", encoding="utf-8").write("\n".join(lines))
print(f"written {OUT}, {len(lines)} lines, tables={len(TABLES)}")
