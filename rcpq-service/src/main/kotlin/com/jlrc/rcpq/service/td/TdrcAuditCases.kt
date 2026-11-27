package com.jlrc.rcpq.service.td

import com.jlrc.rcpq.service.proc.ProcHandler
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Component

/**
 * PKG_人才派遣.PRC_员工终止审核_批量 补移植（桥单 021，BC-01 缺口腿；2026-11-27）。
 * 源 = work/pkg_all/PACKAGE_BODY_PKG_人才派遣.sql:61~170，逐行忠实转写。
 *
 * 语义（源码权威——任务单括号"不翻状态"与源码不符，源码 L102 `UPDATE TC90 SET ATB90R='5'`
 * 明确翻状态，按铁律1 以源码为准）：对主键串(逗号分隔 TB91.ATB910)逐行——
 *   ①取 TB91(ATB911='23')行→取 TC90(ATC900=ATB91Z)行，任一取不到/多行→静默跳过该行(appCode 维持 1)
 *   ②TC90.ATB90R='5'
 *   ③TC90_LIST 终止字段回填(AAE100='0'/ATB90R='5'+14 个终止字段自 TC90 拷贝, WHERE 在保行)
 *   ④TB91 补记审核五要素+ATB912='3'+AAE013 清空
 * 经办人查询非单行 → -1"获取经办人信息出错"；主键串含非数字 → 外层 -1 审核出错。
 */
@Component
class TdrcAuditCases(private val jdbcTemplate: JdbcTemplate) {

    /** PRC_员工终止审核_批量：PRM_ATB910 主键串/PRM_AAE011 经办人（源 L61-170） */
    fun auditTerminateBatch(prmAtb910: String, prmAae011: String): Map<String, Any?> {
        return try {
            // SELECT * INTO 单行语义: 非单行(0/多)均异常 → -1（源 L84-91）
            val users = jdbcTemplate.query(
                "SELECT aae011, aae017, aae019, aae020, aae022 FROM sys_org_user_unit WHERE aae011=?",
                { rs, _ -> arrayOf(rs.getString("aae011"), rs.getString("aae017"), rs.getString("aae019"),
                    rs.getString("aae020"), rs.getString("aae022")) },
                prmAae011)
            if (users.size != 1) return mapOf("code" to -1, "msg" to "获取经办人信息出错")
            val user = users[0]
            // 主键串切分: REGEXP_SUBSTR '[^,]+' 跳过空段; 非数字段→NUMBER 表赋值异常→外层 -1（源 L93-97）
            val tab = prmAtb910.split(",").mapNotNull { it.trim().takeIf { s -> s.isNotEmpty() } }
                .map { it.toLong() }
            for (id in tab) {
                try {
                    // 单行取数: 0/多行 → 静默跳过（源 INNER EXCEPTION → RAISE E_EXCEPTION → 行内 WHEN → 继续）
                    val tb91s = jdbcTemplate.query(
                        "SELECT atb910, atb91z FROM tb91 WHERE atb911='23' AND atb910=?",
                        { rs, _ -> Pair(rs.getLong("atb910"), (rs.getObject("atb91z") as Number).toLong()) }, id)
                    if (tb91s.size != 1) continue
                    val (atb910, atb91z) = tb91s[0]
                    val tc90s = jdbcTemplate.queryForList(
                        "SELECT * FROM tc90 WHERE atc900=?", atb91z)
                    if (tc90s.size != 1) continue
                    val tc90 = tc90s[0]
                    // ② TC90.ATB90R='5'（源 L102）
                    jdbcTemplate.update("UPDATE tc90 SET atb90r='5' WHERE atc900=?", atb91z)
                    // ③ TC90_LIST 终止字段回填（源 L104-129, WHERE 在保行 aae100='1'/atb90r='3'）
                    val lst = jdbcTemplate.update(
                        """UPDATE tc90_list SET aae100='0', atb90r='5',
                           aaa202=?, aaa502=?, atc90i=?, aae003=?, atc90k=?, atc90l=?, atc90av=?,
                           atc90n=?, atc90o=?, atc90au=?, atc90as=?,
                           bae011=?, bae017=?, bae036=?, bae019=?, bae020=?, bae022=?
                           WHERE aae100='1' AND atb90r='3' AND atc900=?""",
                        tc90["aaa202"], tc90["aaa502"], tc90["atc90i"], tc90["aae003"],
                        tc90["atc90k"], tc90["atc90l"], tc90["atc90av"], tc90["atc90n"],
                        tc90["atc90o"], tc90["atc90au"], tc90["atc90as"],
                        tc90["bae011"], tc90["bae017"], tc90["bae036"], tc90["bae019"],
                        tc90["bae020"], tc90["bae022"], atb91z)
                    // ④ TB91 补记审核五要素+ATB912='3'+AAE013 清空（源 L131-140）
                    jdbcTemplate.update(
                        """UPDATE tb91 SET atb912='3', aae013=NULL,
                           bae011=?, bae017=?, bae036=now(), bae019=?, bae020=?, bae022=?
                           WHERE atb910=?""",
                        user[0], user[1], user[3], user[4], user[2], atb910)
                } catch (e: Exception) {
                    // 源行内 WHEN E_EXCEPTION THEN P_APCODE := 1 —— 单行异常静默继续（忠实）
                }
            }
            mapOf("code" to 1, "msg" to null as String?)
        } catch (e: NumberFormatException) {
            mapOf("code" to -1, "msg" to "审核出错，错误信息：ORA-0172 无效数字(${e.message})，请将错误信息发给维护工程师")
        } catch (e: Exception) {
            mapOf("code" to -1, "msg" to "审核出错，错误信息：${e.message}，请将错误信息发给维护工程师")
        }
    }
}

/** /proc 注册面: PKG_人才派遣.PRC_员工终止审核_批量（110→111, 桥单 021） */
@org.springframework.stereotype.Component
class TdrcAuditProc(private val c: TdrcAuditCases) : ProcHandler {
    override fun procName() = "PKG_人才派遣.PRC_员工终止审核_批量"
    override fun handle(params: Map<String, Any?>): Map<String, Any?> {
        // 传参与旧侧签名对齐(PRM_ATB910/PRM_AAE011 小写); 兼容裸列名键
        val atb910 = params["prm_atb910"]?.toString() ?: params["atb910"]?.toString()
            ?: return mapOf("appCode" to -1, "errMsg" to "缺少 prm_atb910")
        val aae011 = params["prm_aae011"]?.toString() ?: params["aae011"]?.toString()
            ?: return mapOf("appCode" to -1, "errMsg" to "缺少 prm_aae011")
        return c.auditTerminateBatch(atb910, aae011)
    }
}
