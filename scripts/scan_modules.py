#!/usr/bin/env python3
"""业务模块定量盘点: 页面/交互/Service/存储过程/表 → work/modules.json"""
import os
import re
import json
from collections import defaultdict, Counter

REPO = "/home/ubuntu/github/rainbowpq"
RES = os.path.join(REPO, "src/main/resources")
WEB = os.path.join(REPO, "src/main/webapp")
JAVA = os.path.join(REPO, "src/main/java")

getval = lambda seg, k: (re.search(r'name="%s"\s+value="([^"]+)"' % k, seg) or [None, None]) and (
    re.search(r'name="%s"\s+value="([^"]+)"' % k, seg).group(1) if re.search(r'name="%s"\s+value="([^"]+)"' % k, seg) else None)

modules = defaultdict(lambda: {
    "pages": 0, "interactions": 0, "generic_int": 0, "stores": set(), "tables": set(),
    "java_files": [], "java_lines": 0, "custom_methods": 0, "procs": set(),
})

def modkey(page):
    seg = page.split("/lemis/pages/")[1].split("/")
    if seg[0] == "lemis":
        seg = seg[1:]
    m = seg[0]
    sub = seg[1] if len(seg) > 1 else ""
    if m == "td":
        return f"td/{sub}" if sub else "td"
    if m == "az":
        return f"az/{sub}" if sub else "az"
    return m

# 页面+交互
for dp, _, fs in os.walk(RES):
    for fn in fs:
        if not fn.endswith(".xml"):
            continue
        text = open(os.path.join(dp, fn), encoding="utf-8", errors="replace").read()
        for m in re.finditer(r'<bean\s+name="(/lemis/pages/[^"]+\.jsp)"\s+class="com\.neusoft\.lemis\.cp33\.pojo\.JspBean"', text):
            k = modkey(m.group(1))
            modules[k]["pages"] += 1
            seg = text[text.find(m.group(0)):]
            for ref in re.findall(r'<ref\s+bean="([^"]+\.datastore\.xml)"', text[text.find(m.group(0)):][:3000]):
                modules[k]["stores"].add(ref.split("/")[-1].replace("applicationContext-", "").replace(".datastore.xml", ""))
            for imb in re.finditer(r'<bean[^>]*class="com\.neusoft\.lemis\.cp33\.pojo\.InteractionBean"[^>]*>(.*?)</bean>', text, re.S):
                modules[k]["interactions"] += 1
                b = getval(imb.group(1), "bean") or ""
                if "PoJo" in b:
                    modules[k]["generic_int"] += 1
        # datastore → entity/table
        for m in re.finditer(r'<bean\s+name="[^"]*?([^/\"]+\.datastore\.xml)"\s+class="com\.neusoft\.lemis\.cp33\.pojo\.PojoDataStoreBean"', text):
            ent = getval(text[m.start():m.start() + 1500], "entity")
            if ent:
                tb = ent[4:].replace(".hbm.xml", "").split("/")[-1]
                modules[k2 := modkey("/lemis/pages/" + fn.replace("applicationContext-", "").replace(".jsp.xml", ""))]["tables"] if False else None

# 表归属: datastore 文件路径含模块路径 → 归该模块
for dp, _, fs in os.walk(RES):
    for fn in fs:
        if not (fn.endswith(".datastore.xml") and dp.replace("\\", "/").find("/lemis/") >= 0):
            continue
        rel = os.path.relpath(dp, os.path.join(RES, "com/neusoft/lemis/apps"))
        if not rel.startswith("lemis/"):
            continue
        parts = rel.split("/")
        m = parts[1] if parts[0] == "lemis" else parts[0]
        sub = parts[2] if m in ("td", "az") and len(parts) > 2 else ""
        k = f"{m}/{sub}" if sub else m
        text = open(os.path.join(dp, fn), encoding="utf-8", errors="replace").read()
        for m2 in re.finditer(r'<bean\s+name="[^"]*"\s+class="com\.neusoft\.lemis\.cp33\.pojo\.PojoDataStoreBean"', text):
            ent = getval(text[m2.start():m2.start() + 1500], "entity")
            if ent:
                modules[k]["tables"].add(ent[4:].replace(".hbm.xml", "").split("/")[-1])

# Java Service + 行数 + 自定义方法数
for dp, _, fs in os.walk(os.path.join(JAVA, "com/neusoft/lemis/apps/lemis")):
    rel = os.path.relpath(dp, os.path.join(JAVA, "com/neusoft/lemis/apps/lemis"))
    parts = rel.split("/")
    if parts[0] in ("xa", "az", "td", "wts"):
        k = f"{parts[0]}/{parts[1]}" if len(parts) > 1 else parts[0]
        for fn in fs:
            if fn.endswith(".java"):
                t = open(os.path.join(dp, fn), encoding="utf-8", errors="replace").read()
                t2 = re.sub(r"/\*.*?\*/", "", t, flags=re.S)
                nm = len(re.findall(r"^\s*public\s+\w[\w.<>\[\]]*\s+(\w+)\s*\(", t2, re.M))
                nm -= len(re.findall(r"^\s*public\s+.*get[A-Z]\w*\(", t2, re.M)) + len(re.findall(r"^\s*public\s+void\s+set[A-Z]\w*\(", t2, re.M))
                modules[k]["java_files"].append(fn)
                modules[k]["java_lines"] += len(t.splitlines())
                modules[k]["custom_methods"] += max(0, nm)

# 前端存储过程调用
for dp, _, fs in os.walk(os.path.join(WEB, "lemis/pages")):
    rel = os.path.relpath(dp, os.path.join(WEB, "lemis/pages"))
    if rel == ".":
        continue
    parts = rel.split("/")
    k = modkey("/lemis/pages/" + rel)
    for fn in fs:
        if fn.endswith((".jsp", ".js")):
            t = open(os.path.join(dp, fn), encoding="utf-8", errors="replace").read()
            for m in re.finditer(r'proName\s*=\s*"([a-zA-Z0-9_.]+)"', t):
                modules[k]["procs"].add(m.group(1))

out = {k: {
    "pages": v["pages"], "interactions": v["interactions"], "custom_int": v["interactions"] - v["generic_int"],
    "tables": len(v["tables"]), "java_files": len(v["java_files"]), "java_lines": v["java_lines"],
    "custom_methods": v["custom_methods"], "procs": sorted(v["procs"]),
} for k, v in sorted(modules.items())}
os.makedirs("/home/ubuntu/github/rainbow-rcpq/work", exist_ok=True)
json.dump(out, open("/home/ubuntu/github/rainbow-rcpq/work/modules.json", "w"), ensure_ascii=False, indent=1)

print(f"{'模块':14s} {'页面':>4s} {'交互':>4s} {'自定义':>4s} {'表':>3s} {'Java':>4s} {'行数':>6s} {'方法':>4s} {'过程':>3s}")
for k, v in sorted(out.items()):
    print(f"{k:14s} {v['pages']:5d} {v['interactions']:5d} {v['custom_int']:5d} {v['tables']:4d} "
          f"{v['java_files']:5d} {v['java_lines']:6d} {v['custom_methods']:5d} {len(v['procs']):4d}")
