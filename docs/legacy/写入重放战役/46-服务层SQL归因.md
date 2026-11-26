# 写入重放战役 46 — 服务层 SQL 归因机械化(2026-11-25)

> 应"补深度": 缺口④ 服务方法体内部——bean.method 之后真正执行的 SQL/HQL/实体操作→读写哪些表。
> 不反编译, 用 **javap -p -c 字节码常量池**: SQL/HQL 都是 ldc String 常量, 确定性还原。

## 一、管线

`scripts/replay/service_sql_attribution.py`:
1. 全 XML → 交互注册(709 个 code)+ bean id→class FQN(`<bean name="X" class="...service.Y">`)
2. 非路由 bean.method(72 对)→ javap 抽每方法的 String 常量 + entities 引用
3. 正则分类: `update/insert into/delete from X`→写 X; `from X`→读 X(HQL 实体取 FQN 末段=表名约定);
   `PKG_x.PRC_y`→调过程; HibernateTemplate.save/update/delete→涉及实体=写(经 Hibernate)
4. 表名白名单校验=all_tab_columns 721 对象(db_columns.tsv), 防字节码噪音

## 二、结果

| 层 | 数量 | 说明 |
|---|---|---|
| 交互总数 | 709 | 路由型 637(表=store 实体, 页面层已载) |
| 非路由 bean.method | 72 | 方法体命中 70, 空体 2 |
| 带写表的方法 | 40 | 如 tdbd.doCbSh→写 TC8G,TC9E 读 TC90/LIST/TC92 |
| 带过程的方法 | 5 | **tdbf.reCommit→PKG_RCPQ_FZ.PRC_GENERATE_TFMX(⑦号注释禁用包的活调用点!)**; tddd.doSaveTc93→PKG_人才派遣.PRC_SALARYCOMPUTESINGLE |
| 清单深度信号 | 63 行 | 按键→接口 行追加 `→{写:… 调:… 读:…}` |

## 三、方法学坑(复用要点)

- **javap 方法头锚定**: 声明行=恰好 2 空格缩进; 指令行 ≥4 空格。不锚定会被带 `(…)` 的
  `// Method` 注释行劫持, 方法归属全错(首轮 0 命中的根因)
- 生成器同名遮蔽第二例: main() 里晚置的函数内 `import json` 遮蔽模块级导入 → UnboundLocalError
  (与反编译遮蔽类同型的 Python 版)
- TC90_LIST 是真表(204 列)非噪音 — 白名单校验必需

## 四、深度边界(诚实标注)

javap 只见本类方法体: ①StringBuilder 拼接 SQL 片段可还原(全大写表名在白名单内才计),
跨常量拆到表名中段的漏 ②委托给其他 Service/DAO 的间接 SQL 不追(v2 可递归)
③路由型 637 个的表已在页面层(store 实体), 不重复归因。
