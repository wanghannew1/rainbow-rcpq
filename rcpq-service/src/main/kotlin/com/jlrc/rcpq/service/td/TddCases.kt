package com.jlrc.rcpq.service.td

import com.fasterxml.jackson.databind.ObjectMapper
import com.jlrc.rcpq.entity.AA80
import com.jlrc.rcpq.entity.AA81
import com.jlrc.rcpq.entity.AA90
import com.jlrc.rcpq.entity.TC8Z
import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.util.concurrent.ConcurrentHashMap

/**
 * td/d 8a-lite: tdda/tddd/tddf 三服务合并(共 ~274L)。
 *
 * - tdda.update(atb900/atb930 CSV, rows): AA90 模板行克隆到多协议(每对协议 × 每模板行)
 * - tdda.updateAa90(rows): 修改行 = 新插入(不含主键) + 按 aaa900 删旧行(复制改删模式)
 * - tddd.update(tc8z rows): TC93.atc93r/atc93s 回写 + TC8Z 保存
 * - tddd.doSaveTc93: TC93 保存后调 PKG_人才派遣公共.PRC_SALARYCOMPUTESINGLE——
 *   【坏功能】备份库 user_procedures 无此过程 → 原 ProcedureDAOUtil 必报错, 忠实抛文档化异常
 * - tddf.doSaveAa80: 默认工资名册切换(aaa802='1' 时全表置 0);
 *   aaa800 非空分支旧 HQL bug('0' and aaa800<>x → SET 语法错误)忠实报错
 * - tddf.doSaveAa81: AA81 保存; 新增时按 AA90 的 (atb900,atb930) 组克隆工资名册行
 */
@Component
class TddCases(
    private val jdbcTemplate: JdbcTemplate,
    private val objectMapper: ObjectMapper,
) {
    @PersistenceContext
    lateinit var em: EntityManager

    private val colTypes = ConcurrentHashMap<String, String>()
    private fun typeOf(table: String, col: String): String = colTypes.computeIfAbsent("$table.$col") {
        jdbcTemplate.queryForObject(
            "SELECT data_type FROM information_schema.columns WHERE table_name=? AND column_name=?",
            String::class.java, table, col.lowercase()) ?: "character varying"
    }
    private fun bindCol(table: String, k: String, v: String): Any = when (typeOf(table, k)) {
        "numeric", "bigint", "integer", "smallint" -> v.toBigDecimalOrNull() ?: v.toLongOrNull() ?: 0L
        else -> v
    }

    // ---------- tdda.update: AA90 模板克隆到多协议 ----------

    @Transactional
    fun cloneTemplatesToAgreements(atb900Csv: String, atb930Csv: String, rows: List<Map<String, Any?>>): Map<String, Any?> {
        val atb900s = atb900Csv.split(",").filter { it.isNotBlank() }
        val atb930s = atb930Csv.split(",").filter { it.isNotBlank() }
        var cloned = 0
        for (j in atb900s.indices) {
            for (row in rows) {
                val tpl = objectMapper.convertValue(row.filterKeys { it != "__state" }, AA90::class.java)
                val aa90 = AA90()
                aa90.aaa800 = tpl.aaa800; aa90.aaa810 = tpl.aaa810
                aa90.aaa901 = tpl.aaa901; aa90.aaa902 = tpl.aaa902
                aa90.aae011 = tpl.aae011; aa90.aae017 = tpl.aae017
                aa90.aae019 = tpl.aae019; aa90.aae020 = tpl.aae020
                aa90.aae022 = tpl.aae022; aa90.aae036 = tpl.aae036
                aa90.atb900 = atb900s[j].toLong()
                aa90.atb930 = atb930s[j].toLong()
                aa90.acb208 = tpl.acb208
                em.persist(aa90)
                cloned++
            }
        }
        return mapOf("cloned" to cloned, "pairs" to atb900s.size)
    }

    // ---------- tdda.updateAa90: 复制改删 ----------

    @Transactional
    fun copyUpdateAa90(rows: List<Map<String, Any?>>): Map<String, Any?> {
        var updated = 0
        for (row in rows) {
            if (row["__state"]?.toString() == "modified" || row["aaa900"] != null) {
                val old = objectMapper.convertValue(row.filterKeys { it != "__state" }, AA90::class.java)
                val aa90 = AA90()
                aa90.atb900 = old.atb900; aa90.aaa901 = old.aaa901; aa90.aaa902 = old.aaa902
                aa90.aae011 = old.aae011; aa90.aae017 = old.aae017; aa90.aae019 = old.aae019
                aa90.aae020 = old.aae020; aa90.aae022 = old.aae022; aa90.aae036 = old.aae036
                aa90.atb930 = old.atb930; aa90.aaa810 = old.aaa810
                aa90.aaa800 = old.aaa800; aa90.acb208 = old.acb208
                em.persist(aa90)
                old.aaa900?.let {
                    jdbcTemplate.update("DELETE FROM aa90 WHERE aaa900=?", it)
                }
                updated++
            }
        }
        return mapOf("updated" to updated)
    }

    // ---------- tddd.update: TC93 结算标志回写 + TC8Z 保存 ----------

    @Transactional
    fun updateTc93Flags(rows: List<Map<String, Any?>>): Map<String, Any?> {
        var n = 0
        for (row in rows) {
            val tc8z = objectMapper.convertValue(row.filterKeys { it != "__state" }, TC8Z::class.java)
            val atc930 = tc8z.atc930 ?: continue
            jdbcTemplate.update(
                "UPDATE tc93 SET atc93r=?, atc93s=? WHERE atc930=?",
                tc8z.atc93r, tc8z.atc93s, atc930)
            em.merge(tc8z)
            n++
        }
        return mapOf("updated" to n)
    }

    // ---------- tddd.doSaveTc93: 保存后调单人工资计算(坏功能) ----------

    @Transactional
    fun doSaveTc93(tc93Row: Map<String, Any?>): Map<String, Any?> {
        val atc930 = tc93Row["atc930"]?.toString()?.toLongOrNull()
            ?: throw IllegalArgumentException("缺少 atc930")
        // TC93 整行按列类型回写(键 atc930)
        val fields = tc93Row.filterKeys { it != "__state" }
        for ((k, v) in fields) {
            if (k == "atc930") continue
            jdbcTemplate.update("UPDATE tc93 SET $k=? WHERE atc930=?",
                bindCol("tc93", k, v.toString()), atc930)
        }
        // 【坏功能第三例】PRC_SALARYCOMPUTESINGLE 备份库不存在 → 旧调用必抛 -30032
        throw IllegalStateException(
            "调用[PKG_人才派遣公共.PRC_SALARYCOMPUTESINGLE]出错：过程在备份库不存在(旧系统运行时同样报错)，TC93 已保存。")
    }

    // ---------- tddf.doSaveAa80: 默认名册切换 ----------

    @Transactional
    fun doSaveAa80(aa80Row: Map<String, Any?>): Map<String, Any?> {
        val aa80 = objectMapper.convertValue(aa80Row.filterKeys { it != "__state" }, AA80::class.java)
        if (aa80.aaa802 == "1") {
            if (aa80.aaa800 == null) {
                jdbcTemplate.update("UPDATE aa80 SET aaa802='0'")
                em.persist(aa80)
            } else {
                // 旧 HQL bug: SET 子句用 'and' 而非逗号 → 运行时语法错误, 忠实报错
                throw IllegalStateException("旧 tddf.doSaveAa80 HQL bug: \"set aaa802='0' and aaa800<>x\" SET 子句语法错误, 旧系统运行时同样报错")
            }
        } else {
            em.merge(aa80)
        }
        return mapOf("aaa800" to aa80.aaa800)
    }

    // ---------- tddf.doSaveAa81: AA81 保存 + AA90 克隆 ----------

    @Transactional
    fun doSaveAa81(rows: List<Map<String, Any?>>): Map<String, Any?> {
        var saved = 0
        var cloned = 0
        for (row in rows) {
            val aa81 = objectMapper.convertValue(row.filterKeys { it != "__state" }, AA81::class.java)
            if (aa81.aaa810 != null) {
                em.merge(aa81)
                saved++
            } else {
                em.persist(aa81)
                em.flush()
                saved++
                // 按 AA90 已有 (atb900, atb930) 组克隆新行
                val groups = jdbcTemplate.query(
                    "SELECT atb900::text || '$' || atb930::text FROM aa90 WHERE aaa800=? GROUP BY atb900, atb930",
                    { rs, _ -> rs.getString(1) }, aa81.aaa800)
                for (g in groups) {
                    val parts = g.split("$")
                    val aa90 = AA90()
                    aa90.aaa901 = aa81.aaa901; aa90.aaa902 = aa81.aaa902
                    aa90.aae011 = aa81.aae011; aa90.aae017 = aa81.aae017
                    aa90.aae036 = aa81.aae036; aa90.aae019 = aa81.aae019
                    aa90.aae020 = aa81.aae020; aa90.aae022 = aa81.aae022
                    aa90.atb900 = parts[0].toLong()
                    aa90.atb930 = parts.getOrNull(1)?.toLongOrNull()
                    aa90.aaa810 = aa81.aaa810
                    aa90.aaa800 = aa81.aaa800
                    aa90.acb208 = aa81.acb208
                    em.persist(aa90)
                    cloned++
                }
            }
        }
        return mapOf("saved" to saved, "cloned" to cloned)
    }
}
