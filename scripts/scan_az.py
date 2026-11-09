#!/usr/bin/env python3
"""az 模块细扫: 页面/交互/store/表/服务方法 → work/az_detail.json"""
import os, re, json
from collections import defaultdict

REPO = "/home/ubuntu/github/rainbowpq"
RES = os.path.join(REPO, "src/main/resources")
JAVA = os.path.join(REPO, "src/main/java/com/neusoft/lemis/apps/lemis/az")

getval = lambda seg, k: (re.search(r'name="%s"\s+value="([^"]+)"' % k, seg) or [None, None]) and (
    re.search(r'name="%s"\s+value="([^"]+)"' % k, seg).group(1) if re.search(r'name="%s"\s+value="([^"]+)"' % k, seg) else None)

M = defaultdict(lambda: {"pages": [], "inter": [], "stores": set(), "tables": set(), "java": {}})

for dp, _, fs in os.walk(RES):
    for fn in fs:
        if not fn.endswith(".xml"):
            continue
        text = open(os.path.join(dp, fn), encoding="utf-8", errors="replace").read()
        for m in re.finditer(r'<bean\s+name="(/lemis/pages/az/[^"]+\.jsp)"\s+class="com\.neusoft\.lemis\.cp33\.pojo\.JspBean"', text):
            k = m.group(1).split("/lemis/pages/az/")[1].rsplit("/", 1)[0]  # a 或 b
            M[k]["pages"].append(m.group(1).split("/")[-1])
            for imb in re.finditer(r'<bean[^>]*class="com\.neusoft\.lemis\.cp33\.pojo\.InteractionBean"[^>]*>(.*?)</bean>', text, re.S):
                M[k]["inter"].append({"page": m.group(1).split("/")[-1],
                    "code": getval(imb.group(1), "code"), "type": getval(imb.group(1), "type"),
                    "bean": getval(imb.group(1), "bean"), "method": getval(imb.group(1), "method"),
                    "validate": getval(imb.group(1), "metadata_validate")})
        for m in re.finditer(r'<bean\s+name="[^"]*"\s+class="com\.neusoft\.lemis\.cp33\.pojo\.PojoDataStoreBean"', text):
            seg = text[m.start():m.start() + 1500]
            ent = getval(seg, "entity")
            if ent and ("/az/" in text[m.start():m.start()] or "az" in fn or True):
                # 仅统计 az 目录下的 datastore
                pass
for dp, _, fs in os.walk(os.path.join(RES, "com/neusoft/lemis/apps/lemis/az")):
    rel = os.path.relpath(dp, os.path.join(RES, "com/neusoft/lemis/apps/lemis/az"))
    k = rel.split("/")[0]
    for fn in fs:
        if fn.endswith(".datastore.xml"):
            t = open(os.path.join(dp, fn), encoding="utf-8", errors="replace").read()
            for m in re.finditer(r'class="com\.neusoft\.lemis\.cp33\.pojo\.PojoDataStoreBean"', t):
                ent = getval(t[m.start():m.start() + 1500], "entity")
                nm = getval(t[m.start():m.start() + 300], "name")
                if ent:
                    M[k]["tables"].add(ent[4:].replace(".hbm.xml", "").split("/")[-1])
                    M[k]["stores"].add(nm or fn.replace("applicationContext-", "").replace(".datastore.xml", ""))

for l2 in sorted(os.listdir(JAVA)):
    d2 = os.path.join(JAVA, l2)
    if not os.path.isdir(d2):
        continue
    for dp, _, fs in os.walk(d2):
        for fn in fs:
            if not fn.endswith(".java"):
                continue
            t = open(os.path.join(dp, fn), encoding="utf-8", errors="replace").read()
            t2 = re.sub(r"/\*.*?\*/", "", t, flags=re.S)
            methods = re.findall(r"public\s+[\w.<>\[\]]+\s+(\w+)\s*\(([^)]*)\)", t2)
            methods = [(n, p.strip()[:70]) for n, p in methods if not re.match(r"(get|set)[A-Z]", n)]
            procs = sorted(set(re.findall(r"[A-Za-z0-9_.]*(?:PRC|PKG)[A-Za-z0-9_.]*", t2)))
            M[l2]["java"][fn] = {"lines": len(t.splitlines()),
                "file": os.path.relpath(os.path.join(dp, fn), REPO),
                "methods": methods, "procs": procs}

out = {k: {"pages": v["pages"], "inter": v["inter"], "stores": sorted(v["stores"]),
            "tables": sorted(v["tables"]), "java": v["java"]} for k, v in sorted(M.items())}
json.dump(out, open("/home/ubuntu/github/rainbow-rcpq/work/az_detail.json", "w"), ensure_ascii=False, indent=1)

for k, v in out.items():
    print(f"=== az/{k}: {len(v['pages'])}页 {len(v['inter'])}交互 表:{v['tables']} store:{v['stores']}")
    for fn, d in v["java"].items():
        print(f"  {fn} ({d['lines']}L) procs={d['procs']}")
        for n, p in d["methods"]:
            print(f"    {n}({p[:55]})")
print("交互明细:")
for k, v in out.items():
    for i in v["inter"]:
        if i["bean"] and "PoJo" not in i["bean"]:
            print(f"  [{k}] {i['page']} {i['code']} {i['type']} -> {i['bean']}.{i['method']} validate={i['validate']}")
