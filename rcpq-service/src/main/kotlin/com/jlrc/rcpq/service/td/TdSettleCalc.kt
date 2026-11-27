package com.jlrc.rcpq.service.td

import com.jlrc.rcpq.service.calc.InsuranceCalcEngine
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.math.RoundingMode

/**
 * ⑥PRC_费用结算 主过程忠实转写(桥单 20261127-034; 源 L749-2405, 1657L)。
 * 行级语义: 内层 BEGIN..EXCEPTION WHEN E_EXCEPTION(合同单元不符)→删行继续;
 * 外层 WHEN OTHERS→'费用结算程序出错...'外壳。子过程/引擎失败原样透传(SettleRet)。
 */
@Component
class TdSettleCalc(
    private val s: TdSettleSupport,
    private val engine: InsuranceCalcEngine,
    private val bbfCases: BbfCases,
    private val rateCases: TdSettleRateCases,
    private val listCases: TdSettleListCases,
) {

    private fun nz(v: Any?): BigDecimal = s.nvl(v)

    @Transactional
    fun feeSettle(atb930: String, atc931: String, atc937: String, atc932: String, aae011: String): Map<String, Any?> {
        return try {
            body(atb930, atc931, atc937, atc932, aae011)
            mapOf("appCode" to 1, "errMsg" to null as String?)
        } catch (e: SettleRet) {
            mapOf("appCode" to e.code, "errMsg" to e.msg)
        } catch (e: Exception) {
            mapOf("appCode" to -1, "errMsg" to "费用结算程序出错，错误信息：${e.message}，请将错误信息发给维护工程师")
        }
    }

    private fun body(atb930: String, atc931: String, atc937: String, atc932: String, aae011: String) {
        // SELECT * INTO 三连(无局部守卫, 失败走外壳)
        val user = s.jdbc.queryForList("SELECT * FROM sys_org_user_unit WHERE aae011=?", aae011).singleOrNull()
            ?: throw RuntimeException("ORA-01403: 未找到任何数据 (SYS_ORG_USER_UNIT)")
        val rTb93 = s.single("tb93", "atb930=?", atb930.toLong())
            ?: throw RuntimeException("ORA-01403: 未找到任何数据 (TB93)")
        val rAb01 = s.single("ab01", "aab001=?", rTb93["aab001"])
            ?: throw RuntimeException("ORA-01403: 未找到任何数据 (AB01)")
        // 已确认守卫
        val nCnt = s.jdbc.queryForObject(
            "SELECT COUNT(1) FROM tc93 WHERE aae214='1' AND atc931=?::numeric AND atc937=? AND atb930=?",
            Long::class.java, atc931.toBigDecimal(), atc937, atb930.toLong()) ?: 0L
        if (nCnt > 0) throw SettleRet(-1, "【${rTb93["atb931"]}】$atc931 月费用清单已确认，无法重新结算")
        val nCntAll = s.jdbc.queryForObject(
            "SELECT COUNT(1) FROM tc93 WHERE atc931=?::numeric AND atc937=? AND atb930=?",
            Long::class.java, atc931.toBigDecimal(), atc937, atb930.toLong()) ?: 0L
        if (nCntAll == 0L) {
            // 源调 PKG_工资发放.PRC_工资花名册(L850, 未移植记账项)——TC93 空单元路径
            throw RuntimeException("PKG_工资发放.PRC_工资花名册 未移植(桥单 20261127-034 记账项)")
        }
        // 待审核守卫
        val nPend = s.jdbc.queryForObject(
            """SELECT COUNT(1) FROM tc90 a, tb91 b WHERE a.atc900=b.atb91z
               AND (b.atb912='2' OR a.atb90r='2') AND a.atb930=?""",
            Long::class.java, atb930.toLong()) ?: 0L
        if (nPend > 0) throw SettleRet(-1, "该结算单元存在待审核员工信息，不符合结算条件")
        try { s.prevYm(atc931) } catch (e: Exception) {
            throw SettleRet(-1, "结算年月格式不正确，无法重新结算")
        }
        // 代理人员无工资先生成 TC93
        listCases.virtualAgency(atb930, atc931, atc937, aae011)
        if (atc937 == "1") {
            listCases.buiTuiHistory(atb930, atc931, atc937, aae011)
            listCases.debtList(atb930, atc931, atc937, aae011)
            listCases.compensation(atb930, atc931, atc937, aae011)
        }
        var nJsq = 0
        val rows = s.jdbc.queryForList(
            "SELECT * FROM tc93 WHERE atc931=?::numeric AND atc937=? AND atb930=? ORDER BY atc900",
            atc931.toBigDecimal(), atc937, atb930.toLong())
        for (rec0 in rows) {
            nJsq += 1
            var nCntBj = BigDecimal.ZERO
            var nAaa922 = BigDecimal.ZERO
            var nDlDw = BigDecimal.ZERO
            var nDlGr = BigDecimal.ZERO
            var nMinus: Long = 0
            val atc931L = atc931.toLong()
            var vTaxAtc93ab = BigDecimal.ZERO
            var vTaxAtc93ad = BigDecimal.ZERO
            var vTaxAtc93r = BigDecimal.ZERO
            var vTaxAtc93ar = BigDecimal.ZERO
            var nAtc93ab1: Long = 0
            var nDylj = BigDecimal.ZERO
            var nQzd: Long = 0
            val rTc93 = rec0.toMutableMap()
            val atc900 = s.long(rec0["atc900"])
            // SELECT * INTO TC90
            val tc90 = s.jdbc.queryForList("SELECT * FROM tc90 WHERE atc900=?", atc900).singleOrNull()
                ?: throw RuntimeException("ORA-01403: 未找到任何数据 (TC90 $atc900)")
            // 个人交现金到账检查(只判足不足, 不落库)
            var x3Left = nz(rTc93["atc93x3"])
            if (x3Left > BigDecimal.ZERO) {
                val books = s.jdbc.queryForList(
                    "SELECT atb973 FROM tb97 WHERE dstatus='1' AND atb973 > 0 AND atb930=? AND aac001=?",
                    rTc93["atb930"], rTc93["aac001"])
                for (b in books) {
                    val bal = nz(b["atb973"])
                    if (bal >= x3Left && x3Left > BigDecimal.ZERO) x3Left = BigDecimal.ZERO
                    else if (bal < x3Left && x3Left > BigDecimal.ZERO) x3Left -= bal
                }
                if (x3Left > BigDecimal.ZERO) throw SettleRet(-1,
                    "不符合结算条件，原因为：${rTc93["aac003"]}个人交现金部分未到账或者余额不足")
            }
            if (s.str(tc90["atb930"]) != s.str(rTc93["atb930"])) {
                s.jdbc.update("DELETE FROM tc93 WHERE atc930=?", rTc93["atc930"])
                continue
            }
            val rates = rateCases.rates(tc90, atc932)
            // 初始化(R_TC93 计算列清零/置源值, 源 L1030-1071 逐列)
            val zeroKeys = listOf("aaa301", "aaa302", "aaa303", "aaa305", "bba001", "bba002",
                "bba003", "bba004", "bba005", "caa001", "atb90t", "atc93bb", "baa001", "baa002",
                "baa003", "caa002", "atc93bc", "atc93bd", "atc93ba", "atc93be", "bba200",
                "baa200", "baa300", "bba300", "atb90a", "atc93aa", "atc93bf", "atc93r",
                "atc93ar", "atc93e", "atc9p3")
            for (k in zeroKeys) rTc93[k] = BigDecimal.ZERO
            rTc93["atc93w"] = "0"
            rTc93["aae00b"] = tc90["aae00b"] ?: nJsq.toLong()
            if (atc937 == "1") {
                rTc93["atc93bb"] = nz(s.jdbc.queryForObject(
                    "SELECT COALESCE(SUM(atc053),0) FROM tc05 WHERE atc054='1' AND aae002=?::numeric AND atc900=?",
                    BigDecimal::class.java, atc931.toBigDecimal(), atc900))
                rTc93["atc93bh"] = nz(s.jdbc.queryForObject(
                    "SELECT COALESCE(SUM(atc053),0) FROM tc05 WHERE atc054='2' AND aae002=?::numeric AND atc900=?",
                    BigDecimal::class.java, atc931.toBigDecimal(), atc900))
            }
            var nDel = s.jdbc.queryForObject(
                """SELECT COUNT(1) FROM tc93 WHERE atc931 >= ?::numeric AND atc931 < ?::numeric
                   AND atc930 <> ? AND atb930=? AND aac001=?""",
                Long::class.java, (s.str(tc90["atc90c"])?.take(10)?.replace("-", "")?.take(6)?.toBigDecimal()
                    ?: BigDecimal.ZERO), atc931.toBigDecimal(), rTc93["atc930"], tc90["atb930"], tc90["aac001"]) ?: 0L
            val agg = s.jdbc.queryForList(
                """SELECT COUNT(1) AS c0,
                   COALESCE(SUM(CASE WHEN COALESCE(bba001,0)+COALESCE(bba003,0)+COALESCE(baa001,0)
                       +COALESCE(baa003,0)+COALESCE(bba004,0) > 0 THEN 1 ELSE 0 END),0) AS c1,
                   COALESCE(SUM(CASE WHEN COALESCE(bba002,0)+COALESCE(baa002,0) > 0 THEN 1 ELSE 0 END),0) AS c2,
                   COALESCE(SUM(CASE WHEN COALESCE(caa001,0)+COALESCE(caa002,0) > 0 THEN 1 ELSE 0 END),0) AS c3
                   FROM tc93 WHERE atc931 < ?::numeric AND atc930 <> ? AND atc900=?""",
                atc931.toBigDecimal(), rTc93["atc930"], atc900).firstOrNull() ?: mapOf()
            val nTc93 = s.long(agg["c0"]) ?: 0L
            val nTc93_1 = s.long(agg["c1"]) ?: 0L
            val nTc93_2 = s.long(agg["c2"]) ?: 0L
            val nTc93_3 = s.long(agg["c3"]) ?: 0L
            nMinus = s.jdbc.queryForObject(
                "SELECT COUNT(DISTINCT aae002) FROM tc93 WHERE atc931 >= ?::numeric AND atc931 < ?::numeric AND aac001=?",
                Long::class.java, (atc931.substring(0, 4) + "01").toBigDecimal(),
                atc931.toBigDecimal(), tc90["aac001"]) ?: 0L
            if ((atc931L == s.long(tc90["aae002"]) || nDel == 0L) && atc937 == "1" &&
                tc90["atc90c"] != null && nTc93 == 0L) {
                val start = s.str(tc90["atc90c"])!!.take(10).replace("-", "").take(6)
                nCntBj = BigDecimal(s.monthsBetween(atc931, start))
                nCntBj = (nCntBj - BigDecimal(nDel)).max(BigDecimal.ZERO)
                if (start.substring(0, 4) != atc931.substring(0, 4) && nCntBj > BigDecimal.ZERO) {
                    nAaa922 = if (atc931.substring(0, 4) < s.str(rec0["aae002"])!!.substring(0, 4)) {
                        BigDecimal(s.monthsBetween(s.str(rec0["aae002"])!!,
                            atc931.substring(0, 4) + "01") + 1)
                    } else {
                        BigDecimal(s.monthsBetween(atc931, atc931.substring(0, 4) + "01") + 1 - nMinus)
                    }
                } else {
                    nAaa922 = nCntBj.add(BigDecimal.ONE)
                }
            }
            if (s.str(tc90["atc90g"]) == "1" && atc937 == "1") {
                // 结算计算险金(引擎等价, 费率自带=征缴路径)
                val xjbl = InsuranceCalcEngine.Tc01(jslb = "1").apply {
                    this.atc900 = atc900
                    atb90k = nz(rates.k); atb90l = nz(rates.l); atb90m = nz(rates.m)
                    atb90n = nz(rates.n); atb90nJ = nz(rates.nJ); atb90o = nz(rates.o)
                    atb90p = nz(rates.p); atb90q = nz(rates.q); atb90r = nz(rates.r)
                    atb90sDw = nz(rates.sDw); atb90sGr = nz(rates.sGr); aaa399 = nz(rates.aaa399)
                }
                val (code1, res1) = engine.calc(tc90, atc931, xjbl)
                if (code1 == -1) throw SettleRet(-1, res1 as? String ?: "计算险金出错")
                val r = res1 as InsuranceCalcEngine.Tc01
                rTc93["aae005"] = tc90["aae005"]
                rTc93["bbb001"] = tc90["bbb001"]; rTc93["bbb002"] = tc90["bbb002"]
                // 停缴当月清零段 / 当月险金赋值段(源 L1189-1300 逐行, 含 AAA404 分支误写 AAA302 笔误忠实)
                if ((s.str(tc90["atc90p"]) == "3" || s.str(tc90["atc90pa"]) == "3" ||
                        s.str(tc90["atc90w"]) == "3") && s.long(tc90["aae003"]) == atc931L) {
                    if (s.str(tc90["atc90p"]) == "3") {
                        rTc93["aaa301"] = BigDecimal.ZERO; rTc93["bba001"] = BigDecimal.ZERO
                        rTc93["bba003"] = BigDecimal.ZERO; rTc93["baa001"] = BigDecimal.ZERO
                        rTc93["baa003"] = BigDecimal.ZERO
                    }
                    if (s.str(tc90["atc90pa"]) == "3") {
                        rTc93["aaa302"] = BigDecimal.ZERO; rTc93["bba002"] = BigDecimal.ZERO
                        rTc93["bba005"] = BigDecimal.ZERO; rTc93["atb90t"] = BigDecimal.ZERO
                        rTc93["atc93bc"] = BigDecimal.ZERO; rTc93["atc93bd"] = BigDecimal.ZERO
                        rTc93["baa002"] = BigDecimal.ZERO
                    }
                    if (s.str(tc90["atc90pb"]) == "3") {
                        rTc93["aaa305"] = BigDecimal.ZERO; rTc93["bba004"] = BigDecimal.ZERO
                    }
                    if (s.str(tc90["atc90w"]) == "3") {
                        rTc93["aaa303"] = BigDecimal.ZERO; rTc93["caa001"] = BigDecimal.ZERO
                        rTc93["caa002"] = BigDecimal.ZERO
                    }
                    rTc93["bba200"] = BigDecimal.ZERO; rTc93["baa200"] = BigDecimal.ZERO
                } else {
                    fun flagOr(k: String): Boolean =
                        tc90[k] == null || s.str(tc90[k]) == "0"
                    rTc93["aaa301"] = if (flagOr("aaa401")) null else r.aaa301
                    rTc93["aaa302"] = if (flagOr("aaa402")) null else r.aaa302
                    rTc93["aaa305"] = if (flagOr("aaa404")) null else r.aaa302 // 源笔误: AAA404 分支写 AAA302
                    rTc93["aaa303"] = if (flagOr("aaa407")) null else r.aaa303
                    rTc93["aaa302"] = r.aaa302; rTc93["aaa305"] = r.aaa305; rTc93["aaa303"] = r.aaa303
                    rTc93["bba001"] = r.bba001; rTc93["bba002"] = r.bba002; rTc93["bba003"] = r.bba003
                    rTc93["bba004"] = r.bba004; rTc93["bba005"] = r.bba005
                    rTc93["caa001"] = r.caa001; rTc93["atb90t"] = r.atb90t
                    rTc93["atc93bc"] = r.daa002; rTc93["baa001"] = r.baa001
                    rTc93["baa002"] = r.baa002; rTc93["baa003"] = r.baa003
                    rTc93["atc93bd"] = r.daa001; rTc93["caa002"] = r.caa002
                    rTc93["bba200"] = r.bba200; rTc93["baa200"] = r.baa200
                }
                if (listOf("10", "11", "12", "13", "14", "15", "16")
                        .contains(s.str(tc90["atc9wc"])) &&
                    s.str(tc90["aae002"])?.takeLast(2) != "12" && atc931.takeLast(2) != "12") {
                    rTc93["atc93bc"] = BigDecimal.ZERO; rTc93["atc93bd"] = BigDecimal.ZERO
                }
                // 生成当月补缴(源 L1306)
                if (atc931L >= 202012) {
                    val cond = ((s.str(tc90["aaa401"]) == "1" && nz(tc90["aaa201"]).toLong() < atc931L && nTc93_1 == 0L) ||
                        (s.str(tc90["aaa402"]) == "1" && nz(tc90["aaa601"]).toLong() < atc931L &&
                            s.str(tc90["aaa110"])?.let { it.length >= 4 && it.substring(2, 4) == "99" } == true && nTc93_2 == 0L) ||
                        (s.str(tc90["aaa407"]) == "1" && nz(tc90["aaa501"]).toLong() < atc931L && nTc93_3 == 0L) ||
                        s.str(tc90["atc90p"]) == "2" || s.str(tc90["atc90pa"]) == "2" || s.str(tc90["atc90w"]) == "2")
                    if (cond) {
                        val br = try {
                            bbfCases.bfInternal(atc900!!, atc931, "2")
                        } catch (e: BbfErr) {
                            throw SettleRet(-1, e.m)
                        }
                        if (br["code"] != 1) throw SettleRet(-1, s.str(br["msg"]) ?: "保险当月补缴出错")
                        val bf = br["bf"] as? Map<String, Any?> ?: mapOf()
                        rTc93["atc93be"] = nz(rTc93["atc93be"]) + nz(bf["baa200"]) + nz(bf["daa001"])
                        rTc93["atc93ba"] = nz(rTc93["atc93ba"]) + nz(bf["bba200"]) + nz(bf["daa002"])
                    }
                }
                // 计算上月补退(TC07)
                val tc07s = s.jdbc.queryForList(
                    "SELECT * FROM tc07 WHERE atc07z IN ('0','1') AND aae002=?::numeric AND atc900=?",
                    atc931.toBigDecimal(), atc900)
                for (t in tc07s) {
                    if (s.str(t["atc072"]) != "80") {
                        rTc93["atc93be"] = nz(rTc93["atc93be"]) + nz(t["baa200"])
                        rTc93["atc93ba"] = nz(rTc93["atc93ba"]) + nz(t["bba200"])
                    }
                }
            }
            val aae00p = s.str(tc90["aae00p"])
            if (listOf("1", "2", "3", "4", "7").contains(aae00p)) {
                dispatch(rTc93, tc90, rTb93, rAb01, atc931, atc931L, atc937, nCntBj,
                    nAaa922, nDlDw, nDlGr, nMinus, vTaxAtc93ab, vTaxAtc93ad, vTaxAtc93r,
                    vTaxAtc93ar, nAtc93ab1, nDylj, nQzd, aae00p!!)
            } else if (aae00p == "5") {
                agency(rTc93, tc90, rTb93, atc931, atc937, nCntBj)
            } else if (aae00p == "6") {
                agentOnly(rTc93)
            }
            finalUpdate(rTc93, tc90, rec0)
        }
    }

    /** 尾程大 UPDATE(源 L2265-2397 逐列, NVL→COALESCE, aae013 CASE) */
    private fun finalUpdate(r: MutableMap<String, Any?>, tc90: Map<String, Any?>, rec0: Map<String, Any?>) {
        s.jdbc.update(
            """UPDATE tc93 SET aae00b=?, atc93aa=COALESCE(?::numeric,0), atc936=COALESCE(?::numeric,0),
               atc93ab=COALESCE(?::numeric,0), atc9p3=COALESCE(?::numeric,0), atc93d=COALESCE(?::numeric,0),
               atc93c=COALESCE(?::numeric,0), atc93s=COALESCE(?::numeric,0), atc93ac=COALESCE(?::numeric,0),
               atc93ad=COALESCE(?::numeric,0), atc93ae=COALESCE(?::numeric,0), atc93e=COALESCE(?::numeric,0),
               atc93m=COALESCE(?::numeric,0), atb90a=COALESCE(?::numeric,0),
               bba200=COALESCE(?::numeric,0), baa200=COALESCE(?::numeric,0),
               bba300=COALESCE(?::numeric,0), baa300=COALESCE(?::numeric,0), atc9p2=0, keys_1='',
               keys_2=?, atb90z=0, atc93w=?, aaa301=COALESCE(?::numeric,0), aaa302=COALESCE(?::numeric,0),
               aaa305=COALESCE(?::numeric,0), aaa303=COALESCE(?::numeric,0),
               bba001=COALESCE(?::numeric,0), bba002=COALESCE(?::numeric,0), bba003=COALESCE(?::numeric,0),
               bba004=COALESCE(?::numeric,0), bba005=COALESCE(?::numeric,0),
               caa001=COALESCE(?::numeric,0), atb90t=COALESCE(?::numeric,0),
               atc93ba=COALESCE(?::numeric,0), atc93bb=COALESCE(?::numeric,0),
               atc93bc=COALESCE(?::numeric,0), baa001=COALESCE(?::numeric,0),
               baa002=COALESCE(?::numeric,0), baa003=COALESCE(?::numeric,0),
               caa002=COALESCE(?::numeric,0), atc93bd=COALESCE(?::numeric,0),
               atc93be=COALESCE(?::numeric,0), atc93bf=COALESCE(?::numeric,0),
               atc93bg=COALESCE(?::numeric,0), atc301=COALESCE(?::numeric,0),
               atc302=COALESCE(?::numeric,0), atc303=COALESCE(?::numeric,0),
               atc304=COALESCE(?::numeric,0), atc305=COALESCE(?::numeric,0),
               atc306=COALESCE(?::numeric,0), atc30h=COALESCE(?::numeric,0),
               atc30i=COALESCE(?::numeric,0), atc93y1=COALESCE(?::numeric,0),
               atc93y2=COALESCE(?::numeric,0), atc93y3=COALESCE(?::numeric,0),
               atc93yz=COALESCE(?::numeric,0), atc93z1=COALESCE(?::numeric,0),
               atc93z2=COALESCE(?::numeric,0), atc93z3=COALESCE(?::numeric,0),
               atc93x1=COALESCE(?::numeric,0), atc93x3=COALESCE(?::numeric,0),
               atc93x5=COALESCE(?::numeric,0), atc93bh=COALESCE(?::numeric,0),
               atc93ag=COALESCE(?::numeric,0),
               aae013=(CASE WHEN ? IS NULL THEN '正常核算' ELSE ? END),
               atc93p='0', atc93g='1', aae114='1', aae214='0',
               aab004_1=?, atc908=?,
               atc93w1=COALESCE(?::numeric,0), atc93w2=COALESCE(?::numeric,0),
               atc93w3=COALESCE(?::numeric,0), atc93w4=COALESCE(?::numeric,0),
               atc93w5=COALESCE(?::numeric,0), atc93w6=COALESCE(?::numeric,0),
               atc93w7=COALESCE(?::numeric,0), atc93w8=COALESCE(?::numeric,0),
               atc93w9=COALESCE(?::numeric,0), atc93w10=COALESCE(?::numeric,0),
               atc93w11=COALESCE(?::numeric,0), atc93w12=COALESCE(?::numeric,0),
               atc93w13=COALESCE(?::numeric,0), atc93w14=COALESCE(?::numeric,0),
               atc93w15=COALESCE(?::numeric,0), atc93w16=COALESCE(?::numeric,0),
               atc93w17=COALESCE(?::numeric,0), atc93w18=COALESCE(?::numeric,0),
               atc93w19=COALESCE(?::numeric,0), atc93w20=COALESCE(?::numeric,0),
               atc93w21=COALESCE(?::numeric,0), atc93w22=COALESCE(?::numeric,0),
               atc93w23=COALESCE(?::numeric,0), atc93w24=COALESCE(?::numeric,0),
               atc93w25=COALESCE(?::numeric,0), atc93w26=COALESCE(?::numeric,0),
               atc93w27=COALESCE(?::numeric,0), atc93w28=COALESCE(?::numeric,0),
               atc93w29=COALESCE(?::numeric,0), atc93w30=COALESCE(?::numeric,0),
               atc93w31=COALESCE(?::numeric,0), atc93w32=COALESCE(?::numeric,0),
               atc93w33=COALESCE(?::numeric,0), atc93w34=COALESCE(?::numeric,0),
               atc93w35=COALESCE(?::numeric,0), atc93w36=COALESCE(?::numeric,0),
               atc93r=COALESCE(?::numeric,0), atc93ar=COALESCE(?::numeric,0)
               WHERE atc930=?""",
            tc90["aae00b"], r["atc93aa"], r["atc936"], r["atc93ab"], r["atc9p3"], r["atc93d"],
            r["atc93c"], r["atc93s"], r["atc93ac"], r["atc93ad"], r["atc93ae"], r["atc93e"],
            r["atc93m"], r["atb90a"], r["bba200"], r["baa200"], r["bba300"], r["baa300"],
            r["keys_2"], r["atc93w"], r["aaa301"], r["aaa302"], r["aaa305"], r["aaa303"],
            r["bba001"], r["bba002"], r["bba003"], r["bba004"], r["bba005"],
            r["caa001"], r["atb90t"], r["atc93ba"], r["atc93bb"], r["atc93bc"],
            r["baa001"], r["baa002"], r["baa003"], r["caa002"], r["atc93bd"],
            r["atc93be"], r["atc93bf"], r["atc93bg"], r["atc301"], r["atc302"],
            r["atc303"], r["atc304"], r["atc305"], r["atc306"], r["atc30h"], r["atc30i"],
            r["atc93y1"], r["atc93y2"], r["atc93y3"], r["atc93yz"], r["atc93z1"],
            r["atc93z2"], r["atc93z3"], r["atc93x1"], r["atc93x3"], r["atc93x5"],
            r["atc93bh"], r["atc93ag"], rec0["aae013"], rec0["aae013"],
            tc90["aab004_1"], tc90["atc908"],
            r["atc93w1"], r["atc93w2"], r["atc93w3"], r["atc93w4"], r["atc93w5"], r["atc93w6"],
            r["atc93w7"], r["atc93w8"], r["atc93w9"], r["atc93w10"], r["atc93w11"], r["atc93w12"],
            r["atc93w13"], r["atc93w14"], r["atc93w15"], r["atc93w16"], r["atc93w17"], r["atc93w18"],
            r["atc93w19"], r["atc93w20"], r["atc93w21"], r["atc93w22"], r["atc93w23"], r["atc93w24"],
            r["atc93w25"], r["atc93w26"], r["atc93w27"], r["atc93w28"], r["atc93w29"], r["atc93w30"],
            r["atc93w31"], r["atc93w32"], r["atc93w33"], r["atc93w34"], r["atc93w35"], r["atc93w36"],
            r["atc93r"], r["atc93ar"], r["atc930"])
    }

    /** 派遣/劳务段(aae00p∈12347; 源 L1400-2000 含个税累计制) — 详见 body 调用 */
    private fun dispatch(
        r: MutableMap<String, Any?>, tc90: Map<String, Any?>, rTb93: Map<String, Any?>,
        rAb01: Map<String, Any?>, atc931: String, atc931L: Long, atc937: String,
        nCntBj: BigDecimal, nAaa922: BigDecimal, nDlDw: BigDecimal, nDlGr: BigDecimal,
        nMinus: Long, vTaxAtc93ab0: BigDecimal, vTaxAtc93ad0: BigDecimal,
        vTaxAtc93r0: BigDecimal, vTaxAtc93ar0: BigDecimal, nAtc93ab1In: Long,
        nDylj: BigDecimal, nQzdIn: Long, aae00p: String,
    ) {
        val vAaa922 = s.qzd(if (aae00p == "3") "2" else "1")
        // 历史欠款(atc937='1')
        if (atc937 == "1") {
            val prev = s.prevYm(s.str(r["atc931"])!!)
            val qks = s.jdbc.queryForList(
                "SELECT atc930, COALESCE(atc93e,0) AS atc93e FROM tc93 WHERE atc93e <> 0 AND atc931=?::numeric AND atc900=?",
                prev.toBigDecimal(), tc90["atc900"])
            for (qk in qks) {
                var e = nz(qk["atc93e"])
                val t9as = s.jdbc.queryForList(
                    "SELECT COALESCE(atb9a1,0) AS atb9a1 FROM tb9a WHERE dstatus='1' AND atb9a2='2' AND ukey=?",
                    qk["atc930"])
                for (t in t9as) e += nz(t["atb9a1"])
                r["atc93be"] = nz(r["atc93be"]) - e
            }
        }
        // 应发工资 33 项(源 L1439-1473 逐项符号)
        r["atc93aa"] = nz(r["atc93x"]) + nz(r["atc933"]) - nz(r["atc934"]) - nz(r["atc93l"]) -
            nz(r["atc93y"]) + nz(r["atc935"]) +
            nz(r["atc93w1"]) + nz(r["atc93w2"]) + nz(r["atc93w3"]) + nz(r["atc93w4"]) +
            nz(r["atc93w5"]) + nz(r["atc93w6"]) - nz(r["atc93w7"]) + nz(r["atc93w8"]) +
            nz(r["atc93w9"]) + nz(r["atc93w10"]) + nz(r["atc93w11"]) + nz(r["atc93w12"]) +
            nz(r["atc93w13"]) + nz(r["atc93w14"]) + nz(r["atc93w15"]) + nz(r["atc93w16"]) +
            nz(r["atc93w17"]) - nz(r["atc93w18"]) - nz(r["atc93w19"]) - nz(r["atc93w20"]) +
            nz(r["atc93w21"]) + nz(r["atc93w22"]) + nz(r["atc93w23"]) + nz(r["atc93w24"]) +
            nz(r["atc93w25"]) - nz(r["atc93w26"]) + nz(r["atc93w27"]) + nz(r["atc93w33"]) +
            nz(r["atc93w32"]) + nz(r["atc93w35"]) + nz(r["atc93w36"])
        r["atc936"] = nz(r["atc93w4"]) + nz(r["atc93w21"]) + nz(r["atc93w33"])
        r["atc93s"] = BigDecimal.ZERO; r["atc93ae"] = null; r["keys_2"] = null
        // 年终奖税
        val k = nz(r["atc93k"])
        if (k > BigDecimal.ZERO) {
            val br = s.jdbc.queryForList(
                "SELECT aaa933, aaa934 FROM aa93 WHERE aaa935='2' AND ?/12 > aaa931 AND ?/12 <= aaa932",
                k, k).singleOrNull() ?: throw RuntimeException("ORA-01403: 未找到任何数据 (AA93 年终奖档)")
            r["atc93ac"] = k * nz(br["aaa933"]) - nz(br["aaa934"])
        }
        var vTaxAtc93ab = vTaxAtc93ab0
        var vTaxAtc93ad = vTaxAtc93ad0
        var vTaxAtc93r = vTaxAtc93r0
        var vTaxAtc93ar = vTaxAtc93ar0
        var nAtc93ab1 = nAtc93ab1In
        var nQzd = nQzdIn
        var nDyljL = nDylj
        val aa = nz(r["atc93aa"])
        if (aa.compareTo(BigDecimal.ZERO) != 0 || (aa.compareTo(BigDecimal.ZERO) == 0 && s.str(tc90["atb90r"]) == "3")) {
            val agg = s.jdbc.queryForList(
                """SELECT COALESCE(SUM(COALESCE(t.atc9p3,0)),0) a, COALESCE(SUM(COALESCE(t.atc93ad,0)),0) b,
                   COALESCE(SUM(COALESCE(t.atc93r,0)),0) c, COALESCE(SUM(COALESCE(t.atc93r,0)),0) d,
                   COALESCE(SUM(CASE WHEN t.atc931=?::numeric AND t.atc900 <> ? THEN 1 ELSE 0 END),0) e,
                   COALESCE(SUM(CASE WHEN t.aae002=?::numeric AND t.atc93r <> 0 THEN 1 ELSE 0 END),0) f,
                   COALESCE(SUM(CASE WHEN t.atc931=?::numeric AND t.atc930 <> ? THEN COALESCE(t.atc9p3,0) ELSE 0 END),0) g,
                   COALESCE(SUM(CASE WHEN t.atc93r > 0 AND t.atb930=? AND t.aae002=?::numeric THEN 1 ELSE 0 END),0) h
                   FROM tc93 t, ab01 bb
                   WHERE t.aae214='1' AND t.aab001=bb.aab001 AND bb.aaa030=?
                     AND t.aae002 >= ?::numeric AND t.aae002 <= ?::numeric
                     AND t.atc930 <> ? AND t.aac001=?""",
                atc931.toBigDecimal(), tc90["atc900"], r["aae002"],
                atc931.toBigDecimal(), r["atc930"], r["atb930"], r["aae002"],
                rAb01["aaa030"], (s.str(r["aae002"])!!.substring(0, 4) + "01").toBigDecimal(),
                r["aae002"], r["atc930"], tc90["aac001"]).firstOrNull() ?: mapOf()
            vTaxAtc93ab = nz(agg["a"]); vTaxAtc93ad = nz(agg["b"])
            vTaxAtc93r = nz(agg["c"]); vTaxAtc93ar = nz(agg["d"])
            nAtc93ab1 = s.long(agg["e"]) ?: 0L
            nQzd = s.long(agg["f"]) ?: 0L
            nDyljL = nz(agg["g"])
            // 累计专项附加(TC30, 同区划)
            val t30 = s.jdbc.queryForList(
                """SELECT COALESCE(SUM(COALESCE(a.atc301,0)),0) a1, COALESCE(SUM(COALESCE(a.atc302,0)),0) a2,
                   COALESCE(SUM(COALESCE(a.atc303,0)),0) a3, COALESCE(SUM(COALESCE(a.atc304,0)),0) a4,
                   COALESCE(SUM(COALESCE(a.atc305,0)),0) a5, COALESCE(SUM(COALESCE(a.atc306,0)),0) a6,
                   COALESCE(SUM(COALESCE(a.atc30h,0)),0) a7, COALESCE(SUM(COALESCE(a.atc30i,0)),0) a8
                   FROM tc30 a, ab01 b WHERE a.aab001=b.aab001 AND b.aaa030=?
                   AND a.aae001=?::numeric AND a.aac001=?""",
                rAb01["aaa030"], s.long(r["aae002"]), tc90["aac001"]).firstOrNull() ?: mapOf()
            r["atc301"] = nz(t30["a1"]); r["atc302"] = nz(t30["a2"]); r["atc303"] = nz(t30["a3"])
            r["atc304"] = nz(t30["a4"]); r["atc305"] = nz(t30["a5"]); r["atc306"] = nz(t30["a6"])
            r["atc30h"] = nz(t30["a7"]); r["atc30i"] = nz(t30["a8"])
        } else {
            vTaxAtc93ab = BigDecimal.ZERO; r["atc93bg"] = BigDecimal.ZERO; vTaxAtc93ad = BigDecimal.ZERO
        }
        agencyFee(r, tc90, rTb93, atc937, nCntBj, nDlDw, nDlGr, false)
        if (s.str(tc90["atb90c"]) == "1") {
            r["atc93aa"] = nz(r["atc93aa"]) + nz(r["baa200"]) + nz(r["atc93be"])
        }
        val aa2 = nz(r["atc93aa"])
        if (aa2.compareTo(BigDecimal.ZERO) != 0 || (aa2.compareTo(BigDecimal.ZERO) == 0 && nz(r["atc93x3"]) == BigDecimal.ZERO)) {
            if (s.str(tc90["atb90r"]) == "3" || (s.str(tc90["atb90r"]) == "5" && aa2.compareTo(BigDecimal.ZERO) != 0)) {
                r["atc9p3"] = aa2 - (nz(r["baa200"]) + nz(r["atc93be"])) -
                    nz(r["atc93bd"]) - nz(r["atc93w4"]) - nz(r["atc93w21"]) - nz(r["atc93w33"])
                if (nAtc93ab1 == 0L && (aa2.compareTo(BigDecimal.ZERO) != 0 ||
                            (aa2.compareTo(BigDecimal.ZERO) == 0 && s.str(tc90["atb90r"]) == "3"))) {
                    var n922 = nAaa922
                    if (n922.compareTo(BigDecimal.ZERO) == 0) n922 = BigDecimal.ONE
                    if (nQzd == 0L) {
                        r["atc93r"] = n922 * vAaa922
                        r["atc93ar"] = vTaxAtc93ar + nz(r["atc93r"])
                    } else {
                        r["atc93r"] = BigDecimal.ZERO; r["atc93ar"] = BigDecimal.ZERO
                    }
                    if (aae00p == "3") {
                        r["atc9p3"] = nz(r["atc9p3"])
                    } else {
                        val kk = nz(r["atc93k"])
                        if (kk > BigDecimal.ZERO) {
                            r["atc9p3"] = nz(r["atc9p3"]) - n922 * vAaa922
                        } else if (kk.compareTo(BigDecimal.ZERO) == 0) {
                            if (nQzd == 0L) {
                                r["atc9p3"] = nz(r["atc9p3"]) - n922 * vAaa922 -
                                    (vTaxAtc93ar - vTaxAtc93r).max(BigDecimal.ZERO)
                            } else {
                                r["atc9p3"] = nz(r["atc9p3"])
                            }
                        } else {
                            r["atc9p3"] = BigDecimal.ZERO
                        }
                    }
                }
                r["atc93ab"] = if (aae00p != "3") vTaxAtc93ab + nz(r["atc9p3"]) else nz(r["atc9p3"])
                var vTaxSalary = if (aae00p != "3") nz(r["atc93ab"]) -
                    nz(r["atc301"]) - nz(r["atc302"]) - nz(r["atc303"]) - nz(r["atc304"]) -
                    nz(r["atc305"]) - nz(r["atc306"]) - nz(r["atc30h"]) - nz(r["atc30i"])
                else nz(r["atc9p3"])
                if (s.long(r["aae002"])!! > 202012L) {
                    if (vTaxSalary <= BigDecimal.ZERO) {
                        r["atc93ad"] = BigDecimal.ZERO
                        r["atc93d"] = nz(r["atc93ad"])
                    } else {
                        var vTaxRate = BigDecimal.ZERO
                        var vSks = BigDecimal.ZERO
                        if (aae00p == "3") {
                            if (vTaxSalary > BigDecimal(4000)) {
                                vTaxSalary = (nDyljL + vTaxSalary).multiply(BigDecimal("0.8"))
                                    .setScale(2, RoundingMode.HALF_UP)
                                r["atc93r"] = vTaxSalary.multiply(BigDecimal("0.2"))
                                r["atc93ar"] = vTaxSalary.multiply(BigDecimal("0.2"))
                            } else if (vTaxSalary > BigDecimal(800)) {
                                vTaxSalary = vTaxSalary.subtract(BigDecimal(800))
                                    .setScale(2, RoundingMode.HALF_UP)
                                r["atc93r"] = vTaxSalary.multiply(BigDecimal("0.2"))
                                r["atc93ar"] = vTaxSalary.multiply(BigDecimal("0.2"))
                            } else {
                                // 源: 赋零后继续流转(无 early return)
                                vTaxSalary = BigDecimal.ZERO
                                r["atc93r"] = BigDecimal.ZERO; r["atc93ar"] = BigDecimal.ZERO
                                vTaxRate = BigDecimal.ZERO; vSks = BigDecimal.ZERO
                                r["atc93ad"] = BigDecimal.ZERO
                            }
                            if (nz(r["atc9p3"]) > BigDecimal(800)) {
                                val one = s.jdbc.queryForList(
                                    "SELECT aaa933, aaa934 FROM aa93 WHERE aaa935='1' AND ? > aaa931 AND ? <= aaa932",
                                    vTaxSalary, vTaxSalary).singleOrNull()
                                    ?: throw RuntimeException("ORA-01403: 未找到任何数据 (AA93 劳务档)")
                                vTaxRate = nz(one["aaa933"]); vSks = nz(one["aaa934"])
                            }
                            val prevPaid = nz(s.jdbc.queryForObject(
                                """SELECT COALESCE(SUM(COALESCE(atc93ad,0)),0) FROM tc93
                                   WHERE aae002=?::numeric AND atc930 <> ? AND atb930=? AND aac001=?""",
                                BigDecimal::class.java, atc931.toBigDecimal(), r["atc930"],
                                r["atb930"], tc90["aac001"]))
                            val taxSa = vTaxSalary.multiply(vTaxRate).subtract(vSks).subtract(prevPaid)
                            r["atc93ad"] = taxSa
                        } else {
                            val br = s.jdbc.queryForList(
                                "SELECT aaa933, aaa934 FROM aa93 WHERE aaa935='9' AND ? > aaa931 AND ? <= aaa932",
                                vTaxSalary, vTaxSalary).singleOrNull()
                                ?: throw RuntimeException("ORA-01403: 未找到任何数据 (AA93 综合档)")
                            vTaxRate = nz(br["aaa933"]); vSks = nz(br["aaa934"])
                            val taxSa = vTaxSalary.multiply(vTaxRate).subtract(vSks)
                            r["atc93ad"] = taxSa - vTaxAtc93ad - nz(r["atc93bg"])
                        }
                        if (nz(r["atc93ad"]) < BigDecimal.ZERO) r["atc93ad"] = BigDecimal.ZERO
                        r["atc93d"] = nz(r["atc93ad"])
                    }
                } else {
                    r["atc93d"] = nz(r["atc93ad"])
                }
            } else {
                r["atc9p3"] = BigDecimal.ZERO; r["atc93ab"] = BigDecimal.ZERO
                r["atc93d"] = BigDecimal.ZERO; r["atc93ad"] = BigDecimal.ZERO
            }
            if (atc931L == 202312L && s.long(tc90["atc900"]) == 20002L) {
                r["atc93d"] = BigDecimal.ZERO; r["atc93ad"] = BigDecimal.ZERO
            }
            if (atc931L >= 202109L &&
                ((s.str(r["atb931"]) ?: "").contains("后勤疫情") ||
                    listOf(38551L, 38533L, 38733L, 38750L, 38751L, 38754L, 38755L, 38793L,
                        38794L, 39152L).contains(s.long(rTb93["atb930"])))) {
                r["atc9p3"] = BigDecimal.ZERO; r["atc93ab"] = BigDecimal.ZERO
                r["atc93d"] = BigDecimal.ZERO; r["atc93ad"] = BigDecimal.ZERO
            }
            r["atc93z1"] = nz(r["atc93aa"]) - nz(r["atc93bg"]) - nz(r["atc93ad"]) -
                nz(r["baa200"]) - nz(r["atc93be"]) - nz(r["atc93bd"]) +
                nz(r["atc93k"]) - nz(r["atc93ac"]) + nz(r["atc93m"]) + nz(r["atc93x3"])
            if (atc937 == "1") {
                r["atc93y1"] = if (listOf("1", "3").contains(s.str(tc90["atc90za"])))
                    nz(r["atc93aa"]).multiply(BigDecimal(2)).divide(BigDecimal(100), 2, RoundingMode.HALF_UP)
                else BigDecimal.ZERO
                r["atc93z2"] = if (listOf("2", "3").contains(s.str(tc90["atc90za"])))
                    (nz(r["atc93w9"]) + nz(r["atc93x"])).multiply(BigDecimal(5))
                        .divide(BigDecimal(1000), 0, RoundingMode.HALF_UP)
                else BigDecimal.ZERO
            }
            r["atc93y2"] = nz(r["atc93bc"]) + nz(r["atc93bd"])
            r["atc93y3"] = nz(r["atb90a"])
            r["atc93yz"] = nz(r["bba200"]) + nz(r["baa200"]) + nz(r["atc93be"]) +
                nz(r["atc93ba"]) + nz(r["atc93y1"]) + nz(r["atc93y2"]) + nz(r["atc93y3"]) +
                nz(r["atc93bb"]) + nz(r["atc93x1"]) + nz(r["atc93x5"]) + nz(r["atc93w34"])
            r["atc93bf"] = nz(r["atc93aa"]) + nz(r["bba200"]) + nz(r["atc93bc"]) +
                nz(r["atb90a"]) + nz(r["atc93ba"]) + nz(r["atc93x1"]) + nz(r["atc93x5"]) +
                nz(r["atc93y1"]) + nz(r["atc93bb"]) + nz(r["atc93k"]) + nz(r["atc93m"]) +
                nz(r["atc93w34"])
            r["atc93c"] = nz(r["atc93z1"]) - nz(r["atc93z2"]) - nz(r["atc93z3"]) -
                nz(r["baa300"]) - nz(r["atc93w28"]) - nz(r["atc93ag"]) -
                nz(r["atc93bh"]) - nz(r["atc93w29"])
            if (nz(r["atc93c"]) < BigDecimal.ZERO || nz(r["atc93z1"]) < BigDecimal.ZERO) {
                r["atc93e"] = nz(r["atc93c"]); r["atc93w"] = "1"
                r["atc93c"] = BigDecimal.ZERO; r["atc93z1"] = BigDecimal.ZERO
            } else {
                r["atc93e"] = BigDecimal.ZERO
            }
        } else if (nz(r["atc93x3"]) > BigDecimal.ZERO) {
            // 应发为零且个人交现金(源 L2063-2140)
            if (nz(r["bba200"]) == BigDecimal.ZERO) {
                r["atb90a"] = BigDecimal.ZERO; r["baa300"] = BigDecimal.ZERO
            } else {
                r["atb90a"] = nz(r["atb90a"]) + nz(r["baa300"]); r["baa300"] = BigDecimal.ZERO
            }
            r["atc93ab"] = BigDecimal.ZERO; r["atc93z1"] = BigDecimal.ZERO; r["atc93z2"] = BigDecimal.ZERO
            r["atc93y1"] = BigDecimal.ZERO
            r["atc93y2"] = nz(r["atc93bc"])
            r["atc93y3"] = nz(r["atb90a"])
            r["atc93yz"] = nz(r["bba200"]) + nz(r["baa200"]) + nz(r["atc93be"]) +
                nz(r["atc93ba"]) + nz(r["atc93y1"]) + nz(r["atc93y2"]) + nz(r["atc93y3"]) +
                nz(r["atc93w34"])
            r["atc93z2"] = if (s.str(tc90["atc90za"]) == "2")
                nz(r["atc93z1"]).multiply(BigDecimal(5)).divide(BigDecimal(1000), 2, RoundingMode.HALF_UP)
            else BigDecimal.ZERO
            r["atc93c"] = nz(r["atc93x3"]) - (nz(r["baa200"]) + nz(r["atc93be"]) + nz(r["atc93bd"]))
            if (nz(r["atc93c"]) < BigDecimal.ZERO || nz(r["atc93z1"]) < BigDecimal.ZERO) {
                r["atc93e"] = nz(r["atc93c"])
            } else {
                r["atc93e"] = BigDecimal.ZERO
            }
            r["bba200"] = nz(r["bba200"]) + nz(r["atc93bb"])
            r["atc93bf"] = nz(r["atc93aa"]) + nz(r["bba200"]) + nz(r["atc93bc"]) +
                nz(r["atb90a"]) + nz(r["atc93ba"]) + nz(r["atc93x1"]) + nz(r["atc93x5"]) +
                nz(r["atc93m"]) + nz(r["atc93w34"])
            r["atc93r"] = BigDecimal.ZERO; r["atc93ar"] = BigDecimal.ZERO
            r["atc93d"] = BigDecimal.ZERO; r["atc93ad"] = BigDecimal.ZERO
        }
    }

    /** 代理费月标准(源 L1609-1685; useZ1: '5' 保险代理段用 Z1 其余用 AA) */
    private fun agencyFee(
        r: MutableMap<String, Any?>, tc90: Map<String, Any?>, rTb93: Map<String, Any?>,
        atc937: String, nCntBj: BigDecimal, nDlDw: BigDecimal, nDlGr: BigDecimal, useZ1: Boolean,
    ) {
        if (atc937 == "1") {
            val t = s.str(rTb93["atb93t"])
            val base = if (useZ1) nz(r["atc93z1"]) else nz(r["atc93aa"])
            when {
                t == "5" || t == null -> {
                    r["atb90a"] = nz(tc90["atb90a"]) + nDlDw
                    r["baa300"] = nz(tc90["atb90b"]) + nDlGr
                }
                t == "1" -> { r["atb90a"] = nDlDw; r["baa300"] = nDlGr }
                t == "2" -> {
                    r["atb90a"] = base * nz(rTb93["atb93u"]).divide(BigDecimal(100), 10, RoundingMode.HALF_UP) + nDlDw
                    r["baa300"] = nDlGr
                }
                t == "3" -> {
                    r["atb90a"] = (base + nz(r["bba001"]) + nz(r["bba003"]) + nz(r["bba002"]) +
                        nz(r["bba004"]) + nz(r["bba005"]) + nz(r["atb90t"])) *
                        nz(rTb93["atb93u"]).divide(BigDecimal(100), 10, RoundingMode.HALF_UP) + nDlDw
                    r["baa300"] = nDlGr
                }
                t == "4" -> {
                    r["atb90a"] = (base + nz(r["caa001"])) *
                        nz(rTb93["atb93u"]).divide(BigDecimal(100), 10, RoundingMode.HALF_UP) + nDlDw
                    r["baa300"] = nDlGr
                }
                else -> {
                    r["atb90a"] = nz(tc90["atb90a"]) + nDlDw
                    r["baa300"] = nz(tc90["atb90b"]) + nDlGr
                }
            }
            if (nz(r["atc93aa"]) + nz(r["bba200"]) + nz(r["baa200"]) == BigDecimal.ZERO) {
                r["atb90a"] = BigDecimal.ZERO; r["baa300"] = BigDecimal.ZERO
            }
        }
        if (s.str(rTb93["atb93t"]) == "1") {
            r["atb90a"] = BigDecimal.ZERO; r["baa300"] = BigDecimal.ZERO
        } else {
            r["atb90a"] = nz(r["atb90a"]) + nz(tc90["atb90a"]) * nCntBj + nz(r["atc93w30"])
            r["baa300"] = nz(r["baa300"]) + nz(tc90["atb90b"]) * nCntBj + nz(r["atc93w31"])
        }
    }

    /** 保险代理段(aae00p='5'; 源 L2137-2350) */
    private fun agency(
        r: MutableMap<String, Any?>, tc90: Map<String, Any?>, rTb93: Map<String, Any?>,
        atc931: String, atc937: String, nCntBj: BigDecimal,
    ) {
        r["atc93aa"] = nz(r["atc93x"]) + nz(r["atc933"]) - nz(r["atc934"]) - nz(r["atc93l"]) -
            nz(r["atc93y"]) + nz(r["atc935"]) +
            nz(r["atc93w1"]) + nz(r["atc93w2"]) + nz(r["atc93w3"]) + nz(r["atc93w4"]) +
            nz(r["atc93w5"]) + nz(r["atc93w6"]) - nz(r["atc93w7"]) + nz(r["atc93w8"]) +
            nz(r["atc93w9"]) + nz(r["atc93w10"]) + nz(r["atc93w11"]) + nz(r["atc93w12"]) +
            nz(r["atc93w13"]) + nz(r["atc93w14"]) + nz(r["atc93w15"]) + nz(r["atc93w16"]) +
            nz(r["atc93w17"]) - nz(r["atc93w18"]) - nz(r["atc93w19"]) - nz(r["atc93w20"]) +
            nz(r["atc93w21"]) + nz(r["atc93w22"]) + nz(r["atc93w23"]) + nz(r["atc93w24"]) +
            nz(r["atc93w25"]) - nz(r["atc93w26"]) + nz(r["atc93w27"]) + nz(r["atc93w33"]) +
            nz(r["atc93w32"]) + nz(r["atc93w35"]) + nz(r["atc93w36"])
        r["atc936"] = nz(r["atc93w4"]) + nz(r["atc93w21"]) + nz(r["atc93w33"])
        r["atc93s"] = BigDecimal.ZERO; r["atc93ae"] = null; r["keys_2"] = null
        r["atc93bg"] = BigDecimal.ZERO
        agencyFee(r, tc90, rTb93, atc937, nCntBj, BigDecimal.ZERO, BigDecimal.ZERO, true)
        // 累加补缴代理费('5' 段无 atb93t='1' 短路; BAA300 自乘怪癖忠实)
        r["atb90a"] = nz(r["atb90a"]) + nz(tc90["atb90a"]) * nCntBj + nz(r["atc93w30"])
        r["baa300"] = nz(r["baa300"]) + nz(r["baa300"]) * nCntBj + nz(r["atc93w31"])
        r["atc9p3"] = BigDecimal.ZERO; r["atc93r"] = BigDecimal.ZERO
        r["atc93ar"] = BigDecimal.ZERO; r["atc93ab"] = BigDecimal.ZERO
        r["atc93ad"] = BigDecimal.ZERO; r["atc93d"] = nz(r["atc93ad"])
        r["atc93z1"] = nz(r["atc93m"]) - nz(r["atc93bd"])
        r["atc93y1"] = BigDecimal.ZERO
        r["atc93y2"] = nz(r["atc93bc"])
        r["atb90a"] = nz(r["atb90a"]) + nz(r["baa300"])
        r["atc93y3"] = nz(r["atb90a"])
        r["atc93yz"] = nz(r["bba200"]) + nz(r["baa200"]) + nz(r["atc93be"]) + nz(r["atc93ba"]) +
            nz(r["atc93y1"]) + nz(r["atc93y2"]) + nz(r["atc93y3"]) + nz(r["atc93bb"]) +
            nz(r["atc93x1"]) + nz(r["atc93x5"])
        r["bba200"] = nz(r["bba200"]) + nz(r["atc93bb"])
        r["atc93bf"] = nz(r["atc93yz"]) + nz(r["atc93k"]) + nz(r["atc93m"])
        r["atc93z2"] = BigDecimal.ZERO
        r["atc93c"] = r["atc93z1"]
        if (nz(r["atc93c"]) < BigDecimal.ZERO) {
            r["atc93e"] = r["atc93c"]; r["atc93c"] = BigDecimal.ZERO; r["atc93z1"] = BigDecimal.ZERO
        }
    }

    /** 代理人员段(aae00p='6'; 源 L2352-2382) */
    private fun agentOnly(r: MutableMap<String, Any?>) {
        r["atc93aa"] = BigDecimal.ZERO
        r["atc93bf"] = nz(r["atb90a"])
        r["atc93y1"] = BigDecimal.ZERO; r["atc93y2"] = BigDecimal.ZERO
        r["atc93y3"] = BigDecimal.ZERO; r["atc93yz"] = BigDecimal.ZERO
        r["atc93s"] = BigDecimal.ZERO; r["atc93ae"] = null; r["keys_2"] = null
        r["atc93bg"] = BigDecimal.ZERO; r["atc93ab"] = BigDecimal.ZERO
        r["atc93r"] = BigDecimal.ZERO; r["atc93ar"] = BigDecimal.ZERO
        r["atc93ad"] = BigDecimal.ZERO; r["atc93d"] = BigDecimal.ZERO
        r["atc93z1"] = BigDecimal.ZERO; r["atc93z2"] = BigDecimal.ZERO
        r["atc93c"] = BigDecimal.ZERO
    }
}
