package com.jlrc.rcpq.service.td

import com.jlrc.rcpq.service.calc.InsuranceCalcEngine
import com.jlrc.rcpq.service.proc.ProcHandler
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * td/b/p 批量导入弹窗五对过程（旧 PKG_派遣批量导入 规格重写）+ tdbp_cont 信息更新导入
 * （旧 PKG_IMP.PRC_AC01_GX_IMP）+ td/b/b 终止弹窗的 PRC_序列。
 *
 * 通道契约（旧 lemis.imp.Import → ria_pojo.doImport）：
 *   tc90_impnew→tc90_bp(员工合同新增) tc90_impdel→tc90_bp1(终止) tc90_bp2→tc90_bp2(更新)
 *   tc90_bp3→tc90_bp3(续签) tc90_bp4→tc90_bp4(新增保险) ac01_gx→ac01_gx(基本信息更新)。
 * 校验/提交由弹窗 JS 经 getProcedure 逐对调用；ace10a 状态机：
 *   NULL/0/2/3/4 参与校验 → 3=通过 2=失败(4=变更类提示)；提交处理 3(或 3,4)行 → 1=完成。
 *
 * 忠实怪癖（保持原行为）：
 * - 代码项判断成功会把 N_OK 重置回 3（此前累积的失败标志被清，仅 V_MSG 文本残留，
 *   由行尾 "V_MSG 非空即失败" 兜底）；ATB90C 校验提示文案复用"是否签订退休三方协议"。
 * - 用工方式(ATC909)校验的守卫误用 REC.AAC013 IS NOT NULL（复制粘贴残留）。
 * - 养老基数上下限调用传入带"-属地名"的原始 AAA100（bx='1' 分支不使用该参数，无副作用）。
 * - 员工导入提交：前合同编号继承查询(AAA001/005/009/004)后被整块置 NULL 覆盖——死代码，
 *   仅"变更单位"分支从 BGQ 合同重新继承；工伤参保年月 AAA901 先 :=AAA201 再被
 *   :=TO_CHAR(ATC90C,'yyyymm') 覆盖（后者生效）；AAE00P∈(2,3,4,6) 强制各险种标志='0'；
 *   AAE00P NOT IN (1,5) → ATB90R='3'（含 7-保险代理）；AA97/意外险两个块整段注释（死代码）。
 * - 终止提交：AAA604 回退值取 REC.AAA602、AAA904 取 REC.AAA902（原 CASE 复制错误，保留）。
 * - 续签提交：REC.ATC90A 无'-'时 SUBSTR(x,1,0)=NULL → atc90a 落 NULL（模板填'1'裸码时触发）。
 * - 新增保险提交：TC92 变更快照"养老基数→转正劳动报酬(atc905_)"的错位映射（原样保留）。
 * - PRC_序列：动态序列名超 Oracle 11g 30 字符上限时 ORA-00972 → 创建/取号均失败 → OUT 置 NULL。
 */
@Component
class TdbpImportCases(
    private val jdbcTemplate: JdbcTemplate,
    private val hzCases: TdcImpHzCases,
    private val engine: InsuranceCalcEngine,
) {
    private val NF = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    private val YMD = DateTimeFormatter.BASIC_ISO_DATE

    /** 行级失败: msg/code 随异常携带(code 缺省回落外层 nOk——0=库错, 2=校验败, 4=变更提示不中止) */
    private class RuleException(val msg: String? = null, val code: Int? = null) : Exception(msg)

    /** catch 统一落行: 合并异常消息, 状态取 显式code ?: 外层nOk(原 PL/SQL 每个失败点 N_OK:=2; V_MSG+=..; RAISE 的等价) */
    private fun fail(e: RuleException, table: String, id: Long, nOk: Int, msg: String?) {
        val m = if (e.msg != null) (msg ?: "") + e.msg else msg
        mark(table, id, (e.code ?: nOk).toString(), m)
    }

    /** 整批中止(等价原过程 OUTER OTHERS → PRM_CODE=-1 返回, 已处理行不回滚) */
    private class AbortException : Exception()

    /** varchar→number 隐式转换(ORACLE 语义: 垃圾值 ORA-01722 整批中止) */
    private fun strictNum(v: Any?): BigDecimal? = when {
        v == null -> null
        num(v) != null -> num(v)
        else -> throw AbortException()
    }

    /** 动态整行 INSERT 中的 varchar→number 严格化(行级守卫转 RuleException) */
    private fun strictCol(v: Any?): BigDecimal? = when {
        v == null -> null
        num(v) != null -> num(v)
        else -> throw IllegalArgumentException()
    }

    // ---------- 通用小件 ----------

    private fun str(v: Any?): String? = v?.toString()

    /** Excel-era varchar 列上的数值（to_number 语义：非数值→null，调用侧按失败处理） */
    private fun num(v: Any?): BigDecimal? = when (v) {
        null -> null
        is BigDecimal -> v
        is Number -> BigDecimal(v.toString())
        else -> v.toString().trim().toBigDecimalOrNull()
    }

    private fun lng(v: Any?): Long? = num(v)?.let { if (it.scale() <= 0) it.longValueExact() else it.toLong() }

    /** TO_DATE(x,'yyyy-mm-dd') 的 Oracle 宽松等价：接受 2026-01-02 与 20260102 */
    private fun day(v: Any?): LocalDate? {
        val s = str(v) ?: return null
        return try {
            LocalDate.parse(s, NF)
        } catch (e: Exception) {
            try {
                LocalDate.parse(s.replace("-", "").take(8), YMD)
            } catch (e2: Exception) {
                null
            }
        }
    }

    private fun loadRow(table: String, id: Long): MutableMap<String, Any?> = jdbcTemplate.query(
        "SELECT * FROM $table WHERE id=?", { rs, _ ->
            val md = rs.metaData
            val m = mutableMapOf<String, Any?>()
            for (i in 1..md.columnCount) m[md.getColumnLabel(i).lowercase()] = rs.getObject(i)
            m
        }, id).first() ?: mutableMapOf()

    /** 行尾统一回写 ace10a/ace10b */
    private fun mark(table: String, id: Long, state: String, msg: String?) {
        jdbcTemplate.update("UPDATE $table SET ace10a=?, ace10b=? WHERE id=?", state, msg, id)
    }

    /**
     * PRC_缴费基数上下限（PRM_JS_RS OUT 语义: 夹取结果回写目标列; PRM_JS 为 NULL → 结果 NULL 不夹取）。
     * 原 PL/SQL: JS<下限→下限, JS>上限→上限, 无 AA98 行→下限按 0(仅上限仍生效)。
     */
    private fun clamp(bx: String, aae002: String, aaa100: String?, js: BigDecimal?): BigDecimal? =
        if (js == null) null else engine.clampBase(bx, aae002, aaa100, js)

    private fun seqNext(name: String): Long =
        jdbcTemplate.queryForObject("SELECT nextval('$name')", Long::class.java)!!

    /**
     * PRC_序列：动态取号 seq_<aaa030><aaa110前4><lx>_<aae002>；不存在则建。
     * 注意: PG 事务中任一语句失败即整体中止——不能用 Oracle 的"先试取号失败再建"模式,
     * 改为先 to_regclass 探测存在性(零失败语句), 再按需 CREATE; 建号失败 → NULL。
     * 名称超 30 字符按 ORA-00972(Oracle 11g 标识符上限)置 NULL, 与原行为一致。
     * 返回 aaa030 || NVL(aaa110前4,'0000') || aae002 || lx || LPAD(号,4,0)。
     */
    fun 序列(aaa030: String?, aae002: String?, aaa110: String?, lx: String): String? {
        val y4 = aaa110?.take(4) ?: ""
        val name = "seq_${aaa030 ?: ""}$y4${lx.lowercase()}_${aae002 ?: ""}"
        if (name.length > 30) return null
        val n: Long? = try {
            val exists = jdbcTemplate.queryForObject(
                "SELECT to_regclass('public.\"$name\"') IS NOT NULL", Boolean::class.java)
            if (!exists) jdbcTemplate.execute("CREATE SEQUENCE IF NOT EXISTS \"$name\" START 1")
            seqNext(name)
        } catch (e: Exception) {
            null
        }
        return n?.let { "${aaa030 ?: ""}${y4.ifEmpty { "0000" }}${aae002 ?: ""}$lx${it.toString().padStart(4, '0')}" }
    }

    /**
     * PRC_代码项判断：AA10(AAA100,AAA102) 校验。'code-名称' 复合值取'-'前段；
     * AAE00P 必须整段匹配（'码-名称'，旧模板防伪）；空+必录 → 不能为空。
     * 返回 (code, value, msg)：3=通过。
     */
    fun codeItem(aaa100: String, aaa102: String?, comm: String, ifbl: String): Triple<Int, String?, String?> {
        if (aaa102 != null) {
            val p = aaa102.indexOf('-')
            val base = if (p >= 0) aaa102.substring(0, p) else aaa102
            val row = jdbcTemplate.query(
                "SELECT aaa102, aaa103 FROM aa10 WHERE aaa102=? AND aaa100=?",
                { rs, _ -> Pair(rs.getString("aaa102"), rs.getString("aaa103")) }, base, aaa100
            ).firstOrNull() ?: return Triple(2, null, "【$comm】不正确|")
            if (aaa100 == "AAE00P" && aaa102 != row.first + "-" + row.second)
                return Triple(2, null, "【$comm】不正确,请下载最新导入模板|")
            return Triple(3, row.first, null)
        }
        return if (ifbl == "1") Triple(2, null, "【$comm】不能为空|") else Triple(3, null, null)
    }

    private fun user(aae011: String): Map<String, Any?>? = jdbcTemplate.query(
        "SELECT * FROM sys_org_user_unit WHERE aae011=?", { rs, _ ->
            val md = rs.metaData
            val m = mutableMapOf<String, Any?>()
            for (i in 1..md.columnCount) m[md.getColumnLabel(i).lowercase()] = rs.getObject(i)
            m
        }, aae011).firstOrNull()

    private fun aa95(): Map<String, Any?>? = jdbcTemplate.query(
        "SELECT * FROM aa95 WHERE aaa952='1'", { rs, _ ->
            val md = rs.metaData
            val m = mutableMapOf<String, Any?>()
            for (i in 1..md.columnCount) m[md.getColumnLabel(i).lowercase()] = rs.getObject(i)
            m
        }).firstOrNull()

    /** AA12 区划：ID=前缀段(去'-属地名')；返回 (id,label) 或 null */
    private fun aa12(raw: Any?, suffix: String = ""): Pair<String, String>? {
        val s = str(raw) ?: return null
        val p = s.indexOf('-')
        val id = if (p >= 0) s.substring(0, p) + suffix else s + suffix
        return jdbcTemplate.query(
            "SELECT id, label FROM aa12 WHERE id=?", { rs, _ -> Pair(rs.getString("id"), rs.getString("label")) }, id
        ).firstOrNull()
    }

    private fun aa10Name(aaa100: String, aaa102: String?): String? = jdbcTemplate.query(
        "SELECT aaa103 FROM aa10 WHERE aaa102=? AND aaa100=?",
        { rs, _ -> rs.getString("aaa103") }, aaa102, aaa100).firstOrNull()

    /** 每行经办人审计列参数（bae011/017/019/020/022, bae036 由 SQL now() 填充） */
    private fun auditArgs(user: Map<String, Any?>): Array<Any?> = arrayOf(
        str(user["aae011"]), str(user["aae017"]), str(user["aae019"]), str(user["aae020"]), str(user["aae022"]))

    // ---------- PRC_员工导入校验 ----------

    @Transactional
    fun checkEmployeeImport(ace100: String, aae011: String): Map<String, Any?> {
        val u = user(aae011)
            ?: return mapOf("appCode" to -1, "errMsg" to "为查询到经办人信息")
        val a95 = aa95()
            ?: return mapOf("appCode" to -1, "errMsg" to "为设置时限参数")
        val a951 = (a95["aaa951"] as? Number)?.toInt() ?: 0
        val today = LocalDate.now()
        val curYm = today.format(DateTimeFormatter.ofPattern("yyyyMM"))
        val curYmd = today.format(DateTimeFormatter.ofPattern("yyyyMMdd"))
        val curYear = today.format(DateTimeFormatter.ofPattern("yyyy"))

        val rows = jdbcTemplate.query(
            "SELECT id FROM tc90_bp WHERE (ace10a IS NULL OR ace10a IN ('0','2','3','4')) AND ace100=?",
            { rs, _ -> rs.getLong("id") }, ace100.toLong())

        for (id in rows) {
            var nOk = 3
            var msg: String? = null
            val rec = loadRow("tc90_bp", id)
            var ac01Aac001: Long? = null
            var ac01Aac002 = str(rec["aac002"])
            val aaa303Orig = str(rec["aaa303"]) // 原始公积金基数(变更判定用)
            try {
                rec["ace10a"] = 3
                var atc900Bgq: Long? = null
                var aae013: String? = null
                var atc9wc: String? = null
                // 去除身份证中的特殊字符
                rec["aac002"] = str(rec["aac002"])?.replace("\r", "")?.replace("\n", "")
                    ?.replace("\t", "")?.replace("x", "X")?.replace(" ", "")
                val aac002 = str(rec["aac002"])
                if (aac002 == null || str(rec["aac003"]) == null)
                    throw RuleException("身份证号码和姓名必须填写|", 2)
                if (rec["abp001"] == null && (aac002.length >= 15)) {
                    rec["aac003"] = str(rec["aac003"])?.replace("\r", "")?.replace("\n", "")
                        ?.replace("\t", "")?.replace(" ", "")
                }
                rec["aac001"] = null
                atc900Bgq = null; rec["atc900_bgq"] = null; atc9wc = null; rec["atc9wc"] = null

                var aac006: String? = null
                if (rec["abp001"] == null) {
                    if (aac002.length >= 15) {
                        val parsed = hzCases.parseAac002(aac002)
                        if ((parsed[0] as Int) != 1) throw RuleException("公民身份证号码不合法|", 2)
                        ac01Aac002 = parsed[3] as String?
                        aac006 = parsed[2] as String?
                    } // else: ac01Aac002 已 = 原值
                } else {
                    ac01Aac002 = aac002
                }

                // 用工性质（必录，即时中止）
                codeItem("AAE00P", str(rec["aae00p"]), "用工性质", "1").let { (c, v, m) ->
                    nOk = c; msg = (msg ?: "") + (m ?: ""); rec["aae00p"] = v
                }
                if (nOk != 3) throw RuleException()
                val ygbzName = aa10Name("AAE00P", str(rec["aae00p"]))
                    ?: throw RuleException("用工性质填写值不正确|", 2).also { nOk = 2 }

                // 是否存在基本信息
                val ac01 = jdbcTemplate.query(
                    "SELECT aac001, aac003 FROM ac01 WHERE aac002=? LIMIT 1",
                    { rs, _ -> Pair(rs.getLong("aac001"), rs.getString("aac003")) }, ac01Aac002
                ).firstOrNull()
                ac01Aac001 = ac01?.first
                rec["aac001"] = ac01Aac001
                if (ac01 != null) {
                    if (str(rec["aac003"]) != ac01.second)
                        throw RuleException("该身份证号码已登记基本信息，姓名为【${ac01.second}】|", 2)
                    val aae00p = str(rec["aae00p"])
                    fun cnt(where: String, vararg args: Any?): Int =
                        jdbcTemplate.queryForObject("SELECT COUNT(*) FROM tc90 WHERE $where", Int::class.java, *args)!!
                    if (aae00p in setOf("1", "3", "4", "5", "7")) {
                        // 20201127 保险代理控制
                        if (cnt("aae00p=? AND aae100='1' AND atb90r='3' AND aac001=?", aae00p, ac01Aac001) > 0)
                            throw RuleException("该人员已经存在有效的【$ygbzName】合同|", 2)
                        if (cnt("aae00p IN ('1','3','4','5','7') AND atb90r NOT IN ('3','5') AND aac001=?", ac01Aac001) > 0)
                            throw RuleException("该人员存在审核流程中的【$ygbzName】合同|", 2)
                        if (cnt("aae100='1' AND atb90r='3' AND atb930=? AND aac001=?", rec["atb930"], ac01Aac001) > 0)
                            throw RuleException("该人员已经存在有效的【$ygbzName】合同|", 2)
                    } else {
                        if (cnt("aae100='1' AND atb90r='3' AND atb930=? AND aac001=?", rec["atb930"], ac01Aac001) > 0)
                            throw RuleException("该人员已经存在有效的【$ygbzName】合同|", 2)
                        if (cnt("aae00p=? AND atb90r NOT IN ('3','5') AND atb930=? AND aac001=?", aae00p, rec["atb930"], ac01Aac001) > 0)
                            throw RuleException("该人员存在审核流程中的【$ygbzName】合同|", 2)
                    }
                }

                // 累积规则（代码项判断成功会把 nOk 重置回 3——原行为）
                if (rec["aac005"] != null) codeItem("AAC005", str(rec["aac005"]), "民族", "1").let { (c, v, m) ->
                    nOk = c; msg = (msg ?: "") + (m ?: ""); rec["aac005"] = v
                }
                if (rec["aac011"] != null) codeItem("AAC011", str(rec["aac011"]), "学历", "1").let { (c, v, m) ->
                    nOk = c; msg = (msg ?: "") + (m ?: ""); rec["aac011"] = v
                }
                if (rec["atc90ax"] != null) {
                    if (day(rec["atc90ax"]) == null) { nOk = 2; msg = (msg ?: "") + "【身份证有效期】格式不正确|" }
                } else { nOk = 2; msg = (msg ?: "") + "【身份证有效期】不能为空|" }
                codeItem("AAC012", str(rec["aac012"]), "个人身份", "1").let { (c, v, m) ->
                    nOk = c; msg = (msg ?: "") + (m ?: ""); rec["aac012"] = v
                }
                if (rec["atc905"] != null) {
                    if (num(rec["atc905"]) == null) { nOk = 2; msg = (msg ?: "") + "【合同工资】格式不正确|" }
                } else { nOk = 2; msg = (msg ?: "") + "【合同工资】不能为空|" }
                if (rec["bbb002"] != null) codeItem("BBB002", str(rec["bbb002"]), "发放银行", "0").let { (c, v, m) ->
                    nOk = c; msg = (msg ?: "") + (m ?: ""); rec["bbb002"] = v
                }
                if (rec["aac009"] != null) codeItem("AAC009", str(rec["aac009"]), "户口性质", "1").let { (c, v, m) ->
                    nOk = c; msg = (msg ?: "") + (m ?: ""); rec["aac009"] = v
                }
                if (rec["aaa605"] != null) codeItem("AAA605", str(rec["aaa605"]), "是否存在职业病危害", "1").let { (c, v, m) ->
                    nOk = c; msg = (msg ?: "") + (m ?: ""); rec["aaa605"] = v
                }
                if (rec["aac013"] != null) codeItem("AAC013", str(rec["aac013"]), "招工性质", "1").let { (c, v, m) ->
                    nOk = c; msg = (msg ?: "") + (m ?: ""); rec["aac013"] = v
                }
                if (str(rec["aae00p"]) == "1" && rec["aae005"] == null) { nOk = 2; msg = (msg ?: "") + "【联系电话】不能为空|" }
                if (rec["aae005"] != null && str(rec["aae005"])!!.length > 1 && str(rec["aae005"])!![1] == '.') {
                    nOk = 2; msg = (msg ?: "") + "【联系电话】格式不正确|"
                }
                // 用工方式（守卫误用 AAC013——原样保留）
                if (rec["aac013"] != null) codeItem("ATC909", str(rec["atc909"]), "用工方式", "1").let { (c, v, m) ->
                    nOk = c; msg = (msg ?: "") + (m ?: ""); rec["atc909"] = v
                }
                if (rec["atc90b"] != null) {
                    if (day(rec["atc90b"]) == null) { nOk = 2; msg = (msg ?: "") + "【协议首签日期】格式不正确|" }
                } else { nOk = 2; msg = (msg ?: "") + "【协议首签日期】不能为空|" }
                // 合同类别
                if (rec["atc90a"] != null) {
                    val s = str(rec["atc90a"])!!
                    val p = s.indexOf('-')
                    rec["atc90a"] = if (p > 0) s.substring(0, p) else null // INSTR=0 → SUBSTR(x,1,0)=NULL
                    val ok = rec["atc90a"]?.let { aa10Name("ATC90A", str(it)) } != null
                    if (!ok) { nOk = 2; msg = (msg ?: "") + "【合同类别】不正确|" }
                } else rec["atc90a"] = "1"
                if (rec["atc90c"] != null) {
                    if (day(rec["atc90c"]) == null) { nOk = 2; msg = (msg ?: "") + "【合同开始日期】格式不正确|" }
                } else { nOk = 2; msg = (msg ?: "") + "【合同开始日期】不能为空|" }
                if (str(rec["atc90a"]) == "1") {
                    if (rec["atc90d"] != null) {
                        if (day(rec["atc90d"]) == null) { nOk = 2; msg = (msg ?: "") + "【合同终止日期】格式不正确|" }
                    } else { nOk = 2; msg = (msg ?: "") + "当【合同类型】选择【1-固定期限】时，【合同终止日期】不能为空|" }
                    val d = day(rec["atc90d"]); val c = day(rec["atc90c"])
                    if (d == null || c == null) { nOk = 2 } // 原内层 OTHERS→N_OK:=2（无消息）
                    else if (d.isBefore(c)) { nOk = 2; msg = (msg ?: "") + "【合同终止日期】小于【合同开始日期】|" }
                }
                if (rec["atc907"] != null) codeItem("ATC907", str(rec["atc907"]), "工时制度", "1").let { (c, v, m) ->
                    nOk = c; msg = (msg ?: "") + (m ?: ""); rec["atc907"] = v
                }
                if (rec["atc90ah"] != null) codeItem("ATC90AH", str(rec["atc90ah"]), "是否签订退休三方协议", "1").let { (c, v, m) ->
                    nOk = c; msg = (msg ?: "") + (m ?: ""); rec["atc90ah"] = v
                }
                // 费用开始年月
                if (rec["aae002"] != null) {
                    if (day(str(rec["aae002"]) + "01") == null) { nOk = 2; msg = (msg ?: "") + "【费用开始年月】格式不正确|" }
                    if (str(rec["aae00p"]) in setOf("1", "5", "7")) {
                        val ym = str(rec["aae002"])!!
                        if (ym < curYm) { nOk = 2; msg = (msg ?: "") + "【费用开始年月】不能小于为当月|" }
                        if (curYmd.substring(6, 8).toInt() > a951 && ym == curYm) {
                            nOk = 2; msg = (msg ?: "") + "已超过办理时限，【费用开始年月】不能录入为当月，可以设置为下月|"
                        }
                    }
                } else { nOk = 2; msg = (msg ?: "") + "【费用开始年月】不能为空|" }
                if (rec["atb90c"] != null) codeItem("ATB90C", str(rec["atb90c"]), "是否签订退休三方协议", "1").let { (c, v, m) ->
                    nOk = c; msg = (msg ?: "") + (m ?: ""); rec["atb90c"] = v
                }
                if (rec["atb90a"] == null && rec["atb90b"] == null) {
                    nOk = 2; msg = (msg ?: "") + "【单位服务费月标准】和【个人承担服务费月标准】不能同时为空|"
                } else if (rec["atb90a"] != null && num(rec["atb90a"]) == null) {
                    nOk = 2; msg = (msg ?: "") + "【单位服务费月标准】格式不正确|"
                }
                if (rec["atc90zb"] != null) codeItem("ATC90ZB", str(rec["atc90zb"]), "是否参加工会", "1").let { (c, v, m) ->
                    nOk = c; msg = (msg ?: "") + (m ?: ""); rec["atc90zb"] = v
                }
                if (rec["atc90za"] != null) codeItem("ATC90ZA", str(rec["atc90za"]), "工会会费扣除", "1").let { (c, v, m) ->
                    nOk = c; msg = (msg ?: "") + (m ?: ""); rec["atc90za"] = v
                }
                // 养老
                if (rec["aaa401"] != null) codeItem("AAA401", str(rec["aaa401"]), "是否参加养老保险", "1").let { (c, v, m) ->
                    nOk = c; msg = (msg ?: "") + (m ?: ""); rec["aaa401"] = v
                }
                if (str(rec["aaa401"]) == "1") {
                    if (str(rec["aae00p"]) in setOf("1", "5")) {
                        if (rec["aaa301"] != null) {
                            if (num(rec["aaa301"]) == null) { nOk = 2; msg = (msg ?: "") + "【养老缴费基数】格式不正确|" }
                        } else { nOk = 2; msg = (msg ?: "") + "当【用工性质】选择【1-派遣】时，【养老缴费基数】不能为空|" }
                        if (rec["aaa100"] != null) {
                            if (aa12(rec["aaa100"]) == null) { nOk = 2; msg = (msg ?: "") + "【社保参保属地】格式不正确|" }
                        } else { nOk = 2; msg = (msg ?: "") + "【社保参保属地】不能为空|" }
                        if (rec["aaa203"] != null) {
                            if (day(str(rec["aaa203"]) + "01") == null) { nOk = 2; msg = (msg ?: "") + "【社保参保年月】格式不正确|" }
                        } else { nOk = 2; msg = (msg ?: "") + "【社保参保年月】不能为空|" }
                        // 基数上下限: PRM_JS=合同工资, 夹取结果 OUT 回写 REC.AAA301（bx=1 不使用属地参数）
                        rec["aaa301"] = clamp("1", curYm, str(rec["aaa100"]), num(rec["atc905"]))
                        if (rec["atc90p"] != null) codeItem("ATC90P", str(rec["atc90p"]), "社保参保状态", "1").let { (c, v, m) ->
                            nOk = c; msg = (msg ?: "") + (m ?: ""); rec["atc90p"] = v
                        }
                    }
                } else { rec["aaa301"] = null; rec["aaa100"] = null }
                // 医保
                if (rec["aaa402"] != null) codeItem("AAA402", str(rec["aaa402"]), "是否参加医疗保险", "1").let { (c, v, m) ->
                    nOk = c; msg = (msg ?: "") + (m ?: ""); rec["aaa402"] = v
                }
                if (str(rec["aaa402"]) == "1") {
                    if (str(rec["aae00p"]) in setOf("1", "5")) {
                        if (rec["aaa302"] != null) {
                            if (num(rec["aaa302"]) == null) { nOk = 2; msg = (msg ?: "") + "【医保缴费基数】格式不正确|" }
                        } else { nOk = 2; msg = (msg ?: "") + "当【用工性质】选择【1-派遣】时,【医保缴费基数】不能为空|" }
                        if (rec["aaa110"] != null) {
                            if (aa12(rec["aaa110"]) == null) { nOk = 2; msg = (msg ?: "") + "【医保参保属地】格式不正确|" }
                        } else { nOk = 2; msg = (msg ?: "") + "【医保参保属地】不能为空|" }
                        if (rec["aaa603"] != null) {
                            if (day(str(rec["aaa603"]) + "01") == null) { nOk = 2; msg = (msg ?: "") + "【医保参保年月】格式不正确|" }
                            val a110 = str(rec["aaa110"])
                            val pref = a110?.let { val p = it.indexOf('-'); if (p >= 0) it.substring(0, p) else it }
                            if (str(rec["aaa603"])!! < curYm && pref != null && pref.length >= 4 && pref.substring(2, 4) != "99") {
                                nOk = 2; msg = (msg ?: "") + "参加市医保，【医保参保年月】不能小于为当月|"
                            }
                        } else { nOk = 2; msg = (msg ?: "") + "【医保参保年月】不能为空|" }
                        val a110 = str(rec["aaa110"])
                        val pref = a110?.let { val p = it.indexOf('-'); if (p >= 0) it.substring(0, p) else it }
                        rec["aaa302"] = clamp("2", curYm, pref, num(rec["atc905"]))
                        if (rec["atc90pa"] != null) codeItem("ATC90PA", str(rec["atc90pa"]), "医保参保状态", "1").let { (c, v, m) ->
                            nOk = c; msg = (msg ?: "") + (m ?: ""); rec["atc90pa"] = v
                        }
                    }
                } else { rec["aaa302"] = null; rec["aaa110"] = null }
                // 公积金
                if (rec["aaa407"] != null) codeItem("AAA407", str(rec["aaa407"]), "是否缴纳公积金", "1").let { (c, v, m) ->
                    nOk = c; msg = (msg ?: "") + (m ?: ""); rec["aaa407"] = v
                }
                if (str(rec["aaa407"]) == "1") {
                    if (str(rec["aae00p"]) in setOf("1", "5")) {
                        if (rec["aaa303"] != null) {
                            if (num(rec["aaa303"]) == null) { nOk = 2; msg = (msg ?: "") + "【公积金缴费基数】格式不正确|" }
                        } else { nOk = 2; msg = (msg ?: "") + "当【用工性质】选择【1-派遣】时,【公积金缴费基数】不能为空|" }
                        codeItem("ATB90S", str(rec["atb90s"]), "公积金比例", "1").let { (c, v, m) ->
                            nOk = c; msg = (msg ?: "") + (m ?: ""); rec["atb90s"] = v
                        }
                        if (rec["aaa120"] != null) {
                            if (aa12(rec["aaa120"]) == null) { nOk = 2; msg = (msg ?: "") + "【公积金参保属地】格式不正确|" }
                        } else { nOk = 2; msg = (msg ?: "") + "【公积金参保属地】不能为空|" }
                        if (rec["aaa503"] != null) {
                            if (day(str(rec["aaa503"]) + "01") == null) { nOk = 2; msg = (msg ?: "") + "【公积金参保年月】格式不正确|" }
                        } else { nOk = 2; msg = (msg ?: "") + "【公积金参保年月】不能为空|" }
                        val a120 = str(rec["aaa120"])
                        val pref = a120?.let { val p = it.indexOf('-'); if (p >= 0) it.substring(0, p) else it }
                        rec["aaa303"] = clamp("3", curYm, pref, num(rec["aaa303"]))
                        if (rec["atc90w"] != null) codeItem("ATC90W", str(rec["atc90w"]), "公积金参保状态", "1").let { (c, v, m) ->
                            nOk = c; msg = (msg ?: "") + (m ?: ""); rec["atc90w"] = v
                        }
                    }
                } else { rec["aaa303"] = null; rec["atb90s"] = null; rec["aaa120"] = null }

                // 原合同保险状态联动（变更单位判定）
                if (str(rec["aae00p"]) in setOf("1", "5", "7") && ac01Aac001 != null) {
                    var prev = jdbcTemplate.query(
                        "SELECT * FROM tc90 WHERE atc900=(SELECT max(atc900) FROM tc90 WHERE " +
                            "(atc90p IN ('0','2') OR atc90pa IN ('0','2') OR atc90w IN ('0','2')) " +
                            "AND aae00p IN ('1','5','7') AND aac001=?)",
                        { rs, _ -> tc90Row(rs) }, ac01Aac001).firstOrNull()
                    if (prev != null && prev["atc900"] != null) {
                        if (str(prev["atc90p"]) == "0" || str(prev["atc90pa"]) == "0" || str(prev["atc90w"]) == "0")
                            aae013 = "在${str(prev["aab004"])}下参保"
                        atc900Bgq = (prev["atc900"] as Number).toLong(); rec["atc900_bgq"] = atc900Bgq
                        atc9wc = "10"; rec["atc9wc"] = atc9wc
                        val rec120 = str(rec["aaa120"])
                        val rec120p = rec120?.let { val p = it.indexOf('-'); if (p >= 0) it.substring(0, p) else it }
                        if (str(prev["atc90w"]) in setOf("0", "2") && str(prev["aaa120"]) == rec120p) {
                            if (num(prev["aaa303"]) != num(rec["aaa303"]))
                                throw RuleException("公积金已参保，不允许变更基数|", 2).also { nOk = 2 }
                        }
                        val p110 = str(prev["aaa110"])
                        val p110p = p110?.let { val p = it.indexOf('-'); if (p >= 0) it.substring(0, p) else it }
                        val rec110 = str(rec["aaa110"])
                        val rec110p = rec110?.let { val p = it.indexOf('-'); if (p >= 0) it.substring(0, p) else it }
                        if (str(prev["atc90pa"]) == "0" && p110p != null && p110p.length >= 4 && p110p.substring(2, 4) != "99" &&
                            num(prev["aaa302"]) != num(rec["aaa302"]) && p110p == rec110p
                        ) {
                            nOk = 4
                            aae013 = (aae013 ?: "") + "医保已参保，不允许变更基数,系统自动将医保基数替换为原基数|"
                            rec["aaa302"] = prev["aaa302"]
                        }
                    } else {
                        prev = jdbcTemplate.query(
                            "SELECT * FROM tc90 WHERE atc900=(SELECT max(atc900) FROM tc90 WHERE atc90w='1' AND aac001=?)",
                            { rs, _ -> tc90Row(rs) }, ac01Aac001).firstOrNull()
                        if (prev != null && prev["atc900"] != null) {
                            val rec120 = str(rec["aaa120"])
                            val rec120p = rec120?.let { val p = it.indexOf('-'); if (p >= 0) it.substring(0, p) else it }
                            if (str(prev["aaa120"]) == rec120p && num(prev["aaa303"]) != num(aaa303Orig)) {
                                val a509 = str(prev["aaa509"]) // 公积金停保年月(实际)
                                val aae002 = str(rec["aae002"])
                                val start = aae002?.let { day(it + "01") }
                                val stop = a509?.let { day(it + "01") }
                                if (start != null && stop != null) {
                                    val months = (start.year - stop.year) * 12 + (start.monthValue - stop.monthValue)
                                    if (months + 1 < 12) { nOk = 2; msg = (msg ?: "") + "公积金停保不满一年，不允许变更基数|" }
                                }
                            }
                        }
                    }
                }
                // 意外险
                if (rec["atc051"] != null && day(rec["atc051"]) == null) { nOk = 2; msg = (msg ?: "") + "【意外险开始日期】格式不正确|" }
                if (rec["atc052"] != null && day(rec["atc052"]) == null) { nOk = 2; msg = (msg ?: "") + "【意外险结束日期】格式不正确|" }
                if (rec["atc053"] != null && num(rec["atc053"]) == null) { nOk = 2; msg = (msg ?: "") + "【意外险额度】格式不正确|" }
                if (rec["atc053"] != null && (rec["atc051"] == null || rec["atc052"] == null)) {
                    nOk = 2; msg = (msg ?: "") + "【意外险】信息填写不完整|"
                }
                // 跨年基数标识（SUBSTR(x,1,4) < TO_CHAR(SYSDATE,'yyyy') 字符串比较）
                val y203 = str(rec["aaa203"])?.take(4)
                val y503 = str(rec["aaa503"])?.take(4)
                val y603 = str(rec["aaa603"])?.take(4)
                if (listOf(y203, y503, y603).any { it != null && it < curYear }) {
                    if (rec["atc90r"] == null) { nOk = 2; msg = (msg ?: "") + "保险跨年【跨年缴费基数标识】必须填写|" }
                    else codeItem("ATC90R", str(rec["atc90r"]), "跨年缴费基数标识", "1").let { (c, v, m) ->
                        nOk = c; msg = (msg ?: "") + (m ?: ""); rec["atc90r"] = v
                    }
                }
                if (msg != null && msg!!.isNotEmpty()) { nOk = 2; throw RuleException() }

                // 行通过 → 回写解析列（原 SET ROW 摘要）
                val newState = if (atc900Bgq == null) nOk.toString() else "4"
                jdbcTemplate.update(
                    "UPDATE tc90_bp SET aac001=?, aac002=?, aac003=?, aaa301=?, aaa304=?, aaa302=?, " +
                        "aaa305=?, aaa306=?, aaa303=?, aaa100=?, aaa110=?, aaa120=?, ace10a=?, " +
                        "ace10b=?, aae013=?, atc900_bgq=?, atc9wc=? WHERE id=?",
                    ac01Aac001, ac01Aac002, str(rec["aac003"]),
                    num(rec["aaa301"]), num(rec["aaa301"]), num(rec["aaa302"]),
                    num(rec["aaa302"]), num(rec["aaa302"]), num(rec["aaa303"]),
                    str(rec["aaa100"]), str(rec["aaa110"]), str(rec["aaa120"]),
                    newState, (msg ?: "") + (aae013 ?: ""), aae013, atc900Bgq, atc9wc, id)
            } catch (e: RuleException) {
                fail(e, "tc90_bp", id, nOk, msg)
            }
        }
        val stat = jdbcTemplate.queryForMap(
            "SELECT COUNT(1) total, " +
                "COALESCE(SUM(CASE WHEN ace10a='3' THEN 1 ELSE 0 END),0) pass, " +
                "COALESCE(SUM(CASE WHEN ace10a<>'3' THEN 1 ELSE 0 END),0) fail " +
                "FROM tc90_bp WHERE ace100=?", ace100.toLong())
        return mapOf("appCode" to 1, "errMsg" to
            "校验完成，共导入【${stat["total"]}】条数据，其中【${stat["pass"]}】条数据符合要求，【${stat["fail"]}】条数据不符合要求！")
    }

    private fun tc90Row(rs: java.sql.ResultSet): MutableMap<String, Any?> {
        val md = rs.metaData
        val m = mutableMapOf<String, Any?>()
        for (i in 1..md.columnCount) m[md.getColumnLabel(i).lowercase()] = rs.getObject(i)
        return m
    }

    // ---------- PRC_员工导入提交 ----------

    @Transactional
    fun commitEmployeeImport(ace100: String, aae011: String): Map<String, Any?> {
        val u = user(aae011)
            ?: return mapOf("appCode" to -1, "errMsg" to "查询经办人信息出错，请将错误信息发给维护工程师")
        val rows = jdbcTemplate.query(
            "SELECT id FROM tc90_bp WHERE ace10a IN ('3','4') AND ace100=? ORDER BY id",
            { rs, _ -> rs.getLong("id") }, ace100.toLong())

        for (id in rows) {
            var nOk = 1
            var msg: String? = null
            val rec = loadRow("tc90_bp", id)
            try {
                val aac002Raw = str(rec["aac002"])
                var aac006: String? = null
                var aac004g: String? = null
                // 新增/复用个人基本信息
                var ac01Aac001: Long?
                if (rec["aac001"] == null) {
                    val gen = jdbcTemplate.queryForObject(
                        "SELECT nextval('seq_ac01_aac001'), nextval('seq_sys_baz001'), nextval('seq_sys_baz002')",
                        { rs, _ -> arrayOf(rs.getLong(1), rs.getLong(2), rs.getLong(3)) })!!
                    var aac002 = aac002Raw
                    if (rec["abp001"] == null && (aac002Raw?.length ?: 0) >= 15) {
                        val parsed = hzCases.parseAac002(aac002Raw ?: "")
                        if ((parsed[0] as Int) != 1) { nOk = 2; throw RuleException("身份证格式不正确|", 2) }
                        aac004g = parsed[1] as String?
                        aac006 = parsed[2] as String?
                        aac002 = parsed[3] as String?
                    }
                    var aac005: String? = null
                    if (rec["aac005"] != null) aac005 = codeItem("AAC005", str(rec["aac005"]), "民族", "0").second
                    var aac011: String? = null
                    if (rec["aac011"] != null) aac011 = codeItem("AAC011", str(rec["aac011"]), "学历", "1").second
                    val aac009 = codeItem("AAC009", str(rec["aac009"]), "户口性质", "1").second
                    val bab305 = jdbcTemplate.query(
                        "SELECT label FROM aa12 WHERE id='220000000000'",
                        { rs, _ -> rs.getString("label") }).firstOrNull()
                    ac01Aac001 = gen[0]
                    jdbcTemplate.update(
                        "INSERT INTO ac01 (aac001, baz001, baz002, aac002, aac003, aac004, aac006, aac005, " +
                            "aac011, aac009, aae006, aab299, bab305, aae005, aae011, aae019, aae017, aae020, aae022, aae036) " +
                            "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,now())",
                        ac01Aac001, gen[1], gen[2], aac002, str(rec["aac003"]), aac004g,
                        aac006?.let { day(it) }?.let { java.sql.Date.valueOf(it) },
                        aac005, aac011, aac009, str(rec["atc902"]), "220000000000", bab305,
                        str(rec["aae005"])?.take(20), str(u["aae011"]), str(u["aae019"]), str(u["aae017"]),
                        str(u["aae020"]), str(u["aae022"]))
                } else {
                    ac01Aac001 = (rec["aac001"] as Number).toLong()
                    val exists = jdbcTemplate.query(
                        "SELECT COUNT(*) FROM ac01 WHERE aac001=?", { rs, _ -> rs.getInt(1) }, ac01Aac001
                    ).firstOrNull() ?: 0
                    if (exists == 0)
                        return mapOf("appCode" to -1,
                            "errMsg" to "查询个人基本信息出错，请将错误信息发给维护工程师")
                }
                val ac01Row = jdbcTemplate.query(
                    "SELECT * FROM ac01 WHERE aac001=?", { rs, _ -> tc90Row(rs) }, ac01Aac001
                ).first() ?: throw IllegalStateException("ac01 missing")

                val ids = jdbcTemplate.queryForObject(
                    "SELECT nextval('seq_tc90_atc900'), nextval('seq_sys_baz001'), nextval('seq_sys_baz002'), " +
                        "nextval('seq_tb91_atb910')",
                    { rs, _ -> arrayOf(rs.getLong(1), rs.getLong(2), rs.getLong(3), rs.getLong(4)) })!!
                val atc900 = ids[0]; val baz001 = ids[1]; val baz002 = ids[2]; val atb910 = ids[3]

                // ---- 组装 TC90 行(内存 map, 终局一次 INSERT) ----
                val t = mutableMapOf<String, Any?>()
                t["atc900"] = atc900; t["baz001"] = baz001; t["baz002"] = baz002
                t["aac001"] = ac01Aac001
                t["aac002"] = str(ac01Row["aac002"])
                t["aac003"] = str(ac01Row["aac003"])
                t["aac004"] = str(ac01Row["aac004"])
                t["aac006"] = ac01Row["aac006"]
                // TRUNC(MONTHS_BETWEEN(SYSDATE, AAC006)/12) — 复用 V8 整月近似垫片
                t["atc901"] = ac01Row["aac006"]?.let {
                    jdbcTemplate.queryForObject(
                        "SELECT trunc(public.months_between(current_date, ?)/12)", BigDecimal::class.java,
                        java.sql.Date.valueOf(day(it).toString()))
                }
                codeItem("AAE00P", str(rec["aae00p"]), "用工性质", "1").let { (_, v, _) -> t["aae00p"] = v }
                if (rec["aac005"] != null) t["aac005"] = codeItem("AAC005", str(rec["aac005"]), "民族", "1").second
                if (rec["aac011"] != null) t["aac011"] = codeItem("AAC011", str(rec["aac011"]), "学历", "1").second
                t["aae006"] = str(rec["atc902"])?.take(200)
                t["atc902"] = str(rec["atc902"])?.take(200)
                if (rec["atc90ax"] != null) t["atc90ax"] = day(rec["atc90ax"])?.let { java.sql.Date.valueOf(it) }
                t["aac012"] = codeItem("AAC012", str(rec["aac012"]), "人员身份", "1").second
                t["aae00b"] = str(rec["aae00b"])?.take(20)
                t["aac009"] = codeItem("AAC009", str(rec["aac009"]), "户口性质", "1").second ?: "11"
                t["aaa605"] = codeItem("AAA605", str(rec["aaa605"]), "是否存在职业病危害", "1").second ?: "0"
                t["aac013"] = codeItem("AAC013", str(rec["aac013"]), "招工性质", "1").second ?: "1"
                t["aae00p"] = codeItem("AAE00P", str(rec["aae00p"]), "用工性质", "1").second
                t["aae005"] = str(rec["aae005"])?.take(50)
                t["aab004_1"] = str(rec["aab004_1"])?.take(100)
                t["aae00a"] = str(rec["aae00a"])?.take(20)
                t["atc909"] = codeItem("ATC909", str(rec["atc909"]), "用工性质", "1").second ?: "1"
                t["atc90b"] = day(rec["atc90b"])?.let { java.sql.Date.valueOf(it) }
                t["atc90a"] = codeItem("ATC90A", str(rec["atc90a"]), "合同类型", "1").second ?: "1"
                t["atc90c"] = day(rec["atc90c"])?.let { java.sql.Date.valueOf(it) }
                t["atc90d"] = day(rec["atc90d"])?.let { java.sql.Date.valueOf(it) }
                t["atc905"] = num(rec["atc905"])
                t["atc907"] = codeItem("ATC907", str(rec["atc907"]), "工时制度", "0").second
                t["atc908"] = (str(rec["atc908"])?.take(100)) ?: "工人"
                t["atc90ah"] = codeItem("ATC90AH", str(rec["atc90ah"]), "是否签订退休三方协议", "1").second ?: "0"
                t["aae002"] = num(rec["aae002"])
                t["atb90c"] = codeItem("ATB90C", str(rec["atb90c"]), "是否个人社保单位承担", "1").second ?: "0"
                t["atb90a"] = num(rec["atb90a"])
                t["atb90b"] = num(rec["atb90b"]) ?: BigDecimal.ZERO
                t["atc90zb"] = codeItem("ATC90ZB", str(rec["atc90zb"]), "是否参加工会", "1").second ?: "1"
                if (rec["atc90za"] != null) t["atc90za"] = codeItem("ATC90ZA", str(rec["atc90za"]), "工会会费扣除", "1").second
                if (rec["aaa401"] != null) t["aaa401"] = codeItem("AAA401", str(rec["aaa401"]), "是否参加养老保险", "1").second
                t["aaa403"] = t["aaa401"]; t["aaa404"] = t["aaa401"]
                if (rec["aaa100"] != null) aa12(rec["aaa100"])?.let { t["aaa100"] = it.first; t["aaa101"] = it.second }
                else if (t["aaa401"] == "1") { t["aaa100"] = "220100000000"; t["aaa101"] = "吉林省长春市" }
                t["aaa201"] = num(rec["aaa203"]); t["aaa203"] = num(rec["aaa203"])
                // 前合同编号继承（后被整块置 NULL 覆盖——死代码, 仅变更单位分支重新继承）
                t["atc90p"] = codeItem("ATC90P", str(rec["atc90p"]), "社保参保状态", "1").second
                if (rec["aaa402"] != null) t["aaa402"] = codeItem("AAA402", str(rec["aaa402"]), "是否参加医疗保险", "1").second
                t["aaa405"] = t["aaa402"]
                if (rec["aaa110"] != null) aa12(rec["aaa110"])?.let { t["aaa110"] = it.first; t["aaa111"] = it.second }
                else if (t["aaa402"] == "1") { t["aaa110"] = "220100000000"; t["aaa111"] = "吉林省长春市" }
                val a110 = str(t["aaa110"])
                if (a110 != null && a110.length >= 4 && a110.substring(2, 4) == "99") {
                    val c90c = t["atc90c"] as? java.sql.Date
                    t["aaa601"] = c90c?.let { BigDecimal(DateTimeFormatter.ofPattern("yyyyMM").format(it.toLocalDate())) }
                    t["aaa603"] = t["aaa601"]
                } else {
                    t["aaa601"] = num(rec["aaa603"]); t["aaa603"] = num(rec["aaa603"])
                }
                t["atc90pa"] = codeItem("ATC90PA", str(rec["atc90pa"]), "医保参保状态", "1").second
                // 工伤 = 社保镜像（AAA901 两次赋值, 后者生效）
                t["atc90pb"] = t["atc90p"]
                val c90cDate = t["atc90c"] as? java.sql.Date
                t["aaa901"] = c90cDate?.let { BigDecimal(DateTimeFormatter.ofPattern("yyyyMM").format(it.toLocalDate())) }
                t["aaa902"] = null
                // 前合同 AAA009 查询后被整块置 NULL 覆盖——死代码
                t["aaa130"] = t["aaa100"]; t["aaa131"] = t["aaa101"]
                t["aaa903"] = t["aaa901"]
                t["aaa904"] = null
                t["aaa908"] = null; t["aaa909"] = null
                if (rec["aaa407"] != null) t["aaa407"] = codeItem("AAA407", str(rec["aaa407"]), "是否缴纳公积金", "1").second
                if (rec["aaa120"] != null) aa12(rec["aaa120"])?.let { t["aaa120"] = it.first; t["aaa121"] = it.second }
                else if (t["aaa407"] == "1") { t["aaa120"] = "220100000000"; t["aaa121"] = "吉林省长春市" }
                if (rec["atb90s"] != null) t["atb90s"] = codeItem("ATB90S", str(rec["atb90s"]), "公积金比例", "1").second
                t["aaa501"] = num(rec["aaa503"]); t["aaa503"] = num(rec["aaa503"])
                t["atc90w"] = codeItem("ATC90W", str(rec["atc90w"]), "公积金参保状态", "1").second
                t["aaa301"] = num(rec["aaa301"]); t["aaa302"] = num(rec["aaa302"]); t["aaa303"] = num(rec["aaa303"])
                t["aaa304"] = num(rec["aaa301"]); t["aaa305"] = num(rec["aaa301"]); t["aaa306"] = num(rec["aaa302"])
                t["atc90g"] = if (listOf("aaa401", "aaa402", "aaa403", "aaa404", "aaa407").any { t[it] == "1" }) "1" else "0"
                t["bbb001"] = str(rec["bbb001"])?.replace(" ", "")?.take(100)
                t["bbb002"] = codeItem("BBB002", str(rec["bbb002"]), "发放银行", "1").second
                if (t["bbb001"] == null) {
                    t["bbb001"] = str(rec["bbb001"])?.replace(" ", "")?.take(100)
                }
                if (t["bbb002"] == null) t["bbb002"] = "02000"
                if (str(t["aae00p"]) in setOf("2", "3", "4", "6")) {
                    for (k in listOf("aaa401", "aaa402", "aaa403", "aaa404", "aaa405", "aaa407")) t[k] = "0"
                }
                t["aae100"] = "1"; t["atc90f"] = "1"; t["ccc001"] = "1"
                t["atb930"] = rec["atb930"]
                t["atc90x"] = jdbcTemplate.query(
                    "SELECT atb931 FROM tb93 WHERE atb930=?", { rs, _ -> rs.getString("atb931") },
                    (rec["atb930"] as Number).toLong()).firstOrNull()
                t["atb900"] = rec["atb900"]
                t["aab001"] = rec["aab001"]
                t["id"] = str(rec["id"])
                t["aab004"] = str(rec["aab004"])
                t["atc90e"] = java.sql.Date.valueOf(LocalDate.now())
                t["aaa001"] = null; t["aaa002"] = null; t["aaa003"] = null; t["aaa004"] = null; t["aaa005"] = null; t["aaa009"] = null
                t["atc90q"] = null; t["atc90t1"] = null; t["atc90t2"] = null; t["atc90t3"] = null
                t["atc90t4"] = null; t["atc90t5"] = null; t["atc90t6"] = null
                t["atc90aa"] = null; t["atc90ab"] = null; t["atc90ac"] = null; t["atc90ad"] = null
                t["atc90ae"] = null; t["atc90ag"] = null; t["atc90aq"] = null; t["atc90ar"] = null
                t["atc90ai"] = "0"; t["atc90at"] = "0"
                if (rec["atc90r"] != null) t["atc90r"] = codeItem("ATC90R", str(rec["atc90r"]), "跨年缴费基数标识", "1").second
                t["aae011"] = str(u["aae011"]); t["aae017"] = str(u["aae017"]); t["aae019"] = str(u["aae019"])
                t["aae020"] = str(u["aae020"]); t["aae022"] = str(u["aae022"])
                t["aae036"] = java.sql.Timestamp(System.currentTimeMillis())
                t["aae013"] = if (str(t["atc90p"]) == "0" || str(t["atc90w"]) == "0" || str(t["atc90pa"]) == "0") "历史数据"
                else str(rec["aae013"])?.take(500)
                val tb912 = if (str(t["aae00p"]) !in setOf("1", "5")) "3" else "2"
                t["atb90r"] = tb912 // 原: NOT IN (1,5) → '3', 否则 '2'（与 tb912 同值同条件）
                t["atb910"] = atb910
                t["atc9wc"] = rec["atc9wc"]

                // ---- 变更单位分支（ATC900_BGQ 非空）----
                if (rec["atc900_bgq"] != null) {
                    val bgq = jdbcTemplate.query(
                        "SELECT * FROM tc90 WHERE atc900=?", { rs, _ -> tc90Row(rs) },
                        (rec["atc900_bgq"] as Number).toLong()).firstOrNull()
                        ?: return mapOf("appCode" to -1, "errMsg" to "查询变更前合同出错，原因为：无数据")
                    var atc90pOld: String? = null; var atc90paOld: String? = null
                    var atc90wOld: String? = null; var atc90pbOld: String? = null
                    var bgqA209: Any? = bgq["aaa209"]; var bgqA509: Any? = bgq["aaa509"]
                    var bgqA609: Any? = bgq["aaa609"]; var bgqA909: Any? = bgq["aaa909"]
                    t["aaa001"] = str(bgq["aaa001"]); t["aaa005"] = str(bgq["aaa005"])
                    t["aaa004"] = str(bgq["aaa004"]); t["aaa009"] = str(bgq["aaa009"])
                    t["aac008"] = str(bgq["aac008"])
                    fun appTick(code: String) {
                        val cur = str(t["atc9wc"])
                        t["atc9wc"] = if (cur == null) code
                        else if (!cur.contains(code)) "$cur,$code" else cur
                    }
                    // 社保
                    if ((str(bgq["atc90p"]) == "1" || (bgq["atc90p"] == null &&
                                (str(bgq["aaa401"]) == "0" || bgq["aaa401"] == null || bgq["aaa209"] != null))) && t["aaa401"] == "1") {
                        t["atc90p"] = null; t["aaa201"] = null; t["aaa202"] = null; t["aaa204"] = null; t["aaa209"] = null
                        if (t["aaa401"] == "1") appTick("21")
                    } else {
                        t["atc90p"] = str(bgq["atc90p"]); t["aaa201"] = bgq["aaa201"]; t["aaa203"] = bgq["aaa203"]
                        t["aaa208"] = bgq["aaa208"]; t["aaa202"] = null; t["aaa204"] = null; t["aaa209"] = null
                        bgqA209 = t["aae002"]
                    }
                    if (t["aaa401"] == null) {
                        t["aaa401"] = str(bgq["aaa401"]); t["aaa403"] = str(bgq["aaa403"])
                        t["aaa301"] = bgq["aaa301"]; t["aaa304"] = bgq["aaa304"]
                        t["aaa100"] = str(bgq["aaa100"]); t["aaa101"] = str(bgq["aaa101"])
                    }
                    // 医保
                    if ((str(bgq["atc90pa"]) == "1" || (bgq["atc90pa"] == null &&
                                (str(bgq["aaa402"]) == "0" || bgq["aaa402"] == null || bgq["aaa609"] != null))) && t["aaa402"] == "1") {
                        t["atc90pa"] = null; t["aaa601"] = null; t["aaa602"] = null; t["aaa604"] = null; t["aaa609"] = null
                        if (t["aaa402"] == "1") appTick("22")
                    } else {
                        t["atc90pa"] = str(bgq["atc90pa"]); t["aaa601"] = bgq["aaa601"]; t["aaa603"] = bgq["aaa603"]
                        t["aaa608"] = bgq["aaa608"]; t["aaa602"] = null; t["aaa604"] = null; t["aaa609"] = null
                        bgqA609 = t["aae002"]
                    }
                    if (t["aaa402"] == null) {
                        t["aaa402"] = str(bgq["aaa402"]); t["aaa405"] = str(bgq["aaa405"])
                        t["aaa302"] = bgq["aaa302"]; t["aaa305"] = bgq["aaa305"]; t["aaa306"] = bgq["aaa306"]
                        t["aaa110"] = str(bgq["aaa110"]); t["aaa111"] = str(bgq["aaa111"])
                    }
                    // 工伤
                    if ((str(bgq["atc90pb"]) == "1" || (bgq["atc90pb"] == null &&
                                (str(bgq["aaa404"]) == "0" || bgq["aaa404"] == null))) && t["aaa404"] == "1") {
                        t["atc90pb"] = null; t["aaa901"] = null; t["aaa902"] = null; t["aaa904"] = null; t["aaa909"] = null
                        if (t["aaa401"] == "1") appTick("24") // 原判断用 AAA401——原样保留
                    } else {
                        t["atc90pb"] = str(bgq["atc90pb"]); t["aaa901"] = bgq["aaa901"]; t["aaa903"] = bgq["aaa903"]
                        t["aaa908"] = bgq["aaa908"]; t["aaa902"] = null; t["aaa904"] = null; t["aaa909"] = null
                        bgqA909 = t["aae002"]
                    }
                    if (t["aaa401"] == null) {
                        t["aaa404"] = str(bgq["aaa404"]); t["aaa305"] = bgq["aaa305"]
                        t["aaa130"] = str(bgq["aaa130"]); t["aaa131"] = str(bgq["aaa131"])
                    }
                    // 公积金
                    if ((str(bgq["atc90w"]) == "1" || (bgq["atc90w"] == null &&
                                (str(bgq["aaa407"]) == "0" || bgq["aaa407"] == null))) && t["aaa407"] == "1") {
                        t["atc90w"] = null; t["aaa501"] = null; t["aaa502"] = null; t["aaa504"] = null; t["aaa509"] = null
                        if (t["aaa407"] == "1") appTick("23")
                    } else {
                        t["atc90w"] = str(bgq["atc90w"]); t["aaa501"] = bgq["aaa501"]; t["aaa503"] = bgq["aaa503"]
                        t["aaa508"] = bgq["aaa508"]; t["aaa502"] = null; t["aaa504"] = null; t["aaa509"] = null
                        bgqA509 = t["aae002"]
                    }
                    if (t["aaa407"] == null) {
                        t["aaa407"] = str(bgq["aaa407"]); t["aaa303"] = bgq["aaa303"]
                        t["aaa120"] = str(bgq["aaa120"]); t["aaa121"] = str(bgq["aaa121"])
                        t["atb90s"] = str(bgq["atb90s"])
                    }
                    // 原合同"维持现状"默认
                    if (str(bgq["atc90p"]) == "0" && t["aaa401"] == "1") {
                        atc90pOld = "3"; atc90pbOld = "3"
                        t["aaa201"] = bgq["aaa201"]; t["aaa203"] = bgq["aaa203"]; t["aaa208"] = bgq["aaa208"]
                        bgqA209 = t["aae002"]
                        t["aaa901"] = bgq["aaa901"]; t["aaa903"] = bgq["aaa903"]; t["aaa908"] = bgq["aaa908"]
                        bgqA909 = t["aae002"]
                    }
                    if (str(bgq["atc90pa"]) == "0" && t["aaa402"] == "1") {
                        atc90paOld = "3"
                        t["aaa601"] = bgq["aaa601"]; t["aaa603"] = bgq["aaa603"]; t["aaa608"] = bgq["aaa608"]
                        bgqA609 = t["aae002"]
                    }
                    if (str(bgq["atc90w"]) == "0" && t["aaa407"] == "1") {
                        atc90wOld = "3"
                        t["aaa501"] = bgq["aaa501"]; t["aaa503"] = bgq["aaa503"]; t["aaa508"] = bgq["aaa508"]
                        bgqA509 = t["aae002"]
                    }
                    // 已参保合同的三类变更
                    if (str(bgq["atc90p"]) == "0") {
                        if (num(bgq["atc905"]) != num(t["atc905"]) && (num(t["atc905"]) ?: BigDecimal.ZERO) > BigDecimal.ZERO && t["aaa401"] == "1") {
                            t["atc90p"] = null; t["aaa201"] = t["aae002"]; t["aaa203"] = t["aae002"]; t["aaa208"] = null
                            atc90pOld = str(bgq["atc90p"]); bgqA209 = null
                            t["atc90pb"] = null; t["aaa901"] = t["aae002"]; t["aaa903"] = t["aae002"]; t["aaa908"] = null
                            atc90pbOld = str(bgq["atc90pb"]); t["atc9wc"] = (str(t["atc9wc"]) ?: "") + ",13"
                            bgqA909 = null
                        }
                        if (str(bgq["aaa100"]) != str(t["aaa100"]) && t["aaa401"] == "1") {
                            t["aaa001"] = null; t["atc90p"] = null; t["aaa201"] = t["aae002"]; t["aaa203"] = t["aae002"]; t["aaa208"] = null
                            atc90pOld = str(bgq["atc90p"]); t["aac008"] = str(t["aac008"])?.replace("27", ""); bgqA209 = null
                            t["aaa009"] = null; t["atc90pb"] = null; t["aaa901"] = t["aae002"]; t["aaa903"] = t["aae002"]; t["aaa908"] = null
                            atc90pbOld = str(bgq["atc90pb"]); bgqA909 = null
                        }
                        if (t["aaa401"] == "0") {
                            t["atc90p"] = null; t["aaa201"] = null; t["aaa203"] = null; t["aaa208"] = null
                            atc90pOld = str(bgq["atc90p"]); t["aac008"] = str(t["aac008"])?.replace("27", ""); bgqA209 = null
                            t["atc90pb"] = null; t["aaa901"] = null; t["aaa903"] = null; t["aaa908"] = null
                            atc90pbOld = str(bgq["atc90pb"]); bgqA909 = null
                        }
                    }
                    if (str(bgq["atc90pa"]) == "0") {
                        if (num(bgq["aaa302"]) != num(t["aaa302"]) && (num(t["aaa302"]) ?: BigDecimal.ZERO) > BigDecimal.ZERO &&
                            str(bgq["aaa110"]) == str(t["aaa110"]) && str(bgq["aaa110"])?.let {
                                it.length >= 4 && it.substring(2, 4) == "99"
                            } == true && t["aaa402"] == "1") {
                            t["atc90pa"] = null; t["aaa601"] = t["aae002"]; t["aaa603"] = t["aae002"]; t["aaa608"] = null
                            atc90paOld = str(bgq["atc90pa"])
                        }
                        if (str(bgq["aaa110"]) != str(t["aaa110"]) && t["aaa402"] == "1") {
                            t["aaa005"] = null; t["atc90pa"] = null; t["aaa601"] = t["aae002"]; t["aaa603"] = t["aae002"]; t["aaa608"] = null
                            atc90paOld = str(bgq["atc90pa"])
                            t["aac008"] = str(t["aac008"])?.replace("51", "")?.replace("52", "")
                        }
                        if (t["aaa402"] == "0") {
                            t["atc90pa"] = null; t["aaa601"] = null; t["aaa603"] = null; t["aaa608"] = null
                            atc90paOld = str(bgq["atc90pa"])
                            t["aac008"] = str(t["aac008"])?.replace("51", "")?.replace("52", "")
                        }
                    }
                    if (str(bgq["atc90w"]) == "0") {
                        // 基数调整块整段注释（死代码）
                        if (str(bgq["aaa120"]) == str(t["aaa120"]) && str(bgq["atb90s"]) != str(t["atb90s"]) && t["aaa407"] == "1") {
                            val cur = str(t["atc9wc"])
                            t["atc9wc"] = if (cur == null) "11" else if (!cur.contains("11")) "$cur,11" else cur
                        }
                        if (str(bgq["aaa120"]) != str(t["aaa120"]) && t["aaa407"] == "1") {
                            t["aaa004"] = null; t["atc90w"] = null; t["aaa501"] = t["aae002"]; t["aaa503"] = t["aae002"]; t["aaa508"] = null
                            atc90wOld = str(bgq["atc90w"])
                        }
                        if (t["aaa407"] == "0") {
                            t["atc90w"] = null; t["aaa501"] = null; t["aaa503"] = null; t["aaa508"] = null
                            atc90wOld = str(bgq["atc90w"])
                        }
                    }
                    jdbcTemplate.update(
                        "UPDATE tc90 SET atc90p=?, atc90pa=?, atc90w=?, atc90pb=?, aaa209=?, aaa509=?, aaa609=?, aaa909=? WHERE atc900=?",
                        atc90pOld, atc90paOld, atc90wOld, atc90pbOld, bgqA209, bgqA509, bgqA609, bgqA909,
                        (rec["atc900_bgq"] as Number).toLong())
                }

                // AB01 + 派遣编号 + 历史合同工资结束年月 + 落库
                val ab01 = jdbcTemplate.query(
                    "SELECT * FROM ab01 WHERE aab001=?", { rs, _ -> tc90Row(rs) }, (rec["aab001"] as Number).toLong()
                ).firstOrNull()
                if (ab01 == null) { nOk = 0; throw RuleException("查询单位信息出错") }
                if (str(t["aae00p"]) in setOf("1", "5", "7")) {
                    t["zynum"] = 序列(str(ab01["aaa030"]), str(t["aae002"]), str(t["aaa110"]), "A")
                }
                val aae002Num = num(t["aae002"]) ?: BigDecimal.ZERO
                jdbcTemplate.query(
                    "SELECT atc900 FROM tc90 WHERE atc90av >= ? AND atb930=? AND aac001=?",
                    { rs, _ -> rs.getLong("atc900") }, aae002Num, t["atb930"], ac01Aac001
                ).forEach { ls ->
                    val prevMonth = day(str(t["aae002"]) + "01")!!.minusMonths(1)
                        .format(DateTimeFormatter.ofPattern("yyyyMM"))
                    jdbcTemplate.update("UPDATE tc90 SET atc90av=? WHERE atc900=?", BigDecimal(prevMonth), ls)
                }
                try {
                    insertTc90(t)
                } catch (e: Exception) { nOk = 0; throw RuleException("插入合同信息出错") }
                insertTc90ListMirror(atc900 as Long) // 原无守卫 → 失败冒泡整批中止
                try {
                    jdbcTemplate.update(
                        "INSERT INTO tb91 (atb910, baz001, baz002, atb91z, atb911, atb912, atb913, aab004, atb931, " +
                            "aae011, aae017, aae036, aae019, aae020, aae022, aae013) " +
                            "VALUES (?,?,?,?,?,?,?,?,?,?,?,now(),?,?,?,?)",
                        atb910, baz001, baz002, atc900, "20", tb912,
                        "${t["aac003"]}于${LocalDate.now()}新增员工信息，请审核",
                        t["aab004"], t["atc90x"], str(u["aae011"]), str(u["aae017"]),
                        str(u["aae019"]), str(u["aae020"]), str(u["aae022"]), t["aae013"])
                } catch (e: Exception) { nOk = 0; throw RuleException("插入审核表信息出错") }
                jdbcTemplate.update("UPDATE tc90_bp SET ace10a='1' WHERE id=?", id)
            } catch (e: RuleException) {
                fail(e, "tc90_bp", id, nOk, msg)
            }
        }
        val stat = jdbcTemplate.queryForMap(
            "SELECT COUNT(1) total, " +
                "COALESCE(SUM(CASE WHEN ace10a='1' THEN 1 ELSE 0 END),0) ok, " +
                "COALESCE(SUM(CASE WHEN ace10a<>'1' THEN 1 ELSE 0 END),0) fail " +
                "FROM tc90_bp WHERE ace100=?", ace100.toLong())
        return mapOf("appCode" to 1, "errMsg" to
            "提交完成，共导入【${stat["total"]}】条数据，其中【${stat["ok"]}】条数据成功，【${stat["fail"]}】条数据失败！")
    }

    /** TC90 动态整行 INSERT（列集取自 information_schema，类型按列绑定） */
    private fun insertTc90(t: Map<String, Any?>) {
        val cols = jdbcTemplate.query(
            "SELECT column_name, data_type FROM information_schema.columns " +
                "WHERE table_schema='public' AND table_name='tc90' ORDER BY ordinal_position",
            { rs, _ -> Pair(rs.getString(1), rs.getString(2)) })
        val names = StringBuilder(); val ph = StringBuilder(); val args = mutableListOf<Any?>()
        for ((i, c) in cols.withIndex()) {
            val v = t[c.first]
            if (i > 0) { names.append(','); ph.append(',') }
            names.append(c.first); ph.append('?')
            args.add(when (c.second) {
                "numeric", "bigint", "integer", "smallint", "double precision" ->
                    when (v) { null -> null; is BigDecimal -> v; is Number -> BigDecimal(v.toString()); else -> strictCol(v) }
                "timestamp without time zone", "date" -> v
                else -> str(v)
            })
        }
        jdbcTemplate.update("INSERT INTO tc90 ($names) VALUES ($ph)", *args.toTypedArray())
    }

    /** INSERT INTO TC90_LIST SELECT ... 全列镜像（列集=tc90_list-ikey） */
    private fun insertTc90ListMirror(atc900: Long) {
        val cols = jdbcTemplate.query(
            "SELECT column_name FROM information_schema.columns " +
                "WHERE table_schema='public' AND table_name='tc90_list' AND column_name<>'ikey' " +
                "ORDER BY ordinal_position", { rs, _ -> rs.getString(1) })
        val collist = cols.joinToString(",")
        val selist = cols.joinToString(",")
        jdbcTemplate.update(
            "INSERT INTO tc90_list (ikey, $collist) SELECT nextval('seq_tc90_list_ikey'), $selist FROM tc90 WHERE atc900=?",
            atc900)
    }

    // ---------- PRC_员工终止导入校验 ----------

    @Transactional
    fun checkTerminateImport(ace100: String, aae011: String): Map<String, Any?> {
        val u = user(aae011)
            ?: return mapOf("appCode" to -1, "errMsg" to "为查询到经办人信息")
        val a95 = aa95()
            ?: return mapOf("appCode" to -1, "errMsg" to "为设置时限参数")
        val a951 = (a95["aaa951"] as? Number)?.toInt() ?: 0
        val today = LocalDate.now()
        val curYm = today.format(DateTimeFormatter.ofPattern("yyyyMM"))
        val curYmd = today.format(DateTimeFormatter.ofPattern("yyyyMMdd"))

        val rows = jdbcTemplate.query(
            "SELECT id FROM tc90_bp1 WHERE (ace10a IS NULL OR ace10a IN ('0','2','3')) AND ace100=?",
            { rs, _ -> rs.getLong("id") }, ace100.toLong())
        for (id in rows) {
            var nOk = 3
            var msg: String? = null
            val rec = loadRow("tc90_bp1", id)
            try {
                rec["ace10a"] = 3
                // 原身份证/姓名清洗与 PRC_PARSE_AAC002 校验整段被注释（死代码）
                val tc90 = jdbcTemplate.query(
                    "SELECT * FROM tc90 WHERE atc900=(SELECT max(atc900) FROM tc90 WHERE atb930=? AND aac003=? AND aac002=?)",
                    { rs, _ -> tc90Row(rs) }, rec["atb930"], rec["aac003"], rec["aac002"]
                ).firstOrNull()
                if (tc90 == null || tc90["atc900"] == null)
                    throw RuleException("无有效合同，请核对填写的姓名和身份证号码是否正确|", 2).also { nOk = 2 }
                val aac001 = (tc90["aac001"] as Number?)?.toLong()
                if (jdbcTemplate.queryForObject(
                        "SELECT COUNT(*) FROM tc90 WHERE aae00p IN ('1','3','4') AND atb90r NOT IN ('3','5') AND aac001=?",
                        Int::class.java, aac001) > 0)
                    throw RuleException("该人员存在审核流程中合同|", 2).also { nOk = 2 }
                val atb90r = str(tc90["atb90r"])
                // 合同终止日期
                if (rec["atc90i"] != null) {
                    if (day(rec["atc90i"]) == null) { nOk = 2; msg = (msg ?: "") + "【合同终止日期】格式不正确|" }
                } else { nOk = 2; msg = (msg ?: "") + "【合同终止日期】不能为空|" }
                // 四险报减年月（报减仅当对应险种参加）
                if (rec["aaa202"] != null && str(tc90["aaa401"]) == "1") {
                    if (day(str(rec["aaa202"]) + "01") == null) { nOk = 2; msg = (msg ?: "") + "【社保报减年月】格式不正确|" }
                    if (str(rec["aaa202"])!! < curYm && atb90r != "5") { nOk = 2; msg = (msg ?: "") + "【社保报减年月】不能小于当月|" }
                }
                if (rec["aaa502"] != null && str(tc90["aaa407"]) == "1") {
                    if (day(str(rec["aaa502"]) + "01") == null) { nOk = 2; msg = (msg ?: "") + "【公积金报减年月】格式不正确|" }
                    if (str(rec["aaa502"])!! < curYm && atb90r != "5") { nOk = 2; msg = (msg ?: "") + "【公积金报减年月】不能小于当月|" }
                }
                if (rec["aaa602"] != null && str(tc90["aaa402"]) == "1") {
                    if (day(str(rec["aaa602"]) + "01") == null) { nOk = 2; msg = (msg ?: "") + "【医保报减年月】格式不正确|" }
                    if (str(rec["aaa602"])!! < curYm && atb90r != "5") { nOk = 2; msg = (msg ?: "") + "【医保报减年月】不能小于当月|" }
                }
                if (rec["aaa902"] != null && str(tc90["aaa404"]) == "1") {
                    if (day(str(rec["aaa902"]) + "01") == null) { nOk = 2; msg = (msg ?: "") + "【工伤报减年月】格式不正确|" }
                    if (str(rec["aaa902"])!! < curYm && atb90r != "5") { nOk = 2; msg = (msg ?: "") + "【工伤报减年月】不能小于当月|" }
                }
                // 资料提交年月
                if (rec["aae003"] != null) {
                    if (day(str(rec["aae003"]) + "01") == null) { nOk = 2; msg = (msg ?: "") + "【资料提交年月】格式不正确|" }
                    if (str(tc90["aae00p"]) in setOf("1", "5")) {
                        val v = str(rec["aae003"])!!
                        if (v < curYm && atb90r != "5") { nOk = 2; msg = (msg ?: "") + "【资料提交年月】不能小于为当月|" }
                        if (curYmd.substring(6, 8).toInt() > a951 && v == curYm && atb90r != "5")
                            msg = (msg ?: "") + "已超过办理时限，【资料提交年月】不能录入为当月，可以设置为下月|".let { nOk = 2; it }
                        if (curYmd.substring(6, 8).toInt() > a951 && str(rec["aaa202"]) == curYm && atb90r != "5")
                            msg = (msg ?: "") + "已超过办理时限，【社保报减年月】不能录入为当月，可以设置为下月|".let { nOk = 2; it }
                        if (curYmd.substring(6, 8).toInt() > a951 && str(rec["aaa502"]) == curYm && atb90r != "5")
                            msg = (msg ?: "") + "已超过办理时限，【公积金报减年月】不能录入为当月，可以设置为下月|".let { nOk = 2; it }
                        if (curYmd.substring(6, 8).toInt() > a951 && str(rec["aaa602"]) == curYm && atb90r != "5")
                            msg = (msg ?: "") + "已超过办理时限，【医保报减年月】不能录入为当月，可以设置为下月|".let { nOk = 2; it }
                    }
                } else { nOk = 2; msg = (msg ?: "") + "【资料提交年月】不能为空|" }
                codeItem("ATC90K", str(rec["atc90k"]), "合同终止原因", "1").let { (c, v, m) ->
                    nOk = c; msg = (msg ?: "") + (m ?: ""); rec["atc90k"] = v
                }
                codeItem("ATC90L", str(rec["atc90l"]), "是否享受失业金", "1").let { (c, v, m) ->
                    nOk = c; msg = (msg ?: "") + (m ?: ""); rec["atc90l"] = v
                }
                if (rec["atc90av"] != null) {
                    if (day(str(rec["atc90av"]) + "01") == null) { nOk = 2; msg = (msg ?: "") + "【工资结束年月】格式不正确|" }
                } else { nOk = 2; msg = (msg ?: "") + "【工资结束年月】不能为空|" }
                codeItem("ATC90N", str(rec["atc90n"]), "是否经济赔偿", "1").let { (c, v, m) ->
                    nOk = c; msg = (msg ?: "") + (m ?: ""); rec["atc90n"] = v
                }
                if (rec["atc90p"] != null) codeItem("ATC90P", str(rec["atc90p"]), "社保参保状态", "1").let { (c, v, m) ->
                    nOk = c; msg = (msg ?: "") + (m ?: ""); rec["atc90p"] = v
                }
                if (rec["atc90pa"] != null) codeItem("ATC90PA", str(rec["atc90pa"]), "医保参保状态", "1").let { (c, v, m) ->
                    nOk = c; msg = (msg ?: "") + (m ?: ""); rec["atc90pa"] = v
                }
                if (rec["atc90w"] != null) codeItem("ATC90W", str(rec["atc90w"]), "公积金参保状态", "1").let { (c, v, m) ->
                    nOk = c; msg = (msg ?: "") + (m ?: ""); rec["atc90w"] = v
                }
                if (msg != null && msg!!.isNotEmpty()) { nOk = 2; throw RuleException() }
                jdbcTemplate.update(
                    "UPDATE tc90_bp1 SET bae011=?, bae017=?, bae019=?, bae036=now(), bae020=?, bae022=?, " +
                        "aac001=?, aac002=?, aac003=?, ace10a=?, ace10b=? WHERE id=?",
                    *auditArgs(u), rec["aac001"], rec["aac002"], rec["aac003"], nOk.toString(), msg, id)
            } catch (e: RuleException) {
                val m = if (e.msg != null) (msg ?: "") + e.msg else msg
                jdbcTemplate.update(
                    "UPDATE tc90_bp1 SET bae011=?, bae017=?, bae019=?, bae036=now(), bae020=?, bae022=?, " +
                        "ace10a=?, ace10b=? WHERE id=?",
                    *auditArgs(u), (e.code ?: nOk).toString(), m, id)
            }
        }
        return mapOf("appCode" to 1, "errMsg" to null as String?)
    }

    // ---------- PRC_员工终止导入提交 ----------

    @Transactional
    fun commitTerminateImport(ace100: String, aae011: String): Map<String, Any?> {
        val u = user(aae011)
            ?: return mapOf("appCode" to -1, "errMsg" to "查询经办人信息出错，请将错误信息发给维护工程师")
        val rows = jdbcTemplate.query(
            "SELECT id FROM tc90_bp1 WHERE ace10a='3' AND ace100=?", { rs, _ -> rs.getLong("id") }, ace100.toLong())
        for (id in rows) {
            var nOk = 1
            var msg: String? = null
            val rec = loadRow("tc90_bp1", id)
            try {
                val tc90 = jdbcTemplate.query(
                    "SELECT * FROM tc90 WHERE atc900=(SELECT max(atc900) FROM tc90 WHERE atb930=? AND aac003=? AND aac002=?)",
                    { rs, _ -> tc90Row(rs) }, rec["atb930"], rec["aac003"], rec["aac002"]
                ).firstOrNull()
                if (tc90 == null || tc90["atc900"] == null)
                    throw RuleException("无有效合同|", 2).also { nOk = 2 }
                val atc900 = (tc90["atc900"] as Number).toLong()
                val atb90r = str(tc90["atb90r"])
                val aae00p = str(tc90["aae00p"])
                // 新终止信息（列类型: tc90.atc90i/aae003(numeric)/aaa202..902(numeric)/atc90k/l/av(numeric)）
                val atc90i = day(rec["atc90i"])
                val aae003 = strictNum(rec["aae003"])
                val a202 = strictNum(rec["aaa202"]); val a502 = strictNum(rec["aaa502"])
                val a602 = strictNum(rec["aaa602"]); val a902 = strictNum(rec["aaa902"])
                val atc90k = codeItem("ATC90K", str(rec["atc90k"]), "合同终止原因", "1").second
                val atc90l = codeItem("ATC90L", str(rec["atc90l"]), "是否享受失业金", "1").second
                val atc90av = strictNum(rec["atc90av"]) // tc90.atc90av numeric; varchar→number 隐式转换
                val atc90n = codeItem("ATC90N", str(rec["atc90n"]), "是否经济赔偿", "1").second
                val atc90p = if (rec["atc90p"] != null) codeItem("ATC90P", str(rec["atc90p"]), "社保参保状态", "1").second else str(tc90["atc90p"])
                val atc90pa = if (rec["atc90pa"] != null) codeItem("ATC90PA", str(rec["atc90pa"]), "医保参保状态", "1").second else str(tc90["atc90pa"])
                val atc90w = if (rec["atc90w"] != null) codeItem("ATC90W", str(rec["atc90w"]), "公积金参保状态", "1").second else str(tc90["atc90w"])
                val (newAtb90r, tb912) = if (aae00p !in setOf("1", "5", "7")) "5" to "3" else "2" to "2"
                val ab01 = jdbcTemplate.query(
                    "SELECT * FROM ab01 WHERE aab001=?", { rs, _ -> tc90Row(rs) }, (tc90["aab001"] as Number).toLong()
                ).firstOrNull()
                if (ab01 == null) { nOk = 0; throw RuleException("查询单位信息出错") }
                var jynum: String? = null
                if (aae00p in setOf("1", "5", "7") && atb90r == "3")
                    jynum = 序列(str(ab01["aaa030"]), str(rec["aae003"]), str(tc90["aaa110"]), "D")
                if (atb90r == "3") {
                    jdbcTemplate.update(
                        "UPDATE tc90 SET atc90i=?, aae003=?, aaa202=?, aaa204=?, aaa502=?, aaa504=?, aaa602=?, " +
                            "aaa604=?, aaa902=?, aaa904=?, atc90k=?, atc90l=?, atc90av=?, atc90n=?, " +
                            "bae011=?, bae017=?, bae036=now(), bae019=?, bae020=?, bae022=?, atb90r=?, " +
                            "atc90p=(CASE WHEN ?='1' THEN ? ELSE atc90p END), " +
                            "atc90pa=(CASE WHEN ?='1' THEN ? ELSE atc90pa END), " +
                            "atc90pb=(CASE WHEN ?='1' THEN ? ELSE atc90pb END), " +
                            "atc90w=(CASE WHEN ?='1' THEN ? ELSE atc90w END), jynum=? WHERE atc900=?",
                        atc90i, aae003, a202, a202, a502, a502, a602, a602, a902, a902,
                        atc90k, atc90l, atc90av, atc90n,
                        str(u["aae011"]), str(u["aae017"]), str(u["aae019"]), str(u["aae020"]), str(u["aae022"]),
                        newAtb90r,
                        atc90p, atc90p, atc90pa, atc90pa,
                        // 原 CASE 用 R_TC90.ATC90PB——终止流程未赋值(=原值), 保留语义
                        str(tc90["atc90pb"]), str(tc90["atc90pb"]), atc90w, atc90w, jynum, atc900)
                    val ids = jdbcTemplate.queryForObject(
                        "SELECT nextval('seq_tb91_atb910'), nextval('seq_sys_baz001'), nextval('seq_sys_baz002')",
                        { rs, _ -> arrayOf(rs.getLong(1), rs.getLong(2), rs.getLong(3)) })!!
                    jdbcTemplate.update(
                        "INSERT INTO tb91 (atb910, baz001, baz002, atb91z, atb911, atb912, atb913, aab004, atb931, " +
                            "aae011, aae017, aae036, aae019, aae020, aae022) " +
                            "VALUES (?,?,?,?,?,?,?,?,?,?,?,now(),?,?,?)",
                        ids[0], ids[1], ids[2], atc900, "23", tb912,
                        "${tc90["aac003"]}于${LocalDate.now()}终止员工信息，请审核",
                        tc90["aab004"], tc90["aab004"], str(u["aae011"]), str(u["aae017"]),
                        str(u["aae019"]), str(u["aae020"]), str(u["aae022"]))
                } else if (atb90r == "5") {
                    jdbcTemplate.update(
                        "UPDATE tc90 SET " +
                            "aaa202=(CASE WHEN ?='0' THEN ? ELSE aaa202 END), aaa204=(CASE WHEN ?='0' THEN ? ELSE aaa204 END), " +
                            "aaa502=(CASE WHEN ?='0' THEN ? ELSE aaa502 END), aaa504=(CASE WHEN ?='0' THEN ? ELSE aaa504 END), " +
                            "aaa602=(CASE WHEN ?='0' THEN ? ELSE aaa602 END), aaa604=(CASE WHEN ?='0' THEN ? ELSE aaa604 END), " +
                            "aaa902=(CASE WHEN ?='0' THEN ? ELSE aaa902 END), aaa904=(CASE WHEN ?='0' THEN ? ELSE aaa904 END), " +
                            "atc90av=?, atc90i=?, atc90k=? WHERE atc900=?",
                        // 忠实怪癖: AAA604 的回写值取 R_TC90.AAA602、AAA904 取 AAA902（原复制错误）
                        atc90p, a202, atc90p, a202,
                        atc90w, a502, atc90w, a502,
                        atc90pa, a602, atc90pa, a602,
                        str(tc90["atc90pb"]), a902, str(tc90["atc90pb"]), a902,
                        atc90av, atc90i, atc90k, atc900)
                }
                jdbcTemplate.update("UPDATE tc90_bp1 SET ace10a='1' WHERE id=?", id)
            } catch (e: RuleException) {
                fail(e, "tc90_bp1", id, nOk, msg)
            }
        }
        return mapOf("appCode" to 1, "errMsg" to null as String?)
    }

    // ---------- PRC_员工更新校验 ----------

    @Transactional
    fun checkUpdateImport(ace100: String, aae011: String): Map<String, Any?> {
        val u = user(aae011)
            ?: return mapOf("appCode" to -1, "errMsg" to "为查询到经办人信息")
        // 批级清洗（原首条 UPDATE，五列去空白/制表/换行）
        jdbcTemplate.update(
            """UPDATE tc90_bp2 SET
               aac002=replace(replace(replace(replace(replace(aac002,chr(13),''),chr(10),''),chr(9),''),'x','X'),' ',''),
               aac003=replace(replace(replace(replace(aac003,chr(13),''),chr(10),''),chr(9),''),' ',''),
               aae005=replace(replace(replace(replace(aae005,chr(13),''),chr(10),''),chr(9),''),' ',''),
               aae00b=replace(replace(replace(replace(aae00b,chr(13),''),chr(10),''),chr(9),''),' ',''),
               aab004_1=replace(replace(replace(replace(aab004_1,chr(13),''),chr(10),''),chr(9),''),' ',''),
               atc908=replace(replace(replace(replace(atc908,chr(13),''),chr(10),''),chr(9),''),' ',''),
               bbb001=replace(replace(replace(replace(bbb001,chr(13),''),chr(10),''),chr(9),''),' ','')
               WHERE ace100=?""", ace100.toLong())
        val rows = jdbcTemplate.query(
            "SELECT id FROM tc90_bp2 WHERE (ace10a IS NULL OR ace10a IN ('0','2','3','4')) AND ace100=?",
            { rs, _ -> rs.getLong("id") }, ace100.toLong())
        for (id in rows) {
            var nOk = 3
            var msg: String? = null
            val rec = loadRow("tc90_bp2", id)
            try {
                rec["ace10a"] = 3
                val found = jdbcTemplate.query(
                    "SELECT atc900 FROM tc90 WHERE atc900=(SELECT max(atc900) FROM tc90 WHERE " +
                        "(atb90r='3' OR atc90av >= ? OR aac008 LIKE '%80%' OR atc90p='0' OR atc90pa='0' OR atc90w='0') " +
                        "AND atb930=? AND " +
                        "replace(replace(replace(replace(aac003,chr(13),''),chr(10),''),chr(9),''),' ','')=? AND aac002=?)",
                    { rs, _ -> rs.getLong("atc900") },
                    java.math.BigDecimal(curYmStatic()), rec["atb930"], rec["aac003"], rec["aac002"]).firstOrNull()
                if (found == null) { nOk = 2; msg = (msg ?: "") + "根据【身份证号码+姓名】未匹配到合同信息|" }
                if (rec["bbb002"] != null) codeItem("BBB002", str(rec["bbb002"]), "发放银行", "1").let { (c, v, m) ->
                    nOk = c; msg = (msg ?: "") + (m ?: ""); rec["bbb002"] = v
                }
                if (rec["aaa605"] != null) codeItem("AAA605", str(rec["aaa605"]), "是否存在职业病危害", "1").let { (c, v, m) ->
                    nOk = c; msg = (msg ?: "") + (m ?: ""); rec["aaa605"] = v
                }
                if (rec["aae005"] != null && str(rec["aae005"])!!.length > 1 && str(rec["aae005"])!![1] == '.') {
                    nOk = 2; msg = (msg ?: "") + "【联系电话】格式不正确|"
                }
                if (rec["atb90c"] != null) codeItem("ATB90C", str(rec["atb90c"]), "是否签订退休三方协议", "1").let { (c, v, m) ->
                    nOk = c; msg = (msg ?: "") + (m ?: ""); rec["atb90c"] = v
                }
                if (rec["atb90a"] != null && num(rec["atb90a"]) == null) { nOk = 2; msg = (msg ?: "") + "【单位服务费月标准】格式不正确|" }
                if (rec["atb90b"] != null && num(rec["atb90b"]) == null) { nOk = 2; msg = (msg ?: "") + "【个人承担服务费月标准】格式不正确|" }
                if (rec["atc90zb"] != null) codeItem("ATC90ZB", str(rec["atc90zb"]), "是否参加工会", "1").let { (c, v, m) ->
                    nOk = c; msg = (msg ?: "") + (m ?: ""); rec["atc90zb"] = v
                }
                if (rec["atc90za"] != null) codeItem("ATC90ZA", str(rec["atc90za"]), "工会会费扣除", "1").let { (c, v, m) ->
                    nOk = c; msg = (msg ?: "") + (m ?: ""); rec["atc90za"] = v
                }
                if (msg != null && msg!!.isNotEmpty()) { nOk = 2; throw RuleException() }
                mark("tc90_bp2", id, nOk.toString(), msg)
            } catch (e: RuleException) {
                fail(e, "tc90_bp2", id, nOk, msg)
            }
        }
        return mapOf("appCode" to 1, "errMsg" to null as String?)
    }

    private fun curYmStatic(): String = java.time.YearMonth.now().format(DateTimeFormatter.ofPattern("yyyyMM"))

    // ---------- PRC_员工更新提交 ----------

    @Transactional
    fun commitUpdateImport(ace100: String, aae011: String): Map<String, Any?> {
        val u = user(aae011)
            ?: return mapOf("appCode" to -1, "errMsg" to "查询经办人信息出错，请将错误信息发给维护工程师")
        // 原: 提交内部先重跑校验
        checkUpdateImport(ace100, aae011)
        val rows = jdbcTemplate.query(
            "SELECT id FROM tc90_bp2 WHERE ace10a IN ('3','4') AND ace100=? ORDER BY id",
            { rs, _ -> rs.getLong("id") }, ace100.toLong())
        for (id in rows) {
            var nOk = 1
            var msg: String? = null
            val rec = loadRow("tc90_bp2", id)
            try {
                val tc90 = jdbcTemplate.query(
                    "SELECT * FROM tc90 WHERE atc900=(SELECT max(atc900) FROM tc90 WHERE " +
                        "(atb90r='3' OR atc90av >= ? OR aac008 LIKE '%80%' OR atc90p='0' OR atc90pa='0' OR atc90w='0') " +
                        "AND atb930=? AND " +
                        "replace(replace(replace(replace(aac003,chr(13),''),chr(10),''),chr(9),''),' ','')=? AND aac002=?)",
                    { rs, _ -> tc90Row(rs) },
                    java.math.BigDecimal(curYmStatic()), rec["atb930"], rec["aac003"], rec["aac002"]).firstOrNull()
                if (tc90 == null || tc90["atc900"] == null)
                    throw RuleException("根据【身份证号码+姓名】未匹配到合同信息|", 2).also { nOk = 2 }
                val atc900 = (tc90["atc900"] as Number).toLong()
                var aaa605: String? = str(tc90["aaa605"])
                if (rec["aaa605"] != null) aaa605 = codeItem("AAA605", str(rec["aaa605"]), "是否存在职业病危害", "1").second
                var atb90c: String? = str(tc90["atb90c"])
                if (rec["atb90c"] != null) atb90c = codeItem("ATB90C", str(rec["atb90c"]), "是否个人社保单位承担", "1").second
                var atb90a = tc90["atb90a"]; var atb90b = tc90["atb90b"]
                if ((num(rec["atb90a"]) ?: BigDecimal.ZERO) > BigDecimal.ZERO || (num(rec["atb90b"]) ?: BigDecimal.ZERO) > BigDecimal.ZERO) {
                    atb90a = num(rec["atb90a"]) ?: BigDecimal.ZERO; atb90b = num(rec["atb90b"]) ?: BigDecimal.ZERO
                }
                var atc90zb = str(tc90["atc90zb"])
                atc90zb = if (rec["atc90zb"] != null) codeItem("ATC90ZB", str(rec["atc90zb"]), "是否参加工会", "1").second else null
                var atc90za = str(tc90["atc90za"])
                atc90za = if (rec["atc90za"] != null) codeItem("ATC90ZA", str(rec["atc90za"]), "工会会费扣除", "1").second else null
                var bbb001 = str(tc90["bbb001"])
                if (rec["bbb001"] != null) bbb001 = str(rec["bbb001"])?.replace(" ", "")?.take(100)
                var bbb002 = str(tc90["bbb002"])
                if (rec["bbb002"] != null) bbb002 = codeItem("BBB002", str(rec["bbb002"]), "发放银行", "1").second
                for (tbl in listOf("tc90", "tc90_list")) {
                    jdbcTemplate.update(
                        "UPDATE $tbl SET aae005=?, aab004_1=?, aae00b=?, atc908=?, atc90ax=?, aaa605=?, atb90c=?, " +
                            "atb90a=?, atb90b=?, atc90zb=?, atc90za=?, bbb001=?, bbb002=? WHERE atc900=?",
                        if (rec["aae005"] != null) str(rec["aae005"])?.take(50) else tc90["aae005"],
                        if (rec["aab004_1"] != null) str(rec["aab004_1"])?.take(100) else tc90["aab004_1"],
                        if (rec["aae00b"] != null) str(rec["aae00b"])?.take(20) else tc90["aae00b"],
                        if (rec["atc908"] != null) str(rec["atc908"])?.take(100) else tc90["atc908"],
                        if (rec["atc90ax"] != null) day(rec["atc90ax"])?.let { java.sql.Date.valueOf(it) } else tc90["atc90ax"],
                        aaa605, atb90c, atb90a, atb90b, atc90zb, atc90za, bbb001, bbb002, atc900)
                }
                jdbcTemplate.update("UPDATE tc90_bp2 SET ace10a='1' WHERE id=?", id)
            } catch (e: RuleException) {
                fail(e, "tc90_bp2", id, nOk, msg)
            }
        }
        return mapOf("appCode" to 1, "errMsg" to null as String?)
    }

    private fun upd2Deprecated() {} // (已并入合并 UPDATE 双表循环)

    // ---------- PRC_员工续签校验 ----------

    @Transactional
    fun checkRenewImport(ace100: String, aae011: String): Map<String, Any?> {
        val u = user(aae011)
            ?: return mapOf("appCode" to -1, "errMsg" to "为查询到经办人信息")
        jdbcTemplate.update(
            """UPDATE tc90_bp3 SET
               aac002=replace(replace(replace(replace(replace(aac002,chr(13),''),chr(10),''),chr(9),''),'x','X'),' ',''),
               aac003=replace(replace(replace(replace(aac003,chr(13),''),chr(10),''),chr(9),''),' ',''),
               aae005=replace(replace(replace(replace(aae005,chr(13),''),chr(10),''),chr(9),''),' ','')
               WHERE ace100=?""", ace100.toLong())
        val rows = jdbcTemplate.query(
            "SELECT id FROM tc90_bp3 WHERE (ace10a IS NULL OR ace10a IN ('0','2','3','4')) AND ace100=?",
            { rs, _ -> rs.getLong("id") }, ace100.toLong())
        for (id in rows) {
            var nOk = 3
            var msg: String? = null
            val rec = loadRow("tc90_bp3", id)
            try {
                rec["ace10a"] = 3
                val found = jdbcTemplate.query(
                    "SELECT atc900 FROM tc90 WHERE aae100='1' AND atb90r='3' AND " +
                        "replace(replace(replace(replace(aac003,chr(13),''),chr(10),''),chr(9),''),' ','')=? AND aac002=?",
                    { rs, _ -> rs.getLong("atc900") }, rec["aac003"], rec["aac002"]).firstOrNull()
                if (found == null) { nOk = 2; msg = (msg ?: "") + "根据【身份证号码+姓名】未匹配到合同信息|" }
                if (rec["aae005"] != null && str(rec["aae005"])!!.length > 1 && str(rec["aae005"])!![1] == '.') {
                    nOk = 2; msg = (msg ?: "") + "【联系电话】格式不正确|"
                }
                if (rec["atc90a"] != null) {
                    val s = str(rec["atc90a"])!!
                    val p = s.indexOf('-')
                    rec["atc90a"] = if (p > 0) s.substring(0, p) else null
                    if (rec["atc90a"] == null || aa10Name("ATC90A", rec["atc90a"] as String) == null) {
                        nOk = 2; msg = (msg ?: "") + "【合同类别】不正确|"
                    }
                } else rec["atc90a"] = "1"
                if (rec["atc90c"] != null) {
                    if (day(rec["atc90c"]) == null) { nOk = 2; msg = (msg ?: "") + "【合同开始日期】格式不正确|" }
                } else { nOk = 2; msg = (msg ?: "") + "【合同开始日期】不能为空|" }
                if (str(rec["atc90a"]) in setOf("1", "3")) {
                    if (rec["atc90d"] != null) {
                        if (day(rec["atc90d"]) == null) { nOk = 2; msg = (msg ?: "") + "【合同终止日期】格式不正确|" }
                    } else { nOk = 2; msg = (msg ?: "") + "当【合同类型】选择【1-固定期限】和【3-以完成一定工作任务为期限】时，【合同终止日期】不能为空|" }
                    val d = day(rec["atc90d"]); val c = day(rec["atc90c"])
                    if (d == null || c == null) { nOk = 2 }
                    else if (d.isBefore(c)) { nOk = 2; msg = (msg ?: "") + "【合同终止日期】小于【合同开始日期】|" }
                }
                if (msg != null && msg!!.isNotEmpty()) { nOk = 2; throw RuleException() }
                mark("tc90_bp3", id, nOk.toString(), msg)
            } catch (e: RuleException) {
                fail(e, "tc90_bp3", id, nOk, msg)
            }
        }
        return mapOf("appCode" to 1, "errMsg" to null as String?)
    }

    // ---------- PRC_员工续签提交 ----------

    @Transactional
    fun commitRenewImport(ace100: String, aae011: String): Map<String, Any?> {
        val u = user(aae011)
            ?: return mapOf("appCode" to -1, "errMsg" to "查询经办人信息出错，请将错误信息发给维护工程师")
        checkRenewImport(ace100, aae011)
        val rows = jdbcTemplate.query(
            "SELECT id FROM tc90_bp3 WHERE ace10a IN ('3','4') AND ace100=? ORDER BY id",
            { rs, _ -> rs.getLong("id") }, ace100.toLong())
        for (id in rows) {
            var nOk = 1
            var msg: String? = null
            val rec = loadRow("tc90_bp3", id)
            try {
                val tc90 = jdbcTemplate.query(
                    "SELECT * FROM tc90 WHERE aae100='1' AND atb90r='3' AND " +
                        "replace(replace(replace(replace(aac003,chr(13),''),chr(10),''),chr(9),''),' ','')=? AND aac002=?",
                    { rs, _ -> tc90Row(rs) }, rec["aac003"], rec["aac002"]).firstOrNull()
                if (tc90 == null || tc90["atc900"] == null)
                    throw RuleException("根据【身份证号码+姓名】未匹配到合同信息|", 2).also { nOk = 2 }
                val atc900 = (tc90["atc900"] as Number).toLong()
                // 签订历史快照
                val qdls = jdbcTemplate.queryForObject(
                    "SELECT nextval('seq_sys_baz001'), nextval('seq_sys_baz002'), nextval('seq_tc90_qdls_id')",
                    { rs, _ -> arrayOf(rs.getLong(1), rs.getLong(2), rs.getLong(3)) })!!
                try {
                    jdbcTemplate.update(
                        "INSERT INTO tc90_qdls (baz001, baz002, id, atc900, atc90f, atc90a, atc90c, atc90d, " +
                            "aae011, aae019, aae017, aae036, aae020, aae022) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?)",
                        qdls[0], qdls[1], qdls[2], atc900, tc90["atc90f"], tc90["atc90a"], tc90["atc90c"], tc90["atc90d"],
                        tc90["aae011"], tc90["aae019"], tc90["aae017"], tc90["aae036"], tc90["aae020"], tc90["aae022"])
                } catch (e: Exception) {
                    return mapOf("appCode" to -1, "errMsg" to "记录合同签订历史出错")
                }
                val aab004_1 = if (rec["aab004_1"] != null) str(rec["aab004_1"])?.take(100) else tc90["aab004_1"]
                val aae00b = if (rec["aae00b"] != null) str(rec["aae00b"])?.take(20) else tc90["aae00b"]
                val atc908 = if (rec["atc908"] != null) str(rec["atc908"])?.take(100) else tc90["atc908"]
                // 忠实: 无'-'时 INSTR=0 → SUBSTR(x,1,0)=NULL
                val aRaw = str(rec["atc90a"]) ?: ""
                val p = aRaw.indexOf('-')
                val atc90a = if (p > 0) aRaw.substring(0, p) else null
                val atc90c = day(rec["atc90c"])
                val atc90d = if (rec["atc90d"] != null) day(rec["atc90d"]) else null
                for (tbl in listOf("tc90_list", "tc90")) {
                    jdbcTemplate.update(
                        "UPDATE $tbl SET atc90f='2', aab004_1=?, aae00b=?, atc908=?, atc90a=?, atc90c=?, atc90d=? WHERE atc900=?",
                        aab004_1, aae00b, atc908, atc90a,
                        atc90c?.let { java.sql.Date.valueOf(it) }, atc90d?.let { java.sql.Date.valueOf(it) }, atc900)
                }
                jdbcTemplate.update("UPDATE tc90_bp3 SET ace10a='1' WHERE id=?", id)
            } catch (e: RuleException) {
                fail(e, "tc90_bp3", id, nOk, msg)
            }
        }
        return mapOf("appCode" to 1, "errMsg" to null as String?)
    }

    // ---------- PRC_新增保险校验 ----------

    @Transactional
    fun checkNewInsuranceImport(ace100: String, aae011: String): Map<String, Any?> {
        val u = user(aae011)
            ?: return mapOf("appCode" to -1, "errMsg" to "为查询到经办人信息")
        jdbcTemplate.update(
            """UPDATE tc90_bp4 SET
               aac002=replace(replace(replace(replace(replace(aac002,chr(13),''),chr(10),''),chr(9),''),'x','X'),' ',''),
               aac003=replace(replace(replace(replace(aac003,chr(13),''),chr(10),''),chr(9),''),' ',''),
               aaa100=replace(replace(replace(replace(aaa100,chr(13),''),chr(10),''),chr(9),''),' ',''),
               aaa201=replace(replace(replace(replace(aaa201,chr(13),''),chr(10),''),chr(9),''),' ',''),
               aaa301=replace(replace(replace(replace(aaa301,chr(13),''),chr(10),''),chr(9),''),' ',''),
               aaa110=replace(replace(replace(replace(aaa110,chr(13),''),chr(10),''),chr(9),''),' ',''),
               aaa601=replace(replace(replace(replace(aaa601,chr(13),''),chr(10),''),chr(9),''),' ',''),
               aaa302=replace(replace(replace(replace(aaa302,chr(13),''),chr(10),''),chr(9),''),' ',''),
               aaa120=replace(replace(replace(replace(aaa120,chr(13),''),chr(10),''),chr(9),''),' ',''),
               atb90s=replace(replace(replace(replace(atb90s,chr(13),''),chr(10),''),chr(9),''),' ',''),
               aaa501=replace(replace(replace(replace(aaa501,chr(13),''),chr(10),''),chr(9),''),' ',''),
               aaa303=replace(replace(replace(replace(aaa303,chr(13),''),chr(10),''),chr(9),''),' ',''),
               atc90c=replace(replace(replace(replace(atc90c,chr(13),''),chr(10),''),chr(9),''),' ','')
               WHERE ace100=?""", ace100.toLong())
        val rows = jdbcTemplate.query(
            "SELECT id FROM tc90_bp4 WHERE (ace10a IS NULL OR ace10a IN ('0','2','3')) AND ace100=?",
            { rs, _ -> rs.getLong("id") }, ace100.toLong())
        for (id in rows) {
            var nOk = 3
            var msg: String? = null
            val rec = loadRow("tc90_bp4", id)
            try {
                rec["ace10a"] = 3
                val tc90 = jdbcTemplate.query(
                    "SELECT * FROM tc90 WHERE aae00p IN ('1','5') AND atb90r='3' AND atb930=? AND " +
                        "replace(replace(replace(replace(aac003,chr(13),''),chr(10),''),chr(9),''),' ','')=? AND aac002=?",
                    { rs, _ -> tc90Row(rs) }, rec["atb930"], rec["aac003"], rec["aac002"]).firstOrNull()
                if (tc90 == null || tc90["atc900"] == null)
                    throw RuleException("根据【身份证号码+姓名】未匹配到合同信息|", 2).also { nOk = 2 }
                if (str(tc90["aae00p"]) !in setOf("1", "5"))
                    throw RuleException("员工非派遣或保险代理人员，无法新增保险|", 2).also { nOk = 2 }
                if (rec["atc90c"] == null) throw RuleException("【合同开始时间】不能为空|", 2).also { nOk = 2 }
                else if (day(rec["atc90c"]) == null) throw RuleException("【合同开始时间】格式不正确|", 2).also { nOk = 2 }
                // 新增社保
                if ((str(tc90["aaa401"]) == "0" || tc90["aaa401"] == null) &&
                    (rec["aaa100"] != null || rec["aaa201"] != null || rec["aaa301"] != null)) {
                    if (rec["aaa100"] != null) {
                        if (aa12(rec["aaa100"], "00000000") == null) { nOk = 2; msg = (msg ?: "") + "【社保参保属地】格式不正确|" }
                    } else { nOk = 2; msg = (msg ?: "") + "【社保参保属地】不能为空|" }
                    if (rec["aaa201"] != null) {
                        if (day(str(rec["aaa201"]) + "01") == null) { nOk = 2; msg = (msg ?: "") + "【社保参保年月】格式不正确|" }
                    } else { nOk = 2; msg = (msg ?: "") + "【社保参保年月】不能为空|" }
                    if (rec["aaa301"] != null) {
                        if (num(rec["aaa301"]) == null) { nOk = 2; msg = (msg ?: "") + "【社保缴费基数】格式不正确|" }
                    } else { nOk = 2; msg = (msg ?: "") + "【社保缴费基数】不能为空|" }
                }
                // 新增医保
                if ((str(tc90["aaa402"]) == "0" || tc90["aaa402"] == null) &&
                    (rec["aaa110"] != null || rec["aaa601"] != null || rec["aaa302"] != null)) {
                    if (rec["aaa110"] != null) {
                        if (aa12(rec["aaa110"], "00000000") == null) { nOk = 2; msg = (msg ?: "") + "【医保参保属地】格式不正确|" }
                    } else { nOk = 2; msg = (msg ?: "") + "【医保参保属地】不能为空|" }
                    if (rec["aaa601"] != null) {
                        if (day(str(rec["aaa601"]) + "01") == null) { nOk = 2; msg = (msg ?: "") + "【医保参保年月】格式不正确|" }
                    } else { nOk = 2; msg = (msg ?: "") + "【医保参保年月】不能为空|" }
                    if (rec["aaa302"] != null) {
                        if (num(rec["aaa302"]) == null) { nOk = 2; msg = (msg ?: "") + "【医保缴费基数】格式不正确|" }
                    } else { nOk = 2; msg = (msg ?: "") + "【医保缴费基数】不能为空|" }
                }
                // 新增公积金
                if ((str(tc90["aaa407"]) == "0" || tc90["aaa407"] == null) &&
                    (rec["aaa120"] != null || rec["aaa501"] != null || rec["aaa303"] != null || rec["atb90s"] != null)) {
                    if (rec["aaa120"] != null) {
                        if (aa12(rec["aaa120"], "00000000") == null) { nOk = 2; msg = (msg ?: "") + "【公积金参保属地】格式不正确|" }
                    } else { nOk = 2; msg = (msg ?: "") + "【公积金参保属地】不能为空|" }
                    if (rec["aaa501"] != null) {
                        if (day(str(rec["aaa501"]) + "01") == null) { nOk = 2; msg = (msg ?: "") + "【公积金参保年月】格式不正确|" }
                    } else { nOk = 2; msg = (msg ?: "") + "【公积金参保年月】不能为空|" }
                    if (rec["aaa303"] != null) {
                        if (num(rec["aaa303"]) == null) { nOk = 2; msg = (msg ?: "") + "【公积金缴费基数】格式不正确|" }
                    } else { nOk = 2; msg = (msg ?: "") + "【公积金缴费基数】不能为空|" }
                    codeItem("ATB90S", str(rec["atb90s"]), "公积金比例", "1").let { (c, v, m) ->
                        nOk = c; msg = (msg ?: "") + (m ?: ""); rec["atb90s"] = v
                    }
                }
                if (msg != null && msg!!.isNotEmpty()) { nOk = 2; throw RuleException() }
                mark("tc90_bp4", id, nOk.toString(), msg)
            } catch (e: RuleException) {
                fail(e, "tc90_bp4", id, nOk, msg)
            }
        }
        return mapOf("appCode" to 1, "errMsg" to null as String?)
    }

    // ---------- PRC_新增保险提交 ----------

    @Transactional
    fun commitNewInsuranceImport(ace100: String, aae011: String): Map<String, Any?> {
        val u = user(aae011)
            ?: return mapOf("appCode" to -1, "errMsg" to "查询经办人信息出错，请将错误信息发给维护工程师")
        checkNewInsuranceImport(ace100, aae011)
        val curYm = curYmStatic()
        val rows = jdbcTemplate.query(
            "SELECT id FROM tc90_bp4 WHERE ace10a='3' AND ace100=? ORDER BY id",
            { rs, _ -> rs.getLong("id") }, ace100.toLong())
        for (id in rows) {
            var nOk = 1
            var msg: String? = null
            val rec = loadRow("tc90_bp4", id)
            try {
                val tc90 = jdbcTemplate.query(
                    "SELECT * FROM tc90 WHERE aae00p IN ('1','5') AND atb90r='3' AND atb930=? AND " +
                        "replace(replace(replace(replace(aac003,chr(13),''),chr(10),''),chr(9),''),' ','')=? AND aac002=?",
                    { rs, _ -> tc90Row(rs) }, rec["atb930"], rec["aac003"], rec["aac002"]).firstOrNull()
                if (tc90 == null || tc90["atc900"] == null)
                    throw RuleException("根据【身份证号码+姓名】未匹配到合同信息|", 2).also { nOk = 2 }
                val atc900 = (tc90["atc900"] as Number).toLong()
                val ids = jdbcTemplate.queryForObject(
                    "SELECT nextval('seq_tc92_atc920'), nextval('seq_tb91_atb910'), nextval('seq_sys_baz001'), nextval('seq_sys_baz002')",
                    { rs, _ -> arrayOf(rs.getLong(1), rs.getLong(2), rs.getLong(3), rs.getLong(4)) })!!
                val atc920 = ids[0]; val atb910 = ids[1]; val baz001 = ids[2]; val baz002 = ids[3]

                // TC92 变更快照: 以合同当前值做 前值/后值 双份, 随后各分支覆盖后值
                val before = tc90Snapshot(tc90)
                val after = tc90Snapshot(tc90)
                val atc9wc = { code: String ->
                    val cur = str(tc90["atc9wc"])
                    if (cur == null) code else if (!cur.contains(code)) "$cur,$code" else cur
                }
                // 新增社保（内存变更, 随 TC92 快照走审核; 落库仅 ATC9WC）
                if ((str(tc90["aaa401"]) == "0" || tc90["aaa401"] == null) &&
                    (rec["aaa100"] != null || rec["aaa201"] != null || rec["aaa301"] != null)) {
                    val d = aa12(rec["aaa100"], "00000000")
                        ?: throw RuleException("【社保参保属地】格式不正确|", 2).also { nOk = 2 }
                    tc90["aaa100"] = d.first; tc90["aaa101"] = d.second
                    tc90["atc90g"] = "1"; tc90["aaa401"] = "1"
                    tc90["aaa201"] = num(rec["aaa201"]); tc90["aaa203"] = tc90["aaa201"]
                    tc90["aaa301"] = clamp("1", curYm, str(tc90["aaa100"]), num(rec["aaa301"]))
                    tc90["aaa304"] = tc90["aaa301"]
                    tc90["atc9wc"] = atc9wc("21")
                    after["atc90g"] = "1"; after["aaa401"] = "1"; after["atc90p"] = null
                    after["atc905"] = num(rec["aaa301"]) // 忠实: 养老基数写"转正劳动报酬(变更后)"错位映射
                    after["aaa201"] = tc90["aaa201"]; after["aaa203"] = tc90["aaa201"]
                    after["aaa100"] = tc90["aaa100"]; after["aaa101"] = tc90["aaa101"]
                    after["aaa301"] = tc90["aaa301"]; after["aaa304"] = tc90["aaa301"]
                }
                // 新增医保
                if ((str(tc90["aaa402"]) == "0" || tc90["aaa402"] == null) &&
                    (rec["aaa110"] != null || rec["aaa601"] != null || rec["aaa302"] != null)) {
                    val d = aa12(rec["aaa110"], "00000000")
                        ?: throw RuleException("【医保参保属地】格式不正确|", 2).also { nOk = 2 }
                    tc90["aaa110"] = d.first; tc90["aaa111"] = d.second
                    tc90["atc90g"] = "1"; tc90["aaa402"] = "1"
                    tc90["aaa601"] = num(rec["aaa601"]); tc90["aaa603"] = tc90["aaa601"]
                    tc90["aaa302"] = clamp("2", curYm, str(tc90["aaa110"]), num(rec["aaa302"]))
                    tc90["aaa305"] = tc90["aaa302"]; tc90["aaa306"] = tc90["aaa302"]
                    tc90["atc9wc"] = atc9wc("22")
                    after["atc90g"] = "1"; after["aaa402"] = "1"; after["atc90pa"] = null
                    after["aaa601"] = tc90["aaa601"]; after["aaa603"] = tc90["aaa601"]
                    after["aaa110"] = tc90["aaa110"]; after["aaa111"] = tc90["aaa111"]
                    after["aaa302"] = tc90["aaa302"]; after["aaa306"] = tc90["aaa302"]
                    after["aaa404"] = "1"; after["atc90pb"] = null
                    after["aaa901"] = tc90["aaa601"]; after["aaa903"] = tc90["aaa601"]
                    after["aaa130"] = tc90["aaa110"]; after["aaa131"] = tc90["aaa111"]
                    after["aaa305"] = tc90["aaa302"]
                }
                // 新增公积金
                if ((str(tc90["aaa407"]) == "0" || tc90["aaa407"] == null) &&
                    (rec["aaa120"] != null || rec["aaa501"] != null || rec["aaa303"] != null || rec["atb90s"] != null)) {
                    val d = aa12(rec["aaa120"], "00000000")
                        ?: throw RuleException("【公积金参保属地】格式不正确|", 2).also { nOk = 2 }
                    tc90["aaa120"] = d.first; tc90["aaa121"] = d.second
                    tc90["atc90g"] = "1"; tc90["aaa407"] = "1"
                    tc90["aaa501"] = num(rec["aaa501"]); tc90["aaa503"] = tc90["aaa501"]
                    tc90["aaa303"] = clamp("3", curYm, str(tc90["aaa120"]), num(rec["aaa303"]))
                    val atb90s = codeItem("ATB90S", str(rec["atb90s"]), "公积金比例", "1")
                    if (atb90s.first != 3) {
                        nOk = atb90s.first; msg = (msg ?: "") + (atb90s.third ?: ""); throw RuleException()
                    }
                    nOk = 1
                    tc90["atb90s"] = atb90s.second
                    tc90["atc9wc"] = atc9wc("23")
                    after["atc90g"] = "1"; after["aaa407"] = "1"; after["atc90w"] = null
                    after["aaa501"] = tc90["aaa501"]; after["aaa503"] = tc90["aaa501"]
                    after["atb90s"] = tc90["atb90s"]
                    after["aaa120"] = tc90["aaa120"]; after["aaa121"] = tc90["aaa121"]
                    after["aaa303"] = tc90["aaa303"]
                }
                // 签订历史快照（仅部分字段, 无 atc90c 之外取原值）
                val qdls = jdbcTemplate.queryForObject(
                    "SELECT nextval('seq_sys_baz001'), nextval('seq_sys_baz002'), nextval('seq_tc90_qdls_id')",
                    { rs, _ -> arrayOf(rs.getLong(1), rs.getLong(2), rs.getLong(3)) })!!
                try {
                    jdbcTemplate.update(
                        "INSERT INTO tc90_qdls (baz001, baz002, id, atc900, atc90f, atc90a, atc90c, atc90d, " +
                            "aae011, aae019, aae017, aae036, aae020, aae022) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?)",
                        qdls[0], qdls[1], qdls[2], atc900, tc90["atc90f"], tc90["atc90a"],
                        tc90["atc90c"], tc90["atc90d"], tc90["aae011"], tc90["aae019"], tc90["aae017"],
                        tc90["aae036"], tc90["aae020"], tc90["aae022"])
                } catch (e: Exception) {
                    return mapOf("appCode" to -1, "errMsg" to "记录合同签订历史出错")
                }
                jdbcTemplate.update("UPDATE tc90 SET atc9wc=? WHERE atc900=?", tc90["atc9wc"], atc900)
                try {
                    jdbcTemplate.update(
                        "INSERT INTO tb91 (atb910, baz001, baz002, atb91z, atb911, atb912, atb913, aae011, aae017, " +
                            "aae036, aae019, aae020, aae022, bae011, bae017, bae036, bae019, bae020, bae022, aae013, atb900, aab00e, aab004, atb931) " +
                            "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)",
                        atb910, baz001, baz002, atc920, "21", "2",
                        "${tc90["aac003"]}于${LocalDate.now()}信息变更，请审核",
                        str(u["aae011"]), str(u["aae017"]), java.sql.Timestamp(System.currentTimeMillis()),
                        str(u["aae019"]), str(u["aae020"]), str(u["aae022"]),
                        null, null, null, null, null, null, null,
                        str(atc900), null, tc90["aab004"], tc90["atc90x"])
                } catch (e: Exception) {
                    return mapOf("appCode" to -1, "errMsg" to "记录审核信息出错")
                }
                try {
                    insertTc92(atc920, baz001, baz002, atb910, atc900, u, before, after, tc90)
                } catch (e: Exception) {
                    return mapOf("appCode" to -1, "errMsg" to "记录变更信息出错")
                }
                // 原成功路径: UPDATE TC90_BP4 SET ace10a = N_OK (=1)
                mark("tc90_bp4", id, "1", null)
            } catch (e: RuleException) {
                fail(e, "tc90_bp4", id, nOk, msg)
            }
        }
        return mapOf("appCode" to 1, "errMsg" to null as String?)
    }

    /** TC92 快照取值器: 变更前/后共用的字段投影(全部字段以 before 值铺底, 分支覆写 after) */
    private fun tc90Snapshot(m: Map<String, Any?>): MutableMap<String, Any?> {
        val keys = listOf("aac009", "atc909", "atc90a", "atc90b", "atc90c", "atc90d", "atc90g", "atc90h",
            "aaa001", "aaa002", "aaa003", "aaa004", "aaa100", "aaa101", "aaa301", "aaa302", "aaa303",
            "aaa406", "bbb001", "bbb002", "atc90r", "atc90u", "aae005", "atc902", "aaa407", "aac002", "aac003",
            "aac004", "aac005", "aac006", "aaa304", "aaa305", "aaa306", "atb90s", "atc90aa", "atc90ab", "atc90ac",
            "atc90ad", "atc90ae", "atc908", "atc907", "atb90ab", "atc905", "atc90ah", "atc90aj", "atc90ak",
            "atc90al", "atc90am", "atc90an", "atb90ad", "aaa401", "aaa402", "aaa403", "aaa404", "aaa405",
            "atc904", "aab001_1", "id_1", "aab004_1", "aae013", "aaa110", "aaa111", "aaa120", "aaa121",
            "aac012", "aaa201", "aaa501", "aaa601", "atb90a", "atb90b", "aae006", "atb90c", "aae00p",
            "aaa803", "aae00b", "aaa005", "atc90av", "aaa202", "aaa502", "aaa602", "aaa130", "aaa131",
            "aaa901", "aaa902", "atc90p", "aaa203", "aaa208", "atc90pa", "aaa603", "aaa608", "atc90w",
            "aaa503", "aaa508", "atc90pb", "aaa903", "aaa908")
        return mutableMapOf<String, Any?>(*(keys.map { it to m[it] }).toTypedArray())
    }

    /** TC92 全字段 INSERT（前值列 + '_' 后值列） */
    private fun insertTc92(
        atc920: Long, baz001: Long, baz002: Long, atb910: Long, atc900: Long,
        u: Map<String, Any?>, before: Map<String, Any?>, after: Map<String, Any?>, tc90: Map<String, Any?>,
    ) {
        val cols = mutableListOf<Pair<String, Any?>>()
        fun put(c: String, v: Any?) = cols.add(c to v)
        put("atc920", atc920); put("baz001", baz001); put("baz002", baz002)
        put("aae011", str(u["aae011"])); put("aae017", str(u["aae017"]))
        put("aae036", java.sql.Timestamp(System.currentTimeMillis()))
        put("aae019", str(u["aae019"])); put("aae020", str(u["aae020"])); put("aae022", str(u["aae022"]))
        put("atb911", "21"); put("atc900", atc900); put("atc921", java.sql.Timestamp(System.currentTimeMillis()))
        put("atb910", atb910); put("aac001", tc90["aac001"])
        for (k in before.keys) {
            put(k, before[k])
            put("${k}_", if (k == "atc90g") (after[k] ?: "1") else after[k])
        }
        // 忠实: id_1_ 与 aab004_1_ 取 tc90.id_1 / aab004_1(原赋值即同源)
        val names = StringBuilder(); val ph = StringBuilder(); val args = mutableListOf<Any?>()
        for ((i, c) in cols.withIndex()) {
            if (i > 0) { names.append(','); ph.append(',') }
            names.append(c.first); ph.append('?'); args.add(c.second)
        }
        jdbcTemplate.update("INSERT INTO tc92 ($names) VALUES ($ph)", *args.toTypedArray())
    }

    // ---------- PKG_IMP.PRC_AC01_GX_IMP（tdbp_cont Tab2 信息更新导入提交） ----------

    @Transactional
    fun commitAc01GxImport(ace100: String): Map<String, Any?> {
        val rows = jdbcTemplate.query(
            "SELECT * FROM ac01_gx WHERE ace100=?", { rs, _ -> tc90Row(rs) }, ace100.toLong())
        for (rec in rows) {
            val id = (rec["id"] as Number).toLong()
            val parsed = hzCases.parseAac002(str(rec["aac002"]) ?: "")
            if ((parsed[0] as Int) == 1) {
                val aac002_18 = parsed[3] as String?
                val ac01 = jdbcTemplate.query(
                    "SELECT aac001 FROM ac01 WHERE aac002=? AND aac003=?",
                    { rs, _ -> rs.getLong("aac001") }, aac002_18, rec["aac003"]).firstOrNull()
                if (ac01 != null) {
                    val tc90 = jdbcTemplate.query(
                        "SELECT atc900 FROM tc90 WHERE aae100='1' AND atb90r='3' AND aac001=?",
                        { rs, _ -> rs.getLong("atc900") }, ac01).firstOrNull()
                    if (tc90 != null) {
                        try {
                            val atc90aq = num(rec["atc90aq"]) ?: throw IllegalStateException()
                            jdbcTemplate.update(
                                "UPDATE tc90 SET atc90aq=?, aae00a=COALESCE(nullif(?, ''), substring(aae00a,1,30)) WHERE atc900=?",
                                atc90aq, str(rec["aae00a"])?.take(30), tc90)
                            updAc01(rec, ac01)
                            jdbcTemplate.update(
                                "UPDATE ac01_gx SET ace10a='1', aac001=?, atc900=? WHERE id=?", ac01, tc90, id)
                        } catch (e: Exception) {
                            jdbcTemplate.update(
                                "UPDATE ac01_gx SET atc900=?, aac001=?, ace10a='1', " +
                                    "ace10b='存在有效合同信息,但【年度包干经费】格式不正确,未更新【年度包干经费】' WHERE id=?",
                                tc90, ac01, id)
                            updAc01(rec, ac01)
                        }
                    } else {
                        jdbcTemplate.update(
                            "UPDATE ac01_gx SET aac001=?, ace10a='1', " +
                                "ace10b='存在基本信息,不存在有效合同信息,未更新【年度包干经费】' WHERE id=?", ac01, id)
                        updAc01(rec, ac01)
                    }
                } else {
                    jdbcTemplate.update(
                        "UPDATE ac01_gx SET ace10a='0', ace10b='不存在个人基本信息' WHERE id=?", id)
                }
            } else {
                jdbcTemplate.update(
                    "UPDATE ac01_gx SET ace10a='0', ace10b='身份证号码错误' WHERE id=?", id)
            }
        }
        return mapOf("appCode" to 1, "errMsg" to null as String?)
    }

    /** AC01 八列信息更新（DECODE(x,NULL,旧值,前30位) 语义） */
    private fun updAc01(rec: Map<String, Any?>, aac001: Long) {
        val aae00a = str(rec["aae00a"])?.takeIf { it.isNotEmpty() }?.take(30)
        val acc0m1 = str(rec["acc0m1"])?.takeIf { it.isNotEmpty() }?.take(30)
        val aae00n = str(rec["aae00n"])?.takeIf { it.isNotEmpty() }?.take(30)
        jdbcTemplate.update(
            "UPDATE ac01 SET acc030=?, acc0m1=COALESCE(?, acc0m1), aae00n=COALESCE(?, aae00n), aae007=?, " +
                "aae00d=?, aae00a=COALESCE(?, aae00a), aae00h=?, aae00i=? WHERE aac001=?",
            rec["acc030"], acc0m1, aae00n, rec["aae007"], rec["aae00d"], aae00a, rec["aae00h"], rec["aae00i"], aac001)
    }
}

/** ---------- /proc 注册面（PKG_派遣批量导入.* 十一个 + PKG_IMP.PRC_AC01_GX_IMP） ---------- */

@Component
class TdbpCheckEmployeeProc(private val c: TdbpImportCases) : ProcHandler {
    override fun procName() = "PKG_派遣批量导入.PRC_员工导入校验"
    override fun handle(params: Map<String, Any?>): Map<String, Any?> {
        val ace100 = params["ace100"]?.toString() ?: return mapOf("appCode" to -1, "errMsg" to "缺少 ace100")
        val aae011 = params["aae011"]?.toString() ?: return mapOf("appCode" to -1, "errMsg" to "缺少 aae011")
        return c.checkEmployeeImport(ace100, aae011)
    }
}

@Component
class TdbpCommitEmployeeProc(private val c: TdbpImportCases) : ProcHandler {
    override fun procName() = "PKG_派遣批量导入.PRC_员工导入提交"
    override fun handle(params: Map<String, Any?>): Map<String, Any?> {
        val ace100 = params["ace100"]?.toString() ?: return mapOf("appCode" to -1, "errMsg" to "缺少 ace100")
        val aae011 = params["aae011"]?.toString() ?: return mapOf("appCode" to -1, "errMsg" to "缺少 aae011")
        return c.commitEmployeeImport(ace100, aae011)
    }
}

@Component
class TdbpCheckTerminateProc(private val c: TdbpImportCases) : ProcHandler {
    override fun procName() = "PKG_派遣批量导入.PRC_员工终止导入校验"
    override fun handle(params: Map<String, Any?>): Map<String, Any?> {
        val ace100 = params["ace100"]?.toString() ?: return mapOf("appCode" to -1, "errMsg" to "缺少 ace100")
        val aae011 = params["aae011"]?.toString() ?: return mapOf("appCode" to -1, "errMsg" to "缺少 aae011")
        return c.checkTerminateImport(ace100, aae011)
    }
}

@Component
class TdbpCommitTerminateProc(private val c: TdbpImportCases) : ProcHandler {
    override fun procName() = "PKG_派遣批量导入.PRC_员工终止导入提交"
    override fun handle(params: Map<String, Any?>): Map<String, Any?> {
        val ace100 = params["ace100"]?.toString() ?: return mapOf("appCode" to -1, "errMsg" to "缺少 ace100")
        val aae011 = params["aae011"]?.toString() ?: return mapOf("appCode" to -1, "errMsg" to "缺少 aae011")
        return c.commitTerminateImport(ace100, aae011)
    }
}

@Component
class TdbpCheckUpdateProc(private val c: TdbpImportCases) : ProcHandler {
    override fun procName() = "PKG_派遣批量导入.PRC_员工更新校验"
    override fun handle(params: Map<String, Any?>): Map<String, Any?> {
        val ace100 = params["ace100"]?.toString() ?: return mapOf("appCode" to -1, "errMsg" to "缺少 ace100")
        val aae011 = params["aae011"]?.toString() ?: return mapOf("appCode" to -1, "errMsg" to "缺少 aae011")
        return c.checkUpdateImport(ace100, aae011)
    }
}

@Component
class TdbpCommitUpdateProc(private val c: TdbpImportCases) : ProcHandler {
    override fun procName() = "PKG_派遣批量导入.PRC_员工更新提交"
    override fun handle(params: Map<String, Any?>): Map<String, Any?> {
        val ace100 = params["ace100"]?.toString() ?: return mapOf("appCode" to -1, "errMsg" to "缺少 ace100")
        val aae011 = params["aae011"]?.toString() ?: return mapOf("appCode" to -1, "errMsg" to "缺少 aaee011".replace("ee", "e"))
        return c.commitUpdateImport(ace100, aae011)
    }
}

@Component
class TdbpCheckRenewProc(private val c: TdbpImportCases) : ProcHandler {
    override fun procName() = "PKG_派遣批量导入.PRC_员工续签校验"
    override fun handle(params: Map<String, Any?>): Map<String, Any?> {
        val ace100 = params["ace100"]?.toString() ?: return mapOf("appCode" to -1, "errMsg" to "缺少 ace100")
        val aae011 = params["aae011"]?.toString() ?: return mapOf("appCode" to -1, "errMsg" to "缺少 aae011")
        return c.checkRenewImport(ace100, aae011)
    }
}

@Component
class TdbpCommitRenewProc(private val c: TdbpImportCases) : ProcHandler {
    override fun procName() = "PKG_派遣批量导入.PRC_员工续签提交"
    override fun handle(params: Map<String, Any?>): Map<String, Any?> {
        val ace100 = params["ace100"]?.toString() ?: return mapOf("appCode" to -1, "errMsg" to "缺少 ace100")
        val aae011 = params["aae011"]?.toString() ?: return mapOf("appCode" to -1, "errMsg" to "缺少 aae011")
        return c.commitRenewImport(ace100, aae011)
    }
}

@Component
class TdbpCheckNewInsuranceProc(private val c: TdbpImportCases) : ProcHandler {
    override fun procName() = "PKG_派遣批量导入.PRC_新增保险校验"
    override fun handle(params: Map<String, Any?>): Map<String, Any?> {
        val ace100 = params["ace100"]?.toString() ?: return mapOf("appCode" to -1, "errMsg" to "缺少 ace100")
        val aae011 = params["aae011"]?.toString() ?: return mapOf("appCode" to -1, "errMsg" to "缺少 aae011")
        return c.checkNewInsuranceImport(ace100, aae011)
    }
}

@Component
class TdbpCommitNewInsuranceProc(private val c: TdbpImportCases) : ProcHandler {
    override fun procName() = "PKG_派遣批量导入.PRC_新增保险提交"
    override fun handle(params: Map<String, Any?>): Map<String, Any?> {
        val ace100 = params["ace100"]?.toString() ?: return mapOf("appCode" to -1, "errMsg" to "缺少 ace100")
        val aae011 = params["aae011"]?.toString() ?: return mapOf("appCode" to -1, "errMsg" to "缺少 aae011")
        return c.commitNewInsuranceImport(ace100, aae011)
    }
}

/** td/b/b 终止弹窗取号（OUT 首参 → "para" 兼容旧前端 value["para"]） */
@Component
class TdbpSequenceProc(private val c: TdbpImportCases) : ProcHandler {
    override fun procName() = "PKG_派遣批量导入.PRC_序列"
    override fun handle(params: Map<String, Any?>): Map<String, Any?> {
        val aaa030 = params["aaa030"]?.toString() ?: return mapOf("appCode" to -1, "errMsg" to "缺少 aaa030")
        val aae002 = params["aae002"]?.toString() ?: return mapOf("appCode" to -1, "errMsg" to "缺少 aae002")
        val aaa110 = params["aaa110"]?.toString() ?: ""
        val lx = params["lx"]?.toString() ?: "A"
        val s = c.序列(aaa030, aae002, aaa110, lx)
        return if (s == null) mapOf("appCode" to 1, "para" to null, "seq" to null)
        else mapOf("appCode" to 1, "para" to s, "seq" to s)
    }
}

@Component
class TdbpAc01GxProc(private val c: TdbpImportCases) : ProcHandler {
    override fun procName() = "PKG_IMP.PRC_AC01_GX_IMP"
    override fun handle(params: Map<String, Any?>): Map<String, Any?> {
        val ace100 = params["ace100"]?.toString() ?: return mapOf("appCode" to -1, "errMsg" to "缺少 ace100")
        return c.commitAc01GxImport(ace100)
    }
}
