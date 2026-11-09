package com.jlrc.rcpq.service.td

import com.jlrc.rcpq.entity.TB90
import com.jlrc.rcpq.entity.TB91
import com.jlrc.rcpq.entity.TB93
import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

/**
 * td/a 派遣协议用例（业务链起点）。
 * 语义来源：旧 tdaaService.doSave/doCommit + tdabService.doContinue（规格重写）。
 * 测试用户同 az（TODO 接入 Spring Security 后替换）。
 */
@Component
class TdaProtocolCases {
    @PersistenceContext
    lateinit var em: EntityManager

    private val TEST = mapOf(
        "aae011" to "9999", "aae017" to "测试用户",
        "aae019" to "00000000", "aae020" to "彩虹人才试点", "aae022" to "120114",
    )

    /** 协议保存（tdaaService.doSave）：保存 TB90；TB91 无主键则新建日志并回写关联号 */
    @Transactional
    fun saveAgreement(tb90Row: Map<String, Any?>, tb91Row: Map<String, Any?>): Map<String, Any?> {
        val tb90 = toEntity<TB90>(tb90Row)
        val tb91 = toEntity<TB91>(tb91Row)
        if (tb90.atb900 == null) em.persist(tb90) else em.merge(tb90)
        em.flush() // 使 SEQ_TB90_ATB900 生成协议号
        if (tb91.atb910 == null) {
            tb91.atb91z = tb90.atb900
            tb91.atb912 = "1"
            em.persist(tb91)
            em.flush()
            tb90.atb910 = tb91.atb910
            em.merge(tb90)
        } else {
            em.merge(tb91)
        }
        return mapOf("atb900" to tb90.atb900, "atb910" to tb90.atb910)
    }

    /** 协议提交（tdaaService.doCommit）：更新协议/日志；无批次时创建 TB93 派遣批次（状态 02/1） */
    @Transactional
    fun commitAgreement(
        tb90Row: Map<String, Any?>,
        tb91Row: Map<String, Any?>,
        aab004: String?,
        aab998: String?,
    ): Map<String, Any?> {
        val tb90 = toEntity<TB90>(tb90Row)
        val tb91 = toEntity<TB91>(tb91Row)
        if (tb90.atb900 == null) em.persist(tb90) else em.merge(tb90)
        if (tb91.atb910 == null) em.persist(tb91) else em.merge(tb91)
        val atb900 = tb90.atb900 ?: throw IllegalArgumentException("缺少协议号 atb900")
        val existing = em.createQuery(
            "select t from TB93 t where t.atb900 = :atb900", TB93::class.java)
            .setParameter("atb900", atb900).resultList
        var tb93Created = false
        if (existing.isEmpty()) {
            val tb93 = TB93()
            tb93.atb900 = atb900
            tb93.atb900_1 = atb900
            tb93.aab001 = tb90.aab001
            tb93.atb931 = aab004
            tb93.atb90ad = tb90.atb90ad
            tb93.aab998 = aab998
            tb93.atb934 = "02"
            tb93.atb935 = "1"
            tb93.atb90ac = tb90.atb90ac
            tb93.atb90y = tb90.atb90y
            tb93.atb90aa = tb90.atb90aa
            tb93.atb90ab = tb90.atb90ab
            tb93.atb90a = tb90.atb90a
            tb93.atb90b = tb90.atb90b
            tb93.atb90z = tb90.atb90z
            tb93.atb90c = tb90.atb90c
            tb93.aae011 = TEST["aae011"]
            tb93.aae017 = TEST["aae017"]
            tb93.aae020 = TEST["aae020"]
            tb93.aae019 = TEST["aae019"]
            tb93.aae022 = TEST["aae022"]
            tb93.aae036 = LocalDateTime.now()
            em.persist(tb93)
            tb93Created = true
        }
        return mapOf("atb900" to atb900, "tb93Created" to tb93Created)
    }

    /** 协议续签（tdabService.doContinue）：新协议落库；TB91 建新旧关联；TB93 批次迁移到新协议号；
     *  atb90w='1' 时同步 8 个批次字段到 atb900_1=原协议号 的历史行 */
    @Transactional
    fun continueAgreement(
        newRow: Map<String, Any?>,
        tmpRow: Map<String, Any?>,
        tb91Row: Map<String, Any?>,
        atb90w: String?,
    ): Map<String, Any?> {
        val tb90new = toEntity<TB90>(newRow)
        val tb91 = toEntity<TB91>(tb91Row)
        val oldAtb900 = (tmpRow["atb900"] ?: throw IllegalArgumentException("缺少原协议号 atb900")).toString().toLong()
        val oldTb90 = em.find(TB90::class.java, oldAtb900)
            ?: throw IllegalArgumentException("原协议不存在: $oldAtb900")
        tb90new.atb90w = null // 原文: 续签标志清空
        if (oldTb90 != null) em.merge(oldTb90)
        if (tb90new.atb900 == null) em.persist(tb90new) else em.merge(tb90new)
        em.flush()
        tb91.atb91z = tb90new.atb900
        tb91.atb900 = oldAtb900.toString()
        if (tb91.atb910 == null) em.persist(tb91) else em.merge(tb91)
        em.flush()
        tb90new.atb910 = tb91.atb910
        em.merge(tb90new)
        // 批次迁移: TB93.atb900 旧→新
        em.createQuery("update TB93 t set t.atb900 = :newId where t.atb900 = :oldId")
            .setParameter("newId", tb90new.atb900)
            .setParameter("oldId", oldAtb900)
            .executeUpdate()
        if (atb90w == "1") {
            em.createQuery(
                """update TB93 t set t.atb90ac = :ac, t.atb90y = :y, t.atb90aa = :aa, t.atb90ab = :ab,
                   t.atb90a = :a, t.atb90b = :b, t.atb90z = :z, t.atb90ad = :ad
                   where t.atb900_1 = :oldId""".trimIndent())
                .setParameter("ac", tb90new.atb90ac)
                .setParameter("y", tb90new.atb90y)
                .setParameter("aa", tb90new.atb90aa)
                .setParameter("ab", tb90new.atb90ab)
                .setParameter("a", tb90new.atb90a)
                .setParameter("b", tb90new.atb90b)
                .setParameter("z", tb90new.atb90z)
                .setParameter("ad", tb90new.atb90ad)
                .setParameter("oldId", oldAtb900)
                .executeUpdate()
        }
        return mapOf(
            "newAtb900" to tb90new.atb900,
            "oldAtb900" to oldAtb900,
            "atb910" to tb90new.atb910,
        )
    }

    @Suppress("UNCHECKED_CAST")
    private inline fun <reified T : Any> toEntity(row: Map<String, Any?>): T =
        com.fasterxml.jackson.databind.ObjectMapper().convertValue(
            row.filterKeys { it != "__state" }, T::class.java)
}
