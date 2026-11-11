package com.jlrc.rcpq.web

import org.apache.poi.ss.usermodel.CellStyle
import org.apache.poi.ss.usermodel.CellType
import org.apache.poi.ss.usermodel.Row as PoiRow
import org.apache.poi.ss.usermodel.WorkbookFactory
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Component
import java.io.ByteArrayOutputStream
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

/**
 * 报盘导出: 以 resources/baopan 局方模板为底稿, POI 打开→填充→另存, 完整保留
 * 模板说明行/列名/示例行/合并区/代码页(Sheet2 等)。
 *
 * 模板 = 最新标准(SI-Batch 输入输出/, 已入库):
 *   social_add.xlsx  企业职工批量参保登记模板(增员) — 社保失业工伤, 27 列,
 *                    R1 说明/R2 单位信息/R3 列名/R4 小明示例行必须保留, R5 起数据
 *   social_cut.xlsx  单位职工停保模板(减员) — 5 列 + 追加第 6 列「合同终止原因」
 *   medical_add.xls  001 批量职工新参保(增员) — 医保 17 列, 日期列=Excel 序列号,
 *                    按医保参保属地拆 省(2299)/市 两文件
 *   medical_cut.xls  批量减员申报(减员) — 医保 3 列, 同样拆省/市
 *
 * 业务口径 = 东软现网(员工异动/补退费管理=社保线, 市医保线): 名册由合同推导 —
 * 增员=当月合同开始且生效(atb90r='3'), 减员=当月合同终止(atc90i), 与 /wb/roster 预览同源。
 *
 * 字典映射: 民族/终止原因名读 aa10(库内码表, 东软原生);
 * 户口性质/人员身份/停保原因/属地→工作地址/默认值 为内置映射
 * (参考 SI-Batch db.py 种子, 标注为可调, 以东软现网实际报送口径逐步校正)。
 */
@Component
class BaopanExporter(private val jdbcTemplate: JdbcTemplate) {

    /** 报盘一行(增员/减员共用, 用到的字段才填充) */
    data class Row(
        val aac002: String = "",
        val aac003: String = "",
        val mz: String? = null,        // aac005 民族(码)
        val hjxz: String? = null,      // aac009 户口性质(码)
        val rySf: String? = null,      // aac012 人员身份(码)
        val salary: java.math.BigDecimal? = null, // atc905 申报工资
        val start: LocalDate? = null,  // atc90c 合同开始
        val end: LocalDate? = null,    // atc90i 合同终止
        val reason: String? = null,    // atc90k 合同终止原因(码)
        val phone: String? = null,     // aae005 联系电话
        val addr: String? = null,      // atc902 地址
        val zip: String? = null,       // aac183 邮政编码
        val locSocial: String? = null, // aaa100 社保参保属地(12 位)
        val locMedical: String? = null,// aaa110 医保参保属地(12 位)
        val medYm: Long? = null,       // aaa601 医保参保年月 yyyymm
        val aab001: Long? = null,
        val aab004: String? = null,
    )

    companion object {
        private val ID_LEN = 18
        /** Excel 1900 日期系统历元(含 1900 闰年 bug 补偿) */
        private val EXCEL_EPOCH = LocalDate.of(1899, 12, 30)
        private val ISO = DateTimeFormatter.BASIC_ISO_DATE
        private const val XLSX_TYPE = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
        private const val ZIP_TYPE = "application/zip"
        private const val PROVINCE_PREFIX = "2299"
        const val DOC_TYPE = "居民身份证（户口簿）"

        /** 户口性质: 输入码 → 医保户口性质值(可调, 参考 SI-Batch 种子) */
        val HOUSEHOLD = mapOf(
            "10" to "非农业户口（城镇）", "11" to "本地非农业户口（本地城镇）",
            "12" to "外地非农业户口（外地城镇）", "20" to "农业户口（农村）",
            "21" to "本地农业户口（本地农村）", "22" to "外地农业户口（外地农村）",
            "30" to "港澳台侨人员", "40" to "外国人")

        /** 人员身份: 91 干部→职员, 92 工人→企业职工(可调) */
        val PERSON_IDENTITY = mapOf("91" to "职员", "92" to "企业职工")

        /** 合同终止原因码 → 社保停保原因(可调, 参考 SI-Batch mapping_stop_reason) */
        val STOP_REASON = mapOf(
            "01" to "其他原因中断缴费", "02" to "人员被判刑收监执行", "03" to "用人单位辞退",
            "04" to "工作关系调转", "07" to "个人原因解除", "08" to "用人单位辞退",
            "09" to "个人原因解除", "10" to "用人单位辞退", "11" to "用人单位辞退",
            "12" to "其他原因中断缴费", "13" to "工作关系调转", "14" to "个人原因解除",
            "15" to "用人单位辞退", "16" to "人员达到享受养老待遇条件")

        /** 属地(12 位) → 工作地址(可调, 参考 SI-Batch 种子 9 值) */
        val WORK_ADDRESS = mapOf(
            "220100000000" to "长春市市本级", "220200000000" to "吉林市市本级",
            "220300000000" to "四平市市本级", "220400000000" to "辽源市市本级",
            "220500000000" to "通化市市本级", "220600000000" to "白山市市本级",
            "220700000000" to "松原市市本级", "220800000000" to "白城市市本级",
            "229900000000" to "吉林省省本级")

        /** 身份证第 17 位(index 16)奇偶推导性别: 奇=男 偶=女; 非 18 位→null */
        fun deriveGender(idCard: String?): String? {
            val t = idCard?.trim() ?: return null
            if (t.length != ID_LEN) return null
            val d = t[16]
            if (!d.isDigit()) return null
            return if (d.code % 2 == 1) "男" else "女"
        }

        /** 身份证第 7-14 位(index 6..13)推导出生日期 yyyyMMdd; 非 18 位→null */
        fun deriveBirthday(idCard: String?): String? {
            val t = idCard?.trim() ?: return null
            if (t.length != ID_LEN) return null
            val b = t.substring(6, 14)
            return if (b.all { it.isDigit() }) b else null
        }

        /** 'yyyyMMdd' → Excel 1900 序列号; 非法→null */
        fun excelSerial(ymd: String?): Double? {
            if (ymd == null || ymd.length != 8 || !ymd.all { it.isDigit() }) return null
            val d = runCatching { LocalDate.parse(ymd, ISO) }.getOrNull() ?: return null
            return java.time.temporal.ChronoUnit.DAYS.between(EXCEL_EPOCH, d).toDouble()
        }

        /** yyyymm(Long, 如 202611) → 'yyyyMMdd'(补 01); 非法→null */
        fun yyyymmToYmd(ym: Long?): String? {
            if (ym == null) return null
            val t = ym.toString().padStart(6, '0')
            if (t.length != 6 || !t.all { it.isDigit() }) return null
            val m = t.substring(4, 6).toInt()
            if (m < 1 || m > 12) return null
            return t + "01"
        }

        /** LocalDate → 'yyyyMMdd'; null→null */
        fun ymd(d: LocalDate?): String? = d?.format(ISO)

        /** 取首个 '-' 之前的码部; 无 '-' 原样 */
        fun codePrefix(v: String?): String? {
            val t = v?.trim() ?: return null
            return if (t.isEmpty()) null else t.substringBefore("-").trim().ifEmpty { null }
        }

        /** 取末个 '-' 之后的文本; 无 '-' 原样 */
        fun stripPrefix(v: String?): String? {
            val t = v?.trim() ?: return null
            return if (t.isEmpty()) null else t.substringAfterLast("-").trim().ifEmpty { null }
        }

        /** 医保属地是否省直: 属地码前 4 位以 2299 开头; 属地缺失按市(存量口径, 可调) */
        fun isProvince(loc: String?): Boolean =
            codePrefix(loc)?.startsWith(PROVINCE_PREFIX) == true

        /** 多文件打包 zip(医保省/市拆分用) */
        fun zipFiles(files: LinkedHashMap<String, ByteArray>): ByteArray {
            val bos = ByteArrayOutputStream()
            ZipOutputStream(bos).use { z ->
                for ((name, bytes) in files) {
                    z.putNextEntry(ZipEntry(name))
                    z.write(bytes)
                    z.closeEntry()
                }
            }
            return bos.toByteArray()
        }
    }

    // ---------- aa10 码表(库内, 东软原生字典) ----------

    private fun aa10Map(category: String): Map<String, String> = jdbcTemplate.query(
        "SELECT aaa102, aaa103 FROM aa10 WHERE aaa100=? ORDER BY aaa102",
        { rs, _ -> rs.getString(1) to rs.getString(2) }, category).toMap()

    /** 民族: 纯数字码→aa10(AAC005); 带前缀→剥前缀; 其他原样; 空→"" */
    private fun ethnicity(v: String?, dict: Map<String, String>): String {
        val t = v?.trim() ?: return ""
        if (t.isEmpty()) return ""
        if (t.all { it.isDigit() }) return dict[t] ?: ""
        return stripPrefix(t) ?: ""
    }

    private fun household(v: String?): String {
        val t = v?.trim() ?: return ""
        if (t.isEmpty()) return ""
        return HOUSEHOLD[t] ?: HOUSEHOLD[codePrefix(t)] ?: ""
    }

    private fun personIdentity(v: String?): String {
        val t = v?.trim() ?: return "职员"
        if (t.isEmpty()) return "职员"
        return PERSON_IDENTITY[t] ?: PERSON_IDENTITY[codePrefix(t)] ?: "职员"
    }

    /** 合同终止原因展示值: aa10(ATC90K) 有名→"码-名"; 否则原样 */
    private fun reasonText(v: String?, dict: Map<String, String>): String {
        val t = v?.trim() ?: return ""
        if (t.isEmpty()) return ""
        val name = dict[codePrefix(t)]
        return if (name != null && !t.contains("-")) "$t-$name" else t
    }

    /** 停保原因: 终止原因码 → 映射; 无映射回退原值(参考 SI-Batch 口径) */
    private fun stopReason(v: String?): String {
        val t = v?.trim() ?: return ""
        if (t.isEmpty()) return ""
        return STOP_REASON[t] ?: STOP_REASON[codePrefix(t)] ?: t
    }

    // ---------- 模板单元格工具 ----------

    private fun tpl(name: String) = javaClass.getResourceAsStream("/baopan/$name")!!

    /** 写字符串单元格: 已有单元格(含公式)先移除→新建, 沿用模板样式; null 跳过留空 */
    private fun put(row: PoiRow, col: Int, value: String?, style: CellStyle? = null) {
        if (value == null) return
        val old = row.getCell(col)
        val st = style ?: old?.cellStyle
        if (old != null) row.removeCell(old)
        val c = row.createCell(col)
        if (st != null) c.cellStyle = st
        c.setCellValue(value)
    }

    /** 写数值单元格(工资/序列号); null 跳过 */
    private fun putNum(row: PoiRow, col: Int, value: Double?, style: CellStyle? = null) {
        if (value == null) return
        val old = row.getCell(col)
        val st = style ?: old?.cellStyle
        if (old != null) row.removeCell(old)
        val c = row.createCell(col)
        if (st != null) c.cellStyle = st
        c.setCellValue(value)
    }

    /** 清除从 from 行起到底的字面值单元格, 保留公式单元格(模板预填的推导公式) */
    private fun clearLiterals(sheet: org.apache.poi.ss.usermodel.Sheet, from: Int) {
        if (sheet.lastRowNum < from) return
        for (r in from..sheet.lastRowNum) {
            val row = sheet.getRow(r) ?: continue
            row.toList().filter { it.cellType != CellType.FORMULA }.forEach { row.removeCell(it) }
        }
    }

    /** 清除从 from 行起到底的全部单元格(含公式; 医保模板无公式区) */
    private fun clearAll(sheet: org.apache.poi.ss.usermodel.Sheet, from: Int) {
        if (sheet.lastRowNum < from) return
        for (r in from..sheet.lastRowNum) {
            val row = sheet.getRow(r) ?: continue
            row.toList().forEach { row.removeCell(it) }
        }
    }

    private fun bytes(wb: org.apache.poi.ss.usermodel.Workbook): ByteArray {
        val bos = ByteArrayOutputStream()
        wb.use { it.write(bos) }
        return bos.toByteArray()
    }

    // ---------- 社保增员(企业职工批量参保登记模板, 27 列 A..AA, R5 起数据) ----------

    fun socialAdd(rows: List<Row>, unitCode: String?, unitName: String?): ByteArray {
        val ethnic = aa10Map("AAC005")
        val wb = WorkbookFactory.create(tpl("social_add.xlsx"))
        val ws = wb.getSheet("人员名单") ?: wb.getSheetAt(0)
        // R2 单位信息: B2=单位编号值, E2=单位名称值(合并区 E2:L2 锚点); 未指定则保持模板原样
        val r2 = ws.getRow(1) ?: ws.createRow(1)
        if (unitCode != null) (r2.getCell(1) ?: r2.createCell(1)).setCellValue(unitCode)
        if (unitName != null) (r2.getCell(4) ?: r2.createCell(4)).setCellValue(unitName)
        val start = 4 // 0-based → R5
        for ((i, r) in rows.withIndex()) {
            val cs = ymd(r.start)
            val row = ws.getRow(start + i) ?: ws.createRow(start + i)
            put(row, 0, DOC_TYPE)                       // A *证件类型
            put(row, 1, r.aac002)                       // B *证件号码
            put(row, 2, r.aac003)                       // C *姓名
            put(row, 3, ethnicity(r.mz, ethnic).ifEmpty { null }) // D *民族
            put(row, 4, deriveGender(r.aac002))         // E *性别(身份证 17 位推导)
            put(row, 5, deriveBirthday(r.aac002))       // F *出生日期(身份证 7-14 位)
            put(row, 6, cs)                             // G *参加工作日期=合同开始
            put(row, 7, "中国")                          // H *国籍
            put(row, 8, "非农民工")                      // I 农民工标识
            put(row, 9, personIdentity(r.rySf))         // J *人员身份
            put(row, 10, "一般账户")                     // K 临时账户标识
            putNum(row, 11, r.salary?.toDouble())       // L *申报工资
            put(row, 12, cs)                            // M *参保日期=合同开始
            put(row, 13, "是")                           // N 企业养老
            put(row, 14, "是")                           // O 失业保险
            put(row, 15, "是")                           // P 工伤保险
            put(row, 16, "否")                           // Q 是否兼职工伤
            put(row, 17, r.addr)                        // R 户口所在地址
            put(row, 18, r.addr)                        // S 常住地址
            put(row, 19, WORK_ADDRESS[r.locSocial]      // T *工作所在地址=属地映射
                ?.ifEmpty { null })
            put(row, 20, r.phone)                       // U *联系电话
            put(row, 21, "企业职工")                     // V 参保身份
            // W/Z(实际工作单位三项/外国人名) 留空
            put(row, 26, "1.0")                         // AA 模板版本
        }
        // 数据尾行之后: 清掉模板残留示例数据/字面值, 保留预填公式(空行显示为空)
        clearLiterals(ws, start + rows.size)
        return bytes(wb)
    }

    // ---------- 社保减员(单位职工停保模板, 5 列 + 追加「合同终止原因」) ----------

    fun socialCut(rows: List<Row>): ByteArray {
        val reasons = aa10Map("ATC90K")
        val wb = WorkbookFactory.create(tpl("social_cut.xlsx"))
        val ws = wb.getSheet("Sheet1")
        // 表头行(模板 R1)追加第 6 列「合同终止原因」(SI-Batch 口径: 保留原始输入值)
        val h = ws.getRow(0) ?: ws.createRow(0)
        put(h, 5, "合同终止原因")
        clearAll(ws, 1) // 清掉模板示例行(杨蕊)
        for ((i, r) in rows.withIndex()) {
            val row = ws.getRow(1 + i) ?: ws.createRow(1 + i)
            put(row, 0, r.aac002)                    // 证件号码
            put(row, 1, r.aac003)                    // 姓名
            put(row, 2, ymd(r.end))                  // 停保日期=合同终止 yyyymmdd
            put(row, 3, stopReason(r.reason))        // 停保原因=终止原因映射
            put(row, 4, "")                          // 备注
            put(row, 5, reasonText(r.reason, reasons)) // 合同终止原因(原值, 码-名)
        }
        return bytes(wb)
    }

    // ---------- 医保增员(001 批量职工新参保, 17 列, 日期=序列号, 省/市拆分) ----------

    fun medicalAdd(rows: List<Row>, ym: String): LinkedHashMap<String, ByteArray> {
        val out = LinkedHashMap<String, ByteArray>()
        val (province, city) = splitByLocation(rows)
        if (province.isNotEmpty())
            out["省医保增员_$ym.xls"] = medicalAddFile(province, true)
        if (city.isNotEmpty())
            out["市医保增员_$ym.xls"] = medicalAddFile(city, false)
        return out
    }

    private fun medicalAddFile(rows: List<Row>, province: Boolean): ByteArray {
        val ethnic = aa10Map("AAC005")
        val wb = WorkbookFactory.create(tpl("medical_add.xls"))
        val ws = wb.getSheet("Sheet1")
        val textFmt = wb.createDataFormat().getFormat("@")
        val dateFmt = wb.createDataFormat().getFormat("m/d/yy")
        val textStyle = wb.createCellStyle().apply { setDataFormat(textFmt) }
        val dateStyle = wb.createCellStyle().apply { setDataFormat(dateFmt) }
        clearAll(ws, 1) // 清掉模板示例行(战旭东等)
        for ((i, r) in rows.withIndex()) {
            val row = ws.getRow(1 + i) ?: ws.createRow(1 + i)
            put(row, 0, DOC_TYPE)                                   // 证件类型
            put(row, 1, r.aac002, textStyle)                        // 证件号码(text)
            put(row, 2, r.aac003)                                   // 姓名
            put(row, 3, deriveGender(r.aac002))                     // 性别(身份证推导)
            put(row, 4, ethnicity(r.mz, ethnic).ifEmpty { null })   // 民族
            putNum(row, 5, excelSerial(deriveBirthday(r.aac002)), dateStyle) // 出生日期(序列号)
            putNum(row, 6, excelSerial(ymd(r.start)), dateStyle)    // 首次参加工作日期=合同开始
            put(row, 7, household(r.hjxz).ifEmpty { null })         // 户口性质(字典映射)
            put(row, 8, "未说明的婚姻状况")                           // 婚姻状态
            put(row, 9, "健康或良好")                                // 健康状况
            put(row, 10, r.addr)                                    // 居住地址
            put(row, 11, r.zip, textStyle)                          // 居住地邮政编码(text)
            // 固定电话: 留空
            put(row, 13, r.phone, textStyle)                        // 手机号码(text)
            putNum(row, 14, r.salary?.toDouble())                   // 申报工资
            put(row, 15, "非在编")                                   // 编制类型
            // 本次参保时间: 省=合同开始; 市=医保参保年月+01(东软口径)
            val enroll = if (province) ymd(r.start) else yyyymmToYmd(r.medYm)
            putNum(row, 16, excelSerial(enroll), dateStyle)
        }
        return bytes(wb)
    }

    // ---------- 医保减员(批量减员申报, 3 列, 省/市拆分) ----------

    fun medicalCut(rows: List<Row>, ym: String): LinkedHashMap<String, ByteArray> {
        val out = LinkedHashMap<String, ByteArray>()
        val (province, city) = splitByLocation(rows)
        if (province.isNotEmpty())
            out["省医保减员_$ym.xls"] = medicalCutFile(province)
        if (city.isNotEmpty())
            out["市医保减员_$ym.xls"] = medicalCutFile(city)
        return out
    }

    private fun medicalCutFile(rows: List<Row>): ByteArray {
        val wb = WorkbookFactory.create(tpl("medical_cut.xls"))
        val ws = wb.getSheet("Sheet1")
        val textFmt = wb.createDataFormat().getFormat("@")
        val textStyle = wb.createCellStyle().apply { setDataFormat(textFmt) }
        clearAll(ws, 1) // 清掉模板示例行(韩艳蕾等)
        for ((i, r) in rows.withIndex()) {
            val row = ws.getRow(1 + i) ?: ws.createRow(1 + i)
            put(row, 0, r.aac003)            // 姓名
            put(row, 1, DOC_TYPE)            // 证件类型(默认居民身份证（户口簿）)
            put(row, 2, r.aac002, textStyle) // 证件号码
        }
        return bytes(wb)
    }

    /** 按医保参保属地拆省/市; 属地缺失归市(存量口径, 可调) */
    private fun splitByLocation(rows: List<Row>): Pair<List<Row>, List<Row>> =
        rows.partition { isProvince(it.locMedical) }
}
