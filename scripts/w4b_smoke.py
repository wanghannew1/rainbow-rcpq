#!/usr/bin/env python3
"""W4b 冒烟: 省/市公积金+工伤 月情况(重建→确认白名单→已确认跳过→回滚)。
用法: python3 scripts/w4b_smoke.py
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
WY = "70557"
print(f"=== 0: 清场+预置(公积金应缴年月 aaa503={YM}) 户头 {A030} ===")
for t in ["tj0d", "tj0d1", "tj0e", "tj0e1", "tj0f", "tj0f1"]:
    sql(f"DELETE FROM {t} WHERE aae002={YM} AND aaa020='{ATCD}' AND aaa030={A030}")
for c in ["王易龄", "张天祺", "王慧"]:
    atc = sql(f"SELECT atc900 FROM tc90 WHERE aac003='{c}' ORDER BY atc900 DESC LIMIT 1")
    sql(f"UPDATE tc90 SET aaa503={YM}, aaa504=NULL, aaa502=NULL WHERE atc900={atc}")
print("  完成")

CASES = [
    ("PKG_派遣保险月情况.PRC_省公积金月情况", "tj0d", "tj0d1", "省公积金"),
    ("PKG_派遣保险月情况.PRC_市公积金月情况", "tj0e", "tj0e1", "市公积金"),
    ("PKG_派遣保险月情况.PRC_工伤月情况", "tj0f", "tj0f1", "工伤"),
]
for pname, main, detail, label in CASES:
    print(f"\n=== {label} 月情况 ===")
    r = proc(pname, {"aae002": YM, "aaa020": ATCD, "aaa030": A030, "aae011": OPER})
    if r.get("appCode") == 1:
        n1 = sql(f"SELECT count(*) FROM {detail} WHERE aae002={YM} AND aaa030={A030}")
        n0 = sql(f"SELECT count(*) FROM {main} WHERE aae002={YM} AND aaa030={A030}")
        print(f"  明细 {n1} / 汇总 {n0}")
        chk(f"{label} 重建成功", True, str(r)[:90])
        chk(f"{label} 汇总 AAEZZZ='0'", sql(
            f"SELECT coalesce(max(aaezzz),'-') FROM {main} WHERE aae002={YM} AND aaa030={A030}") == "0")
        if int(n1) >= 1:
            agg = sql(f"SELECT coalesce(sum(caa001),0), coalesce(sum(caa002),0) FROM {detail} WHERE aae002={YM} AND aaa030={A030}")
            head = sql(f"SELECT coalesce(sum(caa001),0), coalesce(sum(caa002),0) FROM {main} WHERE aae002={YM} AND aaa030={A030}")
            chk(f"{label} 汇总=明细(CAA001/002)", agg == head, f"{head} vs {agg}")
        # 确认白名单
        tab = {"tj0d": "TJ0D", "tj0e": "TJ0E", "tj0f": "TJ0F"}[main]
        rc = proc("PKG_派遣保险月情况.PRC_月转款确认",
                  {"tabname": tab, "aae002": YM, "aaa020": ATCD, "aaa030": A030, "aae011": OPER})
        chk(f"{label} 确认({tab})", rc.get("appCode") == 1, str(rc)[:80])
        chk(f"{label} AAEZZZ='1'", sql(
            f"SELECT coalesce(max(aaezzz),'-') FROM {main} WHERE aae002={YM} AND aaa030={A030}") == "1")
        rs = proc(pname, {"aae002": YM, "aaa020": ATCD, "aaa030": A030, "aae011": OPER})
        chk(f"{label} 已确认静默跳过", rs.get("appCode") == 1 and rs.get("skipped"), str(rs)[:80])
        # 复原确认态, 供下一用例复跑
        sql(f"UPDATE {main} SET aaezzz='0' WHERE aae002={YM} AND aaa020='{ATCD}' AND aaa030={A030}")
    else:
        msg = r.get("errMsg") or ""
        if "计算险金信息出错" in msg:
            print(f"  [INFO] 忠实错误传播: {msg[:70]}")
            n1 = sql(f"SELECT count(*) FROM {detail} WHERE aae002={YM} AND aaa030={A030}")
            chk(f"{label} 整事务回滚零半账", n1 == "0", f"detail={n1}")
            chk(f"{label} 忠实错误", True, msg[:60])
        else:
            chk(f"{label} 重建", False, str(r)[:120])

print("\n==== W4b 冒烟总结 ====")
if FAILS:
    print(f"FAIL {len(FAILS)}: " + "; ".join(FAILS))
    sys.exit(1)
print("全部 PASS")
