package com.jlrc.rcpq.service

import com.jlrc.rcpq.service.model.ColumnMeta
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.sql.Types

class MetadataValidatorTest {

    private val meta = listOf(
        ColumnMeta(name = "aaa100", label = "代码类别", nullable = false, precision = 3, jdbcType = Types.VARCHAR),
        ColumnMeta(name = "aaa102", label = "代码", nullable = false, primaryKey = true, precision = 4, jdbcType = Types.VARCHAR),
        ColumnMeta(name = "aaa103", label = "名称", nullable = false, precision = 6, jdbcType = Types.VARCHAR),
        ColumnMeta(name = "aaa105", label = "数值", nullable = true, precision = 5, jdbcType = Types.NUMERIC),
        ColumnMeta(name = "aaa107", label = "备注", nullable = true, precision = -1, jdbcType = Types.VARCHAR),
    )

    @Test
    fun `合法行通过`() {
        val r = MetadataValidator.validateRow(
            mapOf("aaa100" to "ABC", "aaa102" to "0001", "aaa103" to "测试", "aaa105" to "12.5"),
            meta,
        )
        assertTrue(r.valid)
    }

    @Test
    fun `非空列缺失失败`() {
        val r = MetadataValidator.validateRow(mapOf("aaa102" to "0001", "aaa103" to "x"), meta)
        assertFalse(r.valid)
        assertEquals(-30027L, r.code)
        assertEquals("[代码类别]数据不可为空", r.message)
    }

    @Test
    fun `空字符串视同缺失`() {
        val r = MetadataValidator.validateRow(mapOf("aaa100" to "", "aaa102" to "0001", "aaa103" to "x"), meta)
        assertFalse(r.valid)
        assertEquals("[代码类别]数据不可为空", r.message)
    }

    @Test
    fun `新增记录跳过主键非空校验但仍校验长度`() {
        // isNew: 主键 aaa102 为空不报非空; 但 aaa103 超长仍报错
        val r = MetadataValidator.validateRow(
            mapOf("aaa100" to "AB", "aaa102" to "", "aaa103" to "一二三四五六七"), meta, isNew = true,
        )
        assertFalse(r.valid)
        assertEquals("[名称]数据过长", r.message)
    }

    @Test
    fun `中文按2字符计长`() {
        assertEquals(6, MetadataValidator.displayLength("一二三"))
        assertEquals(4, MetadataValidator.displayLength("ab一"))
    }

    @Test
    fun `字符串超长失败_中文口径`() {
        // precision=6, "一二三四" = 8 > 6
        val r = MetadataValidator.validateRow(
            mapOf("aaa100" to "AB", "aaa102" to "0001", "aaa103" to "一二三四"), meta,
        )
        assertFalse(r.valid)
        assertEquals("[名称]数据过长", r.message)
    }

    @Test
    fun `数值长度去负号小数点计`() {
        val r = MetadataValidator.validateRow(
            mapOf("aaa100" to "AB", "aaa102" to "0001", "aaa103" to "x", "aaa105" to "-12.34"),
            meta,
        )
        // "1234" = 4 <= 5 通过
        assertTrue(r.valid)
        val r2 = MetadataValidator.validateRow(
            mapOf("aaa100" to "AB", "aaa102" to "0001", "aaa103" to "x", "aaa105" to "-123.456"),
            meta,
        )
        // "123456" = 6 > 5 失败
        assertFalse(r2.valid)
        assertEquals("[数值]数据过长", r2.message)
    }

    @Test
    fun `无precision列不校验长度`() {
        val r = MetadataValidator.validateRow(
            mapOf("aaa100" to "AB", "aaa102" to "0001", "aaa103" to "x", "aaa107" to "很长".repeat(500)),
            meta,
        )
        assertTrue(r.valid)
    }
}
