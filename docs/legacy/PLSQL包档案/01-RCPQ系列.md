# 01 · PL/SQL 包档案 —— RCPQ 系列（8 包）

> 2026-11-14。源：`work/pkg_all/PACKAGE[_BODY]_PKG_*.sql` 转储；编译状态 `work/db_deep_2026-11-14.txt` §A4 + `work/db_deep2_2026-11-14.txt` §1/§2（user_errors）；调用方=对 `src/main/webapp`（js/jsp/rdx）与 `src/main/java` 全量 grep（剔除 `//` 注释行）+ `work/pkg_all` 包间 grep。提取脚本：`scripts/db/profile_rcpq_pkgs.py`（签名/触碰表/注释禁用检测）、`scripts/db/rcpq_proc_matrix.py`（每过程有效行占比×调用方矩阵）、`scripts/db/find_rcpq_callers.py`、`scripts/db/find_rcpq_crossrefs.py`；底稿在 `work/rcpq_profile_raw.txt`、`work/rcpq_proc_matrix.txt`、`work/rcpq_callers.txt`、`work/rcpq_xrefs.txt`、`work/rcpq_live_code.txt`。
>
> **状态标记**：`正常`=有完整逻辑；`禁用壳`=主体被 `/*…*/` 整块注释、活代码仅剩声明+`P_APCODE:=1`+EXCEPTION（有效行占比<15%，即"调用即假成功"）；`接受桩`=原本就只置 1 无业务动作；`瘦`=有效行 15%~35%，大部分被注释。所有过程末尾均带标准 OUT `P_APCODE/P_ERRMSG`，表中 OUT 列只列**非标准**出参。

## 0. 系列总览

| 包 | 体量(行/过程) | 规格头 | 编译(A4) | 根因 | 前端/Java 活引用 | 被其他包调 | 一句话定性 | 重写路线 |
|---|---|---|---|---|---|---|---|---|
| **PKG_RCPQ** | 6514 / 42 | 2010 系老核心 | BODY INVALID | 平台视图 `SYS_ORG_USER_UNIT` 连坐 | 6 处 JS | FZ/新结算/HM/NEW2/PKG_CHECK 调它 8 处 | 险金异动/补缴/入户清单/审核全流程核心，但 1/3 过程已成注释壳，公共取参过程 `PRC_GET_PARA` 是**恒返 -1 的中毒壳** | 拆分重写（测算组→Kotlin；清单组→PL/pgSQL；异动补缴组→废弃候选） |
| **PKG_RCPQ_NEW2** | 3528 / 2 | "人才派遣"2018 | BODY INVALID | **真列漂移** `TC9U.ATC9U7` 不存在 | 0 | 0 | 2018 年计费数据(JFSJ)结算重构尝试，触碰表全空、无任何调用方 | **废弃候选**（不移栽） |
| **PKG_RCPQ_FZ** | 6734 / 30 | 无头注释 | **SPEC+BODY 均 INVALID** | spec 参数声明 `SYS_ORG_USER_UNIT%ROWTYPE` 连坐 | 9 处 JS + 1 处 Java | RCPQ/新结算/HM 调它 8 处 | 费用/工资结算"辅助"包：险金审核、过月、补退反馈、名册导出；8 过程为禁用壳（含 Java 调用的 TFMX 与前端调用的 CHANGE_BASE） | 拆分重写（审核/反馈/名册→Kotlin+PL/pgSQL；禁用壳组→废弃候选） |
| **PKG_RCPQ_FZ_新结算** | 828 / 1 | "优化费用结算"2018 | BODY INVALID | **真列漂移** 记录组件 `AC10.ATC9U2` 缺失 | 0（前端调用已注释） | 0 | FZ.PRC_SALARYCOMPUTE 的重写实验，被 `PKG_费用结算.PRC_费用结算` 取代后弃坑 | **废弃候选** |
| **PKG_RCPQ_HM** | 2571 / 16+F1 | 无头注释 | BODY INVALID | 平台视图连坐 + **真列漂移** `AA97.AAA97G` ×4 | 6 处 JS（3 个键点击即 500） | RCPQ/FZ/WY/独立过程调它 8 处 | 员工管理周边：比例调整、公积金缴存、离职工资、合同续签、二级单位调转、TC93/AC10 月末调整（3 大过程为禁用壳，其中一个被 6 处包间调用） | 拆分重写（先救 3 个死键；壳组按调用方取舍） |
| **PKG_RCPQ_TJ** | 2579 / 17 | 无头注释 | BODY INVALID | 依赖视图 `V_AB01_TB90_TB93` INVALID 连坐（ORA-04063） | 8 个 rdx 报表 + tdna 事项提醒 | 0 | 统计报表专用包：7 个管道表函数 + 提醒/预警/收支统计，逻辑全部完整（无禁用壳） | **PL/pgSQL 保留**（修视图即恢复；统计函数可改 Kotlin 只读查询） |
| **PKG_RCFP_TJ** | 440 / 4 | "民办培训管理统计"2011 | BODY INVALID | **真列漂移** `TC10.AAEB36` 列不存在 | 3 个 2011 年 rdx 报表 | 0 | 非普高等教育毕业生就业审批/办事大厅统计，与人才派遣业务无关的另一条产品线 | **废弃候选**（业务确认已停后不移栽） |
| **PKG_RCPQ_WY** | 2894 / 23 | 无头注释 | BODY INVALID | 平台视图 `SYS_ORG_USER_UNIT` 连坐 | 7 处 JS（当前全部 500） | FZ 调它 4 个过程 8 处、PKG_CHECK 1 处 | 终止/退费/撤销、工资名册不代发、网银(TC8I)到账核销；2 过程禁用壳（含档案已载明的 `PRC_TERMINATE_UNDO`） | 拆分重写（7 个活键→Kotlin；网银/到账组→废弃候选） |

### 0.1 系列级硬事实（本档案直接沿用既有结论，出处见括号）

1. **全部 8 个包体 INVALID**（db_deep §A4）；规格头除 `PKG_RCPQ_FZ` 外均 VALID——**FZ 连规格都 INVALID**，因其唯一以 `SYS_ORG_USER_UNIT%ROWTYPE` 作过程入参（`PACKAGE_PKG_RCPQ_FZ.sql:59`，`PRC_TC9T_AC10` 的 `PRM_USER` 参数）。调用 INVALID 包体的任何过程 → 懒重编译失败 → ORA-04063/PLS-00905（员工管理菜单 05-重放实测已验证 HM/WY 两例点击即 HTTP 500）。
2. **平台视图连坐根因**：UniEAP 平台视图 `SYS_ORG_USER_UNIT` 依赖 `V_ORG_USER_UNIT`，而后者**不在视图清单 D1 中（根本不存在）**→ 该视图编译必败 → 所有声明 `R_LJ SYS_ORG_USER_UNIT%ROWTYPE` 的包体（RCPQ:350、WY:12、HM:40、FZ spec:59）全部 PLS-00201（db_deep2 §2 各首错行）。修好这一个视图即可让 4 个包恢复"可编译"，**但不会修复第 0.2 节的逻辑级空壳**。
3. **真列漂移 4 包**（重编译永远过不去，db_deep2 §2 + 04/05 文档）：`PKG_RCPQ_NEW2`(`TC9U.ATC9U7`)、`PKG_RCPQ_FZ_新结算`(`ATC9U2` 组件)、`PKG_RCFP_TJ`(`AAEB36`)、`PKG_RCPQ_HM`(`AA97.AAA97G` ×4，2026-11-14 实机重放确认 PRC_CHANGE_EJDW 不可调用)。源码定位：NEW2 `PACKAGE_BODY_PKG_RCPQ_NEW2.sql:2086,3047`（均在 PRC_CONFIRM_JFSJ）；新结算 `:223`；RCFP_TJ `:71-72`（声明区 `tc10.aaeb36%TYPE`）；HM `:574,580,590,611,657`（均在 PRC_GJJ_SX）。
4. **注释禁用壳 18 个**（有效行占比<15%，`work/rcpq_proc_matrix.txt`）：分布 RCPQ×6、FZ×7、HM×3、WY×2（清单见各包 §5）。这是本系列最大的隐性风险——**过程签名还在、前端还在调用、返回永远是"成功"，但什么都没做**。

### 0.2 注释禁用壳中"仍被活调用方引用"的 6 个死键（危险度最高）

| 死键 | 空壳位置 | 谁在调 | 后果 |
|---|---|---|---|
| `PKG_RCPQ.PRC_GENARATE_AA97` | :5461-5580（11% 有效） | tdcac/tdcab/tdcq_dialog_xz.js:25 | 「生成新签员工补缴名册」假成功 |
| `PKG_RCPQ.PRC_GET_PARA` | :5952-6037（16%，恒置 `P_APCODE:=-1`） | FZ:759/4334/5018/6070、新结算:134、HM:1643/2294、NEW2:74、PKG_CHECK:603 | 公共取参中毒：全部调用方按 `IF P_APCODE='1'` 分支静默跳过每个员工（FZ:775 实测分支） |
| `PKG_RCPQ_FZ.PRC_GENERATE_TFMX` | :1356-1600（6%） | **Java** tdbfService.java:155 | 退费明细生成假成功（先例已载 00-总览） |
| `PKG_RCPQ_FZ.PRC_CHANGE_BASE` | :1601-1876（7%） | tdbd_dialog_zzsh.js:158、tdbd_dialog_zhongzhi_sh.js:72 | 终止审核「生成补退明细」假成功 |
| `PKG_RCPQ_HM.PRC_UPDATE_TC93_AC10` | :1553-1920（7%） | PKG_RCPQ:4788、FZ:2129/6095/6106、WY:960、独立过程 PRC_SALARYCOMPUTE_SH:166 | 合同终止当月 TC93/AC10 调整不执行，OUT 主键返回 NULL |
| `PKG_RCPQ_WY.PRC_TERMINATE_UNDO` | :620-984（7%，主体 :649-977 整块注释） | tdbq_cont.js:22 | 「撤销解除」两级缺陷（包级 INVALID 500；修包后变假成功）——04 文档已载 |

---

## 1. PKG_RCPQ（核心）

### 1.1 用途推断
人才派遣险金业务的总装配车间：社保/公积金报增报减异动（TC9U）、补缴费用明细与提交（TC9T）、退费审核（TFSH）、按月生成工资/险金/生育/工伤/经济补偿各类入户清单（TC8G/H/J/L/M/N、TJ05/06/07）、社保申报计算、缴费基数确认、员工/合同审核确认（TB91→TC90 状态机）。AC10.AAC104/105（社保/公积金处理状态）的系内主要流转者（db_deep §B 计数 51/27 处）。

### 1.2 过程清单（42 个；实现行号=包体内）

| # | 过程 | 关键 IN | 非标准 OUT | 功能 | 状态 |
|---|---|---|---|---|---|
| 1 | PRC_INS_TC9U :3 | 查询条件串, FLAG 1报增/2报减, 经办人5项 | — | 生成公积金异动信息（TC9U） | **禁用壳**（5%） |
| 2 | PRC_COM_TC9U :259 | TC9U 主键串, FLAG, 经办人5项 | — | 公积金异动信息提交 | **禁用壳**（17%） |
| 3 | PRC_INS_TC9T :335 | ATC900, 补缴起止月, 结算月 JSNY, 险种, 公积金比例 | **BAZ001 批次号** | 生成社保补缴费用明细（TC9T） | **禁用壳**（4%；批次号恒 NULL） |
| 4 | PRC_COM_TC9T :817 | ATC900, 起止月, MONTHS, 经办人 | — | 社保补缴提交 | 正常 |
| 5 | PRC_RECOM_TC9T :944 | ATB910, ATC9E0 | — | 审核不通过后再次提交（TC9E/TB91/TC9T 回 '2'） | 正常 |
| 6 | PRC_CANCEL_TC9T :966 | ATB910, ATC9E0 | — | 取消补缴申请（删 TC9E/TB91/TC9T） | 正常 |
| 7 | PRC_MEASUREMENT_TOOLS_1 :985 | 包干经费, 月数, 比例合计, 管理费, 应发工资 | TYPE, RESULT | 测算五项 | 正常 |
| 8 | PRC_MEASUREMENT_TOOLS_2 :1082 | 应发工资, 月数, 基数, 社险, 公积金, 管理费 | 多项反推值 | 测算反推 | 正常 |
| 9 | PRC_MEASUREMENT_TOOLS_3 :1225 | A,B,C | D~G | 二次发放个税测算 | 正常 |
| 10 | PRC_MEASUREMENT_TOOLS_4 :1291 | 应发 | 个税 | 计算个税 | 正常 |
| 11 | PRC_MEASUREMENT_TOOLS_5 :1331 | 年终奖 | 月均, 个税 | 计算年终奖个税（系内公共被调） | 正常 |
| 12 | PRC_MEASUREMENT_TOOLS_6 :1378 | 缴费基数, 公积金基数 | BAZ001 | 测算落库 TJAA82 | 正常 |
| 13 | PRC_MEASUREMENT_TOOLS_7 :1498 | 身份证, 姓名, 年月, ATC937/930 | 税前工资, 合计 | 按人算税 | 正常 |
| 14 | PRC_GJJ_SY :1581 | 键串, ATC900, 报增/报减 | — | 公积金顺延（TC9U） | 正常 |
| 15 | PRC_TFSH :1613 | ATB910, ATB91Z, 审核结果, 备注, 经办人5项 | — | 退费审核：主体分支已禁用，仅保留 TB91/TC9E 状态 UPDATE | **禁用壳**（13%，状态改写仍生效） |
| 16 | PRC_SC_GZQD :1827 | 经办年月, 主键, 发放银行/账号/名称, 发放次数, 经办人 | — | 生成工资入户清单（TC8G/TC8H） | 正常 |
| 17 | PRC_SC_XJZFMX :1965 | 社险所属年月, 经办人5项 | — | 按月生成各单位险金支付情况（TC8J/TC8L） | 正常 |
| 18 | PRC_SC_DWSZMXDZ :2220 | 社险所属年月, 经办人5项 | — | 按月生成各单位收支明细对账表 | 正常 |
| 19 | PRC_YH_CGHP :2458 | 条件串 | — | 工资入户清单回盘（TC8G/TC8UMX 状态翻转） | 正常 |
| 20 | PRC_SC_SYQD :2510 | 发放银行/账号/名称, 经办人5项 | — | 生育入户清单（TC8M/TC8N/TJ05） | 正常 |
| 21 | PRC_SC_GSQD :2608 | 同上 | — | 工伤入户清单 | 正常 |
| 22 | PRC_YH_SYGSHP :2718 | 发放银行, TYPE 生育/工伤, 经办人 | — | 生育/工伤发放成功回盘 | 正常 |
| 23 | PRC_SC_JSDY :2896 | 经办年月, 银行, 发放次数 | BAZ002 批次号 | 生成可发放工资的结算单元（TC8W） | 正常 |
| 24 | PRC_SC_SBSBJS :3019 | 社险所属年月, 当月/补缴养老个人, LB 1社保/2公积金/3 both | BAZ002 | 社保申报计算表（TJ06） | 正常 |
| 25 | PRC_SC_FYQD :3345 | 结算单位/名称, 工资/社保年月, 结算月, 发放次数 | — | 生成费用清单（TJ02） | **禁用壳**（3%，1388 行全注释；JS 调用也已注释 tddd_dialog.js:337） |
| 26 | PRC_COMPUTE_TC93 :4733 | 结算单元, 工资/社保年月, 发放次数, ATC93G, 经办人 | — | 计算 TC93 结算金额（内部转调 FZ.PRC_TC93_TC9P、HM.PRC_UPDATE_TC93_AC10） | 正常 |
| 27 | PRC_COMFIRM_SH :4852 | TB91 主键串, 审核标识, 经办人6项 | — | 员工信息/合同审核确认（TB91→TC90/TC90_LIST/AC10 状态机） | 正常 |
| 28 | PRC_UPDATE_STATUS :5445 | ATB930, AAA971 | — | 更新导出状态——**接受桩**（只置 1，无 DML） | 接受桩 |
| 29 | PRC_GENARATE_AA97 :5461 | 所属年度, 经办人 | — | 生成新签员工需补缴人员名册（AA97） | **禁用壳**（11%；前端 3 键在调） |
| 30 | PRC_缴费基数确认 :5581 | 结算单元, 所属年度, 经办人6项 | — | 缴费基数调整确认（AC10_JS） | **禁用壳**（5%） |
| 31 | PRC_AC10_JS :5899 | ATC900, 个人编号, 社保年月, 各险种基数 | — | 社险缴费基数写 AC10_JS | 瘦（18%） |
| 32 | PRC_GET_PARA :5952 | AAB001, 参保属地, 所属年月 | 单位客户号 + 10 项单位/个人比例 | 取参保比例参数——**中毒壳**：活代码仅剩 `P_APCODE:=-1; P_ERRMSG:='测算出错…'`（:5975-5976），逻辑 :5977 起全注释 | **禁用壳**（16%，全链路公共依赖） |
| 33 | PRC_BJ_BACK :6038 | 键串, 反馈结果 | — | 公积金补缴反馈（TC9U） | 正常 |
| 34 | PRC_DA_SH :6059 | TC97 主键串, 审核标志, 转入, 经办人5项 | — | TC97 档案审核 | 正常 |
| 35 | PRC_SC_JJBCQD :6108 | 发放银行/账号/名称, 经办人5项 | — | 经济补偿金入户清单（TC8Y/TC8Y_LIST） | 正常 |
| 36 | PRC_YH_JJBCJ :6195 | 发放银行, TYPE, 经办人 | — | 经济补偿金/残保金发放成功回盘 | 正常 |
| 37 | PRC_SC_SYQD_WH :6241 | 发放银行 | — | 生育入户清单银行信息维护 | 正常 |
| 38 | PRC_SC_SYQD_SH2 :6268 | 起止时间, ATC9Q1, 结算单元, 经办人 | — | 生育入户清单审核 | 正常 |
| 39 | PRC_SC_RHQD_WH :6300 | 发放银行 | — | 入户清单银行维护 | 正常 |
| 40 | PRC_SC_TC8G :6324 | 结算单元, 经办年月 | — | 工资入户清单（TC8G）状态清理 | 正常 |
| 41 | PRC_SC_TJ07 :6353 | 结算年月 | — | 重建 TJ07 结算状态表 | 正常 |
| 42 | PRC_撤销合同终止 :6474 | ATC900 | CODE, MSG | 撤销合同终止（TC90 恢复有效） | 正常 |

### 1.3 调用方
- 前端 JS（全部活跃，非注释行）：
  - `PRC_COMFIRM_SH` ← `src/main/webapp/lemis/pages/td/b/d/tdbd_cont.js:234`、`tdbd_cont.js:289`、`src/main/webapp/lemis/pages/td/a/d/tdad_cont.js:28`
  - `PRC_GENARATE_AA97` ← `src/main/webapp/lemis/pages/td/c/ac/tdcac_dialog_xz.js:25`、`td/c/ab/tdcab_dialog_xz.js:25`、`td/c/q/tdcq_dialog_xz.js:25`（**死键**：空壳假成功）
  - `PRC_撤销合同终止` ← `src/main/webapp/lemis/pages/td/b/d/tdbd_cont.js:422`
- Java：无（`src/main/java` 0 处）。
- 包间（被调）：`PRC_GET_PARA` ← FZ:759/4334/5018/6070、FZ_新结算:134、HM:1643/2294、NEW2:74、**PKG_CHECK:603**（PKG_CHECK 本体 VALID，调用时才炸 ORA-04063）；`PRC_MEASUREMENT_TOOLS_5` ← FZ:256/6696、新结算:649、PKG_IMP:3801；`PRC_MEASUREMENT_TOOLS_7` ← FZ:209、新结算:593。
- 包间（本包外调，体现链路耦合）：→ `PKG_RCPQ_FZ.PRC_TC93_TC9P`（:4832）、→ `PKG_RCPQ_HM.PRC_UPDATE_TC93_AC10`（:4788，目标是禁用壳）。

### 1.4 触碰表（INSERT/UPDATE/DELETE）
AC01(U), AC10(U), TB90(U), TB91(D/I/U), TC8G(I/U), TC8H(D/I), TC8J(D/I), TC8L(I/U), TC8M(D/I), TC8N(D/I), TC8S(U), TC8UMX(U), TC8W(I/U), TC8Y(U), TC8Y_LIST(U), TC90(U), TC90_LIST(U), TC93(U), TC97(U), TC9E(D/U), TC9P(U), TC9Q(U), TC9Q_GS(U), TC9Q_LOG(I), TC9T(D/U), TC9U(U), TJ05(D/I), TJ06(D/I), TJ07(D/I), TJAA82(I)。

### 1.5 状态判定
- BODY INVALID（A4），首错 `:349:19 PLS-00201: identifier 'SYS_ORG_USER_UNIT' must be declared`（db_deep2 §2）→ 平台视图连坐，非自身逻辑缺陷。
- 6 个禁用壳（INS_TC9U/INS_TC9T/TFSH/SC_FYQD/GENARATE_AA97/缴费基数确认）+ GET_PARA 中毒壳 + AC10_JS 瘦身 + UPDATE_STATUS 接受桩。
- 前端 6 处活引用中 3 处（GENARATE_AA97）落在空壳上；其余 3 处（COMFIRM_SH、撤销合同终止）逻辑完好，但当前包体 INVALID → 实际也是 500。

### 1.6 重写建议
**拆分重写**。依据：42 过程中被引用且逻辑完好的只有 COMFIRM_SH/撤销合同终止/COM_TC9T/RECOM/CANCEL/测算组/清单组；① 测算工具组（TOOLS_1~7，纯计算无状态）→ **Kotlin 服务层**（可单测）；② 入户清单/回盘组（SC_*/YH_*，集合批量 DML）→ **PL/pgSQL 保留**；③ 异动补缴组（INS_TC9U/COM_TC9U/INS_TC9T/TFSH/GENARATE_AA97/缴费基数确认/SC_FYQD 7 壳）→ **废弃候选**，前端对应按键应同步摘除；④ GET_PARA 必须重写为真实现（全链路依赖），优先级最高。

---

## 2. PKG_RCPQ_NEW2

### 2.1 用途推断
2018-08 NEUSOFT"人才派遣"计费数据（JFSJ = 计费数据）结算重构尝试：`PRC_CHECK_JFSJ` 校验/生成、`PRC_CONFIRM_JFSJ` 确认提交（JFSJ02/JFSJ_MX）。规格头自带 `DEF_OK/DEF_ERR` 常量风格，是一次独立于 FZ 的新结算实验。

### 2.2 过程清单（2 个）

| # | 过程 | 关键 IN | 非标准 OUT | 功能 | 状态 |
|---|---|---|---|---|---|
| 1 | PRC_CHECK_JFSJ :15 | ACE100, 结算年月, 缴费类型, 缴费险种 | — | 校验并生成计费数据（JFSJ02/JFSJ_MX） | 正常（65%） |
| 2 | PRC_CONFIRM_JFSJ :1385 | 同上 + AAA030/031 | — | 确认提交计费数据；:2086/:3047 引用 `ATC9U7` 列（真列漂移所在） | 正常（36%）但**编译必败** |

### 2.3 调用方
- 前端 JS：**0**；Java：**0**。
- 包间：本包外调 `PKG_RCPQ.PRC_GET_PARA`（:74，中毒壳）；**无任何包/页面调用本包**。

### 2.4 触碰表
JFSJ02(D/I/U), JFSJ_MX(I/U)。JFSJ 系"计费数据"三表当前全空（docs/数据库结构.md:549-551）；但序列 `SEQ_JFSJ02_ID` 已耗 267,861 号（work/sequences_2026-11-14.txt:235）→ 历史上真实跑过，现已停。

### 2.5 状态判定
BODY INVALID，首错 `:2085:45 PL/SQL: ORA-00904: "ATC9U7": invalid identifier` —— `TC9U` 表无 `ATC9U7` 列，**真编译失败，修数据即可复现、修列才能救活**（00-总览已载）。加上零调用方 + 触碰表全空，三层全死。

### 2.6 重写建议
**废弃候选**（不移栽）。依据：编译必败 + 零调用方 + 目标表空置。唯一保留动作：把 JFSJ02/JFSJ_MX 与 SEQ_JFSJ02_ID 列入"遗留表清理清单"，勿误认为活跃结算路径。

---

## 3. PKG_RCPQ_FZ

### 3.1 用途推断
费用/工资结算的"辅助包"（FZ 缩写含义未在源码注明）：险金审核（XJSH）、退费明细生成（TFMX）、费用过月（TJ04）、社保/公积金补退反馈（FEEDBACK_SB/GJJ）、工资名册导出/勾选、结算费用测算（PRO_COM_TEST 系列）、首月险金生成（CREATE_AC10）、基数调整补退明细（CHANGE_BASE）。是 RCPQ 主包结算链的下游与旁路。

### 3.2 过程清单（30 个）

| # | 过程 | 关键 IN | 非标准 OUT | 功能 | 状态 |
|---|---|---|---|---|---|
| 1 | PRO_COM_TEST :4 | 个人编号, 结算信息, 发放次数, 本月工资各项 | — | 结算费用测算（单员工试算） | 正常 |
| 2 | PRC_BT_OTHER :294 | ATC900, 起止月, 结算月, 各险种基数 | — | 其他原因补贴明细生成 | **禁用壳**（3%） |
| 3 | PRC_INSERT_TC9TCURR :718 | 结算单元, 社保年月, 结算月, FLAG, 经办人5项 | — | 补缴明细生成（TC9T 流程） | 正常 |
| 4 | PRC_TC9T_AC10 :1000 | TC9T 行, **SYS_ORG_USER_UNIT 行**, 备注, ATC9EO, 数据来源 | — | TC9T 写 AC10——参数携带平台视图行类型 → **连坐 spec INVALID** | **禁用壳**（13%） |
| 5 | PRC_XJSH :1100 | FLAG, 条件, ATC9T0, 经办人 | — | 险金审核（AC10.ATC9T4='3'） | 正常 |
| 6 | PRC_CANCLE_XJSH :1313 | ID | — | 撤销险金审核 | 正常 |
| 7 | PRC_GENERATE_TFMX :1356 | ATC900, 经办人, ATB910, 退费起止, 社保/公积金退费标志 | — | 生成退费明细（TC9E）——先例已载 00-总览 | **禁用壳**（6%；Java 在调） |
| 8 | PRC_CHANGE_BASE :1601 | AAA970, 费用终止年月, 经办人5项 | NUM | 基数调整/终止补退明细生成 | **禁用壳**（7%；前端 2 键在调） |
| 9 | PRC_PASS_JS :1877 | ATC900, ATC9T4 | — | 明细结算过审（UPDATE AC10.ATC9T4） | 正常 |
| 10 | PRC_CONFIRM_TJ02 :1896 | 结算单元, 名称, 发放次数, 结算月, LB 1确认/2撤销, 经办人 | — | 结算单元费用确认/撤销（TJ02 状态） | 正常 |
| 11 | PRC_SALARYCOMPUTE :2094 | 派遣协议, 结算单元, 工资年月, 发放次第, 社保年月, 经办人 | — | 费用结算主过程（TC93/AC10/TC9P）——前端已切 `PKG_费用结算.PRC_费用结算`，成**孤儿**（tddd_dialog_compute.js:417 已注释） | 正常（71%） |
| 12 | PRC_CANCLESALARYCOMPUTE :2240 | 结算单元, 工资年月, 发放次第, 社保年月 | — | 撤销费用结算 | 正常 |
| 13 | PRC_INS_TJ04 :2372 | 社保年月, 发放次数 | — | 生成 TJ04 过月批次 | 正常 |
| 14 | PRC_PASS_TJ04 :2611 | 结算年月, 发放次数, 经办人 | — | 费用过月（本月 TC93 复制生成下月，含个税），593 行全注释 | **禁用壳**（4%） |
| 15 | PRC_FEEDBACK_SB :3204 | IDS, 经办人 | — | 社保补退数据反馈（生成反向补/退数据） | 正常 |
| 16 | PRC_FEEDBACK_GJJ :3410 | IDS, 经办人 | — | 公积金补退数据反馈 | 正常 |
| 17 | prc_工资名册导出保存 :3581 | 结算单元, 含离职标志, 工资/社保年月, 次第, ISALL, 经办人 | — | 工资名册导出保存 | 正常 |
| 18 | prc_生成工资名头 :4164 | 派遣协议, 结算单元 | HEADER | 工资名册表头生成（AA90 列拼串） | 瘦（20%） |
| 19 | PRC_GET_MONEY :4264 | 单位编号, 补缴起止月, 各险种基数 | — | 补缴金额测算 | 正常 |
| 20 | PRC_CREATE_CS :4436 | 结算单元, 结算月, 月起止, 经办人, 金额 | ID | 生成测算批次（TC8U_CS/TJ02_CS） | 正常 |
| 21 | PRC_INS_TC94 :4882 | ATC900, 结算单元 | — | TC94 明细插入 | 正常 |
| 22 | PRC_CREATE_TITLE :4908 | 结算单元 | HEADER | 工资名册标题生成 | 正常 |
| 23 | PRC_CREATE_AC10 :4988 | 结算单元, 名称, 社保年月, 参保属地, 单位编号, 经办人 | — | 首月险金生成（AC10） | **禁用壳**（9%） |
| 24 | PRC_DELETE_AC10 :5118 | BAZ001 | — | **接受桩**（只置 1，无 DML） | 接受桩 |
| 25 | PRC_DEL_TC9P :5130 | ATC9P0 | — | 删除 TC9P | 正常 |
| 26 | PRC_TC93_TC9P :5187 | 结算单元, 结算月, 费用月, 工资/社保年月, 发放次数 | — | TC93→TC9P 生成（被 PKG_RCPQ:4832 调） | 正常 |
| 27 | PRC_CANCLE_TJ04 :5301 | 结算单元主键串, 结算年月, 发放次数, 经办人 | — | 取消过月（删下月 TC94/TC93） | 正常 |
| 28 | prc_工资名册勾选保存 :5385 | 同 17 | — | 工资名册勾选保存 | 正常 |
| 29 | PRC_SALARYCOMPUTE2 :5970 | 同 11 | — | 结算第二版——整体弃用 | **禁用壳**（5%） |
| 30 | PRO_COM_TEST_TJ04 :6468 | 同 1 | — | TJ04 版费用测算 | 正常 |

### 3.3 调用方
- 前端 JS（活跃）：`PRC_CHANGE_BASE` ← `td/b/d/tdbd_dialog_zzsh.js:158`、`td/b/d/tdbd_dialog_zhongzhi_sh.js:72`（死键）；`PRC_CONFIRM_TJ02` ← `td/d/d/tddd_dialog.js:47`、`:83`；`PRC_CREATE_CS` ← `td/d/v/tddv_cont.js:134`；`PRC_GET_MONEY` ← `td/b/f/tdbf_dialog_xinqcl.js:157,247,1259`、`td/b/a/tdba_cont.js:2231`；`PRC_PASS_JS` ← `td/b/d/tdbd_dialog_mxjs.js:17,41`；`prc_工资名册导出保存`/`prc_生成工资名头` ← `td/d/b/tddb_dialog_export.js:191,204`。
- Java：`PRC_GENERATE_TFMX` ← `src/main/java/com/neusoft/lemis/apps/lemis/td/b/f/service/tdbfService.java:155`（死键）。
- 已注释的前端引用：`PRC_SALARYCOMPUTE`（tddd_dialog_compute.js:417）、`pkg_rcpq.PRC_sc_fyqd`（tddd_dialog.js:337）——不再算调用方。
- 包间（被调）：`PRO_COM_TEST` ← 新结算:456、HM:1154/1838；`PRO_COM_TEST_TJ04` ← HM:2489；`PRC_TC93_TC9P` ← PKG_RCPQ:4832。本包自调 `PRC_CANCLE_TJ04`（:5382）、`PRO_COM_TEST`（:6300）。
- 包间（外调）：→ RCPQ.GET_PARA/TOOLS_5/TOOLS_7、→ HM.PRC_UPDATE_TC93_AC10（:2129/6095/6106）、HM.PRC_UPDATE_TC93_AC10_TJ04（:3177）、→ WY.PRC_SET_ATC904（:1203/1298/1340/3215/3398）、WY.PRC_INS_AC10_CURR（:3690，禁用壳）、WY.PRC_CHECK_AA90_MOD（:4054/5861）、WY.PRO_INS_TC8UMX（:2031）。

### 3.4 触碰表
AC10(D/I/U), TC31(U), TC8U(D/I), TC8UMX(D), TC8U_CS(D/I/U), TC90(U), TC91(U), TC91_LOG(D), TC93(D/I/U), TC94(D), TC9E(U), TC9P(D/U), TC9T(D/I), TJ02(U), TJ02_CS(D/I), TJ04(I/U)。

### 3.5 状态判定
- **SPEC+BODY 双 INVALID**（A4 唯一规格头失效者）：spec 首错 `:59:41 PLS-00201 SYS_ORG_USER_UNIT`（PRC_TC9T_AC10 的 `PRM_USER IN SYS_ORG_USER_UNIT%ROWTYPE` 参数），body 随之 PLS-00905（db_deep2 §2）。
- 7 个禁用壳（BT_OTHER/TC9T_AC10/GENERATE_TFMX/CHANGE_BASE/PASS_TJ04/CREATE_AC10/SALARYCOMPUTE2）+ 1 接受桩（DELETE_AC10）+ 1 瘦（生成工资名头）；30 过程中仅 21 个有完整逻辑。
- 有真实活引用的 9 个入口中 2 个落在空壳上（CHANGE_BASE、GENERATE_TFMX）。

### 3.6 重写建议
**拆分重写**。依据：① 险金审核组（XJSH/CANCLE_XJSH/PASS_JS/CONFIRM_TJ02/FEEDBACK_SB/GJJ）→ **Kotlin 服务层**（状态机清晰、被前端活用）；② 工资名册组（导出保存/勾选保存/名头/标题）→ **PL/pgSQL 保留**（拼串导出适合 SQL）；③ 测算组（PRO_COM_TEST 系列/GET_MONEY/CREATE_CS）→ **Kotlin 服务层**；④ 结算主过程（SALARYCOMPUTE 已被 PKG_费用结算 取代、SALARYCOMPUTE2/CREATE_AC10/PASS_TJ04 壳）→ **废弃候选**；⑤ 重写时必须消除 `SYS_ORG_USER_UNIT%ROWTYPE` 入参反模式（这是 spec 失效的唯一起因）。

---

## 4. PKG_RCPQ_FZ_新结算

### 4.1 用途推断
2018-07-17 "优化费用结算"：`PKG_RCPQ_FZ.PRC_SALARYCOMPUTE` 的重写实验版（参数注释里直接标"没用上"，可见是移植半成品）。

### 4.2 过程清单（1 个）

| # | 过程 | 关键 IN | 非标准 OUT | 功能 | 状态 |
|---|---|---|---|---|---|
| 1 | PRC_SALARYCOMPUTE :15 | 结算单元, 工资年月, 发放次第, 社保年月（ATB900/AAE011 标注"没用上"） | — | 优化版结算主过程（TC93/TC9P/AC10/TC8S/TC31） | 正常（62%）但**编译必败** |

### 4.3 调用方
- 前端 JS：唯一引用 `tddd_dialog_compute.js:418` **已被注释**（活跃行是 `PKG_费用结算.PRC_费用结算`，:419）。Java 0。
- 包间：0 被调。本包外调 RCPQ.GET_PARA（:134，中毒壳）/TOOLS_5（:649）/TOOLS_7（:593）、FZ.PRO_COM_TEST（:456）。

### 4.4 触碰表
AC10(U), TC31(U), TC8S(U), TC93(U), TC9P(U)。

### 4.5 状态判定
BODY INVALID，首错 `:222:18 PLS-00302: component 'ATC9U2' must be declared`——`R_AC10.ATC9U2` 记录组件在 AC10 表不存在（真列漂移，00-总览已载）。零调用方 + 被 PKG_费用结算 取代。

### 4.6 重写建议
**废弃候选**。依据：编译必败 + 前端已切走 + 零包间调用。RCPQ 结算功能以 `PKG_费用结算`（中文核心业务包组）为唯一基准线，本包仅作历史参考。

---

## 5. PKG_RCPQ_HM

### 5.1 用途推断
员工管理周边的"卫星包"：比例调整批量补退测算（CPT_RATIO）、公积金按月缴存生成（GJJ_SX→AC10_JS）、到款/冲账审核（SET_DK/SET_KF/FINANCE_SH）、离职工资校验与提交（CHECK/COMMIT_LZ_SALARY）、单位支付批量处理（SET_DW_ZF/_GS、IST_GS 工伤）、合同终止日期查询（GET_ATC90D）、二级单位调转（CHANGE_EJDW）、续签数据生成（CREATE_XQ）、合同终止当月 TC93/AC10 调整（UPDATE_TC93_AC10 系列，含过月版 _TJ04）。任务标注其为"外网"包，但**源码调用方全部在内网员工管理菜单**（tdbb/tdbe/tdbf/tdba），网银/到账特征反而集中在 PKG_RCPQ_WY——缩写含义与部署归属建议另行核实。

### 5.2 过程清单（16 个）

| # | 过程 | 关键 IN | 非标准 OUT | 功能 | 状态 |
|---|---|---|---|---|---|
| 1 | PRC_CPT_RATIO :4 | 起止年月, 人员范围, 12 项单位/个人比例, 经办人 | BAZ001 批次号 | 比例调整批量补退测算（TC9E/AC10） | **禁用壳**（4%） |
| 2 | PRC_GJJ_SX :501 | AAA971, 经办人5项 | — | 公积金按月缴存生成（AC10_JS）——**AA97.AAA97G 列漂移所在**（:574/580/590/611/657） | 正常（80%）但致整包编译必败 |
| 3 | PRC_SET_DK :667 | ATC8U0 | — | 到款确认（TC8U.ATC8U1/ATC93U='1'，联动 TC93） | 正常 |
| 4 | PRC_SET_KF :694 | ATC8U0, 备注 | — | 划扣状态设置（TC8U/TC93） | 正常 |
| 5 | PRC_FINANCE_SH :723 | KEYS, TYPES, RESULT, 审核人 | — | 到账及冲账审核 | 正常 |
| 6 | FN_SPLIT :1051 | 字符串, 分隔符 | 返回 T_ARRAY | 字符串拆分工具 | 正常 |
| 7 | PRC_CHECK_LZ_SALARY :1082 | ACE100, 结算单元, 工资年月, 发放次数 | — | 校验离职员工导入工资正确性（TC93_LZ） | 正常 |
| 8 | PRC_COMMIT_LZ_SALARY :1209 | ACE100, 结算单元 | — | 离职工资提交（TC93 生成） | 正常 |
| 9 | PRC_SET_DW_ZF :1345 | KEYS, 经办人 | — | 单位支付/作废批量处理（TC9Q 置 '3' 个人已支付） | 正常 |
| 10 | PRC_IST_GS :1435 | ID, TYPES, 经办人 | — | 插入工伤数据 | 正常 |
| 11 | PRC_SET_DW_ZF_GS :1475 | KEYS | — | 单位支付（工伤版） | 正常 |
| 12 | PRC_GET_ATC90D :1534 | ATC90C | ATC90D | 取合同终止日期（月加法） | 正常（前端 4 处在调） |
| 13 | PRC_UPDATE_TC93_AC10 :1553 | ATC900, 工资次第, 社保年月, ATC930, 结算月 | **KEY_1, KEY_2** | 合同终止当月 TC93/AC10 调整——**禁用壳**（7%），OUT 主键恒 NULL；被 RCPQ/FZ/WY/独立过程 6 处调用 | **禁用壳** |
| 14 | PRC_CHANGE_EJDW :1921 | AAB001, KEYS | — | 调转二级单位（UPDATE TC90/TC90_LIST 换单位）——**包级 INVALID → tdbe 确认键点击即 500**（04/05 文档实测） | 正常（82%） |
| 15 | PRC_CREATE_XQ :1960 | KEYS, 合同类别/签订类型/起止日期, 经办人 | BAZ001 批次号 | 生成续签数据（TC90_LIST 插入 + TC90 更新）——tdbb 续签键，包级 500 | 正常（30% 瘦） |
| 16 | PRC_UPDATE_TC93_AC10_TJ04 :2204 | 同 13 | KEY_1, KEY_2 | 过月版 TC93/AC10 调整（FZ:3177 调用） | **禁用壳**（7%） |

### 5.3 调用方
- 前端 JS（活跃）：`PRC_CHANGE_EJDW` ← `td/b/e/tdbe_dialog_query.js:43`；`PRC_CREATE_XQ` ← `td/b/b/tdbb_dialog_renewall.js:34`；`PRC_GET_ATC90D` ← `td/b/b/tdbb_dialog_renew.js:240`、`tdbb_dialog_renewall.js:112`、`td/b/f/tdbf_dialog_xinqcl.js:615`、`td/b/a/tdba_cont.js:1017`。
- Java：0。
- 包间（被调）：`PRC_UPDATE_TC93_AC10` ← PKG_RCPQ:4788、FZ:2129/6095/6106、WY:960、独立过程 `PROCEDURE_PRC_SALARYCOMPUTE_SH.sql:166`；`PRC_UPDATE_TC93_AC10_TJ04` ← FZ:3177。
- 包间（外调）：→ RCPQ.GET_PARA（:1643/2294）、FZ.PRO_COM_TEST（:1154/1838）、FZ.PRO_COM_TEST_TJ04（:2489）；自调 PRC_SET_DK（:811/871）。

### 5.4 触碰表
AA97(U), AC10_JS(I/U), TC8T(U), TC8U(U), TC8V(D/U), TC8Y(U), TC8Y_LIST(U), TC90(U), TC90_LIST(I/U), TC93(I/U), TC93_LZ(U), TC94(I), TC9Q(U), TC9Q_GS(I/U), TC9Q_LOG(I)。

### 5.5 状态判定
- BODY INVALID 双重病因：① 平台视图 `:39:13 PLS-00201 SYS_ORG_USER_UNIT`；② **真列漂移 `AA97.AAA97G` ×4 处 ORA-00904**（源码可见 5 行引用，均在 PRC_GJJ_SX；00-总览/04 文档已载）。即便修好平台视图，②仍使编译必败。
- 2026-11-14 实机重放：tdbe「确认转入」→ HTTP 500（05-重放实测.md:28）；tdbb 续签/合同起止查询同因不可用。
- 3 个禁用壳（CPT_RATIO、UPDATE_TC93_AC10、UPDATE_TC93_AC10_TJ04），其中 UPDATE_TC93_AC10 被 6 处包间调用——RCPQ/FZ/WY 的终止结算链即使自身恢复编译，走到这一步也是"空转 + 主键 NULL"。

### 5.6 重写建议
**拆分重写（优先级系列内最高）**。依据：前端 3 个活键（EJDW 调转、CREATE_XQ 续签、GET_ATC90D 日期查询）当前全部 500，是员工管理菜单的直接故障点。① GET_ATC90D/CHANGE_EJDW/CREATE_XQ/FINANCE_SH/SET_DK/SET_KF/CHECK_LZ_SALARY/COMMIT_LZ_SALARY → **Kotlin 服务层**（单表/小事务，救活 3 键成本最低，顺带删掉 AAA97G 死列引用）；② UPDATE_TC93_AC10 系列 2 壳 → 按调用方决策：RCPQ/FZ/WY 重写时把该逻辑并入调用方或 **Kotlin 重实现**，禁用壳本体**废弃候选**；③ CPT_RATIO 壳 → **废弃候选**；④ FN_SPLIT → 语言内置替代，不留。

---

## 6. PKG_RCPQ_TJ

### 6.1 用途推断
统计报表专用包（TJ=统计）：7 个返回嵌套表（TAB_工资表 等）的管道函数供 .rdx 报表引擎 `SELECT * FROM TABLE(...)` 调用；外加平均人数、包干经费、收支统计与 4 类提醒/预警生成器。全系**唯一没有禁用壳**的包。

### 6.2 过程清单（17 个）

| # | 过程 | 关键 IN | 非标准 OUT | 功能 | 状态 |
|---|---|---|---|---|---|
| 1 | PRC_GET_CNT :4 | 结算单元主键串, 起止年月 | COUNT 平均人数 | 计算结算单元平均人数 | 正常 |
| 2 | FUN_工资表 :72 | 结算单元, 年月 | 返回 TAB_工资表 | 报表函数：工资表 | 正常 |
| 3 | FUN_员工年收入统计表 :172 | 单位编号, 年月 | 返回 TAB_员工年收入统计 | 报表函数：员工年收入 | 正常 |
| 4 | FUN_管理费分项明细汇总 :236 | 年月 | 返回 TAB_管理费分项明细汇总 | 报表函数：管理费分项明细 | 正常 |
| 5 | FUN_往来款分类统计 :413 | 起止年月, 单位编号 | 返回 TAB_往来款项 | 报表函数：往来款分类 | 正常 |
| 6 | FUN_年平均人数统计 :586 | 起止年月 | 返回 TAB_平均人数 | 报表函数：年平均人数 | 正常 |
| 7 | FUN_管理费分类统计 :696 | 起止年月 | 返回 TAB_管理费分类统计 | 报表函数：管理费分类 | 正常 |
| 8 | PRC_包干经费统计 :741 | 年月 | CODE, MSG | 生成包干经费统计（TJ01） | 正常 |
| 9 | FUN_人员统计 :827 | 统计年月, LABEL 1一级/2二级 | 返回嵌套表 | 报表函数：人员统计表 | 正常 |
| 10 | PRC_FEE_WARNING :1298 | — | — | 费用测算预警（TJ03） | 正常 |
| 11 | PRC_RH_LIST :1409 | 经办年月, 经办人 | — | 月入户清单统计表（TJ015） | 正常 |
| 12 | PRC_WARM_SUGGESTION :1548 | 经办人 | — | 事项提醒（TJ016） | 正常 |
| 13 | PRC_LESS_THREE :1736 | — | NUM | 折子号余额不足 3 个月提醒（TJ017） | 正常 |
| 14 | PRC_INS_TC8K :1810 | 统计年度 | — | 生成 TC8K 年度统计 | 正常 |
| 15 | PRC_EXPIRATION_REMINDER :1988 | — | START, END | 计算合同到期区间提醒 | 正常 |
| 16 | PRC_INCOME :2055 | 年月 | — | 收入统计（TJ01_INCOME） | 正常 |
| 17 | PRC_EXPENDITURE :2128 | 起止年月 | CODE, MSG | 支出统计（工资按入户回盘、险金按地税托收、残保金等分项，TJ01_EXPENDITURE） | 正常 |

### 6.3 调用方
- 报表 .rdx（`SELECT * FROM TABLE(PKG_RCPQ_TJ.FUN_xxx(...))` / `call pkg_...`）：`FUN_工资表` ← `src/main/webapp/lemis/report/td/tdhc_工资表.rdx:2131`；`FUN_员工年收入统计表` ← `tdhb_员工年收入统计.rdx:818`；`FUN_管理费分项明细汇总` ← `tdhd_管理费分项明细汇总.rdx:3824`（同文件另有 11 处 :4032~:6112）；`FUN_往来款分类统计` ← `tdhg_往来款项统计.rdx:1035`；`FUN_年平均人数统计` ← `tdhf_年平均人数统计.rdx:665`；`FUN_管理费分类统计` ← `tdhh_管理费分类统计.rdx:431`；`FUN_人员统计` ← `tdhe_人员统计表_一级.rdx:677`、`tdhe_人员统计表_二级.rdx:742`。
- 页面 JS：`PRC_WARM_SUGGESTION` ← `src/main/webapp/lemis/pages/td/n/a/tdna_cont.js:290`（事项提醒）。
- Java / 包间：0 被调、0 外调（系列内唯一无包间耦合的包）。

### 6.4 触碰表
TC8K(D/I), TC8Q(D/I), TD03(U), TJ01(D/I), TJ015(D/I), TJ016(D/I), TJ017(I), TJ01_EXPENDITURE(D/I/U), TJ01_INCOME(D/I), TJ03(I)。TJ01 1539 行 / TJ03 1442 行、TJ015/TD03 空（docs/数据库结构.md:234-235,486,490）→ 报表线半停。

### 6.5 状态判定
BODY INVALID，首错 `:461:16 PL/SQL: ORA-04063: view "CCRCPQ.V_AB01_TB90_TB93" has errors`——`V_AB01_TB90_TB93` 视图依赖 `SYS_ORG_USER_UNIT` 连坐（db_deep D2）。自身逻辑 100% 完整（17 项全正常、无注释禁用）。**修好平台视图即整包复活**，统计报表（tdhc~tdhh 8 个 rdx）与 tdna 事项提醒随之恢复。

### 6.6 重写建议
**PL/pgSQL 保留**（低成本先修视图）。依据：逻辑完整、零耦合、纯只读/临时表统计，复杂度中等；7 个管道表函数在 PG 可用 `RETURNS TABLE` + `RETURNS SETOF` 直译。次选：统计函数改 **Kotlin 只读查询**（报表引擎若同期替换）。提醒/预警四过程随主流程一起保留。

---

## 7. PKG_RCFP_TJ

### 7.1 用途推断
2011-01 作者 pansl（Neusoft）"民办培训管理统计"包，`AUTHID CURRENT_USER`：非普高等教育毕业生就业审批统计（TJ_01/TJ_02）与"郑州市人社局人事人才办事大厅工作情况统计表"（TJ_03）。与人才派遣主业务**不同产品线**，是历史遗留的搭车代码。

### 7.2 过程清单（4 个）

| # | 过程 | 关键 IN | 非标准 OUT | 功能 | 状态 |
|---|---|---|---|---|---|
| 1 | PRC_CREATE_HZD :6 | — | NUMBER（序列号） | 生成日期序列（回执/汇总单号） | 正常 |
| 2 | prc_tj_01 :59 | 统计起止时间, 经办人 | — | 就业审批统计（读 TB10/TC10 → 写 TJTK1/TJTK2） | 正常（62%）但**编译必败** |
| 3 | prc_tj_02 :157 | 呈办单位流水号, 批次, 统计起止, 经办人 | — | 就业审批花名册（TJTK2） | 正常（66%） |
| 4 | prc_tj_03 :237 | 年度, 季度, 部门/工作人员/业务范围, 经办人 | — | 郑州市人社局人事人才办事大厅工作情况统计表（TJTK3） | 正常（60%） |

### 7.3 调用方
- 报表 .rdx（小写调用，`call pkg_rcfp_tj.prc_...`）：`prc_tj_01` ← `src/main/webapp/lemis/report/tm/tmba_2011_非普高等教育毕业生就业审批表.rdx:854`；`prc_tj_02` ← `tm/tmbb_2011_非普高等教育毕业生就业审批登记表.rdx:756`；`prc_tj_03` ← `tm/tmbc_2011_郑州市人社局人事人才办事大厅工作情况统计表.rdx:1564`。
- `PRC_CREATE_HZD` 无任何调用方。Java/包间：0。

### 7.4 触碰表
TJTK1(I, 3 行), TJTK2(I, 5 行), TJTK3(I, 2 行)；读 TB10（13 行）、TC10（**AAEB36 列不存在**，`PACKAGE_BODY_PKG_RCFP_TJ.sql:71-72` 声明 `tc10.aaeb36%TYPE` 即编译错）。产出表仅 2~5 行 → 2011 年后基本未再生成。

### 7.5 状态判定
BODY INVALID，首错 `:70:23 PLS-00302: component 'AAEB36' must be declared`（真列漂移，00-总览已载）。三个 2011 年报表定义还在 webapp 里，但目标统计表近乎空 → 业务线实际已停。

### 7.6 重写建议
**废弃候选**（确认"民办培训/毕业生就业审批"业务已停后不移栽）。依据：编译必败 + 产线外业务 + 目标表空置 + 无包间耦合。若业务方确认还要这三张 2011 报表，再按"Kotlin 只读统计"小成本重写（440 行、逻辑简单）。

---

## 8. PKG_RCPQ_WY

### 8.1 用途推断
员工状态收尾与资金到账的"外网/网银"侧包（WY 缩写源码未注明；docs/数据库状态字段与关联关系梳理.md:153 猜测"外网接口"）：合同终止撤销（TERMINATE_UNDO）、合同期限变更撤销（QXBG_BACK）、正常退费生成（RETURNS_NORMAL）、险金提交（COMMIT_ALL/COMMIT_XZ）、工资名册不代发、网银（TC8I/TC8I_LIST）导入核销与审核、事项到账（TC8Y/TC8Y_LIST）、TD04 确认（生成 TC8U/TC8UMX）。TC8I/TC8I_LIST/TC8Y/TC8Y_LIST 仅 3~5 行、TC8T/TC8UMX/TC8V 空（docs/数据库结构.md:382-399,421,576-578）→ 网银/到账支线基本没跑起来。

### 8.2 过程清单（23 个）

| # | 过程 | 关键 IN | 非标准 OUT | 功能 | 状态 |
|---|---|---|---|---|---|
| 1 | PRC_RETURNS_NORMAL :4 | ATC900, 退费起止, 退费类别, 经办人 | ATC9E0 退费数据 | 正常退费生成（TC9E） | 正常 |
| 2 | PRC_DELETE_XQ :292 | TC90_LIST 主键串, 合同主键 | — | 删除续签数据 | 正常 |
| 3 | PRC_SET_ATC904 :339 | ATC900 | — | 按 AC10 处理状态回写 TC90.ATC904（险金审核/社保反馈标志） | 正常 |
| 4 | PRC_INS_AC10_CURR :389 | ATC900, 社保年月, 备注, 经办人 | — | 生成当月险金（AC10）——FZ:3690 调用的是它 | **禁用壳**（11%） |
| 5 | PRC_CHECK_ISLOWER :538 | AAA971, TYPE | VALID NUMBER | 校验是否低于缴存下限（AA98） | 正常 |
| 6 | PRC_TERMINATE_UNDO :620 | ATC900, 经办人 | — | 撤销合同终止（恢复 TC90/TC90_LIST、删终止退费、按上月重建当月 TC93/AC10）——主体 :649-977 整块 `/*…*/`（档案已载的空壳先例）；前端 tdbq 在调，包级 INVALID → 点击即 500（05 文档） | **禁用壳**（7%） |
| 7 | PRC_QXBG_BACK :985 | ATC900, IKEY | — | 合同期限变更撤销 | 正常 |
| 8 | PRC_GET_AC10_CNT :1037 | AAC001 | — | AC10 当月计数 | 正常 |
| 9 | PRC_COMMIT_ALL :1079 | 条件串 | — | 险金全部提交 | 正常 |
| 10 | PRC_COMMIT_XZ :1150 | KEYS | — | 险金提交（新增） | 正常 |
| 11 | PRC_CHECK_AA90_MOD :1226 | 结算单元, 工资/社保年月, 发放次数 | FLAG | 检查单位资料是否有当月修改（FZ:4054/5861 调） | 正常 |
| 12 | PRC_工资名册_不代发 :1311 | 派遣协议, 工资/社保年月, 次第, 经办人 | — | 生成不代发工资名册 | 正常 |
| 13 | PRC_CS_ZJ :1506 | 费用清单年月, 结算单元, 发放次数 | — | 测算折子余额清单（AA83 重建） | 正常 |
| 14 | PRC_SET_TC93 :1873 | 结算单元, ATB90AC | — | 设置 TC93 标志 | 正常 |
| 15 | PRC_GET_INFO :1896 | ATC900, 结算单元 | 代发标志, 工作年限, 近 12 月/月均收入等 | 取员工综合信息 | 正常 |
| 16 | PRC_CONFIRM_TD04 :2002 | 结算单元, 结算月, 经办人 | — | TD04 确认：汇总生成 TC8U/TC8UMX | 瘦（32%） |
| 17 | PRO_INS_TC8UMX :2185 | ATC8U0 | — | 生成 TC8UMX 明细（FZ:2031 调） | 正常 |
| 18 | PRC_INS_TC8I_LIST :2372 | 核销费用主键串, 网银主键, 经办人 | — | 网银核销明细（TC8I_LIST） | 正常 |
| 19 | PRC_INS_TC8I_LIST1 :2447 | TC8H 主键, 网银主键, 社保年月, FLAG | — | TC8H→TC8I_LIST 生成 | 正常 |
| 20 | PRC_SH_TC8ILIST :2680 | ID, AAE114, 经办人 | — | 网银明细审核 | 正常 |
| 21 | PRC_DEL_TC8V :2739 | ATC8V0 | — | 删除 TC8V | 正常 |
| 22 | PRC_GET_JFSJ :2786 | 年月 | ATC8N1, ATC8N2 | 取当月养老缴费合计（JFSJ_MX；PKG_CHECK:3811 调） | 正常 |
| 23 | PRC_INS_TC8Y_LIST :2818 | 事项到账主键, TC9Q 主键, 到账日期, 经办人, 金额合计 | — | 生育到账登记（TC8Y_LIST） | 正常 |

### 8.3 调用方
- 前端 JS（活跃，当前因包级 INVALID 全部 500）：`PRC_COMMIT_ALL`/`PRC_COMMIT_XZ` ← `td/b/a/tdba_dialog_query_tc90.js:57/:30`；`PRC_CONFIRM_TD04` ← `td/d/y/tddy_dialog.js:15`；`PRC_GET_INFO` ← `td/d/x/tddx_cont.js:293`；`PRC_QXBG_BACK` ← `td/b/b/tdbb_dialog_modify.js:21`；`PRC_SET_TC93` ← `td/a/a/tdaa_dialog_tb93.js:112`；`PRC_TERMINATE_UNDO` ← `td/b/q/tdbq_cont.js:22`。
- Java：0。
- 包间（被调）：`PRC_SET_ATC904` ← FZ:1203/1298/1340/3215/3398；`PRC_INS_AC10_CURR` ← FZ:3690（**目标是禁用壳**）；`PRC_CHECK_AA90_MOD` ← FZ:4054/5861；`PRO_INS_TC8UMX` ← FZ:2031；`PRC_GET_JFSJ` ← PKG_CHECK:3811。本包自调 PRC_CONFIRM_TD04（:2181）、PRC_DEL_TC8V（:2783）、PRC_INS_TC8I_LIST（:2443/2676）、PRC_INS_TC8Y_LIST（:2891）、PRC_SH_TC8ILIST（:2736）、PRC_INS_AC10_CURR（:1389）。

### 8.4 触碰表
AA83(D/I/U), AA97(U), AC10(I), TB91(U), TC8I(U), TC8I_LIST(D/I/U), TC8T(U), TC8U(I/U), TC8UMX(I/U), TC8V(D), TC8Y(U), TC8Y_LIST(I), TC90(U), TC90_LIST(D/U), TC93(U), TC9E(I/U), TC9Q(U), TD04(U)。

### 8.5 状态判定
BODY INVALID，首错 `:11:12 PLS-00201 SYS_ORG_USER_UNIT`（平台视图连坐）。7 个前端活键全部点击即 500（比静态"空壳假成功"更早失败，05 文档已载 tdbq 实测）。2 个禁用壳（TERMINATE_UNDO、INS_AC10_CURR——后者被 FZ 调用，险金当月生成链空转）+ 1 瘦（CONFIRM_TD04）。SYS_ERR_LOG 全史 0 条 TERMINATE_UNDO 记录 = 生产从未成功用过（04 文档）。

### 8.6 重写建议
**拆分重写**。依据：① 前端 7 键中 5 个逻辑完好（COMMIT_ALL/COMMIT_XZ/QXBG_BACK/SET_TC93/GET_INFO）→ **Kotlin 服务层**（恢复 td/a、td/b/a、td/b/b、td/d/x、td/d/y 五个页面的死键）；② TERMINATE_UNDO 需**从注释残骸里考古重建**（365 行被注释的撤销逻辑是唯一蓝本）→ 先按 04 文档确认业务是否仍要，再决定 Kotlin 重实现或**废弃候选**；③ INS_AC10_CURR 壳 → 并入 FZ 重写的当月险金生成，本体废弃；④ 网银/到账组（TC8I×3、TC8Y、TD04、TC8UMX、CS_ZJ，目标表全空）→ **废弃候选**；⑤ 工资名册_不代发 → 若 td/b/t 花名册核对仍要则入 PL/pgSQL。

---

## 附：本档案的可复现命令

```bash
python3 -I scripts/db/profile_rcpq_pkgs.py      # 签名/触碰表/禁用检测 -> work/rcpq_profile_raw.txt
python3 -I scripts/db/rcpq_proc_matrix.py       # 每过程有效行占比 × 调用方 -> work/rcpq_proc_matrix.txt
python3 -I scripts/db/find_rcpq_callers.py      # 前端/Java 调用点 -> work/rcpq_callers.txt
python3 -I scripts/db/find_rcpq_crossrefs.py    # 包间交叉引用 -> work/rcpq_xrefs.txt
python3 -I scripts/db/rcpq_live_code.py         # 禁用壳的活代码行 -> work/rcpq_live_code.txt
```
