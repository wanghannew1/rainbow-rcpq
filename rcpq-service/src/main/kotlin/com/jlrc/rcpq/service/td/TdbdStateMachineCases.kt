package com.jlrc.rcpq.service.td

import com.fasterxml.jackson.databind.ObjectMapper
import com.jlrc.rcpq.entity.AC05
import com.jlrc.rcpq.entity.AC10_JS
import com.jlrc.rcpq.entity.TB91
import com.jlrc.rcpq.entity.TB93
import com.jlrc.rcpq.entity.TC90
import com.jlrc.rcpq.entity.TC90_LIST
import com.jlrc.rcpq.entity.TC92
import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * td/b/d 合同状态机（旧 tdbdService 689L 规格重写，8 方法 → 7 用例 + 反射复制器）。
 *
 * 方法映射: copyProperty→copyTc90ToList(反射)/doNew/doJbbg/doRenewAll/doTerminateCancle/doJsdy/doBg/doCbSh。
 * 关键语义:
 * - doNew: atb90r="3"(审核通过)→TC90_LIST 归档+AC01 回写+AC05(类型1)；否则删 TC90_BG 变更行
 * - doJbbg: 身份证/姓名变更 → 9 表广播(AC01/TC90/TC90_LIST/TC9W/TC9U/TC9E/TC9T/TC9V/TC93)
 * - doRenewAll: atb912="3" → 旧协议按新行身份证匹配翻转(在执行→终止5 / 反向复活)
 * - doTerminateCancle: 清 17 个终止字段+复活(3/1)；atb911="27" 加删 TC93_TF；最后删 TB91 行
 * - doJsdy: atb912="3" → TC90_LIST 其他行终止+本行复活+TC90 换单位/批次；否则删 TC90_LIST
 * - doBg: TC90_LIST 在执行行 ~45 字段同步 + AC10_JS 社保/公积金基数新档
 * - doCbSh: atb912="3" → TC90 ~90 字段(含 aaa203 系条件组)+TC90_LIST 60 字段+TC8G/TC9E 回填
 *
 * 原码说明: 旧 doBg/doCbSh 大段 set 复制在 Kotlin 中以属性名反射等价实现（实体同源生成，属性名一致）。
 */
@Component
class TdbdStateMachineCases(
    private val jdbcTemplate: JdbcTemplate,
    private val objectMapper: ObjectMapper,
) {
    @PersistenceContext
    lateinit var em: EntityManager

    @Suppress("UNCHECKED_CAST")
    private inline fun <reified T : Any> toEntity(row: Map<String, Any?>): T =
        objectMapper.convertValue(row.filterKeys { it != "__state" }, T::class.java)

    /** TC90 → TC90_LIST 全字段反射复制（等价原 copyProperty ~150 字段） */
    private fun copyTc90ToList(tc90: TC90): TC90_LIST {
        val dst = TC90_LIST()
        for (m in tc90.javaClass.methods.filter { it.name.startsWith("get") && it.parameterCount == 0 }) {
            val prop = m.name.removePrefix("get").let { n ->
                n[0].lowercaseChar() + n.substring(1)
            }
            val setter = TC90_LIST::class.java.methods.firstOrNull {
                it.name == "set${prop.replaceFirstChar { c -> c.uppercaseChar() }}" && it.parameterCount == 1
            } ?: continue
            try {
                setter.invoke(dst, m.invoke(tc90))
            } catch (_: Exception) {
            }
        }
        return dst
    }

    /** 通用行值应用: 反射按属性名赋值(类型自动转换) */
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

    private fun String.toBigDecimalOrNull(): BigDecimal? = runCatching { BigDecimal(this) }.getOrNull()

    /** 测试用户 */
    private fun applyTestUser(target: Any) {
        val kv = mapOf(
            "aae011" to "9999", "aae017" to "测试用户", "aae019" to "00000000",
            "aae020" to "彩虹人才试点", "aae022" to "120114",
        )
        applyRow(target, kv)
    }

    // ---------- doNew 新建审核 ----------

    @Transactional
    fun doNew(tc90Row: Map<String, Any?>, tb91Row: Map<String, Any?>): Map<String, Any?> {
        val tc90 = toEntity<TC90>(tc90Row)
        val tb91 = toEntity<TB91>(tb91Row)
        if (tc90.atc900 == null) em.persist(tc90) else em.merge(tc90)
        if (tb91.atb910 == null) em.persist(tb91) else em.merge(tb91)
        var archived = false
        if (tc90.atb90r == "3") {
            val hist = copyTc90ToList(tc90)
            em.merge(hist)
            // AC01 回写 12 字段(decode null→'' 语义)
            if (tc90.aac001 != null) {
                jdbcTemplate.update(
                    """UPDATE ac01 SET aac005=COALESCE(?, ''), aac009=?, cab299=?, cab305=?, aae00b=COALESCE(?, ''),
                       aae00a=COALESCE(?, ''), aab001=?, id=?, aab004=?, aab001_1=?, id_1=?, aab004_1=?
                       WHERE aac001=?""".trimIndent(),
                    tc90.aac005, tc90.aac009, tc90.aab299, tc90.bab305, tc90.aae00b, tc90.aae00a,
                    tc90.aab001, tc90.id, tc90.aab004, tc90.aab001_1, tc90.id_1, tc90.aab004_1, tc90.aac001)
            }
            val ac05 = AC05()
            ac05.atc900 = tc90.atc900
            ac05.aac001 = tc90.aac001
            ac05.atb930 = tc90.atb930
            ac05.type = "1"
            ac05.aae002 = tc90.aae002
            em.merge(ac05)
            archived = true
        } else {
            tc90.atc900?.let {
                jdbcTemplate.update("DELETE FROM tc90_bg WHERE atc900=?", it)
            }
        }
        em.flush()
        return mapOf("atc900" to tc90.atc900, "atb90r" to tc90.atb90r, "archived" to archived)
    }

    // ---------- doJbbg 基本信息变更(9 表广播) ----------

    private val JBGG_TABLES = listOf("ac01", "tc90", "tc90_list")
    private val JBGG_TABLES_ID_ONLY = listOf("tc9w", "tc9u", "tc9e", "tc9t", "tc9v", "tc93")

    @Transactional
    fun doJbbg(tc92Row: Map<String, Any?>, tb91Row: Map<String, Any?>): Map<String, Any?> {
        val tc92 = toEntity<TC92>(tc92Row)
        val tb91 = toEntity<TB91>(tb91Row)
        val aac001 = tc92.aac001 ?: throw IllegalArgumentException("缺少 aac001")
        var updated = 0
        // 身份证变更(原语义: aac002 变 → 广播 aac002/aac004/aac006)
        val idBefore = em.createQuery("select t.aac002 from AC01 t where t.aac001=:i", String::class.java)
            .setParameter("i", aac001).singleResult
        if (tc92.aac002 != null && tc92.aac002 != idBefore) {
            // 原文: AC01/TC90/TC90_LIST 更新3列; TC9* 系只更新 aac002
            for (tb in JBGG_TABLES) {
                updated += jdbcTemplate.update(
                    "UPDATE $tb SET aac002=?, aac004=?, aac006=? WHERE aac001=?",
                    tc92.aac002, tc92.aac004, tc92.aac006, aac001)
            }
            for (tb in JBGG_TABLES_ID_ONLY) {
                updated += jdbcTemplate.update("UPDATE $tb SET aac002=? WHERE aac001=?", tc92.aac002, aac001.toString())
            }
        }
        // 姓名变更(广播 aac003)
        val nameBefore = em.createQuery("select t.aac003 from AC01 t where t.aac001=:i", String::class.java)
            .setParameter("i", aac001).singleResult
        if (tc92.aac003 != null && tc92.aac003 != nameBefore) {
            for (tb in JBGG_TABLES) {
                updated += jdbcTemplate.update("UPDATE $tb SET aac003=? WHERE aac001=?", tc92.aac003, aac001)
            }
        }
        if (tb91.atb910 == null) em.persist(tb91) else em.merge(tb91)
        return mapOf("aac001" to aac001, "updatedRows" to updated)
    }

    // ---------- doRenewAll 续签全部 ----------

    @Transactional
    fun doRenewAll(newRows: List<Map<String, Any?>>, tb91Row: Map<String, Any?>): Map<String, Any?> {
        val tb91 = toEntity<TB91>(tb91Row)
        var flipped = 0
        if (tb91.atb912 == "3") {
            // atb900 字段是逗号分隔的旧协议号清单(原文 in (...))
            val oldIds = (tb91Row["atb900"]?.toString() ?: "")
                .split(",").mapNotNull { it.trim().toLongOrNull() }
            val lastMonth = LocalDate.now().minusMonths(1).format(DateTimeFormatter.ofPattern("yyyyMM"))
            for (oldId in oldIds) {
                val oldTc90 = em.find(TC90::class.java, oldId) ?: continue
                val newRow = newRows.firstOrNull { it["aac002"]?.toString() == oldTc90.aac002 }
                if (newRow != null && newRow["aae100"]?.toString() == "1") {
                    oldTc90.aae100 = "0"; oldTc90.atb90r = "5"; oldTc90.aae003 = lastMonth.toLongOrNull()
                } else {
                    oldTc90.aae100 = "1"; oldTc90.atb90r = "3"; oldTc90.aae003 = null
                }
                em.merge(oldTc90)
                flipped++
            }
        }
        var saved = 0
        for (r in newRows) {
            val tc90 = toEntity<TC90>(r)
            if (tc90.atc900 == null) em.persist(tc90) else em.merge(tc90)
            saved++
        }
        if (tb91.atb910 == null) em.persist(tb91) else em.merge(tb91)
        return mapOf("flippedOld" to flipped, "savedNew" to saved)
    }

    // ---------- doTerminateCancle 终止撤销 ----------

    private val TERMINATE_FIELDS = listOf(
        "aaa202", "aaa502", "atc90i", "atc90k", "atc90j", "atc90l", "atc90av", "atc90o", "atc90n",
        "atc90au", "atc90as", "bae011", "bae017", "bae019", "bae020", "bae022", "bae036", "aae003",
    )

    @Transactional
    fun doTerminateCancle(tb91Id: Long, atb911: String?): Map<String, Any?> {
        val tb91 = em.find(TB91::class.java, tb91Id)
            ?: throw IllegalArgumentException("TB91 不存在: $tb91Id")
        val atc900 = tb91.atb91z ?: throw IllegalArgumentException("TB91 缺少 atb91z")
        val tc90 = em.find(TC90::class.java, atc900)
            ?: throw IllegalArgumentException("合同不存在: $atc900")
        for (f in TERMINATE_FIELDS) setFieldNull(tc90, f)
        tc90.atb90r = "3"
        tc90.aae100 = "1"
        em.merge(tc90)
        if (atb911 == "27") {
            jdbcTemplate.update("DELETE FROM tc93_tf WHERE atc900=?", atc900)
        }
        em.remove(tb91)
        return mapOf("atc900" to atc900, "revived" to true)
    }

    private fun copyProp(src: Any, dst: Any, prop: String) {
        val getter = src.javaClass.methods.firstOrNull {
            it.name == "get${prop.replaceFirstChar { c -> c.uppercaseChar() }}" && it.parameterCount == 0
        } ?: return
        val setter = dst.javaClass.methods.firstOrNull {
            it.name == "set${prop.replaceFirstChar { c -> c.uppercaseChar() }}" && it.parameterCount == 1
        } ?: return
        try {
            setter.invoke(dst, getter.invoke(src))
        } catch (_: Exception) {
        }
    }

    private fun setFieldNull(obj: Any, name: String) {
        val setter = obj.javaClass.methods.firstOrNull {
            it.name == "set${name.replaceFirstChar { c -> c.uppercaseChar() }}" && it.parameterCount == 1
        } ?: return
        try {
            setter.invoke(obj, null)
        } catch (_: Exception) {
        }
    }

    // ---------- doJsdy 结算单元调转 ----------

    @Transactional
    fun doJsdy(tb91Row: Map<String, Any?>, atb930Pair: String?): Map<String, Any?> {
        val tb91 = toEntity<TB91>(tb91Row)
        val pair = atb930Pair ?: tb91.atb900 ?: throw IllegalArgumentException("缺少 atb930")
        val parts = pair.split(";")
        val tb930 = parts.getOrNull(1) ?: throw IllegalArgumentException("atb930 格式应为 'x;y'")
        val tb93 = em.createQuery("select t from TB93 t where t.atb930 = :atb930", TB93::class.java)
            .setParameter("atb930", tb930).resultList.firstOrNull()
            ?: throw IllegalArgumentException("TB93 批次不存在: $tb930")
        var affected = 0
        if (tb91.atb912 == "3") {
            val lists = em.createQuery(
                "select t from TC90_LIST t where t.atb910 = :atb910", TC90_LIST::class.java)
                .setParameter("atb910", tb91.atb910).resultList
            for (l in lists) {
                val atc900 = l.atc900
                // 其他行终止(原语义: aae100=0/5 + 备注)
                jdbcTemplate.update(
                    """UPDATE tc90_list SET aae100='0', atb90r='5',
                       aae013 = COALESCE(aae013,'') || '|因结算单元调转而终止,调转时间' || to_char(now(),'yyyy/mm/dd')
                       WHERE aae100='1' AND atc900=? AND ikey<>?""".trimIndent(),
                    atc900, l.ikey)
                l.aae100 = "1"; l.atb90r = "3"
                em.merge(l)
                val ab01 = tb93.aab001?.let {
                    em.createQuery("select a from AB01 a where a.aab001 = :aab001", com.jlrc.rcpq.entity.AB01::class.java)
                        .setParameter("aab001", it).resultList.firstOrNull()
                }
                if (ab01 != null) {
                    jdbcTemplate.update(
                        "UPDATE tc90 SET aab001=?, aab004=?, atb930=?, atc90x=?, atb900=? WHERE atc900=?",
                        ab01.aab001, ab01.aab004, tb930, tb93.atb931, tb93.atb900, atc900)
                }
                affected++
            }
        } else {
            affected = jdbcTemplate.update("DELETE FROM tc90_list WHERE atb910=?", tb91.atb910)
        }
        if (tb91.atb910 == null) em.persist(tb91) else em.merge(tb91)
        return mapOf("affected" to affected, "tb930" to tb930)
    }

    // ---------- doBg 变更生效 ----------

    /** doBg 同步的 TC90→TC90_LIST 字段(原文 ~45 项, 反射按名复制) */
    private val BG_FIELDS = listOf(
        "aae005", "atc902", "aac009", "atc909", "atc90a", "atc90b", "atc90c", "atc90d", "atc908",
        "atc907", "atb90ab", "atc90aa", "atc905", "atc90ah", "atc90ab", "atc90ac", "atc90ad",
        "atc90ae", "atc90aj", "atc90ak", "atc90al", "atc90am", "atc90an", "atc90g", "atc90h",
        "aaa001", "aaa002", "aaa003", "aaa004", "aaa100", "aaa101", "aaa301", "aaa302", "aaa303",
        "aaa304", "aaa305", "aaa306", "aaa407", "atc904", "atc90r", "atb90ad", "bbb001", "bbb002",
        "atc90u",
    )

    @Transactional
    fun doBg(tc90Row: Map<String, Any?>, tb91Row: Map<String, Any?>): Map<String, Any?> {
        val tc90 = toEntity<TC90>(tc90Row)
        val tb91 = toEntity<TB91>(tb91Row)
        val atc900 = tc90.atc900 ?: throw IllegalArgumentException("缺少 atc900")
        // TC90_LIST 在执行行同步 45 字段
        val actives = em.createQuery(
            "select t from TC90_LIST t where t.aae100='1' and t.atb90r='3' and t.atc900=:atc900", TC90_LIST::class.java)
            .setParameter("atc900", atc900).resultList
        for (l in actives) {
            for (f in BG_FIELDS) copyProp(tc90, l, f)
            em.merge(l)
        }
        // AC10_JS 社保基数(tc90Row 带出 aaa301 时)
        tc90Row["aaa301"]?.let { v301 ->
            val amt = v301.toString().toBigDecimalOrNull() ?: BigDecimal.ZERO
            val curr = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMM")).toLong()
            jdbcTemplate.update("UPDATE ac10_js SET aae100='0' WHERE type='1' AND atc900=?", atc900)
            val js = AC10_JS()
            js.aac001 = tc90.aac001
            js.atc900 = atc900
            js.sxny = curr
            js.type = "1"
            js.aaa301 = amt; js.aaa302 = amt; js.aaa304 = amt; js.aaa305 = amt; js.aaa306 = amt
            js.aae100 = "1"
            em.persist(js)
        }
        // AC10_JS 公积金基数(aaa303)
        tc90Row["aaa303"]?.let { v303 ->
            val amt = v303.toString().toBigDecimalOrNull() ?: BigDecimal.ZERO
            val curr = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMM")).toLong()
            jdbcTemplate.update("UPDATE ac10_js SET aae100='0' WHERE type='2' AND atc900=?", atc900)
            val js = AC10_JS()
            js.aac001 = tc90.aac001
            js.atc900 = atc900
            js.sxny = curr
            js.type = "2"
            js.aaa303 = amt
            js.aae100 = "1"
            em.persist(js)
        }
        if (tc90.atc900 != null && tc90.atc900 == atc900) em.merge(tc90)
        if (tb91.atb910 == null) em.persist(tb91) else em.merge(tb91)
        return mapOf("atc900" to atc900, "syncedListRows" to actives.size)
    }

    // ---------- doCbSh 超龄审核 ----------

    /** doCbSh: TC92→TC90 主表字段(原文 ~90 项, 反射按名复制 + 条件组) */
    private val CBSH_TC90_FIELDS = listOf(
        "atc90g", "atc90h", "aaa001", "aaa002", "aaa005", "aaa004", "aac012", "atb90c", "atc90b",
        "atc90c", "atc90d", "aaa201", "aaa501", "aaa601", "aaa901", "aaa100", "aaa101", "aaa110",
        "aaa111", "aaa120", "aaa121", "aaa130", "aaa131", "aaa301", "aaa302", "aaa304", "aaa305",
        "aaa306", "aaa303", "atb90s", "aaa407", "aaa401", "aaa402", "aaa404", "atc904", "aaa406",
        "atc90r", "atc90ad", "bbb001", "bbb002", "atc90u", "atc90ah", "atc90aj", "atc90ak",
        "atc90al", "atc90am", "atc90an", "atc905", "atb90a", "atb90b", "aae005", "aae006",
        "atb90c", "aae00p", "aaa803", "aab004_1", "aae00b", "atc90av", "aaa202", "aaa502",
        "aaa602", "aaa902", "atc90p", "aaa208", "atc90pa", "aaa608", "atc90pb", "aaa908",
        "atc90w", "aaa508",
    )
    private val CBSH_LIST_FIELDS = listOf(
        "atc90b", "atc90c", "atc90d", "atc90g", "atc90h", "aaa001", "aaa002", "aaa005", "aaa004",
        "aaa201", "aaa501", "aaa601", "aaa203", "aaa503", "aaa603", "aaa100", "aaa101", "aaa110",
        "aaa111", "aaa120", "aaa121", "aaa301", "aaa302", "aaa304", "aaa305", "aaa306", "aaa303",
        "aaa407", "aaa401", "aaa402", "atb90s", "atc904", "aaa406", "atc90r", "atc90ad", "bbb001",
        "bbb002", "atc90u", "atc90ah", "atc90aj", "atc90ak", "atc90al", "atc90am", "atc90an",
        "atc905", "atb90a", "atb90b", "aae005", "aae006", "atb90c", "aae00p", "aaa803", "aab004_1",
        "aae00b", "atc90av", "aaa202", "aaa502", "aaa602",
    )

    @Transactional
    fun doCbSh(atc900: Long, tb91Row: Map<String, Any?>, tc92Row: Map<String, Any?>): Map<String, Any?> {
        val tb91 = toEntity<TB91>(tb91Row)
        val tc92 = toEntity<TC92>(tc92Row)
        if (tb91.atb912 == "3") {
            val tc90 = em.find(TC90::class.java, atc900)
                ?: throw IllegalArgumentException("合同不存在: $atc900")
            applyRow(tc90, tc92Row.filterKeys { it in CBSH_TC90_FIELDS.toSet() && tc92Row[it] != null })
            // 条件组: atc90p/pa/pb/w 非"0"时取申报值, 否则取原值
            val flag = { f: String -> tc92Row[f]?.toString() }
            val cond = { flagCol: String, newProp: String, oldProp: String ->
                if (flag(flagCol) != "0") tc92Row[newProp] else tc92Row[oldProp]
            }
            applyRow(tc90, mapOf(
                "aaa203" to cond("atc90p", "aaa201_", "aaa203_"),
                "aaa603" to cond("atc90pa", "aaa601_", "aaa603_"),
                "aaa903" to cond("atc90pb", "aaa901_", "aaa903_"),
                "aaa503" to cond("atc90w", "aaa501_", "aaa503_"),
                "aaa204" to tc92Row["aaa202_"], "aaa504" to tc92Row["aaa502_"],
                "aaa604" to tc92Row["aaa602_"], "aaa904" to tc92Row["aaa902_"],
                "aaa209" to tc92Row["aaa202_"], "aaa509" to tc92Row["aaa502_"],
                "aaa609" to tc92Row["aaa602_"], "aaa909" to tc92Row["aaa902_"],
            ))
            em.merge(tc90)
            // TC90_LIST 最新历史行 60 字段
            val maxIkey = em.createQuery(
                "select max(t.ikey) from TC90_LIST t where t.atc900 = :atc900", Long::class.java)
                .setParameter("atc900", atc900).singleResult
            if (maxIkey != null) {
                em.find(TC90_LIST::class.java, maxIkey)?.let { hist ->
                    applyRow(hist, tc92Row.mapKeys { it.key.removeSuffix("_") }
                        .filterKeys { it in CBSH_LIST_FIELDS.toSet() })
                    em.merge(hist)
                }
            }
            // TC8G bbb001 回填(原 null 时)
            val bbb001N = tc92Row["bbb001_"]?.toString()
            val bbb001O = tc92.bbb001
            if (bbb001N != null && bbb001O == null) {
                jdbcTemplate.update("UPDATE tc8g SET bbb001=? WHERE bbb001 IS NULL AND atc900=?", bbb001N, atc900)
            }
            val aaa004N = tc92Row["aaa004_"]?.toString()
            if (aaa004N != null) {
                jdbcTemplate.update("UPDATE tc9e SET atc9v1=? WHERE atc900=?", aaa004N, atc900)
            }
        }
        if (tb91.atb910 == null) em.persist(tb91) else em.merge(tb91)
        return mapOf("atc900" to atc900, "applied" to (tb91.atb912 == "3"))
    }
}
