#!/usr/bin/env python3
"""BC-41 保险补退明细五子移植 门开探针三分支+BC-09 扩展腿 — 桥单 20261127-041。

镜像 scripts/db/SupProbeOld.java 的 RCPQ_TEST 旧侧实弹(同键同值):
  分支E 报错面: aae011='nouser41' → appCode=-1 '无法计算补退，原因为经办人信息不正确'
  分支Z 零操作面: 门关(atc90p='2') → appCode=1, TC07 零行
  分支C 产出面: 门开(atc90p='0', AAA201=209901<AAA203=209902) + AC10 两月 →
    TC07 一行 atc072='10'(窗=[AAA201,AAA203-1月]=仅209901, 旧侧实证同判):
    bba001=-100 bba003=-10 baa001=-50 baa003=-5 bba200=-110 baa200=-55 atc9eh=-165
  扩展腿 P4 结算消费: TC93 上月(209905)行+提取上月工资(209906)+费用结算(209906) →
    TC07 补退段消费 → TC93 atc93ba=-110(单位) atc93be=-55(个人), 全列 dump 对照。
隔离键: aab001/atb930=20990961, atb900=20990962, atc900=20990601, aae002=209906,
aac003='补退探针员卷41'。隔离月 209906=bc41(登记)。
用法: python3 scripts/e2e_bc41_supplement.py
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
UNIT, TB900, ATC = "20990961", "20990962", "20990601"
NAME, AAC001 = "补退探针员卷41", 998072
YM_EXT = "209906"
FAKE_REG = "209900000000"

PASS, FAIL = [], []


def rec(name, ok, detail=""):
    (PASS if ok else FAIL).append(name)
    print(("✅" if ok else "❌"), name, ("— " + detail if detail else ""))


def sql(q):
    r = subprocess.run(["psql", PG, "-tAc", q], capture_output=True, text=True)
    return (r.stdout or "").strip()


def exe(q, *a):
    args = []
    for v in a:
        args.append(str(v))
    q = q % tuple(args)
    r = subprocess.run(["psql", PG, "-c", q], capture_output=True, text=True)
    if r.returncode != 0:
        print("SQL_ERR:", (r.stderr or "").strip().splitlines()[:1], "::", q[:90])


def http(method, url, data=None):
    try:
        req = urllib.request.Request(url, method=method,
                                     data=json.dumps(data).encode() if data is not None else None,
                                     headers={"Content-Type": "application/json"})
        with urllib.request.urlopen(req, timeout=120) as r:
            return r.status, json.loads(r.read().decode("utf-8", "replace"))
    except urllib.error.HTTPError as e:
        try:
            return e.code, json.loads(e.read().decode("utf-8", "replace"))
        except Exception:
            return e.code, {}


def proc(name, body):
    return http("POST", APP + "/proc/" + quote(name), body)


def full_row(table, where):
    v = sql(f"SELECT COALESCE(MAX(row_to_json(t)::text),'-') FROM (SELECT * FROM {table} WHERE {where}) t")
    return None if v in ("-", "") else json.loads(v)


def dump(label, table, where):
    v = sql(f"SELECT COALESCE(MAX(row_to_json(t)::text),'-') FROM (SELECT * FROM {table} WHERE {where}) t")
    path = os.path.join(os.path.dirname(os.path.dirname(os.path.abspath(__file__))),
                        "work", "bc41_pg_rowdump.txt")
    with open(path, "a", encoding="utf-8") as f:
        f.write(f"[{label}] {table}: {v}\n")
    return None if v == "-" else json.loads(v)


def insert_full(table, row, overrides):
    cols = {}
    for k, v in row.items():
        cols[k] = v
    cols.update(overrides)
    names, vals = [], []
    for k, v in cols.items():
        names.append(k)
        if v is None:
            vals.append("NULL")
        elif isinstance(v, bool):
            vals.append("TRUE" if v else "FALSE")
        elif isinstance(v, (int, float)):
            vals.append(str(v))
        else:
            vals.append("'" + str(v).replace("'", "''") + "'")
    exe("INSERT INTO %s (%s) VALUES (%s)" % (table, ",".join(names), ",".join(vals)))


def tc07_rows():
    return sql(f"SELECT COUNT(*) FROM tc07 WHERE aae002={YM_EXT}::numeric AND atc900={ATC}::numeric")


def main():
    global ATC
    path = os.path.join(os.path.dirname(os.path.dirname(os.path.abspath(__file__))),
                        "work", "bc41_pg_rowdump.txt")
    open(path, "w").close()
    # ── 预清(隔离键, 与 SupProbeOld.java 同) ──
    for stmt in (f"DELETE FROM tc07 WHERE aae002={YM_EXT}::numeric AND atc900={ATC}::numeric",
                 f"DELETE FROM ac10 WHERE atc900={ATC}::numeric",
                 f"DELETE FROM tc90 WHERE atc900={ATC}::numeric",
                 f"DELETE FROM tb93 WHERE atb930={UNIT}::numeric",
                 f"DELETE FROM tb90 WHERE atb900={TB900} AND aab001={UNIT}::numeric",
                 f"DELETE FROM ab01 WHERE aab001={UNIT}::numeric",
                 f"DELETE FROM tc93 WHERE atb930={UNIT}::numeric",
                 "DELETE FROM ac10 WHERE id IN (2099060001,2099060002)",
                 f"DELETE FROM tb91 WHERE atb913='{NAME}'",
                 f"DELETE FROM tc90 WHERE aac003='{NAME}' OR aac002='220183199906018801'",
                 f"DELETE FROM aa94 WHERE aaa100='{FAKE_REG}'"):
        subprocess.run(["psql", PG, "-c", stmt], capture_output=True)

    # ── 夹具(与旧侧克隆源对齐: tc90 克隆 53031 → 身份列同源) ──
    exe("INSERT INTO tb93 (atb900, atb930, aab001, atb931) VALUES (%s, %s, %s, '补退探针单元卷41')",
        TB900, UNIT, UNIT)
    tb90 = full_row("tb90", "aab001=(SELECT aab001 FROM tb93 WHERE atb930=39737)")
    insert_full("tb90", tb90, {"atb900": int(TB900), "aab001": int(UNIT)})
    ab01 = full_row("ab01", "aab001=2239")
    insert_full("ab01", ab01, {"aab001": int(UNIT), "aab004": "补退探针单位卷41"})
    # 人: bc09 同款 tc001 拆形状链(53031 全克隆行携带脏关联列会引结算下游入雷区)
    ID1 = "220183199906018801"
    BAE = {"bae011": "jiaq", "bae017": "4101", "bae019": "贾悫",
           "bae020": "吉林省彩虹人才开发咨询服务有限公司", "bae022": "410100000000"}
    row90 = {
        "aac001": AAC001, "aac002": ID1, "aac003": NAME, "aac004": "2",
        "aac006": "1999-06-01", "atc901": "28", "aac011": "21", "aac009": "11",
        "aac005": "01", "aac012": "91", "aac013": "1", "aae005": "13000000000",
        "aae006": "长春市", "atc902": "长春市", "aae00p": "1", "aac008": "11",
        "atb930": int(UNIT), "atb900": int(TB900), "aab001": int(UNIT), "aab004": "补退探针单位卷41",
        "atc908": "工人", "atc909": "1", "atc90a": "1", "atc90x": "补退探针单元卷41",
        "atc90b": "2018-12-01", "atc90c": "2018-12-01", "atc90d": "2035-11-30",
        "atc90ax": "2035-12-31", "atc90f": "1", "atc90g": "1", "atc905": "2800",
        "aaa100": FAKE_REG, "aaa101": "吉林省重放区", "aaa110": FAKE_REG, "aaa111": "吉林省重放区",
        "aaa120": FAKE_REG, "aaa121": "吉林省重放区", "aaa130": FAKE_REG, "aaa131": "吉林省重放区",
        "aaa201": 209901, "aaa202": 209901, "aaa203": 209902, "aaa204": 209902,
        "aaa208": 209906, "aaa209": 209906,
        "aaa501": 209901, "aaa502": 209901, "aaa503": 209902, "aaa504": 209902,
        "aaa508": 209906, "aaa509": 209906,
        "aaa601": 209901, "aaa602": 209901, "aaa603": 209902, "aaa604": 209902,
        "aaa608": 209906, "aaa609": 209906,
        "aaa301": 2800, "aaa302": 2800, "aaa303": 2800, "aaa304": 2800,
        "aaa305": 2800, "aaa306": 2800,
        "aaa401": "1", "aaa402": "1", "aaa403": "1", "aaa404": "1",
        "aaa405": "1", "aaa407": "1", "aaa408": "1",
        "aae100": "1", "aae002": 209905, "aae003": 209912,
        "atc90p": "2", "atc90w": "2", "atc90pa": "2", "atc90pb": "2",
        "atc90ai": "0", "atc90at": "0", "atc90ah": "0",
        "atb90c": "0", "atb90a": "40", "atb90b": "0", "atc90zb": "1",
        "atb90s": "12:7", "atb90r": "1",
    }
    tb91 = {"atb913": NAME, "atb911": "20", "aae011": "jiaq",
            "aae002": 209905, "aae003": 209905, "atb912": "1"}
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
    if dbr != "3":
        rec("夹具-人", False, f"atc900={atc} atb90r={dbr}"); sys.exit(1)
    atc = int(atc)
    # 探针门旗标: 分支Z 需门关('2') → 分支C 置门开('0'); 窗口列与旧侧同值
    n = sql(f"SELECT COUNT(*) FROM tc90 WHERE atc900={atc} AND atb930={UNIT}::numeric")
    rec("夹具", n == "1", f"TB93+TB90+AB01+TC90 就位(tc001 链, atc900={atc}, atb90r=3)")
    ATC = str(atc)

    # ── 分支 E: 经办人错误 ──
    st, r = proc("PKG_保险补退.PRC_保险补退明细",
                 {"aae002": YM_EXT, "atc900": ATC, "aae011": "nouser41"})
    ok = st == 200 and (r or {}).get("appCode") == -1 and \
        (r or {}).get("errMsg") == "无法计算补退，原因为经办人信息不正确"
    rec("分支E 报错面(经办人)", ok, f"HTTP={st} appCode={(r or {}).get('appCode')} errMsg={(r or {}).get('errMsg')}")

    # ── 分支 Z: 门关零操作 ──
    st, r = proc("PKG_保险补退.PRC_保险补退明细",
                 {"aae002": YM_EXT, "atc900": ATC, "aae011": "jiaq"})
    n = tc07_rows()
    ok = st == 200 and (r or {}).get("appCode") == 1 and n == "0"
    rec("分支Z 零操作面(门关)", ok, f"HTTP={st} appCode={(r or {}).get('appCode')} TC07={n}(应0)")

    # ── 分支 C: 门开产出 ──
    exe("UPDATE tc90 SET atc90p='0' WHERE atc900=%s", ATC)
    exe("INSERT INTO ac10 (id, aac101, atc900, atc932, bba001, bba003, baa001, baa003) "
        "VALUES (2099060001, '1', %s, 209901, 100, 10, 50, 5)", ATC)
    exe("INSERT INTO ac10 (id, aac101, atc900, atc932, bba001, bba003, baa001, baa003) "
        "VALUES (2099060002, '1', %s, 209902, 60, 6, 30, 3)", ATC)
    st, r = proc("PKG_保险补退.PRC_保险补退明细",
                 {"aae002": YM_EXT, "atc900": ATC, "aae011": "jiaq"})
    n = tc07_rows()
    row = dump("分支C后-TC07产出(atc072=10)", "tc07",
               f"aae002={YM_EXT}::numeric AND atc900={ATC}::numeric")
    core_ok = row is not None and (
        str(row.get("aae003")) == "209901" and row.get("atc072") == "10" and
        row.get("atc073") == "5" and row.get("atc70x") == "1" and row.get("atc70y") == "1" and
        float(row.get("bba001")) == -100 and float(row.get("bba003")) == -10 and
        float(row.get("baa001")) == -50 and float(row.get("baa003")) == -5 and
        float(row.get("bba200")) == -110 and float(row.get("baa200")) == -55 and
        float(row.get("atc9eh")) == -165 and row.get("atc07z") == "0" and
        row.get("datasource") == "30" and
        row.get("aae011") == "jiaq" and row.get("aae017") == "4101")
    ok = st == 200 and (r or {}).get("appCode") == 1 and n == "1" and core_ok
    rec("分支C 产出面(门开)", ok, f"HTTP={st} appCode={(r or {}).get('appCode')} TC07={n}(应1, "
        f"窗=[AAA201,AAA203-1月]=209901单月, 与旧侧同判) 核心值={'✓' if core_ok else row}")
    if row is not None:
        # 夹具换 tc001 链后身份列随 tc90 行(不再对齐 53031 克隆); 断言身份列与合同行一致
        t90row = full_row("v_tc90_qpkg", f"atc900={ATC}")  # 视图行(模板同源)
        same_ident = (row.get("aaa001") == t90row.get("aaa001") and
                      row.get("aab004") == t90row.get("aab004") and
                      row.get("aaa030") == t90row.get("aaa030") and
                      row.get("aaa031") == t90row.get("aaa031"))
        rec("分支C 身份列与合同行一致(tc001 夹具语义)", same_ident,
            f"AAA001={row.get('aaa001')} AAB004={row.get('aab004')} AAA030={row.get('aaa030')}")

    # ── 扩展腿 P4: TC07 产出→结算差额消费 ──
    tc93 = full_row("tc93", "atc930=4545599")
    insert_full("tc93", tc93, {
        "atc930": int(UNIT), "atc931": 209905, "atc932": 209905,
        "aae001": 209905, "aae002": 209905, "atb930": int(UNIT),
        "atc900": int(ATC), "aae214": "1", "atc937": "1",
        "aac001": AAC001, "aac003": NAME, "aac002": "220183199906018801"})
    n = sql(f"SELECT COUNT(*) FROM tc93 WHERE atb930={UNIT}::numeric AND atc931=209905")
    rec("扩展腿-TC93上月", n == "1", f"209905 行={n}")
    for i, (code, rate) in enumerate([("01", 16), ("02", 0.5), ("03", 7), ("04", 0.5),
                                      ("05", 0.8), ("06", 8), ("07", 2), ("08", 0), ("12", 0)]):
        exe("INSERT INTO aa94 (aaa940, aaa941, aaa100, aaa101, aaa942, aaa943, aaa946, aaa947, aae011) "
            "VALUES (%s, %s, %s, '吉林省重放区', %s, 1, 209001, 209912, 'jiaq')",
            int(f"2099091{i + 1}"), code, FAKE_REG, rate)
    st, r = proc("PKG_费用结算.PRC_提取上月工资",
                 {"atb930": UNIT, "atc931": YM_EXT, "atc937": "1", "aae011": "jiaq"})
    ok = st == 200 and (r or {}).get("appCode") == 1
    rec("扩展腿-提取上月工资", ok, f"HTTP={st} appCode={(r or {}).get('appCode')} errMsg={(r or {}).get('errMsg')}")
    st, r = proc("PKG_费用结算.PRC_费用结算",
                 {"atb930": UNIT, "atc931": YM_EXT, "atc937": "1", "atc932": YM_EXT, "aae011": "jiaq"})
    ok = st == 200 and (r or {}).get("appCode") == 1
    rec("扩展腿-费用结算(门开人)", ok, f"HTTP={st} appCode={(r or {}).get('appCode')} errMsg={(r or {}).get('errMsg')}")
    row = dump("扩展腿后-TC93目标月(差额列消费)", "tc93",
               f"atb930={UNIT}::numeric AND atc931={YM_EXT}::numeric AND atc937='1' AND aae214='0'")
    consume_ok = row is not None and float(row.get("atc93ba")) == -110 and float(row.get("atc93be")) == -55
    rec("扩展腿-TC07差额消费(ATC93BA=-110/ATC93BE=-55)", consume_ok,
        f"atc93ba={row.get('atc93ba') if row else None} atc93be={row.get('atc93be') if row else None}")
    n_hist = int(sql(f"SELECT COUNT(*) FROM tc93 WHERE atb930={UNIT}::numeric AND aae013='补退历史'"))
    rec("扩展腿-补退历史补零行在册(aae013='补退历史')", n_hist >= 1,
        f"补行={n_hist}(脚本双结算 faithful: 提取内含结算+显式结算各跑一次名单子过程)")

    # ── 清场 ──
    for stmt in (f"DELETE FROM tc07 WHERE aae002={YM_EXT}::numeric AND atc900={ATC}::numeric",
                 f"DELETE FROM ac10 WHERE atc900={ATC}::numeric",
                 f"DELETE FROM tc93 WHERE atb930={UNIT}::numeric",
                 f"DELETE FROM tc90 WHERE atc900={ATC}::numeric",
                 f"DELETE FROM tb93 WHERE atb930={UNIT}::numeric",
                 f"DELETE FROM tb90 WHERE atb900={TB900} AND aab001={UNIT}::numeric",
                 f"DELETE FROM ab01 WHERE aab001={UNIT}::numeric",
                 f"DELETE FROM aa94 WHERE aaa100='{FAKE_REG}'",
                 "DELETE FROM ac10 WHERE id IN (2099060001,2099060002)",
                 f"DELETE FROM tb91 WHERE atb913='{NAME}'",
                 f"DELETE FROM tc90 WHERE aac003='{NAME}' OR aac002='220183199906018801'"):
        subprocess.run(["psql", PG, "-c", stmt], capture_output=True)
    left = sum(int(sql(q)) for q in (
        f"SELECT COUNT(*) FROM tc90 WHERE aac003='{NAME}'",
        f"SELECT COUNT(*) FROM ac10 WHERE atc900={ATC}",
        f"SELECT COUNT(*) FROM tc07 WHERE atc900={ATC}",
        f"SELECT COUNT(*) FROM tb93 WHERE atb930={UNIT}",
        f"SELECT COUNT(*) FROM tc93 WHERE atb930={UNIT}"))
    rec("清场", left == 0, f"残留={left}")

    print(f"\n=== BC-41 保险补退明细探针+扩展腿: {len(PASS)} 过 / {len(FAIL)} 败 ===")
    sys.exit(1 if FAIL else 0)


if __name__ == "__main__":
    main()
