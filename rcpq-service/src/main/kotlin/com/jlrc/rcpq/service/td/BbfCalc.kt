package com.jlrc.rcpq.service.td

import com.jlrc.rcpq.service.calc.InsuranceCalcEngine
import com.jlrc.rcpq.service.proc.ProcHandler
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter

/**
 * W4d-2: PRC_保险当月补缴（旧 PKG_保险补退 L84-1010, 926L 规格重写, 2026-11-17）。
 *
 * 语义: 按合同算「当月应补缴明细」→ TC01BF 全字段累计结果。
 *   - LB='1' 转款: 起点级联(社保 AAA203<AAA208 / 医保 AAA608=ym&省属地99 / 工伤 AAA908=ym /
 *     公积金 AAA508=ym&atc90w 条件), 逐月引擎重算直接累加, 不写库;
 *   - LB='2' 结算: 先 DELETE AC10 当月该员工; ym<202101 静默 return; 逐月:
 *     社保/公积金查 TC93 已缴(AAE214='1' 且金额>0)→只拷基数不累金额, 未缴→写 AC10
 *     (DUP→该月金额清零); 医保/工伤无条件写 AC10(DUP→该月清零);
 *   - 忠实旧计数器复制粘贴: 医保循环累加 N_NY_4、工伤累加 N_NY_2(YS=GREATEST 无影响);
 *   - 忠实硬编码 hack: atc900=17175 且补缴年<2023 → 失业缴归零(社保 LB='1');
 *   - 末段: YS=GREATEST(四月数), ATC9WC 非空→YS=0; BBA200/BAA200/ATC9EH 合计;
 *   - 勘误: 无 TC08(施工图初版该条有误)。
 */
class BbfErr(val m: String) : RuntimeException(m)

@Component
class BbfCases(
    private val jdbc: JdbcTemplate,
    private val engine: InsuranceCalcEngine,
) {
    private val ok = 1
    private val Z = BigDecimal.ZERO
    private val FMT = DateTimeFormatter.ofPattern("yyyyMM")

    private fun newBf(): MutableMap<String, Any?> {
        val m = mutableMapOf<String, Any?>()
        for (k in listOf("aaa301", "aaa302", "aaa308", "aaa304", "aaa305", "aaa306",
                 "atb90k", "atb90l", "atb90m", "atb90n", "atb90o", "aaa399",
                 "atb90p", "atb90q", "atb90r", "atb90s_dw", "atb90s_gr",
                 "bba001", "bba002", "bba003", "bba004", "bba005", "atb90t", "bba000",
                 "baa001", "baa002", "baa003", "daa001", "daa002", "baa000",
                 "caa001", "caa002", "bba200", "baa200", "atc9eh", "ys", "dnys")) m[k] = Z
        return m
    }

    private fun add(m: MutableMap<String, Any?>, k: String, v: BigDecimal?) {
        m[k] = (m[k] as BigDecimal).add(v ?: Z)
    }

    private fun copyBases(m: MutableMap<String, Any?>, c: InsuranceCalcEngine.Tc01) {
        c.aaa301?.let { m["aaa301"] = it }
        c.aaa304?.let { m["aaa304"] = it }
        c.atb90k?.let { m["atb90k"] = it }
        c.atb90m?.let { m["atb90m"] = it }
        c.atb90p?.let { m["atb90p"] = it }
        c.atb90r?.let { m["atb90r"] = it }
    }

    private fun copyBasesYb(m: MutableMap<String, Any?>, c: InsuranceCalcEngine.Tc01) {
        c.aaa302?.let { m["aaa302"] = it }
        c.aaa306?.let { m["aaa306"] = it }
        c.atb90l?.let { m["atb90l"] = it }
        c.atb90o?.let { m["atb90o"] = it }
        c.atb90q?.let { m["atb90q"] = it }
        c.aaa399?.let { m["aaa399"] = it }
    }

    private fun loadTc90(atc900: Long): Map<String, Any?> =
        jdbc.query("SELECT * FROM tc90 WHERE atc900=?", { rs, _ ->
            val md = rs.metaData
            (1..md.columnCount).associate { md.getColumnLabel(it).lowercase() to rs.getObject(it) }
        }, atc900).firstOrNull() ?: throw BbfErr("查询员工信息出错，无法计算补发信息")

    private fun calcMonth(atc900: Long, month: String): InsuranceCalcEngine.Tc01 {
        val row = loadTc90(atc900)
        val (code, result) = engine.calc(row, month, InsuranceCalcEngine.Tc01(jslb = "2"))
        if (code == -1) throw BbfErr("计算补发信息出错：$result")
        return result as InsuranceCalcEngine.Tc01
    }

    private fun bd(v: Any?): BigDecimal = (v as? Number)?.let { BigDecimal(it.toString()) } ?: Z

    private fun ksOf(col: Any?): LocalDate? =
        col?.toString()?.takeIf { it.length >= 6 }
            ?.let { runCatching { YearMonth.parse(it.substring(0, 6), FMT).atDay(1) }.getOrNull() }

    /** LB='2' AC10 写入; 返回 false = 写入失败(DUP 等价)→该月金额清零 */
    private fun insAc10(aac101: String, tc90: Map<String, Any?>, ym: Long, month: String,
                        c: InsuranceCalcEngine.Tc01): Boolean = try {
        jdbc.update(
            """INSERT INTO ac10 (id, aac001, atc900, atb930, atc932, aae001, aae002,
               atc9t4, aac101, aaa301, aaa304, atb90k, atb90m, atb90p, atb90r,
               bba001, bba003, baa001, baa003, aae036,
               aaa302, aaa306, atb90l, atb90o, atb90q, aaa399, bba002, bba005, atb90t,
               baa002, daa001, aaa308, daa002, aaa305, atb90n,
               aaa303, atb90s_dw, atb90s_gr, caa001, caa002)
               VALUES (nextval('seq_ac10_id'), ?, ?, ?, ?, ?, ?, '1', ?, ?, ?, ?, ?, ?, ?,
               ?, ?, ?, ?, now(), ?, ?, ?, ?, ?, ?, ?, ?, ?,
               ?, ?, ?, ?, ?, ?,
               ?, ?, ?, ?, ?)""",
            (tc90["aac001"] as? Number)?.toLong(), (tc90["atc900"] as Number).toLong(), tc90["atb930"],
            month, ym, ym, aac101,
            c.aaa301, c.aaa304, c.atb90k, c.atb90m, c.atb90p, c.atb90r,
            c.bba001, c.bba003, c.baa001, c.baa003,
            c.aaa302, c.aaa306, c.atb90l, c.atb90o, c.atb90q, c.aaa399,
            c.bba002, c.bba005, c.atb90t, c.baa002, c.daa001, c.aaa308, c.daa002,
            c.aaa305, c.atb90n,
            c.aaa303, c.atb90sDw, c.atb90sGr, c.caa001, c.caa002)
        true
    } catch (e: Exception) {
        false
    }

    /** /proc 入口(独立事务)。进程内调用(费用结算等)走 bfInternal —— 共享事务内 BbfErr
     *  穿越 @Transactional 边界会标 rollback-only, 外层 catch 后 commit 即
     *  UnexpectedRollbackException(2026-11-27 BC-09 实证), 故拆无注解内联体。 */
    @Transactional
    fun bf(atc900: Long, aae002: String, lb: String): Map<String, Any?> = bfInternal(atc900, aae002, lb)

    fun bfInternal(atc900: Long, aae002: String, lb: String): Map<String, Any?> {
        val ym = aae002.toLongOrNull() ?: throw BbfErr("业务年月格式不正确，无法计算补发信息")
        if (lb.isBlank()) throw BbfErr("业务类别格式不正确，无法计算补发信息")
        val tc90 = loadTc90(atc900)
        if (tc90["aae00p"]?.toString() !in listOf("1", "5", "7")) {
            return mapOf("code" to ok, "msg" to null as String?, "bf" to newBf(), "skip" to true)
        }
        if (lb == "2") {
            // ac10.aac001=numeric, 原串化绑定在 PG 报 numeric=varchar(2026-11-27 BC-09 首爆修复)
            jdbc.update("DELETE FROM ac10 WHERE aae002=? AND aac001=?",
                ym.toBigDecimal(), (tc90["aac001"] as? Number)?.toLong())
        }
        val bf = newBf()
        if (lb == "2" && ym < 202101L) {
            return mapOf("code" to ok, "msg" to null as String?, "bf" to bf, "skip" to true)
        }
        val jsny = YearMonth.parse(aae002, FMT).minusMonths(1).atDay(1)
        var ks1: LocalDate? = null; var ks2: LocalDate? = null
        var ks3: LocalDate? = null; var ks4: LocalDate? = null
        var ny1 = 0; var ny2 = 0; var ny3 = 0; var ny4 = 0
        var ny1dn = 0; var ny2dn = 0; var ny3dn = 0; var ny4dn = 0
        val ymN = ym
        // LB='1' 起点级联(忠实四分支)
        if (lb == "1" && tc90["aaa401"]?.toString() == "1" &&
            tc90["atc90p"]?.toString() in listOf("0", "1") &&
            bd(tc90["aaa203"]) < bd(tc90["aaa208"])) {
            ks1 = ksOf(tc90["aaa203"])
        }
        if (lb == "1" && tc90["aaa402"]?.toString() == "1" &&
            tc90["atc90pa"]?.toString() in listOf("0", "1") &&
            (tc90["aaa608"] as? Number)?.toLong() == ym &&
            bd(tc90["aaa603"]) < bd(tc90["aaa608"]) &&
            tc90["aaa110"]?.toString()?.let { it.length >= 4 && it.substring(2, 4) == "99" } == true) {
            ks2 = ksOf(tc90["aaa603"])
        }
        if (lb == "1" && tc90["aaa404"]?.toString() == "1" &&
            tc90["atc90pb"]?.toString() in listOf("0", "1") &&
            (tc90["aaa908"] as? Number)?.toLong() == ym &&
            bd(tc90["aaa903"]) < bd(tc90["aaa908"])) {
            ks4 = ksOf(tc90["aaa903"])
        }
        if (lb == "1" && tc90["aaa407"]?.toString() == "1" &&
            (tc90["aaa508"] as? Number)?.toLong() == ym &&
            bd(tc90["aaa503"]) < bd(tc90["aaa508"]) &&
            (tc90["atc90w"]?.toString() in listOf("0", "1") ||
                (tc90["atc90w"] == null &&
                    tc90["aaa120"]?.toString()?.let { it.length >= 4 && it.substring(2, 4) == "99" } == true))) {
            ks3 = ksOf(tc90["aaa503"])
        }
        // LB='2': TC93 max 已缴年月(忠实: N_CNT_1/N_CNT_2 同款 SQL)
        var nCnt1 = 190001L; var nCnt2 = 190001L; var nCnt3 = 190001L; var nCnt4 = 190001L
        val aac001 = (tc90["aac001"] as? Number)?.toLong()  // tc93.aac001=numeric
        if (lb == "2") {
            nCnt1 = jdbc.queryForObject(
                """SELECT coalesce(max(atc931),190001) FROM tc93
                   WHERE coalesce(bba001,0)+coalesce(bba003,0)+coalesce(baa001,0)+coalesce(baa003,0)>0
                     AND aae002<? AND aac001=?""", Long::class.java, ym, aac001) ?: 190001L
            nCnt2 = nCnt1
            nCnt3 = jdbc.queryForObject(
                """SELECT coalesce(max(atc931),190001) FROM tc93
                   WHERE coalesce(bba004,0)>0 AND aae002<? AND aac001=?""",
                Long::class.java, ym, aac001) ?: 190001L
            nCnt4 = jdbc.queryForObject(
                """SELECT coalesce(max(atc931),190001) FROM tc93
                   WHERE coalesce(caa001,0)+coalesce(caa002,0)>0 AND aae002<? AND aac001=?""",
                Long::class.java, ym, aac001) ?: 190001L
            val srcYm = (tc90["aae002"] as? Number)?.toLong()
            if (srcYm != null && srcYm <= ym) {
                if (tc90["aaa401"]?.toString() == "1" && nCnt1 <= ym && srcYm == ym &&
                    bd(tc90["aaa201"]) < bd(tc90["aae002"]) &&
                    (tc90["atc9wc"] == null || (tc90["atc9wc"]?.toString() ?: "").contains("21"))) {
                    if (tc90["atc90p"] == null || tc90["atc90p"] == "2") ks1 = ksOf(tc90["aaa201"])
                    else if (tc90["atc90p"] == "0" && tc90["atc9wc"] == null) ks1 = ksOf(tc90["aaa203"])
                }
                if (tc90["aaa401"]?.toString() == "1" && srcYm >= 202012 && srcYm < ym &&
                    tc90["atc90p"] == "2" && bd(tc90["aaa208"]) <= bd(ym)) {
                    val cnt = jdbc.queryForObject(
                        "SELECT count(1) FROM tc93 WHERE atc937='1' AND atc931=? AND atc900=?",
                        Int::class.java, srcYm, atc900)
                    if (cnt == 0) ks1 = ksOf(tc90["aaa201"])
                }
                if (tc90["aaa402"]?.toString() == "1" && nCnt2 <= ym && srcYm == ym &&
                    bd(tc90["aaa601"]) < bd(tc90["aae002"]) &&
                    tc90["aaa110"]?.toString()?.let { it.length >= 4 && it.substring(2, 4) == "99" } == true &&
                    (tc90["atc9wc"] == null || (tc90["atc9wc"]?.toString() ?: "").contains("22"))) {
                    if (tc90["atc90pa"] == null || tc90["atc90pa"] == "2") ks2 = ksOf(tc90["aaa601"])
                    else if (tc90["atc90pa"] == "0" && tc90["atc9wc"] == null) ks2 = ksOf(tc90["aaa603"])
                }
            }
        }
        // ---- 社保 WHILE(养老/失业) ----
        var k = ks1
        while (k != null && !k.isAfter(jsny)) {
            val cur = k
            val month = cur.format(FMT)
            if (lb == "1") runCatching { engine.basesForYear(atc900, cur.year.toString()) }
            val c = calcMonth(atc900, month)
            copyBases(bf, c)
            if (lb == "2") {
                val paid = jdbc.queryForObject(
                    """SELECT count(1) FROM tc93 WHERE coalesce(baa001,0)+coalesce(baa003,0)
                       +coalesce(bba001,0)+coalesce(bba003,0)>0 AND aae214='1'
                       AND atc931=? AND atc900=?""", Int::class.java, month, atc900) > 0
                if (paid) {
                    // 忠实: 已缴月只拷基数比例, 金额不累加
                } else {
                    val ins = insAc10("1", tc90, ym, month, c)
                    add(bf, "bba001", if (ins) c.bba001 else Z)
                    add(bf, "bba003", if (ins) c.bba003 else Z)
                    add(bf, "baa001", if (ins) c.baa001 else Z)
                    add(bf, "baa003", if (ins) c.baa003 else Z)
                    ny1 += 1; ny1dn += 1
                }
            } else {
                // 忠实: hack atc900=17175 且补缴年<2023 → 失业单位/个人缴归零
                val hack = atc900 == 17175L && cur.year < 2023
                add(bf, "bba001", c.bba001)
                add(bf, "bba003", if (hack) Z else c.bba003)
                add(bf, "baa001", c.baa001)
                add(bf, "baa003", if (hack) Z else c.baa003)
                ny1 += 1; ny1dn += 1
            }
            k = cur.plusMonths(1)
        }
        // ---- 医保 WHILE(忠实计数器 ny4) ----
        k = ks2
        while (k != null && !k.isAfter(jsny)) {
            val cur = k
            val month = cur.format(FMT)
            val c = calcMonth(atc900, month)
            copyBasesYb(bf, c)
            if (lb == "2") {
                val ins = insAc10("2", tc90, ym, month, c)
                add(bf, "bba002", if (ins) c.bba002 else Z)
                add(bf, "bba005", if (ins) c.bba005 else Z)
                add(bf, "atb90t", if (ins) c.atb90t else Z)
                add(bf, "baa002", if (ins) c.baa002 else Z)
                add(bf, "daa001", if (ins) c.daa001 else Z)
                add(bf, "daa002", if (ins) c.daa002 else Z)
                ny4 += 1; ny4dn += 1
            } else {
                add(bf, "bba002", c.bba002); add(bf, "bba005", c.bba005)
                add(bf, "atb90t", c.atb90t); add(bf, "baa002", c.baa002)
                ny4 += 1; ny4dn += 1
            }
            k = cur.plusMonths(1)
        }
        // ---- 工伤 WHILE(忠实计数器 ny2) ----
        k = ks4
        while (k != null && !k.isAfter(jsny)) {
            val cur = k
            val month = cur.format(FMT)
            val c = calcMonth(atc900, month)
            c.aaa305?.let { bf["aaa305"] = it }
            c.atb90n?.let { bf["atb90n"] = it }
            if (lb == "2") {
                val ins = insAc10("4", tc90, ym, month, c)
                add(bf, "bba004", if (ins) c.bba004 else Z)
                ny2 += 1; ny2dn += 1
            } else {
                add(bf, "bba004", c.bba004)
                ny2 += 1; ny2dn += 1
            }
            k = cur.plusMonths(1)
        }
        // ---- 公积金 WHILE ----
        k = ks3
        while (k != null && !k.isAfter(jsny)) {
            val cur = k
            val month = cur.format(FMT)
            val c = calcMonth(atc900, month)
            c.aaa303?.let { bf["aaa303"] = it }
            c.atb90sDw?.let { bf["atb90s_dw"] = it }
            c.atb90sGr?.let { bf["atb90s_gr"] = it }
            if (lb == "2") {
                val paid = jdbc.queryForObject(
                    """SELECT count(1) FROM tc93 WHERE coalesce(caa001,0)+coalesce(caa002,0)>0
                       AND aae214='1' AND atc931=? AND atc900=?""", Int::class.java, month, atc900) > 0
                if (!paid) {
                    val ins = insAc10("3", tc90, ym, month, c)
                    add(bf, "caa001", if (ins) c.caa001 else Z)
                    add(bf, "caa002", if (ins) c.caa002 else Z)
                    ny3 += 1; ny3dn += 1
                }
            } else {
                add(bf, "caa001", c.caa001); add(bf, "caa002", c.caa002)
                ny3 += 1; ny3dn += 1
            }
            k = cur.plusMonths(1)
        }
        // ---- 末段 ----
        bf["ys"] = BigDecimal(maxOf(ny1, ny2, ny3, ny4))
        bf["dnys"] = BigDecimal(maxOf(ny1dn, ny2dn, ny3dn, ny4dn))
        if (tc90["atc9wc"] != null) bf["ys"] = Z
        val bba200 = (bf["bba001"] as BigDecimal).add(bf["bba003"] as BigDecimal)
            .add(bf["bba002"] as BigDecimal).add(bf["bba004"] as BigDecimal)
            .add(bf["bba005"] as BigDecimal).add(bf["atb90t"] as BigDecimal)
            .add(bf["caa001"] as BigDecimal)
        val baa200 = (bf["baa001"] as BigDecimal).add(bf["baa003"] as BigDecimal)
            .add(bf["baa002"] as BigDecimal).add(bf["caa002"] as BigDecimal)
        bf["bba200"] = bba200
        bf["baa200"] = baa200
        bf["atc9eh"] = bba200.add(baa200)
        return mapOf("code" to ok, "msg" to null as String?, "bf" to bf, "skip" to false)
    }
}

@Component
class BbfProc(private val c: BbfCases) : ProcHandler {
    override fun procName() = "PKG_保险补退.PRC_保险当月补缴"
    override fun handle(params: Map<String, Any?>): Map<String, Any?> {
        for (k in listOf("atc900", "aae002", "lb"))
            if (params[k] == null) return mapOf("appCode" to -1, "errMsg" to "缺少 $k")
        val atc900 = params["atc900"]!!.toString().toLongOrNull()
            ?: return mapOf("appCode" to -1, "errMsg" to "atc900 非数值")
        val r = try {
            c.bf(atc900, params["aae002"]!!.toString(), params["lb"]!!.toString())
        } catch (e: BbfErr) {
            return mapOf("appCode" to -1, "errMsg" to e.m)
        }
        @Suppress("UNCHECKED_CAST")
        val bf = r["bf"] as Map<String, Any?>
        return mutableMapOf("appCode" to r["code"], "errMsg" to r["msg"], "skip" to r["skip"]).apply {
            putAll(bf)
        }
    }
}
