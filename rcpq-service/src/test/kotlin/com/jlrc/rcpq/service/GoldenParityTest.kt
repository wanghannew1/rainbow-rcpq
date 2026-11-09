package com.jlrc.rcpq.service

import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.sql.DriverManager

/**
 * 行为对照管线闭环测试：备份库黄金样本(Oracle 原值) vs PG 现库逐行逐列比对。
 * 运行前提：本地 PG16 已应用 V1-V3（默认 localhost:5432/rainbow_rcpq，可用
 * PGURL/PGUSER/PGPASS 环境变量覆盖）。
 * 比对口径：字符串化后比较（两侧 dumps 均为 getString 口径；数值/时间格式两侧一致）。
 */
class GoldenParityTest {

    private val url = System.getenv().getOrDefault("PGURL", "jdbc:postgresql://localhost:5432/rainbow_rcpq")
    private val user = System.getenv().getOrDefault("PGUSER", "rcpq")
    private val pass = System.getenv().getOrDefault("PGPASS", "rcpq_dev")

    private data class Golden(val table: String, val totalRows: Long, val rows: List<Map<String, Any?>>)

    private fun golden(): List<Golden> {
        val json = javaClass.getResourceAsStream("/golden/xa-samples.json")!!.readBytes()
        val root = ObjectMapper().readValue(json, Map::class.java) as Map<String, Map<String, Any?>>
        return root.map { (t, v) ->
            @Suppress("UNCHECKED_CAST")
            Golden(t, (v["totalRows"] as Number).toLong(), v["sampled"] as List<Map<String, Any?>>)
        }
    }

    @Test
    fun `PG 数据与备份库黄金样本逐行一致`() {
        Class.forName("org.postgresql.Driver")
        val conn = DriverManager.getConnection(url, user, pass)
        val diffs = mutableListOf<String>()
        var compared = 0
        for (g in golden()) {
            val pkCols: List<Pair<String, Boolean>> = when (g.table) { // 列名 to 是否数值
                "AA01" -> listOf("aaa010" to true)
                "AA10" -> listOf("aaa100" to false, "aaa102" to false)
                "AA12" -> listOf("id" to false)
                else -> listOf("qseq" to true)
            }
            for (row in g.rows) {
                if (pkCols.any { (c, _) -> row[c] == null }) { diffs += "${g.table}: 样本行缺主键"; continue }
                val where = pkCols.joinToString(" AND ") { "${it.first} = ?" }
                val stmt = conn.prepareStatement("select * from ${g.table.lowercase()} where $where")
                pkCols.forEachIndexed { i, (c, numeric) ->
                    val v = row[c].toString()
                    val asLong = v.toLongOrNull()
                    if (numeric && asLong != null) stmt.setLong(i + 1, asLong) else stmt.setString(i + 1, v)
                }
                val rs = stmt.executeQuery()
                val keyDesc = pkCols.joinToString(",") { (c, _) -> "${c}=${row[c]}" }
                if (!rs.next()) {
                    diffs += "${g.table}[$keyDesc]: PG 缺行"
                    continue
                }
                val md = rs.metaData
                val pgRow = (1..md.columnCount).associate { md.getColumnName(it).lowercase() to rs.getString(it) }
                for ((col, oracleVal) in row) {
                    if (col.startsWith("__")) continue
                    compared++
                    val pgVal = pgRow[col]
                    // 时间口径归一: Oracle getString 带毫秒尾缀(.0)/DATE带午夜时分秒, 值等价
                    val norm = { s: String? -> s?.trim()?.removeSuffix(".0")?.removeSuffix(" 00:00:00") }
                    if (norm(oracleVal?.toString()) != norm(pgVal)) {
                        diffs += "${g.table}[$keyDesc].$col: oracle=${oracleVal} pg=$pgVal"
                    }
                }
            }
        }
        conn.close()
        assertTrue(diffs.isEmpty(), "对照发现 ${diffs.size} 处差异(共比对了 $compared 个单元格):\n" + diffs.take(20).joinToString("\n"))
    }
}
