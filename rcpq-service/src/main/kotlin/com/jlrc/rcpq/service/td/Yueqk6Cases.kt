package com.jlrc.rcpq.service.td

import com.jlrc.rcpq.service.proc.ProcHandler
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.sql.Timestamp

/**
 * W4d-3 月垫付族（旧 PKG_派遣保险月情况 L4882-5999, 2026-11-17, tdce:53 页）。
 * 独立于 Yueqk1~5 — 修订历史教训: 新波次独立新文件, 禁止对已验证文件做行级手术。
 *
 * 源: PACKAGE_BODY_PKG_派遣保险月情况.sql (work/pkg_yueqian_full.sql)
 *   - PRC_月垫付情况(编排器)      L4882-4944  AA03/经办人两查 + 硬序四连调
 *   - PRC_社保月垫付情况          L4945-5071  TTYPE='11', TJ0A 行, XH=取 TJ0A.XH
 *   - PRC_医保月垫付情况          L5072-5396  '21'(省,TJ0B) + '22'(市,TJ0C, 排退休)
 *   - PRC_工伤月垫付情况          L5397-5617  '31'/'32' 按区划 SUBSTR(3,2)='99' 分流
 *   - PRC_公积金月垫付情况        L5618-5999  '41'(TJ0D) + '42'(TJ0E)
 *
 * 编排器硬序(注释原文): 「调用过程顺序不能调整, 因为清欠金额依次要扣除社保、医保、工伤、公积金」
 * — 医保读 '11'、工伤读 '11'+'21,22'、公积金读 '11'+'21,22'+'31,32' 的 TJ0Z.MNUM_SUM。
 *
 * V_TB96 结算判定三分支(四线同构):
 *   ① 无结算记录(0 行或 SELECT INTO 多行=TOO_MANY_ROWS 同炸) → 全额垫付 dfsign='1'/dfmoney=应收
 *   ② ATB96Z IN ('1','2') 欠费 → 全额垫付
 *   ③ ATB96Z='0' 已到账: (BX_ZJ-前序额度) < 应收 → 差额垫付; >= → dfsign='0'/dfmoney=0
 *   忠实怪点: dfmoney 公式 = max(应收-BX_ZJ, 0) — 前序额度只进判据不进金额公式;
 *   ATB96Z 其他值('9')三分支全不中 → 保持 dfsign='0'/dfmoney=NULL。
 * 结构性修正: 旧 INSERT TJ0Z 逐行 + 出错 RETURN(半账) → 单事务 YueErr6 整批回滚;
 *   先删后插同范围(TTYPE+户头+月)与旧一致, pk_tj0z(ttype,aae002,atb930) 两库同构。
 */
class YueErr6(val m: String) : RuntimeException(m)

@Component
class Yueqk6Cases(private val jdbc: JdbcTemplate) {

    private val Z = BigDecimal.ZERO

    /** NULL 保持(省医保 MNUM_SUM=Z+B 无 NVL 的三值逻辑)。v_tb96.bx_zj 系 PG text(忠实 Oracle TO_CHAR), 须收 String */
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

    /** 省医保 MNUM_SUM := Z + B — Oracle 加法 NULL 传染 */
    private fun addN(a: BigDecimal?, b: BigDecimal?): BigDecimal? =
        if (a == null || b == null) null else a.add(b)

    /** V_TB96: 恰好一行→该行; 0 行/多行(NO_DATA_FOUND 与 TOO_MANY_ROWS 都走 WHEN OTHERS→全额垫付)→null */
    private fun vTb96(ym: Long, atb930: Long): Map<String, Any?>? =
        jdbc.queryForList(
            "SELECT * FROM v_tb96 WHERE atc937='1' AND aae002=? AND atb930=? LIMIT 2", ym, atb930)
            .takeIf { it.size == 1 }?.firstOrNull()

    /** 读社保'11'额度: SELECT INTO 单行语义 — 0 行→0, 多行(TOO_MANY_ROWS)→0 忠实 */
    private fun dz11(ym: Long, atb930: Long): BigDecimal =
        jdbc.queryForList("SELECT mnum_sum FROM tj0z WHERE ttype='11' AND aae002=? AND atb930=? LIMIT 2", ym, atb930)
            .takeIf { it.size == 1 }?.firstOrNull()?.let { bd0(it["mnum_sum"]) } ?: Z

    /** 读多 TTYPE 汇总额度(医保/工伤): NVL(SUM(...),0) */
    private fun dzSum(ym: Long, atb930: Long, ttypes: String): BigDecimal =
        jdbc.queryForObject(
            "SELECT COALESCE(SUM(mnum_sum),0) FROM tj0z WHERE aae002=? AND atb930=? AND ttype IN ($ttypes)",
            BigDecimal::class.java, ym, atb930) ?: Z

    /**
     * 垫付判定三分支(旧 IF 链忠实)。mnumSum=应收; tb96=V_TB96 单行或 null; cascade=前序险种已扣额度。
     * 返回 (dfsign, dfmoney)。三值逻辑: mnumSum=NULL 时比较不成立, 分支全不中保持 '0'/NULL。
     */
    private fun judge(mnumSum: BigDecimal?, tb96: Map<String, Any?>?, cascade: BigDecimal): Pair<String, BigDecimal?> {
        if (tb96 == null) return "1" to mnumSum               // ① 无记录 → 全额垫付
        val z = tb96["atb96z"]?.toString()
        val bxzj = bd0(tb96["bx_zj"])
        if (z == "1" || z == "2") return "1" to mnumSum       // ② 欠费 → 全额垫付
        if (z == "0") {                                       // ③ 已到账
            val avail = bxzj.subtract(cascade)
            if (mnumSum != null && avail < mnumSum)
                return "1" to maxOf(mnumSum.subtract(bxzj), Z) // 差额: 只减 BX_ZJ 不减前序额度(忠实)
            if (mnumSum != null && avail >= mnumSum)
                return "0" to Z
        }
        return "0" to null                                    // ATB96Z 其他值('9')/NULL 应收: 忠实不落分支
    }

    /** 业务员 ZZZ011/ZZZ019 按 AB01.BAE011 反查 SYS_ORG_USER_UNIT, 查不到置 NULL(忠实) */
    private fun zzzUser(bae011: String?): Pair<String?, String?> {
        if (bae011 == null) return null to null
        return jdbc.query(
            "SELECT aae011, aae019 FROM sys_org_user_unit WHERE aae011=?",
            { rs, _ -> Pair(rs.getString("aae011"), rs.getString("aae019")) }, bae011)
            .firstOrNull() ?: (null to null)
    }

    /** INSERT TJ0Z 全 41 列显式清单(旧 INSERT INTO TJ0Z VALUES R_TJ0Z 逐列对齐)。 */
    private fun insertZ(
        ym: Long, xh: Long?, u: Map<String, Any?>, aaa030: Long, aaa031: String?,
        ttype: String, pnumZ: Long?, mnumSumZ: BigDecimal?, pnumB: Long?, mnumSumB: BigDecimal?,
        mnumSum: BigDecimal?, user: Array<String?>, dfsign: String, zzz011: String?, zzz019: String?,
        dfmoney: BigDecimal?,
    ) {
        try {
            jdbc.update(
                """INSERT INTO tj0z (aae002, xh, atb930, atb931, aab001, aab004, aaa030, aaa031,
                   aaa020, aaa021, ttype, pnum_z, mnum_sum_z, pnum_b, mnum_sum_b, mnum_sum,
                   aae011, aae017, aae036, aae019, aae020, aae022, aaezzz,
                   aae111, aae117, aae136, aae119, aae120, aae122,
                   dfsign, zzz011, zzz019, dfmoney, dfmsg, dftime,
                   aae311, aae317, aae336, aae319, aae320, aae322)
                   VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,
                           ?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)""",
                ym, xh, nm(u["atb930"]), u["atb931"] as String?, nm(u["aab001"]), u["aab004"] as String?,
                aaa030, aaa031, u["aaa020"] as String?, u["aaa021"] as String?, ttype,
                pnumZ, mnumSumZ, pnumB, mnumSumB, mnumSum,
                user[0], user[1], Timestamp(System.currentTimeMillis()), user[2], user[3], user[4], "0",
                null, null, null, null, null, null,
                dfsign, zzz011, zzz019, dfmoney, null, null,
                null, null, null, null, null, null)
        } catch (e: Exception) {
            throw YueErr6("${u["atb930"]}统计垫付信息出错${e.cause?.message ?: e.message}")
        }
    }

    /** PRC_月垫付情况 编排器: AA03/经办人两查 + 硬序四连调(次序不可调, 清欠级联依赖前序写入)。 */
    @Transactional
    fun dianfu(aae002: String, aaa030: String, aae011: String): Map<String, Any?> {
        val ym = aae002.toLongOrNull() ?: throw YueErr6("年月格式不正确|")
        val aaa030L = aaa030.toLongOrNull() ?: throw YueErr6("险金户头信息不正确")
        val aa03 = jdbc.query("SELECT aaa030, aaa031 FROM aa03 WHERE aaa030=?",
            { rs, _ -> arrayOf(rs.getObject("aaa030"), rs.getString("aaa031")) }, aaa030L)
            .firstOrNull() ?: throw YueErr6("险金户头信息不正确")
        val user = jdbc.query(
            "SELECT aae011, aae017, aae019, aae020, aae022 FROM sys_org_user_unit WHERE aae011=?",
            { rs, _ -> arrayOf(rs.getString("aae011"), rs.getString("aae017"), rs.getString("aae019"),
                rs.getString("aae020"), rs.getString("aae022")) }, aae011).firstOrNull()
            ?: throw YueErr6("经办人信息不正确")
        val aaa031 = aa03[1] as String?
        var n = shebaoDf(ym, aaa030L, aaa031, user)
        n += yibaoDf(ym, aaa030L, aaa031, user)
        n += gongshangDf(ym, aaa030L, aaa031, user)
        n += gjjDf(ym, aaa030L, aaa031, user)
        return mapOf("code" to 1, "msg" to null as String?, "inserted" to n)
    }

    /** PRC_社保月垫付情况: TTYPE='11', TJ0A 已确认行, XH 沿用 TJ0A.XH。 */
    private fun shebaoDf(ym: Long, aaa030: Long, aaa031: String?, user: Array<String?>): Int = try {
        jdbc.update("DELETE FROM tj0z WHERE ttype='11' AND aaa030=? AND aae002=?", aaa030, ym)
        val rows = jdbc.queryForList(
            """SELECT a.xh, a.atb930, a.atb931, a.aab001, a.aab004, a.aaa020, a.aaa021,
               a.atj0a2, a.atj0a7, a.atj0a2_b, a.atj0a7_b, a.atj0a7_z, b.bae011
               FROM tj0a a JOIN ab01 b ON a.aab001=b.aab001
               WHERE a.aaezzz='1' AND a.aaa030=? AND a.aae002=? ORDER BY a.xh NULLS LAST""", aaa030, ym)
        var n = 0
        for (r in rows) {
            val atb930 = nm(r["atb930"]) ?: continue
            // 社保线无级联: 判据 BX_ZJ < ATJ0A7_Z(应收可空, 三值逻辑忠实)
            val (dfsign, dfmoney) = judge(bd(r["atj0a7_z"]), vTb96(ym, atb930), Z)
            val (zzz011, zzz019) = zzzUser(r["bae011"] as String?)
            insertZ(ym, nm(r["xh"]), r, aaa030, aaa031, "11",
                nm(r["atj0a2"]), bd(r["atj0a7"]), nm(r["atj0a2_b"]), bd(r["atj0a7_b"]),
                bd(r["atj0a7_z"]), user, dfsign, zzz011, zzz019, dfmoney)
            n++
        }
        n
    } catch (e: YueErr6) {
        throw e
    } catch (e: Exception) {
        throw YueErr6("统计垫付信息出错${e.cause?.message ?: e.message}")
    }

    /** PRC_医保月垫付情况: '21' 省医保(TJ0B, <=202202 走 ATJ000 分支) + '22' 市医保(TJ0C, 排退休), 级联扣社保。 */
    private fun yibaoDf(ym: Long, aaa030: Long, aaa031: String?, user: Array<String?>): Int = try {
        var xh = 0L
        var n = 0
        // ---- 省医保 ----
        jdbc.update("DELETE FROM tj0z WHERE ttype='21' AND aaa030=? AND aae002=?", aaa030, ym)
        val units = jdbc.queryForList(
            """SELECT DISTINCT a.atb930, a.atb931, a.aab001, a.aab004, a.aaa020, a.aaa021, b.bae011
               FROM tj0b a JOIN ab01 b ON a.aab001=b.aab001
               WHERE a.aaezzz='1' AND a.aaa030=? AND a.aae002=? ORDER BY atb931""", aaa030, ym)
        for (r in units) {
            val atb930 = nm(r["atb930"]) ?: continue
            var pnumZ: Long? = 0L; var mnumZ: BigDecimal? = Z
            var pnumB: Long? = 0L; var mnumB: BigDecimal? = Z
            if (ym <= 202202L) {
                for (b in jdbc.queryForList(
                    """SELECT atj0b2, atj0b5, atj000 FROM tj0b
                       WHERE aaezzz='1' AND aaa030=? AND aae002=? AND atb930=?""",
                    aaa030, ym, nm(r["atb930"]))) {
                    if (b["atj000"]?.toString() == "1") { pnumZ = nm(b["atj0b2"]); mnumZ = bd(b["atj0b5"]) }
                    else { pnumB = nm(b["atj0b2"]); mnumB = bd(b["atj0b5"]) }
                }
            } else {
                for (b in jdbc.queryForList(
                    """SELECT atj0b2, atj0b5, atj0b2_b, atj0b5_b FROM tj0b
                       WHERE aaezzz='1' AND aaa030=? AND aae002=? AND atb930=?""",
                    aaa030, ym, nm(r["atb930"]))) {
                    pnumZ = nm(b["atj0b2"]); mnumZ = bd(b["atj0b5"])
                    pnumB = nm(b["atj0b2_b"]); mnumB = bd(b["atj0b5_b"])
                }
            }
            // 忠实: MNUM_SUM = Z + B 无 NVL(可空传染)
            val mnumSum = addN(mnumZ, mnumB)
            val s11 = dz11(ym, atb930)
            val (dfsign, dfmoney) = judge(mnumSum, vTb96(ym, atb930), s11)
            val (zzz011, zzz019) = zzzUser(r["bae011"] as String?)
            xh += 1
            insertZ(ym, xh, r, aaa030, aaa031, "21", pnumZ, mnumZ, pnumB, mnumB, mnumSum,
                user, dfsign, zzz011, zzz019, dfmoney)
            n++
        }
        // ---- 市医保(排除退休人员单元) ----
        jdbc.update("DELETE FROM tj0z WHERE ttype='22' AND aaa030=? AND aae002=?", aaa030, ym)
        val units2 = jdbc.queryForList(
            """SELECT DISTINCT a.atb930, a.atb931, a.aab001, a.aab004, a.aaa020, a.aaa021, b.bae011
               FROM tj0c a JOIN ab01 b ON a.aab001=b.aab001
               WHERE a.aaezzz='1' AND strpos(a.atb931,'退休人员')=0
                 AND a.aaa030=? AND a.aae002=? ORDER BY atb931""", aaa030, ym)
        for (r in units2) {
            var pnumZ: Long? = 0L; var mnumZ: BigDecimal? = Z
            var pnumB: Long? = 0L; var mnumB: BigDecimal? = Z
            for (c in jdbc.queryForList(
                """SELECT atj0c2, atj0c5, atj000 FROM tj0c
                   WHERE aaezzz='1' AND aaa030=? AND aae002=? AND atb930=?""",
                aaa030, ym, nm(r["atb930"]))) {
                if (c["atj000"]?.toString() == "1") { pnumZ = nm(c["atj0c2"]); mnumZ = bd(c["atj0c5"]) }
                else { pnumB = nm(c["atj0c2"]); mnumB = bd(c["atj0c5"]) }
            }
            val mnumSum = (mnumZ ?: Z).add(mnumB ?: Z)   // 忠实: NVL 双侧
            val atb930 = nm(r["atb930"]) ?: continue
            val s11 = dz11(ym, atb930)
            val (dfsign, dfmoney) = judge(mnumSum, vTb96(ym, atb930), s11)
            val (zzz011, zzz019) = zzzUser(r["bae011"] as String?)
            xh += 1                                       // 省/市共用一个 N_XH 计数器
            insertZ(ym, xh, r, aaa030, aaa031, "22", pnumZ, mnumZ, pnumB, mnumB, mnumSum,
                user, dfsign, zzz011, zzz019, dfmoney)
            n++
        }
        n
    } catch (e: YueErr6) {
        throw e
    } catch (e: Exception) {
        throw YueErr6("统计垫付信息出错${e.cause?.message ?: e.message}")
    }

    /** PRC_工伤月垫付情况: TJ0F 单循环双 TTYPE(区划 SUBSTR(3,2)='99'→'31' 省否则 '32' 市), 级联扣社保+医保。 */
    private fun gongshangDf(ym: Long, aaa030: Long, aaa031: String?, user: Array<String?>): Int = try {
        jdbc.update("DELETE FROM tj0z WHERE ttype IN ('31','32') AND aaa030=? AND aae002=?", aaa030, ym)
        val units = jdbc.queryForList(
            """SELECT DISTINCT a.atb930, a.atb931, a.aab001, a.aab004, a.aaa020, a.aaa021, b.bae011
               FROM tj0f a JOIN ab01 b ON a.aab001=b.aab001
               WHERE a.aaezzz='1' AND a.aaa030=? AND a.aae002=? ORDER BY atb931""", aaa030, ym)
        var xh = 0L
        var n = 0
        for (r in units) {
            val ttype = if ((r["aaa020"] as String?).let { it != null && it.length >= 4 && it.substring(2, 4) == "99" })
                "31" else "32"
            var pnumZ = 0L; var mnumZ = Z; var pnumB = 0L; var mnumB = Z
            if (ym <= 202202L) {
                for (f in jdbc.queryForList(
                    """SELECT atj0f2, atj0f5, atj000 FROM tj0f
                       WHERE aaezzz='1' AND aaa030=? AND aae002=? AND atb930=?""",
                    aaa030, ym, nm(r["atb930"]))) {
                    if (f["atj000"]?.toString() == "1") { pnumZ = nm(f["atj0f2"]) ?: 0; mnumZ = bd0(f["atj0f5"]) }
                    else { pnumB = nm(f["atj0f2"]) ?: 0; mnumB = bd0(f["atj0f5"]) }
                }
            } else {
                for (f in jdbc.queryForList(
                    """SELECT atj0f2, atj0f5, atj0f2_b1, atj0f5_b1, atj0f2_b2, atj0f5_b2 FROM tj0f
                       WHERE aaezzz='1' AND aaa030=? AND aae002=? AND atb930=?""",
                    aaa030, ym, nm(r["atb930"]))) {
                    pnumZ = bd0(f["atj0f2"]).toLong(); mnumZ = bd0(f["atj0f5"])
                    // 忠实: PNUM_B 先按 _B1+_B2 赋值, 紧即被 TJ0F1 正值桶行数覆写(覆写在行循环内)
                    pnumB = bd0(f["atj0f2_b1"]).add(bd0(f["atj0f2_b2"])).toLong()
                    pnumB = jdbc.queryForObject(
                        """SELECT COALESCE(SUM(CASE WHEN atj0f5_b1>0 OR atj0f5_b2>0 THEN 1 ELSE 0 END),0)
                           FROM tj0f1 WHERE aaa030=? AND aae002=? AND atb930=?""",
                        Long::class.java, aaa030, ym, nm(r["atb930"])) ?: 0
                    mnumB = bd0(f["atj0f5_b1"]).add(bd0(f["atj0f5_b2"]))
                }
            }
            val mnumSum = mnumZ.add(mnumB)                // 忠实: NVL 双侧
            val atb930 = nm(r["atb930"]) ?: continue
            val s11 = dz11(ym, atb930)
            val yb = dzSum(ym, atb930, "'21','22'")
            val (dfsign, dfmoney) = judge(mnumSum, vTb96(ym, atb930), s11.add(yb))
            val (zzz011, zzz019) = zzzUser(r["bae011"] as String?)
            xh += 1
            insertZ(ym, xh, r, aaa030, aaa031, ttype, pnumZ, mnumZ, pnumB, mnumB, mnumSum,
                user, dfsign, zzz011, zzz019, dfmoney)
            n++
        }
        n
    } catch (e: YueErr6) {
        throw e
    } catch (e: Exception) {
        throw YueErr6("统计垫付信息出错${e.cause?.message ?: e.message}")
    }

    /** PRC_公积金月垫付情况: '41' 省公积金(TJ0D) + '42' 市公积金(TJ0E), 级联扣社保+医保+工伤; N_XH 跨省/市共用。 */
    private fun gjjDf(ym: Long, aaa030: Long, aaa031: String?, user: Array<String?>): Int = try {
        var xh = 0L
        var n = 0
        // ---- 省公积金 ----
        jdbc.update("DELETE FROM tj0z WHERE ttype='41' AND aaa030=? AND aae002=?", aaa030, ym)
        val units = jdbc.queryForList(
            """SELECT DISTINCT a.atb930, a.atb931, a.aab001, a.aab004, a.aaa020, a.aaa021, b.bae011
               FROM tj0d a JOIN ab01 b ON a.aab001=b.aab001
               WHERE a.aaezzz='1' AND a.aaa030=? AND a.aae002=? ORDER BY atb931""", aaa030, ym)
        for (r in units) {
            var pnumZ: Long? = 0L; var mnumZ: BigDecimal? = Z
            var pnumB: Long? = 0L; var mnumB: BigDecimal? = Z
            for (d in jdbc.queryForList(
                """SELECT atj0d2, atj0d5, atj000 FROM tj0d
                   WHERE aaezzz='1' AND aaa030=? AND aae002=? AND atb930=?""",
                aaa030, ym, nm(r["atb930"]))) {
                if (d["atj000"]?.toString() == "1") { pnumZ = nm(d["atj0d2"]); mnumZ = bd(d["atj0d5"]) }
                else { pnumB = nm(d["atj0d2"]); mnumB = bd(d["atj0d5"]) }
            }
            val mnumSum = (mnumZ ?: Z).add(mnumB ?: Z)
            val atb930 = nm(r["atb930"]) ?: continue
            val cascade = dz11(ym, atb930)
                .add(dzSum(ym, atb930, "'21','22'"))
                .add(dzSum(ym, atb930, "'31','32'"))
            val (dfsign, dfmoney) = judge(mnumSum, vTb96(ym, atb930), cascade)
            val (zzz011, zzz019) = zzzUser(r["bae011"] as String?)
            xh += 1
            insertZ(ym, xh, r, aaa030, aaa031, "41", pnumZ, mnumZ, pnumB, mnumB, mnumSum,
                user, dfsign, zzz011, zzz019, dfmoney)
            n++
        }
        // ---- 市公积金 ----
        jdbc.update("DELETE FROM tj0z WHERE ttype='42' AND aaa030=? AND aae002=?", aaa030, ym)
        val units2 = jdbc.queryForList(
            """SELECT DISTINCT a.atb930, a.atb931, a.aab001, a.aab004, a.aaa020, a.aaa021, b.bae011
               FROM tj0e a JOIN ab01 b ON a.aab001=b.aab001
               WHERE a.aaezzz='1' AND a.aaa030=? AND a.aae002=? ORDER BY atb931""", aaa030, ym)
        for (r in units2) {
            var pnumZ: Long? = 0L; var mnumZ: BigDecimal? = Z
            var pnumB: Long? = 0L; var mnumB: BigDecimal? = Z
            for (e in jdbc.queryForList(
                """SELECT atj0e2, atj0e5, atj000 FROM tj0e
                   WHERE aaezzz='1' AND aaa030=? AND aae002=? AND atb930=?""",
                aaa030, ym, nm(r["atb930"]))) {
                if (e["atj000"]?.toString() == "1") { pnumZ = nm(e["atj0e2"]); mnumZ = bd(e["atj0e5"]) }
                else { pnumB = nm(e["atj0e2"]); mnumB = bd(e["atj0e5"]) }
            }
            val mnumSum = (mnumZ ?: Z).add(mnumB ?: Z)
            val atb930 = nm(r["atb930"]) ?: continue
            val cascade = dz11(ym, atb930)
                .add(dzSum(ym, atb930, "'21','22'"))
                .add(dzSum(ym, atb930, "'31','32'"))
            val (dfsign, dfmoney) = judge(mnumSum, vTb96(ym, atb930), cascade)
            val (zzz011, zzz019) = zzzUser(r["bae011"] as String?)
            xh += 1
            insertZ(ym, xh, r, aaa030, aaa031, "42", pnumZ, mnumZ, pnumB, mnumB, mnumSum,
                user, dfsign, zzz011, zzz019, dfmoney)
            n++
        }
        n
    } catch (e: YueErr6) {
        throw e
    } catch (e: Exception) {
        throw YueErr6("统计垫付信息出错${e.cause?.message ?: e.message}")
    }
}

@Component
class Yueqk6DfProc(private val c: Yueqk6Cases) : ProcHandler {
    override fun procName() = "PKG_派遣保险月情况.PRC_月垫付情况"
    override fun handle(params: Map<String, Any?>): Map<String, Any?> {
        // 旧 tdce_cont.js doCreate 传参: aae002 + aaa030 + aae011(三参, 无 aaa020)
        for (k in listOf("aae002", "aaa030", "aae011"))
            if (params[k] == null) return mapOf("appCode" to -1, "errMsg" to "缺少 $k")
        val r = try {
            c.dianfu(params["aae002"]!!.toString(), params["aaa030"]!!.toString(),
                params["aae011"]!!.toString())
        } catch (e: YueErr6) {
            mapOf("code" to -1, "msg" to e.m, "inserted" to 0)
        }
        return mapOf("appCode" to r["code"], "errMsg" to r["msg"], "inserted" to r["inserted"])
    }
}
