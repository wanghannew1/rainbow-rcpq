# 写入重放战役 33 — xa/h 问题管理平台 + 遗产 bug 链条(2026-11-23, jiaq)

## xa/h/* 问题管理平台(综合信息管理→在线问题管理)

| 页 | 功能 | 通道 | 状态 |
|---|---|---|---|
| xaha 问题登记 | SYS_QUESTION 插入 | `REQ-xaha_cont-sys_questionStore_List-U` | ✅ 200 落库(qid=null!) |
| xahb 信息中心审核 | SYS_QUESTION qstate 翻转 | `REQ-xahb_dialog-sys_questionStore-M` | ❌ 500 :83 指纹 |
| xahc 开发确认 | 同表翻转 | 同上族 | 未测(依赖 xahb) |
| xahd 问题确认 | 同表关闭 | 同上族 | 未测 |

## **遗产 bug 链条(三层级联)**

**底层 bug**: xaha 登记交互(U 型)通过 generic update 写 SYS_QUESTION 时，
**序列生成器未配号**(qid=null)——hibernate 对 assigned-id 实体在有值时跳过序列。
但 SYS_QUESTION 的 qid 本应为序列必配——**登记功能产出的每条问题都缺 PK**。

**中层影响**: qid=null ⇒ 审核页(xahb)的修改无法定位行(PK 匹配失效) ⇒ 审核功能不可达。

**上层表现**: 用户在问题登记页提交的问题**永远停留在 qstate='1'**，
无法被审核/开发/确认流程处理——整个问题管理平台自始不可用。

## 重写指令

- SYS_QUESTION.qid 必须始终由序列 SEQ_SYS_QUESTION_QSEQ 赋值(非空)
- qstate 状态机: '1'(登记)→'2'(审核)→'3'(开发确认)→'4'(关闭)，每步由对应页面驱动
- 遗产版本此功能**全链报废**(登记产出即缺陷品)，重写需完整实现四步流程

## 清场

SYS_QUESTION 测试行删 1(jiaq 谓词)；全库零残留确认。
