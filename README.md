# rainbow-rcpq · 彩虹人才管理系统

原东软 Lemis 人才派遣管理系统的 **Kotlin 规格重写**（版权驱动）。规格与分析文档见原仓库 `rainbowpq/docs/`，决策记录见 `docs/rewrite-decisions.md`。

## 技术栈

Kotlin 1.9 / Spring Boot 3.3 / JDK 21 / PostgreSQL 16（待接入）/ JasperReports（规划）/ Thymeleaf（xa 试点页面形态）

## 模块

| 模块 | 内容 |
|---|---|
| `rcpq-entity` | JPA 实体（由 hbm 元数据生成器产出；xa 试点 4 实体：AA01/AA10/AA12/SYS_QUESTION） |
| `rcpq-service` | 交互契约模型（InteractionSpec/ColumnMeta）+ `GenericCrudDispatcher`（原 LemisBusinessMgr 契约） |
| `rcpq-web` | Boot 应用 + 交互网关 `/interaction/{code}` + 路由表 `interactions.json`（26 条 xa 交互） |
| `rcpq-report` | 报表（JasperReports，占位） |

## 构建

```bash
./gradlew :rcpq-web:build      # 需 JDK 21；依赖走阿里云镜像（见 settings.gradle.kts）
java -jar rcpq-web/build/libs/rcpq-web.jar
```

冒烟验证：`GET /interaction/registry`（应返回 26 条）、`GET /interaction/REQ-xaac_cont-aa01Store-Query`

## 行为等价状态

- [x] 骨架编译 + 启动 + 路由
- [x] query/update 分发语义（QueryExecutor/UpdateExecutor，PG 真数据冒烟：查 14165 分页/插入序列取值 10059+182/改/删零残留）
- [x] 元数据校验（MetadataValidator，8 单测通过）
- [ ] 主从表保存 + 外键回填两种形态
- [x] PG16 数据源 + 序列对齐（V1-V3 已应用，数据与 Oracle 全对齐）
- [x] 黄金样本导出（备份库 → rcpq-service/src/test/resources/golden/xa-samples.json）
- [ ] 行为对照 diff 测试

## 规约

命名空间禁词与类名映射见 `docs/rewrite-decisions.md` §5（lemis/neusoft/unieap/东软 等禁止出现）。
