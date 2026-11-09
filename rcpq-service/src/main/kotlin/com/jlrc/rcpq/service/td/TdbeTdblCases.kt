package com.jlrc.rcpq.service.td

import com.jlrc.rcpq.entity.TB91
import com.jlrc.rcpq.entity.TB93
import com.jlrc.rcpq.entity.TC90
import com.jlrc.rcpq.entity.TC90_LIST
import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

/** td/b/e 单位间转移申请 + td/b/l 结算单元调转归档（旧 tdbeService/tdblService 规格重写） */
@Component
class TdbeTdblCases {
    @PersistenceContext
    lateinit var em: EntityManager

    /**
     * 单位间转移申请（tdbeService.update）：atc900s 为待转移合同号清单（原 "$" 分割）。
     * 每个合同: atb90r="2"(转移待审核) + 新建 TB91(atb911="24", atb912="2", 申请说明) + 回写 atb910。
     */
    @Transactional
    fun transferApply(
        atc900s: List<Long>,
        aab001: String,
        aab004: String,
        time: String,
    ): Map<String, Any?> {
        val results = mutableListOf<Map<String, Any?>>()
        for (atc900 in atc900s) {
            val tc90 = em.find(TC90::class.java, atc900)
                ?: throw IllegalArgumentException("合同不存在: $atc900")
            tc90.atb90r = "2"
            val tb91 = TB91()
            tb91.atb91z = atc900
            tb91.atb911 = "24"
            tb91.atb912 = "2"
            tb91.atb913 = "${tc90.aac003}于$time 申请从${tc90.aab004_1}调转至$aab004,请审核"
            tb91.aae011 = "9999"
            tb91.aae017 = "测试用户"
            tb91.aae019 = "00000000"
            tb91.aae036 = LocalDateTime.now()
            tb91.aae022 = "120114"
            tb91.aae020 = "彩虹人才试点"
            tb91.atb900 = aab001
            em.persist(tb91)
            em.flush()
            tc90.atb910 = tb91.atb910
            em.merge(tc90)
            results.add(mapOf("atc900" to atc900, "atb910" to tb91.atb910))
        }
        return mapOf("transferred" to results, "targetUnit" to aab004)
    }

    /**
     * 结算单元调转归档（tdblService.update）：TB91 置 atb912="2"；按 TB93 批次将每个合同
     * 生成 TC90_LIST 归档行（aae100="0"/atb90r="2"，含批次 5 字段覆盖）。
     */
    @Transactional
    fun archiveForTransfer(atc900s: List<Long>, atb930: String, tb91Row: Map<String, Any?>?): Map<String, Any?> {
        val tb91 = toEntity<TB91>(tb91Row ?: emptyMap())
        tb91.atb912 = "2"
        if (tb91.atb910 == null) em.persist(tb91) else em.merge(tb91)
        em.flush()
        val tb93 = em.createQuery("select t from TB93 t where t.atb930 = :atb930", TB93::class.java)
            .setParameter("atb930", atb930).resultList.firstOrNull()
            ?: throw IllegalArgumentException("TB93 批次不存在: $atb930")
        var n = 0
        for (atc900 in atc900s) {
            val tc90 = em.find(TC90::class.java, atc900)
                ?: throw IllegalArgumentException("合同不存在: $atc900")
            val l = copyToList(tc90, tb91.atb910)
            // 原文覆盖: aae100="0"/atb90r="2" 已含; 批次 5 字段
            l.atb900 = tb93.atb900
            l.atb930 = tb93.atb930
            l.atc90x = tb93.atb931
            l.bbb002 = tb93.bbb002
            l.atb90ab = tb93.atb90ab
            l.atb90ad = tb93.atb90ad
            em.persist(l)
            n++
        }
        return mapOf("archived" to n, "atb930" to atb930, "atb910" to tb91.atb910)
    }

    /** TC90 → TC90_LIST 反射全字段复制（等价原 copyProperty ~150 字段） */
    private fun copyToList(tc90: TC90, atb910: Long?): TC90_LIST {
        val dst = TC90_LIST()
        for (m in tc90.javaClass.methods.filter { it.name.startsWith("get") && it.parameterCount == 0 }) {
            val n = m.name.removePrefix("get")
            val prop = n[0].lowercaseChar() + n.substring(1)
            val setter = TC90_LIST::class.java.methods.firstOrNull {
                it.name == "set${prop.replaceFirstChar { c -> c.uppercaseChar() }}" && it.parameterCount == 1
            } ?: continue
            try {
                setter.invoke(dst, m.invoke(tc90))
            } catch (_: Exception) {
            }
        }
        dst.atb910 = atb910
        dst.aae100 = "0"      // 原文覆盖: 历史行非在执行
        dst.atb90r = "2"
        return dst
    }

    @Suppress("UNCHECKED_CAST")
    private inline fun <reified T : Any> toEntity(row: Map<String, Any?>): T =
        com.fasterxml.jackson.databind.ObjectMapper().convertValue(
            row.filterKeys { it != "__state" }, T::class.java)
}
