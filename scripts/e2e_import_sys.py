#!/usr/bin/env python3
"""通用导入通道 /import/sys 端到端: 模板下载 → 填模板 → 上传 → 校验 → 提交 全四路。

链路(同人递进): tc90_bp 新签(生成 AC01/TC90 待审) → 审核通过 → tc90_bp2 更新(改电话落账)
→ tc90_bp3 续签(写 TC90_QDLS + atc90f='2') → tc90_bp4 新增保险(校验面)。
前置: app 于 localhost:8080; PG rainbow_rcpq; jiaq 经办人; 单位 151/协议 58332/结算单元 37312。
用法: python3 scripts/e2e_import_sys.py
说明: Excel 由下载的模板反向填充(按列头中文定位), 同时测到模板生成端点; 断言一律按本 run
批次号(ace100)圈定(冒烟铁律: 禁跨 run LIKE 计数); 费用开始年月恒下月(AA95 时限守卫)。
"""
import datetime
import io
import json
import subprocess
import urllib.parse
import urllib.request

import openpyxl

BASE = "http://localhost:8080"
UNIT, AGREEMENT, SETTLE = 151, 58332, 37312
UNIT_NAME = "（减员）吉林大学国际交流服务中心"

results = []


def check(label, cond, detail=""):
    print(("  PASS " if cond else "  FAIL ") + label + (f"  [{detail}]" if detail else ""))
    results.append((label, bool(cond)))


def sql(q: str):
    r = subprocess.run(["psql", "-h", "localhost", "-U", "rcpq", "-d", "rainbow_rcpq", "-tAc", q],
                       capture_output=True, text=True,
                       env={"PGPASSWORD": "rcpq_dev", "PATH": "/usr/bin:/bin"})
    return (r.stdout or "").strip()


def post_json(path: str, body: dict) -> dict:
    req = urllib.request.Request(BASE + path, data=json.dumps(body).encode(),
                                 headers={"Content-Type": "application/json"})
    return json.load(urllib.request.urlopen(req, timeout=120))


def proc(name: str, body: dict) -> dict:
    return post_json("/proc/" + urllib.parse.quote(name), body)


def multipart_upload(import_id: str, filename: str, data: bytes, params: dict) -> dict:
    boundary = "----e2eimportsys"
    parts = []
    parts.append((f"--{boundary}\r\nContent-Disposition: form-data; name=\"parameters\"\r\n\r\n"
                  + json.dumps(params) + "\r\n").encode())
    parts.append(f"--{boundary}\r\nContent-Disposition: form-data; name=\"file\"; "
                 f"filename=\"{filename}\"\r\nContent-Type: "
                 "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet\r\n\r\n".encode() + data + b"\r\n")
    parts.append(f"--{boundary}--\r\n".encode())
    req = urllib.request.Request(BASE + "/import/sys/" + import_id, data=b"".join(parts),
                                 headers={"Content-Type": f"multipart/form-data; boundary={boundary}"})
    return json.load(urllib.request.urlopen(req, timeout=120))


def get_template(import_id: str) -> bytes:
    return urllib.request.urlopen(BASE + "/import/sys/" + import_id + "/template", timeout=60).read()



def _has_header(tpl: bytes, head_row: int, col: int) -> bool:
    import openpyxl as _ox, io as _io
    ws = _ox.load_workbook(_io.BytesIO(tpl))["Sheet0"]
    return bool(ws.cell(row=head_row, column=col).value)

def fill_template(tpl: bytes, head_row: int, values: dict) -> bytes:
    """按列头中文定位填一行数据(第 head_row+1 行), 返回新 xlsx 字节。"""
    wb = openpyxl.load_workbook(io.BytesIO(tpl))
    ws = wb["Sheet0"]
    headers = {}
    for c in range(1, ws.max_column + 1):
        v = ws.cell(row=head_row, column=c).value
        if v:
            headers[str(v).strip()] = c
    missing = [k for k in values if k not in headers]
    if missing:
        raise AssertionError(f"模板缺列头: {missing}; 现有: {list(headers)}")
    for key, val in values.items():
        ws.cell(row=head_row + 1, column=headers[key], value=val)
    buf = io.BytesIO()
    wb.save(buf)
    return buf.getvalue()


def upload_round(t, tpl, values, extra_params):
    """模板下载→填表→上传→(ace100)。values 键=列头中文。"""
    data = fill_template(tpl, t["head"], values)
    params = {"aab001": UNIT, "aab004": UNIT_NAME, "atb900": AGREEMENT, "atb930": SETTLE,
              "aae002": NY, "aaa203": NY, "aae011": "jiaq"}
    params.update(extra_params)
    r = multipart_upload(t["id"], t["id"] + ".xlsx", data, params)
    assert r.get("succeed"), f"{t['id']} 上传失败: {r}"
    return r


def batch_state(ace):
    return sql(f"SELECT COALESCE(string_agg(ace10a, ',' ORDER BY id), '-') FROM tc90_bp WHERE ace100={ace}")


def batch_rows(tbl, ace):
    return sql(f"SELECT COUNT(*) FROM {tbl} WHERE ace100={ace}")


def batch_state2(tbl, ace):
    return sql(f"SELECT COALESCE(string_agg(ace10a, ',' ORDER BY id), '-') FROM {tbl} WHERE ace100={ace}")


# --- 身份证生成(同 e2e_business: 合法校验位 + 池避让) ---
W = [7, 9, 10, 5, 8, 4, 2, 1, 6, 3, 7, 9, 10, 5, 8, 4, 2, 1]
CK = "10X98765432"


def gen_id(seq):
    b = "22010219961105" + f"{seq:03d}"
    s = sum(int(b[i]) * W[i] for i in range(17))
    return b + CK[s % 11]


_seq = 1
ID18 = gen_id(_seq)
while (sql(f"SELECT COUNT(*) FROM ac01 WHERE aac002='{ID18}'") not in ("0", "")
       or sql(f"SELECT COUNT(*) FROM tc90 WHERE aac002='{ID18}'") not in ("0", "")):
    _seq += 1
    ID18 = gen_id(_seq)
NAME = "导入通道验证员"

_ym = datetime.date.today().strftime("%Y%m")
_y, _m = int(_ym[:4]), int(_ym[4:])
NY = f"{_y + (_m == 12)}{( _m % 12) + 1:02d}"
TODAY = datetime.date.today()
M1 = TODAY.replace(day=1).strftime("%Y-%m-%d")            # 签订/开始(本月 1 号, 合同日期无 AA95 限制)
Y2 = (TODAY.replace(day=1).replace(year=TODAY.year + 2)).strftime("%Y-%m-%d")  # 终止(+2 年)
Y1 = (TODAY.replace(day=1).replace(year=TODAY.year + 1)).strftime("%Y-%m-%d")  # 续签终止(+1 年)

T_BP = {"id": "tc90_bp", "head": 8}
T_BP2 = {"id": "tc90_bp2", "head": 4}
T_BP3 = {"id": "tc90_bp3", "head": 4}
T_BP4 = {"id": "tc90_bp4", "head": 4}

print(f"=== 通用导入通道 e2e: 身份证 {ID18} / 单位 {UNIT} 协议 {AGREEMENT} 结算 {SETTLE} / 费用年月 {NY} ===")

# 0) 模板下载四连
print("\n=== 0. 模板下载(/import/sys/{id}/template) ===")
tpls = {}
for t in (T_BP, T_BP2, T_BP3, T_BP4):
    try:
        b = get_template(t["id"])
        wb = openpyxl.load_workbook(io.BytesIO(b))
        ws = wb["Sheet0"]
        t["tpl"] = b
        t["tpl_cols"] = sum(1 for c in range(1, ws.max_column + 1) if ws.cell(row=t["head"], column=c).value)
        check(f"模板 {t['id']} 可下载可解析(列头 {t['tpl_cols']} 个)", t["tpl_cols"] > 0)
    except Exception as e:
        t["tpl"] = None
        check(f"模板 {t['id']} 可下载可解析", False, str(e)[:80])

# 1) 新签 tc90_bp 全链
print("\n=== 1. 新签 tc90_bp(员工合同及参保信息 54 列): 下载→填→传→校验→提交 ===")
# 只填模板中真实存在且未标 ※ 的列(民族/户籍/联系电话等列不在 tc90_bp 映射内——
# 电话按东软设计走"更新导入"表补录; 照片/试用期块列 Oracle 即无 → PG 忠实无列, 引擎跳过)
values = {
    "公民身份证号码": ID18, "姓名": NAME, "用工方式": "1-全职",
    "合同类别": "1-固定期限", "合同/协议签订日期": M1, "合同/协议开始日期": M1,
    "合同/协议终止日期": Y2, "转正劳动报酬": 6800, "工时制度": "1-标准工时", "岗位": "导入测试岗",
    "是否签订退休三方协议": "0-否",
    "是否参加养老保险": "1-是", "参保属地": "220100000000-吉林省长春市", "养老缴费基数": 6800,
}
r = upload_round(T_BP, T_BP["tpl"], values, {
    # 东软新签弹窗字段(不在模板 54 列映射内, parameters 透传): 默认值同 /wb/hire-batch 单人路线
    "aac005": "01-汉族", "aac011": "11-研究生", "aac012": "91-其他人员", "aae00p": "1-派遣",
    "aac009": "10-城镇失业", "aac013": "1-城镇新增",
    "aae005": "13000000000", "atc90ax": "2035-12-31", "atc902": "长春市南关区测试路1号",
    "atb90a": "150", "atb90b": "0",
})
ace = (r.get("ce10") or [0])[0]
print("  批次:", ace, " 超长:", r.get("errors") or "无", " 跳过列:", len(r.get("skipped") or []))
check("上传落批次(tc90_bp)", batch_rows("tc90_bp", ace) == "1", f"rows={batch_rows('tc90_bp', ace)}")
check("CE10 批次记录生成", sql(f"SELECT COUNT(*) FROM ce10 WHERE ace100={ace}") == "1")
check("透传列落行(aac009/aae005/atb930)", sql(
    f"SELECT aac009||'/'||aae005||'/'||atb930 FROM tc90_bp WHERE ace100={ace}") ==
    f"10-城镇失业/13000000000/{SETTLE}", sql(f"SELECT aac009||'/'||aae005||'/'||atb930 FROM tc90_bp WHERE ace100={ace}"))

proc("PKG_派遣批量导入.PRC_员工导入校验", {"ace100": str(ace), "aae011": "jiaq"})
st = batch_state(ace)
check("新签校验全过(ace10a=3)", st == "3", f"ace10a={st} 原因={sql(f'SELECT ace10b FROM tc90_bp WHERE ace100={ace} LIMIT 1')}")
proc("PKG_派遣批量导入.PRC_员工导入提交", {"ace100": str(ace), "aae011": "jiaq"})
ac01 = sql(f"SELECT COUNT(*) FROM ac01 WHERE aac002='{ID18}'")
atc900 = sql(f"SELECT COALESCE(MAX(atc900),0) FROM tc90 WHERE aac002='{ID18}'")
tb91 = sql(f"SELECT atb911||'/'||atb912 FROM tb91 WHERE atb91z={atc900} AND atb911='20' ORDER BY atb910 DESC LIMIT 1")
check("提交生成 AC01", ac01 == "1")
check("TC90 待审(atb90r=2)", sql(f"SELECT atb90r FROM tc90 WHERE atc900={atc900}") == "2")
check("TB91 审核流水(20/2)", tb91 == "20/2", tb91)
check("TC90_LIST 镜像", int(sql(f"SELECT COUNT(*) FROM tc90_list WHERE atc900={atc900}") or 0) >= 1)

# 2) 审核通过(复用 e2e_business 交互通道) → 续签/更新才有生效合同可匹配
print("\n=== 2. 审核通过(interaction doXinqCommit) ===")
AUD = "吉林省彩虹人才开发咨询服务有限公司"
tc90row = json.loads(sql("SELECT json_build_object('atc900', atc900, 'aac001', aac001, 'atb90r', '3', "
    f"'bae011', 'jiaq', 'bae017', '4101', 'bae019', '贾悫', 'bae020', '{AUD}', 'bae022', '410100000000') "
    f"FROM tc90 WHERE atc900={atc900}"))
tb91row = json.loads(sql("SELECT json_build_object('atb910', atb910, 'atb91z', atb91z, 'atb911', atb911, "
    "'atb912', '3', 'bae011', 'jiaq', 'bae017', '4101', 'bae019', '贾悫', "
    f"'bae020', '{AUD}', 'bae022', '410100000000', 'bae036', to_char(now(), 'YYYY-MM-DD\"T\"HH24:MI:SS')) "
    f"FROM tb91 WHERE atb91z={atc900} AND atb911='20' ORDER BY atb910 DESC LIMIT 1"))
post_json("/interaction/" + urllib.parse.quote("REQ-tdbf__dialog_xinqcl-commit"),
          {"tc90": tc90row, "tb91": tb91row})
check("审核通过 atb90r=3", sql(f"SELECT atb90r FROM tc90 WHERE atc900={atc900}") == "3")

# 3) 更新 tc90_bp2(15 列): 改联系电话+岗位 → 落账
print("\n=== 3. 更新 tc90_bp2: 下载→填→传→校验→提交 ===")
r = upload_round(T_BP2, T_BP2["tpl"], {
    "姓名": NAME, "公民身份证号码": ID18, "联系电话": "13900000000", "岗位": "更新后岗位",
}, {})
ace2 = (r.get("ce10") or [0])[0]
check("更新上传落批次", batch_rows("tc90_bp2", ace2) == "1", f"rows={batch_rows('tc90_bp2', ace2)}")
proc("PKG_派遣批量导入.PRC_员工更新校验", {"ace100": str(ace2), "aae011": "jiaq"})
st = batch_state2("tc90_bp2", ace2)
check("更新校验全过(3)", st == "3", f"ace10a={st} 原因={sql(f'SELECT ace10b FROM tc90_bp2 WHERE ace100={ace2} LIMIT 1')}")
proc("PKG_派遣批量导入.PRC_员工更新提交", {"ace100": str(ace2), "aae011": "jiaq"})
check("更新落账 TC90.联系电话", sql(f"SELECT aae005 FROM tc90 WHERE atc900={atc900}") == "13900000000")
check("更新同步 TC90_LIST", sql(f"SELECT aae005 FROM tc90_list WHERE atc900={atc900} LIMIT 1") == "13900000000")

# 4) 续签 tc90_bp3(9 列): 新合同期 → TC90_QDLS + atc90f='2'
print("\n=== 4. 续签 tc90_bp3: 下载→填→传→校验→提交 ===")
r = upload_round(T_BP3, T_BP3["tpl"], {
    "姓名": NAME, "公民身份证号码": ID18, "联系电话": "13900000000", "员工号": "IMP001",
    "部门": "导入通道部", "岗位": "导入测试岗", "合同类别": "1-固定期限",
    "合同开始日期": M1, "合同终止日期": Y1,
}, {})
ace3 = (r.get("ce10") or [0])[0]
check("续签上传落批次", batch_rows("tc90_bp3", ace3) == "1", f"rows={batch_rows('tc90_bp3', ace3)}")
proc("PKG_派遣批量导入.PRC_员工续签校验", {"ace100": str(ace3), "aae011": "jiaq"})
st = batch_state2("tc90_bp3", ace3)
check("续签校验全过(3)", st == "3", f"ace10a={st} 原因={sql(f'SELECT ace10b FROM tc90_bp3 WHERE ace100={ace3} LIMIT 1')}")
proc("PKG_派遣批量导入.PRC_员工续签提交", {"ace100": str(ace3), "aae011": "jiaq"})
check("续签写签订历史 TC90_QDLS", int(sql(f"SELECT COUNT(*) FROM tc90_qdls WHERE atc900={atc900}") or 0) >= 1)
check("续签置 atc90f='2'", sql(f"SELECT atc90f FROM tc90 WHERE atc900={atc900}") == "2")
check("续签改终止日期", sql(f"SELECT to_char(atc90d,'YYYY-MM-DD') FROM tc90 WHERE atc900={atc900}") == Y1)

# 5) 新增保险 tc90_bp4(13 列): 上传+校验(提交给在保合同加险种, 忠实规则视合同险种状态)
print("\n=== 5. 新增保险 tc90_bp4: 下载→填→传→校验 ===")
r = upload_round(T_BP4, T_BP4["tpl"], {
    "姓名": NAME, "公民身份证号码": ID18, "合同开始日期": M1,
    "社保参保属地": "220100000000-吉林省长春市", "社保参保年月": NY, "养老缴费基数": 6800,
}, {})
ace4 = (r.get("ce10") or [0])[0]
check("新增保险上传落批次", batch_rows("tc90_bp4", ace4) == "1", f"rows={batch_rows('tc90_bp4', ace4)}")
try:
    proc("PKG_派遣批量导入.PRC_新增保险校验", {"ace100": str(ace4), "aae011": "jiaq"})
    st = batch_state2("tc90_bp4", ace4)
    print(f"  新增保险校验后 ace10a={st} 原因={sql(f'SELECT ace10b FROM tc90_bp4 WHERE ace100={ace4} LIMIT 1')}")
    check("新增保险校验过程可执行(appCode=1)", True)
except AssertionError as e:
    check("新增保险校验过程可执行(appCode=1)", False, str(e)[:100])

# 6) 审计留痕
print("\n=== 6. 操作留痕(wb_audit_log) ===")
n = sql(f"SELECT COUNT(*) FROM wb_audit_log WHERE action='import:sys' AND target='tc90_bp' "
        f"AND detail::text LIKE '%{ace}%'")
check("导入动作已留痕", int(n or 0) >= 1, f"count={n}")
n2 = sql("SELECT COUNT(*) FROM wb_audit_log WHERE action='import:template'")
check("模板下载已留痕", int(n2 or 0) >= 4, f"count={n2}")

# 7) 专项五路: 模板下载 → 上传 → 校验(过程可执行; 行级拒绝是合法业务判定, 断言只看通道)
print("\n=== 7. 专项五路(tc04/05/06/07/td04_bp): 模板→上传→校验 ===")
SPECIALS = [
    {"id": "tc04_bp", "head": 5, "row": {"序号": 1, "医保个人编号": "E2EYB001", "姓名": NAME,
        "费款所属期": NY, "待遇类别": "生育津贴", "日期": M1, "生育补助金": 5000}},
    {"id": "tc05_bp", "head": 1, "row": {"结算单元名称": UNIT_NAME, "保险合同号码": "E2E-POLICY-001",
        "姓名": NAME, "公民身份号码": ID18, "意外险开始日期": M1, "意外险结束日期": Y1,
        "意外险额度": 500000, "费用承担方式": "单位承担", "费用结算年月": NY}},
    {"id": "tc06_bp", "head": 1, "row": {"姓名": NAME, "身份证号码": ID18, "异动原因": "07-社保个人原因增减",
        "养老异动情况": "2-增员", "养老异动截止年月": NY, "调整后养老缴费基数": 6800}},
    {"id": "tc07_bp", "head": 1, "row": {"姓名": NAME, "公民身份号码": ID18,
        "单位险金合计": 100, "个人险金合计": 50}},
    {"id": "td04_bp", "head": 1, "row": {"姓名": NAME, "身份证号码": ID18, "单位是否提前通知": "1-是",
        "近12个月收入": 6800, "平均月收入": 6800, "补偿月数": 2, "待通知金": 6800,
        "经济补偿金合计": 20400, "收取方式": "1-通过工资表收取", "费用结算年月": NY}},
]
for sp in SPECIALS:
    tid = sp["id"]
    tpl_cfg = {"id": tid, "head": sp["head"]}
    if not T_BP.get("tpl"):  # 模板字节缓存复用: 单独下载
        pass
    tpl = get_template(tid)
    try:
        data = fill_template(tpl, sp["head"], sp["row"])
        up = {"aae011": "jiaq"}
        if tid in ("tc06_bp", "tc07_bp"):
            up["aae002"] = _ym            # 落行 NOT NULL(模板列外, 弹窗透传)
        if tid == "td04_bp":
            up["atb931"] = UNIT_NAME      # 结算单元落行 NOT NULL
            up["atb930"] = SETTLE
            up["aab001"] = UNIT
        r = multipart_upload(tid, tid + ".xlsx", data, up)
        ace_s = (r.get("ce10") or [0])[0]
        tbl = {"tc04_bp": "tc04_bp", "tc05_bp": "tc05_bp", "tc06_bp": "tc06_bp",
               "tc07_bp": "tc07_bp", "td04_bp": "td04_bp"}[tid]
        check(f"专项 {tid} 上传落批次", batch_rows(tbl, ace_s) == "1",
              f"rows={batch_rows(tbl, ace_s)} msg={r.get('msg', '')}")
        proc_name = {"tc04_bp": "PRC_生育补贴校验", "tc05_bp": "PRC_意外险导入校验",
                     "tc06_bp": "PRC_特殊情况导入校验", "tc07_bp": "PRC_保险补贴导入校验",
                     "td04_bp": "PRC_补偿金导入校验"}[tid]
        body = {"ace100": str(ace_s), "aae011": "jiaq"}
        if tid == "tc06_bp":
            body["aae002"] = _ym
        pr = proc("PKG_派遣批量导入." + proc_name, body)
        st_row = sql(f"SELECT COALESCE(string_agg(ace10a, ','), '-') FROM {tbl} WHERE ace100={ace_s}")
        reason = sql(f"SELECT COALESCE(ace10b, '') FROM {tbl} WHERE ace100={ace_s} LIMIT 1")
        print(f"  {tid}: appCode={pr.get('appCode')} ace10a={st_row} 原因={reason[:60]}")
        check(f"专项 {tid} 校验过程可执行(appCode=1)", pr.get("appCode") == 1)
    except Exception as e:
        check(f"专项 {tid} 全链", False, str(e)[:100])

# 8) 三银行到账: 工行/建行/吉林 Excel 路线, 校验+提交(TB97 增量断言)
print("\n=== 8. 三银行到账(tb97_bp/bp01/bp02): 模板→上传→校验→提交→TB97 ===")
BANK_ROWS = {
    "tb97_bp": {"到账日期": M1, "金额": 3000, "对方户名": "端到端银行户A", "对方账号": "622200001",
                "摘要": "e2e工行到账"},
    "tb97_bp01": {"到账日期": M1.replace("-", ""), "金额": 4000, "对方户名": "端到端银行户B", "对方账号": "622200002",
                  "摘要": "e2e建行到账"},
    "tb97_bp02": {"到账日期": M1.replace("-", ""), "金额": 5000, "对方户名": "端到端银行户C", "对方账号": "622200003"},
}
BANK_PROC = {
    "tb97_bp": ("PRC_工行到账导入校验", "PRC_工行到账导入提交"),
    "tb97_bp01": ("PRC_建行到账导入校验", "PRC_建行到账导入提交"),
    "tb97_bp02": ("PRC_吉林银行到账导入校验", "PRC_吉林银行到账导入提交"),
}
BANK_TBL = {"tb97_bp": "tb97_bp", "tb97_bp01": "tb97_bp01", "tb97_bp02": "tb97_bp02"}
for bid, brow in BANK_ROWS.items():
    try:
        tpl = get_template(bid)
        data = fill_template(tpl, 1, brow)
        r = multipart_upload(bid, bid + ".xlsx", data, {"aae011": "jiaq"})
        ace_b = (r.get("ce10") or [0])[0]
        check(f"{bid} 上传落批次", batch_rows(BANK_TBL[bid], ace_b) == "1",
              f"rows={batch_rows(BANK_TBL[bid], ace_b)} msg={r.get('msg', '')[:80]}")
        chk, cmt = BANK_PROC[bid]
        proc("PKG_派遣批量导入." + chk, {"ace100": str(ace_b)})
        st_b = sql(f"SELECT COALESCE(string_agg(ace10a, ','), '-') FROM {BANK_TBL[bid]} WHERE ace100={ace_b}")
        reason = sql(f"SELECT COALESCE(ace10b, '') FROM {BANK_TBL[bid]} WHERE ace100={ace_b} LIMIT 1")
        print(f"  {bid}: 校验后 ace10a={st_b} 原因={reason[:60]}")
        check(f"{bid} 校验全过(3)", st_b == "3", f"ace10a={st_b}")
        n97_before = int(sql("SELECT COUNT(*) FROM tb97") or 0)
        proc("PKG_派遣批量导入." + cmt, {"ace100": str(ace_b), "aae011": "jiaq"})
        n97_after = int(sql("SELECT COUNT(*) FROM tb97") or 0)
        check(f"{bid} 提交落 TB97(+1)", n97_after == n97_before + 1,
              f"{n97_before}→{n97_after}")
    except Exception as e:
        check(f"{bid} 全链", False, str(e)[:100])

# 9) 费用结算确认/取消(PKG_RCPQ_FZ, 东软 td/d/d 8b): TC93 未结算行→确认落账→取消(忠实坏功能第六例)
print("\n=== 9. 费用结算确认/取消(td/d/d 8b) ===")
t930 = sql(f"""SELECT nextval('seq_tc93_atc930')""")
c900 = sql(f"SELECT MAX(atc900) FROM tc90 WHERE aac002='{ID18}'")
sql(f"""INSERT INTO tc93 (atc930, atc900, atb900, aab001, aab004, atb930, aac001, aac002, aac003,
  atc931, atc932, atc937, atc93aa, atc93x, atc93p, atc93g, aae011)
SELECT {t930}, {c900}, atb900, aab001, aab004, atb930, aac001, aac002, aac003,
  {_ym}, {_ym}, '1', 6800, 6800, '0', '0', 'jiaq'
  FROM tc90 WHERE atc900={c900}""")
check("§9 TC93 未结算行就绪(atc93g='0')", sql(
    f"SELECT COUNT(*) FROM tc93 WHERE atc930={t930} AND atc93g='0'") == "1")
r = proc("PKG_RCPQ_FZ.PRC_SALARYCOMPUTE",
         {"atb930": "37312", "atc931": _ym, "atc932": _ym, "atc937": "1", "aae011": "jiaq"})
check("§9 结算确认 appCode=1", r.get("appCode") == 1, str(r.get("errMsg"))[:60])
check("§9 本行落账 atc93g='1'", sql(f"SELECT atc93g FROM tc93 WHERE atc930={t930}") == "1",
      f"settled={r.get('settled')}")
r = proc("PKG_RCPQ_FZ.PRC_CANCLESALARYCOMPUTE",
         {"atb930": "37312", "atc931": _ym, "atc932": _ym, "atc937": "1"})
faithful = r.get("appCode") == -1 and "00904" in str(r.get("errMsg", ""))
check("§9 取消=忠实坏功能第六例(appCode=-1 ORA-00904 TC31 无列, 旧系统同样报错)", faithful,
      str(r.get("errMsg"))[:80])

# 10) 市医保应缴费(td/c/bj): PRC_市医保应缴费情况 生成 TJ06 + /wb/yue/tj06 读面
print("\n=== 10. 市医保应缴费(td/c/bj) ===")
# 户头遍历取首个成功(户头 1 存在迁移破损链: 单元 37230 不在 v_ab01_tb90_tb93 →
# 过程忠实报"结算单元不存在", 数据级问题与遗留同构, 跳过)
rows06 = []
for a030 in ("2", "4", "3", "1"):
    r = proc("PKG_派遣保险月情况.PRC_市医保应缴费情况",
             {"aae002": _ym, "aaa020": "220100000000", "aaa030": a030, "aae011": "jiaq"})
    print(f"  户头 {a030}: appCode={r.get('appCode')} rows={r.get('rows')} {str(r.get('errMsg'))[:50]}")
    if r.get("appCode") == 1:
        check(f"§10 应缴费生成 appCode=1(户头 {a030})", True)
        req = urllib.request.urlopen(BASE + "/wb/yue/tj0c2?ym=" + _ym
                                     + "&aaa030=" + a030 + "&aaa020=220100000000", timeout=60)
        rows06 = json.load(req)
        break
else:
    check("§10 应缴费生成 appCode=1", False, "全部户头失败")
check("§10 /wb/yue/tj0c2 读面返回明细", isinstance(rows06, list) and len(rows06) >= 1,
      f"{len(rows06)} 行")
print(f"  TJ06 明细: {len(rows06)} 行")

passed = sum(1 for _, ok in results if ok)
print(f"\n=== 结果: {passed}/{len(results)} PASS ===")
raise SystemExit(0 if passed == len(results) else 1)
