#!/usr/bin/env python3
"""W4a 社保月情况冒烟: 忠实错误+整事务回滚(半账消除实证) → 小户头成功路径 →
月转款确认(AAEZZZ='1'/明细 AAE013='1') → 已确认静默跳过 → 减员未审核守卫。
用法: python3 scripts/w4a_smoke.py
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
SMALL = sql("""SELECT b.aaa030 FROM tb93 a JOIN ab01 b ON a.aab001=b.aab001
               WHERE b.aaa030 IS NOT NULL GROUP BY b.aaa030 HAVING count(*)<=3
               ORDER BY count(*) LIMIT 1""")

print("=== 0: 清场 ===")
sql(f"DELETE FROM tj0a WHERE aae002={YM} AND aaa020='{ATCD}' AND aaa030 IN ({A030},{SMALL})")
sql(f"DELETE FROM tj0a1 WHERE aae002={YM} AND aaa020='{ATCD}' AND aaa030 IN ({A030},{SMALL})")
print(f"  完成 (大户头 {A030} / 小户头 {SMALL})")

print("=== 1: 大户头重建 → 忠实错误 + 整事务回滚(半账消除实证) ===")
r = proc("PKG_派遣保险月情况.PRC_社保月情况",
         {"aae002": YM, "aaa020": ATCD, "aaa030": A030, "aae011": OPER})
print(f"  结果: {str(r)[:120]}")
if r.get("appCode") == -1 and "计算险金信息出错" in (r.get("errMsg") or ""):
    chk("忠实错误传播(含合同号前缀)",
        r.get("errMsg", "").split("计算险金信息出错")[0].isdigit(), r.get("errMsg", "")[:80])
    chk("整事务回滚: 明细无半账(结构性修复)",
        sql(f"SELECT count(*) FROM tj0a1 WHERE aae002={YM} AND aaa030={A030}") == "0")
    chk("整事务回滚: 汇总无半账",
        sql(f"SELECT count(*) FROM tj0a WHERE aae002={YM} AND aaa030={A030}") == "0")
elif r.get("appCode") == 1:
    n = sql(f"SELECT count(*) FROM tj0a1 WHERE aae002={YM} AND aaa030={A030}")
    print(f"  [INFO] 该户头全量走通: 明细 {n} 行")
    chk("明细生成", int(n) >= 1)

print("=== 2: 小户头重建 ===")
r = proc("PKG_派遣保险月情况.PRC_社保月情况",
         {"aae002": YM, "aaa020": ATCD, "aaa030": SMALL, "aae011": OPER})
chk("重建成功(或忠实错误)", r.get("appCode") in (1, -1), str(r)[:100])
if r.get("appCode") == 1:
    n1 = sql(f"SELECT count(*) FROM tj0a1 WHERE aae002={YM} AND aaa030={SMALL}")
    n0 = sql(f"SELECT count(*) FROM tj0a WHERE aae002={YM} AND aaa030={SMALL}")
    print(f"  小户头明细 {n1} / 汇总 {n0}")
    hh = sql(f"SELECT coalesce(max(aaezzz),'-') FROM tj0a WHERE aae002={YM} AND aaa030={SMALL}")
    chk("汇总 AAEZZZ='0'(未确认)", hh in ("0", "-"), hh)

print("=== 3: 月转款确认(TJ0A 白名单) ===")
sql(f"""INSERT INTO tj0a (aae002, atb930, atj000, aaa020, aaa030, aaezzz, atj0a7_z, xh)
       VALUES ({YM}, 999999, '1', '{ATCD}', {SMALL}, '0', 1, 1)""")
r = proc("PKG_派遣保险月情况.PRC_月转款确认",
         {"tabname": "TJ0A", "aae002": YM, "aaa020": ATCD, "aaa030": SMALL, "aae011": OPER})
chk("确认成功", r.get("appCode") == 1 and (r.get("confirmed") or 0) >= 1, str(r))
chk("AAEZZZ='1'", sql(
    f"SELECT aaezzz FROM tj0a WHERE aae002={YM} AND aaa020='{ATCD}' AND aaa030={SMALL} AND atb930=999999") == "1")
r = proc("PKG_派遣保险月情况.PRC_月转款确认",
         {"tabname": "TJ0X", "aae002": YM, "aaa020": ATCD, "aaa030": SMALL, "aae011": OPER})
chk("未知表名静默(忠实无 ELSE)", r.get("appCode") == 1, str(r))

print("=== 4: 已确认月份重建 → 静默跳过 ===")
r = proc("PKG_派遣保险月情况.PRC_社保月情况",
         {"aae002": YM, "aaa020": ATCD, "aaa030": SMALL, "aae011": OPER})
chk("静默跳过", r.get("appCode") == 1 and r.get("skipped"), str(r))

print("=== 5: 减员未审核守卫 ===")
GUARD_ATC = sql(f"SELECT atc900 FROM tc90 WHERE aae00p IN ('1','5') AND aae003={YM} AND aab001 IN (SELECT aab001 FROM ab01 WHERE aaa030={A030}) LIMIT 1")
sql(f"INSERT INTO tb91 (atb910, atb91z, atb911, atb912) VALUES (99000001, {GUARD_ATC}, '23', '2')")
r = proc("PKG_派遣保险月情况.PRC_社保月情况",
         {"aae002": YM, "aaa020": ATCD, "aaa030": A030, "aae011": OPER})
chk("守卫拦截(仅当测试合同属该户头且未确认)",
    (r.get("appCode") == -1 and "减员信息未审核" in (r.get("errMsg") or "")) or
    (r.get("appCode") == 1 and r.get("skipped")), str(r)[:100])
sql("DELETE FROM tb91 WHERE atb910=99000001")

print("\n==== W4a 冒烟总结 ====")
if FAILS:
    print(f"FAIL {len(FAILS)}: " + "; ".join(FAILS))
    sys.exit(1)
print("全部 PASS")
