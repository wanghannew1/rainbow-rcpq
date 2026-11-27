# API 参考（自动生成）
> 生成时间 2026-11-24，来源：`GET /proc`+`GET /interaction/registry` 运行注册面 + Kotlin 源码静态扫描。
> 参数清单为 best-effort 抽取（handler 内 `params["…"]`/必填 listOf），**权威以 rcpq-service 源码为准**。
> 通用契约：`POST` 请求体=JSON 对象（形参名→值），返回 `{appCode:1 成功 / -1 业务拒绝, errMsg, …业务字段}`。
## 1. POST /proc/{包名.过程名} — 注册 111 过程

### PKG_IMP（3）
| 过程 | 参数(源码抽取) | 实现 |
|---|---|---|
| PRC_AC01_GX_IMP | ace100 | `rcpq-service/src/main/kotlin/com/jlrc/rcpq/service/td/TdbpImportCases.kt:2836` |
| PRC_CHECK_TC93 | ace100, atb930, atc931, atc932, atc937 | `rcpq-service/src/main/kotlin/com/jlrc/rcpq/service/td/TdbpImportCases.kt:3019` |
| PRC_COMMIT_TC93 | aae011, aae017, aae019, aae020, aae022, ace100, atc931, atc932 | `rcpq-service/src/main/kotlin/com/jlrc/rcpq/service/td/TdbpImportCases.kt:3030` |

### PKG_IMP_FZ（8）
| 过程 | 参数(源码抽取) | 实现 |
|---|---|---|
| PRC_CHECK_TC9W_HZ_IMP | aae002, ace100, flag | `rcpq-service/src/main/kotlin/com/jlrc/rcpq/service/td/TdcImpHzCheckComm.kt:86` |
| PRC_CHECK_TC9W_HZ_SUCCESS_IMP | ace100, flag | `rcpq-service/src/main/kotlin/com/jlrc/rcpq/service/td/TdcImpHzCases.kt:160` |
| PRC_CHECK_TC9W_HZ_SUCCESS_IMP2 | ace100, flag | `rcpq-service/src/main/kotlin/com/jlrc/rcpq/service/td/TdcImpHzCases.kt:172` |
| PRC_COMMIT_AAC001 | aaa001, aac002 | `rcpq-service/src/main/kotlin/com/jlrc/rcpq/service/td/TdcImpHzCases.kt:193` |
| PRC_COMMIT_TC90_IMP | aae011, aae017, aae019, aae020, aae022, ace100 | `rcpq-service/src/main/kotlin/com/jlrc/rcpq/service/td/TdcTc90ImpCases.kt:510` |
| PRC_COMMIT_TC9W_HZ_SUCCESS_IMP | ace100 | `rcpq-service/src/main/kotlin/com/jlrc/rcpq/service/td/TdcImpHzCases.kt:184` |
| PRC_COMM_TC9W_HZ_IMP | ace100, begin, end, flag | `rcpq-service/src/main/kotlin/com/jlrc/rcpq/service/td/TdcImpHzCheckComm.kt:99` |
| PRC_TC90_IMP | ace100 | `rcpq-service/src/main/kotlin/com/jlrc/rcpq/service/td/TdcTc90ImpCases.kt:500` |

### PKG_RCPQ_FZ（2）
| 过程 | 参数(源码抽取) | 实现 |
|---|---|---|
| PRC_CANCLESALARYCOMPUTE | atb930, atc931, atc932, atc937 | `rcpq-service/src/main/kotlin/com/jlrc/rcpq/service/td/TddSettlementCases.kt:272` |
| PRC_SALARYCOMPUTE | aae011, atb900, atb930, atc931, atc932, atc937 | `rcpq-service/src/main/kotlin/com/jlrc/rcpq/service/td/TddSettlementCases.kt:256` |

### PKG_人才派遣公共（4）
| 过程 | 参数(源码抽取) | 实现 |
|---|---|---|
| PRC_保险补收基数_个人 | aae002, aae011, atc900 | `rcpq-service/src/main/kotlin/com/jlrc/rcpq/service/td/Jishu6Cases.kt:566` |
| PRC_测试计算险金过程 | aae002, atc900 | `rcpq-service/src/main/kotlin/com/jlrc/rcpq/service/calc/CalcProcs.kt:85` |
| PRC_获取补退基数 | atc900, nf | `rcpq-service/src/main/kotlin/com/jlrc/rcpq/service/calc/CalcProcs.kt:121` |
| PRC_计算险金 | aae002, atc900, xjbl | `rcpq-service/src/main/kotlin/com/jlrc/rcpq/service/calc/CalcProcs.kt:28` |

### PKG_保险人员增减（31）
| 过程 | 参数(源码抽取) | 实现 |
|---|---|---|
| PRC_保险补差确认 | aaa020, aaa030, aae002, aae011, flag, xz | `rcpq-service/src/main/kotlin/com/jlrc/rcpq/service/td/Jishu6Cases.kt:551` |
| PRC_公积金封存回盘提交 | aaa020, aaa030, aae002 | `rcpq-service/src/main/kotlin/com/jlrc/rcpq/service/td/TdcgjCases.kt:430` |
| PRC_公积金封存回盘校验 | aaa020, aaa030, aae002 | `rcpq-service/src/main/kotlin/com/jlrc/rcpq/service/td/TdcgjCases.kt:418` |
| PRC_公积金异动回盘提交 | aaa020, aaa030, aae002, aae011 | `rcpq-service/src/main/kotlin/com/jlrc/rcpq/service/td/TdcgjCases.kt:403` |
| PRC_公积金异动回盘校验 | aaa020, aaa030, aae002 | `rcpq-service/src/main/kotlin/com/jlrc/rcpq/service/td/TdcgjCases.kt:390` |
| PRC_公积金异议回盘提交 | aaa020, aaa030, aae002, aae011 | `rcpq-service/src/main/kotlin/com/jlrc/rcpq/service/td/TdcgjCases.kt:456` |
| PRC_公积金异议回盘校验 | aaa020, aaa030, aae002 | `rcpq-service/src/main/kotlin/com/jlrc/rcpq/service/td/TdcgjCases.kt:443` |
| PRC_医保异议同步 | aaa030, aaa100, aae002 | `rcpq-service/src/main/kotlin/com/jlrc/rcpq/service/td/Jishu6Cases.kt:508` |
| PRC_医保退休停保 | aaa604, atc900 | `rcpq-service/src/main/kotlin/com/jlrc/rcpq/service/td/Jishu6Cases.kt:530` |
| PRC_变更参保地 | aaa020, atc900, xz | `rcpq-service/src/main/kotlin/com/jlrc/rcpq/service/td/Jishu6Cases.kt:540` |
| PRC_市医保异动回盘提交 | aaa020, aaa030, aae002, aae011 | `rcpq-service/src/main/kotlin/com/jlrc/rcpq/service/td/TdcAyCases.kt:336` |
| PRC_市医保异动回盘校验 | aaa020, aaa030, aae002 | `rcpq-service/src/main/kotlin/com/jlrc/rcpq/service/td/TdcAyCases.kt:323` |
| PRC_市工伤异动回盘提交 | aaa020, aaa030, aae002, aae011 | `rcpq-service/src/main/kotlin/com/jlrc/rcpq/service/td/TdcbpCases.kt:885` |
| PRC_市工伤异动回盘校验 | aaa020, aaa030, aae002 | `rcpq-service/src/main/kotlin/com/jlrc/rcpq/service/td/TdcbpCases.kt:873` |
| PRC_异议处理 | aae011, flag, id | `rcpq-service/src/main/kotlin/com/jlrc/rcpq/service/td/Jishu6Cases.kt:490` |
| PRC_异议离职同步 | — | `rcpq-service/src/main/kotlin/com/jlrc/rcpq/service/td/Jishu6Cases.kt:501` |
| PRC_省医保基数补差回盘提交 | aaa020, aaa030, aae002, aae011, ace100 | `rcpq-service/src/main/kotlin/com/jlrc/rcpq/service/td/TdcahCases.kt:465` |
| PRC_省医保基数补差回盘校验 | ace100 | `rcpq-service/src/main/kotlin/com/jlrc/rcpq/service/td/TdcahCases.kt:454` |
| PRC_省医保工资表 | aaa030, aaa110, aae002, aae011 | `rcpq-service/src/main/kotlin/com/jlrc/rcpq/service/td/TdcahCases.kt:477` |
| PRC_省医保异动回盘提交 | aaa020, aaa030, aae002, aae011 | `rcpq-service/src/main/kotlin/com/jlrc/rcpq/service/td/TdcahCases.kt:440` |
| PRC_省医保异动回盘校验 | aaa020, aaa030, aae002 | `rcpq-service/src/main/kotlin/com/jlrc/rcpq/service/td/TdcahCases.kt:427` |
| PRC_省工伤减免提交 | aaa020, aaa030, aae002, aae011, ace100 | `rcpq-service/src/main/kotlin/com/jlrc/rcpq/service/td/TdcbpCases.kt:838` |
| PRC_省工伤减免校验 | ace100 | `rcpq-service/src/main/kotlin/com/jlrc/rcpq/service/td/TdcbpCases.kt:827` |
| PRC_省工伤基数补差回盘提交 | aaa020, aaa030, aae002, aae011, ace100 | `rcpq-service/src/main/kotlin/com/jlrc/rcpq/service/td/TdcbpCases.kt:861` |
| PRC_省工伤基数补差回盘校验 | ace100 | `rcpq-service/src/main/kotlin/com/jlrc/rcpq/service/td/TdcbpCases.kt:850` |
| PRC_省工伤异动回盘提交 | aaa020, aaa030, aae002, aae011 | `rcpq-service/src/main/kotlin/com/jlrc/rcpq/service/td/TdcbpCases.kt:814` |
| PRC_省工伤异动回盘校验 | aaa020, aaa030, aae002 | `rcpq-service/src/main/kotlin/com/jlrc/rcpq/service/td/TdcbpCases.kt:802` |
| PRC_社保异动回盘提交 | aaa020, aaa030, aae002, aae011 | `rcpq-service/src/main/kotlin/com/jlrc/rcpq/service/td/TdckProcs.kt:22` |
| PRC_社保异动回盘校验 | aaa020, aaa030, aae002 | `rcpq-service/src/main/kotlin/com/jlrc/rcpq/service/td/TdckProcs.kt:9` |
| PRC_社保补缴明细 | aaa020, aaa030, aae002 | `rcpq-service/src/main/kotlin/com/jlrc/rcpq/service/td/TdckPaymentDetail.kt:228` |
| PRC_社保退休停保 | aaa204, atc900 | `rcpq-service/src/main/kotlin/com/jlrc/rcpq/service/td/Jishu6Cases.kt:520` |

### PKG_保险补退（1）
| 过程 | 参数(源码抽取) | 实现 |
|---|---|---|
| PRC_保险当月补缴 | aae002, atc900, lb | `rcpq-service/src/main/kotlin/com/jlrc/rcpq/service/td/BbfCalc.kt:326` |

### PKG_派遣保险月情况（13）
| 过程 | 参数(源码抽取) | 实现 |
|---|---|---|
| PRC_工伤月情况 | aaa020, aaa030, aae002, aae011 | `rcpq-service/src/main/kotlin/com/jlrc/rcpq/service/td/Yueqk2Cases.kt:282` |
| PRC_工伤补缴情况 | aaa020, aaa030, aae002, aae011 | `rcpq-service/src/main/kotlin/com/jlrc/rcpq/service/td/Yueqk5Cases.kt:462` |
| PRC_市公积金月情况 | aaa020, aaa030, aae002, aae011 | `rcpq-service/src/main/kotlin/com/jlrc/rcpq/service/td/Yueqk2Cases.kt:265` |
| PRC_市公积金补缴情况 | aaa020, aaa030, aae002, aae011 | `rcpq-service/src/main/kotlin/com/jlrc/rcpq/service/td/Yueqk5Cases.kt:445` |
| PRC_市医保应缴费情况 | aaa020, aaa030, aae002, aae011 | `rcpq-service/src/main/kotlin/com/jlrc/rcpq/service/td/Yueqk4Cases.kt:104` |
| PRC_市医保月情况 | aaa020, aaa030, aae002, aae011 | `rcpq-service/src/main/kotlin/com/jlrc/rcpq/service/td/Yueqk3Cases.kt:256` |
| PRC_月垫付情况 | aaa030, aae002, aae011 | `rcpq-service/src/main/kotlin/com/jlrc/rcpq/service/td/Yueqk6Cases.kt:379` |
| PRC_月转款确认 | aaa020, aaa030, aae002, aae011, tabname | `rcpq-service/src/main/kotlin/com/jlrc/rcpq/service/td/YueqkCases.kt:245` |
| PRC_省公积金月情况 | aaa020, aaa030, aae002, aae011 | `rcpq-service/src/main/kotlin/com/jlrc/rcpq/service/td/Yueqk2Cases.kt:248` |
| PRC_省公积金补缴情况 | aaa020, aaa030, aae002, aae011 | `rcpq-service/src/main/kotlin/com/jlrc/rcpq/service/td/Yueqk5Cases.kt:479` |
| PRC_省医保月情况 | aaa020, aaa030, aae002, aae011 | `rcpq-service/src/main/kotlin/com/jlrc/rcpq/service/td/Yueqk3Cases.kt:239` |
| PRC_省医保补缴情况 | aaa020, aaa030, aae002, aae011 | `rcpq-service/src/main/kotlin/com/jlrc/rcpq/service/td/Yueqk5Cases.kt:428` |
| PRC_社保月情况 | aaa020, aaa030, aae002, aae011 | `rcpq-service/src/main/kotlin/com/jlrc/rcpq/service/td/YueqkCases.kt:228` |

### PKG_派遣批量导入（29）
| 过程 | 参数(源码抽取) | 实现 |
|---|---|---|
| PRC_保险补贴导入提交 | aae011, ace100 | `rcpq-service/src/main/kotlin/com/jlrc/rcpq/service/td/TdbpImportCases.kt:2979` |
| PRC_保险补贴导入校验 | aae011, ace100 | `rcpq-service/src/main/kotlin/com/jlrc/rcpq/service/td/TdbpImportCases.kt:2969` |
| PRC_发票导入提交 | aae011, ace100 | `rcpq-service/src/main/kotlin/com/jlrc/rcpq/service/td/TdbpImportCases.kt:2911` |
| PRC_发票导入校验 | ace100 | `rcpq-service/src/main/kotlin/com/jlrc/rcpq/service/td/TdbpImportCases.kt:2902` |
| PRC_吉林银行到账导入提交 | aae011, ace100 | `rcpq-service/src/main/kotlin/com/jlrc/rcpq/service/td/TdbpImportCases.kt:2892` |
| PRC_吉林银行到账导入校验 | ace100 | `rcpq-service/src/main/kotlin/com/jlrc/rcpq/service/td/TdbpImportCases.kt:2883` |
| PRC_员工导入提交 | aae011, ace100 | `rcpq-service/src/main/kotlin/com/jlrc/rcpq/service/td/TdbpImportCases.kt:2731` |
| PRC_员工导入校验 | aae011, ace100 | `rcpq-service/src/main/kotlin/com/jlrc/rcpq/service/td/TdbpImportCases.kt:2721` |
| PRC_员工更新提交 | aae011, ace100 | `rcpq-service/src/main/kotlin/com/jlrc/rcpq/service/td/TdbpImportCases.kt:2771` |
| PRC_员工更新校验 | aae011, ace100 | `rcpq-service/src/main/kotlin/com/jlrc/rcpq/service/td/TdbpImportCases.kt:2761` |
| PRC_员工终止导入提交 | aae011, ace100 | `rcpq-service/src/main/kotlin/com/jlrc/rcpq/service/td/TdbpImportCases.kt:2751` |
| PRC_员工终止导入校验 | aae011, ace100 | `rcpq-service/src/main/kotlin/com/jlrc/rcpq/service/td/TdbpImportCases.kt:2741` |
| PRC_员工续签提交 | aae011, ace100 | `rcpq-service/src/main/kotlin/com/jlrc/rcpq/service/td/TdbpImportCases.kt:2791` |
| PRC_员工续签校验 | aae011, ace100 | `rcpq-service/src/main/kotlin/com/jlrc/rcpq/service/td/TdbpImportCases.kt:2781` |
| PRC_工行到账导入提交 | aae011, ace100 | `rcpq-service/src/main/kotlin/com/jlrc/rcpq/service/td/TdbpImportCases.kt:2854` |
| PRC_工行到账导入校验 | ace100 | `rcpq-service/src/main/kotlin/com/jlrc/rcpq/service/td/TdbpImportCases.kt:2845` |
| PRC_序列 | aaa030, aaa110, aae002, lx | `rcpq-service/src/main/kotlin/com/jlrc/rcpq/service/td/TdbpImportCases.kt:2822` |
| PRC_建行到账导入提交 | aae011, ace100 | `rcpq-service/src/main/kotlin/com/jlrc/rcpq/service/td/TdbpImportCases.kt:2873` |
| PRC_建行到账导入校验 | ace100 | `rcpq-service/src/main/kotlin/com/jlrc/rcpq/service/td/TdbpImportCases.kt:2864` |
| PRC_意外险导入提交 | aae011, ace100 | `rcpq-service/src/main/kotlin/com/jlrc/rcpq/service/td/TdbpImportCases.kt:2999` |
| PRC_意外险导入校验 | aae011, ace100 | `rcpq-service/src/main/kotlin/com/jlrc/rcpq/service/td/TdbpImportCases.kt:2989` |
| PRC_新增保险提交 | aae011, ace100 | `rcpq-service/src/main/kotlin/com/jlrc/rcpq/service/td/TdbpImportCases.kt:2811` |
| PRC_新增保险校验 | aae011, ace100 | `rcpq-service/src/main/kotlin/com/jlrc/rcpq/service/td/TdbpImportCases.kt:2801` |
| PRC_特殊情况导入提交 | aae011, ace100 | `rcpq-service/src/main/kotlin/com/jlrc/rcpq/service/td/TdbpImportCases.kt:2940` |
| PRC_特殊情况导入校验 | aae002, ace100 | `rcpq-service/src/main/kotlin/com/jlrc/rcpq/service/td/TdbpImportCases.kt:3009` |
| PRC_生育补贴提交 | aae011, ace100 | `rcpq-service/src/main/kotlin/com/jlrc/rcpq/service/td/TdbpImportCases.kt:2959` |
| PRC_生育补贴校验 | ace100 | `rcpq-service/src/main/kotlin/com/jlrc/rcpq/service/td/TdbpImportCases.kt:2950` |
| PRC_补偿金导入提交 | aae011, ace100 | `rcpq-service/src/main/kotlin/com/jlrc/rcpq/service/td/TdbpImportCases.kt:2930` |
| PRC_补偿金导入校验 | ace100 | `rcpq-service/src/main/kotlin/com/jlrc/rcpq/service/td/TdbpImportCases.kt:2921` |

### PKG_缴费基数（19）
| 过程 | 参数(源码抽取) | 实现 |
|---|---|---|
| PRC_公积金基数上下限模板 | atb930 | `rcpq-service/src/main/kotlin/com/jlrc/rcpq/service/td/Jishu5Tc0aCases.kt:565` |
| PRC_公积金基数上下限调整审核 | aae011, aae013, atc0a0, flag | `rcpq-service/src/main/kotlin/com/jlrc/rcpq/service/td/Jishu5Tc0aCases.kt:593` |
| PRC_公积金基数上下限调整提交 | aae011, ace100 | `rcpq-service/src/main/kotlin/com/jlrc/rcpq/service/td/Jishu5Tc0aCases.kt:583` |
| PRC_公积金基数上下限调整校验 | ace100 | `rcpq-service/src/main/kotlin/com/jlrc/rcpq/service/td/Jishu5Tc0aCases.kt:574` |
| PRC_公积金基数下限调整 | aae002, aae011 | `rcpq-service/src/main/kotlin/com/jlrc/rcpq/service/td/Jishu5Tc0aCases.kt:606` |
| PRC_公积金未申报 | — | `rcpq-service/src/main/kotlin/com/jlrc/rcpq/service/td/Jishu5WsbCases.kt:196` |
| PRC_公积金缴费基数核对 | aaa020, aaa030, aae001 | `rcpq-service/src/main/kotlin/com/jlrc/rcpq/service/td/Jishu5Cases.kt:413` |
| PRC_公积金缴费基数确认 | — | `rcpq-service/src/main/kotlin/com/jlrc/rcpq/service/td/Jishu5Cases.kt:474` |
| PRC_医保未申报 | — | `rcpq-service/src/main/kotlin/com/jlrc/rcpq/service/td/Jishu5WsbCases.kt:190` |
| PRC_医保缴费基数核对 | aaa020, aaa030, aae001 | `rcpq-service/src/main/kotlin/com/jlrc/rcpq/service/td/Jishu5Cases.kt:398` |
| PRC_医保缴费基数确认 | — | `rcpq-service/src/main/kotlin/com/jlrc/rcpq/service/td/Jishu5Cases.kt:468` |
| PRC_基数导入提交 | aae011, ace100 | `rcpq-service/src/main/kotlin/com/jlrc/rcpq/service/td/Jishu5ImportCases.kt:429` |
| PRC_基数导入校验 | aae001, ace100 | `rcpq-service/src/main/kotlin/com/jlrc/rcpq/service/td/Jishu5ImportCases.kt:415` |
| PRC_工伤未申报 | — | `rcpq-service/src/main/kotlin/com/jlrc/rcpq/service/td/Jishu5WsbCases.kt:202` |
| PRC_工伤缴费基数核对 | aaa020, aaa030, aae001, aae002, aae011 | `rcpq-service/src/main/kotlin/com/jlrc/rcpq/service/td/Jishu5Cases.kt:428` |
| PRC_工伤缴费基数确认 | — | `rcpq-service/src/main/kotlin/com/jlrc/rcpq/service/td/Jishu5Cases.kt:480` |
| PRC_社保未申报 | — | `rcpq-service/src/main/kotlin/com/jlrc/rcpq/service/td/Jishu5WsbCases.kt:184` |
| PRC_社保缴费基数核对 | aaa020, aaa030, aae001 | `rcpq-service/src/main/kotlin/com/jlrc/rcpq/service/td/Jishu5Cases.kt:383` |
| PRC_社保缴费基数确认 | — | `rcpq-service/src/main/kotlin/com/jlrc/rcpq/service/td/Jishu5Cases.kt:462` |
| PRC_员工终止审核_批量 | prm_atb910(主键串), prm_aae011 | `rcpq-service/src/main/kotlin/com/jlrc/rcpq/service/td/TdrcAuditCases.kt:29` |

## 2. 查询/工作台端点（/wb 等 Controller 面）

### ExcelImportController — `/import`
| 方法 | 路径 |
|---|---|
| POST | `/import/tdgf` |
| POST | `/import/tddc` |

### HzController — `/wb/hz`
| 方法 | 路径 |
|---|---|
| POST | `/wb/hz/push` |
| GET | `/wb/hz/roster` |
| GET | `/wb/hz/heads` |
| POST | `/wb/hz/confirm` |
| POST | `/wb/hz/next` |
| POST | `/wb/hz/pass` |
| GET | `/wb/hz/bp` |
| GET | `/wb/hz/bj` |
| POST | `/wb/hz/import-bp` |

### InteractionGatewayController — `/interaction`
| 方法 | 路径 |
|---|---|
| GET | `/interaction/{code}` |
| GET | `/interaction/registry` |
| POST | `/interaction/{code}` |
| POST | `/interaction/{code}/validate` |

### JishuViewController — `/wb/jishu`
| 方法 | 路径 |
|---|---|
| GET | `/wb/jishu/aa97` |
| GET | `/wb/jishu/bp` |
| GET | `/wb/jishu/batches` |
| GET | `/wb/jishu/tc0a_bp` |
| GET | `/wb/jishu/tc0a` |
| GET | `/wb/jishu/dw` |
| GET | `/wb/jishu/gr` |

### ProcGatewayController — `/proc`
| 方法 | 路径 |
|---|---|
| POST | `/proc/{name:.+}` |
| GET | `/proc` |

### SysImportController — `/import/sys`
| 方法 | 路径 |
|---|---|
| POST | `/import/sys/{importId}` |
| GET | `/import/sys/{importId}/template` |

### WorkbenchController — `/wb`
| 方法 | 路径 |
|---|---|
| POST | `/wb/next-batch` |
| POST | `/wb/hire-batch` |
| GET | `/wb/audit-row/{atc900}` |
| POST | `/wb/salary-setup` |
| POST | `/wb/arrival-batch` |
| POST | `/wb/invoice-batch` |
| POST | `/wb/renew-batch` |
| GET | `/wb/audit-queue` |
| GET | `/wb/batch/{ace100}` |
| GET | `/wb/contract-search` |
| GET | `/wb/units` |
| GET | `/wb/agreements` |
| GET | `/wb/regions` |
| GET | `/wb/settle-units` |
| GET | `/wb/aaa030s` |
| GET | `/wb/districts` |
| GET | `/wb/opers` |
| GET | `/wb/codes` |
| GET | `/wb/arrivals` |
| GET | `/wb/invoices` |
| POST | `/wb/terminate-batch` |
| GET | `/wb/roster` |
| GET | `/wb/roster-export` |

### YueViewController — `/wb/yue`
| 方法 | 路径 |
|---|---|
| GET | `/wb/yue/main` |
| GET | `/wb/yue/detail` |
| GET | `/wb/yue/df` |
| GET | `/wb/yue/tj0c2` |

### YyViewController — `/wb/yy`
| 方法 | 路径 |
|---|---|
| GET | `/wb/yy/list` |

### SyncController — `/sync`
| 方法 | 路径 |
|---|---|
| POST | `/sync/run` |
| GET | `/sync/status` |
| GET | `/sync/tables` |

## 3. POST /interaction/{REQ-编号} — 注册 685 交互
泛型行编辑保存通道（传改好状态的整行）。清单=`GET /interaction/registry`；每交互的 store/实体绑定见 `InteractionRegistry.kt` 与 docs/operations-manual.md §2。

## 4. 导入与同步
| 端点 | 说明 |
|---|---|
| POST /import/tddc | 合同批量导入引擎（tc90_bp 族四路） |
| POST /import/sys/{importId} | 通用导入通道（sys_import 配置驱动, 契约=docs/import-channel.md） |
| GET /import/sys/{importId}/template | 模板生成下载 |
| POST /sync/run · GET /sync/status · GET /sync/tables | Oracle→PG 同步（生产=外挂 scripts/sync_daily.sh） |

## 5. 自述端点（运行时可查）
- `GET /proc` — 过程注册清单（名 单）
- `GET /interaction/registry` — 交互注册清单


> 2026-11-27: +PKG_人才派遣.PRC_员工终止审核_批量(桥单 021 补移植, 源=work/pkg_all/PACKAGE_BODY_PKG_人才派遣.sql:61-170; BC-01 缺口腿关闭)。110→111。
