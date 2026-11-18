#!/usr/bin/env python3
"""W5b 基数导入校验/提交冒烟: AA97_BP 批次流全链。
校验: 富化写回(合同/户头/调整前基数)/社保空/市公积金必须整数/比例填写不正确/比例必须填写/
      市医保空; TO_NUMBER 致命→整批回滚零半账。
提交: AA97 INSERT 落库(属地择一 302/303/305, AAE214 全'0', 经办六件套, 1e7 序列)/
      同批双生行 dup→UPDATE last-wins/已确认线 AAE214=1 不可覆写 guard/无合同失败行。
前置: V16(uk_aa97 唯一索引 + seq_aa97_aaa970 抬段 1e7)。
用法: python3 scripts/w5b_smoke.py
"""
import json
import sys
import urllib.request
import urllib.parse
import subprocess

BASE = "http://localhost:8080"
YEAR = "2026"                     # 资格窗口 202601; aa97 落 2026(张天祺该年无行→INSERT 通路干净)
ACE1, ACE2, ACE3 = 99000001, 99000002, 99000003
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


def get(path):
    return json.load(urllib.request.urlopen(BASE + path, timeout=60))


def chk(name, cond, detail=""):
    print(f"  [{'PASS' if cond else 'FAIL'}] {name} {detail}")
    if not cond:
        FAILS.append(name)


# ---- 人物/合同(fixture, 勘察 2026-11-18; 校验/提交不触碰 tc90) ----
# who=(aac002, aac003, atb930); ZTQ 另带 atc900 打头
ZTQ = sql("SELECT aac002,aac003,atb930 FROM tc90 WHERE atc900=70558").split("|")   # 张天祺 双生行(提交通路)
ZTQ_ATC = "70558"
LX = sql("SELECT aac002,aac003,atb930 FROM tc90 WHERE atc900=33512").split("|")           # 芦鑫 社保空
TMS = sql("SELECT aac002,aac003,atb930 FROM tc90 WHERE atc900=12720").split("|")          # 唐铭声 公积金×3
WXD = sql("SELECT aac002,aac003,atb930 FROM tc90 WHERE atc900=45440").split("|")          # 王欣迪 医保市空

print("=== 0: 前置+清场 ===")
chk("uk_aa97 索引在(V16)", sql("SELECT count(*) FROM pg_indexes WHERE indexname='uk_aa97'") == "1")
chk("seq_aa97_aaa970>=1e7", int(sql("SELECT last_value FROM seq_aa97_aaa970") or 0) >= 10000001)
ZTQ_AAC001 = sql(f"SELECT DISTINCT aac001 FROM tc90 WHERE atc900={ZTQ_ATC}")
for q in [
    f"DELETE FROM aa97_bp WHERE ace100 IN ({ACE1},{ACE2},{ACE3},99000009)",
    f"DELETE FROM aa97 WHERE aae001={YEAR} AND aac001={ZTQ_AAC001} AND aaa970>=10000001",
]:
    sql(q)
chk("张天祺 aa97 无存量(INSERT 通路干净)",
    sql(f"SELECT count(*) FROM aa97 WHERE aae001={YEAR} AND aac001={ZTQ_AAC001}") == "0")

BP_COLS = ("id, ace100, aac002, aac003, atb930, aae001, aaa301_1, aaa302_1, aaa302_2, "
           "aaa303_1, aaa303_2, aaa305_1, aaa305_2, atb90s_dw, atb90s_gr, ace10a")


def lit(v):
    """SQL 字面量: None→NULL, 数字→原样, 字符串→引号"""
    if v is None:
        return "NULL"
    if isinstance(v, (int, float)):
        return str(v)
    return "'" + str(v).replace("'", "''") + "'"


def bp_row(rid, who, a301, a302_1=None, a302_2=None, a303_1=None, a303_2=None,
           a305_1=None, a305_2=None, dw=None, gr=None, ace10a=None, atc900=None):
    aac002, aac003, atb930 = who[0], who[1], who[2]
    batch = ACE1 if rid < 990000010 else (ACE2 if rid < 990000020 else ACE3)
    vals = ", ".join(lit(v) for v in [rid, batch, aac002, aac003, atb930, int(YEAR), a301,
                                      a302_1, a302_2, a303_1, a303_2, a305_1, a305_2,
                                      dw, gr, atc900, ace10a])
    return (f"INSERT INTO aa97_bp ({BP_COLS.replace('ace10a', 'atc900, ace10a')}) VALUES ({vals})")


print("=== 1: 校验(富化写回+四险种失败分支) ===")
TWIN = dict(a302_1="1111", a302_2="2222", a303_1="5555", a303_2="6666",
            a305_1="3333", a305_2="4444", dw="12.0", gr="7.0")
sql(bp_row(990000001, ZTQ, "6001", **TWIN))
sql(bp_row(990000002, ZTQ, "6002", **TWIN))
sql(bp_row(990000003, LX, None))                                               # 社保空
sql(bp_row(990000004, TMS, "5000", a302_1="5000", a302_2="5000",
           a303_1="5555", a303_2="5000.5", a305_1="5000", a305_2="5000", dw="12.0", gr="7.0"))  # 市公积金整数
sql(bp_row(990000005, TMS, "5000", a302_1="5000", a302_2="5000",
           a303_1="5555", a303_2="5000", a305_1="5000", a305_2="5000", dw="5.0", gr="7.0"))     # 单位比例错
sql(bp_row(990000006, TMS, "5000", a302_1="5000", a302_2="5000",
           a303_1="5555", a303_2="5000", a305_1="5000", a305_2="5000"))                         # 比例缺失
sql(bp_row(990000007, WXD, "5000", a302_1="5000",                              # 市医保空(302_2 NULL)
           a303_1="5000", a303_2="5000", a305_1="5000", a305_2="5000", dw="12.0", gr="7.0"))
r = proc("PKG_缴费基数.PRC_基数导入校验", {"ace100": str(ACE1), "aae001": YEAR})
chk("校验 appCode=1", r.get("appCode") == 1, str(r))
chk("rows=7 passed=2 failed=5", r.get("rows") == 7 and r.get("passed") == 2 and r.get("failed") == 5, str(r))
exp_atc = sql(f"SELECT max(atc900) FROM tc90 WHERE atc90g='1' AND aac002='{ZTQ[0]}' AND aac003='{ZTQ[1]}' AND atb930={ZTQ[2]}")
t = sql(f"SELECT ace10a, coalesce(ace10b,'-'), coalesce(atc900::text,'-'), coalesce(aaa301::text,'-'), coalesce(aaa030::text,'-'), aaa302, aaa302_1, aaa302_2 FROM aa97_bp WHERE id=990000001").split("|")
chk("双生行1 置'3'且富化(atc900=合同MAX/调整前=tc905/户头落)", t[0] == "3" and t[2] == exp_atc, str(t))
chk("富化 aaa301(调整前)=TC90.ATC905", abs(float(t[3]) - float(sql(f"SELECT atc905 FROM tc90 WHERE atc900={ZTQ_ATC}") or -1)) < 0.01, t[3])
chk("双生行2 置'3'(同合同)", sql("SELECT ace10a FROM aa97_bp WHERE id=990000002") == "3")
chk("社保空→'2'不能为空", sql("SELECT ace10a||'|'||ace10b FROM aa97_bp WHERE id=990000003") == "2|【社保缴费基数】不能为空")
chk("市公积金非整数→'2'", sql("SELECT ace10b FROM aa97_bp WHERE id=990000004") == "【市公积金缴费基数】必须为整数")
chk("单位比例5.0→'2'填写不正确", sql("SELECT ace10b FROM aa97_bp WHERE id=990000005") == "【公积金单位缴存比列】填写不正确")
chk("比例缺失→'2'必须填写(源无RAISE缺陷忠实)", sql("SELECT ace10b FROM aa97_bp WHERE id=990000006") == "【公积金缴存比列】必须填写")
chk("医保参保地市 302_2 空→'2'", sql("SELECT ace10b FROM aa97_bp WHERE id=990000007") == "医保参保地为市，【市医保缴费基数】不能为空")

print("=== 2: 提交(AA97 INSERT 落库+属地择一+序列1e7) ===")
r = proc("PKG_缴费基数.PRC_基数导入提交", {"ace100": str(ACE1), "aae011": OPER})
chk("提交 appCode=1 committed=2", r.get("appCode") == 1 and r.get("committed") == 2 and r.get("failed") == 0, str(r))
chk("双生行置'1'", sql("SELECT count(*) FROM aa97_bp WHERE id IN (990000001,990000002) AND ace10a='1'") == "2")
a97 = sql(f"SELECT count(*) FROM aa97 WHERE aae001={YEAR} AND aac001={ZTQ_AAC001}")
chk("AA97 恰一行(同批双生 dup→UPDATE)", a97 == "1", a97)
row = sql(f"""SELECT aaa301_1, aaa302_1, aaa303_1, aaa305_1, aae214_1||aae214_2||aae214_3||aae214_4,
             aae100, coalesce(aae011,'-'), coalesce(aae017,'-'), aaa970>=10000001, coalesce(baz001,0)>0,
             coalesce(aac003,'-'), atc900={ZTQ_ATC}, aae036 IS NOT NULL
             FROM aa97 WHERE aae001={YEAR} AND aac001={ZTQ_AAC001}""").split("|")
# PG numeric 列回读带 scale(6002.00), 数值比较
chk("申报基数 last-wins=6002(双生行2)", abs(float(row[0]) - 6002) < 0.01, str(row))
chk("属地择一 市医保 302←302_2=2222", abs(float(row[1]) - 2222) < 0.01, str(row))
chk("属地择一 市公积金 303←303_2=6666", abs(float(row[2]) - 6666) < 0.01, str(row))
chk("属地择一 市工伤 305←305_2=4444", abs(float(row[3]) - 4444) < 0.01, str(row))
chk("AAE214_1~4 全'0'/AAE100='0'", row[4] == "0000" and row[5] == "0", str(row))
chk("经办六件套(jiaq/4101/AAE036)", row[6] == OPER and row[7] == "4101" and row[11] == "t", str(row))
chk("AAA970 从 1e7 起+BAZ001 发号", row[8] == "t" and row[9] == "t", str(row))
chk("合同/姓名落库", row[10] == "张天祺", str(row))

print("=== 3: 已确认线 guard(AAE214=1 不可覆写) ===")
a970 = sql(f"SELECT aaa970 FROM aa97 WHERE aae001={YEAR} AND aac001={ZTQ_AAC001}")
sql(f"UPDATE aa97 SET aae214_1='1', aae214_3='1' WHERE aaa970={a970}")
sql(f"UPDATE aa97_bp SET aaa301_1='7777', atb90s_gr='8.0' WHERE id=990000002")
r = proc("PKG_缴费基数.PRC_基数导入提交", {"ace100": str(ACE1), "aae011": OPER})
chk("重提 committed=2(幂等)", r.get("appCode") == 1 and r.get("committed") == 2, str(r))
g = sql(f"SELECT aaa301_1, atb90s_gr, coalesce(aae013,'-') FROM aa97 WHERE aaa970={a970}").split("|")
chk("社保线已确认 301_1 不覆写(保持6002)", abs(float(g[0]) - 6002) < 0.01, str(g))
chk("公积金线已确认 比例不覆写(保持7)", abs(float(g[1]) - 7) < 0.01, str(g))
chk("AAE013=NULL(源赋 NULL)", g[2] == "-", str(g))
sql(f"UPDATE aa97 SET aae214_1='0', aae214_3='0' WHERE aaa970={a970}")

print("=== 4: 提交失败行(无合同) ===")
sql(f"""INSERT INTO aa97_bp (id, ace100, atc900, aac001, aae001, ace10a)
        VALUES (990000009, {ACE3}, 999999999, 999999, {YEAR}, '3')""")
r = proc("PKG_缴费基数.PRC_基数导入提交", {"ace100": str(ACE3), "aae011": OPER})
chk("提交 failed=1", r.get("appCode") == 1 and r.get("failed") == 1, str(r))
chk("置'0'+原因", sql("SELECT ace10a||'|'||ace10b FROM aa97_bp WHERE id=990000009") == "0|未查询到合同信息")

print("=== 5: TO_NUMBER 致命→整批报错+整事务回滚零半账 ===")
sql(bp_row(990000011, ZTQ, "6003", **TWIN))      # 本会过
sql(bp_row(990000012, ZTQ, "abc"))               # 社保基数格式错→TO_NUMBER 致命
r = proc("PKG_缴费基数.PRC_基数导入校验", {"ace100": str(ACE2), "aae001": YEAR})
chk("校验致命 appCode=-1+忠实文案", r.get("appCode") == -1 and "派遣员工工资基数导入校验出错" in (r.get("errMsg") or ""), str(r)[:120])
chk("整事务回滚(先行行未留半账)", sql(
    f"SELECT count(*) FROM aa97_bp WHERE ace100={ACE2} AND ace10a IS NOT NULL") == "0")

print("\n==== W5b 冒烟总结 ====")
for q in [
    f"DELETE FROM aa97_bp WHERE ace100 IN ({ACE1},{ACE2},{ACE3},99000009)",
    f"DELETE FROM aa97 WHERE aae001={YEAR} AND aac001={ZTQ_AAC001} AND aaa970>=10000001",
]:
    sql(q)
if FAILS:
    print(f"FAIL {len(FAILS)}: " + "; ".join(FAILS))
    sys.exit(1)
print("全部 PASS(已清桩)")
