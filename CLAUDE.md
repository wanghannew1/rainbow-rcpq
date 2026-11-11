# rainbow-rcpq 项目须知（新会话必读）

跨语言重写项目：旧东软 Lemis 派遣系统(rainbowpq, 本目录的只读参照) → 彩虹人才管理系统
(Kotlin + Spring Boot 3 + PostgreSQL 16, 本仓库)。**后端已收官**(2026-11-10)：9/9 波次全移植、
48 个 /proc 过程、80 视图、黄金对照 140/140、端到端 20/20、种子缺口补齐、实体对齐。

## 必读文档（都在 docs/）
- business-process.md — 业务全景/业务员月度节拍/数据表地图/**端点映射**
- operations-manual.md — 新人 API 操作指导书(三类入口/请求体实例/状态机/排错速查)
- legacy-analysis/module-inventory.md — 波次状态与坏功能八例总账
- scripts/e2e_business.py — 端到端可执行参照(新业务员先读它)

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
   (PKG_IMP/PKG_IMP_FZ/PKG_派遣批量导入 三包生产即 INVALID)的源码是从未编译通过的残骸
2. 差集必须 `sort -n`（字典序污染 comm 曾把 AC10 差 3 误读，实漏 21261）
3. BigDecimal 比较一律 compareTo（equals 含 scale，5000≠5000.00）
4. PG 事务任一语句失败即毒化——不能照搬 Oracle"先试错再建"模式，先探测(to_regclass)
5. INSERT 占位符与列逐一核对（now() 位置错位是高频事故）；bp 批次表 varchar 值绑
   numeric 目标列显式 ::numeric/num()
6. 备份库 Oracle 172.16.0.60:1521/orcl ccrcpq/ccrc（JDK8+ojdbc14，脚本在 scripts/db/）
7. 大文件/脚本放 work/(gitignore)或 scripts/，禁 /tmp

## 三类 API 入口（前端对接面）
- POST /proc/{包名.过程名} — 48 个业务过程（校验/提交/计算，appCode=1 成功）
- POST /interaction/{REQ-编号} — 行编辑保存（**传改好状态的整行**，非增量指令）
- POST /import/tddc、/import/sys/{importId} — Excel 导入
- GET  /proc、/interaction/registry — 清单

## 当前阶段：前端（业务员工作台）
方案：静态页 + Vue3/ElementPlus(资源下载进 static/vendor, 内网无 CDN 依赖)，
Spring 直接托管 /static。按月度节拍分页：新签导入→审核台→工资核对→到账→发票→
险金测算→变更/终止。每页对接上面已验证的端点，参照 e2e_business.py 的调用序列。
