# 写入重放战役 51 — 零散 SERVICE 页实弹 4/4(2026-11-25)

> ◻119 主战场第四轮: tdhb 公积金基数上下限调整查询(TC0A)/tdiw 省医保退休补缴(TC40)。

## 一、战果

| 用例 | 内容 | 结果 |
|---|---|---|
| MS-01 | tdhb 查询形状(TC0A) | ✅ |
| MS-02 | tdhb 实查命中(SQL 夹具→页面通道, 条件小写) | ✅ |
| MS-03 | tdiw 查询形状(TC40) | ✅ |
| MS-04 | tdiw 删除通道(SQL 夹具→页面 Del→零残留) | ✅ |

## 二、知识

- tdhb/tdiw 均"SQL 夹具→页面通道验证"型: 数据生产在别处(退休补缴数据由报增/退休链生成),
  页面只做查/删 — 页面通道本身健康。
- TC40(退休人员主数据)与 TC0A(基数调整年月)结构入档; 夹具=合成主键 SQL 直插+页面通道清。

## 三、本日 ◻119 总消化

| 卷 | 族 | 页数 |
|---|---|---|
| 48 | 工伤认定族(tdil/tdim/tdiq) | 3 |
| 49 | 工资维护族(tddf/tdgg/tdgh/tdhc) | 4 |
| 50 | 财务发票族(tdlx/tdlv/tdhbb/tdhcc/tdhdd) | 5 |
| 51 | 零散(tdhb/tdiw) | 2 |
| **合计** | | **14 页 ✅** |

回归五件套: gongshang_family / wage_dict_family / fin_invoice_family / misc_service_pages
(+testcase_suite 13 用例), 全部复跑幂等+清场零残留。

## 四、剩余

tddab(工资账号, 动 TC90 大表)/tdcay 医保异动回盘/xahb 弹窗层级/Excel 导入(等用户授权补列)。
