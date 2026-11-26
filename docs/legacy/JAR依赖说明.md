# JAR 依赖说明

## 目录结构

所有 JAR 统一存放在 `src/main/webapp/WEB-INF/lib/`，既是 Maven 编译 classpath，也是 Tomcat 运行时 classpath。

```
src/main/webapp/WEB-INF/lib/
├── *.jar                    # 227 个核心 JAR（编译+运行时都需要）
├── db2/                     # DB2 数据库驱动（仅运行时）
│   ├── db2java.jar
│   ├── db2jcc.jar
│   ├── db2jcc_javax.jar
│   ├── db2jcc_license_cisuz.jar
│   └── db2jcc_license_cu.jar
├── mysql/                   # MySQL 数据库驱动（仅运行时）
│   └── mysql-connector-java-5.0.5-bin.jar
├── sqlserver/               # SQL Server 数据库驱动（仅运行时）
│   └── jtds-1.2.2.jar
├── sybase/                  # Sybase 数据库驱动（仅运行时）
│   └── jconn3.jar
└── integrate/               # UniEAP 集成扩展（仅运行时）
    ├── saxpath.jar
    └── unieap-integrate-3.0.jar
```

## 编译 vs 运行时

| 类别 | JAR 数 | 说明 |
|------|--------|------|
| **编译+运行时** | 227 | Spring, Hibernate, Struts, UniEAP, 日志, 工具库等核心 JAR |
| **仅运行时** | 10 | 数据库驱动（DB2/MySQL/SQLServer/Sybase）+ 扩展库 |

### 编译不需要的 10 个 JAR（仅运行时数据库驱动）

| JAR | 用途 | 本系统是否使用 |
|-----|------|---------------|
| db2java.jar | DB2 JDBC 驱动 | ❌ 不使用（本系统用 Oracle） |
| db2jcc.jar | DB2 JCC 驱动 | ❌ 不使用 |
| db2jcc_javax.jar | DB2 JCC 扩展 | ❌ 不使用 |
| db2jcc_license_cisuz.jar | DB2 许可证 | ❌ 不使用 |
| db2jcc_license_cu.jar | DB2 许可证 | ❌ 不使用 |
| jconn3.jar | Sybase JDBC 驱动 | ❌ 不使用 |
| jtds-1.2.2.jar | SQL Server JDBC 驱动 | ❌ 不使用 |
| mysql-connector-java-5.0.5-bin.jar | MySQL JDBC 驱动 | ❌ 不使用 |
| saxpath.jar | SAX 路径解析 | ❌ 未被源码引用 |
| unieap-integrate-3.0.jar | UniEAP 集成扩展 | ❌ 未被源码引用 |

> 这 10 个 JAR 保留在 git 中是因为它们来自原始 Tomcat webapp，且 Tomcat 启动时会扫描 WEB-INF/lib/ 下所有 JAR。删除不影响编译，但可能影响运行时类加载。

## 常用核心 JAR

| JAR | 版本 | 用途 |
|-----|------|------|
| spring-2.0.4.jar | 2.0.4 | Spring Framework |
| hibernate-3.5.6.jar | 3.5.6 | ORM 框架 |
| struts-1.2.7.jar | 1.2.7 | MVC 框架 |
| ojdbc14.jar | - | Oracle JDBC 驱动 |
| unieap-platform.jar | 3.0 | UniEAP 核心平台 |
| log4j-1.2.15.jar | 1.2.15 | 日志框架 |
| c3p0-0.9.0.jar | 0.9.0 | 数据库连接池 |
| quartz-1.6.0.jar | 1.6.0 | 定时任务 |

## Maven 编译

```bash
# 编译（需要 JDK 8）
export JAVA_HOME=/usr/lib/jvm/java-8-openjdk-amd64
mvn clean compile

# pom.xml 中 237 个 system-scope 依赖指向 WEB-INF/lib/
```

## 注意事项

1. **不要删除 lib/ 目录** — 已删除，所有 JAR 统一在 `src/main/webapp/WEB-INF/lib/`
2. **新增 JAR** — 放入 `src/main/webapp/WEB-INF/lib/`，并在 pom.xml 添加 system-scope 依赖
3. **数据库驱动** — 本系统使用 Oracle（ojdbc14.jar），其他驱动保留但不使用
