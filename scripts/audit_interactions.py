#!/usr/bin/env python3
"""交互路由审计: 逐条检查 interaction 引用的 store 是否有元数据定义。
判定规则: 交互码 store 段 或 所在页面 JspBean 的 datastores refs 任一命中元数据 = 可路由;
GUID 码交互(无 store 段)按页面 refs 判定; 其余 = 旧系统坏配置(输出清单)"""
import json
import re
import os
from collections import defaultdict

RES = "/home/ubuntu/github/rainbowpq/src/main/resources"
meta = set(json.load(open('/home/ubuntu/github/rainbow-rcpq/rcpq-service/src/main/resources/xa-metadata.json')).keys())
inter = json.load(open('/home/ubuntu/github/rainbow-rcpq/rcpq-web/src/main/resources/interactions.json'))

page_refs = defaultdict(set)
for dp, _, fs in os.walk(RES):
    for fn in fs:
        if not fn.endswith(".xml"):
            continue
        text = open(os.path.join(dp, fn), encoding="utf-8", errors="replace").read()
        for m in re.finditer(r'<bean\s+name="(/lemis/pages/[^"]+\.jsp)"\s+class="[^"]*JspBean"', text):
            page = m.group(1).split("/")[-1]
            seg = text[m.start():m.start() + 3000]
            refs = {r.split("/")[-1].replace("applicationContext-", "").replace(".datastore.xml", "").split("@")[0]
                    for r in re.findall(r'<ref\s+bean="([^"]*)"', seg) if ".datastore" in r}
            page_refs[page] |= refs

guid_ok = named_ok = broken_n = 0
broken = []
for x in inter:
    parts = x["code"].split("-")
    store = parts[-2] if len(parts) >= 3 else ""
    cands = {store} | page_refs.get(x["page"], set())
    if len(parts) < 3 or re.match(r"^[0-9a-f]{32}$", parts[1]):
        # GUID 码/无 store 段: 页面 refs 任一命中即按运行时解析通过
        if cands & meta:
            guid_ok += 1
            continue
    elif store in meta or (cands & meta):
        named_ok += 1
        continue
    broken_n += 1
    broken.append({"code": x["code"], "page": x["page"], "reason": "页面所有store均无元数据定义"})

json.dump(broken, open('/home/ubuntu/github/rainbow-rcpq/work/broken_interactions.json', 'w'), ensure_ascii=False, indent=1)
print(f"交互 {len(inter)} 条: 可路由 {guid_ok + named_ok}, 坏引用 {broken_n}")
