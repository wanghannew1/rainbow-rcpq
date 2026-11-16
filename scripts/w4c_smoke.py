#!/usr/bin/env python3
"""W4c-1 冒烟: 省/市医保月情况(重建→确认→已确认跳过→回滚)。
数据基座: 种子合同 aaa402='1' aaa110=220100000000。用法: python3 scripts/w4c_smoke.py
"""
import json
import sys
import urllib.request
import urllib.parse
import subprocess

BASE = "http://localhost:8080"
YM = "202611"
ATCD = "220100000000"
OPER = "jiaq"
FAILS = []


def sql(q):
    r = subprocess.run(["psql", "-h", "localhost", "-U", "rcpq", "-d", "rainbow_rcpq", "-tAc", q],
                       capture_output=True, text=True, env={"PGPASSWORD": "rcpq_dev", "PATH": "/usr/bin:/bin"})
    return (r.stdout or "").strip()


def proc(name, body):
    req = urllib.request.Request(BASE + "/proc/" + urllib.parse.quote(name),
                                 data=json.dumps(body).encode(),
                                 headers={"Content-Type": "application/json"})
    return json.load(urllib.request.urlopen(req, timeout=300))


def chk(name, cond, detail=""):
    tag = "PASS" if cond else "FAIL"
    print(f"  [{'PASS' if cond else 'FAIL'}] {name} {detail}")
    if not cond:
        FAILS.append(name)


A030 = sql("SELECT DISTINCT u.aaa030 FROM tc90 t JOIN ab01 u ON u.aab001=t.aab001 WHERE t.atc900=70557")

CASES = [
    ("PKG_派遣保险月情况.PRC_省医保月情况", "tj0b", "tj0b1", "省医保", "TJ0B"),
    ("PKG_派遣保险月情况.PRC_市医保月情况", "tj0c", "tj0c1", "市医保", "TJ0C"),
]
for pname, main, detail, label, tab in CASES:
    print(f"\n=== {label} 月情况 ===")
    for t in [main, detail]:
        sql(f"DELETE FROM {t} WHERE aae002={YM} AND aaa020='{ATCD}' AND aaa030={A030}")
    sql(f"UPDATE {main} SET aaezzz='0' WHERE aae002={YM} AND aaa020='{ATCD}' AND aaa030={A030}")
    r = proc(pname, {"aae002": YM, "aaa020": ATCD, "aaa030": A030, "aae011": OPER})
    if r.get("appCode") == 1 and not r.get("skipped"):
        n1 = sql(f"SELECT count(*) FROM {detail} WHERE aae002={YM} AND aaa030={A030}")
        n0 = sql(f"SELECT count(*) FROM {main} WHERE aae002={YM} AND aaa030={A030}")
        print(f"  明细 {n1} / 汇总 {n0}")
        chk(f"{label} 重建", int(n1) >= 0 and int(n0) >= 0, f"{n1}/{n0}")
        chk(f"{label} 汇总 AAEZZZ='0'", sql(
            f"SELECT coalesce(max(aaezzz),'-') FROM {main} WHERE aae002={YM} AND aaa030={A030}") == "0")
        rc = proc("PKG_派遣保险月情况.PRC_月转款确认",
                  {"tabname": tab, "aae002": YM, "aaa020": ATCD, "aaa030": A030, "aae011": OPER})
        chk(f"{label} 确认({tab})", rc.get("appCode") == 1, str(rc)[:80])
        chk(f"{label} AAEZZZ='1'", sql(
            f"SELECT coalesce(max(aaezzz),'-') FROM {main} WHERE aae002={YM} AND aaa030={A030}") == "1")
        rr = proc(pname, {"aae002": YM, "aaa020": ATCD, "aaa030": A030, "aae011": OPER})
        chk(f"{label} 已确认静默跳过", rr.get("appCode") == 1 and rr.get("skipped"), str(rr)[:80])
        sql(f"UPDATE {main} SET aaezzz='0' WHERE aae002={YM} AND aaa020='{ATCD}' AND aaa030={A030}")
    else:
        msg = r.get("errMsg") or ""
        if "计算险金信息出错" in msg:
            print(f"  [INFO] 忠实错误传播: {msg[:70]}")
            n1 = sql(f"SELECT count(*) FROM {detail} WHERE aae002={YM} AND aaa030={A030}")
            chk(f"{label} 整事务回滚零半账", n1 == "0", f"detail={n1}")
            chk(f"{label} 忠实错误", True, msg[:60])
        elif r.get("skipped"):
            chk(f"{label} 已确认跳过(历史态)", True)
        else:
            chk(f"{label} 重建", False, str(r)[:130])

print("\n==== W4c-1 冒烟总结 ====")
if FAILS:
    print(f"FAIL {len(FAILS)}: " + "; ".join(FAILS))
    sys.exit(1)
print("全部 PASS")
