package com.jlrc.rcpq.service.td

import com.jlrc.rcpq.service.proc.ProcHandler
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal

/** ②④⑤腿: 提取上月工资/撤销结算/生成费用清单(桥单 20261127-034; 源 L111-234/3154-3200/9449-9805)。 */
@Component
class TdSettleLegs(
    private val s: TdSettleSupport,
    private val calc: TdSettleCalc,
) {

    /** PRC_提取上月工资(L111-234): 守卫→克隆上月行→尾调 PRC_费用结算 */
    @Transactional
    fun extractPriorMonth(atb930: String, atc931: String, atc937: String, aae011: String): Map<String, Any?> {
        return try {
            val nCnt = s.jdbc.queryForObject(
                "SELECT COUNT(1) FROM tc93 WHERE aae214='1' AND atc931=?::numeric AND atc937=? AND atb930=?",
                Long::class.java, atc931.toBigDecimal(), atc937, atb930.toLong()) ?: 0L
            if (nCnt > 0) throw SettleRet(-1, "月费用清单已确认，无法提取上月工资")
            val vNy = try { s.prevYm(atc931) } catch (e: Exception) {
                throw SettleRet(-1, "工资年月格式不正确")
            }
            var nAae002 = s.nowYm()
            if (atc931 > nAae002) nAae002 = atc931
            val user = s.jdbc.queryForList("SELECT * FROM sys_org_user_unit WHERE aae011=?", aae011)
                .singleOrNull() ?: throw RuntimeException("ORA-01403: 未找到任何数据 (SYS_ORG_USER_UNIT)")
            val recs = s.jdbc.queryForList(
                "SELECT * FROM tc93 WHERE atc931=?::numeric AND atc937=? AND atb930=? ORDER BY atc930",
                vNy.toBigDecimal(), atc937, atb930.toLong())
            for (rec in recs) {
                val tc90 = s.jdbc.queryForList("SELECT * FROM tc90 WHERE atc900=?", rec["atc900"])
                    .singleOrNull() ?: throw SettleRet(-1, "提取失败，原因是合同变更了")
                // 源: ATB90R='3' OR ('5' AND ATC90AV >= N_AAE002) — NULL>=x 为 NULL(假)
                val okState = s.str(tc90["atb90r"]) == "3" ||
                    (s.str(tc90["atb90r"]) == "5" &&
                        (s.str(tc90["atc90av"])?.toBigDecimalOrNull()
                            ?.compareTo(nAae002.toBigDecimal()) ?: -1) >= 0)
                if (okState) {
                    val r = rec.toMutableMap()
                    s.jdbc.update(
                        """DELETE FROM tc93 WHERE aae214='0' AND atc931=?::numeric AND atc937=?
                           AND atc900=?""",
                        atc931.toBigDecimal(), atc937, tc90["atc900"])
                    r["atc931"] = atc931.toLong(); r["atc932"] = atc931.toLong()
                    r["aae001"] = atc931.toLong(); r["aae002"] = atc931.toLong()
                    r["atc93d"] = BigDecimal.ZERO; r["atc93ad"] = BigDecimal.ZERO
                    r["aae114"] = "1"; r["aae214"] = "0"; r["atc93g"] = "0"
                    r["aae011"] = user["aae011"]; r["aae019"] = user["aae019"]
                    r["aae017"] = user["aae017"]; r["aae020"] = user["aae020"]
                    r["aae022"] = user["aae022"]; r["aae036"] = s.now()
                    r["atc930"] = s.jdbc.queryForObject("SELECT nextval('seq_tc93_atc930')", Long::class.java)
                    r["baz001"] = s.jdbc.queryForObject("SELECT nextval('seq_sys_baz001')", Long::class.java)
                    r["baz002"] = s.jdbc.queryForObject("SELECT nextval('seq_sys_baz002')", Long::class.java)
                    try {
                        s.insertRow("tc93", r)
                    } catch (e: Exception) {
                        if (!s.isDup(e)) throw RuntimeException(e.message, e)
                        // DUP_VAL_ON_INDEX → 源回 UPDATE atc931/atc932(忠实保留)
                        s.jdbc.update(
                            """UPDATE tc93 SET atc931=?::numeric, atc932=?::numeric
                               WHERE atc931=?::numeric AND atb930=? AND atc937=? AND atc900=?""",
                            atc931.toBigDecimal(), atc931.toBigDecimal(), atc931.toBigDecimal(),
                            r["atb930"], atc937, r["atc900"])
                    }
                }
            }
            // 尾调 PRC_费用结算(源 L221-232: 失败原样透传)
            val r = calc.feeSettle(atb930, atc931, atc937, atc931, aae011)
            if ((r["appCode"] ?: 1) != 1) r else mapOf("appCode" to 1, "errMsg" to null as String?)
        } catch (e: SettleRet) {
            mapOf("appCode" to e.code, "errMsg" to e.msg)
        } catch (e: Exception) {
            mapOf("appCode" to -1, "errMsg" to "提取上月工资出错，错误信息：${e.message}，请将错误信息发给维护工程师")
        }
    }

    /** PRC_撤销结算(L3154-3200): 两守卫+物理删目标月行 */
    @Transactional
    fun cancelSettle(atb930: String, atc931: String, atc937: String, atc932: String): Map<String, Any?> {
        return try {
            val n1 = s.jdbc.queryForObject(
                "SELECT COUNT(1) FROM tc93 WHERE aae214='1' AND atc931=?::numeric AND atc937=? AND atb930=?",
                Long::class.java, atc931.toBigDecimal(), atc937, atb930.toLong()) ?: 0L
            if (n1 > 0) throw SettleRet(-1, "月费用清单已确认，无法删除，请先撤销结算")
            val n2 = s.jdbc.queryForObject(
                """SELECT COUNT(1) FROM tb9a a WHERE dstatus='1' AND atb9a2='2'
                   AND EXISTS (SELECT 1 FROM tc93 WHERE atc930=a.ukey
                       AND atc931=?::numeric AND atc937=? AND atb930=?)""",
                Long::class.java, atc931.toBigDecimal(), atc937, atb930.toLong()) ?: 0L
            if (n2 > 0) throw SettleRet(-1, "存在个人清欠记录，无法删除，请联系前台业务员取消清欠记录")
            s.jdbc.update(
                "DELETE FROM tc93 WHERE atc931=?::numeric AND atc937=? AND atb930=?",
                atc931.toBigDecimal(), atc937, atb930.toLong())
            mapOf("appCode" to 1, "errMsg" to null as String?)
        } catch (e: SettleRet) {
            mapOf("appCode" to e.code, "errMsg" to e.msg)
        } catch (e: Exception) {
            mapOf("appCode" to -1, "errMsg" to "取消费用结算认出错，错误信息：${e.message}，请将错误信息发给维护工程师")
        }
    }

    /** PRC_生成费用清单(L9449-9805): LB='1' 确认(TC93 六件套+TB96 自插头+意外险/经济补偿/补发/欠费联动)
     *  LB='2' 撤销(垫付/清欠守卫→删头→回'0'+联动复位)。源内 COMMIT 段在 PG 单事务内合并(铁律4)，
     *  成功路径可观测行为一致, 失败路径整批回滚(结构性修正, 回执注明)。 */
    @Transactional
    fun genFeeList(atb930: String, atc931: String, atc937: String, aae002: String,
                   lb: String, aae011: String): Map<String, Any?> {
        return try {
            val user = s.jdbc.queryForList("SELECT * FROM sys_org_user_unit WHERE aae011=?", aae011)
                .singleOrNull() ?: throw RuntimeException("ORA-01403: 未找到任何数据")
            val aa01 = s.single("aa01", "aaa001='GWWB'")
                ?: return mapOf("appCode" to -1, "errMsg" to "费用清单无法提交，原因是岗位外包税费比例未设置")
            if (lb == "1") {
                s.jdbc.update(
                    """UPDATE tc93 SET aae214='1', aae211=?, aae217=?, aae236=?, aae219=?, aae220=?, aae222=?
                       WHERE atc937=? AND atc931=?::numeric AND atb930=?""",
                    user["aae011"], user["aae017"], s.now(), user["aae019"], user["aae020"],
                    user["aae022"], atc937, atc931.toBigDecimal(), atb930.toLong())
                val nCnt = s.jdbc.queryForObject(
                    "SELECT COUNT(1) FROM tb96 WHERE atc937=? AND aae002=?::numeric AND atb930=?",
                    Long::class.java, atc937, atc931.toBigDecimal(), atb930.toLong()) ?: 0L
                if (nCnt > 0) throw SettleRet(-1, "此费用清单已提交，无法重复提交")
                if (atc937 == "1") {
                    s.jdbc.update("UPDATE tc05 SET atc05z='1' WHERE aae002=?::numeric AND atb930=?",
                        atc931.toBigDecimal(), atb930.toLong())
                    s.jdbc.update(
                        "UPDATE td04 SET atc9p4='1' WHERE atd04c='1' AND aae002=?::numeric AND atb930=?",
                        atc931.toBigDecimal(), atb930.toLong())
                    s.jdbc.update("UPDATE ac10 SET atc9t4='3' WHERE aae002=?::numeric AND atb930=?",
                        atc931.toBigDecimal(), atb930.toLong())
                    s.jdbc.update(
                        "UPDATE tc07 SET atc07z='1' WHERE atc07z='0' AND aae002=?::numeric AND atb930=?",
                        atc931.toBigDecimal(), atb930.toLong())
                }
                // TB96 头(AB01×TB90×TB93 联接)
                val head = s.jdbc.queryForList(
                    """SELECT c.atb930, c.atb931, a.aab001, a.aab004, a.aaa030, a.aaa031, c.atb90ae
                       FROM ab01 a, tb90 b, tb93 c WHERE a.aab001=b.aab001 AND b.atb900=c.atb900
                       AND c.atb930=?""",
                    atb930.toLong()).singleOrNull()
                    ?: return mapOf("appCode" to -1, "errMsg" to "费用清单确认出错，错误原因结算单元信息选择错误")
                val atb965 = s.jdbc.queryForObject(
                    "SELECT COALESCE(SUM(atc93bf),0) FROM tc93 WHERE aae214='1' AND atc937=? AND atc931=?::numeric AND atb930=?",
                    BigDecimal::class.java, atc937, atc931.toBigDecimal(), atb930.toLong())
                val atb96z = "1"
                var atb963 = BigDecimal.ZERO
                var atb964 = BigDecimal.ZERO
                var atb961 = atb965
                if (s.str(head["atb90ae"]) == "04") {
                    atb963 = s.nvl(aa01["aaa005"])
                    if (s.long(head["atb930"]) == 38147L) atb963 = BigDecimal("0.07")
                    atb964 = atb965.multiply(atb963).setScale(2, java.math.RoundingMode.HALF_UP)
                    atb961 = atb965.add(atb964)
                }
                try {
                    s.jdbc.update(
                        """INSERT INTO tb96 (baz001, baz002, atb960, atb930, atb931, aab001, aab004,
                           aaa030, aaa031, atc937, aae002, atb961, atb962, atb96z, aae013, aae011,
                           aae017, aae036, aae019, aae020, aae022, atb963, atb964, atb965, atb96y)
                           VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?::numeric, ?, 0, ?, NULL, ?, ?, ?, ?, ?, ?, ?, ?, ?, '0')""",
                        s.jdbc.queryForObject("SELECT nextval('seq_sys_baz001')", Long::class.java),
                        s.jdbc.queryForObject("SELECT nextval('seq_sys_baz002')", Long::class.java),
                        s.jdbc.queryForObject("SELECT nextval('seq_tb96_atb960')", Long::class.java),
                        head["atb930"], head["atb931"], head["aab001"], head["aab004"],
                        head["aaa030"], head["aaa031"], atc937, atc931.toBigDecimal(),
                        atb961, atb96z, user["aae011"], user["aae017"], s.now(),
                        user["aae019"], user["aae020"], user["aae022"], atb963, atb964, atb965)
                } catch (e: Exception) {
                    if (s.isDup(e)) {
                        s.jdbc.update(
                            """UPDATE tb96 SET atb961=?, atb962=0, atb963=?, atb964=?, atb965=?,
                               aae011=?, aae017=?, aae036=?, aae019=?, aae020=?, aae022=?
                               WHERE atc937=? AND aae002=?::numeric AND atb930=?""",
                            atb961, atb963, atb964, atb965, user["aae011"], user["aae017"],
                            s.now(), user["aae019"], user["aae020"], user["aae022"],
                            atc937, atc931.toBigDecimal(), atb930.toLong())
                    } else return mapOf("appCode" to -1,
                        "errMsg" to "费用清单确认出错，错误原因无法记录汇总信息：${e.message}")
                }
                // 个人清欠(TC93.ATC93X3>0 时; BC-09 夹具为 0 不触发, 逻辑忠实保留)
                val recs = s.jdbc.queryForList(
                    "SELECT * FROM tc93 WHERE atc931=?::numeric AND atc937=? AND atb930=? ORDER BY atc900",
                    atc931.toBigDecimal(), atc937, atb930.toLong())
                for (r93 in recs) {
                    if (nz(r93["atc93x3"]) > BigDecimal.ZERO) {
                        var left = nz(r93["atc93x3"])
                        val books = s.jdbc.queryForList(
                            "SELECT * FROM tb97 WHERE dstatus='1' AND atb973 > 0 AND atb930=? AND aac001=?",
                            r93["atb930"], r93["aac001"])
                        for (b in books) {
                            val bal = s.nvl(b["atb973"])
                            var a1: BigDecimal
                            var newBal: BigDecimal
                            var newUsed: BigDecimal
                            if (bal >= left && left > BigDecimal.ZERO) {
                                a1 = left; newBal = bal - left
                                newUsed = s.nvl(b["atb972"]) + left; left = BigDecimal.ZERO
                            } else if (bal < left && left > BigDecimal.ZERO) {
                                a1 = bal; newBal = BigDecimal.ZERO
                                newUsed = s.nvl(b["atb972"]) + a1; left -= a1
                            } else continue
                            s.jdbc.update(
                                """INSERT INTO tb9a (atb9a0, atb970, ukey, atb9a1, atb9a2, dstatus,
                                   aae013, aae011, aae017, aae036, aae019, aae020, aae022)
                                   VALUES (?, ?, ?, ?, '2', '1', NULL, ?, ?, ?, ?, ?, ?)""",
                                s.jdbc.queryForObject("SELECT nextval('seq_tb9a_atb9a0')", Long::class.java),
                                b["atb970"], r93["atc930"], a1, r93["aae011"], r93["aae017"],
                                s.now(), r93["aae019"], r93["aae020"], r93["aae022"])
                            s.jdbc.update("UPDATE tb97 SET atb973=?, atb972=? WHERE atb970=?",
                                newBal, newUsed, b["atb970"])
                        }
                        if (left > BigDecimal.ZERO) throw SettleRet(-1,
                            "不符合结算条件，原因为：${r93["aac003"]}个人交现金部分未到账或者余额不足")
                    }
                    val prev = s.prevYm(s.str(r93["atc931"])!!)
                    s.jdbc.update(
                        """UPDATE tc93 SET keys_1=?::text, atc93w='0'
                           WHERE atc93e <> 0 AND atc931=?::numeric AND atc900=?""",
                        s.str(r93["atc930"]), prev.toBigDecimal(), r93["atc900"])
                }
            } else if (lb == "2") {
                val tb96 = s.single("tb96", "atc937=? AND aae002=?::numeric AND atb930=?",
                    atc937, atc931.toBigDecimal(), atb930.toLong())
                    ?: return mapOf("appCode" to -1, "errMsg" to "无法撤销结算清单，原因是未提交清单")
                val n98 = s.jdbc.queryForObject(
                    "SELECT COUNT(1) FROM tb98 WHERE dstatus='1' AND tkey=?",
                    Long::class.java, tb96["atb960"]) ?: 0L
                if (n98 > 0) throw SettleRet(-1, "财务已垫付，无法撤销")
                val n9a = s.jdbc.queryForObject(
                    "SELECT COUNT(1) FROM tb9a WHERE dstatus='1' AND ukey=?",
                    Long::class.java, tb96["atb960"]) ?: 0L
                if (n9a > 0) throw SettleRet(-1, "此费用清单已清欠，无法撤销")
                s.jdbc.update("DELETE FROM tb96 WHERE atb960=?", tb96["atb960"])
                s.jdbc.update(
                    """UPDATE tc93 SET aae214='0', aae211=?, aae217=?, aae236=?, aae219=?, aae220=?, aae222=?
                       WHERE atc937=? AND atc931=?::numeric AND atb930=?""",
                    user["aae011"], user["aae017"], s.now(), user["aae019"], user["aae020"],
                    user["aae022"], atc937, atc931.toBigDecimal(), atb930.toLong())
                if (atc937 == "1") {
                    s.jdbc.update("UPDATE tc05 SET atc05z='0' WHERE aae002=?::numeric AND atb930=?",
                        atc931.toBigDecimal(), atb930.toLong())
                    s.jdbc.update(
                        "UPDATE td04 SET atc9p4='0' WHERE atd04c='1' AND aae002=?::numeric AND atb930=?",
                        atc931.toBigDecimal(), atb930.toLong())
                    s.jdbc.update("UPDATE ac10 SET atc9t4='1' WHERE aae002=?::numeric AND atb930=?",
                        atc931.toBigDecimal(), atb930.toLong())
                    s.jdbc.update(
                        "UPDATE tc07 SET atc07z='0' WHERE atc07z='1' AND aae002=?::numeric AND atb930=?",
                        atc931.toBigDecimal(), atb930.toLong())
                }
                val recs = s.jdbc.queryForList(
                    "SELECT * FROM tc93 WHERE atc931=?::numeric AND atc937=? AND atb930=? ORDER BY atc900",
                    atc931.toBigDecimal(), atc937, atb930.toLong())
                for (r93 in recs) {
                    if (nz(r93["atc93x3"]) > BigDecimal.ZERO) {
                        val t9as = s.jdbc.queryForList(
                            "SELECT * FROM tb9a WHERE dstatus='1' AND atb9a2='2' AND ukey=?",
                            r93["atc930"])
                        for (t in t9as) {
                            s.jdbc.update(
                                "UPDATE tb97 SET atb973=atb973+?, atb972=atb972-? WHERE atb970=?",
                                s.nvl(t["atb9a1"]), s.nvl(t["atb9a1"]), t["atb970"])
                            s.jdbc.update(
                                """UPDATE tb9a SET dstatus='-1', aaea11=?, aaea17=?, aaea36=?,
                                   aaea19=?, aaea20=?, aaea22=? WHERE atb9a0=?""",
                                r93["aae011"], r93["aae017"], s.now(),
                                r93["aae019"], r93["aae020"], r93["aae022"], t["atb9a0"])
                        }
                    }
                    val prev = s.prevYm(s.str(r93["atc931"])!!)
                    s.jdbc.update(
                        "UPDATE tc93 SET keys_1=NULL, atc93w='1' WHERE atc93e <> 0 AND atc931=?::numeric AND atc900=?",
                        prev.toBigDecimal(), r93["atc900"])
                }
            }
            mapOf("appCode" to 1, "errMsg" to null as String?)
        } catch (e: SettleRet) {
            mapOf("appCode" to e.code, "errMsg" to e.msg)
        } catch (e: Exception) {
            mapOf("appCode" to -1, "errMsg" to "费用清单确认出错，错误信息：${e.message}，请将错误信息发给维护工程师")
        }
    }

    private fun nz(v: Any?): BigDecimal = s.nvl(v)
}

// ==================== /proc 注册面(五过程) ====================

@Component
class TdExtractProc(private val legs: TdSettleLegs) : ProcHandler {
    override fun procName() = "PKG_费用结算.PRC_提取上月工资"
    override fun handle(params: Map<String, Any?>): Map<String, Any?> {
        for (k in listOf("atb930", "atc931", "atc937", "aae011"))
            if (params[k] == null) return mapOf("appCode" to -1, "errMsg" to "缺少 $k")
        return legs.extractPriorMonth(params["atb930"]!!.toString(), params["atc931"]!!.toString(),
            params["atc937"]!!.toString(), params["aae011"]!!.toString())
    }
}

@Component
class TdFeeSettleProc(private val calc: TdSettleCalc) : ProcHandler {
    override fun procName() = "PKG_费用结算.PRC_费用结算"
    override fun handle(params: Map<String, Any?>): Map<String, Any?> {
        for (k in listOf("atb930", "atc931", "atc937", "atc932", "aae011"))
            if (params[k] == null) return mapOf("appCode" to -1, "errMsg" to "缺少 $k")
        return calc.feeSettle(params["atb930"]!!.toString(), params["atc931"]!!.toString(),
            params["atc937"]!!.toString(), params["atc932"]!!.toString(), params["aae011"]!!.toString())
    }
}

@Component
class TdCancelSettleProc(private val legs: TdSettleLegs) : ProcHandler {
    override fun procName() = "PKG_费用结算.PRC_撤销结算"
    override fun handle(params: Map<String, Any?>): Map<String, Any?> {
        for (k in listOf("atb930", "atc931", "atc937", "atc932"))
            if (params[k] == null) return mapOf("appCode" to -1, "errMsg" to "缺少 $k")
        return legs.cancelSettle(params["atb930"]!!.toString(), params["atc931"]!!.toString(),
            params["atc937"]!!.toString(), params["atc932"]!!.toString())
    }
}

@Component
class TdGenFeeListProc(private val legs: TdSettleLegs) : ProcHandler {
    override fun procName() = "PKG_费用结算.PRC_生成费用清单"
    override fun handle(params: Map<String, Any?>): Map<String, Any?> {
        for (k in listOf("atb930", "atc931", "atc937", "aae002", "lb", "aae011"))
            if (params[k] == null) return mapOf("appCode" to -1, "errMsg" to "缺少 $k")
        return legs.genFeeList(params["atb930"]!!.toString(), params["atc931"]!!.toString(),
            params["atc937"]!!.toString(), params["aae002"]!!.toString(),
            params["lb"]!!.toString(), params["aae011"]!!.toString())
    }
}
