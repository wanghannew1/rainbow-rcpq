package com.jlrc.rcpq.web

import com.alibaba.excel.EasyExcel
import com.jlrc.rcpq.entity.TC90
import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile

/**
 * Excel 批量导入（旧 tdgfService.getExcal 的 EasyExcel 移植，ADR D1）。
 *
 * 模板契约（与旧系统逐格一致）：首行表头 [流水号(勿动), 姓名, 公民身份证号, 社保个人编号, 公积金账号, 医保卡号]；
 * 数据行按第 0 列流水号查 TC90(atc900)，命中则更新 aaa001=社保个人编号 / aaa004=公积金账号 / aaa003=医保卡号（空值跳过）。
 * 返回码与旧系统一致：0=成功, 1=sheet错误, 2=格式/失败。
 * 已知旧实现怪癖（忠实保留）：姓名列(第1列)读取了但未参与任何写入。
 */
@RestController
@RequestMapping("/import")
class ExcelImportController {
    @PersistenceContext
    lateinit var em: EntityManager

    private val HEADERS = listOf("流水号(勿动)", "姓名", "公民身份证号", "社保个人编号", "公积金账号", "医保卡号")

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
}
