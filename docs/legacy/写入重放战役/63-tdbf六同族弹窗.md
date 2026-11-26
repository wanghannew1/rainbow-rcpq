# 写入重放战役 63 — tdbf 六同族弹窗批量端到端 13/13(2026-11-26)

> 卷62 bgcl 装置泛化, 一卷批量收官 cbcl/dzdwcl/dzjsdycl/renewallcl/xuqcl/zzcl——
> 02 卷待打项"tdbf 余下弹窗"**全部清零**(tdbf 处理层八弹窗全谱: 07 卷 xinqcl+
> 62 卷 bgcl+本卷六)。

## 一、方法(批量泛化装置)

每弹窗四步: ①1=1 探针读生产行特征(atb911 类型码) ②镜像特征造 TB91 夹具
(机构字段=登录人真实值, 卷62 机构过滤律) ③按弹窗自己的 tb91Store-Q 交互装载断言
④按交互类型分流: 有 TB91 独立保存交互→"再次提交"回写断言; 保存骑 TC90 主 store→
跳过(卷62 已立律); 删除型交互→删净断言。
脚本 `scripts/replay/tdbf_six_dialogs_e2e.py`(幂等, 清场按标记圈定)。

## 二、战果 13/13(幂等二跑全绿)

| 弹窗 | 装载 | 动作段 |
|---|---|---|
| cbcl 撤销处理 | ✅ | 再次提交回写 ✅(随行=tc92Store_cbcl 空集) |
| dzdwcl 到账单位处理 | ✅ | 保存骑 TC90 主 store(卷62 律) |
| dzjsdycl 到账结算单元处理 | ✅ | 再次提交回写 ✅(随行=tc90Store_list 空集; atb912='4'取消/'2'提交双交互) |
| renewallcl 续签处理 | ✅ | **取消续签=删除型交互实证**(200+行删净; 更新骑 tc90Store_list-confirm) |
| xuqcl 续期处理 | ✅ | 保存骑 TC90 主 store |
| zzcl 终止处理 | ✅ | 保存骑 TC90 主 store |

## 三、新定律: 随行面(store companion)

**saveWithOther 形状的保存交互, DC 里必须含随行 store(可为空集), 否则 NPE 500**
(PojoAction 家族崩溃; 单 store 发 TB91 更新在 bgcl 可行=其交互无随行面, 在 cbcl/
dzjsdycl 必炸)。判别法: 看页面 JS 的 save 调用形状——`saveWithOther(...)` 即有随行面,
`save()` 即单 store。交互类型三分类: 更新型(回写断言)/删除型(删净断言)/骑主 store 型
(跳过)。renewallcl 的"-cancle"=删除型实证(200+行删净=忠实"取消续签删除续签信息")。

## 四、同步面

- 00-形状律 1.2 已补: _t 整数律/随行面/机构过滤/裸键 JSON(卷62-63)
- rainbow-rcpq: 若 /interaction 网关移植处理层保存, 随行面形状必须复刻
