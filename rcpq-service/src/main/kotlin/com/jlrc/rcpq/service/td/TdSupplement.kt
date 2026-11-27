package com.jlrc.rcpq.service.td

import com.jlrc.rcpq.service.calc.InsuranceCalcEngine
import com.jlrc.rcpq.service.proc.ProcHandler
import org.springframework.stereotype.Component
import java.math.BigDecimal

/**
 * PKG_保险补退.PRC_保险补退明细 链移植(桥单 20261127-041 首波; 2026-11-27)。
 *
 * 源: PACKAGE_BODY_PKG_保险补退.sql(7824 行, .60 ccrcpq 包体 INVALID 但 RCPQ_TEST 场
 * VALID 2026-11-27 重编译=实弹背书; 行区间逐字移植):
 *   编排 PRC_保险补退明细 L2906-3086(费用结算 PRC_补退历史名单 L393-413 循环调用点)
 *   五子 PRC_社保补退明细 L3087-3773 / PRC_医保补退明细 L3775-5358 /
 *        PRC_工伤补退明细 L5360-6470 / PRC_公积金补退明细 L6472-7504 /
 *        PRC_保险补差明细 L7506-7823
 *   助手 PRC_获取补退基数 L23-82 / PRC_保险补收基数_个人 L1356-1721(包内版, 与
 *        在册的 PKG_人才派遣公共 同名件差 AA97 次年基数段 64 行, 故按包内版移植)
 * 036 单门控双轨遗留#1 至此闭环: insuranceDetail 门开路径=真实现。
 * PG 适配: TC07 唯一键=PK(seq_tc07_atc070)→DUP 分支死键, 插入直插; TC08 uk(aae000,
 * atc900,aaa000) 可达→插前预检(社保段命中=静默跳过, 医保/公积金段命中=UPDATE AAA300,
 * 与源 DUP/更新语义等价且规避 PG 毒化)。Oracle 逐子 COMMIT 在 PG 共享事务内不落——
 * 错误路径先前子过程产出在 Oracle 已提交而在新侧随外层事务, 回执注明。
 * 源码笔误忠实: 医保市版 '23' GZHJ 双计 ATB90T; 编排体 N_DLF_3 被保险补差复写; 公积金
 * 市版 '30' JSNY 仅两分支(AAA503>N_AAE002 → NULL → 报错)。
 */
@Component
class TdSupplementCases(
    val s: TdSettleSupport,
    private val engine: InsuranceCalcEngine,
) {

    /** 编排体(L2906-3086): 用户/合同守卫+TC07 清场+五子顺序+代理费月数尾插 */
    fun detail(atc900: Long, aae002: String, aae011: String) {
        try {
            val user = try { s.userRow(aae011) } catch (e: Exception) {
                throw SettleRet(-1, "无法计算补退，原因为经办人信息不正确")
            }
            var tc90 = s.single("v_tc90_qpkg", "atc900=?", atc900)
                ?: throw SettleRet(-1, "无法计算补退，原因为查询合同信息出错")
            if (s.str(tc90["atb90r"]) != "3" && s.str(tc90["aae003"])!! < aae002) {
                tc90 = s.single(
                    "v_tc90_qpkg",
                    "atc900=(SELECT MAX(atc900) FROM tc90 WHERE aae00p IN ('1','5','7') " +
                        "AND atb930=? AND aac001=?)",
                    tc90["atb930"], tc90["aac001"])
                    ?: throw SettleRet(-1, "无法计算补退，原因为查询合同信息出错")
            }
            s.jdbc.update(
                "DELETE FROM tc07 WHERE atc07z='0' AND datasource='30' AND aae002=?::numeric " +
                    "AND atb930=? AND aac001=?",
                aae002.toBigDecimal(), tc90["atb930"], tc90["aac001"])
            val t = template(tc90, user, aae002)
            val dlf1 = sheBao(aae002, tc90, t)
            val dlf2 = yiBao(aae002, tc90, t)
            val dlf4 = gongShang(aae002, tc90, t)
            val dlf3 = gongJijin(aae002, tc90, t)
            buCha(aae002, tc90, t) // 源笔误: 复写 N_DLF_3, 返回值弃用
            if (dlf1 > 0 || dlf2 > 0 || dlf3 > 0) {
                val fz = HashMap(t)
                fz["aaa913"] = maxOf(dlf1, dlf2, dlf3).toBigDecimal() // 取最大月数
                fz["aae003"] = t["aae002"] // 源: R_TC07_FZ.AAE003 := R_TC07.AAE002
                fz["atc072"] = "80"
                fz["atc070"] = -1L // 占位, insertRowOrInline 用 nextval 内联
                try {
                    val cols2 = fz.keys.filter { it.lowercase() in s.tableCols("tc07") && it != "atc070" }
                    val sql2 = "INSERT INTO tc07 (atc070, " + cols2.joinToString(",") +
                        ") VALUES (nextval('seq_tc07_atc070'), " + cols2.joinToString(",") { "?" } + ")"
                    s.jdbc.update(sql2, *cols2.map { fz[it] }.toTypedArray())
                } catch (e: Exception) {
                    if (!s.isDup(e)) throw SettleRet(-1, "记录代理费补缴月数信息出错，原因为${e.cause?.message ?: e.message}")
                }
            }
        } catch (e: SettleRet) {
            throw e // 子过程报错等价 PRM_CODE<>OK 直接返回
        } catch (e: Exception) {
            throw SettleRet(-1, "生成补退信息出错，错误信息：${e.cause?.message ?: e.message}，请将错误信息发给维护工程师")
        }
    }

    /** TC07 模板(L2946-2990): 五子公共行基座(零值+R_TC90 身份列+经办人) */
    internal fun template(
        tc90: Map<String, Any?>, user: Map<String, Any?>, aae002: String,
    ): MutableMap<String, Any?> = mutableMapOf(
        "atc70x" to "1", "atc70y" to "1", "atc073" to "5",
        "aae002" to aae002.toBigDecimal(),
        "atc900" to tc90["atc900"], "aac001" to tc90["aac001"],
        "aaa001" to tc90["aaa001"], "aaa004" to tc90["aaa004"], "aaa005" to tc90["aaa005"],
        "aac003" to tc90["aac003"], "aac002" to tc90["aac002"],
        "atb930" to tc90["atb930"], "atb931" to tc90["atb931"], "atb900" to tc90["atb900"],
        "aab001" to tc90["aab001"], "aab004" to tc90["aab004"],
        "aaa030" to tc90["aaa030"], "aaa031" to tc90["aaa031"],
        "bba001" to BigDecimal.ZERO, "baa001" to BigDecimal.ZERO,
        "bba003" to BigDecimal.ZERO, "baa003" to BigDecimal.ZERO,
        "bba002" to BigDecimal.ZERO, "bba004" to BigDecimal.ZERO,
        "bba005" to BigDecimal.ZERO, "baa002" to BigDecimal.ZERO,
        "atb90t" to BigDecimal.ZERO, "daa001" to BigDecimal.ZERO,
        "caa001" to BigDecimal.ZERO, "caa002" to BigDecimal.ZERO,
        "bba200" to BigDecimal.ZERO, "baa200" to BigDecimal.ZERO,
        "daa002" to BigDecimal.ZERO,
        "datasource" to "30", "atc07z" to "0",
        "aae011" to user["aae011"], "aae017" to user["aae017"],
        "aae036" to s.now(), "aae019" to user["aae019"],
        "aae020" to user["aae020"], "aae022" to user["aae022"],
    )

    /** TC07 行插入; atc070 内联 nextval 单语句(独立 nextval+insert 两语句在池化连接上
     *  存在语句间隙死亡面, 2026-11-27 探针实证: DELETE 后 nextval 未达服务器; 单语句化规避);
     *  源 DUP 分支=PK 死键, isDup 时静默; 返回是否实际落库 */
    internal fun ins07(row: MutableMap<String, Any?>): Boolean {
        val cols = row.keys.filter { it.lowercase() in s.tableCols("tc07") && it != "atc070" }
        val sql = "INSERT INTO tc07 (atc070, ${cols.joinToString(",")}) " +
            "VALUES (nextval('seq_tc07_atc070'), ${cols.joinToString(",") { "?" }})"
        try {
            return s.jdbc.update(sql, *cols.map { row[it] }.toTypedArray()) > 0
        } catch (e: Exception) {
            if (!s.isDup(e)) throw SettleRet(-1, "记录保险补退信息出错，原因为${e.cause?.message ?: e.message}")
            return false
        }
    }

    /** N_KSNY/N_JSNY 派生(源 ADD_MONTHS(TO_DATE(...)) 守卫: 非法年月→起止年月出错) */
    internal fun monthOrErr(ym: String?, months: Int): String =
        if (ym == null || !Regex("^\\d{6}$").matches(ym))
            throw SettleRet(-1, "无法计算补退，原因为计算补退起止年月出错")
        else try {
            s.monthAdd(ym, months)
        } catch (e: Exception) {
            throw SettleRet(-1, "无法计算补退，原因为计算补退起止年月出错")
        }

    /** N_BJ_BEGIN 族: TC07 已有补退最大月+1; 分支差异用参数表达(社保/医保/工伤/公积金/市) */
    internal fun bjBegin(kind: String, atc900: Long, atb930: Long, aac001: Long, aae002: String): Long {
        val sql = when (kind) {
            // 社保 L3113-3132: ATC07Z 0/1/9, datasource='30', 养老失业四列和, ATC900 键
            "sb" -> """SELECT CASE WHEN MAX(aae003) IS NULL THEN 202212
                ELSE (MAX(aae003)::bigint + 1) END FROM tc07 WHERE atc07z IN ('0','1','9')
                AND datasource='30' AND (COALESCE(bba001,0)+COALESCE(bba003,0)
                +COALESCE(baa001,0)+COALESCE(baa003,0)+COALESCE(aaa913,0)) <> 0
                AND aae002<?::numeric AND atc900=?"""
            // 医保 L3837-3860: ATC07Z 0/1, AAE003>=202012, AAE002<202210, 医疗四列和, ATC900 键
            "yb" -> """SELECT CASE WHEN MAX(aae003) IS NULL THEN 202212
                ELSE (MAX(aae003)::bigint + 1) END FROM tc07 WHERE atc07z IN ('0','1')
                AND aae003>=202012 AND aae002<202210 AND (COALESCE(bba002,0)+COALESCE(bba005,0)
                +COALESCE(atb90t,0)+COALESCE(baa002,0)) <> 0
                AND aae002<?::numeric AND atc900=?"""
            // 工伤 L5398-5417: ATC072='43', datasource='30', 医疗五列和, ATB930+AAC001 键
            "gs" -> """SELECT CASE WHEN MAX(aae003) IS NULL THEN 202212
                ELSE (MAX(aae003)::bigint + 1) END FROM tc07 WHERE atc07z IN ('0','1')
                AND atc072='43' AND datasource='30' AND (COALESCE(bba002,0)+COALESCE(bba004,0)
                +COALESCE(bba005,0)+COALESCE(atb90t,0)+COALESCE(baa002,0)) <> 0
                AND aae002<?::numeric AND atb930=? AND aac001=?"""
            // 公积金 L6516-6530: datasource='30', 公积金两列和, ATC900 键
            "gjj" -> """SELECT CASE WHEN MAX(aae003) IS NULL THEN 202212
                ELSE (MAX(aae003)::bigint + 1) END FROM tc07 WHERE atc07z IN ('0','1')
                AND datasource='30' AND (COALESCE(caa001,0)+COALESCE(caa002,0)) <> 0
                AND aae002<?::numeric AND atc900=?"""
            else -> throw IllegalArgumentException(kind)
        }
        return try {
            when (kind) {
                "gs" -> s.jdbc.queryForObject(sql, Long::class.java, aae002.toBigDecimal(), atb930, aac001)
                else -> s.jdbc.queryForObject(sql, Long::class.java, aae002.toBigDecimal(), atc900)
            }
        } catch (e: Exception) {
            // 源 EXCEPTION → N_BJ_BEGIN := N_AAE002(上月)
            s.monthAdd(aae002, -1).toLong()
        }
    }

    /** ATC072 已有最大月(基数补差水位): NVL(MAX(AAE003),202012), ATB930+AAC001 键 */
    internal fun bjN(code: String, atb930: Long, aac001: Long): Long =
        s.jdbc.queryForObject(
            """SELECT COALESCE(MAX(aae003),202012)::bigint FROM tc07
               WHERE atc07z IN ('0','1') AND atc072=? AND atb930=? AND aac001=?""",
            Long::class.java, code, atb930, aac001)

    /** 助手 PRC_获取补退基数(L23-82): 六基数 OUT; PRM_NF<结算年 → 先补收基数个人(包内版) */
    internal fun huoquJishu(atc900: Long, aae002: String, nf: String): InsuranceCalcEngine.Tc01 {
        val t90 = s.single("tc90", "atc900=?", atc900) ?: throw SettleRet(-1, "获取缴费基数出错")
        val t = InsuranceCalcEngine.Tc01()
        t.aaa301 = s.nvl(t90["atc905"]); t.aaa304 = s.nvl(t90["atc905"])
        t.aaa302 = s.nvl(t90["aaa302"]); t.aaa305 = s.nvl(t90["aaa305"])
        t.aaa306 = s.nvl(t90["aaa306"]); t.aaa303 = s.nvl(t90["aaa303"])
        if (nf.toLong() < aae002.substring(0, 4).toLong()) {
            shouBuJiShu(aae002, atc900, "001")
        }
        val rows = s.jdbc.queryForList(
            "SELECT * FROM tc08 WHERE aae000=?::numeric AND aae002=?::numeric AND atc900=?",
            nf.toBigDecimal(), aae002.toBigDecimal(), atc900)
        for (r in rows) {
            when (s.str(r["aaa000"])) {
                "1" -> { t.aaa301 = s.nvl(r["aaa300"]); t.aaa304 = t.aaa301 }
                "2" -> { t.aaa302 = s.nvl(r["aaa300"]); t.aaa305 = t.aaa302; t.aaa306 = t.aaa302 }
                "3" -> t.aaa303 = s.nvl(r["aaa300"])
            }
        }
        return t
    }

    /** 补发 while 循环公共体('15'/'27'/'46'/'36'): 获取补退基数+计算险金引擎等价 */
    internal fun buFaYue(tc90Map: Map<String, Any?>, aae002: String, ksny0: String, jsny: String): List<InsuranceCalcEngine.Tc01> {
        val out = ArrayList<InsuranceCalcEngine.Tc01>()
        var ksny = ksny0
        val real90 = s.single("tc90", "atc900=?", tc90Map["atc900"])
            ?: throw SettleRet(-1, "计算补发信息出错：查询合同信息出错")
        while (ksny <= jsny) {
            val t = huoquJishu(s.long(real90["atc900"])!!, aae002, ksny)
            t.jslb = "1"
            val (code1, res1) = engine.calc(real90, ksny, t)
            if (code1 == -1) throw SettleRet(-1, "计算补发信息出错：${res1 as? String ?: "计算险金出错"}")
            out.add(res1 as InsuranceCalcEngine.Tc01)
            ksny = s.monthAdd(ksny, 1)
        }
        return out
    }

    /**
     * 助手 PRC_保险补收基数_个人(L1356-1721, 包内版): 三险种段 TC08 补收基数落库。
     * AA97 次年基数(AAE001=KSNY 年+1, AAC001) 唯一行取之, 无行/多行→回退 TC90 值(源 OTHERS)。
     * TC08 uk(aae000,atc900,aaa000) 预检: 社保段命中=静默跳过; 医保/公积金段命中=UPDATE AAA300。
     */
    internal fun shouBuJiShu(aae002: String, atc900: Long, aae011: String) {
        val user = try { s.userRow(aae011) } catch (e: Exception) {
            throw SettleRet(-1, "获取经办人信息失败")
        }
        val curYear = aae002.substring(0, 4).toLong()
        // (险种, AAA401 列, 参保始列, 停保始列?/旗标列, 旗标列, 基数列, 区划列对, AAA000)
        data class Seg(val aaa000: String, val flagCol: String, val ksCol: String,
                       val jsFlagCol: String, val jsYmCol: String, val baseCol: String,
                       val q20: String, val q21: String, val prov99: Boolean)
        val segs = listOf(
            Seg("1", "aaa401", "aaa201", "atc90p", "aaa208", "atc905", "aaa100", "aaa101", false),
            Seg("2", "aaa402", "aaa601", "atc90pa", "aaa608", "aaa302", "aaa110", "aaa111", true),
            Seg("3", "aaa407", "aaa501", "atc90w", "aaa508", "aaa303", "aaa120", "aaa121", false))
        val dupSql = "SELECT COUNT(*) FROM tc08 WHERE aae000=?::numeric AND atc900=? AND aaa000=?"
        for (g in segs) {
            val rows = s.jdbc.queryForList(
                """SELECT a.*, b.aaa030, b.aaa031 FROM tc90 a, ab01 b
                   WHERE a.aab001=b.aab001 AND a.${g.flagCol}='1'
                     ${if (g.prov99) "AND SUBSTR(a.aaa110,3,2)='99'" else ""}
                     AND ((a.${g.ksCol}<?::numeric AND a.${g.jsFlagCol} IS NULL
                           AND SUBSTR(a.${g.ksCol},1,4)<?::numeric)
                       OR (a.${g.ksCol}<?::numeric AND a.${g.jsYmCol}=?::numeric
                           AND a.${g.jsFlagCol}='0'
                           AND SUBSTR(a.${g.ksCol},1,4)<SUBSTR(a.${g.jsYmCol},1,4)))
                     AND a.atc900=?""",
                aae002.toBigDecimal(), aae002.substring(0, 4).toBigDecimal(),
                aae002.toBigDecimal(), aae002.toBigDecimal(), atc900)
            for (r in rows) {
                // 源 TO_DATE 格式守卫(三段同文案'社保补发...'): NULL/非法年月即报错
                val ksny = s.str(r[g.ksCol])
                if (ksny == null || !Regex("^\\d{6}$").matches(ksny))
                    throw SettleRet(-1, "社保补发开始年月格式不正确，无法生成补缴基数")
                if (!Regex("^\\d{6}$").matches(aae002))
                    throw SettleRet(-1, "补发结束年月格式不正确，无法生成补缴基数")
                // 按年分组月份序列(D_KSNY..D_JSNY, CONNECT BY 等价)
                var cur = ksny as String; val endYm = aae002
                val perYear = LinkedHashMap<String, Pair<String, String>>() // nf -> (min,max)
                while (cur <= endYm) {
                    val nf = cur.substring(0, 4)
                    perYear[nf] = perYear[nf]?.let { it.copy(second = cur) } ?: Pair(cur, cur)
                    cur = s.monthAdd(cur, 1)
                }
                for ((nf, range) in perYear) {
                    if (nf.toLong() >= curYear) continue // 源: 仅结算年前年份补收
                    val row = linkedMapOf<String, Any?>(
                        "aae002" to aae002.toBigDecimal(), "aae000" to nf.toBigDecimal(),
                        "ksny" to range.first.toBigDecimal(), "jsny" to range.second.toBigDecimal(),
                        "atc900" to r["atc900"], "aac001" to r["aac001"],
                        "aac003" to r["aac003"], "aac002" to r["aac002"],
                        "atb930" to r["atb930"],
                        "atb900" to r["atb900"], "aab001" to r["aab001"], "aab004" to r["aab004"],
                        "aaa030" to r["aaa030"], "aaa031" to r["aaa031"],
                        "aaa000" to g.aaa000,
                        "aae011" to user["aae011"], "aae017" to user["aae017"],
                        "aae036" to s.now(), "aae019" to user["aae019"],
                        "aae020" to user["aae020"], "aae022" to user["aae022"],
                        "aae013" to null, "aaa020" to r[g.q20], "aaa021" to r[g.q21])
                    row["atb931"] = s.single("tb93", "atb930=?", r["atb930"])?.get("atb931")
                        ?: throw SettleRet(-1, "查询结算单位名称出错")
                    // AA97 次年基数: AAE001=(KSNY 年+1), AAC001; 唯一行取之否则回退(源 OTHERS)
                    val nextYear = (nf.toInt() + 1).toString()
                    val baseCol = g.baseCol
                    val fallback = s.nvl(r[baseCol])
                    val aa97 = s.jdbc.queryForList(
                        "SELECT $baseCol FROM aa97 WHERE aae001=? AND aac001=?",
                        nextYear, r["aac001"])
                    row["aaa300"] = if (aa97.size == 1) s.nvl(aa97[0][baseCol]) else fallback
                    val dup = s.jdbc.queryForObject(
                        dupSql, Int::class.java, nf.toBigDecimal(), atc900, g.aaa000) ?: 0
                    if (dup > 0) {
                        if (g.aaa000 == "1") continue // 社保段 DUP→静默
                        // 医保/公积金段 DUP→UPDATE AAA300
                        s.jdbc.update(
                            "UPDATE tc08 SET aaa300=? WHERE aaa000=? AND aae000=?::numeric AND atc900=?",
                            row["aaa300"], g.aaa000, nf.toBigDecimal(), atc900)
                        continue
                    }
                    row["baz001"] = s.jdbc.queryForObject("SELECT nextval('seq_sys_baz001')", Long::class.java)
                    row["baz002"] = s.jdbc.queryForObject("SELECT nextval('seq_sys_baz002')", Long::class.java)
                    row["atc080"] = s.jdbc.queryForObject("SELECT nextval('seq_tc08_atc080')", Long::class.java)
                    try {
                        s.insertRow("tc08", row)
                    } catch (e: Exception) {
                        if (!s.isDup(e)) throw SettleRet(-1, "记录补收基数出错")
                    }
                }
            }
        }
    }
}

/** /proc 注册面: PKG_保险补退.PRC_保险补退明细(费用结算补退历史名单循环目标) */
@Component
class TdSupplementProc(private val c: TdSupplementCases) : ProcHandler {
    override fun procName() = "PKG_保险补退.PRC_保险补退明细"
    override fun handle(params: Map<String, Any?>): Map<String, Any?> {
        val atc900 = (params["atc900"] ?: params["prm_atc900"]
            ?: return mapOf("appCode" to -1, "errMsg" to "缺少 atc900")).toString().toLongOrNull()
            ?: return mapOf("appCode" to -1, "errMsg" to "atc900 非数值")
        val aae002 = params["aae002"]?.toString() ?: params["prm_aae002"]?.toString()
            ?: return mapOf("appCode" to -1, "errMsg" to "缺少 aae002")
        val aae011 = params["aae011"]?.toString() ?: params["prm_aae011"]?.toString()
            ?: return mapOf("appCode" to -1, "errMsg" to "缺少 aae011")
        return try {
            c.detail(atc900, aae002, aae011)
            mapOf("appCode" to 1, "errMsg" to null as String?)
        } catch (e: SettleRet) {
            mapOf("appCode" to e.code, "errMsg" to e.msg)
        }
    }
}

/** BigDecimal 惰性求和: nvl 语义(空值惰性定律) */
internal fun TdSettleSupport.nz(v: Any?): BigDecimal = nvl(v)
