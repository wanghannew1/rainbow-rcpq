package com.jlrc.rcpq.web

import com.alibaba.excel.EasyExcel
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile

/**
 * 通用 Excel 批量导入通道（旧 cp33 ImportUtil.doExcelImport → ria_pojo.do?method=doImport 的等价移植）。
 *
 * POST /import/sys/{importId}  multipart: file + 可选 parameters(JSON, 弹窗透传列, 如 tc90_impnew 的
 * atb930/atb900/aab001/aab004)。
 *
 * 契约 = 旧 sys_import(SYS_IMPORT) + sys_import_mapping(SYS_IMPORT_MAPPING) 配置驱动:
 *   - 模板号 = import_id; 同 template_id 的每行=一个 sheet 配置(data_from=起始行 0 基, min_avai_cell=非空格下限);
 *   - data_form→data_to = Excel 列号(0基)→目标表列;
 *   - 每行生成 id(seq_*, 来自旧 hbm sequence 生成器), ace100=SEQ_CE10_ACE100, aae036=now;
 *   - 行进目标表(entity_name 短名小写), 批次记录进 ce10(ace103=description, ace102=表名大写, ace104=行数);
 *   - 超长(中文按 2 计)仍插入, 但 errors 收集 "行 列 最大长 实际长"(旧 downErrors 契约)。
 * 返回: 成功 succeed=true + ce10(ace100数组) + baz002; 失败 succeed=false + msg, 或 errors 数组(超长收集)。
 * 忠实怪癖: 数值单元格按 double 转字符串(123→"123.0"), 列类型由 PG 按列绑定转换; 弹窗 parameters
 * 在映射之后覆盖(旧 BeanUtils 后写覆盖同属性)。
 */
@RestController
@RequestMapping("/import/sys")
class SysImportController(private val jdbcTemplate: JdbcTemplate) {

    /** 目标表 ID 序列（旧 hbm <generator class="sequence"> 权威） */
    private val idSeq = mapOf(
        "tc90_bp" to "seq_tc90_id", "tc90_bp1" to "seq_tc90_bp1_id", "tc90_bp2" to "seq_tc90_bp2_id",
        "tc90_bp3" to "seq_tc90_bp3_id", "tc90_bp4" to "seq_tc90_bp4_id", "ac01_gx" to "seq_ac01_gx_id",
    )

    @PostMapping("/{importId}")
    @Transactional
    fun importExcel(
        @PathVariable importId: String,
        @RequestParam("file") file: MultipartFile,
        @RequestParam("parameters") parameters: String?,
    ): Map<String, Any?> {
        // 弹窗透传列（旧 dialogData.parameters）
        val extra: Map<String, Any?> = parameters
            ?.let { runCatching { com.fasterxml.jackson.databind.ObjectMapper().readValue(it, Map::class.java) }.getOrNull() }
            ?.mapKeys { it.key.toString().lowercase() } ?: emptyMap()

        val cfg = jdbcTemplate.query(
            "SELECT import_id, sheet_index, data_from, min_avai_cell, entity_name, description " +
                "FROM sys_import WHERE template_id=? ORDER BY sheet_index",
            { rs, _ -> arrayOf<Any?>(
                rs.getString("import_id"), rs.getInt("sheet_index"),
                rs.getInt("data_from"), rs.getString("min_avai_cell"),
                rs.getString("entity_name"), rs.getString("description")) },
            importId).firstOrNull()
            ?: return mapOf("succeed" to false, "msg" to "未配置此importId")
        val mappings = jdbcTemplate.query(
            "SELECT data_form, data_to, data_type FROM sys_import_mapping WHERE import_id=? ORDER BY data_form",
            { rs, _ -> Triple(rs.getString("data_form"), rs.getString("data_to"), rs.getString("data_type")) },
            cfg[0] as String)

        val baz002 = next("seq_sys_baz002")
        val batchIds = mutableListOf<Long>()
        val errors = mutableListOf<String>()

        // 本配置集(现配置均单 sheet; 保留循环骨架以对齐原 per-sheet 配置遍历)
        val sheetIdx = cfg[1] as Int
        val startRow = cfg[2] as Int
        val minCell = (cfg[3] as String?)?.toIntOrNull() ?: 0
        val table = (cfg[4] as String).substringAfterLast('.').lowercase()
        val description = cfg[5] as String?
        run {
            val ace100 = next("seq_ce10_ace100")
            batchIds.add(ace100)

            // 表列与类型（information_schema 权威, 6642bc4 模式）
            val cols = jdbcTemplate.query(
                "SELECT column_name, data_type, character_maximum_length FROM information_schema.columns " +
                    "WHERE table_schema='public' AND table_name=?",
                { rs, _ -> Triple(rs.getString(1), rs.getString(2), (rs.getObject(3) as Number?)?.toInt()) }, table)
                .associate { it.first to Pair(it.second, it.third) }

            val rows = mutableListOf<Map<Int, String>>()
            EasyExcel.read(file.inputStream,
                object : com.alibaba.excel.read.listener.ReadListener<Map<Int, String>> {
                    override fun invoke(data: Map<Int, String>, context: com.alibaba.excel.context.AnalysisContext) {
                        rows.add(data)
                    }
                    override fun doAfterAllAnalysed(context: com.alibaba.excel.context.AnalysisContext) {}
                }).sheet(sheetIdx).headRowNumber(startRow).doRead()

            var n = 0
            var rowNo = startRow
            outer@ for (r in rows) {
                rowNo++
                // 空行判定: 非空格数 >= min_avai_cell(=1) 否则停（原 isEmptyRow 语义）
                val nonEmpty = r.values.count { it != null && it.isNotEmpty() }
                if (nonEmpty < minCell) break@outer
                val colNames = mutableListOf<String>()
                val args = mutableListOf<Any?>()
                colNames.add("id"); args.add(next(idSeq[table] ?: return mapOf("succeed" to false, "msg" to "未配置目标表ID序列: $table")))
                colNames.add("ace100"); args.add(ace100)
                for ((form, to, dataType) in mappings) {
                    val idx = form.toIntOrNull() ?: continue
                    val raw = r[idx]
                    if (raw.isNullOrEmpty()) continue
                    val value = if (dataType?.toIntOrNull() == 2 && raw.toDoubleOrNull() != null) {
                        // 数值+日期型映射 → yyyy-MM-dd（Excel 日期序列自 1899-12-30）
                        java.time.LocalDate.of(1899, 12, 30).plusDays(raw.toDouble().toLong())
                            .format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                    } else raw
                    val maxLen = cols[to]?.second
                    if (maxLen != null && cnLength(value) > maxLen)
                        errors.add("$rowNo ${idx + 1} $maxLen ${cnLength(value)}")
                    colNames.add(to); args.add(value)
                }
                // 弹窗透传列(映射后覆盖)
                for ((k, v) in extra) {
                    if (cols.containsKey(k) && v != null) { colNames.add(k); args.add(v.toString()) }
                }
                if (cols.containsKey("aae036")) { colNames.add("aae036"); args.add(java.sql.Timestamp(System.currentTimeMillis()).toString()) }
                try {
                    jdbcTemplate.update(
                        "INSERT INTO $table (${colNames.joinToString(",")}) VALUES (${colNames.joinToString(",") { "?" }})",
                        *args.toTypedArray())
                } catch (e: Exception) {
                    return mapOf("succeed" to false, "msg" to (e.message ?: e.javaClass.simpleName))
                }
                n++
            }
            jdbcTemplate.update(
                "INSERT INTO ce10 (ace100, baz001, baz002, ace103, ace101, aae036, ace102, ace104) " +
                    "VALUES (?,?,?,?,?,?,?,?)",
                ace100, next("seq_sys_baz001"), baz002, description,
                0, java.sql.Timestamp(System.currentTimeMillis()), table.uppercase(), n.toLong())
        }
        return if (errors.isEmpty()) mapOf("succeed" to true, "ce10" to batchIds, "baz002" to baz002)
        else mapOf("succeed" to true, "ce10" to batchIds, "baz002" to baz002, "errors" to errors)
    }

    /** 旧 getStringLength: 非ASCII 计 2 */
    private fun cnLength(s: String): Int = s.fold(0) { acc, c -> acc + if (c.code >= 0x80) 2 else 1 }

    private fun next(name: String): Long =
        jdbcTemplate.queryForObject("SELECT nextval('$name')", Long::class.java)!!
}
