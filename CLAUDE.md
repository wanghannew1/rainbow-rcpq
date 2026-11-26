# rainbow-rcpq 项目须知（新会话必读）

跨语言重写项目：旧东软 Lemis 派遣系统(rainbowpq, 本目录的只读参照) → 彩虹人才管理系统
(Kotlin + Spring Boot 3 + PostgreSQL 16, 本仓库)。**后端已收官**(2026-11-10)：9/9 波次全移植、
48 个 /proc 过程、80 视图、黄金对照 140/140、端到端 20/20、种子缺口补齐、实体对齐。

## 必读文档（都在 docs/）
- business-process.md — 业务全景/业务员月度节拍/数据表地图/**端点映射**
- operations-manual.md — 新人 API 操作指导书(三类入口/请求体实例/状态机/排错速查/**§10 合同批量导入**)
- import-channel.md — **通用导入通道业务逻辑**(/import/sys 引擎契约/批次状态机/四模板语义/忠实坑)
- legacy-analysis/module-inventory.md — 波次状态与坏功能八例总账
- scripts/e2e_business.py — 端到端可执行参照(新业务员先读它)
- scripts/e2e_import_sys.py — 导入通道 e2e(合同四路+专项五路+三银行+结算确认/取消+市医保应缴费, 51 断言)

## 构建与启动（老坑必踩）
```bash
# PATH 的 java 是 1.8，必须显式 21：
JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64 ./gradlew :rcpq-web:bootJar
fuser -k 8080/tcp; nohup /usr/lib/jvm/java-21-openjdk-amd64/bin/java \
  -jar rcpq-web/build/libs/rcpq-web.jar > work/appNN.log 2>&1 &
```
- jar 重建后**必须重启进程**（LaunchedURLClassLoader 按需加载嵌套 jar，热替换会 NoClassDefFoundError）
- PG: localhost:5432/rainbow_rcpq, 用户 rcpq 密码 rcpq_dev（sudo postgres 密码 ubuntu）
- 远程双推 origin(github)+gitee，SSH 已认证

## 铁律（前辈踩过的坑，违反=浪费窗口）
1. 翻译 PL/SQL 前必读 user_source 真实源码 **且查 user_objects.status**——INVALID 包体
   (PKG_IMP/PKG_IMP_FZ/PKG_派遣批量导入 三包生产即 INVALID)的源码是从未编译通过的残骸。
   **2026-11-11 备份库实测补充: INVALID 包体远不止三包**(PKG_保险人员增减/PKG_RCPQ 系列/
   PKG_工资发放/PKG_财务/PKG_保险补退 等全在册, scripts/db/DbStatus.java 可查全清单)——
   从 INVALID 包移植的过程, 移植前先找该行为的运行时证据(现网实测/黄金对照), 缺证据的按
   "忠实报错 + 可用重建"双轨处理(社保回盘两过程即此模式)
2. 差集必须 `sort -n`（字典序污染 comm 曾把 AC10 差 3 误读，实漏 21261）
3. BigDecimal 比较一律 compareTo（equals 含 scale，5000≠5000.00）
4. PG 事务任一语句失败即毒化——不能照搬 Oracle"先试错再建"模式，先探测(to_regclass)
5. INSERT 占位符与列逐一核对（now() 位置错位是高频事故）；bp 批次表 varchar 值绑
   numeric 目标列显式 ::numeric/num()
6. 备份库 Oracle 172.16.0.60:1521/orcl ccrcpq/ccrc（JDK8+ojdbc14，脚本在 scripts/db/）
7. 大文件/脚本放 work/(gitignore)或 scripts/，禁 /tmp
8. **删除一律逻辑删除**（作废标记，禁物理 DELETE）；**用户操作一律写 wb_audit_log 留痕**
   （AuditLog bean：/proc、/interaction、/wb 写操作、报盘导出已覆盖）；**新操作按键必须用
   wb-dsf-tip 漂浮标注东软对应位置**（菜单/页面码+名称/板块/按键名，悬停显示）；
   **状态/代码字段的值含义一律写 COMMENT ON COLUMN**（scripts/db/column_comments.sql，
   含义必须经代码验证后写入并标注出处，重建库后重跑该脚本）。
   已移植忠实过程中的物理删（如 doXinqCancle）列改造清单逐步替换，不顺手改

## 三类 API 入口（前端对接面）
- POST /proc/{包名.过程名} — 48 个业务过程（校验/提交/计算，appCode=1 成功）
- POST /interaction/{REQ-编号} — 行编辑保存（**传改好状态的整行**，非增量指令）
- POST /import/tddc、/import/sys/{importId} — Excel 导入
- GET  /proc、/interaction/registry — 清单
- POST /sync/run、GET /sync/status|tables — Oracle→PG 每日同步（并行运行期，2026-11-12）：
  **全量自动发现 ~172 表**（sync_daily.sh 无参；OracleSyncJob 静态 specs=21 主要表子集；
  2026-11-26 勘正：含全部 bp 批次表族，"TC9W_BP 系列不在同步"旧口径作废）；**同步表=旧库
  原始表，PG 侧只读不改造**（写在两次同步之间会被覆盖）；PG 序列已抬段防两库撞号
  （scripts/db/seq_offset.sql，新业务键 1e7 段起、共享发号器按 Oracle 键域外推如
  seq_sys_baz002=5e7）。**生产发起=外挂进程**（与业务 JVM 隔离）：
  `scripts/sync_daily.sh [表名...]`（crontab 01:30，退出码接告警；引擎=rcpq-web-sync.jar
  SyncMainKt）；进程内 /sync/run 仅作人工补跑；退役=摘 crontab

## 当前阶段：前端（业务员工作台）
方案：静态页 + Vue3/ElementPlus(资源下载进 static/vendor, 内网无 CDN 依赖)，
Spring 直接托管 /static。按月度节拍分页：新签导入→审核台→工资核对→到账→发票→
险金测算→变更/终止。每页对接上面已验证的端点，参照 e2e_business.py 的调用序列。
