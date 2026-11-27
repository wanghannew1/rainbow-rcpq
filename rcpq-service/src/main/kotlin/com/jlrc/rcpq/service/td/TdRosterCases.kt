package com.jlrc.rcpq.service.td

import com.jlrc.rcpq.service.proc.ProcHandler
import org.springframework.stereotype.Component
import java.math.BigDecimal

/**
 * PKG_工资发放.PRC_工资花名册(L2121-2352) 移植 — 桥单 20261127-045(034 记账项转正:
 * 043 扩展腿实证 TC93 目标月空 → 自动花名册分支为门开人路径必经)。
 * 语义: 经办人/单元行(V_AB01_TB90_TB93)裸 SELECT INTO → 待审核守卫('不符合生成工资
 * 花名册条件') → 清未确认行(aae214<>'1') → N_AAE002=max(now, 结算月) → TC90 联合选择
 * (atb90r='3' 或 ('5'+atc90av>=) 或 四旗标 0+停办年月 NULL 或 省99+农民工80; aae002<=
 * 结算月; UNION atb90r='3'+atc90c 月<=) 逐人造零行 TC93: atc93n='1'(假设上月在),
 * aae013='导出名册生成', atc93u=DECODE(atb90ac,'01','2',NULL), atc93w1..28=0,
 * 经办五件套。DUP→静默(源 DEF_OK)。错误文案 '保存导出工资信息出错...'。
 */
@Component
class TdRosterCases(private val s: TdSettleSupport) {

    fun roster(atb930: String, atc931: String, atc937: String, aae011: String) {
        try {
            val user = s.jdbc.queryForList("SELECT * FROM sys_org_user_unit WHERE aae011=?", aae011)
                .singleOrNull() ?: throw SettleRet(-1, "保存导出工资信息出错，错误信息：ORA-01403: 未找到任何数据 (SYS_ORG_USER_UNIT)，请将错误信息发给维护工程师")
            val dw = s.jdbc.queryForList(
                """SELECT atb90a, atb90b, atb90ac, aab004, atb931, atb90y
                   FROM v_ab01_tb90_tb93 WHERE atb930=?""", atb930.toLong()).singleOrNull()
                ?: throw SettleRet(-1, "保存导出工资信息出错，错误信息：ORA-01403: 未找到任何数据 (V_AB01_TB90_TB93)，请将错误信息发给维护工程师")
            val nCnt = s.jdbc.queryForObject(
                """SELECT COUNT(1) FROM tc90 a, tb91 b WHERE a.atc900=b.atb91z
                   AND (b.atb912='2' OR a.atb90r='2') AND a.atb930=?""",
                Long::class.java, atb930.toLong()) ?: 0L
            if (nCnt > 0) throw SettleRet(-1, "该结算单元存在待审核员工信息，不符合生成工资花名册条件")
            s.jdbc.update(
                """DELETE FROM tc93 WHERE aae214<>'1' AND atc937=? AND atc931=?::numeric
                   AND atb930=?""",
                atc937, atc931.toBigDecimal(), atb930.toLong())
            var nAae002 = s.nowYm()
            if (atc931 > nAae002) nAae002 = atc931
            val items = s.jdbc.queryForList(
                """SELECT * FROM (
                     SELECT * FROM tc90 WHERE (atb90r='3'
                       OR (atb90r='5' AND atc90av>=?::numeric)
                       OR (atc90p='0' AND aaa202 IS NULL)
                       OR (atc90pa='0' AND aaa602 IS NULL)
                       OR (atc90pb='0' AND aaa902 IS NULL)
                       OR (atc90w='0' AND aaa502 IS NULL)
                       OR (SUBSTR(aaa110,3,2)='99' AND INSTR(aac008,'80')>0))
                       AND aae002<=?::numeric AND atb930=?
                     UNION
                     SELECT * FROM tc90 WHERE atb90r='3'
                       AND TO_CHAR(atc90c,'yyyymm')<=? AND atb930=?
                   ) ORDER BY atc900""",
                atc931.toBigDecimal(), atc931.toBigDecimal(), atb930.toLong(),
                atc931, atb930.toLong())
            for (item in items) {
                val z = BigDecimal.ZERO
                val row = linkedMapOf<String, Any?>(
                    "atc930" to s.jdbc.queryForObject(
                        "SELECT nextval('seq_tc93_atc930')", Long::class.java), // 源首列 SEQ_TC93_ATC930.NEXTVAL
                    "atb900" to item["atb900"], "atc900" to item["atc900"],
                    "aab001" to item["aab001"], "aab004" to dw["aab004"],
                    "atb930" to item["atb930"], "atb931" to dw["atb931"],
                    "atc931" to atc931.toLong(), "atc932" to atc931.toLong(), "atc937" to atc937,
                    "aac001" to item["aac001"], "aac003" to item["aac003"], "aac002" to item["aac002"],
                    "atb90a" to z, "atb90z" to z, "atc9p2" to z, "atc9p3" to z,
                    "atc93g" to "0", "atc93n" to "1", "atc93p" to "0", "atc93w" to "0",
                    "aae114" to "0", "aae214" to "0", "aae314" to "0", "aae036" to s.now(),
                    "atc93x" to z, "atc933" to z, "atc934" to z, "atc935" to z, "atc936" to z,
                    "atc93k" to z, "atc93l" to z, "atc93m" to z, "atc93y" to z,
                    "atc938" to z, "atc939" to z, "atc93a" to z, "atc93b" to z,
                    "atc93h" to z, "atc93i" to z, "atc93j" to z, "atc93z" to z,
                    "atc93aa" to z,
                    "aae013" to "导出名册生成",
                    "aae002" to nAae002.toLong(),
                    "atc93u" to (if (s.str(dw["atb90ac"]) == "01") "2" else null),
                    "aae001" to atc931.toLong(),
                    "aae011" to user["aae011"], "aae017" to user["aae017"],
                    "aae019" to user["aae019"], "aae020" to user["aae020"], "aae022" to user["aae022"],
                    "atc93w1" to z, "atc93w2" to z, "atc93w3" to z, "atc93w4" to z,
                    "atc93w5" to z, "atc93w6" to z, "atc93w7" to z, "atc93w8" to z,
                    "atc93w9" to z, "atc93w10" to z, "atc93w11" to z, "atc93w12" to z,
                    "atc93w13" to z, "atc93w14" to z, "atc93w15" to z, "atc93w16" to z,
                    "atc93w17" to z, "atc93w18" to z, "atc93w19" to z, "atc93w20" to z,
                    "atc93w21" to z, "atc93w22" to z, "atc93w23" to z, "atc93w24" to z,
                    "atc93w25" to z, "atc93w26" to z, "atc93w27" to z, "atc93w28" to z,
                )
                try {
                    s.insertRow("tc93", row)
                } catch (e: Exception) {
                    // DUP_VAL_ON_INDEX → PRM_CODE:=DEF_OK 忠实静默
                    if (!s.isDup(e)) throw SettleRet(-1,
                        "保存导出工资信息出错，错误信息：${e.cause?.message ?: e.message}，请将错误信息发给维护工程师")
                }
            }
        } catch (e: SettleRet) {
            throw e
        } catch (e: Exception) {
            throw SettleRet(-1, "保存导出工资信息出错，错误信息：${e.cause?.message ?: e.message}，请将错误信息发给维护工程师")
        }
    }
}

/** /proc 注册面: PKG_工资发放.PRC_工资花名册 */
@Component
class TdRosterProc(private val c: TdRosterCases) : ProcHandler {
    override fun procName() = "PKG_工资发放.PRC_工资花名册"
    override fun handle(params: Map<String, Any?>): Map<String, Any?> {
        val atb930 = params["atb930"]?.toString() ?: params["prm_atb930"]?.toString()
            ?: return mapOf("appCode" to -1, "errMsg" to "缺少 atb930")
        val atc931 = params["atc931"]?.toString() ?: params["prm_atc931"]?.toString()
            ?: return mapOf("appCode" to -1, "errMsg" to "缺少 atc931")
        val atc937 = params["atc937"]?.toString() ?: params["prm_atc937"]?.toString()
            ?: return mapOf("appCode" to -1, "errMsg" to "缺少 atc937")
        val aae011 = params["aae011"]?.toString() ?: params["prm_aae011"]?.toString()
            ?: return mapOf("appCode" to -1, "errMsg" to "缺少 aae011")
        return try {
            c.roster(atb930, atc931, atc937, aae011)
            mapOf("appCode" to 1, "errMsg" to null as String?)
        } catch (e: SettleRet) {
            mapOf("appCode" to e.code, "errMsg" to e.msg)
        }
    }
}
