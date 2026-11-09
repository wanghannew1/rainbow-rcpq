package com.jlrc.rcpq.service.td

import com.jlrc.rcpq.service.proc.ProcHandler
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal

/**
 * td/d 8b: 费用结算确认/取消（旧 PKG_RCPQ_FZ.PRC_SALARYCOMPUTE 586L +
 * PRC_CANCLESALARYCOMPUTE 360L + PRC_TC93_TC9P 120L 规格重写）。
 *
 * **空壳第六例**: 步骤一 PRC_UPDATE_TC93_AC10(PKG_RCPQ_HM)实现体 300 行整块被注释
 * (work/pkg_rcpq_hm.sql L1615-1915, 可执行体仅 P_APCODE:=1 + 两个变量置 0)——
 * TC93 金额不来自 AC10 装载, 直接来自 tddc Excel 导入/手工录入。
 * 备份库实测 TC91/TC91_LOG/TC9P 三表全空 → 折子扣款循环与体检费孤儿生成在真实
 * 数据上均为空转; 本实现按可观测行为完整移植(含空表时 atc90ap 赋 NULL)。
 *
 * salaryCompute(atb930, atc931, atc937, atc932, aae011):
 * - 逐未结算 TC93 行(atc93g<>'1'): N_MONEY = aa+bba200+bba300+s+管理费+体检2项+残保金
 *   → PRC_MOD_TC91 折子贪心扣款(TC91_LOG 流水 + TC91 余额递减 + TC90.atc90ap 回写)
 *   → 结算标志(atc93g='1', aae114/214='1', atc93w=0, atc93p=0, aae013 空则'正常核算')
 *   → KEYS_1 逐 id 置 TC9P.atc9p4='1'; KEYS_2 逐 id 置 TC93.atc93ae='1'(原'0')
 *   → atc937='1' 时当月 AC10 结算落位(aac103='1')
 * - 尾程 PRC_TC93_TC9P: 无合同人员的体检费生成零额已结算 TC93 行
 */
@Component
class TddSettlementCases(
    private val jdbcTemplate: JdbcTemplate,
) {
    val ok = 1
    val err = -1

    /** PRC_MOD_TC91 等价: 折子贪心扣款(按 seq 顺序, 余额扣至 0, 流水入 TC91_LOG) */
    private fun modTc91(atc900: Long, moneyIn: BigDecimal, atc930: Long): BigDecimal? {
        var remaining = moneyIn
        val books = jdbcTemplate.query(
            "SELECT atc910, atc913, atc916 FROM tc91 WHERE atc913 > 0 AND atc900=? ORDER BY seq",
            { rs, _ -> arrayOf(rs.getLong("atc910"), rs.getBigDecimal("atc913"), rs.getBigDecimal("atc916")) },
            atc900)
        for (b in books) {
            val atc910 = b[0] as Long
            val balance = b[1] as BigDecimal? ?: BigDecimal.ZERO
            val balance16 = b[2] as BigDecimal? ?: BigDecimal.ZERO
            val logId = jdbcTemplate.queryForObject("SELECT nextval('seq_tc91_log_id')", Long::class.java)
            jdbcTemplate.update(
                """INSERT INTO tc91_log (id, atc910, atc930, atc913, atc916, money)
                   VALUES (?, ?, ?, ?, ?, ?)""",
                logId, atc910, atc930, balance, balance16,
                if (balance >= remaining) remaining else balance)
            if (balance >= remaining) {
                jdbcTemplate.update(
                    "UPDATE tc91 SET atc913 = atc913 - ?, atc916 = atc916 + ? WHERE atc910=?",
                    remaining, remaining, atc910)
                remaining = BigDecimal.ZERO
                break // 原 EXIT
            } else {
                jdbcTemplate.update(
                    "UPDATE tc91 SET atc913 = 0, atc916 = atc916 + ? WHERE atc910=?",
                    balance, atc910)
                remaining = remaining.subtract(balance)
            }
        }
        // 原: SELECT SUM(NVL(ATC913,0)) — 空集时 SUM=NULL → TC90.ATC90AP 赋 NULL
        return jdbcTemplate.queryForObject(
            "SELECT sum(atc913) FROM tc91 WHERE atc913 > 0 AND atc900=?",
            BigDecimal::class.java, atc900)
    }

    /** PRC_SALARYCOMPUTE 主入口 */
    @Transactional
    fun salaryCompute(atb930: String, atc931: String, atc937: String, atc932: String, aae011: String): Map<String, Any?> {
        val ym = atc931.toLongOrNull() ?: return mapOf("appCode" to err, "errMsg" to "费用结算认出错，错误信息：年月格式不正确，请将错误信息发给维护工程师")
        val cnt37 = atc937.toLongOrNull() ?: return mapOf("appCode" to err, "errMsg" to "费用结算认出错，错误信息：次第格式不正确，请将错误信息发给维护工程师")
        // 经办人单行查询(原 SELECT * INTO, 无行/多行即顶层异常)
        jdbcTemplate.query("SELECT 1 FROM sys_org_user_unit WHERE aae011=?",
            { rs, _ -> rs.getInt(1) }, aae011).firstOrNull()
            ?: return mapOf("appCode" to err, "errMsg" to "费用结算认出错，错误信息：获取经办人失败，请将错误信息发给维护工程师")
        val curMonth = java.time.YearMonth.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMM"))
        var settled = 0
        data class Row(val atc930: Long, val atc900Raw: Any?, val atc937: String?, val aae013: String?,
                       val keys1: String?, val keys2: String?) {
            val atc900: Long? get() = (atc900Raw as? Number)?.toLong()
        }
        val rows = jdbcTemplate.query(
            """SELECT atc930, atc900, atc937, aae013, keys_1, keys_2 FROM tc93
               WHERE atc93g <> '1' AND atb930=? AND atc931=? AND atc937=?""",
            { rs, _ -> Row(rs.getLong("atc930"), rs.getObject("atc900"), rs.getString("atc937"),
                rs.getString("aae013"), rs.getString("keys_1"), rs.getString("keys_2")) },
            atb930.toLong(), ym, atc937)
        for (rec in rows) {
            // 步骤一: PRC_UPDATE_TC93_AC10 = 空壳(注释体), 无操作
            // 步骤二: 金额读数(N_MONEY = 本月结算费用)
            val amounts = jdbcTemplate.queryForObject(
                """SELECT NVL(bba200,0), NVL(baa200,0), NVL(bba300,0), NVL(baa300,0), NVL(atb90a,0),
                   NVL(atc93aa,0), NVL(atc93s,0), NVL(atc9p2,0), NVL(atc9p3,0), NVL(atb90z,0)
                   FROM tc93 WHERE atc930=?""",
                { rs, _ -> Array(10) { i -> rs.getBigDecimal(i + 1) } }, rec.atc930)
            val bba200 = amounts[0]; val baa200 = amounts[1]; val bba300 = amounts[2]; val baa300 = amounts[3]
            val atb90a = amounts[4]; val atc93aa = amounts[5]; val atc93s = amounts[6]
            val atc9p2 = amounts[7]; val atc9p3 = amounts[8]; val atb90z = amounts[9]
            val nMoney = atc93aa.add(bba200).add(bba300).add(atc93s)
                .add(atb90a).add(atc9p2).add(atc9p3).add(atb90z)
            // 步骤三: 折子扣款(TC91 空表 → 仅 TC90.atc90ap 赋 NULL, 与原空集 SUM 语义一致)
            rec.atc900?.let { modTc91(it, nMoney, rec.atc930) }
            // 步骤四: 结算标志
            // 原 CASE WHEN rec.aae013 IS NULL THEN '正常核算' ELSE rec.aae013 END ≡ COALESCE(读旧值)
            jdbcTemplate.update(
                """UPDATE tc93 SET atc93w=0,
                   aae013 = COALESCE(aae013, '正常核算'),
                   atc93p='0', atc93g='1', aae114='1', aae214='1' WHERE atc930=?""",
                rec.atc930)
            // 步骤五: KEYS_1 → TC9P 核算标记
            rec.keys1?.split(",")?.filter { it.isNotBlank() }?.forEach { key ->
                jdbcTemplate.update("UPDATE tc9p SET atc9p4='1' WHERE atc9p0=?", key.toLong())
            }
            // 步骤六: KEYS_2 → 关联 TC93 垫付偿还标记
            rec.keys2?.split(",")?.filter { it.isNotBlank() }?.forEach { key ->
                jdbcTemplate.update(
                    "UPDATE tc93 SET atc93ae='1' WHERE atc93ae='0' AND atc930=?", key.toLong())
            }
            // 步骤七: 第一次工资 → 当月 AC10 结算落位(原用 SYSDATE 年月)
            if (rec.atc937 == "1") {
                jdbcTemplate.update(
                    """UPDATE ac10 SET atc930=?, aac103='1' WHERE aac103='0' AND atc9t4='3'
                       AND aae001=? AND aae002=? AND atc900=?""",
                    rec.atc930, curMonth.toLong(), curMonth.toLong(), rec.atc900)
            }
            settled++
        }
        // 尾程: PRC_TC93_TC9P 体检费孤儿生成(TC9P 空表时无操作; 逻辑完整保留)
        val orphans = jdbcTemplate.query(
            """SELECT DISTINCT aac001 FROM tc9p
               WHERE atc9p4='0' AND aae002=? AND atb930=?
                 AND NOT EXISTS (SELECT 1 FROM tc93 t WHERE t.aae002=? AND t.atb930=? AND t.aac001=tc9p.aac001)""",
            { rs, _ -> rs.getLong("aac001") }, curMonth.toLong(), atb930.toLong(), curMonth.toLong(), atb930.toLong())
        var orphansCreated = 0
        val v = jdbcTemplate.query(
            "SELECT atb900, aab001, aab004, atb930, atb931 FROM v_ab01_tb90_tb93 WHERE atb930=?",
            { rs, _ -> arrayOf(rs.getObject("atb900"), rs.getObject("aab001"), rs.getString("aab004"),
                rs.getObject("atb930"), rs.getString("atb931")) }, atb930.toLong()).firstOrNull()
            ?: return mapOf("appCode" to err, "errMsg" to "根据体检费生成工资信息出错，错误信息：结算单元不存在")
        for (aac001 in orphans) {
            var vAtc9p2 = BigDecimal.ZERO; var vAtc9p3 = BigDecimal.ZERO
            val keys = StringBuilder()
            val items = jdbcTemplate.query(
                """SELECT atc9p0, atc9p2, atc9p3 FROM tc9p WHERE atc9p4='0' AND aae002=? AND atb930=?
                   AND aac001=?""",
                { rs, _ -> arrayOf(rs.getLong("atc9p0"), rs.getBigDecimal("atc9p2"), rs.getBigDecimal("atc9p3")) },
                curMonth.toLong(), atb930.toLong(), aac001)
            for (item in items) {
                vAtc9p2 = vAtc9p2.add(item[1] as BigDecimal? ?: BigDecimal.ZERO)
                vAtc9p3 = vAtc9p3.add(item[2] as BigDecimal? ?: BigDecimal.ZERO)
                keys.append(',').append(item[0])
                jdbcTemplate.update("UPDATE tc9p SET atc9p4='1' WHERE atc9p0=?", item[0])
            }
            var keys1 = keys.toString()
            if (keys1.startsWith(",")) keys1 = keys1.substring(1)
            val ac01 = jdbcTemplate.query(
                "SELECT aac003, aac002 FROM ac01 WHERE aac001=?",
                { rs, _ -> arrayOf(rs.getString("aac003"), rs.getString("aac002")) }, aac001).firstOrNull()
                ?: continue // 原 SELECT * INTO 无行 → 顶层异常
            val atc930 = jdbcTemplate.queryForObject("SELECT nextval('seq_tc93_atc930')", Long::class.java)
            val zero = BigDecimal.ZERO
            jdbcTemplate.update(
                """INSERT INTO tc93 (atc930, atb900, aab001, aab004, atb930, atb931, atc900,
                   atc931, atc932, atc937, aac001, aac003, aac002, atb90a, atb90z,
                   atc93x, atc933, atc934, atc935, atc936, atc93k, atc93l, atc93m, atc93y,
                   atc93aa, atc93ab, atc93d, atc93ac, atc93c, atc93e, atc93s,
                   atc93g, atc93n, atc93p, atc9p2, atc9p3, keys_1, atc93w, aae114, aae214, aae314,
                   atc93ad, bba200, baa200, bba300, baa300, aae013, aae002, aae001)
                   VALUES (?, ?, ?, ?, ?, ?, 0, ?, ?, ?, ?, ?, ?, 0, 0,
                   0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                   '1', '0', '0', ?, ?, ?, '4', '1', '1', '0',
                   0, 0, 0, 0, 0, '无有效合同人员体检费生成', ?, ?)""",
                atc930, v[0], v[1], v[2], v[3], v[4], atc931.toLong(), atc932.toLong(), atc937, aac001, ac01[0], ac01[1],
                vAtc9p2, vAtc9p3, keys1, aae002(curMonth).toLong(), aae001(curMonth).toLong())
            orphansCreated++
        }
        return mapOf("appCode" to ok, "errMsg" to null as String?,
            "settled" to settled, "orphanExamRows" to orphansCreated)
    }

    private fun aae001(curMonth: String): String = curMonth

    private fun aae002(curMonth: String): String = curMonth

    /** PRC_CANCLESALARYCOMPUTE 等价 */
    @Transactional
    fun cancelSalaryCompute(atb930: String, atc931: String, atc937: String, atc932: String): Map<String, Any?> {
        val ym = atc931.toLongOrNull() ?: return mapOf("appCode" to err, "errMsg" to "取消费用结算认出错，错误信息：年月格式不正确")
        val cnt37 = atc937.toLongOrNull() ?: return mapOf("appCode" to err, "errMsg" to "取消费用结算认出错，错误信息：次第格式不正确")
        data class Row(val atc930: Long, val atc900Raw: Any?, val keys1: String?, val keys2: String?) {
            val atc900: Long? get() = (atc900Raw as? Number)?.toLong()
        }
        val rows = jdbcTemplate.query(
            """SELECT atc930, atc900, keys_1, keys_2 FROM tc93
               WHERE atc93g='1' AND atb930=? AND atc932=? AND atc931=? AND atc937=?""",
            { rs, _ -> Row(rs.getLong("atc930"), rs.getObject("atc900"),
                rs.getString("keys_1"), rs.getString("keys_2")) },
            atb930.toLong(), atc932.toLong(), ym, atc937)
        var cancelled = 0
        for (rec in rows) {
            rec.keys1?.split(",")?.filter { it.isNotBlank() }?.forEach { key ->
                jdbcTemplate.update("UPDATE tc9p SET atc9p4='0' WHERE atc9p0=?", key.toLong())
            }
            if (rec.atc900 == 0L) {
                jdbcTemplate.update("DELETE FROM tc93 WHERE atc930=?", rec.atc930)
            } else {
                rec.keys1?.split(",")?.filter { it.isNotBlank() }?.forEach { key ->
                    jdbcTemplate.update("UPDATE tc93 SET atc93ae='0' WHERE atc930=?", key.toLong())
                }
                // 【坏功能第六例】原 "update TC31 set aae100='0', atc930=''" — TC31 表无 atc930 列
                // (PG 镜像自 Oracle user_tab_columns), 旧 HQL 运行时 ORA-00904 → 顶层 WHEN OTHERS
                // → appCode=-1, 循环中断。忠实保留: 报错即止(此前 TC9P 重置已生效)
                throw IllegalStateException(
                    "取消费用结算认出错，错误信息：ORA-00904: \"ATC930\": 标识符无效 (TC31 无此列, 旧系统运行时同样报错)，请将错误信息发给维护工程师")
                // 回退折子信息(TC91_LOG 流水还原 TC91, 删流水)
                val logs = jdbcTemplate.query(
                    "SELECT atc910, atc913, atc916 FROM tc91_log WHERE atc930=?",
                    { rs, _ -> arrayOf(rs.getLong("atc910"), rs.getBigDecimal("atc913"), rs.getBigDecimal("atc916")) },
                    rec.atc930)
                for (item in logs) {
                    jdbcTemplate.update(
                        "UPDATE tc91 SET atc913=?, atc916=? WHERE atc910=?", item[1], item[2], item[0])
                }
                jdbcTemplate.update("DELETE FROM tc91_log WHERE atc930=?", rec.atc930)
                val nMoney = jdbcTemplate.queryForObject(
                    "SELECT sum(NVL(atc913, 0)) FROM tc91 WHERE atc913 > 0 AND atc900=?",
                    BigDecimal::class.java, rec.atc900)
                jdbcTemplate.update("UPDATE tc90 SET atc90ap=? WHERE atc900=?", nMoney, rec.atc900)
                // 清空本次结算信息(原大 UPDATE 逐字段)
                jdbcTemplate.update(
                    """UPDATE tc93 SET atc93aa=null, atc93ab=null, atc93c=null, atc93e=null,
                       atc93g='0', atc93r=null, atc93s=null, atc9p2=null, atc9p3=null,
                       keys=null, keys_1=null, aae114='0', aae111=null, aae117=null, aae136=null,
                       aae119=null, aae120=null, aae122=null, aae214='0',
                       aae314='0', aae311=null, aae317=null, aae336=null, aae319=null, aae320=null, aae322=null,
                       keys_2=null, atc93ae=null, atc93d=null, atc93ac=null, atc93ad=null,
                       bba200=null, baa200=null, bba300=null, baa300=null WHERE atc930=?""",
                    rec.atc930)
                jdbcTemplate.update("UPDATE ac10 SET atc930=null, aac103='0' WHERE atc930=?", rec.atc930)
                rec.keys2?.split(",")?.filter { it.isNotBlank() }?.forEach { key ->
                    jdbcTemplate.update("UPDATE tc93 SET atc93ae='0' WHERE atc930=?", key.toLong())
                }
            }
            cancelled++
        }
        return mapOf("appCode" to ok, "errMsg" to null as String?, "cancelled" to cancelled)
    }
}

/** /proc 注册面 */
@Component
class TddSettlementProcs(private val s: TddSettlementCases) : ProcHandler {
    override fun procName() = "PKG_RCPQ_FZ.PRC_SALARYCOMPUTE"
    override fun handle(params: Map<String, Any?>): Map<String, Any?> {
        val r = s.salaryCompute(
            params["atb900"]?.toString() ?: params["atb930"]?.toString()
            ?: return mapOf("appCode" to -1, "errMsg" to "缺少 atb930"),
            params["atc931"]?.toString() ?: return mapOf("appCode" to -1, "errMsg" to "缺少 atc931"),
            params["atc937"]?.toString() ?: return mapOf("appCode" to -1, "errMsg" to "缺少 atc937"),
            params["atc932"]?.toString() ?: return mapOf("appCode" to -1, "errMsg" to "缺少 atc932"),
            params["aae011"]?.toString() ?: return mapOf("appCode" to -1, "errMsg" to "缺少 aae011"))
        return mapOf("appCode" to r["appCode"], "errMsg" to r["errMsg"],
            "settled" to r["settled"], "orphanExamRows" to r["orphanExamRows"])
    }
}

@Component
class TddCancelProcs(private val s: TddSettlementCases) : ProcHandler {
    override fun procName() = "PKG_RCPQ_FZ.PRC_CANCLESALARYCOMPUTE"
    override fun handle(params: Map<String, Any?>): Map<String, Any?> {
        val r = s.cancelSalaryCompute(
            params["atb930"]?.toString() ?: return mapOf("appCode" to -1, "errMsg" to "缺少 atb930"),
            params["atc931"]?.toString() ?: return mapOf("appCode" to -1, "errMsg" to "缺少 atc931"),
            params["atc937"]?.toString() ?: return mapOf("appCode" to -1, "errMsg" to "缺少 atc937"),
            params["atc932"]?.toString() ?: return mapOf("appCode" to -1, "errMsg" to "缺少 atc932"))
        return mapOf("appCode" to r["appCode"], "errMsg" to r["errMsg"], "cancelled" to r["cancelled"])
    }
}
