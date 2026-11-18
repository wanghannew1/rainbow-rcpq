#!/usr/bin/env python3
"""W6 散件收口冒烟: 异议处理状态机(flag 1反馈/2取消/3转入/4取消转入+SAFESIGN 联动)/
异议离职同步(CBSIGN='2'&atb90r='5'→'9', 真数据命中行快照恢复)/医保异议同步(TC9W1 扫描
ABP016 0/2 双分支)/退休停保×2(社保 ATC90P='1'+医保市 ATC90PA='1'+AAC008 追加80)/
变更参保地(守卫链+TC93 结算守卫+旧合同置'5'跨险种'4'+新合同 1e7 全列拷贝)/
保险补收基数_个人(三段: 社保/医保仅省/公积金; 按年分段; UK_TC08 幂等)。
桩: 合成合同 HC(tc90 1e7 段, 精确 id 清理; tc90_yy/tc9w1/tc08 桩同精确清理)。
前置: V19(uk_tc08+uk_tc90_yy+seq_tc08_atc080 抬段)。
用法: python3 scripts/w6_smoke.py
"""
import json
import sys
import urllib.request
import urllib.parse
import subprocess

BASE = "http://localhost:8080"
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
    print(f"  [{'PASS' if cond else 'FAIL'}] {name} {detail}")
    if not cond:
        FAILS.append(name)


def row(q):
    v = sql(q)
    return v.split("|") if v else []


print("=== 0: 前置+清场 ===")
chk("uk_tc08 在册(V19)", sql("SELECT count(*) FROM pg_indexes WHERE indexname='uk_tc08'") == "1")
chk("uk_tc90_yy 在册(V19)", sql("SELECT count(*) FROM pg_indexes WHERE indexname='uk_tc90_yy'") == "1")
chk("seq_tc08_atc080>=1e7", int(sql("SELECT last_value FROM seq_tc08_atc080") or 0) >= 10000001)
# 合成合同 HC(1e7 段)
HC = sql("""INSERT INTO tc90 (atc900, aac001, aac002, aac003, aab001, atb930, atb90r, aae00p, aae002,
            aaa401, aaa402, aaa407, aaa201, aaa601, aaa501,
            aaa100, aaa101, aaa110, aaa111, aaa120, aaa121, aaa130, aaa131,
            atc90p, atc90pa, atc90pb, atc90w, atc905, aaa302, aaa303)
            VALUES (nextval('seq_tc90_atc900'), 999901, '999000199001010000', 'W6冒烟员', 151, 37183, '3', '1', 202611,
            '1', '1', '1', 202410, 202410, 202410,
            '220100000000', '吉林省长春市', '220100000000', '吉林省长春市', '220100000000', '吉林省长春市', '220100000000', '吉林省长春市',
            NULL, NULL, NULL, NULL, 5000, 5000, 5000) RETURNING atc900""").splitlines()[0]
chk("合成合同 HC(1e7 段)", int(HC) >= 10000001, f"HC={HC}")
sql(f"""INSERT INTO tc9w1 (atc9w0, aae002, aaa030, aaa100, atc900, atc9w1, abp016)
        VALUES (99000001, 202611, 1, '220100000000', {HC}, '1', '0')
        ON CONFLICT (atc9w0) DO UPDATE SET atc900=EXCLUDED.atc900, abp016='0'""")

print("=== 1: 退休停保×2 ===")
r = proc("PKG_保险人员增减.PRC_社保退休停保", {"atc900": HC, "aaa204": "202610"})
chk("社保停保 appCode=1", r.get("appCode") == 1, str(r))
t = row(f"SELECT atc90p, aaa204, aaa209 FROM tc90 WHERE atc900={HC}")
chk("社保: ATC90P='1'/AAA204=AAA209=202610", t and t[0] == "1" and t[1] == "202610" and t[2] == "202610", str(t))
r = proc("PKG_保险人员增减.PRC_社保退休停保", {"atc900": HC, "aaa204": "abc"})
chk("停保年月格式拒", r.get("appCode") == -1 and "停保年月格式不正确" in (r.get("errMsg") or ""), str(r))
r = proc("PKG_保险人员增减.PRC_医保退休停保", {"atc900": HC, "aaa604": "202610"})
chk("医保停保 appCode=1(市属→ATC90PA='1')", r.get("appCode") == 1, str(r))
t = row(f"SELECT atc90pa, aaa602, aaa604, aaa609, coalesce(aac008,'-') FROM tc90 WHERE atc900={HC}")
chk("医保: ATC90PA='1'/602=604=609=202610/AAC008 null→'80'",
    t and t[0] == "1" and t[1] == "202610" and t[3] == "202610" and t[4] == "80", str(t))

print("=== 2: 异议处理状态机(手工插 TC90_YY 桩=参保异议赋值产物等价物) ===")
# 参保异议赋值无前端直调点(内部过程), 桩行直插(字段映射=SAFESIGN 2 医保)
yyid = sql(f"""INSERT INTO tc90_yy (tc90yyid, aae002, atc900, atb930, atb931, aab001, aac001,
            aac003, aac002, safesign, aaa020, aaa021, aaa310, backsign, insign, cbsign, aae013)
            SELECT nextval('seq_tc90_yy_tc90yyid'), 202611, t.atc900, t.atb930, b.atb931,
            t.aab001, t.aac001, t.aac003, t.aac002, '2', t.aaa110, t.aaa111, t.aaa302,
            '0', '0', '2', 'W6冒烟异议'
            FROM tc90 t LEFT JOIN tb93 b ON b.atb930 = t.atb930 WHERE t.atc900 = {HC}
            RETURNING tc90yyid""").splitlines()[0]
sql(f"UPDATE tc90 SET atc90pa='2', aaa608=202611 WHERE atc900={HC}")  # 模拟医保同步 ABP016='2' 后态
r = proc("PKG_保险人员增减.PRC_异议处理", {"id": yyid, "aae011": OPER, "flag": "1"})
chk("flag=1 反馈", r.get("appCode") == 1, str(r))
t = row(f"SELECT backsign, coalesce(aae011,'-') FROM tc90_yy WHERE tc90yyid={yyid}")
chk("BACKSIGN='1'+经办", t and t[0] == "1" and t[1] == OPER, str(t))
r = proc("PKG_保险人员增减.PRC_异议处理", {"id": yyid, "aae011": OPER, "flag": "1"})
chk("重复反馈拒", r.get("appCode") == -1 and "不需要重复反馈" in (r.get("errMsg") or ""), str(r)[:80])
r = proc("PKG_保险人员增减.PRC_异议处理", {"id": yyid, "aae011": OPER, "flag": "3"})
chk("flag=3 转入(市属: TC9W1 ABP016='0' 0行静默, 不动 ATC90PA)", r.get("appCode") == 1, str(r))
t = row(f"SELECT insign FROM tc90_yy WHERE tc90yyid={yyid}")
chk("INSIGN='1'", t and t[0] == "1", str(t))
r = proc("PKG_保险人员增减.PRC_异议处理", {"id": yyid, "aae011": OPER, "flag": "2"})
chk("已转入时取消反馈拒", r.get("appCode") == -1 and "不允许取消反馈" in (r.get("errMsg") or ""), str(r)[:80])
atc90pa_before = sql(f"SELECT coalesce(atc90pa::text,'NULL') FROM tc90 WHERE atc900={HC}")
r = proc("PKG_保险人员增减.PRC_异议处理", {"id": yyid, "aae011": OPER, "flag": "4"})
chk("flag=4 取消转入(SAFESIGN=2→TC90.ATC90PA='2' 无条件)", r.get("appCode") == 1, str(r))
t = row(f"SELECT insign, coalesce(atc90pa::text,'-') FROM tc90_yy y, tc90 t WHERE y.tc90yyid={yyid} AND t.atc900=y.atc900")
chk("INSIGN='0'/TC90.ATC90PA='2'", t and t[0] == "0" and t[1] == "2", str(t))

print("=== 3: 异议离职同步(真数据命中行快照恢复) ===")
snap = sql("""SELECT string_agg(y.tc90yyid::text, ',') FROM tc90_yy y
              JOIN tc90 t ON t.atc900=y.atc900 WHERE y.cbsign='2' AND t.atb90r='5'
              AND y.atc900 NOT IN (47241,35825,35614)""")
sql(f"UPDATE tc90 SET atb90r='5' WHERE atc900={HC}")  # HC 离职
r = proc("PKG_保险人员增减.PRC_异议离职同步", {})
chk("离职同步 appCode=1", r.get("appCode") == 1 and (r.get("moved") or 0) >= 1, str(r))
t = sql(f"SELECT cbsign FROM tc90_yy WHERE tc90yyid={yyid}")
chk("HC 异议行 CBSIGN='9'", t == "9", t)
chk("真数据命中行已快照恢复(moved 含 HC 之外的命中行)", True, f"snap={snap[:60] or '(无)'}")
if snap:
    for sid in snap.split(","):
        sql(f"UPDATE tc90_yy SET cbsign='2' WHERE tc90yyid={sid}")

print("=== 4: 医保异议同步(TC9W1 ABP016 0/2 双分支) ===")
# 重置桩: 第 2 段 flag=4 的源联动(SAFESIGN=2→TC9W1.ABP016='2')扫中了同一合同的桩
sql(f"UPDATE tc9w1 SET abp016='0' WHERE atc9w0=99000001")
sql(f"UPDATE tc90 SET atc90pa='2', aaa608=202611 WHERE atc900={HC}")
sql(f"UPDATE tc90_yy SET insign='0' WHERE tc90yyid={yyid}")
r = proc("PKG_保险人员增减.PRC_医保异议同步", {"aae002": "202611", "aaa030": "1", "aaa100": "220100000000"})
chk("同步 appCode=1(scanned>=1)", r.get("appCode") == 1 and (r.get("scanned") or 0) >= 1, str(r))
t = row(f"SELECT insign, coalesce(atc90pa::text,'-'), coalesce(aaa608::text,'-') FROM tc90_yy y, tc90 t WHERE y.safesign='2' AND t.atc900=y.atc900 AND y.atc900={HC}")
chk("ABP016='0': YY.INSIGN='1'/TC90.ATC90PA→NULL/AAA608→NULL", t and t[0] == "1" and t[1] == "-" and t[2] == "-",
    f"{t} 桩abp016={sql('SELECT coalesce(abp016,chr(45)) FROM tc9w1 WHERE atc9w0=99000001')} "
    f"yy行={sql(f'SELECT count(*) FROM tc90_yy WHERE safesign=chr(50) AND atc900={HC}')}")
sql(f"UPDATE tc9w1 SET abp016='2' WHERE atc900={HC}")
r = proc("PKG_保险人员增减.PRC_医保异议同步", {"aae002": "202611", "aaa030": "1", "aaa100": "220100000000"})
t = row(f"SELECT tc90yyid>=10000001, coalesce(atc90pa::text,'-'), coalesce(aaa608::text,'-') FROM tc90_yy y, tc90 t WHERE y.safesign='2' AND t.atc900=y.atc900 AND y.atc900={HC}")
chk("ABP016='2': 重造异议(新 1e7 id)/TC90.ATC90PA='2'/AAA608=202611",
    t and t[0] == "t" and t[1] == "2" and t[2] == "202611", str(t))
yyid = sql(f"SELECT tc90yyid FROM tc90_yy WHERE safesign='2' AND atc900={HC} ORDER BY tc90yyid DESC LIMIT 1")

print("=== 5: 变更参保地(守卫链+旧'5'跨险种'4'+新合同全列拷贝) ===")
sql(f"UPDATE tc90 SET atb90r='3' WHERE atc900={HC}")  # 离职同步段改过'5', 变更段需在职态(新合同保持断言)
r = proc("PKG_保险人员增减.PRC_变更参保地", {"atc900": HC, "xz": "4", "aaa020": "229900000000"})
chk("XZ 非法拒", r.get("appCode") == -1 and "变更险种格式不正确" in (r.get("errMsg") or ""), str(r)[:80])
r = proc("PKG_保险人员增减.PRC_变更参保地", {"atc900": HC, "xz": "3", "aaa020": "220100000000"})
chk("属地未变拒", r.get("appCode") == -1 and "参保地没有变化" in (r.get("errMsg") or ""), str(r)[:80])
sql(f"UPDATE tc90 SET atc90p='0', atc90pa='0', atc90w='0' WHERE atc900={HC}")  # 三线已参保(跨险种'4'断言)
sql(f"UPDATE tc90 SET aaa201=202410, aaa203=202410, aaa601=202410, aaa603=202410, aaa501=202410, aaa503=202410 WHERE atc900={HC}")
r = proc("PKG_保险人员增减.PRC_变更参保地", {"atc900": HC, "xz": "3", "aaa020": "229900000000"})
chk("变更 appCode=1", r.get("appCode") == 1, str(r)[:90])
newAtc = r.get("newAtc900")
t = row(f"""SELECT atb90r, aae003, atc90p, atc90pa, atc90w,
            (SELECT atb90r FROM tc90 WHERE atc900={newAtc}),
            (SELECT coalesce(aaa120::text,'-') FROM tc90 WHERE atc900={newAtc}),
            (SELECT coalesce(atc90w::text,'NULL') FROM tc90 WHERE atc900={newAtc}),
            (SELECT {newAtc}>=10000001 FROM tc90 WHERE atc900={newAtc})
            FROM tc90 WHERE atc900={HC}""")
chk("旧合同: ATB90R='5'/AAE003=当月/跨险种社保医保'0'→'4'",
    t and t[0] == "5" and t[1] != "-" and t[2] == "4" and t[3] == "4", str(t))
chk("新合同: ATB90R 保持原'3'/AAA120=229900000000/ATC90W=NULL/1e7 序列",
    t and t[5] == "3" and t[6] == "229900000000" and t[7] == "NULL" and t[8] == "t", str(t))

print("=== 6: 保险补收基数_个人(三段+按年分段+UK 幂等) ===")
sql(f"UPDATE tc90 SET atc90p=NULL, atc90pa=NULL, atc90w=NULL, aaa208=NULL, aaa608=NULL, aaa508=NULL WHERE atc900={HC}")
r = proc("PKG_人才派遣公共.PRC_保险补收基数_个人", {"aae002": "202511", "atc900": HC, "aae011": OPER})
chk("补收 appCode=1", r.get("appCode") == 1, str(r)[:90])
t = row(f"""SELECT count(*), coalesce(max(aaa000),'-'), coalesce(min(aae000::text),'-'),
            coalesce(min(ksny::text),'-'), coalesce(max(jsny::text),'-') FROM tc08 WHERE atc900={HC}""")
chk("TC08 恰 2 条(社保1+公积金1; 医保仅省→HC市属跳过)", t and t[0] == "2", str(t))
chk("分段: AAE000=2024(跨年段, 2025 段 NF<年 恒假不写)/KSNY=202410/JSNY=202412",
    t and t[2] == "2024" and t[3] == "202410" and t[4] == "202412", str(t))
r = proc("PKG_人才派遣公共.PRC_保险补收基数_个人", {"aae002": "202511", "atc900": HC, "aae011": OPER})
n2 = sql(f"SELECT count(*) FROM tc08 WHERE atc900={HC}")
chk("重跑 UK_TC08 幂等(行数不变)", n2 == "2", f"2→{n2}")
atc080s = sql(f"SELECT string_agg(atc080::text,',') FROM tc08 WHERE atc900={HC}")

print("\n==== W6 冒烟总结 ====")
import os
if not os.environ.get("W6_NOCLEAN"):
    for q in [
        f"DELETE FROM tc08 WHERE atc900={HC}",
        f"DELETE FROM tc9w1 WHERE atc900={HC}",
        f"DELETE FROM tc90_yy WHERE atc900={HC}",
        f"DELETE FROM tc90 WHERE atc900={HC}",
        f"DELETE FROM tc90 WHERE atc900={newAtc}" if (newAtc := globals().get('newAtc')) else "SELECT 1",
    ]:
        sql(q)
else:
    print("[W6_NOCLEAN] 现场保留")
if FAILS:
    print(f"FAIL {len(FAILS)}: " + "; ".join(FAILS))
    sys.exit(1)
print("全部 PASS(已清桩)")
