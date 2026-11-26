# td/d 工资域对照 — 东软 社保/工资 ↔ 新系统 salary·calc·导入通道

> 2026-11-24。真值源：东软侧=module-inventory 波次 8 行（8a-lite/8a/8b 实测记录）+旧 webapp td/d 13 子目录
> +战役 18/19/21/26 卷（工资计算双过程/平均工资真人全链/花名册/所得税参数）+rewrite-spec 05 卷（含审计勘误）；
> 新侧=salary.js/calc.js 实源+ui-reference 渲染态+TddCases/TddSettlementCases+api-reference。薪资主档表
> TC93/TC94、细项 AA90、参数 AA92/AA93/AA98/AA99。

## 〇、结论速览

- 东软 td/d **42 页 66 交互**（13 子目录：a=tdda 细项/c=tddc 导入/d=tddd 计算/f=tddf 默认名册/g=tddg
  核对/b·v·x·y·ab·ae·af·ag=待勘与弹窗族）+ 派遣工资菜单族 tdcao·tdix·tdbt·tdga → 新系统 **salary**
  （编制→核对→提交 + 结算确认/取消）+ **calc**（险金测算）+ **/import/tddc**（EasyExcel 薪资导入）+
  TddCases 后端（tdda/tddd/tddf 三服务合并规格重写）。
- 薪资主链全通 e2e：导入(37182 批次真数据冒烟)→编制(/wb/salary-setup)→核对(PKG_IMP.CHECK_TC93 三金额
  比对+最低工资 80% 门槛)→提交(atc93p='0')→结算确认(SALARYCOMPUTE atc93g='1'+折子扣款)→取消(忠实
  坏功能第六例 TC31 ORA-00904)。
- **划走未移植三过程**（工资域最后一块）：PRC_计算平均工资(8 参)/PRC_按单位计算平均工资(7 参)/
  PRC_工资花名册前台(5 参, 战役 21 卷实测 200 读路径)——/proc 未注册，前端 tdcao/tdix/tdbt 未接。
- **忠实缺陷账（本域 5 件）**：坏功能第三例(SALARYCOMPUTESINGLE 过程不存在→忠实报错)/第四例(aa80 HQL
  SET 语法错误→忠实)/第六例(cancel TC31 无列→忠实报错)/空壳(PRC_UPDATE_TC93_AC10 300 行被注释)/
  空转(TC91/TC91_LOG/TC9P 全 0 行逻辑保留)。
- 待勘页：tddb/tddv/tddx/tddy/tddab/tddae/af/ag 八个子目录页面语义未逐页勘定（其中 tddx 属死按钮族）。
- 辨析（二期 06 卷）：tdgg"平均工资配置"=AA98.AAA982 社会平均工资（基数区间来源），与 tdix 按单元平均工资、
  tdcao 按人平均工资是三个不同功能；前者随 jishu 上下限链已可用。

## 一、域级总表

| 东软页面 | 内容 | 新承接 | 形态 |
|---|---|---|---|
| tddg 工资核对（工资次第信息） | TC93_BP1 核对批次 | **salary 页**：一键编制(/wb/salary-setup 从合同带单位/结算单元)→核对→提交 | 核对=三金额比对+AA99 最低工资 80% 门槛；核对置 atc93p、结算才置 atc93g（页面说明文字忠实此关系） |
| tddd 费用结算确认（8b） | SALARYCOMPUTE 系 | **salary·结算确认/取消** | PKG_RCPQ_FZ 两过程+TC93_TC9P；落账/折子/体检费孤儿行计数回显；取消=忠实第六例 |
| tddc Excel 导入薪资（8a） | TC93/TC94 | **POST /import/tddc**（EasyExcel） | tddcService(340L 全系统最大单文件)：12 类累加+明细串+atc93aa 合计；37182 批次真数据冒烟全对 |
| tdda 工资细项维护 | AA90 模板克隆/复制改删（换行式修改） | **TddCases 后端就绪，前端未接** | 8a-lite 实测；换行式修改=rewrite-spec 05（二阶段可简化 UPDATE） |
| tddf 默认名册切换 | AA81 克隆 | **TddCases 后端就绪，前端未接** | 8a-lite 实测 |
| tddd TC93 标志回写+TC8Z | — | TddCases 后端 ✓ | 8a-lite 实测 |
| tddb/tddv/tddx/tddy/tddab/tddae/af/ag | 八子目录 | **待勘** | tddx 属死按钮族（第八例）；其余未逐页勘定 |
| tdcao 平均工资（派遣工资菜单） | PRC_计算平均工资+按单位 | ❌ 划走未移植 | 工资域波次最后一块；除零防护=二阶段 backlog |
| tdix 平均工资（按结算单元） | TC93_PJGZ 快照重建（PKG_工资发放.PRC_按单位计算平均工资，body:3-92；读 V_TC93_TC9AA） | ❌ 划走未移植 | 二期 04 卷勘定：**tdix=按单元平均工资快照页**，与按人计算的 tdcao 是两个功能（勿混）；含明细弹窗 |
| —（近似对应待核） | tddag 险金计算器（派遣工资角色，PKG_费用结算.PRC_险金计算器） | **calc 页**（PKG_人才派遣公共.PRC_计算险金→TC01） | 过程不同故 wxyj parity 按"新独有"口径成立；功能形态有近似对应页 tddag（角色界面清单·派遣工资），过程差异待核 |
| tdbt 花名册核对 | PRC_工资花名册前台 | ❌ 未移植（过程）/salary 查询部分承接 | 过程读路径为主（战役 21 卷 200 实测）；核对标记 save 未接 |
| tdga 所得税参数（AA92/AA93） | td/g 域但工资菜单 | ❌ 未接 | AA92 hbm String PK 已修(战役 26)；AA93 实体插入 PK 冲突=需 JVM 观测（注意：05 卷"花名册内部 INSERT AA93"系勘误张冠李戴） |
| tdgb 社保参数（AA98 缴费基数上下限） | td/g 域 | jishu 页上下限链 ✓ | 波次5 |

## 二、逐页按键对照（tddg+tddd ↔ salary 主区）

| 东软按键 | 页面 | 新侧 | 注记 |
|---|---|---|---|
| 查询/清空 | tddg | **搜索**（合同三级检索：身份证/姓名/合同号） | |
| 导入核对/核对 | tddg（TC93_BP1 批次） | 一键编制→核对 | 旧=导入 TC93_BP1 批次再核对；新=/wb/salary-setup 直接从生效合同编制当月行（省一步，语义同键同过程 PKG_IMP.CHECK_TC93） |
| 提交 | tddg | 核对+提交合并键 | atc93p='0'；AA99 80% 门槛拒绝=忠实 |
| 结算确认 | tddd | **结算确认** | atb930+费用年月+次第整批：落账 atc93g='1'+折子扣款+TC9P 标记+体检费孤儿行 |
| 取消结算 | tddd | **取消结算** | 非孤儿行触发忠实第六例（TC31 无 atc930 列 ORA-00904 即止，旧系统同款）；孤儿行物理删=忠实 |
| — | — | 工资年月/次第/本次工资总额 表单 | tddg 工资次第信息面承接 |

## 三、后端对照

| 旧 | 新 | 测试 |
|---|---|---|
| tddcService(340L) | /import/tddc EasyExcel | 37182 批次真数据冒烟全对 |
| tddaService+tdddService(8a-lite 部分)+tddfService（共 ~274L） | TddCases 三服务合并 | 8a-lite 全实测 |
| tddd(82L)+PRC_SALARYCOMPUTE/CANCLESALARYCOMPUTE/TC93_TC9P | TddSettlementCases（/proc 8 过程） | e2e §9 四断言 |
| PRC_计算险金（险金核心） | InsuranceCalcEngine（calc 页+五险全链共用） | **黄金对照 140/140**（Tc01 管线）+e2e |
| PRC_计算平均工资/按单位计算平均工资/工资花名册前台 | **未移植**（划走） | 战役 18/19/21 卷=移植时的行为规格（3088 实测值/除零幻影/读路径 200） |
| PRC_UPDATE_TC93_AC10 | 空壳（300 行被注释，TC93 金额来自 tddc 导入）→ 不移植 | 空壳第六例结案 |

## 四、差异与阶段归属

| 类别 | 条目 | 归属 |
|---|---|---|
| 未移植（划走） | 平均工资双过程+花名册前台三过程；tdcao/tdix/tdbt 前端 | 二阶段工资收尾波（行为规格=战役卷现成：3088/除零幻影/读路径） |
| 前端未接（后端就绪） | tdda AA90 细项维护 UI；tddf 默认名册切换 UI；AA92/AA93 所得税参数页 | 二阶段前端（AA93 启用前置=JVM 观测实体插入谜团） |
| 忠实（缺陷保真） | 坏功能第三/四/六例；空壳 PRC_UPDATE_TC93_AC10 不移植；TC91/TC91_LOG/TC9P 空转逻辑保留 | 一阶段忠实，二阶段按 backlog 改造 |
| 简化 | 编制直连生效合同（旧先导 TC93_BP1 批次）——同键同过程，语义等价 | 一阶段决策在案（dsf-tip 注明） |
| 待勘 | tddb/tddv/tddx/tddy/tddab/tddae/af/ag 八子目录 | 建页/结案前置 |

## 五、测试证据

e2e_import_sys §9（结算确认/取消+忠实第六例断言）；/import/tddc 37182 批次真数据冒烟；e2e_business
（计算险金直调+TC93 链）；黄金对照 140/140（三个年月 60/60+40/40+40/40，bba004IsNull/daa002 偏差修复
在案）；战役 18/19/21 卷为未移植三过程的行为规格（含真人 3088 实测值与零覆盖除零实录）。
