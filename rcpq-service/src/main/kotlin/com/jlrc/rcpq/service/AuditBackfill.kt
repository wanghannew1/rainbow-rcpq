package com.jlrc.rcpq.service

import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Component

/**
 * DC save 框架审计回填移植（桥单 20261127-036-dc-audit-backfill；2026-11-27）。
 *
 * 考据（源码面）：旧 cp33.jar LemisPojoUtil.setPojoSysUserInfo(pojo)（L283-299）——
 *   baz001/baz002/aae011/aae017/aae019/aae020/aae022/aae036 八列 ← AppContext(登录人上下文)
 *   无条件 setProperty，前端传值被覆盖；aae013 等其他列不触碰。
 * 调用面：通用保存体 Update_table（LemisBusinessMgrPoJo.update 实现）与定制服务
 *   (tddfService 等) 均引用；javap 实证 Update_table 在实体赋值后调用。
 * 实证面（跨库对照）：
 *   - insert 行回填：BC-11 AA80/81(定制)/AA98/99(通用) 四审计列=经办人真值；定制通道 baz
 *     另回填会话发号器值，通用通道 baz 空（AppContext 无批号）。
 *   - modify 行不回填：BC-10 AC-02(旧侧 tddab 整行改存)后 aae017/019/020/022 仍空——
 *     保存框架只对新增行回填（BC-11 改后行四列保留自 insert 时回填值）。
 * 移植语义：新增行回填 aae011(登录名)/017/019/020/022(经办人映射)/036(now)，仅当表含该列；
 *   经办人查 sys_org_user_unit 单行，查无 → 不回填（降级=保持前端值，登录态缺失场景）。
 *   baz001/baz002 仅定制 tddf 通道回填（nextval，与旧实证对齐）。
 */
@Component
class AuditBackfill(private val jdbcTemplate: JdbcTemplate) {

    /** 登录人五件套(查无→null=降级不回填) */
    fun userRow(aae011: String?): Map<String, Any?>? {
        if (aae011.isNullOrBlank()) return null
        return jdbcTemplate.queryForList(
            "SELECT aae011, aae017, aae019, aae020, aae022 FROM sys_org_user_unit WHERE aae011=?",
            aae011).singleOrNull()
    }

    /** 新增行审计回填: 就表列集∩审计六列覆盖前端值; 返回是否回填发生。
     *  经办人身份源: 显式参数 → 行内 aae011/bae011(无登录态期 payload 即身份, 考据降级口径)。 */
    fun applyToInsert(row: MutableMap<String, Any?>, tableCols: Set<String>, aae011: String?): Boolean {
        val op = aae011 ?: row["aae011"]?.toString() ?: row["bae011"]?.toString()
        val user = userRow(op) ?: return false
        // aae036 用 ISO 串(实体 LocalDateTime, Jackson 形状; 与 e2e 旧侧 epoch 等价的时间语义)
        val stamp = java.time.LocalDateTime.now().withNano(0).toString()
        if ("aae011" in tableCols) row["aae011"] = user["aae011"]?.toString() ?: aae011
        if ("aae017" in tableCols) row["aae017"] = user["aae017"]?.toString()
        if ("aae019" in tableCols) row["aae019"] = user["aae019"]?.toString()
        if ("aae020" in tableCols) row["aae020"] = user["aae020"]?.toString()
        if ("aae022" in tableCols) row["aae022"] = user["aae022"]?.toString()
        if ("aae036" in tableCols) row["aae036"] = stamp
        return true
    }

    /** 定制通道 baz 发号器回填(旧实证仅 tddf 定制服务有) */
    fun applyBaz(row: MutableMap<String, Any?>, tableCols: Set<String>) {
        if ("baz001" in tableCols && row["baz001"] == null) {
            row["baz001"] = jdbcTemplate.queryForObject("SELECT nextval('seq_sys_baz001')", Long::class.java)
        }
        if ("baz002" in tableCols && row["baz002"] == null) {
            row["baz002"] = jdbcTemplate.queryForObject("SELECT nextval('seq_sys_baz002')", Long::class.java)
        }
    }
}
