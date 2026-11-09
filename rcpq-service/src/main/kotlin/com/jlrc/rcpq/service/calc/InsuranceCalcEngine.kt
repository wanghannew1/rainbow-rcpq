package com.jlrc.rcpq.service.calc

import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Component
import java.math.BigDecimal
import java.math.RoundingMode

/**
 * 险金计算引擎（旧 PKG_人才派遣公共.PRC_计算险金 825L 规格重写，2026-11-10 夜班波次）。
 *
 * 依赖等价:
 * - PRC_缴费基数上下限 → PG 函数 public.fun_缴费基数(V8 同源移植, clamp 语义一致)
 * - PRC_获取缴费比例_按员工 → ratesByEmployee(AA94 费率 + '0.99'/'0.990' 掩码舍入怪癖
 *   + 公积金 atb90s "单位:个人" 拆分 + AA97 跨年公积金比例 + AA03 工伤算法比例)
 * - PRC_获取补退基数 → basesForYear(TC90 基数 + AA97 跨年历史 + TC08 补退覆盖;
 *   原 TC08 医保分支 EXCEPTION 误写 aaa301 无副作用, 忠实不复制)
 * - PRC_获取征缴金额 → levyConfirmed(TJ0A1-F1 确认表; TJ0 系比例列为 varchar 带 '%' 需剥离;
 *   省工伤分支 R_TJ0B1.BBA004 双计 + 读 TJ0B1.ATB90N 的复制粘贴 bug 忠实保留)
 *
 * 忠实怪癖清单(与备份库源码逐行对齐):
 * - INSTR(NVL(aac008,0),..): NULL 户口性质按 '0' 匹配
 * - 费率 <1 按 '0.99'(社保失业 2 位)/'0.990'(医保系 3 位) HALF_UP 舍入
 * - 硬编码减免: atc900=18934 医疗三项归零; 疫情工伤窗口(结算 202007-202011/征缴 202007-202010);
 *   202212 工伤全免; 农民工('27') ≤202112 个人失业免; 灵活就业('51')/失业代缴('52') 征缴免医疗
 * - 大额: AA94 '11'(省 aaa945/市 <202110 用 aaa945 精确月, ≥202110 用 aaa944 区间),
 *   征缴回盘核对(TC9W_BP01.abo003/BP04.abp007), 承担方按 aaa803>结算单元 aaa803,
 *   tc90.aae002>年月 且非 11 月 → 大额归零
 * - 退休('80'): 五险公积金全归零, 市医保大额归零; 达龄('81')工伤归零
 * - 生育并入单位医疗在征缴覆盖前; 征缴覆盖后重判大额承担方
 * - 结算单元缺失 → 顶层异常 → code=-1(原 SELECT INTO 语义)
 */
@Component
class InsuranceCalcEngine(
    private val jdbcTemplate: JdbcTemplate,
) {
    val ok = 1
    val err = -1

    /** TC01 行记录(引擎内传递, 字段名与 TC01 列对应) */
    data class Tc01(
        var atc900: Long? = null, var atb930: Long? = null,
        var aac001: Long? = null, var aac003: String? = null, var aac002: String? = null,
        var aaa301: BigDecimal = ZERO, var aaa302: BigDecimal = ZERO, var aaa308: BigDecimal = ZERO,
        var aaa303: BigDecimal = ZERO, var aaa304: BigDecimal = ZERO,
        var aaa305: BigDecimal = ZERO, var aaa306: BigDecimal = ZERO,
        var atb90k: BigDecimal = ZERO, var atb90l: BigDecimal = ZERO, var atb90m: BigDecimal = ZERO,
        var atb90n: BigDecimal = ZERO, var atb90nJ: BigDecimal? = ZERO, var atb90o: BigDecimal = ZERO,
        var aaa399: BigDecimal = ZERO, var atb90p: BigDecimal = ZERO,
        var atb90q: BigDecimal = ZERO, var atb90r: BigDecimal = ZERO,
        var atb90sDw: BigDecimal = ZERO, var atb90sGr: BigDecimal = ZERO,
        var bba001: BigDecimal = ZERO, var bba002: BigDecimal = ZERO, var bba003: BigDecimal = ZERO,
        var bba004: BigDecimal = ZERO, var bba005: BigDecimal = ZERO, var atb90t: BigDecimal = ZERO,
        var bba000: BigDecimal = ZERO,
        var baa001: BigDecimal = ZERO, var baa002: BigDecimal = ZERO, var baa003: BigDecimal = ZERO,
        var daa001: BigDecimal = ZERO, var daa002: BigDecimal = ZERO, var baa000: BigDecimal = ZERO,
        var caa001: BigDecimal = ZERO, var caa002: BigDecimal = ZERO,
        var bba200: BigDecimal = ZERO, var baa200: BigDecimal = ZERO, var atc9eh: BigDecimal = ZERO,
        var jslb: String? = null,
        /** P6: 征缴工伤覆盖表达式为 NULL(TH0F1.BBA004 空)时禁止覆盖 */
        var bba004IsNull: Boolean = false,
    ) {
        companion object { val ZERO = BigDecimal.ZERO }
    }

    /** TC90 行宽松取值器(numeric→BigDecimal, 其余→String) */
    class Tc90Row(private val m: Map<String, Any?>) {
        fun n(f: String): BigDecimal? = when (val v = m[f]) {
            null -> null; is BigDecimal -> v; is Number -> BigDecimal(v.toString()); else -> v.toString().toBigDecimalOrNull()
        }
        fun s(f: String): String? = when (val v = m[f]) { null -> null; else -> v.toString() }
        fun l(f: String): Long? = (m[f] as? Number)?.toLong()
        fun hasNonNull(f: String) = m[f] != null
    }

    /** 缴费基数上下限(复用 V8 的 public.fun_缴费基数) */
    fun clampBase(bx: String, aae002: String, aaa100: String?, js: BigDecimal?): BigDecimal =
        jdbcTemplate.queryForObject(
            "SELECT public.fun_缴费基数(?, ?, ?, ?)",
            BigDecimal::class.java, bx, aae002, aaa100, js?.toString() ?: "0")

    /** AA94 费率: 值<1 按掩码位数 HALF_UP 舍入(怪癖), 无则缺省 */
    private fun rates94(aaa100: String?, codes: List<String>, ym: Long, scale: Int): Map<String, BigDecimal> {
        val out = mutableMapOf<String, BigDecimal>()
        if (aaa100 == null) return out
        val args = arrayOf<Any?>(*codes.toTypedArray(), ym, ym, aaa100)
        jdbcTemplate.query(
            "SELECT aaa941, aaa942 FROM aa94 WHERE aaa941 IN (${codes.joinToString(",") { "?" }}) " +
                "AND aaa946 <= ? AND aaa947 >= ? AND aaa100 = ?",
            { rs, _ ->
                val raw = rs.getBigDecimal("aaa942")
                out[rs.getString("aaa941")] = if (raw < BigDecimal.ONE) raw.setScale(scale, RoundingMode.HALF_UP) else raw
            }, *args)
        return out
    }

    /** PRC_获取缴费比例_按员工 等价(P5: 组原子性——组内任一 key 缺行全组归零; P8: atb90nj 仅 AA03 赋值) */
    fun ratesByEmployee(tc90: Tc90Row, ym: Long): Map<String, Any?> {
        val r = mutableMapOf<String, Any?>(
            "atb90k" to BigDecimal.ZERO, "atb90p" to BigDecimal.ZERO,
            "atb90m" to BigDecimal.ZERO, "atb90r" to BigDecimal.ZERO,
            "atb90l" to BigDecimal.ZERO, "atb90n" to BigDecimal.ZERO,
            "atb90nj" to null, // 原无 ELSE 赋值: 仅 AA03 分支赋值, 省医保+市工伤组合下为 NULL(减法→bba004=0)
            "atb90o" to BigDecimal.ZERO,
            "atb90q" to BigDecimal.ZERO, "aaa399" to BigDecimal.ZERO,
            "atb90sDw" to BigDecimal.ZERO, "atb90sGr" to BigDecimal.ZERO,
        )
        val sub = { v: String? -> if (v != null && v.length >= 4) v.substring(2, 4) else null } // Oracle SUBSTR(x,3,2)
        val aaa100 = tc90.s("aaa100")
        if (aaa100 != null && tc90.s("aaa401") == "1") {
            rates94(aaa100, listOf("01", "06"), ym, 2).let { t ->
                // P5 组原子性: BEGIN 块内任一 TAB key 缺 → 整组 0
                val k = t["01"]; val p = t["06"]
                if (k != null && p != null) { r["atb90k"] = k; r["atb90p"] = p } else { r["atb90k"] = BigDecimal.ZERO; r["atb90p"] = BigDecimal.ZERO }
            }
            rates94(aaa100, listOf("03", "08"), ym, 2).let { t ->
                val m = t["03"]; val rr = t["08"]
                if (m != null && rr != null) { r["atb90m"] = m; r["atb90r"] = rr } else { r["atb90m"] = BigDecimal.ZERO; r["atb90r"] = BigDecimal.ZERO }
            }
        }
        val aaa110 = tc90.s("aaa110")
        if (aaa110 != null && tc90.s("aaa402") == "1") {
            val t = rates94(aaa110, listOf("02", "04", "05", "07", "12"), ym, 3)
            if (sub(aaa110) == "99") {
                // 省医保: 02/04/05/07/12 任一缺 → 全组 0
                val l = t["02"]; val n = t["04"]; val o = t["05"]; val q = t["07"]; val g = t["12"]
                if (l != null && n != null && o != null && q != null && g != null) {
                    r["atb90l"] = l; r["atb90n"] = n; r["atb90o"] = o; r["atb90q"] = q; r["aaa399"] = g
                } else {
                    r["atb90l"] = BigDecimal.ZERO; r["atb90n"] = BigDecimal.ZERO
                    r["atb90o"] = BigDecimal.ZERO; r["atb90q"] = BigDecimal.ZERO; r["aaa399"] = BigDecimal.ZERO
                }
            } else {
                // 市医保: 02/04/05/07 任一缺 → l/n/o/q 全 0(aaa399 恒 0)
                val l = t["02"]; val n = t["04"]; val o = t["05"]; val q = t["07"]
                if (l != null && n != null && o != null && q != null) {
                    r["atb90l"] = l; r["atb90n"] = n; r["atb90o"] = o; r["atb90q"] = q
                } else {
                    r["atb90l"] = BigDecimal.ZERO; r["atb90n"] = BigDecimal.ZERO
                    r["atb90o"] = BigDecimal.ZERO; r["atb90q"] = BigDecimal.ZERO
                }
                r["aaa399"] = BigDecimal.ZERO
            }
        }
        val aaa130 = tc90.s("aaa130")
        if (aaa130 != null && tc90.s("aaa404") == "1") {
            r["atb90n"] = rates94(aaa130, listOf("04"), ym, 3)["04"] ?: BigDecimal.ZERO
        } else {
            r["atb90n"] = BigDecimal.ZERO // P3: 原块 ELSE 强制归零(覆盖医保组取到的 04)
        }
        // 公积金比例(atb90s "单位:个人"; 跨年取 AA97 aae214_3 in(1,3) 次年; P9: 缺冒号→静默 NULL→0 不抛)
        val atb90s0 = tc90.s("atb90s")
        if (atb90s0 != null && tc90.s("aaa407") == "1") {
            var v: String = atb90s0
            val ymStr = ym.toString()
            val curYear = java.time.LocalDate.now().year.toString()
            if (ymStr.substring(0, 4) != curYear) {
                val nextYear = (ymStr.substring(0, 4).toInt() + 1).toString()
                jdbcTemplate.query("SELECT atb90s FROM aa97 WHERE aae214_3 IN ('1','3') AND aae001=? AND aac001=?",
                    { rs, _ -> rs.getString("atb90s") }, nextYear, tc90.l("aac001")).firstOrNull()?.let { v = it }
            }
            val idx: Int = v.indexOf(':')
            if (idx > 0) {
                val dw: String = v.substring(0, idx)
                val gr: String = v.substring(idx + 1)
                r["atb90sDw"] = dw.trim().toBigDecimalOrNull() ?: BigDecimal.ZERO
                r["atb90sGr"] = gr.trim().toBigDecimalOrNull() ?: BigDecimal.ZERO
            } else {
                // 原 SUBSTR(v,1,-1)=NULL → dw NULL → caa NULL → NVL 0; gr 解析亦失败 → 0
                r["atb90sDw"] = BigDecimal.ZERO; r["atb90sGr"] = BigDecimal.ZERO
            }
        }
        // 工伤算法比例(AA03/AB01; P7.2: aaa110 NULL 时 SUBSTR 比较 UNKNOWN → 跳过; 市(<>'99')且 <202203)
        if (aaa110 != null && sub(aaa110) != "99" && ym.toString() < "202203") {
            val t = jdbcTemplate.query(
                "SELECT a.aaa034, a.aaa035 FROM aa03 a, ab01 b WHERE a.aaa030::text = b.aaa030::text AND b.aab001=?",
                { rs, _ -> arrayOf<BigDecimal?>(rs.getBigDecimal("aaa034"), rs.getBigDecimal("aaa035")) },
                tc90.l("aab001")).firstOrNull()
            if (t != null) { r["atb90n"] = t[0] ?: BigDecimal.ZERO; r["atb90nj"] = t[1] } // nj 可为 null(原无兜底)
            else { r["atb90n"] = BigDecimal.ZERO; r["atb90nj"] = null }
        }
        return r
    }

    /** PRC_获取补退基数 等价 */
    fun basesForYear(atc900: Long, nf: String): Map<String, BigDecimal> {
        val tc90 = jdbcTemplate.query(
            "SELECT atc905, aaa302, aaa305, aaa306, aaa303, aac001 FROM tc90 WHERE atc900=?",
            { rs, _ -> Tc90Row(genericMap(rs, listOf("atc905", "aaa302", "aaa305", "aaa306", "aaa303", "aac001"))) }, atc900)
            .firstOrNull() ?: throw IllegalStateException("获取缴费基数出错")
        val curYear = java.time.LocalDate.now().year.toString()
        var aaa301 = tc90.n("atc905") ?: BigDecimal.ZERO
        var aaa304 = aaa301
        var aaa302 = tc90.n("aaa302") ?: BigDecimal.ZERO
        var aaa305 = tc90.n("aaa305") ?: BigDecimal.ZERO
        var aaa306 = tc90.n("aaa306") ?: BigDecimal.ZERO
        var aaa303 = tc90.n("aaa303") ?: BigDecimal.ZERO
        if (nf < curYear) {
            val hist = jdbcTemplate.query(
                "SELECT aaa301, aaa302, aaa303, aaa304, aaa305, aaa306 FROM aa97 WHERE aae001=? AND aac001=?",
                { rs, _ -> genericMap(rs, listOf("aaa301", "aaa302", "aaa303", "aaa304", "aaa305", "aaa306")) },
                curYear.toLong(), tc90.l("aac001")).firstOrNull()
            if (hist != null) {
                val h = Tc90Row(hist)
                aaa301 = h.n("aaa301") ?: aaa301; aaa302 = h.n("aaa302") ?: aaa302
                aaa303 = h.n("aaa303") ?: aaa303; aaa304 = h.n("aaa304") ?: aaa304
                aaa305 = h.n("aaa305") ?: aaa305; aaa306 = h.n("aaa306") ?: aaa306
            }
        }
        val tc08 = jdbcTemplate.query(
            "SELECT aaa000, aaa300 FROM tc08 WHERE aae000=? AND atc900=?",
            { rs, _ -> arrayOf<Any?>(rs.getString("aaa000"), rs.getBigDecimal("aaa300")) }, nf.toLong(), atc900)
        for (row in tc08) {
            val kind = row[0] as String?
            val amt = row[1] as BigDecimal? ?: BigDecimal.ZERO
            if (kind == "1") { aaa301 = amt; aaa304 = amt }
            if (kind == "2") {
                var hit302_1 = false
                jdbcTemplate.query("SELECT aaa302_1 FROM aa97 WHERE aae001=? AND atc900=?",
                    { rs, _ -> rs.getBigDecimal("aaa302_1") }, nf.toLong(), atc900).firstOrNull()?.let {
                    aaa302 = it; hit302_1 = true
                }
                // P4 忠实: 原 EXCEPTION 分支 R_AAA301 := R.AAA300(有副作用, 影响养老/失业基数)
                if (!hit302_1) aaa301 = amt
                aaa304 = aaa301; aaa302 = amt; aaa305 = amt; aaa306 = amt
            }
            if (kind == "3") { aaa303 = amt }
        }
        return mapOf("aaa301" to aaa301, "aaa302" to aaa302, "aaa303" to aaa303,
            "aaa304" to aaa304, "aaa305" to aaa305, "aaa306" to aaa306)
    }

    private fun genericMap(rs: java.sql.ResultSet, cols: List<String>): Map<String, Any?> =
        cols.associateWith { rs.getObject(it) }

    /** TJ0 比例列(varchar '16%') → BigDecimal; NULL → 0 */
    private fun pct(v: Any?): BigDecimal {
        val s = v?.toString()?.replace("%", "")?.trim() ?: return BigDecimal.ZERO
        return s.toBigDecimalOrNull() ?: BigDecimal.ZERO
    }

    /** PRC_获取征缴金额 等价; 省工伤分支保留旧复制粘贴 bug(TJ0B1.BBA004 双计/读 TJ0B1.ATB90N) */
    fun levyConfirmed(tc90: Tc90Row, ym: Long): Tc01 {
        val r = Tc01()
        data class Row(val atc900: Long?, val vals: Map<String, Any?>)
        fun tj(table: String, extra: List<String>): Row? = jdbcTemplate.query(
            "SELECT atc900${if (extra.isEmpty()) "" else extra.joinToString("") { ", $it" }} FROM $table " +
                "WHERE atj000='1' AND aae013='1' AND aae002=? AND atc900=?",
            { rs, _ -> Row((rs.getObject("atc900") as Number?)?.toLong(), extra.associateWith { rs.getObject(it) }) },
            ym, tc90.l("atc900")).firstOrNull()
        tj("tj0a1", listOf("aaa301", "aaa304", "atb90k", "atb90m", "atb90p", "atb90r", "bba001", "atj0a5", "bba003", "atj0a6", "baa001", "baa003"))?.let { a ->
            val v = a.vals
            v["aaa301"]?.let { r.aaa301 = it as BigDecimal }; v["aaa304"]?.let { r.aaa304 = it as BigDecimal }
            r.atb90k = pct(v["atb90k"]); r.atb90m = pct(v["atb90m"]); r.atb90p = pct(v["atb90p"]); r.atb90r = pct(v["atb90r"])
            r.bba001 = (v["bba001"] as? BigDecimal ?: BigDecimal.ZERO).add(v["atj0a5"] as? BigDecimal ?: BigDecimal.ZERO)
            r.bba003 = (v["bba003"] as? BigDecimal ?: BigDecimal.ZERO).add(v["atj0a6"] as? BigDecimal ?: BigDecimal.ZERO)
            (v["baa001"] as? BigDecimal)?.let { r.baa001 = it }; (v["baa003"] as? BigDecimal)?.let { r.baa003 = it }
        }
        var tj0bBba004 = BigDecimal.ZERO
        tj("tj0b1", listOf("aaa302", "aaa306", "atb90l", "atb90n", "atb90o", "aaa399", "atb90q", "bba002", "bba004", "bba005", "atb90t", "baa002", "daa001"))?.let { b ->
            val v = b.vals
            v["aaa302"]?.let { r.aaa302 = it as BigDecimal }; v["aaa306"]?.let { r.aaa306 = it as BigDecimal }
            r.atb90l = pct(v["atb90l"]); r.atb90n = pct(v["atb90n"]); r.atb90o = pct(v["atb90o"])
            r.aaa399 = pct(v["aaa399"]); r.atb90q = pct(v["atb90q"])
            (v["bba002"] as? BigDecimal)?.let { r.bba002 = it }
            (v["bba004"] as? BigDecimal)?.let { r.bba004 = it; tj0bBba004 = it }
            (v["bba005"] as? BigDecimal)?.let { r.bba005 = it }
            (v["atb90t"] as? BigDecimal)?.let { r.atb90t = it }
            (v["baa002"] as? BigDecimal)?.let { r.baa002 = it }
            (v["daa001"] as? BigDecimal)?.let { r.daa001 = it }
        }
        tj("tj0c1", listOf("aaa302", "aaa305", "aaa306", "atb90l", "atb90n", "atb90o", "atb90q", "bba002", "bba004", "bba005", "baa002", "daa001"))?.let { c ->
            val v = c.vals
            v["aaa302"]?.let { r.aaa302 = it as BigDecimal }
            v["aaa305"]?.let { r.aaa305 = it as BigDecimal }
            v["aaa306"]?.let { r.aaa306 = it as BigDecimal }
            r.atb90l = pct(v["atb90l"]); r.atb90n = pct(v["atb90n"]); r.atb90o = pct(v["atb90o"]); r.atb90q = pct(v["atb90q"])
            (v["bba002"] as? BigDecimal)?.let { r.bba002 = it }; (v["bba004"] as? BigDecimal)?.let { r.bba004 = it }
            (v["bba005"] as? BigDecimal)?.let { r.bba005 = it }; (v["baa002"] as? BigDecimal)?.let { r.baa002 = it }
            (v["daa001"] as? BigDecimal)?.let { r.daa001 = it }
        }
        tj("tj0d1", listOf("aaa303", "atb90s_dw", "atb90s_gr", "caa001", "caa002"))?.let { d ->
            val v = d.vals
            v["aaa303"]?.let { r.aaa303 = it as BigDecimal }
            r.atb90sDw = pct(v["atb90s_dw"]); r.atb90sGr = pct(v["atb90s_gr"])
            (v["caa001"] as? BigDecimal)?.let { r.caa001 = it }; (v["caa002"] as? BigDecimal)?.let { r.caa002 = it }
        }
        val e = tj("tj0e1", listOf("aaa303", "atb90s_dw", "atb90s_gr", "caa001", "caa002"))
        val f = tj("tj0f1", listOf("aaa305", "bba004"))
        if (f != null && f.atc900 != null) {
            (f.vals["aaa305"] as? BigDecimal)?.let { r.aaa305 = it }
            // 旧 bug 忠实: 单位工伤比例读 TJ0B1.ATB90N
            jdbcTemplate.query("SELECT atb90n FROM tj0b1 WHERE atj000='1' AND aae013='1' AND aae002=? AND atc900=?",
                { rs, _ -> rs.getString("atb90n") }, ym, tc90.l("atc900")).firstOrNull()?.let { r.atb90n = pct(it) }
            // 旧 bug 忠实: NVL(TJ0B1.BBA004,0)+累计值+TJ0F1.BBA004(TJ0B1 双计); P6: F 值 NULL → 表达式 NULL → 覆盖块 NVL→0 不覆盖
            val fVal = f.vals["bba004"] as? BigDecimal
            if (fVal == null) r.bba004IsNull = true
            else r.bba004 = tj0bBba004.add(r.bba004).add(fVal)
        }
        if (e != null && e.atc900 != null) {
            val v = e.vals
            v["aaa303"]?.let { r.aaa303 = it as BigDecimal }
            r.atb90sDw = pct(v["atb90s_dw"]); r.atb90sGr = pct(v["atb90s_gr"])
            (v["caa001"] as? BigDecimal)?.let { r.caa001 = it }; (v["caa002"] as? BigDecimal)?.let { r.caa002 = it }
        }
        return r
    }

    /** 大额基值 */
    private fun bigAmount(tc90: Tc90Row, ym: Long, ymStr: String, tb93Aaa803: String?, xjblJslb: String?): Pair<BigDecimal, BigDecimal> {
        var nDaa = BigDecimal.ZERO
        if (tc90.s("aaa110")?.let { it.substring(2, 4) } == "99") {
            nDaa = jdbcTemplate.query(
                "SELECT aaa945 FROM aa94 WHERE aaa100=? AND aaa941='11' AND aaa946<=? AND aaa947>=?",
                { rs, _ -> rs.getBigDecimal("aaa945") }, tc90.s("aaa110"), ym, ym).firstOrNull() ?: BigDecimal.ZERO
            val nCnt = jdbcTemplate.queryForObject(
                "SELECT count(*) FROM tc9w_bp01 WHERE abo003::numeric > 0 AND aae002=? AND aaa001=?",
                Int::class.java, ym, tc90.s("aaa005"))
            if (nCnt == 0 && xjblJslb == "2" && tc90.n("aaa608")?.toLong() == ym) nDaa = BigDecimal.ZERO
        } else {
            if (ymStr < "202110") {
                nDaa = jdbcTemplate.query(
                    "SELECT aaa945 FROM aa94 WHERE aaa100=? AND aaa941='11' AND aaa946=? AND aaa947=?",
                    { rs, _ -> rs.getBigDecimal("aaa945") }, tc90.s("aaa110"), ym, ym).firstOrNull() ?: BigDecimal.ZERO
            } else {
                nDaa = jdbcTemplate.query(
                    "SELECT aaa944 FROM aa94 WHERE aaa100=? AND aaa941='11' AND aaa946<=? AND aaa947>=?",
                    { rs, _ -> rs.getBigDecimal("aaa944") }, tc90.s("aaa110"), ym, ym).firstOrNull() ?: BigDecimal.ZERO
            }
            val nCnt = jdbcTemplate.queryForObject(
                "SELECT count(*) FROM tc9w_bp04 WHERE abp007::numeric > 0 AND aae002=? AND aac000=?",
                Int::class.java, ym, tc90.s("aaa005"))
            if (nCnt == 0 && xjblJslb == "2") nDaa = BigDecimal.ZERO
        }
        if ((tc90.n("aae002")?.toLong() ?: Long.MIN_VALUE) > ym && ymStr.substring(4, 6) != "11") nDaa = BigDecimal.ZERO
        val unitBear = if (tc90.hasNonNull("aaa803")) tc90.s("aaa803") == "1" else tb93Aaa803 == "1"
        return if (unitBear) Pair(BigDecimal.ZERO, nDaa) else Pair(nDaa, BigDecimal.ZERO)
    }

    /** PRC_计算险金 主入口。xjbl null 时按员工取费率; jslb 传结算类别('1'结算/'2'征缴)。 */
    fun calc(tc90Raw: Map<String, Any?>, aae002: String, xjbl: Tc01?): Pair<Int, Any> {
        val ym = aae002.toLongOrNull() ?: return Pair(err, "年月格式不正确")
        val ymStr = ym.toString()
        val tc90 = Tc90Row(tc90Raw)
        val out = Tc01()
        try {
            if (tc90.s("aae00p") == "2") return Pair(ok, out) // 劳务不计算
            val tb93Aaa803 = jdbcTemplate.query(
                "SELECT aaa803 FROM tb93 WHERE atb930=?", { rs, _ -> rs.getString("aaa803") }, tc90.l("atb930"))
                .firstOrNull() ?: throw IllegalStateException("结算单元不存在: ${tc90.l("atb930")}")
            out.atc900 = tc90.l("atc900"); out.atb930 = tc90.l("atb930")
            out.aac001 = tc90.l("aac001"); out.aac003 = tc90.s("aac003"); out.aac002 = tc90.s("aac002")
            // P2: 原 OUT 形参 jslb 初始化即 NULL, 不随 xjbl 携带
            // 费率
            if (xjbl == null || xjbl.atc900 == null) {
                val rates = ratesByEmployee(tc90, ym)
                for (k in listOf("atb90k", "atb90l", "atb90m", "atb90n", "atb90nj", "atb90o", "atb90p", "atb90q", "atb90r", "atb90sDw", "atb90sGr", "aaa399")) {
                    when (k) {
                        "atb90k" -> out.atb90k = rates[k] as BigDecimal; "atb90l" -> out.atb90l = rates[k] as BigDecimal
                        "atb90m" -> out.atb90m = rates[k] as BigDecimal; "atb90n" -> out.atb90n = rates[k] as BigDecimal
                        "atb90nj" -> out.atb90nJ = rates[k] as BigDecimal?; "atb90o" -> out.atb90o = rates[k] as BigDecimal
                        "atb90p" -> out.atb90p = rates[k] as BigDecimal; "atb90q" -> out.atb90q = rates[k] as BigDecimal
                        "atb90r" -> out.atb90r = rates[k] as BigDecimal; "atb90sDw" -> out.atb90sDw = rates[k] as BigDecimal
                        "atb90sGr" -> out.atb90sGr = rates[k] as BigDecimal; "aaa399" -> out.aaa399 = rates[k] as BigDecimal
                    }
                }
            } else {
                out.atb90k = xjbl.atb90k; out.atb90l = xjbl.atb90l; out.atb90m = xjbl.atb90m
                out.atb90n = xjbl.atb90n; out.atb90nJ = xjbl.atb90nJ; out.atb90o = xjbl.atb90o
                out.atb90p = xjbl.atb90p; out.atb90q = xjbl.atb90q; out.atb90r = xjbl.atb90r
                out.atb90sDw = xjbl.atb90sDw; out.atb90sGr = xjbl.atb90sGr; out.aaa399 = xjbl.aaa399
            }
            // 基数(跨年补退)
            val b = basesForYear(tc90.l("atc900")!!, ymStr.substring(0, 4))
            out.aaa301 = b["aaa301"]!!; out.aaa302 = b["aaa302"]!!; out.aaa303 = b["aaa303"]!!
            out.aaa304 = b["aaa304"]!!; out.aaa305 = b["aaa305"]!!; out.aaa306 = b["aaa306"]!!
            // AA97 未来生效医保基数
            val aa97 = jdbcTemplate.query(
                "SELECT aaa302, aae002_2 FROM aa97 WHERE aae001=? AND aac001=?",
                { rs, _ -> arrayOf(rs.getBigDecimal("aaa302"), rs.getObject("aae002_2")) },
                ymStr.substring(0, 4).toInt(), tc90.l("aac001")).firstOrNull()
            if (aa97 != null) {
                val eff = (aa97[1] as? Number)?.toLong()
                if (eff != null && eff > ym) out.aaa302 = aa97[0] as BigDecimal
            }
            // 上下限夹取
            out.aaa301 = clampBase("1", ymStr, tc90.s("aaa100"), out.aaa301)
            out.aaa304 = out.aaa301
            if (instrNvl(tc90.s("aac008"), "80")) {
                out.aaa302 = tc90.n("aaa302") ?: BigDecimal.ZERO // P7.4: 原 NULL 直赋, 下游 NVL→0
            } else {
                out.aaa302 = clampBase("2", ymStr, tc90.s("aaa110"), out.aaa302)
            }
            out.aaa306 = out.aaa302
            out.aaa305 = clampBase("4", ymStr, tc90.s("aaa130"), out.aaa305)
            jdbcTemplate.query("SELECT aaa303 FROM tc0a WHERE atc0a2<=? AND atc0a3>=? AND atc900=?",
                { rs, _ -> rs.getBigDecimal("aaa303") }, ym, ym, tc90.l("atc900"))
                .firstOrNull()?.let { if (it > BigDecimal.ZERO) out.aaa303 = it }
            out.aaa303 = clampBase("3", ymStr, tc90.s("aaa120"), out.aaa303)
            out.aaa308 = tc90.n("aaa308") ?: BigDecimal.ZERO
            // 五险金额(ROUND(x, 2) HALF_UP = Oracle ROUND)
            fun round2(a: BigDecimal, p: BigDecimal): BigDecimal =
                a.multiply(p).divide(BigDecimal(100), 2, RoundingMode.HALF_UP)
            out.bba001 = round2(out.aaa301, out.atb90k)
            out.baa001 = round2(out.aaa301, out.atb90p)
            out.bba003 = round2(out.aaa304, out.atb90m)
            out.baa003 = round2(out.aaa304, out.atb90r)
            out.bba002 = round2(out.aaa302, out.atb90l)
            val a130sub = tc90.s("aaa130")?.let { it.substring(2, 4) }
            out.bba004 = if (a130sub != null && a130sub != "99" && ymStr < "202107") {
                // P8: atb90nJ NULL(省医保+无AA03) → 原表达式 NULL → 下游 NVL 记 0
                val njTerm = out.atb90nJ?.let { round2(out.aaa305, it) }
                njTerm?.subtract(round2(out.aaa305, out.atb90n)) ?: BigDecimal.ZERO
            } else round2(out.aaa305, out.atb90n)
            out.bba005 = round2(out.aaa306, out.atb90o)
            out.atb90t = if (tc90.s("aaa110")?.let { it.substring(2, 4) } == "99") round2(out.aaa302, out.aaa399) else BigDecimal.ZERO
            out.baa002 = round2(out.aaa302.add(tc90.n("aaa308") ?: BigDecimal.ZERO), out.atb90q)
            // 公积金(省 ROUND 0 位/市 ROUND(x) 亦 0 位——旧两分支同为整数化, HALF_UP)
            val gjR = { a: BigDecimal, p: BigDecimal -> a.multiply(p).divide(BigDecimal(100), 0, RoundingMode.HALF_UP) }
            out.caa001 = gjR(out.aaa303, out.atb90sDw)
            out.caa002 = gjR(out.aaa303, out.atb90sGr)
            // 大额
            if (tc90.s("aaa402") == "1" && (
                (((tc90.s("atc90pa") == "0" || tc90.s("atc90pa") == "2") && tc90.n("aae002")?.toLong() == ym && tc90.n("aaa608")?.toLong() == ym)) ||
                    (tc90.s("atc90pa") == null && tc90.n("aae002")?.toLong() == ym) ||
                    ymStr.substring(4, 6) == "12")
            ) {
                val (d1, d2) = bigAmount(tc90, ym, ymStr, tb93Aaa803, xjbl?.jslb)
                out.daa001 = d1; out.daa002 = d2
            }
            // 分险种减免
            val aaa401 = tc90.s("aaa401")
            val atc90p = tc90.s("atc90p")
            val atb90r = tc90.s("atb90r")
            val inSetAtb90r = atb90r == "5" || atb90r == "4" || atb90r == "2" || atb90r == "1"
            // P1: Oracle 三值逻辑——列 NULL 时比较 UNKNOWN, IF 不执行(NULL ≠ 已停缴)
            val noPension = aaa401 == "0" ||
                (aaa401 == "1" && (atc90p == "1" || atc90p == "3") && cmpLe(tc90.n("aaa209"), ym)) ||
                ((atc90p == "0" || atc90p == "2" || atc90p == null) && inSetAtb90r && cmpLe(tc90.n("aaa204"), ym)) ||
                cmpGt(tc90.n("aaa203"), ym)
            if (noPension) { out.bba001 = BigDecimal.ZERO; out.baa001 = BigDecimal.ZERO }
            if (noPension) { out.bba003 = BigDecimal.ZERO; out.baa003 = BigDecimal.ZERO }
            if (!instrNvl(tc90.s("aac008"), "80") && (
                tc90.s("aaa402") == "0" || cmpGt(tc90.n("aaa601"), ym) ||
                    (tc90.s("aaa402") == "1" && (tc90.s("atc90pa") == "1" || tc90.s("atc90pa") == "3") && cmpLe(tc90.n("aaa609"), ym)) ||
                    ((tc90.s("atc90pa") == "0" || tc90.s("atc90pa") == "2" || tc90.s("atc90pa") == null) && inSetAtb90r && cmpLe(tc90.n("aaa604"), ym)))) {
                out.bba002 = BigDecimal.ZERO; out.atb90t = BigDecimal.ZERO
                out.baa002 = BigDecimal.ZERO; out.daa001 = BigDecimal.ZERO; out.daa002 = BigDecimal.ZERO
            }
            if (!instrNvl(tc90.s("aac008"), "80") && (
                tc90.s("aaa404") == "0" ||
                    (tc90.s("aaa404") == "1" && (tc90.s("atc90pb") == "1" || tc90.s("atc90pb") == "3") && cmpLe(tc90.n("aaa909"), ym)) ||
                    ((tc90.s("atc90pb") == "0" || tc90.s("atc90pb") == "2" || tc90.s("atc90pb") == null) && inSetAtb90r && cmpLe(tc90.n("aaa904"), ym)) ||
                    cmpGt(tc90.n("aaa903"), ym))) {
                out.bba004 = BigDecimal.ZERO
            }
            if (!instrNvl(tc90.s("aac008"), "80") && (
                tc90.s("aaa405") == "0" ||
                    (tc90.s("aaa402") == "1" && (tc90.s("atc90pa") == "1" || tc90.s("atc90pa") == "3") && cmpLe(tc90.n("aaa609"), ym)) ||
                    ((tc90.s("atc90pa") == "0" || tc90.s("atc90pa") == "2" || tc90.s("atc90pa") == null) && inSetAtb90r && cmpLe(tc90.n("aaa604"), ym)) ||
                    cmpGt(tc90.n("aaa601"), ym))) {
                out.bba005 = BigDecimal.ZERO
            }
            if (tc90.s("aaa407") == "0" ||
                (tc90.s("aaa407") == "1" && (tc90.s("atc90w") == "1" || tc90.s("atc90w") == "3") && cmpLe(tc90.n("aaa509"), ym)) ||
                (tc90.s("aaa407") == "1" && instrNvl(tc90.s("atc9wc"), "16")) ||
                ((tc90.s("atc90w") == "0" || tc90.s("atc90w") == "2" || tc90.s("atc90w") == null) && inSetAtb90r && cmpLe(tc90.n("aaa504"), ym)) ||
                cmpGt(tc90.n("aaa501"), ym)) {
                out.caa001 = BigDecimal.ZERO; out.caa002 = BigDecimal.ZERO
            }
            if (instrNvl(tc90.s("aac008"), "81")) out.bba004 = BigDecimal.ZERO
            if (instrNvl(tc90.s("aac008"), "80")) {
                out.bba001 = BigDecimal.ZERO; out.baa001 = BigDecimal.ZERO
                out.bba003 = BigDecimal.ZERO; out.baa003 = BigDecimal.ZERO
                out.bba002 = BigDecimal.ZERO; out.baa002 = BigDecimal.ZERO
                out.bba004 = BigDecimal.ZERO; out.bba005 = BigDecimal.ZERO
                out.caa001 = BigDecimal.ZERO; out.caa002 = BigDecimal.ZERO
                val a110sub = tc90.s("aaa110")?.let { it.substring(2, 4) }
                if (a110sub != null && a110sub != "99") out.daa001 = BigDecimal.ZERO // P7.3: NULL 不清
            }
            if (out.atc900 == 18934L) {
                out.bba002 = BigDecimal.ZERO; out.bba005 = BigDecimal.ZERO; out.baa002 = BigDecimal.ZERO
            }
            if (instrNvl(tc90.s("aac008"), "27") && ymStr <= "202112") out.baa003 = BigDecimal.ZERO
            if (instrNvl(tc90.s("aac008"), "51") && xjbl?.jslb == "2") { out.bba002 = BigDecimal.ZERO; out.baa002 = BigDecimal.ZERO }
            if (instrNvl(tc90.s("aac008"), "52") && xjbl?.jslb == "2") {
                out.bba002 = BigDecimal.ZERO; out.baa002 = BigDecimal.ZERO; out.bba005 = BigDecimal.ZERO
            }
            // P2: 原 REC_TC01.JSLB 为 OUT 形参恒 NULL → '202011' 分支死代码, 恒走 202010 窗口
            if (ymStr >= "202007" && ymStr <= "202010") out.bba004 = BigDecimal.ZERO
            if (ymStr == "202212") out.bba004 = BigDecimal.ZERO
            // 生育并入单位医疗(征缴覆盖前)
            out.bba002 = out.bba002.add(out.bba005); out.bba005 = BigDecimal.ZERO
            // 征缴覆盖
            val zj = levyConfirmed(tc90, ym)
            if (zj.bba001.add(zj.bba003).add(zj.baa001).add(zj.baa003) > BigDecimal.ZERO) {
                out.aaa301 = zj.aaa301; out.aaa304 = zj.aaa304
                out.atb90k = zj.atb90k; out.atb90m = zj.atb90m
                out.atb90p = zj.atb90p; out.atb90r = zj.atb90r
                out.bba001 = zj.bba001; out.bba003 = zj.bba003
                out.baa001 = zj.baa001; out.baa003 = zj.baa003
            }
            if (zj.bba002.add(zj.bba005).add(zj.atb90t).add(zj.baa002) > BigDecimal.ZERO) {
                out.aaa302 = zj.aaa302
                out.atb90l = zj.atb90l; out.atb90n = zj.atb90n; out.atb90o = zj.atb90o
                out.aaa399 = zj.aaa399; out.atb90q = zj.atb90q
                out.bba002 = zj.bba002; out.bba005 = zj.bba005; out.atb90t = zj.atb90t; out.baa002 = zj.baa002
                val unitBear = if (tc90.hasNonNull("aaa803")) tc90.s("aaa803") == "1" else tb93Aaa803 == "1"
                if (unitBear) { out.daa001 = BigDecimal.ZERO; out.daa002 = zj.daa001 }
                else { out.daa001 = zj.daa001; out.daa002 = BigDecimal.ZERO }
            }
            if (!zj.bba004IsNull && zj.bba004 > BigDecimal.ZERO) {
                out.aaa305 = zj.aaa305; out.atb90n = zj.atb90n; out.bba004 = zj.bba004
            }
            if (zj.caa001.add(zj.caa002) > BigDecimal.ZERO) {
                out.aaa303 = zj.aaa303; out.atb90sDw = zj.atb90sDw; out.atb90sGr = zj.atb90sGr
                out.caa001 = zj.caa001; out.caa002 = zj.caa002
            }
            out.bba000 = out.bba001.add(out.bba002).add(out.bba003).add(out.bba004).add(out.bba005).add(out.atb90t)
            out.baa000 = out.baa001.add(out.baa002).add(out.baa003)
            out.bba200 = out.bba000.add(out.caa001)
            out.baa200 = out.baa000.add(out.caa002)
            out.atc9eh = out.bba200.add(out.baa200)
            return Pair(ok, out)
        } catch (e: Exception) {
            return Pair(err, "计算险金出错，错误信息：${e.message}，请将错误信息发给维护工程师")
        }
    }

    private fun instrNvl(v: String?, token: String): Boolean = (v ?: "0").contains(token)
    /** Oracle 三值逻辑: 列 NULL → 比较结果 UNKNOWN → 条件为假 */
    private fun cmpLe(v: BigDecimal?, ym: Long): Boolean = v != null && v <= BigDecimal(ym)
    private fun cmpGt(v: BigDecimal?, ym: Long): Boolean = v != null && v > BigDecimal(ym)
}
