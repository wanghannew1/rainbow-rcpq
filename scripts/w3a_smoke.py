#!/usr/bin/env python3
"""W3a 省工伤链冒烟: 推送TC9W3(W0#3 行级兜底) → 登记回盘(bp09) → 异动回盘校验(置'4')/
提交(ATC90PB 状态机+翻 ace10a='1'+AAA305 覆写) → 停保(TC9W3 报减行) → 异议(SAFESIGN='3') →
减免(TC09_BP→TC09 重建) → 补差(TC9W_BP08→TJ0B3 跨年逐月摊分, 缺月校验置'3'忠实)。

数据基座: e2e 种子合同(70557 王易龄/70558 张天祺/70559 王慧, aaa404='1')。
用法: python3 scripts/w3a_smoke.py
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


def sql(q: str):
    r = subprocess.run(["psql", "-h", "localhost", "-U", "rcpq", "-d", "rainbow_rcpq", "-tAc", q],
                       capture_output=True, text=True, env={"PGPASSWORD": "rcpq_dev", "PATH": "/usr/bin:/bin"})
    return (r.stdout or "").strip()


def post(path: str, body: dict) -> dict:
    req = urllib.request.Request(BASE + path, data=json.dumps(body).encode(),
                                 headers={"Content-Type": "application/json"})
    return json.load(urllib.request.urlopen(req, timeout=120))


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


WY, ZQ, WH = atc900_of("王易龄"), atc900_of("张天祺"), atc900_of("王慧")

# ---- 0: 清场 + 工伤态预置(已申报待参保语义: aaa903<当月, atc90pb NULL) ----
step(0, "清场+预置")
for q in [
    f"DELETE FROM tc9w3 WHERE aae002={YM}",
    f"DELETE FROM tc9w_bp09 WHERE aae002={YM}",
    f"DELETE FROM tc9w_bp08 WHERE ace100=990003",
    f"DELETE FROM tc09_bp WHERE ace100=990002",
    f"DELETE FROM tc09 WHERE aae002={YM} AND aaa020='{ATCD}' AND atc900 IN ({WY},{ZQ},{WH})",
    f"DELETE FROM tj0b3 WHERE aae002={YM} AND atc900 IN ({WY},{ZQ},{WH})",
    f"DELETE FROM tc93 WHERE atc900={WY} AND atc931={YM} AND atc937='1'",
    f"DELETE FROM tc90_yy WHERE safesign='3' AND atc900 IN ({WY},{ZQ},{WH})",
    f"UPDATE tc90 SET aaa903=202610, atc90pb=NULL, aaa902=NULL, aaa904=NULL, aaa909=NULL, aaa908=NULL WHERE atc900 IN ({WY},{ZQ},{WH})",
]:
    sql(q)
sexok = sql(f"SELECT count(*) FROM aa10 d WHERE aaa100='AAC004' AND EXISTS "
            f"(SELECT 1 FROM tc90 t WHERE t.atc900 IN ({WY},{ZQ},{WH}) AND t.aac004=d.aaa102)")
chk("性别码入字典(W0#3 前置)", sexok == "1", f"sexok={sexok}")
print("  清场完成")

# ---- 1: 推送 ----
step(1, "推送省工伤名册(push kind=sgongshang → TC9W3)")
r = post("/wb/hz/push", {"ym": YM, "kind": "sgongshang", "aaa110": ATCD, "aae011": OPER})
chk("pushed>=3", r.get("appCode") == 1 and r.get("pushed", 0) >= 3, str(r))
n = sql(f"SELECT count(*) FROM tc9w3 WHERE aae002={YM} AND atc9w1='1' AND atc9w8<>'9'")
chk("tc9w3 行数一致", n == str(r.get("pushed")), f"n={n}")
row = sql(f"SELECT abp001, abp00c, abp00b, aaa302_1, aaa90y FROM tc9w3 WHERE aae002={YM} AND atc900={WY} LIMIT 1")
print("  王易龄 TC9W3:", row)
chk("证件/户口(字典或兜底)/在编NULL/基数/默认失败",
    row.startswith("1 居民身份证（户口簿）|11 本市") and "|90" in row and "3196.00" in row, row)

# ---- 2: 登记回盘 + 校验(置'4') + 提交(ATC90PB 状态机) ----
step(2, "异动回盘: 登记 → 校验 → 提交")
roster = get(f"/wb/hz/roster?ym={YM}&kind=sgongshang")
wy = [x for x in roster if x.get("aac003") == "王易龄"]
chk("名册含王易龄", len(wy) == 1)
r = post("/wb/hz/confirm", {"atc9w0": wy[0]["atc9w0"], "ok": True, "kind": "sgongshang",
                            "aaa001": "GS0001", "aae011": OPER})
chk("登记参保成功", r.get("appCode") == 1, r.get("msg", ""))
r = proc("PKG_保险人员增减.PRC_省工伤异动回盘校验",
         {"aae002": YM, "aaa030": wy[0]["aaa030"], "aaa020": ATCD})
chk("校验通过", r.get("appCode") == 1 and r.get("pass") == 1, str(r))
chk("TC9W3 全批置'4'(校验按户头+属地, 忠实范围)",
    sql(f"SELECT count(*) FROM tc9w3 WHERE aae002={YM} AND atc9w1='1' AND atc9w8='4' AND aaa030={wy[0]['aaa030']}") ==
    sql(f"SELECT count(*) FROM tc9w3 WHERE aae002={YM} AND atc9w1='1' AND aaa030={wy[0]['aaa030']}"))
r = proc("PKG_保险人员增减.PRC_省工伤异动回盘提交",
         {"aae002": YM, "aaa030": wy[0]["aaa030"], "aaa020": ATCD, "aae011": OPER})
chk("提交成功 cbApplied=1", r.get("appCode") == 1 and r.get("cbApplied") == 1, str(r))
t90 = sql(f"SELECT atc90pb, aaa903, aaa009, aaa908 FROM tc90 WHERE atc900={WY}")
print("  王易龄 TC90:", t90)
chk("参保落地(atc90pb='0'/aaa903=参保年月/编号/盖章年月)", t90 == f"0|{YM}|GS0001|{YM}", t90)
chk("bp09 翻 ace10a='1'(工伤特有)",
    sql(f"SELECT ace10a FROM tc9w_bp09 WHERE aae002={YM} AND aac003='王易龄'") == "1")

# ---- 3: 停保(TC9W3 报减行) ----
step(3, "停保链")
zq_id = sql(f"SELECT aac002 FROM tc90 WHERE atc900={ZQ}")
sql(f"""INSERT INTO tc9w3 (baz001, baz002, atc9w0, aae002, atc900, aac003, aaa100, atc9w1, aaa030) VALUES
       (nextval('seq_sys_baz001'), nextval('seq_sys_baz002'), nextval('seq_tc9w_atc9w0'),
       {YM}, {ZQ}, '张天祺', '{ATCD}', '3', {wy[0]['aaa030']})""")
r = proc("PKG_保险人员增减.PRC_省工伤异动回盘提交",
         {"aae002": YM, "aaa030": wy[0]["aaa030"], "aaa020": ATCD, "aae011": OPER})
chk("停保 tzApplied=1", r.get("appCode") == 1 and r.get("tzApplied") == 1, str(r))
t90 = sql(f"SELECT atc90pb, aaa904, aaa909 FROM tc90 WHERE atc900={ZQ}")
chk("张天祺 停保落地", t90 == f"1|{YM}|{YM}", t90)

# ---- 4: 异议(SAFESIGN='3') ----
step(4, "异议链")
wh = [x for x in roster if x.get("aac003") == "王慧"]
r = post("/wb/hz/confirm", {"atc9w0": wh[0]["atc9w0"], "ok": False, "kind": "sgongshang",
                            "aae013": "省工伤参保异议", "aae011": OPER})
chk("登记异议", r.get("appCode") == 1, r.get("msg", ""))
proc("PKG_保险人员增减.PRC_省工伤异动回盘校验",
     {"aae002": YM, "aaa030": wy[0]["aaa030"], "aaa020": ATCD})
r = proc("PKG_保险人员增减.PRC_省工伤异动回盘提交",
         {"aae002": YM, "aaa030": wy[0]["aaa030"], "aaa020": ATCD, "aae011": OPER})
chk("异议提交 yyApplied=1", r.get("appCode") == 1 and r.get("yyApplied") == 1, str(r))
chk("王慧 ATC90PB='2'", sql(f"SELECT atc90pb FROM tc90 WHERE atc900={WH}") == "2")
chk("TC90_YY SAFESIGN='3' cbsign='2'",
    sql(f"SELECT count(*) FROM tc90_yy WHERE safesign='3' AND atc900={WH} AND cbsign='2'") == "1")

# ---- 5: 减免(TC09_BP → TC09) ----
step(5, "减免链")
sql(f"""INSERT INTO tc93 (atc930, atc900, atb900, atb930, aac001, aac003, aac002, atc931, atc937, aae114)
       VALUES (nextval('seq_tc93_atc930'), {WY}, (SELECT atb900 FROM tc90 WHERE atc900={WY}),
       (SELECT atb930 FROM tc90 WHERE atc900={WY}), '45123', '王易龄',
       (SELECT aac002 FROM tc90 WHERE atc900={WY}), {YM}, '1', '1')""")
sql(f"""INSERT INTO tc09_bp (id, ace100, aaa005, aac003, aae002, abp002, bba004, atc9eh,
       aaa030, aaa020)
       VALUES (99000200, 990002, 'GS0001', '王易龄', {YM}, '5000', '100', '500',
       {wy[0]['aaa030']}, '{ATCD}')""")
r = proc("PKG_保险人员增减.PRC_省工伤减免校验", {"ace100": "990002"})
chk("减免校验通过", r.get("appCode") == 1 and r.get("pass") == 1, str(r))
r = proc("PKG_保险人员增减.PRC_省工伤减免提交",
         {"ace100": "990002", "aae002": YM, "aaa030": wy[0]["aaa030"], "aaa020": ATCD, "aae011": OPER})
chk("减免提交 applied=1", r.get("appCode") == 1 and r.get("applied") == 1, str(r))
row = sql(f"SELECT aaa302, bba004, atc9eh FROM tc09 WHERE aae002={YM} AND aac003='王易龄'")
vals = row.split("|") if row else []
chk("TC09 落账", len(vals) == 3 and abs(float(vals[0])-5000) < 0.01
    and abs(float(vals[1])-100) < 0.01 and abs(float(vals[2])-500) < 0.01, row)

# ---- 6: 补差(TC9W_BP08 → TJ0B3 跨年桶) ----
step(6, "补差链(区间 202611-202612, 缺 202612 工资)")
sql(f"""INSERT INTO tc9w_bp08 (id, ace100, aaa009, aac003, aae002, aae003, abp006, aaa030, aaa020)
       VALUES (nextval('seq_tc9w_bp06_id'), 990003, 'GS0001', '王易龄', {YM}, '202612-202702',
       '200', {wy[0]['aaa030']}, '{ATCD}')""")
r = proc("PKG_保险人员增减.PRC_省工伤基数补差回盘校验", {"ace100": "990003"})
chk("补差校验(缺月仍置'3'忠实)", r.get("appCode") == 1 and r.get("pass") == 1, str(r))
chk("ace10b 报缺月", "未查询到202612工资发放信息" in
    sql(f"SELECT coalesce(ace10b,'') FROM tc9w_bp08 WHERE ace100=990003"))
r = proc("PKG_保险人员增减.PRC_省工伤基数补差回盘提交",
         {"ace100": "990003", "aae002": YM, "aaa030": wy[0]["aaa030"], "aaa020": ATCD, "aae011": OPER})
chk("补差提交 applied=1", r.get("appCode") == 1 and r.get("applied") == 1, str(r))
n2 = sql(f"SELECT count(*) FROM tj0b3 WHERE aae002={YM} AND atc900={WY}")
chk("TJ0B3 三月落账(202612~202702)", n2 == "3", f"n={n2}")
row = sql(f"SELECT bba004 FROM tj0b3 WHERE aae002={YM} AND atc900={WY} LIMIT 1")
chk("月摊 200/3=66.67", row is not None and abs(float(row)-66.67) < 0.01, row)

# ---- 总结 ----
print("\n==== W3a 冒烟总结 ====")
if FAILS:
    print(f"FAIL {len(FAILS)}: " + "; ".join(FAILS))
    sys.exit(1)
print("全部 PASS")
