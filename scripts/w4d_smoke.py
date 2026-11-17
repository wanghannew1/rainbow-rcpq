#!/usr/bin/env python3
"""W4d 冒烟: 市医保应缴费情况(真实数据忠实错误+整事务回滚)。
用法: python3 scripts/w4d_smoke.py
"""
import json
import sys
import urllib.request
import urllib.parse
import subprocess

BASE = "http://localhost:8080"
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
print("=== 市医保应缴费(TJ0C2) ===")
sql(f"DELETE FROM tj0c2 WHERE aae002=202611 AND aaa020='220100000000' AND aaa030={A030}")
r = proc("PKG_派遣保险月情况.PRC_市医保应缴费情况",
         {"aae002": "202611", "aaa020": "220100000000", "aaa030": A030, "aae011": "jiaq"})
if r.get("appCode") == 1:
    n = sql(f"SELECT count(*) FROM tj0c2 WHERE aae002=202611 AND aaa030={A030}")
    chk("重建成功", True, f"rows={r.get('rows')} tj0c2={n}")
else:
    msg = r.get("errMsg") or ""
    chk("忠实错误传播", "计算险金信息出错" in msg, msg[:70])
    n = sql(f"SELECT count(*) FROM tj0c2 WHERE aae002=202611 AND aaa030={A030}")
    chk("整事务回滚零半账", n == "0", f"detail={n}")

print("\n==== W4d 冒烟总结 ====")
if FAILS:
    print(f"FAIL {len(FAILS)}: " + "; ".join(FAILS))
    sys.exit(1)
print("全部 PASS")
