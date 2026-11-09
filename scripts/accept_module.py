#!/usr/bin/env python3
"""零自定义模块验收器: 对指定页面前缀的全部交互跑路由验证。

- query 交互 → GET /interaction/{code}，期望返回 table+rows（真数据可查）
- update/count 交互 → POST /interaction/{code} body={"rows":[]}，
  空载荷无写入风险，但完整走通 store 解析→元数据→实体解析→分发链

用法: python3 accept_module.py <页面正则>   例: ^tdl[a-z]  或  ^tdh
输出: PASS/FAIL 清单 + 汇总；FAIL 附首错。
"""
import json
import re
import sys
import urllib.request

BASE = "http://localhost:8080/interaction"
JSON_PATH = "rcpq-web/src/main/resources/interactions.json"


def call(method: str, code: str, body: dict | None = None):
    url = f"{BASE}/{code}"
    data = None
    req = urllib.request.Request(url, method=method)
    if body is not None:
        data = json.dumps(body).encode()
        req.add_header("Content-Type", "application/json")
    try:
        with urllib.request.urlopen(req, data=data, timeout=30) as r:
            return json.loads(r.read())
    except urllib.error.HTTPError as e:
        try:
            return json.loads(e.read())
        except Exception:
            return {"error": f"HTTP {e.code}"}
    except Exception as e:
        return {"error": str(e)}


def main():
    pat = re.compile(sys.argv[1] if len(sys.argv) > 1 else r"^tdl[a-z]")
    its = json.load(open(JSON_PATH))
    its = its if isinstance(its, list) else its.get("interactions", its)
    targets = [it for it in its if pat.match(it.get("page", ""))]
    print(f"目标: {len(targets)} 交互 / {len(set(it['page'] for it in targets))} 页面")
    fails, passes = [], 0
    for it in targets:
        code, method, page = it["code"], it["method"], it["page"]
        if method == "query":
            r = call("GET", code)
            ok = "error" not in r and "table" in r
            detail = f"{r.get('store','')}→{r.get('table','')} rows={r.get('totalCount')}" if ok else f"ERR {r.get('error')}"
        else:
            r = call("POST", code, {"rows": []})
            ok = "error" not in r
            detail = f"{r.get('store','')} ins={r.get('inserted')} upd={r.get('updated')} del={r.get('deleted')}" if ok else f"ERR {r.get('error')}"
        tag = "PASS" if ok else "FAIL"
        print(f"[{tag}] {method:6s} {page:36s} {detail}")
        if ok:
            passes += 1
        else:
            fails.append((code, method, page, detail))
    print(f"\n汇总: {passes}/{len(targets)} PASS, {len(fails)} FAIL")
    for f in fails:
        print("  FAIL", f[1], f[2], f[3])


if __name__ == "__main__":
    main()
