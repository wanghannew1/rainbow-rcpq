-- V8: V6 视图修复(Oracle 导出全角标点致 CREATE 静默失败; sysdate/MINUS/decode/to_char 方言清洗)
-- 生成: scripts/gen_views_fix.py; 仍挂起视图见 work/pending_views.txt

CREATE OR REPLACE FUNCTION public.to_char(n numeric) RETURNS text LANGUAGE sql IMMUTABLE AS $fn$ SELECT n::text $fn$;

CREATE OR REPLACE FUNCTION public.to_number(s text) RETURNS numeric LANGUAGE sql IMMUTABLE AS $fn$ SELECT s::numeric $fn$;


CREATE OR REPLACE VIEW V_TC93_TC9AA AS
SELECT AAE002,
       ATC937,
       AAB004,
       AAC003,
       AAC002,
       TO_CHAR(ATC93AA) ATC93AA,
       TO_CHAR(ATC93C) ATC93C,
       TO_CHAR(CAA002) CAA002,
       TO_CHAR(ATC9AAB) ATC9AAB,
       ATC93W4,
       ATC93W21,
       ATC93W33,
       LX

  FROM (SELECT TO_NUMBER(ATC9AA2) AAE002,
               '1' ATC937,
               AAB004 AAB004,
               AAC003 AAC003,
               AAC002 AAC002,
               TO_NUMBER(ATC9AA4) ATC93AA,
               TO_NUMBER(ATC9AA6) ATC93C,
               0 CAA002,
               TO_NUMBER(ATC9AAB) ATC9AAB,
               0 ATC93W4,
               0 ATC93W21,
               0 ATC93W33,
               '历史工资' LX
          FROM TC9AA
        UNION
        SELECT AAE002,
               ATC937,
               AAB004,
               AAC003,
               AAC002,
               ATC93AA,
               ATC93C,
               CAA002,
               ATC93K ATC9AAB,
               ATC93W4,
               ATC93W21,
               ATC93W33,
               '系统工资' LX
          FROM TC93
         WHERE AAE214 = '1'
           AND ATC931 >= 202012);
