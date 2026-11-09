package com.jlrc.rcpq.service.calc

import com.jlrc.rcpq.service.proc.ProcHandler
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

/**
 * 险金引擎 /proc 注册面:
 * - PKG_人才派遣公共.PRC_计算险金        {atc900, aae002, jslb?} → 全字段结果(只读)
 * - PKG_人才派遣公共.PRC_测试计算险金过程 {atc900, aae002}        → 覆写 TC01 该合同行(原过程 COMMIT)
 * - PKG_人才派遣公共.PRC_获取补退基数    {atc900, nf}            → 六基数
 */
@Component
class CalcProcs(
    private val engine: InsuranceCalcEngine,
    private val jdbcTemplate: JdbcTemplate,
) {
    /** 全列小写 Map(引擎按列名取值) */
    fun loadTc90(atc900: Long): Map<String, Any?>? = jdbcTemplate.query(
        "SELECT * FROM tc90 WHERE atc900=?", { rs, _ ->
            val md = rs.metaData
            (1..md.columnCount).associate { md.getColumnLabel(it).lowercase() to rs.getObject(it) }
        }, atc900).firstOrNull()

    @Component
    class CalcProc(private val engine: InsuranceCalcEngine, private val calcProcs: CalcProcs) : ProcHandler {
        override fun procName() = "PKG_人才派遣公共.PRC_计算险金"
        override fun handle(params: Map<String, Any?>): Map<String, Any?> {
            val atc900 = (params["atc900"] ?: return mapOf("appCode" to -1, "errMsg" to "缺少 atc900")).toString().toLongOrNull()
                ?: return mapOf("appCode" to -1, "errMsg" to "atc900 非数值")
            // 可疑6 等价: 原 PRM_AAE002 NULL → SYSDATE 年月
            val aae002 = params["aae002"]?.toString()
                ?: java.time.YearMonth.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMM"))
            val row = calcProcs.loadTc90(atc900)
                ?: return mapOf("appCode" to -1, "errMsg" to "计算险金出错，错误信息：合同不存在，请将错误信息发给维护工程师")
            // 可疑5: 征缴路径允许调用方自带费率(xjbl.atc900 非空即沿用)
            val xjbl = (params["xjbl"] as? Map<String, Any?>)?.let { x ->
                InsuranceCalcEngine.Tc01(jslb = x["jslb"]?.toString()).apply {
                    this.atc900 = x["atc900"]?.toString()?.toLongOrNull()
                    (x["atb90k"] as? Number)?.let { atb90k = java.math.BigDecimal(it.toString()) }
                    (x["atb90l"] as? Number)?.let { atb90l = java.math.BigDecimal(it.toString()) }
                    (x["atb90m"] as? Number)?.let { atb90m = java.math.BigDecimal(it.toString()) }
                    (x["atb90n"] as? Number)?.let { atb90n = java.math.BigDecimal(it.toString()) }
                    (x["atb90n_j"] as? Number)?.let { atb90nJ = java.math.BigDecimal(it.toString()) }
                    (x["atb90o"] as? Number)?.let { atb90o = java.math.BigDecimal(it.toString()) }
                    (x["atb90p"] as? Number)?.let { atb90p = java.math.BigDecimal(it.toString()) }
                    (x["atb90q"] as? Number)?.let { atb90q = java.math.BigDecimal(it.toString()) }
                    (x["atb90r"] as? Number)?.let { atb90r = java.math.BigDecimal(it.toString()) }
                    (x["atb90s_dw"] as? Number)?.let { atb90sDw = java.math.BigDecimal(it.toString()) }
                    (x["atb90s_gr"] as? Number)?.let { atb90sGr = java.math.BigDecimal(it.toString()) }
                    (x["aaa399"] as? Number)?.let { aaa399 = java.math.BigDecimal(it.toString()) }
                }
            }
            val (code, result) = engine.calc(row, aae002, xjbl)
            return when (code) {
                -1 -> mapOf("appCode" to code, "errMsg" to result)
                else -> {
                    val r = result as InsuranceCalcEngine.Tc01
                    mapOf("appCode" to 1, "errMsg" to null as String?,
                        "aaa301" to r.aaa301, "aaa302" to r.aaa302, "aaa303" to r.aaa303,
                        "aaa304" to r.aaa304, "aaa305" to r.aaa305, "aaa306" to r.aaa306,
                        "atb90k" to r.atb90k, "atb90l" to r.atb90l, "atb90m" to r.atb90m,
                        "atb90n" to r.atb90n, "atb90n_j" to r.atb90nJ, "atb90o" to r.atb90o,
                        "atb90p" to r.atb90p, "atb90q" to r.atb90q, "atb90r" to r.atb90r,
                        "atb90s_dw" to r.atb90sDw, "atb90s_gr" to r.atb90sGr, "aaa399" to r.aaa399,
                        "bba001" to r.bba001, "bba002" to r.bba002, "bba003" to r.bba003,
                        "bba004" to r.bba004, "bba005" to r.bba005, "atb90t" to r.atb90t,
                        "baa001" to r.baa001, "baa002" to r.baa002, "baa003" to r.baa003,
                        "daa001" to r.daa001, "daa002" to r.daa002,
                        "caa001" to r.caa001, "caa002" to r.caa002,
                        "bba000" to r.bba000, "baa000" to r.baa000,
                        "bba200" to r.bba200, "baa200" to r.baa200, "atc9eh" to r.atc9eh)
                }
            }
        }
    }

    @Component
    class TestCalcProc(
        private val engine: InsuranceCalcEngine,
        private val calcProcs: CalcProcs,
        private val jdbcTemplate: JdbcTemplate,
    ) : ProcHandler {
        override fun procName() = "PKG_人才派遣公共.PRC_测试计算险金过程"

        /** 原过程 DELETE+INSERT TC01 并 COMMIT(金测通道) */
        @Transactional
        override fun handle(params: Map<String, Any?>): Map<String, Any?> {
            val atc900 = (params["atc900"] ?: return mapOf("appCode" to -1, "errMsg" to "缺少 atc900")).toString().toLongOrNull()
                ?: return mapOf("appCode" to -1, "errMsg" to "atc900 非数值")
            val aae002 = params["aae002"]?.toString() ?: return mapOf("appCode" to -1, "errMsg" to "缺少 aae002")
            val row = calcProcs.loadTc90(atc900)
                ?: return mapOf("appCode" to -1, "errMsg" to "计算险金出错：合同不存在")
            val (code, result) = engine.calc(row, aae002, InsuranceCalcEngine.Tc01(jslb = "1"))
            if (code == -1) return mapOf("appCode" to code, "errMsg" to result)
            val r = result as InsuranceCalcEngine.Tc01
            jdbcTemplate.update("DELETE FROM tc01 WHERE atc900=?", atc900)
            jdbcTemplate.update(
                """INSERT INTO tc01 (atc900, atb930, aac001, aac003, aac002, jslb,
                   aaa301, aaa302, aaa303, aaa304, aaa305, aaa306, aaa308,
                   atb90k, atb90l, atb90m, atb90n, atb90n_j, atb90o, atb90p, atb90q, atb90r,
                   atb90s_dw, atb90s_gr, aaa399,
                   bba001, bba002, bba003, bba004, bba005, atb90t, bba000,
                   baa001, baa002, baa003, daa001, daa002, baa000,
                   caa001, caa002, bba200, baa200, atc9eh)
                   VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)""",
                r.atc900, r.atb930, r.aac001, r.aac003, r.aac002, r.jslb,
                r.aaa301, r.aaa302, r.aaa303, r.aaa304, r.aaa305, r.aaa306, r.aaa308,
                r.atb90k, r.atb90l, r.atb90m, r.atb90n, r.atb90nJ, r.atb90o, r.atb90p, r.atb90q, r.atb90r,
                r.atb90sDw, r.atb90sGr, r.aaa399,
                r.bba001, r.bba002, r.bba003, r.bba004, r.bba005, r.atb90t, r.bba000,
                r.baa001, r.baa002, r.baa003, r.daa001, r.daa002, r.baa000,
                r.caa001, r.caa002, r.bba200, r.baa200, r.atc9eh)
            return mapOf("appCode" to 1, "errMsg" to null as String?, "atc900" to atc900, "aae002" to aae002)
        }
    }

    @Component
    class BasesProc(private val engine: InsuranceCalcEngine) : ProcHandler {
        override fun procName() = "PKG_人才派遣公共.PRC_获取补退基数"
        override fun handle(params: Map<String, Any?>): Map<String, Any?> {
            val atc900 = (params["atc900"] ?: return mapOf("appCode" to -1, "errMsg" to "缺少 atc900")).toString().toLongOrNull()
                ?: return mapOf("appCode" to -1, "errMsg" to "atc900 非数值")
            val nf = params["nf"]?.toString() ?: return mapOf("appCode" to -1, "errMsg" to "缺少 nf")
            return try {
                val b = engine.basesForYear(atc900, nf)
                mapOf("appCode" to 1, "errMsg" to null as String?,
                    "aaa301" to b["aaa301"], "aaa302" to b["aaa302"], "aaa303" to b["aaa303"],
                    "aaa304" to b["aaa304"], "aaa305" to b["aaa305"], "aaa306" to b["aaa306"])
            } catch (e: IllegalStateException) {
                mapOf("appCode" to -1, "errMsg" to e.message)
            }
        }
    }
}
