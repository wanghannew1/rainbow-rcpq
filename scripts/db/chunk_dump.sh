#!/bin/bash
# 分块补导 skipped 表: 每块独立连接, 避免 Connection reset
JAVA8=/usr/lib/jvm/java-8-openjdk-amd64/bin
OJDBC=/home/ubuntu/github/rainbowpq/src/main/webapp/WEB-INF/lib/ojdbc14.jar
export ORA_PASS
cd "$(dirname "$0")"
for f in /home/ubuntu/github/rainbow-rcpq/work/chunks/chunk*.txt; do
  T=$(cat "$f")
  echo "=== $f ($(echo $T | tr ',' '\n' | wc -l) 表) ==="
  $JAVA8/java -Dfile.encoding=UTF-8 -Dtables="$T" -Dout="$f.sql" -cp ".:$OJDBC" DumpSeed 2>&1 | grep -E "rows$|SKIP" | tail -3
done
echo ALL-DONE
