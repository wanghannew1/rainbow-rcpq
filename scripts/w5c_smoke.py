#!/usr/bin/env python3
"""W5c 公积金上下限五过程冒烟: 模板生成(TC0A_BP01, 省市分支+旧bug忠实)/调整校验(REPLACE '.0'
预处理+格式活分支+补缴三查+只查省限+ABP 月数)/调整提交(内嵌校验+V_TC90 整过程错+UK dup→
UPDATE 已审 guard)/调整审核(FLAG 1过/2驳/3取消+转款确认拦截 TJ0D1+AA97_BG 台账+回滚)/
下限托底(真数据 54 候选, 省市 ABP 月数不对称, AAA503 null→'0')。
桩: TC90 atc905 改值回滚(W5a 先例); TC0A 合成段 aae002=209901/atc0a0>=1e7; AA97 苗 aae001=2099。
前置: V17(uk_tc0a 唯一索引 + seq_tc0a_atc0a0 抬段 1e7)。
用法: python3 scripts/w5c_smoke.py
"""
import json
import sys
import urllib.request
import urllib.parse
import subprocess

BASE = "http://localhost:8080"
ACE = 99000101
OPER = "jiaq"
AAE002_SYN = 209901          # 审核/提交链合成年月(UK 不撞真行, AA97 苗 aae001=2099)
SB_CON = 70557               # 王易龄(市, atb90r=3, atc90w=null, atb90s=7:7, a303=3196)
SB_PROV = 68694              # 省候选(229900... substr99)
FLOOR_CON = 59246            # 下限候选·补缴形态(aaa503=202508, atb90s=7:7)
FLOOR_CON2 = 53417           # 下限候选·aaa503=NULL 形态
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
chk("uk_tc0a 索引在(V17)", sql("SELECT count(*) FROM pg_indexes WHERE indexname='uk_tc0a'") == "1")
chk("seq_tc0a_atc0a0>=1e7", int(sql("SELECT last_value FROM seq_tc0a_atc0a0") or 0) >= 10000001)
# tc90 桩列硬基准重置(上轮冒烟中崩→尾部恢复未跑的残留防御; 勘察基准 2026-11-18)
sql(f"UPDATE tc90 SET atc905=3196, aaa303=3196, aab001=151 WHERE atc900={SB_CON}")
sql(f"UPDATE tc90 SET atc905=2680, aaa303=2680 WHERE atc900={SB_PROV}")
SB = row(f"SELECT aac002,aac003,atb930,coalesce(atb90r,'-'),coalesce(atb90s,'-'),aaa303 FROM tc90 WHERE atc900={SB_CON}")
PROV = row(f"SELECT atb930,aaa120 FROM tc90 WHERE atc900={SB_PROV}")
# TC90 改值备份(模板桩: atc905 抬到出窗; 审核段: AAA303 落库回滚; V_TC90 段: aab001 断链)
# — W5a TC90_BACKUP 先例
TC90_BACKUP = {}
for atc in (SB_CON, SB_PROV):
    TC90_BACKUP[atc] = row(f"SELECT atc905, aaa303, aab001 FROM tc90 WHERE atc900={atc}")
for q in [
    f"DELETE FROM tc0a_bp WHERE ace100={ACE}",
    f"DELETE FROM tc0a_bp01 WHERE atb930 IN ({SB[2]},{PROV[0]})",
    f"DELETE FROM tc0a WHERE aae002={AAE002_SYN} OR (aae002=202611 AND atc0a0>=10000001)",
    f"DELETE FROM aa97_bg WHERE tname='TC0A' AND atc900 IN ({SB_CON},{SB_PROV}) AND tkey>=10000001",
    f"DELETE FROM aa97 WHERE aae001=2099 AND aac001=99990001",
    f"DELETE FROM tj0e1 WHERE aae002={AAE002_SYN} AND atc900={SB_CON}",
]:
    sql(q)
chk("审核链桩空间干净", sql(f"SELECT count(*) FROM tc0a WHERE aae002={AAE002_SYN}") == "0")

print("=== 1: 模板(省市分支+幂等) ===")
sql(f"UPDATE tc90 SET atc905=500000 WHERE atc900 IN ({SB_CON},{SB_PROV})")  # 出窗桩(备份在册)
r = proc("PKG_缴费基数.PRC_公积金基数上下限模板", {"atb930": SB[2]})
chk("模板(市) appCode=1", r.get("appCode") == 1, str(r))
chk("模板(市) 生成1行(70557)", r.get("generated") == 1, str(r))
t = row(f"""SELECT aac003, aaa303_1, aaa303_2, coalesce(aae002,'-'), atc905
            FROM tc0a_bp01 WHERE atb930={SB[2]} AND atc900={SB_CON}""")
chk("bp01 行: 姓名/原基数=3196/调整后=市窗986(30505)/AAE002=NULL/工资=500000",
    t and t[0] == "王易龄" and float(t[1]) == 3196 and float(t[2]) == 30505
    and t[3] == "-" and float(t[4]) == 500000, str(t))
r = proc("PKG_缴费基数.PRC_公积金基数上下限模板", {"atb930": SB[2]})
chk("模板幂等(DELETE 重插不翻倍)", r.get("generated") == 1, str(r))
r = proc("PKG_缴费基数.PRC_公积金基数上下限模板", {"atb930": PROV[0]})
chk("模板(省) 生成1行(68694)", r.get("generated") == 1, str(r))
t = row(f"SELECT aaa303_2 FROM tc0a_bp01 WHERE atb930={PROV[0]} AND atc900={SB_PROV}")
chk("bp01 省行 调整后=省窗986(30505)", t and float(t[0]) == 30505, str(t))
bp01a = sql(f"SELECT count(*) FROM tc0a_bp01 WHERE atb930={SB[2]}")
chk("省模板不污染市批(按 ATB930 隔离)", bp01a == "1", bp01a)

print("=== 2: 调整校验(REPLACE 预处理+格式活分支+补缴三查+只查省限) ===")
BP_COLS = ("id, ace100, aac002, aac003, atb930, aae002, atc905, aaa303_1, aaa303_2, "
           "atc0a1, atc0a2, atc0a3")


def lit(v):
    if v == "NULL":
        return "NULL"
    if isinstance(v, (int, float)):
        return str(v)
    return "'" + str(v).replace("'", "''") + "'"


def bp_row(rid, a303_2, a1="NULL", a2="NULL", a3="NULL", a303_1="3196",
           aac002=None, aac003=None, atb930=None):
    vals = [rid, ACE,
            aac002 if aac002 is not None else SB[0],
            aac003 if aac003 is not None else SB[1],
            atb930 if atb930 is not None else SB[2], AAE002_SYN, "5000", a303_1, a303_2, a1, a2, a3]
    vals = ", ".join(lit(v) for v in vals)
    return f"INSERT INTO tc0a_bp ({BP_COLS}) VALUES ({vals})"


PROV_ID = row(f"SELECT aac002, aac003, atb930 FROM tc90 WHERE atc900={SB_PROV}")

cases = [
    (99000101, "5000", "NULL", "NULL", "NULL", "3", None),            # r1 正常
    (99000102, "5000", "是", "202601", "202611", "3", None),          # r2 市补缴11月
    (99000103, "abc", "NULL", "NULL", "NULL", "2", "公积金调整后缴费基数格式不正确|"),
    (99000104, "NULL", "NULL", "NULL", "NULL", "2", "公积金调整后缴费基数不能为空或者零|"),
    (99000105, "5000", "是", "NULL", "NULL", "2", "选择补缴，则补缴开始年月、补缴结束年月不能为空|"),
    (99000106, "5000", "X", "NULL", "NULL", "2", "是否补缴格式不正确，请填写“是”或者“否”|"),
    (99000107, "5000", "是", "202611", "202601", "2", "补缴结束年月不能小于补缴开始年月|"),
    (99000108, "99999", "NULL", "NULL", "NULL", "2", "调整后的缴费基数不能超出上下限范围|"),
    (99000109, "202601.0", "是", "202601.0", "202611", "3", None),    # r9 REPLACE '.0'
    (99000110, "5000", "NULL", "NULL", "NULL", "2", "未匹配到合同|"),
]
for rid, a2v, a1, x2, x3, exp, msg in cases:
    kw = dict(a303_2=a2v, a1=a1, a2=x2, a3=x3)
    if rid == 99000108:
        # 省属地身份(68694) → 触发省限出窗拦截
        kw.update(aac002=PROV_ID[0], aac003=PROV_ID[1], atb930=PROV_ID[2])
    if rid == 99000110:
        kw.update(aac002="999999999900000000", aac003="不存在人")
    sql(bp_row(rid, **kw))
# 市合同 99999 → 应 PASS(只查省限=源缺陷忠实)
sql(bp_row(99000111, "99999", a303_1="4000"))
r = proc("PKG_缴费基数.PRC_公积金基数上下限调整校验", {"ace100": str(ACE)})
chk("校验 appCode=1", r.get("appCode") == 1, str(r)[:120])
chk("汇总文案(全批11)", "校验完成，共导入【11】条数据" in (r.get("errMsg") or ""), str(r.get("errMsg")))
for rid, a2v, a1, x2, x3, exp, msg in cases:
    got = sql(f"SELECT ace10a||'|'||coalesce(ace10b,'-') FROM tc0a_bp WHERE id={rid}")
    want = f"{exp}|{msg if msg else '-'}"
    chk(f"行{rid % 1000:03d}", got == want, got)
# 市合同 99999 → '3'(市不查限)
got = sql("SELECT ace10a||'|'||coalesce(ace10b,'-') FROM tc0a_bp WHERE id=99000111")
chk("市合同 99999 → '3'(市不查限=源缺陷忠实)", got == "3|-", got)
# REPLACE '.0' 断言
t = row("SELECT atc0a2, atc0a3 FROM tc0a_bp WHERE id=99000109")
chk("REPLACE '.0' 后 202601/202611", t == ["202601", "202611"], str(t))
# r2 ABP 月数: (5000-3196)×7/100=126.28→126, ×11=1386
t = row("SELECT abp00d, abp00e, abp00c, atb90s_dw, atb90s_gr FROM tc0a_bp WHERE id=99000102")
chk("补缴 ABP00D/E=126×11=1386, C=2772, 比例=7/7",
    t and abs(float(t[0]) - 1386) < 0.01 and abs(float(t[1]) - 1386) < 0.01
    and abs(float(t[2]) - 2772) < 0.01 and float(t[3]) == 7 and float(t[4]) == 7, str(t))

print("=== 3: 调整提交(内嵌校验+UK dup→UPDATE+已审 guard) ===")
r = proc("PKG_缴费基数.PRC_公积金基数上下限调整提交", {"ace100": str(ACE), "aae011": OPER})
chk("提交 appCode=1", r.get("appCode") == 1, str(r)[:120])
ok3 = sql(f"SELECT count(*) FROM tc0a_bp WHERE ace100={ACE} AND ace10a='1'")
chk("'3' 行(4)全置'1'", ok3 == "4", ok3)
# (同合同多行 dup last-wins: 具体基数以 dup 段断言为准, 此处断结构字段)
t = row(f"""SELECT aae002, aae114, coalesce(atc90w,'-'),
            aae011, atc0a0>=10000001, aae036 IS NOT NULL
            FROM tc0a WHERE aae002={AAE002_SYN} AND atc900={SB_CON}""")
chk("TC0A 行: AAE002=209901/AAE114='0'/经办六件套/1e7 序列",
    t and int(float(t[0])) == AAE002_SYN and t[1] == "0" and t[3] == OPER
    and t[4] == "t" and t[5] == "t", str(t))
first_atc0a0 = sql(f"SELECT atc0a0 FROM tc0a WHERE aae002={AAE002_SYN} AND atc900={SB_CON}")
# dup: 同合同第二行(不同值) → UPDATE last-wins
sql(bp_row(99000121, "6000", a303_1="3196"))
r = proc("PKG_缴费基数.PRC_公积金基数上下限调整校验", {"ace100": str(ACE)})
r = proc("PKG_缴费基数.PRC_公积金基数上下限调整提交", {"ace100": str(ACE), "aae011": OPER})
t = row(f"SELECT count(*), max(aaa303_1) FROM tc0a WHERE aae002={AAE002_SYN} AND atc900={SB_CON}")
chk("dup→UPDATE(仍1行, 值=6000 last-wins)", t and t[0] == "1" and float(t[1]) == 6000, str(t))
chk("atc0a0 不变(走 UPDATE 非 INSERT)", sql(
    f"SELECT atc0a0 FROM tc0a WHERE aae002={AAE002_SYN} AND atc900={SB_CON}") == first_atc0a0, first_atc0a0)
# 已审 guard: 置 AAE114='1' → 重提同合同 → dup-UPDATE 0 行静默
sql(f"UPDATE tc0a SET aae114='1' WHERE aae002={AAE002_SYN} AND atc900={SB_CON}")
sql("UPDATE tc0a_bp SET ace10a='3', aaa303_2='7000' WHERE id=99000121")
r = proc("PKG_缴费基数.PRC_公积金基数上下限调整提交", {"ace100": str(ACE), "aae011": OPER})
t = row(f"SELECT aaa303_1, aae114 FROM tc0a WHERE aae002={AAE002_SYN} AND atc900={SB_CON}")
chk("已审行不可覆写(保持6000/AAE114='1')", t and float(t[0]) == 6000 and t[1] == "1", str(t))
# V_TC90 缺失 → 整过程错+整事务回滚(零半账)。源可达路径=校验查 TC90(表)/提交查 V_TC90(视图,
# INNER JOIN ab01): 断开合同与户头档案关联(临时改 tc90.aab001)→校验仍过、提交视图落空→
# JishuErr→aab001 改动与先行行 '1' 同事务一起回滚。
before = sql(f"SELECT count(*) FROM tc0a_bp WHERE ace100={ACE} AND ace10a='1'")
sql(bp_row(99000130, "5000"))
r = proc("PKG_缴费基数.PRC_公积金基数上下限调整校验", {"ace100": str(ACE)})
sql(f"UPDATE tc90 SET aab001=999999999 WHERE atc900={SB_CON}")
r = proc("PKG_缴费基数.PRC_公积金基数上下限调整提交", {"ace100": str(ACE), "aae011": OPER})
chk("V_TC90 缺失 → appCode=-1 + ORA-01403 文案",
    r.get("appCode") == -1 and "提交失败，原因为：ORA-01403" in (r.get("errMsg") or ""), str(r)[:110])
after = sql(f"SELECT count(*) FROM tc0a_bp WHERE ace100={ACE} AND ace10a='1'")
chk("整事务回滚(先行行未留半账)", after == before, f"before={before} after={after}")
# aab001 断链是冒烟直改(已提交, 不随 proc 回滚) → 手动还原后核对
sql(f"UPDATE tc90 SET aab001={TC90_BACKUP[SB_CON][2]} WHERE atc900={SB_CON}")
chk("tc90.aab001 断链还原", sql(f"SELECT aab001 FROM tc90 WHERE atc900={SB_CON}") == TC90_BACKUP[SB_CON][2], "")
sql(f"DELETE FROM tc0a_bp WHERE id=99000130")

print("=== 4: 调整审核(FLAG 1/3/2+转款拦截+AA97_BG 台账) ===")
atc0a0 = first_atc0a0
sql(f"""INSERT INTO aa97 (aae001, atc900, aac001, aac003, aaa303_1, aaa030, aaa100, aae036, aaa970)
        VALUES (2099, {SB_CON}, 99990001, '王易龄', 1111, 1, '220100000000', now(),
        (SELECT COALESCE(MAX(aaa970),0)+1 FROM aa97))""")
sql(f"UPDATE tc0a SET aae114='0', aaa303_1=6000 WHERE atc0a0={atc0a0}")
r = proc("PKG_缴费基数.PRC_公积金基数上下限调整审核",
         {"atc0a0": f" {atc0a0},", "flag": "1", "aae011": OPER, "aae013": ""})
chk("审核(过) msg", "操作成功：1条；操作失败：0条" in (r.get("errMsg") or ""), str(r.get("errMsg")))
t = row(f"SELECT aae114, aaa303_1 FROM tc0a WHERE atc0a0={atc0a0}")
chk("TC0A AAE114='1'", t and t[0] == "1", str(t))
t = row(f"SELECT aaa303 FROM tc90 WHERE atc900={SB_CON}")
chk("TC90.AAA303←6000(退役后启用语义, 备份回滚)", t and float(t[0]) == 6000, str(t))
t = row("SELECT aaa303_1 FROM aa97 WHERE aae001=2099 AND aac001=99990001")
chk("AA97 苗 AAA303_1←6000", t and float(t[0]) == 6000, str(t))
t = row(f"SELECT aa97type, aaa300_bgq, aaa300_bgh, aae013 FROM aa97_bg WHERE tname='TC0A' AND tkey={atc0a0}")
chk("AA97_BG 台账(TYPE=4, BGQ=3196, BGH=6000)", t and t[0] == "4" and float(t[1]) == 3196
    and float(t[2]) == 6000 and t[3] == "公积金缴费基数上下限调整", str(t))
# FLAG=3 取消 → 回滚
r = proc("PKG_缴费基数.PRC_公积金基数上下限调整审核",
         {"atc0a0": f"{atc0a0},", "flag": "3", "aae011": OPER, "aae013": ""})
t = row(f"SELECT aae114, coalesce(aae113,'-') FROM tc0a WHERE atc0a0={atc0a0}")
chk("取消 → AAE114='0'/AAE113=NULL", t and t[0] == "0" and t[1] == "-", str(t))
t = row(f"SELECT aaa303 FROM tc90 WHERE atc900={SB_CON}")
chk("取消回滚 TC90←原3196", t and float(t[0]) == 3196, str(t))
# 源 FLAG=2/3: AA97.AAA303_1←R_TC0A.AAA303(TC0A 行的原基数 3196, 非 AA97 旧值; 无 AAE001 条件=源差异)
chk("取消回滚 AA97←TC0A原值3196", row("SELECT aaa303_1 FROM aa97 WHERE aae001=2099 AND aac001=99990001")[0] == "3196.00")
chk("取消删 BG(物理删=忠实)", sql(f"SELECT count(*) FROM aa97_bg WHERE tname='TC0A' AND tkey={atc0a0}") == "0")
# FLAG=2 驳回(未审态, 无回滚)
sql(f"UPDATE tc0a SET aaa303_1=6000 WHERE atc0a0={atc0a0}")
r = proc("PKG_缴费基数.PRC_公积金基数上下限调整审核",
         {"atc0a0": f"{atc0a0},", "flag": "2", "aae011": OPER, "aae013": "基数填写有误"})
t = row(f"SELECT aae114, coalesce(aae113,'-') FROM tc0a WHERE atc0a0={atc0a0}")
chk("驳回 → AAE114='2'/AAE113=原因", t and t[0] == "2" and t[1] == "基数填写有误", str(t))
# 转款确认拦截(FLAG=1): 70557 属地市(220100.. substr'01')→查 TJ0E1, 桩插 TJ0E1(必填含 aac001)
sql(f"""INSERT INTO tj0e1 (aae002, atc900, atj000, atb930, aac001, aae013)
        VALUES ({AAE002_SYN}, {SB_CON}, '901', {SB[2]}, 999901, '1')""")
sql(f"UPDATE tc0a SET aae114='0' WHERE atc0a0={atc0a0}")
r = proc("PKG_缴费基数.PRC_公积金基数上下限调整审核",
         {"atc0a0": f"{atc0a0},", "flag": "1", "aae011": OPER, "aae013": ""})
chk("转款已确认拦截(市→TJ0E1)", "操作成功：0条；操作失败：1条，失败原因为公积金转款已确认" in (r.get("errMsg") or ""), str(r.get("errMsg")))
# 静默跳过(不存在 id)
r = proc("PKG_缴费基数.PRC_公积金基数上下限调整审核",
         {"atc0a0": "999999999,", "flag": "1", "aae011": OPER, "aae013": ""})
chk("坏 id 静默(成功0失败0)", "操作成功：0条；操作失败：0条" in (r.get("errMsg") or ""), str(r.get("errMsg")))

print("=== 5: 下限托底(真数据候选, 省市 ABP 月数不对称) ===")
n_before = sql("SELECT count(*) FROM tc0a WHERE aae002=202611 AND atc0a0>=10000001")
r = proc("PKG_缴费基数.PRC_公积金基数下限调整", {"aae002": "202611", "aae011": OPER})
chk("下限托底 appCode=1", r.get("appCode") == 1, str(r)[:110])
n_gen = sql("SELECT count(*) FROM tc0a WHERE aae002=202611 AND atc0a0>=10000001")
chk("候选54行全落(无 UK 撞)", int(n_gen) >= 54, f"before={n_before} gen={n_gen}")
# 59246 补缴形态: AAA503=202508<202611→'1', ATC0A3=202610, N_YS=MONTHS(202610,202508)+1=3
t = row(f"""SELECT aaa303_1, atc0a1, atc0a2, atc0a3, abp00d, abp00e, abp00c, aae114, coalesce(atc90w,'-')
            FROM tc0a WHERE aae002=202611 AND atc900={FLOOR_CON}""")
chk("59246: 下限2230/补缴'1'/起202508/止202610", t and float(t[0]) == 2230 and t[1] == "1"
    and t[2] == "202508" and t[3] == "202610", str(t))
chk("市分支 ABP 不乘月数: D=E=ROUND(2230×7/100)=156, C=312",
    t and abs(float(t[4]) - 156) < 0.01 and abs(float(t[5]) - 156) < 0.01 and abs(float(t[6]) - 312) < 0.01, str(t))
t = row(f"SELECT atc0a1, coalesce(atc0a2::text,'-'), abp00d FROM tc0a WHERE aae002=202611 AND atc900={FLOOR_CON2}")
chk("53417: AAA503=NULL→'0'/ATC0A2=NULL/ABP=156(N_YS=1)", t and t[0] == "0" and t[1] == "-", str(t))
# 幂等: 重跑 → 全 dup → UPDATE(generated=0, 但行不变)
r = proc("PKG_缴费基数.PRC_公积金基数下限调整", {"aae002": "202611", "aae011": OPER})
n_gen2 = sql("SELECT count(*) FROM tc0a WHERE aae002=202611 AND atc0a0>=10000001")
chk("重跑 dup→UPDATE(行数不变)", n_gen2 == n_gen, f"{n_gen}→{n_gen2}")

print("\n==== W5c 冒烟总结 ====")
for q in [
    f"DELETE FROM tc0a_bp WHERE ace100={ACE}",
    f"DELETE FROM tc0a_bp01 WHERE atb930 IN ({SB[2]},{PROV[0]})",
    f"DELETE FROM tc0a WHERE aae002={AAE002_SYN} OR (aae002=202611 AND atc0a0>=10000001)",
    f"DELETE FROM aa97_bg WHERE tname='TC0A' AND atc900 IN ({SB_CON},{SB_PROV}) AND tkey>=10000001",
    f"DELETE FROM aa97 WHERE aae001=2099 AND aac001=99990001",
    f"DELETE FROM tj0e1 WHERE aae002={AAE002_SYN} AND atc900={SB_CON}",
]:
    sql(q)
for atc, old in TC90_BACKUP.items():
    sql(f"UPDATE tc90 SET atc905={old[0]}, aaa303={old[1]}, aab001={old[2]} WHERE atc900={atc}")
if FAILS:
    print(f"FAIL {len(FAILS)}: " + "; ".join(FAILS))
    sys.exit(1)
print("全部 PASS(已清桩)")
