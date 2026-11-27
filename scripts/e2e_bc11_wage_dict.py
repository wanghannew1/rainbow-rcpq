#!/usr/bin/env python3
"""BC-11 工资维护族字典页(卷49) 新系统同链五腿复跑 — 桥单 20261127-033。

镜像旧侧 rainbowpq/scripts/replay/wage_dict_family_test.py 走新系统通道:
  WZ-01 AA80 模板新增 = POST REQ-tddf_dialog_addAa80-U {aa80:{...}} (tddfService.doSaveAa80;
        无主键→SEQ_AA80_AAA800 配号, 与旧卷49 配方①同语义)
  WZ-02 AA81 细项新增 = POST REQ-tddf_cont-U {rows:[...]} (tddfService.doSaveAa81;
        AA90 副作用=按既有(atb900,atb930)组克隆, 新 aaa800 无组→0 行, 与旧侧同判)
  WZ-03 AA98 区间增改删 = POST REQ-tdgg_cont-update (通用 UpdateExecutor;
        __state new/modified/deleted, PK=id)
  WZ-04 AA99 最低工资增删 = POST REQ-a711678dd2f04e0ead33b960a4c9a070 (通用 UpdateExecutor;
        PK=aaa990 序列配号; 旧侧"首次批内重复配号怪癖重发"配方在新侧无对应物——
        序列配号由 JPA 保证唯一, 首发即落库则不重发, 回执注明)
  WZ-05 tdhc 查询形状 = GET REQ-271a67cf883f44e9977fdb56562f1b25?atc937=0
        (旧侧 '1=0' 任意过滤在新侧不可透传(注入防护), 等价=合法列零命中, 回执注明)
形状差异(回执注明): aae036 旧侧 epoch ms → 新侧 ISO 串(LocalDateTime 实体)。
隔离口径同旧侧: AAE013 标记'卷49-*' + AAA987=209901 + AAE011='jiaq'。
纪律 v2: 各腿落库行全列行态导出 work/bc11_pg_rowdump.txt。
用法: python3 scripts/e2e_bc11_wage_dict.py
"""
import datetime
import json
import os
import subprocess
import sys
import urllib.error
import urllib.request
from urllib.parse import quote

PG = "postgresql://rcpq:rcpq_dev@localhost:5432/rainbow_rcpq"
APP = "http://localhost:8080"
M80, M81, M99 = "卷49-AA80", "测试细项卷49", "卷49-AA99"
MK98 = "aaa981='1' AND aaa987=209901 AND aae011='jiaq' AND aaa985=3000"
NOW = datetime.datetime.now().isoformat(timespec="seconds")
AUD = {"aae011": "jiaq", "aae017": "00000000000", "aae019": "测试工资族",
       "aae020": "测试机构", "aae022": "209900000000"}

TRUTH = {"aae011": "jiaq", "aae017": "4101", "aae019": "贾悫",
         "aae020": "吉林省彩虹人才开发咨询服务有限公司", "aae022": "410100000000"}


def audit_ok(row, with_baz=False):
    """探针断言: 前端传假审计值, 落库应为登录上下文真值(桥单 dc-audit-backfill)"""
    if row is None:
        return False
    for k, v in TRUTH.items():
        if row.get(k) != v:
            return False
    if with_baz and row.get("baz001") is None:
        return False
    return True


PASS, FAIL = [], []


def rec(name, ok, detail=""):
    (PASS if ok else FAIL).append(name)
    print(("✅" if ok else "❌"), name, ("— " + detail if detail else ""))


def sql(q):
    r = subprocess.run(["psql", PG, "-tAc", q], capture_output=True, text=True)
    return (r.stdout or "").strip()


def http(method, url, data=None):
    try:
        req = urllib.request.Request(url, method=method,
                                     data=json.dumps(data).encode() if data is not None else None,
                                     headers={"Content-Type": "application/json"})
        with urllib.request.urlopen(req, timeout=60) as r:
            return r.status, json.loads(r.read().decode("utf-8", "replace"))
    except urllib.error.HTTPError as e:
        try:
            return e.code, json.loads(e.read().decode("utf-8", "replace"))
        except Exception:
            return e.code, {}


def save(code, body):
    return http("POST", f"{APP}/interaction/{quote(code)}", body)


def sql_row(table, where):
    v = sql(f"SELECT COALESCE(MAX(row_to_json(t)::text),'-') FROM (SELECT * FROM {table} WHERE {where}) t")
    return None if v in ("-", "") else json.loads(v)


def dump(label, table, where):
    v = sql(f"SELECT COALESCE(MAX(row_to_json(t)::text),'-') FROM (SELECT * FROM {table} WHERE {where}) t")
    path = os.path.join(os.path.dirname(os.path.dirname(os.path.abspath(__file__))),
                        "work", "bc11_pg_rowdump.txt")
    with open(path, "a", encoding="utf-8") as f:
        f.write(f"[{label}] {table}: {v}\n")
    return None if v == "-" else json.loads(v)


def main():
    path = os.path.join(os.path.dirname(os.path.dirname(os.path.abspath(__file__))),
                        "work", "bc11_pg_rowdump.txt")
    open(path, "w").close()
    # 失败残渣预清(旧套件同手法)
    for stmt in (f"DELETE FROM aa81 WHERE aaa902='{M81}'",
                 f"DELETE FROM aa80 WHERE aae013='{M80}'",
                 f"DELETE FROM aa98 WHERE {MK98}",
                 f"DELETE FROM aa99 WHERE aae013='{M99}'"):
        subprocess.run(["psql", PG, "-c", stmt], capture_output=True)

    # ── WZ-01 AA80 模板新增 ──
    st, r = save("REQ-tddf_dialog_addAa80-U",
                 {"aa80": {"aaa801": "卷49测试模板", "aaa802": "0", "aae013": M80,
                           "aae036": NOW, **AUD}})
    n = sql(f"SELECT COUNT(*) FROM aa80 WHERE aae013='{M80}'")
    mid = sql(f"SELECT MIN(aaa800) FROM aa80 WHERE aae013='{M80}'")
    row80 = sql_row("aa80", f"aaa800={mid}")
    ok = st == 200 and n == "1" and bool(mid) and audit_ok(row80, with_baz=True)
    rec("WZ-01 AA80模板新增", ok, f"HTTP {st}, 库 {n}, 序列配号 aaa800={mid}, 审计回填={'✓' if audit_ok(row80, True) else row80}")
    if not ok:
        sys.exit(1)
    dump("腿WZ-01后-AA80模板行(序列配号)", "aa80", f"aaa800={mid}")

    # ── WZ-02 AA81 细项新增(+AA90 副作用同判 0) ──
    st, r = save("REQ-tddf_cont-U",
                 {"rows": [{"aaa800": int(mid), "aaa901": "1", "aaa902": M81,
                            "acb208": "0", "aae036": NOW, **AUD}]})
    n81 = sql(f"SELECT COUNT(*) FROM aa81 WHERE aaa902='{M81}'")
    n90 = sql(f"SELECT COUNT(*) FROM aa90 WHERE aaa800={mid}")
    row81 = sql_row("aa81", f"aaa902='{M81}'")
    ok = st == 200 and n81 == "1" and n90 == "0" and audit_ok(row81)
    rec("WZ-02 AA81细项新增(+AA90副作用)", ok,
        f"HTTP {st}, AA81 {n81} 行, AA90 关联 {n90} 行(应0), 审计回填={'✓' if audit_ok(row81) else row81}")
    dump("腿WZ-02后-AA81细项行", "aa81", f"aaa902='{M81}'")

    # ── WZ-03 AA98 区间 增→改→删 ──
    r98 = {"aaa981": "1", "aaa982": 60000, "aaa983": 60, "aaa984": 300,
           "aaa985": 3000, "aaa986": 20000, "aaa987": 209901, "aaa988": 209912,
           "aae036": NOW, **AUD}
    st, r = save("REQ-tdgg_cont-update", {"rows": [{"__state": "new", **r98}]})
    n = sql(f"SELECT COUNT(*) FROM aa98 WHERE {MK98}")
    rid = sql(f"SELECT MIN(id) FROM aa98 WHERE {MK98}")
    row98 = sql_row("aa98", f"id={rid}") if rid else None
    ok = st == 200 and n == "1" and bool(rid) and audit_ok(row98)
    rec("WZ-03a AA98区间增", ok, f"HTTP {st}, inserted={(r or {}).get('inserted')}, id={rid}(序列配号), "
        f"审计回填={'✓' if audit_ok(row98) else row98}")
    dump("腿WZ-03增后-AA98区间行(AAA985=3000)", "aa98", f"id={rid}")
    st2, r = save("REQ-tdgg_cont-update",
                  {"rows": [{"__state": "modified", "id": int(rid), "aaa985": 3500}]})
    v = sql(f"SELECT aaa985::text FROM aa98 WHERE id={rid}")
    changed = dump("腿WZ-03改后-AA98区间行(AAA985=3500)", "aa98", f"id={rid}")
    only985 = changed is not None and changed.get("aaa985") == 3500.0
    # modify 行不回填(BC-10 实证口径): 审计列保留 insert 时回填值
    keep = audit_ok(changed) if changed is not None else False
    rec("WZ-03b AA98区间改", st2 == 200 and v.rstrip("0").rstrip(".") == "3500" and only985 and keep,
        f"HTTP {st2}, updated={(r or {}).get('updated')}, aaa985={v}, 改后审计列保留={'✓' if keep else changed}")
    if changed is None:
        sys.exit(1)
    st3, r = save("REQ-tdgg_cont-update", {"rows": [{"__state": "deleted", "id": int(rid)}]})
    n3 = sql(f"SELECT COUNT(*) FROM aa98 WHERE id={rid}")
    rec("WZ-03c AA98区间删", st3 == 200 and n3 == "0",
        f"HTTP {st3}, deleted={(r or {}).get('deleted')}, 残留 {n3}")

    # ── WZ-04 AA99 最低工资 增→删 ──
    r99 = {"aaa991": "1", "aaa992": 2099, "aaa993": 60, "aaa994": 65, "aaa995": 6,
           "aaa996": 209901, "aaa997": 209912, "aaa998": 55,
           "aae013": M99, "aae036": NOW, **AUD}
    st, r = save("REQ-a711678dd2f04e0ead33b960a4c9a070", {"rows": [{"__state": "new", **r99}]})
    n = sql(f"SELECT COUNT(*) FROM aa99 WHERE aae013='{M99}'")
    rid99 = sql(f"SELECT MIN(aaa990) FROM aa99 WHERE aae013='{M99}'")
    row99 = sql_row("aa99", f"aaa990={rid99}") if rid99 else None
    ok = st == 200 and n == "1" and bool(rid99) and audit_ok(row99)
    rec("WZ-04a AA99最低工资增", ok,
        f"HTTP {st}, inserted={(r or {}).get('inserted')}, 配号 aaa990={rid99}(序列), "
        f"审计回填={'✓' if audit_ok(row99) else row99}")
    if ok:
        dump("腿WZ-04增后-AA99最低工资行(序列配号)", "aa99", f"aaa990={rid99}")
        st2, r = save("REQ-a711678dd2f04e0ead33b960a4c9a070",
                      {"rows": [{"__state": "deleted", "aaa990": int(rid99)}]})
        n2 = sql(f"SELECT COUNT(*) FROM aa99 WHERE aae013='{M99}'")
        rec("WZ-04b AA99最低工资删", st2 == 200 and n2 == "0",
            f"HTTP {st2}, deleted={(r or {}).get('deleted')}, 残留 {n2}")
    else:
        rec("WZ-04b AA99最低工资删", False, "增腿未落库, 跳过")

    # ── WZ-05 tdhc 查询形状(零行 rowSet) ──
    st, r = http("GET", f"{APP}/interaction/{quote('REQ-271a67cf883f44e9977fdb56562f1b25')}?atc937=0")
    rows = (r or {}).get("rows")
    total = (r or {}).get("totalCount")
    ok = st == 200 and isinstance(rows, list) and len(rows) == 0 and total == 0
    rec("WZ-05 tdhc查询形状", ok,
        f"HTTP {st}, rows={rows}, totalCount={total}, store={(r or {}).get('store')}(零行形状)")

    # 清场+零残留
    subprocess.run(["psql", PG, "-c", f"DELETE FROM aa81 WHERE aaa902='{M81}'"], capture_output=True)
    subprocess.run(["psql", PG, "-c", f"DELETE FROM aa80 WHERE aae013='{M80}'"], capture_output=True)
    subprocess.run(["psql", PG, "-c", f"DELETE FROM aa98 WHERE {MK98}"], capture_output=True)
    subprocess.run(["psql", PG, "-c", f"DELETE FROM aa99 WHERE aae013='{M99}'"], capture_output=True)
    left = (int(sql(f"SELECT COUNT(*) FROM aa81 WHERE aaa902='{M81}'")) +
            int(sql(f"SELECT COUNT(*) FROM aa80 WHERE aae013='{M80}'")) +
            int(sql(f"SELECT COUNT(*) FROM aa98 WHERE {MK98}")) +
            int(sql(f"SELECT COUNT(*) FROM aa99 WHERE aae013='{M99}'")))
    rec("清场", left == 0, f"残留={left}")

    print(f"\n=== BC-11 工资维护族 新系统复跑: {len(PASS)} 过 / {len(FAIL)} 败 ===")
    sys.exit(1 if FAIL else 0)


if __name__ == "__main__":
    main()
