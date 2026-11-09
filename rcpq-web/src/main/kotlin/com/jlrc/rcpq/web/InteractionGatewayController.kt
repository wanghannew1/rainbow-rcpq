package com.jlrc.rcpq.web

import com.jlrc.rcpq.service.DefaultCrudDispatcher
import com.jlrc.rcpq.service.InteractionContext
import com.jlrc.rcpq.service.MetadataRegistry
import com.jlrc.rcpq.service.QueryExecutor
import com.jlrc.rcpq.service.UpdateExecutor
import com.jlrc.rcpq.service.az.AzCases
import com.jlrc.rcpq.service.td.TdaProtocolCases
import com.jlrc.rcpq.service.td.TdbContractCases
import com.jlrc.rcpq.service.td.TdbcChangeCases
import com.jlrc.rcpq.service.td.TdbdStateMachineCases
import com.jlrc.rcpq.service.td.TdbeTdblCases
import com.jlrc.rcpq.service.td.TdboAjaxCase
import com.jlrc.rcpq.service.td.TdbfAuditCases
import com.jlrc.rcpq.service.td.TdcAhCases
import com.jlrc.rcpq.service.td.TdcBatchCases
import com.jlrc.rcpq.service.td.TdcjCases
import com.jlrc.rcpq.service.td.TdckCases
import com.jlrc.rcpq.service.xa.XabUnitCases
import com.jlrc.rcpq.service.xa.XadCodeCases
import com.jlrc.rcpq.service.MetadataValidator
import org.springframework.web.bind.annotation.*

/**
 * 交互网关（对应原 EAPActionServlet 的交互分发层）。
 * GET /interaction/{code} —— 按 REQ-* 交互码路由到通用分发器或自定义用例。
 * POST /interaction/{code}/validate —— 用真实列元数据校验行数据（行为对照观测面）。
 * TODO-BEHAVIOR: 返回结构需与原 dataStore JSON 对齐（前端依赖），先回显路由结果。
 */
@RestController
@RequestMapping("/interaction")
class InteractionGatewayController(
    private val registry: InteractionRegistry,
    private val dispatcher: DefaultCrudDispatcher,
    private val metadataRegistry: MetadataRegistry,
    private val queryExecutor: QueryExecutor,
    private val updateExecutor: UpdateExecutor,
    private val xabUnitCases: XabUnitCases,
    private val azCases: AzCases,
    private val tdaCases: TdaProtocolCases,
    private val tdbCases: TdbContractCases,
    private val tdbcChangeCases: TdbcChangeCases,
    private val tdbdCases: TdbdStateMachineCases,
    private val tdbeTdblCases: TdbeTdblCases,
    private val tdbfAuditCases: TdbfAuditCases,
    private val tdboAjaxCase: TdboAjaxCase,
    private val tdckCases: TdckCases,
    private val tdcjCases: TdcjCases,
    private val tdcAhCases: TdcAhCases,
    private val tdcBatchCases: TdcBatchCases,
    private val xadCodeCases: XadCodeCases,
) {
    @GetMapping("/{code}")
    fun dispatch(
        @PathVariable code: String,
        @RequestParam params: Map<String, String>,
    ): Map<String, Any?> {
        val spec = registry.resolve(code)
            ?: return mapOf("error" to "未知交互码: $code", "code" to -30010)
        val storeCandidates = storeCandidates(code, spec, params["store"])
        return when (spec.method) {
            "query" -> {
                // 候选按序取首个有元数据的 store（码内段可能是页面名而非 store，回填清单兜底）
                val hit = storeCandidates.firstOrNull { metadataRegistry.meta(it) != null }
                val meta = hit?.let { metadataRegistry.meta(it) }
                    ?: return mapOf("error" to "无store元数据: ${storeCandidates.joinToString(",")}", "code" to -30010)
                val r = queryExecutor.query(meta.table, meta.columns, pksOf(meta), params)
                mapOf(
                    "interaction" to spec.code,
                    "routedTo" to "${spec.bean}.${spec.method}",
                    "store" to hit,
                    "table" to meta.table,
                    "rows" to r.rows,
                    "totalCount" to r.totalCount,
                    "page" to r.page,
                    "pageSize" to r.pageSize,
                )
            }
            "doQuery" -> mapOf(
                "interaction" to spec.code,
                "routedTo" to "${spec.bean}.${spec.method}",
                "codes" to xadCodeCases.queryCodes(params["category"] ?: "健康状况"),
            )
            else -> {
                val ctx = InteractionContext(params.toMutableMap())
                val result = when (spec.method) {
                    "update" -> dispatcher.update(ctx)
                    "count" -> dispatcher.count(ctx)
                    else -> ctx
                }
                mapOf(
                    "interaction" to spec.code,
                    "routedTo" to "${spec.bean}.${spec.method}",
                    "params" to result.params,
                )
            }
        }
    }

    /** 交互码 REQ-<page>-<store>-<action> → store 名（注意 3 段式 REQ-<page>-<action> 的中段是页面名，非 store） */
    private fun storeOf(code: String): String =
        code.split("-").let { if (it.size >= 3) it[it.size - 2] else "" }

    /**
     * store 候选链: 显式参数 > 码内中段 > 页面 XML datastores 回填清单。
     * 哈希交互码(354/683)无码内段；3 段式码中段是页面名——调用方按元数据命中顺序取候选。
     */
    private fun storeCandidates(code: String, spec: com.jlrc.rcpq.service.model.InteractionSpec, explicit: String?): List<String> =
        listOfNotNull(
            explicit?.takeIf { it.isNotBlank() },
            storeOf(code).takeIf { it.isNotBlank() },
        ).plus(spec.stores).distinct()

    private fun pksOf(meta: MetadataRegistry.StoreMeta): List<String> =
        meta.columns.filter { it.primaryKey }.map { it.name }

    @GetMapping("/registry")
    fun registry(): Map<String, Any> = mapOf(
        "count" to registry.size(),
        "interactions" to registry.all().map { mapOf("code" to it.code, "page" to it.page, "method" to it.method) },
    )

    /**
     * 保存通道：POST /interaction/{code}，body={"rows":[{__state:new|modified|deleted,...}]}
     * 对应原 getDS().save() 交互（method=update）。
     */
    @PostMapping("/{code}")
    fun update(@PathVariable code: String, @RequestBody body: Map<String, Any?>): Map<String, Any?> {
        val spec = registry.resolve(code)
            ?: return mapOf("error" to "未知交互码: $code", "code" to -30010)
        val storeCandidates = storeCandidates(code, spec, body["store"]?.toString())
        val store = storeCandidates.firstOrNull { metadataRegistry.meta(it) != null } ?: ""
        // td/b/a 合同用例路由
        if (spec.bean?.endsWith("tdbaService") == true && spec.method == "update") {
            val tc90 = (body["tc90"] as? Map<String, Any?>) ?: return mapOf("error" to "缺少 tc90", "code" to -30001)
            val tb91 = (body["tb91"] as? Map<String, Any?>) ?: return mapOf("error" to "缺少 tb91", "code" to -30001)
            val tc9e = body["tc9e"] as? Map<String, Any?>
            val r = tdbCases.saveContract(tc90, tb91, tc9e)
            return mapOf("interaction" to code, "saved" to r)
        }
        if (spec.bean?.endsWith("tdbaService") == true && spec.method == "doCommit") {
            val tc90 = (body["tc90"] as? Map<String, Any?>) ?: return mapOf("error" to "缺少 tc90", "code" to -30001)
            val tb91 = (body["tb91"] as? Map<String, Any?>) ?: return mapOf("error" to "缺少 tb91", "code" to -30001)
            val r = tdbCases.commitContract(tc90, tb91)
            return mapOf("interaction" to code, "committed" to r)
        }

        // td/b/o 起始日期变更 ajax 直调（旧 SimpleAjax bean 名通道）
        if (spec.bean?.endsWith("tdboAjax") == true && spec.method == "doAjax") {
            val atc900 = (body["atc900"] ?: return mapOf("error" to "缺少 atc900", "code" to -30001))
                .toString().toLongOrNull() ?: return mapOf("error" to "atc900 非数值", "code" to -30001)
            val r = tdboAjaxCase.doAjax(
                atc900,
                body["atc90d"]?.toString() ?: return mapOf("error" to "缺少 atc90d", "code" to -30001),
                body["atc90dOld"]?.toString() ?: "")
            return mapOf("interaction" to code, "ajax" to r)
        }

        // td/c/k 社保报增报减中枢路由
        if (spec.bean?.endsWith("tdckService") == true) {
            when (spec.method) {
                "delete", "delete1" -> {
                    // 两 store 同语义: 按(年月,户头,区划)整批删 TC9W_BP
                    val r = tdckCases.deleteTc9wBp(
                        body["aae002"]?.toString() ?: return mapOf("error" to "缺少 aae002", "code" to -30001),
                        body["aaa030"]?.toString() ?: return mapOf("error" to "缺少 aaa030", "code" to -30001),
                        body["aaa020"]?.toString() ?: return mapOf("error" to "缺少 aaa020", "code" to -30001))
                    return mapOf("interaction" to code, "deleted" to r)
                }
                "doSave" -> {
                    val tc9w = body["tc9w"] as? Map<String, Any?>
                        ?: return mapOf("error" to "缺少 tc9w", "code" to -30001)
                    val r = tdckCases.doSave(tc9w)
                    return mapOf("interaction" to code, "doSave" to r)
                }
            }
        }

        // td/c 7d: 批删克隆家族(tdca/tdcae/tdcaj) + tdcl AA97 保存
        if (spec.bean?.endsWith("tdcaService") == true && spec.method in listOf("delete", "delete1")) {
            val r = tdcBatchCases.delete("tc9w_bp03", mapOf(
                "aae002" to (body["aae002"]?.toString() ?: return mapOf("error" to "缺少 aae002", "code" to -30001))))
            return mapOf("interaction" to code, "deleted" to r)
        }
        if (spec.bean?.endsWith("tdcaeService") == true && spec.method in listOf("delete", "delete1")) {
            val r = if (spec.method == "delete") tdcBatchCases.delete("tc9u_bp02", mapOf(
                "aae002" to (body["aae002"]?.toString() ?: return mapOf("error" to "缺少 aae002", "code" to -30001)),
                "aaa100" to (body["aaa100"]?.toString() ?: return mapOf("error" to "缺少 aaa100", "code" to -30001)),
                "aaa030" to (body["aaa030"]?.toString() ?: return mapOf("error" to "缺少 aaa030", "code" to -30001))))
            else tdcBatchCases.delete("tc9u_bp03", mapOf(
                "aae002" to (body["aae002"]?.toString() ?: return mapOf("error" to "缺少 aae002", "code" to -30001)),
                "aaa020" to (body["aaa020"]?.toString() ?: return mapOf("error" to "缺少 aaa020", "code" to -30001)),
                "aaa030" to (body["aaa030"]?.toString() ?: return mapOf("error" to "缺少 aaa030", "code" to -30001))))
            return mapOf("interaction" to code, "deleted" to r)
        }
        if (spec.bean?.endsWith("tdcajService") == true) {
            when (spec.method) {
                "delete", "delete1" -> {
                    val r = tdcBatchCases.delete("tc9w_bp02", mapOf(
                        "aae002" to (body["aae002"]?.toString() ?: return mapOf("error" to "缺少 aae002", "code" to -30001)),
                        "aaa030" to (body["aaa030"]?.toString() ?: return mapOf("error" to "缺少 aaa030", "code" to -30001)),
                        "aaa020" to (body["aaa020"]?.toString() ?: return mapOf("error" to "缺少 aaa020", "code" to -30001))))
                    return mapOf("interaction" to code, "deleted" to r)
                }
                "doSave" -> {
                    val tc9u = body["tc9u"] as? Map<String, Any?>
                        ?: return mapOf("error" to "缺少 tc9u", "code" to -30001)
                    return mapOf("interaction" to code, "doSave" to tdcBatchCases.doSaveTc9u(tc9u))
                }
            }
        }
        if (spec.bean?.endsWith("TdclService") == true && spec.method == "update") {
            val aa97 = body["aa97"] as? Map<String, Any?>
                ?: return mapOf("error" to "缺少 aa97", "code" to -30001)
            return mapOf("interaction" to code, "saved" to tdcBatchCases.saveAa97(aa97))
        }

        // td/c 7c: 三份 tdcahService 合并路由(ah→bp01/ay→bp04/bp→bp09)
        if (spec.bean?.endsWith("tdcahService") == true) {
            val page = spec.page
            when (spec.method) {
                "delete" -> {
                    val r = tdcAhCases.delete(page,
                        body["aae002"]?.toString() ?: return mapOf("error" to "缺少 aae002", "code" to -30001),
                        body["aaa030"]?.toString() ?: return mapOf("error" to "缺少 aaa030", "code" to -30001),
                        body["aaa020"]?.toString() ?: return mapOf("error" to "缺少 aaa020", "code" to -30001))
                    return mapOf("interaction" to code, "deleted" to r)
                }
                "delete1" -> {
                    val r = tdcAhCases.delete1(page,
                        body["aae002"]?.toString() ?: return mapOf("error" to "缺少 aae002", "code" to -30001),
                        body["aaa030"]?.toString() ?: return mapOf("error" to "缺少 aaa030", "code" to -30001),
                        body["aaa020"]?.toString() ?: "")
                    return mapOf("interaction" to code, "deleted" to r)
                }
                "doSave" -> {
                    val tc9w1 = body["tc9w1"] as? Map<String, Any?>
                        ?: return mapOf("error" to "缺少 tc9w1", "code" to -30001)
                    return mapOf("interaction" to code, "doSave" to tdcAhCases.doSave(tc9w1))
                }
            }
        }

        // td/c/j 补贴确认路由
        if (spec.bean?.endsWith("tdcjService") == true) {
            when (spec.method) {
                "saveNobt" -> {
                    val ids = (body["ids"] as? List<*>)?.mapNotNull { it?.toString()?.toLongOrNull() }
                        ?: return mapOf("error" to "缺少 ids", "code" to -30001)
                    return mapOf("interaction" to code, "saveNobt" to tdcjCases.saveNobt(ids))
                }
                "saveConfirm" -> {
                    val ids = (body["ids"] as? List<*>)?.mapNotNull { it?.toString()?.toLongOrNull() }
                        ?: return mapOf("error" to "缺少 ids", "code" to -30001)
                    return mapOf("interaction" to code, "saveConfirm" to tdcjCases.saveConfirm(ids))
                }
                "AllConfirm" -> {
                    val year = body["year"]?.toString() ?: return mapOf("error" to "缺少 year", "code" to -30001)
                    return mapOf("interaction" to code, "allConfirm" to tdcjCases.allConfirm(year))
                }
            }
        }

        // td/c/k 报增/报减处理 ajax 直调
        if (spec.bean?.endsWith("tdckAjax") == true && spec.method == "doAjax") {
            val r = tdckCases.doAjax(
                body["atc9w0"]?.toString() ?: "",
                body["atc900"]?.toString() ?: return mapOf("error" to "缺少 atc900", "code" to -30001),
                body["flag"]?.toString() ?: "")
            return mapOf("interaction" to code, "ajax" to r)
        }

        // td/b/e 转移申请 + td/b/l 调转归档路由
        if (spec.bean?.endsWith("tdbeService") == true && spec.method == "update") {
            val atc900s = (body["atc900s"] as? List<*>)?.mapNotNull { it?.toString()?.toLongOrNull() }
                ?: return mapOf("error" to "缺少 atc900s", "code" to -30001)
            val r = tdbeTdblCases.transferApply(
                atc900s,
                body["aab001"]?.toString() ?: return mapOf("error" to "缺少 aab001", "code" to -30001),
                body["aab004"]?.toString() ?: return mapOf("error" to "缺少 aab004", "code" to -30001),
                body["time"]?.toString() ?: "")
            return mapOf("interaction" to code, "transferred" to r)
        }
        if (spec.bean?.endsWith("tdblService") == true && spec.method == "update") {
            val atc900s = (body["atc900s"] as? List<*>)?.mapNotNull { it?.toString()?.toLongOrNull() }
                ?: return mapOf("error" to "缺少 atc900s", "code" to -30001)
            val r = tdbeTdblCases.archiveForTransfer(atc900s, body["atb930"]?.toString() ?: "", body["tb91"] as? Map<String, Any?>)
            return mapOf("interaction" to code, "archived" to r)
        }

        // td/b/f 审核处理路由 (6 方法)
        if (spec.bean?.endsWith("tdbfService") == true) {
            val tc90 = body["tc90"] as? Map<String, Any?>
            val tb91 = body["tb91"] as? Map<String, Any?> ?: return mapOf("error" to "缺少 tb91", "code" to -30001)
            when (spec.method) {
                "doXinqCommit" -> {
                    val r = tdbfAuditCases.doXinqCommit(tc90 ?: return mapOf("error" to "缺少 tc90", "code" to -30001), tb91)
                    return mapOf("interaction" to code, "doXinqCommit" to r)
                }
                "doXinqCancle" -> {
                    val r = tdbfAuditCases.doXinqCancle(tc90 ?: return mapOf("error" to "缺少 tc90", "code" to -30001), tb91)
                    return mapOf("interaction" to code, "doXinqCancle" to r)
                }
                "doRenewCancle" -> {
                    val r = tdbfAuditCases.doRenewCancle(tb91)
                    return mapOf("interaction" to code, "doRenewCancle" to r)
                }
                "doJsdyCancle" -> {
                    val r = tdbfAuditCases.doJsdyCancle(tb91)
                    return mapOf("interaction" to code, "doJsdyCancle" to r)
                }
                "terminateCancel" -> {
                    val tc9e = body["tc9e"] as? Map<String, Any?> ?: return mapOf("error" to "缺少 tc9e", "code" to -30001)
                    val r = tdbfAuditCases.terminateCancel(tc90 ?: return mapOf("error" to "缺少 tc90", "code" to -30001), tb91, tc9e)
                    return mapOf("interaction" to code, "terminateCancel" to r)
                }
                "reCommit" -> {
                    val tc9e = body["tc9e"] as? Map<String, Any?> ?: return mapOf("error" to "缺少 tc9e", "code" to -30001)
                    val r = tdbfAuditCases.reCommit(tc90 ?: return mapOf("error" to "缺少 tc90", "code" to -30001), tb91, tc9e)
                    return mapOf("interaction" to code, "reCommit" to r)
                }
            }
        }

        // td/b/d 状态机路由 (8 方法)
        if (spec.bean?.endsWith("tdbdService") == true) {
            val tc90 = body["tc90"] as? Map<String, Any?>
            val tb91 = body["tb91"] as? Map<String, Any?> ?: return mapOf("error" to "缺少 tb91", "code" to -30001)
            when (spec.method) {
                "doNew" -> {
                    val r = tdbdCases.doNew(tc90 ?: return mapOf("error" to "缺少 tc90", "code" to -30001), tb91)
                    return mapOf("interaction" to code, "doNew" to r)
                }
                "doJbbg" -> {
                    val tc92 = body["tc92"] as? Map<String, Any?> ?: return mapOf("error" to "缺少 tc92", "code" to -30001)
                    val r = tdbdCases.doJbbg(tc92, tb91)
                    return mapOf("interaction" to code, "doJbbg" to r)
                }
                "doRenewAll" -> {
                    val newRows = (body["new"] as? List<Map<String, Any?>>) ?: emptyList()
                    val r = tdbdCases.doRenewAll(newRows, tb91)
                    return mapOf("interaction" to code, "doRenewAll" to r)
                }
                "doTerminateCancle" -> {
                    val r = tdbdCases.doTerminateCancle(
                        (body["atb910"] ?: return mapOf("error" to "缺少 atb910")).toString().toLong(),
                        body["atb911"]?.toString())
                    return mapOf("interaction" to code, "doTerminateCancle" to r)
                }
                "doJsdy" -> {
                    val r = tdbdCases.doJsdy(tb91, body["atb930"]?.toString())
                    return mapOf("interaction" to code, "doJsdy" to r)
                }
                "doBg" -> {
                    val r = tdbdCases.doBg(tc90 ?: return mapOf("error" to "缺少 tc90", "code" to -30001), tb91)
                    return mapOf("interaction" to code, "doBg" to r)
                }
                "doCbSh" -> {
                    val atc900 = (body["atc900"] ?: return mapOf("error" to "缺少 atc900")).toString().toLong()
                    val tc92 = body["tc92"] as? Map<String, Any?> ?: emptyMap()
                    val r = tdbdCases.doCbSh(atc900, tb91, tc92)
                    return mapOf("interaction" to code, "doCbSh" to r)
                }
            }
        }

        // td/b/c 合同变更用例路由
        if (spec.bean?.endsWith("TdbcService") == true && spec.method == "saveUpdate") {
            val tc92 = (body["tc92"] as? Map<String, Any?>) ?: return mapOf("error" to "缺少 tc92", "code" to -30001)
            val tc90 = (body["tc90"] as? Map<String, Any?>) ?: return mapOf("error" to "缺少 tc90", "code" to -30001)
            val r = tdbcChangeCases.saveUpdate(tc92, tc90)
            return mapOf("interaction" to code, "changeSaved" to r)
        }
        if (spec.bean?.endsWith("TdbcService") == true && spec.method == "changeJbxx") {
            val r = tdbcChangeCases.changeJbxx(
                (body["aac001"] ?: return mapOf("error" to "缺少 aac001")).toString().toLong())
            return mapOf("interaction" to code, "changeJbxx" to r)
        }

        // td/a 协议用例路由
        if (spec.bean?.endsWith("tdaaService") == true && spec.method == "doSave") {
            val tb90 = (body["tb90"] as? Map<String, Any?>) ?: return mapOf("error" to "缺少 tb90", "code" to -30001)
            val tb91 = (body["tb91"] as? Map<String, Any?>) ?: return mapOf("error" to "缺少 tb91", "code" to -30001)
            val r = tdaCases.saveAgreement(tb90, tb91)
            return mapOf("interaction" to code, "saved" to r)
        }
        if (spec.bean?.endsWith("tdaaService") == true && spec.method == "doCommit") {
            val tb90 = (body["tb90"] as? Map<String, Any?>) ?: return mapOf("error" to "缺少 tb90", "code" to -30001)
            val tb91 = (body["tb91"] as? Map<String, Any?>) ?: return mapOf("error" to "缺少 tb91", "code" to -30001)
            val r = tdaCases.commitAgreement(tb90, tb91, body["aab004"]?.toString(), body["aab998"]?.toString())
            return mapOf("interaction" to code, "committed" to r)
        }
        if (spec.bean?.endsWith("tdabService") == true && spec.method == "doContinue") {
            val newR = (body["new"] as? Map<String, Any?>) ?: return mapOf("error" to "缺少 new", "code" to -30001)
            val tmpR = (body["tmp"] as? Map<String, Any?>) ?: return mapOf("error" to "缺少 tmp", "code" to -30001)
            val tb91 = (body["tb91"] as? Map<String, Any?>) ?: return mapOf("error" to "缺少 tb91", "code" to -30001)
            val r = tdaCases.continueAgreement(newR, tmpR, tb91, body["atb90w"]?.toString())
            return mapOf("interaction" to code, "continued" to r)
        }


        @Suppress("UNCHECKED_CAST")
        val rows = (body["rows"] as? List<Map<String, Any?>>)
            ?: return mapOf("error" to "缺少 rows 数组", "code" to -30001)
        if (spec.bean == "XaabService" && spec.method == "doUpdate") {
            val saved = xabUnitCases.saveUnit(rows.first())
            return mapOf("interaction" to code, "store" to store, "saved" to saved)
        }
        if (spec.bean == "XaabService" && spec.method == "doDelete") {
            val id = rows.first()["id"]?.toString()
                ?: return mapOf("error" to "缺少 id", "code" to -30001)
            val r = xabUnitCases.deleteUnit(id)
            return mapOf("interaction" to code, "store" to store, "deleted" to r)
        }
        // az 自定义用例路由 (bean.method → AzCases)
        val azRoute = when ("${spec.bean}.${spec.method}") {
            "azaa_azaaService.update" -> { val r = azCases.saveUnitTree(rows.first()); return mapOf("interaction" to code, "saved" to r) }
            "azaa_azaaService.doSave" -> { val r = azCases.saveContact(rows.first()); return mapOf("interaction" to code, "saved" to r) }
            "azab_azabService.update" -> { val r = azCases.auditUnitUpdate(rows.first()); return mapOf("interaction" to code, "audited" to r) }
            "AzbaSave.save" -> { val r = azCases.savePerson(rows.first()); return mapOf("interaction" to code, "saved" to r) }
            "AzbaService.changeJbxx" -> {
                val r = azCases.changeJbxx(rows.first())
                return mapOf("interaction" to code, "changeJbxx" to r)
            }
            "AzbaService.doUpdate" -> { val r = azCases.updateLb(rows.first()); return mapOf("interaction" to code, "saved" to r) }
            "azbb_azbbService.update" -> { val r = azCases.auditPersonUpdate(rows.first()); return mapOf("interaction" to code, "audited" to r) }
            "azbd.del" -> {
                val id = rows.first()["aac001"] ?: return mapOf("error" to "缺少 aac001", "code" to -30001)
                val r = azCases.deletePerson(mapOf("aac001" to id))
                return mapOf("interaction" to code, "deleted" to r)
            }
            "azbe.save" -> {
                val r = azCases.mergePerson(
                    (body["h1Aac001"] ?: return mapOf("error" to "缺少 h1Aac001")).toString().toLong(),
                    (body["h2Aac001"] ?: return mapOf("error" to "缺少 h2Aac001")).toString().toLong(),
                    (body["key"] ?: rows.firstOrNull()?.get("aac001") ?: return mapOf("error" to "缺少 key")).toString().toLong())
                return mapOf("interaction" to code, "merged" to r)
            }
            else -> null
        }
        if (spec.method != "update") return mapOf("error" to "非保存交互: ${spec.method}", "code" to -30001)
        return try {
            val r = updateExecutor.execute(store, rows)
            mapOf("interaction" to code, "store" to store,
                "inserted" to r.inserted, "updated" to r.updated, "deleted" to r.deleted)
        } catch (e: IllegalArgumentException) {
            mapOf("error" to (e.message ?: "参数错误"), "code" to -30001)
        }
    }

    /**
     * 行数据元数据校验。交互码形如 REQ-<page>-<store>-<action>，store 取倒数第二段。
     * body = 行数据（列名→值）；可选 "__isNew"=true 表示新增记录（主键非空校验跳过）。
     * 校验规则/错误码/文案与原系统一致（MetadataValidator）。
     */
    @PostMapping("/{code}/validate")
    fun validate(@PathVariable code: String, @RequestBody body: Map<String, Any?>): Map<String, Any?> {
        val spec = registry.resolve(code)
            ?: return mapOf("error" to "未知交互码: $code", "code" to -30010)
        val parts = code.split("-")
        val store = if (parts.size >= 3) parts[parts.size - 2] else ""
        val meta = metadataRegistry.meta(store)
            ?: return mapOf("error" to "无store元数据: $store", "code" to -30010)
        val result = MetadataValidator.validateRow(body, meta.columns, isNew = body["__isNew"] == true)
        return mapOf(
            "interaction" to code,
            "store" to store,
            "table" to meta.table,
            "valid" to result.valid,
            "code" to result.code,
            "message" to result.message,
        )
    }
}
