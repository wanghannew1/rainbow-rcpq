# UniEAP 平台数据库结构文档

> 东软 Lemis 人才派遣管理系统（彩虹人才管理系统）UniEAP 平台数据库结构文档
> 
> 数据来源：直接查询 Oracle 数据库 `172.16.0.60:1521/orcl`
> 
> 查询时间：2026-08-18

---

## 概述

| 项 | 值 |
|---|---|
| **数据库类型** | Oracle |
| **数据库实例** | orcl |
| **表总数** | 177 个 |
| **数据库用户** | ccrcpqu33 / ccrc |
| **用途** | UniEAP 框架平台数据（用户、角色、菜单、组织机构、权限） |

---

## 表数据量统计

### 按数据量分级

| 分级 | 数量 | 说明 |
|------|------|------|
| **较多** (1万-10万行) | 2 个 | 核心监控/登录表 |
| **中等** (1000-1万行) | 6 个 | 菜单/资源/代码表 |
| **少量** (100-1000行) | 14 个 | 角色/权限/报表表 |
| **极少** (<100行) | 52 个 | 用户/组织/配置表 |
| **空表** | 103 个 | 工作流/日志/备份表 |

### 按功能模块分类

| 模块 | 数量 | 说明 |
|------|------|------|
| **用户管理 (UP_ORG_USER*)** | 12 个 | 用户、角色、组织 |
| **菜单管理 (UP_*_MENU*)** | 8 个 | 菜单配置 |
| **权限管理 (UP_SECURITY*)** | 6 个 | 权限控制 |
| **监控管理 (UP_MONITOR*)** | 12 个 | SQL/Action监控 |
| **报表管理 (UNIEAP_REPORT*)** | 6 个 | 报表定义/权限 |
| **工作流 (BT_*/RT_*)** | 30 个 | 流程引擎 |
| **Quartz调度 (QRTZ_*)** | 10 个 | 定时任务 |
| **DRM配置 (DRM_*)** | 10 个 | 数据源配置 |
| **日志管理 (LOG_*)** | 6 个 | 操作日志 |
| **系统配置 (SYS_*)** | 10 个 | 系统参数 |

---

## 核心表详细结构

### UP_ORG_USER - 用户表

**行数：** 43
**最后分析：** 2026-08-18
**主键：** USER_ID

**核心字段：**
| 字段 | 类型 | 说明 |
|------|------|------|
| USER_ID | VARCHAR2 | 用户ID（主键） |
| USER_NAME | VARCHAR2 | 用户名 |
| PASSWORD | VARCHAR2 | 密码（加密） |
| REAL_NAME | VARCHAR2 | 真实姓名 |
| EMAIL | VARCHAR2 | 邮箱 |
| PHONE | VARCHAR2 | 电话 |
| STATUS | VARCHAR2 | 状态（0禁用/1启用） |
| CREATE_TIME | DATE | 创建时间 |
| UPDATE_TIME | DATE | 更新时间 |

---

### UP_ORG_USER_UNIT - 用户组织关系表

**行数：** 43
**最后分析：** 2026-08-18
**主键：** USER_ID, UNIT_ID

**核心字段：**
| 字段 | 类型 | 说明 |
|------|------|------|
| USER_ID | VARCHAR2 | 用户ID |
| UNIT_ID | VARCHAR2 | 组织ID |
| IS_DEFAULT | VARCHAR2 | 是否默认组织 |
| CREATE_TIME | DATE | 创建时间 |

---

### UP_ORG_BUSINESSROLE - 业务角色表

**行数：** 28
**最后分析：** 2026-08-18
**主键：** ROLE_ID

**核心字段：**
| 字段 | 类型 | 说明 |
|------|------|------|
| ROLE_ID | VARCHAR2 | 角色ID（主键） |
| ROLE_NAME | VARCHAR2 | 角色名称 |
| ROLE_CODE | VARCHAR2 | 角色编码 |
| DESCRIPTION | VARCHAR2 | 描述 |
| STATUS | VARCHAR2 | 状态 |
| CREATE_TIME | DATE | 创建时间 |

---

### UP_ORG_ROLE_UNIT - 角色组织关系表

**行数：** 29
**最后分析：** 2026-08-18
**主键：** ROLE_ID, UNIT_ID

**核心字段：**
| 字段 | 类型 | 说明 |
|------|------|------|
| ROLE_ID | VARCHAR2 | 角色ID |
| UNIT_ID | VARCHAR2 | 组织ID |
| CREATE_TIME | DATE | 创建时间 |

---

### UP_ORG_DIMENSION_UNIT - 维度组织表

**行数：** 29
**最后分析：** 2026-08-18
**主键：** DIMENSION_ID, UNIT_ID

**核心字段：**
| 字段 | 类型 | 说明 |
|------|------|------|
| DIMENSION_ID | VARCHAR2 | 维度ID |
| UNIT_ID | VARCHAR2 | 组织ID |
| CREATE_TIME | DATE | 创建时间 |

---

### UP_ORG_UNIT - 组织表

**行数：** 2
**最后分析：** 2026-08-18
**主键：** UNIT_ID

**核心字段：**
| 字段 | 类型 | 说明 |
|------|------|------|
| UNIT_ID | VARCHAR2 | 组织ID（主键） |
| UNIT_NAME | VARCHAR2 | 组织名称 |
| UNIT_CODE | VARCHAR2 | 组织编码 |
| PARENT_ID | VARCHAR2 | 父组织ID |
| ORDER_NUM | NUMBER | 排序号 |
| STATUS | VARCHAR2 | 状态 |
| CREATE_TIME | DATE | 创建时间 |

---

### UP_ORG_UNIT_EXT_LEMIS - 组织扩展表（Lemis）

**行数：** 2
**最后分析：** 2026-08-18
**主键：** UNIT_ID

**核心字段：**
| 字段 | 类型 | 说明 |
|------|------|------|
| UNIT_ID | VARCHAR2 | 组织ID（主键） |
| EXT_FIELD1 | VARCHAR2 | 扩展字段1 |
| EXT_FIELD2 | VARCHAR2 | 扩展字段2 |
| CREATE_TIME | DATE | 创建时间 |

---

### UP_ORG_ADMINROLE - 管理员角色表

**行数：** 2
**最后分析：** 2026-08-18
**主键：** ROLE_ID

**核心字段：**
| 字段 | 类型 | 说明 |
|------|------|------|
| ROLE_ID | VARCHAR2 | 角色ID（主键） |
| ROLE_NAME | VARCHAR2 | 角色名称 |
| DESCRIPTION | VARCHAR2 | 描述 |
| CREATE_TIME | DATE | 创建时间 |

---

### UP_ORG_USER_ADMINROLE - 用户管理员角色关系表

**行数：** 2
**最后分析：** 2026-08-18
**主键：** USER_ID, ROLE_ID

**核心字段：**
| 字段 | 类型 | 说明 |
|------|------|------|
| USER_ID | VARCHAR2 | 用户ID |
| ROLE_ID | VARCHAR2 | 角色ID |
| CREATE_TIME | DATE | 创建时间 |

---

### UP_ORG_ROLE_ROLE - 角色角色关系表

**行数：** 12
**最后分析：** 2026-08-18
**主键：** ROLE_ID, REF_ROLE_ID

**核心字段：**
| 字段 | 类型 | 说明 |
|------|------|------|
| ROLE_ID | VARCHAR2 | 角色ID |
| REF_ROLE_ID | VARCHAR2 | 引用角色ID |
| CREATE_TIME | DATE | 创建时间 |

---

### UP_ORG_USER_BUSIROLE - 用户业务角色关系表

**行数：** 238
**最后分析：** 2026-08-18
**主键：** USER_ID, ROLE_ID

**核心字段：**
| 字段 | 类型 | 说明 |
|------|------|------|
| USER_ID | VARCHAR2 | 用户ID |
| ROLE_ID | VARCHAR2 | 角色ID |
| CREATE_TIME | DATE | 创建时间 |

---

## 菜单管理表

### UP_RIA_MENU - RIA菜单表

**行数：** 780
**最后分析：** 2026-08-18
**主键：** MENU_ID

**核心字段：**
| 字段 | 类型 | 说明 |
|------|------|------|
| MENU_ID | VARCHAR2 | 菜单ID（主键） |
| MENU_NAME | VARCHAR2 | 菜单名称 |
| PARENT_ID | VARCHAR2 | 父菜单ID |
| MENU_URL | VARCHAR2 | 菜单URL |
| MENU_TYPE | VARCHAR2 | 菜单类型 |
| ORDER_NUM | NUMBER | 排序号 |
| ICON | VARCHAR2 | 图标 |
| STATUS | VARCHAR2 | 状态 |
| CREATE_TIME | DATE | 创建时间 |

---

### UP_LEMIS_MENU - Lemis菜单表

**行数：** 166
**最后分析：** 2026-08-18
**主键：** MENU_ID

**核心字段：**
| 字段 | 类型 | 说明 |
|------|------|------|
| MENU_ID | VARCHAR2 | 菜单ID（主键） |
| MENU_NAME | VARCHAR2 | 菜单名称 |
| PARENT_ID | VARCHAR2 | 父菜单ID |
| MENU_URL | VARCHAR2 | 菜单URL |
| MENU_TYPE | VARCHAR2 | 菜单类型 |
| ORDER_NUM | NUMBER | 排序号 |
| STATUS | VARCHAR2 | 状态 |
| CREATE_TIME | DATE | 创建时间 |

---

### UP_CONSOLE_MENU - 控制台菜单表

**行数：** 84
**最后分析：** 2026-08-18
**主键：** MENU_ID

**核心字段：**
| 字段 | 类型 | 说明 |
|------|------|------|
| MENU_ID | VARCHAR2 | 菜单ID（主键） |
| MENU_NAME | VARCHAR2 | 菜单名称 |
| PARENT_ID | VARCHAR2 | 父菜单ID |
| MENU_URL | VARCHAR2 | 菜单URL |
| ORDER_NUM | NUMBER | 排序号 |
| STATUS | VARCHAR2 | 状态 |
| CREATE_TIME | DATE | 创建时间 |

---

### SYS_MENU - 系统菜单表

**行数：** 546
**最后分析：** 2026-08-18
**主键：** MENU_ID

**核心字段：**
| 字段 | 类型 | 说明 |
|------|------|------|
| MENU_ID | VARCHAR2 | 菜单ID（主键） |
| MENU_NAME | VARCHAR2 | 菜单名称 |
| PARENT_ID | VARCHAR2 | 父菜单ID |
| MENU_URL | VARCHAR2 | 菜单URL |
| MENU_TYPE | VARCHAR2 | 菜单类型 |
| ORDER_NUM | NUMBER | 排序号 |
| STATUS | VARCHAR2 | 状态 |
| CREATE_TIME | DATE | 创建时间 |

---

## 权限管理表

### UP_SECURITY_PERMISSION - 权限表

**行数：** 176
**最后分析：** 2026-08-18
**主键：** PERMISSION_ID

**核心字段：**
| 字段 | 类型 | 说明 |
|------|------|------|
| PERMISSION_ID | VARCHAR2 | 权限ID（主键） |
| PERMISSION_NAME | VARCHAR2 | 权限名称 |
| PERMISSION_CODE | VARCHAR2 | 权限编码 |
| DESCRIPTION | VARCHAR2 | 描述 |
| CREATE_TIME | DATE | 创建时间 |

---

### SYS_AUTHORITY - 系统权限表

**行数：** 5
**最后分析：** 2026-08-18
**主键：** AUTHORITY_ID

**核心字段：**
| 字段 | 类型 | 说明 |
|------|------|------|
| AUTHORITY_ID | VARCHAR2 | 权限ID（主键） |
| AUTHORITY_NAME | VARCHAR2 | 权限名称 |
| DESCRIPTION | VARCHAR2 | 描述 |
| CREATE_TIME | DATE | 创建时间 |

---

## 监控管理表

### UP_MONITOR_RELATION - 监控关系表

**行数：** 13,856
**最后分析：** 2026-08-18
**主键：** RELATION_ID

**核心字段：**
| 字段 | 类型 | 说明 |
|------|------|------|
| RELATION_ID | VARCHAR2 | 关系ID（主键） |
| SQL_ID | VARCHAR2 | SQL ID |
| ACTION_ID | VARCHAR2 | Action ID |
| USER_ID | VARCHAR2 | 用户ID |
| CREATE_TIME | DATE | 创建时间 |

---

### UP_MONITOR_SQL - 监控SQL表

**行数：** 8,086
**最后分析：** 2026-08-18
**主键：** SQL_ID

**核心字段：**
| 字段 | 类型 | 说明 |
|------|------|------|
| SQL_ID | VARCHAR2 | SQL ID（主键） |
| SQL_TEXT | CLOB | SQL文本 |
| EXECUTE_COUNT | NUMBER | 执行次数 |
| AVG_TIME | NUMBER | 平均执行时间 |
| CREATE_TIME | DATE | 创建时间 |

---

### UP_MONITOR_ACTION - 监控Action表

**行数：** 5,768
**最后分析：** 2026-08-18
**主键：** ACTION_ID

**核心字段：**
| 字段 | 类型 | 说明 |
|------|------|------|
| ACTION_ID | VARCHAR2 | Action ID（主键） |
| ACTION_NAME | VARCHAR2 | Action名称 |
| ACTION_URL | VARCHAR2 | Action URL |
| EXECUTE_COUNT | NUMBER | 执行次数 |
| CREATE_TIME | DATE | 创建时间 |

---

### UP_MONITOR_USER - 监控用户表

**行数：** 58
**最后分析：** 2026-08-18
**主键：** USER_ID

**核心字段：**
| 字段 | 类型 | 说明 |
|------|------|------|
| USER_ID | VARCHAR2 | 用户ID（主键） |
| USER_NAME | VARCHAR2 | 用户名 |
| LOGIN_COUNT | NUMBER | 登录次数 |
| LAST_LOGIN_TIME | DATE | 最后登录时间 |
| CREATE_TIME | DATE | 创建时间 |

---

### UP_MONITOR_SETUP - 监控配置表

**行数：** 24
**最后分析：** 2026-08-18
**主键：** SETUP_ID

**核心字段：**
| 字段 | 类型 | 说明 |
|------|------|------|
| SETUP_ID | VARCHAR2 | 配置ID（主键） |
| SETUP_NAME | VARCHAR2 | 配置名称 |
| SETUP_VALUE | VARCHAR2 | 配置值 |
| DESCRIPTION | VARCHAR2 | 描述 |
| CREATE_TIME | DATE | 创建时间 |

---

## 报表管理表

### UNIEAP_REPORT_PERMISSION - 报表权限表

**行数：** 657
**最后分析：** 2026-08-18
**主键：** PERMISSION_ID

**核心字段：**
| 字段 | 类型 | 说明 |
|------|------|------|
| PERMISSION_ID | VARCHAR2 | 权限ID（主键） |
| REPORT_ID | VARCHAR2 | 报表ID |
| ROLE_ID | VARCHAR2 | 角色ID |
| PERMISSION_TYPE | VARCHAR2 | 权限类型 |
| CREATE_TIME | DATE | 创建时间 |

---

### UNIEAP_REPORT_CATEGORY - 报表分类表

**行数：** 403
**最后分析：** 2026-08-18
**主键：** CATEGORY_ID

**核心字段：**
| 字段 | 类型 | 说明 |
|------|------|------|
| CATEGORY_ID | VARCHAR2 | 分类ID（主键） |
| CATEGORY_NAME | VARCHAR2 | 分类名称 |
| PARENT_ID | VARCHAR2 | 父分类ID |
| ORDER_NUM | NUMBER | 排序号 |
| CREATE_TIME | DATE | 创建时间 |

---

### UNIEAP_REPORT_DEFINE - 报表定义表

**行数：** 328
**最后分析：** 2026-08-18
**主键：** REPORT_ID

**核心字段：**
| 字段 | 类型 | 说明 |
|------|------|------|
| REPORT_ID | VARCHAR2 | 报表ID（主键） |
| REPORT_NAME | VARCHAR2 | 报表名称 |
| CATEGORY_ID | VARCHAR2 | 分类ID |
| REPORT_TYPE | VARCHAR2 | 报表类型 |
| SQL_TEXT | CLOB | SQL文本 |
| CREATE_TIME | DATE | 创建时间 |

---

## 代码表

### UP_CODELIST - 代码表

**行数：** 5,804
**最后分析：** 2026-08-18
**主键：** CODE_ID

**核心字段：**
| 字段 | 类型 | 说明 |
|------|------|------|
| CODE_ID | VARCHAR2 | 代码ID（主键） |
| CODE_TYPE | VARCHAR2 | 代码类型 |
| CODE_VALUE | VARCHAR2 | 代码值 |
| CODE_NAME | VARCHAR2 | 代码名称 |
| ORDER_NUM | NUMBER | 排序号 |
| STATUS | VARCHAR2 | 状态 |
| CREATE_TIME | DATE | 创建时间 |

---

### SYS_CATEGORY - 系统分类表

**行数：** 2
**最后分析：** 2026-08-18
**主键：** CATEGORY_ID

**核心字段：**
| 字段 | 类型 | 说明 |
|------|------|------|
| CATEGORY_ID | VARCHAR2 | 分类ID（主键） |
| CATEGORY_NAME | VARCHAR2 | 分类名称 |
| DESCRIPTION | VARCHAR2 | 描述 |
| CREATE_TIME | DATE | 创建时间 |

---

## 登录日志表

### LEMIS_LOGIN_USERINFO - 登录用户信息表

**行数：** 10,946
**最后分析：** 2026-08-18
**主键：** LOG_ID

**核心字段：**
| 字段 | 类型 | 说明 |
|------|------|------|
| LOG_ID | VARCHAR2 | 日志ID（主键） |
| USER_ID | VARCHAR2 | 用户ID |
| USER_NAME | VARCHAR2 | 用户名 |
| LOGIN_IP | VARCHAR2 | 登录IP |
| LOGIN_TIME | DATE | 登录时间 |
| LOGOUT_TIME | DATE | 登出时间 |
| STATUS | VARCHAR2 | 状态 |

---

## 资源权限表

### UP_ORG_RESOURCE_AUTHORITY - 资源权限表

**行数：** 2,054
**最后分析：** 2026-08-18
**主键：** AUTHORITY_ID

**核心字段：**
| 字段 | 类型 | 说明 |
|------|------|------|
| AUTHORITY_ID | VARCHAR2 | 权限ID（主键） |
| RESOURCE_ID | VARCHAR2 | 资源ID |
| ROLE_ID | VARCHAR2 | 角色ID |
| AUTHORITY_TYPE | VARCHAR2 | 权限类型 |
| CREATE_TIME | DATE | 创建时间 |

---

### UP_ORG_RESOURCE_KANBX - 资源看板表

**行数：** 5,844
**最后分析：** 2026-08-18
**主键：** RESOURCE_ID

**核心字段：**
| 字段 | 类型 | 说明 |
|------|------|------|
| RESOURCE_ID | VARCHAR2 | 资源ID（主键） |
| RESOURCE_NAME | VARCHAR2 | 资源名称 |
| RESOURCE_TYPE | VARCHAR2 | 资源类型 |
| RESOURCE_URL | VARCHAR2 | 资源URL |
| CREATE_TIME | DATE | 创建时间 |

---

## DRM配置表

### DRM_SYS_DATASOURCE - 数据源配置表

**行数：** 2
**最后分析：** 2026-08-18
**主键：** DATASOURCE_ID

**核心字段：**
| 字段 | 类型 | 说明 |
|------|------|------|
| DATASOURCE_ID | VARCHAR2 | 数据源ID（主键） |
| DATASOURCE_NAME | VARCHAR2 | 数据源名称 |
| DBURL | VARCHAR2 | 数据库URL |
| USERNAME | VARCHAR2 | 用户名 |
| PASSWORD | VARCHAR2 | 密码（加密） |
| DB_TYPE | VARCHAR2 | 数据库类型 |
| STATUS | VARCHAR2 | 状态 |
| CREATE_TIME | DATE | 创建时间 |

---

## Quartz调度表

### QRTZ_LOCKS - Quartz锁表

**行数：** 2
**最后分析：** 2026-08-18
**主键：** SCHED_NAME, LOCK_NAME

**核心字段：**
| 字段 | 类型 | 说明 |
|------|------|------|
| SCHED_NAME | VARCHAR2 | 调度器名称 |
| LOCK_NAME | VARCHAR2 | 锁名称 |

---

## 缓存表

### UP_CACHEOBJECT_TIMESTAMP - 缓存对象时间戳表

**行数：** 232
**最后分析：** 2026-08-18
**主键：** CACHE_KEY

**核心字段：**
| 字段 | 类型 | 说明 |
|------|------|------|
| CACHE_KEY | VARCHAR2 | 缓存键（主键） |
| TIMESTAMP | NUMBER | 时间戳 |
| CREATE_TIME | DATE | 创建时间 |

---

## 空表清单（103个）

### 工作流相关表（BT_*/RT_*）

| 表名 | 说明 |
|------|------|
| BT_ACTIVITY | 流程活动表（空） |
| BT_ACT_PARA | 活动参数表（空） |
| BT_ACT_TEMPLET | 活动模板表（空） |
| BT_APPLICATION | 流程申请表（空） |
| BT_PROCESS | 流程定义表（空） |
| BT_PROC_RELDATA | 流程关联数据表（空） |
| BT_TRANSITION | 流程转移表（空） |
| BT_RESOURCE_CATEGORY | 资源分类表（空） |
| BT_RESOURCE_CATEGORY_MAP | 资源分类映射表（空） |
| BT_PARTICIPANT | 流程参与者表（空） |
| BT_ACT_EVENT | 活动事件表（空） |
| BT_ACT_SUBPROC | 活动子流程表（空） |
| BT_COMPENSATION_ACT_MAP | 补偿活动映射表（空） |
| BT_EVENT_MAP | 事件映射表（空） |
| BT_EVENT_PARA | 事件参数表（空） |
| BT_PROC_CREATOR_MONITOR | 流程创建者监控表（空） |
| BT_RES_TIMER | 资源定时器表（空） |
| BT_SUBPROC_PARA | 子流程参数表（空） |
| BT_VARIABLE_TEMPLET | 变量模板表（空） |
| RT_ACTINST | 活动实例表（空） |
| RT_ACTINST_DATA | 活动实例数据表（空） |
| RT_PROCESSINSTANCE | 流程实例表（空） |
| RT_PROCINST_DATA | 流程实例数据表（空） |
| RT_WORKITEMINST | 工作项实例表（空） |
| RT_WORKITEM_DATA | 工作项数据表（空） |
| RT_ADHOC | 即席流程表（空） |
| RT_APPMSGQUEUE | 应用消息队列表（空） |
| RT_COMPENSATION_MQ | 补偿消息队列表（空） |
| RT_OVERTIME_DEAL | 超时处理表（空） |
| RT_ACTINST_DATA | 活动实例数据表（空） |
| RT_PROC_END_NOTIFY | 流程结束通知表（空） |
| RT_PROC_MONITOR | 流程监控表（空） |
| RT_RECEIVE_EVENT | 接收事件表（空） |
| RT_RETURNMSGQUEUE | 返回消息队列表（空） |
| RT_SEND_EVENT | 发送事件表（空） |
| RT_SEND_EVENT_VAR | 发送事件变量表（空） |

### 日志表（LOG_*）

| 表名 | 说明 |
|------|------|
| LOG_ACTINST | 活动实例日志表（空） |
| LOG_ACTINST_DATA | 活动实例数据日志表（空） |
| LOG_ENGINEMSG | 引擎消息日志表（空） |
| LOG_PROCINST | 流程实例日志表（空） |
| LOG_PROCINST_DATA | 流程实例数据日志表（空） |
| LOG_WORKITEMINST | 工作项实例日志表（空） |
| LOG_WORKITEM_DATA | 工作项数据日志表（空） |

### Quartz调度表（QRTZ_*）

| 表名 | 说明 |
|------|------|
| QRTZ_BLOB_TRIGGERS | Quartz BLOB触发器表（空） |
| QRTZ_CALENDARS | Quartz日历表（空） |
| QRTZ_CRON_TRIGGERS | Quartz Cron触发器表（空） |
| QRTZ_FIRED_TRIGGERS | Quartz已触发触发器表（空） |
| QRTZ_JOB_DETAILS | Quartz作业详情表（空） |
| QRTZ_JOB_LISTENERS | Quartz作业监听器表（空） |
| QRTZ_PAUSED_TRIGGER_GRPS | Quartz暂停触发器组表（空） |
| QRTZ_SCHEDULER_STATE | Quartz调度器状态表（空） |
| QRTZ_SIMPLE_TRIGGERS | Quartz简单触发器表（空） |
| QRTZ_TRIGGERS | Quartz触发器表（空） |
| QRTZ_TRIGGER_LISTENERS | Quartz触发器监听器表（空） |

### DRM配置表（DRM_*）

| 表名 | 说明 |
|------|------|
| DRM_SYS_CODEINTEGRATION | 代码集成配置表（空） |
| DRM_SYS_CONNECTIONPOOL | 连接池配置表（空） |
| DRM_SYS_DATASOURCETYPE | 数据源类型表（空） |
| DRM_SYS_ENVIRONMENTVARIABLE | 环境变量表（空） |
| DRM_SYS_FIELDCONVERTMAPPING | 字段转换映射表（空） |
| DRM_SYS_FIELDSASSOCIATION | 字段关联表（空） |
| DRM_SYS_FIELD_SCHEMA_EXTENDS | 字段模式扩展表（空） |
| DRM_SYS_MLTMEANINGFIELDS | 多义字段表（空） |
| DRM_SYS_MLTMEANINGFIELDSDETAIL | 多义字段详情表（空） |
| DRM_SYS_PUBLICCODEMAINTENANCE | 公共代码维护表（空） |
| DRM_SYS_TABLECONVERTMAPPING | 表转换映射表（空） |
| DRM_SYS_TABLE_SCHEMA_EXTENDS | 表模式扩展表（空） |

### 监控备份表（UP_MONITOR_*_BAK）

| 表名 | 说明 |
|------|------|
| UP_MONITOR_ACTION_BAK | Action监控备份表（空） |
| UP_MONITOR_BL | BL监控表（空） |
| UP_MONITOR_BL_BAK | BL监控备份表（空） |
| UP_MONITOR_RELATION_BAK | 关系监控备份表（空） |
| UP_MONITOR_SQL_BAK | SQL监控备份表（空） |
| UP_MONITOR_USER_BAK | 用户监控备份表（空） |

### 其他空表

| 表名 | 说明 |
|------|------|
| SYS_AGENT | 系统代理表（空） |
| SYS_NOTIFY | 系统通知表（空） |
| SYS_SERVICE_INFO | 系统服务信息表（空） |
| SYS_USER | 系统用户表（空） |
| SYS_WORKDAY | 工作日表（空） |
| SYS_WORK_DAYPART | 工作日时段表（空） |
| SYS_BIZDATA | 业务数据表（空） |
| UD_DATASET_CATEGORY | 数据集分类表（空） |
| UD_DATASET_DEFINE | 数据集定义表（空） |
| UM_METADATA | 元数据表（空） |
| UNIEAP_REPORT_RESULTCATEGORY | 报表结果分类表（空） |
| UNIEAP_REPORT_SCHEDULE | 报表调度表（空） |
| UNIEAP_REPORT_TEMPMEMO | 报表临时备注表（空） |
| UP_APP_TEMPLATE | 应用模板表（空） |
| UP_APP_TEMPLATE_EXT | 应用模板扩展表（空） |
| UP_APP_TEMPLATE_ROLE_MAP | 应用模板角色映射表（空） |
| UP_CATEGORY_CATEGORY | 分类分类表（空） |
| UP_CATEGORY_CATEGORYTYPE | 分类类型表（空） |
| UP_CATEGORY_DEPEND | 分类依赖表（空） |
| UP_CATEGORY_HUMANRESOURCE | 分类人力资源表（空） |
| UP_CATEGORY_NORMALRESOURCE | 分类普通资源表（空） |
| UP_INDIVIDUAL | 个人设置表（空） |
| UP_INDIVIDUAL_DESKTOP | 个人桌面表（空） |
| UP_INDIVIDUAL_DESKTOPCELL | 个人桌面单元表（空） |
| UP_INDIVIDUAL_ECSIDE | 个人侧边栏表（空） |
| UP_INFOTIP | 信息提示表（空） |
| UP_LEMIS_EXT_MENU | Lemis扩展菜单表（空） |
| UP_LEMIS_EXT_MENU_20210603 | Lemis扩展菜单备份表（空） |
| UP_LOGIN_ACCOUNTLIST | 登录账户列表表（空） |
| UP_LOGIN_ACCOUNT_BACKUP | 登录账户备份表（空） |
| UP_LOGIN_USERLIST | 登录用户列表表（空） |
| UP_LOG_LOGTABLE | 日志表（空） |
| UP_ORG_BUSIROLE_UNIT | 业务角色组织表（空） |
| UP_ORG_DELEROLE | 删除角色表（空） |
| UP_ORG_DYNAMICROLE | 动态角色表（空） |
| UP_ORG_DYNAMIC_CONDITION | 动态条件表（空） |
| UP_ORG_PAGEAUTHORITYLIST | 页面权限列表表（空） |
| UP_ORG_ROLE_EXROLE | 角色扩展角色表（空） |
| UP_ORG_ROLE_ROLE_2 | 角色角色关系表2（空） |
| UP_ORG_SERVICE_AUDIT | 服务审计表（空） |
| UP_ORG_STATION | 岗位表（空） |
| UP_ORG_STATION_EXSTATION | 岗位扩展岗位表（空） |
| UP_ORG_STATION_EXT | 岗位扩展表（空） |
| UP_ORG_USER_STATION | 用户岗位表（空） |
| UP_ORG_UNIT_EXT | 组织扩展表（空） |
| UP_ORG_USER_EXT | 用户扩展表（空） |
| UP_QUERY_CATALOG | 查询目录表（空） |
| UP_QUERY_DEF | 查询定义表（空） |
| UP_QUERY_INST | 查询实例表（空） |
| UP_QUERY_TMP | 查询临时表（空） |
| UP_QUERY_VIEW | 查询视图表（空） |
| UP_RES_PAGE | 资源页面表（空） |
| UP_SECURITY_AUDIT | 安全审计表（空） |
| UP_SECURITY_DATA_FILTER | 数据过滤表（空） |
| UP_SECURITY_DATA_FILTER_EXP | 数据过滤表达式表（空） |
| UP_SECURITY_DATA_FILTER_VALUE | 数据过滤值表（空） |
| UU_REPORT_LIU | 报表刘表（空） |
| BIZDATA | 业务数据表（空） |
| F_PRVROLE | 预留角色表（空） |
| PHISTORY | 历史表（空） |
| RES_EVENT | 资源事件表（空） |
| RES_EVENT_VAR | 资源事件变量表（空） |
| UP_ORG_UNIT_EXT_LEMIS_BAK | 组织扩展Lemis备份表（空） |
| UP_ORG_UNIT_BAK | 组织备份表（空） |

---

## 数据维护建议

### 1. 定期清理监控数据

```sql
-- 清理超过30天的监控关系数据
DELETE FROM UP_MONITOR_RELATION 
WHERE CREATE_TIME < SYSDATE - 30;
COMMIT;

-- 清理超过30天的监控SQL数据
DELETE FROM UP_MONITOR_SQL 
WHERE CREATE_TIME < SYSDATE - 30;
COMMIT;

-- 清理超过30天的监控Action数据
DELETE FROM UP_MONITOR_ACTION 
WHERE CREATE_TIME < SYSDATE - 30;
COMMIT;
```

### 2. 定期清理登录日志

```sql
-- 清理超过90天的登录日志
DELETE FROM LEMIS_LOGIN_USERINFO 
WHERE LOGIN_TIME < SYSDATE - 90;
COMMIT;
```

### 3. 定期清理缓存

```sql
-- 清理过期缓存
DELETE FROM UP_CACHEOBJECT_TIMESTAMP 
WHERE TIMESTAMP < (DBMS_UTILITY.GET_TIME - 3600000); -- 1小时前
COMMIT;
```

### 4. 定期分析表

```sql
-- 分析核心表
ANALYZE TABLE UP_ORG_USER COMPUTE STATISTICS;
ANALYZE TABLE UP_ORG_BUSINESSROLE COMPUTE STATISTICS;
ANALYZE TABLE UP_RIA_MENU COMPUTE STATISTICS;
ANALYZE TABLE UP_SECURITY_PERMISSION COMPUTE STATISTICS;
ANALYZE TABLE UP_MONITOR_RELATION COMPUTE STATISTICS;
ANALYZE TABLE LEMIS_LOGIN_USERINFO COMPUTE STATISTICS;
```

### 5. 备份重要表

```sql
-- 备份用户表
CREATE TABLE UP_ORG_USER_BAK_20260818 AS SELECT * FROM UP_ORG_USER;

-- 备份角色表
CREATE TABLE UP_ORG_BUSINESSROLE_BAK_20260818 AS SELECT * FROM UP_ORG_BUSINESSROLE;

-- 备份菜单表
CREATE TABLE UP_RIA_MENU_BAK_20260818 AS SELECT * FROM UP_RIA_MENU;
```

---

## 总结

### 核心发现

1. **表数量**
   - 共 177 个表
   - 103 个空表（58.2%）
   - 52 个极少数据表（<100行）
   - 14 个少量数据表（100-1000行）
   - 6 个中等数据表（1000-1万行）
   - 2 个较多数据表（>1万行）

2. **核心业务表**
   - UP_MONITOR_RELATION（监控关系）：13,856行
   - LEMIS_LOGIN_USERINFO（登录日志）：10,946行
   - UP_MONITOR_SQL（监控SQL）：8,086行
   - UP_ORG_RESOURCE_KANBX（资源看板）：5,844行
   - UP_CODELIST（代码表）：5,804行

3. **空表分析**
   - 103个空表中，大部分是工作流、日志、备份表
   - 这些表可能是预留表或已废弃
   - 建议定期清理

4. **监控数据**
   - 监控相关表数据量大，需要定期清理
   - 建议保留30天内的监控数据

5. **登录日志**
   - 登录日志表数据量大，需要定期清理
   - 建议保留90天内的登录日志

---

**文档版本：** 1.0  
**更新日期：** 2026-08-18  
**数据来源：** Oracle 数据库 `172.16.0.60:1521/orcl`  
**维护人：** Sisyphus
