# LemisBusinessMgr 基类契约（31 个 Service 重写依赖）

> 2026-11-05 生成。来源：`cp33.jar` 内源码（CFR 反编译）+ javap 签名 + 31 个子类静态分析 + `src/main/resources` 全部 InteractionBean 装配扫描。

## 1. 类定位

`com.neusoft.lemis.cp33.pojo.impl.LemisBusinessMgr`（cp33.jar，2011 年，作者 fancy，注释自称「通用Service」）
是全系统业务层的**通用 CRUD 分发器**：

```
LemisBusinessMgr extends HibernateDaoSupport implements Validator, BusinessMgr
  ├─ 委托 PojoMgr   (unieap-ria,  Hibernate POJO 模式: query/update/doCount)
  └─ 委托 LemisSQLMgr (JdbcDaoSupport, SQL 模式: query/doCount)
```

unieap ria 框架的交互生命周期按 `(bean, method)` 调用：**validate → query/update → count**，
本类正是这两个接口的默认实现。31 个业务 Service 继承它获得整套 CRUD 分发能力，
再按需覆盖基类方法或追加自定义交互方法。

## 2. 公共 API（javap 权威签名）

| 方法 | 签名 | 语义 |
|---|---|---|
| `query` | `IDataCenter query(IDataCenter) throws LemisBusinessException` | 双模式分发：`dstype=SQL` → `sqlMgr.query(store)`；否则逐 store → `pojoMgr.query(store)`，结果 store 回填 dc |
| `update` | `IDataCenter update(IDataCenter) throws LemisBusinessException` | **主从表保存**：store 名含 `.` 为从表。主表先 `pojoMgr.update`，随后从表按 `SAVENOM2O` 参数选 `initPKNoM2O`/`initPK` 回填外键再保存。SQL 模式直接抛错（SQLDS 不能用于更新） |
| `count` | `IDataCenter count(IDataCenter) throws LemisBusinessException` | 双模式计数：SQL → `sqlMgr.doCount`；POJO → 对空 store 逐个 `pojoMgr.doCount` |
| `validate` | `ValidateResult validate(IDataCenter) throws LemisBusinessException` | 元数据驱动校验：非 `nullable` 非空校验（新记录跳过主键）；长度校验按精度，**中文等非 ASCII 字符计 2** |
| 属性 | `get/setPojoMgr(PojoMgr)`、`get/setSqlMgr(LemisSQLMgr)` | Spring 注入的两个委托 |

继承自 `HibernateDaoSupport`（服务里实际用到的）：`getHibernateTemplate()`（最常用）、`getSession()`。

### 私有机制（重写需保留语义）

- `initPK(dc, subStoreName)`：一对多从表外键回填——取主表单条记录主键值，写入从表每个级联对象的 `外键属性.主键名`（外键属性名 = 主表 rowSetName 类名小写）
- `initPKNoM2O`：同上但直接写 `主键名`（非级联导航形式），由 `SAVENOM2O` JSON 参数里的 store 名清单切换

## 3. 装配模型（Service 如何被调用）

每个 JSP 页面 = 一个 `JspBean`（datastores + interactions）。每个交互 `InteractionBean`：
`code=REQ-<页面>-<store>-<Q/M>`、`type=Q/M`、`bean=<Spring bean 名>`、`method=<方法名>`。
- 标准页面直接绑 `LemisBusinessMgrPoJo`（基类单例 bean）+ `query`/`update`/`count` —— **31 个 Service 都不参与**
- 自定义页面绑自定义 Service（bean 名 `模块前缀_类名`，如 `tdaa_tdaaService`）+ 其**自定义方法**
- 自定义方法签名约定统一：`public IDataCenter xxx(IDataCenter dc) throws LemisBusinessException`

## 4. 31 个子类契约矩阵

| Service | 模块 | 覆盖基类 | 自定义交互方法 | 挂接交互数 | 基类/模板调用 |
|---|---|---|---|---:|---|
| AzbaSave | az | — | getProcedureDAOUtil, getSequenceDAOUtil, save | 1 | HBT×5 |
| AzbaService | az | — | doUpdate, doSave, changeJbxx | 2 | HBT×9 |
| Azbd | az | — | getProcedureDAOUtil, getSequenceDAOUtil, del | 1 | HBT×3 |
| Azbe | az | — | getProcedureDAOUtil, getSequenceDAOUtil, save | 1 | HBT×6 |
| GetSysLobAjax | common | — | doAjax | 0 | HBT×1 |
| SaveSysLobAjax | common | — | doAjax, getGbk | 0 | HBT×4 |
| TdbcService | td/b | — | changeJbxx, saveUpdate, getDt, getHz | 3 | HBT×3 |
| TdclService | td/c | update | — | 1 | HBT×1 |
| WtsService | wts | — | wtsaasave, wtsabsave, wtsafsave | 3 | super×3 |
| XaabService | xa | — | getXaabAjax, doUpdate, doInHibernate, doDelete | 2 | HBT×6, super×2 |
| XaadService | xa | — | getXaadAjax, doQuery | 1 | HBT×1 |
| azaaService | az | update | getProcedureDAOUtil, doSave | 2 | HBT×2, super×1 |
| azabService | az | update | getProcedureDAOUtil | 1 | HBT×1 |
| azbbService | az | update | getProcedureDAOUtil | 1 | HBT×1 |
| tdaaService | td/a | — | doSave, doCommit | 2 | HBT×2 |
| tdabService | td/a | — | doContinue | 1 | HBT×1 |
| tdbaService | td/b | update | doCommit | 2 | HBT×2 |
| tdbbService | td/b | update | getProcedureDAOUtil, doNewHt, doTerminate, doZhongzhi | 4 | HBT×4 |
| tdbdService | td/b | — | getProcedureDAOUtil, copyProperty, doNew, doJbbg, doRenewAll, doTerminateCancle, doJsdy, doBg, doCbSh | 7 | HBT×7 |
| tdbeService | td/b | update | — | 1 | HBT×1 |
| tdbfService | td/b | — | getProcedureDAOUtil, doXinqCommit, doXinqCancle, doRenewCancle, doJsdyCancle, terminateCancel, reCommit | 6 | HBT×6 |
| tdblService | td/b | update | copyProperty | 2 | HBT×1 |
| tdcaService | td/c | — | delete, delete1 | 2 | HBT×2 |
| tdcaeService | td/c | — | delete, delete1 | 2 | HBT×2 |
| tdcahService | td/c | — | delete, delete1, doSave | 9 | HBT×3 |
| tdcajService | td/c | — | delete, delete1, doSave | 3 | HBT×3 |
| tdcjService | td/c | query,update | saveNobt, saveConfirm, AllConfirm | 3 | HBT×10, super×2 |
| tdckService | td/c | — | delete, delete1, doSave | 3 | HBT×3 |
| tddaService | td/d | update | updateAa90 | 2 | HBT×2 |
| tdddService | td/d | update | getProcedureDAOUtil, doSaveTc93 | 2 | HBT×2 |
| tddfService | td/d | — | doSaveAa80, doSaveAa81 | 2 | HBT×2 |


**覆盖统计**：覆盖 `update` 11 个（TdclService, azaaService, azabService, azbbService, tdbaService, tdbbService, tdbeService, tdblService, tdcjService, tddaService, tdddService）；覆盖 `query` 1 个（tdcjService）；全部 31 个类都有自定义方法或覆盖，无「纯继承」空壳。

## 5. 未挂接 InteractionBean 的类

| 类 | 结论 |
|---|---|
| GetSysLobAjax / SaveSysLobAjax | 在根 `applicationContext.xml` 中注册，走 Ajax 分发通道而非页面 InteractionBean |
| Azbd / Azbe | **已确认挂接**（全库入口盘点发现 `azbd`/`azbe` 小写 bean 名，交互 `azbd.del`、`azbe.save`）——非死代码 |

## 6. Kotlin 重写建议

1. **基类 → 一个通用分发器接口**：`LemisBusinessMgr` 的 query/update/count/validate ≈ 一个
   `CrudDispatcher<IC>`（IC = 交互上下文，等价 IDataCenter）。SQL/POJO 双模式 → 两个实现（JdbcTemplate / ORM）。
2. **主从表保存是核心不变式**：`update` 的「主表先存 + 从表外键回填（initPK 两种形态）+ SQL 禁更新」
   必须原样保留，否则几十个页面交互会静默坏掉。
3. **validate 的两条规则是业务语义**：非空校验（新记录免主键）、长度按精度（非 ASCII 计 2）——重写成纯函数。
4. **自定义交互方法 = 显式用例清单**：矩阵中的每个自定义方法（doSave/doCommit/doTerminate/delete/
   delete1/wtsaasave…）对应一个重写后的显式用例方法，可作重写验收清单；签名统一 `IDataCenter→IDataCenter`，
   重写时改为强类型输入输出。
5. **`getProcedureDAOUtil` 高频出现**（8 个 Service）：存储过程调用是这批 Service 的通用依赖，
   重写需要对应的 Procedure 通道（Spring `SimpleJdbcCall` 或裸 CallableStatement 封装）。
6. **`td.b` 模块业务操作最密集**：doNewHt/doTerminate/doZhongzhi/doRenewAll/doXinqCommit…
   （合同新签/终止/中止/续签），是 td/b 重写顺序靠前的主要工作量。
