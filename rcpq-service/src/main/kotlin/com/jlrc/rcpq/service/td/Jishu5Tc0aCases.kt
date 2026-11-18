package com.jlrc.rcpq.service.td

import com.jlrc.rcpq.service.proc.ProcHandler
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter

/**
 * W5c 公积金基数上下限五过程（旧 PKG_缴费基数 L1756-2577, 2026-11-18, tdcc(20)/tdcd(21) 页）。
 * 独立新文件(W4b 教训)。TC0A/TC0A_BP/TC0A_BP01 全不在同步集合可写; TC90 在同步集合——
 * 审核落库对存量行=「退役后启用」语义(同 W5a 确认)。
 *
 * ① 模板(PRC_公积金基数上下限模板, atb930): AA98 省限(AAA981='2')/市限('4') SYSDATE 现窗
 *   SELECT INTO(无→'模板下载出错…未设置省/市公积金缴费基数上下限')→DELETE TC0A_BP01→
 *   省/市两轮 TC90 候选(ATC90W='0' AND ATC905 出窗 AND AAA303 在窗 AND 属地 99/非99)→
 *   INSERT TC0A_BP01(AAA303_2=上限 986; 源两 IF<986/>986 同赋 986, 外层 WHERE 保证恒命中)。
 *   **旧 bug 忠实**: 市分支候选过滤仍用省窗 R_AA98_1, 仅 AAA303_2 赋值用市窗——现网省/市窗
 *   数值相同不可观测, 代码忠实保留。
 * ② 调整校验(PRC_…调整校验, ace100): 先 REPLACE(ATC0A2/3,'.0','')(Excel 数字尾巴)→AA98 探测
 *   (报错文案 copy-paste 仍称"模板下载出错"=忠实)→(ACE10A IS NULL OR 0/2/3) 逐行:
 *   取合同((ATC90W 0/2/null) AND ATB90R='3' + 身份证+姓名+单元, SELECT INTO 无 MAX→重名 TOO_MANY
 *   同归'未匹配到合同')→AAA303_2 非空 TO_NUMBER(此处 OTHERS 活跃='格式不正确', 与 W5b 死分支
 *   不同)→NULL 走'不能为空或者零'→补缴三查(是/否+起止年月 TO_DATE 活分支+次序)→**只查省限出窗**
 *   (市不查=源缺陷忠实)→补缴算 ABP00D/E=ROUND((303_2-303_1)×比例/100)×月数, ABP00C=和,
 *   省/市两分支代码相同(copy-paste)→全量写回(未补缴时比例/ABP 写回 DB 原值=SET ROW 语义)。
 *   逐行 RowFail(ACE10A='2')不中断; 外层全 catch→返回 -1 map(不 throw——③要内嵌调用)。
 *   汇总按**全批**统计(源 SELECT COUNT WHERE ACE100 不限 ACE10A)。
 * ③ 调整提交(PRC_…调整提交, ace100+aae011): loadUser→**内嵌调校验**(忽略返回, 源同包调用)→
 *   ACE10A='3' 逐行: 取 V_TC90(视图)同条件——**裸 SELECT INTO 无 per-row handler**: 缺行/重名
 *   穿外层=整过程'提交失败，原因为：ORA-01403/01422'(新系统=整事务回滚零半账, W4d 先例)→
 *   建 TC0A 行(ATC0A0=SEQ_TC0A_ATC0A0, AAA303=视图原值, AAA303_1=R.AAA303_2,
 *   ATC0A1=DECODE(是→1 否/null→0), AAE013=NULL, AAE114='0', 经办六件套)→
 *   UK_TC0A(AAE002,ATC900, V17 复刻)命中→UPDATE(WHERE AAE114 IN('0','2') AND AAE002+ATC900,
 *   已审 '1' 行 0 行静默=源忠实; 铁律4 先探测不试错)→ACE10A='1'(INSERT 段 OTHERS→'2'
 *   '记录申请信息失败|')。汇总全批。
 * ④ 调整审核(PRC_…调整审核, atc0a0 逗号串+flag 1过/2驳/3取消+aae011+aae013): 拆串(前端前导
 *   空格+尾逗号, trim 滤空=REGEXP_SUBSTR 语义; 非数字 continue)→逐 id 取 TC0A(无→静默跳过,
 *   源 PRM_CODE 复位继续)→**flag 非法在循环体内 RETURN**(ids 空则不报=源顺序)→
 *   转款确认拦截: 省(SUBSTR(AAA020,3,2)='99')查 TJ0D1 / 市查 TJ0E1 WHERE AAE013='1' AND
 *   AAE002+ATC900——FLAG=1 仅 N_CNT>0 拦, FLAG=2/3 需 N_CNT>0 且旧 AAE114='1' 才拦→
 *   FLAG=1: AAE114='1'+审核六件套(AAE111/117/136/119/120/122)+TC90.AAA303←AAA303_1+
 *   AA97.AAA303_1←同(WHERE AAE001=SUBSTR(AAE002,1,4))+AA97_BG 台账(AA97TYPE='4', BGQ=原/BGH=新,
 *   经办=TC0A 行经办; INSERT 失败→'记录缴费基数变更信息出错' 整过程终止);
 *   FLAG=2(驳): AAE114='2'+AAE113=原因; FLAG=3(取消): AAE114='0'+AAE113=NULL; 两者旧='1' 时
 *   TC90/AA97 回滚原值(**AA97 段无 AAE001 条件=源差异忠实**)+**DELETE AA97_BG**
 *   (物理删=忠实, 铁律8 改造清单在册)。汇总='操作成功：X条；操作失败：Y条，失败原因为
 *   公积金转款已确认，不能再操作'。外层 OTHERS→'垫付失败，原因为：'(copy-paste 文案忠实)。
 * ⑤ 下限托底(PRC_公积金基数下限调整, aae002+aae011): AA98 省/市按**参数年月**窗口(非 SYSDATE;
 *   缺失静默置 NULL→该侧过滤条件 NULL=不选人, 源 record NULL 语义)→候选(TC90×AB01×TB93,
 *   ATC90W 0/2/null, AAA303<对应侧下限)→建 TC0A(AAA303_1=侧 985, ATC0A1=AAA503<参数?'1':'0'
 *   (null 比较=NULL→'0'), ATC0A2=AAA503(可 null), ATC0A3=参数前一月, N_YS=补缴时
 *   MONTHS_BETWEEN(ATC0A3,ATC0A2)+1 否则 1)→**ABP 省市不对称忠实**: 省=ROUND(x×比例/100)×N_YS,
 *   市=ROUND(x×比例/100) 不乘月数→ABP00C=D+E→UK 命中 UPDATE(同提交多一列 ATC90W)。
 *   无前端调用点(外挂过程)。
 */
@Component
class Jishu5Tc0aCases(private val jdbc: JdbcTemplate) {

    private class RowFail(val msg: String) : Exception(msg)

    private val Z = BigDecimal.ZERO

    private fun bd(v: Any?): BigDecimal? = when (v) {
        null -> null
        is Number -> BigDecimal(v.toString())
        is String -> v.trim().takeIf { it.isNotEmpty() }?.let { BigDecimal(it) }
        else -> BigDecimal(v.toString())
    }
    private fun bd0(v: Any?): BigDecimal = bd(v) ?: Z
    private fun nm(v: Any?): Long? = when (v) {
        null -> null
        is Number -> v.toLong()
        is String -> v.trim().takeIf { it.isNotEmpty() }?.let { BigDecimal(it).toLong() }
        else -> null
    }
    private fun s(v: Any?): String? = v?.toString()
    private fun empty(v: Any?): Boolean = v == null || (v is String && v.trim().isEmpty())
    /** Oracle NUMBER→VARCHAR2 隐式转换: 去尾零 */
    private fun numStr(v: BigDecimal?): String? = v?.stripTrailingZeros()?.toPlainString()
    private fun substr34(v: String?): String? = v?.takeIf { it.length >= 4 }?.substring(2, 4)
    private fun nowYm(): Long = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMM")).toLong()
    /** Oracle MONTHS_BETWEEN(整月日期) = 月差 */
    private fun monthsBetween(a: String, b: String): Long {
        val ya = YearMonth.parse(a, DateTimeFormatter.ofPattern("yyyyMM"))
        val yb = YearMonth.parse(b, DateTimeFormatter.ofPattern("yyyyMM"))
        return (ya.year * 12 + ya.monthValue).toLong() - (yb.year * 12 + yb.monthValue).toLong()
    }
    /** Oracle SUBSTR('dw:gr', 1, INSTR-1) / SUBSTR(x, INSTR+1) 拆比例 */
    private fun splitRatio(atb90s: String?): Pair<BigDecimal?, BigDecimal?> {
        if (atb90s == null) return null to null
        val i = atb90s.indexOf(':')
        val dw = if (i > 0) atb90s.substring(0, i).toBigDecimalOrNull() else null
        val gr = if (i >= 0) atb90s.substring(i + 1).toBigDecimalOrNull() else null
        return dw to gr
    }

    /** AA98 现窗(kind: AAA981; atYm: 窗口判定年月) — SELECT INTO 语义: 无/多行→null */
    private fun aa98Window(kind: String, atYm: Long): Map<String, Any?>? =
        jdbc.queryForList("SELECT * FROM aa98 WHERE aaa987<=? AND aaa988>=? AND aaa981=?", atYm, atYm, kind)
            .firstOrNull()

    private fun loadUser(aae011: String, failMsg: String): Array<String?> =
        jdbc.query(
            "SELECT aae011, aae017, aae019, aae020, aae022 FROM sys_org_user_unit WHERE aae011=?",
            { rs, _ -> arrayOf(rs.getString("aae011"), rs.getString("aae017"), rs.getString("aae019"),
                rs.getString("aae020"), rs.getString("aae022")) }, aae011).firstOrNull()
            ?: throw JishuErr(failMsg)

    // ==================== ① 模板 ====================

    @Transactional
    fun muban(atb930: String): Map<String, Any?> {
        val atb930N = atb930.toLongOrNull()
            ?: return mapOf("appCode" to -1, "errMsg" to "模板下载出错，原因为：ORA-01722: 无效数字")
        val ym = nowYm()
        // 省公积金基数上下限(AAA981='2')
        val s98 = aa98Window("2", ym) ?: return mapOf(
            "appCode" to -1, "errMsg" to "模板下载出错，原因为：未设置省公积金缴费基数上下限")
        // 市公积金基数上下限(AAA981='4')
        val c98 = aa98Window("4", ym) ?: return mapOf(
            "appCode" to -1, "errMsg" to "模板下载出错，原因为：未设置市公积金缴费基数上下限")
        val s985 = bd0(s98["aaa985"]); val s986 = bd0(s98["aaa986"])
        val c986 = bd0(c98["aaa986"])
        jdbc.update("DELETE FROM tc0a_bp01 WHERE atb930=?", atb930N)
        var generated = 0
        // 省/市两轮; 市分支候选过滤仍用省窗 = 源旧 bug 忠实(仅 AAA303_2 赋值用市窗 c986)
        // city=false 省分支 / true 市分支; cmp 为布尔常量派生的 '='/'<>'(无注入面)
        for (city in listOf(false, true)) {
            val cmp = if (city) "=" else "<>"
            val rows = jdbc.queryForList(
                """SELECT * FROM tc90 WHERE atc90w='0'
                   AND (atc905<? OR atc905>?) AND aaa303>? AND aaa303<?
                   AND substr(aaa120,3,2) $cmp '99'
                   AND atb930=?""",
                s985, s986, s985, s986, atb930N)
            for (r in rows) {
                val atc905 = bd(r["atc905"])
                // 源两 IF 同赋 986: ATC905<986 或 >986 任一命中(外层 WHERE 保证至少其一)
                val a3032 = if (atc905 != null && (atc905 < s986 || atc905 > s986)) s986 else null
                try {
                    jdbc.update(
                        """INSERT INTO tc0a_bp01 (atb930, atb931, atc900, aac003, aac002, aae002,
                           atc905, aaa303_1, aaa303_2, atc0a1, atc0a2, atc0a3)
                           VALUES (?, NULL, ?, ?, ?, NULL, ?, ?, ?, NULL, NULL, NULL)""",
                        atb930N, nm(r["atc900"]), s(r["aac003"]), s(r["aac002"]),
                        numStr(atc905), numStr(bd(r["aaa303"])), numStr(a3032))
                    generated++
                } catch (e: Exception) {
                    return mapOf("appCode" to -1, "errMsg" to "模板下载出错，原因为：员工数据生成失败")
                }
            }
        }
        return mapOf("appCode" to 1, "errMsg" to null as String?, "generated" to generated)
    }

    // ==================== ② 调整校验 ====================

    /** 校验主体(返回 envelope; 不 throw — ③内嵌调用忽略返回继续) */
    @Transactional
    fun jiaoyan(ace100: String): Map<String, Any?> {
        val ace100N = ace100.toLongOrNull()
            ?: return mapOf("appCode" to -1, "errMsg" to "校验失败，原因为：ORA-01722: 无效数字")
        // Excel 数字尾巴清理(源 REPLACE '.0')
        jdbc.update(
            "UPDATE tc0a_bp SET atc0a2=replace(atc0a2,'.0',''), atc0a3=replace(atc0a3,'.0','') WHERE ace100=?",
            ace100N)
        val ym = nowYm()
        val s98 = aa98Window("2", ym) ?: return mapOf(
            "appCode" to -1, "errMsg" to "模板下载出错，原因为：未设置省公积金缴费基数上下限")
        aa98Window("4", ym) ?: return mapOf(
            "appCode" to -1, "errMsg" to "模板下载出错，原因为：未设置市公积金缴费基数上下限")
        val s985 = bd0(s98["aaa985"]); val s986 = bd0(s98["aaa986"])
        val rows = jdbc.queryForList(
            "SELECT * FROM tc0a_bp WHERE (ace10a IS NULL OR ace10a IN ('0','2','3')) AND ace100=? ORDER BY id",
            ace100N)
        for (rec in rows) {
            val id = nm(rec["id"]) ?: continue
            try {
                jiaoyanRow(rec, s985, s986, id)
            } catch (e: RowFail) {
                jdbc.update("UPDATE tc0a_bp SET ace10a='2', ace10b=? WHERE id=?", e.msg, id)
            }
        }
        // 汇总按全批(源 SELECT COUNT WHERE ACE100, 不限 ACE10A)
        val stat = jdbc.queryForMap(
            """SELECT count(*) total,
               coalesce(sum(case when ace10a='3' then 1 else 0 end),0) ok,
               coalesce(sum(case when ace10a<>'3' then 1 else 0 end),0) ng
               FROM tc0a_bp WHERE ace100=?""", ace100N)
        val msg = "校验完成，共导入【${stat["total"]}】条数据，其中【${stat["ok"]}】条数据符合要求，" +
            "【${stat["ng"]}】条数据不符合要求！"
        return mapOf("appCode" to 1, "errMsg" to msg,
            "rows" to stat["total"], "passed" to stat["ok"], "failed" to stat["ng"])
    }

    /** 校验单行 */
    private fun jiaoyanRow(rec: Map<String, Any?>, s985: BigDecimal, s986: BigDecimal, id: Long) {
        // 合同: SELECT INTO 无 MAX — 重名 TOO_MANY 同归'未匹配到合同'(OTHERS)
        val tcs = jdbc.queryForList(
            """SELECT * FROM tc90 WHERE (atc90w='0' OR atc90w='2' OR atc90w IS NULL) AND atb90r='3'
               AND aac003=? AND aac002=? AND atb930=?""",
            s(rec["aac003"]), s(rec["aac002"]), nm(rec["atb930"]))
        val t = tcs.firstOrNull() ?: throw RowFail("未匹配到合同|")
        if (tcs.size > 1) throw RowFail("未匹配到合同|")

        // 调整后基数: 此处 OTHERS 捕 TO_NUMBER = 活分支(与 W5b 死分支不同)
        val a3032 = if (!empty(rec["aaa303_2"])) {
            try { BigDecimal(s(rec["aaa303_2"])!!.trim()) } catch (e: NumberFormatException) {
                throw RowFail("公积金调整后缴费基数格式不正确|")
            }
        } else {
            // ELSIF NVL(x,0)=0 OR x IS NULL — 非空已走 IF, 此支仅 NULL 可达
            throw RowFail("公积金调整后缴费基数不能为空或者零|")
        }

        if (!empty(rec["atc0a1"])) {
            if (rec["atc0a1"] == "是" && (empty(rec["atc0a2"]) || empty(rec["atc0a3"])))
                throw RowFail("选择补缴，则补缴开始年月、补缴结束年月不能为空|")
            if (rec["atc0a1"] != "是" && rec["atc0a1"] != "否")
                throw RowFail("是否补缴格式不正确，请填写“是”或者“否”|")
        }
        val a2 = s(rec["atc0a2"])
        val a3 = s(rec["atc0a3"])
        if (!empty(a2) && parseYm(a2) == null) throw RowFail("补缴开始年月格式不正确|")
        if (!empty(a3) && parseYm(a3) == null) throw RowFail("补缴结束年月格式不正确|")
        if (!empty(a2) && !empty(a3) && a2!! > a3!!) throw RowFail("补缴结束年月不能小于补缴开始年月|")

        // 只查省限出窗(市不查=源缺陷忠实)
        if (substr34(s(t["aaa120"])) == "99" && (a3032 < s985 || a3032 > s986))
            throw RowFail("调整后的缴费基数不能超出上下限范围|")

        // 补缴: 比例从 ATB90S 'dw:gr' 拆; ABP=ROUND((新-原)×比例/100)×月数(省市两分支代码相同)
        var dw = bd(rec["atb90s_dw"])
        var gr = bd(rec["atb90s_gr"])
        var abpD = bd(rec["abp00d"])
        var abpE = bd(rec["abp00e"])
        var abpC = bd(rec["abp00c"])
        if (rec["atc0a1"] == "是") {
            val nYs = BigDecimal(monthsBetween(a3!!, a2!!) + 1)
            val (dw2, gr2) = splitRatio(s(t["atb90s"]))
            dw = dw2; gr = gr2
            abpD = bd0(a3032).subtract(bd0(rec["aaa303_1"])).multiply(bd0(dw))
                .divide(BigDecimal(100), 0, RoundingMode.HALF_UP).multiply(nYs)
            abpE = bd0(a3032).subtract(bd0(rec["aaa303_1"])).multiply(bd0(gr))
                .divide(BigDecimal(100), 0, RoundingMode.HALF_UP).multiply(nYs)
            abpC = bd0(abpD).add(bd0(abpE))
        }
        // 全量写回: 未补缴时比例/ABP 为 DB 原值(SET ROW 语义)
        jdbc.update(
            """UPDATE tc0a_bp SET ace10a='3', ace10b=NULL, atb90s_dw=?, atb90s_gr=?,
               abp00c=?, abp00d=?, abp00e=? WHERE id=?""",
            dw, gr, abpC?.toPlainString(), abpD?.toPlainString(), abpE?.toPlainString(), id)
    }

    /** Oracle TO_DATE(x||'01','yyyy-mm-dd') 语义: yyyymm 合法性 */
    private fun parseYm(v: String?): YearMonth? = try {
        YearMonth.parse(v!!.trim(), DateTimeFormatter.ofPattern("yyyyMM"))
    } catch (e: Exception) {
        null
    }

    // ==================== ③ 调整提交 ====================

    @Transactional
    fun tijiao(ace100: String, aae011: String): Map<String, Any?> {
        val ace100N = ace100.toLongOrNull()
            ?: return mapOf("appCode" to -1, "errMsg" to "提交失败，原因为：ORA-01722: 无效数字")
        val user = loadUser(aae011, "查询经办人信息出错，错误信息：no data found，请将错误信息发给维护工程师")
        // 内嵌调校验(源同包调用, PRM_CODE/MSG 被覆盖后照样继续)
        jiaoyan(ace100)
        val rows = jdbc.queryForList(
            "SELECT * FROM tc0a_bp WHERE ace10a='3' AND ace100=? ORDER BY id", ace100N)
        var committed = 0
        var failed = 0
        for (rec in rows) {
            val id = nm(rec["id"]) ?: continue
            var nOk = "1"
            var vMsg: String? = null
            try {
                // V_TC90 裸 SELECT INTO: 缺行/重名穿外层=整过程错(源无 per-row handler)
                val tcs = jdbc.queryForList(
                    """SELECT * FROM v_tc90 WHERE (atc90w='0' OR atc90w='2' OR atc90w IS NULL) AND atb90r='3'
                       AND aac003=? AND aac002=? AND atb930=?""",
                    s(rec["aac003"]), s(rec["aac002"]), nm(rec["atb930"]))
                val t = tcs.firstOrNull()
                    ?: throw JishuErr("提交失败，原因为：ORA-01403: no data found")
                if (tcs.size > 1)
                    throw JishuErr("提交失败，原因为：ORA-01422: exact fetch returns more than requested number of rows")
                val aae002 = nm(rec["aae002"])
                val atc900 = nm(t["atc900"])
                val a3031 = bd0(rec["aaa303_2"])
                val atc0a1 = if (rec["atc0a1"] == "是") "1" else "0"
                val dw = bd(rec["atb90s_dw"]); val gr = bd(rec["atb90s_gr"])
                val abpC = bd(rec["abp00c"]); val abpD = bd(rec["abp00d"]); val abpE = bd(rec["abp00e"])
                // 铁律4: 先探测 UK_TC0A(AAE002,ATC900, V17 复刻), 命中走 UPDATE 分支
                val dup = jdbc.queryForObject(
                    "SELECT count(*) FROM tc0a WHERE aae002=? AND atc900=?",
                    Int::class.java, aae002, atc900)!! > 0
                if (dup) {
                    // WHERE AAE114 IN('0','2'): 已审 '1' 行 0 行静默=源忠实
                    jdbc.update(
                        """UPDATE tc0a SET aaa303_1=?, atc0a1=?, atc0a2=?, atc0a3=?,
                           atb90s_dw=?, atb90s_gr=?, abp00c=?, abp00d=?, abp00e=?
                           WHERE aae114 IN ('0','2') AND aae002=? AND atc900=?""",
                        a3031, atc0a1, nm(rec["atc0a2"]), nm(rec["atc0a3"]),
                        dw, gr, abpC?.toPlainString(), abpD?.toPlainString(), abpE?.toPlainString(),
                        aae002, atc900)
                } else {
                    try {
                        // 铁律5: 35 列逐列对位(atc0a0=nextval 内联; aae013=NULL/aae114='0'/aae036=now() 字面量, 余 31 ?)
                        jdbc.update(
                            """INSERT INTO tc0a (atc0a0,  aae002,  atc900,  atb930,
                                atb931,   aab001,  aab004,  aaa030,
                                aaa031,   aaa020,  aaa021,  aaa001,
                                aac001,   aac003,  aac002,  atc905,
                                aaa303,   aaa303_1, atc0a1, atc0a2,
                                atc0a3,   atb90s_dw, atb90s_gr, abp00c,
                                abp00d,   abp00e,  atc90w,
                                aae013,   aae114,  aae011,
                                aae017,   aae036,  aae019,
                                aae020,   aae022)
                                VALUES (nextval('seq_tc0a_atc0a0'), ?, ?, ?,
                                        ?, ?, ?, ?,
                                        ?, ?, ?, ?,
                                        ?, ?, ?, ?,
                                        ?, ?, ?, ?,
                                        ?, ?, ?, ?,
                                        ?, ?, ?,
                                        NULL, '0', ?,
                                        ?, now(), ?,
                                        ?, ?)""",
                            /*aae002*/ aae002, /*atc900*/ atc900,
                            /*atb930*/ nm(t["atb930"]), /*atb931*/ s(t["atb931"]),
                            /*aab001*/ nm(t["aab001"]), /*aab004*/ s(t["aab004"]),
                            /*aaa030*/ nm(t["aaa030"]), /*aaa031*/ s(t["aaa031"]),
                            /*aaa020*/ s(t["aaa120"]), /*aaa021*/ s(t["aaa121"]),
                            /*aaa001*/ s(t["aaa004"]), /*aac001*/ nm(t["aac001"]),
                            /*aac003*/ s(t["aac003"]), /*aac002*/ s(t["aac002"]),
                            /*atc905*/ bd(t["atc905"]), /*aaa303*/ bd(t["aaa303"]),
                            /*aaa303_1*/ a3031, /*atc0a1*/ atc0a1,
                            /*atc0a2*/ nm(rec["atc0a2"]), /*atc0a3*/ nm(rec["atc0a3"]),
                            /*atb90s_dw*/ dw, /*atb90s_gr*/ gr,
                            /*abp00c*/ abpC?.toPlainString(), /*abp00d*/ abpD?.toPlainString(),
                            /*abp00e*/ abpE?.toPlainString(), /*atc90w*/ s(t["atc90w"]),
                            // aae013=NULL, aae114='0'
                            /*aae011*/ user[0], /*aae017*/ user[1],
                            // aae036=now()
                            /*aae019*/ user[2], /*aae020*/ user[3], /*aae022*/ user[4])
                    } catch (e: Exception) {
                        throw RowFail("记录申请信息失败|${e.cause?.message ?: e.message}")
                    }
                }
            } catch (e: RowFail) {
                nOk = "2"; vMsg = e.message
            }
            jdbc.update("UPDATE tc0a_bp SET ace10a=?, ace10b=? WHERE id=?", nOk, vMsg, id)
            if (nOk == "1") committed++ else failed++
        }
        // 汇总按全批
        val stat = jdbc.queryForMap(
            """SELECT count(*) total,
               coalesce(sum(case when ace10a='1' then 1 else 0 end),0) ok,
               coalesce(sum(case when ace10a<>'1' then 1 else 0 end),0) ng
               FROM tc0a_bp WHERE ace100=?""", ace100N)
        val msg = "提交完成，共导入【${stat["total"]}】条数据，其中【${stat["ok"]}】条数据成功，" +
            "【${stat["ng"]}】条数据失败！"
        return mapOf("appCode" to 1, "errMsg" to msg,
            "rows" to stat["total"], "committed" to stat["ok"], "failed" to stat["ng"])
    }

    // ==================== ④ 调整审核 ====================

    @Transactional
    fun shenhe(atc0a0s: String, flag: String, aae011: String, aae013: String): Map<String, Any?> {
        val user = loadUser(aae011, "审核失败，原因为无法查询到经办人信息")
        // 前端传 " id1,id2," — trim 滤空 = Oracle REGEXP_SUBSTR '[^,]+' 语义
        val ids = atc0a0s.split(',').map { it.trim() }.filter { it.isNotEmpty() }
        var sumCg = 0
        var sumSb = 0
        for (idStr in ids) {
            val atc0a0 = idStr.toLongOrNull() ?: continue // 源 TAB1 是 NUMBER 表, 非数字→异常复位 code 继续
            val a = jdbc.queryForList("SELECT * FROM tc0a WHERE atc0a0=?", atc0a0).firstOrNull()
                ?: continue // 源 SELECT INTO 失败→RAISE→复位 code 继续下一 id
            if (flag != "1" && flag != "2" && flag != "3")
                return mapOf("appCode" to -1, "errMsg" to "审核失败，原因为：审核类别不正确")
            // 转款确认拦截: 省查 TJ0D1 / 市查 TJ0E1 WHERE AAE013='1'
            val cnt = jdbc.queryForObject(
                if (substr34(s(a["aaa020"])) == "99")
                    "SELECT count(*) FROM tj0d1 WHERE aae013='1' AND aae002=? AND atc900=?"
                else
                    "SELECT count(*) FROM tj0e1 WHERE aae013='1' AND aae002=? AND atc900=?",
                Int::class.java, nm(a["aae002"]), nm(a["atc900"]))!!
            val blocked = if (flag == "1") cnt > 0 else (cnt > 0 && a["aae114"] == "1")
            if (blocked) { sumSb++; continue }
            when (flag) {
                "1" -> {
                    jdbc.update(
                        """UPDATE tc0a SET aae114='1', aae113=NULL, aae111=?, aae117=?, aae136=now(),
                           aae119=?, aae120=?, aae122=? WHERE atc0a0=?""",
                        user[0], user[1], user[2], user[3], user[4], atc0a0)
                    // TC90 在同步集合: 存量行落库=退役后启用语义(同 W5a 确认)
                    jdbc.update("UPDATE tc90 SET aaa303=? WHERE atc900=?", bd(a["aaa303_1"]), nm(a["atc900"]))
                    jdbc.update(
                        "UPDATE aa97 SET aaa303_1=? WHERE aae001=? AND atc900=?",
                        bd(a["aaa303_1"]), nm(a["aae002"])?.toString()?.take(4)?.toLongOrNull(),
                        nm(a["atc900"]))
                    try {
                        jdbc.update(
                            """INSERT INTO aa97_bg (id, tname, tkey, atc900, aa97type, aaa300_bgq, aaa300_bgh,
                               aae013, aae011, aae017, aae019, aae020, aae022, aae036)
                               VALUES (nextval('seq_aa97_bg_id'), 'TC0A', ?, ?, '4', ?, ?,
                               '公积金缴费基数上下限调整', ?, ?, ?, ?, ?, now())""",
                            nm(a["atc0a0"]), nm(a["atc900"]),
                            bd(a["aaa303"]), bd(a["aaa303_1"]),
                            s(a["aae011"]), s(a["aae017"]), s(a["aae019"]), s(a["aae020"]), s(a["aae022"]))
                    } catch (e: Exception) {
                        return mapOf("appCode" to -1, "errMsg" to "记录缴费基数变更信息出错")
                    }
                    sumCg++
                }
                "2", "3" -> {
                    val wasAudited = a["aae114"] == "1"
                    if (flag == "2") {
                        jdbc.update(
                            """UPDATE tc0a SET aae114='2', aae113=?, aae111=?, aae117=?, aae136=now(),
                               aae119=?, aae120=?, aae122=? WHERE atc0a0=?""",
                            aae013, user[0], user[1], user[2], user[3], user[4], atc0a0)
                    } else {
                        jdbc.update(
                            """UPDATE tc0a SET aae114='0', aae113=NULL, aae111=?, aae117=?, aae136=now(),
                               aae119=?, aae120=?, aae122=? WHERE atc0a0=?""",
                            user[0], user[1], user[2], user[3], user[4], atc0a0)
                    }
                    if (wasAudited) {
                        // 旧='1' 才回滚 TC90/AA97 到原值(AA97 段无 AAE001 条件=源差异忠实)
                        jdbc.update("UPDATE tc90 SET aaa303=? WHERE atc900=?", bd(a["aaa303"]), nm(a["atc900"]))
                        jdbc.update("UPDATE aa97 SET aaa303_1=? WHERE atc900=?", bd(a["aaa303"]), nm(a["atc900"]))
                        // 物理删=忠实(铁律8 改造清单在册)
                        jdbc.update(
                            """DELETE FROM aa97_bg WHERE aa97type='4' AND tname='TC0A'
                               AND tkey=? AND atc900=?""",
                            nm(a["atc0a0"]), nm(a["atc900"]))
                    }
                    sumCg++
                }
            }
        }
        val msg = "操作成功：${sumCg}条；操作失败：${sumSb}条，失败原因为公积金转款已确认，不能再操作"
        return mapOf("appCode" to 1, "errMsg" to msg, "passed" to sumCg, "failed" to sumSb)
    }

    // ==================== ⑤ 下限托底 ====================

    @Transactional
    fun xiaxian(aae002: String, aae011: String): Map<String, Any?> {
        val user = loadUser(aae011, "查询经办人信息出错，错误信息：no data found，请将错误信息发给维护工程师")
        val aae002N = aae002.toLongOrNull()
            ?: return mapOf("appCode" to -1, "errMsg" to "提交失败，原因为：ORA-01722: 无效数字")
        // AA98 窗按参数年月(非 SYSDATE); 缺失静默置 NULL → 该侧过滤 NULL=不选人(源 record NULL 语义)
        val s98 = aa98Window("2", aae002N)
        val c98 = aa98Window("4", aae002N)
        val s985 = s98?.let { bd(it["aaa985"]) }
        val c985 = c98?.let { bd(it["aaa985"]) }
        val prevYm = YearMonth.parse(aae002, DateTimeFormatter.ofPattern("yyyyMM")).minusMonths(1)
            .format(DateTimeFormatter.ofPattern("yyyyMM")).toLong()
        val rows = jdbc.queryForList(
            """SELECT a.*, b.aaa030, b.aaa031, c.atb931 FROM tc90 a, ab01 b, tb93 c
               WHERE a.aab001=b.aab001 AND a.atb930=c.atb930
                 AND (a.atc90w='0' OR a.atc90w IS NULL OR a.atc90w='2')
                 AND ((substr(a.aaa120,3,2)<>'99' AND a.aaa303<?)
                      OR (substr(a.aaa120,3,2)='99' AND a.aaa303<?))""",
            c985, s985)
        var generated = 0
        for (r in rows) {
            val a3031 = if (substr34(s(r["aaa120"])) != "99") c985 else s985
            // Oracle NULL 比较语义: AAA503 null → 条件 NULL → ELSE '0' (bd0 会把 null 变 0, 不可用)
            val a503 = bd(r["aaa503"])
            val atc0a1 = if (a503 != null && a503 < BigDecimal(aae002N)) "1" else "0"
            val (dw0, gr0) = splitRatio(s(r["atb90s"]))
            // N_YS=补缴时 MONTHS_BETWEEN(ATC0A3, ATC0A2)+1, 否则 1 (AAA503 null→'0'→不进)
            val nYs = if (atc0a1 == "1")
                BigDecimal(monthsBetween(prevYm.toString(), a503!!.toPlainString()) + 1)
            else BigDecimal.ONE
            // 省市不对称忠实: 省=ROUND×N_YS, 市=ROUND 不乘
            val isProvince = substr34(s(r["aaa120"])) == "99"
            val abpD = a3031?.multiply(bd0(dw0))?.divide(BigDecimal(100), 0, RoundingMode.HALF_UP)
                ?.let { if (isProvince) it.multiply(nYs) else it }
            val abpE = a3031?.multiply(bd0(gr0))?.divide(BigDecimal(100), 0, RoundingMode.HALF_UP)
                ?.let { if (isProvince) it.multiply(nYs) else it }
            val abpC = bd0(abpD).add(bd0(abpE))
            val atc900 = nm(r["atc900"])
            // 铁律4: 先探测 UK_TC0A(V17), 命中走 UPDATE(WHERE AAE114 IN('0','2'))
            val dup = jdbc.queryForObject(
                "SELECT count(*) FROM tc0a WHERE aae002=? AND atc900=?",
                Int::class.java, aae002N, atc900)!! > 0
            if (dup) {
                jdbc.update(
                    """UPDATE tc0a SET aaa303_1=?, atc0a1=?, atc0a2=?, atc0a3=?,
                       atb90s_dw=?, atb90s_gr=?, abp00c=?, abp00d=?, abp00e=?, atc90w=?
                       WHERE aae114 IN ('0','2') AND aae002=? AND atc900=?""",
                    a3031, atc0a1, bd(r["aaa503"]), prevYm,
                    dw0, gr0, abpC.toPlainString(), abpD?.toPlainString(), abpE?.toPlainString(),
                    s(r["atc90w"]), aae002N, atc900)
            } else {
                try {
                    // 铁律5: 35 列逐列对位(atc0a0=nextval 内联; aae013=NULL/aae114='0'/aae036=now() 字面量, 余 31 ?)
                    jdbc.update(
                        """INSERT INTO tc0a (atc0a0,  aae002,  atc900,  atb930,
                            atb931,   aab001,  aab004,  aaa030,
                            aaa031,   aaa020,  aaa021,  aaa001,
                            aac001,   aac003,  aac002,  atc905,
                            aaa303,   aaa303_1, atc0a1, atc0a2,
                            atc0a3,   atb90s_dw, atb90s_gr, abp00c,
                            abp00d,   abp00e,  atc90w,
                            aae013,   aae114,  aae011,
                            aae017,   aae036,  aae019,
                            aae020,   aae022)
                            VALUES (nextval('seq_tc0a_atc0a0'), ?, ?, ?,
                                    ?, ?, ?, ?,
                                    ?, ?, ?, ?,
                                    ?, ?, ?, ?,
                                    ?, ?, ?, ?,
                                    ?, ?, ?, ?,
                                    ?, ?, ?,
                                    NULL, '0', ?,
                                    ?, now(), ?,
                                    ?, ?)""",
                        /*aae002*/ aae002N, /*atc900*/ atc900,
                        /*atb930*/ nm(r["atb930"]), /*atb931*/ s(r["atb931"]),
                        /*aab001*/ nm(r["aab001"]), /*aab004*/ s(r["aab004"]),
                        /*aaa030*/ nm(r["aaa030"]), /*aaa031*/ s(r["aaa031"]),
                        /*aaa020*/ s(r["aaa120"]), /*aaa021*/ s(r["aaa121"]),
                        /*aaa001*/ s(r["aaa004"]), /*aac001*/ nm(r["aac001"]),
                        /*aac003*/ s(r["aac003"]), /*aac002*/ s(r["aac002"]),
                        /*atc905*/ bd(r["atc905"]), /*aaa303*/ bd(r["aaa303"]),
                        /*aaa303_1*/ a3031, /*atc0a1*/ atc0a1,
                        /*atc0a2*/ bd(r["aaa503"]), /*atc0a3*/ prevYm,
                        /*atb90s_dw*/ dw0, /*atb90s_gr*/ gr0,
                        /*abp00c*/ abpC.toPlainString(), /*abp00d*/ abpD?.toPlainString(),
                        /*abp00e*/ abpE?.toPlainString(), /*atc90w*/ s(r["atc90w"]),
                        // aae013=NULL, aae114='0'
                        /*aae011*/ user[0], /*aae017*/ user[1],
                        // aae036=now()
                        /*aae019*/ user[2], /*aae020*/ user[3], /*aae022*/ user[4])
                    generated++
                } catch (e: Exception) {
                    return mapOf("appCode" to -1,
                        "errMsg" to "生成基数下限调整名单出错，原因为：${e.cause?.message ?: e.message}")
                }
            }
        }
        return mapOf("appCode" to 1, "errMsg" to null as String?, "generated" to generated)
    }
}

// ==================== /proc 注册面 ====================

@Component
class Jishu5Tc0aMubanProc(private val c: Jishu5Tc0aCases) : ProcHandler {
    override fun procName() = "PKG_缴费基数.PRC_公积金基数上下限模板"
    override fun handle(params: Map<String, Any?>): Map<String, Any?> {
        val atb930 = params["atb930"]?.toString() ?: return mapOf("appCode" to -1, "errMsg" to "缺少 atb930")
        return try { c.muban(atb930) } catch (e: JishuErr) { mapOf("appCode" to -1, "errMsg" to e.m) }
    }
}

@Component
class Jishu5Tc0aJiaoyanProc(private val c: Jishu5Tc0aCases) : ProcHandler {
    override fun procName() = "PKG_缴费基数.PRC_公积金基数上下限调整校验"
    override fun handle(params: Map<String, Any?>): Map<String, Any?> {
        val ace100 = params["ace100"]?.toString() ?: return mapOf("appCode" to -1, "errMsg" to "缺少 ace100")
        return try { c.jiaoyan(ace100) } catch (e: JishuErr) { mapOf("appCode" to -1, "errMsg" to e.m) }
    }
}

@Component
class Jishu5Tc0aTijiaoProc(private val c: Jishu5Tc0aCases) : ProcHandler {
    override fun procName() = "PKG_缴费基数.PRC_公积金基数上下限调整提交"
    override fun handle(params: Map<String, Any?>): Map<String, Any?> {
        val ace100 = params["ace100"]?.toString() ?: return mapOf("appCode" to -1, "errMsg" to "缺少 ace100")
        val aae011 = params["aae011"]?.toString() ?: return mapOf("appCode" to -1, "errMsg" to "缺少 aae011")
        return try { c.tijiao(ace100, aae011) } catch (e: JishuErr) { mapOf("appCode" to -1, "errMsg" to e.m) }
    }
}

@Component
class Jishu5Tc0aShenheProc(private val c: Jishu5Tc0aCases) : ProcHandler {
    override fun procName() = "PKG_缴费基数.PRC_公积金基数上下限调整审核"
    override fun handle(params: Map<String, Any?>): Map<String, Any?> {
        val atc0a0 = params["atc0a0"]?.toString() ?: return mapOf("appCode" to -1, "errMsg" to "缺少 atc0a0")
        val flag = params["flag"]?.toString() ?: return mapOf("appCode" to -1, "errMsg" to "缺少 flag")
        val aae011 = params["aae011"]?.toString() ?: return mapOf("appCode" to -1, "errMsg" to "缺少 aae011")
        return try {
            c.shenhe(atc0a0, flag, aae011, params["aae013"]?.toString() ?: "")
        } catch (e: JishuErr) { mapOf("appCode" to -1, "errMsg" to e.m) }
    }
}

@Component
class Jishu5Tc0aXiaxianProc(private val c: Jishu5Tc0aCases) : ProcHandler {
    override fun procName() = "PKG_缴费基数.PRC_公积金基数下限调整"
    override fun handle(params: Map<String, Any?>): Map<String, Any?> {
        val aae002 = params["aae002"]?.toString() ?: return mapOf("appCode" to -1, "errMsg" to "缺少 aae002")
        val aae011 = params["aae011"]?.toString() ?: return mapOf("appCode" to -1, "errMsg" to "缺少 aae011")
        return try { c.xiaxian(aae002, aae011) } catch (e: JishuErr) { mapOf("appCode" to -1, "errMsg" to e.m) }
    }
}
