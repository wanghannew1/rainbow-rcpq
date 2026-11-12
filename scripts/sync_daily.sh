#!/usr/bin/env bash
# sync_daily.sh — Oracle→PG 每日同步外挂入口(crontab 触发, 独立 JVM, 与业务服务隔离)
# 用法: scripts/sync_daily.sh [表名...]   无参=全量 15 表
# crontab(生产): 30 1 * * * /home/ubuntu/github/rainbow-rcpq/scripts/sync_daily.sh >> /home/ubuntu/github/rainbow-rcpq/work/sync.log 2>&1
# 退出码 0=全 OK / 1=有表失败(crontab 可配告警邮件或监控探活 wb_sync_log 最新 status)
# 并行期结束后退役: 删本 crontab 行即可, 业务包无需任何改动。

JAVA21=/usr/lib/jvm/java-21-openjdk-amd64/bin/java
JAR=/home/ubuntu/github/rainbow-rcpq/rcpq-web/build/libs/rcpq-web-sync.jar
export SYNC_PG_URL="jdbc:postgresql://localhost:5432/rainbow_rcpq?reWriteBatchedInserts=true"
export SYNC_PG_USER=rcpq
export SYNC_PG_PASS=rcpq_dev
export SYNC_ORA_URL="jdbc:oracle:thin:@172.16.0.60:1521:orcl"
export SYNC_ORA_USER=ccrcpq
export SYNC_ORA_PASS=ccrc
export TZ=Asia/Shanghai

exec "$JAVA21" -Dfile.encoding=UTF-8 -jar "$JAR" "$@"
