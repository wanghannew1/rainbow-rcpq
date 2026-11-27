#!/usr/bin/env python3
"""BC-45 花名册移植 新侧实弹(桥单 20261127-045): 隔离键同 041 探针, /proc
PKG_工资发放.PRC_工资花名册 → 造册行与旧侧(bc45_roster_old.txt)逐字段同判。
用法: python3 scripts/e2e_bc45_roster.py"""
import datetime
import json
import subprocess
import sys
import urllib.error
import urllib.request
from urllib.parse import quote

exec(open("scripts/e2e_bc41_supplement.py").read().split("def main()")[0])

PASS, FAIL2 = [], []
def rec2(name, ok, detail=""):
    (PASS if ok else FAIL2).append(name)
    print(("✅" if ok else "❌"), name, ("— " + detail if detail else ""))

def main():
    for stmt in (f"DELETE FROM tc93 WHERE atb930={UNIT}::numeric",
                 f"DELETE FROM tb93 WHERE atb930={UNIT}::numeric",
                 f"DELETE FROM tb90 WHERE atb900={TB900} AND aab001={UNIT}::numeric",
                 f"DELETE FROM ab01 WHERE aab001={UNIT}::numeric",
                 f"DELETE FROM tc90 WHERE aac003='{NAME}' OR aac002='220183199906018801'",
                 f"DELETE FROM tb91 WHERE atb913='{NAME}'"):
        subprocess.run(["psql", PG, "-c", stmt], capture_output=True)
    exe("INSERT INTO tb93 (atb900, atb930, aab001, atb931) VALUES (%s, %s, %s, '补退探针单元卷41')", TB900, UNIT, UNIT)
    insert_full("tb90", full_row("tb90", "aab001=(SELECT aab001 FROM tb93 WHERE atb930=39737)"),
                {"atb900": int(TB900), "aab001": int(UNIT)})
    insert_full("ab01", full_row("ab01", "aab001=2239"), {"aab001": int(UNIT), "aab004": "补退探针单位卷41"})
    BAE = {"bae011": "jiaq", "bae017": "4101", "bae019": "贾悫",
           "bae020": "吉林省彩虹人才开发咨询服务有限公司", "bae022": "410100000000"}
    row90 = {"aac001": AAC001, "aac002": "220183199906018801", "aac003": NAME, "aac004": "2",
        "aac006": "1999-06-01", "atc901": "28", "aac011": "21", "aac009": "11", "aac005": "01",
        "aac012": "91", "aac013": "1", "aae005": "13000000000", "aae006": "长春市", "atc902": "长春市",
        "aae00p": "1", "aac008": "11", "atb930": int(UNIT), "atb900": int(TB900), "aab001": int(UNIT),
        "aab004": "补退探针单位卷41", "atc908": "工人", "atc909": "1", "atc90a": "1", "atc90x": "补退探针单元卷41",
        "atc90b": "2018-12-01", "atc90c": "2018-12-01", "atc90d": "2035-11-30", "atc90ax": "2035-12-31",
        "atc90f": "1", "atc90g": "1", "atc905": "2800", "aaa100": FAKE_REG, "aaa101": "吉林省重放区",
        "aaa110": FAKE_REG, "aaa111": "吉林省重放区", "aaa120": FAKE_REG, "aaa121": "吉林省重放区",
        "aaa130": FAKE_REG, "aaa131": "吉林省重放区", "aaa201": 209901, "aaa202": 209901,
        "aaa203": 209902, "aaa204": 209902, "aaa208": 209906, "aaa209": 209906,
        "aaa501": 209901, "aaa502": 209901, "aaa503": 209902, "aaa504": 209902, "aaa508": 209906, "aaa509": 209906,
        "aaa601": 209901, "aaa602": 209901, "aaa603": 209902, "aaa604": 209902, "aaa608": 209906, "aaa609": 209906,
        "aaa301": 2800, "aaa302": 2800, "aaa303": 2800, "aaa304": 2800, "aaa305": 2800, "aaa306": 2800,
        "aaa401": "1", "aaa402": "1", "aaa403": "1", "aaa404": "1", "aaa405": "1", "aaa407": "1", "aaa408": "1",
        "aae100": "1", "aae002": 209905, "aae003": 209912, "atc90p": "2", "atc90w": "2", "atc90pa": "2",
        "atc90pb": "2", "atc90ai": "0", "atc90at": "0", "atc90ah": "0", "atb90c": "0", "atb90a": "40",
        "atb90b": "0", "atc90zb": "1", "atb90s": "12:7", "atb90r": "1"}
    tb91 = {"atb913": NAME, "atb911": "20", "aae011": "jiaq", "aae002": 209905, "aae003": 209905, "atb912": "1"}
    st, r = http("POST", APP + "/interaction/" + quote("REQ-tdba_cont-save"), {"tc90": row90, "tb91": tb91})
    saved = (r or {}).get("saved") or {}
    atc, tb910 = saved.get("atc900"), saved.get("atb910")
    http("POST", APP + "/interaction/" + quote("REQ-tdba_cont-commit"), {"tc90": {"atc900": atc}, "tb91": {"atb910": tb910}})
    now = datetime.datetime.now().isoformat(timespec="seconds")
    http("POST", APP + "/interaction/" + quote("REQ-tdbf__dialog_xinqcl-commit"),
         {"tc90": {"atc900": atc, "atb90r": "3", **BAE}, "tb91": {"atb910": tb910, "atb912": "3", "bae036": now, **BAE}})
    dbr = sql(f"SELECT atb90r FROM tc90 WHERE atc900={atc}")
    rec2("夹具", dbr == "3", f"atc900={atc} atb90r={dbr}")
    # /proc 花名册直调(空单元造册)
    st, r = proc("PKG_工资发放.PRC_工资花名册", {"atb930": UNIT, "atc931": YM_EXT, "atc937": "1", "aae011": "jiaq"})
    ok = st == 200 and (r or {}).get("appCode") == 1
    rec2("/proc 花名册可调", ok, f"HTTP={st} {(r or {}).get('errMsg')}")
    row = full_row("tc93", f"atb930={UNIT}::numeric AND atc931={YM_EXT}::numeric")
    same = row is not None and (
        str(row.get("atc900")) == str(atc) and row.get("atc937") == "1" and
        row.get("atc93n") == "1" and row.get("atc93g") == "0" and row.get("atc93p") == "0" and
        row.get("atc93w") == "0" and row.get("aae114") == "0" and row.get("aae214") == "0" and
        row.get("aae314") == "0" and row.get("aae013") == "导出名册生成" and
        str(row.get("aae002")) == YM_EXT and row.get("atc93u") is None and
        float(row.get("atc93aa") or 0) == 0.0 and float(row.get("atc93x") or 0) == 0.0 and
        float(row.get("atb90a") or 0) == 0.0)
    rec2("造册行与旧侧逐字段同判", same, f"aae013={row.get('aae013')} atc93n={row.get('atc93n')} aae002={row.get('aae002')} atc93aa={row.get('atc93aa')}")
    # 清场
    for stmt in (f"DELETE FROM tc93 WHERE atb930={UNIT}::numeric",
                 f"DELETE FROM tb93 WHERE atb930={UNIT}::numeric",
                 f"DELETE FROM tb90 WHERE atb900={TB900} AND aab001={UNIT}::numeric",
                 f"DELETE FROM ab01 WHERE aab001={UNIT}::numeric",
                 f"DELETE FROM tc90 WHERE aac003='{NAME}'",
                 f"DELETE FROM tb91 WHERE atb913='{NAME}'"):
        subprocess.run(["psql", PG, "-c", stmt], capture_output=True)
    left = sum(int(sql(q)) for q in (
        f"SELECT COUNT(*) FROM tc93 WHERE atb930={UNIT}",
        f"SELECT COUNT(*) FROM tc90 WHERE aac003='{NAME}'",
        f"SELECT COUNT(*) FROM tb93 WHERE atb930={UNIT}"))
    rec2("清场", left == 0, f"残留={left}")
    print(f"\n=== BC-45 花名册探针: {len(PASS)} 过 / {len(FAIL2)} 败 ===")
    sys.exit(1 if FAIL2 else 0)

if __name__ == "__main__":
    main()
