# 对外接口盘点（URL 入口清单）

> 2026-11-05 生成。来源：`web.xml` + `src/main/resources` 全部 2154 个 Spring XML 中的 414 个 JspBean / 714 个 InteractionBean。

## 1. 总览

| 指标 | 数值 |
|---|---:|
| Servlet 入口（web.xml） | 27 |
| 过滤器 | 2 |
| 页面入口（JspBean） | 414 |
| 交互（InteractionBean） | 714 |
| 查询型交互（type=Q） | 513 |
| 修改型交互（type=M） | 201 |
| 走通用基类 LemisBusinessMgrPoJo 的交互 | 642（89%） |
| 走自定义 Service 的交互 | 72 |

**结论先行**：对外 API 的 90% 是「datastore 元数据驱动的标准 CRUD」——绑定通用基类的 `query`/`update`/`count`，由 MetaDataBean 元数据（61942 列定义）+ 1004 个 datastore 配置驱动；真正的自定义代码面只有约 72 个交互，全部集中在 31 个 Service 的自定义方法上。

## 2. web.xml Servlet 入口

| URL | Servlet 名 | 类 |
|---|---|---|
| `(未映射)` | QuartzInitializerServlet | `QuartzInitializerServlet` |
| `(未映射)` | PrintChart | `DisplayChart` |
| `*.do` | action | `EAPActionServlet` |
| `/FLASHPlugInDownload` | FLASHPlugInDownload | `FLASHPlugInDownload` |
| `/FileDownLoadServlet` | FileDownLoadServlet | `FileDownLoadServlet` |
| `/FileDownLoadServlet1` | FileDownLoadServlet1 | `FileDownLoadServlet1` |
| `/FileDownLoadServlet2` | FileDownLoadServlet2 | `FileDownLoadServlet2` |
| `/FileUploadServlet` | FileUploadServlet | `FileUploadServlet` |
| `/GetRemindServlet` | GetRemindServlet | `GetRemindServlet` |
| `/LemisCommonServlet` | LemisCommonServlet | `LemisCommonServlet` |
| `/LemisDownloadServlet` | LemisDownloadServlet | `LemisDownloadServlet` |
| `/LemisPluginServlet` | LemisPluginServlet | `LemisPluginServlet` |
| `/LemisReloadContextServlet` | LemisReloadContextServlet | `LemisReloadContextServlet` |
| `/LemisSysHelpServlet` | LemisSysHelpServlet | `LemisSysHelpServlet` |
| `/MisGisServlet` | MisGisServlet | `MisGisServlet` |
| `/SOAPMonitor` | SOAPMonitorService | `SOAPMonitorService` |
| `/SVGPlugInDownload` | SVGPlugInDownload | `SVGPlugInDownload` |
| `/Tool/*` | ToolServlet | `NWToolListenerServlet` |
| `/WtsBuildXML` | WtsBuildXML | `WtsBuildXML` |
| `/ajaxutil` | AjaxUtil | `AjaxUtil` |
| `/displaySvg` | SVGPrint | `SVGPrintFactory` |
| `/jcaptcha/*` | jcaptcha | `EAPImageCaptchaServlet` |
| `/reportservlet` | reportservlet | `ReportServlet` |
| `/screenServlet` | screenServlet | `ScreenServlet` |
| `/services/*` | AxisServlet | `AxisServlet` |
| `/servlet/AdminServlet` | AdminServlet | `AdminServlet` |
| `/simpleServlet` | simpleServlet | `SimpleServlet` |

过滤器：`transcoding`（*.html *.jsp *.do）、`cache`（*.xsl /unieap/ria3.3/* *.png *.jpg *.gif *.css）

**审计发现**：

- `TerminalRedirectServlet`（`/terminal_redirect`）：类存在于 `com.neusoft.lemis.servlet`，但 web.xml 中声明**已被注释禁用**——对外入口无效，重写可跳过（保留类即可）
- `/displayChart → PrintChart`：映射存在但对应 `<servlet>` 声明在注释块内——**死映射**，运行时该 URL 会 404，重写时无需实现

## 3. 交互通道（JSP → InteractionBean）

主入口 `*.jsp`（Acegi 过滤器链 → JSP）→ 页面 URL 匹配 `JspBean`（bean name = 页面路径）→ 页面内交互按 `REQ-*` 交互码由前端发起 → `(bean, method)` 调用。

| 模块 | 页面数 |
|---|---:|
| td/c | 120 |
| td/b | 62 |
| td/d | 42 |
| td/l | 35 |
| td/h | 31 |
| td/n | 19 |
| td/a | 19 |
| wts | 18 |
| td/i | 16 |
| az/b | 16 |
| xa | 14 |
| td/g | 13 |
| az/a | 8 |
| a | 1 |

### 交互绑定分布

| bean | 交互数 |
|---|---:|
| LemisBusinessMgrPoJo（通用基类） | 642 |
| *（其余 29 个自定义 Service，合计） | 72 |

### 方法频次（被挂接的 method）

| method | 次数 | 说明 |
|---|---:|---|
| `query` | 483 | 基类查询 |
| `update` | 170 | 基类保存 |
| `delete1` | 7 | td/c 自定义删除变体 |
| `delete` | 7 | td/c 自定义删除 |
| `doSave` | 7 | 自定义保存 |
| `doUpdate` | 2 | 自定义 |
| `doCbSh` | 2 | 自定义 |
| `saveUpdate` | 2 | 自定义 |
| `changeJbxx` | 2 | 自定义 |
| `doCommit` | 2 | 自定义 |
| `save` | 2 | 自定义 |
| `doDelete` | 1 | 自定义 |
| `doQuery` | 1 | 自定义 |
| `doZhongzhi` | 1 | 自定义 |

## 4. 自定义交互明细（重写的显式用例清单）

非通用 bean 的全部 72 个交互（页面 → 交互码 → bean.method）：

| Service | 页面 | 交互码 | method |
|---|---|---|---|
| AzbaSave | azba_cont.jsp | `REQ-azba_cont-ac01Store-Save1` | `save` |
| AzbaService | azba_cont.jsp | `REQ-azba_cont-ac01Store-Save` | `changeJbxx` |
| AzbaService | azba_cont.jsp | `REQ-azba_cont-ac01_lbStore-Save` | `doUpdate` |
| Azbd | azbd_cont.jsp | `REQ-azbd_save2` | `del` |
| Azbe | azbe_cont.jsp | `REQ-azbe_cont-ac01Store-Save2` | `save` |
| TdbcService | tdbc_dialog_bg_bsh.jsp | `REQ-c00760bbc00541bdac64461b4b833848` | `saveUpdate` |
| TdbcService | tdbc_dialog_bg_bsh_1.jsp | `REQ-35347be5a3d043c8b57d53833ff75499` | `saveUpdate` |
| TdbcService | tdbc_dialog_jbxx.jsp | `REQ-04f337436d384a9481042ed511ca1805` | `changeJbxx` |
| TdclService | tdcl_dialog_mod.jsp | `REQ-tdcl_dialog_mod-U` | `update` |
| WtsService | wtsaa_cont.jsp | `REQ-wtsaa_cont-wts_machineStore_mgr-M` | `wtsaasave` |
| WtsService | wtsab_dialog.jsp | `REQ-wtsab_dialog-wts_functionStore_mgr-M` | `wtsabsave` |
| WtsService | wtsaf_dialog.jsp | `REQ-wtsaf_dialog-wts_policyStore-M` | `wtsafsave` |
| XaabService | xaab_cont.jsp | `REQ-xaab_cont-aa12Store-Del` | `doDelete` |
| XaabService | xaab_cont.jsp | `REQ-xaab_cont-aa12Store-M` | `doUpdate` |
| XaadService | xaad_cont.jsp | `REQ-xaad_cont-aa10Store_QueryList-Query` | `doQuery` |
| azaaService | azaa_cont.jsp | `REQ-azaa_cont-ab01Store-Update` | `update` |
| azaaService | azaa_dialog_contact.jsp | `REQ-azaa_dialog_contact-ab07Store-save` | `doSave` |
| azabService | azab_cont.jsp | `REQ-azab_cont-ab01Store-save` | `update` |
| azbbService | azbb_cont.jsp | `REQ-azbb_cont-ac01Store-save` | `update` |
| tdaaService | tdaa_cont.jsp | `REQ-tdaa_cont-Commit` | `doCommit` |
| tdaaService | tdaa_cont.jsp | `REQ-tdaa_cont-Save` | `doSave` |
| tdabService | tdab_dialog_xq.jsp | `REQ-tdab_dialog_xq-tb90Store-Update` | `doContinue` |
| tdbaService | tdba_cont.jsp | `REQ-tdba_cont-commit` | `doCommit` |
| tdbaService | tdba_cont.jsp | `REQ-tdba_cont-save` | `update` |
| tdbbService | tdbb_dialog_renew.jsp | `REQ-tdbb_dialog_renew-tc90Store-Save` | `doNewHt` |
| tdbbService | tdbb_dialog_renewall.jsp | `REQ-tdbb_dialog_renewall-save` | `update` |
| tdbbService | tdbb_dialog_terminate.jsp | `REQ-tdbb_dialog_terminate-Commit` | `doTerminate` |
| tdbbService | tdbb_dialog_zhongzhi.jsp | `REQ-b327d8054e6240a7afcee9eff375da4e` | `doZhongzhi` |
| tdbdService | tdbd_cont.jsp | `REQ-4b4e484807574d7ca2c0a3ff534f1f77` | `doTerminateCancle` |
| tdbdService | tdbd_dialog_bgsh.jsp | `REQ-1d7d5f25ee0e42488fb1d37de84f3b98` | `doCbSh` |
| tdbdService | tdbd_dialog_cbsh.jsp | `REQ-b51805f78fbe49c2a18f52a569d36fc7` | `doCbSh` |
| tdbdService | tdbd_dialog_dzjsdy.jsp | `REQ-0ccf1c0a605441129e959bd90d155be7` | `doJsdy` |
| tdbdService | tdbd_dialog_jbbgsh.jsp | `REQ-f6dcdbe7e8d14efab9ff23267cf8316d` | `doJbbg` |
| tdbdService | tdbd_dialog_renewall.jsp | `REQ-tdbd_dialog_renewall-save` | `doRenewAll` |
| tdbdService | tdbd_dialog_xinqsh.jsp | `REQ-tdbd_dialog_xinqsh-save` | `doNew` |
| tdbeService | tdbe_dialog_query.jsp | `REQ-tdbe_dialog_query-save` | `update` |
| tdbfService | tdbf_dialog_dzjsdycl.jsp | `REQ-ae50d1592aaf47a5a47449cf864456d2` | `doJsdyCancle` |
| tdbfService | tdbf_dialog_renewallcl.jsp | `REQ-tdbf_dialog_renewallcl-cancle` | `doRenewCancle` |
| tdbfService | tdbf_dialog_xinqcl.jsp | `REQ-tdbf__dialog_xinqcl-commit` | `doXinqCommit` |
| tdbfService | tdbf_dialog_xinqcl.jsp | `REQ-tdbf_dialog_xinqcl-cancle` | `doXinqCancle` |
| tdbfService | tdbf_dialog_zzcl.jsp | `REQ-c50fe7809f1a4d1e83440109dedd8749` | `reCommit` |
| tdbfService | tdbf_dialog_zzcl.jsp | `REQ-e3670657cd544b96b38bf55dd666d1e7` | `terminateCancel` |
| tdblService | tdbl_cont.jsp | `REQ-047bd90ca8f54b798ed71d49ba3fab1e` | `update` |
| tdblService | tdbs_cont.jsp | `REQ-191956b136c540d3b10f7cc9d4d78e51` | `update` |
| tdcaService | tdca_dialog_import.jsp | `REQ-tdca_cont-tc9wStore-del` | `delete` |
| tdcaService | tdca_dialog_import.jsp | `REQ-tdca_cont-tc9wStore-delete` | `delete1` |
| tdcaeService | tdcae_cont.jsp | `REQ-31e4b1fa86f94459a071ff92a27474df` | `delete1` |
| tdcaeService | tdcae_cont.jsp | `REQ-7d987b6cfe134578bbf3d511d045b894` | `delete` |
| tdcahService | tdcah_dialog_import.jsp | `REQ-tdcah_cont-tc9w_bp01Store-del` | `delete` |
| tdcahService | tdcah_dialog_import.jsp | `REQ-tdcah_cont-tc9w_bp01Store-delete` | `delete1` |
| tdcahService | tdcah_dialog_modify.jsp | `REQ-tdcah_cont-tc9w1Store-save` | `doSave` |
| tdcahService | tdcay_dialog_import.jsp | `REQ-35ccdd77186a471f8ffeb70f0aa09e9c` | `delete` |
| tdcahService | tdcay_dialog_import.jsp | `REQ-61a7886ff15a4ff3abd247d0f779dd7b` | `delete1` |
| tdcahService | tdcay_dialog_modify.jsp | `REQ-ca980873f8fd48c1a3b789d8f8e1f020` | `doSave` |
| tdcahService | tdcbp_dialog_import.jsp | `REQ-4c919c12968a471a9da486e49ed5e360` | `delete1` |
| tdcahService | tdcbp_dialog_import.jsp | `REQ-89e16de3069e4c409121ac09152e5b5a` | `delete` |
| tdcahService | tdcbp_dialog_modify.jsp | `REQ-af30ba4233db47389ca0fea0244b263b` | `doSave` |
| tdcajService | tdcaj_dialog_import.jsp | `REQ-tdcaj_cont-tc9wStore-del` | `delete` |
| tdcajService | tdcaj_dialog_import.jsp | `REQ-tdcaj_cont-tc9wStore-delete` | `delete1` |
| tdcajService | tdcaj_dialog_modify.jsp | `REQ-tdcaj_cont-tc9uStore-save` | `doSave` |
| tdcjService | tdcj_cont.jsp | `REQ-7929530d18d142378994633dcee4de75` | `saveNobt` |
| tdcjService | tdcj_cont.jsp | `REQ-8d41e457ba344945a710b3fd06aad6b9` | `AllConfirm` |
| tdcjService | tdcj_cont.jsp | `REQ-9bd1ac73e67c4b9c9b046cc6897328dd` | `saveConfirm` |
| tdckService | tdck_dialog_import.jsp | `REQ-tdck_cont-tc9wStore-del` | `delete` |
| tdckService | tdck_dialog_import.jsp | `REQ-tdck_cont-tc9wStore-delete` | `delete1` |
| tdckService | tdck_dialog_modify.jsp | `REQ-b35c29976d124db28b5436f497dba26b` | `doSave` |
| tddaService | tdda_dialog_mod.jsp | `REQ-491c60023d8c4d39bb23633b9c15786a` | `updateAa90` |
| tddaService | tdda_dialog_salaryset.jsp | `REQ-tdda_dialog_salsryset-aa90Store-Update` | `update` |
| tdddService | tddd_dialog_grqf.jsp | `REQ-tddd_dialog_grqf-U` | `update` |
| tdddService | tddd_dialog_mod.jsp | `REQ-tddd_dialog_mod_doSave` | `doSaveTc93` |
| tddfService | tddf_cont.jsp | `REQ-tddf_cont-U` | `doSaveAa81` |
| tddfService | tddf_dialog_addAa80.jsp | `REQ-tddf_dialog_addAa80-U` | `doSaveAa80` |

## 5. Azbd / Azbe 死代码判定

**非死代码**。两者均被装配且挂接交互：

- `azbd`（Azbd 类）→ 页面 `azbd_cont.jsp` 交互 `del`（`REQ-azdb_ac01_delete`）
- `azbe`（Azbe 类）→ 页面 `azbe_cont.jsp` 交互 `save`（`REQ-azbe_ac01_save`）

此前误判原因：bean 名为小写（`azbd`/`azbe`），与类名大小写不一致。重写清单需包含这两个类。

## 6. 重写视角

1. **接口面收敛**：对外 API = 414 个页面 × 交互码 + 27 个 Servlet。Kotlin 侧可设计
   「页面交互网关」：`(页面, 交互码) → (service, 方法)` 的路由表直接从本清单生成。
2. **90% 交互零代码迁移**：走通用基类的 642 个交互，只需实现一次通用
   query/update/count（元数据驱动）+ 迁移 1004 个 datastore 配置。
3. **72 个自定义交互是全部手写工作量**：见第 4 节明细，可作为 Kotlin 重写的任务分解与验收清单。
4. **元数据规模是主要风险**：61942 列 MetaDataBean 定义 + 1004 datastore 是标准通道的地基，
   其正确性决定 642 个交互能否零迁移。
