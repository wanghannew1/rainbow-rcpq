# 写入重放战役 07 — tdbf 处理层 xinqcl 双路径(2026-11-21, 8091)

员工5 `重放处理员`(AAC001=998005/ATC900=70768/ATB910=27152331)。**再次提交+取消 双路径打穿**,
终局零残留(含 TC90_BG 孤儿清理)。

## 实录

| 路径 | 交互 | 服务端 | 验证 |
|---|---|---|---|
| 再次提交 | `REQ-tdbf__dialog_xinqcl-commit`(注意双下划线) @ tdbf_dialog_xinqcl | `doXinqCommit`: **TC90_BG 逐列留痕 ← `_o` 字典**(getModifyColName) + saveOrUpdate 两实体 | **TC90_BG 落 row(columnname='aae013')** |
| 取消 | `REQ-tdbf_dialog_xinqcl-cancle`(拼写就是这么烂) | `doXinqCancle`: bulkUpdate 物理删 `TC90 where atc900=` + `TB91 where atb910=` | TC90/TB91 双清 0 |

## 行为语义(重写必读)

1. **TC90_BG 是字段级审计留痕**: 每次处理层再提交, 变更列(columnname+atc900)逐行落 TC90_BG——
   驱动源是 `_o` 旧值字典, 即**留痕范围=前端声明**, 服务端不做 diff。
2. **取消路径不清理 TC90_BG**(孤儿留痕)——墓碑效应: 留痕行比主数据活得久。重写需决定
   留痕生命周期(本次测试孤儿已清)。
3. 物理删直接拼 HQL(`atc900=`+值)——长整型注入面, 重写换参数绑定。
4. tdbf 处理层弹窗按 atb911 分流(20/21/23/24/26), **'25' 无分支静默失败**(文档断言, 代码级已核;
   行为级未重放——需要先造 '25' 打回单, 留待补测)。
5. 其余处理弹窗记录(文档+代码级, 未实弹): bgcl '21'=通用 update 重提(同 04 卷线型);
   zzcl '23' 再提交需退费数据+PRC_GENERATE_TFMX(重前置); dzjsdycl '26' 取消=`update TC90 set
   atb90r='3' where atb910=`+删 TB91(可用 06 卷产物补测)。
