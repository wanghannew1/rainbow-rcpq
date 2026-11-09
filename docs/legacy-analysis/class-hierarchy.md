# rainbowpq 类继承关系树

> 2026-11-05 由静态解析生成（正则提取 package/extends/implements，非编译期结果，泛型边界已忽略）。

## 总览

- Java 文件：670，顶层类型声明：658（class 654、interface 3、enum 1）


| 包 | 类数 |
|---|---:|
| apps.entities | 517 |
| apps.lemis | 46 |
| apps.common | 16 |
| cn | 15 |
| com.fgm.web.menu | 11 |
| com.neusoft.lemis.cp33 | 9 |
| com.neusoft.lemis.log | 9 |
| apps.sc | 7 |
| com.neusoft.lemis.scheduler | 5 |
| apps.lemis_ext | 4 |
| apps.servlet | 4 |
| fr | 4 |
| com.neusoft.unieap.ria | 2 |
| Menus | 2 |
| com.fgm.web.resources | 2 |
| apps.(根) | 1 |
| apps.querypolicy | 1 |
| com.neusoft.lemis.servlet | 1 |
| com.neusoft.lemis.plugins | 1 |
| com.fgm.tracker.apps | 1 |


## 1. 继承树（项目内根，子树 ≥2 节点）

共 3 棵。子树按规模排序。

```
 (**MenuDisplayer** · `menu.displayer`)
└──  (**MessageResourcesMenuDisplayer** · `menu.displayer`)
    ├──  (**DropDownMenuDisplayer** · `menu.displayer`)
    ├──  (**HiermenusDisplayer** · `menu.displayer`)
    └──  (**SimpleMenuDisplayer** · `menu.displayer`)

 (**ProcedureJob** · `scheduler.job` · impl: Job)
├──  (**AsWebFormHrToWb** · `scheduler.job`)
└──  (**doUpdateTc90** · `scheduler.job`)

 (**MenuBase** · `web.menu`)
└──  (**MenuComponent** · `web.menu`)

```

## 2. 继承树（外部根）

根节点是外部框架/JAR 基类（HttpServlet、Spring DAO 支持类、Struts Action 等），项目类作为其直接或间接子类。

```
LemisBusinessMgr（外部基类）
├──  (**GetSysLobAjax** · `apps.common.ajax` · impl: SimpleAjax)
├──  (**SaveSysLobAjax** · `apps.common.ajax` · impl: SimpleAjax)
├──  (**azaaService** · `apps.lemis.az.a.a.service`)
├──  (**azabService** · `apps.lemis.az.a.b.service`)
├──  (**AzbaSave** · `apps.lemis.az.b.a`)
├──  (**AzbaService** · `apps.lemis.az.b.a`)
├──  (**azbbService** · `apps.lemis.az.b.b.service`)
├──  (**Azbd** · `apps.lemis.az.b.d`)
├──  (**Azbe** · `apps.lemis.az.b.e`)
├──  (**tdaaService** · `apps.lemis.td.a.a.service`)
├──  (**tdabService** · `apps.lemis.td.a.b.service`)
├──  (**tdbaService** · `apps.lemis.td.b.a.service`)
├──  (**tdbbService** · `apps.lemis.td.b.b.service`)
├──  (**TdbcService** · `apps.lemis.td.b.c.service`)
├──  (**tdbdService** · `apps.lemis.td.b.d.service`)
├──  (**tdbeService** · `apps.lemis.td.b.e.service`)
├──  (**tdbfService** · `apps.lemis.td.b.f.service`)
├──  (**tdblService** · `apps.lemis.td.b.l.service`)
├──  (**tdcaService** · `apps.lemis.td.c.a.service`)
├──  (**tdcaeService** · `apps.lemis.td.c.ae.service`)
├──  (**tdcahService** · `apps.lemis.td.c.ah.service`)
├──  (**tdcajService** · `apps.lemis.td.c.aj.service`)
├──  (**tdcjService** · `apps.lemis.td.c.j.service`)
├──  (**tdckService** · `apps.lemis.td.c.k.service`)
├──  (**TdclService** · `apps.lemis.td.c.l.service`)
├──  (**tddaService** · `apps.lemis.td.d.a.service`)
├──  (**tdddService** · `apps.lemis.td.d.d.service`)
├──  (**tddfService** · `apps.lemis.td.d.f.service`)
├──  (**XaabService** · `apps.lemis.xa.a.b`)
├──  (**XaadService** · `apps.lemis.xa.a.d`)
└──  (**WtsService** · `apps.lemis_ext.wts.service`)

HibernateDaoSupport（外部基类）
├──  (**CommonUniqueService** · `apps.common.ajax` · impl: SimpleAjax)
├──  (**PhotoSaveAjax** · `apps.common.ajax` · impl: SimpleAjax)
├──  (**SimpleGetValueAjax** · `apps.common.ajax` · impl: SimpleAjax)
├──  (**CodeUtil** · `apps.common.be`)
├──  (**Update_state** · `apps.common.be` · impl: BusinessEventProcessor)
├──  (**Update_table** · `apps.common.be` · impl: BusinessEventProcessor)
├──  (**azaaFileAjax** · `apps.lemis.az.a.a.ajax` · impl: SimpleAjax)
├──  (**TabaAjax** · `apps.lemis.td.b.a.ajax` · impl: SimpleAjax)
├──  (**tdbaAjax** · `apps.lemis.td.b.a.ajax` · impl: SimpleAjax)
├──  (**tdboAjax** · `apps.lemis.td.b.o.ajax` · impl: SimpleAjax)
├──  (**adcaaAjax** · `apps.lemis.td.c.aa.ajax` · impl: SimpleAjax)
├──  (**tdckAjax** · `apps.lemis.td.c.k.ajax` · impl: SimpleAjax)
├──  (**tddaAjax** · `apps.lemis.td.d.a.ajax` · impl: SimpleAjax)
├──  (**tddcAjax** · `apps.lemis.td.d.c.ajax` · impl: SimpleAjax)
├──  (**tddgAjax** · `apps.lemis.td.d.g.ajax` · impl: SimpleAjax)
├──  (**tddvServiceAjax** · `apps.lemis.td.d.v.ajax` · impl: SimpleAjax)
├──  (**XaaAjax** · `apps.lemis.xa.a` · impl: SimpleAjax)
├──  (**XaaService** · `apps.lemis.xa.a`)
├──  (**XaabAjax** · `apps.lemis.xa.a.b` · impl: SimpleAjax)
├──  (**WTSOrgDaoImpl** · `apps.lemis_ext.security` · impl: AuthenticationDao)
├──  (**QuickSearchService** · `cp33.service`)
├──  (**TreeCommonService** · `cp33.service`)
├──  (**TreeQueryService** · `cp33.service`)
└──  (**ImportUtil** · `utils.dao`)

HttpServlet（外部基类）
├──  (**LemisServlet** · `apps.sc`)
│   └──  (**ScreenServlet** · `apps.sc`)
├──  (**BlockFuncsServlet** · `egova.tktools`)
├──  (**FileDownLoadServlet** · `apps.servlet`)
├──  (**FileDownLoadServlet1** · `apps.servlet`)
├──  (**FileDownLoadServlet2** · `apps.servlet`)
├──  (**GetRemindServlet** · `apps.servlet`)
├──  (**LemisDownloadServlet** · `cp33.servlet`)
└──  (**TerminalRedirectServlet** · `lemis.servlet`)

EAPDispatchAction（外部基类）
├──  (**UserTree** · `apps.common.be`)
├──  (**UserTree_mod** · `apps.common.be`)
├──  (**ConstructTree** · `apps.common.tree`)
├──  (**azaaFileService** · `apps.lemis.az.a.a.service`)
├──  (**tddcService** · `apps.lemis.td.d.c.service`)
└──  (**tdgfService** · `apps.lemis.td.g.f.service`)

ActionForm（外部基类）
├──  (**azaaForm** · `apps.lemis.az.a.a.service`)
└──  (**TestForm** · `struts.test`)

TagSupport（外部基类）
├──  (**DisplayMenuTag** · `displayer.taglib`)
└──  (**UseMenuDisplayerTag** · `displayer.taglib`)

Action（外部基类）
├──  (**SaveAction** · `struts.test`)
└──  (**ViewAction** · `struts.test`)

LemisPojoMgr（外部基类）
└──  (**XaadAjax** · `apps.lemis.xa.a.d`)

DataQueryPolicyMgr（外部基类）
└──  (**Aab301_selfAndSub_QueryPolicy** · `apps.querypolicy`)

DefConstants（外部基类）
└──  (**Constants** · `lemis.cp33`)

NamedParameterJdbcDaoSupport（外部基类）
└──  (**LogConsumer** · `lemis.log` · impl: Runnable)

JdbcDaoSupport（外部基类）
└──  (**SchedulerManagerImpl** · `lemis.scheduler` · impl: SchedulerManager, ApplicationContextAware)

LemisPluginActivator（外部基类）
└──  (**Activator** · `plugins.logonhistory`)

ActionServlet（外部基类）
└──  (**ControllerServlet** · `apps.webtracker`)

Exception（外部基类）
└──  (**LoadableResourceException** · `web.resources`)

```

## 3. 接口

```
└──  (**LogService** · `lemis.log`)
└──  (**SchedulerManager** · `lemis.scheduler`)
└──  (**LoadableResource** · `web.resources`)
```

接口的实现者（含 implements 的类）：

- **ApplicationContextAware** ← SchedulerManagerImpl

- **AuthenticationDao** ← WTSOrgDaoImpl

- **BusinessEventProcessor** ← Update_state, Update_table

- **Filter** ← CacheFilter

- **ICodeList** ← PositionCodeImpl

- **Job** ← ProcedureJob

- **LoadableResource** ← MenuRepository

- **LogService** ← LogServiceImpl

- **Runnable** ← LogConsumer

- **SchedulerManager** ← SchedulerManagerImpl

- **Serializable** ← AA01, AA03, AA04, AA05, AA10, AA10_XAEG_CONT, AA11, AA12, AA70, AA71, AA72, AA73, AA74, AA75, AA76, AA77, AA78, AA80, AA81, AA82, AA83, AA90, AA91, AA92, AA93, AA94, AA95, AA96, AA97, AA97_BG, AA97_BP, AA97_DW, AA97_GR, AA98, AA99, AB01, AB01_CHILD, AB01_LB, AB06, AB07, AC01, AC01_BP, AC01_GX, AC01_LB, AC05, AC06, AC07, AC0B, AC0D, AC10, AC10_JS, AT70, AT71, AT72, CA05, CA11, CA111, CA112, CA13, CA14, CA16, CA18, CA21, CBJ_BP, CC0A, CC0P, CC78, CE10, CLIENT_PAD, JFSJ, JFSJ01, JFSJ02, JFSJ_MX, LOG_SCHEDULE, LogManager, SYSLOB, SYS_FILE, SYS_ORG_USER_UNIT, SYS_QUESTION, SYS_UNIT_TREE, TB10, TB11, TB12, TB13, TB14, TB15, TB16, TB17, TB18, TB19, TB70, TB71, TB7Z, TB84, TB86, TB87, TB88, TB89, TB8A, TB90, TB90_CONTACTS, TB91, TB92, TB93, TB93_CBJ, TB94, TB95, TB96, TB97, TB97_BP, TB97_BP01, TB97_BP02, TB98, TB99, TB99_BP, TB9A, TB9B, TB9C, TC01, TC02, TC03, TC04, TC04_BP, TC05, TC05_BP, TC06, TC06_BP, TC07, TC07_BP, TC07_LIST, TC08, TC09, TC09_BP, TC0A, TC0A_BP, TC0A_BP01, TC10, TC11, TC12, TC13, TC13_BP, TC13_PHOTO, TC14, TC15, TC16, TC17, TC20, TC21, TC30, TC30_BP, TC30_BP01, TC30_BP02, TC30_BP03, TC30_BP04, TC31, TC31_BP, TC32, TC32_BP, TC40, TC70, TC71, TC72, TC73, TC74, TC76, TC77, TC78, TC79, TC7A, TC7B, TC7C, TC7D, TC7E, TC7F, TC7G, TC7H, TC7I, TC7J, TC7K, TC7L, TC7M, TC7N, TC7O, TC7P, TC7Q, TC7R, TC7S, TC7V, TC7W, TC7X, TC7Y, TC7Z, TC83, TC84, TC85, TC86, TC87, TC88, TC89, TC8A, TC8B, TC8C, TC8D, TC8E, TC8F, TC8G, TC8G_BP, TC8H, TC8I, TC8I_LIST, TC8J, TC8K, TC8L, TC8M, TC8N, TC8P, TC8Q, TC8R, TC8S, TC8T, TC8U, TC8UMX, TC8U_CS, TC8V, TC8W, TC8Y, TC8Y_LIST, TC8Z, TC90, TC90_BG, TC90_BP, TC90_BP1, TC90_BP2, TC90_BP3, TC90_BP4, TC90_IMP, TC90_INNER, TC90_JFJS, TC90_JFJS_IMP, TC90_LIST, TC90_OUTTER, TC90_QDLS, TC90_TMP, TC90_YY, TC90_ZZ, TC91, TC91_BP, TC91_LOG, TC92, TC92_LIST, TC93, TC93_BP, TC93_BP1, TC93_BZ, TC93_CHECK, TC93_DB, TC93_INFO, TC93_LZ, TC93_PJGZ, TC93_SS, TC93_SSBP, TC93_TF, TC93_TMP, TC94, TC94_LZ, TC95, TC95_BP, TC96, TC96_TMP, TC97, TC97_BP, TC97_BP_WYH, TC97_RECORD, TC98, TC99, TC9A, TC9AA, TC9B, TC9C, TC9D, TC9D_FILE, TC9E, TC9F, TC9F_BP, TC9G, TC9H, TC9I, TC9J, TC9K, TC9L, TC9M, TC9N, TC9P, TC9Q, TC9Q_GS, TC9Q_LOG, TC9R, TC9R_BP, TC9S, TC9T, TC9U, TC9U_BP, TC9U_BP01, TC9U_BP02, TC9U_BP03, TC9V, TC9V_BP, TC9W, TC9W1, TC9W2, TC9W3, TC9W4, TC9W_BP, TC9W_BP01, TC9W_BP02, TC9W_BP03, TC9W_BP04, TC9W_BP05, TC9W_BP06, TC9W_BP07, TC9W_BP08, TC9W_BP09, TC9W_BP10, TC9W_HZ, TC9X, TC9Y, TC9Z, TD01, TD01_BP, TD02, TD03, TD04, TD04_BP, TJ000, TJ01, TJ011, TJ012, TJ013, TJ014, TJ016, TJ017, TJ01_EXPENDITURE, TJ02, TJ02_CS, TJ02_MSG, TJ031, TJ032, TJ04, TJ05, TJ06, TJ07, TJ071, TJ072, TJ0A, TJ0A1, TJ0B, TJ0B1, TJ0B2, TJ0B3, TJ0C, TJ0C1, TJ0C2, TJ0D, TJ0D1, TJ0E, TJ0E1, TJ0F, TJ0F1, TJ0G, TJ0G1, TJ0G2, TJ0Z, TJ0Z1, TJ0Z2, TJ0Z3, TJ0Z4, TJ201, TJ202, TJ203, TJ204, TJ205, TJ206, TJ207, TJ208, TJ209, TJ210, TJ211, TJ212, TJ841, TJ842, TJ843, TJ844, TJAA1, TJAA2, TJAB01, TJAC1, TJAC2, TJAG1, TJAJ1, TJAM1, TJAM2, TJAM3, TJAM4, TJCA1, TJCA2, TJCA3, TJCB1, TJCC1, TJCC2, TJCC3, TJCC4, TJDA3, TJDA4, TJDA5, TJDC1, TJPQ1, TJQC1, TJQC2, TJQC3, TJQC4, TJQC5, TJTK1, TJTK2, TJTK3, USER_INFO, V_AA12, V_AA97_AC10, V_AA97_NF, V_AA97_TC9T, V_AB01, V_AB01_AA03, V_AB01_AB07, V_AB01_TB90, V_AB01_TB90_TB93, V_AB01_TB93, V_AB01_TB96, V_AB01_TREE, V_AC01_TC97, V_AC10_MX, V_AC10_TC90, V_CBJ, V_CC78_TC9J, V_DW_EXPIRE, V_DW_EXPIRE_TX, V_DW_TB90, V_INVOICE_TC93, V_LAST_HT, V_SYS_MENU, V_TB93_AB07, V_TB93_TC93, V_TB93_TC9T, V_TB96, V_TB97_GR, V_TB98, V_TB99, V_TB9A, V_TB9A_GR, V_TB9A_YCX, V_TC02_TC90, V_TC03_TC90, V_TC04, V_TC8F, V_TC8I_LIST, V_TC8P, V_TC8T_TC8V, V_TC90, V_TC90_AB01_TB93, V_TC90_ADW, V_TC90_DFGZ, V_TC90_DZH, V_TC90_DZH2, V_TC90_HTBA, V_TC90_LIST, V_TC90_MAX, V_TC90_TB90_TB93, V_TC90_TB91, V_TC90_TC90LIST, V_TC90_TC97, V_TC90_XJ, V_TC90_XZGJJ, V_TC90_XZGS, V_TC90_XZSB, V_TC90_XZYB_1, V_TC90_XZYB_2, V_TC90_ZB, V_TC90_ZZ, V_TC91_TC90, V_TC93, V_TC93_AB01, V_TC93_AVG, V_TC93_BALANCE, V_TC93_TC9AA, V_TC93_YZS, V_TC97_DA, V_TC97_DSH, V_TC98_AC01, V_TC9A, V_TC9F, V_TC9Q_GS, V_TC9V, V_TC9W, V_TC9W1_AB01, V_TC9W_HZ, V_TD04_TB93, V_TJ02, V_UP_LEMIS_MENU, V_ZDGZFFQD, WBS_SCREEN, WTS_FUNCTION, WTS_LAYERS, WTS_MACHINE, WTS_NAVIGATION, WTS_POLICY, WTS_RESOURCE, WTS_STYLE_TEMPLATE, YTH_DWCB, YTH_DWJF, YTH_GRCB, YTH_GRJF

- **SimpleAjax** ← CommonUniqueService, GetSysLobAjax, PhotoSaveAjax, SaveSysLobAjax, SimpleGetValueAjax, TabaAjax, XaaAjax, XaabAjax, adcaaAjax, azaaFileAjax, tdbaAjax, tdboAjax, tdckAjax, tddaAjax, tddcAjax, tddgAjax, tddvServiceAjax


## 4. 枚举

- **INDEX_TYPE** `com.neusoft.lemis.log` (src/main/java/com/neusoft/lemis/log/LogService.java:9)


## 5. 无继承关系的独立类（单节点根，按包统计）

共 560 个，多为实体/POJO。按包分布：

| 包 | 数量 |
|---|---:|
| apps.entities | 517 |
| cn | 14 |
| com.neusoft.lemis.log | 6 |
| apps.common | 5 |
| apps.sc | 5 |
| com.neusoft.lemis.cp33 | 3 |
| com.fgm.web.menu | 3 |
| apps.lemis_ext | 2 |
| com.neusoft.unieap.ria | 2 |
| Menus | 2 |
| apps.(根) | 1 |


## 6. 覆盖校验

- 类节点覆盖：654/654（未覆盖 0，多重父节点冲突 0）


## 7. 附录：嵌套类与重名类

注：`X$_ONLY`、`X$_TC73`、`X$_1` 等文件为反编译器生成的辅助类，非原始源码。

重名类（同名不同包，引用时注意歧义）：

- **Activator** ×2：src/main/java/lemisLogonHistory/com/neusoft/lemis/plugins/logonhistory/Activator.java, src/main/java/lemisUserInfoPlugin/com/neusoft/lemis/plugins/userinfo/Activator.java

- **CommonUniqueService** ×2：src/main/java/com/neusoft/lemis/apps/common/ajax/CommonUniqueService.java, src/main/java/com/neusoft/lemis/apps/validator/ajaxservice/CommonUniqueService.java

- **CoordsTransformation** ×2：src/main/java/TKTools/cn/com/egova/tktools/CoordsTransformation.java, src/main/java/TKTools/cn/com/egova/tktools/coordTrans/CoordsTransformation.java

- **GetSysLobAjax** ×2：src/main/java/com/neusoft/lemis/apps/common/ajax/GetSysLobAjax.java, src/main/java/com/neusoft/lemis/apps/validator/ajaxservice/GetSysLobAjax.java

- **SaveSysLobAjax** ×2：src/main/java/com/neusoft/lemis/apps/common/ajax/SaveSysLobAjax.java, src/main/java/com/neusoft/lemis/apps/validator/ajaxservice/SaveSysLobAjax.java

- **SimpleGetValueAjax** ×2：src/main/java/com/neusoft/lemis/apps/common/ajax/SimpleGetValueAjax.java, src/main/java/com/neusoft/lemis/apps/validator/ajaxservice/SimpleGetValueAjax.java

- **TB13** ×2：src/main/java/com/neusoft/lemis/apps/entities/baseDB/T/TB13$_ONLY.java, src/main/java/com/neusoft/lemis/apps/entities/baseDB/T/TB13.java

- **TC72** ×2：src/main/java/com/neusoft/lemis/apps/entities/baseDB/T/TC72$_TC73.java, src/main/java/com/neusoft/lemis/apps/entities/baseDB/T/TC72.java

- **TC7L** ×2：src/main/java/com/neusoft/lemis/apps/entities/baseDB/T/TC7L$_ONLY.java, src/main/java/com/neusoft/lemis/apps/entities/baseDB/T/TC7L.java

- **TC7M** ×2：src/main/java/com/neusoft/lemis/apps/entities/baseDB/T/TC7M$_1.java, src/main/java/com/neusoft/lemis/apps/entities/baseDB/T/TC7M.java

- **TC7W** ×2：src/main/java/com/neusoft/lemis/apps/entities/baseDB/T/TC7W$_ONLY.java, src/main/java/com/neusoft/lemis/apps/entities/baseDB/T/TC7W.java

- **TC7Z** ×2：src/main/java/com/neusoft/lemis/apps/entities/baseDB/T/TC7Z$_ONLY.java, src/main/java/com/neusoft/lemis/apps/entities/baseDB/T/TC7Z.java

- **a** ×2：src/main/java/TKTools/cn/com/egova/tktools/CalcBlock.java, src/main/java/TKTools/cn/com/egova/tktools/a.java

- **b** ×2：src/main/java/TKTools/cn/com/egova/tktools/CalcBlock.java, src/main/java/TKTools/cn/com/egova/tktools/b.java

- **tdblService** ×2：src/main/java/com/neusoft/lemis/apps/lemis/td/b/l/service/tdblService.java, src/main/java/com/neusoft/lemis/apps/lemis/td/b/s/service/tdblService.java

- **tdcahService** ×3：src/main/java/com/neusoft/lemis/apps/lemis/td/c/ah/service/tdcahService.java, src/main/java/com/neusoft/lemis/apps/lemis/td/c/ay/service/tdcahService.java, src/main/java/com/neusoft/lemis/apps/lemis/td/c/bp/service/tdcahService.java


嵌套类清单：


## 8. 统计摘要

- 最大继承深度：2（DropDownMenuDisplayer）

- 最大子树 Top10：

  - MenuDisplayer: 5 节点

  - MessageResourcesMenuDisplayer: 4 节点

  - ProcedureJob: 3 节点

  - LemisServlet: 2 节点

  - MenuBase: 2 节点
