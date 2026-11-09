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
    private val xadCodeCases: XadCodeCases,
) {
    @GetMapping("/{code}")
    fun dispatch(
        @PathVariable code: String,
        @RequestParam params: Map<String, String>,
    ): Map<String, Any?> {
        val spec = registry.resolve(code)
            ?: return mapOf("error" to "未知交互码: $code", "code" to -30010)
        val store = storeOf(code)
        return when (spec.method) {
            "query" -> {
                val meta = metadataRegistry.meta(store)
                    ?: return mapOf("error" to "无store元数据: $store", "code" to -30010)
                val r = queryExecutor.query(meta.table, meta.columns, pksOf(meta), params)
                mapOf(
                    "interaction" to spec.code,
                    "routedTo" to "${spec.bean}.${spec.method}",
                    "store" to store,
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

    /** 交互码 REQ-<page>-<store>-<action> → store 名 */
    private fun storeOf(code: String): String =
        code.split("-").let { if (it.size >= 3) it[it.size - 2] else "" }

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
        val store = storeOf(code)
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
