# 实体 → 表映射清单

> 2026-11-05 生成。来源：549 个 `*.hbm.xml`（含主键定义）。映射机制：Hibernate 3 hbm.xml，实体为纯 POJO（无注解）。

## 1. 总览

| 指标 | 数值 |
|---|---:|
| hbm 映射条目 | 549 |
| 项目实体类（src） | 517（**全部有映射**，0 遗漏） |
| 框架自带实体（unieap，无 src） | 32 |
| 物理表 | 543 |
| 其中视图（V_*） | 82 |
| 复合主键实体 | 56 |
| 一表多实体 | 6（全部为反编译产物 `$_` 类共享父表，重写时忽略 `$_` 类） |

## 2. 表前缀分组

| 前缀 | 实体数 | 推断 |
|---|---:|---|
| TC* | 214 | 合同/就业登记明细（业务主体，量最大） |
| TJ* | 91 | 缴费/基金类 |
| V_*（视图） | 82 | 只读查询视图 |
| TB* | 39 | 个人/单位基础信息 |
| AA* | 35 | 公共代码/单位信息 |
| UP_* | 26 | unieap 框架表 |
| AC* | 11 | 人员参保/账户 |
| CA* | 9 | — |
| WTS* | 7 | wts 外网申报 |
| TD* | 6 | 实体类 td 前缀 |
| AB* | 5 | — |
| SYS* | 5 | 系统配置 |
| JF* | 4 | 缴费明细 |
| YTH* | 4 | 一体化平台 |
| AT7* | 3 | 附件类 |
| CC* | 3 | 存储过程包装/配置 |
| WBS* | 1 | — |
| USER* | 1 | — |
| CBJ* | 1 | — |
| CLIENT* | 1 | — |
| CE1* | 1 | — |

## 3. 复合主键实体

| 实体 | 表 | 主键列 |
|---|---|---|
| AA10 | AA10 | AAA100, AAA102 |
| Administrator | UP_ORG_USER_ADMINROLE | ROLE_ID, USER_ID |
| BusinessOperator | UP_ORG_USER_BUSIROLE | ROLE_ID, USER_ID |
| BusinessRoleInUnit | UP_ORG_BUSIROLE_UNIT | ROLE_ID, UNIT_ID |
| CategoriedResource | UP_RES_CATEGORIEDRES | CATEGORY_ID, RESOURCE_ID, RESOURCE_TYPE |
| ExclusionRole | UP_ORG_ROLE_EXROLE | ROLE_ONE_ID, ROLE_TWO_ID |
| ExclusionStation | UP_ORG_STATION_EXSTATION | STATION_ONE_ID, STATION_TWO_ID |
| ManagedBusinessRole | UP_ORG_ROLE_ROLE | ADMIN_ROLE_ID, BUSINESS_ROLE_ID |
| ManagedCategory | UP_RES_ROLE_CATEGORY | ROLE_ID, CATEGORY_ID |
| ManagedDynamicRole | UP_ORG_ROLE_ROLE_2 | ADMIN_ROLE_ID, DYNAMIC_ROLE_ID |
| ManagedUnit | UP_ORG_ROLE_UNIT | ROLE_ID, UNIT_ID |
| MultidimensionalCategory | UP_RES_DIM_CATEGORY | DIMENSION_ID, CATEGORY_ID, PARENT_CATEGORY_ID |
| MultidimensionalUnit | UP_ORG_DIMENSION_UNIT | UNIT_ID, DIMENSION_ID |
| StationedUser | UP_ORG_USER_STATION | STATION_ID, USER_ID |
| TC0A_BP01 | TC0A_BP01 | ATB930, ATC900 |
| TC90_INNER | TC90_INNER | ATC900, FLAG |
| TC93_TMP | TC93_TMP | ATC930, AAC002, AAC003, STR, KEY |
| TC9W_BP05 | TC9W_BP05 | AAE002, ATC900, ABP001 |
| TJ017 | TJ017 | ID, ATJ0171, TABLENAME |
| TJ02 | TJ02 | ATC930, ATB930, ATC932, ATC937, AAE002, TJ02LB |
| TJ04 | TJ04 | ATB930, ATC932, ATC937 |
| TJ05 | TJ05 | AAE002, ATB930 |
| TJ06 | TJ06 | AAE002, ATC900 |
| TJ0A | TJ0A | AAE002, ATB930, ATJ000 |
| TJ0A1 | TJ0A1 | AAE002, ATC900, ATJ000 |
| TJ0B | TJ0B | AAE002, ATB930, ATJ000 |
| TJ0B1 | TJ0B1 | AAE002, ATC900, ATJ000 |
| TJ0B2 | TJ0B2 | AAE002, ATC900, AAE003 |
| TJ0B3 | TJ0B3 | AAE002, ATC900, AAE003 |
| TJ0C | TJ0C | AAE002, AAB004, ATJ000 |
| TJ0C1 | TJ0C1 | AAE002, ATC900, ATJ000 |
| TJ0C2 | TJ0C2 | AAE002, ATC900 |
| TJ0D | TJ0D | AAE002, ATB930, ATJ000 |
| TJ0D1 | TJ0D1 | AAE002, ATC900, ATJ000 |
| TJ0E | TJ0E | AAE002, ATB930, ATJ000 |
| TJ0E1 | TJ0E1 | AAE002, ATC900, ATJ000 |
| TJ0F | TJ0F | AAE002, ATB930, AAA020, ATJ000 |
| TJ0F1 | TJ0F1 | AAE002, ATC900, ATJ000 |
| TJ0G | TJ0G | AAE002, AAB004, ATJ000 |
| TJ0G1 | TJ0G1 | AAE002, ATC900, ATJ000 |
| TJ0G2 | TJ0G2 | AAE002, ATC900 |
| TJ0Z | TJ0Z | AAE002, ATB930, TTYPE |
| TJ0Z4 | TJ0Z4 | AAE002, ATJ001, ATB930 |
| UserInUnit | UP_ORG_USER_UNIT | USER_ID, UNIT_ID |
| V_CBJ | V_CBJ | ATB930, AAE001 |
| V_DW_EXPIRE | V_DW_EXPIRE | ATB930, AAE002 |
| V_DW_EXPIRE_TX | V_DW_EXPIRE_TX | AAE002, AAB001_1 |
| V_INVOICE_TC93 | V_INVOICE_TC93 | ATB930, ATC937, AAE002 |
| V_TC93 | V_TC93 | ATB930, AAB004, ATC931, ATC937 |
| V_TC93_AVG | V_TC93_AVG | AAC002, AAC003, ATC931, ATC933, ATC933_AVG |
| V_TC93_BALANCE | V_TC93_BALANCE | ATB930, ATC937, ATC931 |
| V_TC93_TC9AA | V_TC93_TC9AA | AAE002, AAB004, AAC003, AAC002 |
| V_TC93_YZS | V_TC93_YZS | ATB900, ATC932 |
| V_TD04_TB93 | V_TD04_TB93 | ATB930, AAE002 |
| V_TJ02 | V_TJ02 | ATC930, ATB930, ATC932, ATC937, AAE002, TJ02LB |
| WTS_LAYERS | WTS_LAYERS | LAYER_ID, LAYER_TID |

## 4. 完整映射表（549 条）

按表名字母序。`$_` 结尾为反编译辅助类（与父类同表，重写忽略）。

| 实体 | 表 | 主键 | 视图 | 目录 |
|---|---|---|---|---|
| AA01 | AA01 | AAA010 |  | baseDB/A |
| AA03 | AA03 | AAA030 |  | baseDB/A |
| AA04 | AA04 | AAA040 |  | baseDB/A |
| AA05 | AA05 | ID |  | baseDB/A |
| AA10 | AA10 | AAA100, AAA102 |  | baseDB/A |
| AA10_XAEG_CONT | AA10_XAEG_CONT | AAAA10 |  | baseDB/A |
| AA11 | AA11 | AAA110 |  | baseDB/A |
| AA12 | AA12 | ID |  | baseDB/A |
| AA70 | AA70 | AAA700 |  | baseDB/A |
| AA71 | AA71 | AAA710 |  | baseDB/A |
| AA72 | AA72 | AAA720 |  | baseDB/A |
| AA73 | AA73 | AAA730 |  | baseDB/A |
| AA74 | AA74 | AAA740 |  | baseDB/A |
| AA75 | AA75 | AAA750 |  | baseDB/A |
| AA76 | AA76 | AAA760 |  | baseDB/A |
| AA77 | AA77 | AAA770 |  | baseDB/A |
| AA78 | AA78 | AAA780 |  | baseDB/A |
| AA80 | AA80 | AAA800 |  | baseDB/A |
| AA81 | AA81 | AAA810 |  | baseDB/A |
| AA82 | AA82 | AAA820 |  | baseDB/A |
| AA83 | AA83 | ID |  | baseDB/A |
| AA90 | AA90 | AAA900 |  | baseDB/A |
| AA91 | AA91 | AAA910 |  | baseDB/A |
| AA92 | AA92 | AAA921 |  | baseDB/A |
| AA93 | AA93 | AAA930 |  | baseDB/A |
| AA94 | AA94 | AAA940 |  | baseDB/A |
| AA95 | AA95 | AAA950 |  | baseDB/A |
| AA96 | AA96 | AAA960 |  | baseDB/A |
| AA97 | AA97 | AAA970 |  | baseDB/A |
| AA97_BG | AA97_BG | ID |  | baseDB/A |
| AA97_BP | AA97_BP | ID |  | baseDB/A |
| AA97_DW | AA97_DW | FID |  | baseDB/A |
| AA97_GR | AA97_GR | CID |  | baseDB/A |
| AA98 | AA98 | ID |  | baseDB/A |
| AA99 | AA99 | AAA990 |  | baseDB/A |
| AB01 | AB01 | AAB001 |  | baseDB/A |
| AB01_CHILD | AB01_CHILD | SEQ |  | baseDB/A |
| AB01_LB | AB01_LB | AAB001 |  | baseDB/A |
| AB06 | AB06 | ID |  | baseDB/A |
| AB07 | AB07 | ID |  | baseDB/A |
| AC01 | AC01 | AAC001 |  | baseDB/A |
| AC01_BP | AC01_BP | ID |  | baseDB/A |
| AC01_GX | AC01_GX | ID |  | baseDB/A |
| AC01_LB | AC01_LB | AAC001 |  | baseDB/A |
| AC05 | AC05 | ID |  | baseDB/A |
| AC06 | AC06 | ID |  | baseDB/A |
| AC07 | AC07 | ID |  | baseDB/A |
| AC0B | AC0B | AAC0B0 |  | baseDB/A |
| AC0D | AC0D | AAC0D0 |  | baseDB/A |
| AC10 | AC10 | ID |  | baseDB/A |
| AC10_JS | AC10_JS | ID |  | baseDB/A |
| AT70 | AT70 | AAT700 |  | baseDB/A |
| AT71 | AT71 | AAT710 |  | baseDB/A |
| AT72 | AT72 | ATA720 |  | baseDB/A |
| CA05 | CA05 | ACA050 |  | baseDB/C |
| CA11 | CA11 | ACA110 |  | baseDB/C |
| CA111 | CA111 | ACA1110 |  | baseDB/C |
| CA112 | CA112 | ACA1120 |  | baseDB/C |
| CA13 | CA13 | ID |  | baseDB/C |
| CA14 | CA14 | ID |  | baseDB/C |
| CA16 | CA16 | ACA160 |  | baseDB/C |
| CA18 | CA18 | ID |  | baseDB/C |
| CA21 | CA21 | ACA210 |  | baseDB/C |
| CBJ_BP | CBJ_BP | ID |  | baseDB/C |
| CC0A | CC0A | AAC002 |  | baseDB/C |
| CC0P | CC0P | ACC0P0 |  | baseDB/C |
| CC78 | CC78 | ACC780 |  | baseDB/C |
| CE10 | CE10 | ACE100 |  | baseDB/C |
| CLIENT_PAD | CLIENT_PAD | CLIENTPAD |  | baseDB/C |
| JFSJ | JFSJ | ID |  | baseDB/J |
| JFSJ01 | JFSJ01 | ID |  | baseDB/J |
| JFSJ02 | JFSJ02 | ID |  | baseDB/J |
| JFSJ_MX | JFSJ_MX | ID |  | baseDB/J |
| SYSLOB | SYSLOB | SYSLOBID |  | baseDB/S |
| SYS_FILE | SYS_FILE | ID |  | baseDB/S |
| SYS_ORG_USER_UNIT | SYS_ORG_USER_UNIT | AAE011 |  | baseDB/S |
| SYS_QUESTION | SYS_QUESTION | QSEQ |  | baseDB/S |
| SYS_UNIT_TREE | SYS_UNIT_TREE | ID |  | baseDB/S |
| TB10 | TB10 | ATB100 |  | baseDB/T |
| TB11 | TB11 | ATB110 |  | baseDB/T |
| TB12 | TB12 | ATB120 |  | baseDB/T |
| TB13$_ONLY | TB13 | ATB130 |  | baseDB/T |
| TB13 | TB13 | ATB130 |  | baseDB/T |
| TB14 | TB14 | ATB140 |  | baseDB/T |
| TB15 | TB15 | ATB150 |  | baseDB/T |
| TB16 | TB16 | ATB160 |  | baseDB/T |
| TB17 | TB17 | ATB170 |  | baseDB/T |
| TB18 | TB18 | ATB180 |  | baseDB/T |
| TB19 | TB19 | ATB190 |  | baseDB/T |
| TB70 | TB70 | ATB700 |  | baseDB/T |
| TB71 | TB71 | ATB710 |  | baseDB/T |
| TB7Z | TB7Z | ATB7Z0 |  | baseDB/T |
| TB84 | TB84 | ATB840 |  | baseDB/T |
| TB86 | TB86 | ATB860 |  | baseDB/T |
| TB87 | TB87 | ATB870 |  | baseDB/T |
| TB88 | TB88 | ATB880 |  | baseDB/T |
| TB89 | TB89 | ATB890 |  | baseDB/T |
| TB8A | TB8A | ATB8A0 |  | baseDB/T |
| TB90 | TB90 | ATB900 |  | baseDB/T |
| TB90_CONTACTS | TB90_CONTACTS | ID |  | baseDB/T |
| TB91 | TB91 | ATB910 |  | baseDB/T |
| TB92 | TB92 | ATB920 |  | baseDB/T |
| TB93 | TB93 | ATB930 |  | baseDB/T |
| TB93_CBJ | TB93_CBJ | ID |  | baseDB/T |
| TB94 | TB94 | ATB940 |  | baseDB/T |
| TB95 | TB95 | ATB950 |  | baseDB/T |
| TB96 | TB96 | ATB960 |  | baseDB/T |
| TB97 | TB97 | ATB970 |  | baseDB/T |
| TB97_BP | TB97_BP | ID |  | baseDB/T |
| TB97_BP01 | TB97_BP01 | ID |  | baseDB/T |
| TB97_BP02 | TB97_BP02 | ID |  | baseDB/T |
| TB98 | TB98 | ATB980 |  | baseDB/T |
| TB99 | TB99 | ATB990 |  | baseDB/T |
| TB99_BP | TB99_BP | ID |  | baseDB/T |
| TB9A | TB9A | ATB9A0 |  | baseDB/T |
| TB9B | TB9B | ATB9B0 |  | baseDB/T |
| TB9C | TB9C | ATB9C0 |  | baseDB/T |
| TC01 | TC01 | ATC900 |  | baseDB/T |
| TC02 | TC02 | ATC020 |  | baseDB/T |
| TC03 | TC03 | ATC030 |  | baseDB/T |
| TC04 | TC04 | ATC040 |  | baseDB/T |
| TC04_BP | TC04_BP | ID |  | baseDB/T |
| TC05 | TC05 | ATC050 |  | baseDB/T |
| TC05_BP | TC05_BP | ID |  | baseDB/T |
| TC06 | TC06 | ATC060 |  | baseDB/T |
| TC06_BP | TC06_BP | ID |  | baseDB/T |
| TC07 | TC07 | ATC070 |  | baseDB/T |
| TC07_BP | TC07_BP | ID |  | baseDB/T |
| TC07_LIST | TC07_LIST | ID |  | baseDB/T |
| TC08 | TC08 | ATC080 |  | baseDB/T |
| TC09 | TC09 | ATC090 |  | baseDB/T |
| TC09_BP | TC09_BP | ID |  | baseDB/T |
| TC0A | TC0A | ATC0A0 |  | baseDB/T |
| TC0A_BP | TC0A_BP | ID |  | baseDB/T |
| TC0A_BP01 | TC0A_BP01 | ATB930, ATC900 |  | baseDB/T |
| TC10 | TC10 | ATC100 |  | baseDB/T |
| TC11 | TC11 | ATC110 |  | baseDB/T |
| TC12 | TC12 | ATC120 |  | baseDB/T |
| TC13 | TC13 | ATC130 |  | baseDB/T |
| TC13_BP | TC13_BP | ATC130 |  | baseDB/T |
| TC13_PHOTO | TC13_PHOTO | ID |  | baseDB/T |
| TC14 | TC14 | ATC140 |  | baseDB/T |
| TC15 | TC15 | ATC150 |  | baseDB/T |
| TC16 | TC16 | ATC160 |  | baseDB/T |
| TC17 | TC17 | ATC170 |  | baseDB/T |
| TC20 | TC20 | ATC200 |  | baseDB/T |
| TC21 | TC21 | ATC210 |  | baseDB/T |
| TC30 | TC30 | ATC300 |  | baseDB/T |
| TC30_BP | TC30_BP | ID |  | baseDB/T |
| TC30_BP01 | TC30_BP01 | ID |  | baseDB/T |
| TC30_BP02 | TC30_BP02 | ID |  | baseDB/T |
| TC30_BP03 | TC30_BP03 | ID |  | baseDB/T |
| TC30_BP04 | TC30_BP04 | ID |  | baseDB/T |
| TC31 | TC31 | ATC310 |  | baseDB/T |
| TC31_BP | TC31_BP | ID |  | baseDB/T |
| TC32 | TC32 | ATC320 |  | baseDB/T |
| TC32_BP | TC32_BP | ID |  | baseDB/T |
| TC40 | TC40 | ATC400 |  | baseDB/T |
| TC70 | TC70 | ATC700 |  | baseDB/T |
| TC71 | TC71 | ATC710 |  | baseDB/T |
| TC72 | TC72 | ATC720 |  | baseDB/T |
| TC72$_TC73 | TC72 | ATC720 |  | baseDB/T |
| TC73 | TC73 | ATC700 |  | baseDB/T |
| TC74 | TC74 | ATC740 |  | baseDB/T |
| TC76 | TC76 | ATC760 |  | baseDB/T |
| TC77 | TC77 | ATC770 |  | baseDB/T |
| TC78 | TC78 | ATC780 |  | baseDB/T |
| TC79 | TC79 | ATC790 |  | baseDB/T |
| TC7A | TC7A | ATC7A0 |  | baseDB/T |
| TC7B | TC7B | ATC7B0 |  | baseDB/T |
| TC7C | TC7C | ATC7C0 |  | baseDB/T |
| TC7D | TC7D | ATC7D0 |  | baseDB/T |
| TC7E | TC7E | ATC7E0 |  | baseDB/T |
| TC7F | TC7F | ATC7F0 |  | baseDB/T |
| TC7G | TC7G | ATC7G0 |  | baseDB/T |
| TC7H | TC7H | ATC7H0 |  | baseDB/T |
| TC7I | TC7I | ATC7I0 |  | baseDB/T |
| TC7J | TC7J | ATC7J0 |  | baseDB/T |
| TC7K | TC7K | ATC7K0 |  | baseDB/T |
| TC7L$_ONLY | TC7L | ATC7L0 |  | baseDB/T |
| TC7L | TC7L | ATC7L0 |  | baseDB/T |
| TC7M | TC7M | ATC7M0 |  | baseDB/T |
| TC7M$_1 | TC7M | ATC7M0 |  | baseDB/T |
| TC7N | TC7N | ATC7N0 |  | baseDB/T |
| TC7O | TC7O | ATC7O0 |  | baseDB/T |
| TC7P | TC7P | ATC7P0 |  | baseDB/T |
| TC7Q | TC7Q | ATC7Q0 |  | baseDB/T |
| TC7R | TC7R | ATC7R0 |  | baseDB/T |
| TC7S | TC7S | ATC7S0 |  | baseDB/T |
| TC7V | TC7V | ATC7V0 |  | baseDB/T |
| TC7W$_ONLY | TC7W | ATC7W0 |  | baseDB/T |
| TC7W | TC7W | ATC7W0 |  | baseDB/T |
| TC7X | TC7X | ATC7X0 |  | baseDB/T |
| TC7Y | TC7Y | ATC7Y0 |  | baseDB/T |
| TC7Z$_ONLY | TC7Z | ATC7Z0 |  | baseDB/T |
| TC7Z | TC7Z | ATC7Z0 |  | baseDB/T |
| TC83 | TC83 | ATC830 |  | baseDB/T |
| TC84 | TC84 | ATC840 |  | baseDB/T |
| TC85 | TC85 | ATC850 |  | baseDB/T |
| TC86 | TC86 | ATC860 |  | baseDB/T |
| TC87 | TC87 | ATC870 |  | baseDB/T |
| TC88 | TC88 | ATC880 |  | baseDB/T |
| TC89 | TC89 | ATC890 |  | baseDB/T |
| TC8A | TC8A | ATC8A0 |  | baseDB/T |
| TC8B | TC8B | ATC8B0 |  | baseDB/T |
| TC8C | TC8C | ATC8C0 |  | baseDB/T |
| TC8D | TC8D | ATC8D0 |  | baseDB/T |
| TC8E | TC8E | ATC8E0 |  | baseDB/T |
| TC8F | TC8F | ATC8F0 |  | baseDB/T |
| TC8G | TC8G | ATC8G0 |  | baseDB/T |
| TC8G_BP | TC8G_BP | ATC8G0 |  | baseDB/T |
| TC8H | TC8H | ATC8H0 |  | baseDB/T |
| TC8I | TC8I | ATC8I0 |  | baseDB/T |
| TC8I_LIST | TC8I_LIST | ID |  | baseDB/T |
| TC8J | TC8J | ATC8J0 |  | baseDB/T |
| TC8K | TC8K | ATC8K0 |  | baseDB/T |
| TC8L | TC8L | ATC8L0 |  | baseDB/T |
| TC8M | TC8M | ATC8M0 |  | baseDB/T |
| TC8N | TC8N | ATC8N0 |  | baseDB/T |
| TC8P | TC8P | ATC8P0 |  | baseDB/T |
| TC8Q | TC8Q | ATC8Q0 |  | baseDB/T |
| TC8R | TC8R | ATC8R0 |  | baseDB/T |
| TC8S | TC8S | ATC8S0 |  | baseDB/T |
| TC8T | TC8T | ATC8T0 |  | baseDB/T |
| TC8U | TC8U | ATC8U0 |  | baseDB/T |
| TC8UMX | TC8UMX | ID |  | baseDB/T |
| TC8U_CS | TC8U_CS | ID |  | baseDB/T |
| TC8V | TC8V | ATC8V0 |  | baseDB/T |
| TC8W | TC8W | ATC8W0 |  | baseDB/T |
| TC8Y | TC8Y | ATC8Y0 |  | baseDB/T |
| TC8Y_LIST | TC8Y_LIST | ID |  | baseDB/T |
| TC8Z | TC8Z | ID |  | baseDB/T |
| TC90 | TC90 | ATC900 |  | baseDB/T |
| TC90_BG | TC90_BG | ID |  | baseDB/T |
| TC90_BP | TC90_BP | ID |  | baseDB/T |
| TC90_BP1 | TC90_BP1 | ID |  | baseDB/T |
| TC90_BP2 | TC90_BP2 | ID |  | baseDB/T |
| TC90_BP3 | TC90_BP3 | ID |  | baseDB/T |
| TC90_BP4 | TC90_BP4 | ID |  | baseDB/T |
| TC90_IMP | TC90_IMP | ID |  | baseDB/T |
| TC90_INNER | TC90_INNER | ATC900, FLAG |  | baseDB/T |
| TC90_JFJS | TC90_JFJS | ID |  | baseDB/T |
| TC90_JFJS_IMP | TC90_JFJS_IMP | ID |  | baseDB/T |
| TC90_LIST | TC90_LIST | IKEY |  | baseDB/T |
| TC90_OUTTER | TC90_OUTTER | ID |  | baseDB/T |
| TC90_QDLS | TC90_QDLS | ID |  | baseDB/T |
| TC90_TMP | TC90_TMP | ID |  | baseDB/T |
| TC90_YY | TC90_YY | TC90YYID |  | baseDB/T |
| TC90_ZZ | TC90_ZZ | ID |  | baseDB/T |
| TC91 | TC91 | ATC910 |  | baseDB/T |
| TC91_BP | TC91_BP | ID |  | baseDB/T |
| TC91_LOG | TC91_LOG | ID |  | baseDB/T |
| TC92 | TC92 | ATC920 |  | baseDB/T |
| TC92_LIST | TC92_LIST | ID |  | baseDB/T |
| TC93 | TC93 | ATC930 |  | baseDB/T |
| TC93_BP | TC93_BP | ID |  | baseDB/T |
| TC93_BP1 | TC93_BP1 | ID |  | baseDB/T |
| TC93_BZ | TC93_BZ | ID |  | baseDB/T |
| TC93_CHECK | TC93_CHECK | CHECKID |  | baseDB/T |
| TC93_DB | TC93_DB | ID |  | baseDB/T |
| TC93_INFO | TC93_INFO | ID |  | baseDB/T |
| TC93_LZ | TC93_LZ | ID |  | baseDB/T |
| TC93_PJGZ | TC93_PJGZ | ID |  | baseDB/T |
| TC93_SS | TC93_SS | ID |  | baseDB/T |
| TC93_SSBP | TC93_SSBP | ID |  | baseDB/T |
| TC93_TF | TC93_TF | ATC900 |  | baseDB/T |
| TC93_TMP | TC93_TMP | ATC930, AAC002, AAC003, STR … |  | baseDB/T |
| TC94 | TC94 | ATC940 |  | baseDB/T |
| TC94_LZ | TC94_LZ | ID |  | baseDB/T |
| TC95 | TC95 | ATC950 |  | baseDB/T |
| TC95_BP | TC95_BP | ID |  | baseDB/T |
| TC96 | TC96 | ATC960 |  | baseDB/T |
| TC96_TMP | TC96_TMP | ATC96T |  | baseDB/T |
| TC97 | TC97 | ATC970 |  | baseDB/T |
| TC97_BP | TC97_BP | ID |  | baseDB/T |
| TC97_BP_WYH | TC97_BP_WYH | ID |  | baseDB/T |
| TC97_RECORD | TC97_RECORD | ID |  | baseDB/T |
| TC98 | TC98 | ATC980 |  | baseDB/T |
| TC99 | TC99 | ATC990 |  | baseDB/T |
| TC9A | TC9A | ATC9A0 |  | baseDB/T |
| TC9AA | TC9AA | ID |  | baseDB/T |
| TC9B | TC9B | ATC9B0 |  | baseDB/T |
| TC9C | TC9C | ATC9C0 |  | baseDB/T |
| TC9D | TC9D | ATC9D0 |  | baseDB/T |
| TC9D_FILE | TC9D_FILE | ID |  | baseDB/T |
| TC9E | TC9E | ATC9E0 |  | baseDB/T |
| TC9F | TC9F | ATC9F0 |  | baseDB/T |
| TC9F_BP | TC9F_BP | ID |  | baseDB/T |
| TC9G | TC9G | ATC9G0 |  | baseDB/T |
| TC9H | TC9H | ATC9H0 |  | baseDB/T |
| TC9I | TC9I | ATC9I0 |  | baseDB/T |
| TC9J | TC9J | ATC9J0 |  | baseDB/T |
| TC9K | TC9K | ATC9K0 |  | baseDB/T |
| TC9L | TC9L | ATC9L0 |  | baseDB/T |
| TC9M | TC9M | ATC9M0 |  | baseDB/T |
| TC9N | TC9N | ATC9N0 |  | baseDB/T |
| TC9P | TC9P | ATC9P0 |  | baseDB/T |
| TC9Q | TC9Q | ATC9Q0 |  | baseDB/T |
| TC9Q_GS | TC9Q_GS | ID |  | baseDB/T |
| TC9Q_LOG | TC9Q_LOG | ID |  | baseDB/T |
| TC9R | TC9R | ATC9R0 |  | baseDB/T |
| TC9R_BP | TC9R_BP | ATC9R0 |  | baseDB/T |
| TC9S | TC9S | ATC9S0 |  | baseDB/T |
| TC9T | TC9T | ATC9T0 |  | baseDB/T |
| TC9U | TC9U | ATC9U0 |  | baseDB/T |
| TC9U_BP | TC9U_BP | ID |  | baseDB/T |
| TC9U_BP01 | TC9U_BP01 | ATC9U0 |  | baseDB/T |
| TC9U_BP02 | TC9U_BP02 | ATC9U0 |  | baseDB/T |
| TC9U_BP03 | TC9U_BP03 | ATC9U0 |  | baseDB/T |
| TC9V | TC9V | ATC9V0 |  | baseDB/T |
| TC9V_BP | TC9V_BP | ID |  | baseDB/T |
| TC9W | TC9W | ATC9W0 |  | baseDB/T |
| TC9W1 | TC9W1 | ATC9W0 |  | baseDB/T |
| TC9W2 | TC9W2 | ATC9W0 |  | baseDB/T |
| TC9W3 | TC9W3 | ATC9W0 |  | baseDB/T |
| TC9W4 | TC9W4 | ATC9W0 |  | baseDB/T |
| TC9W_BP | TC9W_BP | ID |  | baseDB/T |
| TC9W_BP01 | TC9W_BP01 | ID |  | baseDB/T |
| TC9W_BP02 | TC9W_BP02 | ID |  | baseDB/T |
| TC9W_BP03 | TC9W_BP03 | ID |  | baseDB/T |
| TC9W_BP04 | TC9W_BP04 | ID |  | baseDB/T |
| TC9W_BP05 | TC9W_BP05 | AAE002, ATC900, ABP001 |  | baseDB/T |
| TC9W_BP06 | TC9W_BP06 | ID |  | baseDB/T |
| TC9W_BP07 | TC9W_BP07 | ID |  | baseDB/T |
| TC9W_BP08 | TC9W_BP08 | ID |  | baseDB/T |
| TC9W_BP09 | TC9W_BP09 | ID |  | baseDB/T |
| TC9W_BP10 | TC9W_BP10 | ID |  | baseDB/T |
| TC9W_HZ | TC9W_HZ | ID |  | baseDB/T |
| TC9X | TC9X | ATC9X0 |  | baseDB/T |
| TC9Y | TC9Y | ATC9Y0 |  | baseDB/T |
| TC9Z | TC9Z | ATC9Z0 |  | baseDB/T |
| TD01 | TD01 | ID |  | baseDB/T |
| TD01_BP | TD01_BP | ID |  | baseDB/T |
| TD02 | TD02 | ID |  | baseDB/T |
| TD03 | TD03 | ATD030 |  | baseDB/T |
| TD04 | TD04 | ATD040 |  | baseDB/T |
| TD04_BP | TD04_BP | ID |  | baseDB/T |
| TJ000 | TJ000 | TJ0000 |  | baseDB/T |
| TJ01 | TJ01 | ID |  | baseDB/T |
| TJ011 | TJ011 | TJ0110 |  | baseDB/T |
| TJ012 | TJ012 | TJ0120 |  | baseDB/T |
| TJ013 | TJ013 | TJ0130 |  | baseDB/T |
| TJ014 | TJ014 | TJ0140 |  | baseDB/T |
| TJ016 | TJ016 | ID |  | baseDB/T |
| TJ017 | TJ017 | ID, ATJ0171, TABLENAME |  | baseDB/T |
| TJ01_EXPENDITURE | TJ01_EXPENDITURE | ID |  | baseDB/T |
| TJ02 | TJ02 | ATC930, ATB930, ATC932, ATC937 … |  | baseDB/T |
| TJ02_CS | TJ02_CS | ID |  | baseDB/T |
| TJ02_MSG | TJ02_MSG | ID |  | baseDB/T |
| TJ031 | TJ031 | TJ0310 |  | baseDB/T |
| TJ032 | TJ032 | TJ0310 |  | baseDB/T |
| TJ04 | TJ04 | ATB930, ATC932, ATC937 |  | baseDB/T |
| TJ05 | TJ05 | AAE002, ATB930 |  | baseDB/T |
| TJ06 | TJ06 | AAE002, ATC900 |  | baseDB/T |
| TJ07 | TJ07 | ATJ070 |  | baseDB/T |
| TJ071 | TJ071 | TJ0710 |  | baseDB/T |
| TJ072 | TJ072 | TJ0720 |  | baseDB/T |
| TJ0A | TJ0A | AAE002, ATB930, ATJ000 |  | baseDB/T |
| TJ0A1 | TJ0A1 | AAE002, ATC900, ATJ000 |  | baseDB/T |
| TJ0B | TJ0B | AAE002, ATB930, ATJ000 |  | baseDB/T |
| TJ0B1 | TJ0B1 | AAE002, ATC900, ATJ000 |  | baseDB/T |
| TJ0B2 | TJ0B2 | AAE002, ATC900, AAE003 |  | baseDB/T |
| TJ0B3 | TJ0B3 | AAE002, ATC900, AAE003 |  | baseDB/T |
| TJ0C | TJ0C | AAE002, AAB004, ATJ000 |  | baseDB/T |
| TJ0C1 | TJ0C1 | AAE002, ATC900, ATJ000 |  | baseDB/T |
| TJ0C2 | TJ0C2 | AAE002, ATC900 |  | baseDB/T |
| TJ0D | TJ0D | AAE002, ATB930, ATJ000 |  | baseDB/T |
| TJ0D1 | TJ0D1 | AAE002, ATC900, ATJ000 |  | baseDB/T |
| TJ0E | TJ0E | AAE002, ATB930, ATJ000 |  | baseDB/T |
| TJ0E1 | TJ0E1 | AAE002, ATC900, ATJ000 |  | baseDB/T |
| TJ0F | TJ0F | AAE002, ATB930, AAA020, ATJ000 |  | baseDB/T |
| TJ0F1 | TJ0F1 | AAE002, ATC900, ATJ000 |  | baseDB/T |
| TJ0G | TJ0G | AAE002, AAB004, ATJ000 |  | baseDB/T |
| TJ0G1 | TJ0G1 | AAE002, ATC900, ATJ000 |  | baseDB/T |
| TJ0G2 | TJ0G2 | AAE002, ATC900 |  | baseDB/T |
| TJ0Z | TJ0Z | AAE002, ATB930, TTYPE |  | baseDB/T |
| TJ0Z1 | TJ0Z1 | TJID |  | baseDB/T |
| TJ0Z2 | TJ0Z2 | TJID |  | baseDB/T |
| TJ0Z3 | TJ0Z3 | AAE002 |  | baseDB/T |
| TJ0Z4 | TJ0Z4 | AAE002, ATJ001, ATB930 |  | baseDB/T |
| TJ201 | TJ201 | TJ2010 |  | baseDB/T |
| TJ202 | TJ202 | TJ2020 |  | baseDB/T |
| TJ203 | TJ203 | TJ2030 |  | baseDB/T |
| TJ204 | TJ204 | TJ2040 |  | baseDB/T |
| TJ205 | TJ205 | TJ2050 |  | baseDB/T |
| TJ206 | TJ206 | TJ2060 |  | baseDB/T |
| TJ207 | TJ207 | TJ2070 |  | baseDB/T |
| TJ208 | TJ208 | TJ2080 |  | baseDB/T |
| TJ209 | TJ209 | TJ2090 |  | baseDB/T |
| TJ210 | TJ210 | TJ2100 |  | baseDB/T |
| TJ211 | TJ211 | TJ2110 |  | baseDB/T |
| TJ212 | TJ212 | TJ2120 |  | baseDB/T |
| TJ841 | TJ841 | TJ8410 |  | baseDB/T |
| TJ842 | TJ842 | TJ8420 |  | baseDB/T |
| TJ843 | TJ843 | TJ8430 |  | baseDB/T |
| TJ844 | TJ844 | TJ8440 |  | baseDB/T |
| TJAA1 | TJAA1 | TJAA10 |  | baseDB/T |
| TJAA2 | TJAA2 | TJAA20 |  | baseDB/T |
| TJAB01 | TJAB01 | TJAB01 |  | baseDB/T |
| TJAC1 | TJAC1 | TJAC10 |  | baseDB/T |
| TJAC2 | TJAC2 | TJAC20 |  | baseDB/T |
| TJAG1 | TJAG1 | TJAG10 |  | baseDB/T |
| TJAJ1 | TJAJ1 | TJAJ10 |  | baseDB/T |
| TJAM1 | TJAM1 | TJAM10 |  | baseDB/T |
| TJAM2 | TJAM2 | TJAM20 |  | baseDB/T |
| TJAM3 | TJAM3 | TJAM30 |  | baseDB/T |
| TJAM4 | TJAM4 | TJAM40 |  | baseDB/T |
| TJCA1 | TJCA1 | TJCA10 |  | baseDB/T |
| TJCA2 | TJCA2 | TJCA20 |  | baseDB/T |
| TJCA3 | TJCA3 | TJCA30 |  | baseDB/T |
| TJCB1 | TJCB1 | TJCB10 |  | baseDB/T |
| TJCC1 | TJCC1 | TJCC10 |  | baseDB/T |
| TJCC2 | TJCC2 | TJCC20 |  | baseDB/T |
| TJCC3 | TJCC3 | TJCC30 |  | baseDB/T |
| TJCC4 | TJCC4 | ID |  | baseDB/T |
| TJDA3 | TJDA3 | TJDA30 |  | baseDB/T |
| TJDA4 | TJDA4 | TJDA40 |  | baseDB/T |
| TJDA5 | TJDA5 | TJDA50 |  | baseDB/T |
| TJDC1 | TJDC1 | TJDC10 |  | baseDB/T |
| TJPQ1 | TJPQ1 | TJPQ10 |  | baseDB/T |
| TJQC1 | TJQC1 | TJQC10 |  | baseDB/T |
| TJQC2 | TJQC2 | TJQC10 |  | baseDB/T |
| TJQC3 | TJQC3 | TJQC10 |  | baseDB/T |
| TJQC4 | TJQC4 | TJQC10 |  | baseDB/T |
| TJQC5 | TJQC5 | TJQC10 |  | baseDB/T |
| TJTK1 | TJTK1 | TJTK10 |  | baseDB/T |
| TJTK2 | TJTK2 | TJTK20 |  | baseDB/T |
| TJTK3 | TJTK3 | TJTK30 |  | baseDB/T |
| AdministrativeRole | UP_ORG_ADMINROLE | ROLE_ID |  | conf/unieap/orgnization/hbm |
| BusinessRole | UP_ORG_BUSINESSROLE | ROLE_ID |  | conf/unieap/orgnization/hbm |
| BusinessRoleInUnit | UP_ORG_BUSIROLE_UNIT | ROLE_ID, UNIT_ID |  | conf/unieap/orgnization/hbm |
| DelegatedRole | UP_ORG_DELEROLE | ROLE_ID |  | conf/unieap/orgnization/hbm |
| Dimension | UP_ORG_DIMENSION | DIMENSION_ID |  | conf/unieap/orgnization/hbm |
| MultidimensionalUnit | UP_ORG_DIMENSION_UNIT | UNIT_ID, DIMENSION_ID |  | conf/unieap/orgnization/hbm |
| DynamicRole | UP_ORG_DYNAMICROLE | ROLE_ID |  | conf/unieap/orgnization/hbm |
| DynamicCondition | UP_ORG_DYNAMIC_CONDITION | CONDITION_ID |  | conf/unieap/orgnization/hbm |
| ResourceAuthority | UP_ORG_RESOURCE_AUTHORITY | RESOURCE_AUTHORITY_ID |  | conf/unieap/orgnization/hbm |
| ExclusionRole | UP_ORG_ROLE_EXROLE | ROLE_ONE_ID, ROLE_TWO_ID |  | conf/unieap/orgnization/hbm |
| ManagedBusinessRole | UP_ORG_ROLE_ROLE | ADMIN_ROLE_ID, BUSINESS_ROLE_ID |  | conf/unieap/orgnization/hbm |
| ManagedDynamicRole | UP_ORG_ROLE_ROLE_2 | ADMIN_ROLE_ID, DYNAMIC_ROLE_ID |  | conf/unieap/orgnization/hbm |
| ManagedUnit | UP_ORG_ROLE_UNIT | ROLE_ID, UNIT_ID |  | conf/unieap/orgnization/hbm |
| ServiceAudit | UP_ORG_SERVICE_AUDIT | AUDIT_ID |  | conf/unieap/orgnization/hbm |
| Station | UP_ORG_STATION | STATION_ID |  | conf/unieap/orgnization/hbm |
| ExclusionStation | UP_ORG_STATION_EXSTATION | STATION_ONE_ID, STATION_TWO_ID |  | conf/unieap/orgnization/hbm |
| Unit | UP_ORG_UNIT | UNIT_ID |  | conf/unieap/orgnization/hbm |
| User | UP_ORG_USER | USER_ID |  | conf/unieap/orgnization/hbm |
| Administrator | UP_ORG_USER_ADMINROLE | ROLE_ID, USER_ID |  | conf/unieap/orgnization/hbm |
| BusinessOperator | UP_ORG_USER_BUSIROLE | ROLE_ID, USER_ID |  | conf/unieap/orgnization/hbm |
| StationedUser | UP_ORG_USER_STATION | STATION_ID, USER_ID |  | conf/unieap/orgnization/hbm |
| UserInUnit | UP_ORG_USER_UNIT | USER_ID, UNIT_ID |  | conf/unieap/orgnization/hbm |
| CategoriedResource | UP_RES_CATEGORIEDRES | CATEGORY_ID, RESOURCE_ID, RESOURCE_TYPE |  | conf/unieap/resourcemanager/hbm |
| Category | UP_RES_CATEGORY | CATEGORY_ID |  | conf/unieap/resourcemanager/hbm |
| MultidimensionalCategory | UP_RES_DIM_CATEGORY | DIMENSION_ID, CATEGORY_ID, PARENT_CATEGORY_ID |  | conf/unieap/resourcemanager/hbm |
| ManagedCategory | UP_RES_ROLE_CATEGORY | ROLE_ID, CATEGORY_ID |  | conf/unieap/resourcemanager/hbm |
| USER_INFO | USER_INFO | USERINFO |  | baseDB/U |
| V_AA12 | V_AA12 | ID | ✓ | baseDB/V |
| V_AA97_AC10 | V_AA97_AC10 | ID | ✓ | baseDB/V |
| V_AA97_NF | V_AA97_NF | AAA971 | ✓ | baseDB/V |
| V_AA97_TC9T | V_AA97_TC9T | ATC9T0 | ✓ | baseDB/V |
| V_AB01 | V_AB01 | AAB001 | ✓ | baseDB/V |
| V_AB01_AA03 | V_AB01_AA03 | AAB001 | ✓ | baseDB/V |
| V_AB01_AB07 | V_AB01_AB07 | ID | ✓ | baseDB/V |
| V_AB01_TB90 | V_AB01_TB90 | ATB900 | ✓ | baseDB/V |
| V_AB01_TB90_TB93 | V_AB01_TB90_TB93 | ATB930 | ✓ | baseDB/V |
| V_AB01_TB93 | V_AB01_TB93 | ATB930 | ✓ | baseDB/V |
| V_AB01_TB96 | V_AB01_TB96 | ATB960 | ✓ | baseDB/V |
| V_AB01_TREE | V_AB01_TREE | AAB001 | ✓ | baseDB/V |
| V_AC01_TC97 | V_AC01_TC97 | ATC970 | ✓ | baseDB/V |
| V_AC10_MX | V_AC10_MX | ID | ✓ | baseDB/V |
| V_AC10_TC90 | V_AC10_TC90 | ID | ✓ | baseDB/V |
| V_CBJ | V_CBJ | ATB930, AAE001 | ✓ | baseDB/V |
| V_CC78_TC9J | V_CC78_TC9J | ACC780 | ✓ | baseDB/V |
| V_DW_EXPIRE | V_DW_EXPIRE | ATB930, AAE002 | ✓ | baseDB/V |
| V_DW_EXPIRE_TX | V_DW_EXPIRE_TX | AAE002, AAB001_1 | ✓ | baseDB/V |
| V_DW_TB90 | V_DW_TB90 | ATB900 | ✓ | baseDB/V |
| V_INVOICE_TC93 | V_INVOICE_TC93 | ATB930, ATC937, AAE002 | ✓ | baseDB/V |
| V_LAST_HT | V_LAST_HT | ATC900 | ✓ | baseDB/V |
| V_SYS_MENU | V_SYS_MENU | NAME | ✓ | baseDB/V |
| V_TB93_AB07 | V_TB93_AB07 | ATB930 | ✓ | baseDB/V |
| V_TB93_TC93 | V_TB93_TC93 | ATC930 | ✓ | baseDB/V |
| V_TB93_TC9T | V_TB93_TC9T | ATC9T0 | ✓ | baseDB/V |
| V_TB96 | V_TB96 | ATB960 | ✓ | baseDB/V |
| V_TB97_GR | V_TB97_GR | ATB970 | ✓ | baseDB/V |
| V_TB98 | V_TB98 | ATB980 | ✓ | baseDB/V |
| V_TB99 | V_TB99 | ATB990 | ✓ | baseDB/V |
| V_TB9A | V_TB9A | ATB9A0 | ✓ | baseDB/V |
| V_TB9A_GR | V_TB9A_GR | ATB9A0 | ✓ | baseDB/V |
| V_TB9A_YCX | V_TB9A_YCX | ATB9A0 | ✓ | baseDB/V |
| V_TC02_TC90 | V_TC02_TC90 | ATC900 | ✓ | baseDB/V |
| V_TC03_TC90 | V_TC03_TC90 | ATC900 | ✓ | baseDB/V |
| V_TC04 | V_TC04 | ATC040 | ✓ | baseDB/V |
| V_TC8F | V_TC8F | ATC8F0 | ✓ | baseDB/V |
| V_TC8I_LIST | V_TC8I_LIST | ID | ✓ | baseDB/V |
| V_TC8P | V_TC8P | ATC8P0 | ✓ | baseDB/V |
| V_TC8T_TC8V | V_TC8T_TC8V | ID | ✓ | baseDB/V |
| V_TC90 | V_TC90 | ATC900 | ✓ | baseDB/V |
| V_TC90_AB01_TB93 | V_TC90_AB01_TB93 | ATC900 | ✓ | baseDB/V |
| V_TC90_ADW | V_TC90_ADW | ATC900 | ✓ | baseDB/V |
| V_TC90_DFGZ | V_TC90_DFGZ | ATC900 | ✓ | baseDB/V |
| V_TC90_DZH | V_TC90_DZH | ATC900 | ✓ | baseDB/V |
| V_TC90_DZH2 | V_TC90_DZH2 | ATC900 | ✓ | baseDB/V |
| V_TC90_HTBA | V_TC90_HTBA | ATC900 | ✓ | baseDB/V |
| V_TC90_LIST | V_TC90_LIST | IKEY | ✓ | baseDB/V |
| V_TC90_MAX | V_TC90_MAX | ATC900 | ✓ | baseDB/V |
| V_TC90_TB90_TB93 | V_TC90_TB90_TB93 | ATC900 | ✓ | baseDB/V |
| V_TC90_TB91 | V_TC90_TB91 | ATB910 | ✓ | baseDB/V |
| V_TC90_TC90LIST | V_TC90_TC90LIST | IKEY | ✓ | baseDB/V |
| V_TC90_TC97 | V_TC90_TC97 | ATC900 | ✓ | baseDB/V |
| V_TC90_XJ | V_TC90_XJ | ATC900 | ✓ | baseDB/V |
| V_TC90_XZGJJ | V_TC90_XZGJJ | ATC900 | ✓ | baseDB/V |
| V_TC90_XZGS | V_TC90_XZGS | ATC900 | ✓ | baseDB/V |
| V_TC90_XZSB | V_TC90_XZSB | ATC900 | ✓ | baseDB/V |
| V_TC90_XZYB_1 | V_TC90_XZYB_1 | ATC900 | ✓ | baseDB/V |
| V_TC90_XZYB_2 | V_TC90_XZYB_2 | ATC900 | ✓ | baseDB/V |
| V_TC90_ZB | V_TC90_ZB | ATC900 | ✓ | baseDB/V |
| V_TC90_ZZ | V_TC90_ZZ | ZZID | ✓ | baseDB/V |
| V_TC91_TC90 | V_TC91_TC90 | ATC910 | ✓ | baseDB/V |
| V_TC93 | V_TC93 | ATB930, AAB004, ATC931, ATC937 | ✓ | baseDB/V |
| V_TC93_AB01 | V_TC93_AB01 | ATC930 | ✓ | baseDB/V |
| V_TC93_AVG | V_TC93_AVG | AAC002, AAC003, ATC931, ATC933 … | ✓ | baseDB/V |
| V_TC93_BALANCE | V_TC93_BALANCE | ATB930, ATC937, ATC931 | ✓ | baseDB/V |
| V_TC93_TC9AA | V_TC93_TC9AA | AAE002, AAB004, AAC003, AAC002 | ✓ | baseDB/V |
| V_TC93_YZS | V_TC93_YZS | ATB900, ATC932 | ✓ | baseDB/V |
| V_TC97_DA | V_TC97_DA | ATC970 | ✓ | baseDB/V |
| V_TC97_DSH | V_TC97_DSH | ATC970 | ✓ | baseDB/V |
| V_TC98_AC01 | V_TC98_AC01 | ATC980 | ✓ | baseDB/V |
| V_TC9A | V_TC9A | ATC9A0 | ✓ | baseDB/V |
| V_TC9F | V_TC9F | ATC9F0 | ✓ | baseDB/V |
| V_TC9Q_GS | V_TC9Q_GS | ATC9Q0 | ✓ | baseDB/V |
| V_TC9V | V_TC9V | KEY | ✓ | baseDB/V |
| V_TC9W | V_TC9W | ATC9W0 | ✓ | baseDB/V |
| V_TC9W1_AB01 | V_TC9W1_AB01 | ATC9W0 | ✓ | baseDB/V |
| V_TC9W_HZ | V_TC9W_HZ | ID | ✓ | baseDB/V |
| V_TD04_TB93 | V_TD04_TB93 | ATB930, AAE002 | ✓ | baseDB/V |
| V_TJ02 | V_TJ02 | ATC930, ATB930, ATC932, ATC937 … | ✓ | baseDB/V |
| V_UP_LEMIS_MENU | V_UP_LEMIS_MENU | NAME | ✓ | baseDB/V |
| V_ZDGZFFQD | V_ZDGZFFQD | ATC930 | ✓ | baseDB/V |
| WBS_SCREEN | WBS_SCREEN | SCREENID |  | baseDB/W |
| WTS_FUNCTION | WTS_FUNCTION | FUNCTION_ID |  | baseDB/W |
| WTS_LAYERS | WTS_LAYERS | LAYER_ID, LAYER_TID |  | baseDB/W |
| WTS_MACHINE | WTS_MACHINE | MACHINE_ID |  | baseDB/W |
| WTS_NAVIGATION | WTS_NAVIGATION | NAVIGATION_ID |  | baseDB/W |
| WTS_POLICY | WTS_POLICY | POLICY_ID |  | baseDB/W |
| WTS_RESOURCE | WTS_RESOURCE | ID |  | baseDB/W |
| WTS_STYLE_TEMPLATE | WTS_STYLE_TEMPLATE | ID |  | baseDB/W |
| YTH_DWCB | YTH_DWCB | AAB001 |  | baseDB/Y |
| YTH_DWJF | YTH_DWJF | AAB001 |  | baseDB/Y |
| YTH_GRCB | YTH_GRCB | AAC001 |  | baseDB/Y |
| YTH_GRJF | YTH_GRJF | AAC001 |  | baseDB/Y |

## 5. 框架实体（hbm 有、src 无，32 个）

AdministrativeRole、Administrator、BusinessOperator、BusinessRole、BusinessRoleInUnit、CategoriedResource、Category、DelegatedRole、Dimension、DynamicCondition、DynamicRole、ExclusionRole、ExclusionStation、ManagedBusinessRole、ManagedCategory、ManagedDynamicRole、ManagedUnit、MultidimensionalCategory、MultidimensionalUnit、ResourceAuthority、ServiceAudit、Station、StationedUser、TB13$_ONLY、TC72$_TC73、TC7L$_ONLY、TC7M$_1、TC7W$_ONLY、TC7Z$_ONLY、Unit、User、UserInUnit

这些是 unieap/acegi 框架的用户-角色-资源模型实体，重写鉴权模块时需要一并考虑。

## 6. 重写视角

1. **实体层可完全机械化**：517 个纯 POJO + hbm → Kotlin data class（或 JPA 注解实体）可脚本生成；
   语义等价性靠表名 + 主键 + 列清单逐一对账。
2. **82 个 V_* 视图实体是只读查询面**：重写时映射为只读 DTO/查询对象，不必给写通道。
3. **56 个复合主键实体是易错点**：JPA 对复合键支持繁琐（@IdClass/@EmbeddedId），
   生成器要专门处理；`$_` 类直接丢弃。
4. **删除策略对齐**：系统大量「删除」实际是更新状态位（td/c 的 delete/delete1），
   实体层不需要物理删除级联——保持无 cascade 即可。
