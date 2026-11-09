package com.jlrc.rcpq.service.td

import com.fasterxml.jackson.databind.ObjectMapper
import com.jlrc.rcpq.entity.TB91
import com.jlrc.rcpq.entity.TC90
import com.jlrc.rcpq.entity.TC90_BG
import com.jlrc.rcpq.entity.TC9E
import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

/**
 * td/b/f 审核处理（旧 tdbfService 163L 规格重写，6 方法）。
 *
 * 方法映射: doXinqCommit/doXinqCancle(新签审核通过/撤销) / doRenewCancle(续签撤销) /
 * doJsdyCancle(结算单元调转撤销) / terminateCancel(终止处理) / reCommit(终止重新提交)。
 * 关键语义:
 * - doXinqCommit: 变更列逐列写 TC90_BG(atc900,columnname) → 保存 TC90/TB91
 *   （原 PojoEntity.getModifyColName 以"载荷 vs 库中现值"逐列 diff 等价）
 * - doXinqCancle: 删 TC90(atc900) + 删 TB91(atb910)——新签待审行无归档，硬删
 * - doRenewCancle: 删续签新行(atb910 关联) → 旧行按 TB91.atb900 逗号清单复活(atb90r='3',aae100='1') → 删 TB91
 * - doJsdyCancle: 撤销调转：TC90.atb90r 回 '3'（atb910 关联行）→ 删 TB91
 * - terminateCancel: 保存带终止字段的 TC90 → 删 TB91 → 删 AC10(aac109='2',atc9e0) → 删 TC9E
 * - reCommit: 保存 TC90/TB91 → 删旧 TC9E(atb910 关联)及其 AC10(aac109='2') →
 *   PKG_RCPQ_FZ.PRC_GENERATE_TFMX(atc900, user, atb910, 起~止年月, atc90as, atc90m) 重新生成明细，appCode<1 抛 -30032
 */
@Component
class TdbfAuditCases(
    private val jdbcTemplate: JdbcTemplate,
    private val objectMapper: ObjectMapper,
) {
    @PersistenceContext
    lateinit var em: EntityManager

    @Suppress("UNCHECKED_CAST")
    private inline fun <reified T : Any> toEntity(row: Map<String, Any?>): T =
        objectMapper.convertValue(row.filterKeys { it != "__state" }, T::class.java)

    // ---------- doXinqCommit 新签审核通过 ----------

    @Transactional
    fun doXinqCommit(tc90Row: Map<String, Any?>, tb91Row: Map<String, Any?>): Map<String, Any?> {
        val tc90 = toEntity<TC90>(tc90Row)
        val tb91 = toEntity<TB91>(tb91Row)
        val atc900 = tc90.atc900 ?: throw IllegalArgumentException("缺少 atc900")
        // 原语义: 修改列逐列记 TC90_BG（页面上被改动的字段名清单）
        val logged = mutableListOf<String>()
        val current = em.find(TC90::class.java, atc900)
        if (current != null) {
            for ((k, v) in tc90Row) {
                if (k.startsWith("__")) continue
                val getter = TC90::class.java.methods.firstOrNull {
                    it.name == "get${k.replaceFirstChar { c -> c.uppercaseChar() }}" && it.parameterCount == 0
                } ?: continue
                val before = runCatching { getter.invoke(current) }.getOrNull()
                val after = when (before) {
                    null -> v
                    is Long -> v?.toString()?.toLongOrNull()
                    is java.math.BigDecimal -> v?.toString()?.toBigDecimalOrNull()
                    else -> v?.toString()
                }
                if (after != before) {
                    val bg = TC90_BG()
                    bg.atc900 = atc900
                    bg.columnname = k
                    em.persist(bg)
                    logged.add(k)
                }
            }
        }
        if (current == null) em.persist(tc90) else { applyRow(current, tc90Row); em.merge(current) }
        if (tb91.atb910 == null) em.persist(tb91) else em.merge(tb91)
        return mapOf("atc900" to atc900, "bgColumns" to logged)
    }

    /** 通用行值应用: 反射按属性名赋值（类型自动转换），等价 saveOrUpdate 的脏字段落库 */
    private fun applyRow(target: Any, row: Map<String, Any?>) {
        for ((k, v) in row) {
            if (k.startsWith("__")) continue
            val setter = target.javaClass.methods.firstOrNull {
                it.name == "set${k.replaceFirstChar { c -> c.uppercaseChar() }}" && it.parameterCount == 1
            } ?: continue
            val pt = setter.parameterTypes[0]
            val converted: Any? = when {
                v == null -> null
                pt == java.lang.Long::class.java -> v.toString().toLongOrNull() ?: continue
                pt == java.math.BigDecimal::class.java -> v.toString().toBigDecimalOrNull() ?: continue
                pt == java.time.LocalDate::class.java -> runCatching { java.time.LocalDate.parse(v.toString().take(10)) }.getOrNull() ?: continue
                pt == java.time.LocalDateTime::class.java ->
                    runCatching { java.time.LocalDateTime.parse(v.toString().replace(" ", "T").take(19)) }.getOrNull() ?: continue
                else -> v.toString()
            }
            try {
                setter.invoke(target, converted)
            } catch (_: Exception) {
            }
        }
    }

    // ---------- doXinqCancle 新签撤销 ----------

    @Transactional
    fun doXinqCancle(tc90Row: Map<String, Any?>, tb91Row: Map<String, Any?>): Map<String, Any?> {
        val atc900 = toEntity<TC90>(tc90Row).atc900 ?: throw IllegalArgumentException("缺少 atc900")
        val atb910 = toEntity<TB91>(tb91Row).atb910 ?: throw IllegalArgumentException("缺少 atb910")
        em.createQuery("delete from TC90 t where t.atc900 = :i").setParameter("i", atc900).executeUpdate()
        em.createQuery("delete from TB91 t where t.atb910 = :i").setParameter("i", atb910).executeUpdate()
        return mapOf("atc900" to atc900, "atb910" to atb910, "cancelled" to true)
    }

    // ---------- doRenewCancle 续签撤销 ----------

    @Transactional
    fun doRenewCancle(tb91Row: Map<String, Any?>): Map<String, Any?> {
        val tb91 = toEntity<TB91>(tb91Row)
        val atb910 = tb91.atb910 ?: throw IllegalArgumentException("缺少 atb910")
        // 1. 删续签新行（本次续签生成、atb910 关联）
        val delNew = em.createQuery("delete from TC90 t where t.atb910 = :i")
            .setParameter("i", atb910).executeUpdate()
        // 2. 旧行复活: atb900 为逗号分隔旧协议号清单(原文 in (...))
        var revived = 0
        val oldIds = (tb91Row["atb900"]?.toString() ?: "")
            .split(",").mapNotNull { it.trim().toLongOrNull() }
        for (oldId in oldIds) {
            revived += jdbcTemplate.update("UPDATE tc90 SET atb90r='3', aae100='1' WHERE atc900=?", oldId)
        }
        // 3. 删审核单
        em.createQuery("delete from TB91 t where t.atb910 = :i").setParameter("i", atb910).executeUpdate()
        return mapOf("atb910" to atb910, "deletedNew" to delNew.toInt(), "revivedOld" to revived)
    }

    // ---------- doJsdyCancle 结算单元调转撤销 ----------

    @Transactional
    fun doJsdyCancle(tb91Row: Map<String, Any?>): Map<String, Any?> {
        val tb91 = toEntity<TB91>(tb91Row)
        val atb910 = tb91.atb910 ?: throw IllegalArgumentException("缺少 atb910")
        val n = jdbcTemplate.update("UPDATE tc90 SET atb90r='3' WHERE atb910=?", atb910)
        em.createQuery("delete from TB91 t where t.atb910 = :i").setParameter("i", atb910).executeUpdate()
        return mapOf("atb910" to atb910, "restored" to n)
    }

    // ---------- terminateCancel 终止处理 ----------

    @Transactional
    fun terminateCancel(tc90Row: Map<String, Any?>, tb91Row: Map<String, Any?>, tc9eRow: Map<String, Any?>): Map<String, Any?> {
        val tc90 = toEntity<TC90>(tc90Row)
        val atc900 = tc90.atc900 ?: throw IllegalArgumentException("缺少 atc900")
        val atb910 = toEntity<TB91>(tb91Row).atb910 ?: throw IllegalArgumentException("缺少 atb910")
        val tc9eId = toEntity<TC9E>(tc9eRow).atc9e0 ?: throw IllegalArgumentException("缺少 atc9e0")
        val current = em.find(TC90::class.java, atc900) ?: throw IllegalArgumentException("合同不存在: $atc900")
        applyRow(current, tc90Row)
        em.merge(current)
        em.createQuery("delete from TB91 t where t.atb910 = :i").setParameter("i", atb910).executeUpdate()
        val delAc10 = jdbcTemplate.update("DELETE FROM ac10 WHERE aac109='2' AND atc9e0=?", tc9eId)
        val delTc9e = em.createQuery("delete from TC9E t where t.atc9e0 = :i")
            .setParameter("i", tc9eId).executeUpdate()
        em.flush()
        return mapOf("atc900" to atc900, "ac10Deleted" to delAc10, "tc9eDeleted" to delTc9e.toInt())
    }

    // ---------- reCommit 终止重新提交 ----------

    @Transactional
    fun reCommit(tc90Row: Map<String, Any?>, tb91Row: Map<String, Any?>, tc9eRow: Map<String, Any?>): Map<String, Any?> {
        val tc90 = toEntity<TC90>(tc90Row)
        val tb91 = toEntity<TB91>(tb91Row)
        val newTc9e = toEntity<TC9E>(tc9eRow)
        val atc900 = tc90.atc900 ?: throw IllegalArgumentException("缺少 atc900")
        val atb910 = tb91.atb910 ?: throw IllegalArgumentException("缺少 atb910")
        val start = newTc9e.aae001 ?: throw IllegalArgumentException("TC9E 缺少 aae001(起年月)")
        val end = newTc9e.aae002 ?: throw IllegalArgumentException("TC9E 缺少 aae002(止年月)")
        val current = em.find(TC90::class.java, atc900) ?: throw IllegalArgumentException("合同不存在: $atc900")
        applyRow(current, tc90Row)
        em.merge(current)
        if (tb91.atb910 == null) em.persist(tb91) else em.merge(tb91)
        // 旧 TC9E(atb910 关联)及其 AC10(aac109='2') 先删
        val oldTc9e = em.createQuery("select t from TC9E t where t.atb910 = :i", TC9E::class.java)
            .setParameter("i", atb910).resultList.firstOrNull()
        if (oldTc9e != null) {
            jdbcTemplate.update("DELETE FROM ac10 WHERE aac109='2' AND atc9e0=?", oldTc9e.atc9e0)
            em.remove(oldTc9e)
            em.flush()
        }
        // PRC_GENERATE_TFMX(atc900, user.aae011, atb910, start_, end_, atc90as, atc90m)
        val after = em.find(TC90::class.java, atc900)!!
        val rc = generateTfmx(atc900, "9999", atb910, start, end, after.atc90as, after.atc90m)
        if (rc.appCode < 1) throw IllegalStateException("PRC_GENERATE_TFMX: ${rc.errMsg} (code=${rc.appCode})")
        return mapOf("atc900" to atc900, "atb910" to atb910, "tfmx" to rc)
    }

    data class ProcResult(val appCode: Int, val errMsg: String?, val detail: Map<String, Any?> = emptyMap())

    /**
     * PKG_RCPQ_FZ.PRC_GENERATE_TFMX 等价实现（终止重新生成明细）。
     * TODO-PLSQL: 备份库源码待人工评审比对（旧包 PKG_RCPQ_FZ 259 行）。
     */
    private fun generateTfmx(
        atc900: Long, aae011: String, atb910: Long,
        start: Long, end: Long, atc90as: String?, atc90m: String?,
    ): ProcResult {
        throw UnsupportedOperationException("PRC_GENERATE_TFMX 源码待备份库导出后翻译")
    }
}
