# 员工管理域前后端功能对照 — 东软 td/b ↔ 新系统工作台

> 2026-11-24 定稿。方法沿 wxyj-frontend-parity 体例：东软侧真值=`rainbowpq/docs/员工管理菜单按键分析/00~05`
> （12 菜单页 **[实测]** DOM 抓取+**[代码级]** 行号可溯分析）+ 波次移植实源；新侧真值=`static/js/pages/*.js`
> 实源+`*Cases.kt` 过程移植实源+ui-reference.md 渲染态清单。逐按键追到业务过程与库事务。
> 两阶段方针：一阶段忠实（含缺陷）/二阶段升级，差异逐条标注阶段归属；通道级缺口与 docs/rewrite-spec/README.md
> "通道级缺口"节互相引用。

## 〇、结论速览

- 东软员工管理菜单 **12 页**（菜单 #1~#20，外加影子页 tdbs）→ 新系统 **5 个承接页**（hire 新签导入 /
  imports 合同批量导入 / audit 新签审核台 / change 续签终止 / insure 参保变更停保[含 tdbr 补收]）+
  salary（tdbt 查询部分）+ **3 页后端就绪前端未接**（tdbc 变更 / tdbe·tdbl 调转）+ **打印面未承接**（tdbo 五键）。
- 主链全通且 e2e 实测：新签（单人 hire + 批量 imports 四路）→ 审核（通过/撤销）→ 终止（报减名册通道）→ 续签。
  移植形态全部标注在 §一总表与 §二逐页表。
- **四个有意改道**：tdba 暂存线→导入线统一；tdbp 解除导入线(imp_tab3)→change 终止区报减名册通道；
  审核通过 doXinqCommit 替代 PRC_COMFIRM_SH 的 '20' 分支；终止原因固定 09（旧 tdbb 多原因）。
- **两类真缺口**：①通道级（打回 PRC_员工批量审核不通过 / COMFIRM_SH '21'~'25' 分派 / '25' 姓名 4 表同步）
  ——见 rewrite-spec README 缺口节；②前端未接（tdbc 变更双通道、tdbe/tdbl 调转、撤销合同终止、
  错误数据删除、tdbr 修改、tdbt 核对标记 save）——后端全部就绪。
- **死键面有意不移植**（旧系统本来就是死的，忠实原则=不复活也不照搬）：tdbq 撤销解除假成功键、
  tdbi/tdbm 404 死页、tdbp tab2 信息更新死 Tab、'27' 隐藏入口、tdbs 影子页。新系统导入功能首次可用
  （旧三导入包生产 INVALID）。

## 一、域级总表（菜单顺序）

| # | 东软菜单/页面 | 弹窗数 | 新承接 | 移植形态 |
|---|---|---|---|---|
| 1 | tdba 新增员工信息申请 | 7 | **hire 新签导入** | 改道：暂存三 store 线(TC90+TB91+TC9E)→导入线(TC90_BP→校验→提交→TB91)；单人=导入线单行 |
| 2 | tdbb 合同终止或解除申请 | 6 | **change 合同终止区** | 改道：页面双键(清单后终止/终止申请)→批次通道 PRC_员工终止导入(TB911=23)；终止原因固定 09=简化(差异④) |
| 3 | tdbc 员工信息变更申请 | 6 | **后端就绪前端未接** | doBg/doCbSh/TdbcChangeCases 已注册(InteractionGatewayController:408)与冒烟实测，无工作台页面 |
| 5 | tdbe 调转二级单位/部门申请 | 1 | **后端就绪前端未接** | TdbeTdblCases.transferApply 实测(5e)；旧 PL/SQL 直生效+多选分隔符 bug 只能单选——新为 TB91 审核制；**东软 tdbe 页死因 2026-11-25 定性升级=DS 坏引用层（包修复不救活）**，二阶段建页时以 tdbl 同构实现为准 |
| 4 | tdbd 员工信息审核 | 12 | **audit 新签审核台（部分）** | '20' 通过=doXinqCommit；批量打回=缺(通道缺口①)；撤销合同终止=后端 doTerminateCancle 就绪前端未接；'22'/'24' 不可达=忠实同构(旧硬编码过滤) |
| 12 | tdbl 员工变更结算单元调转 | 0 | **后端就绪前端未接** | doJsdy/archiveForTransfer 实测(5e/5f)；穿梭框 UI 未建 |
| 16 | tdbp 合同导入&信息更新（最常用） | 5 | **imports 四页签 + hire** | 新签✓54列/未解除更新✓15列(无审核直达=忠实)/续签✓9列/新增保险✓13列；**解除导入(imp_tab3)→改道 change 终止区**；模板下载=活键改进(配置反向生成)；tab2 信息更新死 Tab=忠实不移植 |
| 15 | tdbo 劳动合同终止员工查询 | 1 | **未承接** | 查询面可由 money/change 侧替代；**打印五键=打印面未建**（旧三键取首行 bug 不受影响） |
| 17 | tdbq 撤销解除 | 0 | **有意不移植** | 旧=假成功死键(PRC_TERMINATE_UNDO 调用被注释)，忠实原则不复活；如需真撤销属二阶段新功能 |
| 18 | tdbr 跨年补收基数确认 | 1 | **insure 个人补收区（部分）** | 生成=PRC_保险补收基数_个人 ✓(重跑幂等 UK_TC08)；**修改键(dialog_modify)未接** |
| 20 | tdbt 应发工资花名册核对 | 0 | **salary（查询部分）** | 查询读 TC93 ✓；核对标记 save(tc93_checkStore GUID)未接——战役 21 卷即记"待页面 recon" |
| — | tdbs 影子页（不在菜单） | — | 有意不移植 | 旧=手敲 URL 绕过校验的旁路页 |

## 二、逐页按键对照

### 2.1 tdba ↔ hire（新签导入）

| 东软按键 | jsid | 新侧 | 形态/注记 |
|---|---|---|---|
| 二代身份证读卡 | jsid_read | ❌ | 外设面，二阶段评估 |
| 头像上传 | btn_photo | ❌ | 照片列 ATC90T1-T6 即 TC90_BP 缺列家族；导入暂不承载 |
| 批量导入 | btn_import | → **imports 页** | 弹窗独立成页（升级） |
| 折子号维护 | btn_zz | ❌ | 旧跳转目标 tdbi 整页 404（双重死键），不移植 |
| 黑名单 | jsid_cancle | ❌ | 未承接（id 叫 cancle 名叫黑名单=旧命名陷阱，不复刻） |
| 删除 | jsid_del | ❌ | 未接（铁律8：新系统删除一律逻辑删，需二阶段设计） |
| 历史合同信息 | jsid_history | ❌ | query_tc90/tc90List 弹窗未建 |
| 暂存 | btn_save | ❌ | 暂存线不沿用（改道导入线） |
| 提交到审核 | btn_confirm | **③ 生成批次→校验→提交** | 单键三步：TC90_BP 单行→PRC_员工导入校验/提交→TB91('20'/'2')待审；dsf-tip 注明 tdb/a+tdbp 合并 |
| 继续新增 | btn_add_c | ❌ | 表单重置即等价，未单独做键 |
| 更换结算单元 | btn_add_r | ❌ | 未接 |

后端面：TdbpImportCases 员工导入对（1882L）冒烟七路全绿；AC01/TC90/TB91/TC90_LIST/ZYNUM 链与旧一致。

### 2.2 tdbp ↔ imports（四页签）+ hire

| 东软弹窗 | 列数 | 新侧页签 | 形态 |
|---|---|---|---|
| imp_tab1 合同新增导入 | 54 | imports·新签导入(54列) | 同名过程对 PRC_员工导入校验/提交 ✓ |
| imp_tab3 合同批量解除及更新 | — | ❌ → **change 终止区** | 改道：解除走终止批次(TC90_BP1→PRC_员工终止导入)；旧线"解除+更新"一体的语义拆分 |
| imp_tab4 未解除合同批量更新 | 15 | imports·更新导入(15列) | **无审核直达正式表=忠实**（旧即不写 TB91） |
| imp_tab5 合同续签批量导入 | 9 | imports·续签导入(9列)+change 续签区 | 直达正式表=忠实 |
| imp_tab6 批量新增保险 | 13 | imports·新增保险导入(13列) | ✓ |
| 合同导入模板下载 | id_down(隐藏死键) | **每页签·下载模板（活键）** | 升级：sys_import_mapping 反向生成 xlsx |
| 查询 | id_query | ✓（批次预览 /wb/batch） | |
| tab2 信息更新（清空/查询/重置/模板/导入 5 键） | ⛔ 整 Tab 死 | 有意不移植 | 旧 page_init 已 removeChild，忠实 |

### 2.3 tdbd+tdbf ↔ audit（新签审核台）

| 东软按键 | 页面 | 新侧 | 形态 |
|---|---|---|---|
| 批量审核通过 | tdbd jsid_pl | **通过**（逐行 doXinqCommit） | '20' 分支等价：TC90_BG 留痕+TC90/TB91 落账；批量循环=逐行点按（二阶段可加批量） |
| 单笔审核 | tdbd btn_leader | 同上（逐行） | 同一通道 |
| 批量审核不通过 | tdbd jsid_pl_cancel | ❌ **打回通道缺** | PRC_员工批量审核不通过未移植（TB912 '2'→'4'+意见，可重提）；现"驳回"键=doXinqCancle 物理删语义≠打回——通道缺口①，审核台动工时补 |
| 撤销合同终止 | tdbd jsid_cancle | 后端就绪前端未接 | doTerminateCancle(TdbdStateMachineCases 8 方法之一)实测 |
| 错误数据删除 | tdbd jsid_del | ❌ 未接 | 铁律8 逻辑删改造二阶段 |
| 处理 | tdbf btn_confirm | **通过**（doXinqCommit 即旧 xinqcl 处理层） | TC90_BG `_o` 驱动逐列留痕=忠实（含取消不清理留痕的孤儿行为） |
| — | — | 驳回=doXinqCancle | audit 现有键，语义=撤销新签申请(删 TC90+TB91)，页面 tip 已标注"待改造逻辑删" |
| '21' 参保变更审核 | COMFIRM_SH 分支 | 后端 doCbSh ✓（TC90 ~90 字段+TC90_LIST 60 字段回填） | 前端入口未接（变更页未建） |
| '25' 姓名/身份证 4 表同步 | COMFIRM_SH 分支 | ❌ 零覆盖 | 通道缺口① |
| '22'/'24' 审核不可达 | 旧硬编码过滤 | 忠实同构 | 续签走导入直达（旧 tdbp 同样不写 TB91） |

### 2.4 tdbb+tdbc ↔ change（续签/终止）+ 后端

| 东软按键 | 页面 | 新侧 | 形态 |
|---|---|---|---|
| 查询/清空条件 | tdbb | **搜索** ✓ | |
| 终止申请 | tdbb btn_cancel | **终止区·校验+提交** | TC90_BP1→PRC_员工终止导入校验/提交；有效合同→TB911='23' 审核流；报减名册/工资结束 atc90av/费用结束 aae003 同旧 |
| 清单后终止（'27' 入口） | tdbb jsid_zhongzhi | 有意不移植 | 旧系统已隐藏（批量审核静默丢 '27' 同源），忠实 |
| 历史合同查看 | tdbb | 有意不移植 | 旧目标 tdbm_dialog_ht.jsp 404 死键 |
| 基本信息变更(不审核) | tdbc id_change | 后端就绪前端未接 | 旧=直改 AC01+TC90 无痕（多合同 get(0) bug）——rewrite-spec 03 直改无痕条目；二阶段接时按档案护栏设计 |
| 变更合同信息(审核) | tdbc jsid_mod_sh | 后端就绪前端未接 | doBg/doCbSh：TC92 `_后`列镜像刷新+TC92_LIST 逐字段审计含 AA10 中文翻译（5b 实测 18 字段） |
| 确认恢复有效 | tdbq jsid_confirm | 有意不移植 | 旧=假成功死键 |

### 2.5 tdbe/tdbl/tdbo/tdbr/tdbt（简表）

| 页面 | 东软按键 | 新侧 | 形态 |
|---|---|---|---|
| tdbe 调转二级单位 | 调转二级单位 btn_block | 后端 transferApply 就绪前端未接 | 旧 PL/SQL 直生效+只能单选 bug；新 TB91 审核制('24') |
| tdbl 结算单元调转 | 调转>>/全部调转/<</全部回退/提交到审核 | 后端 doJsdy/archiveForTransfer 就绪前端未接 | 穿梭框 UI 未建；'26' 通道 |
| tdbo 终止员工查询 | 打印解除证明书/生活补助表/失业登记证明书/转出户口/转出档案 | ❌ 打印面未建 | 旧三打印键取首行 bug 不随移植；查询面可由现有页替代 |
| tdbr 跨年补收基数确认 | 生成明细/修改 | **insure·生成补收基数** ✓；修改未接 | PRC_保险补收基数_个人：三段(社保/医保仅省/公积金)按年分段 TC08；本年段不写=源语义 |
| tdbt 花名册核对 | 查询/重置（主面无写键） | **salary 查询** 部分 | TC93 读 ✓；核对标记 save(tc93_checkStore)未接（战役 21 卷待 recon 项） |

## 三、后端过程/服务面对照（本域）

| 旧通道 | 新实现 | 测试 |
|---|---|---|
| tdbaService(125L) 三 store 暂存 | 改道导入线（TdbpImportCases 员工导入对） | 七路冒烟+e2e ✓ |
| tdbbService(128L) doTerminate 等 | TdbdStateMachineCases/批次终止对 | e2e 步骤10 ✓ |
| tdbdService(689L) 8 方法 | TdbdStateMachineCases 8 方法→7 用例+反射复制器 | doJbbg 9 表广播 updatedRows=25 精确闭合 |
| tdbfService(163L) 6 方法 | TdbfAuditCases 6 方法 | 全实测（doXinqCancle 硬删=忠实待改造清单） |
| tdbeService(64L)+tdblService(219L) | TdbeTdblCases(transferApply/archiveForTransfer) | 5e/5f 实测 |
| PKG_派遣批量导入 员工五对 | TdbpImportCases(1882L) | 九路冒烟+e2e_import_sys 59 断言 |
| PRC_COMFIRM_SH(PKG_RCPQ, 9 参) | **未整体移植**——'20'=doXinqCommit 等价；'21'=doCbSh；'26'=doJsdy；'23'部分=terminateCancel；'25'❌ | 通道缺口① |
| PRC_员工批量审核不通过(PKG_人才派遣) | **未移植** | 通道缺口① |
| PRC_GENERATE_TFMX | 等价 no-op（旧源码整块被注释禁用，决策点关闭） | — |
| PRC_保险补收基数_个人 | W6 移植 ✓ | insure 页 e2e |
| PRO_GET_CNT 失业金月数（旧连环失效 atc90l 恒'0'） | 新系统不依赖该计算（终止走导入对），失效链不随移植 | — |

## 四、差异总账与阶段归属

| 类别 | 条目 | 归属 |
|---|---|---|
| 改道（记录理由） | tdba 暂存线→导入线统一；tdbp 解除线→终止批次通道；COMFIRM_SH '20'→doXinqCommit | 一阶段决策已定，e2e 背书 |
| 通道缺口 | 打回（PRC_员工批量审核不通过）；COMFIRM_SH '25' 姓名 4 表同步零覆盖；'23' 分派与 terminateCancel 等价性待核 | 一阶段欠账，审核台动工时补（见 rewrite-spec README） |
| 前端未接（后端就绪） | tdbc 变更双通道页；tdbe/tdbl 调转穿梭框；撤销合同终止；错误数据删除；tdbr 修改键；tdbt 核对标记 save；tdba 历史合同弹窗/删除/黑名单 | 二阶段前端排期 |
| 有意不移植（旧死键/死页） | tdbq 假成功键；tdbi/tdbm 404 页；tdbp tab2 死 Tab；'27' 隐藏入口；tdbs 影子页 | 永久（忠实=不复活死功能） |
| 外设/打印面 | 读卡、头像上传、tdbo 打印五键 | 二阶段评估 |
| 忠实同构（含缺陷） | 续签/未解除更新无审核直达；'22'/'24' 不可达；doXinqCancle 物理删；TC90_BG 取消不清理；ACE10A 校验门槛 | 一阶段忠实，二阶段按 backlog 改造 |
| 简化（需业务确认） | 终止原因固定 09-职工个人原因（旧 tdbb 多原因） | 待业务定：是否需还原多原因 |
