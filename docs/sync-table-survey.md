# 同步表族调查与每晚同步体系（2026-11-21 定稿）

> 背景：备份库(.60) 每晚被生产 dmp 重建（约 01:40），PG 侧每晚 03:30 自动同步。
> 本文档记录：哪些表同步、怎么裁定的、一次性搬迁做了什么、工具在哪、并行期语义与退役方法。
> 决策人：用户（2026-11-21 三连拍板：不要自留地月份界限 / 原封不动照搬 / 只同步有用的表）。

## 一、共有表全量体检（表取舍的三重证据法）

Oracle∩PG **双方带主键**的共有表 **579 张**（`scripts/db/DiscoverShared.java` 实测）。
逐表三问：

| 证据 | 工具 | 含义 |
|---|---|---|
| ① .70 应用近期 SQL 是否引用 | `scripts/db/AppSqlProbe.java`（v$sql 按 JDBC 会话过滤，排除 dmp 导入噪音） | 应用层读写证据（666 条 SQL/237KB 文本池） |
| ② PL/SQL 包体/页面源码是否引用 | 反编译源码全库逐名 grep（`rainbowpq/work/pkg_all/*.sql` + webapp + java） | **过程内部读写的表不在应用 SQL 里，必须交叉**，否则误杀 |
| ③ 数据新鲜度 | `scripts/db/DeadTableProbe.java`（日期列 max 值 + v$sql 引用） | 停在远古且无人引用 = 死表 |

### 数字链

```
共有表 579
  ├─ 应用近期引用 163
  ├─ 应用未见 416 ── 源码交叉 ──> 358 实为业务表(近期未跑到的过程内部读写, 保留同步)
  └─                        └──> 58 全无踪迹 = 死表(排除)
Oracle 空表(0 行, 从未使用) ≈397 张 ── 排除
日期戳备份残表(_20260725 类, 6+)   ── 正则排除
= 每晚同步集 177 张(动态, 随库变化; 182-5 张死表有行者)
```

### 死表判定 v2（2026-11-21 复核：实体类命中 ≠ 业务使用）

首版 58 张的源码交叉存在盲区：Hibernate **实体映射类**（baseDB/C/CA05.java 等）会让 grep 命中，
但实体类≠业务使用。典型案例 **CA05"郑州本市农村村组表"**：16 行全是郑州区划（4101）+2011 年
admin 的测试垃圾（"sdf"/"测试村组"），系东软 Lemis 产品自郑州项目复制的实施残留，吉林侧
无任何页面/过程使用。修正口径后死表扩至 **154 张**（新增 96：TC7A-7Z 二十六连号产品线残段、
TC9B-9Z 系、TJ2xx/TJ8xx 系、WTS 触屏系统全家、V_* 视图实体化群、CA05 类郑州残留）。
工具链不变（AppSqlProbe→源码交叉，源码范围剔除实体类目录）。

### 死表清单（154 张，`OracleSyncJob.deadTables` 排除项）

```
MAP_ICON MAP_LAYER MAP_PIC SYS_MSG SYS_USER TC75 TC81
TJ0A1_20260725 TJ0B1_20260725 TJ0C1_20260725 TJ0D1_20260725 TJ0E1_20260725 TJ0F1_20260725
TJ101 TJ213 TJ214 TJ215 TJ216 TJBB5 TJBB6 TJBB7 TJBH4 TJBH5 TJB_WORK
TJDA1 TJDA2 TJDB1 TJDB2 TJDB3 TJDC2 TJDC3 TJDC4
VB01 VB40 VB42 VB45 VB46 VC01 VC02 VC03_BP VC0M
VD20 VD23 VD24 VD25 VV91 VV92
V_AC10 V_AC10_ZCXJ V_TB93_TC93_ZCXJ
WTS_CODE ZE09 ZE10 ZE12 ZE30 ZE32 ZE33 ZH_CH_DWHZ
```

特征：GIS 残件 / 平台残留 / 日期戳备份 / 物化视图拷贝(VC01 等) / 未知老表。
另发现：东软库内有大量同名**视图实体化成表**的现象（V_* 带 43k 行的 VC01），同步时按表处理。

## 二、每晚同步体系

```
每晚 ~01:40  生产 dmp 重建 .60 备份库
     ↓ scripts/sync_daily.sh 就绪门自动等待(连不上/表空 → 5 分钟×12 重试)
03:30  crontab → flock 单实例锁 → OracleSyncJob 自动发现 182 张有用表
     └─ 逐表原封不动 UPSERT(按双方主键), 单表失败隔离不影响全局
留痕: wb_sync_log 表(行数/状态/耗时) + work/sync.log; 退出码 0=全OK/1=有表失败/2=源库未就绪/3=已有实例
```

- **表取舍四道筛**（自动，随库动态）：双方主键 → Oracle 有数据 → 非死表清单 → 非日期戳残表
- **特殊钩子**（人工 specs 覆盖自动发现）：TC90_YY 按业务键去重保留最大主键（W6
  uk_tc90_yy 与 Oracle 历史重复组的调和）、TC90_LIST 按 AAE036 取最新（源 PK 失效）
- **新系统独有的表**（wb_audit_log/wb_sync_log 等 PG 独有）= 交集自动排除 = 自留地，不同步
- **并行期实话**：新系统对东软表的修改会在次日 03:30 被东软的账覆盖（镜像模式固有属性）；
  并行期结束（dmp 停）→ 删 crontab 行即退役，业务包零改动，此后新系统的写为永久写

## 三、名单表族历史搬迁（一次性，2026-11-21 执行）

`scripts/db/BackfillRoster.java`（可重跑，ON CONFLICT DO NOTHING）：

| 表 | 历史行(≤202610) | 月份跨度 |
|---|---|---|
| TC9W 社保 | 33,662 | 201911-202610 |
| TC9W1 市医保 | 26,819 | 201911-202610 |
| TC9W2 省医保 | 4,896 | 201911-202610 |
| TC9W3 省工伤 | 21,283 | 202106-202610 |
| TC9W4 市工伤 | 0 | — |
| TC9U 公积金 | 21,715 | 201911-202610 |

**序列防撞**：六表发号序列已抬段 `GREATEST(max(主键)+1, 10000001)` —— 11 月后新系统发号
从 1e7 段起，永不撞历史主键（与 seq_offset.sql 同策略）。

**口径**：名单表族原封不动全量（无月份界限），行内容零加工；202611+ 名单由新系统生成
（并行期晚同步会带东软 11 月旧账进来与自账共存——用户已确认测试数据可覆盖，混账风险自担）。

## 四、工具索引（scripts/db/，classpath 需 ojdbc14+postgresql 驱动）

| 工具 | 用途 | 复跑场景 |
|---|---|---|
| DiscoverShared.java | 共有表清单+PK+行量 | 扩同步集/体检时 |
| AppSqlProbe.java | 应用 SQL 引用探针 | 死表复核时 |
| DeadTableProbe.java | 数据新鲜度探针 | 死表复核时 |
| BackfillRoster.java | 名单表族历史搬迁+序列抬段 | PG 重建库后 |
| DbGolden.java | 东软回盘锁黄金直调 | 锁行为取证时 |

驱动机路径（本机）：`/home/ubuntu/test/rainbowpq/lib/ojdbc14.jar` +
`~/.gradle/caches/modules-2/files-2.1/org.postgresql/postgresql/42.7.4/.../postgresql-42.7.4.jar`

## 五、已勘定的坑（复跑前必读）

1. **ojdbc14 是 JDBC3**：必须显式 `Class.forName("oracle.jdbc.driver.OracleDriver")`
2. **游标纪律**：批量 count(*) 探针必须 close（首跑 510 探针泄漏 → ORA-01000 全程崩）
3. **dmp 导入噪音**：v$sql 含导入会话 SQL，须按 module like '%JDBC%' 过滤才是应用证据
4. **过程内部表**：应用 SQL 不含 PL/SQL 过程内部读写，必须与包体源码交叉，否则误杀 358 张
5. **Oracle 侧也有 DATE 列语义差异**：TC9U 族列名异体（atc9u1/atc9u8），名单表族之外的表
   接入前先核对列名
6. 本机 ojdbc14 路径：`/home/ubuntu/test/rainbowpq/lib/ojdbc14.jar`
