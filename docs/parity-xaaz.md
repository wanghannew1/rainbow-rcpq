# xa/az 域对照 — 东软 字典·单位·人员·问题平台 ↔ 新系统泛型通道

> 2026-11-24。真值源：东软侧=xa-module-spec.md（14 页×26 交互 Spring 装配级清单）+module-inventory
> xa/az 行+战役 22~27/33 卷（字典参数页行为实录）+JSP 源码；新侧=AzCases/XadCodeCases/XabUnitCases
> 规格重写实源+interactions.json（xa/az 页 63 交互）+V3 种子+Metadata 单测。本域特殊性：**旧侧零存储过程**
> （xa 前端无任何 proName，az 仅 7 个简单 PL/SQL），承接形态=泛型 CRUD 通道+规格重写用例，非逐页 UI 平移。

## 〇、结论速览

- 东软 xa（14 页/26 交互/642 行）+az/a（8 页/11 交互/435 行）+az/b（16 页/26 交互/780 行）= **38 页 63 交互**
  → 新系统 **0 个独立维护页**：全部以"泛型 CRUD 通道（GenericCrudDispatcher）+交互注册表（interactions.json
  63 条同名同构）+元数据校验（MetadataValidator）+V3 种子"承接；**维护 UI 未建**（工作台按业务节拍设计，
  主档维护属二阶段前端），读侧已由 /wb/codes·districts·units·aaa030s·opers 五查询端点服务 14 个工作台页。
- 后端为**规格重写**（AzCases javadoc 明示"七个类，规格重写，非逐行翻译"），7 个 PL/SQL 过程逐条有移植
  决策（§三），其中移植期**发现孤儿过程一处**（PRC_个人基本信息变更 全库零引用——旧代码调用名少"基本"
  二字，真实过程存在但从未被调）。
- 字典页家族怪癖（程序化 delete 数组解析缺陷，战役 22/23 卷）在泛型通道中**不存在**=升级项；但维护页
  未建前无对照实测。
- 问题平台 xah*（5 页 17 交互）后端通道在册，页面未承接——旧 SYS_QUESTION 系内部工单域，qid=null
  级联 bug（SEQ_SYS_QUESTION_QSESSION 未接线，审计已证）不随移植。

## 一、域级总表

| 东软页面 | 交互数 | 表 | 新承接 | 形态 |
|---|---|---|---|---|
| xaad 代码字典维护 | 4 | AA10 | XadCodeCases+注册表+V3 种子 | 规格重写（doQuery）；维护 UI 未建 |
| xaab 行政区划维护 | 3 | AA12 | XabUnitCases（doUpdate/doDelete/getChildId） | 规格重写；树形 ID 12 位区划码保留 |
| xaac 综合参数维护 | 2 | AA01 | 注册表泛型+V3 种子 | 泛型 CRUD；生产基线 8 行（摆设域） |
| xahb/xaha/xahc/xahd/xahe 问题平台 | 17 | SYS_QUESTION | 注册表在册，页面未承接 | 旧内部工单域；SEQ 未接线 bug 不随移植 |
| azaa 单位信息/树维护 | 7 | AB01(+AB01_CHILD) | AzCases.rebuildUnitChildIndex | PRC_CREATE_CHILD→WITH RECURSIVE 层级重建（升级：递归 CTE 替代过程） |
| azab 单位变更审核 | 2 | AB06 | AzCases.auditAb06 | PRC_单位基本信息变更→逐字段写 AB06（列中文注释=ColumnMeta.label 等价） |
| azad 单位附件 | 2 | 附件表 | 注册表泛型 | |
| azba 人员档案 | 15 | AC01/AC06 | AzCases.auditAc06Field+broadcast | PRC_个人信息变更→数据字典三重验证逐列对齐（BAZ001/002/AAE035/036）；**孤儿过程发现**：真实过程全库零引用系旧调用名错 |
| azbb/azbc/azbd/azbe 人员子页 | 11 | AC01/TC90 系 | AzCases+P_UPDATE_VALUE/P_DELETE 动态广播 | 查 information_schema 展开键列表；渐进迁移期仅命中已迁移表=已记录限制 |
| azac~azah 其余 | — | — | module-inventory az/a 8 页 ✅(波次2) | 通用化验收波，单测+冒烟 |

## 二、交互通道对照

interactions.json 内 xa/az 页面 **63 条同名同构**（code/bean/method/stores 四元组与旧 Spring 装配 XML 一致）：
azaa 7 · azba 15（最重，人员档案读写族）· xahc 5 · xaad 4 · xaha/xahd/azbc/azbe 各 3 · azab/azad/xaab/xaac/xahb/xahd 各 2~3。
泛型分发=GenericCrudDispatcher；元数据必填校验=-30050 家族同构（MetadataValidatorTest 8 断言）。

## 三、过程/服务对照（10 旧类 → 3 新用例类 + 7 过程决策）

| 旧类/过程 | 新实现 | 决策/注记 |
|---|---|---|
| XaadService.doQuery | XadCodeCases | 规格重写 |
| XaabService.doUpdate/doDelete + XaabAjax.getChildId | XabUnitCases | AA12 树语义保留 |
| XaaService(HibernateDaoSupport) | GenericCrudDispatcher 泛型 | 升级：免逐实体 Dao |
| azaaService | AzCases（单位树族） | |
| azabService | AzCases.auditAb06 | =PRC_单位基本信息变更 |
| AzbaService/AzbaSave | AzCases.auditAc06Field | =PRC_个人信息变更；孤儿过程发现 |
| azbbService/Azbd/Azbe | AzCases.broadcast 族 | P_UPDATE_VALUE/P_DELETE 动态化 |
| PRC_CREATE_CHILD | rebuildUnitChildIndex | WITH RECURSIVE 替代 |
| PRC_AC01_UPDATE_ONLY | JPA update | 等价：动态列更新 |
| PKG_COMMON.P_UPDATE_VALUE/P_DELETE | broadcastUpdate/broadcastDelete | 渐进迁移期限制已记录 |

## 四、读侧服务对照（主档 → 工作台下拉/联动）

| 旧页面查询 | 新查询端点 | 消费方 |
|---|---|---|
| xaad AA10 码值查询 | GET /wb/codes?category= | 14 页全部码表下拉（AAE00P/ATC90A 等） |
| xaab AA12 区划树 | GET /wb/districts | hz/yue/insure 参保属地（长春市/省直两档） |
| azaa AB01 单位 | GET /wb/units · /wb/settle-units · /wb/agreements | hire/imports/change 三向联动 |
| azba AC01 人员 | GET /wb/contract-search · /wb/opers | insure/salary 搜索、全页经办人 |

## 五、差异与阶段归属

| 类别 | 条目 | 归属 |
|---|---|---|
| 前端未建 | 38 页维护 UI（字典/区划/参数/单位树/人员档案/问题平台） | 二阶段前端（主档维护非月度节拍页；读侧已通，写侧通道在册即插即用） |
| 升级（通道内建） | 字典删除家族怪癖不存在；CREATE_CHILD 递归 CTE；元数据校验统一单一来源 | 二阶段方针内嵌（规格重写时完成） |
| 既有挂账 | AA10 两库分叉 406 行合并待业务确认；AB01.aaa031 名称回填 857 行 | 二阶段 backlog（见记忆总账） |
| 不随移植 | 问题平台 qid=null 级联（SEQ 未接线实锤）；字典页删除解析缺陷 | 永久（旧缺陷不复刻） |
| 移植期发现 | PRC_个人基本信息变更=孤儿过程（旧调用名笔误）| 已决策记录于 AzCases javadoc |
| 待核 | azad 附件表清单；azbb~azbe 四子页交互的页面对应关系 | 二阶段建页时逐条对 |

## 六、测试证据

- 单测：MetadataRegistryTest(3)+MetadataValidatorTest(8) 全绿。
- 波次2 冒烟（az a+b 合并波，2026-11-09 收官）：6 个自定义用例移植与 xa 同量级显式用例；变更审核链
  （PRC_单位/个人信息变更→Kotlin）+人员合并/删除审计。
- V3__xa_seed_data.sql（DumpSeed 全量 INSERT）+DumpGolden 黄金样本（xa 4 表列名归一小写口径）。
- 战役 22~27 卷字典参数页行为实录（AA01 8→9 行实测/AA94 183 行/删除家族怪癖）为二阶段建页时的行为规格。
