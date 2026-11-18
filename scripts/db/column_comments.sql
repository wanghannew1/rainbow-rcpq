-- column_comments.sql — 状态/代码字段值含义注释(2026-11-11 用户定调: 东软很多字段注释
-- 未写数值含义, 查找困难; 新系统用到并经代码验证的字段一律写入列注释)。
-- 含义来源=新系统代码(WorkbenchController/BaopanExporter/HzController/TdckCases/ api.js),
-- 每行标注验证处; 重建库后需重跑本脚本。码表类字段(值域在 aa10)注释指向码表类别。
-- 已核对存量: 以下注释前为空(东软未写)。

-- ===== tc9w 社保名册工作表 =====
COMMENT ON COLUMN tc9w.atc9w1 IS '异动类型: 1=报增(新参保, HzController.push) 3=停保/报减(TdckCases.fallbackCommit 停保处理)';
COMMENT ON COLUMN tc9w.atc9w8 IS '状态: 1=待校验(推送初值) 4=已校验(PRC_社保异动回盘校验置) 9=作废(新系统约定: 名册重推逻辑删除标记, 查询面过滤, HzController.push/roster)';
COMMENT ON COLUMN tc9w.aaa100 IS '参保属地(12位区划码, 报增查询条件之一)';

-- ===== tc9w_bp 社保回盘批次表(成果确认/申报结果落地) =====
COMMENT ON COLUMN tc9w_bp.ace10a IS '批次校验标志(通用): 3=过/有效 2=拒(原因见ace10b) 1=完成(api.js BATCH_LABEL; 回盘提交只处理 ace10a=''3'' 行, TdckCases.fallbackCommit)';
COMMENT ON COLUMN tc9w_bp.aaa90w IS '成果类型: 1=参保成功 2=参保异议(TdckCases.fallbackCommit 三路处理)';
COMMENT ON COLUMN tc9w_bp.abp001 IS '实际参保时间 yyyy-MM-dd(回盘提交按前10位ISO解析, HzController.confirm)';
COMMENT ON COLUMN tc9w_bp.ace10b IS '批次拒绝原因(ace10a=''2'' 时)';

-- ===== tc90 员工合同主表 =====
COMMENT ON COLUMN tc90.atb90r IS '合同状态: 2=待审 3=生效(api.js contractState; 报盘/名册取 atb90r=''3'')';
COMMENT ON COLUMN tc90.atc90p IS '社保报增报减标志: 0=报增/已参保 1=报减/停保 2=参保异议(.doAjax 审核模式 bz→0/bj→1; fallbackCommit 成功→0/停保→1/异议→2)';
COMMENT ON COLUMN tc90.aaa203 IS '社保实际参保年月 yyyymm(回盘提交按 TC9W_BP.abp001 落; 补缴明细条件 aaa203<当月)';
COMMENT ON COLUMN tc90.aaa208 IS '工资截止年月 yyyymm(补缴明细条件 aaa208=当月)';
COMMENT ON COLUMN tc90.aaa201 IS '社保参保年月 yyyymm(申报口径; 延续至下月 doAjax 续月 +1)';
COMMENT ON COLUMN tc90.aaa202 IS '报减延续年月 yyyymm(报减失败延续 doAjax 续月 +1)';
COMMENT ON COLUMN tc90.aaa601 IS '医保参保年月 yyyymm(医保报盘市侧本次参保时间=aaa601+01, BaopanExporter)';
COMMENT ON COLUMN tc90.aaa110 IS '医保参保属地(12位区划码; 医保报盘拆省2299/市, BaopanExporter.splitByLocation; 缺失归市-可调口径)';
COMMENT ON COLUMN tc90.aaa100 IS '社保参保属地(12位区划码; 工作地址映射见 BaopanExporter.WORK_ADDRESS)';
COMMENT ON COLUMN tc90.atc90k IS '合同终止原因码(01..16, 值域见 aa10 类别 ATC90K; 社保停保原因映射 BaopanExporter.STOP_REASON)';
COMMENT ON COLUMN tc90.aac005 IS '民族码(值域见 aa10 类别 AAC005, 如 01=汉族; 报盘走 aa10 映射)';
COMMENT ON COLUMN tc90.aac009 IS '户口性质码(值域见 aa10 类别 AAC009; 医保报盘按 BaopanExporter.HOUSEHOLD 映射为医保口径)';
COMMENT ON COLUMN tc90.aac012 IS '人员身份码: 91=干部(报盘→职员) 92=工人(报盘→企业职工), aa10 类别 AAC012';

-- ===== 批次表通用(ce10 批次体系) =====
COMMENT ON COLUMN tc90_bp.ace10a IS '批次行校验结果: 3=过 2=拒(原因 ace10b) 1=完成(api.js BATCH_LABEL; PRC_员工导入校验/提交 写)';
COMMENT ON COLUMN tc90_bp.ace10b IS '批次行拒绝原因(ace10a=''2'' 时, 校验逐行写回)';
COMMENT ON COLUMN tc93_bp1.ace10a IS '批次行校验结果: 3=过 2=拒(原因 ace10b) 1=完成(工资核对批次)';
COMMENT ON COLUMN tb97_bp.ace10a IS '批次行校验结果: 3=过 2=拒(原因 ace10b) 1=完成(银行到账批次)';
COMMENT ON COLUMN tb99_bp.ace10a IS '批次行校验结果: 3=过 2=拒(原因 ace10b) 1=完成(发票批次)';

-- ===== 工作台新增 =====
COMMENT ON TABLE wb_audit_log IS '工作台操作留痕(2026-11-11 定调: 用户操作一律留痕; 覆盖/proc、/interaction、/wb 写操作、报盘导出; AuditLog bean 只插入不更新)';

-- ===== W1 公积金链(2026-11-16, 出处=旧包源码+AA10 字典+备份库分布, 详见 docs/five-insurance-migration.md W0/W1) =====
COMMENT ON COLUMN tc9u.atc9u8 IS '公积金名单审核状态(AA10 字典): 1=暂存 2=提交审核 3=生效 4=提交前台(终态)。旧系统置4语句被注释(保险人员增减 L6555-6560)全表停1; W0#1 裁决: 新系统异动回盘提交成功后整批置4(2026-11-16 实测生产回盘链活跃)。9=新系统作废标记(逻辑删除, 旧库无此义)';
COMMENT ON COLUMN tc9u.atc9u1 IS '公积金业务类型: 1=报增(开户) 2=启封后转出标记(异议回盘提交置2, 旧源码 L6470-6472)';
COMMENT ON COLUMN tc9u.atc9uy IS '公积金业务类型镜像位: 异议回盘提交启封分支与 atc9u1 同置 2(旧源码 L6471)';
COMMENT ON COLUMN tc9u.atc9u2 IS '公积金业务状态(生成义): 1=当月新增(与 aa10 字典申报结果义分叉, 同 TC9W 家族 atc9w2)';
COMMENT ON COLUMN tc9u.abp00a IS '单位缴存比例(整数百分数, 拆自 tc90.atb90s 冒号前段)';
COMMENT ON COLUMN tc9u.abp00b IS '个人缴存比例(整数百分数, 拆自 tc90.atb90s 冒号后段)';
COMMENT ON COLUMN tc9u_bp02.abp002 IS '公积金封存回盘状态: 封存=按封存行处理(提交回写 TC90.AAA004), 其他值跳过; 异议校验启封分支按本表 aaa001 反查封存信息';
COMMENT ON COLUMN tc9u_bp02.ace10a IS '封存桶行状态: 1=新开户已落账(封存提交写入); 旧系统校验结果回写被注释, 清洗后为空';
COMMENT ON COLUMN tc9u_bp03.abp002 IS '公积金异议回盘匹配键=身份证号(清洗 x→X/去空白)';
COMMENT ON COLUMN tc9u_bp03.abp003 IS '公积金异议回盘内容: 含"100066835"且含"状态封存"→启封, 否则→异议(校验判定写 abp004)';
COMMENT ON COLUMN tc9u_bp03.abp004 IS '公积金异议回盘判定: 启封/异议(异议回盘校验写入; 提交按此分流)';
COMMENT ON COLUMN tc9u_bp03.ace10a IS '异议桶行校验结果: 3=过 2=拒(原因 ace10b), 提交只消费 3';
COMMENT ON COLUMN tc9w_bp02.abp002 IS '公积金异动回盘缴存状态: 正常=校验/提交消费(匹配+编号+比例校验→TC90.ATC90W 置0), 其他值仅置 ace10a=3 不参与提交';
COMMENT ON COLUMN tc9w_bp02.aaa90z IS '公积金异动回盘差异码: 10=申报基数与合同 aaa303 不符(差异值记 aaa303_1 旧/aaa303_2 新, 非失败)';
COMMENT ON COLUMN tc9w_bp02.ace10a IS '公积金异动回盘桶行状态: 3=校验过(待提交) 2=校验拒(ace10b 原因) 1=提交完成';

-- ===== W2 省医保链(2026-11-17, 出处=旧包源码 L2840-3790+AA10 字典+备份库分布) =====
COMMENT ON COLUMN tc9w2.atc9w8 IS '省医保名单审核状态(AA10): 1=暂存 2=提交审核 3=审核通过 4=结果提交前台(终态)。省医保异动回盘**校验**即整批置4(旧源码 L2929 未注释, 与公积金 W0#1 相反; 库内 4=4696 实证)。9=新系统作废标记(重推逻辑删除, 旧库无此义)';
COMMENT ON COLUMN tc9w2.atc9w2 IS '省医保业务状态(生成义): 1=合同开始当月 2=非当月(生成过程按 atc90c 写, 与 AA10 字典"申报结果0未申报/1已申报"分叉, W0#2 裁决按生成义忠实; 库内无 0/NULL 印证)';
COMMENT ON COLUMN tc9w2.aaa90y IS '省医保申报结果(生成默认): 90=默认失败(生成置, 回盘成功由 TC9W_BP01 消费后改写 TC90)';
COMMENT ON COLUMN tc9w_bp01.aaa90w IS '省医保回盘申报结果: 1=参保成功 2=异议(校验验值, 提交分流)';
COMMENT ON COLUMN tc9w_bp01.ace10a IS '省医保回盘桶行状态: 3=校验过(待提交) 2=校验拒(ace10b 原因)。注意: 提交完成后不翻 ace10a(与社保/公积金不同), 提交完成标记=aae236 盖章';
COMMENT ON COLUMN tc9w_bp06.aaa005 IS '省医保基数补差桶匹配键=医保个人编号(tc90.aaa005, 非证件号, 校验/提交均按此匹配)';
COMMENT ON COLUMN tc9w_bp06.ace10a IS '补差桶行状态: 3=校验过 2=拒(原因 ace10b) 1=已落 TJ0B2 台账; 批次号=ace100(CE10 导入批次)';
COMMENT ON TABLE tc9w_bp07 IS '省医保缴费工资表(工资表过程重建: 物理删旧批+调险金引擎算五险扣款, 守卫=当月 TC9W2 已回盘则拒绝)';

-- ===== W3a 省工伤链(2026-11-17, 出处=旧包源码 L3372-9161/L7470-8104+AA10 字典) =====
COMMENT ON COLUMN tc9w3.atc9w8 IS '省工伤名单审核状态(AA10): 1=暂存 4=结果提交前台(终态)。省工伤异动回盘校验置4(旧源码 L7865 未注释=活, 范围=户头+属地+年月; 库内 3×4=11815/3×1=1970 实证)。9=新系统作废标记(重推逻辑删除, 旧库无此义)';
COMMENT ON COLUMN tc9w3.atc9w2 IS '省工伤业务状态(生成义): 1=合同开始当月 2=非当月(与 AA10 申报结果义分叉, 同 tc9w2.atc9w2)';
COMMENT ON COLUMN tc9w_bp09.ace10a IS '省工伤回盘桶行状态: 3=校验过 2=校验拒 1=提交完成(省工伤提交翻1, 与省医保不翻不同); 提交完成另见 aae236 盖章';
COMMENT ON COLUMN tc9w_bp09.aaa90w IS '省工伤回盘申报结果: 1=参保成功 2=异议(AA10)';
COMMENT ON COLUMN tc09_bp.ace10a IS '省工伤减免桶行状态: 3=校验过 2=拒(ace10b 原因) 1=已落 TC09; 匹配键=AAA005(经 tc90.aaa009 医保编号, 旧复制粘贴系忠实)';
COMMENT ON COLUMN tc09_bp.bba004 IS '工伤减免额度(零值/非数值校验拒)';
COMMENT ON COLUMN tc09_bp.atc9eh IS '缴费合计(零值/非数值校验拒)';
COMMENT ON COLUMN tc9w_bp08.aae003 IS '费款所属期: yyyymm 或 yyyymm-yyyymm 区间; 校验单月=查前一月工资/提交单月=当月(旧系统自体差异忠实)';
COMMENT ON COLUMN tc9w_bp08.ace10a IS '省工伤补差桶行状态: 3=校验过(缺月也置3, 信息入 ace10b, 忠实) 2=拒 1=已落 TJ0B3';
COMMENT ON COLUMN tc09.atc091 IS '结算方式: 1=(减免提交固定值)';

-- ===== W3b 市工伤链(2026-11-17, 出处=旧包源码 L8105-8897) =====
COMMENT ON COLUMN tc9w4.atc9w8 IS '市工伤名单审核状态(AA10): 1=暂存 4=提交前台(终态)。市工伤异动回盘校验置4(旧源码 L8529 未注释=活, 范围=户头+年月无属地)。9=新系统作废标记(重推逻辑删除, 旧库无此义)';
COMMENT ON COLUMN tc9w_bp10.ace10a IS '市工伤回盘桶行状态: 3=校验过 2=校验拒(提交前置拒全月) 1=提交完成; 桶无申报结果列(aaa90w), 未回盘报增行由提交的 TC90 扫描置 ATC90PB=2';
COMMENT ON COLUMN tc9w_bp10.aaa90y IS '市工伤回盘差异码串: 11=基数不符 12=灵活就业 14=大额 15=达龄退休 16=退出灵活就业 17=失业代缴(11 时 AAA305 按 abp004 覆写)';
COMMENT ON COLUMN tc9w_bp10.aac000 IS '市工伤回盘文件个人编号(提交回写 tc90.aaa009; 与 aaa001 列不同源的旧命名)';
COMMENT ON COLUMN tc9w_bp10.aae001 IS '回盘月: ATC90PB NULL→业务年月(本月新增), 否则→医保参保年月 aaa603(校验写入)';

-- ===== W4a 月情况/月转款(2026-11-17, 出处=PKG_派遣保险月情况 L3-84/L85-313/L4019-4184) =====
COMMENT ON COLUMN tj0a.aaezzz IS '月转款确认标识: 0=生成未确认(重建写入) 1=已确认(月转款确认置, 经办入 aae111/117/119/120/122); 已确认月重建静默跳过(忠实)';
COMMENT ON COLUMN tj0a1.aae013 IS '月转款确认联动位: 确认时明细整批置 1(与主表 aaezzz 同步)';
COMMENT ON COLUMN tj0a1.atj0aa IS '当月新增标识: 合同 aaa208(工资截止年月)=业务年月 → 1, 否则 0(转款情况写入)';
COMMENT ON COLUMN tj0a.atj0a2 IS '缴费人数: 本单元 atj0a7>0 的员工计数(重建时累加)';
COMMENT ON COLUMN tj0a1.atj0a5 IS '养老滞纳金合计(源 tc9w_bp.abp002 ::numeric, 回盘文件写入)';

-- ===== W4c-1 医保双线月情况(2026-11-17, 出处=PKG_派遣保险月情况 L314-635/L1126-1551) =====
COMMENT ON COLUMN tj0b.aae001 IS '省医保下期应缴年月(重建置=业务年月+1, ADD_MONTHS 语义; numeric)';
COMMENT ON COLUMN tj0c.aae001 IS '市医保下期应缴年月(重建置=业务年月+1; numeric)';
COMMENT ON COLUMN tj0b1.daa001 IS '省医保个人大病: 引擎 daa001+daa002; AAA608=业务年月时取 TC9W_BP01.ABO003(>0); AAA603<>业务年月归零(忠实)';
COMMENT ON COLUMN tj0c1.daa001 IS '市医保个人大病: TC9W_BP04 sum(abp007)(户头+年月+姓名+证件, rownum=1); hack 220822199710080425+202509→100 忠实';
COMMENT ON COLUMN tj0b1.atj0b3 IS '省医保单位缴费合计=BBA002(含公务员补助比例额)+ATB90T+BBA005';
COMMENT ON COLUMN tj0c1.atj0c3 IS '市医保单位缴费合计=BBA002+BBA004+BBA005';

-- ===== W4d 市医保应缴费(2026-11-17, 出处=PKG_派遣保险月情况 L1552-1700) =====
COMMENT ON COLUMN tj0c2.atj0aa IS '新参保当月标识: 合同 AAE002=业务年月 且 ATC90PA NULL → 1; 此时查 TC9W1.ABP016, 值 1/2 跳过该行(忠实)';
COMMENT ON COLUMN tj0c2.aaa603 IS '参保时间(合同 AAA603 原值, 明细展示列)';

-- ===== W4d-2 保险当月补缴(2026-11-17, 出处=PKG_保险补退 L84-1010) =====
COMMENT ON COLUMN ac10.aac101 IS 'AC10 险种归类: 1=社保(养老失业) 2=医保 3=公积金 4=工伤(保险当月补缴 LB=2 滞纳桶写入; DUP→该月金额清零继续)';
COMMENT ON COLUMN ac10.atc9t4 IS '补缴审核状态固定置 1(保险当月补缴 LB=2 写入)';

-- ===== W4d-2② 补缴情况×4(2026-11-17, 出处=PKG_派遣保险月情况 L636-810/3047-3211/2469-2623/1941-2107) =====
COMMENT ON COLUMN tj0b1.atj0b6 IS '省医保补缴滞纳金: TC9W_BP01.ABO004(单行 SELECT INTO; 多行/无行→DAA001 亦 NULL)';
COMMENT ON COLUMN tj0f1.atj0f6 IS '工伤补缴滞纳金: TC9W_BP09.ABO004(单行 SELECT INTO; 多行/无行→DAA001 亦 NULL)';
COMMENT ON COLUMN tj0d1.atj000 IS '数据来源: 1=月重建 2=补缴情况(省公积金活码重写版走 TC9U_BP01 回盘桶 abp005/006)';
COMMENT ON COLUMN tj0b1.atj000 IS '数据来源: 1=月重建 2=补缴情况';
COMMENT ON COLUMN tj0f1.atj000 IS '数据来源: 1=月重建 2=补缴情况';

-- ===== W4d-3 月垫付族(2026-11-17, 出处=PKG_派遣保险月情况 L4882-5999, tdce:53) =====
COMMENT ON COLUMN tj0z.ttype IS '月转款类别: 11=社保 21=省医保 22=市医保 31=省工伤 32=市工伤 41=省公积金 42=市公积金(工伤按结算单元区划 AAA020 第3-4位=99 分流省/市; 编排器硬序 11→21/22→31/32→41/42 清欠级联)';
COMMENT ON COLUMN tj0z.dfsign IS '垫付标识: 0=无需垫付 1=垫付(V_TB96 判定: 无结算记录或 ATB96Z 1/2 欠费→全额垫付; ATB96Z=0 已到账且 BX_ZJ-前序险种额度<应收→差额垫付; 注意 dfmoney 公式只减 BX_ZJ 不减前序额度, 旧系统忠实)');
COMMENT ON COLUMN tj0z.dfmoney IS '垫付金额: 全额垫付=转款合计; 差额垫付=GREATEST(应收-BX_ZJ,0); 省医保 MNUM_SUM=缴费+补缴 无 NVL, 可为 NULL(dfmoney 随之 NULL)';
COMMENT ON COLUMN tj0z.aaezzz IS '确认标志: 0=未确认(月垫付重建置 0; 旧已确认跳过分支整块被注释, 现行重建直接先删后插)';
COMMENT ON COLUMN tj0z.dfmsg IS '未到账原因(月垫付重建置 NULL; 业务员后续填写)';
COMMENT ON COLUMN tj0z.dftime IS '预计到账时间(月垫付重建置 NULL; 业务员后续填写)';

-- ===== W5a 缴费基数核对/确认(2026-11-17, 出处=PKG_缴费基数 L636-1755, tdcq/tdcab/tdcac/tdch 页) =====
COMMENT ON COLUMN aa97.aae214_1 IS '社保基数核对状态: 0=未核对(无合同/资格不符被退回) 3=已核对待确认 1=已确认(同时回写 TC90 atc905/aaa301/aaa304←AAA301_1 按同人 MAX(ATC900))';
COMMENT ON COLUMN aa97.aae214_2 IS '医保基数核对状态: 0=未核对 3=已核对待确认 1=已确认(回写 TC90 aaa302/aaa306←AAA302_1 按自身 ATC900); 核对含 INSTR(aac008,80) 退休旁路, ATC90PA CASE 80+市→NULL/80+省→0';
COMMENT ON COLUMN aa97.aae214_3 IS '公积金基数核对状态: 0=未核对或现基数=申报 3=已核对待确认(现基数<>申报) 1=已确认(回写 TC90 aaa303←AAA303_1 按同人 MAX(ATC900)); 核对唯一活跃调基数上下限且夹取值写回 AAA303_1, ABP00C/D/E=ROUND(申报×比例/100)省市均整数化HALF_UP';
COMMENT ON COLUMN aa97.aae214_4 IS '工伤基数核对状态: 0=未核对 3=已核对待确认 1=已确认(回写 TC90 aaa305←AAA305_1 按自身 ATC900); 主路径 ATC90PB=3 项被注释, 另有 ELSIF PB=3 分支: 同年同人 AA97 唯一才过且调医保上下限仅报错不落值';
COMMENT ON COLUMN aa97.aaa301_1 IS '社保单位申报缴费基数(Excel 导入; 确认时落 TC90; 已确认线 AAE214_1=1 后导入不覆盖)';
COMMENT ON COLUMN aa97.aaa302_1 IS '医保单位申报缴费基数(省/市按 AAA110 第3-4位择一, 导入提交时选定; 确认落 TC90 aaa302/aaa306)';
COMMENT ON COLUMN aa97.aaa303_1 IS '公积金调整后缴费基数(省/市择一; 核对时被上下限夹取后写回; 确认落 TC90 aaa303)';
COMMENT ON COLUMN aa97.aaa305_1 IS '工伤单位申报缴费基数(省/市择一; 确认落 TC90 aaa305)';
COMMENT ON COLUMN aa97.abp00d IS '单位公积金月缴存额=ROUND(AAA303_1×ATB90S_DW/100) 整数化 HALF_UP(核对生成; PG varchar 存 numeric)';
COMMENT ON COLUMN aa97.abp00c IS '个人公积金月缴存额=ROUND(AAA303_1×ATB90S_GR/100) 整数化 HALF_UP(核对生成; PG varchar 存 numeric)';
COMMENT ON COLUMN aa97.abp00e IS '公积金月缴存额合计=ABP00C+ABP00D(核对生成; PG varchar 存 numeric)';

-- ===== W5b 基数导入校验/提交(2026-11-18, 出处=PKG_缴费基数 L5-635, tdcl(29) 页 导入对话框) =====
COMMENT ON COLUMN aa97_bp.ace10a IS '基数导入批次行状态: NULL=新导入未校验 3=校验通过 2=校验失败(原因见ace10b) 1=已提交(AA97已落库) 0=提交失败(原因见ace10b); 校验只消费NULL行(复检需重导, Oracle真实分布 NULL/0/1/2/3=11580/2596/69585/32107/30510), 提交消费1/3行(PKG_缴费基数.PRC_基数导入校验/提交)';
COMMENT ON COLUMN aa97_bp.ace10b IS '基数导入批次行失败原因(校验/提交逐行写回: 未查询到劳动合同/险金户头/缴费基数不能为空为零/必须为整数/比例填写不正确 等)';
COMMENT ON COLUMN aa97.aae100 IS '生效标记: 0=未生效(导入提交初值, 核对/确认族不写此列)';

-- ===== W5c 公积金上下限五过程(2026-11-18, 出处=PKG_缴费基数 L1756-2577, tdcc(20)/tdcd(21) 页) =====
COMMENT ON COLUMN tc0a.aae114 IS '公积金上下限调整审核标识: 0=未审(提交/下限托底初值) 1=审核通过(同时 TC90.AAA303←AAA303_1+AA97.AAA303_1←同(按 AAE001=SUBSTR(AAE002,1,4))+AA97_BG 台账 TYPE=4) 2=审核不通过(AAE113=原因; 已审态回滚 TC90/AA97 到 AAA303 原值并删 BG); FLAG=2/3 已审回滚段对 AA97 无 AAE001 条件=源差异';
COMMENT ON COLUMN tc0a_bp.ace10a IS '公积金上下限调整批次行状态: NULL/0/2/3=校验消费集(NULL新导入 3=过 2=拒原因ace10b) 1=已提交(TC0A已落库); 校验只有省限出窗检查(市不查=源缺陷), 提交前内嵌重跑校验(PKG_缴费基数.PRC_公积金基数上下限调整校验/提交)';
COMMENT ON COLUMN aa97_bg.aa97type IS '基数变更台账类别: 2=TC9W_BP04 补缴链(TdcAyCases) 4=TC0A 公积金上下限调整(审核 FLAG=1 插入, FLAG=2/3 已审回滚时物理删除=源忠实, 铁律8 改造清单在册)';
