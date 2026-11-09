package com.jlrc.rcpq.service.td

import com.jlrc.rcpq.service.proc.ProcHandler
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

/**
 * td/c/k 社保异动汇总回盘: CHECK_TC9W_HZ_IMP(312L) + COMM_TC9W_HZ_IMP(201L)。
 *
 * 【坏功能第七例】两过程的 TC9W 匹配/更新查询引用不存在的列 atc9w9/atc9wb/atc9wf/
 * atc9wh/atc9wi/atc9wj(tc9w 表 87 列镜像自 Oracle user_tab_columns, 均无)——
 * 旧 Hibernate HQL "could not resolve property" 运行时即抛 → 顶层 WHEN OTHERS
 * → appCode=-1。故整条汇总导入链在旧系统不可用(与 tdckAjax 同根, 见 8a)。
 * 本实现忠实保留: 证件清洗(产生于 HQL 之前, 事务内生效)后按旧向页面报错。
 * 表头互斥删除(DELETE 同 flag 其他批次)先于 HQL, 忠实生效。
 */
@Component
class TdcImpHzCheckComm(
    private val jdbcTemplate: JdbcTemplate,
    private val cases: TdcImpHzCases,
) {
    val ok = 1
    val err = -1

    /** PRC_CHECK_TC9W_HZ_IMP */
    @Transactional
    fun checkHzImp(ace100: String, flag: String, aae002: String): Map<String, Any?> {
        val ace100N = ace100.toLongOrNull()
            ?: return mapOf("code" to err, "msg" to "社保人员异动回盘校验出错，错误信息：批次号格式不正确")
        jdbcTemplate.update("DELETE FROM tc9w_hz WHERE flag=? AND ace100<>?", flag, ace100N)
        val rows = jdbcTemplate.query(
            "SELECT id, aac002, aac003 FROM tc9w_hz WHERE ace100=?" +
                (if (flag != "bz") " AND ace10a IS NULL" else ""),
            { rs, _ -> arrayOf<Any?>(rs.getLong("id"), rs.getString("aac002"), rs.getString("aac003")) },
            ace100N)
        var pass = 0
        for (rec in rows) {
            val aac002 = clean(rec[1] as String?)
            val aac003 = clean(rec[2] as String?)
            jdbcTemplate.update("UPDATE tc9w_hz SET aac002=?, aac003=? WHERE id=?", aac002, aac003, rec[0] as Long)
            val parsed = cases.parseAac002(aac002 ?: "")
            if ((parsed[0] as Int) != 1) {
                jdbcTemplate.update(
                    "UPDATE tc9w_hz SET ace10a='2', ace10b=? WHERE id=?",
                    "公民身份证号码不合法|", rec[0] as Long)
                continue
            }
            // 此后 TC9W 匹配 = 坏功能第七例, 忠实报错
            throw IllegalStateException(hzBrokenMessage("校验"))
        }
        return mapOf("code" to ok, "msg" to null as String?,
            "rows" to rows.size, "pass" to pass, "reject" to rows.size - pass)
    }

    /** PRC_COMM_TC9W_HZ_IMP */
    @Transactional
    fun commHzImp(ace100: String, flag: String, begin: String, end: String): Map<String, Any?> {
        val ace100N = ace100.toLongOrNull()
            ?: return mapOf("code" to err, "errMsg" to "社保人员异动回盘提交出错")
        val rows = jdbcTemplate.query(
            "SELECT id, aac002, aac003 FROM tc9w_hz WHERE ace10a='3' AND ace100=?",
            { rs, _ -> arrayOf<Any?>(rs.getLong("id"), rs.getString("aac002"), rs.getString("aac003")) },
            ace100N)
        for (rec in rows) {
            val aac002 = clean(rec[1] as String?)
            // TC9W 更新 = 坏功能第七例, 忠实报错
            throw IllegalStateException(hzBrokenMessage("提交"))
        }
        return mapOf("code" to ok, "errMsg" to null as String?, "batchRows" to rows.size)
    }

    private fun hzBrokenMessage(stage: String): String =
        "$stage 出错，错误信息：ORA-00904 ATC9W9/ATC9WB/ATC9WF 列不存在(TC9W 表 87 列镜像无此三列, " +
            "旧 Hibernate HQL 运行时同样抛 could not resolve property)，请将错误信息发给维护工程师"

    private fun clean(v: String?): String? =
        v?.replace("\r", "")?.replace("\n", "")?.replace("\t", "")?.replace("x", "X")?.replace(" ", "")
}

@Component
class TdcImpHzCheckImpProc(
    private val c: TdcImpHzCheckComm,
    private val cases: TdcImpHzCases,
) : ProcHandler {
    override fun procName() = "PKG_IMP_FZ.PRC_CHECK_TC9W_HZ_IMP"
    override fun handle(params: Map<String, Any?>): Map<String, Any?> {
        val r = c.checkHzImp(
            params["ace100"]?.toString() ?: return mapOf("appCode" to -1, "errMsg" to "缺少 ace100"),
            params["flag"]?.toString() ?: "bz",
            params["aae002"]?.toString() ?: "")
        return mapOf("appCode" to r["code"], "errMsg" to r["msg"],
            "rows" to r["rows"], "pass" to r["pass"], "reject" to r["reject"])
    }
}

@Component
class TdcImpHzCommImpProc(private val c: TdcImpHzCheckComm) : ProcHandler {
    override fun procName() = "PKG_IMP_FZ.PRC_COMM_TC9W_HZ_IMP"
    override fun handle(params: Map<String, Any?>): Map<String, Any?> {
        val r = c.commHzImp(
            params["ace100"]?.toString() ?: return mapOf("appCode" to -1, "errMsg" to "缺少 ace100"),
            params["flag"]?.toString() ?: "bz",
            params["begin"]?.toString() ?: return mapOf("appCode" to -1, "errMsg" to "缺少 begin"),
            params["end"]?.toString() ?: return mapOf("appCode" to -1, "errMsg" to "缺少 end"))
        return mapOf("appCode" to r["code"], "errMsg" to null as String?, "batchRows" to r["batchRows"])
    }
}
