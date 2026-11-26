# 新旧对照总账（自动生成）

> 生成 2026-11-24。旧侧真值=备份库 user_source/user_views/user_sequences/user_tables 探针(scripts/db/work/LegacyInventory.java → legacy_*.tsv)+webapp 源码 grep；新侧真值=运行注册面(/proc)+interactions.json+PG 实况。分区引用既有的 entity-table-map.md / wxyj-frontend-parity.md / business-process.md §四，不重复。

**对照文档族**：本账（机读总账：过程/交互/表/视图/序列/忠实标注）· `parity-tdb.md`（员工管理）· `parity-xaaz.md`（字典/单位/人员）· `parity-tdp.md`（导入）· `parity-tdd.md`（工资）· `parity-roles.md`（角色业务链，29 角色权限模型）· `parity-ui.md`（界面对照）· `wxyj-frontend-parity.md`（五险，样板）；流程= business-process.md §四；通道缺口= rewrite-spec/README.md。

## 1. 存储过程对照（旧 525 过程 ↔ 新注册 110）

### 1.1 已移植对照（同名注册 110）

| 过程 | 旧包(spec行) | 新实现 | e2e/冒烟直调 |
|---|---|---|---|
| PRC_AC01_GX_IMP | PKG_IMP(171) | `rcpq-service/src/main/kotlin/com/jlrc/rcpq/service/td/TdbpImportCases.kt:2836` |  |
| PRC_CHECK_TC93 | PKG_IMP(117) | `rcpq-service/src/main/kotlin/com/jlrc/rcpq/service/td/TdbpImportCases.kt:3019` | ✓ |
| PRC_COMMIT_TC93 | PKG_IMP(124) | `rcpq-service/src/main/kotlin/com/jlrc/rcpq/service/td/TdbpImportCases.kt:3030` | ✓ |
| PRC_CHECK_TC9W_HZ_IMP | PKG_IMP_FZ(14) | `rcpq-service/src/main/kotlin/com/jlrc/rcpq/service/td/TdcImpHzCheckComm.kt:86` |  |
| PRC_CHECK_TC9W_HZ_SUCCESS_IMP | PKG_IMP_FZ(25) | `rcpq-service/src/main/kotlin/com/jlrc/rcpq/service/td/TdcImpHzCases.kt:160` |  |
| PRC_CHECK_TC9W_HZ_SUCCESS_IMP2 | PKG_IMP_FZ(33) | `rcpq-service/src/main/kotlin/com/jlrc/rcpq/service/td/TdcImpHzCases.kt:172` |  |
| PRC_COMMIT_AAC001 | PKG_IMP_FZ(37) | `rcpq-service/src/main/kotlin/com/jlrc/rcpq/service/td/TdcImpHzCases.kt:193` |  |
| PRC_COMMIT_TC90_IMP | PKG_IMP_FZ(6) | `rcpq-service/src/main/kotlin/com/jlrc/rcpq/service/td/TdcTc90ImpCases.kt:510` |  |
| PRC_COMMIT_TC9W_HZ_SUCCESS_IMP | PKG_IMP_FZ(30) | `rcpq-service/src/main/kotlin/com/jlrc/rcpq/service/td/TdcImpHzCases.kt:184` |  |
| PRC_COMM_TC9W_HZ_IMP | PKG_IMP_FZ(19) | `rcpq-service/src/main/kotlin/com/jlrc/rcpq/service/td/TdcImpHzCheckComm.kt:99` |  |
| PRC_TC90_IMP | PKG_IMP_FZ(3) | `rcpq-service/src/main/kotlin/com/jlrc/rcpq/service/td/TdcTc90ImpCases.kt:500` |  |
| PRC_CANCLESALARYCOMPUTE | PKG_RCPQ_FZ(114) | `rcpq-service/src/main/kotlin/com/jlrc/rcpq/service/td/TddSettlementCases.kt:272` | ✓ |
| PRC_SALARYCOMPUTE | PKG_RCPQ_FZ(106) | `rcpq-service/src/main/kotlin/com/jlrc/rcpq/service/td/TddSettlementCases.kt:256` | ✓ |
| PRC_保险补收基数_个人 | PKG_人才派遣公共(73) | `rcpq-service/src/main/kotlin/com/jlrc/rcpq/service/td/Jishu6Cases.kt:566` | ✓ |
| PRC_测试计算险金过程 | PKG_人才派遣公共(44) | `rcpq-service/src/main/kotlin/com/jlrc/rcpq/service/calc/CalcProcs.kt:85` |  |
| PRC_获取补退基数 | PKG_人才派遣公共(63) | `rcpq-service/src/main/kotlin/com/jlrc/rcpq/service/calc/CalcProcs.kt:121` |  |
| PRC_计算险金 | PKG_人才派遣公共(38) | `rcpq-service/src/main/kotlin/com/jlrc/rcpq/service/calc/CalcProcs.kt:28` | ✓ |
| PRC_保险补差确认 | PKG_保险人员增减(291) | `rcpq-service/src/main/kotlin/com/jlrc/rcpq/service/td/Jishu6Cases.kt:551` | ✓ |
| PRC_公积金封存回盘提交 | PKG_保险人员增减(209) | `rcpq-service/src/main/kotlin/com/jlrc/rcpq/service/td/TdcgjCases.kt:430` | ✓ |
| PRC_公积金封存回盘校验 | PKG_保险人员增减(204) | `rcpq-service/src/main/kotlin/com/jlrc/rcpq/service/td/TdcgjCases.kt:418` | ✓ |
| PRC_公积金异动回盘提交 | PKG_保险人员增减(198) | `rcpq-service/src/main/kotlin/com/jlrc/rcpq/service/td/TdcgjCases.kt:403` | ✓ |
| PRC_公积金异动回盘校验 | PKG_保险人员增减(192) | `rcpq-service/src/main/kotlin/com/jlrc/rcpq/service/td/TdcgjCases.kt:390` | ✓ |
| PRC_公积金异议回盘提交 | PKG_保险人员增减(182) | `rcpq-service/src/main/kotlin/com/jlrc/rcpq/service/td/TdcgjCases.kt:456` | ✓ |
| PRC_公积金异议回盘校验 | PKG_保险人员增减(177) | `rcpq-service/src/main/kotlin/com/jlrc/rcpq/service/td/TdcgjCases.kt:443` | ✓ |
| PRC_医保异议同步 | PKG_保险人员增减(285) | `rcpq-service/src/main/kotlin/com/jlrc/rcpq/service/td/Jishu6Cases.kt:508` | ✓ |
| PRC_医保退休停保 | PKG_保险人员增减(218) | `rcpq-service/src/main/kotlin/com/jlrc/rcpq/service/td/Jishu6Cases.kt:530` | ✓ |
| PRC_变更参保地 | PKG_保险人员增减(222) | `rcpq-service/src/main/kotlin/com/jlrc/rcpq/service/td/Jishu6Cases.kt:540` | ✓ |
| PRC_市医保异动回盘提交 | PKG_保险人员增减(121) | `rcpq-service/src/main/kotlin/com/jlrc/rcpq/service/td/TdcAyCases.kt:336` | ✓ |
| PRC_市医保异动回盘校验 | PKG_保险人员增减(116) | `rcpq-service/src/main/kotlin/com/jlrc/rcpq/service/td/TdcAyCases.kt:323` | ✓ |
| PRC_市工伤异动回盘提交 | PKG_保险人员增减(262) | `rcpq-service/src/main/kotlin/com/jlrc/rcpq/service/td/TdcbpCases.kt:885` | ✓ |
| PRC_市工伤异动回盘校验 | PKG_保险人员增减(257) | `rcpq-service/src/main/kotlin/com/jlrc/rcpq/service/td/TdcbpCases.kt:873` | ✓ |
| PRC_异议处理 | PKG_保险人员增减(227) | `rcpq-service/src/main/kotlin/com/jlrc/rcpq/service/td/Jishu6Cases.kt:490` | ✓ |
| PRC_异议离职同步 | PKG_保险人员增减(290) | `rcpq-service/src/main/kotlin/com/jlrc/rcpq/service/td/Jishu6Cases.kt:501` | ✓ |
| PRC_省医保基数补差回盘提交 | PKG_保险人员增减(79) | `rcpq-service/src/main/kotlin/com/jlrc/rcpq/service/td/TdcahCases.kt:465` | ✓ |
| PRC_省医保基数补差回盘校验 | PKG_保险人员增减(76) | `rcpq-service/src/main/kotlin/com/jlrc/rcpq/service/td/TdcahCases.kt:454` | ✓ |
| PRC_省医保工资表 | PKG_保险人员增减(96) | `rcpq-service/src/main/kotlin/com/jlrc/rcpq/service/td/TdcahCases.kt:477` | ✓ |
| PRC_省医保异动回盘提交 | PKG_保险人员增减(70) | `rcpq-service/src/main/kotlin/com/jlrc/rcpq/service/td/TdcahCases.kt:440` | ✓ |
| PRC_省医保异动回盘校验 | PKG_保险人员增减(65) | `rcpq-service/src/main/kotlin/com/jlrc/rcpq/service/td/TdcahCases.kt:427` | ✓ |
| PRC_省工伤减免提交 | PKG_保险人员增减(272) | `rcpq-service/src/main/kotlin/com/jlrc/rcpq/service/td/TdcbpCases.kt:838` | ✓ |
| PRC_省工伤减免校验 | PKG_保险人员增减(269) | `rcpq-service/src/main/kotlin/com/jlrc/rcpq/service/td/TdcbpCases.kt:827` | ✓ |
| PRC_省工伤基数补差回盘提交 | PKG_保险人员增减(89) | `rcpq-service/src/main/kotlin/com/jlrc/rcpq/service/td/TdcbpCases.kt:861` | ✓ |
| PRC_省工伤基数补差回盘校验 | PKG_保险人员增减(86) | `rcpq-service/src/main/kotlin/com/jlrc/rcpq/service/td/TdcbpCases.kt:850` | ✓ |
| PRC_省工伤异动回盘提交 | PKG_保险人员增减(244) | `rcpq-service/src/main/kotlin/com/jlrc/rcpq/service/td/TdcbpCases.kt:814` | ✓ |
| PRC_省工伤异动回盘校验 | PKG_保险人员增减(239) | `rcpq-service/src/main/kotlin/com/jlrc/rcpq/service/td/TdcbpCases.kt:802` | ✓ |
| PRC_社保异动回盘提交 | PKG_保险人员增减(40) | `rcpq-service/src/main/kotlin/com/jlrc/rcpq/service/td/TdckProcs.kt:22` | ✓ |
| PRC_社保异动回盘校验 | PKG_保险人员增减(35) | `rcpq-service/src/main/kotlin/com/jlrc/rcpq/service/td/TdckProcs.kt:9` | ✓ |
| PRC_社保补缴明细 | PKG_保险人员增减(46) | `rcpq-service/src/main/kotlin/com/jlrc/rcpq/service/td/TdckPaymentDetail.kt:228` | ✓ |
| PRC_社保退休停保 | PKG_保险人员增减(214) | `rcpq-service/src/main/kotlin/com/jlrc/rcpq/service/td/Jishu6Cases.kt:520` | ✓ |
| PRC_保险当月补缴 | PKG_保险补退(20) | `rcpq-service/src/main/kotlin/com/jlrc/rcpq/service/td/BbfCalc.kt:326` | ✓ |
| PRC_工伤月情况 | PKG_派遣保险月情况(107) | `rcpq-service/src/main/kotlin/com/jlrc/rcpq/service/td/Yueqk2Cases.kt:282` | ✓ |
| PRC_工伤补缴情况 | PKG_派遣保险月情况(119) | `rcpq-service/src/main/kotlin/com/jlrc/rcpq/service/td/Yueqk5Cases.kt:462` |  |
| PRC_市公积金月情况 | PKG_派遣保险月情况(83) | `rcpq-service/src/main/kotlin/com/jlrc/rcpq/service/td/Yueqk2Cases.kt:265` | ✓ |
| PRC_市公积金补缴情况 | PKG_派遣保险月情况(95) | `rcpq-service/src/main/kotlin/com/jlrc/rcpq/service/td/Yueqk5Cases.kt:445` |  |
| PRC_市医保应缴费情况 | PKG_派遣保险月情况(53) | `rcpq-service/src/main/kotlin/com/jlrc/rcpq/service/td/Yueqk4Cases.kt:104` | ✓ |
| PRC_市医保月情况 | PKG_派遣保险月情况(41) | `rcpq-service/src/main/kotlin/com/jlrc/rcpq/service/td/Yueqk3Cases.kt:256` | ✓ |
| PRC_月垫付情况 | PKG_派遣保险月情况(172) | `rcpq-service/src/main/kotlin/com/jlrc/rcpq/service/td/Yueqk6Cases.kt:379` | ✓ |
| PRC_月转款确认 | PKG_派遣保险月情况(149) | `rcpq-service/src/main/kotlin/com/jlrc/rcpq/service/td/YueqkCases.kt:245` | ✓ |
| PRC_省公积金月情况 | PKG_派遣保险月情况(59) | `rcpq-service/src/main/kotlin/com/jlrc/rcpq/service/td/Yueqk2Cases.kt:248` | ✓ |
| PRC_省公积金补缴情况 | PKG_派遣保险月情况(71) | `rcpq-service/src/main/kotlin/com/jlrc/rcpq/service/td/Yueqk5Cases.kt:479` |  |
| PRC_省医保月情况 | PKG_派遣保险月情况(17) | `rcpq-service/src/main/kotlin/com/jlrc/rcpq/service/td/Yueqk3Cases.kt:239` | ✓ |
| PRC_省医保补缴情况 | PKG_派遣保险月情况(29) | `rcpq-service/src/main/kotlin/com/jlrc/rcpq/service/td/Yueqk5Cases.kt:428` |  |
| PRC_社保月情况 | PKG_派遣保险月情况(5) | `rcpq-service/src/main/kotlin/com/jlrc/rcpq/service/td/YueqkCases.kt:228` | ✓ |
| PRC_保险补贴导入提交 | PKG_派遣批量导入(68) | `rcpq-service/src/main/kotlin/com/jlrc/rcpq/service/td/TdbpImportCases.kt:2979` |  |
| PRC_保险补贴导入校验 | PKG_派遣批量导入(64) | `rcpq-service/src/main/kotlin/com/jlrc/rcpq/service/td/TdbpImportCases.kt:2969` |  |
| PRC_发票导入提交 | PKG_派遣批量导入(128) | `rcpq-service/src/main/kotlin/com/jlrc/rcpq/service/td/TdbpImportCases.kt:2911` | ✓ |
| PRC_发票导入校验 | PKG_派遣批量导入(125) | `rcpq-service/src/main/kotlin/com/jlrc/rcpq/service/td/TdbpImportCases.kt:2902` | ✓ |
| PRC_吉林银行到账导入提交 | PKG_派遣批量导入(121) | `rcpq-service/src/main/kotlin/com/jlrc/rcpq/service/td/TdbpImportCases.kt:2892` |  |
| PRC_吉林银行到账导入校验 | PKG_派遣批量导入(118) | `rcpq-service/src/main/kotlin/com/jlrc/rcpq/service/td/TdbpImportCases.kt:2883` |  |
| PRC_员工导入提交 | PKG_派遣批量导入(22) | `rcpq-service/src/main/kotlin/com/jlrc/rcpq/service/td/TdbpImportCases.kt:2731` | ✓ |
| PRC_员工导入校验 | PKG_派遣批量导入(18) | `rcpq-service/src/main/kotlin/com/jlrc/rcpq/service/td/TdbpImportCases.kt:2721` | ✓ |
| PRC_员工更新提交 | PKG_派遣批量导入(84) | `rcpq-service/src/main/kotlin/com/jlrc/rcpq/service/td/TdbpImportCases.kt:2771` | ✓ |
| PRC_员工更新校验 | PKG_派遣批量导入(80) | `rcpq-service/src/main/kotlin/com/jlrc/rcpq/service/td/TdbpImportCases.kt:2761` | ✓ |
| PRC_员工终止导入提交 | PKG_派遣批量导入(30) | `rcpq-service/src/main/kotlin/com/jlrc/rcpq/service/td/TdbpImportCases.kt:2751` | ✓ |
| PRC_员工终止导入校验 | PKG_派遣批量导入(26) | `rcpq-service/src/main/kotlin/com/jlrc/rcpq/service/td/TdbpImportCases.kt:2741` | ✓ |
| PRC_员工续签提交 | PKG_派遣批量导入(92) | `rcpq-service/src/main/kotlin/com/jlrc/rcpq/service/td/TdbpImportCases.kt:2791` | ✓ |
| PRC_员工续签校验 | PKG_派遣批量导入(88) | `rcpq-service/src/main/kotlin/com/jlrc/rcpq/service/td/TdbpImportCases.kt:2781` | ✓ |
| PRC_工行到账导入提交 | PKG_派遣批量导入(107) | `rcpq-service/src/main/kotlin/com/jlrc/rcpq/service/td/TdbpImportCases.kt:2854` | ✓ |
| PRC_工行到账导入校验 | PKG_派遣批量导入(104) | `rcpq-service/src/main/kotlin/com/jlrc/rcpq/service/td/TdbpImportCases.kt:2845` | ✓ |
| PRC_序列 | PKG_派遣批量导入(4) | `rcpq-service/src/main/kotlin/com/jlrc/rcpq/service/td/TdbpImportCases.kt:2822` |  |
| PRC_建行到账导入提交 | PKG_派遣批量导入(114) | `rcpq-service/src/main/kotlin/com/jlrc/rcpq/service/td/TdbpImportCases.kt:2873` |  |
| PRC_建行到账导入校验 | PKG_派遣批量导入(111) | `rcpq-service/src/main/kotlin/com/jlrc/rcpq/service/td/TdbpImportCases.kt:2864` |  |
| PRC_意外险导入提交 | PKG_派遣批量导入(76) | `rcpq-service/src/main/kotlin/com/jlrc/rcpq/service/td/TdbpImportCases.kt:2999` |  |
| PRC_意外险导入校验 | PKG_派遣批量导入(72) | `rcpq-service/src/main/kotlin/com/jlrc/rcpq/service/td/TdbpImportCases.kt:2989` |  |
| PRC_新增保险提交 | PKG_派遣批量导入(100) | `rcpq-service/src/main/kotlin/com/jlrc/rcpq/service/td/TdbpImportCases.kt:2811` | ✓ |
| PRC_新增保险校验 | PKG_派遣批量导入(96) | `rcpq-service/src/main/kotlin/com/jlrc/rcpq/service/td/TdbpImportCases.kt:2801` | ✓ |
| PRC_特殊情况导入提交 | PKG_派遣批量导入(38) | `rcpq-service/src/main/kotlin/com/jlrc/rcpq/service/td/TdbpImportCases.kt:2940` |  |
| PRC_特殊情况导入校验 | PKG_派遣批量导入(34) | `rcpq-service/src/main/kotlin/com/jlrc/rcpq/service/td/TdbpImportCases.kt:3009` |  |
| PRC_生育补贴提交 | PKG_派遣批量导入(55) | `rcpq-service/src/main/kotlin/com/jlrc/rcpq/service/td/TdbpImportCases.kt:2959` |  |
| PRC_生育补贴校验 | PKG_派遣批量导入(52) | `rcpq-service/src/main/kotlin/com/jlrc/rcpq/service/td/TdbpImportCases.kt:2950` |  |
| PRC_补偿金导入提交 | PKG_派遣批量导入(135) | `rcpq-service/src/main/kotlin/com/jlrc/rcpq/service/td/TdbpImportCases.kt:2930` |  |
| PRC_补偿金导入校验 | PKG_派遣批量导入(132) | `rcpq-service/src/main/kotlin/com/jlrc/rcpq/service/td/TdbpImportCases.kt:2921` |  |
| PRC_公积金基数上下限模板 | PKG_缴费基数(60) | `rcpq-service/src/main/kotlin/com/jlrc/rcpq/service/td/Jishu5Tc0aCases.kt:565` | ✓ |
| PRC_公积金基数上下限调整审核 | PKG_缴费基数(70) | `rcpq-service/src/main/kotlin/com/jlrc/rcpq/service/td/Jishu5Tc0aCases.kt:593` | ✓ |
| PRC_公积金基数上下限调整提交 | PKG_缴费基数(66) | `rcpq-service/src/main/kotlin/com/jlrc/rcpq/service/td/Jishu5Tc0aCases.kt:583` | ✓ |
| PRC_公积金基数上下限调整校验 | PKG_缴费基数(63) | `rcpq-service/src/main/kotlin/com/jlrc/rcpq/service/td/Jishu5Tc0aCases.kt:574` | ✓ |
| PRC_公积金基数下限调整 | PKG_缴费基数(76) | `rcpq-service/src/main/kotlin/com/jlrc/rcpq/service/td/Jishu5Tc0aCases.kt:606` | ✓ |
| PRC_公积金未申报 | PKG_缴费基数(98) | `rcpq-service/src/main/kotlin/com/jlrc/rcpq/service/td/Jishu5WsbCases.kt:196` | ✓ |
| PRC_公积金缴费基数核对 | PKG_缴费基数(48) | `rcpq-service/src/main/kotlin/com/jlrc/rcpq/service/td/Jishu5Cases.kt:413` |  |
| PRC_公积金缴费基数确认 | PKG_缴费基数(53) | `rcpq-service/src/main/kotlin/com/jlrc/rcpq/service/td/Jishu5Cases.kt:474` |  |
| PRC_医保未申报 | PKG_缴费基数(89) | `rcpq-service/src/main/kotlin/com/jlrc/rcpq/service/td/Jishu5WsbCases.kt:190` | ✓ |
| PRC_医保缴费基数核对 | PKG_缴费基数(24) | `rcpq-service/src/main/kotlin/com/jlrc/rcpq/service/td/Jishu5Cases.kt:398` |  |
| PRC_医保缴费基数确认 | PKG_缴费基数(29) | `rcpq-service/src/main/kotlin/com/jlrc/rcpq/service/td/Jishu5Cases.kt:468` |  |
| PRC_基数导入提交 | PKG_缴费基数(8) | `rcpq-service/src/main/kotlin/com/jlrc/rcpq/service/td/Jishu5ImportCases.kt:429` | ✓ |
| PRC_基数导入校验 | PKG_缴费基数(4) | `rcpq-service/src/main/kotlin/com/jlrc/rcpq/service/td/Jishu5ImportCases.kt:415` | ✓ |
| PRC_工伤未申报 | PKG_缴费基数(107) | `rcpq-service/src/main/kotlin/com/jlrc/rcpq/service/td/Jishu5WsbCases.kt:202` | ✓ |
| PRC_工伤缴费基数核对 | PKG_缴费基数(36) | `rcpq-service/src/main/kotlin/com/jlrc/rcpq/service/td/Jishu5Cases.kt:428` |  |
| PRC_工伤缴费基数确认 | PKG_缴费基数(41) | `rcpq-service/src/main/kotlin/com/jlrc/rcpq/service/td/Jishu5Cases.kt:480` |  |
| PRC_社保未申报 | PKG_缴费基数(80) | `rcpq-service/src/main/kotlin/com/jlrc/rcpq/service/td/Jishu5WsbCases.kt:184` | ✓ |
| PRC_社保缴费基数核对 | PKG_缴费基数(12) | `rcpq-service/src/main/kotlin/com/jlrc/rcpq/service/td/Jishu5Cases.kt:383` |  |
| PRC_社保缴费基数确认 | PKG_缴费基数(17) | `rcpq-service/src/main/kotlin/com/jlrc/rcpq/service/td/Jishu5Cases.kt:462` |  |

### 1.2 新注册但旧库无同名（0）


### 1.3 旧有而新未注册（415）——结案/死码/未含, 逐条归属见 module-inventory 波次行

| 过程 | 旧包(spec行) |
|---|---|
| DELETE1.PRINT | DELETE1(5) |
| F.P_INSERT_COLUMN | F(64) |
| F.P_UPDATE_VALUE | F(69) |
| FUNC_AAC00215_18 | (独立FUNCTION)() |
| FUN_CHECK_DATE | (独立FUNCTION)() |
| FUN_R_AA12_GETLABEL | (独立FUNCTION)() |
| FUN_R_AAA102_AAA103 | (独立FUNCTION)() |
| FUN_R_CA18_LABEL | (独立FUNCTION)() |
| FUN_保险计算 | (独立FUNCTION)() |
| FUN_缴费基数 | (独立FUNCTION)() |
| F_AA10 | (独立FUNCTION)() |
| F_AA10_DETAIL | (独立FUNCTION)() |
| F_BIRTHDAY_TO_AGE | (独立FUNCTION)() |
| F_BLOB2CLOB | (独立FUNCTION)() |
| F_GET_AAA005 | (独立FUNCTION)() |
| F_GET_SEQ | (独立FUNCTION)() |
| F_PARSE_MONEY | (独立FUNCTION)() |
| F_生成人事代理缴费编号 | (独立FUNCTION)() |
| GETAA10 | (独立PROCEDURE)() |
| PKG_CHECK.PRC_CHECK_GJJ | PKG_CHECK(19) |
| PKG_CHECK.PRC_CHECK_JFSJ | PKG_CHECK(22) |
| PKG_CHECK.PRC_CHECK_SB | PKG_CHECK(16) |
| PKG_CHECK.PRC_COMMIT_AAC001 | PKG_CHECK(41) |
| PKG_CHECK.PRC_CONFIRM_JFSJ | PKG_CHECK(28) |
| PKG_CHECK.PRC_CREATE_INNER | PKG_CHECK(2) |
| PKG_CHECK.PRC_CREATE_INNERA | PKG_CHECK(7) |
| PKG_CHECK.PRC_GET_JFSJ | PKG_CHECK(36) |
| PKG_CHECK.PRC_IMPORT_OUTTER | PKG_CHECK(12) |
| PKG_COMMON.FOUND_TABLE_COLUMNNAME | PKG_COMMON(85) |
| PKG_COMMON.PRM_CREAT_ORDERNUMBER | PKG_COMMON(88) |
| PKG_COMMON.P_DELETE | PKG_COMMON(79) |
| PKG_COMMON.P_GET_AA01 | PKG_COMMON(60) |
| PKG_COMMON.P_UPDATE_VALUE | PKG_COMMON(72) |
| PKG_DAGL.PRC_CHECK_TC97_IMP | PKG_DAGL(12) |
| PKG_DAGL.PRC_COMMIT_TC97_IMP | PKG_DAGL(16) |
| PKG_DAGL.PRC_GENERATE_DAML | PKG_DAGL(25) |
| PKG_DAGL.PRC_GET_ZDTZDBH | PKG_DAGL(7) |
| PKG_IMP.PRC_CHECK_AA97_IMP | PKG_IMP(32) |
| PKG_IMP.PRC_CHECK_CBJ | PKG_IMP(146) |
| PKG_IMP.PRC_CHECK_TC30_IMP | PKG_IMP(180) |
| PKG_IMP.PRC_CHECK_TC31_IMP | PKG_IMP(187) |
| PKG_IMP.PRC_CHECK_TC8G_BP | PKG_IMP(99) |
| PKG_IMP.PRC_CHECK_TC8G_BP_BCJ | PKG_IMP(174) |
| PKG_IMP.PRC_CHECK_TC8G_BP_GS | PKG_IMP(111) |
| PKG_IMP.PRC_CHECK_TC8G_BP_SY | PKG_IMP(105) |
| PKG_IMP.PRC_CHECK_TC90_IMP | PKG_IMP(2) |
| PKG_IMP.PRC_CHECK_TC93_IMP | PKG_IMP(48) |
| PKG_IMP.PRC_CHECK_TC9F_BP | PKG_IMP(134) |
| PKG_IMP.PRC_CHECK_TC9R_IMP | PKG_IMP(62) |
| PKG_IMP.PRC_CHECK_TC9U_HZ_IMP | PKG_IMP(24) |
| PKG_IMP.PRC_CHECK_TC9U_IMP | PKG_IMP(74) |
| PKG_IMP.PRC_CHECK_TC9V_HP | PKG_IMP(93) |
| PKG_IMP.PRC_CHECK_TC9V_IMP | PKG_IMP(82) |
| PKG_IMP.PRC_CHECK_TC9W_IMP | PKG_IMP(16) |
| PKG_IMP.PRC_COMMIT_CBJ | PKG_IMP(149) |
| PKG_IMP.PRC_COMMIT_TC90_IMP | PKG_IMP(6) |
| PKG_IMP.PRC_COMMIT_TC93_IMP | PKG_IMP(55) |
| PKG_IMP.PRC_COMMIT_TC97_WYH_IMP | PKG_IMP(167) |
| PKG_IMP.PRC_COMM_TC9U_HZ_IMP | PKG_IMP(28) |
| PKG_IMP.PRC_COM_AA97_IMP | PKG_IMP(37) |
| PKG_IMP.PRC_COM_TC30_IMP | PKG_IMP(183) |
| PKG_IMP.PRC_COM_TC31_IMP | PKG_IMP(191) |
| PKG_IMP.PRC_COM_TC8G_BP | PKG_IMP(102) |
| PKG_IMP.PRC_COM_TC8G_BP_BCJ | PKG_IMP(177) |
| PKG_IMP.PRC_COM_TC8G_BP_GS | PKG_IMP(114) |
| PKG_IMP.PRC_COM_TC8G_BP_SY | PKG_IMP(108) |
| PKG_IMP.PRC_COM_TC9F_BP | PKG_IMP(137) |
| PKG_IMP.PRC_COM_TC9R_IMP | PKG_IMP(66) |
| PKG_IMP.PRC_COM_TC9U_IMP | PKG_IMP(78) |
| PKG_IMP.PRC_COM_TC9V_HP | PKG_IMP(96) |
| PKG_IMP.PRC_COM_TC9V_IMP | PKG_IMP(85) |
| PKG_IMP.PRC_COM_TC9W_IMP | PKG_IMP(20) |
| PKG_IMP.PRC_TC91_IMP | PKG_IMP(153) |
| PKG_IMP.PRC_TC97_WYH_IMP | PKG_IMP(164) |
| PKG_IMP.PRC_TD01_COMMIT | PKG_IMP(160) |
| PKG_IMP.PRC_TD01_IMP | PKG_IMP(157) |
| PKG_JOB.PRC_CHECK_TC90 | PKG_JOB(2) |
| PKG_PINYIN.PRC_CREATE_PINYIN | PKG_PINYIN(14) |
| PKG_RCFP_TJ.PRC_CREATE_HZD | PKG_RCFP_TJ(9) |
| PKG_RCFP_TJ.PRC_TJ_01 | PKG_RCFP_TJ(14) |
| PKG_RCFP_TJ.PRC_TJ_02 | PKG_RCFP_TJ(21) |
| PKG_RCFP_TJ.PRC_TJ_03 | PKG_RCFP_TJ(29) |
| PKG_RCPQ.PRC_AC10_JS | PKG_RCPQ(253) |
| PKG_RCPQ.PRC_BJ_BACK | PKG_RCPQ(282) |
| PKG_RCPQ.PRC_CANCEL_TC9T | PKG_RCPQ(46) |
| PKG_RCPQ.PRC_COMFIRM_SH | PKG_RCPQ(225) |
| PKG_RCPQ.PRC_COMPUTE_TC93 | PKG_RCPQ(217) |
| PKG_RCPQ.PRC_COM_TC9T | PKG_RCPQ(33) |
| PKG_RCPQ.PRC_COM_TC9U | PKG_RCPQ(11) |
| PKG_RCPQ.PRC_DA_SH | PKG_RCPQ(286) |
| PKG_RCPQ.PRC_GENARATE_AA97 | PKG_RCPQ(239) |
| PKG_RCPQ.PRC_GET_PARA | PKG_RCPQ(265) |
| PKG_RCPQ.PRC_GJJ_SY | PKG_RCPQ(112) |
| PKG_RCPQ.PRC_INS_TC9T | PKG_RCPQ(22) |
| PKG_RCPQ.PRC_INS_TC9U | PKG_RCPQ(2) |
| PKG_RCPQ.PRC_MEASUREMENT_TOOLS_1 | PKG_RCPQ(52) |
| PKG_RCPQ.PRC_MEASUREMENT_TOOLS_2 | PKG_RCPQ(61) |
| PKG_RCPQ.PRC_MEASUREMENT_TOOLS_3 | PKG_RCPQ(73) |
| PKG_RCPQ.PRC_MEASUREMENT_TOOLS_4 | PKG_RCPQ(86) |
| PKG_RCPQ.PRC_MEASUREMENT_TOOLS_5 | PKG_RCPQ(91) |
| PKG_RCPQ.PRC_MEASUREMENT_TOOLS_6 | PKG_RCPQ(96) |
| PKG_RCPQ.PRC_MEASUREMENT_TOOLS_7 | PKG_RCPQ(102) |
| PKG_RCPQ.PRC_RECOM_TC9T | PKG_RCPQ(42) |
| PKG_RCPQ.PRC_SC_DWSZMXDZ | PKG_RCPQ(152) |
| PKG_RCPQ.PRC_SC_FYQD | PKG_RCPQ(207) |
| PKG_RCPQ.PRC_SC_GSQD | PKG_RCPQ(175) |
| PKG_RCPQ.PRC_SC_GZQD | PKG_RCPQ(130) |
| PKG_RCPQ.PRC_SC_JJBCQD | PKG_RCPQ(297) |
| PKG_RCPQ.PRC_SC_JSDY | PKG_RCPQ(192) |
| PKG_RCPQ.PRC_SC_RHQD_WH | PKG_RCPQ(323) |
| PKG_RCPQ.PRC_SC_SBSBJS | PKG_RCPQ(199) |
| PKG_RCPQ.PRC_SC_SYQD | PKG_RCPQ(164) |
| PKG_RCPQ.PRC_SC_SYQD_SH2 | PKG_RCPQ(316) |
| PKG_RCPQ.PRC_SC_SYQD_WH | PKG_RCPQ(313) |
| PKG_RCPQ.PRC_SC_TC8G | PKG_RCPQ(327) |
| PKG_RCPQ.PRC_SC_TJ07 | PKG_RCPQ(332) |
| PKG_RCPQ.PRC_SC_XJZFMX | PKG_RCPQ(143) |
| PKG_RCPQ.PRC_TFSH | PKG_RCPQ(117) |
| PKG_RCPQ.PRC_UPDATE_STATUS | PKG_RCPQ(234) |
| PKG_RCPQ.PRC_YH_CGHP | PKG_RCPQ(160) |
| PKG_RCPQ.PRC_YH_JJBCJ | PKG_RCPQ(308) |
| PKG_RCPQ.PRC_YH_SYGSHP | PKG_RCPQ(186) |
| PKG_RCPQ.PRC_撤销合同终止 | PKG_RCPQ(335) |
| PKG_RCPQ.PRC_缴费基数确认 | PKG_RCPQ(243) |
| PKG_RCPQ_FZ.PRC_BT_OTHER | PKG_RCPQ_FZ(30) |
| PKG_RCPQ_FZ.PRC_CANCLE_TJ04 | PKG_RCPQ_FZ(206) |
| PKG_RCPQ_FZ.PRC_CANCLE_XJSH | PKG_RCPQ_FZ(71) |
| PKG_RCPQ_FZ.PRC_CHANGE_BASE | PKG_RCPQ_FZ(84) |
| PKG_RCPQ_FZ.PRC_CONFIRM_TJ02 | PKG_RCPQ_FZ(98) |
| PKG_RCPQ_FZ.PRC_CREATE_AC10 | PKG_RCPQ_FZ(183) |
| PKG_RCPQ_FZ.PRC_CREATE_CS | PKG_RCPQ_FZ(166) |
| PKG_RCPQ_FZ.PRC_CREATE_TITLE | PKG_RCPQ_FZ(179) |
| PKG_RCPQ_FZ.PRC_DELETE_AC10 | PKG_RCPQ_FZ(192) |
| PKG_RCPQ_FZ.PRC_DEL_TC9P | PKG_RCPQ_FZ(195) |
| PKG_RCPQ_FZ.PRC_FEEDBACK_GJJ | PKG_RCPQ_FZ(133) |
| PKG_RCPQ_FZ.PRC_FEEDBACK_SB | PKG_RCPQ_FZ(129) |
| PKG_RCPQ_FZ.PRC_GENERATE_TFMX | PKG_RCPQ_FZ(74) |
| PKG_RCPQ_FZ.PRC_GET_MONEY | PKG_RCPQ_FZ(151) |
| PKG_RCPQ_FZ.PRC_INSERT_TC9TCURR | PKG_RCPQ_FZ(47) |
| PKG_RCPQ_FZ.PRC_INS_TC94 | PKG_RCPQ_FZ(175) |
| PKG_RCPQ_FZ.PRC_INS_TJ04 | PKG_RCPQ_FZ(120) |
| PKG_RCPQ_FZ.PRC_PASS_JS | PKG_RCPQ_FZ(94) |
| PKG_RCPQ_FZ.PRC_PASS_TJ04 | PKG_RCPQ_FZ(124) |
| PKG_RCPQ_FZ.PRC_SALARYCOMPUTE2 | PKG_RCPQ_FZ(222) |
| PKG_RCPQ_FZ.PRC_TC93_TC9P | PKG_RCPQ_FZ(198) |
| PKG_RCPQ_FZ.PRC_TC9T_AC10 | PKG_RCPQ_FZ(58) |
| PKG_RCPQ_FZ.PRC_XJSH | PKG_RCPQ_FZ(65) |
| PKG_RCPQ_FZ.PRC_工资名册勾选保存 | PKG_RCPQ_FZ(212) |
| PKG_RCPQ_FZ.PRC_工资名册导出保存 | PKG_RCPQ_FZ(137) |
| PKG_RCPQ_FZ.PRC_生成工资名头 | PKG_RCPQ_FZ(146) |
| PKG_RCPQ_FZ.PRO_COM_TEST | PKG_RCPQ_FZ(2) |
| PKG_RCPQ_FZ.PRO_COM_TEST_TJ04 | PKG_RCPQ_FZ(230) |
| PKG_RCPQ_FZ_新结算.PRC_SALARYCOMPUTE | PKG_RCPQ_FZ_新结算(12) |
| PKG_RCPQ_HM.PRC_CHANGE_EJDW | PKG_RCPQ_HM(79) |
| PKG_RCPQ_HM.PRC_CHECK_LZ_SALARY | PKG_RCPQ_HM(43) |
| PKG_RCPQ_HM.PRC_COMMIT_LZ_SALARY | PKG_RCPQ_HM(50) |
| PKG_RCPQ_HM.PRC_CPT_RATIO | PKG_RCPQ_HM(3) |
| PKG_RCPQ_HM.PRC_CREATE_XQ | PKG_RCPQ_HM(84) |
| PKG_RCPQ_HM.PRC_FINANCE_SH | PKG_RCPQ_HM(34) |
| PKG_RCPQ_HM.PRC_GET_ATC90D | PKG_RCPQ_HM(75) |
| PKG_RCPQ_HM.PRC_GJJ_SX | PKG_RCPQ_HM(19) |
| PKG_RCPQ_HM.PRC_IST_GS | PKG_RCPQ_HM(58) |
| PKG_RCPQ_HM.PRC_SET_DK | PKG_RCPQ_HM(27) |
| PKG_RCPQ_HM.PRC_SET_DW_ZF | PKG_RCPQ_HM(54) |
| PKG_RCPQ_HM.PRC_SET_DW_ZF_GS | PKG_RCPQ_HM(63) |
| PKG_RCPQ_HM.PRC_SET_KF | PKG_RCPQ_HM(30) |
| PKG_RCPQ_HM.PRC_UPDATE_TC93_AC10 | PKG_RCPQ_HM(66) |
| PKG_RCPQ_HM.PRC_UPDATE_TC93_AC10_TJ04 | PKG_RCPQ_HM(94) |
| PKG_RCPQ_NEW2.PRC_CHECK_JFSJ | PKG_RCPQ_NEW2(13) |
| PKG_RCPQ_NEW2.PRC_CONFIRM_JFSJ | PKG_RCPQ_NEW2(19) |
| PKG_RCPQ_TJ.PRC_EXPENDITURE | PKG_RCPQ_TJ(53) |
| PKG_RCPQ_TJ.PRC_EXPIRATION_REMINDER | PKG_RCPQ_TJ(46) |
| PKG_RCPQ_TJ.PRC_FEE_WARNING | PKG_RCPQ_TJ(28) |
| PKG_RCPQ_TJ.PRC_GET_CNT | PKG_RCPQ_TJ(2) |
| PKG_RCPQ_TJ.PRC_INCOME | PKG_RCPQ_TJ(50) |
| PKG_RCPQ_TJ.PRC_INS_TC8K | PKG_RCPQ_TJ(43) |
| PKG_RCPQ_TJ.PRC_LESS_THREE | PKG_RCPQ_TJ(35) |
| PKG_RCPQ_TJ.PRC_RH_LIST | PKG_RCPQ_TJ(39) |
| PKG_RCPQ_TJ.PRC_WARM_SUGGESTION | PKG_RCPQ_TJ(31) |
| PKG_RCPQ_TJ.PRC_包干经费统计 | PKG_RCPQ_TJ(20) |
| PKG_RCPQ_WY.PRC_CHECK_AA90_MOD | PKG_RCPQ_WY(49) |
| PKG_RCPQ_WY.PRC_CHECK_ISLOWER | PKG_RCPQ_WY(26) |
| PKG_RCPQ_WY.PRC_COMMIT_ALL | PKG_RCPQ_WY(43) |
| PKG_RCPQ_WY.PRC_COMMIT_XZ | PKG_RCPQ_WY(46) |
| PKG_RCPQ_WY.PRC_CONFIRM_TD04 | PKG_RCPQ_WY(83) |
| PKG_RCPQ_WY.PRC_CS_ZJ | PKG_RCPQ_WY(63) |
| PKG_RCPQ_WY.PRC_DELETE_XQ | PKG_RCPQ_WY(11) |
| PKG_RCPQ_WY.PRC_DEL_TC8V | PKG_RCPQ_WY(106) |
| PKG_RCPQ_WY.PRC_GET_AC10_CNT | PKG_RCPQ_WY(39) |
| PKG_RCPQ_WY.PRC_GET_INFO | PKG_RCPQ_WY(72) |
| PKG_RCPQ_WY.PRC_GET_JFSJ | PKG_RCPQ_WY(109) |
| PKG_RCPQ_WY.PRC_INS_AC10_CURR | PKG_RCPQ_WY(20) |
| PKG_RCPQ_WY.PRC_INS_TC8I_LIST | PKG_RCPQ_WY(89) |
| PKG_RCPQ_WY.PRC_INS_TC8I_LIST1 | PKG_RCPQ_WY(94) |
| PKG_RCPQ_WY.PRC_INS_TC8Y_LIST | PKG_RCPQ_WY(114) |
| PKG_RCPQ_WY.PRC_QXBG_BACK | PKG_RCPQ_WY(35) |
| PKG_RCPQ_WY.PRC_RETURNS_NORMAL | PKG_RCPQ_WY(3) |
| PKG_RCPQ_WY.PRC_SET_ATC904 | PKG_RCPQ_WY(15) |
| PKG_RCPQ_WY.PRC_SET_TC93 | PKG_RCPQ_WY(68) |
| PKG_RCPQ_WY.PRC_SH_TC8ILIST | PKG_RCPQ_WY(101) |
| PKG_RCPQ_WY.PRC_TERMINATE_UNDO | PKG_RCPQ_WY(31) |
| PKG_RCPQ_WY.PRC_工资名册_不代发 | PKG_RCPQ_WY(56) |
| PKG_RCPQ_WY.PRO_INS_TC8UMX | PKG_RCPQ_WY(88) |
| PKG_SYNCHR_UPDATE.PRC_AB01_UPDATE | PKG_SYNCHR_UPDATE(36) |
| PKG_SYNCHR_UPDATE.PRC_AC01_UPDATE | PKG_SYNCHR_UPDATE(31) |
| PKG_SYNCHR_UPDATE.PRC_变更结算单元名称 | PKG_SYNCHR_UPDATE(41) |
| PKG_人事代理.PRC_个人费用核算 | PKG_人事代理(31) |
| PKG_人事代理.PRC_单位费用核算 | PKG_人事代理(45) |
| PKG_人事代理.PRC_委托方式变更 | PKG_人事代理(27) |
| PKG_人事代理.PRC_学生档案比对 | PKG_人事代理(13) |
| PKG_人事代理.PRC_档案合并 | PKG_人事代理(23) |
| PKG_人事代理.PRC_生成商调字号 | PKG_人事代理(57) |
| PKG_人事代理.PRC_生成学生档案编号 | PKG_人事代理(10) |
| PKG_人事代理.PRC_生成档案编号 | PKG_人事代理(7) |
| PKG_人事代理.PRC_生成死档编号 | PKG_人事代理(60) |
| PKG_人才派遣.PRC_COM_TC9T | PKG_人才派遣(142) |
| PKG_人才派遣.PRC_COM_TC9T_TF | PKG_人才派遣(151) |
| PKG_人才派遣.PRC_COM_TC9T_TF1 | PKG_人才派遣(162) |
| PKG_人才派遣.PRC_COM_TC9W | PKG_人才派遣(119) |
| PKG_人才派遣.PRC_DEL_TC9T | PKG_人才派遣(172) |
| PKG_人才派遣.PRC_INS_TC9W | PKG_人才派遣(110) |
| PKG_人才派遣.PRC_MOD_TC91 | PKG_人才派遣(179) |
| PKG_人才派遣.PRC_YDSH | PKG_人才派遣(129) |
| PKG_人才派遣.PRC_删除员工 | PKG_人才派遣(184) |
| PKG_人才派遣.PRC_删除结算单元 | PKG_人才派遣(194) |
| PKG_人才派遣.PRC_协议续签审核 | PKG_人才派遣(81) |
| PKG_人才派遣.PRC_取消协议续签 | PKG_人才派遣(92) |
| PKG_人才派遣.PRC_合同终止 | PKG_人才派遣(187) |
| PKG_人才派遣.PRC_员工单位调转 | PKG_人才派遣(41) |
| PKG_人才派遣.PRC_员工平均工资计算 | PKG_人才派遣(73) |
| PKG_人才派遣.PRC_员工批量审核不通过 | PKG_人才派遣(2) |
| PKG_人才派遣.PRC_员工终止审核 | PKG_人才派遣(11) |
| PKG_人才派遣.PRC_员工终止审核_批量 | PKG_人才派遣(7) |
| PKG_人才派遣.PRC_员工调转单位申请 | PKG_人才派遣(99) |
| PKG_人才派遣.PRC_工资名册导出保存 | PKG_人才派遣(51) |
| PKG_人才派遣.PRC_生成工资名头 | PKG_人才派遣(62) |
| PKG_人才派遣.PRC_生成派遣协议编号 | PKG_人才派遣(33) |
| PKG_人才派遣.PRC_结算单元变更单位 | PKG_人才派遣(198) |
| PKG_人才派遣公共.PRC_缴费基数上下限 | PKG_人才派遣公共(56) |
| PKG_人才派遣公共.PRC_获取征缴金额 | PKG_人才派遣公共(78) |
| PKG_人才派遣公共.PRC_获取缴费比例_按员工 | PKG_人才派遣公共(22) |
| PKG_人才派遣公共.PRC_获取缴费比例_按结算单元 | PKG_人才派遣公共(7) |
| PKG_人才派遣公共.PRC_计算平均工资 | PKG_人才派遣公共(48) |
| PKG_人才派遣公共.PRC_退休身份认定 | PKG_人才派遣公共(4) |
| PKG_保险人员增减.PRC_保险申报 | PKG_保险人员增减(5) |
| PKG_保险人员增减.PRC_公积金人员增减 | PKG_保险人员增减(127) |
| PKG_保险人员增减.PRC_公积金基数调整补缴赋值 | PKG_保险人员增减(188) |
| PKG_保险人员增减.PRC_公积金报增员工 | PKG_保险人员增减(134) |
| PKG_保险人员增减.PRC_参保异议赋值 | PKG_保险人员增减(279) |
| PKG_保险人员增减.PRC_市公积金比例调整赋值 | PKG_保险人员增减(165) |
| PKG_保险人员增减.PRC_市公积金补缴赋值 | PKG_保险人员增减(171) |
| PKG_保险人员增减.PRC_市公积金赋值 | PKG_保险人员增减(159) |
| PKG_保险人员增减.PRC_市医保人员增减 | PKG_保险人员增减(102) |
| PKG_保险人员增减.PRC_市医保报增员工 | PKG_保险人员增减(109) |
| PKG_保险人员增减.PRC_市工伤人员增减 | PKG_保险人员增减(250) |
| PKG_保险人员增减.PRC_测试 | PKG_保险人员增减(299) |
| PKG_保险人员增减.PRC_省公积金启封封存赋值 | PKG_保险人员增减(147) |
| PKG_保险人员增减.PRC_省公积金开户赋值 | PKG_保险人员增减(141) |
| PKG_保险人员增减.PRC_省公积金补缴赋值 | PKG_保险人员增减(153) |
| PKG_保险人员增减.PRC_省医保人员增减 | PKG_保险人员增减(51) |
| PKG_保险人员增减.PRC_省医保报增员工 | PKG_保险人员增减(58) |
| PKG_保险人员增减.PRC_省工伤人员增减 | PKG_保险人员增减(232) |
| PKG_保险人员增减.PRC_社保人员增减 | PKG_保险人员增减(18) |
| PKG_保险人员增减.PRC_社保增减赋值 | PKG_保险人员增减(10) |
| PKG_保险人员增减.PRC_社保报增员工 | PKG_保险人员增减(25) |
| PKG_保险人员增减.PRC_社保新参保转续保 | PKG_保险人员增减(32) |
| PKG_保险补退.PRC_保险上月补退明细 | PKG_保险补退(35) |
| PKG_保险补退.PRC_保险补差明细 | PKG_保险补退(95) |
| PKG_保险补退.PRC_保险补收基数 | PKG_保险补退(26) |
| PKG_保险补退.PRC_保险补收基数_个人 | PKG_保险补退(30) |
| PKG_保险补退.PRC_保险补退明细 | PKG_保险补退(66) |
| PKG_保险补退.PRC_公积金补退明细 | PKG_保险补退(89) |
| PKG_保险补退.PRC_医保补退明细 | PKG_保险补退(77) |
| PKG_保险补退.PRC_工伤补退明细 | PKG_保险补退(83) |
| PKG_保险补退.PRC_测试当月补缴 | PKG_保险补退(4) |
| PKG_保险补退.PRC_社保上月补退明细 | PKG_保险补退(40) |
| PKG_保险补退.PRC_社保补退明细 | PKG_保险补退(71) |
| PKG_保险补退.PRC_离职未参保补退保险 | PKG_保险补退(58) |
| PKG_保险补退.PRC_获取补退基数 | PKG_保险补退(9) |
| PKG_基本信息.PRC_AC01_UPDATE_ONLY | PKG_基本信息(31) |
| PKG_基本信息.PRC_CHECK_AC01_IMP | PKG_基本信息(9) |
| PKG_基本信息.PRC_COMM_AC01_IMP | PKG_基本信息(12) |
| PKG_基本信息.PRC_CREATE_CHILD | PKG_基本信息(56) |
| PKG_基本信息.PRC_GET_ROOT | PKG_基本信息(60) |
| PKG_基本信息.PRC_INIT_CHILD | PKG_基本信息(64) |
| PKG_基本信息.PRC_PARSE_AAC002 | PKG_基本信息(2) |
| PKG_基本信息.PRC_个人基本信息变更 | PKG_基本信息(20) |
| PKG_基本信息.PRC_单位基本信息变更 | PKG_基本信息(36) |
| PKG_基本信息.PRC_单位树维护 | PKG_基本信息(48) |
| PKG_基本信息.PRC_获取节点的路径 | PKG_基本信息(52) |
| PKG_工资发放.PRC_TC93_PASS | PKG_工资发放(42) |
| PKG_工资发放.PRC_修改工资细项模板 | PKG_工资发放(16) |
| PKG_工资发放.PRC_工资名册勾选保存 | PKG_工资发放(23) |
| PKG_工资发放.PRC_工资名册导出保存 | PKG_工资发放(33) |
| PKG_工资发放.PRC_工资导入提交 | PKG_工资发放(102) |
| PKG_工资发放.PRC_工资细项赋值 | PKG_工资发放(92) |
| PKG_工资发放.PRC_工资花名册 | PKG_工资发放(96) |
| PKG_工资发放.PRC_工资花名册前台 | PKG_工资发放(109) |
| PKG_工资发放.PRC_工资项目校验 | PKG_工资发放(76) |
| PKG_工资发放.PRC_工资项目赋值 | PKG_工资发放(88) |
| PKG_工资发放.PRC_工资项目额度 | PKG_工资发放(80) |
| PKG_工资发放.PRC_按单位计算平均工资 | PKG_工资发放(4) |
| PKG_工资发放.PRC_添加工资花名册 | PKG_工资发放(64) |
| PKG_工资发放.PRC_生成工资名头 | PKG_工资发放(11) |
| PKG_工资发放.PRC_生成工资项目名称 | PKG_工资发放(73) |
| PKG_工资发放.PRC_计算补偿金 | PKG_工资发放(51) |
| PKG_应急指挥系统_统计.PRC_BIAA | PKG_应急指挥系统_统计(2) |
| PKG_数据转换.PRC_工资发放2 | PKG_数据转换(6) |
| PKG_数据转换.PRC_异议 | PKG_数据转换(10) |
| PKG_数据转换.PRC_批量调整公积金比例 | PKG_数据转换(4) |
| PKG_数据转换.PRC_新增合同基数 | PKG_数据转换(5) |
| PKG_数据转换.PRC_省工伤减免校验 | PKG_数据转换(7) |
| PKG_数据转换.PRC_重新计算个税 | PKG_数据转换(11) |
| PKG_派遣保险月情况.PRC_公积金差异说明 | PKG_派遣保险月情况(167) |
| PKG_派遣保险月情况.PRC_公积金月垫付情况 | PKG_派遣保险月情况(192) |
| PKG_派遣保险月情况.PRC_医保差异说明 | PKG_派遣保险月情况(162) |
| PKG_派遣保险月情况.PRC_医保月垫付情况 | PKG_派遣保险月情况(182) |
| PKG_派遣保险月情况.PRC_工伤月垫付情况 | PKG_派遣保险月情况(187) |
| PKG_派遣保险月情况.PRC_工伤转款情况 | PKG_派遣保险月情况(113) |
| PKG_派遣保险月情况.PRC_工伤转款情况1 | PKG_派遣保险月情况(125) |
| PKG_派遣保险月情况.PRC_市公积金基数补缴情况 | PKG_派遣保险月情况(101) |
| PKG_派遣保险月情况.PRC_市公积金转款情况 | PKG_派遣保险月情况(89) |
| PKG_派遣保险月情况.PRC_市医保转款情况 | PKG_派遣保险月情况(47) |
| PKG_派遣保险月情况.PRC_市工伤应缴费情况 | PKG_派遣保险月情况(143) |
| PKG_派遣保险月情况.PRC_市工伤月情况 | PKG_派遣保险月情况(131) |
| PKG_派遣保险月情况.PRC_市工伤转款情况 | PKG_派遣保险月情况(137) |
| PKG_派遣保险月情况.PRC_省公积金基数补缴情况 | PKG_派遣保险月情况(77) |
| PKG_派遣保险月情况.PRC_省公积金转款情况 | PKG_派遣保险月情况(65) |
| PKG_派遣保险月情况.PRC_省医保转款情况 | PKG_派遣保险月情况(23) |
| PKG_派遣保险月情况.PRC_省医保转款情况1 | PKG_派遣保险月情况(35) |
| PKG_派遣保险月情况.PRC_社保差异说明 | PKG_派遣保险月情况(157) |
| PKG_派遣保险月情况.PRC_社保月垫付情况 | PKG_派遣保险月情况(177) |
| PKG_派遣保险月情况.PRC_社保转款情况 | PKG_派遣保险月情况(11) |
| PKG_派遣批量导入.PRC_代码项判断 | PKG_派遣批量导入(11) |
| PKG_派遣批量导入.PRC_历史工资导入提交 | PKG_派遣批量导入(42) |
| PKG_派遣批量导入.PRC_工资基数申报提交 | PKG_派遣批量导入(48) |
| PKG_派遣批量导入.PRC_工资基数申报校验 | PKG_派遣批量导入(45) |
| PKG_派遣批量导入.PRC_新增意外险 | PKG_派遣批量导入(59) |
| PKG_测试过程.PRC_1 | PKG_测试过程(4) |
| PKG_社区合并拆分.PRC_社区合并 | PKG_社区合并拆分(8) |
| PKG_社区合并拆分.PRC_社区拆分 | PKG_社区合并拆分(13) |
| PKG_税务接口.PRC_实际纳税回盘提交 | PKG_税务接口(40) |
| PKG_税务接口.PRC_实际纳税回盘校验 | PKG_税务接口(37) |
| PKG_税务接口.PRC_新个税回盘提交 | PKG_税务接口(34) |
| PKG_税务接口.PRC_新个税回盘校验 | PKG_税务接口(31) |
| PKG_税务接口.PRC_税务一次性奖金报盘 | PKG_税务接口(20) |
| PKG_税务接口.PRC_税务人员增减 | PKG_税务接口(10) |
| PKG_税务接口.PRC_税务员工导入提交 | PKG_税务接口(6) |
| PKG_税务接口.PRC_税务员工导入校验 | PKG_税务接口(2) |
| PKG_税务接口.PRC_税务薪金报盘 | PKG_税务接口(15) |
| PKG_税务接口.PRC_薪金所得回盘提交 | PKG_税务接口(28) |
| PKG_税务接口.PRC_薪金所得回盘校验 | PKG_税务接口(25) |
| PKG_税务税收导入.PRC_导入提交 | PKG_税务税收导入(7) |
| PKG_税务税收导入.PRC_导入校验 | PKG_税务税收导入(4) |
| PKG_税务税收导入.PRC_数据比对 | PKG_税务税收导入(11) |
| PKG_系统管理.PRC_系统消息提取 | PKG_系统管理(6) |
| PKG_统计报表.PRC_前台工作量统计 | PKG_统计报表(18) |
| PKG_统计报表.PRC_劳务派遣单位经营情况分表 | PKG_统计报表(13) |
| PKG_统计报表.PRC_劳务派遣单位经营情况统计表 | PKG_统计报表(4) |
| PKG_统计报表.PRC_终止劳务派遣服务统计表 | PKG_统计报表(10) |
| PKG_统计报表.PRC_结算汇总表 | PKG_统计报表(23) |
| PKG_统计报表.PRC_险金户头汇总表 | PKG_统计报表(26) |
| PKG_缴费基数.PRC_公积金未申报名册 | PKG_缴费基数(103) |
| PKG_缴费基数.PRC_医保未申报名册 | PKG_缴费基数(94) |
| PKG_缴费基数.PRC_工伤未申报名册 | PKG_缴费基数(112) |
| PKG_缴费基数.PRC_社保未申报名册 | PKG_缴费基数(85) |
| PKG_职介_月报.PRC_职介_月报 | PKG_职介_月报(4) |
| PKG_财务.PRC_个人欠费核销 | PKG_财务(16) |
| PKG_财务.PRC_单位一次性核销 | PKG_财务(34) |
| PKG_财务.PRC_单位垫付审核 | PKG_财务(48) |
| PKG_财务.PRC_单位垫付申请 | PKG_财务(44) |
| PKG_财务.PRC_单位欠费核销 | PKG_财务(25) |
| PKG_财务.PRC_单笔生成工资 | PKG_财务(61) |
| PKG_财务.PRC_发票申请 | PKG_财务(5) |
| PKG_财务.PRC_取消个人核销 | PKG_财务(21) |
| PKG_财务.PRC_取消单位一次性核销 | PKG_财务(40) |
| PKG_财务.PRC_取消单位核销 | PKG_财务(30) |
| PKG_财务.PRC_垫付信息删除 | PKG_财务(53) |
| PKG_财务.PRC_工资提交_批量 | PKG_财务(68) |
| PKG_财务.PRC_生成工资 | PKG_财务(56) |
| PKG_费用结算.PRC_单笔重新结算 | PKG_费用结算(59) |
| PKG_费用结算.PRC_历史欠款名单 | PKG_费用结算(40) |
| PKG_费用结算.PRC_同步银行账号 | PKG_费用结算(95) |
| PKG_费用结算.PRC_增加历史合同 | PKG_费用结算(112) |
| PKG_费用结算.PRC_工资发放失败 | PKG_费用结算(97) |
| PKG_费用结算.PRC_当年历史工资 | PKG_费用结算(16) |
| PKG_费用结算.PRC_批量不予补退 | PKG_费用结算(117) |
| PKG_费用结算.PRC_提取上月工资 | PKG_费用结算(22) |
| PKG_费用结算.PRC_撤销结算 | PKG_费用结算(62) |
| PKG_费用结算.PRC_更新工资发放年月 | PKG_费用结算(68) |
| PKG_费用结算.PRC_生成可结算单元 | PKG_费用结算(75) |
| PKG_费用结算.PRC_生成工资清单 | PKG_费用结算(82) |
| PKG_费用结算.PRC_生成费用清单 | PKG_费用结算(100) |
| PKG_费用结算.PRC_经济补偿金名单 | PKG_费用结算(46) |
| PKG_费用结算.PRC_虚拟代理工资 | PKG_费用结算(28) |
| PKG_费用结算.PRC_补退历史名单 | PKG_费用结算(34) |
| PKG_费用结算.PRC_计算年终奖 | PKG_费用结算(11) |
| PKG_费用结算.PRC_费用结算 | PKG_费用结算(52) |
| PKG_费用结算.PRC_险金计算器 | PKG_费用结算(108) |
| PRC_ADD | (独立PROCEDURE)() |
| PRC_INSERT_JB | (独立PROCEDURE)() |
| PRC_PARSE_AAC002 | (独立PROCEDURE)() |
| PRC_R_6J | (独立PROCEDURE)() |
| PRC_R_6J_DL | (独立PROCEDURE)() |
| PRC_SALARYCOMPUTE_SH | (独立PROCEDURE)() |
| PRC_险金补退费.PRC_手工险金退费 | PRC_险金补退费(16) |
| PRC_险金补退费.PRC_险金补缴 | PRC_险金补退费(39) |
| PRC_险金补退费.PRC_险金补缴1 | PRC_险金补退费(66) |
| PRC_险金补退费.PRC_险金退费 | PRC_险金补退费(4) |
| PRM_AAC002_CHECK | (独立PROCEDURE)() |
| PRO_GET_CNT | (独立PROCEDURE)() |
| PRO_批量调出 | (独立PROCEDURE)() |
| PRO_档案编号 | (独立PROCEDURE)() |
| PRO_生成档案号 | (独立PROCEDURE)() |
| TEST | (独立FUNCTION)() |

## 2. 交互对照（旧页面引用 653 REQ ↔ 新注册 685）

- 双侧同名：**628**
- 旧引用而新未注册（25）：REQ-2f6e68e56bbd4cafb25304909a0faf02, REQ-35ccdd77186a471f8ffeb70f0aa09e9cete, REQ-5b0e9af266e741459e809a8d7b378fe5, REQ-7fbb1be056ba40b4ab355a1831d82357, REQ-89e16de3069e4c409121ac09152e5b5aete, REQ-SysCommon-Count, REQ-SysCommon-Export, REQ-SysCommon-Print, REQ-SysCommon-Query, REQ-SysCommon-Update, REQ-UTIL-dateService-Q, REQ-UTIL-parameterService-Q, REQ-UTIL-procedureService-Q, REQ-UTIL-sequenceService-Q, REQ-VS-ajaxValidator-Q, REQ-WG-chartConfig-Q, REQ-WG-quickSearch-Q, REQ-WG-treeCommon-Q, REQ-WG-treeQuery-Q, REQ-aacd_cont-cc0mStore-Q, REQ-azbc_dialog_detail-ac01Store_detail-Query, REQ-tdcah_cont-tc9w2Store-Q, REQ-tdcah_cont-tc9w2Store-save, REQ-tdcah_cont-tc9w3Store-Q, REQ-tdcah_cont-tc9w3Store-save
- 新注册而旧页面无引用（57，多为服务端 -Q 查询通道/生成清单时并入）：AJAX-tdbo_tdboAjax, AJAX-tdck_tdckAjax, REQ-04613b57ed51433dbbe275ce33922744, REQ-109fe5ffab7c4285a58b6baf64d7f3af, REQ-1a0452fe10fa43c988482709bb077a4f, REQ-1a7af2ca93ab4838bc560009b3350ddf, REQ-1e367da50dc748a8998f67d200d19339, REQ-211c97897aff46449311c1ccc54e83d8, REQ-25ae7c7653db4978b1608ccce4876da2, REQ-28ddbee63d934ffab60eee6efc1c80bf, REQ-2e696f0b30ab4ca295728aa223ef0703, REQ-305fd3dea2044c31b239e3ec8b89623c, REQ-31a750b2fb3a48d68c3f278f825114d4, REQ-361489e2a1cf48888ad8a3738540ea29, REQ-40cbe43b227d49d2aaad9e843099e87a, REQ-4c43e3f3a1a74824b9a81f0f28bcd893, REQ-4c919c12968a471a9da486e49ed5e360, REQ-5466943e51f84592a8f594fa7de3fd7b, REQ-59b2eebc67e647c18c9c125bc8957f9d, REQ-5b6d552d48934e549bb869d1ee1ace44, REQ-61a7886ff15a4ff3abd247d0f779dd7b, REQ-9b8f163320a84b94b153581c3b3822e3, REQ-a4b8ed810d1143b682b3bb6c337bb416, REQ-af30ba4233db47389ca0fea0244b263b, REQ-azba_cont-ac01Store-Save1 …
- 交互明细（bean/method/stores）双侧同名同构：见 `GET /interaction/registry` 与 interactions.json；旧 bean 级全量(714)见 legacy-analysis/url-entry-points.md。

## 3. 表对照（旧 622 表 ↔ PG 642 表）

- PG 未建（2）：sys_temp_fbt, wbs_screen
- PG 独有（22，导入批次/审计/迁移辅助）：aa75, aa77, aa78, ab01_lb, ac01_lb, sys_codelist, sys_login_userinfo, sys_menu, sys_org_user_ext_lemis, sys_org_user_unit, sys_role_user, sys_unit_ext, sys_unit_tree, sys_user_menu_authority, tab_sjbg, tb8a, wb_audit_log, wb_sync_log, yth_dwcb, yth_dwjf, yth_grcb, yth_grjf
- 每日同步覆盖（Oracle 赢，只读）：**21** 张 — aa10, ab01, ac01, ce10, tb90, tb91, tb93, tb97, tb99, tc06, tc90, tc90_list, tc90_yy, tc92, tc93, tc9u, tc9w, tc9w1, tc9w2, tc9w3, tc9w4
- 逐实体映射（549 hbm→543 表）：见 `docs/legacy-analysis/entity-table-map.md`（不重复）。

## 4. 视图对照（旧 94 ↔ PG 80）

- 旧有 PG 无（14）：sys_codelist, sys_login_userinfo, sys_menu, sys_org_user_ext_lemis, sys_org_user_unit, sys_role_user, sys_unit_ext, sys_unit_tree, sys_user_menu_authority, tab_sjbg, v_aa97_nf, v_aa97_tc9t, v_tc90_xj, v_up_lemis_menu
- PG 独有（0）：

## 5. 序列对照（旧 666 ↔ PG 670）

- 旧有 PG 无（1，多为未移植域 accompanies）：SEQ_1D_202611
- PG 已抬段 1e7 防撞号（seq_offset.sql）；对齐账见 seq_sweep.sql。

## 6. 忠实行为/已知缺陷保真标注（rcpq-service 源码 grep “忠实”，160 处）

| 位置 | 标注 |
|---|---|
| `rcpq-service/src/main/kotlin/com/jlrc/rcpq/service/calc/InsuranceCalcEngine.kt:16` | *   原 TC08 医保分支 EXCEPTION 误写 aaa301 无副作用, 忠实不复制) |
| `rcpq-service/src/main/kotlin/com/jlrc/rcpq/service/calc/InsuranceCalcEngine.kt:18` | *   省工伤分支 R_TJ0B1.BBA004 双计 + 读 TJ0B1.ATB90N 的复制粘贴 bug 忠实保留) |
| `rcpq-service/src/main/kotlin/com/jlrc/rcpq/service/calc/InsuranceCalcEngine.kt:20` | * 忠实怪癖清单(与备份库源码逐行对齐): |
| `rcpq-service/src/main/kotlin/com/jlrc/rcpq/service/calc/InsuranceCalcEngine.kt:223` | // P4 忠实: 原 EXCEPTION 分支 R_AAA301 := R.AAA300(有副作用, 影响养老/失业基数) |
| `rcpq-service/src/main/kotlin/com/jlrc/rcpq/service/calc/InsuranceCalcEngine.kt:292` | // 旧 bug 忠实: 单位工伤比例读 TJ0B1.ATB90N; NULL/无行 → 覆盖块将 NULL 写回(P6 同族: atb90nIsNull) |
| `rcpq-service/src/main/kotlin/com/jlrc/rcpq/service/calc/InsuranceCalcEngine.kt:298` | // 旧 bug 忠实: NVL(TJ0B1.BBA004,0)+累计值+TJ0F1.BBA004(TJ0B1 双计); P6: F 值 NULL → 表达式 NULL → 覆盖块 NVL→0 不覆盖 |
| `rcpq-service/src/main/kotlin/com/jlrc/rcpq/service/td/Jishu5Cases.kt:19` | * 资格判定(SYSDATE 年度动态, 核对仅在当年窗口有效=旧系统忠实) → UPDATE AA97 置 '3'/'0' → |
| `rcpq-service/src/main/kotlin/com/jlrc/rcpq/service/td/Jishu5Cases.kt:26` | *         ABP00C/D/E 省市两分支同为 ROUND 整数化(注释称"市进位"按代码忠实 HALF_UP scale0), |
| `rcpq-service/src/main/kotlin/com/jlrc/rcpq/service/td/Jishu5Cases.kt:36` | /** 行级不合格信号: 该人 AAE214_k 置 '0', 不中断批次(忠实 per-row BEGIN..EXCEPTION) */ |
| `rcpq-service/src/main/kotlin/com/jlrc/rcpq/service/td/Jishu5Cases.kt:127` | // 忠实: 80 旁路在外层, 连 AAE00P 都绕过 |
| `rcpq-service/src/main/kotlin/com/jlrc/rcpq/service/td/Jishu5Cases.kt:174` | // 忠实: clamp IN=OUT=303_1 → 夹取值写回 R_AA97.AAA303_1 |
| `rcpq-service/src/main/kotlin/com/jlrc/rcpq/service/td/Jishu5Cases.kt:180` | // 省/市两分支同为 ROUND 整数化(源注释"市进位"与代码不符, 按代码忠实) |
| `rcpq-service/src/main/kotlin/com/jlrc/rcpq/service/td/Jishu5Cases.kt:236` | // 忠实: OUT=AAA305 随即被 UPDATE 的 TC90.AAA305 覆盖, 仅报错不落值 |
| `rcpq-service/src/main/kotlin/com/jlrc/rcpq/service/td/Yueqk4Cases.kt:13` | * 三态候选过滤(忠实): (参保中 ATC90PA 0/3 且 AAE002<=当月 且 AAA602 未到期) OR |
| `rcpq-service/src/main/kotlin/com/jlrc/rcpq/service/td/Yueqk4Cases.kt:15` | * 新参保行 ATJ0AA='1' 并查 TC9W1.ABP016 ('1','2' 跳过该行); 单元排除 37664(忠实); |
| `rcpq-service/src/main/kotlin/com/jlrc/rcpq/service/td/Yueqk4Cases.kt:53` | // 新参保行: ATJ0AA='1' + TC9W1 报增行 ABP016 ('1','2') → 跳过(忠实) |
| `rcpq-service/src/main/kotlin/com/jlrc/rcpq/service/td/TdcImpHzCases.kt:18` | *   (原 TC9W 联动分支整块被注释, 忠实只保留 TC90 更新) |
| `rcpq-service/src/main/kotlin/com/jlrc/rcpq/service/td/Yueqk5Cases.kt:19` | *     hack 22010319930822063X→BBA004=16.29; ATJ0F3/F5=BBA004; 34560/34889 no-op 忠实） |
| `rcpq-service/src/main/kotlin/com/jlrc/rcpq/service/td/Yueqk5Cases.kt:21` | *     hack ym=202411×4 人固定基数/缴存额 忠实） |
| `rcpq-service/src/main/kotlin/com/jlrc/rcpq/service/td/Yueqk5Cases.kt:23` | *     CAA001/002=回盘桶 abp005/006, 保险当月补缴调用已被注释→不调(忠实), 无大病/滞纳金） |
| `rcpq-service/src/main/kotlin/com/jlrc/rcpq/service/td/Yueqk5Cases.kt:112` | // 忠实: 硬编码清零 hack(数据域限定) |
| `rcpq-service/src/main/kotlin/com/jlrc/rcpq/service/td/Yueqk5Cases.kt:195` | rs++ // 忠实: N_RS 无条件自增(插/不插都算) |
| `rcpq-service/src/main/kotlin/com/jlrc/rcpq/service/td/Yueqk5Cases.kt:206` | // 忠实: hack ym=202411 × 4 人固定基数/缴存额(数据域限定) |
| `rcpq-service/src/main/kotlin/com/jlrc/rcpq/service/td/Yueqk5Cases.kt:297` | // 忠实: TC9W_BP09 单行取 ABO004/ABO003, 多行/无行 → NULL(忠实 SELECT INTO 语义) |
| `rcpq-service/src/main/kotlin/com/jlrc/rcpq/service/td/Yueqk5Cases.kt:304` | // 忠实: 硬编码 hack(数据域限定) |
| `rcpq-service/src/main/kotlin/com/jlrc/rcpq/service/td/Yueqk5Cases.kt:306` | if (atc900 == 34560L) { /* 忠实: 旧源码 no-op */ } |
| `rcpq-service/src/main/kotlin/com/jlrc/rcpq/service/td/Yueqk5Cases.kt:307` | if (atc900 == 34889L) { /* 忠实: 旧源码 no-op */ } |
| `rcpq-service/src/main/kotlin/com/jlrc/rcpq/service/td/Yueqk5Cases.kt:349` | *  abp005/006, 保险当月补缴调用已被注释→不调(忠实); 无大病/滞纳金。 */ |
| `rcpq-service/src/main/kotlin/com/jlrc/rcpq/service/td/Yueqk2Cases.kt:18` | * 结构性修正: 旧单位循环内裸 COMMIT → 单事务(YueErr 回滚)。硬编码个人 hack 忠实保留。 |
| `rcpq-service/src/main/kotlin/com/jlrc/rcpq/service/td/Yueqk2Cases.kt:128` | /** 工伤月情况(TJ0F/F1): TC10(ATC105='2') UNION; TC18=202201 专项覆写; 硬编码个人归零 hack 忠实。 |
| `rcpq-service/src/main/kotlin/com/jlrc/rcpq/service/td/Yueqk2Cases.kt:202` | val b4 = if (yg["aac002"] == "220282199106213512") Z else base4 // 旧硬编码 hack 忠实 |
| `rcpq-service/src/main/kotlin/com/jlrc/rcpq/service/td/Jishu6Cases.kt:24` | *    排除=源活性补丁忠实) 且 tc90.ATB90R='5'(离职/终止) → CBSIGN='9'。源含 COMMIT—— |
| `rcpq-service/src/main/kotlin/com/jlrc/rcpq/service/td/Jishu6Cases.kt:38` | *    '0'→'4'(源顺序语义: 状态列改后年月列 CASE 读改后值→恒 ELSE=年月实际不更新, 忠实)→ |
| `rcpq-service/src/main/kotlin/com/jlrc/rcpq/service/td/Jishu6Cases.kt:46` | *    '2'——段内错误文案沿用"社保…"=copy-paste 忠实), |
| `rcpq-service/src/main/kotlin/com/jlrc/rcpq/service/td/Jishu6Cases.kt:47` | *    公积金(aaa407/aaa501,503,508/ATC90W/基数=AAA303, '3'; **无工伤段=源缺席忠实**)。 |
| `rcpq-service/src/main/kotlin/com/jlrc/rcpq/service/td/Jishu6Cases.kt:160` | // 硬编码排除三合同=源活性补丁忠实 |
| `rcpq-service/src/main/kotlin/com/jlrc/rcpq/service/td/Jishu6Cases.kt:282` | // (源顺序语义: 状态列改后年月列 CASE 读改后值→恒 ELSE=年月实际不更新, 忠实照抄) |
| `rcpq-service/src/main/kotlin/com/jlrc/rcpq/service/td/Jishu5WsbCases.kt:20` | *   (**工伤清单属地列用 AAA120=公积金属地, 应为 AAA130 的 copy-paste 旧 bug 忠实保留**; |
| `rcpq-service/src/main/kotlin/com/jlrc/rcpq/service/td/Jishu5WsbCases.kt:25` | *   (无 EXCEPTION→AA12 缺行=整过程'出错'忠实)→按单元(DISTINCT ATB930)循环: |
| `rcpq-service/src/main/kotlin/com/jlrc/rcpq/service/td/Jishu5WsbCases.kt:32` | *   AAB001/004, 失败 RAISE→复位 code 跳过该单元=忠实 SkipUnit)→名册(见下)→INSERT DW |
| `rcpq-service/src/main/kotlin/com/jlrc/rcpq/service/td/Jishu5WsbCases.kt:41` | /** 四线差异表(列名常量, 无注入面); 工伤属地列 aaa120=源 copy-paste bug 忠实 */ |
| `rcpq-service/src/main/kotlin/com/jlrc/rcpq/service/td/Jishu5WsbCases.kt:80` | // AA12 SELECT INTO 无 EXCEPTION: 缺行→外层'出错'(忠实) |
| `rcpq-service/src/main/kotlin/com/jlrc/rcpq/service/td/Jishu5ImportCases.kt:15` | *   '为设置时限参数' 错别字忠实)→ ACE10A IS NULL 行逐行: |
| `rcpq-service/src/main/kotlin/com/jlrc/rcpq/service/td/Jishu5ImportCases.kt:16` | *   取有效合同(ATC90G='1' + 身份证+姓名+结算单元, MAX(ATC900); TOO_MANY 分支 ATC900=PK 实际不可达忠实保留) |
| `rcpq-service/src/main/kotlin/com/jlrc/rcpq/service/td/Jishu5ImportCases.kt:22` | *     V_MSG 置后【无 RAISE=源活性缺陷忠实】→ 单位/个人比例整数段{12,7} 校验 |
| `rcpq-service/src/main/kotlin/com/jlrc/rcpq/service/td/Jishu5ImportCases.kt:23` | *     (Oracle SUBSTR(x,1,INSTR(x,'.')-1): 无小数点→NULL→检查静默通过=源活性绕过忠实)) |
| `rcpq-service/src/main/kotlin/com/jlrc/rcpq/service/td/Jishu5ImportCases.kt:26` | *   (源"格式不正确"捕获分支 NO_DATA_FOUND 永不命中=死分支, 忠实按整批报错); |
| `rcpq-service/src/main/kotlin/com/jlrc/rcpq/service/td/Jishu5ImportCases.kt:33` | *   AAE214_k<>'1' 才覆写申报基数(SQL 三值: NULL 行也走 ELSE=不覆写, 源活性语义忠实; |
| `rcpq-service/src/main/kotlin/com/jlrc/rcpq/service/td/Jishu5ImportCases.kt:34` | *   比例两列同挂 AAE214_3 公积金线=源 quirks 忠实; WHERE AAE001+ATC900 换合同人匹配 0 行静默=忠实); |
| `rcpq-service/src/main/kotlin/com/jlrc/rcpq/service/td/Jishu5ImportCases.kt:83` | // AA95 时限参数探测(AAA952='2' 唯一行; 无行/多行→错别字报错忠实; R_AA95 现行无消费点) |
| `rcpq-service/src/main/kotlin/com/jlrc/rcpq/service/td/Jishu5ImportCases.kt:131` | // 源 TOO_MANY_ROWS 分支: ATC900=PK 实际不可达, 忠实保留 |
| `rcpq-service/src/main/kotlin/com/jlrc/rcpq/service/td/Jishu5ImportCases.kt:215` | // 比例必填 V_MSG 置后【无 RAISE=源活性缺陷忠实】: 先记下, 比例格式两查后再败 |
| `rcpq-service/src/main/kotlin/com/jlrc/rcpq/service/td/Jishu5ImportCases.kt:294` | // (属地 NULL 走 ELSE 分支=源三值忠实) |
| `rcpq-service/src/main/kotlin/com/jlrc/rcpq/service/td/Jishu5ImportCases.kt:314` | // 比例两列同挂 AAE214_3=源 quirks; WHERE AAE001+ATC900 换合同人匹配 0 行静默=忠实) |
| `rcpq-service/src/main/kotlin/com/jlrc/rcpq/service/td/TdcbpCases.kt:12` | *   - PRC_省工伤异动回盘校验     L7809-7939（TC9W_BP09 桶; 清洗**无 WHERE=全表清洗**(忠实); |
| `rcpq-service/src/main/kotlin/com/jlrc/rcpq/service/td/TdcbpCases.kt:18` | *                                  姓名/额度 BBA004/合计 ATC9EH 校验; 无外层 OTHERS 包装(忠实)） |
| `rcpq-service/src/main/kotlin/com/jlrc/rcpq/service/td/TdcbpCases.kt:23` | *                                  TC93 无月时以 TC90 合成占位(其 ATB931 误取单位名, 忠实)） |
| `rcpq-service/src/main/kotlin/com/jlrc/rcpq/service/td/TdcbpCases.kt:62` | /** 费款所属期解析: 'yyyymm-yyyymm' 区间 / 单月 → 起止 LocalDate(singleAdj=true 时单月=前一月, 校验忠实) */ |
| `rcpq-service/src/main/kotlin/com/jlrc/rcpq/service/td/TdcbpCases.kt:83` | // 忠实: 旧清洗 UPDATE 无 WHERE = 全表清洗(L7827-7841) |
| `rcpq-service/src/main/kotlin/com/jlrc/rcpq/service/td/TdcbpCases.kt:87` | // 忠实: 旧系统此置终态未注释(活), 范围含 aaa100(属地) |
| … | 其余 100 处同法 grep |
