package com.jlrc.rcpq.service.az

import com.fasterxml.jackson.databind.ObjectMapper
import com.github.promeg.pinyinhelper.Pinyin
import com.jlrc.rcpq.entity.AB01
import com.jlrc.rcpq.entity.AB06
import com.jlrc.rcpq.entity.AB07
import com.jlrc.rcpq.entity.AC01
import com.jlrc.rcpq.entity.AC06
import com.jlrc.rcpq.entity.TC90
import com.jlrc.rcpq.entity.TC90_LIST
import com.jlrc.rcpq.service.MetadataRegistry
import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

/**
 * az 模块自定义用例（单位树/人员档案），语义来源：旧 azaaService/azabService/AzbaService/
 * AzbaSave/azbbService/Azbd/Azbe 七个类（规格重写，非逐行翻译）。
 *
 * 过程移植决策（原 7 个 PL/SQL → Kotlin）：
 * - PRC_CREATE_CHILD → rebuildUnitChildIndex（WITH RECURSIVE 层级展开重建 AB01_CHILD）
 * - PRC_单位基本信息变更 → auditAb06（逐字段写 AB06；列中文注释以 ColumnMeta.label 等价）
 * - PRC_个人信息变更 → 旧库中该过程不存在（azbb 为旧系统坏功能），按意图以 auditAc06 等价实现
 * - PRC_AC01_UPDATE_ONLY → JPA update（等价：动态更新 AC01 指定列）
 * - PKG_COMMON.P_UPDATE_VALUE / P_DELETE → broadcastUpdate / broadcastDelete
 *   （查 information_schema 动态展开含键列表；渐进迁移期仅命中已迁移表——限制已记录）
 * - pkg_pinyin.prc_create_pinyin → TinyPinyin 全拼小写（等价）
 *
 * 登录上下文：旧服务依赖 SysUserInfo，试点以固定测试用户代替（TODO 接入 Spring Security 后替换）。
 */
@Component
class AzCases(
    private val metadataRegistry: MetadataRegistry,
    private val jdbcTemplate: JdbcTemplate,
    private val objectMapper: ObjectMapper,
) {
    @PersistenceContext
    lateinit var em: EntityManager

    // ---------- 单位(az/a) ----------

    /** 单位树新增/更新（azaaService.update）：新节点置父 haschild；同步单位名到合同冗余列；重建子树索引 */
    @Transactional
    fun saveUnitTree(row: Map<String, Any?>): Map<String, Any?> {
        val ab01 = toEntity<AB01>(row)
        val isNew = ab01.aab001 == null
        if (isNew) {
            ab01.haschild = "false"
            em.persist(ab01)
            em.flush() // 使序列生成 aab001
            ab01.parentid?.takeIf { it.isNotEmpty() }?.let { pid ->
                // 树链: parentid → id 列(非主键 aab001)
                em.createQuery("select a from AB01 a where a.id = :id", AB01::class.java)
                    .setParameter("id", pid).resultList.firstOrNull()?.let { p ->
                        if (p.haschild == "false") p.haschild = "true"
                    }
            }
        } else {
            em.merge(ab01)
        }
        // 单位名称同步到合同冗余列（原 4 条 bulkUpdate）
        val name = ab01.aab004 ?: ""
        val code = ab01.aab001
        if (code != null) {
            jdbcTemplate.update("UPDATE tc90 SET aab004=? WHERE aab001=?", name, code)
            jdbcTemplate.update("UPDATE tc90_list SET aab004=? WHERE aab001=?", name, code)
            jdbcTemplate.update("UPDATE tc90 SET aab004_1=? WHERE aab001_1=?", name, code)
            jdbcTemplate.update("UPDATE tc90_list SET aab004_1=? WHERE aab001_1=?", name, code)
        }
        rebuildUnitChildIndex(ab01.parentid, TEST_USER_AAE011)
        return mapOf("aab001" to code, "id" to ab01.id, "haschild" to ab01.haschild)
    }

    /** 联系人保存（azaaService.doSave）：aab078='1' 的主联系人唯一化 */
    @Transactional
    fun saveContact(row: Map<String, Any?>): Map<String, Any?> {
        val ab07 = toEntity<AB07>(row)
        if (ab07.aab078 == "1") {
            val exclude = if (ab07.id != null) " and t.id <> :id" else ""
            val q = em.createQuery("update AB07 t set t.aab078 = null where t.aab001 = :aab001$exclude")
            q.setParameter("aab001", ab07.aab001)
            if (ab07.id != null) q.setParameter("id", ab07.id)
            q.executeUpdate()
        }
        if (ab07.id == null) em.persist(ab07) else em.merge(ab07)
        return mapOf("id" to ab07.id)
    }

    /** 单位变更审核（azabService.update）：逐字段审计写 AB06（列中文名取元数据 label）+ 保存 */
    @Transactional
    fun auditUnitUpdate(row: Map<String, Any?>): Map<String, Any?> {
        val meta = metadataRegistry.meta("ab01Store")
            ?: throw IllegalArgumentException("无store元数据: ab01Store")
        val aab001 = (row["aab001"] ?: throw IllegalArgumentException("缺少 aab001")).toString().toLong()
        val orig = em.find(AB01::class.java, aab001) ?: throw IllegalArgumentException("单位不存在: $aab001")
        var n = 0
        for ((col, newVal) in row) {
            if (col.startsWith("__") || col == "aab001") continue
            val old = fieldOf(orig, col)?.toString() ?: ""
            val new = newVal?.toString() ?: ""
            if (old == new) continue
            val label = meta.columns.firstOrNull { it.name == col }?.label ?: col
            auditAb06(aab001, col, label, old, new)
            n++
        }
        applyRow(orig, row.filterKeys { it != "__state" })
        em.merge(orig)
        return mapOf("aab001" to aab001, "auditedFields" to n)
    }

    // ---------- 人员(az/b) ----------

    /** 人员保存（AzbaSave.save）：新增取 SEQ_AC01_AAC001；四要素变更广播；AC06 日志；拼音；Cc0P 登记 */
    @Transactional
    fun savePerson(row: Map<String, Any?>): Map<String, Any?> {
        val key = row["aac001"]?.toString()
        return if (key.isNullOrEmpty() || key == "null") {
            val ac01 = toEntity<AC01>(row.filterKeys { it != "__state" })
            // 原 aae006=aac010+aac00b+c+d 拼接, 但三段属性在实体中不存在(BeanUtil 静默 null) → 实际恒等于 aac010
            ac01.aae006 = ac01.aac010
            // 原 aac022 拼音写入: AC01 无该属性, 旧系统实际未持久化 → 忠实省略
            em.persist(ac01)
            em.flush()
            logAc06(ac01, op = null)
            logCc0p(ac01, "001", "姓名：${ac01.aac003},身份证号：${ac01.aac002},于${ac01.aae036}做了基本信息登记")
            mapOf("aac001" to ac01.aac001)
        } else {
            val id = key.toLong()
            val orig = em.find(AC01::class.java, id) ?: throw IllegalArgumentException("人员不存在: $id")
            val changed = changedCore(orig, row)
            applyRow(orig, row.filterKeys { it != "__state" })
            orig.aae006 = orig.aac010
            if (changed) {
                broadcastUpdate("aac001", id.toString(),
                    "aac002,aac003,aac004,aac006",
                    listOf(row["aac002"], row["aac003"], row["aac004"], row["aac006"]),
                    exclude = setOf("ac06", "cc0p"))
            }
            logAc06(orig, op = null)
            em.merge(orig)
            mapOf("aac001" to id, "changed" to changed)
        }
    }

    /** 基本信息变更同步合同（AzbaService.changeJbxx）：AC01 保存 + 约30个人字段同步到在执行合同 */
    @Transactional
    fun changeJbxx(row: Map<String, Any?>): Map<String, Any?> {
        val aac001 = (row["aac001"] ?: throw IllegalArgumentException("缺少 aac001")).toString().toLong()
        val ac01 = em.find(AC01::class.java, aac001) ?: throw IllegalArgumentException("人员不存在: $aac001")
        applyRow(ac01, row.filterKeys { it != "__state" })
        em.merge(ac01)
        var n = 0
        // 原 find 条件 aae100='1' and atb90r='3'（在执行标志）
        val tc90s = em.createQuery(
            "select t from TC90 t where t.aae100 = '1' and t.atb90r = '3' and t.aac001 = :aac001", TC90::class.java)
            .setParameter("aac001", aac001).resultList
        for (t in tc90s) {
            syncPersonFields(ac01, t); em.merge(t); n++
        }
        val lists = em.createQuery(
            "select t from TC90_LIST t where t.aae100 = '1' and t.atb90r = '3' and t.aac001 = :aac001", TC90_LIST::class.java)
            .setParameter("aac001", aac001).resultList
        for (t in lists) {
            syncPersonFieldsList(ac01, t); em.merge(t); n++
        }
        return mapOf("aac001" to aac001, "synced" to n)
    }

    /** 联系人变体保存（AzbaService.doUpdate） */
    @Transactional
    fun updateLb(row: Map<String, Any?>): Map<String, Any?> {
        val ac01Lb = toEntity<com.jlrc.rcpq.entity.AC01_LB>(row)
        return if (ac01Lb.aac001 == null) {
            em.persist(ac01Lb); mapOf("saved" to "new")
        } else {
            em.merge(ac01Lb); mapOf("saved" to ac01Lb.aac001)
        }
    }

    /** 个人信息变更审核（azbbService.update）：原 PRC_个人信息变更 在旧库不存在（坏功能），按意图等价实现 */
    @Transactional
    fun auditPersonUpdate(row: Map<String, Any?>): Map<String, Any?> {
        val meta = metadataRegistry.meta("ac01Store")
            ?: throw IllegalArgumentException("无store元数据: ac01Store")
        val aac001 = (row["aac001"] ?: throw IllegalArgumentException("缺少 aac001")).toString().toLong()
        val orig = em.find(AC01::class.java, aac001) ?: throw IllegalArgumentException("人员不存在: $aac001")
        var n = 0
        for ((col, newVal) in row) {
            if (col.startsWith("__") || col == "aac001") continue
            val old = fieldOf(orig, col)?.toString() ?: ""
            val new = newVal?.toString() ?: ""
            if (old == new) continue
            val label = meta.columns.firstOrNull { it.name == col }?.label ?: col
            auditAc06Field(aac001, col, label, old, new)
            n++
        }
        applyRow(orig, row.filterKeys { it != "__state" })
        em.merge(orig)
        return mapOf("aac001" to aac001, "auditedFields" to n)
    }

    /** 人员删除（Azbd.del）：aac00k='00' 解挂 → AC06 删除日志 → P_DELETE 等价广播删除（保留 AC06） */
    @Transactional
    fun deletePerson(row: Map<String, Any?>): Map<String, Any?> {
        val aac001 = (row["aac001"] ?: throw IllegalArgumentException("缺少 aac001")).toString().toLong()
        val ac01 = em.find(AC01::class.java, aac001) ?: throw IllegalArgumentException("人员不存在: $aac001")
        // 原 aac00k/cac001 解挂分支: 属性在实体中不存在, 旧系统为静默空操作 → 忠实省略
        logAc06(ac01, op = "删除")
        em.flush()
        // 广播删除子数据(AC06 日志保留); 主行由 em.remove 负责(排除避免乐观锁冲突)
        broadcastDelete("aac001", aac001.toString(), exclude = setOf("ac06", "ac01"))
        em.remove(ac01)
        return mapOf("deleted" to aac001)
    }

    /** 人员合并（Azbe.save）：h1 并入 h2（key=h2 新主键），删 h1 留日志，四要素差异广播 */
    @Transactional
    fun mergePerson(h1Aac001: Long, h2Aac001: Long, key: Long): Map<String, Any?> {
        val h1 = em.find(AC01::class.java, h1Aac001) ?: throw IllegalArgumentException("人员不存在: $h1Aac001")
        val h2 = em.find(AC01::class.java, h2Aac001) ?: throw IllegalArgumentException("人员不存在: $h2Aac001")
        logAc06(h1, op = null)
        val changed = listOf("aac002", "aac003", "aac004", "aac006").any {
            fieldOf(h2, it)?.toString().orEmpty() != fieldOf(h1, it)?.toString().orEmpty()
        }
        if (changed) {
            broadcastUpdate("aac001", key.toString(),
                "aac002,aac003,aac004,aac006,aac001",
                listOf(fieldOf(h2, "aac002"), fieldOf(h2, "aac003"), fieldOf(h2, "aac004"), fieldOf(h2, "aac006"), key),
                exclude = setOf("ac06", "cc0p"))
        }
        em.flush()
        // 原 updateAc01 将 h2.aac001 改为 key: JPA 不可改主键 → 原生重键(语义等价)
        jdbcTemplate.update("UPDATE ac01 SET aac001=? WHERE aac001=?", key, h2Aac001)
        // 原 cac001 解挂分支: 属性不存在, 旧系统静默空操作 → 忠实省略
        em.remove(h1)
        return mapOf("merged" to h1Aac001, "into" to key)
    }

    /** 单位树子索引重建（PRC_CREATE_CHILD 等价：WITH RECURSIVE 展开根子树全量重写 AB01_CHILD） */
    /** 从已提交的父节点起向上找根(新节点自身未提交, 不可作为起点) */
    @Transactional
    fun rebuildUnitChildIndex(parentId: String?, operator: String) {
        if (parentId.isNullOrEmpty()) return
        val rootId = jdbcTemplate.queryForObject(
            """WITH RECURSIVE up AS (
                 SELECT id, parentid, 0 AS lvl FROM ab01 WHERE id = ?
                 UNION ALL
                 SELECT a.id, a.parentid, up.lvl + 1 FROM ab01 a JOIN up ON a.id = up.parentid
               ) SELECT id FROM up
                 WHERE parentid IS NULL OR parentid NOT IN (SELECT id FROM ab01) OR lvl > 100
                 ORDER BY lvl DESC LIMIT 1""".trimIndent(),
            String::class.java, parentId) ?: return
        jdbcTemplate.update("DELETE FROM ab01_child WHERE root = ?", rootId)
        jdbcTemplate.update(
            """INSERT INTO ab01_child (seq, aab001, aab004, id, aae011, lvl, root)
               SELECT nextval('seq_ab01_child_seq'), aab001, aab004, id, ?, (depth + 1), ?
               FROM (WITH RECURSIVE tree AS (
                       SELECT id, parentid, aab001, aab004, 0 AS depth FROM ab01 WHERE id = ?
                       UNION ALL
                       SELECT c.id, c.parentid, c.aab001, c.aab004, t.depth + 1
                       FROM ab01 c JOIN tree t ON c.parentid = t.id
                     ) SELECT id, parentid, aab001, aab004, depth FROM tree) s""".trimIndent(),
            operator, rootId, rootId)
    }

    // ---------- 通用广播（PKG_COMMON.P_UPDATE_VALUE / P_DELETE 等价） ----------

    /** 广播更新：所有含键列的表(排除表外)按主键更新指定列(键按列类型绑定) */
    private fun broadcastUpdate(keyCol: String, id: String, cols: String, vals: List<Any?>, exclude: Set<String>) {
        val pairs = cols.split(",").zip(vals)
        val tables = tablesWithColumn(keyCol).filter { it !in exclude }
        for (tb in tables) {
            // 原 P_UPDATE_VALUE 语义: 每表只更新该表实际存在的列
            val tableCols = jdbcTemplate.queryForList(
                "SELECT column_name FROM information_schema.columns WHERE table_schema='public' AND table_name=?",
                String::class.java, tb).toSet()
            val present = pairs.filter { (c, _) -> c in tableCols }
            if (present.isEmpty()) continue
            val setSql = present.joinToString(", ") { (c, _) -> "$c = ?" }
            val setVals = present.map { it.second }.toTypedArray()
            jdbcTemplate.update(
                "UPDATE $tb SET $setSql WHERE $keyCol = ${keyBind(tb, keyCol, id)}", *setVals)
        }
    }

    /** 广播删除：所有含键列的表(排除表外)按主键删除(键按列类型内联, 白名单已过滤) */
    private fun broadcastDelete(keyCol: String, id: String, exclude: Set<String>) {
        val tables = tablesWithColumn(keyCol).filter { it !in exclude }
        for (tb in tables) {
            jdbcTemplate.update("DELETE FROM $tb WHERE $keyCol = ${keyBind(tb, keyCol, id)}")
        }
    }

    /** 键值绑定片段：数值列内联(白名单列名+数值校验), 否则参数化字符串 */
    private fun keyBind(table: String, keyCol: String, id: String): String {
        return when (jdbcTemplate.queryForObject(
            """SELECT data_type FROM information_schema.columns
               WHERE table_schema='public' AND table_name=? AND column_name=?""",
            String::class.java, table, keyCol)) {
            "bigint", "integer", "numeric", "smallint", "double precision", "real" ->
                if (id.toLongOrNull() != null) id  // 数值键须合法数字
                else "'${id.replace("'", "''")}'"
            else -> "'${id.replace("'", "''")}'"
        }
    }

    private fun tablesWithColumn(col: String): List<String> =
        jdbcTemplate.queryForList(
            """SELECT c.table_name FROM information_schema.columns c
               JOIN information_schema.tables t ON t.table_schema = c.table_schema AND t.table_name = c.table_name
               WHERE c.table_schema='public' AND c.column_name=? AND t.table_type='BASE TABLE'
               ORDER BY c.table_name""", String::class.java, col)

    // ---------- 日志与工具 ----------

    /** AB06 单位变更日志（PRC_单位基本信息变更 等价；列中文名取元数据 label） */
    private fun auditAb06(aab001: Long, col: String, label: String, before: String, after: String) {
        val e = AB06()
        e.aab001 = aab001
        // 原过程: aae0b1=字段名, aae0b2=列中文注释, aae0b3=旧值, aae0b4=新值
        setField(e, "aae0b1", col)
        setField(e, "aae0b2", label)
        setField(e, "aae0b3", before)
        setField(e, "aae0b4", after)
        setField(e, "aae011", TEST_USER_AAE011)
        setField(e, "aae017", TEST_USER_AAE017)
        setField(e, "aae019", TEST_USER_AAE019)
        setField(e, "aae020", TEST_USER_AAE020)
        em.persist(e)
    }

    /** AC06 人员变更日志（AzbaSave/Azbd 的 saveAc06 等价） */
    private fun logAc06(ac01: AC01, op: String?) {
        val e = AC06()
        copyPersonToAc06(ac01, e)
        setField(e, "aae013", op ?: "")
        em.persist(e)
    }

    /** AC06 字段级审计（旧 PRC_个人信息变更 意图等价） */
    private fun auditAc06Field(aac001: Long, col: String, label: String, before: String, after: String) {
        val e = AC06()
        setField(e, "aac001", aac001)
        setField(e, "aae0b1", col)
        setField(e, "aae0b2", label)
        setField(e, "aae0b3", before)
        setField(e, "aae0b4", after)
        setField(e, "aae011", TEST_USER_AAE011)
        em.persist(e)
    }

    /** Cc0P 操作日志（Cc0pUtil.saveCc0p 等价，acc0p2=操作类型码） */
    private fun logCc0p(ac01: AC01, type: String, message: String) {
        val e = com.jlrc.rcpq.entity.CC0P()
        setField(e, "acc0p2", type)
        setField(e, "aae013", message)
        setField(e, "aac001", ac01.aac001)
        em.persist(e)
    }

    private fun changedCore(orig: AC01, row: Map<String, Any?>): Boolean =
        listOf("aac002", "aac003", "aac004", "aac006").any {
            (fieldOf(orig, it)?.toString() ?: "") != (row[it]?.toString() ?: "")
        }

    private fun row0(e: AC01, col: String) = fieldOf(e, col)

    /** 元数据 label（复用注册表；实体字段→元数据列名同为小写） */
    private fun labelOf(store: String, col: String): String =
        metadataRegistry.meta(store)?.columns?.firstOrNull { it.name == col }?.label ?: col

    @Suppress("UNCHECKED_CAST")
    private inline fun <reified T : Any> toEntity(row: Map<String, Any?>): T =
        objectMapper.convertValue(row.filterKeys { it != "__state" }, T::class.java)

    /** 反射读/写实体属性（元数据列名=属性名，小写一致） */
    private fun fieldOf(obj: Any, name: String): Any? = try {
        obj.javaClass.getMethod(propGet(name)).invoke(obj)
    } catch (e: Exception) {
        null
    }

    private fun setField(obj: Any, name: String, value: Any?) {
        val m = obj.javaClass.methods.firstOrNull { it.name == propSet(name) } ?: return
        val paramType = m.parameterTypes[0]
        val v = when (paramType) {
            java.lang.Long::class.java -> (value?.toString() ?: return).let {
                if (it.contains('.')) it.toDouble().toLong() else it.toLong()
            }
            else -> value
        }
        m.invoke(obj, v)
    }

    private fun applyRow(target: Any, row: Map<String, Any?>) {
        for ((k, v) in row) {
            val getter = target.javaClass.methods.firstOrNull { it.name == propGet(k) } ?: continue
            val setter = target.javaClass.methods.firstOrNull { it.name == propSet(k) } ?: continue
            val pt = setter.parameterTypes[0]
            val converted: Any? = when {
                v == null -> null
                pt == java.lang.Long::class.java -> v.toString().toDoubleOrNull()?.toLong() ?: v.toString().toLongOrNull()
                pt == java.math.BigDecimal::class.java -> java.math.BigDecimal(v.toString())
                pt == java.time.LocalDate::class.java -> java.time.LocalDate.parse(v.toString().take(10))
                pt == java.time.LocalDateTime::class.java ->
                    java.time.LocalDateTime.parse(v.toString().replace(" ", "T").take(19))
                else -> v.toString()
            }
            setter.invoke(target, converted)
        }
    }

    private fun copyPersonToAc01(ac01: AC01, row: Map<String, Any?>) = applyRow(ac01, row)
    private fun copyPersonToAc06(ac01: AC01, e: AC06) {
        // AC06 与 AC01 同名列复制（原 BeanUtil.copyProperties(entityobj, entityobj_Ac06)）
        for (m in ac01.javaClass.methods.filter { it.name.startsWith("get") && it.parameterCount == 0 }) {
            val prop = m.name.removePrefix("get").replaceFirstChar { it.lowercaseChar() }
            val setter = e.javaClass.methods.firstOrNull { it.name == "set${prop.replaceFirstChar { c -> c.uppercaseChar() }}" }
            if (setter != null) {
                try { setter.invoke(e, m.invoke(ac01)) } catch (_: Exception) { /* 类型不匹配的列跳过 */ }
            }
        }
    }

    /** 姓名全拼（pkg_pinyin.prc_create_pinyin 等价；非汉字原样） */
    private fun fullPinyin(name: String?): String? {
        if (name.isNullOrEmpty()) return null
        return buildString {
            for (c in name) append(Pinyin.toPinyin(c.toString(), c.toString()))
        }
    }

    private fun propGet(name: String) = "get${name.replaceFirstChar { it.uppercaseChar() }}"
    private fun propSet(name: String) = "set${name.replaceFirstChar { it.uppercaseChar() }}"

    companion object {
        /** 试点固定测试用户（TODO 接入 Spring Security 后从登录态取） */
        const val TEST_USER_AAE011 = "9999"
        const val TEST_USER_AAE017 = "测试用户"
        const val TEST_USER_AAE019 = "00000000"
        const val TEST_USER_AAE020 = "彩虹人才试点"

        /** AC01→TC90 字段同步映射（changeJbxx 原文 30 项，含尾部覆盖语义） */
        val SYNC_FIELDS = listOf(
            "aac005", "aac009", "aac024", "aac033", "aac017", "aac007", "aae00d", "aae008", "atc90r",
            "aab299", "bab305", "aac025", "aac011", "aac181", "aac182", "aac180", "aac015", "aac184",
            "aca111", "aca112", "aac185", "aac014", "bac014", "aae00a", "aac013", "aae00p", "aac006",
        )
    }

    private fun syncPersonFields(ac01: AC01, t: TC90) {
        for (f in SYNC_FIELDS) copyProp(ac01, t, f)
        copyProp(ac01, t, "aae005", "aae00c")   // 原文: aae005=aae00c(尾部覆盖)
        copyProp(ac01, t, "atc902", "aae006")
        copyProp(ac01, t, "aae009", "aae009")
        copyProp(ac01, t, "bbb001", "bbb001")
    }

    private fun syncPersonFieldsList(ac01: AC01, t: TC90_LIST) {
        for (f in SYNC_FIELDS) copyProp(ac01, t, f)
        copyProp(ac01, t, "aae005", "aae00c")
        copyProp(ac01, t, "atc902", "aae006")
        copyProp(ac01, t, "aae009", "bbb001")   // 原文: aae009 最终取 bbb001
        copyProp(ac01, t, "aae005", "aae00c")
    }

    private fun copyProp(src: Any, dst: Any, srcProp: String, dstProp: String = srcProp) {
        val v = try { src.javaClass.getMethod(propGet(srcProp)).invoke(src) } catch (_: Exception) { return }
        val setter = dst.javaClass.methods.firstOrNull { it.name == propSet(dstProp) } ?: return
        try { setter.invoke(dst, v) } catch (_: Exception) { }
    }
}
