#!/usr/bin/env python3
"""从旧系统页面 XML 提取每页 datastores 清单，回填 interactions.json 的 store 段缺失。

旧系统语义: InteractionBean 不绑 store，页面 DataCenter 携带命名 store 集合；
页面 XML 的 <property name="datastores"> 以 "storeName@路径" 形式列出全部 store。
新网关 storeOf() 只能解析 "REQ-<page>-<store>-<action>" 可读码；哈希码(354/683)
无 store 段 → 通用 query/update 无法路由。

回填后 interactions.json 每条增加 "stores": [storeName...]（按页面 XML 权威清单），
网关解析顺序: ?store= 显式参数 > 码内 store 段 > spec.stores[0]。
"""
import json
import re
import sys
from pathlib import Path

OLD = Path("/home/ubuntu/github/rainbowpq/src/main/resources/com/neusoft/lemis/apps/lemis")
NEW_JSON = Path(__file__).resolve().parent.parent / "rcpq-web/src/main/resources/interactions.json"

def page_stores_map():
    """page文件名 -> [storeName...]，扫旧树全部 *-*.jsp.xml 的 datastores 属性"""
    out = {}
    for xml in OLD.rglob("applicationContext-*.jsp.xml"):
        text = xml.read_text(encoding="utf-8", errors="replace")
        m = re.search(r'<property name="datastores">\s*<list>(.*?)</list>', text, re.S)
        if not m:
            continue
        stores = re.findall(r'<ref bean="([^@]+)@', m.group(1))
        if stores:
            out[xml.name] = stores  # applicationContext-<page>.jsp.xml
    return out

def main():
    mapping = page_stores_map()
    data = json.loads(NEW_JSON.read_text(encoding="utf-8"))
    items = data if isinstance(data, list) else data.get("interactions", data)
    patched, unmatched = 0, []
    for it in items:
        page = it.get("page", "")
        key = f"applicationContext-{page}.xml"
        if key in mapping:
            it["stores"] = mapping[key]
            patched += 1
        else:
            unmatched.append(page)
    NEW_JSON.write_text(json.dumps(items, ensure_ascii=False, indent=1) + "\n", encoding="utf-8")
    print(f"patched={patched}/{len(items)} unmatched_pages={len(set(unmatched))}")
    for p in sorted(set(unmatched))[:15]:
        print("  no-xml:", p)

if __name__ == "__main__":
    main()
