#!/usr/bin/env python3
"""td/b|c|d 三级子模块细扫: pages/interactions/custom/tables/procs/服务行数 → work/td_detail.json"""
import os, re, json
from collections import defaultdict

REPO = "/home/ubuntu/github/rainbowpq"
RES = os.path.join(REPO, "src/main/resources")
WEB = os.path.join(REPO, "src/main/webapp")
JAVA = os.path.join(REPO, "src/main/java/com/neusoft/lemis/apps/lemis/td")

def getval(seg, k):
    m = re.search(r'name="%s"\s+value="([^"]+)"' % k, seg)
    return m.group(1) if m else None

M = defaultdict(lambda: {"pages": 0, "inter": 0, "custom_int": 0, "stores": set(),
                          "tables": set(), "procs": set(), "java": {}, "generic_int": 0})

# 页面/交互/引用store: /lemis/pages/td/<l1>/<l2>/... (c 有更深? 先按两级, c 特判 c/a 等)
def key_of(page):
    seg = page.split("/lemis/pages/")[1].split("/")
    # seg = [td, l1, l2, ...]
    if len(seg) >= 3 and seg[1] in ("b", "c", "d"):
        return f"td/{seg[1]}/{seg[2]}"
    return None

for dp, _, fs in os.walk(RES):
    for fn in fs:
        if not fn.endswith(".xml"):
            continue
        text = open(os.path.join(dp, fn), encoding="utf-8", errors="replace").read()
        for m in re.finditer(r'<bean\s+name="(/lemis/pages/[^"]+\.jsp)"\s+class="com\.neusoft\.lemis\.cp33\.pojo\.JspBean"', text):
            k = key_of(m.group(1))
            if not k:
                continue
            M[k]["pages"] += 1
            seg = text[text.find(m.group(0)):][:3000]
            for ref in re.findall(r'<ref\s+bean="([^"]+)"', seg):
                if ".datastore" in ref:
                    M[k]["stores"].add(ref.split("/")[-1].split("@")[0].replace("applicationContext-", ""))
            for imb in re.finditer(r'<bean[^>]*class="com\.neusoft\.lemis\.cp33\.pojo\.InteractionBean"[^>]*>(.*?)</bean>', text, re.S):
                M[k]["inter"] += 1
                b = getval(imb.group(1), "bean") or ""
                if "PoJo" in b:
                    M[k]["generic_int"] += 1
                else:
                    M[k]["custom_int"] += 1

# 表: datastore 文件按路径归 td/<l1>/<l2>
for dp, _, fs in os.walk(os.path.join(RES, "com/neusoft/lemis/apps/lemis/td")):
    rel = os.path.relpath(dp, os.path.join(RES, "com/neusoft/lemis/apps/lemis/td")).split("/")
    if len(rel) < 2 or rel[0] not in ("b", "c", "d"):
        continue
    k = f"td/{rel[0]}/{rel[1]}"
    for fn in fs:
        if fn.endswith(".datastore.xml"):
            t = open(os.path.join(dp, fn), encoding="utf-8", errors="replace").read()
            for m in re.finditer(r'class="com\.neusoft\.lemis\.cp33\.pojo\.PojoDataStoreBean"', t):
                ent = getval(t[m.start():m.start() + 1500], "entity")
                if ent:
                    M[k]["tables"].add(ent[4:].replace(".hbm.xml", "").split("/")[-1])

# Java 服务: td/<l1>/<l2>/*.java
for l1 in ("b", "c", "d"):
    base = os.path.join(JAVA, l1)
    if not os.path.isdir(base):
        continue
    for l2 in sorted(os.listdir(base)):
        d2 = os.path.join(base, l2, "service")
        if not os.path.isdir(d2):
            d2 = os.path.join(base, l2)
        if not os.path.isdir(d2):
            continue
        k = f"td/{l1}/{l2}"
        for fn in sorted(os.listdir(d2)):
            if not fn.endswith(".java"):
                continue
            t = open(os.path.join(d2, fn), encoding="utf-8", errors="replace").read()
            t2 = re.sub(r"/\*.*?\*/", "", t, flags=re.S)
            lines = len(t.splitlines())
            methods = re.findall(r"^\s*public\s+(?:[\w.<>\[\]]+)\s+(\w+)\s*\(([^)]*)\)", t2, re.M)
            methods = [(n, p) for n, p in methods if not re.match(r"(get|set)[A-Z]", n)]
            procs = set(re.findall(r"(?:call\s+)?(?:PKG[\w.]*\.|PRC_)[A-Za-z0-9_]+", t2))
            M[k]["java"][fn] = {"lines": lines, "methods": [m[0] for m in methods],
                                 "procs": sorted(set(re.findall(r"[A-Za-z0-9_.]*(?:PRC|PKG)[A-Za-z0-9_.]*", t2)))}

# 前端过程调用按子模块
for dp, _, fs in os.walk(os.path.join(WEB, "lemis/pages/td")):
    rel = os.path.relpath(dp, os.path.join(WEB, "lemis/pages/td")).split("/")
    if len(rel) < 2 or rel[0] not in ("b", "c", "d"):
        continue
    k = f"td/{rel[0]}/{rel[1]}"
    for fn in fs:
        if fn.endswith((".jsp", ".js")):
            t = open(os.path.join(dp, fn), encoding="utf-8", errors="replace").read()
            for m in re.finditer(r'proName\s*=\s*"([a-zA-Z0-9_.]+)"', t):
                M[k]["procs"].add(m.group(1))

out = {k: {
    "pages": v["pages"], "inter": v["inter"], "custom_int": v["custom_int"],
    "tables": sorted(v["tables"]), "n_tables": len(v["tables"]),
    "procs": sorted(v["procs"]), "java": v["java"],
} for k, v in sorted(M.items()) if v["pages"] or v["java"]}
json.dump(out, open("/home/ubuntu/github/rainbow-rcpq/work/td_detail.json", "w"), ensure_ascii=False, indent=1)

print(f"{'子模块':16s} {'页':>3s} {'交互':>4s} {'自定义':>4s} {'表':>3s} {'过程':>2s}  服务(行/方法)")
for k, v in out.items():
    js = "; ".join(f"{f.replace('.java','')}({d['lines']}L:{len(d['methods'])}m)" for f, d in v["java"].items()) or "—"
    print(f"{k:16s} {v['pages']:4d} {v['inter']:5d} {v['custom_int']:5d} {v['n_tables']:4d} "
          f"{len(v['procs']):4d}  {js[:90]}")
