#!/usr/bin/env python3
"""工作台新链路冒烟: 模拟 static/index.html 各页真实调用序列(/wb + /proc + /interaction)。
可重复跑(身份证顺序生成防重)。跑完自清理: 审核通过链路留合同, 驳回链路删净。"""
import json
import subprocess
import urllib.request
import urllib.parse

BASE = "http://localhost:8080"


def sql(q):
    r = subprocess.run(["psql", "-h", "localhost", "-U", "rcpq", "-d", "rainbow_rcpq", "-tAc", q],
                       capture_output=True, text=True, env={"PGPASSWORD": "rcpq_dev", "PATH": "/usr/bin:/bin"})
    return (r.stdout or "").strip()


def post(path, body):
    req = urllib.request.Request(BASE + path, data=json.dumps(body).encode(),
                                 headers={"Content-Type": "application/json"})
    return json.load(urllib.request.urlopen(req, timeout=60))


def get(path):
    return json.load(urllib.request.urlopen(BASE + path, timeout=60))


def proc(name, body):
    r = post("/proc/" + urllib.parse.quote(name), body)
    assert r.get("appCode") == 1, (name, r)
    return r


W = [7, 9, 10, 5, 8, 4, 2, 1, 6, 3, 7, 9, 10, 5, 8, 4, 2, 1]
CK = "10X98765432"
seq = 900
ID18 = "22010219961105" + f"{seq:03d}"
ID18 += CK[sum(int(ID18[i]) * W[i] for i in range(17)) % 11]
while sql(f"SELECT COUNT(*) FROM ac01 WHERE aac002='{ID18}'") not in ("0", ""):
    seq += 1
    b = "22010219961105" + f"{seq:03d}"
    ID18 = b + CK[sum(int(b[i]) * W[i] for i in range(17)) % 11]
NAME = "工作台冒烟员"

# ① 经办人/基础数据选择器
opers = get("/wb/opers")
assert any(o["aae011"] == "jiaq" for o in opers), "opers 缺 jiaq"
units = get("/wb/units")
u151 = next(u for u in units if u["aab001"] == 151)
agreements = get("/wb/agreements?aab001=151")
assert any(a["atb900"] == 58332 for a in agreements)
settle = get("/wb/settle-units?aab001=151")
assert any(s["atb930"] == 37312 for s in settle)
print(f"① 选择器 OK: 单位'{u151['aab004']}' 协议58332 结算37312 经办{len(opers)}人")

# ② 新签导入链(hire.js 序列)
r = post("/wb/hire-batch", {
    "aac002": ID18, "aac003": NAME, "aae00p": "1-派遣", "atb930": 37312, "atb900": 58332,
    "aab001": 151, "atc90a": "1-固定期限", "atc90c": "2026-11-01", "atc90d": "2028-11-01",
    "atc905": "6800", "aae002": "202611", "aaa401": "1-是",
    "aaa100": "220100000000-吉林省长春市", "aaa203": "202611",
    "aaa301": "6800", "aab004": u151["aab004"],
    "aae005": "13000000000", "atc90ax": "2035-12-31", "atc90b": "2026-11-01",
    "atb90a": "150", "atb90b": "0"})
ace = r["ace100"]
proc("PKG_派遣批量导入.PRC_员工导入校验", {"ace100": str(ace), "aae011": "jiaq"})
b = get(f"/wb/batch/{ace}")
assert b["table"] == "tc90_bp" and b["rows"][0]["ace10a"] == "3", b
print(f"② 批次 {ace} 校验过(ace10a=3), 原因列: {b['rows'][0]['ace10b']!r}")
proc("PKG_派遣批量导入.PRC_员工导入提交", {"ace100": str(ace), "aae011": "jiaq"})
atc900 = sql(f"SELECT atc900 FROM tc90 WHERE aac002='{ID18}' LIMIT 1")
assert atc900, "提交后无合同"

# ③ 审核队列出现 → 通过(commit 交互) → 出队
queue = get("/wb/audit-queue")
row = next(r for r in queue if r["atc900"] == int(atc900))
post("/interaction/" + urllib.parse.quote("REQ-tdbf__dialog_xinqcl-commit"), {
    "tc90": {"atc900": row["atc900"], "atb90r": "3", "bae011": "jiaq", "bae017": "4101",
             "bae019": "贾悫", "bae020": "吉林省彩虹人才开发咨询服务有限公司", "bae022": "410100000000"},
    "tb91": {"atb910": row["atb910"], "atb91z": row["atc900"], "atb911": "20", "atb912": "3",
             "bae036": "2026-11-11T09:00:00", "bae011": "jiaq", "bae017": "4101",
             "bae019": "贾悫", "bae020": "吉林省彩虹人才开发咨询服务有限公司", "bae022": "410100000000"}})
st = sql(f"SELECT atb90r FROM tc90 WHERE atc900={atc900}")
assert st == "3", f"审核后 atb90r={st}"
assert not any(r["atc900"] == int(atc900) for r in get("/wb/audit-queue"))
print(f"③ 队列→通过→出队 OK (atc900={atc900}, atb90r=3)")

# ④ 第二张身份证再导一单 → 队列出现 → 驳回(cancle 交互) → 合同+流水删净
seq2 = seq + 1
b2 = "22010219961105" + f"{seq2:03d}"
ID18B = b2 + CK[sum(int(b2[i]) * W[i] for i in range(17)) % 11]
r = post("/wb/hire-batch", {
    "aac002": ID18B, "aac003": NAME + "2", "aae00p": "1-派遣", "atb930": 37312, "atb900": 58332,
    "aab001": 151, "atc90a": "1-固定期限", "atc90c": "2026-12-01", "atc90d": "2028-12-01",
    "atc905": "6800", "aae002": "202612", "aaa401": "1-是",
    "aaa100": "220100000000-吉林省长春市", "aaa203": "202612",
    "aaa301": "6800", "aab004": u151["aab004"],
    "aae005": "13000000000", "atc90ax": "2035-12-31", "atc90b": "2026-12-01",
    "atb90a": "150", "atb90b": "0"})
ace2 = r["ace100"]
proc("PKG_派遣批量导入.PRC_员工导入校验", {"ace100": str(ace2), "aae011": "jiaq"})
proc("PKG_派遣批量导入.PRC_员工导入提交", {"ace100": str(ace2), "aae011": "jiaq"})
atc900b = sql(f"SELECT atc900 FROM tc90 WHERE aac002='{ID18B}' AND aac003='{NAME}2' LIMIT 1")
row2 = next(r for r in get("/wb/audit-queue") if r["atc900"] == int(atc900b))
post("/interaction/" + urllib.parse.quote("REQ-tdbf_dialog_xinqcl-cancle"),
     {"tc90": {"atc900": row2["atc900"]}, "tb91": {"atb910": row2["atb910"]}})
left = sql(f"SELECT (SELECT COUNT(*) FROM tc90 WHERE atc900={atc900b})"
           f"||'/'||(SELECT COUNT(*) FROM tb91 WHERE atb910={row2['atb910']})")
assert left == "0/0", f"驳回后残留 {left}"
assert not any(r["atc900"] == int(atc900b) for r in get("/wb/audit-queue"))
print(f"④ 队列→驳回→删净 OK (atc900={atc900b}, 残留 {left})")

# ⑤ 工资核对批次可视化(salary.js 序列, 用④留下的空壳合同已删, 直接用 ③ 的生效合同)
setup = post("/wb/salary-setup", {"atc900": int(atc900), "ym": 202611, "atc93aa": "6800", "atb930": 37312})
proc("PKG_IMP.PRC_CHECK_TC93", {"ace100": str(setup["ace100"]), "atb930": "37312",
                                "atc931": "202611", "atc932": "202611", "atc937": "1"})
b = get(f"/wb/batch/{setup['ace100']}")
mine = [r for r in b["rows"] if r["aac003"] == NAME]
assert mine and mine[0]["ace10a"] == "3", mine
proc("PKG_IMP.PRC_COMMIT_TC93", {"ace100": str(setup["ace100"]), "atc932": "202611",
                                 "atc931": "202611", "aae011": "jiaq", "aae017": "4101",
                                 "aae019": "贾悫", "aae020": "吉林省彩虹人才开发咨询服务有限公司",
                                 "aae022": "410100000000"})
p = sql(f"SELECT atc93p FROM tc93 WHERE atc930={setup['atc930']}")
assert p == "0", f"atc93p={p}"
print(f"⑤ 工资编制→核对(批次可见)→提交 OK (atc930={setup['atc930']}, atc93p=0)")

print("\n工作台新链路冒烟: 5/5 PASS")
