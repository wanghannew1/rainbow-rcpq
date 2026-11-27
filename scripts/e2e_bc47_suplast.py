#!/usr/bin/env python3
"""BC-47 保险补退第二波 双过程 探针·新旧同判 — 桥单 20261127-047。

对象: PKG_保险补退.PRC_保险上月补退明细(L1723-2905) + PRC_保险补收基数(单位版, L1012-1334)。
旧侧真值: RCPQ_TEST 场包体 VALID(DBMS_DDL 重编译 2026-11-27 零错误), SupLastOld.java 实弹捕获
  (scripts/db/work/bc47_old_probe.txt): E 报错/Z 零操作/A 门开全差异灯/B 离职未参保 + 单位版
  三段(Run1 反转守卫 return 0 行/Run2 医保 2098 行/Run3 公积金 2098 行; 经办人隔离 lvf)。
新侧: 同夹具(克隆源 tb93@39737/ab01@2239/tc90@53031, 键改写 20990971/20990972/20990701)
  经 /proc 调用, TC07/TC08 行逐列对照(除 atc070/aae036/baz001/baz002/atc080 时变列)。
零残留清场。用法: python3 scripts/e2e_bc47_suplast.py
"""
import json
import os
import subprocess
import sys
import urllib.request
from decimal import Decimal

PG = "postgresql://rcpq:rcpq_dev@localhost:5432/rainbow_rcpq"
BASE = "http://localhost:8080"
ATC900 = 20990701
MARK = "补退探针员卷47"

PASS, FAIL = [], []


def rec(name, ok, detail=""):
    (PASS if ok else FAIL).append(name)
    print(("✅" if ok else "❌"), name, ("— " + detail if detail else ""))


def sql(q):
    r = subprocess.run(["psql", PG, "-tAc", q], capture_output=True, text=True)
    return (r.stdout or "").strip()


def sql_json(q):
    out = sql(f"SELECT row_to_json(t) FROM ({q}) t")
    return json.loads(out) if out else None


def proc(name, params):
    from urllib.parse import quote
    req = urllib.request.Request(BASE + "/proc/" + quote(name, safe=""),
                                 data=json.dumps(params).encode(),
                                 headers={"Content-Type": "application/json"})
    with urllib.request.urlopen(req, timeout=60) as r:
        return json.load(r)


def clone(table, where, overrides):
    """全行克隆+键改写(等价旧侧 ROWTYPE 克隆): SELECT row_to_json → 改键 → 全列 INSERT"""
    row = sql_json(f"SELECT * FROM {table} WHERE {where}")
    assert row, f"克隆源缺失: {table} WHERE {where}"
    row.update(overrides)
    cols, vals = [], []
    for k, v in row.items():
        cols.append(k)
        vals.append("NULL" if v is None else "'" + str(v).replace("'", "''") + "'")
    sql(f"INSERT INTO {table} ({','.join(cols)}) VALUES ({','.join(vals)})")


C1A = "1、参保时间差异：申报参保年月为【209901】，实际参保年月为【209902】。\r2、农村户口：申报参保年月为【209901】，实际参保年月为【209902】。\r3、209903征缴与工资金额差异：征缴金额为【500】，工资金额为【900】。\r"
C2A = "1、参保时间差异：申报参保年月为【209901】，实际参保年月为【209902】。\r2、209904征缴与工资金额差异：征缴金额为【1335】，工资金额为【1790】。\r"
C3A = "1、参保时间差异：申报参保年月为【209901】，实际参保年月为【209902】。\r2、基数不符。\r3、209903征缴与工资金额差异：征缴金额为【70】，工资金额为【100】。\r"
C1B = "1、209903征缴与工资金额差异：征缴金额为【500】，工资金额为【900】。\r2、未参保离职：申报参保年月为【209901】。\r"
C2B = "1、209904征缴与工资金额差异：征缴金额为【1335】，工资金额为【1790】。\r2、未参保离职：申报参保年月为【209901】。\r"
C3B = "1、209903征缴与工资金额差异：征缴金额为【70】，工资金额为【100】。\r2、未参保离职：申报参保年月为【209901】。\r"

BASE_ROW = {
    "aae002": "209906", "aae003": "209905", "atc70x": "1", "atc70y": "1",
    "atc900": "20990701", "aac001": "998072", "aaa001": "1109134186",
    "aaa004": "801202010727", "aaa005": "22000011000094128037",
    "aac003": MARK, "aac002": "220183199906018801", "atb930": "20990971",
    "atb931": "补退探针单元卷47", "atb900": "20990972", "aab001": "20990971",
    "aab004": "吉林省假肢康复中心", "aaa030": "1",
    "aaa031": "吉林省彩虹人才开发资源服务有限公司", "atc073": "5",
    "atc07z": "0", "aae011": "jiaq", "aae017": "4101", "aae019": "贾悫",
    "aae020": "吉林省彩虹人才开发咨询服务有限公司", "aae022": "410100000000",
    "datasource": "30",
}
OLD_A = {**BASE_ROW,
         "bba001": "-260", "baa001": "-180", "bba003": "-116", "baa003": "-112",
         "bba002": "-240", "bba004": "-220", "bba005": "-200", "baa002": "-195",
         "atb90t": "-13", "daa001": "0", "daa002": "-45", "caa001": "791", "caa002": "703",
         "bba200": "-258", "baa200": "216", "atc9eh": "-87",
         "comm_1": C1A, "comm_2": C2A, "comm_3": C3A}
OLD_B = {**BASE_ROW,
         "bba001": "-480", "baa001": "-415", "bba003": "-337", "baa003": "-349",
         "bba002": "-658", "bba004": "-646", "bba005": "-634", "baa002": "-651",
         "atb90t": "-34", "daa001": "-35", "daa002": "-70", "caa001": "-197", "caa002": "-154",
         "bba200": "-2986", "baa200": "-1569", "atc9eh": "-4660",
         "comm_1": C1B, "comm_2": C2B, "comm_3": C3B}
OLD_TC08 = {
    "aae002": "209906", "aae000": "2098", "atc900": "20990701", "aac001": "998072",
    "aac003": MARK, "aac002": "220183199906018801", "atb930": "20990971",
    "atb931": "补退探针单元卷47", "atb900": "20990972", "aab001": "20990971",
    "aab004": "吉林省假肢康复中心", "aaa030": "1",
    "aaa031": "吉林省彩虹人才开发资源服务有限公司",
    "ksny": "209801", "jsny": "209812", "aaa300": "4417",
    "aae011": "lvf", "aae017": "4101", "aae019": "吕菲",
    "aae020": "吉林省彩虹人才开发咨询服务有限公司", "aae022": "410100000000",
}


def eq(expected, got):
    return Decimal(expected) == Decimal(got)


def cmp_row(expected, row, skip, tag):
    bad = []
    for k, v in expected.items():
        g = (row or {}).get(k)
        if g is None:
            bad.append(f"{k}=NULL(期望{v})")
            continue
        gs = str(g)
        try:
            same = eq(v, gs)
        except Exception:
            same = v == gs
        if not same:
            bad.append(f"{k}: 期望{v} 实得{gs}")
    rec(f"{tag} 逐列对照({len(expected)}列, 跳过{','.join(skip)})", not bad,
        "；".join(bad[:6]) if bad else "零真偏差")


def fixture():
    clone("tb93", "atb930=39737",
          {"atb930": "20990971", "atb900": "20990972", "aab001": "20990971",
           "atb931": "补退探针单元卷47", "aaa803": "1"})
    clone("ab01", "aab001=2239", {"aab001": "20990971", "aab004": "补退探针单位卷47"})
    clone("tc90", f"atc900=53031", {
        "atc900": "20990701", "aab001": "20990971", "atb930": "20990971",
        "atb900": "20990972", "aac001": "998072", "aac002": "220183199906018801",
        "aac003": MARK, "atb90r": "3", "atc90g": "1", "aae00p": "1",
        "atc90p": "0", "aaa201": "209901", "aaa203": "209902", "aaa208": "209905",
        "aaa209": "209906", "aae002": "209905", "aae003": "209901",
        "atc90pa": "0", "aaa601": "209901", "aaa603": "209902", "aaa608": "209905",
        "aaa110": "1299", "aaa803": "1",
        "atc90w": "0", "aaa501": "209901", "aaa503": "209902", "aaa508": "209905",
        "aaa120": "1299", "aac008": "1127", "aae011": "jiaq"})
    tb90 = sql_json("SELECT * FROM tb90 WHERE aab001=(SELECT aab001 FROM tb93 WHERE atb930=39737) ORDER BY atb900 LIMIT 1")
    tb90.update({"atb900": "20990972", "aab001": "20990971"})
    cols, vals = [], []
    for k, v in tb90.items():
        cols.append(k)
        vals.append("NULL" if v is None else "'" + str(v).replace("'", "''") + "'")
    sql(f"INSERT INTO tb90 ({','.join(cols)}) VALUES ({','.join(vals)})")
    # AC10 三月
    for i, (ny, b1, b3, a1, a3, b2, b4, b5, t, a2, c1, c2) in enumerate([
            (209901, 100, 10, 50, 5, 90, 80, 70, 2, 60, 30, 20),
            (209902, 60, 6, 30, 3, 50, 40, 30, 1, 35, 20, 15),
            (209903, 10, 1, 5, 1, 8, 6, 4, 1, 6, 12, 9)], start=1):
        sql(f"INSERT INTO ac10 (id, aac101, atc900, atc932, bba001, bba003, baa001, baa003, "
            f"bba002, bba004, bba005, atb90t, baa002, caa001, caa002) VALUES "
            f"(209907000{i},'1',{ATC900},{ny},{b1},{b3},{a1},{a3},{b2},{b4},{b5},{t},{a2},{c1},{c2})")


def fixture_diffs():
    """征缴/工资差异夹具(照旧侧场序: Z 场在先, 此批在 Z 后插)"""
    sql(f"INSERT INTO tj0a1 (aae002, atc900, atj000, atb930, aac001, bba001, bba003, baa001, baa003, atj0a7) "
        f"VALUES (209903,{ATC900},'1',20990971,998072,110,120,130,140,500)")
    sql(f"INSERT INTO tj0b1 (aae002, atc900, atj000, atb930, aac001, bba002, bba004, bba005, atb90t, baa002, daa001) "
        f"VALUES (209904,{ATC900},'1',20990971,998072,310,320,330,10,350,15)")
    sql(f"INSERT INTO tj0d1 (aae002, atc900, atj000, atb930, aac001, caa001, caa002, atj0d5) "
        f"VALUES (209903,{ATC900},'1',20990971,998072,40,30,70)")
    sql(f"INSERT INTO tj0d1 (aae002, atc900, atj000, atb930, aac001, caa001, caa002, atj0d5) "
        f"VALUES (209905,{ATC900},'2',20990971,998072,888,777,1665)")
    # TC93 两月(已确认工资行)
    sql(f"INSERT INTO tc93 (atc930, atb900, atc900, atc931, atc937, aae214, bba001, bba003, baa001, baa003, caa001, caa002) "
        f"VALUES (2099070001,20990972,{ATC900},209903,'1','1',210,220,230,240,55,45)")
    sql(f"INSERT INTO tc93 (atc930, atb900, atc900, atc931, atc937, aae214, bba002, bba004, bba005, atb90t, baa002, atc93bc, atc93bd, caa001, caa002) "
        f"VALUES (2099070002,20990972,{ATC900},209904,'1','1',410,420,430,20,450,25,35,65,50)")


def tc07_row():
    return sql_json(f"SELECT * FROM tc07 WHERE aae002=209906 AND atc900={ATC900} ORDER BY atc070")


LAST = "PKG_保险补退.PRC_保险上月补退明细"
UNIT = "PKG_保险补退.PRC_保险补收基数"


def main():
    # ── 夹具 ──
    for t in ("tc07", "ac10", "tc93", "tj0a1", "tj0b1", "tj0d1", "tc08"):
        sql(f"DELETE FROM {t} WHERE atc900={ATC900}")
    sql(f"DELETE FROM tc08 WHERE aac001=998072")
    for t, w in (("tc90", f"atc900={ATC900}"), ("tb93", "atb930=20990971"),
                 ("tb90", "atb900=20990972 AND aab001=20990971"), ("ab01", "aab001=20990971")):
        sql(f"DELETE FROM {t} WHERE {w}")
    fixture()
    n = int(sql(f"SELECT COUNT(*) FROM tc90 WHERE atc900={ATC900}"))
    rec("夹具在位", n == 1, f"tc90={n}")
    op_ok = sql("SELECT COUNT(*) FROM sys_org_user_unit WHERE aae011='lvf' "
                "AND 'lvf' NOT IN (SELECT DISTINCT COALESCE(aae011,'') FROM tc90)")
    rec("单位版隔离经办人 lvf(零合同)", op_ok == "1", f"n={op_ok}")

    # ── 上月补退明细 E/Z/A/B ──
    r = proc(LAST, {"aae002": "209906", "atc900": str(ATC900), "aae011": "nouser47"})
    rec("E 报错面", r.get("appCode") == -1 and r.get("errMsg") == "无法计算补退，原因为经办人信息不正确",
        f"appCode={r.get('appCode')} errMsg={r.get('errMsg')}")

    sql(f"UPDATE tc90 SET atc90p='2', atc90pa='2', atc90w='2' WHERE atc900={ATC900}")
    r = proc(LAST, {"aae002": "209906", "atc900": str(ATC900), "aae011": "jiaq"})
    n = int(sql(f"SELECT COUNT(*) FROM tc07 WHERE aae002=209906 AND atc900={ATC900}"))
    rec("Z 零操作面", r.get("appCode") == 1 and n == 0, f"appCode={r.get('appCode')} rows={n}")

    fixture_diffs()  # 场序照旧侧: Z 先于差异夹具(当月差异段无条件, 先插会污染 Z)
    sql(f"UPDATE tc90 SET atc90p='0', atc90pa='0', atc90w='0' WHERE atc900={ATC900}")
    r = proc(LAST, {"aae002": "209906", "atc900": str(ATC900), "aae011": "jiaq"})
    rec("A 门开 appCode", r.get("appCode") == 1, f"resp={json.dumps(r, ensure_ascii=False)[:120]}")
    cmp_row(OLD_A, tc07_row(), ("atc070", "aae036"), "A 产出面 vs 旧侧")

    sql(f"UPDATE tc90 SET atb90r='5', aae003=209906, atc90p='2', atc90pa='2', atc90w='2' WHERE atc900={ATC900}")
    r = proc(LAST, {"aae002": "209906", "atc900": str(ATC900), "aae011": "jiaq"})
    rec("B 离职面 appCode", r.get("appCode") == 1, "")
    cmp_row(OLD_B, tc07_row(), ("atc070", "aae036"), "B 产出面 vs 旧侧")

    # ── 单位版三段 ──
    sql(f"DELETE FROM tc08 WHERE aac001=998072")
    sql(f"UPDATE tc90 SET atb90r='3', atc90p=NULL, aaa201=209801, aaa208=209906, "
        f"aaa401='1', aaa402='0', aaa407='0', aae011='lvf' WHERE atc900={ATC900}")
    r = proc(UNIT, {"aae002": "209906", "aae011": "lvf"})
    n = int(sql("SELECT COUNT(*) FROM tc08 WHERE aac001=998072"))
    rec("单位 Run1 反转守卫 return", r.get("appCode") == 1 and n == 0,
        f"appCode={r.get('appCode')} rows={n}")

    sql(f"UPDATE tc90 SET aaa401='0', aaa402='1', atc90pa=NULL, aaa601=209801, aaa603=209801 WHERE atc900={ATC900}")
    r = proc(UNIT, {"aae002": "209906", "aae011": "lvf"})
    rows = sql(f"SELECT row_to_json(t) FROM (SELECT * FROM tc08 WHERE aac001=998072 ORDER BY aaa000) t")
    arr = [json.loads(x) for x in rows.splitlines() if x.strip()]
    rec("单位 Run2 医保段落库", r.get("appCode") == 1 and len(arr) == 1,
        f"appCode={r.get('appCode')} rows={len(arr)}")
    if arr:
        cmp_row({**OLD_TC08, "aaa000": "2"}, arr[0],
                ("baz001", "baz002", "atc080", "aae036"), "Run2 TC08 vs 旧侧")

    sql(f"UPDATE tc90 SET aaa402='0', aaa407='1', atc90w=NULL, aaa501=209801, aaa503=209801 WHERE atc900={ATC900}")
    r = proc(UNIT, {"aae002": "209906", "aae011": "lvf"})
    rows = sql(f"SELECT row_to_json(t) FROM (SELECT * FROM tc08 WHERE aac001=998072 ORDER BY aaa000) t")
    arr = [json.loads(x) for x in rows.splitlines() if x.strip()]
    rec("单位 Run3 公积金段落库", r.get("appCode") == 1 and len(arr) == 2,
        f"appCode={r.get('appCode')} rows={len(arr)}")
    if len(arr) == 2:
        cmp_row({**OLD_TC08, "aaa000": "3"}, arr[1],
                ("baz001", "baz002", "atc080", "aae036"), "Run3 TC08 vs 旧侧")

    # ── 清场+零残留 ──
    for t in ("tc07", "ac10", "tc93", "tj0a1", "tj0b1", "tj0d1", "tc08"):
        sql(f"DELETE FROM {t} WHERE atc900={ATC900}")
    sql("DELETE FROM tc08 WHERE aac001=998072")
    sql(f"DELETE FROM tc90 WHERE atc900={ATC900}")
    sql("DELETE FROM tb93 WHERE atb930=20990971")
    sql("DELETE FROM tb90 WHERE atb900=20990972 AND aab001=20990971")
    sql("DELETE FROM ab01 WHERE aab001=20990971")
    res = sql(f"SELECT (SELECT COUNT(*) FROM tc90 WHERE atc900={ATC900})||'/'||"
              f"(SELECT COUNT(*) FROM ac10 WHERE atc900={ATC900})||'/'||"
              f"(SELECT COUNT(*) FROM tc07 WHERE atc900={ATC900})||'/'||"
              f"(SELECT COUNT(*) FROM tc93 WHERE atc900={ATC900})||'/'||"
              f"(SELECT COUNT(*) FROM tc08 WHERE aac001=998072)||'/'||"
              f"(SELECT COUNT(*) FROM ab01 WHERE aab001=20990971)")
    rec("清场零残留", res == "0/0/0/0/0/0", f"residue={res}")

    print(f"\n=== BC-47 上月补退明细+单位版补收基数 新旧同判: {len(PASS)} 过 / {len(FAIL)} 败 ===")
    sys.exit(1 if FAIL else 0)


if __name__ == "__main__":
    main()
