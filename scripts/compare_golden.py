#!/usr/bin/env python3
"""黄金对照: work/golden_calc_<年月>.csv(Oracle 真值, GoldenCalc.java 生成) vs PG /proc/PRC_计算险金。
用法: python3 scripts/compare_golden.py 202610 202611 202606
列全等(数值容差 1e-6, 空=NULL 等价); 输出逐行 diff 摘要。exit 0=全对 1=有差异。"""
import csv, json, sys, urllib.request, urllib.parse

def run(ym):
    rows = list(csv.DictReader(open(f'work/golden_calc_{ym}.csv', encoding='utf-8')))
    cols = [c for c in rows[0].keys() if c not in ('atc900', 'code', 'msg')]
    url = 'http://localhost:8080/proc/' + urllib.parse.quote('PKG_人才派遣公共.PRC_计算险金')
    checked = 0; diffs = []
    for r in rows:
        req = urllib.request.Request(url, data=json.dumps({"atc900": r['atc900'], "aae002": ym}).encode(),
            headers={'Content-Type': 'application/json'})
        try:
            res = json.load(urllib.request.urlopen(req, timeout=30))
        except Exception as e:
            diffs.append((r['atc900'], 'http', str(e)[:40])); continue
        if res.get('appCode') != 1:
            diffs.append((r['atc900'], 'appCode', str(res.get('errMsg'))[:60])); continue
        checked += 1
        d = []
        for c in cols:
            ov, pv = r[c], res.get(c)
            try:
                if (ov == '' or ov is None) and (pv is None or pv == ''): continue
                if ov == '' or pv is None or pv == '': d.append((c, ov, pv)); continue
                if abs(float(ov) - float(pv)) > 1e-6: d.append((c, ov, pv))
            except (ValueError, TypeError):
                if str(ov) != str(pv): d.append((c, ov, pv))
        if d: diffs.append((r['atc900'], 'DIFF', str(d)[:120]))
    print(f"{ym}: total={len(rows)} checked={checked} diff_rows={len(diffs)}")
    for d in diffs[:8]: print(" ", d)
    return len(diffs)

if __name__ == '__main__':
    yms = sys.argv[1:] or ['202610', '202611', '202606']
    sys.exit(1 if any(run(y) for y in yms) else 0)
