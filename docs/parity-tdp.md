# td/p 导入域对照 — 东软导入弹窗族·SY_IMPORT 引擎 ↔ 新系统统一导入通道

> 2026-11-24。真值源：东软侧=员工管理菜单按键分析/01（tdbp 逐弹窗）+战役 08/16 卷+module-inventory 第八例
> （生产全量 INVALID 实证）+cp33 ImportUtil 源码；新侧=SysImportController/ExcelImportController 实源
> +PG sys_import（112 线）/sys_import_mapping（1864 映射）实况+页面 js 承接面+e2e_import_sys 59 断言。
> 契约细节见 docs/import-channel.md（不重复）；tdbp 员工五弹窗的页面对照见 parity-tdb.md §2.2（不重复）。

## 〇、结论速览

- 导入域=**生产可用性反转最大**的域：旧系统 16+ 处导入弹窗的校验/提交按钮**生产全部死按钮**（三导入包
  INVALID，第八例实锤）→ 新系统统一引擎后**导入功能首次可用**，e2e 59 断言全绿。
- 旧=配置驱动引擎（SY_IMPORT/SYS_IMPORT_MAPPING，cp33 ImportUtil）+散布 16+ 页的弹窗；新=同一配置模型
  忠实移植（112 线/1864 映射，V9+V10 种子）+**统一入口** POST /import/sys/{importId}+模板反向生成+五个
  引擎级修复（§三）。
- 112 线中**17 线已接页面**（imports 4+special 5+money 3+jishu 5），其余 **95 线未接**——回盘桶族(tc9w_bp×13)
  改道 hz import-bp 定制通道（M5）、工资族(tc93/tc95/tc97)有 /import/tddc 与单笔路线、余为培训/就业资金域
  （结案：非派遣主线）。
- 已知线级缺口：tdbp imp_tab3「合同批量解除及更新」导入线未承接（改道终止批次通道，parity-tdb §2.2）。

## 一、导入入口对照（旧 16+ 处弹窗 ↔ 新承接）

| 东软入口 | 线/暂存表 | 新承接 | 形态 |
|---|---|---|---|
| tdbp 四窗（新签/解除/未解除更新/续签/新增保险） | TC90_BP~BP4 | imports 四页签+hire（解除窗改道 change） | 员工管理域详表=parity-tdb §2.2 |
| tdba/tdca/tdcb/tdcl/tddg/tdcaw/bf/bi/tdlb/tdly×3/tddx/tdaf 各导入弹窗 | 各 bp 表 | ❌ 旧=死按钮（INVALID）；新按需接 | 第八例：死按钮族不逐一平移，引擎在册随时可接 |
| tdcay 市医保异动回盘导入 | TC9W_BP04 | **/wb/hz/import-bp**（定制通道） | 改道：局方实样列序+证件/行序双对齐逻辑复杂，M5 定制解析（社保 9 列/市医保 17 列/省 7 列实样在 baopan/huipan） |
| tdck 成果确认导入（社保） | TC9W_BP | 同上（hz import-bp kind=social） | 改道同上 |
| 公积金封存/异议回盘导入（tdcae 系） | TC9W_BP02/03 | sys_import 在册（tc9u_bp02_imp/tc9u_bp03_imp），页面=hz 公积金桶待局方实样 | W0#4 挂账 |
| tddc Excel 导入薪资 | TC93/TC94 | **POST /import/tddc**（EasyExcel 直达） | 波次8a：tddcService(340L 全系统最大单文件)移植，PRC_CHECK_TC93/COMMIT_TC93 对+e2e ✓ |
| tdcl 基数导入弹窗 | AA97_BP | **jishu 页**（4 线 aa97_bp 族+tc0a_bp） | 波次5：AA97_BP 批次流+UK 先探测（铁律4） |
| 税收回盘/比对（PKG_税务税收导入） | TC97 系 | sys_import 在册（tc97_bp/tc97_bp_wyh），页面未接 | 税务域待接 |
| — | TC95_BP 报盘暂存 | sys_import 在册（tc95_bp），未接 | 报盘=导出面，BaopanExporter 已另行覆盖 |

## 二、线级对照（112 线分组）

| 分组 | 线数 | 新承接 |
|---|---|---|
| tc90 族（新签/更新/续签/新增保险/终止/AC01 更新） | 9 | imports 4+hire+e2e ✓；AC01_GX=波次5 冒烟（现库脚本未保留）；余=死码结案（第八例） |
| tc9w 族（回盘桶 01/02/03/04/06/08/09/bj/bz） | 13 | hz import-bp 定制通道承接社保/市医保两线；公积金桶待实样（W0#4）；余在册未接 |
| aa97 族（基数申报社保/医保/公积金/公积金基本） | 4 | jishu 页 ✓（波次5 全绿） |
| tb97 族（三银行到账） | 3 | money 页 ✓（e2e TB97 增量断言） |
| tc04/05/06/07/td04（专项五路） | 5 | special 页 ✓（e2e 59 断言） |
| tc93 族（工资/银付/案件） | 6 | /import/tddc ✓（TC93 主线）；余未接 |
| tc30/tc31（小额/案件?） | 6 | 未接（培训就业域） |
| cc 族（职介/就业补贴 13 线）/hb/hc 族（补贴 14 线）/cb 族（5 线）/ec01/gc08/gc40/qc01/td01/ac01 等单线 | ~43 | 未接——**结案：培训/就业资金域，非派遣主线**（sys_import 配置与映射 1864 行全量在册，接页即用） |
| tc9u/tc9v/tc9r/tc9f/tc9aa/tc95/tc97/tc9aa 等 | ~14 | 在册未接（随所属域排期） |

> 未接 ≠ 不可用：引擎按 importId 即查即跑，页面接入成本=一个上传键+批次预览（imports 页范式）。

## 三、引擎对照（cp33 ImportUtil → SysImportController）

| 维度 | 东软 cp33 | 新引擎 | 形态 |
|---|---|---|---|
| 协议 | multipart .xls + importId（转小写查 SY_IMPORT）+ javabean/parameters | 同构（.xls/.xlsx） | 忠实 |
| 解析 | POI 3.2（2008，仅 HSSF） | 现代 POI（xlsx 双支持）+EasyExcel（/import/tddc） | 升级 |
| 列绑定 | 全字符串 setObject | **typed() 分型绑定**（数字/日期/文本） | 引擎三修①（修复全字符串绑定事故） |
| 映射 | SYS_IMPORT_MAPPING DB 表 | 同构 DB 表（1864 行）+**种子去重** | 引擎三修② |
| 表结构漂移 | 无处理（列缺失即炸） | **27 漂移列跳过+响应 skipped 透传** | 引擎三修③ |
| 错误 | catch(Exception) 静默 {succeed:false}（部署级静默死亡，战役 16 卷） | **根因完整透出**（appCode+行级原因） | 升级（03 卷 D3-1 防护规则落地） |
| 模板 | 静态 .xls 下发（隐藏死键） | GET /import/sys/{id}/template 按 mapping 反向生成 xlsx | 升级 |
| 暂存/批次 | bp 表族+CE10 批次+ACE10A 状态机（0/1/2/3/4） | 同构保留 | 忠实（状态机=operations-manual §背下来条目） |
| 审计 | 无 | import:sys/import:template 写 wb_audit_log | 铁律8 |

## 四、差异与阶段归属

| 类别 | 条目 | 归属 |
|---|---|---|
| 结案 | 95 条未接线（培训/就业资金域非派遣主线+死码线）——配置全量在册，接页即用 | 永久结案+按需启用 |
| 改道 | 回盘桶线→hz import-bp 定制通道；解除导入→终止批次 | 一阶段决策（M5/W7 在案） |
| 页面未接（线在册） | 公积金桶（待局方实样 W0#4）、税务 tc97、tc95 报盘、tc93 族余线 | 二阶段排期（前置=实样） |
| 忠实 | ACE10A 状态机/暂存表同构/批次语义；27 漂移列跳过=TC90_BP 表结构现状 | 一阶段 |
| 升级 | typed 绑定/种子去重/根因透传/模板反向生成/EasyExcel/xlsx | 引擎三修+模板生成（W7 在案） |
| 待核 | tc30/tc31/tc9aa 等单线语义未逐条勘定（结案域） | 启用前置 |

## 五、测试证据

e2e_import_sys.py 59 断言（四路+专项五路+三银行+结算确认/取消+应缴费）；e2e_business 步骤 2（新签导入链）、
步骤 10（终止导入）；w5b/w5c（基数导入 AA97_BP 批次流）；w2（局方实样导入 3 行行序对齐）；冒烟九路正+负
路径（波次5）。生产可用性对照：旧=全死按钮（INVALID 实锤），新=首次可用。
