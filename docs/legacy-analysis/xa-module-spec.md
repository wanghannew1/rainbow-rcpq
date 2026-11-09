# xa 模块规格书（重写第一站）

> 2026-11-06 生成。范围：`/lemis/pages/xa/**` 全部 14 页面；数据源：Spring 装配 XML（JspBean/datastore/InteractionBean）+ xa Java 源码。

## 1. 模块画像

| 指标 | 值 |
|---|---|
| 页面 | 14（cont 列表页 7 + dialog 编辑页 7，h 子模块 4 组 + a/b/c/d 定制组） |
| 交互 | 26 |
| datastore | 9 |
| 涉及表/视图 | 4（其中视图 0） |
| 存储过程调用 | **0**（xa 前端无任何 proName —— 纯标准 CRUD，理想试点） |
| 自定义 Service | XaabService（LemisBusinessMgr 子类）、XaadService（同）、XaaService（HibernateDaoSupport） |

## 2. 页面 × 交互清单

| 页面 | datastore（→表） | 交互 |
|---|---|---|
| **xahb_cont.jsp** | sys_questionStore→SYS_QUESTION<br>sys_questionStore_List→SYS_QUESTION<br>sys_questionStore_Query→SYS_QUESTION | `REQ-xahb_cont-sys_questionStore_Query-Q` Q LemisBusinessMgrPoJo.query |
| **xahb_dialog.jsp** | sys_questionStore→SYS_QUESTION<br>sys_questionStore_List→SYS_QUESTION<br>sys_questionStore_Query→SYS_QUESTION | `REQ-xahb_dialog-sys_questionStore-Q` Q LemisBusinessMgrPoJo.query<br>`REQ-xahb_dialog-sys_questionStore-M` M LemisBusinessMgrPoJo.update |
| **xahd_cont.jsp** | sys_questionStore→SYS_QUESTION<br>sys_questionStore_List→SYS_QUESTION<br>sys_questionStore_Query→SYS_QUESTION | `REQ-xahd_cont-sys_questionStore_Query-Q` Q LemisBusinessMgrPoJo.query |
| **xahd_dialog.jsp** | sys_questionStore→SYS_QUESTION<br>sys_questionStore_List→SYS_QUESTION<br>sys_questionStore_Query→SYS_QUESTION | `REQ-xahd_dialog-sys_questionStore-Q` Q LemisBusinessMgrPoJo.query<br>`REQ-xahd_dialog-sys_questionStore-M` M LemisBusinessMgrPoJo.update |
| **xahe_cont.jsp** | sys_questionStore→SYS_QUESTION<br>sys_questionStore_List→SYS_QUESTION<br>sys_questionStore_Query→SYS_QUESTION | `REQ-xahe_cont-sys_questionStore_Query-Q` Q LemisBusinessMgrPoJo.query |
| **xahe_dialog.jsp** | sys_questionStore→SYS_QUESTION<br>sys_questionStore_List→SYS_QUESTION<br>sys_questionStore_Query→SYS_QUESTION | `REQ-xahe_dialog-sys_questionStore_Query-Q` Q LemisBusinessMgrPoJo.query |
| **xahc_cont.jsp** | sys_questionStore→SYS_QUESTION<br>sys_questionStore_List→SYS_QUESTION<br>sys_questionStore_Query→SYS_QUESTION<br>sys_questionStore_dispose→SYS_QUESTION | `REQ-xahc_cont-sys_questionStore_Query-Q` Q LemisBusinessMgrPoJo.query |
| **xahc_dialog.jsp** | sys_questionStore→SYS_QUESTION<br>sys_questionStore_List→SYS_QUESTION<br>sys_questionStore_Query→SYS_QUESTION<br>sys_questionStore_dispose→SYS_QUESTION | `REQ-xahc_dialog-sys_questionStore-Q` Q LemisBusinessMgrPoJo.query<br>`REQ-xahc_dialog-sys_questionStore-M` M LemisBusinessMgrPoJo.update |
| **xahc_dialog_dispose.jsp** | sys_questionStore→SYS_QUESTION<br>sys_questionStore_List→SYS_QUESTION<br>sys_questionStore_Query→SYS_QUESTION<br>sys_questionStore_dispose→SYS_QUESTION | `REQ-xahc_dialog_dispose-sys_questionStore-Q` Q LemisBusinessMgrPoJo.query<br>`REQ-xahc_dialog_dispose-sys_questionStore-M` M LemisBusinessMgrPoJo.update |
| **xaha_cont.jsp** | sys_questionStore→SYS_QUESTION<br>sys_questionStore_List→SYS_QUESTION<br>sys_questionStore_Query→SYS_QUESTION | `REQ-xaha_cont-sys_questionStore_Query-Q` Q LemisBusinessMgrPoJo.query<br>`REQ-xaha_cont-sys_questionStore_List-U` M LemisBusinessMgrPoJo.update |
| **xaha_dialog.jsp** | sys_questionStore→SYS_QUESTION<br>sys_questionStore_List→SYS_QUESTION<br>sys_questionStore_Query→SYS_QUESTION | `REQ-xaha_dialog-sys_questionStore-Q` Q LemisBusinessMgrPoJo.query<br>`REQ-xaha_dialog-sys_questionStore-M` M LemisBusinessMgrPoJo.update |
| **xaab_cont.jsp** | aa12Store→AA12<br>aa12Store_M→AA12 | `REQ-xaab_cont-aa12Store-Query` Q LemisBusinessMgrPoJo.query<br>`REQ-xaab_cont-aa12Store-Del` M XaabService.doDelete<br>`REQ-xaab_cont-aa12Store-M` M XaabService.doUpdate |
| **xaad_cont.jsp** | aa10Store_Query→AA10<br>aa10Store_QueryList→AA10 | `REQ-xaad_cont-aa10Store_QueryList-Query` Q XaadService.doQuery<br>`REQ-xaad_cont-aa10Store_Delete` M LemisBusinessMgrPoJo.update<br>`REQ-xaad_cont-aa10Store_Save` M LemisBusinessMgrPoJo.update<br>`REQ-xaad_cont-aa10Store_Query-Query` Q LemisBusinessMgrPoJo.query ✦validate |
| **xaac_cont.jsp** | aa01Store→AA01 | `REQ-xaac_cont-aa01Store-Save` M LemisBusinessMgrPoJo.update<br>`REQ-xaac_cont-aa01Store-Query` Q LemisBusinessMgrPoJo.query |

## 3. 表 → datastore 映射

| 表 | store 数 | 主键 | 说明 |
|---|---:|---|---|
| AA01 | 1 | aaa010 | 综合参数信息 |
| AA10 | 2 | aaa100, aaa102 | 代码表 |
| AA12 | 2 | id | 行政区划表 |
| SYS_QUESTION | 4 | qseq | 实施问题记录信息表 |

## 4. xa Java 代码清单

| 文件 | 角色 |
|---|---|
| `src/main/java/com/neusoft/lemis/apps/lemis/xa/a/XaaAjax.java` | Ajax 通道 bean（applicationContext.xml 注册，前端 `doAjax("xaaAjax", dc, …)` 直调） |
| `src/main/java/com/neusoft/lemis/apps/lemis/xa/a/XaaService.java` | ⚠️ 全库零装配引用（无 Spring bean、无页面交互、无 JS 调用）——**死代码候选，重写跳过** |
| `src/main/java/com/neusoft/lemis/apps/lemis/xa/a/b/XaabAjax.java` | 单位 Ajax 通道 bean（JS `doAjax("xaabAjax")`，含保存回调链） |
| `src/main/java/com/neusoft/lemis/apps/lemis/xa/a/b/XaabService.java` | 单位服务：doUpdate/doDelete/getXaabAjax/doInHibernate（LemisBusinessMgr 子类，页面交互绑定） |
| `src/main/java/com/neusoft/lemis/apps/lemis/xa/a/d/XaadAjax.java` | 人员 Ajax 通道 bean（JS `doAjax("xaadAjax")`） |
| `src/main/java/com/neusoft/lemis/apps/lemis/xa/a/d/XaadService.java` | 人员服务：doQuery/getXaadAjax（LemisBusinessMgr 子类，页面交互绑定） |

## 5. 行为规格要点（重写验收口径）

1. **标准链路占绝对多数**：交互 `Q→query` / `M→update` 全部走通用 `LemisBusinessMgrPoJo`，
   行为由列元数据驱动（nullable→非空校验、precision→长度校验中文×2）——重写时由
   `GenericCrudDispatcher` + `ColumnMeta` 承接，xa 不需要写一行业务代码。
2. **仅 2 处自定义逻辑**：
   - `REQ-xaab_cont-aa12Store-Del → XaabService.doDelete`（单位删除，需读源码确认是物理删还是状态删）
   - `REQ-xaad_cont-aa10Store_QueryList-Query → XaadService.doQuery`（人员查询，含自定义过滤）
3. **validate 标志**：仅部分 M 交互开启 `metadata_validate=true`——重写网关要保持逐交互配置。
4. **主从表**：dialog 页普遍「主 store + `.` 从 store」结构（如 ac01Store.ac07Store），
   走基类 update 的外键回填逻辑——PG 侧同一事务语义。
5. **涉及表仅 4 张**：AA01 综合参数、AA10 代码表（复合主键 aaa100+aaa102）、AA12 行政区划、SYS_QUESTION 实施问题记录——xa 是最小业务面模块，试点风险最低。
6. **Ajax 旁路通道**：页面交互之外，前端还有 `doAjax(beanName, dc)` 直调 Spring bean（xaaAjax/xaabAjax/xaadAjax，经 `/ajaxutil` servlet）——重写的交互网关要同时覆盖两条通道。

## 6. 开工清单（xa 试点）

1. Kotlin 骨架：`com.jlrc.rcpq` 四模块（web/service/entity/report），Thymeleaf 页面形态
2. 实体生成：按 §3 表清单跑 hbm→Kotlin data class/JPA 实体生成器（含 56 复合主键的通用处理）
3. `GenericCrudDispatcher`：query/update/count/validate 四操作 + 交互网关路由表（§2 清单直接生成）
4. 两个自定义用例：`XabUnitService.doDelete/doUpdate`、`XadPersonService.doQuery`（读旧源码定语义）
5. 行为对照：备份库导 AA12/AA10/AA01/SYS_QUESTION 样本数据 → 旧新系统同请求 diff

## 附录 A：自定义用例语义（2026-11-06 补充，源码实读）

**XaabService**（125 行，AA12 行政区划）：
- `doUpdate(dc)`：`bulkUpdate(update_aa12)` + `saveOrUpdate(aa12)` + `flush` —— 整行保存与部分列更新混合（bulkUpdate 为手写 SQL）
- `doDelete(dc)`：**软删除**。先 `executeFind` 查从属数据 → `find(cldSQL)`/`find(cldSQL_1)` → `delete(从属记录首条)` → 两次 `bulkUpdate(update_aa12)` 更新状态字段。**非物理删除**，重写时 SQL 原样迁移并对照

**XaadService**（58 行，AA10 代码表）：
- `doQuery(dc)`：自定义 HQL `find(hql)` 查询列表（非元数据标准查询）

> 两个用例的 HQL/SQL 原文在 Java 源码内嵌，重写实现时逐句迁移（TODO-BEHAVIOR）。
