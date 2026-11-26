# 05 - GET_PARA 中毒壳与连坐链深挖（2026-11-16 实证版）

> 承 01-RCPQ系列.md「PRC_GET_PARA 是恒返 -1 的中毒壳（9 处调用静默跳员工）」与 00-总览
> 「V_ORG_USER_UNIT 根本不存在」两处结论。本篇用**机械注释审计 + 运行时探针 + 全历史错误
> 考古**三路证据复核，修正三处、证实三处。所有行号可对照 `work/pkg_all/` 转储。
> 证据工具：`scripts/comment_nesting_audit.py`、`scripts/db/DbGetPara*.java`（探针/考古 SQL）。

## 0. 结论速览

| # | 结论 | 对旧结论 |
|---|---|---|
| C1 | 9 处调用中 **5 处在块注释内**，活跃仅 4 处；4 处中真正可达前端的只有 1 处（FZ.PRC_GET_MONEY ← tdbf 新增弹窗） | **修正** 01 档案「FZ/新结算/HM/NEW2/PKG_CHECK 共 9 处」 |
| C2 | CCRCPQU33.V_ORG_USER_UNIT **存在且 VALID**，SELECT 授权也在；坏的是 ccrcpq 本地视图 SYS_ORG_USER_UNIT（ORA-00942，导入时序性失败），且**白天访问即懒自愈** | **修正** 00-总览「V_ORG_USER_UNIT 根本不存在（非 INVALID）」 |
| C3 | 稳态（白天）行为 = 中毒壳静默执行：`code=-1` + 11 项费率 OUT 全 NULL + 荒谬文案「ORA-0000: normal, successful completion」——**探针实测** | **证实** 01 档案「静默跳员工」，但仅在自愈后稳态成立 |
| C4 | tdbf 新增员工补缴金额计算（GET_MONEY）在中毒壳下是**静默空金额**（code 保持 1，服务层当成功），不是弹错 | **新发现** |
| C5 | AA94 费率表 **09/10（公积金比例）整表零行**——即使壳未中毒，公积金比例也永远取不到 | **新发现** |
| C6 | HM:1643/2294 两个调用点均在注释内 | **印证** rainbow-rcpq 侧「空壳第六例 PRC_UPDATE_TC93_AC10 实现体整块注释」 |

## 1. 壳本体（PKG_RCPQ BODY :5952-6035）

活代码仅两句（:5975-5976）：

```plsql
P_APCODE := -1;
P_ERRMSG := '测算出错，错误信息：' || SQLERRM || '，请将错误信息发给维护工程师';
```

`SQLERRM` 在非异常上下文取值 = `ORA-0000: normal, successful completion`，故实际文案为
**「测算出错，错误信息：ORA-0000: normal, successful completion，请将错误信息发给维护工程师」**
（探针实测原文）。注释区（:5977-6033）还原了原语义：

1. `V_AB01_AA03` 按 AAB001 取单位客户号 AAA033（异常时报「请选择派遣单位对应的公司」）；
2. `AA94` 按 `AAA941 IN ('01'..'10') AND AAA100=参保属地 AND AAA946<=年月<=AAA947`
   取 10 项比例；值 <1 时按 `TO_CHAR(AAA942,'0.99')` 格式化；
3. 01~08=五险单位/个人比例，09=单位公积金、10=个人公积金比例。

## 2. 九调用点机械审计

工具：`scripts/comment_nesting_audit.py`（逐行跟踪 `/* */` 嵌套深度；dump 中 `\*`/`*\`
是旧开发者手工转义的注释内嵌注释文本，不参与解析）。结果：

| # | 位置 | 所属过程 | 判定 | 备注 |
|---|---|---|---|---|
| 1 | FZ:759 | PRC_INSERT_TC9TCURR | 活跃代码 | **过程全库零调用方**（前端/包内/包间均无）→ 死过程 |
| 2 | FZ:4334 | PRC_GET_MONEY | 活跃代码 | **现役前端路径**：tdbf_dialog_xinqcl.js:157/247/1259 |
| 3 | FZ:5018 | PRC_CREATE_AC10 | **注释内**（/\* 5014 → \*/ 5112） | 01 档案误计为活跃 |
| 4 | FZ:6070 | PRC_SALARYCOMPUTE2 | **注释内**（/\* 6044 起） | 过程本身亦死码（全库无调用方，rainbow-rcpq 侧已结案） |
| 5 | 新结算:134 | PRC_SALARYCOMPUTE | 活跃代码 | 弃坑实验包（01 档案：前端调用已注释，被 PKG_费用结算 取代） |
| 6 | HM:1643 | PRC_UPDATE_TC93_AC10 | **注释内** | 即空壳第六例过程 |
| 7 | HM:2294 | PRC_UPDATE_TC93_AC10_TJ04 | **注释内** | 同族空壳 |
| 8 | WY:425 | PRC_INS_AC10_CURR | **注释内** | 01 档案「禁用壳」 |
| 9 | PKG_CHECK:603 | PRC_CHECK_JFSJ | 活跃代码 | **前端入口已不存在**（src/main 仅 JFSJ Hibernate 实体，无页面引用；2018 年有 4 条运行记录） |

## 3. 运行时行为模型：连坐 → 懒自愈 → 稳态中毒（2026-11-16 探针实证）

**每晚 02:03-02:04 备份导入**重建 `CCRCPQ.SYS_ORG_USER_UNIT` 视图（user_objects.created
=当天），创建时刻被引用对象尚未就绪 → 编译失败 ORA-00942 → 视图 INVALID → **连坐**：

| 包 | SPEC | BODY | BODY 编译错误（user_errors） |
|---|---|---|---|
| PKG_RCPQ | VALID | INVALID | :349/:827 PLS-00201 SYS_ORG_USER_UNIT；:848 ORA-04063 view has errors |
| PKG_RCPQ_FZ | **INVALID** | INVALID | SPEC:59 `SYS_ORG_USER_UNIT%ROWTYPE` 连坐（PLS-00304 无有效 SPEC） |
| PKG_RCPQ_WY | VALID | INVALID | :11 PLS-00201；:21 ORA-04063 |
| PKG_RCPQ_HM | VALID | INVALID | :39 PLS-00201；:573 AAA97G 列漂移（真伤） |
| PKG_RCPQ_NEW2 | VALID | INVALID | :2085/:3046 ATC9U7、:2249 ATC9WB 列漂移（真伤） |
| PKG_RCPQ_FZ_新结算 | VALID | INVALID | :222/:223 ATC9U2/ATC9V9 组件缺失、:368 ATC930（残骸） |
| PKG_CHECK | VALID | VALID | 无（唯一全 VALID 成员，调用 GET_PARA 时跨包） |

**白天懒自愈**（2026-11-16 探针实测，`scripts/db/DbGetPara8.java`）：

```
SELECT COUNT(*) FROM SYS_ORG_USER_UNIT  →  成功（视图访问即重编译 → VALID）
CALL PKG_RCPQ.PRC_GET_PARA(...) 第1次  →  调用成功 code=-1 errmsg=[测算出错，错误信息：
    ORA-0000: normal, successful completion，请将错误信息发给维护工程师] 11 项费率 OUT 全 null
第2次  →  同上
探针后 user_objects: VIEW VALID + PKG_RCPQ BODY VALID
```

即：**首调无 ORA-04068/04063**（新会话按需重编译静默完成），包体自愈为 VALID 后进入稳态
——中毒壳稳定执行、恒 -1、全 NULL。凌晨窗口（自愈发生前）调用才走 ORA-06508 报错路径。

> 注：探针把备份库的 PKG_RCPQ BODY 治愈为 VALID 了；每晚导入会重新连坐，无副作用。

## 4. 连坐根因链勘误（对 00-总览）

- 00-总览原表述：「UniEAP 视图 V_ORG_USER_UNIT 根本不存在（非 INVALID），连坐 4 包」。
- 实测（all_objects/all_synonyms/user_tab_privs）：
  - `CCRCPQU33.V_ORG_USER_UNIT` VIEW **VALID**；
  - 本地视图 `CCRCPQ.SYS_ORG_USER_UNIT` VIEW INVALID，user_errors 仅一行 `ORA-00942`；
  - 视图定义（DBMS_METADATA）：`SELECT "ACCOUNT","AAE011",...,"ISLEADER" FROM CCRCPQU33.V_ORG_USER_UNIT`（FORCE 建）；
  - 授权 `CCRCPQU33 → CCRCPQ : SELECT` **存在**。
- 归因：对象与授权俱在，唯余**导入时序**——每晚 imp 重建本地视图的瞬间 V_ORG_USER_UNIT
  尚未就绪（或自身临时 INVALID），编译失败定格；白天首次访问懒重编译自愈。此模型同时
  解释了「清早第一次操作必报连接/编译类异常」的一部分现象。

## 5. 全历史错误考古（SYS_ERR_LOG 2010→2026 无断档）

| 关键字 | 全史 | 分布 |
|---|---|---|
| PKG_RCPQ_FZ | 77 | 2018=54, 2019=23（样本：**`存储过程[PKG_RCPQ_FZ.PRC_GET_MONEY]指定回滚:计算社保补缴金额合计出错…ORA-01403`**，2018-01-05） |
| ORA-06508 | 2,098 | 2019=31, 2020=131, 2021=369（样本调用方 `PKG_费用结算.PRC_费用结算`）, 2022=377, 2023=18, 2024=0, **2025=1,172**（样本调用方 `PKG_缴费基数.PRC_公积金缴费基数核对`）, 2026=**0**（同年日志健康，5,216 条） |
| ORA-04063 | 0 | 全史零条——懒自愈先于报错发生，错误从未落日志 |
| INSERT_TC9TCURR / CHECK_JFSJ | 0 / 4 | 后者仅 2018（当时页面尚在） |

时间线解读：

- **2018 年 FZ 包能编译、GET_MONEY 真实执行过**（业务级 ORA-01403 弹窗为证，死因=AA98
  省平工资查询无护栏，见 §6）→ 坏视图连坐是 **2018 年之后**才进入常态；
- ORA-06508 波 = 业务员点菜单时刻落在「凌晨重置后未自愈」窗口，或重编译当刻依赖未就绪；
  2025 年 1,172 条是公积金基数核对重度使用，2026 年归零 = 相关菜单不再被踩（非修复）；
- 中毒壳何时形成不可断代（user_source 只有现态）；2018 年报错发生在 GET_PARA 调用之前
  （AA98 处），无法证明当时壳是否已中毒。

## 6. 三个活跃调用点的中毒后果详析

### 6.1 FZ.PRC_GET_MONEY（:4264-4435）——唯一现役前端路径 ⭐

- 调用方：`src/main/webapp/lemis/pages/td/b/f/tdbf_dialog_xinqcl.js:157/247/1259`
  （新增员工弹窗 doSetSum 等三处，传 单位编号/补缴起止年月/五险基数/参保属地/户口性质）。
- 头部注释（:4293-4299）= 补缴计算完整业务规格，移植必读：
  - 养老/医疗/生育/工伤用**当前月**比例，失业用**补缴月**比例；
  - 农村户籍按 AA01 参数 SYBX 判断是否补失业个人部分；
  - 跨年补缴：工伤、生育不补；养老补缴基数=省平工资（AA98.AAA982），其余险种按当前基数；
  - 公积金参数用页面录入值（不走 AA94）。
- **中毒后果**：:4308 `P_APCODE:=1` 初始化 → :4334 调壳返回 -1 → :4350 `IF P_APCODE=1`
  为假 → ELSE 分支只设 `P_ERRMSG:='获取当前缴费比例出错…'` 就 RETURN（**不重置 code**）
  → OUT 金额 BBA000/BAA000/DAA001 从未赋值 → **服务层按 code=1 成功返回，前端拿到
  null 金额且无任何提示 = 静默空金额**（C4）。
- 旁证：2018 年同路径报的是「ORA-01403 未找到任何数据」弹窗（AA98 上年度省平工资查询
  无 NO_DATA_FOUND 护栏，:4329-4332）——当年至少会弹错，如今连错都不弹。

### 6.2 PKG_CHECK.PRC_CHECK_JFSJ（:541-1725）

- GET_PARA 调用（:603）后**无任何 P_APCODE 门**（:604-695 直落业务分支）→ NULL 费率
  直接进 JFSJ_MX 明细：`BBA001 := ROUND(NVL(基数,0) * N_YANGLAO_DW / 100, 2)`（:695）
  = NULL，费率列 ATB90K/L… 原样落 NULL（:742-746）→ 校验明细**静默产出 NULL 金额行**。
- 现状缓解：前端入口已不存在（仅 2018 年 4 条运行记录），学术意义为主。

### 6.3 FZ.PRC_INSERT_TC9TCURR（:759 调用点）

- `IF P_APCODE='1'` 包住整段 TC9T 测算行构造（员工级全跳过，:775 起为 01 档案「实测分支」）
  ——但过程全库零调用方，该分支从未有机会执行。01 档案把它当「静默跳员工」的行为证据，
  实际是**从未可达的代码**。

## 7. AA94 数据现实与原语义可复原性

```
AAA941  行数  生效区间
01      11   201501~209912   单位养老
02      17   201701~202712   单位医疗
03      11   201501~209912   单位失业
04      19   201701~202612   单位工伤
05      14   201701~209912   单位生育
06       9   201501~209912   个人养老
07      15   201701~202712   个人医疗
08      11   201501~209912   个人失业
09/10   ——   从未配置（公积金比例整表零行）
11      68   201701~202712   （GET_PARA 不读）
12       8   201701~202712   （GET_PARA 不读）
```

- 表 VALID、区间覆盖当下（2026-11 落在 02/04/07 等码的生效区间内），**五险八项原语义
  完全可复原**；公积金 09/10 从未配置——旧系统公积金比例从来不是 AA94 供给的。
- `V_AB01_AA03`（单位客户号）VIEW VALID。

## 8. 对重写侧的保真输入（决策材料；本仓库不改 rainbow-rcpq）

1. **tdbf 新增员工补缴金额计算**：生产现役路径，真实现状=静默空金额（§6.1）。
   重写若移植该弹窗金额计算，需决策：复刻「静默空金额」还是按头部注释的业务规格
   （§6.1 四条）真实现？规格是完整的，AA94 数据是齐的，真实现零障碍。
2. **PG 侧费率口径**：AA94 09/10 公积金零行 → 任何「按 AA94 取公积金比例」的实现都是
   空中楼阁；PG 侧已有自己的费率/缴费基数实现，不必为对齐旧壳而复刻缺陷。
3. **「ORA-0000」文案**：是 `SQLERRM` 在非异常上下文的模式缺陷（毒壳+GET_MONEY ELSE
   分支同款）。复刻与否只影响提示文案，不影响语义。
4. **sleepy 现象**：每晚连坐+白天懒自愈模型（§3/§4）= 旧系统「清早第一次点报错、再点
   就好」的另一半根因（此前已归因于 DBCP 连接失效，两者叠加）。重写侧 PG 无此问题。
5. 01 档案「GET_PARA 必须重写为真实现（优先级最高）」建议**降级**：其全部活跃调用方
   中只有 tdbf 金额计算一条真实业务路径受益，且该路径现属「静默空金额」而非数据错误。

## 9. 方法与工具附录

- `scripts/comment_nesting_audit.py <符号名>`：dump 注释嵌套审计（活跃/注释内判定 +
  所属过程定位）。局限：依赖 dump 无转义遗漏；`/*`/`*/` 同格先开后关。
- `scripts/db/DbGetPara.java`（包状态+user_errors+连坐对象+AA94）、`DbGetPara2.java`
  （视图根因+全历史 ORA 分布+夜间样本）、`DbGetPara3.java`（分年报错样本）、
  `DbGetPara5.java`（授权核查）、`DbGetPara8.java`（**活体探针**：直调无副作用毒壳 +
  坏视图自愈验证）、`DbGetPara9.java`（AA94 全码值分布）。
- JDBC 坑：`CallableStatement` 里 Oracle 不认 `OUT?` 语法，OUT 参数用纯 `?` 占位 +
  `registerOutParameter` 表达（DbGetPara6/7 两次失败教训，DbGetPara8 为 16 参数终版）。
- 局限性：① user_source 无历史，中毒壳断代不可考；② SYS_ERR_LOG 对编译失败型异常
  有「裸 LemisBusinessException」漏记口径（详见 oracle 备份库笔记），分年统计取的是
  带关键字样本，2026 归零结论已用同年 5,216 条日志健康度校准。
