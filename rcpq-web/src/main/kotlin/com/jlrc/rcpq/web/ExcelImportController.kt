package com.jlrc.rcpq.web

import com.alibaba.excel.EasyExcel
import com.jlrc.rcpq.entity.TC90
import com.jlrc.rcpq.entity.TC93
import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import java.math.BigDecimal

/**
 * Excel 批量导入通道。
 *
 * /import/tdgf — 旧 tdgfService.getExcal（社保个人编号/公积金账号/医保卡号回写）。
 * /import/tddc — 旧 tddcService.getExcal（工资明细 12 类金额累加, 见下方契约）。
 */
@RestController
@RequestMapping("/import")
class ExcelImportController(
    private val jdbcTemplate: JdbcTemplate,
) {
    @PersistenceContext
    lateinit var em: EntityManager

    private val HEADERS = listOf("流水号(勿动)", "姓名", "公民身份证号", "社保个人编号", "公积金账号", "医保卡号")

    /**
     * 模板契约（与旧系统逐格一致）：首行表头 [流水号(勿动), 姓名, 公民身份证号, 社保个人编号, 公积金账号, 医保卡号]；
     * 数据行按第 0 列流水号查 TC90(atc900)，命中则更新 aaa001=社保个人编号 / aaa004=公积金账号 / aaa003=医保卡号（空值跳过）。
     * 返回码与旧系统一致：0=成功, 1=sheet错误, 2=格式/失败。
     * 已知旧实现怪癖（忠实保留）：姓名列(第1列)读取了但未参与任何写入。
     */
    @PostMapping("/tdgf")
    @Transactional
    fun importTdgf(@RequestParam("file") file: MultipartFile): String {
        val rows = mutableListOf<Map<Int, String>>()
        EasyExcel.read(file.inputStream, object : com.alibaba.excel.read.listener.ReadListener<Map<Int, String>> {
            override fun invoke(data: Map<Int, String>, context: com.alibaba.excel.context.AnalysisContext) {
                rows.add(data)
            }
            override fun doAfterAllAnalysed(context: com.alibaba.excel.context.AnalysisContext) {}
        }).sheet(0).headRowNumber(0).doRead()

        if (rows.isEmpty()) return "1"
        val header = rows.first()
        for (i in HEADERS.indices) {
            if (header[i] != HEADERS[i]) return "2"
        }

        var n = 0
        for (r in rows.drop(1)) {
            val serial = r[0]?.toString() ?: continue
            if (serial.isEmpty()) continue
            val atc900 = serial.toLongOrNull() ?: continue
            val tc90 = em.find(TC90::class.java, atc900) ?: continue
            r[3]?.takeIf { it.isNotEmpty() }?.let { tc90.aaa001 = it }
            r[4]?.takeIf { it.isNotEmpty() }?.let { tc90.aaa004 = it }
            r[5]?.takeIf { it.isNotEmpty() }?.let { tc90.aaa003 = it }
            em.merge(tc90)
            n++
        }
        return "0" // 旧契约: 成功码(更新 $n 行)
    }

    /**
     * td/d/c 工资明细 Excel 导入（旧 tddcService.getExcal 340L 的 EasyExcel 移植）。
     *
     * 模板契约: 首行表头 = title 参数(逗号分隔); 第 0 列 = TC93.ATC930 流水号;
     * 第 7 列 = atc93x(本次基本工资); 第 8 列起 = 工资项金额, 项类别取
     * AA90(atb930, acb208='0', 按 aaa901,aaa900 排序)逐列对应。
     * 12 类金额累加进 TC93(atc933~atc93x5) + 明细串(atc938~atc93x6, "列名:值，" 去尾逗号);
     * atc93aa 合计公式按原序; atc93p='0'。返回码: 0=成功, 1=sheet错误, 2=表头不符/失败(回滚)。
     * 忠实怪癖: (a)数据行遇第 0 列为空即停止(旧 while 条件; `!= ""` 为 Java 恒真死代码);
     * (b)TC94 对象只构造从未保存(死对象, 仅列合计生效); (c)atc931/932/937/time 参数读取但未使用。
     */
    @PostMapping("/tddc")
    @Transactional
    fun importTddc(
        @RequestParam("file") file: MultipartFile,
        @RequestParam("title") title: String,
        @RequestParam("atb930") atb930: String,
    ): String {
        val titleValues = title.split(",")
        val rows = mutableListOf<Map<Int, String>>()
        EasyExcel.read(file.inputStream, object : com.alibaba.excel.read.listener.ReadListener<Map<Int, String>> {
            override fun invoke(data: Map<Int, String>, context: com.alibaba.excel.context.AnalysisContext) {
                rows.add(data)
            }
            override fun doAfterAllAnalysed(context: com.alibaba.excel.context.AnalysisContext) {}
        }).sheet(0).headRowNumber(0).doRead()
        if (rows.isEmpty()) return "1" // sheet 错误
        val header = rows.first()
        for (i in titleValues.indices) {
            if (header[i] != titleValues[i]) return "2"
        }
        val categories = jdbcTemplate.query(
            "SELECT aaa901 FROM aa90 WHERE atb930=? AND acb208='0' ORDER BY aaa901, aaa900",
            { rs, _ -> rs.getString("aaa901") }, atb930.toLong())
        // 类别 → (金额累加器, 明细串)
        val sums = Array(12) { BigDecimal.ZERO }
        val details = Array(12) { "" }
        for (r in rows.drop(1)) {
            val cell0 = r[0] ?: break // 忠实: 第 0 列空 → 停止扫描
            val atc930 = cell0.toLongOrNull() ?: return "2"
            val tc93 = em.find(TC93::class.java, atc930) ?: return "2"
            val mSums = mutableMapOf<Int, BigDecimal>()
            val mDetails = mutableMapOf<Int, String>()
            for (c in 8 until titleValues.size) {
                val aaa901 = categories[c - 8] // 越界 → 异常 → 网关回 "2"(旧 IndexOutOfBounds 语义)
                val raw = r[c]
                val bd = if (raw.isNullOrEmpty()) BigDecimal.ZERO else BigDecimal(raw)
                mSums[aaa901.toInt()] = (mSums[aaa901.toInt()] ?: BigDecimal.ZERO).add(bd)
                mDetails[aaa901.toInt()] = (mDetails[aaa901.toInt()] ?: "") + titleValues[c] + "：" + (raw ?: "0") + "，"
            }
            for (k in 1..12) {
                val s = mSums[k] ?: BigDecimal.ZERO
                var d = mDetails[k] ?: ""
                if (d.isNotEmpty()) d = d.substring(0, d.length - 1)
                sums[k - 1] = s; details[k - 1] = d
            }
            val atc93x = r[7]?.takeIf { it.isNotEmpty() }?.let { BigDecimal(it) } ?: BigDecimal.ZERO
            tc93.atc93x = atc93x
            tc93.atc933 = sums[0]; tc93.atc934 = sums[1]; tc93.atc935 = sums[2]; tc93.atc936 = sums[3]
            tc93.atc93k = sums[4]; tc93.atc93l = sums[5]; tc93.atc93m = sums[6]; tc93.atc93y = sums[7]
            tc93.atc93z3 = sums[8]; tc93.atc93x1 = sums[9]; tc93.atc93x3 = sums[10]; tc93.atc93x5 = sums[11]
            // 原序合计: x + 933 - 934 - 93l - 93y + 93m + 936 + (935 + 93k + 93z3 + 93x1 + 93x3)
            tc93.atc93aa = atc93x.add(sums[0]).subtract(sums[1]).subtract(sums[5]).subtract(sums[7])
                .add(sums[6]).add(sums[3]).add(sums[2].add(sums[4]).add(sums[8]).add(sums[9]).add(sums[10]))
            tc93.atc938 = details[0]; tc93.atc939 = details[1]; tc93.atc93a = details[2]; tc93.atc93b = details[3]
            tc93.atc93h = details[4]; tc93.atc93i = details[5]; tc93.atc93j = details[6]; tc93.atc93z = details[7]
            tc93.atc93z4 = details[8]; tc93.atc93x2 = details[9]; tc93.atc93x4 = details[10]; tc93.atc93x6 = details[11]
            tc93.atc93p = "0"
            em.merge(tc93)
        }
        return "0"
    }
}
