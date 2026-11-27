#!/usr/bin/env python3
"""BC-09 派遣单位月费用结算链 新系统同链五腿复跑 — 桥单 20261127-034(任务书 031)。

镜像旧侧 rainbowpq/scripts/replay/tddd_settlement_test.py(卷38, 10/10 绿) 走新系统通道:
  夹具   = TB93(单元 20990901)+TB90(抄真行 39737 改造)+AB01(抄真行 2239 改造)+人(全链审核态,
           tc001 拆形状 POST, aab001/atb930=单元, 停缴年月=209804 卷39 险金清零门)+AA94 假区划
           费率 9 行(码 01~08+12)+TC93 上月行(抄真行 4545599 改造, aae214='1')
  腿A    = /proc PKG_费用结算.PRC_提取上月工资 {atb930,atc931,atc937,aae011}
           (源内自带 PRC_费用结算 调用 — 腿②行态已含计算列)
  腿B    = /proc PKG_费用结算.PRC_费用结算 {atb930,atc931,atc937,atc932,aae011}
           → 纪律 v2 全列行态 diff vs 031 附件旧侧行态(逐列, 序列/时间戳/审计时点列排除)
  腿C/D  = /proc PKG_费用结算.PRC_生成费用清单 lb=1/2 (aae214 '0'→'1'→'0' + TB96 自插头守卫)
  腿E    = /proc PKG_费用结算.PRC_撤销结算 → 目标月余行=0
  清场   = TC93/TB96/AA94/TB90/TB93/AB01 + 人(PKG_人才派遣.PRC_删除员工, TC93 先清避工资守卫)
隔离月 209905(登记表 docs/import-channel.md §6); 单元 20990901/人 998071 与旧侧同形状。
用法: python3 scripts/e2e_bc09_settle.py
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
UNIT, T_NAME, T_AAC001 = "20990901", "重放结算员", 998071
YM_PRIOR, YM_TGT, CI = "209904", "209905", "1"  # 上月必须相邻(源 prevYm 语义)
FAKE_REG = "209900000000"
AUD = "吉林省彩虹人才开发咨询服务有限公司"
BAE = {"bae011": "jiaq", "bae017": "4101", "bae019": "贾悫", "bae020": AUD, "bae022": "410100000000"}
OLD_DUMP = "/home/ubuntu/github/rewrite-bridge/done/20261127-031.attachment-bc09-rowdump.txt"
# 旧侧腿③行态中允许不同列: 主键/发号器域(atc930/baz001/baz002)、时点戳(aae036/aae236)、
# 过月标记历史残留(aae114 源逻辑两库同 '1' 不排)、备注(aae013 同判不排)
EXCLUDE_DIFF = {"atc930", "baz001", "baz002", "aae036", "aae236", "aae336", "atc90e",
                "atc900",  # 合同号序列域(隔离夹具各自配号)
                "atc931", "atc932", "aae001", "aae002", "aae114",  # 隔离月平移 209901→209905
                "atb930", "atb931", "aab001", "aab004", "aac001", "aac003", "aac002",
                # 结算单元/人员身份键(隔离作用域同形状不同值, 已由夹具断言钉住)
                }

PASS, FAIL = [], []


def rec(name, ok, detail=""):
    (PASS if ok else FAIL).append(name)
    print(("✅" if ok else "❌"), name, ("— " + detail if detail else ""))


def sql(q):
    r = subprocess.run(["psql", PG, "-tAc", q], capture_output=True, text=True)
    return (r.stdout or "").strip()


def lit(v):
    """串值一律加引号(row_to_json 已把数值列转 JSON 数字, 串值=varchar 语义, 不得 float 化——
    首轮 220100000000→'220100000000.0' 14 字符撑爆 varchar(12) 即此教训)"""
    if v is None:
        return "NULL"
    if isinstance(v, bool):
        return "TRUE" if v else "FALSE"
    if isinstance(v, (int, float)):
        return str(v)
    return "'" + str(v).replace("'", "''") + "'"


def exe(q, *a):
    """python 侧插值(psql CLI 无绑定参数); a 空时按原样执行"""
    if a:
        for x in a:
            q = q.replace("%s", lit(x), 1)
    r = subprocess.run(["psql", PG, "-tAc", q], capture_output=True, text=True)
    if r.returncode != 0:
        raise RuntimeError((r.stderr or "")[:200])
    return (r.stdout or "").strip()


def http(method, url, data=None):
    try:
        req = urllib.request.Request(url, method=method,
                                     data=json.dumps(data).encode() if data is not None else None,
                                     headers={"Content-Type": "application/json"})
        with urllib.request.urlopen(req, timeout=180) as r:
            return r.status, json.loads(r.read().decode("utf-8", "replace"))
    except urllib.error.HTTPError as e:
        try:
            return e.code, json.loads(e.read().decode("utf-8", "replace"))
        except Exception:
            return e.code, {}


def proc(name, body):
    return http("POST", APP + "/proc/" + quote(name), body)


W = [7, 9, 10, 5, 8, 4, 2, 1, 6, 3, 7, 9, 10, 5, 8, 4, 2]
CK = "10X98765432"


def id_full(base17):
    return base17 + CK[sum(int(c) * w for c, w in zip(base17, W)) % 11]


ID1 = id_full("22018319990101886")


def full_row(table, where):
    v = sql(f"SELECT COALESCE(MAX(row_to_json(t)::text),'-') FROM (SELECT * FROM {table} WHERE {where}) t")
    if v in ("-", ""):
        return None
    try:
        return json.loads(v)
    except Exception:
        return None


def dump(label, table, where):
    row = full_row(table, where)
    path = os.path.join(os.path.dirname(os.path.dirname(os.path.abspath(__file__))),
                        "work", "bc09_pg_rowdump.txt")
    with open(path, "a", encoding="utf-8") as f:
        f.write(f"[{label}] {table}: {json.dumps(row, ensure_ascii=False)}\n")
    return row


def insert_full(table, row, overrides):
    """整行抄数→改键→动态 INSERT(列交集)"""
    row = dict(row)
    row.update(overrides)
    keys = list(row.keys())
    placeholders = ",".join("%s" for _ in keys)
    q = f"INSERT INTO {table} ({','.join(keys)}) VALUES ({placeholders})"
    try:
        exe(q, *[row[k] for k in keys])
    except RuntimeError as e:
        import os as _os
        with open(_os.path.join(_os.path.dirname(_os.path.dirname(_os.path.abspath(__file__))),
                                "work", "bc09_fail.sql"), "w", encoding="utf-8") as f:
            f.write(q + "\n\n--values:\n" + "\n".join(f"{k}={row[k]!r}" for k in keys))
        raise RuntimeError(f"{table} 插入失败: {e}")


def norm(v):
    if v == "" :
        v = None  # 旧侧 dump NULL→''、新侧 row_to_json 保持 null, 等价化
    if isinstance(v, float):
        return f"{v:g}"
    if isinstance(v, str):
        try:
            f = float(v)
            return f"{f:g}"
        except ValueError:
            return v
    if isinstance(v, int):
        return str(v)
    return v


def main():
    open(os.path.join(os.path.dirname(os.path.dirname(os.path.abspath(__file__))),
                      "work", "bc09_pg_rowdump.txt"), "w").close()
    # ── 预检+残渣预清(旧套件同手法) ──
    for stmt in (
        f"DELETE FROM tc93 WHERE atb930={UNIT}::numeric",
        "DELETE FROM tb96 WHERE atb930=" + UNIT,
        "DELETE FROM aa94 WHERE aaa100='" + FAKE_REG + "'",
        f"DELETE FROM tb90 WHERE atb900=20990902 AND aab001={UNIT}::numeric",
        f"DELETE FROM tb93 WHERE atb930={UNIT}::numeric",
        f"DELETE FROM ab01 WHERE aab001={UNIT}::numeric",
        f"DELETE FROM tb91 WHERE atb913='{T_NAME}'",
        f"DELETE FROM tc90 WHERE aac003='{T_NAME}' OR aac002='{ID1}'"):
        subprocess.run(["psql", PG, "-c", stmt], capture_output=True)
    for q, label in ((f"SELECT COUNT(*) FROM tb93 WHERE atb930={UNIT}::numeric", "TB93 单元键"),
                     (f"SELECT COUNT(*) FROM tc90 WHERE aac001={T_AAC001}", "aac001")):
        v = sql(q)
        if v not in ("0", ""):
            rec("预检", False, f"{label}={v} 非空"); sys.exit(1)

    # ── ① 夹具 ──
    exe("INSERT INTO tb93 (atb900, atb930, aab001, atb931) VALUES (20990902, %s::numeric, %s::numeric, '重放结算单元')",
        UNIT, UNIT)
    tb90 = full_row("tb90", "aab001=(SELECT aab001 FROM tb93 WHERE atb930=39737)")
    insert_full("tb90", tb90, {"atb900": 20990902, "aab001": int(UNIT)})
    ab01 = full_row("ab01", "aab001=2239")
    insert_full("ab01", ab01, {"aab001": int(UNIT), "aab004": "重放结算单位"})
    rec("夹具-单位", sql(f"SELECT COUNT(*) FROM ab01 WHERE aab001={UNIT}::numeric") == "1",
        "TB93+TB90+AB01 就位")

    # 人: tc001 拆形状(tdba_cont-save) → tc002(commit) → tc003(doxinq 审核)
    stop = {f"aaa{n}": YM_PRIOR for n in (203, 204, 208, 209, 503, 504, 508, 509, 603, 604, 608, 609)}
    row90 = {
        "aac001": T_AAC001, "aac002": ID1, "aac003": T_NAME, "aac004": "2",
        "aac006": "1999-01-01", "atc901": "28", "aac011": "21", "aac009": "11",
        "aac005": "01", "aac012": "91", "aac013": "1", "aae005": "13000000000",
        "aae006": "长春市", "atc902": "长春市", "aae00p": "5", "aac008": "27",
        "atb930": int(UNIT), "atb900": 58332, "aab001": int(UNIT), "aab004": "重放结算单位",
        "atc908": "工人", "atc909": "1", "atc90a": "1", "atc90x": "重放结算单元",
        "atc90b": "2018-12-01", "atc90c": "2018-12-01", "atc90d": "2035-11-30",
        "atc90ax": "2035-12-31", "atc90f": "1", "atc90g": "1", "atc905": "2800",
        "aaa100": FAKE_REG, "aaa101": "吉林省重放区", "aaa110": FAKE_REG, "aaa111": "吉林省重放区",
        "aaa120": FAKE_REG, "aaa121": "吉林省重放区", "aaa130": FAKE_REG, "aaa131": "吉林省重放区",
        "aaa201": YM_TGT, "aaa202": YM_TGT, "aaa203": YM_PRIOR, "aaa204": YM_PRIOR,
        "aaa208": YM_PRIOR, "aaa209": YM_PRIOR,
        "aaa501": YM_TGT, "aaa502": YM_TGT, "aaa503": YM_PRIOR, "aaa504": YM_PRIOR,
        "aaa508": YM_PRIOR, "aaa509": YM_PRIOR,
        "aaa601": YM_TGT, "aaa602": YM_TGT, "aaa603": YM_PRIOR, "aaa604": YM_PRIOR,
        "aaa608": YM_PRIOR, "aaa609": YM_PRIOR,
        "aaa301": 2800, "aaa302": 2800, "aaa303": 2800, "aaa304": 2800,
        "aaa305": 2800, "aaa306": 2800,
        "aaa401": "1", "aaa402": "1", "aaa403": "1", "aaa404": "1",
        "aaa405": "1", "aaa407": "1", "aaa408": "1",
        "aae100": "1", "aae002": YM_TGT, "aae003": YM_TGT,
        "atc90p": "2", "atc90w": "2", "atc90pa": "2", "atc90pb": "2",
        "atc90ai": "0", "atc90at": "0", "atc90ah": "0",
        "atb90c": "0", "atb90a": "40", "atb90b": "0", "atc90zb": "1",
        "atb90s": "12:7", "atb90r": "1",
    }
    tb91 = {"atb913": T_NAME, "atb911": "20", "aae011": "jiaq",
            "aae002": YM_TGT, "aae003": YM_TGT, "atb912": "1"}
    st, r = http("POST", APP + "/interaction/" + quote("REQ-tdba_cont-save"),
                 {"tc90": row90, "tb91": tb91})
    saved = (r or {}).get("saved") or {}
    atc, tb910 = saved.get("atc900"), saved.get("atb910")
    if not (st == 200 and atc and tb910):
        rec("夹具-人TC001", False, f"http={st} {r}"); sys.exit(1)
    st, r = http("POST", APP + "/interaction/" + quote("REQ-tdba_cont-commit"),
                 {"tc90": {"atc900": atc}, "tb91": {"atb910": tb910}})
    now = datetime.datetime.now().isoformat(timespec="seconds")
    st, r = http("POST", APP + "/interaction/" + quote("REQ-tdbf__dialog_xinqcl-commit"),
                 {"tc90": {"atc900": atc, "atb90r": "3", **BAE},
                  "tb91": {"atb910": tb910, "atb912": "3", "bae036": now, **BAE}})
    dbr = sql(f"SELECT atb90r FROM tc90 WHERE atc900={atc}")
    rec("夹具-人", dbr == "3", f"atc900={atc} atb90r={dbr}(审核通过态)")

    for i, (code, rate) in enumerate([("01", 16), ("02", 0.5), ("03", 7), ("04", 0.5),
                                      ("05", 0.8), ("06", 8), ("07", 2), ("08", 0), ("12", 0)]):
        exe("INSERT INTO aa94 (aaa940, aaa941, aaa100, aaa101, aaa942, aaa943, aaa946, aaa947, aae011) "
            "VALUES (%s, %s, %s, '吉林省重放区', %s, 1, 209001, 209912, 'jiaq')",
            int(f"2099090{i + 1}"), code, FAKE_REG, rate)
    n94 = sql(f"SELECT COUNT(*) FROM aa94 WHERE aaa100='{FAKE_REG}'")
    rec("夹具-费率", n94 == "9", f"AA94 假区划费率行={n94}(应9)")

    tc93 = full_row("tc93", "atc930=4545599")
    insert_full("tc93", tc93, {
        "atc930": int(UNIT), "atc931": int(YM_PRIOR), "atc932": int(YM_PRIOR),
        "aae001": int(YM_PRIOR), "aae002": int(YM_PRIOR), "atb930": int(UNIT),
        "atc900": atc, "aae214": "1", "atc937": CI,
        "aac001": T_AAC001, "aac003": T_NAME, "aac002": ID1})
    n = sql(f"SELECT COUNT(*) FROM tc93 WHERE atb930={UNIT}::numeric AND atc931={YM_PRIOR}::numeric")
    rec("夹具-TC93上月", n == "1", f"{YM_PRIOR} 行={n}(atc930={UNIT})")
    dump("夹具输入-TC93上月(计算输入)", "tc93", f"atb930={UNIT}::numeric AND atc931={YM_PRIOR}::numeric")
    dump("夹具输入-TC90合同(费率/基数输入)", "tc90", f"atc900={atc}")

    # ── ② 腿A 提取上月工资 ──
    st, r = proc("PKG_费用结算.PRC_提取上月工资",
                 {"atb930": UNIT, "atc931": YM_TGT, "atc937": CI, "aae011": "jiaq"})
    n_new = sql(f"SELECT COUNT(*) FROM tc93 WHERE atb930={UNIT}::numeric AND atc931={YM_TGT}::numeric AND aae214='0'")
    ok = st == 200 and (r or {}).get("appCode") == 1 and n_new == "1"
    rec("腿A 提取上月工资", ok, f"HTTP={st} appCode={(r or {}).get('appCode')} errMsg={(r or {}).get('errMsg')} "
        f"目标月未确认行={n_new}(应1)")
    dump("腿A提取后-TC93目标月", "tc93", f"atb930={UNIT}::numeric AND atc931={YM_TGT}::numeric")

    # ── ③ 腿B 费用结算(主事件+纪律 v2 逐列 diff vs 旧侧附件) ──
    st, r = proc("PKG_费用结算.PRC_费用结算",
                 {"atb930": UNIT, "atc931": YM_TGT, "atc937": CI, "atc932": YM_TGT, "aae011": "jiaq"})
    ok = st == 200 and (r or {}).get("appCode") == 1
    rec("腿B 费用结算", ok, f"HTTP={st} appCode={(r or {}).get('appCode')} errMsg={(r or {}).get('errMsg')}")
    new_row = dump("腿B结算后-TC93目标月(全列)", "tc93",
                   f"atb930={UNIT}::numeric AND atc931={YM_TGT}::numeric AND atc937='{CI}' AND aae214='0'")
    if new_row is None:
        rec("腿B 行态", False, "结算后目标月行缺失"); sys.exit(1)
    # 旧侧腿③行态解析+逐列对照
    old_row = None
    for line in open(OLD_DUMP, encoding="utf-8"):
        if line.startswith("[腿③结算后-TC93目标月"):
            old_row = json.loads(line.split(": ", 1)[1])
    diffs = []
    if old_row and new_row:
        for k, ov in old_row.items():
            if k in EXCLUDE_DIFF or k not in new_row:
                continue
            nv = new_row.get(k)
            if norm(ov) != norm(nv if not isinstance(nv, str) else nv):
                if isinstance(nv, (int, float)) or (isinstance(nv, str) and nv.replace(".", "").replace("-", "").isdigit()):
                    if float(ov or 0) == float(nv or 0):
                        continue
                diffs.append((k, ov, nv))
    rec("腿B 全列 diff(vs 031 附件腿③)", old_row is not None and not diffs,
        f"对照列数={len(old_row) if old_row else 0}, 差异={diffs[:12]}")
    fee = {k: new_row.get(k) if new_row else None for k in
           ("atc93aa", "atc93ab", "atc93s", "bba001", "bba002", "baa200", "atc93d", "atc93ad")}
    has_fee = any(float(new_row.get(k) or 0) > 0 for k in ("bba001", "bba002", "baa200")) if new_row else False
    rec("腿B 计算列指纹", has_fee and norm(fee.get("bba001")) == "448" and
        norm(fee.get("atc93aa")) == "3088", f"计算列={fee} 单位缴非零={'✓' if has_fee else '✗'}")

    # ── ④ 腿C/D 清单确认/撤销 ──
    st, r = proc("PKG_费用结算.PRC_生成费用清单",
                 {"atb930": UNIT, "atc931": YM_TGT, "atc937": CI, "aae002": YM_TGT,
                  "lb": "1", "aae011": "jiaq"})
    a214 = sql(f"SELECT aae214 FROM tc93 WHERE atb930={UNIT}::numeric AND atc931={YM_TGT}::numeric")
    n96 = sql(f"SELECT COUNT(*) FROM tb96 WHERE atb930={UNIT}::numeric")
    ok = st == 200 and (r or {}).get("appCode") == 1 and a214 == "1" and n96 == "1"
    rec("腿C 清单确认(LB=1)", ok, f"HTTP={st} appCode={(r or {}).get('appCode')} errMsg={(r or {}).get('errMsg')} "
        f"TC93.AAE214={a214}(应'1') TB96自插头={n96}(应1)")
    dump("腿C确认后-TC93目标月(aae214=1)", "tc93", f"atb930={UNIT}::numeric AND atc931={YM_TGT}::numeric")
    dump("腿C确认后-TB96清单头(过程自插)", "tb96", f"atb930={UNIT}::numeric")
    st, r = proc("PKG_费用结算.PRC_生成费用清单",
                 {"atb930": UNIT, "atc931": YM_TGT, "atc937": CI, "aae002": YM_TGT,
                  "lb": "2", "aae011": "jiaq"})
    a214 = sql(f"SELECT aae214 FROM tc93 WHERE atb930={UNIT}::numeric AND atc931={YM_TGT}::numeric")
    rec("腿D 清单撤销(LB=2)", st == 200 and (r or {}).get("appCode") == 1 and a214 == "0",
        f"HTTP={st} appCode={(r or {}).get('appCode')} errMsg={(r or {}).get('errMsg')} TC93.AAE214={a214}(应回'0')")

    # ── ⑤ 腿E 撤销结算 ──
    st, r = proc("PKG_费用结算.PRC_撤销结算",
                 {"atb930": UNIT, "atc931": YM_TGT, "atc937": CI, "atc932": YM_TGT})
    n_left = sql(f"SELECT COUNT(*) FROM tc93 WHERE atb930={UNIT}::numeric AND atc931={YM_TGT}::numeric")
    rec("腿E 撤销结算", st == 200 and (r or {}).get("appCode") == 1 and n_left == "0",
        f"HTTP={st} appCode={(r or {}).get('appCode')} errMsg={(r or {}).get('errMsg')} 目标月余行={n_left}(应0)")

    # ── 清场(TC93 先清避 PRC_删除员工 工资守卫; 人走移植过程) ──
    for stmt in (f"DELETE FROM tc93 WHERE atb930={UNIT}::numeric",
                 f"DELETE FROM tb96 WHERE atb930={UNIT}::numeric",
                 f"DELETE FROM aa94 WHERE aaa100='{FAKE_REG}'",
                 f"DELETE FROM tb90 WHERE atb900=20990902 AND aab001={UNIT}::numeric",
                 f"DELETE FROM tb93 WHERE atb930={UNIT}::numeric",
                 f"DELETE FROM ab01 WHERE aab001={UNIT}::numeric"):
        subprocess.run(["psql", PG, "-c", stmt], capture_output=True)
    st, r = proc("PKG_人才派遣.PRC_删除员工", {"prm_atc900": str(atc)})
    subprocess.run(["psql", PG, "-c", f"DELETE FROM tb91 WHERE atb910={tb910}"], capture_output=True)
    subprocess.run(["psql", PG, "-c", f"DELETE FROM tb91 WHERE atb913='{T_NAME}'"], capture_output=True)
    left = sql(f"SELECT COUNT(*) FROM tc93 WHERE atb930={UNIT}::numeric")
    left2 = sql(f"SELECT COUNT(*) FROM tc90 WHERE aac001={T_AAC001} OR aac002='{ID1}'")
    left3 = sql(f"SELECT COUNT(*) FROM tb91 WHERE atb913='{T_NAME}'")
    rec("清场", left == "0" and left2 == "0" and left3 == "0",
        f"TC93 残={left} 人残={left2} TB91 残={left3} (删除员工 appCode={(r or {}).get('appCode')})")

    print(f"\n=== BC-09 结算链 新系统复跑: {len(PASS)} 过 / {len(FAIL)} 败 ===")
    sys.exit(1 if FAIL else 0)


if __name__ == "__main__":
    main()
