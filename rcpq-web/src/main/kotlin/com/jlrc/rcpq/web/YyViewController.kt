package com.jlrc.rcpq.web

import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

/**
 * 异议处理查询面（W6 前端, 配合 PKG_保险人员增减.PRC_异议处理/异议离职同步/医保异议同步）。
 * 页码 wb-dsf-tip: tdcf(44) 反馈侧 · tdcg(45) 转入侧 · tdcad 医保异议同步入口。
 * 只读; 旧系统 tdcf/tdcg doQuery 首步隐式跑 PRC_异议离职同步(前端包装为查询前触发)。
 */
@RestController
@RequestMapping("/wb/yy")
class YyViewController(private val jdbc: JdbcTemplate) {

    /** TC90_YY 异议列表(状态过滤可组合; 旧 tdcf 条件=cbsign='2' AND bae011=经办) */
    @GetMapping("/list")
    fun list(@RequestParam(required = false) backsign: String?,
             @RequestParam(required = false) insign: String?,
             @RequestParam(required = false) cbsign: String?,
             @RequestParam(required = false) safesign: String?,
             @RequestParam(required = false) bae011: String?): List<Map<String, Any?>> {
        var sql = """SELECT tc90yyid, safesign, aae002, atc900, aac001, aac003, aac002,
                            atb930, atb931, aab004, aaa310, aaa020, aaa021,
                            backsign, insign, cbsign, aae013, aae011, aae111
                     FROM tc90_yy WHERE tc90yyid >= 10000001"""
        val args = mutableListOf<Any>()
        if (!backsign.isNullOrBlank()) { sql += " AND backsign=?"; args += backsign }
        if (!insign.isNullOrBlank()) { sql += " AND insign=?"; args += insign }
        if (!cbsign.isNullOrBlank()) { sql += " AND cbsign=?"; args += cbsign }
        if (!safesign.isNullOrBlank()) { sql += " AND safesign=?"; args += safesign }
        if (!bae011.isNullOrBlank()) { sql += " AND bae011=?"; args += bae011 }
        return jdbc.queryForList("$sql ORDER BY tc90yyid DESC LIMIT 200", *args.toTypedArray())
    }
}
