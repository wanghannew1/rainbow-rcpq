package com.jlrc.rcpq.service.td

import com.jlrc.rcpq.service.proc.ProcHandler
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal

/**
 * W5d 未申报清单/名册×4（旧 PKG_缴费基数 L2578-3205, 2026-11-18, tdcq/tdcab/tdcac/tdch
 * 各 dialog_view2 弹层, 调用面=[aae001, aaa030, aaa020]）。AA97_DW(单位清单)/AA97_GR(个人
 * 名册)全不在同步集合可写。
 *
 * 四线同构, 参数化差异表:
 *   线    XZ  状态列    生效列   清单属地列  NOT-EXISTS列  GR.BXZT←  GR.AAA000←  资格40x列
 *   社保  1   atc90p   aaa202  aaa100     aaa301_1      atc90p    aaa001      aaa401
 *   医保  2   atc90pa  aaa602  aaa110     aaa302_1      atc90pa   aaa005      aaa402
 *   公积金 3  atc90w   aaa502  aaa120     aaa303_1      atc90w    aaa004      aaa407
 *   工伤  4   atc90pb  aaa902  **aaa120** aaa305_1      atc90pb   aaa009      aaa404
 *   (**工伤清单属地列用 AAA120=公积金属地, 应为 AAA130 的 copy-paste 旧 bug 忠实保留**;
 *    现网传参=区划代码时 aaa120/aaa130 常同值不可观测, 代码保留)。
 *   名册 4 过程(独立 PL/SQL)无前端调用点, 作为清单内部步骤实现(faithful 结构: weishenbaoGr)。
 *
 * 清单过程: DELETE AA97_DW/AA97_GR(WHERE XZ+AAA030+AAA100+AAE001)→AA12 SELECT INTO
 *   (无 EXCEPTION→AA12 缺行=整过程'出错'忠实)→按单元(DISTINCT ATB930)循环:
 *   候选资格= (P='0' AND (生效列 IS NULL OR 生效列>年||'01'))
 *          OR (40x='1' AND P IS NULL AND ATB90R='3')
 *          OR (P='2' AND ATB90R='3'), 且 AAE00P IN('1','5','7'),
 *          且 AAE002 <= 年-1||'12'(数字减法拼接, 即上年 12 月及以前生效),
 *          且 NOT EXISTS(AA97 WHERE 申报基数>0 AND AAE001=年 AND AAC001=同——已申报者排除)。
 *   每单元: FID 发号→户头信息(AB01.BAE011→经办+子查 AAE019+AAA030/031+TB93 ATB930/931+
 *   AAB001/004, 失败 RAISE→复位 code 跳过该单元=忠实 SkipUnit)→名册(见下)→INSERT DW
 *   (PSUM=名册人数; 失败→整过程'出错' RETURN)。
 * 名册: 按单元逐人插 AA97_GR(CID=SEQ_AA97_GR_CID, FID 关联, BXZT=线状态列(可 null),
 *   AAA000=线编号列, XZ 随线); 任一人失败→'N出错…' 整过程败(新系统=事务回滚零半账, W4d 先例)。
 * 外层 OTHERS→'出错，错误信息：…请将错误信息发给维护工程师'。
 */
@Component
class Jishu5WsbCases(private val jdbc: JdbcTemplate) {

    /** 四线差异表(列名常量, 无注入面); 工伤属地列 aaa120=源 copy-paste bug 忠实 */
    private data class Line(
        val xz: String, val pCol: String, val ymCol: String, val dCol: String,
        val neCol: String, val bxztCol: String, val noCol: String, val col40: String,
    )

    private val lines = mapOf(
        "sb" to Line("1", "atc90p", "aaa202", "aaa100", "aaa301_1", "atc90p", "aaa001", "aaa401"),
        "yb" to Line("2", "atc90pa", "aaa602", "aaa110", "aaa302_1", "atc90pa", "aaa005", "aaa402"),
        "gjj" to Line("3", "atc90w", "aaa502", "aaa120", "aaa303_1", "atc90w", "aaa004", "aaa407"),
        "gs" to Line("4", "atc90pb", "aaa902", "aaa120", "aaa305_1", "atc90pb", "aaa009", "aaa404"),
    )

    private class SkipUnit : Exception("skip")

    private fun nm(v: Any?): Long? = when (v) {
        null -> null
        is Number -> v.toLong()
        is String -> v.trim().takeIf { it.isNotEmpty() }?.let { BigDecimal(it).toLong() }
        else -> null
    }
    private fun s(v: Any?): String? = v?.toString()

    private fun err(msg: String): Map<String, Any?> = mapOf("appCode" to -1, "errMsg" to msg)

    @Transactional
    fun weishenbao(line: String, aae001: String, aaa030: String, aaa020: String): Map<String, Any?> {
        val ln = lines[line] ?: return err("出错，错误信息：未知险种，请将错误信息发给维护工程师")
        val aae001N = aae001.toLongOrNull()
            ?: return err("出错，错误信息：ORA-01722: 无效数字，请将错误信息发给维护工程师")
        val aaa030N = aaa030.toLongOrNull()
            ?: return err("出错，错误信息：ORA-01722: 无效数字，请将错误信息发给维护工程师")
        val prevYm = "${aae001N - 1}12".toLong()
        val y01 = "${aae001N}01".toLong()

        jdbc.update("DELETE FROM aa97_dw WHERE xz=? AND aaa030=? AND aaa100=? AND aae001=?",
            ln.xz, aaa030N, aaa020, aae001N)
        jdbc.update("DELETE FROM aa97_gr WHERE xz=? AND aaa030=? AND aaa100=? AND aae001=?",
            ln.xz, aaa030N, aaa020, aae001N)
        // AA12 SELECT INTO 无 EXCEPTION: 缺行→外层'出错'(忠实)
        val aa12 = jdbc.queryForList("SELECT * FROM aa12 WHERE id=?", aaa020).firstOrNull()
            ?: return err("出错，错误信息：ORA-01403: no data found，请将错误信息发给维护工程师")

        // 按单元循环(清单查询: 资格+户头+属地+生效期+已申报排除)
        val units = jdbc.queryForList(
            """SELECT DISTINCT a.atb930 FROM tc90 a, ab01 b
               WHERE a.aab001 = b.aab001
                 AND (((${ln.pCol} IN ('0') AND (${ln.ymCol} IS NULL OR ${ln.ymCol} > ?))
                     OR (a.${ln.col40} = '1' AND ${ln.pCol} IS NULL AND a.atb90r = '3')
                     OR ((${ln.pCol} = '2') AND a.atb90r = '3'))
                     AND a.aae00p IN ('1', '5', '7'))
                 AND b.aaa030 = ?
                 AND a.${ln.dCol} = ?
                 AND a.aae002 <= ?
                 AND NOT EXISTS (SELECT 1 FROM aa97
                       WHERE ${ln.neCol} > 0 AND aae001 = ? AND aac001 = a.aac001)""",
            y01, aaa030N, aaa020, prevYm, aae001N)
        var unitsOk = 0
        var unitsSkipped = 0
        var totalPersons = 0L
        for (u in units) {
            val atb930 = nm(u["atb930"]) ?: continue
            try {
                // 户头信息(AB01+TB93): 失败→跳过该单元(源 RAISE E_EXCEPTION→复位 code)
                val head = jdbc.queryForList(
                    """SELECT a.bae011 AS aae011,
                       (SELECT aae019 FROM sys_org_user_unit WHERE aae011 = a.bae011) AS aae019,
                       a.aaa030, a.aaa031, b.atb930, b.atb931, a.aab001, a.aab004
                       FROM ab01 a, tb93 b WHERE a.aab001 = b.aab001 AND b.atb930 = ?""",
                    atb930).firstOrNull() ?: throw SkipUnit()
                val fid = jdbc.queryForObject("SELECT nextval('seq_aa97_dw_fid')", Long::class.java)!!
                // 名册(内部过程 weishenbaoGr): 按单元逐人插 AA97_GR
                val persons = jdbc.queryForList(
                    """SELECT * FROM tc90 a
                       WHERE (((${ln.pCol} IN ('0') AND (${ln.ymCol} IS NULL OR ${ln.ymCol} > ?))
                           OR (${ln.col40} = '1' AND ${ln.pCol} IS NULL AND a.atb90r = '3')
                           OR ((${ln.pCol} = '2') AND a.atb90r = '3'))
                           AND a.aae00p IN ('1', '5', '7'))
                         AND a.atb930 = ?
                         AND a.aae002 <= ?
                         AND NOT EXISTS (SELECT 1 FROM aa97
                               WHERE ${ln.neCol} > 0 AND aae001 = ? AND aac001 = a.aac001)""",
                    y01, atb930, prevYm, aae001N)
                var nNum = 0L
                for (p in persons) {
                    try {
                        jdbc.update(
                            """INSERT INTO aa97_gr (cid, fid, aae001, atb930, atb931, aab001, aab004,
                               aaa030, aaa031, aaa100, aaa101, atc900, aac001, aac003, aac002,
                               bxzt, aaa000, xz)
                               VALUES (nextval('seq_aa97_gr_cid'), ?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)""",
                            fid, aae001N, nm(head["atb930"]), s(head["atb931"]),
                            nm(head["aab001"]), s(head["aab004"]), nm(head["aaa030"]), s(head["aaa031"]),
                            s(aa12["id"]), s(aa12["label"]),
                            nm(p["atc900"]), nm(p["aac001"]), s(p["aac003"]), s(p["aac002"]),
                            s(p[ln.bxztCol]), s(p[ln.noCol]), ln.xz)
                        nNum++
                    } catch (e: Exception) {
                        // 源: '${N_NUM}出错，错误信息：…' 整名册败 → 清单 RETURN
                        throw JishuErr("${nNum + 1}出错，错误信息：${e.cause?.message ?: e.message}，请将错误信息发给维护工程师")
                    }
                }
                // DW 行(PSUM=名册人数; 失败→整过程'出错' RETURN)
                try {
                    jdbc.update(
                        """INSERT INTO aa97_dw (fid, aae001, aaa100, aaa101, xz, psum,
                           aae011, aae019, aaa030, aaa031, atb930, atb931, aab001, aab004)
                           VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?)""",
                        fid, aae001N, s(aa12["id"]), s(aa12["label"]), ln.xz, nNum,
                        s(head["aae011"]), s(head["aae019"]), nm(head["aaa030"]), s(head["aaa031"]),
                        nm(head["atb930"]), s(head["atb931"]), nm(head["aab001"]), s(head["aab004"]))
                } catch (e: Exception) {
                    throw JishuErr("出错，错误信息：${e.cause?.message ?: e.message}，请将错误信息发给维护工程师")
                }
                unitsOk++
                totalPersons += nNum
            } catch (e: SkipUnit) {
                unitsSkipped++ // 源: RAISE E_EXCEPTION→PRM_CODE 复位→继续下一单元
            }
        }
        return mapOf("appCode" to 1, "errMsg" to null as String?,
            "units" to unitsOk, "skipped" to unitsSkipped, "persons" to totalPersons)
    }
}

// ==================== /proc 注册面(清单×4; 名册无独立调用面=内部步骤) ====================

private fun wsbParams(params: Map<String, Any?>): Triple<String, String, String>? {
    val aae001 = params["aae001"]?.toString() ?: return null
    val aaa030 = params["aaa030"]?.toString() ?: return null
    val aaa020 = params["aaa020"]?.toString() ?: return null
    return Triple(aae001, aaa030, aaa020)
}

private fun wsbCall(c: Jishu5WsbCases, line: String, params: Map<String, Any?>): Map<String, Any?> {
    val (aae001, aaa030, aaa020) = wsbParams(params)
        ?: return mapOf("appCode" to -1, "errMsg" to "缺少参数(需 aae001/aaa030/aaa020)")
    return try { c.weishenbao(line, aae001, aaa030, aaa020) }
    catch (e: JishuErr) { mapOf("appCode" to -1, "errMsg" to e.m) }
}

@Component
class Jishu5SbWsbProc(private val c: Jishu5WsbCases) : ProcHandler {
    override fun procName() = "PKG_缴费基数.PRC_社保未申报"
    override fun handle(params: Map<String, Any?>) = wsbCall(c, "sb", params)
}

@Component
class Jishu5YbWsbProc(private val c: Jishu5WsbCases) : ProcHandler {
    override fun procName() = "PKG_缴费基数.PRC_医保未申报"
    override fun handle(params: Map<String, Any?>) = wsbCall(c, "yb", params)
}

@Component
class Jishu5GjjWsbProc(private val c: Jishu5WsbCases) : ProcHandler {
    override fun procName() = "PKG_缴费基数.PRC_公积金未申报"
    override fun handle(params: Map<String, Any?>) = wsbCall(c, "gjj", params)
}

@Component
class Jishu5GsWsbProc(private val c: Jishu5WsbCases) : ProcHandler {
    override fun procName() = "PKG_缴费基数.PRC_工伤未申报"
    override fun handle(params: Map<String, Any?>) = wsbCall(c, "gs", params)
}
