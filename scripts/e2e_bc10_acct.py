#!/usr/bin/env python3
"""BC-10 工资账号维护(tddab, TC90.BBB001) 新系统同链三腿复跑 — 桥单 20261127-032。

镜像旧侧 rainbowpq/scripts/replay/tddab_acct_test.py(卷52) 走新系统通道:
  夹具   = SQL 合成 TC90 行(ATC900=20990101, 隔离 aac002=999999190001019999,
           BBB001=TEST-ACCT-001, AAE013='卷52夹具') 与旧侧逐列同形状
  腿A    = GET  /interaction/REQ-470083e72d86488fb7a69019c3361af1?aac002=...(tc90Store 查询)
  腿B/C  = POST /interaction/REQ-08b24c93930d40c88de55e16ec4f24bc
           body={"rows":[{__state:'modified', atc900, bbb001}]} (通用 UpdateExecutor;
           旧侧形状=整行+_t=3, 新侧=minimal-fields+PK, BC-05 先例——形状差异回执注明)
纪律 v2: 夹具/腿B 后/腿C 后全列行态导出 work/bc10_pg_rowdump.txt; 非目标列不变断言
(全列逐列 diff, 仅 bbb001 允许变化)。
用法: python3 scripts/e2e_bc10_acct.py
"""
import json
import os
import subprocess
import sys
import urllib.error
import urllib.request
from urllib.parse import quote

PG = "postgresql://rcpq:rcpq_dev@localhost:5432/rainbow_rcpq"
APP = "http://localhost:8080"
MARK_ID = "999999190001019999"
Q_CODE = "REQ-470083e72d86488fb7a69019c3361af1"
U_CODE = "REQ-08b24c93930d40c88de55e16ec4f24bc"

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


def full_row():
    v = sql(f"SELECT row_to_json(t)::text FROM (SELECT * FROM tc90 WHERE aac002='{MARK_ID}') t")
    return json.loads(v) if v else None


def dump(label, row):
    path = os.path.join(os.path.dirname(os.path.dirname(os.path.abspath(__file__))),
                        "work", "bc10_pg_rowdump.txt")
    with open(path, "a", encoding="utf-8") as f:
        f.write(f"[{label}] tc90: {json.dumps(row, ensure_ascii=False)}\n")


def norm(v):
    """数值/时间戳列跨形状规整(Decimal 尾零/时间戳微秒), 只用于不变性比较"""
    if isinstance(v, float):
        return int(v) if v == int(v) else round(v, 6)
    if isinstance(v, str) and len(v) == 10 and v[4] == "-" and v[7] == "-":
        return v
    return v


def main():
    doc = open(os.path.abspath(__file__), encoding="utf-8").read()
    assert "999999190001019999" in doc
    subprocess.run(["psql", PG, "-c",
                    f"DELETE FROM tc90 WHERE aac002='{MARK_ID}'"], capture_output=True)
    path = os.path.join(os.path.dirname(os.path.dirname(os.path.abspath(__file__))),
                        "work", "bc10_pg_rowdump.txt")
    open(path, "w").close()

    # 夹具(旧侧 INSERT 逐列同形状)
    subprocess.run(["psql", PG, "-c", (
        "INSERT INTO tc90 (atc900, atb930, aab001, aac001, aac002, aac003, "
        "bbb001, aae100, atb90r, atc90f, aae002, aae003, aae011, aae013) VALUES "
        "(20990101, 20990099, 20990001, 20990101, "
        f"'{MARK_ID}', '测试账号员', 'TEST-ACCT-001', '1', '1', '1', "
        "'209901', '209912', 'jiaq', '卷52夹具')")], capture_output=True)
    pre = full_row()
    ok_fx = pre is not None and pre["bbb001"] == "TEST-ACCT-001"
    rec("夹具", ok_fx, f"atc900={pre and pre['atc900']} bbb001={pre and pre['bbb001']}")
    if not ok_fx:
        sys.exit(1)
    dump("夹具输入-TC90合成行(BBB001=TEST-ACCT-001)", pre)

    # 腿A 页面查询通道(tc90Store 按 aac002)
    st, r = http("GET", f"{APP}/interaction/{quote(Q_CODE)}?aac002={MARK_ID}")
    rows = (r or {}).get("rows") or []
    hit = sum(1 for x in rows if x.get("aac002") == MARK_ID)
    rec("腿A 页面查询通道", st == 200 and hit >= 1, f"HTTP {st}, 命中 {hit}, totalCount={(r or {}).get('totalCount')}")

    # 腿B 页面保存改账号(minimal-fields+PK 形状, BC-05 先例)
    st, r = http("POST", f"{APP}/interaction/{quote(U_CODE)}",
                 {"rows": [{"__state": "modified", "atc900": 20990101, "bbb001": "TEST-ACCT-002"}]})
    post_b = full_row()
    v = post_b.get("bbb001") if post_b else None
    changed = {k for k in (set(pre) | set(post_b or {}))
               if norm(pre.get(k)) != norm((post_b or {}).get(k))}
    rec("腿B 页面保存改账号", st == 200 and v == "TEST-ACCT-002" and changed == {"bbb001"},
        f"HTTP {st}, BBB001={v}, updated={(r or {}).get('updated')}, 变化列={sorted(changed)}(应仅bbb001)")
    dump("腿B保存后-TC90(BBB001→TEST-ACCT-002)", post_b)

    # 腿C 幂等重存(同值重存)
    st, r = http("POST", f"{APP}/interaction/{quote(U_CODE)}",
                 {"rows": [{"__state": "modified", "atc900": 20990101, "bbb001": "TEST-ACCT-002"}]})
    post_c = full_row()
    stable = json.dumps(post_c, sort_keys=True, ensure_ascii=False) == \
        json.dumps(post_b, sort_keys=True, ensure_ascii=False)
    rec("腿C 幂等重存", st == 200 and stable,
        f"HTTP {st}, updated={(r or {}).get('updated')}, 行态不变={'✓' if stable else '✗'}")
    dump("腿C幂等重存后-TC90(行态不变)", post_c)

    # 清场
    subprocess.run(["psql", PG, "-c", f"DELETE FROM tc90 WHERE aac002='{MARK_ID}'"], capture_output=True)
    left = sql(f"SELECT COUNT(*) FROM tc90 WHERE aac002='{MARK_ID}'")
    rec("清场", left == "0", f"残留={left}")

    print(f"\n=== BC-10 工资账号维护 新系统复跑: {len(PASS)} 过 / {len(FAIL)} 败 ===")
    sys.exit(1 if FAIL else 0)


if __name__ == "__main__":
    main()
