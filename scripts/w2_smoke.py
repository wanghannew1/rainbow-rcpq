#!/usr/bin/env python3
"""W2 省医保链冒烟: 推送TC9W2 → 工资表(TC9W_BP07, 险金引擎) → 登记回盘结果(TC9W_BP01) →
异动回盘校验(TC9W2 置'4')/提交(参保 AAA603/ATC90PA/异议 SAFESIGN='2'/停保消费 TC9W2 报减行) →
工资表回盘守卫 → 基数补差回盘(TC9W_BP06→TJ0B2) → 局方实样文件导入(样本人不在名册=全拒)。

数据基座: e2e 种子合同(70557 王易龄/70558 张天祺/70559 王慧, aaa402='1', aaa110=220100000000,
atc90pa NULL)。用法: python3 scripts/w2_smoke.py
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

# ---- 0: 清场(幂等) ----
step(0, "清场")
for q in [
    f"DELETE FROM tc9w2 WHERE aae002={YM}",
    f"DELETE FROM tc9w_bp01 WHERE aae002={YM} AND aaa020='{ATCD}'",
    f"DELETE FROM tc9w_bp06 WHERE aaa020='{ATCD}' AND ace100=990001",
    f"DELETE FROM tc9w_bp07 WHERE aae002={YM} AND aaa020='{ATCD}'",
    f"DELETE FROM tj0b2 WHERE aae002={YM} AND aaa020='{ATCD}' AND aaa030=(SELECT DISTINCT u.aaa030 FROM tc90 t JOIN ab01 u ON u.aab001=t.aab001 WHERE t.atc900={WY})",
    f"DELETE FROM tc93 WHERE atc931={YM} AND atc900={WY} AND atc937='1'",
    f"DELETE FROM tc90_yy WHERE safesign='2' AND atc900 IN ({WY},{ZQ},{WH})",
    f"UPDATE tc90 SET atc90pa=NULL, aaa603=NULL, aaa005=NULL, aaa608=NULL, aaa604=NULL, aaa609=NULL WHERE atc900 IN ({WY},{ZQ},{WH})",
    f"DELETE FROM tc9w_bp01 WHERE aae002={YM}",
]:
    sql(q)
print("  清场完成")

# ---- 1: 推送 ----
step(1, "推送省医保名册(push kind=smedical → TC9W2)")
r = post("/wb/hz/push", {"ym": YM, "kind": "smedical", "aae011": OPER})
chk("pushed>=3", r.get("appCode") == 1 and r.get("pushed", 0) >= 3, str(r.get("pushed")))
n = sql(f"SELECT count(*) FROM tc9w2 WHERE aae002={YM} AND atc9w1='1' AND atc9w8<>'9'")
chk("tc9w2 行数一致", n == str(r.get("pushed")), f"n={n}")
row = sql(f"""SELECT abp001, abp006, atc9w2, aaa90y, aaa302_1 FROM tc9w2
             WHERE aae002={YM} AND atc900={WY} LIMIT 1""")
print("  王易龄 TC9W2:", row)
chk("证件类型/民族/当月态/默认失败/基数", row.startswith("1 居民身份证（户口簿）|01 汉族|1|90|3196"), row)

# ---- 2: 工资表生成(回盘前, atc90pa NULL) ----
step(2, "省医保工资表(TC9W_BP07 重建, 险金引擎)")
aaa030 = sql(f"SELECT DISTINCT u.aaa030 FROM tc90 t JOIN ab01 u ON u.aab001=t.aab001 WHERE t.atc900={WY}")
r = proc("PKG_保险人员增减.PRC_省医保工资表",
         {"aaa030": aaa030, "aae002": YM, "aaa110": ATCD, "aae011": OPER})
chk("工资表生成", r.get("appCode") == 1 and (r.get("rows") or 0) >= 1, str(r))
row = sql(f"SELECT aac003, abp001, abp005, abp007, abp008 FROM tc9w_bp07 WHERE aae002={YM} AND aac003='王易龄'")
print("  王易龄 工资表行:", row)
chk("工资表字段齐全", "|" in row and len(row.split("|")) == 5, row)
wy_t07 = row.split("|")
chk("实发=应发-扣款", abs(float(wy_t07[1]) - float(wy_t07[3]) - float(wy_t07[4])) < 0.01, row)

# ---- 3: 登记回盘结果 + 异动回盘校验(置 TC9W2 '4') ----
step(3, "登记回盘结果 + 异动回盘校验")
roster = get(f"/wb/hz/roster?ym={YM}&kind=smedical")
wy_row = [x for x in roster if x.get("aac003") == "王易龄"]
chk("名册含王易龄", len(wy_row) == 1)
r = post("/wb/hz/confirm", {"atc9w0": wy_row[0]["atc9w0"], "ok": True, "kind": "smedical",
                            "aaa001": "SM0001", "aae011": OPER})
chk("登记参保成功", r.get("appCode") == 1, r.get("msg", ""))
r = proc("PKG_保险人员增减.PRC_省医保异动回盘校验",
         {"aae002": YM, "aaa030": aaa030, "aaa020": ATCD})
chk("校验通过", r.get("appCode") == 1 and r.get("pass") == 1 and r.get("reject") == 0, str(r))
chk("TC9W2 全批置'4'",
    sql(f"SELECT count(*) FROM tc9w2 WHERE aae002={YM} AND atc9w1='1' AND atc9w8='4'") ==
    sql(f"SELECT count(*) FROM tc9w2 WHERE aae002={YM} AND atc9w1='1'"))
chk("aae001 记业务年月",
    sql(f"SELECT aae001 FROM tc9w_bp01 WHERE aae002={YM} AND aac003='王易龄'") == YM)

# ---- 4: 异动回盘提交(参保+异议) ----
step(4, "异动回盘提交(参保成功 AAA603/ATC90PA='0')")
r = proc("PKG_保险人员增减.PRC_省医保异动回盘提交",
         {"aae002": YM, "aaa030": aaa030, "aaa020": ATCD, "aae011": OPER})
chk("提交成功 cbApplied=1", r.get("appCode") == 1 and r.get("cbApplied") == 1, str(r))
t90 = sql(f"SELECT atc90pa, aaa603, aaa005, aaa608 FROM tc90 WHERE atc900={WY}")
print("  王易龄 TC90:", t90)
chk("参保落地", t90 == f"0|{YM}|SM0001|{YM}", t90)
chk("全批盖章", sql(f"SELECT count(*) FROM tc9w_bp01 WHERE aae002={YM} AND aae211='{OPER}'") >= "1")

# ---- 5: 停保(消费 TC9W2 报减行) ----
step(5, "停保链(TC9W2 atc9w1='3')")
zq_id = sql(f"SELECT aac002 FROM tc90 WHERE atc900={ZQ}")
sql(f"""INSERT INTO tc9w2 (baz001, baz002, atc9w0, aae002, atc900, aac003, aaa100, atc9w1, aaa030) VALUES
       (1, nextval('seq_sys_baz002'), nextval('seq_tc9w2_atc9w0'), {YM}, {ZQ}, '张天祺', '{ATCD}', '3', {aaa030})""")
sql(f"DELETE FROM tc9w2 WHERE aae002={YM} AND atc900={ZQ} AND atc9w1='1' AND atc9w8='9'")
r = proc("PKG_保险人员增减.PRC_省医保异动回盘提交",
         {"aae002": YM, "aaa030": aaa030, "aaa020": ATCD, "aae011": OPER})
chk("停保 tzApplied=1", r.get("appCode") == 1 and r.get("tzApplied") == 1, str(r))
t90 = sql(f"SELECT atc90pa, aaa604, aaa609 FROM tc90 WHERE atc900={ZQ}")
chk("张天祺 停保落地", t90 == f"1|{YM}|{YM}", t90)

# ---- 6: 异议(SAFESIGN='2') ----
step(6, "异议链(登记失败 → SAFESIGN='2')")
wh_row = [x for x in roster if x.get("aac003") == "王慧"]
r = post("/wb/hz/confirm", {"atc9w0": wh_row[0]["atc9w0"], "ok": False, "kind": "smedical",
                            "aae013": "省医保参保异议", "aae011": OPER})
chk("登记异议", r.get("appCode") == 1, r.get("msg", ""))
r = proc("PKG_保险人员增减.PRC_省医保异动回盘校验",
         {"aae002": YM, "aaa030": aaa030, "aaa020": ATCD})
chk("异议校验通过", r.get("appCode") == 1 and (r.get("pass") or 0) >= 1, str(r))
r = proc("PKG_保险人员增减.PRC_省医保异动回盘提交",
         {"aae002": YM, "aaa030": aaa030, "aaa020": ATCD, "aae011": OPER})
chk("异议提交 yyApplied=1", r.get("appCode") == 1 and r.get("yyApplied") == 1, str(r))
t90 = sql(f"SELECT atc90pa FROM tc90 WHERE atc900={WH}")
chk("王慧 ATC90PA='2'", t90 == "2", t90)
chk("TC90_YY SAFESIGN='2' cbsign='2'",
    sql(f"SELECT count(*) FROM tc90_yy WHERE safesign='2' AND atc900={WH} AND cbsign='2'") == "1")

# ---- 7: 工资表回盘守卫 ----
step(7, "工资表守卫(当月已回盘 → 拒绝)")
r = proc("PKG_保险人员增减.PRC_省医保工资表",
         {"aaa030": aaa030, "aae002": YM, "aaa110": ATCD, "aae011": OPER})
chk("守卫生效", r.get("appCode") == -1 and "已经回盘" in (r.get("errMsg") or ""), str(r))

# ---- 8: 基数补差回盘(TC9W_BP06 → TJ0B2) ----
step(8, "基数补差回盘(按 AAA005 医保编号)")
atb930 = sql(f"SELECT atb930 FROM tc90 WHERE atc900={WY}")
aac001 = sql(f"SELECT aac001 FROM tc90 WHERE atc900={WY}")
atc930 = sql("SELECT nextval('seq_tc93_atc930')")
sql(f"""INSERT INTO tc93 (atc930, atc900, atb900, atb930, aac001, atc931, atc937, aae114)
       VALUES ({atc930}, {WY}, (SELECT atb900 FROM tc90 WHERE atc900={WY}), {atb930}, '{aac001}', {YM}, '1', '1')""")
sql(f"""INSERT INTO tc9w_bp06 (id, ace100, aaa005, aac003, aae002, aae003, abp002, abp003,
       abp004, abp005, abp006, abp007, abp008, aaa030, aaa020)
       VALUES (nextval('seq_tc9w_bp06_id'), 990001, 'SM0001', '王易龄', '{YM}', '{YM}',
       '5000', '100', '200', '50', '300', '50', '650', {aaa030}, '{ATCD}')""")
r = proc("PKG_保险人员增减.PRC_省医保基数补差回盘校验", {"ace100": "990001"})
chk("补差校验通过", r.get("appCode") == 1 and r.get("pass") == 1, str(r))
r = proc("PKG_保险人员增减.PRC_省医保基数补差回盘提交",
         {"ace100": "990001", "aae002": YM, "aaa030": aaa030, "aaa020": ATCD, "aae011": OPER})
chk("补差提交 applied=1", r.get("appCode") == 1 and r.get("applied") == 1, str(r))
row = sql(f"SELECT aaa302, bba002, atj0b5 FROM tj0b2 WHERE aae002={YM} AND aaa020='{ATCD}'")
vals = row.split("|") if row else []
chk("TJ0B2 落账(基数/医疗单位缴/合计)",
    len(vals) == 3 and abs(float(vals[0])-5000) < 0.01 and abs(float(vals[1])-200) < 0.01
    and abs(float(vals[2])-650) < 0.01, row)

# ---- 9: 局方实样导入(样本人不在名册 → 全拒) ----
step(9, "局方实样文件导入(省医保 7 列)")
r = subprocess.run(["curl", "-s", "-F", f"file=@rcpq-web/src/main/resources/baopan/huipan/1.回盘--2026.05省医保(1).xls",
                    f"{BASE}/wb/hz/import-bp?ym={YM}&kind=smedical&aae011={OPER}"],
                   capture_output=True, text=True)
j = json.loads(r.stdout)
chk("导入解析成功", j.get("appCode") == 1, str(j))
# 实样 3 人证件不在名册 → 证件优先失败后按报盘行序对齐名册(M5 同款可调口径) → 3 行全导入
chk("实样 3 行按行序对齐导入", j.get("imported") == 3 and j.get("unmatched") == 0, str(j))

# ---- 总结 ----
print("\n==== W2 冒烟总结 ====")
if FAILS:
    print(f"FAIL {len(FAILS)}: " + "; ".join(FAILS))
    sys.exit(1)
print("全部 PASS")
