package com.jlrc.rcpq.service.td

import com.jlrc.rcpq.service.proc.ProcHandler
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter

/**
 * 合同 Excel 导入（旧 PKG_IMP_FZ.PRC_TC90_IMP 700L + PRC_COMMIT_TC90_IMP 427L 规格重写）。
 *
 * checkTc90Imp: 批次行全量校验。即时规则(结算单元/部门/身份证/退休年龄/姓名/合同互斥)
 * 中止行；累积规则(必填/字典/格式/基数上下限/日期序)汇入 ACE10B 后统一判定。
 * 成功行回填解析列(原 SET ROW)。commitTc90Imp: AC01(新/改)+TC90(杭州属地硬编码
 * AAA100='330100000000')+TB91(ATB911='20')+补缴判定(TB93.ATB90AE vs ATC904→ATB90R='4'/'2')。
 * TC90 写入采用列映射动态 INSERT(等价原 INSERT INTO TC90 VALUES R_TC90 整行)。
 */
@Component
class TdcTc90ImpCases(
    private val jdbcTemplate: JdbcTemplate,
    private val hzCases: TdcImpHzCases,
) {
    val ok = 1
    val err = -1
    private val NF = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    private class RuleException(val msg: String) : Exception(msg)

    fun parseId(id: String): Array<Any?> = hzCases.parseAac002(id)

    // ---------- PRC_TC90_IMP ----------

    @Transactional
    fun checkTc90Imp(ace100: String): Map<String, Any?> {
        val ace100N = ace100.toLongOrNull()
            ?: return mapOf("appCode" to err, "errMsg" to "员工合同校验出错，错误信息：批次号格式不正确")
        jdbcTemplate.update(
            "UPDATE tc90_imp SET aac002 = upper(regexp_replace(aac002, '\\s', '', 'g')), " +
                "aac003 = regexp_replace(aac003, '\\s', '', 'g') WHERE ace100=?", ace100N)
        val curYm = YearMonth.now().format(DateTimeFormatter.ofPattern("yyyyMM")).toLong()
        val sbBound = aa98Bound("1", curYm)
            ?: return mapOf("appCode" to err, "errMsg" to "未查询到当前年月的最低最高缴费标准设置。")
        val gjjBound = aa98Bound("2", curYm)
            ?: return mapOf("appCode" to err, "errMsg" to "未查询到当前年月的最低最高缴费标准设置。")
        val ret = jdbcTemplate.query(
            "SELECT aaa993, aaa994 FROM aa99 WHERE aaa991='2'",
            { rs, _ -> arrayOf(rs.getInt("aaa993"), rs.getInt("aaa994")) }).firstOrNull()
            ?: return mapOf("appCode" to err, "errMsg" to "未查询到退休年龄的设置。")
        val today = LocalDate.now()
        val txNv = today.minusMonths(ret[0].toLong() * 12)
        val txNan = today.minusMonths(ret[1].toLong() * 12)
        val rows = jdbcTemplate.query(
            "SELECT id FROM tc90_imp WHERE ace10a IS NULL AND ace100=?",
            { rs, _ -> rs.getLong("id") }, ace100N)
        var pass = 0
        var reject = 0
        for (id in rows) {
            try {
                validateRow(id, sbBound, gjjBound, txNv, txNan)
                pass++
            } catch (e: RuleException) {
                jdbcTemplate.update("UPDATE tc90_imp SET ace10a='2', ace10b=? WHERE id=?", e.msg, id)
                reject++
            }
        }
        return mapOf("appCode" to ok, "errMsg" to null as String?,
            "rows" to rows.size, "pass" to pass, "reject" to reject)
    }

    private fun aa98Bound(cat: String, ym: Long): Pair<BigDecimal, BigDecimal>? = jdbcTemplate.query(
        "SELECT aaa985, aaa986 FROM aa98 WHERE aaa981=? AND aaa987<=? AND aaa988>=?",
        { rs, _ -> Pair(rs.getBigDecimal("aaa985"), rs.getBigDecimal("aaa986")) },
        cat, ym, ym).firstOrNull()

    /** 行全列加载(下线列名 → 值) */
    private fun loadRow(id: Long): Map<String, Any?> = jdbcTemplate.query(
        "SELECT * FROM tc90_imp WHERE id=?", { rs, _ ->
        val md = rs.metaData
        (1..md.columnCount).associate { md.getColumnLabel(it).lowercase() to rs.getObject(it) }
    }, id).firstOrNull()!!

    private fun validateRow(
        id: Long, sbBound: Pair<BigDecimal, BigDecimal>,
        gjjBound: Pair<BigDecimal, BigDecimal>, txNv: LocalDate, txNan: LocalDate,
    ) {
        val f = loadRow(id)
        val str = { k: String -> when (val v = f[k]) { null -> null; else -> v.toString() } }
        val num = { k: String -> (f[k] as? BigDecimal) ?: (f[k] as? Number)?.let { BigDecimal(it.toString()) } }
        val msg = StringBuilder()
        var nOk = "3"
        fun abort(m: String): Nothing = throw RuleException(m)
        fun accum(m: String) { nOk = "2"; msg.append(m) }

        // 即时规则 1-4
        val t930 = str("atb930") ?: abort("结算单元主键不可为空|")
        val unit = jdbcTemplate.query(
            """SELECT a.atb900::text a900, a.aab001::text ab0, b.id::text bid, b.aab004 a4, a.atb931
               FROM tb93 a, ab01 b WHERE a.aab001=b.aab001 AND a.atb930::text=?""",
            { rs, _ -> arrayOf<Any?>(rs.getString("a900"), rs.getString("ab0"), rs.getString("bid"),
                rs.getString("a4"), rs.getString("atb931")) }, t930).firstOrNull()
            ?: abort("通过结算单元主键未查询到结算单元|")
        val ab1 = str("aab001_1") ?: abort("部门主键不可为空|")
        val dept = jdbcTemplate.query(
            "SELECT id::text bid, aab004 a4 FROM ab01_child WHERE root=? AND aab001=?::numeric",
            { rs, _ -> arrayOf<Any?>(rs.getString("bid"), rs.getString("a4")) },
            unit[2], ab1).firstOrNull() ?: abort("部门与结算单元未对应|")
        val aac002 = str("aac002") ?: abort("公民身份证号码不可为空|")
        val parsed = parseId(aac002)
        if ((parsed[0] as Int) != 1) abort("公民身份证号码不合法|")
        val aac004 = parsed[1] as String
        val aac006 = parsed[2] as String?
        val birth = aac006?.let { runCatching { LocalDate.parse(it, NF) }.getOrNull() }
        if (aac004 == "1") {
            if (birth != null && birth.isBefore(txNan)) abort("已到达退休年龄|")
        } else {
            if (birth != null && birth.isBefore(txNv)) abort("已到达退休年龄|")
        }
        val aac003 = str("aac003") ?: abort("姓名不可为空|")
        var aac001: Long? = null
        val ac01Row = jdbcTemplate.query(
            "SELECT aac001::text a1, aac003 a3 FROM ac01 WHERE aac002=?",
            { rs, _ -> arrayOf<Any?>(rs.getString("a1"), rs.getString("a3")) }, aac002).firstOrNull()
        if (ac01Row != null) {
            aac001 = (ac01Row[0] as String).toLongOrNull()
            if (ac01Row[1] != aac003) abort("基本信息中姓名与导入姓名不同|")
            if (jdbcTemplate.queryForObject(
                    "SELECT count(1) FROM tc90 WHERE aae100='1' AND atb90r='3' AND aac001=?",
                    Int::class.java, aac001) > 0) abort("存在有效的派遣合同|")
            if (jdbcTemplate.queryForObject(
                    "SELECT count(1) FROM tc90 WHERE atb90r<>'3' AND atb90r<>'5' AND aac001=?",
                    Int::class.java, aac001) > 0) abort("存在审核中的派遣合同|")
        }
        // ===== 累积规则区 =====
        val aac009 = str("aac009")
        if (aac009 == null) accum("户口性质必填|")
        val aae008 = str("aae008")
        if (aae008 == null) accum("邮编必填|")
        else if (aae008.length != 6) accum("请输入正确的邮编|")
        else if (aae008.toLongOrNull() == null) accum("邮编格式错误|")
        val aab299 = str("aab299")
        if (aab299 == null) accum("现户籍所在地必填|")
        else if (jdbcTemplate.query(
                "SELECT label FROM aa12 WHERE id=?", { rs, _ -> rs.getString("label") }, aab299
            ).firstOrNull() == null) accum("行政区划代码有误|")
        val aae006 = str("aae006")
        if (aae006 == null) accum("现居住地详细地址必填|")
        else if (aae006.toByteArray(Charsets.UTF_8).size > 200) accum("现居住地详细地址长度过长|")
        if (str("aae005") == null) accum("手机号必填|")
        val aae00p = str("aae00p")
        if (aae00p == null) accum("用工性质必填|")
        else if (jdbcTemplate.queryForObject(
                "SELECT count(1) FROM aa10 WHERE aaa100='AAE00P' AND aaa102=substr(?,1,2)",
                Int::class.java, aae00p) == 0) accum("用工性质代码有误|")
        val aac013 = str("aac013")
        if (aac013 == null) accum("招工性质必填|")
        else if (jdbcTemplate.queryForObject(
                "SELECT count(1) FROM aa10 WHERE aaa100='AAC013' AND aaa102=substr(?,1,2)",
                Int::class.java, aac013) == 0) accum("招工性质代码有误|")
        val e2 = str("aae002")
        if (e2 == null) accum("费用开始年月必填|")
        else runCatching { YearMonth.parse(e2.substring(0, 6), DateTimeFormatter.ofPattern("yyyyMM")) }
            .getOrElse { accum("费用开始年月格式有误|") }
        val atc909 = str("atc909")
        if (atc909 == null) accum("用工方式必填|")
        else if (jdbcTemplate.queryForObject(
                "SELECT count(1) FROM aa10 WHERE aaa100='ATC909' AND aaa102=substr(?,1,2)",
                Int::class.java, atc909) == 0) accum("用工方式代码有误|")
        val atc90c = str("atc90c")
        var atc90aP: String? = null
        var atc90cP: LocalDate? = null
        var atc90dP: LocalDate? = null
        if (atc909?.take(2) in listOf("01", "02", "07", "08")) {
            val atc90a = str("atc90a")
            if (atc90a == null) accum("合同类别为派遣、代理、编外合同工、其他,合同类别必填|")
            else atc90aP = atc90a.take(1)
            if (atc90c == null) accum("协议/合同开始日期必填|")
            else atc90cP = runCatching { LocalDate.parse(atc90c, NF) }.getOrElse { accum("协议/合同开始日期格式有误|"); null }
            if (atc90aP in listOf("1", "3", "4", "5")) {
                val atc90d = str("atc90d")
                if (atc90d == null) accum("协议/合同开终止日期必填|")
                else atc90dP = runCatching { LocalDate.parse(atc90d, NF) }.getOrElse { accum("协议/合同开终止日期格式有误|"); null }
            }
        } else {
            if (atc90c == null) accum("协议/合同开始日期做为协议首签日期必填|")
            else atc90cP = runCatching { LocalDate.parse(atc90c, NF) }.getOrElse { accum("协议/合同开始日期格式有误|"); null }
        }
        val atc907 = str("atc907")
        if (atc907 == null) accum("工时制度必填|")
        else if (atc907.take(1) in listOf("2", "3") && str("atc908") == null) accum("岗位必填|")
        val atc90aa = str("atc90aa")
        if (atc90aa != null && atc90aa.take(1) == "1") {
            val ab = str("atc90ab")
            if (ab == null) accum("试用期开始时间必填|")
            else runCatching { LocalDate.parse(ab, NF) }.getOrElse { accum("试用期开始时间格式有误|") }
            val ac = str("atc90ac")
            if (ac == null) accum("试用期截止时间必填|")
            else runCatching { LocalDate.parse(ac, NF) }.getOrElse { accum("试用期截止时间格式有误|") }
            val ad = str("atc90ad")
            if (ad == null) accum("试用期劳动报酬必填|")
            else if (ad.toBigDecimalOrNull() == null) accum("试用期劳动报酬格式有误|")
            if (str("atc90ae") == null) accum("转正日期必填|")
        }
        val atc90ah = str("atc90ah")
        if (atc90ah == null) accum("是否签订退休三方协议必填|")
        else if (atc90ah.take(1) == "1") {
            if (str("atc90aj") == null) accum("退休三方协议类别必填|")
            val ak = str("atc90ak")
            if (ak == null) accum("退休时间必填|")
            else runCatching { LocalDate.parse(ak, NF) }.getOrElse { accum("退休时间格式有误|") }
            val al = str("atc90al")
            if (al == null) accum("签订退休三方协议日期必填|")
            else runCatching { LocalDate.parse(al, NF) }.getOrElse { accum("签订退休三方协议日期格式有误|") }
            val am = str("atc90am")
            if (am == null) accum("是否收管理费必填|")
            else if (am.take(1) == "1" && str("atc90an") == null) accum("代办退休服务费必填|")
            else if (am.take(1) == "1" && str("atc90an")?.toBigDecimalOrNull() == null) accum("代办退休服务费格式有误|")
        }
        val atc90g = str("atc90g")
        if (atc90g == null) accum("是否参保必填|")
        else if (atc90g.take(1) == "1") {
            if (atc909?.take(2) in listOf("03", "04", "05")) {
                accum("退休返聘、见习、实习不能参保|")
            } else {
                if (str("atc90h") == null) accum("在职人员参保类型必填|")
                val b301 = str("aaa301")
                if (b301 == null) accum("社保缴费基数必填|")
                else {
                    val v = b301.toBigDecimalOrNull()
                    if (v == null) accum("社保缴费基数格式有误|")
                    else {
                        if (v < sbBound.first) accum("社保缴费基数低于最低缴费标准|")
                        else if (v > sbBound.second) accum("社保缴费基数高于最高缴费标准|")
                    }
                }
                val c904 = str("atc904")
                if (c904 == null) accum("参保时间必填|")
                else runCatching { YearMonth.parse(c904.substring(0, 6), DateTimeFormatter.ofPattern("yyyyMM")) }
                    .getOrElse { accum("参保时间格式有误|") }
                if (str("atc9wc") == null) accum("新增原因必填|")
            }
        }
        if (atc909?.take(2) in listOf("01", "02", "07", "08")) {
            if (str("aaa407") == null) accum("是否缴纳公积金必填|")
        }
        if (str("aaa407")?.take(1) == "1") {
            val b303 = str("aaa303")
            if (b303 == null) accum("公积金缴费基数必填|")
            else {
                val v = b303.toBigDecimalOrNull()
                if (v == null) accum("公积金缴费基数格式有误|")
                else {
                    if (v < gjjBound.first) accum("公积金缴费基数低于最低缴费标准|")
                    else if (v > gjjBound.second) accum("公积金缴费基数高于最高缴费标准|")
                }
            }
        }
        if (str("aaa406") == null) accum("是否参加商业意外险必填|")
        if (atc90cP != null && atc90dP != null && atc90cP.isAfter(atc90dP))
            accum("合同终止时间不能早于合同开始时间|")
        val abD = str("atc90ab")?.let { runCatching { LocalDate.parse(it, NF) }.getOrNull() }
        val acD = str("atc90ac")?.let { runCatching { LocalDate.parse(it, NF) }.getOrNull() }
        if (abD != null && acD != null && abD.isAfter(acD)) accum("试用期截止时间不能早于试用期开始时间|")
        if (str("aac181") != null && runCatching { LocalDate.parse(str("aac181")!!, NF) }.isFailure)
            accum("毕业时间格式有误|")
        if (nOk == "2") throw RuleException(msg.toString())

        // 成功行: 解析列回填(原 SET ROW; _ 列与 aac004/aac006/匹配回填列)
        jdbcTemplate.update(
            """UPDATE tc90_imp SET ace10a='3', aac004=?, aac006=?::date, atc90x=?, atb900=?::numeric,
               aab001=?::numeric, id_=?::numeric, aab004_1=?, id_1=?::numeric, bab305=?,
               aac009_=substr(?,1,2), aae00p_=substr(?,1,2), aac013_=substr(?,1,2),
               atc909_=substr(?,1,2), atc90a_=substr(?,1,1),
               atc90c_=?::date, atc90d_=?::date, atc907_=substr(?,1,1),
               atc90aa_=substr(?,1,1), atc90ab_=?::date, atc90ac_=?::date, atc90ad_=?::numeric,
               atc90ae_=?::date, atc90ah_=substr(?,1,1), atc90aj_=substr(?,1,1), atc90ak_=?::date,
               atc90al_=?::date, atc90am_=substr(?,1,1), atc90an_=?::numeric,
               atc90g_=substr(?,1,1), atc90h_=substr(?,1,1), aaa301_=?::numeric,
               atc9wc_=substr(?,1,5), aaa407_=substr(?,1,1), aaa303_=?::numeric,
               aaa406_=substr(?,1,1), aac005_=substr(?,1,2), aac017_=substr(?,1,1),
               aac024_=substr(?,1,2), aac011_=substr(?,1,2),
               aac181_=?::date, aac180=?, aac182=? WHERE id=?""",
            aac004, aac006, unit[4], unit[0], unit[1], unit[2], unit[3], unit[2], str("bab305"),
            str("aac009"), str("aae00p"), str("aac013"), atc909, str("atc90a"), atc90cP?.toString(), atc90dP?.toString(), str("atc907"), str("atc90aa"),
            str("atc90ab"), str("atc90ac"), str("atc90ad"), str("atc90ae"),
            str("atc90ah"), str("atc90aj"), str("atc90ak"), str("atc90al"), str("atc90am"), str("atc90an"),
            str("atc90g"), str("atc90h"), str("aaa301"), str("atc9wc"), str("aaa407"), str("aaa303"), str("aaa406"),
            str("aac005"), str("aac017"), str("aac024"), str("aac011"),
            str("aac181") ?: "", str("aac180") ?: "", str("aac182") ?: "", id)
    }

    // ---------- PRC_COMMIT_TC90_IMP ----------

    @Transactional
    fun commitTc90Imp(
        ace100: String, aae011: String, aae017: String, aae019: String, aae020: String, aae022: String,
    ): Map<String, Any?> {
        val ace100N = ace100.toLongOrNull()
            ?: return mapOf("appCode" to err, "errMsg" to "员工合同提交出现错误，错误信息：批次号格式不正确")
        val nDd = LocalDate.now().dayOfMonth
        val aaa951 = jdbcTemplate.query(
            "SELECT aaa951 FROM aa95 WHERE aaa100='330100000000' AND aaa952='1'",
            { rs, _ -> rs.getInt("aaa951") }).firstOrNull()
            ?: return mapOf("appCode" to err, "errMsg" to "未查询到社保异动参数。")
        val curYmS = YearMonth.now().format(DateTimeFormatter.ofPattern("yyyyMM"))
        val nextYmS = YearMonth.now().plusMonths(1).format(DateTimeFormatter.ofPattern("yyyyMM"))
        val rows = jdbcTemplate.query(
            "SELECT id FROM tc90_imp WHERE ace10a='3' AND ace100=?",
            { rs, _ -> rs.getLong("id") }, ace100N)
        var created = 0
        for (row in rows) {
            val f = loadRow(row)
            val str = { k: String -> when (val v = f[k]) { null -> null; else -> v.toString() } }
            val dec = { k: String -> str(k)?.toBigDecimalOrNull() }
            val lng = { k: String -> str(k)?.toLongOrNull() }
            val aac001Old = lng("aac001")
            val aac002 = str("aac002")
            val aac003 = str("aac003")
            val atb930 = lng("atb930") ?: continue
            val aab001 = lng("aab001") ?: continue
            // AC01 新建或按证件更新
            val aac001: Long
            if (aac001Old == null) {
                aac001 = jdbcTemplate.queryForObject("SELECT nextval('seq_ac01_aac001')", Long::class.java)
                jdbcTemplate.update(
                    """INSERT INTO ac01 (baz001, aac001, aac002, aac003, aac004, aac006, aac009,
                       cab299, cab305, aae005, aae006, aae008, aae00c, aac013, aae00p, aae00r, aae013,
                       aae011, aae017, aae036, aae019, aae020, aae022,
                       aac005, aac024, aac011, aac017, aac181, aac180, aac182, aae00a, aae00d, bbb001)
                       VALUES (?, ?, ?, ?, ?, substr(?,1,10)::date, substr(?,1,2),
                       ?, ?, ?, ?, ?::numeric, substr(?,1,2), substr(?,1,2), substr(?,1,2), '0', '员工合同导入',
                       ?, ?, now(), ?, ?, ?,
                       substr(?,1,2), substr(?,1,2), substr(?,1,2), substr(?,1,1), substr(?,1,8)::date, ?, ?, ?, ?, ?)""",
                    ace100N, aac001, aac002, aac003, str("aac004"),
                    replace((str("aac006_") ?: str("aac006") ?: ""), "-", ""),
                    str("aac009_"),
                    str("aab299"), str("bab305"), str("aae005"), str("aae006"), str("aae008"),
                    str("aae005"), str("aac013_"), str("aae00p_"),
                    aae011, aae017, aae019, aae020, aae022,
                    str("aac005_"), str("aac024_"), str("aac011_"), str("aac017_"),
                    str("aac181_"), str("aac180"), str("aac182"), str("aae00a_"), str("aae00d_"),
                    str("bbb001"))
            } else {
                jdbcTemplate.update(
                    """UPDATE ac01 SET aac009=substr(?,1,2), cab299=?, cab305=?,
                       aae005=?, aae006=?, aae008=?, aae00c=?,
                       aac013=substr(?,1,2), aae00p=substr(?,1,2), aac005=substr(?,1,2),
                       aac024=substr(?,1,2), aac011=substr(?,1,2), aac017=substr(?,1,1),
                       aac181=substr(?,1,8)::date, aac180=?, aac182=?, aae00a=?, aae00d=?, bbb001=?
                       WHERE aac001=?""",
                    str("aac009_"), str("aab299"), str("bab305"),
                    str("aae005"), str("aae006"), str("aae008"), str("aae005"),
                    str("aac013_"), str("aae00p_"), str("aac005_"), str("aac024_"),
                    str("aac011_"), str("aac017_"), str("aac181_"), str("aac180"),
                    str("aac182"), str("aae00a_"), str("aae00d_"), str("bbb001"), aac001Old)
                aac001 = aac001Old
            }
            val ac01 = jdbcTemplate.query(
                "SELECT aac017, aac011, aac024, aac005, aac181::text, aac180, aac182, aac183, " +
                    "aac007::text, aac015, aac184::text, aca111, aca112, aac014, bac014, aac185::text, " +
                    "aac025, aae008, aae00d, atc90r, aae009, aac033, aae005 FROM ac01 WHERE aac001=?",
                { rs, _ -> Array<Any?>(23) { i -> rs.getObject(i + 1) } }, aac001).firstOrNull()
                ?: continue
            val g = { i: Int -> ac01[i - 1] }
            val atc900 = jdbcTemplate.queryForObject("SELECT nextval('seq_tc90_atc900')", Long::class.java)
            val atb910 = jdbcTemplate.queryForObject("SELECT nextval('seq_tb91_atb910')", Long::class.java)
            val isInsured = str("atc90g_") == "1"
            // 补缴判定(TB93.ATB90AE 扣款模式 vs ATC904)
            val atb90ae = jdbcTemplate.query(
                "SELECT atb90ae FROM tb93 WHERE atb930=?",
                { rs, _ -> rs.getString("atb90ae") }, atb930).firstOrNull()
            val c904 = str("atc904")
            var vFlag = "0"
            if (isInsured && atb90ae != null) {
                val prev = YearMonth.now().minusMonths(1).format(DateTimeFormatter.ofPattern("yyyyMM"))
                val curr = YearMonth.now().format(DateTimeFormatter.ofPattern("yyyyMM"))
                val next = YearMonth.now().plusMonths(1).format(DateTimeFormatter.ofPattern("yyyyMM"))
                when (atb90ae) {
                    "01" -> if (c904 != null && c904.substring(0, 6) < prev) vFlag = "1"
                    "02" -> if (c904 != null && c904.substring(0, 6) < curr) vFlag = "1"
                    "03" -> if (c904 != null && c904.substring(0, 6) < next) vFlag = "1"
                }
            }
            val atb90r = if (vFlag == "1") "4" else "2"
            val atb912 = if (vFlag == "1") "4" else "2"
            val atb913 = (aac003 ?: "") + "于" +
                LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd")) + "新增员工信息(所在结算单元：" +
                (str("atc90x") ?: "") + ")," + if (vFlag == "1") "该人员应该进行补缴" else "请审核"
            // 黑名单/老员工/备注继承
            var atc90aw = "0"
            var atc90af = "0"
            var aae013Inherit: String? = null
            if (aac001Old != null) {
                atc90aw = if (jdbcTemplate.queryForObject(
                        "SELECT count(1) FROM aa91 WHERE aae100='1' AND aac001=?",
                        Int::class.java, aac001) > 0) "1" else "0"
                atc90af = if (jdbcTemplate.queryForObject(
                        "SELECT count(*) FROM tc90 WHERE atb90r='5' AND aab001=? AND aac001=?",
                        Int::class.java, aab001, aac001) > 0) "1" else "0"
                aae013Inherit = jdbcTemplate.query(
                    "SELECT aae013 FROM tc90 WHERE atc900=(SELECT max(atc900) FROM tc90 WHERE aac001=?)",
                    { rs, _ -> rs.getString("aae013") }, aac001).firstOrNull()
            }
            val age = str("aac006")?.let { b ->
                runCatching {
                    val bd = LocalDate.parse(b, NF)
                    BigDecimal(YearMonth.now().year - bd.year)
                }.getOrNull()
            } ?: BigDecimal.ZERO
            val b301 = dec("aaa301_")
            val b303 = dec("aaa303_")
            val isContract = str("atc909_") in listOf("01", "02", "07", "08")
            val insMap = mapOf(
                "aaa301" to b301, "aaa302" to b301, "aaa303" to b303,
                "aaa304" to b301, "aaa305" to b301, "aaa306" to b301)
            // TC90 动态列写入(列映射生成 INSERT)
            val cols = linkedMapOf<String, Any?>(
                "baz001" to ace100N, "atc900" to atc900,
                "atb930" to atb930, "atb900" to lng("atb900"), "aab001" to aab001,
                "id" to lng("id_"), "aab004" to str("aab004_1"),
                "aab001_1" to aab001, "id_1" to lng("id_1"), "aab004_1" to str("aab004_1"),
                "aac001" to aac001, "aac003" to str("aac003"), "aac002" to str("aac002"), "aac004" to str("aac004"),
                "aac006" to str("aac006")?.let { d -> runCatching { LocalDate.parse(d, NF) }.getOrNull() },
                "atc901" to age,
                "aab299" to str("aab299"), "bab305" to (str("bab305") ?: str("aab299")),
                "aac017" to g(1), "aac011" to g(2), "aac024" to g(3),
                "aae006" to str("aae006"), "aac009" to (str("aac009_") ?: "0"),
                "aac005" to g(4), "aac033" to g(22), "aae005" to g(23),
                "aac181" to g(5), "aac180" to g(6), "aac182" to g(7), "aac183" to g(8),
                "aac007" to g(9), "aac015" to g(10), "aac184" to g(11),
                "aca111" to g(12), "aca112" to g(13), "aac014" to g(14), "bac014" to g(15),
                "aac185" to g(16), "aac025" to g(17), "aae008" to g(18), "aae00d" to g(19),
                "atc902" to str("aae006"), "atc90r" to g(20), "aae009" to g(21), "aae00a" to g(21),
                "atc907" to str("atc907_"), "atc908" to str("atc908"),
                "aac013" to str("aac013_"), "aae00p" to str("aae00p_"), "atc909" to str("atc909_"),
                "atc90a" to str("atc90a_"), "atc90e" to LocalDate.now(),
                "atc90b" to str("atc90c_")?.let { runCatching { LocalDate.parse(it, NF) }.getOrNull() }, "atc90c" to str("atc90c_")?.let { runCatching { LocalDate.parse(it, NF) }.getOrNull() }, "atc90d" to str("atc90d_")?.let { runCatching { LocalDate.parse(it, NF) }.getOrNull() },
                "aaa100" to "330100000000", "aaa101" to "浙江省杭州市",
                "atc90f" to "1",
                "atc90g" to str("atc90g_"), "atc90h" to str("atc90h_"),
                "aaa301" to insMap["aaa301"], "aaa302" to insMap["aaa302"], "aaa303" to insMap["aaa303"],
                "aaa304" to insMap["aaa304"], "aaa305" to insMap["aaa305"], "aaa306" to insMap["aaa306"],
                "aaa401" to (str("atc90g_") ?: "0"), "aaa402" to (str("atc90g_") ?: "0"),
                "aaa403" to (str("atc90g_") ?: "0"), "aaa404" to (str("atc90g_") ?: "0"),
                "aaa405" to (str("atc90g_") ?: "0"),
                "aaa406" to str("aaa406_"),
                "aaa407" to (str("aaa407_") ?: "0"),
                "bbb001" to str("bbb001"),
                "aae100" to "1",
                "aae011" to aae011, "aae017" to aae017, "aae036" to LocalDate.now(),
                "aae019" to aae019, "aae020" to aae020, "aae022" to aae022,
                "ccc001" to "1", "atc90x" to str("atc90x"),
                "atc90ae" to str("atc90ae_"),
                "aaa201" to (if (isInsured) (if (nDd - aaa951 > 0) nextYmS else curYmS).toBigDecimal() else null),
                "aaa501" to (if (str("aaa407_") == "1") (if (nDd - aaa951 > 0) nextYmS else curYmS).toBigDecimal() else null),
                "atc90aa" to str("atc90aa_"), "atc90ab" to str("atc90ab_"),
                "atc90ac" to str("atc90ac_"), "atc90ad" to dec("atc90ad_"),
                "atc90ah" to str("atc90ah_"), "atc90ai" to "0",
                "atc90aj" to str("atc90aj_"), "atc90ak" to str("atc90ak_"),
                "atc90al" to str("atc90al_"), "atc90am" to str("atc90am_"), "atc90an" to dec("atc90an_"),
                "atc9wc" to str("atc9wc_"), "aae002" to lng("aae002"),
                "atc90aw" to atc90aw, "atc90af" to atc90af,
                "aae013" to (aae013Inherit ?: "") + "|员工合同批量导入",
                "atb90r" to atb90r, "atb910" to atb910,
            )
            // 动态 INSERT(TC90 共 180 列, 未列入的列落默认 NULL)
            val names = cols.keys.joinToString(",")
            val ph = cols.keys.joinToString(",") { "?" }
            jdbcTemplate.update(
                "INSERT INTO tc90 ($names) VALUES ($ph)",
                *cols.values.toTypedArray())
            // TB91
            jdbcTemplate.update(
                """INSERT INTO tb91 (atb910, baz001, atb91z, atb911, atb912, atb913,
                   aae011, aae017, aae036, aae019, aae020, aae022, aab004, atb931)
                   VALUES (?, ?, ?, '20', ?, ?, ?, ?, now(), ?, ?, ?, ?, ?)""",
                atb910, ace100N, atc900, atb912, atb913,
                aae011, aae017, aae019, aae020, aae022,
                str("aab004_1") ?: str("aab004"), str("atc90x") ?: str("atc90x"))
            jdbcTemplate.update("UPDATE tc90 SET atb910=? WHERE atc900=?", atb910, atc900)
            val note = if (vFlag == "1") "应该进行补缴,请至【审核信息处理】模块进行维护" else null
            jdbcTemplate.update(
                "UPDATE tc90_imp SET ace10a='1', ace10b=?, atc900=? WHERE id=?", note, atc900, row)
            created++
        }
        return mapOf("appCode" to ok, "errMsg" to null as String?, "created" to created)
    }

    // 行内辅助(列值访问)
    private fun s(f: Map<String, Any?>, k: String): String? =
        when (val v = f["$k"] ?: f[k.lowercase()]) { null -> null; else -> v.toString() }

}

@Component
class TdcTc90ImpCheckProc(private val c: TdcTc90ImpCases) : ProcHandler {
    override fun procName() = "PKG_IMP_FZ.PRC_TC90_IMP"
    override fun handle(params: Map<String, Any?>): Map<String, Any?> {
        val r = c.checkTc90Imp(params["ace100"]?.toString() ?: return mapOf("appCode" to -1, "errMsg" to "缺少 ace100"))
        return mapOf("appCode" to r["appCode"], "errMsg" to r["errMsg"],
            "rows" to r["rows"], "pass" to r["pass"], "reject" to r["reject"])
    }
}

@Component
class TdcTc90ImpCommitProc(private val c: TdcTc90ImpCases) : ProcHandler {
    override fun procName() = "PKG_IMP_FZ.PRC_COMMIT_TC90_IMP"
    override fun handle(params: Map<String, Any?>): Map<String, Any?> {
        val r = c.commitTc90Imp(
            params["ace100"]?.toString() ?: return mapOf("appCode" to -1, "errMsg" to "缺少 ace100"),
            params["aae011"]?.toString() ?: "9999",
            params["aae017"]?.toString() ?: "测试用户",
            params["aae019"]?.toString() ?: "00000000",
            params["aae020"]?.toString() ?: "彩虹人才试点",
            params["aae022"]?.toString() ?: "120114")
        return mapOf("appCode" to r["appCode"], "errMsg" to r["errMsg"], "created" to r["created"])
    }
}
