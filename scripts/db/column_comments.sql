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
