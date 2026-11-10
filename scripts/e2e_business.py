#!/usr/bin/env python3
"""端到端业务验证: 一个派遣员工从签约→审核→险金→工资→核对→到账→发票→续签→终止全链。

前置: app 于 localhost:8080; PG rainbow_rcpq; jiaq 经办人。
每一节点走新系统 API(/proc 过程网关 + /interactions 交互网关), 断言跨环节数据传播。
用法: python3 scripts/e2e_business.py
"""
import json
import urllib.request
import urllib.parse
import subprocess
import datetime

BASE = "http://localhost:8080"
OPER = {"aae011": "jiaq", "aae017": "4101", "aae019": "贾悫",
        "aae020": "吉林省彩虹人才开发咨询服务有限公司", "aae022": "410100000000"}
YM = "202611"
P = subprocess.run(["psql", "-h", "localhost", "-U", "rcpq", "-d", "rainbow_rcpq", "-tAc"],
                   capture_output=True, text=True)


def sql(q: str):
    r = subprocess.run(["psql", "-h", "localhost", "-U", "rcpq", "-d", "rainbow_rcpq", "-tAc", q],
                       capture_output=True, text=True, env={"PGPASSWORD": "rcpq_dev", "PATH": "/usr/bin:/bin"})
    return (r.stdout or "").strip()


def post(path: str, body: dict) -> dict:
    req = urllib.request.Request(BASE + path, data=json.dumps(body).encode(),
                                headers={"Content-Type": "application/json"})
    return json.load(urllib.request.urlopen(req, timeout=120))


def proc(name: str, body: dict) -> dict:
    return post("/proc/" + urllib.parse.quote(name), body)


def step(n, title):
    print(f"\n=== 步骤 {n}: {title} ===")


results = []


def check(label, cond, detail=""):
    print(("  PASS " if cond else "  FAIL ") + label + (f"  [{detail}]" if detail else ""))
    results.append((label, bool(cond)))


# 合法身份证生成
W = [7, 9, 10, 5, 8, 4, 2, 1, 6, 3, 7, 9, 10, 5, 8, 4, 2, 1]
CK = "10X98765432"

# 成年固定生日 + 顺序号防重(3 位, 已存在则递增)
def gen_id(seq):
    b = "22010219961105" + f"{seq:03d}"
    s = sum(int(b[i]) * W[i] for i in range(17))
    return b + CK[s % 11]

_seq = 1
ID18 = gen_id(_seq)
while sql(f"SELECT COUNT(*) FROM ac01 WHERE aac002='{ID18}'") not in ("0", ""):
    _seq += 1
    ID18 = gen_id(_seq)
NAME = "端到端验证员"

step(0, "准备: 合法测试身份证 + 批次号")
print("  身份证:", ID18, " (按当天日期生成, 重跑不冲突)")
ace = sql("SELECT nextval('seq_ce10_ace100')")
print("  批次:", ace)

step(1, "业务员·新签合同批量导入(员工导入校验)")
sql(f"""INSERT INTO tc90_bp (id, ace100, aac002, aac003, aac005, aac011, aac012, aae00p, aae005, atc902,
  atc90ax, aac009, aac013, atc909, atc90b, atc90a, atc90c, atc90d, atc905, atc907, atc908, atc90ah,
  aae002, atb90c, atb90a, atb90b, atc90zb, aaa401, aaa100, aaa203, aaa301, atc90p,
  aab004_1, aae00a, atb930, atb900, aab001, aab004)
VALUES (nextval('seq_tc90_id'), {ace}, '{ID18}', '{NAME}', '01-汉族', '11-研究生', '91-其他人员',
  '1-派遣', '13000000000', '长春市南关区测试路1号', '2035-12-31', '10-城镇失业', '1-城镇新增', '1-全职',
  '2026-11-01', '1-固定期限', '2026-11-01', '2028-11-01', '6800', '1-标准工时', '端到端测试岗', '0-否',
  '{YM}', '0-否', '150', '0', '1-是', '1-是', '220100000000-吉林省长春市', '{YM}', '6800', '0-未参保',
  '端到端部', 'E2E001', 37312, 58332, 151, '（减员）吉林大学国际交流服务中心')""")
r = proc("PKG_派遣批量导入.PRC_员工导入校验", {"ace100": ace, "aae011": "jiaq"})
check("校验 appCode=1", r.get("appCode") == 1, str(r.get("errMsg"))[:50])

step(2, "业务员·新签合同提交(生成 AC01/TC90/TB91/TC90_LIST)")
r = proc("PKG_派遣批量导入.PRC_员工导入提交", {"ace100": ace, "aae011": "jiaq"})
check("提交 appCode=1", r.get("appCode") == 1, str(r.get("errMsg"))[:50])
st = sql(f"""SELECT 'ac01='||COALESCE((SELECT COUNT(*)::text FROM ac01 WHERE aac002='{ID18}'),'0')
  ||' tc90='||COALESCE((SELECT atc900::text FROM tc90 WHERE aac002='{ID18}' LIMIT 1),'-')
  ||' atb90r='||COALESCE((SELECT atb90r FROM tc90 WHERE aac002='{ID18}' LIMIT 1),'-')
  ||' tb91='||COALESCE((SELECT atb911||'/'||atb912 FROM tb91 WHERE atb91z=(SELECT MAX(atc900) FROM tc90 WHERE aac002='{ID18}')),'-')""")
print("  ", st)
atc900 = sql(f"SELECT atc900 FROM tc90 WHERE aac002='{ID18}' LIMIT 1")
check("AC01 已生成", "ac01=1" in st)
check("TC90 待审(atb90r=2)", "atb90r=2" in st)
check("TB91 审核流水(20/2)", "20/2" in st)

step(3, "审核岗·新签审核通过(交互网关 doXinqCommit)")
# UniEAP 行编辑保存模型: 审核岗在页面把状态改好(atb90r=3 / atb912=3)后整行提交
AUD = "吉林省彩虹人才开发咨询服务有限公司"
tc90row = sql("SELECT json_build_object('atc900', atc900, 'aac001', aac001, 'atb90r', '3', "
  f"'bae011', 'jiaq', 'bae017', '4101', 'bae019', '贾悫', 'bae020', '{AUD}', 'bae022', '410100000000') "
  f"FROM tc90 WHERE atc900={atc900}")
tb91row = sql("SELECT json_build_object('atb910', atb910, 'atb91z', atb91z, 'atb911', atb911, 'atb912', '3', "
  f"'bae011', 'jiaq', 'bae017', '4101', 'bae019', '贾悫', 'bae020', '{AUD}', 'bae022', '410100000000', "
  "'bae036', to_char(now(), 'YYYY-MM-DD\"T\"HH24:MI:SS')) "
  f"FROM tb91 WHERE atb91z={atc900} AND atb911='20' ORDER BY atb910 DESC LIMIT 1")
r = post("/interaction/" + urllib.parse.quote("REQ-tdbf__dialog_xinqcl-commit"),
         {"tc90": json.loads(tc90row), "tb91": json.loads(tb91row)})
st = sql(f"SELECT atb90r FROM tc90 WHERE atc900={atc900}")
tb = sql(f"SELECT atb912 FROM tb91 WHERE atb91z={atc900} AND atb911='20' ORDER BY atb910 DESC LIMIT 1")
check("审核通过 atb90r=3(生效)", st == "3", "atb90r=" + st)
check("TB91 已审(3)", tb == "3", "atb912=" + tb)

step(4, "险金计算(当月)")
r = proc("PKG_人才派遣公共.PRC_计算险金", {"atc900": atc900, "aae002": YM})
check("计算险金 appCode=1", r.get("appCode") == 1, f"atc9eh={r.get('atc9eh')}")

step(5, "工资岗·当月工资行生成(TC93 编制)")
tc90r = sql(f"SELECT json_build_object('atb900', atb900, 'atb930', atb930, 'aab001', aab001, 'atc900', atc900) FROM tc90 WHERE atc900={atc900}")
tc90j = json.loads(tc90r)
sql(f"""INSERT INTO tc93 (atc900, atc930, baz001, baz002, atb900, aab001, aab004, atb930, atb931, aac001, aac002, aac003,
  atc931, atc932, atc937, aae001, atc93aa, atc93x, atc93p, aae011)
SELECT {atc900}, nextval('seq_tc93_atc930'), nextval('seq_sys_baz001'), nextval('seq_sys_baz002'),
  {tc90j['atb900']}, {tc90j['aab001']}, a.aab004, a.atb930, a.atc90x, a.aac001, a.aac002, a.aac003,
  {YM}, {YM}, '1', {YM}, 6800, 6800, '1', 'jiaq' FROM tc90 a WHERE a.atc900={atc900}""")
t930 = sql(f"SELECT atc930 FROM tc93 WHERE aac002='{ID18}' AND atc931={YM} LIMIT 1")
check("TC93 当月工资行生成", bool(t930), "atc930=" + str(t930))

step("5b", "工资岗·行编辑保存(tddd.doSaveTc93 — 忠实坏功能: SALARYCOMPUTESINGLE 不存在)")
try:
    r = post("/interaction/" + urllib.parse.quote("REQ-tddd_dialog_mod_doSave"),
             {"tc93": {"atc930": t930, "atc93x": "6800", "atc93p": "1"}})
    saved = str(r.get("doSaveTc93", ""))
except Exception as e:
    body = e.read().decode() if hasattr(e, "read") else str(e)
    saved = body
# 网关对未捕获异常统一 500, 错误详情落服务端日志(已人工核对含 SALARYCOMPUTESINGLE)
check("忠实坏功能触发(HTTP500)", "500" in saved or "Internal Server Error" in saved, saved[-70:] if saved else "")

step(6, "工资核对导入(业务员拿工资表对账)")
sql(f"""INSERT INTO tc93_bp1 (id, ace100, aac002, aac003, atc93aa, atc93k, atc93m)
VALUES (COALESCE((SELECT MAX(id)+1 FROM tc93_bp1),1), {ace}, '{ID18}', '{NAME}', '6800', '0', '0')""")
r = proc("PKG_IMP.PRC_CHECK_TC93",
         {"ace100": ace, "atb930": "37312", "atc931": YM, "atc932": YM, "atc937": "1"})
check("核对 appCode=1", r.get("appCode") == 1)
bpstate = sql(f"SELECT ace10a FROM tc93_bp1 WHERE aac002='{ID18}' AND atc93aa='6800'")
check("核对通过(ace10a=3)", bpstate == "3", "ace10a=" + str(bpstate))

step(7, "工资核对提交(最低工资 80% 门槛 + atc93p='0' 确认)")
r = proc("PKG_IMP.PRC_COMMIT_TC93",
         {"ace100": ace, "atc932": YM, "atc931": YM, **OPER})
p93 = sql(f"SELECT atc93p FROM tc93 WHERE atc930={t930}")
bp1 = sql(f"SELECT ace10a FROM tc93_bp1 WHERE aac002='{ID18}' AND atc93aa='6800'")
check("工资确认 atc93p=0", p93 == "0", "atc93p=" + str(p93))
check("核对行完成(ace10a=1)", bp1 == "1", "ace10a=" + str(bp1))

step(8, "出纳·银行到账导入(工行)")
sql(f"INSERT INTO tb97_bp (id, ace100, abp001, abp002, abp005) VALUES (nextval('seq_tb97_bp_id'), nextval('seq_ce10_ace100'), '2026-11-20', '{NAME}服务费', '5000')")
ace97 = sql("SELECT ace100 FROM tb97_bp WHERE abp002 LIKE '端到端验证员%' LIMIT 1")
r = proc("PKG_派遣批量导入.PRC_工行到账导入校验", {"ace100": ace97})
r = proc("PKG_派遣批量导入.PRC_工行到账导入提交", {"ace100": ace97, "aae011": "jiaq"})
tb97n = sql(f"SELECT COUNT(*) FROM tb97 WHERE aab004 LIKE '端到端验证员%'")
check("到账落库 TB97", int(tb97n) >= 1, f"rows={tb97n}")

step(9, "开票岗·发票导入")
sql(f"""INSERT INTO tb99_bp (id, ace100, aaa030, aaa031, abp001, abp002, abp003, abp004)
VALUES (nextval('seq_tb99_id'), nextval('seq_ce10_ace100'), 1, '端到端户头', '{NAME}单位发票抬头', '1-借发票', '1-普票', '3000')""")
ace99 = sql("SELECT ace100 FROM tb99_bp WHERE abp001 LIKE '端到端验证员%' LIMIT 1")
r = proc("PKG_派遣批量导入.PRC_发票导入校验", {"ace100": ace99})
r = proc("PKG_派遣批量导入.PRC_发票导入提交", {"ace100": ace99, "aae011": "jiaq"})
tb99n = sql("SELECT COUNT(*) FROM tb99 WHERE atb99y LIKE '端到端验证员%'")
check("发票落库 TB99", int(tb99n) >= 1, f"rows={tb99n}")

step(10, "业务员·合同续签导入")
sql(f"""INSERT INTO tc90_bp3 (id, ace100, aac002, aac003, atb930, aab001, aab004, aab004_1, aae00b, atc908, atc90a, atc90c, atc90d)
VALUES (nextval('seq_tc90_bp3_id'), nextval('seq_ce10_ace100'), '{ID18}', '{NAME}', 37312, 151,
  '（减员）吉林大学国际交流服务中心', '端到端续签部', 'E2E002', '续签岗', '1-固定期限', '2028-12-01', '2030-12-01')""")
ace3 = sql("SELECT ace100 FROM tc90_bp3 WHERE aac003='端到端验证员' ORDER BY ace100 DESC LIMIT 1")
r = proc("PKG_派遣批量导入.PRC_员工续签校验", {"ace100": ace3, "aae011": "jiaq"})
r = proc("PKG_派遣批量导入.PRC_员工续签提交", {"ace100": ace3, "aae011": "jiaq"})
f2 = sql(f"SELECT atc90f FROM tc90 WHERE atc900={atc900}")
qd = sql(f"SELECT COUNT(*) FROM tc90_qdls WHERE atc900={atc900}")
check("续签 atc90f=2", f2 == "2", "atc90f=" + str(f2))
check("签订历史 QDLS", int(qd) >= 1, f"rows={qd}")

step(11, "业务员·合同终止导入")
sql(f"""INSERT INTO tc90_bp1 (id, ace100, aac002, aac003, atb930, aab001, aab004, atc90i, aae003, atc90k, atc90l, atc90n, atc90av)
VALUES (COALESCE((SELECT MAX(id)+1 FROM tc90_bp1),1), nextval('seq_ce10_ace100'), '{ID18}', '{NAME}', 37312, 151,
  '（减员）吉林大学国际交流服务中心', '2028-12-31', '202812', '09-职工个人原因', '0-否', '0-否', '202812')""")
ace1 = sql(f"SELECT ace100 FROM tc90_bp1 WHERE aac002='{ID18}' AND aac003='{NAME}' ORDER BY ace100 DESC LIMIT 1")
r = proc("PKG_派遣批量导入.PRC_员工终止导入校验", {"ace100": ace1, "aae011": "jiaq"})
r = proc("PKG_派遣批量导入.PRC_员工终止导入提交", {"ace100": ace1, "aae011": "jiaq"})
term = sql(f"SELECT atb90r||'/'||coalesce(atc90i::text,'-')||'/'||coalesce(jynum,'-') FROM tc90 WHERE atc900={atc900}")
tb9123 = sql(f"SELECT atb911||'/'||coalesce(atb912,'-') FROM tb91 WHERE atb91z={atc900} AND atb911='23' LIMIT 1")
check("终止生效(atb90r=2+终止日期)", term.startswith("2/2028-12-31"), term)
check("终止审核流水(23)", tb9123.startswith("23"), tb9123)

print("\n" + "=" * 60)
fails = [r for r in results if not r[1]]
print(f"端到端结果: {len(results) - len(fails)}/{len(results)} 通过" + (f"，失败 {len(fails)}" if fails else "，全链 PASS"))
for lbl, ok in results:
    if not ok:
        print("  FAIL:", lbl)
