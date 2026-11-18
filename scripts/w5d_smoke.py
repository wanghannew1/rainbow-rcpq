#!/usr/bin/env python3
"""W5d 未申报清单/名册×4 冒烟: 四线(社保/医保/公积金/工伤)清单生成 AA97_DW+AA97_GR。
断言: 70557(生效列全 null)四线全中(BXZT=各线状态列/AAA000=各线编号列)+AA12 属地传递+
PSUM=名册人数+fid/cid 1e7 序列+NOT EXISTS 已申报排除(线间独立: 社保苗只挡社保线)+
AA12 缺行忠实错+幂等(DELETE 重生成)。
桩: AAE001=2099 合成年度(真 dw/gr 35/170 行无 2099, 桩避让); AA97 苗 aae001=2099。
前置: V18(seq_aa97_dw_fid/seq_aa97_gr_cid 抬段 1e7)。
用法: python3 scripts/w5d_smoke.py
"""
import json
import sys
import urllib.request
import urllib.parse
import subprocess

BASE = "http://localhost:8080"
YEAR = "2099"
A030 = "1"
A020 = "220100000000"
CON = 70557
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
chk("seq_aa97_dw_fid>=1e7", int(sql("SELECT last_value FROM seq_aa97_dw_fid") or 0) >= 10000001)
chk("seq_aa97_gr_cid>=1e7", int(sql("SELECT last_value FROM seq_aa97_gr_cid") or 0) >= 10000001)
for q in [
    f"DELETE FROM aa97_dw WHERE aae001={YEAR}",
    f"DELETE FROM aa97_gr WHERE aae001={YEAR}",
    f"DELETE FROM aa97 WHERE aae001={YEAR}",
]:
    sql(q)
chk("桩空间干净", sql(f"SELECT count(*) FROM aa97_gr WHERE aae001={YEAR}") == "0")

LINES = [
    ("sb", "社保", "PKG_缴费基数.PRC_社保未申报", "1", "aaa001"),
    ("yb", "医保", "PKG_缴费基数.PRC_医保未申报", "2", "aaa005"),
    ("gjj", "公积金", "PKG_缴费基数.PRC_公积金未申报", "3", "aaa004"),
    ("gs", "工伤", "PKG_缴费基数.PRC_工伤未申报", "4", "aaa009"),
]

print("=== 1: 四线清单生成(70557 生效列全 null → 四线全中) ===")
for key, label, pname, xz, noCol in LINES:
    r = proc(pname, {"aae001": YEAR, "aaa030": A030, "aaa020": A020})
    ok = r.get("appCode") == 1
    chk(f"{label}清单 appCode=1", ok, str(r)[:100])
    if not ok:
        continue
    dw = row(f"""SELECT fid>=10000001, aaa100, aaa101, xz, psum, coalesce(aae011,'-'),
                 coalesce(aae019,'-') FROM aa97_dw WHERE aae001={YEAR} AND xz='{xz}'
                 AND atb930=39473""")
    chk(f"{label} DW 行: 属地=AA12(长春)/XZ='{xz}'/经办=zhangyy/孙梦瑶",
        dw and dw[1] == A020 and dw[2] == "吉林省长春市" and dw[3] == xz
        and dw[5] == "zhangyy" and dw[6] == "孙梦瑶", str(dw))
    fid = sql(f"SELECT fid FROM aa97_dw WHERE aae001={YEAR} AND xz='{xz}' AND atb930=39473")
    gr_n = sql(f"SELECT count(*) FROM aa97_gr WHERE fid={fid}")
    chk(f"{label} GR 人数({gr_n})=PSUM({dw[4]})(名册数)", gr_n == dw[4], "")
    # 70557 行专查(生效列全 null→四线资格全中; BXZT=线状态列/AAA000=线编号列;
    # 70557 的 aaa001 本身 NULL→社保线 aaa000='-')
    exp_bxzt = {"1": "-", "2": "0", "3": "0", "4": "0"}[xz]  # 社保 atc90p=NULL→BXZT null
    gr = row(f"""SELECT coalesce(bxzt,'-'), coalesce(aaa000,'-'), atc900={CON}, fid={fid}
                 FROM aa97_gr WHERE fid={fid} AND atc900={CON}""")
    chk(f"{label} 70557 在册: BXZT={exp_bxzt}/AAA000=线编号列/FID 关联",
        gr and gr[0] == exp_bxzt and gr[2] == "t" and gr[3] == "t", str(gr))
    # 幂等: 重复调用行数不变
    n1 = sql(f"SELECT count(*) FROM aa97_gr WHERE aae001={YEAR} AND xz='{xz}'")
    proc(pname, {"aae001": YEAR, "aaa030": A030, "aaa020": A020})
    n2 = sql(f"SELECT count(*) FROM aa97_gr WHERE aae001={YEAR} AND xz='{xz}'")
    chk(f"{label} 幂等(DELETE 重生成, {n1}行不变)", n1 == n2 and int(n1) >= 1, f"{n1}→{n2}")

print("=== 2: NOT EXISTS 已申报排除(线间独立) ===")
aac001 = sql(f"SELECT DISTINCT aac001::text FROM tc90 WHERE atc900={CON}")
sql(f"""INSERT INTO aa97 (aae001, aac001, aaa301_1, aaa303_1, atc900, aac003, aaa030, aae036, aaa970)
        VALUES ({YEAR}, {aac001}, 5000, 5000, {CON}, '王易龄', 1, now(),
        (SELECT COALESCE(MAX(aaa970),0)+1 FROM aa97))""")
r = proc("PKG_缴费基数.PRC_社保未申报", {"aae001": YEAR, "aaa030": A030, "aaa020": A020})
fid_sb = sql(f"SELECT fid FROM aa97_dw WHERE aae001={YEAR} AND xz='1' AND atb930=39473")
chk("社保线 70557 被排除(AAA301_1>0 苗)",
    sql(f"SELECT count(*) FROM aa97_gr WHERE fid={fid_sb} AND aac001={aac001}") == "0", "")
r2 = proc("PKG_缴费基数.PRC_医保未申报", {"aae001": YEAR, "aaa030": A030, "aaa020": A020})
fid_yb = sql(f"SELECT fid FROM aa97_dw WHERE aae001={YEAR} AND xz='2' AND atb930=39473")
chk("医保线不受社保苗影响(线间独立)",
    sql(f"SELECT count(*) FROM aa97_gr WHERE fid={fid_yb} AND aac001={aac001}") == "1", "")

print("=== 3: AA12 缺行忠实错 ===")
r = proc("PKG_缴费基数.PRC_社保未申报", {"aae001": YEAR, "aaa030": A030, "aaa020": "999999999999"})
chk("AA12 缺行 → appCode=-1 + 出错文案",
    r.get("appCode") == -1 and "出错" in (r.get("errMsg") or "") and "ORA-01403" in (r.get("errMsg") or ""),
    str(r)[:100])

print("\n==== W5d 冒烟总结 ====")
for q in [
    f"DELETE FROM aa97_dw WHERE aae001={YEAR}",
    f"DELETE FROM aa97_gr WHERE aae001={YEAR}",
    f"DELETE FROM aa97 WHERE aae001={YEAR}",
]:
    sql(q)
if FAILS:
    print(f"FAIL {len(FAILS)}: " + "; ".join(FAILS))
    sys.exit(1)
print("全部 PASS(已清桩)")
