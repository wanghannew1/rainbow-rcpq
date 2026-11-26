-- V19: 补种 tc9w_bp04(市医保异动回盘)导入线配置 —— §5.1 配置缺口旗标闭环(2026-11-26)
-- 渊源: 活库自 M4 期起有此配置但迁移种子(V9/V10)未收录, 重建库会缺线报"未配置此importId"。
-- 语义实证=战役 61 卷(9/9 端到端): data_from=1, 17 列映射(0=aab000…16=abp00c),
-- 实体 TC9W_BP04(无 ace100 列, 批次键=BAZ002/上传级 SEQ_SYS_BAZ002), parameters 五键盖章
-- (aae002/aaa030/aaa031/aaa020/aaa021); 校验/提交=PRC_市医保异动回盘校验/提交(TdcAyCases)。
-- 引擎配套: SysImportController idSeq 增 seq_tc9w_bp04_id + 批次列按表存在性门控(同提交)。
-- 幂等: 均带 NOT EXISTS 守卫(活库已存在时零影响)。
-- [e13c7f3 后修正 2026-11-26] 初版映射 INSERT 漏 mapping_id(NOT NULL 无默认主键)——
--   空库重建必炸("null value in column mapping_id"), 当时全绿因活库行已在、守卫空转掩盖。
--   本版=Oracle 备份库 2026-11-26 逐字重捕(7 列含 mapping_id/description), 逐行守卫。

INSERT INTO sys_import (import_id, template_id, template_version, check_version, title,
                        file_suffix, sheet_index, data_type, separator, xpath, data_from,
                        entity_name, java_bean, description, template_url, proc_name,
                        min_avai_cell, template_name)
SELECT 'tc9w_bp04', 'tc9w_bp04', 1, 0, 'tc9w_bp04',
       'xls', 0, 'grid', NULL, NULL, 1,
       'com.neusoft.lemis.apps.entities.baseDB.T.TC9W_BP04', NULL, '市医保异动报盘导入', NULL, NULL,
       1, NULL
WHERE NOT EXISTS (SELECT 1 FROM sys_import WHERE import_id = 'tc9w_bp04');

INSERT INTO sys_import_mapping (mapping_id, import_id, mapping_type, data_form, data_to, description, data_type)
SELECT v.mid, 'tc9w_bp04', 1, v.form::text, v.col, v.descr, 0
FROM (VALUES ('tc9w_bp04_01',0,'aab000','保险单位编号'),
             ('tc9w_bp04_02',1,'aae003','对应结算日期'),
             ('tc9w_bp04_03',2,'aac000','保险个人编号'),
             ('tc9w_bp04_04',3,'aac002','公民身份证号码'),
             ('tc9w_bp04_05',4,'aac003','姓名'),
             ('tc9w_bp04_06',5,'abp001','人员类别'),
             ('tc9w_bp04_07',6,'abp002','缴费标志'),
             ('tc9w_bp04_08',7,'abp003','到账日期'),
             ('tc9w_bp04_09',8,'abp004','缴费基数'),
             ('tc9w_bp04_10',9,'abp005','缴费金额'),
             ('tc9w_bp04_11',10,'abp006','基本医疗'),
             ('tc9w_bp04_12',11,'abp007','个人缴纳大额'),
             ('tc9w_bp04_13',12,'abp008','个人缴纳医疗'),
             ('tc9w_bp04_14',13,'abp009','单位缴纳医疗'),
             ('tc9w_bp04_15',14,'abp00a','划个人帐户'),
             ('tc9w_bp04_16',15,'abp00b','单位缴纳工伤'),
             ('tc9w_bp04_17',16,'abp00c','单位缴纳生育')) AS v(mid, form, col, descr)
WHERE NOT EXISTS (
    SELECT 1 FROM sys_import_mapping m WHERE m.mapping_id = v.mid);
