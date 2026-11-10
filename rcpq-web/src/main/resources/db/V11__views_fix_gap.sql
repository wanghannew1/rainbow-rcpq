-- V11: 挂起视图收尾(pkg_common 垫片 + decode→CASE + (+)→LEFT JOIN + CONNECT BY→递归 CTE)
CREATE OR REPLACE VIEW v_ab01 AS WITH RECURSIVE ab01_tree AS ( SELECT aab001, aab004, aab00a, aab00e, id, 0 AS lv FROM ab01 WHERE id = '000000000000000000000000000000000000000000000' UNION ALL SELECT c.aab001, c.aab004, c.aab00a, c.aab00e, c.id, p.lv + 1 FROM ab01 c JOIN ab01_tree p ON c.parentid = p.id) SELECT aab001, aab004, aab00a, aab00e, id, lv FROM ab01_tree;
CREATE OR REPLACE VIEW v_ac10 AS SELECT   ATC900,
       ATB930,
       AAE001,
       SUM(CASE WHEN (ATC9EN) IS NOT DISTINCT FROM ('3') THEN (CASE WHEN (aac104) IS NOT DISTINCT FROM ('1') THEN (coalesce(BBA001,0)) ELSE (0) END) ELSE (0) END) BBA001,
       SUM(CASE WHEN (ATC9EN) IS NOT DISTINCT FROM ('3') THEN (CASE WHEN (aac104) IS NOT DISTINCT FROM ('1') THEN (coalesce(BBA002,0)) ELSE (0) END) ELSE (0) END) BBA002,
       SUM(CASE WHEN (ATC9EN) IS NOT DISTINCT FROM ('3') THEN (CASE WHEN (aac104) IS NOT DISTINCT FROM ('1') THEN (coalesce(BBA003,0)) ELSE (0) END) ELSE (0) END) BBA003,
       SUM(CASE WHEN (ATC9EN) IS NOT DISTINCT FROM ('3') THEN (CASE WHEN (aac104) IS NOT DISTINCT FROM ('1') THEN (coalesce(BBA004,0)) ELSE (0) END) ELSE (0) END) BBA004,
       SUM(CASE WHEN (ATC9EN) IS NOT DISTINCT FROM ('3') THEN (CASE WHEN (aac104) IS NOT DISTINCT FROM ('1') THEN (coalesce(BBA005,0)) ELSE (0) END) ELSE (0) END) BBA005,
       SUM(CASE WHEN (ATC9EN) IS NOT DISTINCT FROM ('3') THEN (CASE WHEN (aac104) IS NOT DISTINCT FROM ('1') THEN (coalesce(BBA000,0)) ELSE (0) END) ELSE (0) END) BBA000,
       SUM(CASE WHEN (ATC9EN) IS NOT DISTINCT FROM ('3') THEN (CASE WHEN (aac104) IS NOT DISTINCT FROM ('1') THEN (coalesce(BAA001,0)) ELSE (0) END) ELSE (0) END) BAA001,
       SUM(CASE WHEN (ATC9EN) IS NOT DISTINCT FROM ('3') THEN (CASE WHEN (aac104) IS NOT DISTINCT FROM ('1') THEN (coalesce(BAA002,0)) ELSE (0) END) ELSE (0) END) BAA002,
       SUM(CASE WHEN (ATC9EN) IS NOT DISTINCT FROM ('3') THEN (CASE WHEN (aac104) IS NOT DISTINCT FROM ('1') THEN (coalesce(BAA003,0)) ELSE (0) END) ELSE (0) END) BAA003,
       SUM(CASE WHEN (ATC9EN) IS NOT DISTINCT FROM ('3') THEN (CASE WHEN (aac104) IS NOT DISTINCT FROM ('1') THEN (coalesce(BAA000,0)) ELSE (0) END) ELSE (0) END) BAA000,
       SUM(CASE WHEN (ATC9EN) IS NOT DISTINCT FROM ('3') THEN (CASE WHEN (aac105) IS NOT DISTINCT FROM ('1') THEN (coalesce(CAA001,0)) ELSE (0) END) ELSE (0) END) CAA001,
       SUM(CASE WHEN (ATC9EN) IS NOT DISTINCT FROM ('3') THEN (CASE WHEN (aac105) IS NOT DISTINCT FROM ('1') THEN (coalesce(CAA002,0)) ELSE (0) END) ELSE (0) END) CAA002,
       SUM(CASE WHEN (ATC9EN) IS NOT DISTINCT FROM ('3') THEN (CASE WHEN (aac104) IS NOT DISTINCT FROM ('1') THEN (coalesce(ATB90T,0)) ELSE (0) END) ELSE (0) END) ATB90T,
       SUM(CASE WHEN (ATC9EN) IS NOT DISTINCT FROM ('3') THEN (CASE WHEN (aac104) IS NOT DISTINCT FROM ('1') THEN (coalesce(BBA000,0)) ELSE (0) END+CASE WHEN (aac105) IS NOT DISTINCT FROM ('1') THEN (coalesce(caa001,0)) ELSE (0) END) ELSE (0) END) BBA200, --单位险金合计
       SUM(CASE WHEN (ATC9EN) IS NOT DISTINCT FROM ('3') THEN (CASE WHEN (aac104) IS NOT DISTINCT FROM ('1') THEN (coalesce(BAA000,0)) ELSE (0) END+CASE WHEN (aac105) IS NOT DISTINCT FROM ('1') THEN (coalesce(caa002,0)) ELSE (0) END) ELSE (0) END) BAA200, --个人险金合计
       SUM(CASE WHEN (ATC9EN) IS NOT DISTINCT FROM ('3') THEN (CASE WHEN (aac104) IS NOT DISTINCT FROM ('1') THEN (coalesce(DAA001,0)) ELSE (0) END) ELSE (0) END) DAA001, --个人大病
       SUM(CASE WHEN (ATC9EN) IS NOT DISTINCT FROM ('3') THEN (CASE WHEN (aac104) IS NOT DISTINCT FROM ('1') THEN (coalesce(ATC93AF,0)) ELSE (0) END) ELSE (0) END) ATC93AF, --困难补助
       SUM(CASE
             WHEN ATC9EN <> '3' THEN
              CASE WHEN (aac104) IS NOT DISTINCT FROM ('1') THEN (BBA000) ELSE (0) END
             ELSE
              0
           END) BBA100, --当月单位社保补缴合计
       SUM(CASE
             WHEN ATC9EN <> '3' THEN
              CASE WHEN (aac104) IS NOT DISTINCT FROM ('1') THEN (BAA000) ELSE (0) END
             ELSE
              0
           END) BAA100, --当月个人社保补缴合计
       SUM(CASE
             WHEN ATC9EN <> '3' THEN
              CASE WHEN (aac105) IS NOT DISTINCT FROM ('1') THEN (CAA001) ELSE (0) END
             ELSE
              0
           END) CAA101, --当月单位公积金补缴
       SUM(CASE
             WHEN ATC9EN <> '3' THEN
              CASE WHEN (aac105) IS NOT DISTINCT FROM ('1') THEN (CAA002) ELSE (0) END
             ELSE
              0
           END) CAA102, --当月个人公积金补缴
       SUM(CASE
             WHEN ATC9EN <> '3' THEN
              CASE WHEN (aac104) IS NOT DISTINCT FROM ('1') THEN (BBA000) ELSE (0) END+CASE WHEN (aac105) IS NOT DISTINCT FROM ('1') THEN (CAA001) ELSE (0) END
             ELSE
              0
           END) BBA300, --当月单位险金补缴合计
       SUM(CASE
             WHEN ATC9EN <> '3' THEN
              CASE WHEN (aac104) IS NOT DISTINCT FROM ('1') THEN (BAA000) ELSE (0) END+CASE WHEN (aac105) IS NOT DISTINCT FROM ('1') THEN (CAA002) ELSE (0) END
             ELSE
              0
           END) BAA300, --当月个人险金补缴合计
       SUM(CASE
             WHEN ATC9EN <> '3' THEN
              CASE WHEN (aac104) IS NOT DISTINCT FROM ('1') THEN (BAA001) ELSE (0) END
             ELSE
              0
           END) BAA101,--个人养老补缴
       SUM(CASE
             WHEN ATC9EN <> '3' THEN
              CASE WHEN (aac104) IS NOT DISTINCT FROM ('1') THEN (BAA002) ELSE (0) END
             ELSE
              0
           END) BAA102,--个人医疗补缴
       SUM(CASE
             WHEN ATC9EN <> '3' THEN
              CASE WHEN (aac104) IS NOT DISTINCT FROM ('1') THEN (BAA003) ELSE (0) END
             ELSE
              0
           END) BAA103--个人失业补缴

  FROM AC10
 WHERE AAE001 > 201512 and ATC9T4 = '3'
 GROUP BY ATC900, ATB930, AAE001

 

;
CREATE OR REPLACE VIEW v_ac10_zcxj AS SELECT ATC900,
       ATB930,
       AAE001,
       SUM(CASE WHEN (ATC9EN) IS NOT DISTINCT FROM ('3') THEN (CASE WHEN (aac104) IS NOT DISTINCT FROM ('1') THEN (coalesce(BBA001,0)) ELSE (0) END) ELSE (0) END) BBA001,
       SUM(CASE WHEN (ATC9EN) IS NOT DISTINCT FROM ('3') THEN (CASE WHEN (aac104) IS NOT DISTINCT FROM ('1') THEN (coalesce(BBA002,0)) ELSE (0) END) ELSE (0) END) BBA002,
       SUM(CASE WHEN (ATC9EN) IS NOT DISTINCT FROM ('3') THEN (CASE WHEN (aac104) IS NOT DISTINCT FROM ('1') THEN (coalesce(BBA003,0)) ELSE (0) END) ELSE (0) END) BBA003,
       SUM(CASE WHEN (ATC9EN) IS NOT DISTINCT FROM ('3') THEN (CASE WHEN (aac104) IS NOT DISTINCT FROM ('1') THEN (coalesce(BBA004,0)) ELSE (0) END) ELSE (0) END) BBA004,
       SUM(CASE WHEN (ATC9EN) IS NOT DISTINCT FROM ('3') THEN (CASE WHEN (aac104) IS NOT DISTINCT FROM ('1') THEN (coalesce(BBA005,0)) ELSE (0) END) ELSE (0) END) BBA005,
       SUM(CASE WHEN (ATC9EN) IS NOT DISTINCT FROM ('3') THEN (CASE WHEN (aac104) IS NOT DISTINCT FROM ('1') THEN (coalesce(BBA000,0)) ELSE (0) END) ELSE (0) END) BBA000,
       SUM(CASE WHEN (ATC9EN) IS NOT DISTINCT FROM ('3') THEN (CASE WHEN (aac104) IS NOT DISTINCT FROM ('1') THEN (coalesce(BAA001,0)) ELSE (0) END) ELSE (0) END) BAA001,
       SUM(CASE WHEN (ATC9EN) IS NOT DISTINCT FROM ('3') THEN (CASE WHEN (aac104) IS NOT DISTINCT FROM ('1') THEN (coalesce(BAA002,0)) ELSE (0) END) ELSE (0) END) BAA002,
       SUM(CASE WHEN (ATC9EN) IS NOT DISTINCT FROM ('3') THEN (CASE WHEN (aac104) IS NOT DISTINCT FROM ('1') THEN (coalesce(BAA003,0)) ELSE (0) END) ELSE (0) END) BAA003,
       SUM(CASE WHEN (ATC9EN) IS NOT DISTINCT FROM ('3') THEN (CASE WHEN (aac104) IS NOT DISTINCT FROM ('1') THEN (coalesce(BAA000,0)) ELSE (0) END) ELSE (0) END) BAA000,
       SUM(CASE WHEN (ATC9EN) IS NOT DISTINCT FROM ('3') THEN (CASE WHEN (aac105) IS NOT DISTINCT FROM ('1') THEN (coalesce(CAA001,0)) ELSE (0) END) ELSE (0) END) CAA001,
       SUM(CASE WHEN (ATC9EN) IS NOT DISTINCT FROM ('3') THEN (CASE WHEN (aac105) IS NOT DISTINCT FROM ('1') THEN (coalesce(CAA002,0)) ELSE (0) END) ELSE (0) END) CAA002,
       SUM(CASE WHEN (ATC9EN) IS NOT DISTINCT FROM ('3') THEN (CASE WHEN (aac104) IS NOT DISTINCT FROM ('1') THEN (coalesce(BBA000,0)) ELSE (0) END+CASE WHEN (aac105) IS NOT DISTINCT FROM ('1') THEN (coalesce(caa001,0)) ELSE (0) END) ELSE (0) END) BBA200, --单位险金合计
       SUM(CASE WHEN (ATC9EN) IS NOT DISTINCT FROM ('3') THEN (CASE WHEN (aac104) IS NOT DISTINCT FROM ('1') THEN (coalesce(BAA000,0)) ELSE (0) END+CASE WHEN (aac105) IS NOT DISTINCT FROM ('1') THEN (coalesce(caa002,0)) ELSE (0) END) ELSE (0) END) BAA200, --个人险金合计
       SUM(CASE WHEN (ATC9EN) IS NOT DISTINCT FROM ('3') THEN (CASE WHEN (aac104) IS NOT DISTINCT FROM ('1') THEN (coalesce(DAA001,0)) ELSE (0) END) ELSE (0) END) DAA001, --个人大病
       SUM(CASE WHEN (ATC9EN) IS NOT DISTINCT FROM ('3') THEN (CASE WHEN (aac104) IS NOT DISTINCT FROM ('1') THEN (coalesce(ATC93AF,0)) ELSE (0) END) ELSE (0) END) ATC93AF, --困难补助
       SUM(CASE
             WHEN ATC9EN <> '3' THEN
              CASE WHEN (aac104) IS NOT DISTINCT FROM ('1') THEN (BBA000) ELSE (0) END
             ELSE
              0
           END) BBA100, --当月单位社保补缴合计
       SUM(CASE
             WHEN ATC9EN <> '3' THEN
              CASE WHEN (aac104) IS NOT DISTINCT FROM ('1') THEN (BAA000) ELSE (0) END
             ELSE
              0
           END) BAA100, --当月个人社保补缴合计
       SUM(CASE
             WHEN ATC9EN <> '3' THEN
              CASE WHEN (aac105) IS NOT DISTINCT FROM ('1') THEN (CAA001) ELSE (0) END
             ELSE
              0
           END) CAA101, --当月单位公积金补缴
       SUM(CASE
             WHEN ATC9EN <> '3' THEN
              CASE WHEN (aac105) IS NOT DISTINCT FROM ('1') THEN (CAA002) ELSE (0) END
             ELSE
              0
           END) CAA102, --当月个人公积金补缴
       SUM(CASE
             WHEN ATC9EN <> '3' THEN
              CASE WHEN (aac104) IS NOT DISTINCT FROM ('1') THEN (BBA000) ELSE (0) END+CASE WHEN (aac105) IS NOT DISTINCT FROM ('1') THEN (CAA001) ELSE (0) END
             ELSE
              0
           END) BBA300, --当月单位险金补缴合计
       SUM(CASE
             WHEN ATC9EN <> '3' THEN
              CASE WHEN (aac104) IS NOT DISTINCT FROM ('1') THEN (BAA000) ELSE (0) END+CASE WHEN (aac105) IS NOT DISTINCT FROM ('1') THEN (CAA002) ELSE (0) END
             ELSE
              0
           END) BAA300, --当月个人险金补缴合计
       SUM(CASE
             WHEN ATC9EN <> '3' THEN
              CASE WHEN (aac104) IS NOT DISTINCT FROM ('1') THEN (BAA001) ELSE (0) END
             ELSE
              0
           END) BAA101,--个人养老补缴
       SUM(CASE
             WHEN ATC9EN <> '3' THEN
              CASE WHEN (aac104) IS NOT DISTINCT FROM ('1') THEN (BAA002) ELSE (0) END
             ELSE
              0
           END) BAA102,--个人医疗补缴
       SUM(CASE
             WHEN ATC9EN <> '3' THEN
              CASE WHEN (aac104) IS NOT DISTINCT FROM ('1') THEN (BAA003) ELSE (0) END
             ELSE
              0
           END) BAA103--个人失业补缴

  FROM AC10
 WHERE AAE001 >= 201812 and ATC9T4 = '3'
 GROUP BY ATC900, ATB930, AAE001

 

;
CREATE OR REPLACE VIEW v_tb93_ab07 AS SELECT A.BAZ001,
       A.BAZ002,
       A.ATB930,
       A.ATB900,
       A.AAB001,
       A.ATB931,
       A.BBB002,
       A.ATB932,
       A.ATB933,
       A.ATB934,

       A.ATB935,
       A.AAB998,
       A.ATB936,
       A.ATB937,
       A.ATB938,
       A.AAE006,
       A.ATB90AC,
       A.ATB939,
       A.ATB90Y,
       A.ATB90AA,
       A.ATB90AB,
       A.ATB90C,
       A.ATB90A,
       A.ATB90B,
       A.ATB90Z,
       A.ATB90AD,
       A.AAE013,
       A.AAE011,
       A.AAE017,
       A.AAE036,
       A.AAE019,
       A.AAE020,
       A.AAE022,
       A.ATB900_1,
       B.AAC003,
       B.AAB073,
       B.AAB075,
       C.AAB00A,
       C.ATB904,
       (select aab00e from ab01 where aab001=A.Aab001) as AAB00E
  FROM tb90 c JOIN tb93 a ON a.atb900 = c.atb900 LEFT JOIN (SELECT * FROM ab07 WHERE aab078 = '1') b ON a.atb931 = b.aab004

 

;
CREATE OR REPLACE VIEW v_tb93_tc93 AS SELECT TC93.ATC930, --工资编号
       TC93.ATB930, --单位派遣协议编号
       TC93.AAB004, --派往单位名称
       TC93.ATC931, --工资所属年月
       TC93.ATC932, --社保所属年月
       TC93.ATC937, --工资发放次数
       TC93.ATB931, --结算单元名称
       TC93.AAC002, --身份证号码
       TC93.AAC003, --姓名
       (select bbb001 from ac01 where aac001=TC93.Aac001) AS BBB001, --工资帐号
       TC93.BBB002, --发放银行
       TC93.ATB90A, --管理费
       TC93.ATB90Z, --残疾人保障金(元人)
       TC93.ATC93X, --本次基本工资
       TC93.ATC933, --本次应发工资
       TC93.ATC934, --本次扣款
       TC93.ATC935, --本次补发
       TC93.ATC936, --本次免税
       TC93.ATC93K, --本次年终奖
       TC93.ATC93L, --本次事假
       TC93.ATC93M, --本次经济补偿金
       TC93.ATC93Y, --本次病假
       TC93.ATC93AA, --本次工资总额
       TC93.ATC93AB, --本次应税金额
       TC93.ATC93D, --本次个人所得税
       TC93.ATC93AC, --本次个人所得税(年终奖)
       TC93.ATC93AD, --本次个人所得税(工资)
       TC93.ATC93C, --本次实发金额
       TC93.ATC93G, --是否结算
       TC93.ATC93N, --是否存在上月工资
       TC93.ATC93P, --是否需审核
       TC93.ATC93S, --单位垫付险金
       TC93.ATC93T, --是否欠费
       TC93.ATC93U, --工资是否可发情况
       TC93.ATC93V, --费用状态
       TC93.ATC93W, --结算信息来源
       TC93.AAE114, --审核标记
       TC93.AAE111, --审核人代码
       TC93.AAE117, --审核机构代码
       TC93.AAE136, --审核日期
       TC93.AAE119, --审核人
       TC93.AAE120, --审核机构
       TC93.AAE122, --审核机构行政区划
       TC93.AAE214, --确认标记
       TC93.AAE211, --确认人代码
       TC93.AAE217, --确认机构代码
       TC93.AAE236, --确认日期
       TC93.AAE219, --确认人
       TC93.AAE220, --确认机构
       TC93.AAE222, --确认机构行政区划
       TC93.AAE314, --过月标记
       TC93.AAE311, --过月人代码
       TC93.AAE317, --过月机构代码
       TC93.AAE336, --过月日期
       TC93.AAE319, --过月人
       TC93.AAE320, --过月机构
       TC93.AAE322, --过月机构行政区划
       TC93.ATC910, --流水号
       TC93.AAE011, -- 经办人代码
       TC93.AAE017, --经办机构代码
       TC93.AAE036, --经办日期
       TC93.AAE019, --经办人
       TC93.AAE020, --经办机构
       TC93.AAE013, --备注
       TC93.aae002,--费用清单年月
       TC93.aae001,
       tc93.atc900,
       cast(coalesce(AC10.BAA000, 0) AS numeric(12,2)) BAA000,
       cast(coalesce(AC10.BAA001, 0) AS numeric(12,2)) BAA001,
       cast(coalesce(AC10.BAA002, 0) AS numeric(12,2))  BAA002,
       cast(coalesce(AC10.BAA003, 0)AS numeric(12,2)) BAA003,
       cast(coalesce(AC10.BBA001, 0) AS numeric(12,2))BBA001,
       cast(coalesce(AC10.BBA002, 0)AS numeric(12,2)) BBA002,
       cast(coalesce(AC10.BBA003, 0) AS numeric(12,2))BBA003,
       cast(coalesce(AC10.BBA004, 0) AS numeric(12,2))BBA004,
       cast(coalesce(AC10.BBA005, 0) AS numeric(12,2))BBA005,
       cast(coalesce(AC10.BBA000, 0) AS numeric(12,2))BBA000,
       cast(coalesce(AC10.CAA001, 0) AS numeric(12,2))CAA001,
       cast(coalesce(AC10.CAA002, 0) AS numeric(12,2))CAA002,
       cast(coalesce(AC10.BBA200, 0) AS numeric(12,2))BBA200,
       cast(coalesce(AC10.BAA200, 0) AS numeric(12,2))BAA200,
       cast(coalesce(AC10.DAA001, 0) AS numeric(12,2))DAA001,
       cast(coalesce(AC10.ATC93AF, 0) AS numeric(12,2))ATC93AF,
       cast(coalesce(AC10.BBA100, 0) AS numeric(12,2))BBA100,
       cast(coalesce(AC10.BAA100, 0) AS numeric(12,2))BAA100,
       cast(coalesce(AC10.CAA101, 0) AS numeric(12,2))CAA101,
       cast(coalesce(AC10.CAA102, 0) AS numeric(12,2))CAA102,
       cast(coalesce(AC10.BBA300, 0) AS numeric(12,2))BBA300,
       cast(coalesce(AC10.BAA300, 0) AS numeric(12,2))BAA300,
       cast(coalesce(AC10.BAA101, 0) AS numeric(12,2))BAA101,
       cast(coalesce(AC10.BAA102, 0) AS numeric(12,2))BAA102,
       cast(coalesce(AC10.BAA103, 0) AS numeric(12,2))BAA103,
       cast(coalesce(TC93.ATC9P2, 0) AS numeric(12,2))ATC9P2,
       cast(coalesce(TC93.ATC9P3, 0) AS numeric(12,2))ATC9P3,
       (SELECT AAE005 FROM tc90 WHERE atc900 = tc93.atc900) AAE005,--联系电话
       (SELECT AAE00B FROM tc90 WHERE atc900 = tc93.atc900) AAE00B,--员工号
       (SELECT ATB90Y FROM tb93 WHERE atb930 = tc93.atb930) ATB90Y,--是否代发工资
       (SELECT count(1) FROM tc8g where atc930=TC93.atc930 and atc8g4='1') atc8g0,--网银是否发放成功
       (SELECT aae00a FROM TC90 WHERE ATC900=TC93.ATC900) AAE00A,--职工号
       (SELECT atc904 FROM TC90 WHERE ATC900=TC93.ATC900) ATC904,--职工号
       TC93.ATC93AG,
       to_char(coalesce((SELECT ATC301 FROM  TC30 WHERE AAE001=TC93.AAE002 AND AAC002=TC93.AAC002),0)) ATC301,
       to_char(coalesce((SELECT ATC302 FROM  TC30 WHERE AAE001=TC93.AAE002 AND AAC002=TC93.AAC002),0)) ATC302,
to_char(coalesce((SELECT ATC303 FROM  TC30 WHERE AAE001=TC93.AAE002 AND AAC002=TC93.AAC002),0)) ATC303,
to_char(coalesce((SELECT ATC304 FROM  TC30 WHERE AAE001=TC93.AAE002 AND AAC002=TC93.AAC002),0)) ATC304,
to_char(coalesce((SELECT ATC305 FROM  TC30 WHERE AAE001=TC93.AAE002 AND AAC002=TC93.AAC002),0)) ATC305,
to_char(coalesce((SELECT ATC306 FROM  TC30 WHERE AAE001=TC93.AAE002 AND AAC002=TC93.AAC002),0))  ATC306

  FROM TC93
  LEFT JOIN V_AC10 AC10
    ON (TC93.ATC900 = AC10.ATC900 AND TC93.ATC937 = '1' AND
       TC93.AAE001 = AC10.AAE001 AND TC93.ATB930 = AC10.ATB930 ) where tc93.aae002>'201612' order by atc904,atc930

 

;
CREATE OR REPLACE VIEW v_tb93_tc93_zcxj AS SELECT TC93.ATC930, --工资编号
       TC93.ATB930, --单位派遣协议编号
       TC93.AAB004, --派往单位名称
       TC93.ATC931, --工资所属年月
       TC93.ATC932, --社保所属年月
       TC93.ATC937, --工资发放次数
       TC93.ATB931, --结算单元名称
       TC93.AAC002, --身份证号码
       TC93.AAC003, --姓名
       (select bbb001 from ac01 where aac001=TC93.Aac001) AS BBB001, --工资帐号
       TC93.BBB002, --发放银行
       TC93.ATB90A, --管理费
       TC93.ATB90Z, --残疾人保障金(元人)
       TC93.ATC93X, --本次基本工资
       TC93.ATC933, --本次应发工资
       TC93.ATC934, --本次扣款
       TC93.ATC935, --本次补发
       TC93.ATC936, --本次免税
       TC93.ATC93K, --本次年终奖
       TC93.ATC93L, --本次事假
       TC93.ATC93M, --本次经济补偿金
       TC93.ATC93Y, --本次病假
       TC93.ATC93AA, --本次工资总额
       TC93.ATC93AB, --本次应税金额
       TC93.ATC93D, --本次个人所得税
       TC93.ATC93AC, --本次个人所得税(年终奖)
       TC93.ATC93AD, --本次个人所得税(工资)
       TC93.ATC93C, --本次实发金额
       TC93.ATC93G, --是否结算
       TC93.ATC93N, --是否存在上月工资
       TC93.ATC93P, --是否需审核
       TC93.ATC93S, --单位垫付险金
       TC93.ATC93T, --是否欠费
       TC93.ATC93U, --工资是否可发情况
       TC93.ATC93V, --费用状态
       TC93.ATC93W, --结算信息来源
       TC93.AAE114, --审核标记
       TC93.AAE111, --审核人代码
       TC93.AAE117, --审核机构代码
       TC93.AAE136, --审核日期
       TC93.AAE119, --审核人
       TC93.AAE120, --审核机构
       TC93.AAE122, --审核机构行政区划
       TC93.AAE214, --确认标记
       TC93.AAE211, --确认人代码
       TC93.AAE217, --确认机构代码
       TC93.AAE236, --确认日期
       TC93.AAE219, --确认人
       TC93.AAE220, --确认机构
       TC93.AAE222, --确认机构行政区划
       TC93.AAE314, --过月标记
       TC93.AAE311, --过月人代码
       TC93.AAE317, --过月机构代码
       TC93.AAE336, --过月日期
       TC93.AAE319, --过月人
       TC93.AAE320, --过月机构
       TC93.AAE322, --过月机构行政区划
       TC93.ATC910, --流水号
       TC93.AAE011, -- 经办人代码
       TC93.AAE017, --经办机构代码
       TC93.AAE036, --经办日期
       TC93.AAE019, --经办人
       TC93.AAE020, --经办机构
       TC93.AAE013, --备注
       TC93.aae002,--费用清单年月
       TC93.aae001,
       tc93.atc900,
       cast(coalesce(AC10.BAA000, 0) AS numeric(12,2)) BAA000,
       cast(coalesce(AC10.BAA001, 0) AS numeric(12,2)) BAA001,
       cast(coalesce(AC10.BAA002, 0) AS numeric(12,2))  BAA002,
       cast(coalesce(AC10.BAA003, 0)AS numeric(12,2)) BAA003,
       cast(coalesce(AC10.BBA001, 0) AS numeric(12,2))BBA001,
       cast(coalesce(AC10.BBA002, 0)AS numeric(12,2)) BBA002,
       cast(coalesce(AC10.BBA003, 0) AS numeric(12,2))BBA003,
       cast(coalesce(AC10.BBA004, 0) AS numeric(12,2))BBA004,
       cast(coalesce(AC10.BBA005, 0) AS numeric(12,2))BBA005,
       cast(coalesce(AC10.BBA000, 0) AS numeric(12,2))BBA000,
       cast(coalesce(AC10.CAA001, 0) AS numeric(12,2))CAA001,
       cast(coalesce(AC10.CAA002, 0) AS numeric(12,2))CAA002,
       cast(coalesce(AC10.BBA200, 0) AS numeric(12,2))BBA200,
       cast(coalesce(AC10.BAA200, 0) AS numeric(12,2))BAA200,
       cast(coalesce(AC10.DAA001, 0) AS numeric(12,2))DAA001,
       cast(coalesce(AC10.ATC93AF, 0) AS numeric(12,2))ATC93AF,
       cast(coalesce(AC10.BBA100, 0) AS numeric(12,2))BBA100,
       cast(coalesce(AC10.BAA100, 0) AS numeric(12,2))BAA100,
       cast(coalesce(AC10.CAA101, 0) AS numeric(12,2))CAA101,
       cast(coalesce(AC10.CAA102, 0) AS numeric(12,2))CAA102,
       cast(coalesce(AC10.BBA300, 0) AS numeric(12,2))BBA300,
       cast(coalesce(AC10.BAA300, 0) AS numeric(12,2))BAA300,
       cast(coalesce(AC10.BAA101, 0) AS numeric(12,2))BAA101,
       cast(coalesce(AC10.BAA102, 0) AS numeric(12,2))BAA102,
       cast(coalesce(AC10.BAA103, 0) AS numeric(12,2))BAA103,
       cast(coalesce(TC93.ATC9P2, 0) AS numeric(12,2))ATC9P2,
       cast(coalesce(TC93.ATC9P3, 0) AS numeric(12,2))ATC9P3,
       (SELECT AAE005 FROM tc90 WHERE atc900 = tc93.atc900) AAE005,--联系电话
       (SELECT AAE00B FROM tc90 WHERE atc900 = tc93.atc900) AAE00B,--员工号
       (SELECT ATB90Y FROM tb93 WHERE atb930 = tc93.atb930) ATB90Y,--是否代发工资
       (SELECT count(1) FROM tc8g where atc930=TC93.atc930 and atc8g4='1') atc8g0,--网银是否发放成功
       (SELECT aae00a FROM TC90 WHERE ATC900=TC93.ATC900) AAE00A,--职工号
       (SELECT atc904 FROM TC90 WHERE ATC900=TC93.ATC900) ATC904--职工号

  FROM TC93
  LEFT JOIN V_AC10_ZCXJ AC10
    ON (TC93.ATC900 = AC10.ATC900 AND TC93.ATC937 = '1' AND
       TC93.AAE001 = AC10.AAE001 AND TC93.ATB930 = AC10.ATB930 ) where tc93.aae002>'201812' order by atc904,atc930

 

;
DROP VIEW IF EXISTS v_tc8t_tc8v;
CREATE OR REPLACE VIEW v_tc8t_tc8v AS SELECT row_number() OVER ()   AS ID,
(CASE
         WHEN A.AAE114 = '0' AND B.AAE114 = '0' THEN
          '1'
         WHEN A.AAE114 = '0' THEN
          '2'
         ELSE
          '3'
       END) TYPE,
       A.ATC8T0,
       A.ATB930,
       A.ATB931,
       A.AAB001,
       A.AAB004,
       A.ATC8T1,

       A.ATC8T2,
       A.ATC8T3,
       A.ATC8T4,
       A.ATC8T5,
       A.ATC8T6,
       A.ATC8T7,
       A.AAE114,
       A.AAE111,
       A.AAE117,
       A.AAE136,
       A.AAE119,
       A.AAE120,
       A.AAE122,
       A.AAE013,
       A.AAE011,
       A.AAE017,
       A.AAE036,
       A.AAE019,
       A.AAE020,
       A.AAE022,
       B.AAE114 AS AAE214,
       B.ATC8V0,
       B.ATC8U0,
       B.ATC931,
       B.ATC932,
       B.ATC937,
       B.AAE002,
       B.ATC8V1,
       (SELECT ATB90Y FROM TB93 WHERE ATB930=A.ATB930) ATB90Y
  FROM tc8t a LEFT JOIN tc8v b ON a.atc8t0 = b.atc8t0
   AND (A.AAE114 = '0' OR B.AAE114 = '0')

 

;
CREATE OR REPLACE VIEW v_tc90_ab01_tb93 AS SELECT ATC900,
       AAE002,
       AAE003,
       a.AAB004,
       C.ATB931,
       B.AAA030,
       B.AAA031,
       AAC003,
       AAC002,
       AAC004,
       AAC008,
       AAA001,
       ATC90P,
       (SELECT AAE013
          FROM TC9W_BP
         WHERE aaa90w='2' and AAE002 = A.AAA208
           AND aac003 = A.aac003
           and aac002=a.aac002 LIMIT 1) AAE013_1,
       AAA201,
       AAA202,
       AAA203,
       AAA204,
       AAA208,
       AAA209,
       AAA100,
       AAA101,
       AAA005,
       ATC90PA,
       (CASE
         WHEN substring(AAA110, 3, 2) = '99' THEN
          (SELECT AAE013
             FROM TC9W_BP01
            WHERE AAE002 = A.AAA608
           AND aac003 = A.aac003
           and aac002=a.aac002 LIMIT 1)
         ELSE
          (SELECT AAE013
             FROM TC9W1
            WHERE AAE002 = substring(pkg_common.get_param()::text, 1, 6)::numeric
           AND aac003 = A.aac003
           and aac002=a.aac002 LIMIT 1)
       END) AAE013_2,
       (SELECT AAA90Y
          FROM TC9W_BP04
         WHERE AAE002 = substring(pkg_common.get_param()::text, 1, 6)::numeric
           AND aac003 = A.aac003
           and aac002=a.aac002 LIMIT 1) AAA90Y,
       AAA601,
       AAA602,
       AAA603,
       AAA604,
       AAA608,
       AAA609,
       AAA110,
       AAA111,
       AAA009,
       ATC90PB,
        (SELECT AAE013
          FROM TC9W_BP09
         WHERE aaa90w='2' and AAE002 = A.AAA908
           AND aac003 = A.aac003
           and aac002=a.aac002 LIMIT 1) AAE013_4,
       AAA901,
       AAA902,
       AAA903,
       AAA904,
       AAA908,
       AAA909,
       AAA130,
       AAA131,
       AAA004,
       ATC90W,
       (SELECT ABP003
          FROM TC9U_BP03
         WHERE ACE10A = '1'
           AND AAE002 = A.AAA208
           AND aac003 = A.aac003
           and ABP002=a.aac002 LIMIT 1) AAE013_3,
       (SELECT AAA90Z
          FROM TC9W_BP02
         WHERE AAE002 = A.AAA508
           AND aac003 = A.aac003
           and aac002=a.aac002 LIMIT 1) AAA90Z,
       AAA501,
       AAA502,
       AAA503,
       AAA504,
       AAA508,
       A.AAA509,
       a.AAA120,
       a.AAA121,
       AAA301,
       AAA302,
       AAA303,
       AAA304,
       AAA305,
       AAA306,
       a.ATB90S,
       a.AAE036,
       a.AAE019,
       a.AAE020,
       A.ATB90R,
       a.aae005,
       B.BAE011 ZZZ011


  FROM TC90 A, AB01 B, TB93 C
 WHERE A.AAB001 = B.AAB001
   AND A.ATB930 = C.ATB930

 

;
CREATE OR REPLACE VIEW v_tc90_dfgz AS SELECT TC90.BAZ001,
       TC90.BAZ002,
       TC90.ATC900,

       TC90.ATB930,
       TC90.ATB900,
       TC90.AAB001,
       TC90.ID,
       TC90.AAB004,
       TC90.AAB001_1,
       TC90.ID_1,
       TC90.AAB004_1,
       TC90.AAC001,
       TC90.AAC003,
       TC90.AAC002,
       TC90.AAC004,
       TC90.AAC006,
       TC90.ATC901,
       TC90.AAB299,
       TC90.BAB305,
       TC90.AAC017,
       TC90.AAC011,
       TC90.AAC024,
       TC90.AAE006,
       TC90.AAC009,
       TC90.AAC005,
       TC90.AAC033,
       TC90.AAE005,
       TC90.AAC181,
       TC90.AAC180,
       TC90.AAC182,
       TC90.AAC183,
       TC90.AAC007,
       TC90.AAC015,
       TC90.AAC184,
       TC90.ACA111,
       TC90.ACA112,
       TC90.AAC014,
       TC90.BAC014,
       TC90.AAC185,
       TC90.AAC025,
       TC90.AAE008,
       TC90.AAE00D,
       TC90.ATC902,
       TC90.ATC90R,
       TC90.AAE009,
       TC90.AAE00A,
       TC90.AAC013,
       TC90.AAE00P,
       TC90.ATC903,
       TC90.ATC904,
       TC90.ATC905,
       TC90.ATC907,
       TC90.ATC908,
       TC90.ATC909,
       TC90.ATC90A,
       TC90.ATC90B,
       TC90.ATC90C,
       TC90.ATC90D,
       TC90.ATC90E,
       TC90.ATB904,
       TC90.ATC90F,
       TC90.ATC90G,
       TC90.ATC90H,
       TC90.AAA001,
       TC90.AAA002,
       TC90.AAA003,
       TC90.AAA004,
       TC90.AAA100,
       TC90.AAA101,
       TC90.AAA201,
       TC90.AAA202,
       TC90.AAA301,
       TC90.AAA302,
       TC90.AAA303,
       TC90.AAA304,
       TC90.AAA305,
       TC90.AAA306,
       TC90.ATB90S,
       TC90.AAA401,
       TC90.AAA402,
       TC90.AAA403,
       TC90.AAA404,
       TC90.AAA405,
       TC90.AAA406,
       TC90.AAA407,
       (SELECT BBB001 FROM AC01 WHERE AAC001=TC90.AAC001) BBB001,
       TC90.AAE100,
       TC90.AAE011,
       TC90.AAE017,
       TC90.AAE036,
       TC90.AAE019,
       TC90.AAE020,
       TC90.AAE022,
       TC90.ATC90I,
       TC90.ATC90J,
       TC90.ATC90K,
       TC90.ATC90L,
       TC90.ATC90M,
       TC90.ATC90N,
       TC90.ATC90O,
       TC90.AAE013,
       TC90.BAE011,
       TC90.BAE017,
       TC90.BAE036,
       TC90.BAE019,
       TC90.BAE020,
       TC90.BAE022,
       TC90.CCC001,
       TC90.ATC90X,
       TC90.ATB903,
       TC90.ATC90P,
       TC90.ATC90Q,
       TC90.ATC90T1,
       TC90.ATC90T2,
       TC90.ATC90T3,
       TC90.ATC90T4,
       TC90.ATC90T5,
       TC90.ATC90T6,
       TC90.ATC90U,
       TC90.ATB910,
       TC90.ATB90R,
       TC90.AAA501,
       TC90.AAA502,
       TC90.ATC90W,
       TC90.ATC9V1,
       TC90.ATB90AB,
       TC90.ATC90AA,
       TC90.ATC90AB,
       TC90.ATC90AC,
       TC90.ATC90AD,
       TC90.ATC90AE,
       TC90.ATC90AF,
       TC90.ATC90AG,
       TC90.ATC90AH,
       TC90.ATC90AI,
       TC90.ATC90AJ,
       TC90.ATC90AK,
       TC90.ATC90AL,
       TC90.ATC90AM,
       TC90.ATC90AN,
       TC90.ATC90AO,
       TC90.ATC90AP,
       TC90.ATC90AQ,
       TC90.ATC90AR,
       TC90.ATB90AD,
       TC90.ATC90AS,
       TC90.ATC90AT,
       TC90.ATC90AU,
       TC90.ATC90AV,
       TC90.AAE00B,
       TC90.ATC9WC,
       TC90.ATC90AW,
       TC90.AAE002,
       TC90.AAE003,
       TB93.ATB931,
       TB93.BBB002,
       (SELECT AAB00E FROM AB01 WHERE AAB001=TC90.AAB001) AAB00E
  FROM TC90, TB93
 WHERE TB93.ATB930 = TC90.ATB930
   AND TB93.ATB90Y = '1'
   AND ATC900 IN (SELECT ATC900 FROM TC93 WHERE AAE001 = to_char(now(),'YYYYMM')::numeric)

 

;
CREATE OR REPLACE VIEW v_tc90_tc97 AS select t.BAZ001,
       t.BAZ002,
       t.ATC900,
       t.ATB930,
       t.ATB900,
       t.AAB001,
       t.ID,
       t.AAB004,
       t.AAB001_1,
       t.ID_1,
       t.AAB004_1,
       t.AAC001,
       t.AAC003,
       t.AAC002,
       t.AAC004,
       t.AAC006,
       t.ATC901,
       t.AAB299,
       t.BAB305,
       t.AAC017,
       t.AAC011,
       t.AAC024,
       t.AAE006,
       t.AAC009,
       t.AAC005,
       t.AAC033,
       t.AAE005,
       t.AAC181,
       t.AAC180,
       t.AAC182,
       t.AAC183,
       t.AAC007,
       t.AAC015,
       t.AAC184,
       t.ACA111,
       t.ACA112,
       t.AAC014,
       t.BAC014,
       t.AAC185,
       t.AAC025,
       t.AAE008,
       t.AAE00D,
       t.ATC902,
       t.ATC90R,
       t.AAE009,
       t.AAE00A,
       t.AAC013,
       t.AAE00P,
       t.ATC903,
       t.ATC904,
       t.ATC905,
       t.ATC907,
       t.ATC908,
       t.ATC909,
       t.ATC90A,
       t.ATC90B,
       t.ATC90C,
       t.ATC90D,
       t.ATC90E,
       t.ATB904,
       t.ATC90F,
       t.ATC90G,
       t.ATC90H,
       t.AAA001,
       t.AAA002,
       t.AAA003,
       t.AAA004,
       t.AAA100,
       t.AAA101,
       t.AAA201,
       t.AAA202,
       t.AAA301,
       t.AAA302,
       t.AAA303,
       t.AAA304,
       t.AAA305,
       t.AAA306,
       t.ATB90S,
       t.AAA401,
       t.AAA402,
       t.AAA403,
       t.AAA404,
       t.AAA405,
       t.AAA406,
       t.AAA407,
       t.BBB001,
       t.BBB002,
       t.AAE100,
       t.AAE011,
       t.AAE017,
       t.AAE036,
       t.AAE019,
       t.AAE020,
       t.AAE022,
       t.ATC90I,
       t.ATC90J,
       t.ATC90K,
       t.ATC90L,
       t.ATC90M,
       t.ATC90N,
       t.ATC90O,
       t.AAE013,
       t.BAE011,
       t.BAE017,
       t.BAE036,
       t.BAE019,
       t.BAE020,
       t.BAE022,
       t.CCC001,
       t.ATC90X,
       t.ATB903,
       t.ATC90P,
       t.ATC90Q,
       t.ATC90T1,
       t.ATC90T2,
       t.ATC90T3,
       t.ATC90T4,
       t.ATC90T5,
       t.ATC90T6,
       t.ATC90U,
       t.ATB910,
       t.ATB90R,
       t.AAA501,
       t.AAA502,
       t.ATC90W,
       t.ATC9V1,
       t.ATB90AB,
       t.ATC90AA,
       t.ATC90AB,
       t.ATC90AC,
       t.ATC90AD,
       t.ATC90AE,
       t.ATC90AF,
       t.ATC90AG,
       t.ATC90AH,
       t.ATC90AI,
       t.ATC90AJ,
       t.ATC90AK,
       t.ATC90AL,
       t.ATC90AM,
       t.ATC90AN,
       t.ATC90AO,
       t.ATC90AP,
       t.ATC90AQ,
       t.ATC90AR,
       t.ATB90AD,
       t.ATC90AS,
       t.ATC90AT,

       tc97.ATC977,
       tc97.ATC970,
       tc97.ATC978,
       tc97.atc97a,
       t.aae00b,
       (select aae013 from ac01 where aac001=t.aac001) as aae013b
  from tc90 t LEFT JOIN (select * from tc97 a where a.ATC97B = '1') tc97 ON t.atc900 = tc97.id

 

;
CREATE OR REPLACE VIEW v_tc90_xzgjj AS SELECT ATC900,
       AAE002,
       AAE003,
       a.AAB004,
       AAC003,
       AAC002,
       AAC004,
       AAC008,
       AAA001,
       ATC90P,
       AAA201,
       AAA202,
       AAA100,
       AAA101,
       AAA005,
       ATC90PA,
       AAA601,
       AAA602,
       AAA110,
       AAA111,
       AAA004,
       ATC90W,
       AAA501,
       AAA502,
       AAA120,
       AAA121,
       AAA301,
       AAA302,
       AAA303,
       AAA304,
       AAA305,
       AAA306,
       ATB90S,
       a.AAE036,
       a.AAE019,
       a.AAE020

  FROM TC90 A, AB01 B
 WHERE A.AAB001 = B.AAB001
   AND B.AAA030 = substring(pkg_common.get_param()::text, 1, 1)::numeric
   AND A.AAA120 = substring(pkg_common.get_param()::text, 8, 12)
   AND A.ATC90W = '2'
   AND A.ATB90R <> '5'
   order by a.atc900

 

;
CREATE OR REPLACE VIEW v_tc90_xzgjj_1 AS SELECT BAZ001,BAZ002,ATC900,ATB930,ATB900,AAB001,ID,AAB004,AAB001_1,ID_1,AAB004_1,AAC001,AAC003,AAC002,AAC004,AAC006,ATC901,AAB299,BAB305,AAC017,AAC011,AAC024,AAE006,AAC009,AAC005,AAC033,AAE005,AAC181,AAC180,AAC182,AAC183,AAC007,AAC015,AAC184,ACA111,ACA112,AAC014,BAC014,AAC185,AAC025,AAE008,AAE00D,ATC902,ATC90R,AAE009,AAE00A,AAC013,AAE00P,ATC903,ATC904,ATC905,ATC907,ATC908,ATC909,ATC90A,ATC90B,ATC90C,ATC90D,ATC90E,ATB904,ATC90F,ATC90G,ATC90H,AAA001,AAA002,AAA003,AAA004,AAA100,AAA101,AAA201,AAA202,AAA301,AAA302,AAA303,AAA304,AAA305,AAA306,ATB90S,AAA401,AAA402,AAA403,AAA404,AAA405,AAA406,AAA407,BBB001,BBB002,AAE100,AAE011,AAE017,AAE036,AAE019,AAE020,AAE022,ATC90I,ATC90J,ATC90K,ATC90L,ATC90M,ATC90N,ATC90O,AAE013,BAE011,BAE017,BAE036,BAE019,BAE020,BAE022,CCC001,ATC90X,ATB903,ATC90P,ATC90Q,ATC90T1,ATC90T2,ATC90T3,ATC90T4,ATC90T5,ATC90T6,ATC90U,ATB910,ATB90R,AAA501,AAA502,ATC90W,ATC9V1,ATB90AB,ATC90AA,ATC90AB,ATC90AC,ATC90AD,ATC90AE,ATC90AF,ATC90AG,ATC90AH,ATC90AI,ATC90AJ,ATC90AK,ATC90AL,ATC90AM,ATC90AN,ATC90AO,ATC90AP,ATC90AQ,ATC90AR,ATB90AD,ATC90AS,ATC90AT,ATC90AU,ATC90AV,AAE00B,ATC9WC,ATC90AW,AAE002,AAE003,AAC012,ATB90RA,AAA701,AAA702,ATC90PA,AAA601,AAA602,AAA005,AAA110,AAA111,AAA120,AAA121,AAA308,ATC90AX,ATB90C,ATB90A,ATB90B,AAA408,AAA801,ATC90ZA,ATC90ZB,AAA803,AAA203,AAA204,AAA503,AAA504,AAA603,AAA604,AAC008

  FROM TC90
 WHERE ATC900 IN
       (SELECT A.ATC900
          FROM TC90 A, AB01 B
         WHERE A.AAB001 = B.AAB001
           AND B.AAA030 = substring(pkg_common.get_param()::text, 1, 1)::numeric
           AND A.AAA407 = '1'
           AND A.ATC90w IS NULL
           AND A.AAA501 <= substring(pkg_common.get_param()::text, 2, 6)::numeric
           AND A.AAA120 = substring(pkg_common.get_param()::text, 8, 12)
        EXCEPT (SELECT ATC900
                FROM tc9u
               WHERE ATC9U1 IN ('1', '2')
                 AND AAA030 = substring(pkg_common.get_param()::text, 1, 1)::numeric
                 AND AAA100 = substring(pkg_common.get_param()::text, 8, 12)
                 AND AAE002 = substring(pkg_common.get_param()::text, 2, 6)::numeric))

 

;
CREATE OR REPLACE VIEW v_tc90_xzgs AS SELECT ATC900,
       AAE002,
       AAE003,
       a.AAB004,
       AAC003,
       AAC002,
       AAC004,
       AAC008,
       AAA001,
       ATC90P,
       AAA201,
       AAA202,
       AAA100,
       AAA101,
       AAA005,
       ATC90PA,
       AAA601,
       AAA602,
       AAA110,
       AAA111,
       AAA004,
       ATC90W,
       AAA501,
       AAA502,
       AAA120,
       AAA121,
       ATC90PB,
       AAA901,
       AAA902,
       AAA130,
       AAA131,
       AAA009,
       AAA301,
       AAA302,
       AAA303,
       AAA304,
       AAA305,
       AAA306,
       ATB90S,
        a.AAE036,
       a.AAE019,
       a.AAE020


  FROM TC90 A, AB01 B
 WHERE A.AAB001 = B.AAB001
   AND B.AAA030 = substring(pkg_common.get_param()::text, 1, 1)::numeric
   AND AAA130 = substring(pkg_common.get_param()::text, 8, 12)
   AND ATC90PB = '2'

 

;
CREATE OR REPLACE VIEW v_tc90_xzsb AS SELECT a.ATC900,
       a.AAE002,
       a.AAE003,
       a.AAB004,
       a.AAC003,
       a.AAC002,
       a.AAC004,
       a.AAC008,
       a.AAA001,
      a. ATC90P,
       a.AAA201,
       a.AAA202,
       a.AAA100,
       a.AAA101,
       a.AAA005,
       a.ATC90PA,
      a. AAA601,
       a.AAA602,
       a.AAA110,
       a.AAA111,
       a.AAA004,
       a.ATC90W,
       a.AAA501,
       a.AAA502,
       a.AAA120,
       a.AAA121,
       a.AAA301,
       a.AAA302,
       AAA303,
       a.AAA304,
       a.AAA305,
       a.AAA306,
       a.ATB90S,
       a.AAE036,
       a.AAE019,
       a.AAE020,
       (SELECT AAE013
          FROM TC9W_BP
         WHERE AAE002 = A.AAA208
           AND ATC900 = A.ATC900) AAE013_1


  FROM TC90 A, AB01 B
 WHERE A.AAB001 = B.AAB001
   AND A.ATB90R = '3'
   AND B.AAA030 = substring(pkg_common.get_param()::text, 1, 1)::numeric
   AND A.AAA100 = substring(pkg_common.get_param()::text, 8, 12)
   AND A.ATC90P = '2'

 

;
CREATE OR REPLACE VIEW v_tc90_xzyb AS SELECT ATC900,
       AAE002,
       AAE003,
       a.AAB004,
       AAC003,
       AAC002,
       AAC004,
       AAC008,
       AAA001,
       ATC90P,
       AAA201,
       AAA202,
       AAA100,
       AAA101,
       AAA005,
       ATC90PA,
       AAA601,
       AAA602,
       AAA110,
       AAA111,
       AAA004,
       ATC90W,
       AAA501,
       AAA502,
       AAA120,
       AAA121,
       AAA301,
       AAA302,
       AAA303,
       AAA304,
       AAA305,
       AAA306,
       ATB90S,
       a.AAE036,
       a.AAE019,
       a.AAE020

  FROM TC90 A, AB01 B
 WHERE A.AAB001 = B.AAB001
   AND B.AAA030 = substring(pkg_common.get_param()::text, 1, 1)::numeric
   AND AAA110 = substring(pkg_common.get_param()::text, 8, 12)
   AND ATC90PA = '2'

 

;
CREATE OR REPLACE VIEW v_tc90_xzyb_1 AS SELECT ATC900,
       AAE002,
       AAE003,
       a.AAB004,
       AAC003,
       AAC002,
       AAC004,
       AAC008,
       AAA001,
       ATC90P,
       AAA201,
       AAA202,
       AAA100,
       AAA101,
       AAA005,
       ATC90PA,
       AAA601,
       AAA602,
       AAA110,
       AAA111,
       AAA004,
       ATC90W,
       AAA501,
       AAA502,
       AAA120,
       AAA121,
       AAA301,
       AAA302,
       AAA303,
       AAA304,
       AAA305,
       AAA306,
       ATB90S,
        a.AAE036,
       a.AAE019,
       a.AAE020


  FROM TC90 A, AB01 B
 WHERE A.AAB001 = B.AAB001
   AND B.AAA030 = substring(pkg_common.get_param()::text, 1, 1)::numeric
   AND AAA110 = substring(pkg_common.get_param()::text, 8, 12)
   AND ATC90PA = '2'
   AND ATB90R <> '5'
   order by a.atc900

 

;
CREATE OR REPLACE VIEW v_tc90_xzyb_2 AS SELECT ATC900,
       AAE002,
       AAE003,
       a.AAB004,
       AAC003,
       AAC002,
       AAC004,
       AAC008,
       AAA001,
       ATC90P,
       AAA201,
       AAA202,
       AAA100,
       AAA101,
       AAA005,
       ATC90PA,
       AAA601,
       AAA602,
       AAA110,
       AAA111,
       AAA004,
       ATC90W,
       AAA501,
       AAA502,
       AAA120,
       AAA121,
       AAA301,
       AAA302,
       AAA303,
       AAA304,
       AAA305,
       AAA306,
       ATB90S,
        a.AAE036,
       a.AAE019,
       a.AAE020


  FROM TC90 A, AB01 B
 WHERE A.AAB001 = B.AAB001
   AND B.AAA030 = substring(pkg_common.get_param()::text, 1, 1)::numeric
   AND AAA110 = substring(pkg_common.get_param()::text, 8, 12)
   AND ATC90PA = '2'

 

;
CREATE OR REPLACE VIEW v_tc93_aae005 AS SELECT TC93.ATC930, --工资编号
       TC93.ATB930, --单位派遣协议编号
       TC93.AAB004, --派往单位名称
       TC93.ATC931, --工资所属年月
       TC93.ATC932, --社保所属年月
       TC93.ATC937, --工资发放次数
       TC93.ATB931, --结算单元名称
       TC93.AAC002, --身份证号码
       TC93.AAC003, --姓名
       (select bbb001 from ac01 where aac001=TC93.Aac001) AS BBB001, --工资帐号
       TC93.BBB002, --发放银行
       TC93.ATB90A, --管理费
       TC93.ATB90Z, --残疾人保障金(元人)
       TC93.ATC93X, --本次基本工资
       TC93.ATC933, --本次应发工资
       TC93.ATC934, --本次扣款
       TC93.ATC935, --本次补发
       TC93.ATC936, --本次免税
       TC93.ATC93K, --本次年终奖
       TC93.ATC93L, --本次事假
       TC93.ATC93M, --本次经济补偿金
       TC93.ATC93Y, --本次病假
       TC93.ATC93AA, --本次工资总额
       TC93.ATC93AB, --本次应税金额
       TC93.ATC93D, --本次个人所得税
       TC93.ATC93AC, --本次个人所得税(年终奖)
       TC93.ATC93AD, --本次个人所得税(工资)
       TC93.ATC93C, --本次实发金额
       TC93.ATC93G, --是否结算
       TC93.ATC93N, --是否存在上月工资
       TC93.ATC93P, --是否需审核
       TC93.ATC93S, --单位垫付险金
       TC93.ATC93T, --是否欠费
       TC93.ATC93U, --工资是否可发情况
       TC93.ATC93V, --费用状态
       TC93.ATC93W, --结算信息来源
       TC93.AAE114, --审核标记
       TC93.AAE111, --审核人代码
       TC93.AAE117, --审核机构代码
       TC93.AAE136, --审核日期
       TC93.AAE119, --审核人
       TC93.AAE120, --审核机构
       TC93.AAE122, --审核机构行政区划
       TC93.AAE214, --确认标记
       TC93.AAE211, --确认人代码
       TC93.AAE217, --确认机构代码
       TC93.AAE236, --确认日期
       TC93.AAE219, --确认人
       TC93.AAE220, --确认机构
       TC93.AAE222, --确认机构行政区划
       TC93.AAE314, --过月标记
       TC93.AAE311, --过月人代码
       TC93.AAE317, --过月机构代码
       TC93.AAE336, --过月日期
       TC93.AAE319, --过月人
       TC93.AAE320, --过月机构
       TC93.AAE322, --过月机构行政区划
       TC93.ATC910, --流水号
       TC93.AAE011, -- 经办人代码
       TC93.AAE017, --经办机构代码
       TC93.AAE036, --经办日期
       TC93.AAE019, --经办人
       TC93.AAE020, --经办机构
       TC93.AAE013, --备注
       TC93.aae002,--费用清单年月
       TC93.aae001,
       tc93.atc900,
       cast(coalesce(AC10.BAA000, 0) AS numeric(12,2)) BAA000,
       cast(coalesce(AC10.BAA001, 0) AS numeric(12,2)) BAA001,
       cast(coalesce(AC10.BAA002, 0) AS numeric(12,2))  BAA002,
       cast(coalesce(AC10.BAA003, 0)AS numeric(12,2)) BAA003,
       cast(coalesce(AC10.BBA001, 0) AS numeric(12,2))BBA001,
       cast(coalesce(AC10.BBA002, 0)AS numeric(12,2)) BBA002,
       cast(coalesce(AC10.BBA003, 0) AS numeric(12,2))BBA003,
       cast(coalesce(AC10.BBA004, 0) AS numeric(12,2))BBA004,
       cast(coalesce(AC10.BBA005, 0) AS numeric(12,2))BBA005,
       cast(coalesce(AC10.BBA000, 0) AS numeric(12,2))BBA000,
       cast(coalesce(AC10.CAA001, 0) AS numeric(12,2))CAA001,
       cast(coalesce(AC10.CAA002, 0) AS numeric(12,2))CAA002,
       cast(coalesce(AC10.BBA200, 0) AS numeric(12,2))BBA200,
       cast(coalesce(AC10.BAA200, 0) AS numeric(12,2))BAA200,
       cast(coalesce(AC10.DAA001, 0) AS numeric(12,2))DAA001,
       cast(coalesce(AC10.ATC93AF, 0) AS numeric(12,2))ATC93AF,
       cast(coalesce(AC10.BBA100, 0) AS numeric(12,2))BBA100,
       cast(coalesce(AC10.BAA100, 0) AS numeric(12,2))BAA100,
       cast(coalesce(AC10.CAA101, 0) AS numeric(12,2))CAA101,
       cast(coalesce(AC10.CAA102, 0) AS numeric(12,2))CAA102,
       cast(coalesce(AC10.BBA300, 0) AS numeric(12,2))BBA300,
       cast(coalesce(AC10.BAA300, 0) AS numeric(12,2))BAA300,
       cast(coalesce(AC10.BAA101, 0) AS numeric(12,2))BAA101,
       cast(coalesce(AC10.BAA102, 0) AS numeric(12,2))BAA102,
       cast(coalesce(AC10.BAA103, 0) AS numeric(12,2))BAA103,
       cast(coalesce(TC93.ATC9P2, 0) AS numeric(12,2))ATC9P2,
       cast(coalesce(TC93.ATC9P3, 0) AS numeric(12,2))ATC9P3,
       (SELECT AAE005 FROM tc90 WHERE atc900 = tc93.atc900) AAE005,--联系电话
       (SELECT AAE00B FROM tc90 WHERE atc900 = tc93.atc900) AAE00B,--员工号
       (SELECT ATB90Y FROM tb93 WHERE atb930 = tc93.atb930) ATB90Y,--是否代发工资
       (SELECT count(1) FROM tc8g where atc930=TC93.atc930 and atc8g4='1') atc8g0,--网银是否发放成功
       (SELECT aae00a FROM TC90 WHERE ATC900=TC93.ATC900) AAE00A,--职工号
       (SELECT atc904 FROM TC90 WHERE ATC900=TC93.ATC900) ATC904--职工号

  FROM TC93
  LEFT JOIN V_AC10 AC10
    ON (TC93.ATC900 = AC10.ATC900 AND TC93.ATC937 = '1' AND
       TC93.AAE001 = AC10.AAE001 AND TC93.ATB930 = AC10.ATB930 AND ac10.AAE001>'201712') where tc93.aae001>'201712'

 

;
CREATE OR REPLACE VIEW v_tc93_zcxj AS SELECT
       T.AAC002,
       T.AAC003,
       T.AAE005,
       T.AAE002,
       (coalesce(SUM(ATC93M), 0)) ATC93M,
       (coalesce(SUM(ATC93K), 0)) ATC93K,
       (coalesce(SUM(ATC93AA), 0)-coalesce(SUM(ATC93K), 0)) ATC933,
       (coalesce(SUM(ATC93C), 0)-coalesce(SUM(ATC93K), 0)+coalesce(SUM(ATC93AC), 0)) ATC93C,
       (coalesce(SUM(atc93d), 0)-coalesce(SUM(ATC93AC), 0))  ATC93D,
       (coalesce(SUM(BAA001), 0)) BAA001,
       (coalesce(SUM(BAA002), 0)) BAA002,
       (coalesce(SUM(BAA003), 0)) BAA003,
       (coalesce(SUM(CAA002), 0)) CAA002,
       (coalesce(SUM(BAA300), 0)) BAA100,
       (coalesce(SUM(BAA101), 0)+coalesce(SUM(BAA102), 0)+coalesce(SUM(BAA103), 0))       BAA200,
       (coalesce(SUM(CAA102),0)) CAA200,
       (coalesce(SUM(ATC93S),0)) ATC93S


  FROM V_TB93_TC93_ZCXJ T where  t.atb90y='1'  and T.atc8g0>0 AND T.AAE002>'201812'
 GROUP BY T.AAC002, T.AAC003, T.AAE005, T.AAE002

 

;
CREATE OR REPLACE VIEW v_tc9a AS SELECT
ATC9A0,
AAB004,
AAB004_1,
AAC001,
AAC002,
AAC003,
ATC9A8,
ATC9A9,
AAE036,
(SELECT MAX(ATC978) FROM TC97 WHERE AAC001=TC9A.AAC001::numeric AND ATC977='1') AS ATC978
 from TC9A

 

;
CREATE OR REPLACE VIEW v_tc9f AS SELECT  BAZ001 ,
  BAZ002 ,
  ATC9F0 ,
  AAB001 ,
  ID     ,
  AAB004 ,
  AAC001 ,
  AAC002 ,
  AAC003 ,
  AAC004 ,
  AAC011 ,
  AAC181 ,
  AAC180 ,
  AAC182 ,
  AAC183 ,
  AAE00B ,
  AAE00C ,
  ATC9F2 ,
  ATC9F3 ,
  ATC9F4 ,
  ATC9F5 ,
  ATC9F6 ,
  ATC9F7 ,
  ATC9F8 ,
  ATC9F9 ,
  ATC9FA ,
  ATC9FB ,
  ATC9FC ,
  ATC9FD ,
  ATC9FE ,
  ATC9FF ,
  ATC9FG ,
  ATC9FH ,
  ATC9FI ,
  ATC9FJ ,
  ATC9FK ,
  ATC9FL ,
  ATC9FM ,
  ATC9FN ,
  (SELECT CASE WHEN (COUNT(1)) IS NOT DISTINCT FROM ('0') THEN ('2') ELSE ('1') END FROM TC90 WHERE AAE100='1' AND ATB90R='3' AND AAC002=TC9F.AAC002)ATC9FO ,
  AAE100 ,
  BAE011 ,
  BAE017 ,
  BAE036 ,
  BAE019 ,
  BAE020 ,
  BAE022 ,
  AAE013 ,
  AAE011 ,
  AAE017 ,
  AAE036 ,
  AAE019 ,
  AAE020 ,
  AAE022 ,
  AAE00M ,
  ATC9FP ,
  ATC9FQ ,
  ATC9FR ,
  ATC9FS
  FROM TC9F

 

;
