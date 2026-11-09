package com.jlrc.rcpq.service.td

import com.fasterxml.jackson.databind.ObjectMapper
import com.jlrc.rcpq.entity.TC9W
import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * td/c/k 社保报增报减中枢（旧 tdckService 67L + tdckAjax 80L 规格重写）。
 *
 * 交互用例（对应原 IDataCenter 方法）:
 * - delete/delete1: 按(年月,户头,区划)整批删 TC9W_BP（两 store 同语义合并一实现）
 * - doSave: TC9W.aaa001 与库中不一致时同步 TC90.AAA001（单位编号变更联动）
 * - doAjax(报增/报减处理, AJAX-tdck_tdckAjax 通道):
 *   · 续月模式(atc9w0 为空): TC90/TC90_LIST 报增起始月(aaa201)或报减终止月(aaa202) +1 月;
 *     TC9W 待审行(atc9w8='3',atc9w9='1'报增/'0'报减,atc9wb='2')同字段 +1 月,
 *     aae013 追加"社保报增(减)失败,自动延续至<新月>进行报增(减)"(取旧值拼接, Oracle SET 同值语义)
 *   · 审核模式(atc9w0 非空): TC90/TC90_LIST.atc90p = '0'(报增bz)/'1'(报减);
 *     TC9W 置 atc9wb='1', aae013=null, atc9wg=now, atc9wh=COALESCE(旧,now), atc9wf='1'
 *
 * 过程用例（lemis.util.getProcedure 通道, /proc/{pkg}.{name}）:
 * - PRC_社保异动回盘校验: TC9W_BP 文本清洗 → 年月格式校验 → 逐行按申报结果(aaa90w)
 *   校验并回填匹配合同(atc900=max(在保新参保行)), ace10a=3通过/2退回+ace10b原因
 * - PRC_社保异动回盘提交: 停保行→TC90 atc90p='1'(特殊情况 TC06 例外);
 *   成功行→TC90 参保落地(aaa203=回盘参保年月, aaa001 社保编号, aac008 '21'含'27'补标,
 *   atc90p='0') + TC90_YY cbsign='0'; 异议行→PRC_参保异议赋值 记 TC90_YY + atc90p='2';
 *   全批盖章经办人
 * - PRC_参保异议赋值: TC90_YY 异议行构造(SAFESIGN 1-4 字段映射), 删旧插新(DUP→成功)
 */
@Component
class TdckCases(
    private val jdbcTemplate: JdbcTemplate,
    private val objectMapper: ObjectMapper,
) {
    @PersistenceContext
    lateinit var em: EntityManager

    /** Oracle 常量 DEF_OK/DEF_ERR */
    private val ok = 1
    private val err = -1

    private fun toEntityTC9W(row: Map<String, Any?>): TC9W =
        objectMapper.convertValue(row.filterKeys { it != "__state" }, TC9W::class.java)

    // ---------- delete / delete1（两 store 同语义） ----------

    @Transactional
    fun deleteTc9wBp(aae002: String, aaa030: String, aaa020: String): Map<String, Any?> {
        // aae002/aaa030 为 numeric 列; aaa020 为 varchar
        val n = jdbcTemplate.update(
            "DELETE FROM tc9w_bp WHERE aae002=? AND aaa030=? AND aaa020=?",
            aae002.toLong(), aaa030.toLong(), aaa020)
        return mapOf("deleted" to n)
    }

    // ---------- doSave（TC9W 保存 + 单位编号联动） ----------

    @Transactional
    fun doSave(tc9wRow: Map<String, Any?>): Map<String, Any?> {
        val tc9w = toEntityTC9W(tc9wRow)
        val atc9w0 = tc9w.atc9w0 ?: throw IllegalArgumentException("缺少 atc9w0")
        val aaa001Before = jdbcTemplate.queryForObject(
            "SELECT aaa001 FROM tc9w WHERE atc9w0=?", String::class.java, atc9w0)
        var synced = 0
        if (aaa001Before != tc9w.aaa001) {
            synced = jdbcTemplate.update(
                "UPDATE tc90 SET aaa001=? WHERE atc900=?", tc9w.aaa001, tc9w.atc900)
        }
        em.merge(tc9w)
        return mapOf("atc9w0" to atc9w0, "tc90Synced" to synced)
    }

    // ---------- doAjax 报增/报减处理 ----------

    /**
     * 报增/报减处理。忠实保留旧系统"部分提交"可观测行为:
     * TC90/TC90_LIST 的更新生效（旧 SimpleAjax 无事务包裹, bulkUpdate 各自自动提交）;
     * 随后的 TC9W 更新引用 tc9w 不存在的列(aaa201/atc9w9/atc9wb/atc9wg/atc9wh/atc9wf,
     * 旧 TC9W 实体 88 字段亦无 → 旧 Hibernate HQL 编译即抛), 抛错给页面——与旧一致。
     */
    fun doAjax(atc9w0: String, atc900: String, flag: String): Map<String, Any?> {
        if (atc9w0.isEmpty()) {
            // 续月模式: 报增失败→aaa201 延续 / 报减失败→aaa202 延续
            // aaa201/aaa202 为 numeric 月份列; aae013 拼接取旧值(Oracle/PG UPDATE SET 均读旧行值)
            for (atc900One in atc900.split(",").filter { it.isNotBlank() }) {
                if (flag == "bz") {
                    jdbcTemplate.update(
                        """UPDATE tc90 SET aaa201 = to_char(to_date(aaa201::text,'YYYYMM') + interval '1 month','YYYYMM')::numeric
                           WHERE atc900=?""", atc900One.toLong())
                    jdbcTemplate.update(
                        """UPDATE tc90_list SET aaa201 = to_char(to_date(aaa201::text,'YYYYMM') + interval '1 month','YYYYMM')::numeric
                           WHERE atc900=?""", atc900One.toLong())
                } else {
                    jdbcTemplate.update(
                        """UPDATE tc90 SET aaa202 = to_char(to_date(aaa202::text,'YYYYMM') + interval '1 month','YYYYMM')::numeric
                           WHERE atc900=?""", atc900One.toLong())
                    jdbcTemplate.update(
                        """UPDATE tc90_list SET aaa202 = to_char(to_date(aaa202::text,'YYYYMM') + interval '1 month','YYYYMM')::numeric
                           WHERE atc900=?""", atc900One.toLong())
                }
            }
            throw IllegalStateException(brokenTc9wMessage("续月模式"))
        }
        // 审核模式: atc90p 置 '0'(报增)/'1'(报减), TC9W 审核通过
        val newP = if (flag == "bz") "0" else "1"
        for (i in atc9w0.split(",").filter { it.isNotBlank() }.withIndex()) {
            val atc9w0One = i.value
            val atc900One = atc900.split(",").getOrNull(i.index) ?: ""
            if (atc900One.isNotBlank()) {
                jdbcTemplate.update("UPDATE tc90 SET atc90p=? WHERE atc900=?", newP, atc900One.toLong())
                jdbcTemplate.update("UPDATE tc90_list SET atc90p=? WHERE atc900=?", newP, atc900One.toLong())
            }
        }
        throw IllegalStateException(brokenTc9wMessage("审核模式"))
    }

    private fun brokenTc9wMessage(mode: String): String =
        "$mode TC9W 更新在旧系统即失败: HQL 引用 tc9w 不存在的列(aaa201/atc9w9/atc9wb/atc9wg/" +
            "atc9wh/atc9wf, 旧实体亦无) → Hibernate 抛 could not resolve property。本实现保持" +
            "同行为: TC90/TC90_LIST 部分提交已生效, 此处按旧向页面报错。"

    // ---------- PRC_社保异动回盘校验 ----------

    @Transactional
    fun fallbackCheck(aae002: String, aaa030: String, aaa020: String): Map<String, Any?> {
        // 年月格式(原 TO_DATE(aae002||'01','yyyy-mm-dd') 宽容解析 = 6 位有效年月)
        val aae002N = runCatching { aae002.toLong() }.getOrElse {
            return mapOf("code" to err, "msg" to "社保年月格式不正确|")
        }
        runCatching { java.time.YearMonth.parse(aae002, DateTimeFormatter.ofPattern("yyyyMM")) }
            .getOrElse { return mapOf("code" to err, "msg" to "社保年月格式不正确|") }
        // 文本清洗(回车/换行/制表/空格; 身份证 x→X); aaa030/aae002 为 numeric 列
        jdbcTemplate.update(
            """UPDATE tc9w_bp SET
               aac002 = replace(replace(replace(replace(replace(aac002, chr(13), ''), chr(10), ''), chr(9), ''), 'x', 'X'), ' ', ''),
               aac003 = replace(replace(replace(replace(aac003, chr(13), ''), chr(10), ''), chr(9), ''), ' ', ''),
               abp001 = replace(replace(replace(replace(abp001, chr(13), ''), chr(10), ''), chr(9), ''), ' ', '')
               WHERE aaa030=? AND aaa020=? AND aae002=?""",
            aaa030.toLong(), aaa020, aae002N)
        // 待审 TC9W 置 '4'
        jdbcTemplate.update(
            """UPDATE tc9w SET atc9w8='4' WHERE atc9w8='1' AND aaa100=? AND aaa030=? AND aae002=?""",
            aaa020, aaa030.toLong(), aae002N)
        // 逐行校验(按 id 主键迭代 = 原 ROWID 语义)
        data class Row(val id: Long, val aaa90w: String?, val aaa001: String?, val abp001: String?,
                       val aac002: String?, val aae001: String?, val atb931: String?,
                       val aaa201_1: String?, val aaa201_2: String?)
        val rows = jdbcTemplate.query(
            """SELECT id, aaa90w, aaa001, abp001, aac002, aae001, atb931, aaa201_1, aaa201_2
               FROM tc9w_bp WHERE aaa030=? AND aaa020=? AND aae002=?""",
            { rs, _ -> Row(rs.getLong("id"), rs.getString("aaa90w"), rs.getString("aaa001"),
                rs.getString("abp001"), rs.getString("aac002"), rs.getString("aae001"),
                rs.getString("atb931"), rs.getString("aaa201_1"), rs.getString("aaa201_2")) },
            aaa030.toLong(), aaa020, aae002N)
        var pass = 0
        var reject = 0
        for (rec in rows) {
            var nOk = "3"
            var msg: String? = null
            var atc900: Long? = null
            var atb930: Long? = null
            var aab001: Long? = null
            var aab004: String? = null
            var aac008: String? = null
            var valid = true
            if (rec.aaa90w != "1" && rec.aaa90w != "2") {
                nOk = "2"; msg = (msg ?: "") + "申报结果格式不正确|"; valid = false
            }
            if (valid) {
                // 匹配在保员工: atc900 = max(atc900 of aaa401='1' 新参保系(aae00p 1/5/7) 同区划同证件)
                val found = jdbcTemplate.query(
                    """SELECT atc900, atb930, aab001, aab004, aac008 FROM tc90
                       WHERE atc900 = (SELECT max(atc900) FROM tc90
                                       WHERE aaa401='1' AND aae00p IN ('1','5','7')
                                         AND aaa100=? AND aac002=?)""",
                    { rs, _ -> arrayOf(rs.getLong("atc900"), rs.getObject("atb930"),
                        rs.getObject("aab001"), rs.getString("aab004"), rs.getString("aac008")) },
                    aaa020, rec.aac002).firstOrNull()
                if (found == null) {
                    nOk = "2"; msg = (msg ?: "") + "匹配员工信息出错|"
                } else {
                    atc900 = found[0] as Long
                    atb930 = (found[1] as Number?)?.toLong()
                    aab001 = (found[2] as Number?)?.toLong()
                    aab004 = found[3] as String?
                    aac008 = found[4] as String?
                }
            }
            if (valid && rec.aaa90w == "1") {
                if (rec.aaa001.isNullOrBlank()) {
                    nOk = "2"; msg = (msg ?: "") + "参保成功的人员必须填写社保个人编号|"; valid = false
                } else if (rec.aaa001.contains('.')) {
                    nOk = "2"; msg = (msg ?: "") + "社保个人编号格式不正确|"; valid = false
                } else runCatching { LocalDate.parse(rec.abp001!!.take(10)) }.getOrElse {
                    nOk = "2"; msg = (msg ?: "") + "参保年月格式不正确|"; valid = false
                }
            }
            jdbcTemplate.update(
                """UPDATE tc9w_bp SET ace10a=?, ace10b=?, aae001=?, atc900=?, atb930=?, atb931=?,
                   aab001=?, aab004=?, aaa90x=null, aaa201_1=?, aaa201_2=?, aac008_1=?, aac008_2=null
                   WHERE id=?""",
                nOk, msg, rec.aae001, atc900, atb930, rec.atb931, aab001, aab004,
                rec.aaa201_1, rec.aaa201_2, aac008, rec.id)
            if (nOk == "3") pass++ else reject++
        }
        return mapOf("code" to ok, "msg" to null as String?, "rows" to rows.size, "pass" to pass, "reject" to reject)
    }

    // ---------- PRC_社保异动回盘提交 ----------

    @Transactional
    fun fallbackCommit(aae002: String, aaa030: String, aaa020: String, aae011: String): Map<String, Any?> {
        val aae002N = aae002.toLongOrNull()
            ?: return mapOf("code" to err, "msg" to "社保年月格式不正确|")
        // 经办人(原 SYS_ORG_USER_UNIT 单行查询, 多行/无行即 -1)
        val user = jdbcTemplate.query(
            "SELECT aae011, aae017, aae019, aae020, aae022 FROM sys_org_user_unit WHERE aae011=?",
            { rs, _ -> arrayOf(rs.getString("aae011"), rs.getString("aae017"), rs.getString("aae019"),
                rs.getString("aae020"), rs.getString("aae022")) },
            aae011).firstOrNull()
            ?: return mapOf("code" to err, "msg" to "获取经办人信息出错")
        // 停保处理: TC9W atc9w1='3' → TC90 atc90p='1'(无特殊情况时); aaa204/aaa209 为 numeric
        data class TzRow(val atc900: Long?, val aac003: String?)
        val tzRows = jdbcTemplate.query(
            "SELECT atc900, aac003 FROM tc9w WHERE atc9w1='3' AND aaa030=? AND aaa100=? AND aae002=?",
            { rs, _ -> TzRow((rs.getObject("atc900") as Number?)?.toLong(), rs.getString("aac003")) },
            aaa030.toLong(), aaa020, aae002N)
        var tzApplied = 0
        for (tb in tzRows) {
            val atc900 = tb.atc900 ?: return mapOf("code" to err, "msg" to "【${tb.aac003}】新参保更新出错")
            val nSpecial = jdbcTemplate.queryForObject(
                """SELECT count(*) FROM tc06 WHERE aae100='1' AND atc062 IN ('1','2')
                   AND (aae002=? OR (aae002<=? AND atc06a>=?))
                   AND aac001=(SELECT aac001 FROM tc90 WHERE atc900=?)""",
                Int::class.java, aae002N, aae002N, aae002N, atc900)
            if (nSpecial == 0) {
                jdbcTemplate.update(
                    """UPDATE tc90 SET
                       aaa204 = CASE WHEN atc90p='0' THEN ? ELSE aaa204 END,
                       aaa209 = CASE WHEN atc90p='0' THEN ? ELSE aaa209 END,
                       atc90p = '1'
                       WHERE atc900=?""", aae002N, aae002N, atc900)
                tzApplied++
            }
        }
        // 参保成功处理
        data class CbRow(val atc900: Long?, val aac003: String?, val aaa001: String?, val abp001: String?, val aac009: String?)
        val cbRows = jdbcTemplate.query(
            "SELECT atc900, aac003, aaa001, abp001, aac009 FROM tc9w_bp WHERE ace10a='3' AND aaa90w='1' AND aaa030=? AND aaa020=? AND aae002=?",
            { rs, _ -> CbRow((rs.getObject("atc900") as Number?)?.toLong(), rs.getString("aac003"), rs.getString("aaa001"),
                rs.getString("abp001"), rs.getString("aac009")) },
            aaa030.toLong(), aaa020, aae002N)
        var cbApplied = 0
        for (cb in cbRows) {
            val atc900 = cb.atc900 ?: return mapOf("code" to err, "msg" to "【${cb.aac003}】新参保更新出错")
            val cbsign = cb.abp001?.take(10)?.let { runCatching { LocalDate.parse(it) }.getOrNull() }
                ?: return mapOf("code" to err, "msg" to "【${cb.aac003}】新参保更新出错")
            val cbl203 = java.time.YearMonth.from(cbsign).format(DateTimeFormatter.ofPattern("yyyyMM")).toLong()
            val n = jdbcTemplate.update(
                """UPDATE tc90 SET
                   aaa203 = ?,
                   aaa208 = CASE WHEN atc90p IS NULL OR atc90p='2' THEN ? ELSE aaa208 END,
                   atc90p = '0',
                   aaa001 = ?,
                   aac008 = CASE
                     WHEN strpos(?, '21') > 0 AND (aac008 IS NULL OR aac008='27') THEN '27'
                     WHEN strpos(?, '21') > 0 AND aac008 IS NOT NULL AND instr(aac008,'27')=0 THEN aac008 || ',27'
                     ELSE aac008 END
                   WHERE atc900=?""",
                cbl203, aae002N, cb.aaa001, cb.aac009, cb.aac009, atc900)
            if (n == 0) return mapOf("code" to err, "msg" to "【${cb.aac003}】新参保更新出错")
            jdbcTemplate.update(
                "UPDATE tc90_yy SET cbsign='0', aaa203=? WHERE safesign='1' AND atc900=?", cbl203, atc900)
            cbApplied++
        }
        // 异议处理
        data class YyRow(val atc900: Long?, val aae013: String?)
        val yyRows = jdbcTemplate.query(
            "SELECT atc900, aae013 FROM tc9w_bp WHERE ace10a='3' AND aaa90w='2' AND aaa030=? AND aaa020=? AND aae002=?",
            { rs, _ -> YyRow((rs.getObject("atc900") as Number?)?.toLong(), rs.getString("aae013")) },
            aaa030.toLong(), aaa020, aae002N)
        var yyApplied = 0
        for (yy in yyRows) {
            val atc900 = yy.atc900 ?: continue
            val r = objectionAssign(atc900.toString(), "1", aae002, yy.aae013 ?: "")
            if ((r["code"] as Int) != ok) return r
            jdbcTemplate.update("UPDATE tc90 SET atc90p='2', aaa208=? WHERE atc900=?", aae002N, atc900)
            yyApplied++
        }
        // 全批盖章经办人
        jdbcTemplate.update(
            """UPDATE tc9w_bp SET aae211=?, aae217=?, aae236=now(), aae219=?, aae220=?, aae222=?
               WHERE aaa030=? AND aaa020=? AND aae002=?""",
            user[0], user[1], user[2], user[3], user[4], aaa030.toLong(), aaa020, aae002N)
        return mapOf("code" to ok, "msg" to null as String?,
            "stopApplied" to tzApplied, "successApplied" to cbApplied, "objectionApplied" to yyApplied)
    }

    // ---------- PRC_参保异议赋值 ----------

    @Transactional
    fun objectionAssign(atc900Str: String, safesign: String, aae002: String, aae013: String): Map<String, Any?> {
        val atc900 = atc900Str.toLongOrNull() ?: return mapOf("code" to err, "msg" to "查询合同出错")
        val tc90 = jdbcTemplate.query(
            """SELECT baz001, baz002, atb930, atb900, aab001, aab004, zynum, aac001, aac003, aac002,
                      aac004, aac006, atc905, atc90c, atc90d,
                      aaa301, aaa100, aaa101, aaa201, aaa203,
                      aaa302, aaa110, aaa111, aaa601, aaa603,
                      aaa305, aaa130, aaa131, aaa901, aaa903,
                      aaa303, aaa120, aaa121, aaa501, aaa503
               FROM tc90 WHERE atc900=?""",
            { rs, _ -> (1..35).map { rs.getObject(it) } }, atc900).firstOrNull()
            ?: return mapOf("code" to err, "msg" to "查询合同出错")
        val tb931 = jdbcTemplate.query(
            "SELECT atb931 FROM tb93 WHERE atb930=?", { rs, _ -> rs.getString("atb931") }, tc90[2])
            .firstOrNull() ?: return mapOf("code" to err, "msg" to "查询结算单元名称出错")
        val unit = jdbcTemplate.query(
            "SELECT aaa030, aaa031, bae011 FROM ab01 WHERE aab001=?",
            { rs, _ -> arrayOf(rs.getObject("aaa030"), rs.getString("aaa031"), rs.getString("bae011")) },
            tc90[4]).firstOrNull() ?: return mapOf("code" to err, "msg" to "查询险金户头出错")
        // SAFESIGN 字段映射(1社保/2医保/3工伤?/4公积金 — 与旧 IN 分支一致)
        val (aaa310, aaa020, aaa021, aaa201, aaa203) = when (safesign) {
            "1" -> listOf(tc90[15], tc90[16], tc90[17], tc90[18], tc90[19])
            "2" -> listOf(tc90[20], tc90[21], tc90[22], tc90[23], tc90[24])
            "3" -> listOf(tc90[25], tc90[26], tc90[27], tc90[28], tc90[29])
            "4" -> listOf(tc90[30], tc90[31], tc90[32], tc90[33], tc90[34])
            else -> listOf(null, null, null, null, null)
        }
        jdbcTemplate.update("DELETE FROM tc90_yy WHERE safesign=? AND atc900=?", safesign, atc900)
        val seqId = jdbcTemplate.queryForObject(
            "SELECT nextval('seq_tc90_yy_tc90yyid')", Long::class.java)
        val n = jdbcTemplate.update(
            """INSERT INTO tc90_yy (tc90yyid, atb931, aaa030, aaa031, bae011, baz001, baz002,
               aae002, atc900, atb930, atb900, aab001, aab004, zynum, aac001, aac003, aac002,
               aac004, aac006, atc905, safesign, aaa310, aaa020, aaa021, aaa201, aaa203,
               atc90c, atc90d, backsign, insign, cbsign, aae013)
               VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, '0', '0', '2', ?)""",
            seqId, tb931, unit[0], unit[1], unit[2], tc90[0], tc90[1],
            aae002.toLongOrNull(), atc900, tc90[2], tc90[3], tc90[4], tc90[5], tc90[6], tc90[7], tc90[8], tc90[9],
            tc90[10], tc90[11], tc90[12], safesign, aaa310, aaa020, aaa021, aaa201, aaa203,
            tc90[13], tc90[14], aae013)
        return mapOf("code" to ok, "msg" to null as String?, "tc90yyid" to seqId, "inserted" to n)
    }
}
