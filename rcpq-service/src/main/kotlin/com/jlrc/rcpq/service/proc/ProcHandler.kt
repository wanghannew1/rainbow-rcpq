package com.jlrc.rcpq.service.proc

/**
 * PL/SQL 过程处理器（旧 lemis.util.getProcedure + ProcedureDAOUtil 通道的等价注册面）。
 *
 * 命名 = 原 "包名.过程名"（如 PKG_保险人员增减.PRC_社保异动回盘校验）。
 * 返回约定: {appCode: Int, errMsg: String?, ...业务键}；appCode>=1 成功，<1 失败（原 DEF_OK/DEF_ERR）。
 * 每翻译一个过程实现本接口注册即生效；未注册名由网关返回 appCode=-1 + 未实现提示。
 */
interface ProcHandler {
    /** 原 "包名.过程名" */
    fun procName(): String

    /** 形参名 = 原 PRM_xxx 小写（如 aae002/aaa030/aaa020/aae011） */
    fun handle(params: Map<String, Any?>): Map<String, Any?>
}
