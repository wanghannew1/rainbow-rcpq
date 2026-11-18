package com.jlrc.rcpq.web

import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

/**
 * 基数申报工作台查询面（W5 收尾波, 配合 PKG_缴费基数 14 过程:
 * 核对/确认×4 · 基数导入校验/提交 · 上下限模板/校验/提交/审核 · 未申报清单/名册×4）。
 * 只读查询; 页码 wb-dsf-tip: tdcl(29)/tdcq(30)/tdcab(31)/tdcac(32)/tdch(33)/tdcc(20)/tdcd(21)。
 */
@RestController
@RequestMapping("/wb/jishu")
class JishuViewController(private val jdbc: JdbcTemplate) {

    /** 线 → (险种 XZ, 核对状态列, 属地列, 申报基数列) — 与 PKG_缴费基数 四线差异表同源 */
    private val lines = mapOf(
        "sb" to ("1" to listOf("aaa100", "aae214_1", "aaa301_1")),
        "yb" to ("2" to listOf("aaa110", "aae214_2", "aaa302_1")),
        "gjj" to ("3" to listOf("aaa120", "aae214_3", "aaa303_1")),
        "gs" to ("4" to listOf("aaa130", "aae214_4", "aaa305_1")))

    private fun lineOf(line: String?) = lines[line?.trim()?.lowercase()]

    /** 核对/确认工作台: AA97 该年度该户头该线属地的核对结果集 */
    @GetMapping("/aa97")
    fun aa97(@RequestParam aae001: String, @RequestParam aaa030: Long,
             @RequestParam aaa020: String, @RequestParam line: String): List<Map<String, Any?>> {
        if (!Regex("^\\d{4}$").matches(aae001)) return emptyList()
        val ln = lineOf(line) ?: return emptyList()
        val (xz, cols) = ln
        val (dCol, stCol, baseCol) = cols
        return jdbc.queryForList(
            """SELECT aaa970, aac001, aac003, aac002, $stCol, $baseCol, aaa300,
                      atb930, aab004, aaa100, aaa110, aaa120, aaa130,
                      aae002_1, aae002_2, aae002_3, aae002_4
               FROM aa97 WHERE aae001=? AND aaa030=? AND $dCol=?
               ORDER BY aac003 NULLS LAST LIMIT 500""",
            aae001.toLong(), aaa030, aaa020)
    }

    /** 基数导入: 批次行(AA97_BP), 含校验/提交状态与失败原因 */
    @GetMapping("/bp")
    fun bp(@RequestParam ace100: Long): List<Map<String, Any?>> = jdbc.queryForList(
        """SELECT id, ace100, aac002, aac003, atb930, atb931, aae001,
                  aaa301_1, aaa302_1, aaa302_2, aaa303_1, aaa303_2, aaa305_1, aaa305_2,
                  atb90s_dw, atb90s_gr, ace10a, ace10b
           FROM aa97_bp WHERE ace100=? ORDER BY id LIMIT 500""", ace100)

    /** 基数导入: 最近批次列表(按批次聚合行数/状态) */
    @GetMapping("/batches")
    fun batches(): List<Map<String, Any?>> = jdbc.queryForList(
        """SELECT ace100, count(*) n,
                  count(*) FILTER (WHERE ace10a='3') ok3,
                  count(*) FILTER (WHERE ace10a='2') ng2,
                  count(*) FILTER (WHERE ace10a='1') ok1,
                  max(aae001::text) aae001
           FROM aa97_bp WHERE ace100>=10000001 GROUP BY ace100
           ORDER BY ace100 DESC LIMIT 10""")

    /** 上下限调整: 批次行(TC0A_BP) */
    @GetMapping("/tc0a_bp")
    fun tc0aBp(@RequestParam ace100: Long): List<Map<String, Any?>> = jdbc.queryForList(
        """SELECT id, ace100, aac002, aac003, atb930, aae002, atc905, aaa303_1, aaa303_2,
                  atc0a1, atc0a2, atc0a3, atb90s_dw, atb90s_gr, abp00c, abp00d, abp00e,
                  ace10a, ace10b
           FROM tc0a_bp WHERE ace100=? ORDER BY id LIMIT 500""", ace100)

    /** 上下限审核: TC0A 列表(可按审核标识过滤; aae002=基数调整年月) */
    @GetMapping("/tc0a")
    fun tc0a(@RequestParam(required = false) aae002: Long?,
             @RequestParam(required = false) aae114: String?): List<Map<String, Any?>> {
        var sql = """SELECT atc0a0, aae002, atc900, aac003, aac002, aaa020, aaa021,
                            atc905, aaa303, aaa303_1, atc0a1, atc0a2, atc0a3,
                            atb90s_dw, atb90s_gr, abp00c, abp00d, abp00e,
                            aae114, aae113, aae011, aae036
                     FROM tc0a WHERE atc0a0 >= 10000001"""
        val args = mutableListOf<Any>()
        if (aae002 != null) { sql += " AND aae002=?"; args += aae002 }
        if (!aae114.isNullOrBlank()) { sql += " AND aae114=?"; args += aae114 }
        return jdbc.queryForList("$sql ORDER BY atc0a0 DESC LIMIT 200", *args.toTypedArray())
    }

    /** 未申报单位清单(AA97_DW) */
    @GetMapping("/dw")
    fun dw(@RequestParam aae001: String, @RequestParam aaa030: Long,
           @RequestParam aaa020: String, @RequestParam line: String): List<Map<String, Any?>> {
        if (!Regex("^\\d{4}$").matches(aae001)) return emptyList()
        val xz = lineOf(line)?.first ?: return emptyList()
        return jdbc.queryForList(
            """SELECT fid, aae001, atb930, atb931, aab001, aab004, aaa100, aaa101,
                      aaa030, aaa031, aae011, aae019, xz, psum
               FROM aa97_dw WHERE aae001=? AND aaa030=? AND aaa100=? AND xz=?
               ORDER BY atb930 NULLS LAST LIMIT 500""",
            aae001.toLong(), aaa030, aaa020, xz)
    }

    /** 未申报名册(AA97_GR, 按清单 FID 展开) */
    @GetMapping("/gr")
    fun gr(@RequestParam fid: Long): List<Map<String, Any?>> = jdbc.queryForList(
        """SELECT cid, fid, aae001, atb930, atb931, aac001, aac003, aac002,
                  bxzt, aaa000, xz, atc900
           FROM aa97_gr WHERE fid=? ORDER BY aac003 NULLS LAST LIMIT 500""", fid)
}
