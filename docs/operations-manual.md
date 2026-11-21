# 派遣业务操作指导书（新人版 · API 端口操作）

> 读者: 新业务员/新程序员。服务地址 `http://localhost:8080`（下文省略主机）。
> 所有操作已在新系统端到端验证（20/20，见 docs/business-process.md 第五节与 scripts/e2e_business.py）。
>
> **三类入口，先记住**：
> | 入口 | 路径形态 | 什么时候用 |
> |---|---|---|
> | 过程网关 | `POST /proc/{包名.过程名}` | 一切 PL/SQL 移植的业务动作（校验/提交/计算） |
> | 交互网关 | `POST /interaction/{REQ-编号}` | 页面行编辑保存（审核通过/变更/保存工资行） |
> | Excel 通道 | `POST /import/tddc`、`POST /import/sys/{importId}` | 单元给的工资 Excel、通用批量导入 |
>
> 通用约定：请求体 JSON；`appCode=1` 成功、`-1` 失败且 `errMsg` 给中文原因；中文过程名在
> HTTP 库里需 URL 编码（curl 浏览器地址栏可直接用）。查询/审计类只读操作不会改数据。

## 0. 开工前确认

```bash
curl -s localhost:8080/proc | python3 -m json.tool | head -5        # 已注册过程清单(48 个)
curl -s localhost:8080/interaction/registry | head -c 300           # 可用交互清单
```

身份：所有"提交"类过程需要经办人参数 `aae011`(工号)。测试库可用 `jiaq`(贾悫)。
经办人五件套（部分提交过程要全量）：aae011=工号, aae017=机构代码, aae019=姓名, aae020=机构名, aae022=行政区划。

## 1. 派遣人员入职（新签合同）——最高频业务

### 1.1 单人录入式（走过程网关，两步）

**第一步：准备批次行**（把合同要素写进导入批次表 TC90_BP，一行一份合同）。
字段含义见 docs/business-process.md §二.1；必填：aac002 身份证(18 位, 校验位须合法)、
aac003 姓名、aae00p 用工性质('1-派遣')、atb930 结算单元、atb900 派遣协议、aab001 单位、
aae002 费用开始年月(≥当月)、atc90a/c/d 合同起止、aaa401/aaa100/aaa203/aaa301 养保参保信息。

```bash
# psql 直插(业务系统前端表单等价于此)
psql -h localhost -U rcpq -d rainbow_rcpq -c "INSERT INTO tc90_bp (id, ace100, aac002, aac003,
  aac005, aac012, aae00p, aac009, atc90a, atc90c, atc90d, atc905, aae002, aaa401, aaa100,
  aaa203, aaa301, atb930, atb900, aab001, aab004)
  VALUES (nextval('seq_tc90_id'), nextval('seq_ce10_ace100'), '身份证', '姓名', '01-汉族',
  '91-其他人员', '1-派遣', '10-城镇失业', '1-固定期限', '2026-11-01', '2028-11-01', '6800',
  '202611', '1-是', '220100000000-吉林省长春市', '202611', '6800', 37312, 58332, 151, '单位名')"
```

**第二步：校验 + 提交**（注意 ace100 用上一步插入的批次号）：

```bash
curl -X POST localhost:8080/proc/PKG_派遣批量导入.PRC_员工导入校验 \
  -H 'Content-Type: application/json' -d '{"ace100":"批次号","aae011":"jiaq"}'
# 成功: {"appCode":1,"errMsg":"校验完成，共导入【1】条…符合…不符合…"}
# 失败行: 批次表 ace10a='2', ace10b=原因(如"该人员已经存在有效的合同")
curl -X POST localhost:8080/proc/PKG_派遣批量导入.PRC_员工导入提交 \
  -H 'Content-Type: application/json' -d '{"ace100":"批次号","aae011":"jiaq"}'
# 成功后自动生成: AC01(个人信息)+TC90(合同, atb90r='2' 待审)+TB91(审核流水 20/2)+TC90_LIST(镜像)+派遣编号 ZYNUM
```

状态机（背下来）：批次行 ace10a：空/0/2/3/4 参与校验 → `3`=通过 `2`=拒绝(ace10b 原因) `4`=变更提示；
提交只处理 `3`（含 4）→ 成功置 `1`。合同 atb90r：`2` 待审 → `3` 生效 → 终止流程另转。

### 1.2 Excel 批量式（单位给你一份 Excel）

```bash
curl -X POST localhost:8080/import/sys/tc90_impnew -F "file=@员工合同.xlsx" \
  -F 'parameters={"atb930":37312,"atb900":58332,"aab001":151,"aab004":"单位名"}'
# 返回 {"succeed":true,"ce10":[批次号],"baz002":…}; 之后同 1.1 第二步校验+提交
```

Excel 模板列序由 `sys_import_mapping` 配置决定（data_form=列序 → data_to=表列），与旧系统模板一致。

## 2. 审核岗：新签合同生效（双人复核制）

**模型（重要）**：这是 UniEAP"行编辑保存"——不是发指令，而是**把改好状态的整行提交**。

```bash
# 1) 查出待审行(改状态前)
psql … -tAc "SELECT json_build_object('atc900',atc900,'aac001',aac001,'atb90r','3',
  'bae011','jiaq','bae017','4101','bae019','姓名','bae020','机构','bae022','区划')
  FROM tc90 WHERE atc900=合同号"
psql … -tAc "SELECT json_build_object('atb910',atb910,'atb91z',atb91z,'atb911',atb911,'atb912','3',
  'bae036', to_char(now(),'YYYY-MM-DD\"T\"HH24:MI:SS'))
  FROM tb91 WHERE atb91z=合同号 AND atb911='20' ORDER BY atb910 DESC LIMIT 1"
# 2) 整行提交(注意 atb90r 已改 '3', atb912 已改 '3'; bae036 必须 ISO 格式带 T)
curl -X POST localhost:8080/interaction/REQ-tdbf__dialog_xinqcl-commit \
  -H 'Content-Type: application/json' \
  -d '{"tc90":{…上面第一段…},"tb91":{…上面第二段…}}'
# 验证: tc90.atb90r='3'(生效), tb91.atb912='3'(已审)
```

退回用 `REQ-tdbf__dialog_xinqcl-cancle`（删 TC90+TB91，单位重报）。撤销/终止类还有
`terminateCancel / reCommit / doRenewCancle / doJsdyCancle`（同样整行模型，详见 /interaction/registry）。

## 3. 五险一金报增（名册→报盘→回盘→确认）——工作台"社保 / 医保报盘"+"社保报增回盘"页

> 2026-11-11 起 M1/M2/M3 已建: 名册由合同推导（替代生产即 INVALID 的报增名册导入），
> 回盘校验/提交复用已移植过程。操作留痕（wb_audit_log）/逻辑删除全站生效。
> 工作台每个按键悬停可看东软对应位置（td/h/y 名册、td/c/k 回盘）。

### 3.1 报盘查询与导出（页面"社保 / 医保报盘"，东软 tdhy 名册）

```bash
# 报盘查询(增=当月合同开始且生效 atb90r='3'; 减=当月合同终止)
curl 'localhost:8080/wb/roster?kind=social&flow=add&ym=202611[&aab001=单位]'   # social|medical × add|cut
# 报盘导出: 社保→参保登记/停保 .xlsx; 医保→按医保参保属地(aaa110)拆省(2299)/市打包 .zip
curl -o 社保增员_202611.xlsx 'localhost:8080/wb/roster-export?kind=social&flow=add&ym=202611&aae011=经办'
```

### 3.2 社保报增回盘（页面"社保报增回盘"，东软 tdck 成果确认/回盘）

```bash
# 1) 生成名册并推送 TC9W(户头从 ab01 按单位取; 重推旧行置 atc9w8='9' 作废, 可追溯)
curl -X POST localhost:8080/wb/hz/push -H 'Content-Type: application/json' \
  -d '{"ym":"202611","aae011":"jiaq"}'
# 2) 报增查询(工作表) / 户头下拉
curl 'localhost:8080/wb/hz/roster?ym=202611[&aaa030=户头][&aaa020=区划]'; curl 'localhost:8080/wb/hz/heads?ym=202611'
# 3) 成果确认: 报增成功(实际参保时间+局端个人编号→TC9W_BP aaa90w='1') / 报增失败(异议='2'+原因)
curl -X POST localhost:8080/wb/hz/confirm -H 'Content-Type: application/json' \
  -d '{"atc9w0":行号,"ok":true,"abp001":"2026-11-03","aaa001":"局端编号"}'
# 4) 成功校验 → 回盘提交(按 TC9W_BP 三路更新 TC90: 成功置 aaa203/atc90p='0', 异议转参保异议, 停保置 atc90p='1')
curl -X POST localhost:8080/proc/PKG_保险人员增减.PRC_社保异动回盘校验 \
  -H 'Content-Type: application/json' -d '{"aae002":"202611","aaa030":"1","aaa020":"220100000000"}'
curl -X POST localhost:8080/proc/PKG_保险人员增减.PRC_社保异动回盘提交 \
  -H 'Content-Type: application/json' -d '{"aae002":"202611","aaa030":"1","aaa020":"220100000000","aae011":"jiaq"}'
# 5) 延续至下月(勾选行; tc90.aaa201+1月, TC9W 更新沿用旧系统报错行为) / 审核通过(atc90p='0')
curl -X POST localhost:8080/wb/hz/next -H 'Content-Type: application/json' -d '{"atc900":[合同号]}'
# 6) 补缴明细: PRC_社保补缴明细 重建 TC9W_BP05 → GET /wb/hz/bj 查询
# 页面另有: /wb/hz/bp 回盘批次查询; 局方回盘文件导入待 M5(见 five-insurance-migration.md)
```

月确认状态检查（办特殊登记/异动前）：

```bash
curl -X POST localhost:8080/proc/PKG_派遣批量导入.PRC_特殊情况导入校验 \
  -H 'Content-Type: application/json' -d '{"ace100":"批次","aae002":"202611"}'
# 若该月社保/医保/公积金已确认 → appCode=-1 "XX增减变化已经确认，无法再办理"
```

## 4. 每月工资（工资岗）

### 4.1 编制当月工资行（TC93）
页面等价：`/interaction/REQ-tddd_dialog_mod_doSave`（行编辑，body={"tc93":{…整行…}}）。
⚠ 忠实坏功能：保存后调用 PRC_SALARYCOMPUTESINGLE 在备份库不存在 → HTTP 500 + "TC93 已保存"
提示（旧系统同样报错），TC93 行已落库。生成批量行用 SQL 模板（参照 scripts/e2e_business.py 步骤 5）。

### 4.2 单位工资 Excel 导入（回写 TC93 金额列）

```bash
curl -X POST localhost:8080/import/tddc -F "file=@工资明细.xlsx" \
  -F "title=流水号(勿动),姓名,身份证,…,基本工资,项1,项2,…" -F "atb930=结算单元"
# 0=成功 1=sheet 错 2=表头不符(整批回滚); 12 类金额列按 AA90 工资项自动累加
```

### 4.3 工资核对导入（批次 TC93_BP1）+ 校验 + 提交

```bash
# 批次行: aac002/aac003/atc93aa 本次工资总额/atc93k 年终奖/atc93m 经济补偿金
curl -X POST localhost:8080/proc/PKG_IMP.PRC_CHECK_TC93 -H 'Content-Type: application/json' \
  -d '{"ace100":"批次","atb930":"37312","atc931":"202611","atc932":"202611","atc937":"1"}'
# 比对: 三金额 vs 系统值 + 年终奖年度一次发放检查; 通过置 ace10a='3' 并回填 atc930
curl -X POST localhost:8080/proc/PKG_IMP.PRC_COMMIT_TC93 -H 'Content-Type: application/json' \
  -d '{"ace100":"批次","atc932":"202611","atc931":"202611","aae011":"jiaq","aae017":"4101",
       "aae019":"姓名","aae020":"机构","aae022":"区划"}'
# 最低工资 80% 门槛: atc937='1' 且系统免税额 atc93l=0 且导入额 < AA99.aaa992*0.8 → 行拒
# 成功: tc93.atc93p='0'(确认) + 批次行 ace10a='1'
```

## 5. 险金计算（测算任意合同的当月五险一金）

```bash
curl -X POST localhost:8080/proc/PKG_人才派遣公共.PRC_计算险金 \
  -H 'Content-Type: application/json' -d '{"atc900":合同号,"aae002":"202611"}'
# 返回全部险金字段: aaa301-306 基数 / bba001-bba005,baa001-003 单位个人缴 / atc9eh 合计 等
# 数据可信度: 与 Oracle 原过程黄金对照 202610/202611/202606 三月 140/140 全对
```

## 6. 合同变更：续签 / 终止

### 6.1 续签（批次表 TC90_BP3，Excel 一行一份）
```bash
# 批次行: aac002/aac003/atb930/aab001/aab004/atc90a 新合同类别/atc90c 新开始/atc90d 新终止
curl -X POST localhost:8080/proc/PKG_派遣批量导入.PRC_员工续签校验 \
  -H 'Content-Type: application/json' -d '{"ace100":"批次","aae011":"jiaq"}'
curl -X POST localhost:8080/proc/PKG_派遣批量导入.PRC_员工续签提交 \
  -H 'Content-Type: application/json' -d '{"ace100":"批次","aae011":"jiaq"}'
# 效果: 旧合同快照进 TC90_QDLS(签订历史), tc90.atc90f='2'(续签), 合同期/部门/岗位/员工号更新
```

### 6.2 终止（批次表 TC90_BP1）
```bash
# 批次行: atc90i 终止日期 / aae003 费用结束年月 / atc90k 终止原因('09-职工个人原因') /
#         atc90l 失业金 / atc90n 经济赔偿 / atc90av 工资结束年月 / aaa202/502/602/902 四险报减年月
curl -X POST localhost:8080/proc/PKG_派遣批量导入.PRC_员工终止导入校验 \
  -H 'Content-Type: application/json' -d '{"ace100":"批次","aae011":"jiaq"}'
curl -X POST localhost:8080/proc/PKG_派遣批量导入.PRC_员工终止导入提交 \
  -H 'Content-Type: application/json' -d '{"ace100":"批次","aae011":"jiaq"}'
# 效果: 有效合同(atb90r='3') → atb90r='2'+TB91(类型 23 待审)+终止编号 JYNUM;
#       审核岗过审后终止生效; 已终审(atb90r='5')合同直接落报减年月
```

## 7. 收款与开票（出纳/开票岗）

```bash
# 银行到账(批次表 tb97_bp: abp001 日期/abp002 户名/abp005 金额; 建行=BP01 列序不同, 吉林银行=BP02)
curl -X POST localhost:8080/proc/PKG_派遣批量导入.PRC_工行到账导入校验 \
  -H 'Content-Type: application/json' -d '{"ace100":"批次"}'
curl -X POST localhost:8080/proc/PKG_派遣批量导入.PRC_工行到账导入提交 \
  -H 'Content-Type: application/json' -d '{"ace100":"批次","aae011":"jiaq"}'
# 发票(批次表 tb99_bp: abp001 抬头/abp002 借或到账/abp003 专普票/abp004-011 八类费项目金额)
curl -X POST localhost:8080/proc/PKG_派遣批量导入.PRC_发票导入校验 \
  -H 'Content-Type: application/json' -d '{"ace100":"批次"}'
curl -X POST localhost:8080/proc/PKG_派遣批量导入.PRC_发票导入提交 \
  -H 'Content-Type: application/json' -d '{"ace100":"批次","aae011":"jiaq"}'
```

## 8. 专项业务（同构：批次表 → 校验 → 提交；工作台入口见 §10.5）

| 业务 | 批次表 | 校验过程 | 提交过程 | 备注 |
|---|---|---|---|---|
| 特殊情况登记 | tc06_bp | PRC_特殊情况导入校验(另需 aae002 异动年月) | PRC_特殊情况导入提交 | 月确认后冻结 |
| 生育补贴 | tc04_bp | PRC_生育补贴校验 | PRC_生育补贴提交 | 医保编号匹配, aae002='0'=当月 |
| 保险补贴 | tc07_bp | PRC_保险补贴导入校验 | PRC_保险补贴导入提交 | 已有手补行→覆盖 |
| 意外险 | tc05_bp | PRC_意外险导入校验 | PRC_意外险导入提交 | 需 TB9B 保单号 |
| 补偿金 | td04_bp | PRC_补偿金导入校验 | PRC_补偿金导入提交 | 自动生成中文大写金额 |

## 9. 查询与排错

```bash
curl -s localhost:8080/proc                        # 48 个可用过程
curl -s localhost:8080/interaction/registry        # 可用交互(泛查询 REQ-* 也在这)
curl -X POST localhost:8080/interaction/{REQ编号} -d '{…store 行…}'   # 泛查询/保存
```

**排错速查**：
- `appCode=-1 errMsg=过程未实现或未注册` → 该过程属于生产死包(PKG_IMP 的 TC9W/AA97/TC90 对)，见坏功能第八例。
- `errMsg=公民身份证号码不合法` → 检查校验位/出生日期(不得为未来)。
- `errMsg=该人员已经存在有效的合同` → 先办终止再重签，或改用更新导入。
- `errMsg=XX增减变化已经确认` → 该月社保/医保/公积金已回盘确认，业务顺延下月。
- HTTP 500 + "SALARYCOMPUTESINGLE" → 忠实坏功能(旧系统同样报错)，TC93 行已保存，可继续核对流程。
- 到账/发票批次行 ace10a 不变 → 先跑校验(提交内部也会重校验)，失败原因在 ace10b。
- 数据"少行"疑问 → 与备份库对照前先 `sort -n`（差集陷阱见第八例记录）。

**三银行到账 Excel 批量**（工作台"到账 / 发票"页"银行回单 Excel 批量"区）：工行/建行/吉林银行
各一模板(工行 5 列、建行 5 列含摘要、吉林 4 列)；**建行/吉林到账日期填 8 位 `20261101`**(东软同规则)，
工行可填文本日期；校验全过后提交落 TB97(atc900=-1 待核销)，再走到账核销认领。

**端到端参考实现**：scripts/e2e_business.py = 本手册 §1→§2→§5→§4.1→§4.3→§7→§6 的完整可执行串联，
新业务员建议先读它再动手。

## 10. 合同批量导入（工作台"合同批量导入"页 · 东软 tdb/p 四弹窗）

> 适用: 单位批量送来 Excel 的四人场景——新签入职 / 在职信息更新 / 合同续签 / 新增险种。
> 单人零星办理用"新签导入"页(表单)与"续签 / 终止"页; 本页是批量 Excel 面。

### 10.1 四步上手（每个页签同构）

1. **选单位**：页顶 单位 → 派遣协议 → 结算单元 三级下拉(唯一时自动选中)。结算单元会透传进批次行,
   更新导入按【身份证+姓名+结算单元】匹配合同, 选错匹配不上。
2. **下载模板**：每页签各自模板(新签 54 列/更新 15 列/续签 9 列/新增保险 13 列)。
   列头带 ※ 的列系统暂不接收(可留空)。**列顺序不可增删调换**; 日期填 `2026-11-01` 式文本;
   代码列填带码全称(如 `1-固定期限`, 只填"固定期限"会校验不过)。
3. **填表 → 选择 Excel → 上传导入**：上传即落批次(返回批次号 ace100), 超长列会提示但已入库。
4. **校验 → 提交**：校验逐行给出通过/拒绝(原因见批次明细"原因"列, 改正后重新上传);
   全过后点提交。新签提交后转**新签审核台**复核(双人复核制), 其余三路直接落账。

### 10.2 新签页签的"弹窗参数"（模板 54 列之外）

人员类别/民族/户口性质/联系电话/身份证有效期/服务费月标准等不在模板列内, 由页面"② 新签弹窗参数"
区透传。**人员类别(默认 1-派遣)、联系电话、身份证有效期三项必填**, 缺任一校验不过;
社保参保年月自动填下月(每月 5 号后当月不可新办, 旧系统同规则)。费用开始年月同此。

### 10.3 排错速查

| 现象 | 原因与处理 |
|---|---|
| 上传失败"未配置此importId" | 模板号写错(sys_import 无此行) |
| 校验"【用工性质】不能为空" | 页顶漏选人员类别(aae00p) |
| 校验"【身份证有效期】/【联系电话】不能为空" | "② 新签弹窗参数"区漏填 |
| 校验"根据【身份证号码+姓名】未匹配到合同信息" | 更新/续签: 人未签或合同未生效(先走新签+审核) |
| 校验"该人员已经存在有效的合同" | 新签: 先办终止再重签, 或改用更新页签 |
| 校验"当【合同类型】选择【1-固定期限】…" | 固定期限合同必须填终止日期 |
| 上传后批次明细为空 | Excel 数据起始行不对(新签列头在第 8 行/其余第 4 行), 或首行即空行 |
| 提交按钮灰 | 先校验; 有拒绝行先修正重新上传 |
| 导出的模板打开列头错位 | 下载后勿用 WPS 精简模式改列序; 以模板列头为准 |

### 10.4 留痕与追溯

上传/校验/提交/模板下载全部写入 wb_audit_log(含文件名/批次号/行数/超长清单),
纠纷追溯按批次号 ace100 查 CE10 与批次表即可。
端到端可执行参照: `scripts/e2e_import_sys.py`(26 断言), 业务逻辑细节见 docs/import-channel.md。

### 10.5 专项登记导入（工作台"专项登记导入"页 · 东软专项五弹窗）

五路: 生育补贴 / 意外险 / 特殊情况 / 个人补退 / 经济补偿金。每页签四步同 §10.1(下载模板→填表→
上传→校验→提交), 但**无需先选单位**(匹配键在 Excel 列内), 仅三处额外参数:

| 页签 | 额外参数 | 校验要点 |
|---|---|---|
| 特殊情况 | 异动年月(必填, 默认当月) | 异动原因/情况填带码全称(如 `07-社保个人原因增减`、`2-增员`); 当月已回盘确认则冻结 |
| 个人补退 | 补退年月(必填, 默认当月) | 按身份证+姓名匹配生效合同 |
| 补偿金 | 结算单元(必选, 下拉) | 收取方式填带码全称(`1-通过工资表收取`/`0-不通过工资表收取`); 合计自动生成中文大写 |
| 生育补贴 | 无 | 医保个人编号+费款所属期匹配合同 |
| 意外险 | 无 | 结算单元名称+保单号匹配, 已结算月份拒绝 |

校验出现"未匹配到合同/保单"属正常业务拒绝——先确认人员合同生效、保单(TB9B)已登记。
