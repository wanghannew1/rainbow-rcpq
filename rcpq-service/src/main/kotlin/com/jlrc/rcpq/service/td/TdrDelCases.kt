package com.jlrc.rcpq.service.td

import com.jlrc.rcpq.service.proc.ProcHandler
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Component

/**
 * PKG_人才派遣.PRC_删除员工 补移植（桥单 029，BC-06 清场腿；2026-11-27）。
 * 源 = work/pkg_all/PACKAGE_BODY_PKG_人才派遣.sql:1798~1882，逐行忠实转写。
 *
 * 语义（源码权威）：
 *   ①守卫: TC93 存在 AAE214='1' 工资行 → -1 "不允许删除，原因是已经生成工资"
 *   ②守卫: TC90 按 ATC900 单行取数，0/多行 → -1 "不允许删除，原因是未查询到合同信息"
 *     （Oracle SELECT * INTO：NO_DATA_FOUND/TOO_MANY_ROWS 同路径；ATC900 主键下实际只走 0 行）
 *   ③社保基数回填（ATC9WC 非空时）：找同人(AAC002+AAC003)其他最新合同(MAX(ATC900)≠本号)
 *     的 TC90_LIST 基数三列(ATC90P/ATC90PA/ATC90W)；取数非单行(0/多行)→三列置空跳过回填
 *     （Oracle 行内 OTHERS 语义忠实：同人无他合同/TC90_LIST 多行均静默置空）；
 *     三列任一非空才 UPDATE 回填到那本合同
 *   ④TC9W/TC9W2/TC9W1/TC9U 增减守卫四段在源码体中被块注释禁用 → 忠实不实现
 *   ⑤物理删六表: TC90_LIST/TC90/TC93/TC08/AA97/tc90_yy WHERE ATC900=本号
 *
 * W-A 用户裁定叠加层(2026-11-26，非 Oracle 源语义)：删前把六表将删行整行 jsonb 备份到
 * 新侧专表 rcpq_del_backup(V21, 无 Oracle 对应→同步不触碰)；备份失败 → -1 阻断删除
 * （备份是裁定目的，失守即失去物理删安全网）。PRM_ATC900 非数字 → -1(Oracle 隐式
 * TO_NUMBER 的 ORA-0172 同判)。返回面附备份/删除行数明细键(新侧可观测性，不影响 appCode)。
 */
@Component
class TdrDelCases(private val jdbcTemplate: JdbcTemplate) {

    /** 删除顺序与源码 DELETE 语句顺序一致；备份同序（W-A） */
    private val tables = listOf("tc90_list", "tc90", "tc93", "tc08", "aa97", "tc90_yy")

    fun deleteEmployee(prmAtc900: String): Map<String, Any?> {
        val atc900 = prmAtc900.trim().toLongOrNull()
            ?: return mapOf("appCode" to -1, "errMsg" to "ORA-0172 无效数字: $prmAtc900")
        // ① 已生成工资守卫（源 L1806-1812）
        val nCnt = jdbcTemplate.queryForObject(
            "SELECT COUNT(1) FROM tc93 WHERE aae214='1' AND atc900=?", Long::class.java, atc900) ?: 0L
        if (nCnt > 0) {
            return mapOf("appCode" to -1, "errMsg" to "不允许删除，原因是已经生成工资")
        }
        // ② TC90 单行取数守卫（源 L1814-1823, SELECT * INTO 行语义）
        val tc90s = jdbcTemplate.queryForList("SELECT * FROM tc90 WHERE atc900=?", atc900)
        if (tc90s.size != 1) {
            return mapOf("appCode" to -1, "errMsg" to "不允许删除，原因是未查询到合同信息")
        }
        val tc90 = tc90s[0]
        // ③ 社保基数回填（源 L1825-1857；ATC9WC 非空门）
        var backfilled = 0
        if (tc90["atc9wc"] != null) {
            val aac003 = tc90["aac003"]?.toString()
            val aac002 = tc90["aac002"]?.toString()
            val rows = try {
                jdbcTemplate.queryForList(
                    """SELECT a.atc900, b.atc90p, b.atc90pa, b.atc90w
                       FROM tc90 a, tc90_list b
                       WHERE a.atc900 = b.atc900 AND a.atc900 <> ?
                         AND a.atc900 = (SELECT MAX(atc900) FROM tc90
                                         WHERE atc900 <> ? AND aac003 = ? AND aac002 = ?)""",
                    atc900, atc900, aac003, aac002)
            } catch (e: Exception) {
                emptyList<Map<String, Any?>>()
            }
            // SELECT * INTO 行内 OTHERS: 0/多行 → 三列置空(静默跳过回填)
            val r = rows.singleOrNull()
            val p = r?.get("atc90p"); val pa = r?.get("atc90pa"); val w = r?.get("atc90w")
            if (r != null && (p != null || pa != null || w != null)) {
                val rAtc900 = (r["atc900"] as Number).toLong()
                backfilled = jdbcTemplate.update(
                    "UPDATE tc90 SET atc90p=?, atc90pa=?, atc90w=? WHERE atc900=?",
                    p, pa, w, rAtc900)
            }
        }
        // W-A 备份层：任一表备份失败 → -1 阻断删除（裁定目的失守不放行）
        val backed = mutableMapOf<String, Int>()
        for (t in tables) {
            try {
                backed[t] = jdbcTemplate.update(
                    "INSERT INTO rcpq_del_backup (table_name, atc900, row_data) " +
                        "SELECT ?, ?, to_jsonb(x) FROM $t x WHERE atc900=?", t, atc900, atc900)
            } catch (e: Exception) {
                return mapOf("appCode" to -1,
                    "errMsg" to "删前备份失败($t): ${e.message}，已中止删除")
            }
        }
        // ⑤ 物理删六表（源 L1873-1878，顺序同源）
        val deleted = mutableMapOf<String, Int>()
        for (t in tables) {
            deleted[t] = jdbcTemplate.update("DELETE FROM $t WHERE atc900=?", atc900)
        }
        return mapOf("appCode" to 1, "errMsg" to null as String?,
            "backfilled" to backfilled, "backup" to backed, "deleted" to deleted)
    }
}

/** /proc 注册面: PKG_人才派遣.PRC_删除员工（桥单 029, /proc 111→112） */
@Component
class TdrDelProc(private val c: TdrDelCases) : ProcHandler {
    override fun procName() = "PKG_人才派遣.PRC_删除员工"
    override fun handle(params: Map<String, Any?>): Map<String, Any?> {
        // 传参与旧侧签名对齐(PRM_ATC900 小写); 兼容裸列名键
        val atc900 = params["prm_atc900"]?.toString() ?: params["atc900"]?.toString()
            ?: return mapOf("appCode" to -1, "errMsg" to "缺少 prm_atc900")
        return c.deleteEmployee(atc900)
    }
}
