# 东软版权资产盘点（重写必改清单）

> 2026-11-05 生成。目的：为跨语言规格重写列出所有带东软（Neusoft）版权属性的资产——
> **这些内容不得以任何形式出现在重写后的代码库中**。分五层：授权体系、Jar、源码命名、UI 资源、配置部署。

---

## 1. ⚠️ 授权/License 体系（最高优先级：整条链不得进入重写）

系统内有两套**完全独立**的东软授权校验（详见 `docs/许可证绕过说明.md`）：

### 1.1 UniEAP 平台授权（unieap-platform.jar）

| 组件 | 说明 |
|---|---|
| `com.neusoft.unieap.misc.protection.CustomCheck` | **运行时授权校验入口**：`EAPActionServlet` 每请求经 `ProtectionSupport.checkLicense` → `com.jp.protection.LicenseHost` 校验，失败时 `ProtectionConfig.errMsg` 非 null → 全站输出 `License expired` |
| `com.neusoft.unieap.misc.license.Check` | 工具端 license 文件格式校验（LicenseCommand 上传时调用） |
| `ProtectionSupport` / `ProtectionConfig` | 平台侧授权支撑类（同 jar） |
| `Protection.jar` / `ProtectionPriv.jar` | **第三方商业授权 SDK**（`com.jp.protection.pub.LicenseHost`），东软购入用于限制用户 |

### 1.2 报表引擎授权（unieap-report.jar）

| 组件 | 说明 |
|---|---|
| `com.neusoft.report.engine.security.licence.ProtectionCheckTask` | `getProtectionInfo()` 非 0 即抛 `License expired`（报表全功能入口 `RAOMgr.getPermitRAO` 调用） |
| `com.neusoft.report.engine.security.licence.ProtectionManager` | **版本功能限制**：`checkEdition()` 不满足抛 `(1006)该版本此功能限用`，10+ 处报表功能调用 |

### 1.3 相关文件与目录（重写仓库中应全部不出现）

| 路径 | 性质 | 处理 |
|---|---|---|
| `deploy/license-bypass/`（4 个假类：CustomCheck/Check/ProtectionManager/ProtectionCheckTask） | **现有系统**的运行时绕过补丁 | 重写后无授权代码，**整目录不迁移**；保留在本仓库仅为维护旧系统 |
| `docs/许可证绕过说明.md` | 绕过机制分析文档 | 本仓库留档；重写仓库不迁移 |
| `unieap.license` 授权文件 | 东软签发的 license | 已过期（bak 最新 2026-03），**不迁移** |

**重写原则**：新系统不实现任何 license/防拷贝机制，上述类名、包名、校验逻辑一律不存在，也不引入 Protection SDK。

---

## 2. Jar 包清单（WEB-INF/lib 共 232 个）

### 2.1 东软版权 jar（56 个，重写必须全部移除——功能重新实现或用开源替代）

| 组 | 数量 | jar |
|---|---:|---|
| UniEAP 平台 | ~16 | `unieap-platform{,-manager,-ria,-riautils,-cache-3.0,-commontree-3.0,-datawindow-3.1,-drm-3.0,-eapservices-3.0,-ejb-3.0,-service-3.0,-serviceType-3.0,-spring-bean-3.0,-comm}.jar`、`unieap-connection{,-config}.jar`、`unieap-metadata.jar` |
| 报表引擎 | 9 | `unieap-report{,-datasource,-design,-function,-manage,-property,-rbcpool,-variable,-xmlquerier}.jar` |
| 工作流 | 9 | `unieap-workflow-{common,engine,interface,org,servermodel,service,toollistener,wsdl,wsdltool}.jar` |
| 查询/数据集 | 9 | `unieap-query{,-manage,-templet,-instance,-def-design,-man}?`、`unieap-dataset{,-design}.jar`、`unieap-uniquery-design.jar`、`unieap-queryinstance.jar` |
| 其他平台件 | ~8 | `unieap-riautils.jar`、`unieap-scheduler.jar`、`unieap-transaction.jar`、`unieap-integrate-3.0.jar`、`unieap-rbc-connection-3.0.jar`、`unieap-bpel-client.jar`、`extremecomponents-1.0.1-M4-unieap.jar`（开源 extremecomponents 的东软改版） |
| lemis 插件 | 6 | `lemisCalendar.jar`、`lemisDP_ApplicationIcon.jar`、`lemisHelper.jar`、`lemisLogonHistory.jar`、`lemisUserInfoPlugin.jar`、`lemisUserPassword.jar` |
| 业务包 | 1 | `cp33.jar`（**含本系统核心基类** LemisBusinessMgr/LemisSQLMgr/PojoMgr/JspBean/InteractionBean 交互框架） |
| 授权 SDK | 2 | `Protection.jar`、`ProtectionPriv.jar`（随授权链一起移除） |

### 2.2 非东软但同样有版权风险的 jar/源码

| 项 | 性质 | 处理 |
|---|---|---|
| `TKTools/`（16 类，`cn.com.egova.*`） | **混淆的第三方 GIS 工具包**（egova），非东软但版权不明 | 不迁移；GIS 功能评估开源替代（GeoTools 等）或弃用 |
| `menu/com.fgm.web.*`（11 类） | **struts-menu 开源项目的第三方换皮重打包**（MenuDisplayer/MenuRepository/DisplayMenuTag 等均为 struts-menu 类名） | 不迁移；重写用前端菜单方案替代 |
| `fr`（4 类）、`cn`（15 类）、`Menus`、`lemisUserInfoPlugin`、`lemisLogonHistory` 顶层目录 | 反编译时落在源码树根的杂项 | 逐个甄别归属 |

### 2.3 可保留的开源 jar（老版本，重写时升级为新版坐标）

Spring 2.0.4、Struts 1.2.7、Hibernate 3.5.6、Axis、commons-*、c3p0、cglib、acegi-security 0.8.3（Spring Security 前身）、jfreechart/cewolf、batik、fop、itext 等约 170 个——这些是开源组件，重写换成现代等价物（Spring Boot 3 体系自带大部分）。

---

## 3. 源码命名空间（670 个 Java 文件中 633 个带 `com.neusoft` 包名）

### 3.1 包名替换（全部业务代码）

| 现包名 | 文件量级 | 重写建议 |
|---|---:|---|
| `com.neusoft.lemis.apps.entities.*` | 517 | 新包名（如 `<新域>.entity.<域>`），由生成器直接产新名 |
| `com.neusoft.lemis.apps.lemis.{xa,az,td,wts,common,sc,servlet,...}` | ~70 | 新包名；模块边界已由 docs/url-entry-points.md 明确 |
| `com.neusoft.lemis.cp33.*` | 9 | 基类契约重写后并入新框架层 |
| `com.neusoft.lemis.{log,scheduler,servlet,plugins,lemis_ext,unieap}` | ~15 | 新包名 |
| `com.neusoft.unieap.ria.{pojo.util,util}` | **2** | ⚠️ **反编译进 src 的框架源码**（PojoUtil、ConvertionUtil）——直接复制了东软框架代码，重写中按契约重实现，禁止带走 |
| `TKTools`、`menu`、`lemisUserInfoPlugin`、`lemisLogonHistory` | 37 | 见 2.2 |

### 3.2 类名级替换（东软命名贯穿全系统的高频符号）

| 类别 | 类名 |
|---|---|
| 基类/框架 | `LemisBusinessMgr`、`LemisSQLMgr`、`LemisPojoMgr`、`DataQueryPolicyMgr`、`PojoMgr`(unieap) |
| Servlet | `LemisServlet`、`ScreenServlet`、`LemisDownloadServlet`、`LemisCommonServlet`、`LemisSysHelpServlet`、`LemisPluginServlet`、`LemisReloadContextServlet` |
| 异常/工具 | `LemisException`、`LemisBusinessException`、`LemisApplicationException`、`LemisPojoUtil`、`IOUtil/IOUtil1/IOUtilTxt` |
| 实体/POJO | `PojoEntity`、`PojoList`、`IDataCenter`、`IDataStore`、`IMetaData`（unieap ria 接口——新系统设计自己的交互模型） |
| 页面模型 | `JspBean`、`InteractionBean`、`PojoDataStoreBean`、`MetaDataBean`、`CustomDataStoreBean` |

> 错误码常量（`Constants.LEMIS_ERRNO_*`）、交互码前缀 `REQ-*` 属于行为语义，重写时**换命名保语义**。

---

## 4. UI 文案与静态资源

### 4.1 版权页脚（重写界面必须替换为甲方署名）

| 文件:行 | 文案 |
|---|---|
| `lemis/pages/skins/skin1/index.jsp:229` | 东软集团股份有限公司 |
| `unieap/pages/login/locking.jsp:85` | ©版权所有 沈阳东软软件股份有限公司 Copyright By Neusoft Co.,Ltd. |
| `unieap/pages/workflow/welcome.jsp:65` | 版权所有 © 2010 东软集团 |

### 4.2 图片资源

- `unieap/images/logo.gif`、`unieap/style/images/logo.gif`（平台 logo）
- `unieap/images/example/neusoft.gif`、`unieap/images/example/logo_neusoft.gif`（东软标示例图）
- `lemis/Resources/logo/applogo.png`、`console/Resources/logo/applogo.png`（应用 logo）

### 4.3 整目录级 UI 资产（东软前端框架层）

- `webapp/unieap/**`：UniEAP 3.x 前端层（Dojo ria3.3 定制 dijit、datawindow、报表交互页、登录/锁屏页）——重写用新前端栈（如 Vue/React）整体替代，**不复制 JS**
- `webapp/lemis/pages/skins/**`：皮肤层（含版权页脚）
- `webapp/console/**`、`webapp/desktop/**`：UniEAP 控制台与桌面组件

---

## 5. 配置与部署层

| 位置 | 内容 |
|---|---|
| `web.xml` | `<display-name>unieap3.0</display-name>` → 新应用名 |
| `OPConnectionPool.xml` 等数据源配置 | 数据源名 `UNIEAP`、库名 `hzjnpq` 等平台命名（commit 4fdcedc） |
| `WEB-INF/conf/{unieap,lemis,console}` | 三套配置树，key/节点名带 unieap 命名 |
| 交互/datastore XML（2154 个） | bean 类名全部指向 `com.neusoft.lemis.cp33.pojo.*`——重写后由新框架的路由表替代（见 url-entry-points.md 第 6 节） |

---

## 6. 处理策略汇总

| 层 | 策略 |
|---|---|
| License 体系 | **不迁移、不重实现**。新系统从架构上就不存在授权校验；Protection SDK 与 4 个绕过类留在旧仓库维护旧系统 |
| 东软 jar（56 个） | 不迁移。平台能力（RIA 交互/报表/工作流/调度）按契约重实现或选型开源（JasperReports/Flowable/Quartz 等） |
| `com.neusoft.*` 包名 | 100% 换新命名空间；类名按 3.2 语义改名 |
| 混进 src 的框架源码（2 个 unieap 类 + `$_` 辅助类） | 不迁移 |
| UI | 页脚署名换甲方；logo 换新；前端整体新栈 |
| 数据库 schema | 表结构不动（重写验证锚点），但 `UP_*/SYS_*` 框架表的新系统归属（框架表重建 vs 沿用）需在重写设计时决策 |
