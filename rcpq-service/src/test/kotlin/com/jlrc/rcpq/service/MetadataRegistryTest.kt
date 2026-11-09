package com.jlrc.rcpq.service

import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.springframework.core.io.ClassPathResource

class MetadataRegistryTest {

    @Test
    fun `xa 9 个 store 元数据可加载`() {
        val reg = MetadataRegistry(ObjectMapper())
        reg.load()
        // 全量迁移后注册表为 560 store；本测试钉 xa 试点 9 store 全部可解析（原意）
        val xaStores = listOf(
            "aa01Store", "aa10Store_Query", "aa10Store_QueryList", "aa12Store", "aa12Store_M",
            "sys_questionStore", "sys_questionStore_List", "sys_questionStore_Query", "sys_questionStore_dispose",
        )
        assertEquals(9, xaStores.size)
        assertTrue(reg.size() >= 9)
        for (s in xaStores) assertNotNull(reg.meta(s), "缺少 xa store: $s")
    }

    @Test
    fun `aa12Store 元数据解析正确`() {
        val reg = MetadataRegistry(ObjectMapper())
        reg.load()
        val m = reg.meta("aa12Store")!!
        assertEquals("AA12", m.table)
        assertTrue(m.columns.any { it.primaryKey && it.name == "id" })
        // 原 XML 标注的中文 label 保留
        assertTrue(m.columns.any { it.label.isNotEmpty() })
    }

    @Test
    fun `validate 用真实元数据跑通 — SYS_QUESTION 主键非空可跳过`() {
        val reg = MetadataRegistry(ObjectMapper())
        reg.load()
        val sq = reg.meta("sys_questionStore")!!
        val row = sq.columns.associate { it.name to "" } // 全空行
        val r = MetadataValidator.validateRow(row, sq.columns, isNew = true)
        // isNew 时主键 qseq 跳过; 但非主键非空列会报第一条
        assertFalse(r.valid)
        assertTrue(r.message.endsWith("数据不可为空"))
    }
}
