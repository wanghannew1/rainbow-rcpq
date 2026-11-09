package com.jlrc.rcpq.service.td

import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate

/**
 * td/b/o 合同起始日期变更（旧 tdboAjax.doAjax 44L 规格重写，tdbo_dialog_mod.jsp 直调通道）。
 *
 * 原语义（HQL 逐句等价）:
 * - TC90.atc90i = 新日期；aae013 备注 = substr(aae013||';<经办人>:<旧日期>',1,500)
 *   —— 追加审计备注截 500 字符（Oracle NULL 串接为 ''，PG 需 COALESCE）
 * - TC90_LIST 仅最新归档行(ikey=max)同步 atc90i
 * - 返回 "1"；异常 -30032
 */
@Component
class TdboAjaxCase(
    private val jdbcTemplate: JdbcTemplate,
) {
    @Transactional
    fun doAjax(atc900: Long, atc90d: String, atc90dOld: String, aae019: String = "00000000"): Map<String, Any?> {
        val d = LocalDate.parse(atc90d.take(10))
        jdbcTemplate.update(
            """UPDATE tc90 SET atc90i=?,
               aae013 = left(COALESCE(aae013,'') || ';' || ? || ':' || ?, 500)
               WHERE atc900=?""".trimIndent(),
            d, aae019, atc90dOld, atc900)
        val n = jdbcTemplate.update(
            """UPDATE tc90_list SET atc90i=? WHERE ikey =
               (SELECT max(ikey) FROM tc90_list WHERE atc900=?)""".trimIndent(),
            d, atc900)
        return mapOf("result" to "1", "atc900" to atc900, "listRows" to n)
    }
}
