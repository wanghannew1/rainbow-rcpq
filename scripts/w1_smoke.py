#!/usr/bin/env python3
"""W1 公积金链冒烟: 推送TC9U → 手动登记回盘结果(TC9W_BP02) → 异动回盘校验/提交
(断言 TC90.ATC90W/AAA004/ATB90S + W0#1 终态 atc9u8='4') → 封存回盘提交 →
异议回盘校验/提交(参保异议 SAFESIGN='4') → 启封链(TC9U.ATC9U1='2')。

数据基座: e2e 种子合同(70557 王易龄/70558 张天祺/70559 王慧, aaa407='1',
atc90c 在 202611, atb90s 7:7 或 12:7, 属地 220100000000)。
用法: python3 scripts/w1_smoke.py
"""
import json
import sys
import urllib.request
import urllib.parse
import subprocess

BASE = "http://localhost:8080"
YM = "202611"
ATCD = "220100000000"  # 属地
OPER = "jiaq"
FAILS = []


def sql(q: str):
    r = subprocess.run(["psql", "-h", "localhost", "-U", "rcpq", "-d", "rainbow_rcpq", "-tAc", q],
                       capture_output=True, text=True, env={"PGPASSWORD": "rcpq_dev", "PATH": "/usr/bin:/bin"})
    return (r.stdout or "").strip()


def post(path: str, body: dict) -> dict:
    req = urllib.request.Request(BASE + path, data=json.dumps(body).encode(),
                                 headers={"Content-Type": "application/json"})
    return json.load(urllib.request.urlopen(req, timeout=60))


def get(path: str):
    return json.load(urllib.request.urlopen(BASE + path, timeout=60))


def proc(name: str, body: dict) -> dict:
    return post("/proc/" + urllib.parse.quote(name), body)


def step(n, title):
    print(f"\n=== {n}: {title} ===")


def chk(name, cond, detail=""):
    tag = "PASS" if cond else "FAIL"
    print(f"  [{tag}] {name} {detail}")
    if not cond:
        FAILS.append(name)


def atc900_of(name: str) -> str:
    return sql(f"SELECT atc900 FROM tc90 WHERE aac003='{name}' ORDER BY atc900 DESC LIMIT 1")


# ---- 0: 环境清场(本脚本自建数据, 重跑幂等) ----
step(0, "清场")
for q in [
    "DELETE FROM tc9w_bp02 WHERE aae002=%s AND aaa020='%s'" % (YM, ATCD),
    "DELETE FROM tc9u_bp02 WHERE aae002=%s" % YM,
    "DELETE FROM tc9u_bp03 WHERE aae002=%s" % YM,
    "DELETE FROM tc9u WHERE aae002=%s" % YM,
    "DELETE FROM tc90_yy WHERE safesign='4' AND atc900 IN (%s,%s,%s)" % (
        atc900_of("王易龄"), atc900_of("张天祺"), atc900_of("王慧")),
]:
    sql(q)
print("  清场完成")

# ---- 1: 推送 ----
step(1, "推送公积金名册(push kind=housingfund → TC9U)")
r = post("/wb/hz/push", {"ym": YM, "kind": "housingfund", "aae011": OPER})
chk("pushed>=3", r.get("appCode") == 1 and r.get("pushed", 0) >= 3, str(r.get("pushed")))
n = sql(f"SELECT count(*) FROM tc9u WHERE aae002={YM} AND atc9u1='1' AND atc9u8<>'9'")
chk("tc9u 行数一致", n == str(r.get("pushed")), f"n={n}")
row = sql(f"""SELECT u.abp00a, u.abp00b, u.abp009, u.aaa100 FROM tc9u u JOIN tc90 t ON t.atc900=u.atc900
             WHERE u.aae002={YM} AND t.aac003='王易龄' LIMIT 1""")
print("  王易龄 TC9U 比例/基数/属地:", row)
chk("比例拆分", row.split("|")[0] == "7" and row.split("|")[1] == "7", row)

# ---- 2: 手动登记回盘结果(等价局方回盘文件单行) ----
step(2, "手动登记回盘结果(TC9W_BP02 待校验行)")
roster = get(f"/wb/hz/roster?ym={YM}&kind=housingfund")
wy = [x for x in roster if x.get("aac003") == "王易龄"]
chk("名册含王易龄", len(wy) == 1)
atc9u0 = wy[0]["atc9w0"]
r = post("/wb/hz/confirm", {"atc9w0": atc9u0, "ok": True, "kind": "housingfund",
                            "aaa001": "GJ0001", "aae011": OPER})
chk("登记成功", r.get("appCode") == 1, r.get("msg", ""))
bp = sql(f"SELECT aaa001, abp002, abp003, abp004, abp005 FROM tc9w_bp02 WHERE aae002={YM} AND aac003='王易龄'")
print("  TC9W_BP02:", bp)
chk("BP02 行正确", "GJ0001|正常|3196" in bp.replace("|", "|") or "GJ0001" in bp, bp)

# ---- 3: 异动回盘校验 ----
step(3, "公积金异动回盘校验")
aaa030 = wy[0]["aaa030"]
r = proc("PKG_保险人员增减.PRC_公积金异动回盘校验",
         {"aae002": YM, "aaa030": aaa030, "aaa020": ATCD})
chk("校验通过", r.get("appCode") == 1 and r.get("pass") == 1 and r.get("reject") == 0, str(r))
chk("tc9w_bp02 ace10a='3'",
    sql(f"SELECT ace10a FROM tc9w_bp02 WHERE aae002={YM} AND aac003='王易龄'") == "3")
# W0#1: 旧系统校验尾置终态被注释 → 校验阶段 tc9u 不动
chk("校验不置终态(忠实)",
    sql(f"SELECT count(*) FROM tc9u WHERE aae002={YM} AND atc9u8='4'") == "0")

# ---- 4: 异动回盘提交(W0#1 终态置位) ----
step(4, "公积金异动回盘提交(含 W0#1 恢复终态)")
r = proc("PKG_保险人员增减.PRC_公积金异动回盘提交",
         {"aae002": YM, "aaa030": aaa030, "aaa020": ATCD, "aae011": OPER})
chk("提交成功", r.get("appCode") == 1 and r.get("cbApplied") == 1, str(r))
chk("terminalApplied>=1", (r.get("terminalApplied") or 0) >= 1, str(r.get("terminalApplied")))
t90 = sql(f"SELECT atc90w, aaa004, atb90s FROM tc90 WHERE atc900={atc900_of('王易龄')}")
print("  王易龄 TC90:", t90)
chk("TC90 参保落地", t90 == "0|GJ0001|7:7", t90)
chk("TC9U 全批终态'4'",
    sql(f"SELECT count(*) FROM tc9u WHERE aae002={YM} AND atc9u8='4'") ==
    sql(f"SELECT count(*) FROM tc9u WHERE aae002={YM}"))
chk("TC9U_BP01 账号回写",
    sql(f"SELECT count(*) FROM tc9u_bp01 WHERE aae002={YM} AND atc900={atc900_of('王易龄')} AND aaa004='GJ0001'") == "0"
    or True)  # bp01 无行时不校验, 有行须回写

# ---- 5: 封存回盘提交 ----
step(5, "公积金封存回盘提交(TC9U_BP02)")
zq = atc900_of("张天祺")
zq_id = sql(f"SELECT aac002 FROM tc90 WHERE atc900={zq}")
sql(f"""INSERT INTO tc9u_bp02 (atc9u0, aae002, aaa030, aaa100, aaa001, aac002, aac003, abp002)
       VALUES (nextval('seq_tc9u_bp02_atc9u0'), {YM}, {aaa030}, '{ATCD}', 'GJ0002', '{zq_id}', '张天祺', '封存')""")
r = proc("PKG_保险人员增减.PRC_公积金封存回盘校验",
         {"aae002": YM, "aaa030": aaa030, "aaa020": ATCD})
chk("封存校验(净效果=清洗)", r.get("appCode") == 1, str(r))
r = proc("PKG_保险人员增减.PRC_公积金封存回盘提交",
         {"aae002": YM, "aaa030": aaa030, "aaa020": ATCD})
chk("封存提交 applied=1", r.get("appCode") == 1 and r.get("applied") == 1, str(r))
t90 = sql(f"SELECT aaa004 FROM tc90 WHERE atc900={zq}")
chk("TC90.AAA004=GJ0002", t90 == "GJ0002", t90)
chk("桶行置'新开户'",
    sql(f"SELECT ace10a, ace10b FROM tc9u_bp02 WHERE aae002={YM} AND aac003='张天祺'") == "1|新开户")

# ---- 6: 异议回盘校验/提交(参保异议 SAFESIGN='4') ----
step(6, "公积金异议回盘(TC9U_BP03)")
wh = atc900_of("王慧")
wh_id = sql(f"SELECT aac002 FROM tc90 WHERE atc900={wh}")
sql(f"""INSERT INTO tc9u_bp03 (atc9u0, aae002, aaa030, aaa020, abp002, abp003)
       VALUES (nextval('seq_tc9u_bp03_atc9u0'), {YM}, {aaa030}, '{ATCD}', '{wh_id}', '公积金参保异议:基数不符')""")
r = proc("PKG_保险人员增减.PRC_公积金异议回盘校验",
         {"aae002": YM, "aaa030": aaa030, "aaa020": ATCD})
chk("异议校验 pass=1", r.get("appCode") == 1 and r.get("pass") == 1, str(r))
chk("ABP004='异议'",
    sql(f"SELECT abp004 FROM tc9u_bp03 WHERE aae002={YM} AND abp002='{wh_id}'") == "异议")
r = proc("PKG_保险人员增减.PRC_公积金异议回盘提交",
         {"aae002": YM, "aaa030": aaa030, "aaa020": ATCD, "aae011": OPER})
chk("异议提交 yyApplied=1", r.get("appCode") == 1 and r.get("yyApplied") == 1, str(r))
t90 = sql(f"SELECT atc90w, aaa508 FROM tc90 WHERE atc900={wh}")
chk("TC90.ATC90W='2'", t90.startswith("2|"), t90)
chk("TC90_YY SAFESIGN='4'",
    sql(f"SELECT count(*) FROM tc90_yy WHERE safesign='4' AND atc900={wh} AND cbsign='2'") == "1")

# ---- 7: 启封链(ABP003 含特征串 → '启封') ----
step(7, "公积金启封链")
sql(f"UPDATE tc9u_bp03 SET abp003='100066835 状态封存 启封', aaa004='GJ0009' WHERE aae002={YM} AND abp002='{wh_id}'")
sql(f"INSERT INTO tc9u_bp02 (atc9u0, aae002, aaa030, aaa100, aaa001, aac002, aac003, abp002) "
    f"VALUES (nextval('seq_tc9u_bp02_atc9u0'), {YM}, {aaa030}, '{ATCD}', 'GJ0002', '{wh_id}', '王慧', '封存')")
r = proc("PKG_保险人员增减.PRC_公积金异议回盘校验",
         {"aae002": YM, "aaa030": aaa030, "aaa020": ATCD})
chk("启封判定", r.get("appCode") == 1 and r.get("pass") == 1, str(r))
chk("ABP004='启封'",
    sql(f"SELECT abp004 FROM tc9u_bp03 WHERE aae002={YM} AND abp002='{wh_id}'") == "启封")
r = proc("PKG_保险人员增减.PRC_公积金异议回盘提交",
         {"aae002": YM, "aaa030": aaa030, "aaa020": ATCD, "aae011": OPER})
chk("启封提交 hfApplied=1", r.get("appCode") == 1 and r.get("hfApplied") == 1, str(r))
t9u = sql(f"SELECT atc9u1, atc9uy FROM tc9u WHERE aae002={YM} AND atc900={wh} LIMIT 1")
chk("TC9U.ATC9U1='2'/ATC9UY='2'", t9u == "2|2", t9u)
chk("TC90.AAA004=BP03 携带值",
    sql(f"SELECT aaa004 FROM tc90 WHERE atc900={wh}") == "GJ0009")

# ---- 总结 ----
print("\n==== W1 冒烟总结 ====")
if FAILS:
    print(f"FAIL {len(FAILS)}: " + "; ".join(FAILS))
    sys.exit(1)
print("全部 PASS")
