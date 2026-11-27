#!/usr/bin/env python3
"""BC-06 基数确认族(社保/医保/公积金/工伤) 新系统同链五腿复跑 — 桥单 20261127-029。

镜像旧侧 rainbowpq/scripts/replay/jdqr_family_test.py(卷36) 的四险种同构链, 走新系统通道:
  TC-001 暂存  = POST /interaction/REQ-tdba_cont-save       (tdbaService.update→saveContract, 三 store 拆形状①)
  TC-002 提交  = POST /interaction/REQ-tdba_cont-commit     (tdbaService.doCommit, 拆形状①)
  TC-003 审核  = POST /interaction/REQ-tdbf__dialog_xinqcl-commit (doXinqCommit = COMFIRM_SH '20' 分支等价通道,
                 回执 023 口径; 029 阻断清单#1 修正——TC-003 腿本就可达, 无需新移植)
  AA97 批次    = POST /interaction/REQ-tdcl_dialog_mod-U    (saveAa97 = aa97Store 保存通道, 029 阻断清单#2
                 修正——在册可调, 无需新移植; 拆形状②: 单行 map 而非 DC 三 store 复合)
  核对×4      = POST /proc/PKG_缴费基数.PRC_?_缴费基数核对 {aae001,aaa030,aaa020}
  确认×4      = POST /proc/PKG_缴费基数.PRC_?_缴费基数确认 {aae001,aaa030,aaa020,aae002,aae011}
  清场        = POST /proc/PKG_人才派遣.PRC_删除员工 {prm_atc900}  (029 补移植, W-A 删前备份到
                 rcpq_del_backup(V21); TB91 不在删除面内, 按旧套件同口径 SQL 兜底删)
隔离月 209904(登记表 docs/import-channel.md §6); 人/户头/年度/属地与旧侧同形状
(重放基数员 998061 / 户头 9991-9994 / 年 2099 / 假区划 209900000000)。
纪律 v2: 清场前全列行态导出 → work/bc06_pg_rowdump.txt。
用法: python3 scripts/e2e_bc06_jdqr.py
"""
import datetime
import json
import os
import subprocess
import sys
import urllib.error
import urllib.request
from urllib.parse import quote

PG = "postgresql://rcpq:rcpq_dev@localhost:5432/rainbow_rcpq"
APP = "http://localhost:8080"
ISO_YEAR = "2099"
ISO_REG = "209900000000"
ISO_YM = "209904"          # 隔离月(209901/209902/209903 已登记在册)
UNIT_AAB001, UNIT_NAME = 349, "长春力旺实验初级中学"
AUD = "吉林省彩虹人才开发咨询服务有限公司"
T_NAME, T_AAC001 = "重放基数员", 998061

# (险种, 核对proc, 确认proc, 隔离户头, aa97 新基数列, 核对后同步列, 状态列, TC90 目标列, 新值)
XIAN = [
    ("社保", "PRC_社保缴费基数核对", "PRC_社保缴费基数确认", "9991",
     "aaa301_1", "aaa301", "aae214_1", ["atc905", "aaa301", "aaa304"], "3333"),
    ("医保", "PRC_医保缴费基数核对", "PRC_医保缴费基数确认", "9992",
     "aaa302_1", "aaa302", "aae214_2", ["aaa302", "aaa306"], "3333"),
    ("公积金", "PRC_公积金缴费基数核对", "PRC_公积金缴费基数确认", "9993",
     "aaa303_1", "aaa303", "aae214_3", ["aaa303"], "4444"),
    ("工伤", "PRC_工伤缴费基数核对", "PRC_工伤缴费基数确认", "9994",
     "aaa305_1", "aaa305", "aae214_4", ["aaa305"], "3333"),
]
BAE = {"bae011": "jiaq", "bae017": "4101", "bae019": "贾悫", "bae020": AUD, "bae022": "410100000000"}

PASS, FAIL = [], []


def rec(name, ok, detail=""):
    (PASS if ok else FAIL).append(name)
    print(("✅" if ok else "❌"), name, ("— " + detail if detail else ""))


def sql(q):
    r = subprocess.run(["psql", PG, "-tAc", q], capture_output=True, text=True)
    return (r.stdout or "").strip()


def http_json(url, data=None):
    try:
        req = urllib.request.Request(url, data=json.dumps(data or {}).encode(),
                                     headers={"Content-Type": "application/json"})
        with urllib.request.urlopen(req, timeout=120) as r:
            return r.status, json.loads(r.read().decode("utf-8", "replace"))
    except urllib.error.HTTPError as e:
        try:
            return e.code, json.loads(e.read().decode("utf-8", "replace"))
        except Exception:
            return e.code, {}


def proc(name, body):
    return http_json(APP + "/proc/" + quote(name), body)


def ia(code, body):
    return http_json(APP + "/interaction/" + quote(code), body)


W = [7, 9, 10, 5, 8, 4, 2, 1, 6, 3, 7, 9, 10, 5, 8, 4, 2]
CK = "10X98765432"


def id_full(base17):
    return base17 + CK[sum(int(c) * w for c, w in zip(base17, W)) % 11]


ID1 = id_full("22018319990101887")


def tc90_row():
    """旧套件 build_tc90_insert 形状镜像(模板活体行取值, 隔离作用域覆写, 客户端 JS 列补齐)。"""
    return {
        "aac001": T_AAC001, "aac002": ID1, "aac003": T_NAME, "aac004": "2",
        "aac006": "1999-01-01", "atc901": "28", "aac011": "21", "aac009": "11",
        "aac005": "01", "aac012": "91", "aac013": "1", "aae005": "13000000000",
        "aae006": "长春市", "atc902": "长春市", "aae00p": "5", "aac008": "27",
        "atb930": 37312, "atb900": 58332, "aab001": UNIT_AAB001, "aab004": UNIT_NAME,
        "atc908": "工人", "atc909": "1", "atc90a": "1", "atc90x": UNIT_NAME,
        "atc90b": "2018-12-01", "atc90c": "2018-12-01", "atc90d": "2035-11-30",
        "atc90ax": "2035-12-31", "atc90f": "1", "atc90g": "1",
        "atc905": "2800",
        "aaa100": ISO_REG, "aaa101": "吉林省重放区",
        "aaa110": ISO_REG, "aaa111": "吉林省重放区",
        "aaa120": ISO_REG, "aaa121": "吉林省重放区",
        "aaa130": ISO_REG, "aaa131": "吉林省重放区",
        "aaa201": ISO_YM, "aaa202": ISO_YM, "aaa203": ISO_YM, "aaa204": ISO_YM,
        "aaa501": ISO_YM, "aaa502": ISO_YM, "aaa503": ISO_YM, "aaa504": ISO_YM,
        "aaa301": 2800, "aaa302": 2800, "aaa303": 2800,
        "aaa304": 2800, "aaa305": 2800, "aaa306": 2800,
        "aaa401": "1", "aaa402": "1", "aaa403": "1", "aaa404": "1",
        "aaa405": "1", "aaa407": "1", "aaa408": "1",
        "aae100": "1", "aae002": ISO_YM, "aae003": ISO_YM,
        "atc90p": "2", "atc90w": "2", "atc90pa": "2", "atc90pb": "2",
        "atc90ai": "0", "atc90at": "0", "atc90ah": "0",
        "atb90c": "0", "atb90a": "40", "atb90b": "0", "atc90zb": "1",
        "atb90r": "1",  # 旧侧 tdba_cont.js:640 客户端赋值, 服务端不管——新侧行载荷同形状
    }


def tb91_row():
    # 卷35: ATB911='20' 必带(审核按它分支); ATB912='1' 暂存态
    return {"atb913": T_NAME, "atb911": "20", "aae011": "jiaq",
            "aae002": ISO_YM, "aae003": ISO_YM, "atb912": "1"}


def dump_rows(tag, atc, tb910, batch):
    out = []
    for tbl, where in (("tc90", f"atc900={atc}"),
                       ("tb91", f"atb910={tb910}"),
                       ("tc90_list", f"atc900={atc}"),
                       ("aa97", f"aaa970={batch}"),
                       ("tc93", f"atc900={atc}"),
                       ("tc08", f"atc900={atc}"),
                       ("tc90_yy", f"atc900={atc}")):
        v = sql(f"SELECT string_agg(row_to_json(t)::text, ' | ') FROM (SELECT * FROM {tbl} WHERE {where}) t")
        out.append(f"[{tag}] {tbl}: {v or '-'}")
    with open(os.path.join(os.path.dirname(os.path.dirname(os.path.abspath(__file__))),
                           "work", "bc06_pg_rowdump.txt"), "a", encoding="utf-8") as f:
        f.write("\n".join(out) + "\n")


def run_xian(name, proc_chk, proc_cfm, hu, newcol, synccol, statuscol, targets, new_val):
    batch = "2099" + hu[2:] + "0"   # 2099910/2099920/2099930/2099940 每险种唯一
    print(f"\n===== {name} (户头{hu} 批次{batch}) =====")
    # 预检: 人/批次键全零(失败残渣由入口预清兜底)
    for q, label in ((f"SELECT COUNT(*) FROM tc90 WHERE aac001={T_AAC001} OR aac002='{ID1}'", "人"),
                     (f"SELECT COUNT(*) FROM aa97 WHERE aaa970={batch}", "批次键")):
        v = sql(q)
        if v not in ("0", ""):
            rec(f"{name}-预检", False, f"{label}={v} 非空, 跳过该险种")
            return

    # ── TC-001 暂存(三 store 拆形状①: tc90+tb91 两 map POST) ──
    st, r = ia("REQ-tdba_cont-save", {"tc90": tc90_row(), "tb91": tb91_row()})
    atc = (r or {}).get("saved", {}).get("atc900")
    tb910 = (r or {}).get("saved", {}).get("atb910")
    r90r = sql(f"SELECT atb90r FROM tc90 WHERE atc900={atc}") if atc else None
    t912 = sql(f"SELECT atb912 FROM tb91 WHERE atb910={tb910}") if tb910 else None
    link = sql(f"SELECT atb91z||'/'||COALESCE((SELECT atb910 FROM tc90 WHERE atc900={atc})::text,'-') "
               f"FROM tb91 WHERE atb910={tb910}") if tb910 else "-"
    ok = st == 200 and bool(atc) and bool(tb910) and r90r == "1" and t912 == "1" and link == f"{atc}/{tb910}"
    rec(f"{name}-TC001暂存", ok, f"http={st} atc900={atc} atb910={tb910} atb90r={r90r} atb912={t912} 回链={link}")

    # ── TC-002 提交 ──
    st, r = ia("REQ-tdba_cont-commit", {"tc90": {"atc900": atc}, "tb91": {"atb910": tb910}})
    dbr = sql(f"SELECT atb90r FROM tc90 WHERE atc900={atc}")
    dbt = sql(f"SELECT atb912 FROM tb91 WHERE atb910={tb910}")
    ok = st == 200 and dbr == "2" and dbt == "2"
    rec(f"{name}-TC002提交", ok, f"http={st} atb90r={dbr} atb912={dbt}")

    # ── TC-003 审核(等价通道 doXinqCommit = COMFIRM_SH '20' 分支, 回执 023 口径) ──
    now = datetime.datetime.now().isoformat(timespec="seconds")
    st, r = ia("REQ-tdbf__dialog_xinqcl-commit",
               {"tc90": {"atc900": atc, "atb90r": "3", **BAE},
                "tb91": {"atb910": tb910, "atb912": "3", "bae036": now, **BAE}})
    dbr = sql(f"SELECT atb90r FROM tc90 WHERE atc900={atc}")
    dbt = sql(f"SELECT atb912 FROM tb91 WHERE atb910={tb910}")
    ok = st == 200 and dbr == "3" and dbt == "3"
    rec(f"{name}-TC003审核", ok, f"http={st} atb90r={dbr}(应3) atb912={dbt}(应3)")

    # ── AA97 批次行(aa97Store 保存通道 REQ-tdcl_dialog_mod-U, 拆形状②: 单行 map) ──
    st, r = ia("REQ-tdcl_dialog_mod-U",
               {"aa97": {"aaa970": int(batch), "aae001": int(ISO_YEAR), "aab001": UNIT_AAB001,
                         "aab004": UNIT_NAME, "aaa030": int(hu),
                         "aaa100": ISO_REG, "aaa110": ISO_REG, "aaa120": ISO_REG, "aaa130": ISO_REG,
                         "atc900": atc, "aac001": T_AAC001, "aac003": T_NAME, "aac002": ID1,
                         newcol: new_val}})
    n = sql(f"SELECT COUNT(*) FROM aa97 WHERE aaa970={batch}")
    saved = ((r or {}).get("saved") or {}).get("saved")
    ok = st == 200 and n == "1" and saved == "inserted"
    rec(f"{name}-AA97批次", ok, f"http={st} saved={saved} rows={n}")

    # ── 核对 ──
    st, r = proc(f"PKG_缴费基数.{proc_chk}", {"aae001": ISO_YEAR, "aaa030": hu, "aaa020": ISO_REG})
    s = sql(f"SELECT {statuscol} FROM aa97 WHERE aaa970={batch}")
    sync = sql(f"SELECT COALESCE({synccol},0)::text FROM aa97 WHERE aaa970={batch}").rstrip("0").rstrip(".")
    ok = st == 200 and (r or {}).get("appCode") == 1 and s == "3" and sync == "2800"
    rec(f"{name}-核对", ok, f"http={st} appCode={(r or {}).get('appCode')} {statuscol}={s}(应3) "
        f"{synccol}={sync}(应2800=TC90原值) marked={(r or {}).get('marked')}")

    # ── 确认 ──
    st, r = proc(f"PKG_缴费基数.{proc_cfm}",
                 {"aae001": ISO_YEAR, "aaa030": hu, "aaa020": ISO_REG,
                  "aae002": ISO_YM, "aae011": "jiaq"})
    vals = {t: sql(f"SELECT {t}::text FROM tc90 WHERE atc900={atc}") for t in targets}
    s2 = sql(f"SELECT {statuscol} FROM aa97 WHERE aaa970={batch}")
    ok = st == 200 and (r or {}).get("appCode") == 1 and s2 == "1" \
        and all(vals[t].rstrip("0").rstrip(".") == new_val for t in targets)
    rec(f"{name}-确认", ok, f"http={st} appCode={(r or {}).get('appCode')} TC90 {vals}(应{new_val}) "
        f"{statuscol}={s2}(应1) confirmed={(r or {}).get('confirmed')}")

    # 纪律 v2: 清场前全列行态导出
    dump_rows(name, atc, tb910, batch)

    # ── 清场(PRC_删除员工 移植腿 + W-A 备份证据; TB91 按旧套件口径 SQL 兜底) ──
    st, r = proc("PKG_人才派遣.PRC_删除员工", {"prm_atc900": str(atc)})
    left = sql(f"SELECT COUNT(*) FROM tc90 WHERE atc900={atc}")
    left_l = sql(f"SELECT COUNT(*) FROM tc90_list WHERE atc900={atc}")
    left_a = sql(f"SELECT COUNT(*) FROM aa97 WHERE atc900={atc} OR aaa030={hu}")
    bk = sql(f"SELECT table_name||':'||cnt FROM (SELECT table_name, COUNT(*) cnt FROM rcpq_del_backup "
             f"WHERE atc900={atc} GROUP BY table_name) t ORDER BY table_name")
    subprocess.run(["psql", PG, "-c", f"DELETE FROM tb91 WHERE atb910={tb910}"], capture_output=True)
    subprocess.run(["psql", PG, "-c", f"DELETE FROM tb91 WHERE atb913='{T_NAME}'"], capture_output=True)
    left91 = sql(f"SELECT COUNT(*) FROM tb91 WHERE atb913='{T_NAME}'")
    ok = st == 200 and (r or {}).get("appCode") == 1 and left == "0" and left_l == "0" \
        and left_a == "0" and left91 == "0" and "tc90:" in (bk or "") and "aa97:" in (bk or "")
    rec(f"{name}-清场", ok, f"appCode={(r or {}).get('appCode')} tc90={left} list={left_l} "
        f"aa97={left_a} tb91={left91} 备份={bk}")


def main():
    doc = open(os.path.abspath(__file__), encoding="utf-8").read()
    if "隔离月 209904" not in doc:
        print("隔离月声明缺失"); sys.exit(1)
    # 失败残渣预清(旧套件同手法: 上轮失败链不清场→重跑撞行)
    for stmt in (f"DELETE FROM tb91 WHERE atb913='{T_NAME}'",
                 f"DELETE FROM tc90 WHERE aac003='{T_NAME}' OR aac002='{ID1}'",
                 "DELETE FROM aa97 WHERE aaa970 IN (2099910,2099920,2099930,2099940)"):
        subprocess.run(["psql", PG, "-c", stmt], capture_output=True)
    for x in XIAN:
        run_xian(*x)
    # 备份表终态证据(W-A)
    total = sql("SELECT COUNT(*) FROM rcpq_del_backup")
    print(f"\nrcpq_del_backup 终态行数(W-A 备份证据): {total}")
    print(f"\n=== BC-06 基数确认族 新系统复跑: {len(PASS)} 过 / {len(FAIL)} 败 ===")
    sys.exit(1 if FAIL else 0)


if __name__ == "__main__":
    main()
