#!/usr/bin/env python3
"""BC-42 税务/个税域 sys_import 配置对比 + 引擎可行性探针 — 桥单 20261127-042。

①配置行清点: sys_import 两侧 112 行(Oracle ccrcpq@.60 JDBC 导出 work/si_ora.txt;
  PG 侧 work/si_pg.txt), importId 集合逐一对齐, 归一化(numeric 标度表示剥离)逐列 diff
  零真差异(唯一差异=PG 数值列漂移标度表示 0 vs 0.0000000000, 值同)。
②引擎可行性: tc97_bp(员工档案信息导入, 模板 tdfa.xls 生成器, data_from=8, 映射 6 列)
  隔离探针——模板下载→填 1 行(隔离标记 aac003='卷42探针')→上传→tc97_bp 落行→清场。
  idSeq 三件套接线(tc97_bp/tc97_bp_wyh/tc95_bp)随本单提交。
零写面: 仅隔离夹具行+批次行, 清场后零残留。
用法: python3 scripts/e2e_bc42_taxcfg.py
"""
import io
import json
import os
import subprocess
import sys
import urllib.request
from urllib.parse import quote

import openpyxl

PG = "postgresql://rcpq:rcpq_dev@localhost:5432/rainbow_rcpq"
BASE = "http://localhost:8080"
ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))

PASS, FAIL = [], []


def rec(name, ok, detail=""):
    (PASS if ok else FAIL).append(name)
    print(("✅" if ok else "❌"), name, ("— " + detail if detail else ""))


def sql(q):
    r = subprocess.run(["psql", PG, "-tAc", q], capture_output=True, text=True)
    return (r.stdout or "").strip()


def multipart_upload(import_id: str, filename: str, data: bytes, params: dict) -> dict:
    boundary = "----bc42probe"
    parts = []
    for k, v in params.items():
        parts.append(f'--{boundary}\r\nContent-Disposition: form-data; name="{k}"\r\n\r\n{v}\r\n'.encode())
    parts.append(f'--{boundary}\r\nContent-Disposition: form-data; name="file"; filename="{filename}"\r\n'
                 f"Content-Type: application/vnd.openxmlformats-officedocument.spreadsheetml.sheet\r\n\r\n".encode())
    parts.append(data + b"\r\n")
    parts.append(f"--{boundary}--\r\n".encode())
    req = urllib.request.Request(BASE + "/import/sys/" + quote(import_id), data=b"".join(parts),
                                 headers={"Content-Type": f"multipart/form-data; boundary={boundary}"})
    with urllib.request.urlopen(req, timeout=120) as r:
        return json.load(r)


MARK = "卷42探针"
AAC002 = "220183199906018802"


def main():
    # ── ① 配置行清点(两侧 dump 归一化 diff) ──
    sys.path.insert(0, "/tmp")  # 复用归一化逻辑内联
    def norm(v):
        if v == "~":
            return "~"
        try:
            if "." in v:
                d = v.rstrip("0").rstrip(".")
            else:
                d = v
            return str(int(d)) if d.lstrip("-").isdigit() and ("." in v or v.isdigit()) else v
        except Exception:
            return v

    def load(p):
        rows = {}
        for line in open(p, encoding="utf-8"):
            line = line.strip()
            if not line:
                continue
            fields = line.split("|")
            iid = [f for f in fields if f.startswith("IMPORT_ID=")][0][10:]
            rows[iid] = [f.split("=", 1)[1] for f in fields if "=" in f]
        return rows

    names = ["IMPORT_ID","TEMPLATE_ID","TEMPLATE_VERSION","CHECK_VERSION","TITLE","FILE_SUFFIX",
             "SHEET_INDEX","DATA_TYPE","SEPARATOR","XPATH","DATA_FROM","ENTITY_NAME","JAVA_BEAN",
             "DESCRIPTION","TEMPLATE_URL","PROC_NAME","MIN_AVAI_CELL","TEMPLATE_NAME"]
    ora = load(os.path.join(ROOT, "work", "si_ora.txt"))
    pg = load(os.path.join(ROOT, "work", "si_pg.txt"))
    rec("①配置行两侧行数", len(ora) == 112 and len(pg) == 112, f"ORA={len(ora)} PG={len(pg)}")
    rec("①importId 集合对齐", set(ora) == set(pg), f"对称差={sorted(set(ora) ^ set(pg)) or '无'}")
    diffs = []
    for iid in sorted(set(ora) & set(pg)):
        for i, nm in enumerate(names):
            if norm(ora[iid][i]) != norm(pg[iid][i]):
                diffs.append((iid, nm, ora[iid][i], pg[iid][i]))
    rec("①归一化逐列 diff 零真差异", len(diffs) == 0,
        f"真差异={len(diffs)}{' ' + str(diffs[:3]) if diffs else '(唯一表示差=PG numeric 标度, 值同)'}")

    # ── ② 引擎可行性探针(tc97_bp) ──
    for iid in ("tc97_bp", "tc97_bp_wyh", "tc95_bp"):
        n = sql(f"SELECT COUNT(*) FROM sys_import WHERE import_id='{iid}'")
        m = sql(f"SELECT COUNT(*) FROM sys_import_mapping WHERE import_id='{iid}'")
        t = sql(f"SELECT COUNT(*) FROM information_schema.tables WHERE table_name='{iid}'")
        seq_name = "seq_tc95_id" if iid == "tc95_bp" else f"seq_{iid}_id"  # tc95 命名不均匀(hbm 权威)
        s = sql(f"SELECT COUNT(*) FROM pg_sequences WHERE sequencename = '{seq_name}'")
        rec(f"②{iid} 配置在册", n == "1" and t == "1" and s == "1", f"配置行={n} 映射行={m} 表={t} 序列={s}")

    tpl = urllib.request.urlopen(BASE + "/import/sys/" + quote("tc97_bp") + "/template", timeout=60).read()
    wb = openpyxl.load_workbook(io.BytesIO(tpl))
    ws = wb["Sheet0"]
    head_row, values = 8, {"公民身份证号码": AAC002, "姓名": MARK, "档案编号": "B42-0001", "户口编号": "B42-U-0001",
                           "社保个人编号": "99420001", "公积金账号": "9942G0001"}
    headers = {}
    for c in range(1, ws.max_column + 1):
        v = ws.cell(row=head_row, column=c).value
        if v:
            headers[str(v).strip()] = c
    got = {k: headers[k] for k in values if k in headers}
    ok = len(got) == len(values)
    rec("②模板下载+列头对位", ok, f"模板 {len(tpl)}B, 列头命中 {len(got)}/{len(values)}: {list(headers.values())[:8]}")
    if not ok:
        print(f"  实际列头: {list(headers)}")
        sys.exit(1)
    for k, col in got.items():
        ws.cell(row=head_row + 1, column=col, value=values[k])
    buf = io.BytesIO()
    wb.save(buf)

    params = {"aae011": "jiaq", "aae002": "209906"}
    r = multipart_upload("tc97_bp", "tc97_bp.xlsx", buf.getvalue(), params)
    ok = r.get("succeed") is True
    rec("②上传解析面(一次解析)", ok, f"响应={json.dumps(r, ensure_ascii=False)[:160]}")
    if ok:
        n = sql(f"SELECT COUNT(*) FROM tc97_bp WHERE aac002='{AAC002}' AND aac003='{MARK}'")
        rec("②隔离行落库", n == "1", f"tc97_bp 隔离行={n}")
        atc = sql(f"SELECT MIN(atc978) FROM tc97_bp WHERE aac002='{AAC002}' AND aac003='{MARK}'")
        rec("②列值忠实(atc978)", atc == "B42-0001", f"atc978={atc}")
        # 清场: 隔离行+批次行(ce10 by ace100)
        ace = r.get("ce10") or {}
        ace_list = ace.get("ace100") if isinstance(ace, dict) else ace
        subprocess.run(["psql", PG, "-c",
                        f"DELETE FROM tc97_bp WHERE aac002='{AAC002}' AND aac003='{MARK}'"],
                       capture_output=True)
        if ace_list:
            for a in (ace_list if isinstance(ace_list, list) else [ace_list]):
                subprocess.run(["psql", PG, "-c", f"DELETE FROM ce10 WHERE ace100={a}"],
                               capture_output=True)
    left = int(sql(f"SELECT COUNT(*) FROM tc97_bp WHERE aac002='{AAC002}' AND aac003='{MARK}'"))
    rec("清场", left == 0, f"残留={left}")

    print(f"\n=== BC-42 税务配置对比+引擎探针: {len(PASS)} 过 / {len(FAIL)} 败 ===")
    sys.exit(1 if FAIL else 0)


if __name__ == "__main__":
    main()
