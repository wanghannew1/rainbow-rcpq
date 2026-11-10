#!/usr/bin/env python3
"""V11 挂起视图收尾: 从 Oracle 权威文本(work/pending_view_defs.sql)清洗生成 V11 迁移并应用。

清洗规则(逐视图):
- decode(...) → CASE(decode_to_case, NULL=NULL 命中同 Oracle)
- sysdate→now(), LENGTHB→octet_length, (+) 外连接→LEFT JOIN, rownum=1→LIMIT 1
- LEVEL/CONNECT BY(V_AB01) → WITH RECURSIVE 手工模板
- PKG_COMMON.GET_PARAM/SET_PARAM → pkg_common schema 会话参数垫片(set_config)
- numeric=varchar 比较处按表结构加 ::text
- 死码结案不生成: SYS_CODELIST/TAB_SJBG(user_tab_cols 数据字典),
  V_AA97_NF/V_AA97_TC9T(引用 Oracle 也已不存在的 AAA971/AAA97C)

用法: python3 scripts/gen_views_v11.py   (生成+灌库+对照 Oracle count)
"""
import re
import subprocess
import os
import sys
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent))
from decode_to_case import decode_to_case

ROOT = Path(__file__).resolve().parent.parent
DEFS = (ROOT / "work/pending_view_defs.sql").read_text(encoding="utf-8")
OUT = ROOT / "rcpq-web/src/main/resources/db/V11__views_fix_gap.sql"
ENV = {**os.environ, "PGPASSWORD": "rcpq_dev", "ORA_PASS": "ccrc"}

DEAD = {"SYS_CODELIST", "TAB_SJBG", "V_AA97_NF", "V_AA97_TC9T",
    # V_TC90_XJ: FUN_保险计算(=PRC_计算险金 包装器) 视图层逐行调用无 SQL 等价,
    # 能力由 /proc PKG_人才派遣公共.PRC_计算险金 参数化覆盖, 不建(避免双实现漂移)
    "V_TC90_XJ"}


def pg(sql: str, stop=True):
    r = subprocess.run(["psql", "-h", "localhost", "-U", "rcpq", "-d", "rainbow_rcpq",
                        "-tA", "-v", "ON_ERROR_STOP=1" if stop else "0", "-c", sql],
                       capture_output=True, text=True, env=ENV)
    return r.returncode, (r.stderr or "").strip().splitlines()[0:1], r.stdout


def ora_count(view: str) -> int:
    r = subprocess.run(["java", "-cp", ".:/home/ubuntu/github/rainbowpq/src/main/webapp/WEB-INF/lib/ojdbc14.jar",
                        "Q16", view], capture_output=True, text=True,
                       env=ENV, cwd=str(ROOT / "work/tmpq"))
    try:
        return int((r.stdout or "0").strip().splitlines()[-1])
    except Exception:
        return -1


def clean(name: str, body: str) -> str:
    t = body
    t = re.sub(r'/\*.*?\*/', ' ', t, flags=re.S)           # 块注释(hint)
    t = re.sub(r'^\s*--.*$', '', t, flags=re.M)            # 行注释(alter view 残留)
    t = t.replace('SYSDATE', 'now()').replace('sysdate', 'now()')
    t = re.sub(r'\bLENGTHB\(', 'octet_length(', t, flags=re.I)
    t = t.replace('SUBSTR(', 'substring(').replace('substr(', 'substring(')
    t = re.sub(r'INSTR\(', "instr(", t, flags=re.I)
    t = decode_to_case(t)
    t = re.sub(r'TRUNC\(', "trunc(", t, flags=re.I)
    t = re.sub(r'TO_NUMBER\(', "to_number(", t, flags=re.I)
    t = re.sub(r'\bROWNUM\s*=\s*1\b', 'rownum1', t, flags=re.I)  # 占位, 逐视图特判
    t = re.sub(r'\bNVL\(', 'coalesce(', t, flags=re.I)
    t = re.sub(r'\bMINUS\b', 'EXCEPT', t, flags=re.I)
    t = re.sub(r'\bAS\s+NUMBER\b', 'AS numeric', t, flags=re.I)
    t = re.sub(r'\bTO_CHAR\(', 'to_char(', t, flags=re.I)
    t = re.sub(r'\bADD_MONTHS\(', 'add_months(', t, flags=re.I)
    t = re.sub(r'"(\w+)"', r'\1', t)  # Oracle 导出引号列 → 裸名(PG 折叠小写)
    # 标量子查询 rownum=1 → LIMIT 1(平衡括号扫描)
    i2 = 0
    low = t.lower()
    res = []
    while True:
        j = low.find('where rownum1', i2)
        if j < 0:
            res.append(t[i2:]); break
        start = t.rfind('(', i2, j)
        depth = 0; k = start
        while k < len(t):
            if t[k] == '(': depth += 1
            elif t[k] == ')':
                depth -= 1
                if depth == 0: break
            k += 1
        inner = t[start + 1:k]
        inner2 = re.sub(r'WHERE\s+rownum1\s+(and|AND)\s*', 'WHERE ', inner, flags=re.I)
        res.append(t[i2:start + 1] + inner2 + ' LIMIT 1')
        i2 = k
    t = ''.join(res)
    t = t.replace("AAE001 =to_char(now(),'YYYYMM')", "AAE001 = to_char(now(),'YYYYMM')::numeric")
    t = t.replace("AAC001=TC9A.AAC001", "AAC001=TC9A.AAC001::numeric")  # tc9a.aac001 varchar→tc97.aac001 numeric
    t = re.sub(r'(substring|instr)\(\s*pkg_common\.get_param\(\)', r'\1(pkg_common.get_param()::text', t, flags=re.I)
    t = t.replace("SUBSTR(PKG_COMMON.GET_PARAM(), 1, 6)", "substring(pkg_common.get_param()::text, 1, 6)")
    t = t.replace("A.AAE002 <= PKG_COMMON.GET_PARAM()", "A.AAE002 <= pkg_common.get_param()")
    t = t.replace("A.AAE003 >= PKG_COMMON.GET_PARAM()", "A.AAE003 >= pkg_common.get_param()")
    # Oracle: NUMBER 列与字符字面比较 → 隐转数字; PG 需显式 ::numeric
    t = re.sub(r'(AAE002|AAA501|AAA601|AAA201|AAA203|AAA603|AAA204|AAA502|AAA602|AAA301|AAA302|AAA303)\s*(<=|>=|=|<|>)\s*substring\(pkg_common\.get_param\(\)::text,\s*(\d+),\s*(\d+)\)',
               r'\1 \2 substring(pkg_common.get_param()::text, \3, \4)::numeric', t, flags=re.I)
    t = re.sub(r'(B\.AAA030|AAA030)\s*=\s*substring\(pkg_common\.get_param\(\)::text,\s*(\d+),\s*(\d+)\)',
               r'\1 = substring(pkg_common.get_param()::text, \2, \3)::numeric', t)
    t = t.replace('，', ',').replace('（', '(').replace('）', ')')  # 全角标点(Oracle 导出工件)
    # Oracle 外连接 (+): 三个固定形态 → LEFT JOIN(宽松空白匹配)
    t = re.sub(r'FROM\s+TC8T A,\s*TC8V B\s+WHERE A\.ATC8T0 = B\.ATC8T0\(\+\)',
               'FROM tc8t a LEFT JOIN tc8v b ON a.atc8t0 = b.atc8t0', t, flags=re.I)
    t = re.sub(r'FROM\s+TB90 C,\s*TB93 A,\s*\(SELECT \* FROM AB07 WHERE AAB078 = .1.\) B\s+WHERE A\.Atb931 = B\.AAB004\(\+\)\s+AND A\.ATB900 = C\.ATB900',
               "FROM tb90 c JOIN tb93 a ON a.atb900 = c.atb900 LEFT JOIN (SELECT * FROM ab07 WHERE aab078 = '1') b ON a.atb931 = b.aab004", t, flags=re.I)
    t = re.sub(r'from tc90 t,\s*\(select \* from tc97 a where a\.ATC97B = .1.\) tc97\s+where t\.atc900 = tc97\.id\(\+\)',
               "from tc90 t LEFT JOIN (select * from tc97 a where a.ATC97B = '1') tc97 ON t.atc900 = tc97.id", t, flags=re.I)
    return t


def main():
    # 0) pkg_common 垫片(会话参数, SET→GET 同会话传递; 未设置→NULL 同包变量初始)
    shim = """
CREATE SCHEMA IF NOT EXISTS pkg_common;
CREATE OR REPLACE FUNCTION pkg_common.set_param(num numeric) RETURNS numeric AS $$
  BEGIN PERFORM set_config('rcpq.param_value', num::text, false); RETURN num; END;
$$ LANGUAGE plpgsql;
CREATE OR REPLACE FUNCTION pkg_common.get_param() RETURNS numeric AS $$
  BEGIN RETURN nullif(current_setting('rcpq.param_value', true), '')::numeric; END;
$$ LANGUAGE plpgsql;
"""
    code, err, _ = pg(shim)
    print("pkg_common shim:", "OK" if code == 0 else err)

    blocks = re.split(r'-- ===== (\w+) =====', DEFS)
    views = {}
    for i in range(1, len(blocks), 2):
        views[blocks[i]] = blocks[i + 1]
    print("defs loaded:", len(views))

    ok, failed, dead = [], [], []
    stmts = []
    for name, body in views.items():
        if name in DEAD or "NOT FOUND" in body[:60]:
            dead.append(name)
            continue
        body = re.sub(r'^\s*CREATE OR REPLACE VIEW \w+ AS\s*', '', body, flags=re.I)  # 去 Oracle 导出头
        t = clean(name, body)
        # 逐视图特判
        if name == "V_AB01":
            t = ("WITH RECURSIVE ab01_tree AS ("
                 " SELECT aab001, aab004, aab00a, aab00e, id, 0 AS lv FROM ab01"
                 " WHERE id = '000000000000000000000000000000000000000000000'"
                 " UNION ALL"
                 " SELECT c.aab001, c.aab004, c.aab00a, c.aab00e, c.id, p.lv + 1"
                 " FROM ab01 c JOIN ab01_tree p ON c.parentid = p.id)"
                 " SELECT aab001, aab004, aab00a, aab00e, id, lv FROM ab01_tree")
            stmt = f"CREATE OR REPLACE VIEW v_ab01 AS {t}"
        elif name == "V_TC8T_TC8V":
            t = re.sub(r'SELECT\s+ROWNUM\s+AS ID', 'SELECT row_number() OVER ()   AS ID', t, flags=re.I)
            stmt = 'DROP VIEW IF EXISTS v_tc8t_tc8v;' + chr(10) + f"CREATE OR REPLACE VIEW v_tc8t_tc8v AS {t}"
        elif name == "V_TB93_AB07":
            t = t.replace(" FROM TB90 C, TB93 A, (SELECT * FROM AB07 WHERE AAB078 = '1') B"
                          " WHERE A.Atb931 = B.AAB004(+) AND A.ATB900 = C.ATB900",
                          " FROM tb90 c JOIN tb93 a ON a.atb900 = c.atb900"
                          " LEFT JOIN (SELECT * FROM ab07 WHERE aab078 = '1') b ON a.atb931 = b.aab004")
            stmt = f"CREATE OR REPLACE VIEW v_tb93_ab07 AS {t}"
        elif name == "V_TC90_TC97":
            t = t.replace(" from tc90 t,(select * from tc97 a where a.ATC97B = '1') tc97"
                          " where t.atc900 = tc97.id(+)",
                          " from tc90 t LEFT JOIN (select * from tc97 a where a.ATC97B = '1') tc97"
                          " ON t.atc900 = tc97.id")
            stmt = f"CREATE OR REPLACE VIEW v_tc90_tc97 AS {t}"
        elif name == "V_TC90_AB01_TB93":
            t = re.sub(r'WHERE\s+rownum\s*=\s*1\b', 'LIMIT 1', t, flags=re.I)
            stmt = f"CREATE OR REPLACE VIEW v_tc90_ab01_tb93 AS {t}"
        else:
            stmt = f"CREATE OR REPLACE VIEW {name.lower()} AS {t}"
        stmts.append(stmt + ";")

    out = "-- V11: 挂起视图收尾(pkg_common 垫片 + decode→CASE + (+)→LEFT JOIN + CONNECT BY→递归 CTE)\n" + "\n".join(stmts) + "\n"
    OUT.write_text(out, encoding="utf-8")
    apply_fail = []
    for st in stmts:
        c2, e2, _ = pg(st)
        if c2 != 0:
            nm = re.search(r'CREATE OR REPLACE VIEW (\w+)', st)
            apply_fail.append(((nm.group(1) if nm else st[:40]), (e2[0] if e2 else "?")))
    print("V11 applied:", "OK" if not apply_fail else f"{len(apply_fail)} stmt errors")
    for nm, e in apply_fail: print("  APPLY-FAIL", nm, "->", e)

    # 逐视图验证: PG 可查询 + Oracle count 对照
    for name, body in views.items():
        if name in DEAD or "NOT FOUND" in body[:60]:
            continue
        c, e, so = pg(f"SELECT COUNT(*) FROM {name.lower()}".replace("v_ab01 ", "v_ab01 "), stop=False)
        if c != 0:
            failed.append((name, e[0] if e else "?"))
            continue
        oc = ora_count(name)
        pcn = (so or "0").strip()
        mark = "OK" if str(oc) == pcn else f"COUNT-DIFF ora={oc} pg={pcn}"
        print(f"{name}: built {mark}")
        ok.append(name)

    print("\n== summary ==")
    print("built:", len(ok), "failed:", len(failed), "dead:", len(dead))
    for f in failed: print("  FAIL", f)
    print("  dead:", ", ".join(dead))


if __name__ == '__main__':
    main()
