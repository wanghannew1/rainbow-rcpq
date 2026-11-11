package com.jlrc.rcpq.web

import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.web.bind.annotation.*

/**
 * 业务员工作台辅助端点(窄口径白名单, 供 static/index.html 使用; 无自由 SQL 面)。
 * 每个端点对应一个业务动作的批次准备/状态查询, 业务校验与提交仍走 /proc 与 /interaction。
 */
@RestController
@RequestMapping("/wb")
class WorkbenchController(private val jdbcTemplate: JdbcTemplate) {

    private fun nextval(seq: String) = jdbcTemplate.queryForObject("SELECT nextval('$seq')", Long::class.java)!!

    @PostMapping("/next-batch")
    fun nextBatch(): Map<String, Any?> = mapOf("ace100" to nextval("seq_ce10_ace100"))

    /** 新签导入批次行(tc90_bp), 字段白名单逐一绑定 */
    data class HireRow(
        val aac002: String, val aac003: String, val aae00p: String,
        val atb930: Long, val atb900: Long, val aab001: Long,
        val atc90a: String, val atc90c: String, val atc90d: String?,
        val atc905: String, val aae002: String, val aaa401: String?,
        val aaa100: String?, val aaa203: String?, val aaa301: String?,
        val aab004: String?,
    )

    @PostMapping("/hire-batch")
    fun hireBatch(@RequestBody r: HireRow): Map<String, Any?> {
        val ace = nextval("seq_ce10_ace100")
        jdbcTemplate.update(
            """INSERT INTO tc90_bp (id, ace100, aac002, aac003, aac005, aac012, aae00p, aac009, atc90a,
               atc90c, atc90d, atc905, aae002, aaa401, aaa100, aaa203, aaa301, atb930, atb900, aab001, aab004)
               VALUES (nextval('seq_tc90_id'), ?, ?, ?, '01-汉族', '91-其他人员', ?, '10-城镇失业',
               ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)""",
            ace, r.aac002, r.aac003, r.aae00p, r.atc90a, r.atc90c, r.atc90d, r.atc905, r.aae002,
            r.aaa401, r.aaa100, r.aaa203, r.aaa301, r.atb930, r.atb900, r.aab001, r.aab004 ?: "单位名")
        return mapOf("ace100" to ace)
    }

    /** 审核台: 合同行 + 待审 TB91 行(20) */
    @GetMapping("/audit-row/{atc900}")
    fun auditRow(@PathVariable atc900: Long): Map<String, Any?> {
        val tc90 = jdbcTemplate.queryForMap(
            "SELECT atc900, aac003, atb90r, aac001 FROM tc90 WHERE atc900=?", atc900)
        val tb91 = jdbcTemplate.queryForMap(
            "SELECT atb910, atb91z, atb911, atb912 FROM tb91 WHERE atb91z=? AND atb911='20' " +
                "ORDER BY atb910 DESC LIMIT 1", atc900)
        return mapOf("tc90" to tc90, "tb91" to tb91)
    }

    /** 工资核对: 编制 TC93 当月行(从合同带单位/结算单元/经办信息) + 核对批次行 */
    data class SalarySetup(
        val atc900: Long, val ym: Long, val atc93aa: String, val atb930: Long,
    )

    @PostMapping("/salary-setup")
    fun salarySetup(@RequestBody r: SalarySetup): Map<String, Any?> {
        val exists = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM tc93 WHERE atc900=? AND atc931=?",
            Int::class.java, r.atc900, r.ym) ?: 0
        if (exists == 0) {
            jdbcTemplate.update(
                """INSERT INTO tc93 (atc900, atc930, baz001, baz002, atb900, aab001, aab004, atb930,
                   atb931, aac001, aac002, aac003, atc931, atc932, atc937, aae001, atc93aa, atc93x,
                   atc93p, aae011)
                   SELECT ?, nextval('seq_tc93_atc930'), nextval('seq_sys_baz001'), nextval('seq_sys_baz002'),
                   t.atb900, t.aab001, t.aab004, t.atb930, t.atc90x, t.aac001, t.aac002, t.aac003,
                   ?, ?, '1', ?, ?, ?, '1', 'jiaq' FROM tc90 t WHERE t.atc900=?""",
                r.atc900, r.ym, r.ym, r.ym, r.atc93aa, r.atc93aa, r.atc900)
        }
        val atc930 = jdbcTemplate.queryForObject(
            "SELECT atc930 FROM tc93 WHERE atc900=? AND atc931=? LIMIT 1",
            Long::class.java, r.atc900, r.ym)!!
        val aac002 = jdbcTemplate.queryForObject(
            "SELECT aac002 FROM tc93 WHERE atc930=?", String::class.java, atc930)
        val aac003 = jdbcTemplate.queryForObject(
            "SELECT aac003 FROM tc93 WHERE atc930=?", String::class.java, atc930)
        val ace = nextval("seq_ce10_ace100")
        val bid = jdbcTemplate.queryForObject("SELECT COALESCE(MAX(id)+1,1) FROM tc93_bp1", Long::class.java)
        jdbcTemplate.update(
            """INSERT INTO tc93_bp1 (id, ace100, aac002, aac003, atc93aa, atc93k, atc93m)
               VALUES (?, ?, ?, ?, ?, '0', '0')""",
            bid, ace, aac002, aac003, java.math.BigDecimal(r.atc93aa))
        return mapOf("ace100" to ace, "atc930" to atc930)
    }

    /** 到账批次行(tb97_bp) */
    data class ArrivalRow(val abp001: String, val abp002: String, val abp005: String)

    @PostMapping("/arrival-batch")
    fun arrivalBatch(@RequestBody r: ArrivalRow): Map<String, Any?> {
        val ace = nextval("seq_ce10_ace100")
        jdbcTemplate.update(
            "INSERT INTO tb97_bp (id, ace100, abp001, abp002, abp005) VALUES (nextval('seq_tb97_bp_id'), ?, ?, ?, ?)",
            ace, r.abp001, r.abp002, r.abp005)
        return mapOf("ace100" to ace)
    }

    /** 发票批次行(tb99_bp): 固定借发票/普票 + 人事代理服务费一项 */
    data class InvoiceRow(val abp001: String, val abp004: String)

    @PostMapping("/invoice-batch")
    fun invoiceBatch(@RequestBody r: InvoiceRow): Map<String, Any?> {
        val ace = nextval("seq_ce10_ace100")
        jdbcTemplate.update(
            """INSERT INTO tb99_bp (id, ace100, aaa030, aaa031, abp001, abp002, abp003, abp004)
               VALUES (nextval('seq_tb99_id'), ?, 1, '工作台', ?, '1-借发票', '1-普票', ?)""",
            ace, r.abp001, r.abp004)
        return mapOf("ace100" to ace)
    }

    /** 续签批次行(tc90_bp3), 合同要素从生效合同带出 */
    data class RenewRow(
        val aac002: String, val aac003: String, val atc90c: String, val atc90d: String,
    )

    @PostMapping("/renew-batch")
    fun renewBatch(@RequestBody r: RenewRow): Map<String, Any?> {
        val ace = nextval("seq_ce10_ace100")
        jdbcTemplate.update(
            """INSERT INTO tc90_bp3 (id, ace100, aac002, aac003, atb930, aab001, aab004, aab004_1,
               aae00b, atc908, atc90a, atc90c, atc90d)
               SELECT nextval('seq_tc90_bp3_id'), ?, ?, ?, t.atb930, t.aab001, t.aab004,
               '续签', 'R1', '续签岗', '1-固定期限', ?, ? FROM tc90 t
               WHERE t.aac002=? AND t.aac003=? AND t.atb90r='3' LIMIT 1""",
            ace, r.aac002, r.aac003, r.atc90c, r.atc90d, r.aac002, r.aac003)
        return mapOf("ace100" to ace)
    }

    /** 终止批次行(tc90_bp1), 单位要素从合同带出 */
    data class TerminateRow(
        val aac002: String, val aac003: String, val atc90i: String, val atc90av: String,
    )

    @PostMapping("/terminate-batch")
    fun terminateBatch(@RequestBody r: TerminateRow): Map<String, Any?> {
        val ace = nextval("seq_ce10_ace100")
        val bid = jdbcTemplate.queryForObject("SELECT COALESCE(MAX(id)+1,1) FROM tc90_bp1", Long::class.java)
        jdbcTemplate.update(
            """INSERT INTO tc90_bp1 (id, ace100, aac002, aac003, atb930, aab001, aab004,
               atc90i, aae003, atc90k, atc90l, atc90n, atc90av)
               SELECT ?, ?, ?, ?, t.atb930, t.aab001, t.aab004, ?, ?::numeric,
               '09-职工个人原因', '0-否', '0-否', ?::numeric FROM tc90 t
               WHERE t.aac002=? AND t.aac003=? ORDER BY t.atc900 DESC LIMIT 1""",
            bid, ace, r.aac002, r.aac003, r.atc90i, r.atc90av.substring(0, 6),
            r.atc90av, r.aac002, r.aac003)
        return mapOf("ace100" to ace)
    }
}
