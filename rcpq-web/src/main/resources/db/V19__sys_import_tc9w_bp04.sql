-- V19: 补种 tc9w_bp04(市医保异动回盘)导入线配置 —— §5.1 配置缺口旗标闭环(2026-11-26)
-- 渊源: 活库自 M4 期起有此配置但迁移种子(V9/V10)未收录, 重建库会缺线报"未配置此importId"。
-- 语义实证=战役 61 卷(9/9 端到端): data_from=1, 17 列映射(0=aab000…16=abp00c),
-- 实体 TC9W_BP04(无 ace100 列, 批次键=BAZ002/上传级 SEQ_SYS_BAZ002), parameters 五键盖章
-- (aae002/aaa030/aaa031/aaa020/aaa021); 校验/提交=PRC_市医保异动回盘校验/提交(TdcAyCases)。
-- 引擎配套: SysImportController idSeq 增 seq_tc9w_bp04_id + 批次列按表存在性门控(同提交)。
-- 幂等: 均带 NOT EXISTS 守卫(活库已存在时零影响)。

INSERT INTO sys_import (import_id, template_id, template_version, check_version, title,
                        file_suffix, sheet_index, data_type, separator, xpath, data_from,
                        entity_name, java_bean, description, template_url, proc_name,
                        min_avai_cell, template_name)
SELECT 'tc9w_bp04', 'tc9w_bp04', 1, 0, 'tc9w_bp04',
       'xls', 0, 'grid', NULL, NULL, 1,
       'com.neusoft.lemis.apps.entities.baseDB.T.TC9W_BP04', NULL, '市医保异动报盘导入', NULL, NULL,
       1, NULL
WHERE NOT EXISTS (SELECT 1 FROM sys_import WHERE import_id = 'tc9w_bp04');

INSERT INTO sys_import_mapping (import_id, data_form, data_to, data_type)
SELECT 'tc9w_bp04', f.form::text, f.col, 0
FROM (VALUES (0,'aab000'),(1,'aae003'),(2,'aac000'),(3,'aac002'),(4,'aac003'),
             (5,'abp001'),(6,'abp002'),(7,'abp003'),(8,'abp004'),(9,'abp005'),
             (10,'abp006'),(11,'abp007'),(12,'abp008'),(13,'abp009'),(14,'abp00a'),
             (15,'abp00b'),(16,'abp00c')) AS f(form, col)
WHERE NOT EXISTS (
    SELECT 1 FROM sys_import_mapping m
    WHERE m.import_id = 'tc9w_bp04' AND m.data_form = f.form::text);
