package com.jlrc.rcpq.service.td

import com.fasterxml.jackson.databind.ObjectMapper
import com.jlrc.rcpq.entity.TB91
import com.jlrc.rcpq.entity.TC90
import com.jlrc.rcpq.entity.TC9E
import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal

/**
 * td/b/a 新建合同用例（旧 tdbaService.update/doCommit 规格重写）。
 *
 * 语义：
 * - 保存：TC90 落库(序列生成合同号) → TB91 日志关联(atb91z=合同号) →
 *   AC01 人员档案回写社保/公积金编号(bbb001/bbb002) → TC90.atb910 关联；
 *   保险标志 atc90ai/atc90at="1" 时生成/更新 TC9E 保险计算明细，
 *   应缴合计 atc9eh = 个人+单位+其他 三档组合。
 * - 提交：状态置 atb90r="2"/atb912="2"（待审核）。
 */
@Component
class TdbContractCases(
    private val jdbcTemplate: JdbcTemplate,
    private val objectMapper: ObjectMapper,
) {
    @PersistenceContext
    lateinit var em: EntityManager

    /** 合同保存（tdbaService.update） */
    @Transactional
    fun saveContract(tc90Row: Map<String, Any?>, tb91Row: Map<String, Any?>, tc9eRow: Map<String, Any?>?): Map<String, Any?> {
        val tc90 = toEntity<TC90>(tc90Row)
        val tb91 = toEntity<TB91>(tb91Row)
        val isNew = tc90.atc900 == null
        if (isNew) {
            em.persist(tc90)
            em.flush() // SEQ_TC90_ATC900 生成合同号
            tb91.atb91z = tc90.atc900
            em.persist(tb91)
            em.flush()
            // 人员档案回写社保/公积金编号
            jdbcTemplate.update(
                "UPDATE ac01 SET bbb001=?, bbb002=? WHERE aac001=?",
                tc90.bbb001, tc90.bbb002, tc90.aac001)
            tc90.atb910 = tb91.atb910
            em.merge(tc90)
        } else {
            em.merge(tc90)
            em.merge(tb91)
            em.flush()
        }
        // TC9E 保险计算明细（双标志三档）
        val ai = tc90.atc90ai
        val at = tc90.atc90at
        if (isNew) {
            if (ai == "1" || at == "1") {
                val tc9e = toEntity<TC9E>(tc9eRow ?: emptyMap())
                fillIdentifiers(tc9e, tc90, tb91.atb910)
                tc9e.atc9eh = calcTotal(tc9e, ai, at)
                em.persist(tc9e)
            }
        } else {
            if (ai == "0" && at == "0") {
                tc90.atb910?.let { b910 ->
                    jdbcTemplate.update("DELETE FROM tc9e WHERE atb910=? AND atc900=?", b910, tc90.atc900)
                }
            } else if (ai == "1" || at == "1") {
                val tc9e = toEntity<TC9E>(tc9eRow ?: emptyMap())
                if (tc9e.atc9e0 != null) {
                    tc9e.atc9eh = calcTotal(tc9e, ai, at)
                    em.merge(tc9e)
                } else {
                    fillIdentifiers(tc9e, tc90, tb91.atb910)
                    tc9e.atc9eh = calcTotal(tc9e, ai, at)
                    em.persist(tc9e)
                }
            }
        }
        return mapOf("atc900" to tc90.atc900, "atb910" to tb91.atb910, "isNew" to isNew)
    }

    /** 提交（tdbaService.doCommit）：atb90r/atb912 置 "2" 待审核 */
    @Transactional
    fun commitContract(tc90Row: Map<String, Any?>, tb91Row: Map<String, Any?>): Map<String, Any?> {
        val atc900 = (tc90Row["atc900"] ?: throw IllegalArgumentException("缺少 atc900")).toString().toLong()
        val tc90 = em.find(TC90::class.java, atc900) ?: throw IllegalArgumentException("合同不存在: $atc900")
        tc90.atb90r = "2"
        em.merge(tc90)
        tb91Row["atb910"]?.let { b910 ->
            val tb91 = em.find(TB91::class.java, b910.toString().toLong())
            if (tb91 != null) {
                tb91.atb912 = "2"
                em.merge(tb91)
            }
        }
        return mapOf("atc900" to atc900, "atb90r" to "2", "atb912" to "2")
    }

    /** 标识字段复制（原文 9 项） */
    private fun fillIdentifiers(tc9e: TC9E, tc90: TC90, atb910: Long?) {
        tc9e.aac001 = tc90.aac001
        tc9e.aac002 = tc90.aac002
        tc9e.aac003 = tc90.aac003
        tc9e.atb930 = tc90.atb930
        tc9e.atb900 = tc90.atb900
        tc9e.aab001 = tc90.aab001
        tc9e.aab004 = tc90.aab004
        tc9e.atc900 = tc90.atc900
        tc9e.atb910 = atb910
    }

    /** 应缴合计三档：双1=个人+单位+其他；仅个人=个人+单位；仅单位=其他 */
    private fun calcTotal(tc9e: TC9E, ai: String?, at: String?): BigDecimal {
        val baa = tc9e.baa000 ?: BigDecimal.ZERO
        val bba = tc9e.bba000 ?: BigDecimal.ZERO
        val er = tc9e.atc9er ?: BigDecimal.ZERO
        return when {
            ai == "1" && at == "1" -> baa.add(bba).add(er)
            ai == "1" -> baa.add(bba)
            at == "1" -> er
            else -> BigDecimal.ZERO
        }
    }

    @Suppress("UNCHECKED_CAST")
    private inline fun <reified T : Any> toEntity(row: Map<String, Any?>): T =
        objectMapper.convertValue(row.filterKeys { it != "__state" }, T::class.java)
}
