# DC save 框架审计回填 考据与移植对照（桥单 20261127-036-dc-audit-backfill，2026-11-27）

## 一、源码考据（反编译面：cp33.jar 自带 .java 源码，本地 8091 Tomcat WEB-INF/lib）

**回填点**：`com.neusoft.lemis.cp33.pojo.util.LemisPojoUtil#setPojoSysUserInfo(pojo)`（L283-299）：

```java
pub.setProperty(pojo, "baz001", interactionParameterUtil.getBaz001());
pub.setProperty(pojo, "baz002", context.getProperty("baz002"));
pub.setProperty(pojo, "aae011", context.getProperty("aae011"));
pub.setProperty(pojo, "aae017", context.getProperty("aae017"));
pub.setProperty(pojo, "aae019", context.getProperty("aae019"));
pub.setProperty(pojo, "aae020", context.getProperty("aae020"));
pub.setProperty(pojo, "aae022", context.getProperty("aae022"));
pub.setProperty(pojo, "aae036", new Timestamp(context.getProperty("aae036")));
```

- **回填列清单**：baz001/baz002/aae011/aae017/aae019/aae020/aae022/aae036 八列；**aae013 等
  其他列不触碰**（BC-11 旧侧行态 aae013='卷49-*' 前端标记保留实证）。
- **取值源**：`AppContextContainer.getAppContext()`（登录人会话上下文）+ 会话级发号器
  （interParaUtil.getBaz001）。
- **覆盖条件**：无条件 setProperty（前端传值被覆盖）；调用点=通用保存体
  `Update_table`（lemis common be，LemisBusinessMgrPoJo.update 实现；javap 实证 L727/L818
  两处 invokestatic setPojoSysUserInfo，先 setEntityObjOtherValues 后回填）+ 定制服务
  （tddfService 等 binary 引用 LemisPojoUtil）。
- **无守卫的推论**：旧侧所有经框架保存的实体缺这些属性会 NoSuchMethodException——侧面证明
  lemis 表审计列为标配。

## 二、跨库实证矩阵（BC-11 包 20261127-033.attachment + BC-10 包 20261127-032.attachment）

| 通道 | 用例 | baz001/002 | aae017/019/020/022 | 判定 |
|---|---|---|---|---|
| 定制 tddfService（AA80/AA81 增） | WZ-01/02 | **回填**（会话发号器值 13510353/9932326） | **回填**（4101/贾悫/真实单位/410100000000，前端假值被覆盖） | insert 回填全量 |
| 通用 Update_table（AA98/AA99 增） | WZ-03a/04a | **空**（AppContext 无批号→setProperty(null)） | **回填**（同上） | insert 回填审计列 |
| 通用 Update_table（AA98 改） | WZ-03b | 空 | 四列保留自 insert 回填值 | **modify 不回填** |
| 通用 Update_table（TC90 整行改存，BC-10 AC-02） | AC-02 | 空 | **仍空**（旧侧 modify 后 aae017/019/020/022=''） | **modify 不回填（再证）** |

aae011（='jiaq'）与 aae036（时间戳）前端与上下文同值，实证不可区分；按源码面判为回填。

## 三、移植实现（rainbow-rcpq）

- `service/AuditBackfill`：`applyToInsert(row, tableCols, aae011)`——表列集∩{aae011,017,019,
  020,022,036} 覆盖；经办人身份源=显式参数→行内 aae011/bae011（无登录态期 payload 即身份）；
  查 sys_org_user_unit 单行，查无→不回填（降级口径=考据缺省）。aae036=ISO 串（实体
  LocalDateTime 形状）。`applyBaz`=baz001/002 nextval（仅定制通道调，旧实证口径）。
- `UpdateExecutor.execute(store, rows, aae011)`（新重载，控制器传请求经办人）：`__state=new`
  行回填；modify 行不动（实证）。
- `TddCases.doSaveAa80/doSaveAa81`（定制）：insert 行回填审计六列+baz。
- 探针实证（scripts/e2e_bc11_wage_dict.py）：前端传假审计值（00000000000/测试工资族/测试
  机构/209900000000）→落库=登录上下文真值（4101/贾悫/吉林省彩虹人才开发咨询服务有限公司/
  410100000000），改后行审计列保留——9/9 绿。

## 四、已验收 DC save 写链波及排查（任务书第 4 项）

| 链 | 保存通道 | 行态 | 波及判定 |
|---|---|---|---|
| BC-02 报增 | SQL 夹具+/proc | — | 不涉 DC save，不受波及 |
| BC-03 终止 | SQL 夹具+交互 modify | modify | 不回填（实证口径），不受波及 |
| BC-04 bgcl | REQ-tdbc saveUpdate（定制 merge） | modify | 不回填，不受波及 |
| BC-05 六弹窗 | 定制服务（doXinqCommit/doRenewCancle/doJsdy/doCbSh/terminateCancel/reCommit） | 均 modify/merge | 不回填，不受波及；审计面=bae 五件套显式传值（另一列族，非本回填面） |
| BC-10 tddab | UpdateExecutor | **modify** | 不回填（实证再证），原"仅 bbb001 变化"断言仍成立 |
| BC-11 字典族 | 通用+定制 | insert+modify | **受波及=本单移植主体**，探针复照 9/9 ✅ |
| bc06/bc09 夹具（tc001 拆形状 saveContract） | 定制 saveContract | insert | 旧侧同位为前端显式审计值（夹具自带 aae011/五件套），对照面未涉框架回填差异；列入批次 2 复跑顺带核对 |
| e2e_business 步骤3 | doXinqCommit | modify | 不受波及 |

复照计划：BC-11 已本单复照（探针法）；其余链维持原验收（modify 链按实证不受波及）；
bc06/bc09 夹具 insert 链随批次 2 复跑顺带核对审计列。
