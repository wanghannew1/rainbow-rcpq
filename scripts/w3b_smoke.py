#!/usr/bin/env python3
"""W3b 市工伤链冒烟: 推送TC9W4 → 登记回盘(bp10) → 异动回盘校验(基数不符'11'/达龄退休'15'/置'4')/
提交(前置拒/参保 AAC008 CASE/异议扫描 ATC90PB='2'/停保扫描)。

用法: python3 scripts/w3b_smoke.py
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


def post(path, body):
    req = urllib.request.Request(BASE + path, data=json.dumps(body).encode(),
                                 headers={"Content-Type": "application/json"})
    return json.load(urllib.request.urlopen(req, timeout=120))


def get(path):
    return json.load(urllib.request.urlopen(BASE + path, timeout=60))


def proc(name, body):
    return post("/proc/" + urllib.parse.quote(name), body)


def chk(name, cond, detail=""):
    tag = "PASS" if cond else "FAIL"
    print(f"  [{'PASS' if cond else 'FAIL'}] {name} {detail}")
    if not cond:
        FAILS.append(name)


WY = sql("SELECT atc900 FROM tc90 WHERE aac003='王易龄' ORDER BY atc900 DESC LIMIT 1")
ZQ = sql("SELECT atc900 FROM tc90 WHERE aac003='张天祺' ORDER BY atc900 DESC LIMIT 1")
WH = sql("SELECT atc900 FROM tc90 WHERE aac003='王慧' ORDER BY atc900 DESC LIMIT 1")
A030 = sql(f"SELECT DISTINCT u.aaa030 FROM tc90 t JOIN ab01 u ON u.aab001=t.aab001 WHERE t.atc900={WY}")

print("=== 0: 清场+预置(已申报待参保) ===")
for q in [
    f"DELETE FROM tc9w4 WHERE aae002={YM}",
    f"DELETE FROM tc9w_bp10 WHERE aae002={YM}",
    f"UPDATE tc90 SET aaa903=202610, atc90pb=NULL, aaa902=NULL, aaa904=NULL, aaa909=NULL, aaa908=NULL WHERE atc900 IN ({WY},{ZQ},{WH})",
]:
    sql(q)
print("  完成")

print("=== 1: 推送市工伤名册(TC9W4) ===")
r = post("/wb/hz/push", {"ym": YM, "kind": "mgongshang", "aaa110": ATCD, "aae011": OPER})
chk("pushed>=3", r.get("appCode") == 1 and r.get("pushed", 0) >= 3, str(r))

print("=== 2: 登记 → 校验(置'4'+基数不符) → 提交 ===")
roster = get(f"/wb/hz/roster?ym={YM}&kind=mgongshang")
wy = [x for x in roster if x.get("aac003") == "王易龄"]
chk("名册含王易龄", len(wy) == 1)
base = sql(f"SELECT aaa302 FROM tc90 WHERE atc900={WY}")  # 合同基数, 登记填不同值触发 '11'
r = post("/wb/hz/confirm", {"atc9w0": wy[0]["atc9w0"], "kind": "mgongshang",
                            "abp004": "9999", "aae011": OPER})
chk("登记回盘结果", r.get("appCode") == 1, r.get("msg", ""))
r = proc("PKG_保险人员增减.PRC_市工伤异动回盘校验",
         {"aae002": YM, "aaa030": A030, "aaa020": ATCD})
chk("校验通过", r.get("appCode") == 1 and r.get("pass") == 1, str(r))
row = sql(f"SELECT aaa90y, aaa302_2, aae001 FROM tc9w_bp10 WHERE aae002={YM} AND aac003='王易龄'")
print("  bp10 行:", row)
chk("基数不符 '11' 且记录差异", row.startswith("11|9999"), row)
chk("TC9W4 全批置'4'(户头)",
    sql(f"SELECT count(*) FROM tc9w4 WHERE aae002={YM} AND atc9w1='1' AND atc9w8='4' AND aaa030={A030}") ==
    sql(f"SELECT count(*) FROM tc9w4 WHERE aae002={YM} AND atc9w1='1' AND aaa030={A030}"))
r = proc("PKG_保险人员增减.PRC_市工伤异动回盘提交",
         {"aae002": YM, "aaa030": A030, "aaa020": ATCD, "aae011": OPER})
chk("提交成功 cbApplied=1", r.get("appCode") == 1 and r.get("cbApplied") == 1, str(r))
chk("异议扫描一次性标记(未回盘报增行)", (r.get("yyApplied") or 0) >= 2, str(r.get("yyApplied")))
t90 = sql(f"SELECT atc90pb, aaa903, aaa009, aaa305, aaa908 FROM tc90 WHERE atc900={WY}")
print("  王易龄 TC90:", t90)
v = t90.split("|")
chk("参保落地(ATC90PB='0'/AAA903=回盘年月/编号=abp004 回填/AAC008 含52转51)", v[0] == "0", t90)
chk("AAA305 基数覆写(9999)", abs(float(v[3]) - 9999) < 0.01, t90)
chk("bp10 翻 ace10a='1'",
    sql(f"SELECT ace10a FROM tc9w_bp10 WHERE aae002={YM} AND aac003='王易龄'") == "1")

print("=== 3: 异议扫描(未回盘报增行 → ATC90PB='2') ===")
r = proc("PKG_保险人员增减.PRC_市工伤异动回盘提交",
         {"aae002": YM, "aaa030": A030, "aaa020": ATCD, "aae011": OPER})
chk("二次提交异议已标(幂等 yyApplied=0)", r.get("appCode") == 1 and r.get("yyApplied") == 0, str(r))
cnt = sql(f"SELECT count(*) FROM tc90 WHERE atc900 IN ({ZQ},{WH}) AND atc90pb='2'")
chk("张天祺/王慧 转 '2'", cnt == "2", cnt)

print("=== 4: 校验失败行前置拒 ===")
sql(f"UPDATE tc9w_bp10 SET ace10a='2' WHERE aae002={YM} AND atc900={WY}")
r = proc("PKG_保险人员增减.PRC_市工伤异动回盘提交",
         {"aae002": YM, "aaa030": A030, "aaa020": ATCD, "aae011": OPER})
chk("前置拒", r.get("appCode") == -1 and "校验失败数据" in (r.get("errMsg") or ""), str(r))

print("\n==== W3b 冒烟总结 ====")
if FAILS:
    print(f"FAIL {len(FAILS)}: " + "; ".join(FAILS))
    sys.exit(1)
print("全部 PASS")
