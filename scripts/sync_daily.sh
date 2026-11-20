#!/usr/bin/env bash
# sync_daily.sh — Oracle(备份库 .60)→PG 每晚同步(定时触发, 独立 JVM, 与业务服务隔离)
#
# 用法: scripts/sync_daily.sh [表名...]   无参=全量自动发现(有用表 ~172 张: Oracle 有数据+双方主键+非死表, 动态随库变化)
#
# 定时(生产): 30 3 * * * 上面本脚本 >> work/sync.log 2>&1
#   — 03:30 是为避开每晚 dmp 重建窗口(现网 dmp 约 01:40 起重建); 若 dmp 完成时间更晚,
#     改 crontab 时间即可, 脚本内 readiness 门也会自动等待库就绪(最多 60 分钟)。
#
# 安全设计:
#   1. flock 单实例: 同一时刻只允许一个同步进程(防手工补跑与定时撞车)
#   2. 就绪门: dmp 重建期间(.60 可能 drop/导入中)——连不上/表缺失/行数异常 → 每 5 分钟重试,
#      最多 12 次; 超时退出码 2, 不碰 PG。
#   3. 退出码: 0=全 OK / 1=有表失败 / 2=源库未就绪(超时) — crontab 可接告警邮件或
#      监控探活 wb_sync_log 最新 status。
# 并行期结束后退役: 删 crontab 行即可, 业务包零改动。

JAVA21=/usr/lib/jvm/java-21-openjdk-amd64/bin/java
JAR=/home/ubuntu/github/rainbow-rcpq/rcpq-web/build/libs/rcpq-web-sync.jar
export SYNC_PG_URL="jdbc:postgresql://localhost:5432/rainbow_rcpq?reWriteBatchedInserts=true"
export SYNC_PG_USER=rcpq
export SYNC_PG_PASS=rcpq_dev
export SYNC_ORA_URL="jdbc:oracle:thin:@172.16.0.60:1521:orcl"
export SYNC_ORA_USER=ccrcpq
export SYNC_ORA_PASS=ccrc
export TZ=Asia/Shanghai

LOCK=/home/ubuntu/github/rainbow-rcpq/work/sync_daily.lock
OJDBC=/home/ubuntu/test/rainbowpq/lib/ojdbc14.jar
exec 9>"$LOCK"
flock -n 9 || { echo "$(date '+%F %T') 已有同步在跑(flock), 本次跳过"; exit 3; }

# ---- 就绪门: dmp 重建中=连不上/表空 → 等待重试 ----
PROBE_CLS=/home/ubuntu/github/rainbow-rcpq/work/SyncProbe
if [ ! -f "$PROBE_CLS.dir/SyncProbe.class" ]; then
  mkdir -p "$PROBE_CLS.dir"
  cat > "$PROBE_CLS.dir/SyncProbe.java" << 'EOF'
import java.sql.*;
public class SyncProbe {
  public static void main(String[] a) throws Exception {
    try {
      Class.forName("oracle.jdbc.driver.OracleDriver"); // ojdbc14=JDBC3, 无自动注册
      Connection c = DriverManager.getConnection(a[0], a[1], a[2]);
      ResultSet r = c.createStatement().executeQuery("SELECT count(*) FROM tc90 WHERE atc900 IS NOT NULL");
      r.next();
      if (r.getLong(1) < 10000) { System.out.println("rows=" + r.getLong(1)); System.exit(2); }
      System.out.println("ready rows=" + r.getLong(1));
      c.close();
    } catch (Exception e) {
      System.out.println("err: " + e.getMessage());
      System.exit(2);
    }
  }
}
EOF
  javac -cp "$OJDBC" -d "$PROBE_CLS.dir" "$PROBE_CLS.dir/SyncProbe.java"
fi

for i in $(seq 1 12); do
  OUT=$(java -cp "$PROBE_CLS.dir:$OJDBC" SyncProbe "$SYNC_ORA_URL" "$SYNC_ORA_USER" "$SYNC_ORA_PASS" 2>&1)
  if echo "$OUT" | grep -q "^ready"; then
    echo "$(date '+%F %T') 源库就绪($OUT), 开始同步"
    break
  fi
  echo "$(date '+%F %T') 第${i}次探测: 源库未就绪($OUT), 5 分钟后重试"
  [ "$i" = "12" ] && { echo "$(date '+%F %T') 源库 60 分钟未就绪, 放弃(退出码2)"; exit 2; }
  sleep 300
done

exec "$JAVA21" -Dfile.encoding=UTF-8 -jar "$JAR" "$@"
