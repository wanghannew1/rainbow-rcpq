package com.jlrc.rcpq.web

import com.alibaba.excel.EasyExcel
import org.apache.poi.ss.usermodel.BorderStyle
import org.apache.poi.ss.usermodel.CellStyle
import org.apache.poi.ss.usermodel.FillPatternType
import org.apache.poi.ss.usermodel.HorizontalAlignment
import org.apache.poi.ss.usermodel.IndexedColors
import org.apache.poi.ss.usermodel.VerticalAlignment
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import java.io.ByteArrayOutputStream
import java.net.URLEncoder

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
class SysImportController(private val jdbcTemplate: JdbcTemplate, private val audit: AuditLog) {

    /** 目标表 ID 序列（旧 hbm <generator class="sequence"> 权威） */
    private val idSeq = mapOf(
        "tc90_bp" to "seq_tc90_id", "tc90_bp1" to "seq_tc90_bp1_id", "tc90_bp2" to "seq_tc90_bp2_id",
        "tc90_bp3" to "seq_tc90_bp3_id", "tc90_bp4" to "seq_tc90_bp4_id", "ac01_gx" to "seq_ac01_gx_id",
        // 专项五套(2026-11-21, hbm: TC04/05/06/07_BP+TD04_BP → SEQ_TC0x_ID/SEQ_TD04_ID)
        "tc04_bp" to "seq_tc04_id", "tc05_bp" to "seq_tc05_id", "tc06_bp" to "seq_tc06_id",
        "tc07_bp" to "seq_tc07_id", "td04_bp" to "seq_td04_id",
        // 三银行到账(2026-11-21, hbm: TB97_BP/BP01/BP02 → SEQ_TB97_BP*_ID)
        "tb97_bp" to "seq_tb97_bp_id", "tb97_bp01" to "seq_tb97_bp01_id", "tb97_bp02" to "seq_tb97_bp02_id",
        // 市医保异动回盘(2026-11-26, 卷61 端到端实证; TC9W_BP04 PK=id, 批次键=BAZ002 无 ace100)
        "tc9w_bp04" to "seq_tc9w_bp04_id",
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
            ?: run {
                audit.log(extra["aae011"]?.toString(), "import:sys", importId,
                    mapOf("file" to (file.originalFilename ?: ""), "error" to "未配置此importId"))
                return mapOf("succeed" to false, "msg" to "未配置此importId")
            }
        // V10 种子有四套模板(bp2/bp3/bp4/impnew)映射行成对重复 → 去重防 INSERT 重复列报错
        // (Oracle 权威 SYS_IMPORT_MAPPING 每 data_form 一行; 取 min(data_to) 保持幂等)
        val mappings = jdbcTemplate.query(
            "SELECT data_form, min(data_to), min(data_type) FROM sys_import_mapping " +
                "WHERE import_id=? GROUP BY data_form ORDER BY data_form",
            { rs, _ -> Triple(rs.getString(1), rs.getString(2), rs.getString(3)) },
            cfg[0] as String)

        val baz002 = next("seq_sys_baz002")
        val batchIds = mutableListOf<Long>()
        val errors = mutableListOf<String>()
        val skipped = mutableListOf<String>()  // 映射指向表里不存在的列(仅 tc90_bp 27 列, 同 Oracle 表结构漂移)

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
                colNames.add("id"); args.add(next(idSeq[table] ?: run {
                    audit.log(extra["aae011"]?.toString(), "import:sys", importId,
                        mapOf("file" to (file.originalFilename ?: ""), "error" to "未配置目标表ID序列: $table"))
                    return mapOf("succeed" to false, "msg" to "未配置目标表ID序列: $table")
                }))
                // 批次列按表存在性门控(卷61: TC9W_BP04 无 ace100 列, 批次键=BAZ002 **上传级**——
                // 整批共享 line 86 生成的同一个 baz002, 与响应返回值一致; 既有 bp 族表均有 ace100, 行为不变)
                when {
                    cols.containsKey("ace100") -> { colNames.add("ace100"); args.add(ace100) }
                    cols.containsKey("baz002") -> { colNames.add("baz002"); args.add(baz002) }
                }
                for ((form, to, dataType) in mappings) {
                    val idx = form.toIntOrNull() ?: continue
                    // 表里不存在的映射列跳过(information_schema 权威; 该列值不落库, 见响应 skipped)
                    if (!cols.containsKey(to)) {
                        if (to !in skipped) skipped.add(to)
                        continue
                    }
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
                    colNames.add(to); args.add(typed(value, cols[to]?.first))
                }
                // 弹窗透传列(映射后覆盖)
                for ((k, v) in extra) {
                    if (cols.containsKey(k) && v != null) {
                        colNames.add(k); args.add(typed(v.toString(), cols[k]?.first))
                    }
                }
                if (cols.containsKey("aae036")) {
                    // aae036 按列类型分型绑定: 多数批次表是 varchar(存字符串), tc05_bp 等是 timestamp
                    val ts = java.sql.Timestamp(System.currentTimeMillis())
                    colNames.add("aae036")
                    args.add(if (cols["aae036"]?.first?.startsWith("timestamp") == true) ts else ts.toString())
                }
                try {
                    jdbcTemplate.update(
                        "INSERT INTO $table (${colNames.joinToString(",")}) VALUES (${colNames.joinToString(",") { "?" }})",
                        *args.toTypedArray())
                } catch (e: Exception) {
                    // 根因透传: Spring 包装层 message 不含 PG 错误正文, 取 cause 链最底层的驱动报错
                    var root: Throwable = e
                    while (root.cause != null && root.cause !== root) root = root.cause!!
                    val errText = root.message ?: e.message ?: e.javaClass.simpleName
                    audit.log(extra["aae011"]?.toString(), "import:sys", importId,
                        mapOf("file" to (file.originalFilename ?: ""), "error" to errText,
                              "ace100" to ace100, "rowNo" to rowNo))
                    return mapOf("succeed" to false, "msg" to errText)
                }
                n++
            }
            jdbcTemplate.update(
                "INSERT INTO ce10 (ace100, baz001, baz002, ace103, ace101, aae036, ace102, ace104) " +
                    "VALUES (?,?,?,?,?,?,?,?)",
                ace100, next("seq_sys_baz001"), baz002, description,
                0, java.sql.Timestamp(System.currentTimeMillis()), table.uppercase(), n.toLong())
            // 操作留痕(铁律8): 导入=写操作, 失败更需可追溯
            audit.log(extra["aae011"]?.toString(), "import:sys", importId,
                mapOf("file" to (file.originalFilename ?: ""), "table" to table, "ace100" to ace100,
                      "rows" to n, "overlong" to errors.size, "overlongSample" to errors.take(10),
                      "skipped" to skipped))
        }
        val base = mapOf("succeed" to true, "ce10" to batchIds, "baz002" to baz002)
        if (skipped.isEmpty()) return if (errors.isEmpty()) base
        else base + mapOf("errors" to errors)
        return if (errors.isEmpty()) base + mapOf("skipped" to skipped)
        else base + mapOf("errors" to errors, "skipped" to skipped)
    }

    /**
     * 导入模板下载(GET /import/sys/{importId}/template) — 新签/更新/续签/新增保险四弹窗的配套面。
     * 旧系统下发人工维护的静态 .xls(FileDownLoadServlet, SYS_IMPORT.TEMPLATE_URL 指向), 新库
     * template_url 全空 → 由 sys_import_mapping 反向生成, 布局严格对齐引擎解析约定:
     *   第 1 行 = 模板名(sys_import.description 合并居中); 第 2 行 = 填表说明;
     *   第 data_from 行 = 列头(映射 description, 加粗底纹); 数据自第 data_from+1 行起(引擎 headRowNumber=data_from)。
     * 列顺序 = data_form(0 基)升序, 与 sys_import_mapping 一一对应, 不可增删调换。
     */
    @GetMapping("/{importId}/template")
    fun template(@PathVariable importId: String): ResponseEntity<ByteArray> {
        val cfg = jdbcTemplate.query(
            "SELECT description, data_from, min_avai_cell FROM sys_import WHERE template_id=? ORDER BY sheet_index",
            { rs, _ -> Triple(rs.getString("description"), rs.getInt("data_from"), rs.getString("min_avai_cell")) },
            importId).firstOrNull()
            ?: return ResponseEntity.notFound().build()
        val (title, startRow, minCell) = cfg
        // 目标表物理列(information_schema 权威): 表里没有的映射列在模板列头加 ※ 标注(填了不落库)
        val cfgAll = jdbcTemplate.query(
            "SELECT entity_name FROM sys_import WHERE template_id=? ORDER BY sheet_index",
            { rs, _ -> rs.getString(1) }, importId)
        val table = (cfgAll.firstOrNull() ?: "").substringAfterLast('.').lowercase()
        val phys = if (table.isEmpty()) emptySet() else jdbcTemplate.query(
            "SELECT column_name FROM information_schema.columns WHERE table_schema='public' AND table_name=?",
            { rs, _ -> rs.getString(1) }, table).toSet()
        val cols = jdbcTemplate.query(
            "SELECT data_form::int AS df, min(coalesce(description, data_to)) AS label, min(data_to) AS col " +
                "FROM sys_import_mapping WHERE import_id=? AND data_form ~ '^[0-9]+$' " +
                "GROUP BY data_form::int ORDER BY 1",
            { rs, _ -> Triple(rs.getInt("df"), rs.getString("label"), rs.getString("col")) }, importId)
            .map { (_, label, col) -> if (phys.isNotEmpty() && col !in phys) "※$label" else label }
        if (cols.isEmpty()) return ResponseEntity.notFound().build()

        XSSFWorkbook().use { wb ->
            val sheet = wb.createSheet("Sheet0")
            val headStyle = wb.createCellStyle().apply {
                fillForegroundColor = IndexedColors.GREY_25_PERCENT.index
                fillPattern = FillPatternType.SOLID_FOREGROUND
                alignment = HorizontalAlignment.CENTER; verticalAlignment = VerticalAlignment.CENTER
                setBorderTop(BorderStyle.THIN); setBorderBottom(BorderStyle.THIN)
                setBorderLeft(BorderStyle.THIN); setBorderRight(BorderStyle.THIN)
            }
            val bodyStyle = wb.createCellStyle().apply {
                setBorderTop(BorderStyle.THIN); setBorderBottom(BorderStyle.THIN)
                setBorderLeft(BorderStyle.THIN); setBorderRight(BorderStyle.THIN)
            }
            val font = wb.createFont().apply { bold = true }
            val titleStyle = wb.createCellStyle().apply {
                setFont(font); alignment = HorizontalAlignment.CENTER; verticalAlignment = VerticalAlignment.CENTER
            }

            // 布局对齐引擎解析约定: 列头固定在第 data_from 行(0 基 data_from-1), 数据自下一行起。
            // data_from=1(专项四套)时第 0 行就是列头行, 无标题/说明空间 → 说明落第二张 sheet。
            fun row(i: Int) = sheet.getRow(i) ?: sheet.createRow(i)   // POI createRow 重复调用返回同对象
            if (startRow >= 3) {
                val t = row(0).apply {
                    createCell(0).apply { setCellStyle(titleStyle); setCellValue(title ?: importId) }
                    heightInPoints = 28f
                }
                if (cols.size > 1) sheet.addMergedRegion(org.apache.poi.ss.util.CellRangeAddress(0, 0, 0, cols.size - 1))
                row(1).createCell(0).setCellValue(
                    "填表说明: 从第 ${startRow + 1} 行起逐人填写; 列顺序与列名不可增删调换; " +
                        "日期列填 2026-11-01 式文本; 代码列填带码全称(如 1-固定期限); 连续空行即导入结束" +
                        (minCell?.let { "(至少 $it 列非空)" } ?: "") +
                        "; ※标注列系统暂不接收(表结构未含), 可留空" +
                        (DATE_HINTS[importId]?.let { "; $it" } ?: ""))
                t.rowStyle = titleStyle
            } else {
                wb.createSheet("填表说明").createRow(0).createCell(0).setCellValue(
                    "填表说明: 本模板列头在第 ${startRow} 行, 从第 ${startRow + 1} 行起逐人填写; 列顺序与列名不可增删调换; " +
                        "日期列填 2026-11-01 式文本; 代码列填带码全称(如 1-固定期限); 连续空行即导入结束" +
                        (minCell?.let { "(至少 $it 列非空)" } ?: "") +
                        "; ※标注列系统暂不接收(表结构未含), 可留空" +
                        (DATE_HINTS[importId]?.let { "; $it" } ?: ""))
            }

            val hr = row(startRow - 1)
            cols.forEachIndexed { i, name ->
                hr.createCell(i).apply { setCellStyle(headStyle); setCellValue(name) }
                sheet.setColumnWidth(i, 20 * 256)
            }
            for (r in 0 until 20) {
                val fr = row(startRow + r)
                for (i in cols.indices) fr.createCell(i).cellStyle = bodyStyle
            }

            val bytes = ByteArrayOutputStream().use { wb.write(it); it.toByteArray() }
            audit.log(null, "import:template", importId, mapOf("cols" to cols.size, "startRow" to startRow))
            val filename = URLEncoder.encode("${title ?: importId}-导入模板.xlsx", "UTF-8")
            return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename*=UTF-8''$filename")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(bytes)
        }
    }

    /** 旧 getStringLength: 非ASCII 计 2 */
    private fun cnLength(s: String): Int = s.fold(0) { acc, c -> acc + if (c.code >= 0x80) 2 else 1 }

    /**
     * 按目标列类型分型绑定(2026-11-21 补): 旧系统经 hbm 实体按属性类型 set(数值列绑数值),
     * 本引擎此前一律字符串绑定 → PG 对 numeric/date 列直接类型拒绑(Oracle 隐式转换宽容, PG 不行)。
     * 转换失败保留原串, 由逐行 INSERT 的 try/catch 走失败路径。 */
    private fun typed(value: String, colType: String?): Any? {
        if (colType == null) return value
        return runCatching {
            when {
                colType.startsWith("numeric") || colType in NUMERIC_TYPES -> java.math.BigDecimal(value)
                colType == "date" -> java.sql.Date.valueOf(value)
                colType.startsWith("timestamp") -> java.sql.Timestamp.valueOf(value)
                else -> value
            }
        }.getOrDefault(value)
    }

    private companion object {
        private val NUMERIC_TYPES = setOf("smallint", "integer", "bigint", "real", "double precision")

        /** 模板说明的日期格式例外提示(校验规则要求非通用格式的模板; 建行/吉林 day(raw.take(8))=yyyyMMdd) */
        private val DATE_HINTS = mapOf(
            "tb97_bp01" to "注意: 本模板到账日期须填 8 位 yyyyMMdd(如 20261101), 东软建行回盘同规则",
            "tb97_bp02" to "注意: 本模板到账日期须填 8 位 yyyyMMdd(如 20261101), 东软吉林银行回盘同规则",
        )
    }

    private fun next(name: String): Long =
        jdbcTemplate.queryForObject("SELECT nextval('$name')", Long::class.java)!!
}
