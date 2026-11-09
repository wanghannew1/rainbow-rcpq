package com.jlrc.rcpq.service.td

import com.jlrc.rcpq.service.calc.InsuranceCalcEngine
import com.jlrc.rcpq.service.proc.ProcHandler
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter

/**
 * PRC_社保补缴明细（旧 PKG_保险人员增减 L1867-2014 规格重写，险金引擎波解锁）。
 *
 * 语义: 对 (年月, 险金户头, 区划) 批次重建 TC9W_BP05 补缴明细——
 * 逐合同(atc90p='0' 在保 且 aaa203<年月≤aaa208 参保窗口)按 aaa203~年月-1 的月份段
 * 逐年生成: 每月取跨年补退基数(PKG_保险补退 版, 年份<年月年时先 PRC_保险补收基数_个人
 * 生成 TC08) → PRC_计算险金 → 累加养老/失业单位个人额; 旧硬编码 atc900=17175 且
 * 年份<2023 失业归零; 补缴总额>0 才落行。
 * 依赖引擎: InsuranceCalcEngine.calc / 保险补退版 basesForMonth。
 */
@Component
class TdckPaymentDetail(
    private val jdbcTemplate: JdbcTemplate,
    private val engine: InsuranceCalcEngine,
) {
    val ok = 1
    val err = -1

    /** 全列小写 Map(引擎按列名取值) */
    private fun loadTc90(atc900: Long): Map<String, Any?>? = jdbcTemplate.query(
        "SELECT * FROM tc90 WHERE atc900=?", { rs, _ ->
            val md = rs.metaData
            (1..md.columnCount).associate { md.getColumnLabel(it).lowercase() to rs.getObject(it) }
        }, atc900).firstOrNull()

    /** PRC_保险补收基数_个人 等价: 跨年合同生成 TC08 补收基数行(社保/省医保/公积金三类) */
    @Transactional
    fun supplementPriorYearBases(aae002: String, atc900: Long, aae011: String): Map<String, Any?> {
        val user = jdbcTemplate.query(
            "SELECT aae011, aae017, aae019, aae020, aae022 FROM sys_org_user_unit WHERE aae011=?",
            { rs, _ -> arrayOf(rs.getString("aae011"), rs.getString("aae017"), rs.getString("aae019"),
                rs.getString("aae020"), rs.getString("aae022")) }, aae011).firstOrNull()
            ?: return mapOf("code" to err, "msg" to "获取经办人信息失败")
        var inserted = 0
        data class Block(val kind: String, val baseCol: String, val flag: String, val startCol: String,
                         val regionCol: String, val region21Col: String, val curCol: String, val curFlag: String, val curEnd: String)
        // 三块: 社保(atc905/aaa401/aaa201) 省医保(aaa302/aaa402/aaa601, 仅99区划) 公积金(aaa303/aaa407/aaa501)
        val blocks = listOf(
            Block("1", "atc905", "aaa401", "aaa201", "aaa100", "aaa101", "atc90p", "aaa208", "aaa203"),
            Block("2", "aaa302", "aaa402", "aaa601", "aaa110", "aaa111", "atc90pa", "aaa608", "aaa603"),
            Block("3", "aaa303", "aaa407", "aaa501", "aaa120", "aaa121", "atc90w", "aaa508", "aaa503"),
        )
        for (b in blocks) {
            val regionFilter = if (b.kind == "2") " AND substr(aaa110, 3, 2)='99'" else ""
            val rows = jdbcTemplate.query(
                """SELECT a.atc900, a.aac001, a.aac003, a.aac002, a.atb930, a.atb900, a.aab001, a.aab004,
                   a.${b.startCol}::text st, a.${b.curCol} cp, a.${b.curFlag}::text cf,
                   a.${b.regionCol} rg, a.${b.region21Col} rg21, a.${b.baseCol}::text base,
                   b.aaa030::text a030, b.aaa031 a031
                   FROM tc90 a, ab01 bb
                   WHERE a.aab001 = bb.aab001 AND a.${b.flag} = '1'
                     AND a.atc900 = ?
                     AND ((a.${b.startCol}::text < ? AND a.${b.curCol} IS NULL AND substr(a.${b.startCol}::text,1,4) < substr(?::text,1,4))
                       OR (a.${b.curEnd}::text < ? AND a.${b.curFlag}::text = ?::text AND a.${b.curCol}::text = '0' AND substr(a.${b.curEnd}::text,1,4) < substr(?::text,1,4)))
                     $regionFilter""",
                { rs, _ -> arrayOf<Any?>(rs.getLong("atc900"), rs.getString("aac001"), rs.getString("aac003"),
                    rs.getString("aac002"), rs.getLong("atb930"), rs.getLong("atb900"), rs.getLong("aab001"),
                    rs.getString("aab004"), rs.getString("st"), rs.getString("cp"), rs.getString("cf"),
                    rs.getString("rg"), rs.getString("rg21"), rs.getString("base"), rs.getString("a030"), rs.getString("a031")) },
                atc900, aae002, aae002, aae002, aae002, aae002)
            for (r in rows) {
                val st = r[8] as String? ?: continue
                val ks = runCatching { YearMonth.parse(st.substring(0, 6), DateTimeFormatter.ofPattern("yyyyMM")) }
                    .getOrElse { return mapOf("code" to err, "msg" to "社保补发开始年月格式不正确，无法生成补缴基数") }
                val js = runCatching { YearMonth.parse(aae002.substring(0, 6), DateTimeFormatter.ofPattern("yyyyMM")) }
                    .getOrElse { return mapOf("code" to err, "msg" to "补发结束年月格式不正确，无法生成补缴基数") }
                // 逐年分组(至 结束年月 的前一月: 原 MONTHS_BETWEEN(D_JSNY, D_KSNY) 不含结束月)
                var cur = ks
                val groups = linkedMapOf<String, MutableList<YearMonth>>()
                while (cur < js) {
                    groups.getOrPut(cur.format(DateTimeFormatter.ofPattern("yyyy"))) { mutableListOf() }.add(cur)
                    cur = cur.plusMonths(1)
                }
                for ((nf, months) in groups) {
                    if (nf >= aae002.substring(0, 4)) continue
                    val ksn = months.first().format(DateTimeFormatter.ofPattern("yyyyMM"))
                    val jsn = months.last().format(DateTimeFormatter.ofPattern("yyyyMM"))
                    val atb931 = jdbcTemplate.query("SELECT atb931 FROM tb93 WHERE atb930=?",
                        { rs, _ -> rs.getString("atb931") }, r[4]).firstOrNull()
                        ?: return mapOf("code" to err, "msg" to "查询结算单位名称出错")
                    val n = jdbcTemplate.update(
                        """INSERT INTO tc08 (aae002, aae000, ksny, jsny, atc900, aac001, aac003, aac002,
                           atb930, atb931, aaa020, aaa021, atb900, aab001, aab004, aaa030, aaa031,
                           aaa000, aaa300, aae011, aae017, aae036, aae019, aae020, aae022, aae013,
                           baz001, baz002, atc080)
                           SELECT ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, now(), ?, ?, ?, null,
                           nextval('seq_sys_baz001'), nextval('seq_sys_baz002'), nextval('seq_tc08_atc080')""",
                        aae002.toLong(), nf.toLong(), ksn.toLong(), jsn.toLong(), r[0], r[1], r[2], r[3],
                        r[4], atb931, r[11], r[12], r[5], r[6], r[7], r[14], r[15],
                        b.kind, r[13], user[0], user[1], user[2], user[3], user[4])
                    inserted += n
                }
            }
        }
        return mapOf("code" to ok, "msg" to null as String?, "tc08Inserted" to inserted)
    }

    /** PKG_保险补退.PRC_获取补退基数 等价(月度版: 先补收后按 aae000+aae002 过滤 TC08) */
    fun basesForMonth(atc900: Long, aae002: String, nf: String): Map<String, BigDecimal> {
        val base = loadTc90(atc900) ?: throw IllegalStateException("获取缴费基数出错")
        fun g(f: String): BigDecimal = (base[f] as? Number)?.let { BigDecimal(it.toString()) } ?: BigDecimal.ZERO
        val out = mutableMapOf(
            "aaa301" to g("atc905"), "aaa304" to g("atc905"),
            "aaa302" to g("aaa302"), "aaa305" to g("aaa305"), "aaa306" to g("aaa306"),
            "aaa303" to g("aaa303"))
        if (nf < aae002.substring(0, 4)) {
            val r = supplementPriorYearBases(aae002, atc900, "001")
            if ((r["code"] as Int) != ok) throw IllegalStateException(r["msg"]?.toString() ?: "补收基数出错")
        }
        val tc08 = jdbcTemplate.query(
            "SELECT aaa000, aaa300 FROM tc08 WHERE aae000=? AND aae002=? AND atc900=?",
            { rs, _ -> arrayOf(rs.getString("aaa000"), rs.getBigDecimal("aaa300")) },
            nf.toLong(), aae002.toLong(), atc900)
        for (row in tc08) {
            val kind = row[0] as String?
            val amt = row[1] as BigDecimal? ?: BigDecimal.ZERO
            if (kind == "1") { out["aaa301"] = amt; out["aaa304"] = amt }
            if (kind == "2") { out["aaa302"] = amt; out["aaa305"] = amt; out["aaa306"] = amt }
            if (kind == "3") { out["aaa303"] = amt }
        }
        return out
    }

    /** PRC_社保补缴明细 主入口 */
    @Transactional
    fun paymentDetail(aae002: String, aaa030: String, aaa020: String): Map<String, Any?> {
        val ym = aae002.toLongOrNull() ?: return mapOf("code" to err, "msg" to "社保年月格式不正确")
        jdbcTemplate.update("DELETE FROM tc9w_bp05 WHERE aaa030=? AND aaa020=? AND aae002=?",
            aaa030.toLong(), aaa020, ym)
        val aaa031 = jdbcTemplate.query("SELECT aaa031 FROM aa03 WHERE aaa030=?",
            { rs, _ -> rs.getString("aaa031") }, aaa030.toLong()).firstOrNull()
            ?: return mapOf("code" to err, "msg" to "险金户头不存在")
        data class Row(val atc900: Long, val aac001: Long, val aac002: String?, val aac003: String?,
                       val aaa100: String?, val aaa101: String?, val aaa203: String, val aaa001: String?)
        val contracts = jdbcTemplate.query(
            """SELECT a.atc900, a.aac001, a.aac002, a.aac003, a.aaa100, a.aaa101, a.aaa203::text a203, a.aaa001
               FROM tc90 a, ab01 b
               WHERE a.aab001 = b.aab001 AND b.aaa030::text = ?
                 AND a.atc90p = '0' AND a.aaa203::text < ? AND a.aaa208::text = ?""",
            { rs, _ -> Row(rs.getLong("atc900"), rs.getLong("aac001"), rs.getString("aac002"),
                rs.getString("aac003"), rs.getString("aaa100"), rs.getString("aaa101"),
                rs.getString("a203"), rs.getString("aaa001")) },
            aaa030, ym.toString(), ym.toString())
        var rows = 0
        for (tc in contracts) {
            val raw = loadTc90(tc.atc900) ?: continue
            val start = runCatching { YearMonth.parse(tc.aaa203.substring(0, 6), DateTimeFormatter.ofPattern("yyyyMM")) }
                .getOrElse { return mapOf("code" to err, "msg" to "计算补发信息出错：参保起始年月格式不正确") }
            val endExcl = YearMonth.parse(aae002.substring(0, 6), DateTimeFormatter.ofPattern("yyyyMM")).minusMonths(1)
            if (start > endExcl) continue
            // 逐年分组
            var cur = start
            val groups = linkedMapOf<String, MutableList<YearMonth>>()
            while (cur <= endExcl) {
                groups.getOrPut(cur.format(DateTimeFormatter.ofPattern("yyyy"))) { mutableListOf() }.add(cur)
                cur = cur.plusMonths(1)
            }
            for ((nf, months) in groups) {
                var abp004 = BigDecimal.ZERO
                var abp005 = BigDecimal.ZERO; var abp006 = BigDecimal.ZERO
                var abp007 = BigDecimal.ZERO; var abp008 = BigDecimal.ZERO
                for (m in months) {
                    val mStr = m.format(DateTimeFormatter.ofPattern("yyyyMM"))
                    // 跨年补退基数(保险补退版: 先补收, aae000+aae002 过滤) → 覆写本合同基数
                    val bases = basesForMonth(tc.atc900, aae002, nf)
                    val monthRow = HashMap(raw)
                    monthRow["aaa301"] = bases["aaa301"]; monthRow["aaa302"] = bases["aaa302"]
                    monthRow["aaa303"] = bases["aaa303"]; monthRow["aaa304"] = bases["aaa304"]
                    monthRow["aaa305"] = bases["aaa305"]; monthRow["aaa306"] = bases["aaa306"]
                    val (code, result) = engine.calc(monthRow, mStr, InsuranceCalcEngine.Tc01())
                    if (code != ok) return mapOf("code" to err, "msg" to "计算补发信息出错：$result")
                    val r = result as InsuranceCalcEngine.Tc01
                    abp004 = r.aaa301
                    abp005 = abp005.add(r.bba001)
                    abp006 = abp006.add(r.bba003)
                    abp007 = abp007.add(r.baa001)
                    abp008 = abp008.add(r.baa003)
                    // 旧硬编码: 17175 且 年份<2023 失业双项归零
                    if (tc.atc900 == 17175L && nf < "2023") { abp006 = BigDecimal.ZERO; abp008 = BigDecimal.ZERO }
                }
                val ksn = months.first().format(DateTimeFormatter.ofPattern("yyyyMM"))
                val jsn = months.last().format(DateTimeFormatter.ofPattern("yyyyMM"))
                val ys = months.size
                val abp009 = abp005.add(abp007)
                val abp00a = abp006.add(abp008)
                val abp00b = abp005.add(abp006)
                val abp00c = abp007.add(abp008)
                val abp00d = abp00b.add(abp00c)
                if (abp00d > BigDecimal.ZERO) {
                    jdbcTemplate.update(
                        """INSERT INTO tc9w_bp05 (aae002, aaa030, aaa031, aaa020, aaa021, atc900, aaa001,
                           aac002, aac003, aac009, abp001, abp002, abp001_1, abp003, abp004, abp005, abp006,
                           abp007, abp008, abp009, abp00a, abp00b, abp00c, abp00d)
                           VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)""",
                        ym, aaa030.toLong(), aaa031, aaa020, tc.aaa101, tc.atc900, tc.aaa001,
                        tc.aac002, tc.aac003,
                        if (instrContains(tc.aaa100, "27")) "农村" else "非农",
                        ksn, jsn, "$ksn-$jsn", ys.toString(), abp004.toPlainString(),
                        abp005.toPlainString(), abp006.toPlainString(), abp007.toPlainString(),
                        abp008.toPlainString(), abp009.toPlainString(), abp00a.toPlainString(),
                        abp00b.toPlainString(), abp00c.toPlainString(), abp00d.toPlainString())
                    rows++
                }
            }
        }
        return mapOf("code" to ok, "msg" to null as String?, "contracts" to contracts.size, "rows" to rows)
    }

    private fun instrContains(v: String?, token: String): Boolean = (v ?: "0").contains(token)
}

/** /proc 注册面: PKG_保险人员增减.PRC_社保补缴明细 */
@Component
class TdckPaymentDetailProc(private val detail: TdckPaymentDetail) : ProcHandler {
    override fun procName() = "PKG_保险人员增减.PRC_社保补缴明细"
    override fun handle(params: Map<String, Any?>): Map<String, Any?> {
        val aae002 = params["aae002"]?.toString() ?: return mapOf("appCode" to -1, "errMsg" to "缺少 aae002")
        val aaa030 = params["aaa030"]?.toString() ?: return mapOf("appCode" to -1, "errMsg" to "缺少 aaa030")
        val aaa020 = params["aaa020"]?.toString() ?: return mapOf("appCode" to -1, "errMsg" to "缺少 aaa020")
        val r = detail.paymentDetail(aae002, aaa030, aaa020)
        return mapOf("appCode" to r["code"], "errMsg" to r["msg"],
            "contracts" to r["contracts"], "rows" to r["rows"])
    }
}
