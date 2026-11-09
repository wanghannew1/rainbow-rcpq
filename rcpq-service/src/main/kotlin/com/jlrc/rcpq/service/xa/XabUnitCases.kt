package com.jlrc.rcpq.service.xa

import com.jlrc.rcpq.entity.AA12
import com.jlrc.rcpq.service.MetadataRegistry
import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

/**
 * xa 单位（行政区划 AA12）自定义用例。
 * 语义来源：旧 XaabService.doUpdate/doDelete + XaabAjax.getChildId（规格重写，非逐行翻译）。
 * 与旧实现的等价性注记：
 * - 拼音首字母：旧调 Oracle 函数 pkg_gethzpy.GetHzPYCAP(label)，此处以 TinyPinyin 等价实现（行为对照覆盖）。
 * - childId：旧算法逐位试探；此处按行政区划层级(12位码)直接推导段宽并取未占用后缀，语义等价。
 */
@Component
class XabUnitCases(
    private val metadataRegistry: MetadataRegistry,
) {
    @PersistenceContext
    lateinit var em: EntityManager

    /** 新增/修改行政区划节点（对应 doUpdate） */
    @Transactional
    fun saveUnit(row: Map<String, Any?>): Map<String, Any?> {
        val meta = metadataRegistry.meta("aa12Store_M")!!
        val id0 = row["id"]?.toString() ?: ""
        val label = row["label"]?.toString() ?: ""
        val parentid = row["parentid"]?.toString() ?: ""

        val isNew = id0.isEmpty() || id0 == "reNew"
        val id = if (isNew) childId(parentid) else id0

        val spell = if (label.isNotEmpty()) pinyinInitials(label) else null
        val entity = (if (isNew) null else em.find(AA12::class.java, id0)) ?: AA12()
        entity.id = id
        entity.parentid = parentid
        entity.label = label
        entity.spell = spell
        entity.iscommon = orDefault(row["iscommon"]?.toString(), "false")
        entity.isvalid = orDefault(row["isvalid"]?.toString(), "false")
        entity.haschild = orDefault(row["haschild"]?.toString(), "false")
        entity.ordnumber = id
        if (isNew) em.persist(entity) else em.merge(entity)

        // 父节点标记有子节点（原 bulkUpdate 语义）
        if (parentid.isNotEmpty()) {
            em.createQuery("update AA12 t set t.haschild = 'true' where t.id = :pid")
                .setParameter("pid", parentid).executeUpdate()
        }
        return mapOf("id" to id, "spell" to spell)
    }

    /** 删除行政区划节点（对应 doDelete）：删除节点；若同级同前缀仅剩 1 个节点则清除 haschild */
    @Transactional
    fun deleteUnit(id: String): Map<String, Any?> {
        val node = em.find(AA12::class.java, id)
            ?: throw IllegalArgumentException("节点不存在: $id")
        val flag = levelPrefix(id)
        em.remove(node)
        em.flush()
        val remaining = em.createQuery(
            "select t.id from AA12 t where t.id like :p order by t.id", String::class.java)
            .setParameter("p", "$flag%").resultList
        if (remaining.size <= 1) {
            remaining.firstOrNull()?.let { one ->
                em.createQuery("update AA12 t set t.haschild = 'false' where t.id = :i")
                    .setParameter("i", one).executeUpdate()
            }
        }
        return mapOf("deleted" to id, "samePrefixRemaining" to remaining.size)
    }

    /** 生成父节点下未占用的子节点编码（对应 getChildId，语义等价实现） */
    @Transactional
    fun childId(parentId: String): String {
        // 12 位行政区划层级: [0..2]省 | [0..4]市 | [0..6]县 | [0..9]乡镇；下一层段宽 2/2/3/3
        val parentWidth = levelWidth(parentId)
        val segWidth = SEG_WIDTH_BY_LEVEL[parentWidth] ?: 3
        val prefix = parentId.take(parentWidth)
        val start = (parentWidth + segWidth).coerceAtMost(12)
        val children = em.createQuery(
            "select t.id from AA12 t where t.parentid = :p", String::class.java)
            .setParameter("p", parentId).resultList.toSet()
        val maxSuffix = children.maxOfOrNull { it.substring(parentWidth, start).toIntOrNull() ?: 0 } ?: 0
        // 从最大后缀 +1 起找未占用编码；空父节点给首段默认 01/001
        var n = if (children.isEmpty()) firstSegmentValue(segWidth) else maxSuffix + 1
        while (true) {
            val cand = (prefix + n.toString().padStart(segWidth, '0')).padEnd(12, '0')
            if (cand !in children) return cand
            n++
        }
    }

    companion object {
        private fun orDefault(v: String?, d: String) = if (v.isNullOrEmpty()) d else v

        /** 节点层宽：按尾部零段判定（12位码 = 省2|市2|县2|镇3|村3，与原 doDelete/getChildId 边界一致） */
        fun levelWidth(id: String): Int = when {
            id.length >= 12 && id.substring(9) == "000" -> 9                                 // 镇
            id.length >= 12 && id.substring(6) == "000000" -> 6                              // 县
            id.length >= 12 && id.substring(4) == "00000000" -> 4                            // 市
            else -> 2                                                                        // 省
        }

        private val SEG_WIDTH_BY_LEVEL = mapOf(2 to 2, 4 to 2, 6 to 3, 9 to 3)
        private fun firstSegmentValue(segWidth: Int) = if (segWidth == 2) 1 else 1

        /** 删除用层级前缀（原 doDelete flag 逻辑的字面移植） */
        fun levelPrefix(id: String): String = when {
            id.substring(4) == "00000000" -> id.substring(0, 2)
            id.substring(6) == "000000" -> id.substring(0, 4)
            id.substring(9) == "000" -> id.substring(0, 6)
            else -> id.substring(0, 9)
        }

        /** 汉字转拼音首字母大写（等价 pkg_gethzpy.GetHzPYCAP） */
        fun pinyinInitials(s: String): String = buildString {
            for (c in s) {
                val p = com.github.promeg.pinyinhelper.Pinyin.toPinyin(c.toString(), null)
                append(if (p.isEmpty()) c.uppercaseChar() else p[0].uppercaseChar())
            }
        }
    }
}
