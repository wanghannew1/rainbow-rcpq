#!/usr/bin/env python3
"""tc9w_bp04(市医保异动报盘导入) 新系统端到端冒烟 — V19 种子补种后的通道验证。

镜像旧侧端到端(东软战役卷61, 9/9): xls→/import/sys/tc9w_bp04→暂存(参数盖章)→
/proc 校验(告警'11')→/proc 提交(TC90 新参保回写+AA97_BG)→清场。
夹具: 全新身份证 110101199001010023 + TC90 医保合同(真实结算单元 37312 只读引用),
隔离月 209903, 属地 229900000000。前置: 新系统 8080 在跑; V19 种子已应用(V11 已并回 V19)。
隔离月登记表(桥单 20261126-003, 新增隔离月测试先查此表避撞):
209901=e2e_import_sys §11 / 209902=e2e_api_coverage 步骤2 / 209903=本脚本。
"""
import io
import json
import os
import re
import subprocess
import sys
import urllib.request
import urllib.error

PG = "postgresql://rcpq:rcpq_dev@localhost:5432/rainbow_rcpq"
APP = "http://localhost:8080"
F_ID = "110101199001010023"
F_NAME = "E2E卷61回盘人"
F_MONTH = "209903"
TERR = "229900000000"
XLS = "/tmp/e2e_tc9w_bp04.xls"

PASS, FAIL = [], []


def rec(name, ok, detail=""):
    (PASS if ok else FAIL).append(name)
    print(("✅" if ok else "❌"), name, ("— " + detail if detail else ""))


def scalar(q):
    out = subprocess.run(["psql", PG, "-tAc", q], capture_output=True, text=True).stdout
    v = out.strip().split("\n")[0] if out.strip() else ""
    return v if v else None


def http_json(url, data=None, headers=None, files=None):
    try:
        if files:
            boundary = "E2EBP04"
            buf = bytearray()
            for k, v in (data or {}).items():
                buf += f"--{boundary}\r\n".encode()
                buf += f'Content-Disposition: form-data; name="{k}"\r\n\r\n{v}\r\n'.encode()
            fn, content = files
            buf += f"--{boundary}\r\n".encode()
            buf += f'Content-Disposition: form-data; name="file"; filename="{fn}"\r\n'.encode()
            buf += b"Content-Type: application/vnd.ms-excel\r\n\r\n"
            buf += content + b"\r\n"
            buf += f"--{boundary}--\r\n".encode()
            req = urllib.request.Request(url, data=bytes(buf), headers={
                "Content-Type": f"multipart/form-data; boundary={boundary}"})
        else:
            req = urllib.request.Request(url, data=json.dumps(data or {}).encode(),
                                         headers={"Content-Type": "application/json"})
        with urllib.request.urlopen(req, timeout=60) as r:
            return r.status, r.read().decode("utf-8", "replace")
    except urllib.error.HTTPError as e:
        return e.code, e.read().decode("utf-8", "replace")


def main():
    # 0) 夹具
    subprocess.run(["psql", PG, "-c",
                    f"DELETE FROM tc90 WHERE aac002='{F_ID}'"], capture_output=True)
    atc900 = int(scalar("SELECT COALESCE(MAX(atc900),0)+1 FROM tc90"))
    subprocess.run(["psql", PG, "-c", (
        f"INSERT INTO tc90 (atc900, atb930, atb900, aab001, aac001, aac002, aac003,"
        f" aab004, atb90r, aaa402, aae00p, aaa110, aaa302, aae036) VALUES"
        f" ({atc900}, 37312, 58332, 151, 900000002, '{F_ID}', '{F_NAME}',"
        f" 'E2E测试单位', '3', '1', '1', '{TERR}', 5000, now())")], capture_output=True)
    rec("夹具: TC90 医保合同", scalar(f"SELECT COUNT(*) FROM tc90 WHERE aac002='{F_ID}'") == "1",
        f"atc900={atc900}")

    # 1) xls(行1; 列3=aac002/4=aac003/5=abp001/8=abp004)
    import xlwt
    wb = xlwt.Workbook()
    ws = wb.add_sheet("Sheet1")
    for col, val in {3: F_ID, 4: F_NAME, 5: "在职", 8: "6000"}.items():
        ws.write(1, col, val)
    wb.save(XLS)
    rec("造 xls(17 列映射, 最小 4 格)", os.path.exists(XLS))

    # 2) 导入
    params = json.dumps({"aae002": F_MONTH, "aaa030": "4",
                         "aaa031": "吉林省彩虹人才开发咨询服务有限公司(汪清)",
                         "aaa020": TERR, "aaa021": "市医保"}, ensure_ascii=False)
    with open(XLS, "rb") as f:
        st, txt = http_json(f"{APP}/import/sys/tc9w_bp04",
                            data={"parameters": params, "javabean": ""},
                            files=("e2e.xls", f.read()))
    ok = '"succeed":true' in (txt or "") or "'succeed':true" in (txt or "")
    ce = re.search(r'"ce10":\[?(\d+)', txt or "")
    batch = ce.group(1) if ce else None
    rec("导入: succeed:true", ok and bool(batch), f"ce10={batch} http={st} {(txt or '')[:80]}")

    # 3) 暂存验证(参数盖章)
    n = scalar(f"SELECT COUNT(*) FROM tc9w_bp04 WHERE aac002='{F_ID}' AND aae002={F_MONTH}")
    t = scalar(f"SELECT aaa020 FROM tc9w_bp04 WHERE aac002='{F_ID}' AND aae002={F_MONTH}")
    rec("暂存: 落行+参数盖章", n == "1" and t == TERR, f"rows={n} aaa020={t}")

    # 4) 校验(过程网关, 中文过程名 URL 编码)
    from urllib.parse import quote
    st, txt = http_json(f"{APP}/proc/{quote('PKG_保险人员增减.PRC_市医保异动回盘校验')}",
                        {"aae002": F_MONTH, "aaa030": "4", "aaa020": TERR})
    a = scalar(f"SELECT ace10a FROM tc9w_bp04 WHERE aac002='{F_ID}' AND aae002={F_MONTH}")
    y = scalar(f"SELECT aaa90y FROM tc9w_bp04 WHERE aac002='{F_ID}' AND aae002={F_MONTH}")
    rec("校验: ace10a=3+告警'11'", st == 200 and a == "3" and "11" in (y or ""),
        f"http={st} ace10a={a} aaa90y={y}")

    # 5) 提交
    st, txt = http_json(f"{APP}/proc/{quote('PKG_保险人员增减.PRC_市医保异动回盘提交')}",
                        {"aae002": F_MONTH, "aaa030": "4", "aaa020": TERR, "aae011": "jiaq"})
    s = scalar(f"SELECT ace10a FROM tc9w_bp04 WHERE aac002='{F_ID}' AND aae002={F_MONTH}")
    rec("提交: 暂存回写1", st == 200 and s == "1", f"http={st} ace10a={s}")

    row = scalar(f"SELECT TO_CHAR(aaa603,'FM999999')||'/'||atc90pa||'/'||"
                 f"TRIM(TRAILING '.' FROM TRIM(TRAILING '0' FROM aaa302::text))"
                 f" FROM tc90 WHERE aac002='{F_ID}'")
    rec("TC90: 新参保回写", row == f"{F_MONTH}/0/6000", f"aaa603/atc90pa/aaa302={row}(数值规整后)")
    # 桥单 010(BC-03 收尾): 值断言补齐(旧侧对照表 #8 count/bgq/bgh=1/5000/6000; 数值比较免 scale 陷阱)
    bg = scalar(f"SELECT COUNT(*) FROM aa97_bg WHERE atc900={atc900} "
                f"AND aaa300_bgq=5000 AND aaa300_bgh=6000")
    rec("AA97_BG: 底册变更行(bgq=5000/bgh=6000 值钉)", bg == "1", f"count(值匹配)={bg}")

    # 深度对照(纪律 v2, 桥单 010): 清场前全列行态导出, 供跨库逐列 diff(序列/时间戳列对照时排除)
    dump = os.path.join(os.path.dirname(os.path.dirname(os.path.abspath(__file__))),
                        "work", "bc03_pg_rowdump.txt")
    with open(dump, "w", encoding="utf-8") as f:
        for tbl, where in (("tc9w_bp04", f"aac002='{F_ID}' AND aae002={F_MONTH}"),
                           ("tc90", f"atc900={atc900}"),
                           ("aa97_bg", f"atc900={atc900}")):
            out = subprocess.run(["psql", PG, "-tAc",
                                  f"SELECT row_to_json(t) FROM (SELECT * FROM {tbl} WHERE {where}) t"],
                                 capture_output=True, text=True).stdout.strip()
            f.write(f"{tbl}: {out}\n")
    print(f"    全列行态导出 → {dump}")

    # 6) 清场
    for sql in (f"DELETE FROM tc9w_bp04 WHERE aac002='{F_ID}'",
                f"DELETE FROM aa97_bg WHERE atc900={atc900}",
                f"DELETE FROM tc90_yy WHERE atc900={atc900}",
                f"DELETE FROM tc90 WHERE aac002='{F_ID}'",
                f"DELETE FROM ce10 WHERE ace100={batch}" if batch else "SELECT 1"):
        subprocess.run(["psql", PG, "-c", sql], capture_output=True)
    left = scalar(f"SELECT COUNT(*) FROM tc90 WHERE aac002='{F_ID}'")
    rec("清场: 全零", left == "0", f"tc90={left}")
    try:
        os.remove(XLS)
    except OSError:
        pass
    print(f"\n=== tc9w_bp04 新系统端到端: {len(PASS)} 过 / {len(FAIL)} 败 ===")
    sys.exit(1 if FAIL else 0)


if __name__ == "__main__":
    main()
