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
 * W6 散件收口（2026-11-18, PKG_保险人员增减 L6952-7468/9267-9325 + PKG_人才派遣公共
 * L1355-1677; 全源 INVALID → 逐过程按语义直译, 铁律1 无双轨项: 均为短过程无编译态伪影面）。
 *
 * ① PRC_异议处理(id=tc90yyid, aae011, flag): TC90_YY 状态机+经办件套。
 *    flag=1 反馈(BACKSIGN 0→1; 异议行 SAFESIGN='2'且属地市→TC9W1.ABP017='1');
 *    flag=2 取消反馈(1→0; INSIGN='1' 拒; ABP017='0');
 *    flag=3 转入(BACKSIGN='1' 前置, INSIGN 0→1+审核六件套; SAFESIGN: 1→TC90.ATC90P=NULL,
 *    2→TC9W1.ABP016='0' 且属地省→ATC90PA=NULL, 3→ATC90PB=NULL, 4→ATC90W=NULL);
 *    flag=4 取消转入(CBSIGN='2' 前置, INSIGN='1' 前置→0; SAFESIGN: 1→ATC90P='2',
 *    2→TC9W1.ABP016='2' + **ATC90PA='2' 无条件**, 3→ATC90PB='2', 4→ATC90W='2')。
 * ② PRC_异议离职同步(): TC90_YY CBSIGN='2' 且 ATC900 NOT IN(47241,35825,35614)(硬编码
 *    排除=源活性补丁忠实) 且 tc90.ATB90R='5'(离职/终止) → CBSIGN='9'。源含 COMMIT——
 *    新系统 /proc 事务化统一提交, 语义等价。tdcf/tdcg 查询隐式触发(doQuery 首步调用)。
 * ③ PRC_医保异议同步(aae002,aaa030,aaa100): TC9W1 扫描(ATC9W1<>'3'); ABP016='0'→
 *    TC90_YY.INSIGN='1'(safesign='2') + TC90.ATC90PA/AAA608=NULL(WHERE ATC90PA='2');
 *    ABP016='2'→参保异议赋值(复用 TdckCases.objectionAssign, 忽略返回=源覆盖语义)
 *    + TC90.ATC90PA='2'/AAA608=年月(WHERE ATC90PA IS NULL)。
 * ④ PRC_社保退休停保(atc900,aaa204): 查合同(失败'查询员工信息出错')+年月格式校验
 *    (yyyymm, 失败'停保年月格式不正确')→TC90.ATC90P='1'/AAA204=AAA209=停保年月。
 * ⑤ PRC_医保退休停保(atc900,aaa604): 同构校验; 属地市(SUBSTR(AAA110,3,2)<>'99')→
 *    ATC90PA='1', 省→ATC90PA='0'; AAA602/604/609=停保年月; AAC008 追加 ',80'
 *    (null/='80'→'80'; 不含'80'→||',80'; 含→原值)。
 * ⑥ PRC_变更参保地(atc900,xz,aaa020): 守卫链(XZ∈1/2/3; AAA020 空→格式错; AA12 校验;
 *    各线: 属地未变/未选参保(AAA40x<>'1')/未参保成功(状态列<>'0')三拒)→TC93 当月结算守卫
 *    ('{月}单位已经结算，请下月再办理')→旧合同 ATB90R='5'+AAE003=当月+跨险种状态列
 *    '0'→'4'(源顺序语义: 状态列改后年月列 CASE 读改后值→恒 ELSE=年月实际不更新, 忠实)→
 *    UPDATE 旧合同 11 列→新合同全行拷贝+变更后字段(属地/AAA201,203 组/状态列 NULL)→
 *    SEQ_TC90_ATC900 发号 INSERT(失败'新增合同信息出错')。tc90 在同步集合: 新合同 PG 发号
 *    1e7+ 不与同步撞, 旧合同 UPDATE=存量行「退役后启用」语义。
 * ⑦ PRC_保险补收基数_个人(aae002,atc900,aae011)（公共包版=前端调用版; 补退包版差异:
 *    多注释块+AA97 年度基数回查, 不移植）: 三段扫描×按年分段写 TC08——
 *    社保(aaa401/申报 aaa201|实参 aaa203,aaa208/ATC90P/基数=ATC905, AAA000='1'),
 *    医保(aaa402/aaa601,603,608/ATC90PA/**仅省 substr(AAA110,3,2)='99'**/基数=AAA302,
 *    '2'——段内错误文案沿用"社保…"=copy-paste 忠实),
 *    公积金(aaa407/aaa501,503,508/ATC90W/基数=AAA303, '3'; **无工伤段=源缺席忠实**)。
 *    候选: (申报<补收年月 AND 状态列 NULL AND 申报年<补收年) OR (实参<补收年月 AND
 *    实参年月=补收年月 AND 状态列='0' AND 实参年<年)。分段: 补发起点(D_KSNY=申报年月,
 *    失败'社保补发开始年月格式不正确'文案)→补收年月, 按年 GROUP→每年一条 TC08
 *    (AAE000=年, KSNY/JSNY=该年起止月, 基数 AAA300, 经办六件套, 三序列)→INSERT→
 *    UK_TC08(AAE000,ATC900,AAA000, V19 复刻)命中→静默幂等(铁律4 先探测); OTHERS→
 *    '记录补收基数出错' RETURN。
 * 划走: PRC_计算平均工资(公共包 L1086)→工资域波次(文档 W6 注记)。
 * 补差确认(W2 漏项补齐, 2026-11-19): PRC_保险补差确认 TJ0B2/B3.AAEZZZ 置位。
 * 不移植: PRC_测试(源即名"测试")。
 */
@Component
class Jishu6Cases(
    private val jdbc: JdbcTemplate,
    private val tdck: TdckCases,
) {

    private val ok = 1

    private fun nm(v: Any?): Long? = when (v) {
        null -> null
        is Number -> v.toLong()
        is String -> v.trim().takeIf { it.isNotEmpty() }?.let { BigDecimal(it).toLong() }
        else -> null
    }
    private fun s(v: Any?): String? = v?.toString()

    private fun loadUser(aae011: String, failMsg: String): Array<String?> =
        jdbc.query(
            "SELECT aae011, aae017, aae019, aae020, aae022 FROM sys_org_user_unit WHERE aae011=?",
            { rs, _ -> arrayOf(rs.getString("aae011"), rs.getString("aae017"), rs.getString("aae019"),
                rs.getString("aae020"), rs.getString("aae022")) }, aae011).firstOrNull()
            ?: throw JishuErr(failMsg)

    /** AAC008 追加 ',80' CASE(退休停保) */
    private fun aac008Case(): String =
        """(CASE WHEN aac008 IS NULL OR aac008 = '80' THEN '80'
           WHEN aac008 IS NOT NULL AND instr(aac008, '80') = 0 THEN aac008 || ',80'
           ELSE aac008 END)"""

    // ==================== ① 异议处理 ====================

    @Transactional
    fun yiyiChuli(id: String, aae011: String, flag: String): Map<String, Any?> {
        val user = loadUser(aae011, "查询经办用户出错")
        val idN = id.toLongOrNull() ?: throw JishuErr("查询异议信息出错")
        val yy = jdbc.queryForList("SELECT * FROM tc90_yy WHERE tc90yyid=?", idN).firstOrNull()
            ?: throw JishuErr("查询异议信息出错")
        val atc900 = nm(yy["atc900"])
        val aae002 = nm(yy["aae002"])
        val citySide = substr34(s(yy["aaa020"])) != "99"
        when (flag) {
            "1" -> {
                if (yy["backsign"] != "0") throw JishuErr("被选中人员已经反馈了信息，不需要重复反馈")
                if (citySide && yy["safesign"] == "2")
                    jdbc.update("UPDATE tc9w1 SET abp017='1' WHERE aae002=? AND atc900=?", aae002, atc900)
                jdbc.update(
                    """UPDATE tc90_yy SET backsign='1', aae011=?, aae019=?, aae036=now(),
                       aae017=?, aae020=?, aae022=? WHERE tc90yyid=?""",
                    user[0], user[2], user[1], user[3], user[4], idN)
            }
            "2" -> {
                if (yy["backsign"] != "1") throw JishuErr("被选中人员已经取消反馈信息，不需要重复取消")
                if (yy["insign"] == "1") throw JishuErr("被选中人员已经办理转入，不允许取消反馈")
                if (citySide && yy["safesign"] == "2")
                    jdbc.update("UPDATE tc9w1 SET abp017='0' WHERE aae002=? AND atc900=?", aae002, atc900)
                jdbc.update(
                    """UPDATE tc90_yy SET backsign='0', aae011=?, aae019=?, aae036=now(),
                       aae017=?, aae020=?, aae022=? WHERE tc90yyid=?""",
                    user[0], user[2], user[1], user[3], user[4], idN)
            }
            "3" -> {
                if (yy["backsign"] != "1") throw JishuErr("被选中人员尚未反馈，不允许转入")
                if (yy["insign"] == "1") throw JishuErr("被选中人员已经办理转入，不需要重复操作")
                jdbc.update(
                    """UPDATE tc90_yy SET insign='1', aae111=?, aae119=?, aae136=now(),
                       aae117=?, aae120=?, aae122=? WHERE tc90yyid=?""",
                    user[0], user[2], user[1], user[3], user[4], idN)
                when (yy["safesign"]) {
                    "1" -> jdbc.update("UPDATE tc90 SET atc90p=NULL WHERE atc900=?", atc900)
                    "2" -> {
                        jdbc.update("UPDATE tc9w1 SET abp016='0' WHERE aae002=? AND atc900=?", aae002, atc900)
                        if (!citySide) jdbc.update("UPDATE tc90 SET atc90pa=NULL WHERE atc900=?", atc900)
                    }
                    "3" -> jdbc.update("UPDATE tc90 SET atc90pb=NULL WHERE atc900=?", atc900)
                    "4" -> jdbc.update("UPDATE tc90 SET atc90w=NULL WHERE atc900=?", atc900)
                }
            }
            "4" -> {
                if (yy["cbsign"] != "2") throw JishuErr("被选中人员已参保，不允许取消转入")
                if (yy["insign"] == "0") throw JishuErr("被选中人员未办理转入，不需要取消操作")
                jdbc.update(
                    """UPDATE tc90_yy SET insign='0', aae111=?, aae119=?, aae136=now(),
                       aae117=?, aae120=?, aae122=? WHERE tc90yyid=?""",
                    user[0], user[2], user[1], user[3], user[4], idN)
                when (yy["safesign"]) {
                    "1" -> jdbc.update("UPDATE tc90 SET atc90p='2' WHERE atc900=?", atc900)
                    "2" -> {
                        jdbc.update("UPDATE tc9w1 SET abp016='2' WHERE aae002=? AND atc900=?", aae002, atc900)
                        jdbc.update("UPDATE tc90 SET atc90pa='2' WHERE atc900=?", atc900)
                    }
                    "3" -> jdbc.update("UPDATE tc90 SET atc90pb='2' WHERE atc900=?", atc900)
                    "4" -> jdbc.update("UPDATE tc90 SET atc90w='2' WHERE atc900=?", atc900)
                }
            }
        }
        return mapOf("appCode" to ok, "errMsg" to null as String?)
    }

    // ==================== ② 异议离职同步 ====================

    @Transactional
    fun lizhiTongbu(): Map<String, Any?> {
        // 硬编码排除三合同=源活性补丁忠实
        val rows = jdbc.queryForList(
            """SELECT y.tc90yyid, y.atc900, t.atb90r FROM tc90_yy y
               LEFT JOIN tc90 t ON t.atc900 = y.atc900
               WHERE y.cbsign='2' AND y.atc900 NOT IN (47241, 35825, 35614)""")
        var moved = 0
        for (r in rows) {
            // 源: tc90 缺行→R_TC90=NULL→ATB90R 引用为 NULL→NULL='5' 不成立→跳过(等价)
            if (r["atb90r"] == null) continue
            if (r["atb90r"] == "5") {
                jdbc.update("UPDATE tc90_yy SET cbsign='9' WHERE tc90yyid=?", nm(r["tc90yyid"]))
                moved++
            }
        }
        // 源此处 COMMIT — /proc 事务化统一提交, 语义等价
        return mapOf("appCode" to ok, "errMsg" to null as String?, "moved" to moved)
    }

    // ==================== ③ 医保异议同步 ====================

    @Transactional
    fun yibaoYiyiTongbu(aae002: String, aaa030: String, aaa100: String): Map<String, Any?> {
        val aae002N = aae002.toLongOrNull()
            ?: return mapOf("appCode" to -1, "errMsg" to "出错，错误信息：ORA-01722: 无效数字，请将错误信息发给维护工程师")
        val aaa030N = aaa030.toLongOrNull()
            ?: return mapOf("appCode" to -1, "errMsg" to "出错，错误信息：ORA-01722: 无效数字，请将错误信息发给维护工程师")
        val rows = jdbc.queryForList(
            """SELECT * FROM tc9w1 WHERE atc9w1 <> '3' AND aaa030=? AND aaa100=? AND aae002=?""",
            aaa030N, aaa100, aae002N)
        var synced = 0
        for (r in rows) {
            val atc900 = nm(r["atc900"])
            when (r["abp016"]) {
                "0" -> {
                    jdbc.update(
                        "UPDATE tc90_yy SET insign='1' WHERE safesign='2' AND atc900=?", atc900)
                    jdbc.update(
                        "UPDATE tc90 SET atc90pa=NULL, aaa608=NULL WHERE atc90pa='2' AND atc900=?", atc900)
                }
                "2" -> {
                    // 参保异议赋值(源过程调用, 忽略返回=PRM_CODE 覆盖语义)
                    tdck.objectionAssign(atc900?.toString() ?: "", "2", aae002, "")
                    jdbc.update(
                        "UPDATE tc90 SET atc90pa='2', aaa608=? WHERE atc90pa IS NULL AND atc900=?",
                        aae002N, atc900)
                }
            }
            synced++
        }
        return mapOf("appCode" to ok, "errMsg" to null as String?, "scanned" to synced)
    }

    // ==================== ④⑤ 退休停保 ====================

    @Transactional
    fun shebaoTingbao(atc900: String, aaa204: String): Map<String, Any?> {
        val atc900N = atc900.toLongOrNull() ?: return stopErr("查询员工信息出错")
        val t = jdbc.queryForList("SELECT * FROM tc90 WHERE atc900=?", atc900N).firstOrNull()
            ?: return mapOf("appCode" to -1, "errMsg" to "查询员工信息出错")
        if (parseYm(aaa204) == null) return mapOf("appCode" to -1, "errMsg" to "停保年月格式不正确")
        // R_TC90 仅用于存在性校验(ATC900=自身)
        jdbc.update(
            "UPDATE tc90 SET atc90p='1', aaa204=?, aaa209=? WHERE atc900=?",
            aaa204.toLongOrNull(), aaa204.toLongOrNull(), atc900N)
        return mapOf("appCode" to ok, "errMsg" to null as String?)
    }

    @Transactional
    fun yibaoTingbao(atc900: String, aaa604: String): Map<String, Any?> {
        val atc900N = atc900.toLongOrNull() ?: return mapOf("appCode" to -1, "errMsg" to "查询员工信息出错")
        val t = jdbc.queryForList("SELECT * FROM tc90 WHERE atc900=?", atc900N).firstOrNull()
            ?: return mapOf("appCode" to -1, "errMsg" to "查询员工信息出错")
        if (parseYm(aaa604) == null) return mapOf("appCode" to -1, "errMsg" to "停保年月格式不正确")
        val ym = aaa604.toLongOrNull()
        val citySide = substr34(s(t["aaa110"])) != "99"
        val newState = if (citySide) "1" else "0"
        jdbc.update(
            """UPDATE tc90 SET atc90pa=?, aaa602=?, aaa604=?, aaa609=?, aac008=${aac008Case()}
               WHERE atc900=?""",
            newState, ym, ym, ym, atc900N)
        return mapOf("appCode" to ok, "errMsg" to null as String?)
    }

    private fun stopErr(msg: String): Map<String, Any?> = mapOf("appCode" to -1, "errMsg" to msg)

    // ==================== ⑥ 变更参保地 ====================

    @Transactional
    fun biangengCandi(atc900: String, xz: String, aaa020: String): Map<String, Any?> {
        val atc900N = atc900.toLongOrNull()
            ?: return mapOf("appCode" to -1, "errMsg" to "无法变更参保地，原因是查询变更前合同出错")
        val bgq = jdbc.queryForList("SELECT * FROM tc90 WHERE atc900=?", atc900N).firstOrNull()
            ?: return mapOf("appCode" to -1, "errMsg" to "无法变更参保地，原因是查询变更前合同出错")
        if (xz !in setOf("1", "2", "3"))
            return mapOf("appCode" to -1, "errMsg" to "无法变更参保地，原因是变更险种格式不正确")
        val aa12 = if (aaa020.isNotEmpty()) {
            jdbc.queryForList("SELECT * FROM aa12 WHERE id=?", aaa020).firstOrNull()
                ?: return mapOf("appCode" to -1, "errMsg" to "无法变更参保地，原因是变更参保地格式不正确")
        } else return mapOf("appCode" to -1, "errMsg" to "无法变更参保地，原因是变更参保地格式不正确")
        // 各线三守卫(属地未变/未选参保/未参保成功)
        val (dCol, col40, pCol) = when (xz) {
            "1" -> Triple("aaa100", "aaa401", "atc90p")
            "2" -> Triple("aaa110", "aaa402", "atc90pa")
            else -> Triple("aaa120", "aaa407", "atc90w")
        }
        if (s(aa12["id"]) == s(bgq[dCol]))
            return mapOf("appCode" to -1, "errMsg" to "无法变更参保地，原因是参保地没有变化")
        if (bgq[col40] != "1")
            return mapOf("appCode" to -1,
                "errMsg" to "无法变更参保地，原因是该员工选择不参加" + when (xz) { "1" -> "社保"; "2" -> "医保"; else -> "公积金" })
        if (bgq[pCol] != "0")
            return mapOf("appCode" to -1,
                "errMsg" to "无法变更参保地，原因是该员工" + when (xz) { "1" -> "社保"; "2" -> "医保"; else -> "公积金" } + "未参保成功")
        // TC93 当月结算守卫
        val ymNow = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMM"))
        val settled = jdbc.queryForObject(
            "SELECT count(*) FROM tc93 WHERE atc931=? AND atb930=?",
            Int::class.java, ymNow.toLong(), nm(bgq["atb930"]))!! > 0
        if (settled) return mapOf("appCode" to -1,
            "errMsg" to "无法变更参保地，原因是${ymNow}单位已经结算，请下月再办理")

        // 旧合同(bgq): ATB90R='5'+AAE003=当月+跨险种状态列置'4'
        // (源顺序语义: 状态列改后年月列 CASE 读改后值→恒 ELSE=年月实际不更新, 忠实照抄)
        var r5 = "5"
        var aae003 = ymNow.toLong()
        var pSb = s(bgq["atc90p"]); var a204 = bdL(bgq["aaa204"]); var a209 = bdL(bgq["aaa209"])
        var pYb = s(bgq["atc90pa"]); var a604 = bdL(bgq["aaa604"]); var a609 = bdL(bgq["aaa609"])
        var pGjj = s(bgq["atc90w"]); var a504 = bdL(bgq["aaa504"]); var a509 = bdL(bgq["aaa509"])
        when (xz) {
            "1" -> {
                // 变更前医保 / 变更前公积金(状态列 '0'→'4'; 年月列 CASE 读改后值)
                pYb = if (pYb == "0") "4" else pYb
                a604 = if (pYb == "0") ymNow.toLong() else a604
                a609 = if (pYb == "0") ymNow.toLong() else a609
                pGjj = if (pGjj == "0") "4" else pGjj
                a504 = if (pGjj == "0") ymNow.toLong() else a504
                a509 = if (pGjj == "0") ymNow.toLong() else a509
            }
            "2" -> {
                pSb = if (pSb == "0") "4" else pSb
                a204 = if (pSb == "0") ymNow.toLong() else a204
                a209 = if (pSb == "0") ymNow.toLong() else a209
                pGjj = if (pGjj == "0") "4" else pGjj
                a504 = if (pGjj == "0") ymNow.toLong() else a504
                a509 = if (pGjj == "0") ymNow.toLong() else a509
            }
            "3" -> {
                pSb = if (pSb == "0") "4" else pSb
                a204 = if (pSb == "0") ymNow.toLong() else a204
                a209 = if (pSb == "0") ymNow.toLong() else a209
                pYb = if (pYb == "0") "4" else pYb
                a604 = if (pYb == "0") ymNow.toLong() else a604
                a609 = if (pYb == "0") ymNow.toLong() else a609
            }
        }
        jdbc.update(
            """UPDATE tc90 SET atb90r=?, aae003=?, atc90p=?, aaa204=?, aaa209=?,
               atc90pa=?, aaa604=?, aaa609=?, atc90w=?, aaa504=?, aaa509=? WHERE atc900=?""",
            r5, aae003, pSb, a204, a209, pYb, a604, a609, pGjj, a504, a509, atc900N)
        // 新合同: 全列动态拷贝(information_schema 列序, 零列名风险)+变更后字段覆写。
        // 源 R_TC90_BGH := R_TC90_BGQ 在 ATB90R='5' 赋值之前 → 新行保持原 atb90r。
        val cols = jdbc.queryForList(
            """SELECT column_name FROM information_schema.columns
               WHERE table_name='tc90' AND is_generated='NEVER' AND is_identity='NO'
               ORDER BY ordinal_position""")
            .map { it["column_name"].toString() }
        val src = bgq.toMutableMap()
        src["atc900"] = jdbc.queryForObject("SELECT nextval('seq_tc90_atc900')", Long::class.java)!!
        when (xz) {
            "1" -> {
                src["aaa100"] = s(aa12["id"]); src["aaa101"] = s(aa12["label"])
                src["aaa201"] = ymNow.toLong(); src["aaa203"] = ymNow.toLong()
                src["atc90p"] = null; src["aaa208"] = null
            }
            "2" -> {
                src["aaa110"] = s(aa12["id"]); src["aaa111"] = s(aa12["label"])
                src["aaa601"] = ymNow.toLong(); src["aaa603"] = ymNow.toLong()
                src["atc90pa"] = null; src["aaa608"] = null
            }
            "3" -> {
                src["aaa120"] = s(aa12["id"]); src["aaa121"] = s(aa12["label"])
                src["aaa501"] = ymNow.toLong(); src["aaa503"] = ymNow.toLong()
                src["atc90w"] = null; src["aaa508"] = null
            }
        }
        try {
            val placeholders = cols.joinToString(",") { "?" }
            jdbc.update(
                "INSERT INTO tc90 (${cols.joinToString(",")}) VALUES ($placeholders)",
                *cols.map { src[it] }.toTypedArray())
        } catch (e: Exception) {
            return mapOf("appCode" to -1,
                "errMsg" to "无法变更参保地，原因是新增合同信息出错${e.cause?.message ?: e.message}")
        }
        return mapOf("appCode" to ok, "errMsg" to null as String?, "newAtc900" to src["atc900"])
    }

    private fun bdL(v: Any?): Long? = nm(v)

    // ==================== 补差确认(W2 漏项补齐: 生成侧已移, 确认置位缺) ====================

    /** PRC_保险补差确认(xz: 2医保→TJ0B2 / 3工伤→TJ0B3; flag: 1确认→AAEZZZ='1'/else→'0')。
     *  源 L9326-9405, 两 IF 块同构(仅表名差)+COMMIT→/proc 事务化等价。 */
    @Transactional
    fun buchaQueRen(xz: String, flag: String, aae002: String, aaa030: String, aaa020: String,
                    aae011: String): Map<String, Any?> {
        val user = loadUser(aae011, "查询经办用户出错")
        val table = when (xz) {
            "2" -> "tj0b2"; "3" -> "tj0b3"
            else -> return mapOf("appCode" to -1, "errMsg" to "险种格式不正确")
        }
        jdbc.update(
            """UPDATE $table SET aaezzz=?, aae111=?, aae117=?, aae136=now(),
               aae119=?, aae120=?, aae122=? WHERE aaa030=? AND aaa020=? AND aae002=?""",
            if (flag == "1") "1" else "0",
            user[0], user[1], user[2], user[3], user[4],
            aaa030.toLongOrNull(), aaa020, aae002.toLongOrNull())
        return mapOf("appCode" to ok, "errMsg" to null as String?)
    }

    // ==================== ⑦ 保险补收基数_个人(公共包版) ====================

    /** 三段参数化: col40/申报列/实参列/实参年月列/状态列/基数列/AAA000/属地两列/额外过滤 */
    private data class BsSeg(
        val col40: String, val a201: String, val a203: String, val a208: String,
        val pCol: String, val baseCol: String, val aaa000: String,
        val d1: String, val d2: String, val filter: String,
    )

    @Transactional
    fun bushouJishu(aae002: String, atc900: String, aae011: String): Map<String, Any?> {
        val user = loadUser(aae011, "获取经办人信息失败")
        val aae002N = aae002.toLongOrNull()
            ?: return mapOf("appCode" to -1, "errMsg" to "补发结束年月格式不正确，无法生成补缴基数")
        val atc900N = atc900.toLongOrNull()
            ?: return mapOf("appCode" to -1, "errMsg" to "社保补发开始年月格式不正确，无法生成补缴基数")
        val ymCur = aae002.substring(0, 4) // SUBSTR(PRM_AAE002,1,4)
        val segs = listOf(
            BsSeg("aaa401", "aaa201", "aaa203", "aaa208", "atc90p", "atc905", "1",
                "aaa100", "aaa101", ""),
            BsSeg("aaa402", "aaa601", "aaa603", "aaa608", "atc90pa", "aaa302", "2",
                "aaa110", "aaa111", " AND substr(a.aaa110, 3, 2) = '99'"),
            BsSeg("aaa407", "aaa501", "aaa503", "aaa508", "atc90w", "aaa303", "3",
                "aaa120", "aaa121", ""),
        )
        var generated = 0
        for (seg in segs) {
            val rows = jdbc.queryForList(
                """SELECT a.*, b.aaa030, b.aaa031 FROM tc90 a, ab01 b
                   WHERE a.aab001 = b.aab001 AND ${seg.col40} = '1'
                     AND ((${seg.a201}::numeric < ? AND ${seg.pCol} IS NULL
                         AND substr(${seg.a201}::text, 1, 4)::numeric < ?)
                     OR (${seg.a203}::numeric < ? AND ${seg.a208}::numeric = ? AND ${seg.pCol} = '0'
                         AND substr(${seg.a203}::text, 1, 4) < substr(${seg.a208}::text, 1, 4)))
                     ${seg.filter}
                     AND a.atc900 = ?""",
                aae002N, ymCur.toLong(), aae002N, aae002N, atc900N)
            for (r in rows) {
                val dKs = parseYm(s(r[seg.a201]))
                    ?: return mapOf("appCode" to -1,
                        "errMsg" to "社保补发开始年月格式不正确，无法生成补缴基数")
                val dJs = parseYm(aae002)
                    ?: return mapOf("appCode" to -1,
                        "errMsg" to "补发结束年月格式不正确，无法生成补缴基数")
                // 按年分段(源: 月序列 GROUP BY 年; NF < 补收年才写)
                var cur = dKs
                val byYear = linkedMapOf<Int, MutableList<YearMonth>>()
                while (!cur.isAfter(dJs.minusMonths(1))) {
                    byYear.getOrPut(cur.year) { mutableListOf() }.add(cur)
                    cur = cur.plusMonths(1)
                }
                for ((year, months) in byYear) {
                    if (year >= ymCur.toInt()) continue // 源: NF < SUBSTR(PRM_AAE002,1,4)
                    val ksny = months.first().format(DateTimeFormatter.ofPattern("yyyyMM")).toLong()
                    val jsny = months.last().format(DateTimeFormatter.ofPattern("yyyyMM")).toLong()
                    // 铁律4: 先探测 UK_TC08(AAE000,ATC900,AAA000, V19 复刻)→命中静默幂等
                    val dup = jdbc.queryForObject(
                        "SELECT count(*) FROM tc08 WHERE aae000=? AND atc900=? AND aaa000=?",
                        Int::class.java, year, atc900N, seg.aaa000)!! > 0
                    if (dup) continue
                    val atb931 = jdbc.queryForList(
                        "SELECT atb931 FROM tb93 WHERE atb930=?", nm(r["atb930"])).firstOrNull()
                        ?: return mapOf("appCode" to -1, "errMsg" to "查询结算单位名称出错")
                    try {
                        jdbc.update(
                            """INSERT INTO tc08 (aae002, aae000, atc900, aac001, aac003, aac002,
                               atb930, atb931, aaa020, aaa021, atb900, aab001, aab004,
                               aaa030, aaa031, ksny, jsny, aaa000, aaa300,
                               baz001, baz002, atc080,
                               aae011, aae017, aae036, aae019, aae020, aae022, aae013)
                               VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?,
                                       nextval('seq_sys_baz001'), nextval('seq_sys_baz002'),
                                       nextval('seq_tc08_atc080'), ?, ?, now(), ?, ?, ?, NULL)""",
                            aae002N, year, atc900N, nm(r["aac001"]), s(r["aac003"]), s(r["aac002"]),
                            nm(r["atb930"]), s(atb931), s(r[seg.d1]), s(r[seg.d2]),
                            nm(r["atb900"]), nm(r["aab001"]), s(r["aab004"]),
                            nm(r["aaa030"]), s(r["aaa031"]), ksny, jsny, seg.aaa000,
                            bd(r[seg.baseCol]),
                            user[0], user[1], user[2], user[3], user[4])
                        generated++
                    } catch (e: Exception) {
                        return mapOf("appCode" to -1,
                            "errMsg" to "记录补收基数出错")
                    }
                }
            }
        }
        return mapOf("appCode" to ok, "errMsg" to null as String?, "generated" to generated)
    }

    private fun bd(v: Any?): BigDecimal? = when (v) {
        null -> null
        is Number -> BigDecimal(v.toString())
        is String -> v.trim().takeIf { it.isNotEmpty() }?.let { BigDecimal(it) }
        else -> BigDecimal(v.toString())
    }

    private fun parseYm(v: String?): YearMonth? = try {
        YearMonth.parse(v!!.trim(), DateTimeFormatter.ofPattern("yyyyMM"))
    } catch (e: Exception) {
        null
    }

    private fun substr34(v: String?): String? = v?.takeIf { it.length >= 4 }?.substring(2, 4)
}

// ==================== /proc 注册面 ====================

@Component
class Jishu6YiyiChuliProc(private val c: Jishu6Cases) : ProcHandler {
    override fun procName() = "PKG_保险人员增减.PRC_异议处理"
    override fun handle(params: Map<String, Any?>): Map<String, Any?> {
        val id = params["id"]?.toString() ?: return mapOf("appCode" to -1, "errMsg" to "缺少 id")
        val aae011 = params["aae011"]?.toString() ?: return mapOf("appCode" to -1, "errMsg" to "缺少 aae011")
        val flag = params["flag"]?.toString() ?: return mapOf("appCode" to -1, "errMsg" to "缺少 flag")
        return try { c.yiyiChuli(id, aae011, flag) } catch (e: JishuErr) { mapOf("appCode" to -1, "errMsg" to e.m) }
    }
}

@Component
class Jishu6LizhiTongbuProc(private val c: Jishu6Cases) : ProcHandler {
    override fun procName() = "PKG_保险人员增减.PRC_异议离职同步"
    override fun handle(params: Map<String, Any?>) = try { c.lizhiTongbu() }
    catch (e: JishuErr) { mapOf("appCode" to -1, "errMsg" to e.m) }
}

@Component
class Jishu6YibaoYiyiTongbuProc(private val c: Jishu6Cases) : ProcHandler {
    override fun procName() = "PKG_保险人员增减.PRC_医保异议同步"
    override fun handle(params: Map<String, Any?>): Map<String, Any?> {
        val aae002 = params["aae002"]?.toString() ?: return mapOf("appCode" to -1, "errMsg" to "缺少 aae002")
        val aaa030 = params["aaa030"]?.toString() ?: return mapOf("appCode" to -1, "errMsg" to "缺少 aaa030")
        val aaa100 = params["aaa100"]?.toString() ?: return mapOf("appCode" to -1, "errMsg" to "缺少 aaa100")
        return try { c.yibaoYiyiTongbu(aae002, aaa030, aaa100) }
        catch (e: JishuErr) { mapOf("appCode" to -1, "errMsg" to e.m) }
    }
}

@Component
class Jishu6SbTingbaoProc(private val c: Jishu6Cases) : ProcHandler {
    override fun procName() = "PKG_保险人员增减.PRC_社保退休停保"
    override fun handle(params: Map<String, Any?>): Map<String, Any?> {
        val atc900 = params["atc900"]?.toString() ?: return mapOf("appCode" to -1, "errMsg" to "缺少 atc900")
        val aaa204 = params["aaa204"]?.toString() ?: return mapOf("appCode" to -1, "errMsg" to "缺少 aaa204")
        return try { c.shebaoTingbao(atc900, aaa204) } catch (e: JishuErr) { mapOf("appCode" to -1, "errMsg" to e.m) }
    }
}

@Component
class Jishu6YbTingbaoProc(private val c: Jishu6Cases) : ProcHandler {
    override fun procName() = "PKG_保险人员增减.PRC_医保退休停保"
    override fun handle(params: Map<String, Any?>): Map<String, Any?> {
        val atc900 = params["atc900"]?.toString() ?: return mapOf("appCode" to -1, "errMsg" to "缺少 atc900")
        val aaa604 = params["aaa604"]?.toString() ?: return mapOf("appCode" to -1, "errMsg" to "缺少 aaa604")
        return try { c.yibaoTingbao(atc900, aaa604) } catch (e: JishuErr) { mapOf("appCode" to -1, "errMsg" to e.m) }
    }
}

@Component
class Jishu6BiangengProc(private val c: Jishu6Cases) : ProcHandler {
    override fun procName() = "PKG_保险人员增减.PRC_变更参保地"
    override fun handle(params: Map<String, Any?>): Map<String, Any?> {
        val atc900 = params["atc900"]?.toString() ?: return mapOf("appCode" to -1, "errMsg" to "缺少 atc900")
        val xz = params["xz"]?.toString() ?: return mapOf("appCode" to -1, "errMsg" to "缺少 xz")
        val aaa020 = params["aaa020"]?.toString() ?: ""
        return try { c.biangengCandi(atc900, xz, aaa020) } catch (e: JishuErr) { mapOf("appCode" to -1, "errMsg" to e.m) }
    }
}

@Component
class Jishu6BuchaQueRenProc(private val c: Jishu6Cases) : ProcHandler {
    override fun procName() = "PKG_保险人员增减.PRC_保险补差确认"
    override fun handle(params: Map<String, Any?>): Map<String, Any?> {
        val xz = params["xz"]?.toString() ?: return mapOf("appCode" to -1, "errMsg" to "缺少 xz")
        val flag = params["flag"]?.toString() ?: return mapOf("appCode" to -1, "errMsg" to "缺少 flag")
        val aae002 = params["aae002"]?.toString() ?: return mapOf("appCode" to -1, "errMsg" to "缺少 aae002")
        val aaa030 = params["aaa030"]?.toString() ?: return mapOf("appCode" to -1, "errMsg" to "缺少 aaa030")
        val aaa020 = params["aaa020"]?.toString() ?: return mapOf("appCode" to -1, "errMsg" to "缺少 aaa020")
        val aae011 = params["aae011"]?.toString() ?: return mapOf("appCode" to -1, "errMsg" to "缺少 aae011")
        return try { c.buchaQueRen(xz, flag, aae002, aaa030, aaa020, aae011) }
        catch (e: JishuErr) { mapOf("appCode" to -1, "errMsg" to e.m) }
    }
}

@Component
class Jishu6BushouProc(private val c: Jishu6Cases) : ProcHandler {
    override fun procName() = "PKG_人才派遣公共.PRC_保险补收基数_个人"
    override fun handle(params: Map<String, Any?>): Map<String, Any?> {
        val aae002 = params["aae002"]?.toString() ?: return mapOf("appCode" to -1, "errMsg" to "缺少 aae002")
        val atc900 = params["atc900"]?.toString() ?: return mapOf("appCode" to -1, "errMsg" to "缺少 atc900")
        val aae011 = params["aae011"]?.toString() ?: return mapOf("appCode" to -1, "errMsg" to "缺少 aae011")
        return try { c.bushouJishu(aae002, atc900, aae011) } catch (e: JishuErr) { mapOf("appCode" to -1, "errMsg" to e.m) }
    }
}
