package com.jlrc.rcpq.service.td

import com.jlrc.rcpq.service.calc.InsuranceCalcEngine
import com.jlrc.rcpq.service.proc.ProcHandler
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal

/** 重建路径失败信号: 穿越事务边界触发回滚(旧 ProcedureService "指定回滚" 语义) */
class YueErr(val m: String) : RuntimeException(m)

/**
 * W4a 月情况/月转款（旧 PKG_派遣保险月情况, 社保线参考实现, 2026-11-17）。
 *
 * 源: rainbowpq/work/pkg_all/PACKAGE_BODY_PKG_派遣保险月情况.sql
 *   - PRC_社保月情况   L3-84（守卫+已确认跳过+清表+调转款情况）
 *   - PRC_社保转款情况 L85-313（TB93 单元×TC90 在保员工 → 计算险金(JSLB='2') → TJ0A1 明细/TJ0A 汇总）
 *   - PRC_月转款确认   L4019-4184（TABNAME 白名单 → 主表 AAEZZZ='1'+经办六件套, 明细 AAE013='1'）
 *
 * 结构性修正（W4 根因定位, 见 docs/five-insurance-migration.md W4 节）:
 *   - 旧单位循环内裸 COMMIT → 半账残留, 重跑 ORA-00001。新实现全程单事务, 中途失败整体回滚;
 *   - 硬编码个人补缴补丁(aac002=220282199106213512 且 ym=202312 固定金额)为生产数据 hack, 不移植;
 *   - 补缴子计算器 PRC_保险当月补缴(AAA203<AAA208=ym 时触发)W4c 接入, 当前补缴列置 0
 *     (冒烟数据避开补缴态; TdckPaymentDetail 的逐年引擎可复用)。
 */
@Component
class YueqkCases(
    private val jdbc: JdbcTemplate,
    private val calcProcs: com.jlrc.rcpq.service.calc.CalcProcs,
    private val engine: InsuranceCalcEngine,
) {
    private val ok = 1
    private val err = -1

    private fun err(msg: String) = mapOf("code" to err, "msg" to msg)
    private val Z = BigDecimal.ZERO

    private fun pct(v: BigDecimal?): String? = v?.stripTrailingZeros()?.toPlainString() + "%"

    /** PRC_社保月情况(重建入口): 三查 + 减员未审核守卫 + 已确认跳过 + 清表 + 转款明细重建 */
    @Transactional
    fun shebaoYue(aae002: String, aaa020: String, aaa030: String, aae011: String): Map<String, Any?> {
        val ym = aae002.toLongOrNull() ?: return err("社保年月格式不正确|")
        jdbc.query("SELECT id, label FROM aa12 WHERE id=?",
            { rs, _ -> rs.getString("id") }, aaa020).firstOrNull()
            ?: throw YueErr("参保地区划信息不正确")
        val aa03 = jdbc.query(
            "SELECT aaa030, aaa031 FROM aa03 WHERE aaa030=?",
            { rs, _ -> arrayOf(rs.getObject("aaa030"), rs.getString("aaa031")) },
            aaa030.toLong()).firstOrNull() ?: throw YueErr("险金户头信息不正确")
        val user = jdbc.query(
            "SELECT aae011, aae017, aae019, aae020, aae022 FROM sys_org_user_unit WHERE aae011=?",
            { rs, _ -> arrayOf(rs.getString("aae011"), rs.getString("aae017"), rs.getString("aae019"),
                rs.getString("aae020"), rs.getString("aae022")) },
            aae011).firstOrNull() ?: throw YueErr("经办人信息不正确")
        // 守卫: 存在减员未审核(TB91 '23'/'2' + 社保旗标 aae00p 1/5)
        val guard = jdbc.queryForObject(
            """SELECT count(*) FROM tb91 a, tc90 b, ab01 c
               WHERE a.atb91z=b.atc900 AND b.aab001=c.aab001 AND a.atb911='23' AND a.atb912='2'
                 AND b.aae00p IN ('1','5') AND c.aaa030=? AND b.aae003=?""",
            Int::class.java, aaa030.toLong(), ym)
        if (guard > 0) throw YueErr("不符合条件，原因是存在参保人员合同减员信息未审核")
        // 已确认守卫: AAEZZZ='1' 已存在 → 静默成功不重建(忠实)
        val confirmed = jdbc.queryForObject(
            "SELECT count(*) FROM tj0a WHERE aaezzz='1' AND aaa020=? AND aaa030=? AND aae002=?",
            Int::class.java, aaa020, aaa030.toLong(), ym)
        if (confirmed > 0) return mapOf("code" to ok, "msg" to null as String?,
            "skipped" to "已确认月份不重建", "units" to 0, "rows" to 0)
        jdbc.update("DELETE FROM tj0a WHERE aaa020=? AND aaa030=? AND aae002=?",
            aaa020, aaa030.toLong(), ym)
        jdbc.update("DELETE FROM tj0a1 WHERE aaa020=? AND aaa030=? AND aae002=?",
            aaa020, aaa030.toLong(), ym)
        zhuanKuanShebao(ym, aaa020, aa03, user)
        return mapOf("code" to ok, "msg" to null as String?, "units" to 0, "rows" to 0)
    }

    /** PRC_社保转款情况: 单元×员工 → 险金引擎 → TJ0A1/TJ0A。全程单事务(旧单位内裸 COMMIT 修正)。 */
    private fun zhuanKuanShebao(
        ym: Long, aaa020: String, aa03: Array<Any?>, user: Array<String?>,
    ): Map<String, Any?> {
        val units = jdbc.query(
            """SELECT a.atb930, a.atb931, a.aab001, b.aab004 FROM tb93 a JOIN ab01 b ON a.aab001=b.aab001
               WHERE b.aaa030=? ORDER BY a.atb931""",
            { rs, _ -> arrayOf(rs.getObject("atb930"), rs.getString("atb931"),
                rs.getObject("aab001"), rs.getString("aab004")) }, aa03[0])
        var xh = 0
        var detailRows = 0
        for (qy in units) {
            var rs = 0
            var bj = 0
            var atb90k: BigDecimal? = null; var atb90m: BigDecimal? = null
            var atb90p: BigDecimal? = null; var atb90r: BigDecimal? = null
            var bba001 = Z; var bba003 = Z; var baa001 = Z; var baa003 = Z
            var bba001B = Z; var bba003B = Z; var baa001B = Z; var baa003B = Z
            var atj0a5 = Z; var atj0a6 = Z
            val emps = jdbc.queryForList(
                """SELECT * FROM tc90 WHERE aaa401='1' AND aaa100=?
                   AND ((atc90p='0' AND aaa203<=?) OR (atc90p IN ('1','3') AND aaa204>?))
                   AND atb930=?""",
                aaa020, ym, ym, qy[0])
            for (yg in emps) {
                val atc900 = (yg["atc900"] as Number).toLong()
                val (code, result) = engine.calc(yg, ym.toString(), InsuranceCalcEngine.Tc01(jslb = "2"))
                if (code == -1) throw YueErr("${atc900}计算险金信息出错：$result")
                val c = result as InsuranceCalcEngine.Tc01
                // 补缴(AAA203<AAA208=ym 时触发 保险当月补缴) — W4c 接入, 当前置 0(见类注释)
                val bba001b = Z; val bba003b = Z; val baa001b = Z; val baa003b = Z
                val (a5, a6) = jdbc.query(
                    "SELECT coalesce(sum(coalesce(abp002::numeric,0)),0), coalesce(sum(coalesce(abp003::numeric,0)),0) FROM tc9w_bp WHERE atc900=? AND aae002=?",
                    { rs, _ -> arrayOf(rs.getBigDecimal(1), rs.getBigDecimal(2)) }, atc900, ym)
                    .firstOrNull() ?: arrayOf(Z, Z)
                val a3 = (c.bba001 ?: Z).add(c.baa001 ?: Z)
                val a4 = (c.bba003 ?: Z).add(c.baa003 ?: Z)
                val a7 = a3.add(a4)
                val a3b = bba001b.add(baa001b)
                val a4b = bba003b.add(baa003b)
                val a7b = a3b.add(a4b).add(a5).add(a6)
                val a3z = a3.add(a3b).add(a5)
                val a4z = a4.add(a4b).add(a6)
                val a7z = a7.add(a7b)
                val aaa208 = (yg["aaa208"] as Number?)?.toLong()
                val atj0aa = if (aaa208 == ym) "1" else "0"
                if (a7 > Z) rs++
                if (a7b > Z) bj++
                if (a7z > Z) {
                    try {
                        jdbc.update(
                            """INSERT INTO tj0a1 (aae002, atc900, atj000, atb930, atb931, aab001, aab004,
                               aaa030, aaa031, aaa020, aaa021, aaa001, aac001, aac003, aac002, aac009,
                               aac008, aaa301, aaa304, atb90k, atb90m, atb90p, atb90r,
                               bba001, baa001, atj0a3, bba003, baa003, atj0a4, atj0a5, atj0a6, atj0a7,
                               bba001_b, baa001_b, atj0a3_b, bba003_b, baa003_b, atj0a4_b, atj0a7_b,
                               atj0a3_z, atj0a4_z, atj0a7_z, atj0aa)
                               VALUES (?, ?, '1', ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?,
                               ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)""",
                            ym, atc900, qy[0], qy[1], qy[2], qy[3], aa03[0], aa03[1],
                            yg["aaa100"], yg["aaa101"], yg["aaa001"],
                            (yg["aac001"] as Number?)?.toLong(),
                            yg["aac003"], yg["aac002"], yg["aac009"], yg["aac008"],
                            c.aaa301, c.aaa304, pct(c.atb90k), pct(c.atb90m), pct(c.atb90p), pct(c.atb90r),
                            c.bba001 ?: Z, c.baa001 ?: Z, a3, c.bba003 ?: Z, c.baa003 ?: Z, a4,
                            a5, a6, a7, bba001b, baa001b, a3b, bba003b, baa003b, a4b, a7b,
                            a3z, a4z, a7z, atj0aa)
                    } catch (e: Exception) {
                        throw YueErr("记录转款明细信息出错${e.message ?: ""}")
                    }
                    detailRows++
                }
                atb90k = c.atb90k; atb90m = c.atb90m; atb90p = c.atb90p; atb90r = c.atb90r
                bba001 = bba001.add(c.bba001 ?: Z); baa001 = baa001.add(c.baa001 ?: Z)
                bba003 = bba003.add(c.bba003 ?: Z); baa003 = baa003.add(c.baa003 ?: Z)
                bba001B = bba001B.add(bba001b); baa001B = baa001B.add(baa001b)
                bba003B = bba003B.add(bba003b); baa003B = baa003B.add(baa003b)
                atj0a5 = atj0a5.add(a5); atj0a6 = atj0a6.add(a6)
            }
            val a3 = bba001.add(baa001)
            val a4 = bba003.add(baa003)
            val a7 = a3.add(a4)
            val a3b = bba001B.add(baa001B)
            val a4b = bba003B.add(baa003B)
            val a7b = a3b.add(a4b).add(atj0a5).add(atj0a6)
            val a3z = a3.add(a3b).add(atj0a5)
            val a4z = a4.add(a4b).add(atj0a6)
            val a7z = a7.add(a7b)
            if (a7z > Z) {
                xh++
                try {
                    jdbc.update(
                        """INSERT INTO tj0a (aae002, atb930, atb931, aab001, aab004, aaa030, aaa031,
                           aaa020, aaa021, atj000, aaezzz, atj0a2, atj0a2_b, atb90k, atb90m, atb90p, atb90r,
                           bba001, bba003, baa001, baa003, atj0a3, atj0a4, atj0a7,
                           bba001_b, bba003_b, baa001_b, baa003_b, atj0a3_b, atj0a4_b,
                           atj0a5, atj0a6, atj0a7_b, atj0a3_z, atj0a4_z, atj0a7_z, xh,
                           aae011, aae017, aae036, aae019, aae020, aae022)
                           VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, '1', '0', ?, ?, ?, ?, ?, ?,
                           ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?,
                           ?, ?, now(), ?, ?, ?)""",
                        ym, qy[0], qy[1], qy[2], qy[3], aa03[0], aa03[1],
                        aaa020, jdbc.queryForObject("SELECT label FROM aa12 WHERE id=?", String::class.java, aaa020),
                        rs, bj, pct(atb90k), pct(atb90m), pct(atb90p), pct(atb90r),
                        bba001, bba003, baa001, baa003, a3, a4, a7,
                        bba001B, bba003B, baa001B, baa003B, a3b, a4b,
                        atj0a5, atj0a6, a7b, a3z, a4z, a7z, xh,
                        user[0], user[1], user[2], user[3], user[4])
                } catch (e: Exception) {
                    throw YueErr("记录转款汇总信息出错${e.message ?: ""}")
                }
            }
            // 忠实修正注记: 旧此处 COMMIT(单位半账残留根因), 新实现统一事务收口
        }
        return mapOf("code" to ok, "msg" to null as String?,
            "units" to units.size, "rows" to detailRows)
    }

    /** PRC_月转款确认: TABNAME 白名单 → 主表 AAEZZZ='1'+经办六件套, 明细 AAE013='1'。
     *  旧动态分支改显式白名单(波次计划铁律); 未知表名静默成功(旧 END IF 无 ELSE, 忠实);
     *  差异说明调用已被旧注释禁用, 不移植。 */
    @Transactional
    fun queRen(tabname: String, aae002: String, aaa020: String, aaa030: String, aae011: String): Map<String, Any?> {
        val ym = aae002.toLongOrNull() ?: return err("年月格式不正确|")
        val user = jdbc.query(
            "SELECT aae011, aae017, aae019, aae020, aae022 FROM sys_org_user_unit WHERE aae011=?",
            { rs, _ -> arrayOf(rs.getString("aae011"), rs.getString("aae017"), rs.getString("aae019"),
                rs.getString("aae020"), rs.getString("aae022")) },
            aae011).firstOrNull() ?: return err("经办人信息不正确")
        // TABNAME → (主表, 明细表): 旧 IF/ELSIF 六分支显式化
        val tables = mapOf(
            "TJ0A" to "tj0a1", "TJ0B" to "tj0b1", "TJ0C" to "tj0c1",
            "TJ0D" to "tj0d1", "TJ0E" to "tj0e1", "TJ0F" to "tj0f1")
        val main = tabname.trim().uppercase()
        val detail = tables[main] ?: return mapOf("code" to ok, "msg" to null as String?, "confirmed" to 0)
        val n = jdbc.update(
            """UPDATE $main SET aaezzz='1', aae111=?, aae117=?, aae136=now(), aae119=?, aae120=?, aae122=?
               WHERE aaa020=? AND aaa030=? AND aae002=?""",
            user[0], user[1], user[2], user[3], user[4], aaa020, aaa030.toLong(), ym)
        jdbc.update(
            "UPDATE $detail SET aae013='1' WHERE aaa020=? AND aaa030=? AND aae002=?",
            aaa020, aaa030.toLong(), ym)
        return mapOf("code" to ok, "msg" to null as String?, "confirmed" to n)
    }
}

/** ---------- /proc 注册面（PKG_派遣保险月情况, W4a） ---------- */

@Component
class YueqkShebaoProc(private val c: YueqkCases) : ProcHandler {
    override fun procName() = "PKG_派遣保险月情况.PRC_社保月情况"
    override fun handle(params: Map<String, Any?>): Map<String, Any?> {
        for (k in listOf("aae002", "aaa020", "aaa030", "aae011"))
            if (params[k] == null) return mapOf("appCode" to -1, "errMsg" to "缺少 $k")
        val r = try {
            c.shebaoYue(params["aae002"]!!.toString(), params["aaa020"]!!.toString(),
                params["aaa030"]!!.toString(), params["aae011"]!!.toString())
        } catch (e: YueErr) {
            mapOf("code" to -1, "msg" to e.m, "units" to 0, "rows" to 0, "skipped" to null)
        }
        return mapOf("appCode" to r["code"], "errMsg" to r["msg"],
            "units" to r["units"], "rows" to r["rows"], "skipped" to r["skipped"])
    }
}

@Component
class YueqkQueRenProc(private val c: YueqkCases) : ProcHandler {
    override fun procName() = "PKG_派遣保险月情况.PRC_月转款确认"
    override fun handle(params: Map<String, Any?>): Map<String, Any?> {
        for (k in listOf("tabname", "aae002", "aaa020", "aaa030", "aae011"))
            if (params[k] == null) return mapOf("appCode" to -1, "errMsg" to "缺少 $k")
        val r = c.queRen(params["tabname"]!!.toString(), params["aae002"]!!.toString(),
            params["aaa020"]!!.toString(), params["aaa030"]!!.toString(), params["aae011"]!!.toString())
        return mapOf("appCode" to r["code"], "errMsg" to r["msg"], "confirmed" to r["confirmed"])
    }
}
