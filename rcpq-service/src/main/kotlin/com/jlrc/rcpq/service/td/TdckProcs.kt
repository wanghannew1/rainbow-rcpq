package com.jlrc.rcpq.service.td

import com.jlrc.rcpq.service.proc.ProcHandler
import org.springframework.stereotype.Component

/** PKG_保险人员增减 过程注册（/proc/{name} 网关分发） */
@Component
class TdckCheckProc(private val tdck: TdckCases) : ProcHandler {
    override fun procName() = "PKG_保险人员增减.PRC_社保异动回盘校验"
    override fun handle(params: Map<String, Any?>): Map<String, Any?> {
        val aae002 = params["aae002"]?.toString() ?: return mapOf("appCode" to -1, "errMsg" to "缺少 aae002")
        val aaa030 = params["aaa030"]?.toString() ?: return mapOf("appCode" to -1, "errMsg" to "缺少 aaa030")
        val aaa020 = params["aaa020"]?.toString() ?: return mapOf("appCode" to -1, "errMsg" to "缺少 aaa020")
        val r = tdck.fallbackCheck(aae002, aaa030, aaa020)
        return mapOf("appCode" to r["code"], "errMsg" to r["msg"], "rows" to r["rows"],
            "pass" to r["pass"], "reject" to r["reject"])
    }
}

@Component
class TdckCommitProc(private val tdck: TdckCases) : ProcHandler {
    override fun procName() = "PKG_保险人员增减.PRC_社保异动回盘提交"
    override fun handle(params: Map<String, Any?>): Map<String, Any?> {
        val aae002 = params["aae002"]?.toString() ?: return mapOf("appCode" to -1, "errMsg" to "缺少 aae002")
        val aaa030 = params["aaa030"]?.toString() ?: return mapOf("appCode" to -1, "errMsg" to "缺少 aaa030")
        val aaa020 = params["aaa020"]?.toString() ?: return mapOf("appCode" to -1, "errMsg" to "缺少 aaa020")
        val aae011 = params["aae011"]?.toString() ?: return mapOf("appCode" to -1, "errMsg" to "缺少 aae011")
        val r = tdck.fallbackCommit(aae002, aaa030, aaa020, aae011)
        return mapOf("appCode" to r["code"], "errMsg" to r["msg"],
            "stopApplied" to r["stopApplied"], "successApplied" to r["successApplied"],
            "objectionApplied" to r["objectionApplied"])
    }
}
