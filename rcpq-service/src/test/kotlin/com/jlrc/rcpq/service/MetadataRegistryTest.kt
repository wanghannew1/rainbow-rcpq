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
        assertEquals(9, reg.size())
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
