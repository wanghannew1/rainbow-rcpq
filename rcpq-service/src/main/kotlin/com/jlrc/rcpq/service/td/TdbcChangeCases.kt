package com.jlrc.rcpq.service.td

import com.jlrc.rcpq.entity.AA10
import com.jlrc.rcpq.entity.AC01
import com.jlrc.rcpq.entity.TC90
import com.jlrc.rcpq.entity.TC90_LIST
import com.jlrc.rcpq.entity.TC92
import com.jlrc.rcpq.entity.TC92_LIST
import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

/**
 * td/b/c 合同变更用例（旧 TdbcService.saveUpdate/changeJbxx 规格重写）。
 *
 * 语义：
 * - saveUpdate：TC92 变更申请落库 → 18 个变更后字段回填 TC90 → 逐字段审计写 TC92_LIST
 *   （字段中文名映射硬编码；代码类经 AA10 翻译为中文，日期/文本直译，null→"(空)"）→
 *   TC90_LIST 最新历史行(max ikey)同步 18 字段。
 * - changeJbxx：人员基本信息(AC01)变更同步到在执行合同(TC90/TC90_LIST 各约30字段)。
 *
 * 原码缺陷修正：旧循环 `tc90 = (TC90)tc90List.get(0)` 恒取首元素——按意图改为逐行同步。
 * 操作人上下文：试点固定测试用户。
 */
@Component
class TdbcChangeCases(
    private val objectMapper: ObjectMapper,
) {
    @PersistenceContext
    lateinit var em: EntityManager

    /** 字段名 → (中文段名, 审计类别 Hz(代码类别)/Dt(日期直译)) */
    private val AUDIT_MAP = linkedMapOf(
        "atc909_" to Triple("用工方式", "Hz", "ATC909"),
        "atc90a_" to Triple("合同类别", "Hz", "ATC90A"),
        "atc90c_" to Triple("合同开始日期", "Dt", null),
        "atc90d_" to Triple("合同终止日期", "Dt", null),
        "atc907_" to Triple("工时制度", "Hz", "ATC907"),
        "atc908_" to Triple("岗位", "Dt", null),
        "atc905_" to Triple("转正劳动报酬", "Dt", null),
        "atc90aa_" to Triple("有无试用期", "Hz", "ATC90AA"),
        "atc90ab_" to Triple("试用期开始时间", "Dt", null),
        "atc90ac_" to Triple("试用期截止时间", "Dt", null),
        "atc90ad_" to Triple("试用期劳动报酬", "Dt", null),
        "atc90ae_" to Triple("转正日期", "Dt", null),
        "atc90ah_" to Triple("是否签订退休三方协议", "Hz", "ATC90AH"),
        "atc90aj_" to Triple("退休三方协议类别", "Hz", "ATC90AJ"),
        "atc90ak_" to Triple("退休时间", "Dt", null),
        "atc90al_" to Triple("签订退休三方协议日期", "Dt", null),
        "atc90am_" to Triple("是否收管理费", "Hz", "ATC90AM"),
        "atc90an_" to Triple("代办退休服务费", "Dt", null),
        "aae013_" to Triple("合同备注", "Dt", null),
    )

    /** TC92(带_后缀) → TC90 回填的 18 个字段 */
    private val FILL_FIELDS = AUDIT_MAP.keys.filter { it != "aae013_" }

    /** 合同变更保存（saveUpdate） */
    @Transactional
    fun saveUpdate(tc92Row: Map<String, Any?>, tc90Row: Map<String, Any?>): Map<String, Any?> {
        val atc900 = (tc92Row["atc900"] ?: tc90Row["atc900"]
            ?: throw IllegalArgumentException("缺少 atc900")).toString().toLong()
        val tc90 = em.find(TC90::class.java, atc900)
            ?: throw IllegalArgumentException("合同不存在: $atc900")

        // 1) TC92 变更申请落库(atb910/atc900 非空列, 缺省取合同的 TB91 关联号)
        val tc92 = toEntity<TC92>(tc92Row)
        if (tc92.atb910 == null) tc92.atb910 = tc90.atb910
        if (tc92.atc900 == null) tc92.atc900 = atc900
        // 原值快照必须在 merge 之前(em.find 返回受管实例, merge 后即被新值覆盖)
        val origSnapshot: Map<String, Any?> = if (tc92.atc920 != null) {
            em.find(TC92::class.java, tc92.atc920)?.let { entityToMap(it) } ?: emptyMap()
        } else emptyMap()
        if (tc92.atc920 == null) em.persist(tc92) else em.merge(tc92)
        em.flush()

        // 2) 变更后字段回填 TC90（原文 18 项）
        for (f in FILL_FIELDS) {
            copyProp(tc92, tc90, f, f.removeSuffix("_"))
        }
        em.merge(tc90)

        // 3) 逐字段审计（行值 vs 原值快照）
        val current = toEntity<TC92>(tc92Row)
        var audited = 0
        for ((field, meta) in AUDIT_MAP) {
            val (shName, kind, codeType) = meta
            val before = origSnapshot[field]?.toString()
            val after = strProp(current, field)
            if (before == after) continue
            val expres = when (kind) {
                "Hz" -> translateHz(codeType!!, before, after)
                else -> describeDt(shName, before, after)
            }
            val l = TC92_LIST()
            l.atc900 = atc900
            l.atc920 = tc92.atc920
            l.bgzd = shName
            l.bgh = after
            l.bgq = before
            l.aae013 = expres
            l.bgsj = java.time.LocalDate.now()
            l.aae036 = LocalDateTime.now()
            applyUser(l)
            em.persist(l)
            audited++
        }

        // 4) TC90_LIST 最新历史行同步 18 字段（原意修复：逐行）
        val maxIkey = em.createQuery("select max(t.ikey) from TC90_LIST t where t.atc900 = :atc900", Long::class.java)
            .setParameter("atc900", atc900).singleResult
        if (maxIkey != null) {
            val hist = em.find(TC90_LIST::class.java, maxIkey)
            if (hist != null) {
                for (f in FILL_FIELDS) copyProp(tc92, hist, f, f.removeSuffix("_"))
                em.merge(hist)
            }
        }
        return mapOf("atc900" to atc900, "auditedFields" to audited)
    }

    /** 人员信息变更同步在执行合同（changeJbxx，与 AzbaService.changeJbxx 同语义） */
    @Transactional
    fun changeJbxx(aac001: Long): Map<String, Any?> {
        val ac01 = em.find(AC01::class.java, aac001)
            ?: throw IllegalArgumentException("人员不存在: $aac001")
        var n = 0
        val tc90s = em.createQuery(
            "select t from TC90 t where t.aae100='1' and t.atb90r='3' and t.aac001=:aac001", TC90::class.java)
            .setParameter("aac001", aac001).resultList
        for (t in tc90s) {
            syncPerson(ac01, t); em.merge(t); n++
        }
        val lists = em.createQuery(
            "select t from TC90_LIST t where t.aae100='1' and t.atb90r='3' and t.aac001=:aac001", TC90_LIST::class.java)
            .setParameter("aac001", aac001).resultList
        for (t in lists) {
            syncPersonList(ac01, t); em.merge(t); n++
        }
        return mapOf("aac001" to aac001, "synced" to n)
    }

    // ---------- 助手 ----------

    /** getHz 等价：AA10 代码表翻译 "字段: 旧中文 --> 新中文" */
    private fun translateHz(aaa100: String, before: String?, after: String?): String {
        fun name(code: String?): String? {
            if (code.isNullOrEmpty()) return null
            return em.createQuery(
                "select t.aaa103 from AA10 t where t.aaa100 = :cat and t.aaa102 = :code", String::class.java)
                .setParameter("cat", aaa100).setParameter("code", code)
                .resultList.firstOrNull() ?: code
        }
        val b = before?.takeIf { it.isNotEmpty() }?.let { name(it) } ?: "(空)"
        val a = after?.takeIf { it.isNotEmpty() }?.let { name(it) } ?: "(空)"
        return "$b --> $a"
    }

    /** getDt 等价："字段: 旧 --> 新"（null→"(空)"） */
    private fun describeDt(sh: String, before: String?, after: String?): String {
        val b = before?.takeIf { it.isNotEmpty() } ?: "(空)"
        val a = after?.takeIf { it.isNotEmpty() } ?: "(空)"
        return "$sh: $b --> $a"
    }


    private fun entityToMap(e: TC92): Map<String, Any?> {
        val m = mutableMapOf<String, Any?>()
        for (f in AUDIT_MAP.keys) m[f] = strProp(e, f)
        return m
    }

    @Suppress("UNCHECKED_CAST")
    private inline fun <reified T : Any> toEntity(row: Map<String, Any?>): T =
        objectMapper.convertValue(row.filterKeys { it != "__state" }, T::class.java)

    private fun strProp(obj: Any, name: String): String? = try {
        obj.javaClass.getMethod("get${name.replaceFirstChar { it.uppercaseChar() }}").invoke(obj)?.toString()
    } catch (_: Exception) {
        null
    }

    private fun copyProp(src: Any, dst: Any, srcProp: String, dstProp: String = srcProp) {
        val v = try {
            src.javaClass.getMethod("get${srcProp.replaceFirstChar { it.uppercaseChar() }}").invoke(src)
        } catch (_: Exception) {
            return
        }
        val setter = dst.javaClass.methods.firstOrNull {
            it.name == "set${dstProp.replaceFirstChar { c -> c.uppercaseChar() }}"
        } ?: return
        try {
            setter.invoke(dst, v)
        } catch (_: Exception) {
        }
    }

    private fun applyUser(l: TC92_LIST) {
        l.aae011 = "9999"
        l.aae017 = "测试用户"
        l.aae019 = "00000000"
        l.aae020 = "彩虹人才试点"
        l.aae022 = "120114"
    }

    /** AC01→TC90 同步字段（原文 27 项 + 尾部覆盖 3 项，与 AzCases.SYNC_FIELDS 同源） */
    private fun syncPerson(ac01: AC01, t: TC90) {
        for (f in AzSync.SYNC) copyProp(ac01, t, f)
        copyProp(ac01, t, "aae005", "aae00c")
        copyProp(ac01, t, "atc902", "aae006")
        copyProp(ac01, t, "aae009", "aae009")
        copyProp(ac01, t, "bbb001", "bbb001")
    }

    private fun syncPersonList(ac01: AC01, t: TC90_LIST) {
        for (f in AzSync.SYNC) copyProp(ac01, t, f)
        copyProp(ac01, t, "aae005", "aae00c")
        copyProp(ac01, t, "atc902", "aae006")
        copyProp(ac01, t, "aae009", "bbb001")   // 原文: aae009 最终取 bbb001
        copyProp(ac01, t, "aae005", "aae00c")
        copyProp(ac01, t, "aae00p", "aae00p")
        copyProp(ac01, t, "aac013", "aac013")
    }

    /** 同步字段清单（与 AzCases.SYNC_FIELDS 同源，集中一处） */
    object AzSync {
        val SYNC = listOf(
            "aac005", "aac009", "aac024", "aac033", "aac017", "aac007", "aae00d", "aae008", "atc90r",
            "aab299", "bab305", "aac025", "aac011", "aac181", "aac182", "aac180", "aac015", "aac184",
            "aca111", "aca112", "aac185", "aac014", "bac014", "aae00a", "aac013", "aae00p", "aae009",
        )
    }
}
