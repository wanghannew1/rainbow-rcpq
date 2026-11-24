#!/usr/bin/env python3
"""API 覆盖补齐 e2e: 2026-11-24 审计甄别的 22 个"无入库回归用例"过程/端点。
全部对现有 202611 节拍数据做可重复断言(appCode=1+契约字段, 计数只断 ≥0/数字),
重复跑安全。覆盖: 社保/市医保异动回盘校验+提交, 社保补缴明细+hz/bj,
缴费基数核对/确认×4 险种, 月垫付情况+yue/df, 保险补差确认, 保险当月补缴。"""
import json
import subprocess
import urllib.parse
import urllib.request

BASE = "http://localhost:8080"
YM = "202611"
YEAR = "2026"
A030 = "1"
ATCD = "220100000000"
OPER = "jiaq"
FAILS = []


def sql(q):
    r = subprocess.run(["psql", "-h", "localhost", "-U", "rcpq", "-d", "rainbow_rcpq", "-tAc", q],
                       capture_output=True, text=True, env={"PGPASSWORD": "rcpq_dev", "PATH": "/usr/bin:/bin"})
    return (r.stdout or "").strip()


def post(path, body, timeout=120):
    req = urllib.request.Request(BASE + path, data=json.dumps(body).encode(),
                                 headers={"Content-Type": "application/json"})
    return json.load(urllib.request.urlopen(req, timeout=timeout))


def get(path, timeout=120):
    return json.load(urllib.request.urlopen(BASE + path, timeout=timeout))


def proc(name, body, timeout=120):
    return post("/proc/" + urllib.parse.quote(name), body, timeout)


def chk(name, cond, detail=""):
    print(f"  [{'PASS' if cond else 'FAIL'}] {name} {detail}")
    if not cond:
        FAILS.append(name)


def step(i, t):
    print(f"\n== {i}. {t}")


def num(v):
    try:
        float(v)
        return True
    except (TypeError, ValueError):
        return False


# ---- 1. 社保异动回盘 校验+提交 ----
step(1, "社保异动回盘 校验+提交(hz 页同参)")
r = proc("PKG_保险人员增减.PRC_社保异动回盘校验", {"aae002": YM, "aaa030": A030, "aaa020": ATCD})
chk("社保回盘校验 appCode=1", r.get("appCode") == 1, str(r)[:120])
chk("社保回盘校验 契约字段(pass/reject)", num(r.get("pass")) and num(r.get("reject")), f"pass={r.get('pass')} reject={r.get('reject')}")
r = proc("PKG_保险人员增减.PRC_社保异动回盘提交",
         {"aae002": YM, "aaa030": A030, "aaa020": ATCD, "aae011": OPER})
chk("社保回盘提交 appCode=1", r.get("appCode") == 1, str(r)[:120])
chk("社保回盘提交 契约字段(success/objection/stop)",
    num(r.get("successApplied")) and num(r.get("objectionApplied")) and num(r.get("stopApplied")),
    f"成功={r.get('successApplied')} 异议={r.get('objectionApplied')} 停保={r.get('stopApplied')}")

# ---- 2. 市医保: 局方实样导入 → 校验+提交 ----
step(2, "市医保异动回盘 实样导入+校验+提交")
imp = subprocess.run(["curl", "-s", "-F",
                      f"file=@rcpq-web/src/main/resources/baopan/huipan/市医保回盘2026.04(1).xls",
                      f"{BASE}/wb/hz/import-bp?ym={YM}&kind=medical&aae011={OPER}&aaa030={A030}&aaa020={ATCD}"],
                     capture_output=True, text=True).stdout
try:
    j = json.loads(imp)
    # 重跑容忍: 文件重复导入可能业务拒绝(-1+errMsg); 参数缺失类 400(无 appCode 键)仍判 FAIL
    chk("市医保回盘文件导入 结构化返回", "appCode" in j, str(j)[:120])
except Exception as e:
    chk("市医保回盘文件导入 结构化返回", False, f"{e} {imp[:100]}")
r = proc("PKG_保险人员增减.PRC_市医保异动回盘校验", {"aae002": YM, "aaa030": A030, "aaa020": ATCD})
chk("市医保回盘校验 appCode=1", r.get("appCode") == 1, str(r)[:120])
r = proc("PKG_保险人员增减.PRC_市医保异动回盘提交",
         {"aae002": YM, "aaa030": A030, "aaa020": ATCD, "aae011": OPER})
chk("市医保回盘提交 appCode=1", r.get("appCode") == 1, str(r)[:120])

# ---- 3. 社保补缴明细 + hz/bj 读侧 ----
step(3, "社保补缴明细(重建 TC9W_BP05) + /wb/hz/bj")
r = proc("PKG_保险人员增减.PRC_社保补缴明细", {"aae002": YM, "aaa030": A030, "aaa020": ATCD})
chk("社保补缴明细 appCode=1", r.get("appCode") == 1, str(r)[:120])
chk("社保补缴明细 契约字段(contracts)", num(r.get("contracts")), f"contracts={r.get('contracts')}")
bj = get(f"/wb/hz/bj?ym={YM}&aaa030={A030}&aaa020={urllib.parse.quote(ATCD)}")
chk("/wb/hz/bj 返回清单", isinstance(bj, list), f"{len(bj)} 行")

# ---- 4. 缴费基数核对/确认 ×4 险种 ----
step(4, "缴费基数核对/确认 ×4(社保/医保/公积金/工伤)")
for key, hd, qr in [
    ("社保", "PRC_社保缴费基数核对", "PRC_社保缴费基数确认"),
    ("医保", "PRC_医保缴费基数核对", "PRC_医保缴费基数确认"),
    ("公积金", "PRC_公积金缴费基数核对", "PRC_公积金缴费基数确认"),
    ("工伤", "PRC_工伤缴费基数核对", "PRC_工伤缴费基数确认"),
]:
    r = proc("PKG_缴费基数." + hd, {"aae001": YEAR, "aaa030": A030, "aaa020": ATCD})
    chk(f"{key}核对 appCode=1", r.get("appCode") == 1, str(r)[:120])
    chk(f"{key}核对 契约字段(rows/marked)", num(r.get("rows")) and num(r.get("marked")),
        f"rows={r.get('rows')} marked={r.get('marked')}")
    r = proc("PKG_缴费基数." + qr, {"aae001": YEAR, "aaa030": A030, "aaa020": ATCD,
                                    "aae002": YM, "aae011": OPER})
    chk(f"{key}确认 appCode=1", r.get("appCode") == 1, str(r)[:120])
    chk(f"{key}确认 契约字段(confirmed)", num(r.get("confirmed")), f"confirmed={r.get('confirmed')}")

# ---- 5. 月垫付情况 + /wb/yue/df 读侧 (编排器含四连调, 数据量 ~200 合同时耗时 4~7 分钟) ----
step(5, "月垫付情况(编排器, 慢) + /wb/yue/df")
r = proc("PKG_派遣保险月情况.PRC_月垫付情况",
         {"aae002": YM, "aaa030": A030, "aae011": OPER}, timeout=580)
chk("月垫付情况 appCode=1(先删后插幂等)", r.get("appCode") == 1, str(r)[:160])
df = get(f"/wb/yue/df?ym={YM}&aaa030={A030}")
chk("/wb/yue/df 返回清单", isinstance(df, list), f"{len(df)} 行")

# ---- 6. 保险补差确认(TJ0B2 医保 置位/取消) ----
step(6, "保险补差确认(xz=2 医保)")
for flag, label in [("1", "置位"), ("0", "取消")]:
    r = proc("PKG_保险人员增减.PRC_保险补差确认",
             {"xz": "2", "flag": flag, "aae002": YM, "aaa030": A030,
              "aaa020": ATCD, "aae011": OPER})
    chk(f"保险补差确认 {label} appCode=1", r.get("appCode") == 1, str(r)[:120])

# ---- 7. 保险当月补缴(BbfCalc: atc900/aae002/lb) ----
step(7, "保险当月补缴(补退费引擎直调)")
atc900 = sql(f"SELECT atc900 FROM tc90 WHERE atb90r='3' AND aab001 IS NOT NULL ORDER BY atc900 DESC LIMIT 1")
r = proc("PKG_保险补退.PRC_保险当月补缴", {"atc900": atc900, "aae002": YM, "lb": "1"})
ok1 = r.get("appCode") == 1
structured = r.get("appCode") == -1 and bool(r.get("errMsg"))
chk("保险当月补缴 结构化返回(1 或带 errMsg 的 -1)", ok1 or structured, str(r)[:140])

print("\n==== API 覆盖补齐 e2e 总结 ====")
if FAILS:
    print(f"FAIL {len(FAILS)}: " + "; ".join(FAILS))
    raise SystemExit(1)
print("全部 PASS")
