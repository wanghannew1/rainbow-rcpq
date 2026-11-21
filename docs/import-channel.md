# 通用导入通道业务逻辑文档（/import/sys + 合同批量导入）

> 读者: 后续维护本系统的开发。业务操作视角请看 operations-manual.md §10。
> 成文: 2026-11-21 W7 波次(合同批量导入前端 + 引擎三缺陷修复 + e2e 26 断言)。

## 0. 一页总览

```
Excel 文件 ──POST /import/sys/{importId}──► 通用导入引擎(SysImportController)
                │  配置: sys_import(每模板一行/sheet) + sys_import_mapping(列映射)
                ▼
         BP 批次表(TC90_BP/BP2/BP3/BP4/AC01_GX/…) + CE10 批次记录(ace100)
                │  POST /proc/PKG_派遣批量导入.PRC_员工导入校验 … (逐行规则检查)
                ▼
         批次行 ace10a 状态机(见 §3) ──PRC_…提交──► 落账(AC01/TC90/TC90_LIST/TB91/QDLS)
```

- 前端入口: 工作台"合同批量导入"页(`static/js/pages/imports.js`, 东软 tdb/p 四弹窗工作台化);
  单人表单路线(hire/change 页)走 /wb/hire-batch、/wb/terminate-batch, 与本通道并列等价。
- 旧版对应: cp33 ImportUtil.doExcelImport → ria_pojo.do?method=doImport(配置驱动的通用导入引擎)。

## 1. 引擎契约(POST /import/sys/{importId})

请求: multipart `file` + 可选 `parameters`(JSON, 弹窗透传列)。

解析规则(逐条对齐旧实现):
1. 配置: `sys_import WHERE template_id=importId ORDER BY sheet_index`(现均单 sheet);
   `data_from` = 头行数(EasyExcel headRowNumber, 0 基)——**tc90_bp=8(列头在第 8 行),
   bp2/bp3/bp4=4**; `min_avai_cell`=空行判定下限, 连续空行即停(旧 isEmptyRow 语义)。
2. 列映射: `sys_import_mapping WHERE import_id=?`, `data_form`(0 基 Excel 列号)→`data_to`(目标列);
   引擎按 information_schema 目标列类型**分型绑定**(2026-11-21 修复, 见 §6); 列序无关紧要, 按 form 取格。
3. 每行生成 `id`(seq_*, 见 §7 序列表) + `ace100`(seq_ce10_ace100) + `aae036`=now;
   `parameters` 里与目标表列同名的键在映射之后覆盖(旧 BeanUtils 后写覆盖语义)。
4. 超长(中文按 2 计, 旧 getStringLength)**仍入库**, errors 收集 `"行 列 最大长 实际长"`。
5. 映射指向**表里不存在的列→跳过不落库**(仅 tc90_bp 一套 27 列, 渊源见 §5), 响应带 `skipped`。
6. 返回: `{succeed, ce10:[批次号], baz002[, errors][, skipped]}`; 失败 `{succeed:false, msg}`(msg=驱动层根因)。
7. 留痕: 成功/失败均写 wb_audit_log(`action=import:sys`, detail 含文件名/批次/行数/超长/跳过);
   模板下载写 `action=import:template`(铁律 8)。

## 2. 模板生成(GET /import/sys/{importId}/template)

旧系统下发人工维护的静态 .xls(FileDownLoadServlet), 新库 template_url 全空 →
由 sys_import_mapping **反向生成** xlsx: 第 1 行模板名(合并居中), 第 2 行填表说明,
第 data_from 行列头(加粗底纹), 之后 20 空白填写行。
**data_from=1 的模板(专项四套)第 0 行即列头行, 无标题/说明空间 → 说明落第二张 sheet"填表说明"**
(POI createRow 重复调用返回同对象——曾致列头覆写, 2026-11-21 修)。**列头加 ※ = 该列目标表无此列,
填了不落库**(仅 tc90_bp 27 列)。列顺序=映射顺序, 不可增删调换。

## 3. 批次行状态机(ace10a/ace10b)

```
NULL/0/2/3/4 ──校验──► 3=通过 | 2=拒绝(ace10b=原因, 如"该人员已经存在有效的合同")
                        4=变更类提示(变更单位状态机专用)
3(或3,4) ──提交──► 1=完成(提交内部先重跑校验, 只处理通过行)
```
查询面: `/wb/batch/{ace100}` 返回 `{rows, table}` 供前端批次预览; 标签 api.batchLabel/batchType。

## 4. 四模板业务语义(东软 tdb/p 弹窗)

| | 新签 tc90_bp | 更新 tc90_bp2 | 续签 tc90_bp3 | 新增保险 tc90_bp4 |
|---|---|---|---|---|
| 模板列 | 54(人/合同/参保) | 15(联系/岗位/服务费/工会/银行) | 9(联系/部门/新合同期) | 13(五险属地/年月/基数) |
| 头行数 | 8 | 4 | 4 | 4 |
| 匹配生效合同 | (新人生成,查重用) | 身份证+姓名+**结算单元** | 身份证+姓名 | 身份证+姓名 |
| 弹窗透传参数 | 见下"新签参数" | atb930(必须) | atb930/aab001/aab004 | 同左 |
| 校验要点 | 身份证解析/AA10 代码项/有效合同冲突/养老基数上下限(AA98)/AA95 时限/退休超龄 | 只校验填了的列 | 新合同期格式 | 险种属地(AA12)/年月/基数格式 |
| 提交产物 | AC01+TC90+TC90_LIST+TB91(20·2 待审)→审核台 | UPDATE tc90+tc90_list(只更新填的列) | TC90_QDLS 签订历史+atc90f='2'+新合同期 | 变更快照+险种落账 |

**专项五套(2026-11-21 第二批, 前端"专项登记导入"页 static/js/pages/special.js)**:

| | 生育补贴 tc04_bp | 意外险 tc05_bp | 特殊情况 tc06_bp | 个人补退 tc07_bp | 补偿金 td04_bp |
|---|---|---|---|---|---|
| 模板列 | 7(头行 5) | 9(头行 1) | 22(头行 1) | 4(头行 1) | 10(头行 1) |
| 弹窗透传 | 无 | 无 | aae002 异动年月(落行 NOT NULL+校验参数) | aae002 补退年月(落行 NOT NULL) | atb931 结算单元+atb930/aab001(落行 NOT NULL) |
| 匹配键 | 医保个人编号+费款所属期 | 结算单元名称+保单号(TB9B) | 身份证+姓名 | 身份证+姓名 | 身份证+姓名 |
| 校验要点 | 合同存在+查重 | 保单存在+已结算月拒绝 | 六险种异动+码表(ATC061/062 等带码全称)+月确认冻结 | 合同存在 | 码表(ATD04C 收取方式)+F_PARSE_MONEY 大写 |

id 序列: seq_tc04/05/06/07_id+seq_td04_id(hbm 权威, 2026-11-21 抬段, 见 seq_offset.sql)。
过程契约: 校验=ace100(+tc06 加 aae002; tc07/tc05 加 aae011), 提交=ace100+aae011。

**三银行到账(2026-11-21 第三批, money 页"银行回单 Excel 批量"区)**:
tb97_bp 工商(head=1)/tb97_bp01 建行(5列)/tb97_bp02 吉林银行(4列), id 序列 seq_tb97_bp*_id
(同日抬段)。过程契约同构: 校验=ace100, 提交=ace100+aae011; 提交落 TB97(atc900=-1 待核销哨兵)。
列位移忠实: 工行 户名=abp002/金额=abp005, 建行/吉林 户名=abp003/金额=abp002;
**建行/吉林到账日期须 8 位 yyyyMMdd**(校验 day(raw.take(8))), 工行接受文本日期或 Excel 日期
(1900 基序数)——模板说明已按 DATE_HINTS 注入该提示。

**新签弹窗参数(模板 54 列之外, 全部靠 parameters 透传, 东软弹窗字段)**:
- 单位块: aab001/aab004/atb900/atb930; 年月块: aae002+aaa203(社保参保年月,**恒填下月**——
  AA95 时限守卫: 每月 5 号后费用开始年月不得为当月)。
- 人员块: **aae00p(人员类别, 必录, 缺失即时中止"【用工性质】不能为空")**/aac005 民族/
  aac011 学历/aac012 个人身份/aac009 户口性质/aac013 招工性质/aae005 联系电话(必录)/
  atc90ax 证件有效期(必录)/atc902 户籍地址/atb90a·atb90b 服务费月标准。
  前端默认值与 /wb/hire-batch 单人路线一致。

## 5. 忠实怪癖与已知坑(后人必读)

1. **tc90_bp 映射 54 列中 27 列目标表不存在**(atc90t1-t6 照片、试用期块、atc90g/h 参保标志、
   aaa001-004 参保编号等)。渊源: Oracle 的 TC90_BP 物理表就没有这些列, 而 SYS_IMPORT_MAPPING
   与 PKG_IMP 源码引用它们 → 生产即 INVALID(坏功能第八例), 整条导入链在新系统首次可用。
   PG 实体对齐忠实保持了表结构, 引擎按 information_schema 权威跳过、响应/审计透出。
2. **数值单元格按 double 转字符串**(123→"123.0", 旧 POI 行为), 列类型分型绑定后无害。
3. **V10 种子四套模板映射行成对重复**(bp2/bp3/bp4/impnew 30/15、18/9、26/13、120/60)——
   引擎 GROUP BY data_form 去重兜底; 若将来清理种子, 引擎行为不变。
4. **数据类型绑定曾是全字符串** → PG numeric/date 列直接拒绑(Oracle 隐式转换宽容);
   2026-11-21 起 `typed()` 按列类型转 BigDecimal/Date/Timestamp, 解析失败保留原串走行级失败。
5. **tc90_imp 模板(→TC90_IMP 表)无 ID 序列配置**(旧 hbm generator 不可考), 上传报
   "未配置目标表ID序列"——未接, 需要时补 idSeq 映射。终止批量(tc90_bp1)不走本通道,
   用 change 页 /wb/terminate-batch 表单路线。
6. **序列撞号三连(2026-11-21 实证, 已修)**: bp 族六序列停在迁移数据段(首传即 pk 冲突);
   tb91 序列 1e7 段撞每日同步行(Oracle 键域已 27M, 详见 seq_offset.sql 2026-11-21 两段注释);
   tc90_qdls 同病。**凡在同步集合的表, 抬段前必查 Oracle 侧实际键域, 不能按 1e7 一刀切**。
7. 校验代码项要求"码-名称"整段(如 `1-固定期限`, 防伪), 前端码表下拉已保证; 手工 Excel 要注意。

## 6. e2e

`scripts/e2e_import_sys.py` — 模板下载×4 → 新签全链(下载的模板反向填充→上传→校验→提交→
审核通过) → 更新落账 → 续签(QDLS+atc90f) → 新增保险校验 → 审计留痕, 26 断言全绿(2026-11-21)。
断言一律按本 run 批次圈定; 费用年月恒下月; 合成人身份证池避让(同 e2e_business)。
