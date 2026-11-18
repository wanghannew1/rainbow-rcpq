package com.jlrc.rcpq.service.td

import com.jlrc.rcpq.service.proc.ProcHandler
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal

/**
 * W5b 基数导入校验/提交（旧 PKG_缴费基数 L5-635, 2026-11-18, tdcl(29) 页 基数导入对话框）。
 * 独立新文件(W4b 教训)。AA97_BP 批次流: 导入面(Excel→AA97_BP, 旧 lemis.imp.Import 建_CE10 批次)
 * 尚未移植——本类消费其产物, ACE100 批次键对接。
 *
 * PRC_基数导入校验(ace100+aae001): AA95 时限参数探测(AAA952='2' 唯一行, 现行版仅探测无消费点;
 *   '为设置时限参数' 错别字忠实)→ ACE10A IS NULL 行逐行:
 *   取有效合同(ATC90G='1' + 身份证+姓名+结算单元, MAX(ATC900); TOO_MANY 分支 ATC900=PK 实际不可达忠实保留)
 *   → AB01 取户头 → TC90 富化字段(调整前基数/属地/编号/单位) → 四险种校验:
 *     社保(P='0'&(202 空|>年01) 或 P='2'&R='3' 时 301_1 必填; 填了就查零, 整数校验被注释=死键);
 *     医保(PA 同构 或 INSTR(aac008,'80')退休旁路; 302_1/302_2 按 AAA110 3-4 位=99/非99 择一必填+查零);
 *     工伤(PB 同构+80 旁路; 305_1/305_2 择一, 80 退休豁免空值检查);
 *     公积金(W='0'/'2' 同构无 80 旁路; 303_1/303_2 择一+查零+【整数校验活跃】(唯一); 比例必填
 *     V_MSG 置后【无 RAISE=源活性缺陷忠实】→ 单位/个人比例整数段{12,7} 校验
 *     (Oracle SUBSTR(x,1,INSTR(x,'.')-1): 无小数点→NULL→检查静默通过=源活性绕过忠实))
 *   → 全过: ACE10A='3' + TC90 富化字段整行写回; 任一败: ACE10A='2'+ACE10B。
 *   TO_NUMBER 格式错: Oracle 抛 VALUE_ERROR 内层仅捕 E_EXCEPTION→穿到外层=整过程报错
 *   (源"格式不正确"捕获分支 NO_DATA_FOUND 永不命中=死分支, 忠实按整批报错);
 *   新系统事务化: 致命错整批回滚(旧系统无显式回滚, W4d 同构差异已记档)。
 * PRC_基数导入提交(ace100+aae011): ACE10A IN ('1','3') 行逐行 → 取合同(无→'0') →
 *   三序列发号(AAA970/BAZ001/BAZ002) → AA97 INSERT(指定列=R_AA97 已赋值字段, 未赋值列 NULL;
 *   申报基数按属地择一: AAA110/130/120 3-4 位=99 取 _1 否则 _2; AAE214_1~4='0', AAE100='0',
 *   经办六件套) → UK_AA97(AAE001+AAC001, PG 侧 V16 复刻)命中→UPDATE(铁律4: 先探测不试错,
 *   PG 事务毒化不能 DUP 再捕):
 *   AAE214_k<>'1' 才覆写申报基数(SQL 三值: NULL 行也走 ELSE=不覆写, 源活性语义忠实;
 *   比例两列同挂 AAE214_3 公积金线=源 quirks 忠实; WHERE AAE001+ATC900 换合同人匹配 0 行静默=忠实);
 *   TC90 比例串回写被注释=死赋值不移植 → ACE10A='1'。
 */
@Component
class Jishu5ImportCases(private val jdbc: JdbcTemplate) {

    /** 行级不合格信号: E_EXCEPTION 等价, 置 ACE10A=旗标+ACE10B=原因, 不中断批次 */
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
    /** Oracle ''=NULL 语义: 空串/空白视同 NULL */
    private fun empty(v: Any?): Boolean =
        v == null || (v is String && v.trim().isEmpty())
    private fun is80(v: String?): Boolean = (v ?: "").contains("80")
    /** Oracle SUBSTR(x,3,2) */
    private fun substr34(v: String?): String? = v?.takeIf { it.length >= 4 }?.substring(2, 4)
    /** Oracle NUMBER→VARCHAR2 隐式转换: 去尾零 (5000.00→'5000', 5000.50→'5000.5') */
    private fun numStr(v: BigDecimal?): String? = v?.stripTrailingZeros()?.toPlainString()
    /** Oracle 公积金比例整数段 SUBSTR(x,1,INSTR(x,'.')-1): 无小数点→长度<1→NULL→检查静默通过(源活性绕过) */
    private fun ratioIntPart(v: Any?): String? {
        val sv = s(v) ?: return null
        val i = sv.indexOf('.')
        return if (i > 0) sv.substring(0, i) else null
    }
    /** 公积金比例集合: 源字面 ('12','7','7')/('7','12') 同为 {7,12} */
    private fun inRatio(v: String?): Boolean = v == "12" || v == "7"

    // ==================== PRC_基数导入校验 ====================

    @Transactional
    fun jiaoyan(ace100: String, aae001: String): Map<String, Any?> {
        return try {
            val ace100N = ace100.toLongOrNull()
                ?: throw JishuErr("派遣员工工资基数导入校验出错，错误信息：ORA-01722: 无效数字，请将错误信息发给维护工程师")
            // AA95 时限参数探测(AAA952='2' 唯一行; 无行/多行→错别字报错忠实; R_AA95 现行无消费点)
            if (jdbc.queryForList("SELECT 1 FROM aa95 WHERE aaa952='2'").size != 1)
                return mapOf("appCode" to -1, "errMsg" to "为设置时限参数")
            // ACE10A IS NULL = 本批未校验行(旧 Oracle 无 ORDER BY, PG 侧 ORDER BY id 仅定序)
            val rows = jdbc.queryForList(
                "SELECT * FROM aa97_bp WHERE ace10a IS NULL AND ace100=? ORDER BY id", ace100N)
            var passed = 0
            var failed = 0
            for (rec in rows) {
                val id = nm(rec["id"]) ?: continue
                try {
                    jiaoyanRow(rec, aae001, id)
                    passed++
                } catch (e: RowFail) {
                    jdbc.update("UPDATE aa97_bp SET ace10a='2', ace10b=? WHERE id=?", e.msg, id)
                    failed++
                }
            }
            mapOf("appCode" to 1, "errMsg" to null as String?,
                "rows" to rows.size, "passed" to passed, "failed" to failed)
        } catch (e: JishuErr) {
            throw e
        } catch (e: Exception) {
            throw JishuErr("派遣员工工资基数导入校验出错，错误信息：${e.cause?.message ?: e.message}，请将错误信息发给维护工程师")
        }
    }

    /** 校验单行: 通过→富化整行写回+置'3'; 败→抛 RowFail(调用方置 '2') */
    private fun jiaoyanRow(rec: Map<String, Any?>, aae001: String, id: Long) {
        val y01 = (aae001 + "01").toLong()

        /** TO_NUMBER 致命语义: VALUE_ERROR 穿内层(仅捕 E_EXCEPTION)→整过程报错 */
        fun toNum(v: Any?): BigDecimal? = when {
            v == null || (v is String && v.trim().isEmpty()) -> null
            v is Number -> BigDecimal(v.toString())
            else -> try { BigDecimal((v as String).trim()) } catch (e: NumberFormatException) {
                throw JishuErr("派遣员工工资基数导入校验出错，错误信息：ORA-06502: PL/SQL: " +
                    "numeric or value error: character to number conversion error，请将错误信息发给维护工程师")
            }
        }

        // 有效合同: ATC90G='1' + 身份证+姓名+结算单元, MAX(ATC900)
        val maxAtc = jdbc.queryForList(
            "SELECT max(atc900) AS m FROM tc90 WHERE atc90g='1' AND aac002=? AND aac003=? AND atb930=?",
            s(rec["aac002"]), s(rec["aac003"]), nm(rec["atb930"]))
            .firstOrNull()?.let { nm(it["m"]) }
            ?: throw RowFail("未查询到劳动合同")
        val tcs = jdbc.queryForList("SELECT * FROM tc90 WHERE atc900=?", maxAtc)
        // 源 TOO_MANY_ROWS 分支: ATC900=PK 实际不可达, 忠实保留
        if (tcs.size > 1) throw RowFail("查询到多条有效劳动合同")
        val t = tcs.first()
        val aab001 = nm(t["aab001"])
        val head = jdbc.queryForList("SELECT aaa030, aaa031 FROM ab01 WHERE aab001=?", aab001)
            .firstOrNull() ?: throw RowFail("未查询到险金户头信息")

        val rr = s(t["atb90r"])
        val e80 = is80(s(t["aac008"]))

        // ---- 社保: 必填(条件成立时) + 非空即查零; 整数校验被注释(死键不移植)
        val p = s(t["atc90p"]); val a202 = bd(t["aaa202"])
        if (((p == "0" && (a202 == null || a202.toLong() > y01)) || (p == "2" && rr == "3")) &&
            empty(rec["aaa301_1"])) throw RowFail("【社保缴费基数】不能为空")
        else if (!empty(rec["aaa301_1"])) {
            val v = toNum(rec["aaa301_1"])
            if (v != null && v.compareTo(Z) == 0) throw RowFail("【社保缴费基数】不能为零")
        }

        // ---- 医保: PA 同构 或 80 退休旁路; 302_1/302_2 按 AAA110 3-4 位择一
        val pa = s(t["atc90pa"]); val a602 = bd(t["aaa602"])
        if ((pa == "0" && (a602 == null || a602.toLong() > y01)) || (pa == "2" && rr == "3") || e80) {
            if (empty(rec["aaa302_1"]) && empty(rec["aaa302_2"]))
                throw RowFail("【省医保缴费基数】和【市医保缴费基数】不能同时为空")
            val reg = substr34(s(t["aaa110"]))
            if (reg == "99") {
                if (empty(rec["aaa302_1"])) throw RowFail("医保参保地为省，【省医保缴费基数】不能为空")
                else {
                    val v = toNum(rec["aaa302_1"])
                    if (v != null && v.compareTo(Z) == 0) throw RowFail("【省医保缴费基数】不能为零")
                }
            } else if (reg != null) { // SQL 三值: 属地 NULL 两分支都不走
                if (empty(rec["aaa302_2"])) throw RowFail("医保参保地为市，【市医保缴费基数】不能为空")
                else {
                    val v = toNum(rec["aaa302_2"])
                    if (v != null && v.compareTo(Z) == 0) throw RowFail("【市医保缴费基数】不能为零")
                }
            }
        }

        // ---- 工伤: PB 同构+80 旁路; 80 退休豁免空值检查(同时为空/属地空两处)
        val pb = s(t["atc90pb"]); val a902 = bd(t["aaa902"])
        if ((pb == "0" && (a902 == null || a902.toLong() > y01)) || (pb == "2" && rr == "3") || e80) {
            if (empty(rec["aaa305_1"]) && empty(rec["aaa305_2"]) && !e80)
                throw RowFail("【省工伤缴费基数】和【市工伤缴费基数】不能同时为空")
            val reg = substr34(s(t["aaa130"]))
            if (reg == "99") {
                if (empty(rec["aaa305_1"]) && !e80) throw RowFail("工伤参保地为省，【省工伤缴费基数】不能为空")
                else {
                    val v = toNum(rec["aaa305_1"])
                    if (v != null && v.compareTo(Z) == 0) throw RowFail("【省工伤缴费基数】不能为零")
                }
            } else if (reg != null) {
                if (empty(rec["aaa305_2"]) && !e80) throw RowFail("工伤参保地为市，【市工伤缴费基数】不能为空")
                else {
                    val v = toNum(rec["aaa305_2"])
                    if (v != null && v.compareTo(Z) == 0) throw RowFail("【市工伤缴费基数】不能为零")
                }
            }
        }

        // ---- 公积金: W 同构无 80 旁路; 【整数校验活跃】(四险种唯一) + 比例校验
        val w = s(t["atc90w"]); val a502 = bd(t["aaa502"])
        if ((w == "0" && (a502 == null || a502.toLong() > y01)) || (w == "2" && rr == "3")) {
            if (empty(rec["aaa303_1"]) && empty(rec["aaa303_2"]))
                throw RowFail("【省公积金缴费基数】和【市公积金缴费基数】不能同时为空")
            val reg = substr34(s(t["aaa120"]))
            if (reg == "99") {
                if (empty(rec["aaa303_1"])) throw RowFail("公积金参保地为省，【省公积金缴费基数】不能为空")
                else {
                    val v = toNum(rec["aaa303_1"])!!
                    if (v.compareTo(Z) == 0) throw RowFail("【省公积金缴费基数】不能为零")
                    if (v.stripTrailingZeros().scale() > 0 && !e80)
                        throw RowFail("【省公积金缴费基数】必须为整数")
                }
            } else if (reg != null) {
                if (empty(rec["aaa303_2"])) throw RowFail("公积金参保地为市，【市公积金缴费基数】不能为空")
                else {
                    val v = toNum(rec["aaa303_2"])!!
                    if (v.compareTo(Z) == 0) throw RowFail("【市公积金缴费基数】不能为零")
                    if (v.stripTrailingZeros().scale() > 0 && !e80)
                        throw RowFail("【市公积金缴费基数】必须为整数")
                }
            }
            // 比例必填 V_MSG 置后【无 RAISE=源活性缺陷忠实】: 先记下, 比例格式两查后再败
            val mustFill = if (empty(rec["atb90s_dw"]) || empty(rec["atb90s_gr"]))
                "【公积金缴存比列】必须填写" else null
            val dwPart = ratioIntPart(rec["atb90s_dw"])
            if (dwPart != null && !inRatio(dwPart)) throw RowFail("【公积金单位缴存比列】填写不正确")
            val grPart = ratioIntPart(rec["atb90s_gr"])
            if (grPart != null && !inRatio(grPart)) throw RowFail("【公积金个人缴存比列】填写不正确")
            if (mustFill != null) throw RowFail(mustFill)
        }
        // (AA98 上下限校验整块被注释=死键不移植)

        // ---- 全过: TC90 富化字段整行写回(源 UPDATE SET ROW=REC 净效果) + ACE10A='3'
        jdbc.update(
            """UPDATE aa97_bp SET atc900=?, atb900=?, aab001=?, aab004=?, aaa030=?, aaa031=?,
               aaa301=?, aaa302=?, aaa303=?, aac001=?,
               aaa100=?, aaa101=?, aaa001=?, aaa004=?, aaa005=?, aaa009=?,
               aaa110=?, aaa111=?, aaa120=?, aaa121=?, aaa130=?, aaa131=?,
               aab001_1=NULL, aab001_2=NULL, aab001_3=NULL, ace10a='3', ace10b=NULL WHERE id=?""",
            maxAtc, nm(t["atb900"]), aab001, s(t["aab004"]), nm(head["aaa030"]), s(head["aaa031"]),
            numStr(bd(t["atc905"])), numStr(bd(t["aaa302"])), numStr(bd(t["aaa303"])), nm(t["aac001"]),
            s(t["aaa100"]), s(t["aaa101"]), s(t["aaa001"]), s(t["aaa004"]), s(t["aaa005"]), s(t["aaa009"]),
            s(t["aaa110"]), s(t["aaa111"]), s(t["aaa120"]), s(t["aaa121"]), s(t["aaa130"]), s(t["aaa131"]),
            id)
    }

    // ==================== PRC_基数导入提交 ====================

    @Transactional
    fun tijiao(ace100: String, aae011: String): Map<String, Any?> = try {
        val ace100N = ace100.toLongOrNull()
            ?: throw JishuErr("派遣员工工资基数导入提交出错，错误信息：ORA-01722: 无效数字，请将错误信息发给维护工程师")
        val user = loadUser(aae011)
        val rows = jdbc.queryForList(
            "SELECT * FROM aa97_bp WHERE ace10a IN ('1','3') AND ace100=? ORDER BY id", ace100N)
        var committed = 0
        var failed = 0
        for (rec in rows) {
            val id = nm(rec["id"]) ?: continue
            try {
                tijiaoRow(rec, user)
                jdbc.update("UPDATE aa97_bp SET ace10a='1' WHERE id=?", id)
                committed++
            } catch (e: RowFail) {
                jdbc.update("UPDATE aa97_bp SET ace10a='0', ace10b=? WHERE id=?", e.msg, id)
                failed++
            }
        }
        mapOf("appCode" to 1, "errMsg" to null as String?,
            "rows" to rows.size, "committed" to committed, "failed" to failed)
    } catch (e: JishuErr) {
        throw e
    } catch (e: Exception) {
        throw JishuErr("派遣员工工资基数导入提交出错，错误信息：${e.cause?.message ?: e.message}，请将错误信息发给维护工程师")
    }

    /** 提交单行: AA97 INSERT(唯一键撞→UPDATE); 败→抛 RowFail(调用方置 '0') */
    private fun tijiaoRow(rec: Map<String, Any?>, user: Array<String?>) {
        /** TO_NUMBER 致命语义(同校验): VALUE_ERROR 穿内层→整过程报错 */
        fun toNum(v: Any?): BigDecimal? = when {
            v == null || (v is String && v.trim().isEmpty()) -> null
            v is Number -> BigDecimal(v.toString())
            else -> try { BigDecimal((v as String).trim()) } catch (e: NumberFormatException) {
                throw JishuErr("派遣员工工资基数导入提交出错，错误信息：ORA-06502: PL/SQL: " +
                    "numeric or value error: character to number conversion error，请将错误信息发给维护工程师")
            }
        }

        val atc900 = nm(rec["atc900"])
        val t: Map<String, Any?>? = atc900?.let {
            jdbc.queryForList("SELECT * FROM tc90 WHERE atc900=?", it).firstOrNull()
        }
        if (t == null) throw RowFail("未查询到合同信息")

        // 三序列发号(源一条 SELECT INTO FROM DUAL, PG 逐序列等价)
        val aaa970 = jdbc.queryForObject("SELECT nextval('seq_aa97_aaa970')", Long::class.java)!!
        val baz001 = jdbc.queryForObject("SELECT nextval('seq_sys_baz001')", Long::class.java)!!
        val baz002 = jdbc.queryForObject("SELECT nextval('seq_sys_baz002')", Long::class.java)!!

        // 申报基数: 社保 301_1 无属地拆分; 医保/工伤/公积金按属地 3-4 位=99 取 _1 否则 _2
        // (属地 NULL 走 ELSE 分支=源三值忠实)
        val a301 = bd0(toNum(rec["aaa301_1"]))
        val a302 = bd0(toNum(if (substr34(s(rec["aaa110"])) == "99") rec["aaa302_1"] else rec["aaa302_2"]))
        val a305 = bd0(toNum(if (substr34(s(rec["aaa130"])) == "99") rec["aaa305_1"] else rec["aaa305_2"]))
        val a303 = bd0(toNum(if (substr34(s(rec["aaa120"])) == "99") rec["aaa303_1"] else rec["aaa303_2"]))
        // 比例: Oracle varchar→NUMBER 列隐式转换, 非法数字在 INSERT 处败=逐行失败(非致命)
        fun ratioNum(v: Any?): BigDecimal? = when {
            v == null || (v is String && v.trim().isEmpty()) -> null
            v is Number -> BigDecimal(v.toString())
            else -> try { BigDecimal((v as String).trim()) } catch (e: NumberFormatException) {
                throw RowFail("插入工资基数表失败,ORA-01722: 无效数字")
            }
        }
        val dwNum = ratioNum(rec["atb90s_dw"])
        val grNum = ratioNum(rec["atb90s_gr"])
        // (源 R_TC90.ATB90S 比例串赋值后 UPDATE TC90 被注释=死赋值, 不移植)

        // 铁律4: PG 事务任一语句失败即毒化, 不能照搬 Oracle「DUP_VAL_ON_INDEX 再捕续跑」——
        // 先探测 UK(AAE001+AAC001, V16 复刻 Oracle UK_AA97), 命中走 UPDATE 分支。
        // AAE214_k<>'1' 才覆写(SQL 三值: NULL 行走 ELSE=不覆写, 源活性语义;
        // 比例两列同挂 AAE214_3=源 quirks; WHERE AAE001+ATC900 换合同人匹配 0 行静默=忠实)
        val dup = jdbc.queryForObject(
            "SELECT count(*) FROM aa97 WHERE aae001=? AND aac001=?",
            Int::class.java, nm(rec["aae001"]), nm(rec["aac001"]))!! > 0
        if (dup) {
            // (源 dup-UPDATE 无内层处理器, 出错穿外层=过程级致命, 不另捕)
            jdbc.update(
                """UPDATE aa97 SET atc900=?,        atb930=?,       atb931=?,   aab001=?,
                   aab004=?,        aaa030=?,       aaa031=?,   aaa300=?,
                   aaa301_1=(CASE WHEN aae214_1<>'1' THEN ? ELSE aaa301_1 END),
                   aaa302_1=(CASE WHEN aae214_2<>'1' THEN ? ELSE aaa302_1 END),
                   aaa303_1=(CASE WHEN aae214_3<>'1' THEN ? ELSE aaa303_1 END),
                   aaa305_1=(CASE WHEN aae214_4<>'1' THEN ? ELSE aaa305_1 END),
                   atb90s_dw=(CASE WHEN aae214_3<>'1' THEN ? ELSE atb90s_dw END),
                   atb90s_gr=(CASE WHEN aae214_3<>'1' THEN ? ELSE atb90s_gr END),
                   aae011=?,        aae017=?,       aae036=now(),
                   aae019=?,        aae020=?,       aae022=?,   aae013=NULL
                   WHERE aae001=?   AND atc900=?""",
                /*atc900*/ atc900, /*atb930*/ nm(rec["atb930"]), /*atb931*/ s(rec["atb931"]),
                /*aab001*/ nm(rec["aab001"]), /*aab004*/ s(rec["aab004"]),
                /*aaa030*/ nm(rec["aaa030"]), /*aaa031*/ s(rec["aaa031"]),
                /*aaa300*/ toNum(rec["aaa300"]),
                /*aaa301_1*/ a301, /*aaa302_1*/ a302, /*aaa303_1*/ a303, /*aaa305_1*/ a305,
                /*atb90s_dw*/ dwNum, /*atb90s_gr*/ grNum,
                /*aae011*/ user[0], /*aae017*/ user[1],
                /*aae019*/ user[2], /*aae020*/ user[3], /*aae022*/ user[4],
                /*WHERE aae001*/ nm(rec["aae001"]), /*atc900*/ atc900)
        } else {
            try {
                // 铁律5: 53 列逐列对位(aae036=now() 内联, AAE214×4/AAE100/AAE013 字面量, 余 46 ?)
                jdbc.update(
                    """INSERT INTO aa97 (baz001,       baz002,       aaa970,      atc900,
                        atb930,        atb931,        aab001,      aab004,
                        aaa030,        aaa031,        aac001,      aac003,
                        aac002,        aae001,        aaa001,      aaa004,
                        aaa005,        aaa009,        aaa100,      aaa101,
                        aaa110,        aaa111,        aaa120,      aaa121,
                        aaa130,        aaa131,        aaa300,      aaa301,
                        aaa302,        aaa303,        aaa305,      aaa301_1,
                        aaa302_1,      aaa305_1,      aaa303_1,    atb90s_dw,
                        atb90s_gr,     aae214_1,      aae214_2,    aae214_3,
                        aae214_4,      atc90p,        atc90pa,     atc90w,
                        atc90pb,       aae100,        aae013,      aae011,
                        aae017,        aae036,        aae019,      aae020,
                        aae022)
                        VALUES (?,            ?,            ?,           ?,
                                ?,            ?,            ?,           ?,
                                ?,            ?,            ?,           ?,
                                ?,            ?,            ?,           ?,
                                ?,            ?,            ?,           ?,
                                ?,            ?,            ?,           ?,
                                ?,            ?,            ?,           ?,
                                ?,            ?,            ?,           ?,
                                ?,            ?,            ?,           ?,
                                ?,            '0',          '0',         '0',
                                '0',          ?,            ?,           ?,
                                ?,            '0',          NULL,        ?,
                                ?,            now(),        ?,           ?,
                                ?)""",
                    /*baz001*/ baz001, /*baz002*/ baz002, /*aaa970*/ aaa970, /*atc900*/ atc900,
                    /*atb930*/ nm(rec["atb930"]), /*atb931*/ s(rec["atb931"]),
                    /*aab001*/ nm(rec["aab001"]), /*aab004*/ s(rec["aab004"]),
                    /*aaa030*/ nm(rec["aaa030"]), /*aaa031*/ s(rec["aaa031"]),
                    /*aac001*/ nm(rec["aac001"]), /*aac003*/ s(rec["aac003"]),
                    /*aac002*/ s(rec["aac002"]), /*aae001*/ nm(rec["aae001"]),
                    /*aaa001*/ s(rec["aaa001"]), /*aaa004*/ s(rec["aaa004"]),
                    /*aaa005*/ s(rec["aaa005"]), /*aaa009*/ s(rec["aaa009"]),
                    /*aaa100*/ s(rec["aaa100"]), /*aaa101*/ s(rec["aaa101"]),
                    /*aaa110*/ s(rec["aaa110"]), /*aaa111*/ s(rec["aaa111"]),
                    /*aaa120*/ s(rec["aaa120"]), /*aaa121*/ s(rec["aaa121"]),
                    /*aaa130*/ s(rec["aaa130"]), /*aaa131*/ s(rec["aaa131"]),
                    /*aaa300*/ toNum(rec["aaa300"]), /*aaa301*/ bd0(toNum(rec["aaa301"])),
                    /*aaa302*/ bd0(toNum(rec["aaa302"])), /*aaa303*/ bd0(toNum(rec["aaa303"])),
                    /*aaa305*/ bd0(toNum(rec["aaa305"])), /*aaa301_1*/ a301,
                    /*aaa302_1*/ a302, /*aaa305_1*/ a305, /*aaa303_1*/ a303,
                    /*atb90s_dw*/ dwNum, /*atb90s_gr*/ grNum,
                    // aae214_1..4 = '0','0','0','0'
                    /*atc90p*/ s(t["atc90p"]), /*atc90pa*/ s(t["atc90pa"]),
                    /*atc90w*/ s(t["atc90w"]), /*atc90pb*/ s(t["atc90pb"]),
                    // aae100='0', aae013=NULL
                    /*aae011*/ user[0], /*aae017*/ user[1],
                    // aae036=now()
                    /*aae019*/ user[2], /*aae020*/ user[3], /*aae022*/ user[4])
            } catch (e: Exception) {
                throw RowFail("插入工资基数表失败,${e.cause?.message ?: e.message}")
            }
        }
    }

    private fun loadUser(aae011: String): Array<String?> =
        jdbc.query(
            "SELECT aae011, aae017, aae019, aae020, aae022 FROM sys_org_user_unit WHERE aae011=?",
            { rs, _ -> arrayOf(rs.getString("aae011"), rs.getString("aae017"), rs.getString("aae019"),
                rs.getString("aae020"), rs.getString("aae022")) }, aae011).firstOrNull()
            ?: throw JishuErr("查询经办用户出错")
}

// ==================== /proc 注册面 ====================

@Component
class Jishu5JiaoyanProc(private val c: Jishu5ImportCases) : ProcHandler {
    override fun procName() = "PKG_缴费基数.PRC_基数导入校验"
    override fun handle(params: Map<String, Any?>): Map<String, Any?> {
        val ace100 = params["ace100"]?.toString() ?: return mapOf("appCode" to -1, "errMsg" to "缺少 ace100")
        val aae001 = params["aae001"]?.toString() ?: return mapOf("appCode" to -1, "errMsg" to "缺少 aae001")
        return try {
            c.jiaoyan(ace100, aae001)
        } catch (e: JishuErr) {
            mapOf("appCode" to -1, "errMsg" to e.m)
        }
    }
}

@Component
class Jishu5TijiaoProc(private val c: Jishu5ImportCases) : ProcHandler {
    override fun procName() = "PKG_缴费基数.PRC_基数导入提交"
    override fun handle(params: Map<String, Any?>): Map<String, Any?> {
        val ace100 = params["ace100"]?.toString() ?: return mapOf("appCode" to -1, "errMsg" to "缺少 ace100")
        val aae011 = params["aae011"]?.toString() ?: return mapOf("appCode" to -1, "errMsg" to "缺少 aae011")
        return try {
            c.tijiao(ace100, aae011)
        } catch (e: JishuErr) {
            mapOf("appCode" to -1, "errMsg" to e.m)
        }
    }
}
