# 彩虹人才管理系统 · 重写决策记录（ADR）

> 2026-11-05 敲定并锁定。依据：docs/ 下六份前置分析（class-hierarchy / LemisBusinessMgr-contract /
> url-entry-points / entity-table-map / neusoft-copyright-inventory / database-migration-inventory）。
> **系统名称：彩虹人才管理系统**（原东软 Lemis 人才派遣管理系统的规格重写）。

---

## D1 目标技术栈

| 领域 | 决策 | 替代对象 |
|---|---|---|
| 报表 | **JasperReports**（模板按新引擎重做） | unieap-report 套件 9 jar |
| Excel 导入导出 | **EasyExcel** | IOUtil/IOUtil1/IOUtilTxt 的 60+ 个硬编码 Excel 方法 |
| 工作流 | **不迁移引擎**，沿用现有「状态字段 + 确认服务」模式在服务层显式实现审批 | unieap-workflow 9 jar + 19 张 BT_* 表（业务代码零引用） |
| 调度 | **Spring `@Scheduled`**（3 个 Job 改 Kotlin 定时任务） | Quartz 1.6 + QRTZ_* 12 表 + lemis.scheduler |
| 前端 | **渐进式**：xa 试点先用服务端模板形态（Thymeleaf）打通全链路与行为对照，核心模块稳定后再评估 SPA | JSP + Dojo/unieap-ria 定制层 |
| 基础框架 | Kotlin + Spring Boot 3 + Spring Data JPA（hibernate 6） | Spring 2.0.4 / Struts 1.2.7 / Hibernate 3.5.6 / UniEAP 3.x |

## D2 数据库：PostgreSQL（锁定）

- 版本基准：PostgreSQL 16
- 论据：373 实体 sequence 主键（PG 原生零改动）、PL/SQL→PL/pgSQL 语法距离最近、中文标识符规范、ora2pg 工具链
- 迁移范围见 database-migration-inventory.md：约 550 表 + 82 视图 + 664 序列

## D3 零引用表（330 张）：**不删，只不迁**

- 迁移 DDL 与数据时直接跳过；原表留在 Oracle 备份库（172.16.0.60）天然归档
- 上线稳定一个业务月周期后再评估是否清理源库
- 例外条款：若后续发现某表被动态 SQL/外部系统使用，按「用到再补」处理（同 D4）

## D4 边缘模块：**全部暂不迁移，用到再补**

| 模块 | 内容 | 状态 |
|---|---|---|
| wts 外网申报 | 18 页面 + WtsService + 7 张 WTS_* 表 + PKG_RCPQ_WY | 暂不迁移 |
| TKTools GIS | cn.com.egova 混淆 16 类（仅 1 个 JS 引用） | 暂不迁移（版权不明，永不复制源码） |
| lemis_ext 触摸屏 | 4 类 | 暂不迁移 |
| log 模块 | 9 类 + LOG_* 7 表 | 暂不迁移 |
| scheduler | 5 类 + 3 Job | 暂不迁移（Job 业务如需，转为 Spring 定时任务补回） |
| plugins | LogonHistory / UserInfo 两插件 | 暂不迁移（功能内聚时再实现） |
| unieap console/desktop 前端 | 平台管理控制台 | 不迁移（新系统自带管理页） |

> 含义：新系统初版**不含**上述功能；每补一个模块走一次增量设计，禁止从旧源码翻译。

## D5 命名空间与系统名（版权清洗规格）

### 5.1 名称

| 项 | 值 |
|---|---|
| 系统名称 | **彩虹人才管理系统** |
| Java 包根 | **`com.jlrc.rcpq`**（jlrc / rcpq 均为中性词，无东软归属） |
| Artifact/工程名 | `rainbow-rcpq`（模块：`rcpq-web` / `rcpq-service` / `rcpq-entity` / `rcpq-report` …） |
| web.xml 对应物 | `display-name=彩虹人才管理系统` |

### 5.2 禁词表（新代码库零出现）

`lemis`、`neusoft`、`unieap`、`东软`、`沈阳东软`、`fgm`、`egova`、`LEMIS_ERRNO`（改新枚举但**保留错误码数值**）、`Neusoft Co.,Ltd.` 等署名。

### 5.3 核心类改名映射

| 旧 | 新 |
|---|---|
| `LemisBusinessMgr` | `GenericCrudDispatcher` |
| `LemisSQLMgr` | `SqlQueryExecutor` |
| `LemisPojoMgr`（unieap PojoMgr 契约） | `PojoCrudExecutor` |
| `JspBean` / `InteractionBean` | `PageSpec` / `InteractionSpec` |
| `PojoDataStoreBean` / `MetaDataBean` / `CustomDataStoreBean` | `DataSetSpec` / `ColumnMeta` / `CustomSqlSpec` |
| `IDataCenter` / `IDataStore` / `IMetaData` | `InteractionContext` / `DataSet` / `MetaSchema` |
| `LemisBusinessException` / `LemisApplicationException` / `LemisException` | `BizException` / `AppException` / `BaseException`（错误码数值不变） |
| `LemisPojoUtil` | `PojoReflections` |
| 实体类（AA01/TC90/TJ01…） | **保留短名**（与表名一一对应，利于机械生成与对账） |
| 存储过程中文包名 | 迁移时改英文 + 建映射表（如 `PKG_费用结算` → `pkg_fee_settle`），映射表入 docs |

### 5.4 页脚/署名

所有 UI 署名改为甲方主体（甲方全称待提供，占位：`© 彩虹人才管理系统`）。

---

## 锁定状态

- ✅ D1–D5 已锁定；变更需在本文件追加「修订记录」
- 📌 待甲方输入（不阻塞开工）：甲方全称（页脚署名用）、工作流是否真实使用过流程定义（决定是否引入 Flowable，默认不引入）
- 产线动作顺序：xa 规格书 → naming 落地（本文档 §5）→ xa 试点（Thymeleaf 形态）→ 行为对照管线

---

## 修订记录

- **2026-11-09 v2**：模块重写顺序修订（依据 docs/module-inventory.md 定量盘点）——az 提前至波次 2（主档依赖）、td/g 提至波次 3（通用化验收）、td/i 调整至波次 9（纯通用零成本）。新顺序：xa✅ → az → td/g → td/a → td/b(拆两波) → td/l → td/c(分批) → td/d → td/h/i/n。原顺序记录保留于 git 历史。
