# 备份库 JDBC 工具（scripts/db/）

连接 Oracle 备份库 `172.16.0.60:1521/orcl`（Oracle 11.1，python-oracledb thin 不支持，必须 JDK8+ojdbc14）。

```bash
# 全部工具凭据走环境变量: export ORA_USER=ccrcpq ORA_PASS=***
JDBC=/home/ubuntu/github/rainbowpq/src/main/webapp/WEB-INF/lib/ojdbc14.jar
JAVA8=/usr/lib/jvm/java-8-openjdk-amd64/bin
$JAVA8/javac -encoding UTF-8 Xxx.java
$JAVA8/bin/java -Dfile.encoding=UTF-8 -cp .:$JDBC Xxx
```

| 工具 | 用途 |
|---|---|
|  `DbInv.java`（ORA_USER/ORA_PASS 环境变量） | 双 schema 对象盘点（表/视图/过程/包/序列/触发器/同义词/源码统计）→ /tmp 改为 work/ TSV |
| `DumpGolden.java` | xa 4 表黄金样本导出（列名归一小写！）→ rcpq-service/src/test/resources/golden/ |
| `DumpSeed.java` | xa 4 表全量 INSERT → rcpq-web/src/main/resources/db/V3__xa_seed_data.sql |
| `DumpPackage.java` | 导出 PL/SQL 包(规范+包体)完整源码 → work/pkg_*.sql（PLSQL 翻译前必读，包体可能整块被注释禁用——PRC_GENERATE_TFMX 实例） |
| `SeqVal.java` | 读 user_sequences 当前值（V2 序列对齐用） |

注意：Golden 导出列名必须 toLowerCase()（与元数据小写口径一致，大小写不一致会导致校验误报——2026-11-06 的真实发现）。
