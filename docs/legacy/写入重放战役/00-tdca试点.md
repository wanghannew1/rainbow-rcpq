# 写入重放战役 00 — tdca 社保报增试点(2026-11-21 凌晨, 8091 实例)

全生命周期四环全部打穿，库零残留。靶: `PKG_保险人员增减.PRC_社保报增员工` + tc9wStore_grid
保存交互 `REQ-f9192e418a4e4b85978d45b03f3685a7`。测试行: ATC900=14930(王越)×户头AAA030=4×年月202612。

## 四环实录

| 环 | 载荷要点 | 结果 | DB 验证 |
|---|---|---|---|
| ① proc 生成 | proName 带**前导空格**(照抄页面), paraIn=[ATC900,202612,AAA030,'',zhanghsh], paraOutType=`[]` | 200 `appCode:"1"` | TC9W 33939→33940, 202612 新行 ATC9W0=1397512, ATC9W1='1', AAE011=zhanghsh |
| ② 查询整行 | 网格真味: 交互 `REQ-tdca_cont-tc90Store-Q` + 条件骨架 `atc9w1='1' and aae002='月' and aaa030='户头' order by atc9w0 asc` | 87 字段整行 | — |
| ③ 整行保存 | rowSet.primary=[整行+`_t:3`+`_o:{变更字段:旧值}`] | 200 | AAE013 NULL→'19999999999' 落库, 总数不变(UPDATE 语义) |
| ④ 删除 | 同交互, 行入 rowSet.`delete` 数组 | 200 | 总数复原 33939 |

## 报文形状律(工具已内置, 见 replay2.py)

1. **digest 算法(致命坑)**: `sha1( escape(报文).toLowerCase() )`，服务端 `Escape.escape(body).toLowerCase().getBytes()` 后 SHA-1 比对 header `digest`，**必验非备而不验**。Python 侧勿用 `str.isalnum()`——汉字返回 True 致中文裸进摘要→服务端"传输的数据被篡改"。已修: 仅 ASCII 字母数字与 `@*_+-./` 不转义, 其余 `%XX`/`%uXXXX`。**带中文的 proc/proName/condition 全靠它**。
2. **行状态律**: 修改行必须带 `_t:3`(unieap.ds.Status.DATAMODIFIED, 新行=1, 未改=2——服务端 `_t||NOTMODIFIED` 跳过)+`_o:{字段:旧值}`(仅变更字段)。缺之=200 但静默无操作。
3. **删除律**: 行入 `rowSet.delete` 数组(值原样), 走同一保存交互。
4. **proc 应答**: 成功 `appCode:"1"`+`errMsg:""`; 业务拒绝走 OUT 参数转 LemisException。
5. **响应元数据说谎**: `recordCount:0` 但 rowSet.primary 有行——以 primary 为准。
6. **保存不戳 AAE036**: 提交什么存什么, 时间戳不自动刷新。

## 服务端崩溃指纹(catalina.out 排障速查)

- `NPE at PojoAction.action:82` = dc==null = **报文没过解析**(本次=摘要错), 真错误在其上一行 `DataCenterReaderImpl.parse:84 传输的数据被篡改`。
- `JSONException: null object at PojoAction.action:83 setTitle` = 业务异常无 title(如过程不存在), 走 :83。
- 均与记忆 §PojoAction:83 家族吻合, 已闭环为两类指纹。

## 方法论(下页推广模板)

```
读页面 JS → 抠 proc/交互真味(勿信文档记忆, 本次第4参实为 PRM_AAA020 页面却传 aaa100)
→ 读过程源码(work/pkg_all) 确认读写面/戳记/校验 → JDBC 挑小足迹候选+快照
→ proc 实弹(隔离年月+AAE011 戳记) → 页面同款查询取整行 → save 改低危字段(_t/_o)
→ DB diff 验证 → delete 清场 → DB 复原核验
```
