package com.jlrc.rcpq.service.td

import org.springframework.stereotype.Component
import java.math.BigDecimal

/** 四份虚拟 TC93 名单子过程(桥单 20261127-034; 源 PACKAGE_BODY_PKG_费用结算.sql L235-747)。 */
@Component
class TdSettleListCases(
    private val s: TdSettleSupport,
    private val rateCases: TdSettleRateCases,
) {

    /** ②PRC_虚拟代理工资(L235-362): aae00p∈('6','5') 无目标月行者补零行, aae013=NULL */
    fun virtualAgency(atb930: String, atc931: String, atc937: String, aae011: String) {
        val tb93 = s.single("tb93", "atb930=?", atb930.toLong())
            ?: throw SettleRet(-1, "查询结算单元出错，无法结算")
        val user = try { s.userRow(aae011) } catch (e: Exception) {
            throw SettleRet(-1, "查询经办人信息出错，无法结算")
        }
        var nAae002 = s.nowYm()
        if (atc931 > nAae002) nAae002 = atc931
        val rows = s.jdbc.queryForList(
            """SELECT a.* FROM tc90 a, tb93 b WHERE a.atb930=b.atb930
               AND (a.atb90r='3' OR (a.atb90r='5' AND ((a.atc90p='0' AND a.aaa202 > ?::numeric)
                    OR (a.atc90pa='0' AND a.aaa602 > ?::numeric)
                    OR (a.atc90pb='0' AND a.aaa902 > ?::numeric)
                    OR (a.atc90w='0' AND a.aaa502 > ?::numeric) OR a.atc90av >= ?::numeric))
                    OR (SUBSTR(a.aaa110,3,2)='99' AND INSTR(a.aac008,'80')>0))
               AND a.aae00p IN ('6','5') AND a.aae002 <= ?::numeric
               AND NOT EXISTS (SELECT 1 FROM tc93 WHERE atc937=? AND atc931=?::numeric
                    AND atb930=? AND atc900=a.atc900)
               AND a.atb930=?""",
            atc931.toBigDecimal(), atc931.toBigDecimal(), atc931.toBigDecimal(),
            atc931.toBigDecimal(), nAae002.toBigDecimal(), atc931.toBigDecimal(),
            atc937, atc931.toBigDecimal(), atb930.toLong(), atb930.toLong())
        for (tc90 in rows) {
            val row = s.virtualRow(tc90, tb93, user, atc931, atc937, nAae002, null, s.now(), null)
            try {
                s.insertRow("tc93", row)
            } catch (e: Exception) {
                throw SettleRet(-1, "生成代理人员虚拟工资出错，原因为：${e.message}")
            }
        }
    }

    /** ③PRC_补退历史名单(L363-500): 逐人补退明细 + TC07 待确认行补零行, aae013='补退历史' */
    fun buiTuiHistory(atb930: String, atc931: String, atc937: String, aae011: String) {
        val tb93 = s.single("tb93", "atb930=?", atb930.toLong())
            ?: throw SettleRet(-1, "查询结算单元出错，无法结算")
        val user = try { s.userRow(aae011) } catch (e: Exception) {
            throw SettleRet(-1, "查询经办人信息出错，无法结算")
        }
        if (atc931.toLong() > 202012) {
            val people = s.jdbc.queryForList(
                """SELECT atc900 FROM tc90 WHERE aae00p IN ('1','5','7')
                   AND ((atb90r='3' AND aae002 <= ?::numeric) OR aae003 >= ?::numeric)
                   AND atb930=?""",
                atc931.toBigDecimal(),
                ((atc931.substring(0, 4).toInt() - 1).toString() + "01").toBigDecimal(),
                atb930.toLong())
            for (r in people) {
                try {
                    rateCases.insuranceDetail(s.long(r["atc900"])!!, atc931, aae011)
                } catch (e: SettleRet) {
                    throw SettleRet(-1, "计算补退信息出错，无法结算${r["atc900"]}")
                }
            }
        }
        var nAae002 = s.nowYm()
        if (atc931 > nAae002) nAae002 = atc931
        val rows = s.jdbc.queryForList(
            """SELECT a.* FROM tc90 a, tc07 b WHERE a.atc900=b.atc900
               AND b.atc07z='0' AND b.aae002=?::numeric AND b.atb930=?""",
            atc931.toBigDecimal(), atb930.toLong())
        for (tc90 in rows) {
            val row = s.virtualRow(tc90, tb93, user, atc931, atc937, nAae002,
                "补退历史", s.truncNow(), null)
            try {
                s.insertRow("tc93", row)
            } catch (e: Exception) {
                if (s.isDup(e)) {
                    // DUP_VAL_ON_INDEX → PRM_CODE:=DEF_OK 忠实吞掉
                } else throw SettleRet(-1, "生成代理人员虚拟工资出错，原因为：${e.message}")
            }
        }
    }

    /** ④PRC_历史欠款名单(L501-623): 上月 atc93e<>0∧atc93w='1'∧aae214='1' 人补零行, aae013='欠费' */
    fun debtList(atb930: String, atc931: String, atc937: String, aae011: String) {
        val tb93 = s.single("tb93", "atb930=?", atb930.toLong())
            ?: throw SettleRet(-1, "查询结算单元出错，无法结算")
        val user = try { s.userRow(aae011) } catch (e: Exception) {
            throw SettleRet(-1, "查询经办人信息出错，无法结算")
        }
        var nAae002 = s.nowYm()
        if (atc931 > nAae002) nAae002 = atc931
        val prev = s.prevYm(atc931).toLong()
        val rows = s.jdbc.queryForList(
            """SELECT a.* FROM tc90 a, tc93 b WHERE a.atc900=b.atc900
               AND b.aae214='1' AND b.atc93e <> 0 AND b.atc93w='1'
               AND b.atc931=? AND b.atb930=?""",
            prev, atb930.toLong())
        for (tc90 in rows) {
            val row = s.virtualRow(tc90, tb93, user, atc931, atc937, nAae002,
                "欠费", s.now(), null)
            try {
                s.insertRow("tc93", row)
            } catch (e: Exception) {
                if (s.isDup(e)) {
                    // DUP_VAL_ON_INDEX → 忠实吞掉
                } else throw SettleRet(-1, "生成代理人员虚拟工资出错，原因为：${e.message}")
            }
        }
    }

    /** ⑤PRC_经济补偿金名单(L624-747): TD04 驱动补行 atc93m=atd043, DUP→UPDATE ATC93M */
    fun compensation(atb930: String, atc931: String, atc937: String, aae011: String) {
        val tb93 = s.single("tb93", "atb930=?", atb930.toLong())
            ?: throw SettleRet(-1, "查询结算单元出错，无法结算")
        val user = try { s.userRow(aae011) } catch (e: Exception) {
            throw SettleRet(-1, "查询经办人信息出错，无法结算")
        }
        var nAae002 = s.nowYm()
        if (atc931 > nAae002) nAae002 = atc931
        val rows = s.jdbc.queryForList(
            """SELECT a.*, b.atd043 FROM tc90 a, td04 b WHERE a.atc900=b.atc900
               AND b.aae114='1' AND b.atd04c='1' AND b.aae002=?::numeric
               AND b.atc9p4='0' AND b.atb930=?""",
            atc931.toBigDecimal(), atb930.toLong())
        for (tc90 in rows) {
            val row = s.virtualRow(tc90, tb93, user, atc931, atc937, nAae002,
                null, s.now(), s.nvl(tc90["atd043"]))
            try {
                s.insertRow("tc93", row)
            } catch (e: Exception) {
                if (s.isDup(e)) {
                    s.jdbc.update(
                        """UPDATE tc93 SET atc93m=? WHERE atc931=?::numeric AND atc937=?
                           AND atb930=? AND atc900=?""",
                        row["atc93m"], atc931.toBigDecimal(), atc937, row["atb930"], row["atc900"])
                } else throw SettleRet(-1, "生成代理人员虚拟工资出错，原因为：${e.message}")
            }
        }
    }
}
