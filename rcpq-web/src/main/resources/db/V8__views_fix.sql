-- V8: V6 视图修复(Oracle 导出全角标点致 CREATE 静默失败; sysdate/MINUS/decode/to_char 方言清洗)
-- 生成: scripts/gen_views_fix.py; 仍挂起视图见 work/pending_views.txt

CREATE OR REPLACE FUNCTION public.to_char(n numeric) RETURNS text LANGUAGE sql IMMUTABLE AS $fn$ SELECT n::text $fn$;

CREATE OR REPLACE FUNCTION public.to_number(s text) RETURNS numeric LANGUAGE sql IMMUTABLE AS $fn$ SELECT s::numeric $fn$;


CREATE OR REPLACE VIEW V_TC90_DZH2 AS
SELECT A.AAE00M,
       T.BAZ001,
       T.BAZ002,
       T.ATC900,
       T.ATB930,
       T.ATB900,
       T.AAB001,
       T.ID,
       T.AAB004,
       T.AAB001_1,
       T.ID_1,
       T.AAB004_1,
       T.AAC001,
       T.AAC003,
       T.AAC002,
       T.Atc90ax,
       T.AAC004,
       T.AAC006,
       T.ATC901,
       T.AAB299,
       T.BAB305,
       T.AAC017,
       T.AAC011,
       T.AAC024,
       T.AAE006,
       T.AAC009,
       T.AAC005,
       T.AAC033,
       T.AAE005,
       T.AAC181,
       T.AAC180,
       T.AAC182,
       T.AAC183,
       T.AAC007,
       T.AAC015,
       T.AAC184,
       T.ACA111,
       T.ACA112,
       T.AAC014,
       T.BAC014,
       T.AAC185,
       T.AAC025,
       T.AAE008,
       T.AAE00D,
       T.ATC902,
       T.ATC90R,
       (select max(atc978) from tc97 where atc977='1' and aac002 =t.aac002) AAE009,
       T.AAE00A,
       T.AAC013,
       T.AAE00P,
       T.ATC903,
       T.ATC904,
       T.ATC905,
       T.ATC907,
       T.ATC908,
       T.ATC909,
       T.ATC90A,
       T.ATC90B,
       T.ATC90C,
       T.ATC90D,
       T.ATC90E,
       T.ATB904,
       T.ATC90F,
       T.ATC90G,
       T.ATC90H,
       T.AAA001,
       T.AAA002,
       T.AAA005,
       T.AAA004,
       T.AAA100,
       T.AAA101,
       T.AAA110,
       T.AAA111,
       T.AAA120,
       T.AAA121,
       T.AAA201,
       T.AAA202,
       CASE
         WHEN T.AAA301 IS NOT NULL THEN
          FUN_缴费基数(PRM_BX     => '1',
                   PRM_AAE002 => TO_CHAR(now(), 'yyyymm'),
                   PRM_AAA100 => T.AAA100,
                   PRM_JS     => T.ATC905)
         ELSE
          NULL
       END AAA301,
       CASE
         WHEN T.AAA302 IS NOT NULL THEN
          FUN_缴费基数(PRM_BX     => '2',
                   PRM_AAE002 => TO_CHAR(now(), 'yyyymm'),
                   PRM_AAA100 => T.AAA110,
                   PRM_JS     => T.AAA302)
         ELSE
          NULL
       END AAA302,
       CASE
         WHEN T.AAA303 IS NOT NULL THEN
          FUN_缴费基数(PRM_BX     => '3',
                   PRM_AAE002 => TO_CHAR(now(), 'yyyymm'),
                   PRM_AAA100 => T.AAA120,
                   PRM_JS     => T.AAA303)
         ELSE
          NULL
       END AAA303,
       T.AAA304,
       CASE
         WHEN T.AAA305 IS NOT NULL THEN
          FUN_缴费基数(PRM_BX     => '4',
                   PRM_AAE002 => TO_CHAR(now(), 'yyyymm'),
                   PRM_AAA100 => T.AAA130,
                   PRM_JS     => T.AAA305)
         ELSE
          NULL
       END AAA305,
       T.AAA306,
       T.ATB90S,
       T.AAA401,
       T.AAA402,
       T.AAA403,
       T.AAA404,
       T.AAA405,
       T.AAA406,
       T.AAA407,
       T.BBB001,
       T.BBB002,
       T.AAE100,
       T.AAE011,
       T.AAE017,
       T.AAE036,
       T.AAE019,
       T.AAE020,
       T.AAE022,
       T.ATC90I,
       T.ATC90J,
       T.ATC90K,
       T.ATC90L,
       T.ATC90M,
       T.ATC90N,
       T.ATC90O,
       T.AAE013,
       T.BAE011,
       T.BAE017,
       T.BAE036,
       T.BAE019,
       T.BAE020,
       T.BAE022,
       T.CCC001,
       T.ATC90X,
       T.ATB903,
       T.ATC90P,
       T.ATC90PA,
       T.ATC90Q,
       T.ATC90T1,
       T.ATC90T2,
       T.ATC90T3,
       T.ATC90T4,
       T.ATC90T5,
       T.ATC90T6,
       T.ATC90U,
       T.ATB910,
       T.ATB90R,
       T.AAA501,
       T.AAA502,
       T.AAA601,
       T.AAA602,
       T.ATC90W,
       T.ATC9V1,
       T.ATB90AB,
       T.ATC90AA,
       T.ATC90AB,
       T.ATC90AC,
       T.ATC90AD,
       T.ATC90AE,
       T.ATC90AF,
       T.ATC90AG,
       T.ATC90AH,
       T.ATC90AI,
       T.ATC90AJ,
       T.ATC90AK,

       T.ATC90AL,
       T.ATC90AM,
       T.ATC90AN,
       T.ATC90AO,
       T.ATC90AP,
       T.ATC90AQ,
       T.ATC90AR,
       T.ATB90AD,
       T.ATC90AS,
       T.ATC90AT,
       T.ATC90AU,
       T.ATC90AV,
       T.AAE00B,
       T.ATC9WC,
       T.ATC90AW,
       T.AAC012,
       B.ATB931,
       to_char(t.aae036,'yyyymm') ny,
       T.AAA203,
       T.AAA204,
       T.AAA503,
       T.AAA504,
       T.AAA603,
       T.AAA604,
       T.Zynum,
       T.Jynum,
       t.aac008,
       d.aaa030,
       d.aaa031,
       d.bae011 zzz011,
       t.atc90pb,
       t.aaa901,
       t.aaa902,
       t.aaa903,
       t.aaa904,
       t.aaa908,
       t.aaa909,
       t.aaa130,
       t.aaa131,
       t.aaa009,
       (SELECT AAB00E FROM AB01 WHERE aab001 = T.aab001) AAB00E,
        
       (CASE WHEN t.atb90r='3' THEN t.aae002 WHEN t.atb90r='5' THEN t.aae003 ELSE NULL END) ny2,
       (SELECT AAB00A FROM AB01 WHERE aab001 = T.aab001) AAB00A,
       (select case when count(1)>0 then '有' else '无' end from tc90_zz where aac002=T.aac002 ) as atc90zz,
       (select case when count(1)>0 then '有' else '无' end from tc98 where atc970 in (select atc970 from tc97 where aac001=T.aac001) and  atc985='1')as atc980

  FROM AC01 A, TC90 T, TB93 B, TB90 C,AB01 D
 WHERE A.AAC001 = T.AAC001
   AND T.ATB930 = B.ATB930
   AND C.ATB900 = T.ATB900
   AND t.aab001=d.aab001;

-- Oracle instr(str,sub) 等价: PG strpos 同语义(1起, 无匹配=0)
CREATE OR REPLACE FUNCTION public.instr(str text, sub text) RETURNS int
LANGUAGE sql IMMUTABLE AS $fn$ SELECT strpos(str, sub) $fn$;CREATE OR REPLACE FUNCTION public.fun_缴费基数(prm_bx text, prm_aae002 text, prm_aaa100 text, prm_js text)
RETURNS text LANGUAGE plpgsql IMMUTABLE AS $fn$
DECLARE
  v_cat text; r_lo numeric := 0; r_hi numeric;
BEGIN
  v_cat := CASE prm_bx
             WHEN '1' THEN '1'
             WHEN '2' THEN CASE WHEN substr(prm_aaa100, 3, 2) = '99' THEN '3' ELSE '5' END
             WHEN '3' THEN CASE WHEN substr(prm_aaa100, 3, 2) = '99' THEN '2' ELSE '4' END
             WHEN '4' THEN CASE WHEN substr(prm_aaa100, 3, 2) = '99' THEN '6' ELSE '7' END
           END;
  IF v_cat IS NULL THEN RETURN prm_js; END IF;
  BEGIN
    SELECT aaa985, aaa986 INTO r_lo, r_hi FROM aa98
    WHERE aaa981 = v_cat AND aaa987 <= prm_aae002::numeric AND aaa988 >= prm_aae002::numeric;
  EXCEPTION WHEN OTHERS THEN
    RETURN prm_js;  -- 无行/多行/非数值: 原 EXCEPTION 路径 = 不夹紧
  END;
  IF prm_js::numeric < r_lo THEN RETURN r_lo::text; END IF;
  IF prm_js::numeric > r_hi THEN RETURN r_hi::text; END IF;
  RETURN prm_js;
END $fn$;6 视图修复(Oracle 导出全角标点致 CREATE 静默失败; sysdate/MINUS/decode/to_char 方言清洗)
-- 生成: scripts/gen_views_fix.py; 仍挂起视图见 work/pending_views.txt

CREATE OR REPLACE FUNCTION public.to_char(n numeric) RETURNS text LANGUAGE sql IMMUTABLE AS $fn$ SELECT n::text $fn$;

CREATE OR REPLACE FUNCTION public.to_number(s text) RETURNS numeric LANGUAGE sql IMMUTABLE AS $fn$ SELECT s::numeric $fn$;


CREATE OR REPLACE VIEW V_TC90_DZH2 AS
SELECT A.AAE00M,
       T.BAZ001,
       T.BAZ002,
       T.ATC900,
       T.ATB930,
       T.ATB900,
       T.AAB001,
       T.ID,
       T.AAB004,
       T.AAB001_1,
       T.ID_1,
       T.AAB004_1,
       T.AAC001,
       T.AAC003,
       T.AAC002,
       T.Atc90ax,
       T.AAC004,
       T.AAC006,
       T.ATC901,
       T.AAB299,
       T.BAB305,
       T.AAC017,
       T.AAC011,
       T.AAC024,
       T.AAE006,
       T.AAC009,
       T.AAC005,
       T.AAC033,
       T.AAE005,
       T.AAC181,
       T.AAC180,
       T.AAC182,
       T.AAC183,
       T.AAC007,
       T.AAC015,
       T.AAC184,
       T.ACA111,
       T.ACA112,
       T.AAC014,
       T.BAC014,
       T.AAC185,
       T.AAC025,
       T.AAE008,
       T.AAE00D,
       T.ATC902,
       T.ATC90R,
       (select max(atc978) from tc97 where atc977='1' and aac002 =t.aac002) AAE009,
       T.AAE00A,
       T.AAC013,
       T.AAE00P,
       T.ATC903,
       T.ATC904,
       T.ATC905,
       T.ATC907,
       T.ATC908,
       T.ATC909,
       T.ATC90A,
       T.ATC90B,
       T.ATC90C,
       T.ATC90D,
       T.ATC90E,
       T.ATB904,
       T.ATC90F,
       T.ATC90G,
       T.ATC90H,
       T.AAA001,
       T.AAA002,
       T.AAA005,
       T.AAA004,
       T.AAA100,
       T.AAA101,
       T.AAA110,
       T.AAA111,
       T.AAA120,
       T.AAA121,
       T.AAA201,
       T.AAA202,
       CASE
         WHEN T.AAA301 IS NOT NULL THEN
          FUN_缴费基数(PRM_BX     => '1',
                   PRM_AAE002 => TO_CHAR(now(), 'yyyymm'),
                   PRM_AAA100 => T.AAA100,
                   PRM_JS     => T.ATC905)
         ELSE
          NULL
       END AAA301,
       CASE
         WHEN T.AAA302 IS NOT NULL THEN
          FUN_缴费基数(PRM_BX     => '2',
                   PRM_AAE002 => TO_CHAR(now(), 'yyyymm'),
                   PRM_AAA100 => T.AAA110,
                   PRM_JS     => T.AAA302)
         ELSE
          NULL
       END AAA302,
       CASE
         WHEN T.AAA303 IS NOT NULL THEN
          FUN_缴费基数(PRM_BX     => '3',
                   PRM_AAE002 => TO_CHAR(now(), 'yyyymm'),
                   PRM_AAA100 => T.AAA120,
                   PRM_JS     => T.AAA303)
         ELSE
          NULL
       END AAA303,
       T.AAA304,
       CASE
         WHEN T.AAA305 IS NOT NULL THEN
          FUN_缴费基数(PRM_BX     => '4',
                   PRM_AAE002 => TO_CHAR(now(), 'yyyymm'),
                   PRM_AAA100 => T.AAA130,
                   PRM_JS     => T.AAA305)
         ELSE
          NULL
       END AAA305,
       T.AAA306,
       T.ATB90S,
       T.AAA401,
       T.AAA402,
       T.AAA403,
       T.AAA404,
       T.AAA405,
       T.AAA406,
       T.AAA407,
       T.BBB001,
       T.BBB002,
       T.AAE100,
       T.AAE011,
       T.AAE017,
       T.AAE036,
       T.AAE019,
       T.AAE020,
       T.AAE022,
       T.ATC90I,
       T.ATC90J,
       T.ATC90K,
       T.ATC90L,
       T.ATC90M,
       T.ATC90N,
       T.ATC90O,
       T.AAE013,
       T.BAE011,
       T.BAE017,
       T.BAE036,
       T.BAE019,
       T.BAE020,
       T.BAE022,
       T.CCC001,
       T.ATC90X,
       T.ATB903,
       T.ATC90P,
       T.ATC90PA,
       T.ATC90Q,
       T.ATC90T1,
       T.ATC90T2,
       T.ATC90T3,
       T.ATC90T4,
       T.ATC90T5,
       T.ATC90T6,
       T.ATC90U,
       T.ATB910,
       T.ATB90R,
       T.AAA501,
       T.AAA502,
       T.AAA601,
       T.AAA602,
       T.ATC90W,
       T.ATC9V1,
       T.ATB90AB,
       T.ATC90AA,
       T.ATC90AB,
       T.ATC90AC,
       T.ATC90AD,
       T.ATC90AE,
       T.ATC90AF,
       T.ATC90AG,
       T.ATC90AH,
       T.ATC90AI,
       T.ATC90AJ,
       T.ATC90AK,

       T.ATC90AL,
       T.ATC90AM,
       T.ATC90AN,
       T.ATC90AO,
       T.ATC90AP,
       T.ATC90AQ,
       T.ATC90AR,
       T.ATB90AD,
       T.ATC90AS,
       T.ATC90AT,
       T.ATC90AU,
       T.ATC90AV,
       T.AAE00B,
       T.ATC9WC,
       T.ATC90AW,
       T.AAC012,
       B.ATB931,
       to_char(t.aae036,'yyyymm') ny,
       T.AAA203,
       T.AAA204,
       T.AAA503,
       T.AAA504,
       T.AAA603,
       T.AAA604,
       T.Zynum,
       T.Jynum,
       t.aac008,
       d.aaa030,
       d.aaa031,
       d.bae011 zzz011,
       t.atc90pb,
       t.aaa901,
       t.aaa902,
       t.aaa903,
       t.aaa904,
       t.aaa908,
       t.aaa909,
       t.aaa130,
       t.aaa131,
       t.aaa009,
       (SELECT AAB00E FROM AB01 WHERE aab001 = T.aab001) AAB00E,
        
       (CASE WHEN t.atb90r='3' THEN t.aae002 WHEN t.atb90r='5' THEN t.aae003 ELSE NULL END) ny2,
       (SELECT AAB00A FROM AB01 WHERE aab001 = T.aab001) AAB00A,
       (select case when count(1)>0 then '有' else '无' end from tc90_zz where aac002=T.aac002 ) as atc90zz,
       (select case when count(1)>0 then '有' else '无' end from tc98 where atc970 in (select atc970 from tc97 where aac001=T.aac001) and  atc985='1')as atc980

  FROM AC01 A, TC90 T, TB93 B, TB90 C,AB01 D
 WHERE A.AAC001 = T.AAC001
   AND T.ATB930 = B.ATB930
   AND C.ATB900 = T.ATB900
   AND t.aab001=d.aab001;

-- Oracle instr(str,sub) 等价: PG strpos 同语义(1起, 无匹配=0)
CREATE OR REPLACE FUNCTION public.instr(str text, sub text) RETURNS int
LANGUAGE sql IMMUTABLE AS $fn$ SELECT strpos(str, sub) $fn$;


-- Oracle instr(str,sub) 等价(PG strpos 同语义)
CREATE OR REPLACE FUNCTION public.instr(str text, sub text) RETURNS int
LANGUAGE sql IMMUTABLE AS $fn$ SELECT strpos(str, sub) $fn$;

-- Oracle MONTHS_BETWEEN 整月近似(部分月小数未复刻, 当前消费方仅做 <13 等整月比较)
CREATE OR REPLACE FUNCTION public.months_between(d1 date, d2 date) RETURNS numeric
LANGUAGE sql IMMUTABLE AS $fn$
SELECT (EXTRACT(YEAR FROM d1)*12 + EXTRACT(MONTH FROM d1)) - (EXTRACT(YEAR FROM d2)*12 + EXTRACT(MONTH FROM d2)) $fn$;

-- Oracle SUBSTRB(按字节, UTF8 下近似按字符) 
CREATE OR REPLACE FUNCTION public.substrb(s text, pos int, len int) RETURNS text
LANGUAGE sql IMMUTABLE AS $fn$
SELECT CASE WHEN s IS NULL THEN NULL WHEN pos < 1 THEN substr(s, 1, greatest(len + pos - 1, 0)) ELSE substr(s, pos, len) END $fn$;

-- V_TC90_HTBA: Oracle user_views 原文, DECODE→CASE(scripts/decode_to_case.py, IS NOT DISTINCT FROM 同 NULL=NULL 语义)

CREATE OR REPLACE VIEW V_TC90_HTBA AS
SELECT A.AAA030, --险金户主键
       A.AAA031, --险金户头名称
       B.ATC900, --流水号
       SUBSTR((CASE
                WHEN B.ATC90F = '1' THEN
                 '新订'
                WHEN B.ATC90F = '2' THEN
                 '续订'
                WHEN B.ATC90F = '3' THEN
                 '补订'
                ELSE
                 '新订'
              END),
              1,
              10) GTZT,
       B.ATB90RA,
       B.AAE002 AAA701,
       B.AAE003 AAA702,
       B.AAC001,
       B.AAC002,
       B.AAC003,
       (CASE
         WHEN AAC005 IS NULL THEN
          '汉族'
         ELSE
          (SELECT AAA103
             FROM AA10
            WHERE AAA102 = B.AAC005
              AND AAA100 = 'AAC005')
       END) AAC005,
       (CASE WHEN (AAC011) IS NOT DISTINCT FROM ('11') THEN ('博士') WHEN (AAC011) IS NOT DISTINCT FROM ('14') THEN ('硕士') WHEN (AAC011) IS NOT DISTINCT FROM ('21') THEN ('大学') WHEN (AAC011) IS NOT DISTINCT FROM ('31') THEN ('大专') WHEN (AAC011) IS NOT DISTINCT FROM ('41') THEN ('中专中技') WHEN (AAC011) IS NOT DISTINCT FROM ('47') THEN ('技校') WHEN (AAC011) IS NOT DISTINCT FROM ('61') THEN ('高中') WHEN (AAC011) IS NOT DISTINCT FROM ('44') THEN ('职高') WHEN (AAC011) IS NOT DISTINCT FROM ('71') THEN ('初中') WHEN (AAC011) IS NOT DISTINCT FROM ('81') THEN ('小学') ELSE ('其他') END) AAC011,
       (CASE
         WHEN B.AAE006 IS NULL THEN
          '长春市'
         ELSE
          B.AAE006
       END) BAB305,
       SUBSTR('130000', 1, 20) AAE008,
       (CASE
         WHEN B.AAE006 IS NULL THEN
          '长春市'
         ELSE
          B.AAE006
       END) AAE006,
       (CASE WHEN (B.AAC009) IS NOT DISTINCT FROM ('30') THEN ('台港澳人员') WHEN (B.AAC009) IS NOT DISTINCT FROM ('40') THEN ('外国人') ELSE ('居民户口（含农业户口、非农业户口）') END) AAC009,
       SUBSTR('办事人员和有关人员', 1, 50) ATC908,
       '是' IFZG,
       CASE WHEN (B.AAC012) IS NOT DISTINCT FROM ('27') THEN ('是') ELSE ('否') END IFNMG,
       TO_CHAR(B.ATC90B, 'yyyy-mm-dd') AAC007,
       TO_CHAR(B.ATC90B, 'yyyy-mm-dd') ATC90B,
       '是' IFQDHT,
       (SELECT AAA103
          FROM AA10
         WHERE AAA102 = B.ATC90A
           AND AAA100 = 'ATC90A') ATC90A,
       (CASE
         WHEN ATC90D IS NULL THEN
          '10年以上'
         WHEN MONTHS_BETWEEN(ATC90D, ATC90C) < 13 THEN
          '1年及其以下'
         WHEN MONTHS_BETWEEN(ATC90D, ATC90C) < 37 THEN
          '1至3年(含3年)'
         WHEN MONTHS_BETWEEN(ATC90D, ATC90C) < 61 THEN
          '3至5年(含5年)'
         WHEN MONTHS_BETWEEN(ATC90D, ATC90C) < 121 THEN
          '5至10年(含10年)'
         ELSE
          '10年以上'
       END) HTQX,
       B.ATC90C HTKSRQ,
       B.ATC90D HTJSRQ,
       SUBSTR('劳务派遣', 1, 50) ZGLB,
       SUBSTR('全日制', 1, 50) YGXS,
       B.ATC90C YGKSRQ,
       B.ATC90D YGJSRQ,
       '' SYKSRQ,
       '' SYJSRQ,
       '' HTXQRQ,
       '' IFYANGL,
       '' IFSHIY,
       '' IFGONGS,
       '' IFYIL,
       '' IFSHENGY,
       B.AAE005,
       SUBSTR('劳务派遣合同书', 1, 50) HTMM,
       SUBSTRB(A.AAB004, 1, 25) GZDD, --工作地点
       '无' GZBZ, --工作应达到的标准
       C.ATB90AB, --工资支付日期
       '' YX, --月工资（元）
       '' YXFP, --工资分配制度
       SUBSTR('标准工时工作制', 1, 50) GSZD, --执行工时制度
       '' GZNR, --工作内容
       '' DGSHF, --待工生活费
       '' ZXFS, --执行方式
       '' LDBC, --劳动报酬(元/小时)
       '' TGLDTJ, --提供劳动条件
       ATC90C ATB906, --派遣协议起始日期
       ATC90D ATB907, --派遣协议结束日期
       A.AAB004, --用工单位
       '' BJZF, --病假工资支付方式
       '' SYQGZ, --试用期工资（元）
       '' QTFLDY, --其他福利待遇说明
       '' GZYD, --对工资的其他约定
       '' FWQKSRQ, --服务期起始日期
       '' FWQJSRQ, --服务期终止日期
       '' WYJ, --违约金
       '' JYXZKSRQ, --竞业期限限制开始时间
       '' JYXZJSRQ, --竞业期限限制结束时间
       '' JYXZFW, --竞业限制范围
       '' JYXZBC, --竞业限制补偿(元/月)
       '' WFBMXY, --违反保密协议违约金
       '' YDQT, --双方约定的其他内容
       '' FJXY1, --附加协议1
       '' FJXY2, --附加协议2
       '' FJXY3, --附加协议3
       TO_CHAR(B.ATC90I, 'yyyy-mm-dd') ATC90Q,
       (CASE
         WHEN ATC90K = '01' THEN
          '在试用期间被证明不符合录用条件'
          WHEN ATC90K = '02' THEN
          '劳动者被依法追究刑事责任'
          WHEN ATC90K = '03' THEN
          '严重违反用人单位规章制度'
          WHEN ATC90K = '04' THEN
          '工作调转'
          WHEN ATC90K = '07' THEN
          '劳动合同到期,本人提出不续签'
           WHEN ATC90K = '08' THEN
          '劳动合同到期,单位提出不续签'
           WHEN ATC90K = '09' THEN
          '职工个人原因'
           WHEN ATC90K = '10' THEN
          '用人单位裁员'
           WHEN ATC90K = '11' THEN
          '关闭破产'
           WHEN ATC90K = '12' THEN
          '改制'
           WHEN ATC90K = '13' THEN
          '隶属关系转移'
           WHEN ATC90K = '14' THEN
          '双方协商一致本人解除'
           WHEN ATC90K = '15' THEN
          '双方协商一致单位提出解除'
           WHEN ATC90K = '16' THEN
          '退休'
           WHEN ATC90K = '17' THEN
          '死亡'
         ELSE
          '其他'
       END) ATC90K,
       '' SBYY, --失败原因
       B.ATB90R,
       B.ATC9WC,
       B.AAA110,
       B.AAA111,
       B.AAE00P,
       atc90p,atc90pa,atc90w,b.aaa100,b.aaa101
--alter view v_tc90_htba add constraint pk_v_tc90_htba primary key(atc900) disable novalidate;
  FROM AB01 A, TB93 C, TC90 B
 WHERE A.AAB001 = B.AAB001
   AND C.atb930 = B.atb930;
