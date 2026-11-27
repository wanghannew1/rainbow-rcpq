package com.jlrc.rcpq.service.td

import com.jlrc.rcpq.service.calc.InsuranceCalcEngine
import com.jlrc.rcpq.service.proc.ProcHandler
import org.springframework.stereotype.Component
import java.math.BigDecimal

/**
 * PKG_保险补退 第二波(桥单 20261127-047): PRC_保险补收基数(单位版) + PRC_保险上月补退明细。
 *
 * 源: PACKAGE_BODY_PKG_保险补退.sql 行区间逐字移植
 *   PRC_保险补收基数(单位版) L1012-1334 — 社保段(L1117-1125)按源逐字: 缴费基数上下限
 *     调用含 `PRM_JS_RS => R.AAA301`(OUT 绑 FOR 行字段, 原以为 PLS-00457 编译死点)+反转
 *     守卫 `IF PRM_CODE <> DEF_ERR THEN RETURN`; 社保段不插 TC08(只做 clamp)。
 *   PRC_保险上月补退明细 L1723-2905 — 写面仅两条: 头部 DELETE TC07(atc07z='0',
 *     datasource='30') + 尾部单次 INSERT(全分支内存变异共享 R_TC07, ATC9EH<>0 才落)。
 *
 * ⭐根因勘正(2026-11-27 user_errors 只读取证, 推翻 041 期"L1121=编译死"理论):
 *   两过程文本在 RCPQ_TEST 场 DBMS_DDL 重编译零错误 VALID 且实弹可跑(全角括号
 *   25 行/OUT 绑行字段均非编译阻断); ccrcpq 包体 INVALID 真根因 = 依赖视图
 *   SYS_ORG_USER_UNIT 在 ccrcpq 场 INVALID(ORA-04063 级联, PLS-00201)——凡 %ROWTYPE
 *   引用它的包体全体编不过, 与源文本无关。故两过程旧侧行为证据 = RCPQ_TEST 实弹
 *   (SupLastOld.java), 新侧 e2e_bc47_suplast.py 14/14 同判零真偏差。
 *   单位版社保段反转守卫被实弹观察: 社保候选命中即 clamp 后 RETURN(成功), 医保/
 *   公积金段不可达——逐字移植保持该行为, 可用重建留二阶段。
 *
 * PG 适配: Oracle (+) 外连接译为 LEFT/RIGHT JOIN + COALESCE; SUBSTR(numeric 列)
 * 显式 ::text(PG 无隐式 numeric↔text, 041 个人版潜雷同款已修); ATC070 内联 nextval
 * 单语句(041 语句间隙死亡面规避); DUP(唯一键)仅 23505 静默。
 */
@Component
class TdSupplementUnitCases(val s: TdSettleSupport, private val engine: InsuranceCalcEngine) {

    /** PRC_保险补收基数(单位版, L1012-1334): 按经办人全量三段(社保 clamp-死段/医保/公积金) */
    fun shouUnit(aae002: String, aae011: String) {
        val user = try { s.userRow(aae011) } catch (e: Exception) {
            throw SettleRet(-1, "获取经办人信息失败")
        }
        val curYear = aae002.substring(0, 4) // SUBSTR(PRM_AAE002,1,4) 字符串比较
        val notExists = "AND NOT EXISTS (SELECT 1 FROM tc08 WHERE aae002=a.aae002 AND atc900=a.atc900)"

        // ── 社保段(L1052-1133) 编译死语义逐字 ──
        val sbRows = s.jdbc.queryForList(
            """SELECT a.*, b.aaa030, b.aaa031 FROM tc90 a, ab01 b
               WHERE a.aab001=b.aab001 AND a.aaa401='1'
                 AND ((a.aaa201<?::numeric AND a.atc90p IS NULL
                       AND SUBSTR(a.aaa201::text,1,4)<?::text)
                   OR (a.aaa203<?::numeric AND a.aaa208=?::numeric AND a.atc90p='0'
                       AND SUBSTR(a.aaa203::text,1,4)<SUBSTR(a.aaa208::text,1,4)))
                 AND a.aae011=? $notExists""",
            aae002.toBigDecimal(), curYear, aae002.toBigDecimal(), aae002.toBigDecimal(), aae011)
        for (r in sbRows) {
            val ksny = s.str(r["aaa201"])
            if (ksny == null || !Regex("^\\d{6}$").matches(ksny))
                throw SettleRet(-1, "社保补发开始年月格式不正确，无法生成补缴基数")
            if (!Regex("^\\d{6}$").matches(aae002))
                throw SettleRet(-1, "补发结束年月格式不正确，无法生成补缴基数")
            for ((nf, range) in yearGroups(ksny, aae002)) {
                if (nf >= curYear) continue // 源 NF < SUBSTR(PRM_AAE002,1,4)
                // R_TC08 部分赋值(社保段无 TC08 插入——源如此): 仅 clamp 生效
                // 源 L1117-1125: 缴费基数上下限(PRM_BX='1', KSNY, AAA100, ATC905,
                //   PRM_JS_RS=>R.AAA301 ← OUT 绑行字段 = 编译死点, OUT 移本地 jsRs);
                //   反转守卫 IF PRM_CODE <> DEF_ERR THEN RETURN — clamp 纯计算恒 OK →
                //   首个候选即整过程返回(源编译通过后的字面行为)。
                val jsRs = engine.clampBase("1", range.first, s.str(r["aaa100"]), s.nvl(r["atc905"]))
                val code = 1 // DEF_OK
                if (code != -1) return
            }
        }

        // ── 医保段(L1148-1249): 省区划 99 + AAA601/603/608 门 ──
        // ── 公积金段(L1264-1334): AAA501/503/508 门; D_KSNY 源笔误用 AAA201(逐字) ──
        data class Seg(val aaa000: String, val flag: String, val ks: String, val stop: String,
                       val jsEnd: String, val jsYm: String, val ksnyCol: String,
                       val base: String, val prov: Boolean)
        val segs = listOf(
            Seg("2", "aaa402", "aaa601", "atc90pa", "aaa603", "aaa608", "aaa601", "aaa302", true),
            Seg("3", "aaa407", "aaa501", "atc90w", "aaa503", "aaa508", "aaa201", "aaa303", false))
        for (g in segs) {
            val sub = if (g.prov) " AND SUBSTR(a.aaa110::text,3,2)='99'" else ""
            val rows = s.jdbc.queryForList(
                """SELECT a.*, b.aaa030, b.aaa031 FROM tc90 a, ab01 b
                   WHERE a.aab001=b.aab001 AND a.${g.flag}='1'$sub
                     AND ((a.${g.ks}<?::numeric AND a.${g.stop} IS NULL
                           AND SUBSTR(a.${g.ks}::text,1,4)<?::text)
                       OR (a.${g.jsEnd}<?::numeric AND a.${g.jsYm}=?::numeric AND a.${g.stop}='0'
                           AND SUBSTR(a.${g.jsEnd}::text,1,4)<SUBSTR(a.${g.jsYm}::text,1,4)))
                     AND a.aae011=? $notExists""",
                aae002.toBigDecimal(), curYear, aae002.toBigDecimal(), aae002.toBigDecimal(), aae011)
            for (r in rows) {
                val ksny = s.str(r[g.ksnyCol]) // 公积金段源笔误: D_KSNY 用 AAA201
                if (ksny == null || !Regex("^\\d{6}$").matches(ksny))
                    throw SettleRet(-1, "社保补发开始年月格式不正确，无法生成补缴基数")
                if (!Regex("^\\d{6}$").matches(aae002))
                    throw SettleRet(-1, "补发结束年月格式不正确，无法生成补缴基数")
                for ((nf, range) in yearGroups(ksny, aae002)) {
                    if (nf >= curYear) continue
                    val row8 = linkedMapOf<String, Any?>(
                        "aae002" to aae002.toBigDecimal(), "aae000" to nf.toBigDecimal(),
                        "ksny" to range.first.toBigDecimal(), "jsny" to range.second.toBigDecimal(),
                        "atc900" to r["atc900"], "aac001" to r["aac001"],
                        "aac003" to r["aac003"], "aac002" to r["aac002"], "atb930" to r["atb930"],
                        "atb900" to r["atb900"], "aab001" to r["aab001"], "aab004" to r["aab004"],
                        "aaa030" to r["aaa030"], "aaa031" to r["aaa031"],
                        "aaa000" to g.aaa000, "aaa300" to s.nvl(r[g.base]),
                        "aae011" to user["aae011"], "aae017" to user["aae017"],
                        "aae036" to s.now(), "aae019" to user["aae019"],
                        "aae020" to user["aae020"], "aae022" to user["aae022"],
                        "aae013" to null)
                    val tbs = s.jdbc.queryForList("SELECT atb931 FROM tb93 WHERE atb930=?", r["atb930"])
                    row8["atb931"] = if (tbs.size == 1) tbs[0]["atb931"]
                        else throw SettleRet(-1, "查询结算单位名称出错") // 源 SELECT INTO 非单行→OTHERS
                    row8["baz001"] = s.jdbc.queryForObject("SELECT nextval('seq_sys_baz001')", Long::class.java)
                    row8["baz002"] = s.jdbc.queryForObject("SELECT nextval('seq_sys_baz002')", Long::class.java)
                    row8["atc080"] = s.jdbc.queryForObject("SELECT nextval('seq_tc08_atc080')", Long::class.java)
                    try {
                        s.insertRow("tc08", row8)
                    } catch (e: Exception) {
                        if (!s.isDup(e)) throw SettleRet(-1, "记录补收基数出错") // DUP→DEF_OK 忠实静默
                    }
                }
            }
        }
    }

    /** 年分组(源 CONNECT BY ROWNUM<=MONTHS_BETWEEN(D_JSNY,D_KSNY): D_KSNY..D_JSNY 前一月) */
    private fun yearGroups(ksny: String, jsny: String): LinkedHashMap<String, Pair<String, String>> {
        val perYear = LinkedHashMap<String, Pair<String, String>>() // nf -> (min,max)
        var cur = ksny
        while (cur < jsny) {
            val nf = cur.substring(0, 4)
            perYear[nf] = perYear[nf]?.let { it.copy(second = cur) } ?: Pair(cur, cur)
            cur = s.monthAdd(cur, 1)
        }
        return perYear
    }
}

/** /proc 注册面: PKG_保险补退.PRC_保险补收基数(单位版) */
@Component
class TdSupplementUnitProc(private val c: TdSupplementUnitCases) : ProcHandler {
    override fun procName() = "PKG_保险补退.PRC_保险补收基数"
    override fun handle(params: Map<String, Any?>): Map<String, Any?> {
        val aae002 = params["aae002"]?.toString() ?: params["prm_aae002"]?.toString()
            ?: return mapOf("appCode" to -1, "errMsg" to "缺少 aae002")
        val aae011 = params["aae011"]?.toString() ?: params["prm_aae011"]?.toString()
            ?: return mapOf("appCode" to -1, "errMsg" to "缺少 aae011")
        return try {
            c.shouUnit(aae002, aae011)
            mapOf("appCode" to 1, "errMsg" to null as String?)
        } catch (e: SettleRet) {
            mapOf("appCode" to e.code, "errMsg" to e.msg)
        }
    }
}

@Component
class TdSupplementLastCases(private val s: TdSettleSupport, private val sup: TdSupplementCases) {

    private val sheBaoCols = listOf("bba001", "bba003", "baa001", "baa003")
    private val yiBaoCols = listOf("bba002", "bba004", "bba005", "atb90t", "baa002")
    private val gjjCols = listOf("caa001", "caa002")

    /** PRC_保险上月补退明细(L1723-2905): 上月口径补退合成行, 全分支变异共享 R_TC07 尾部单插 */
    fun lastDetail(aae002: String, atc900: Long, aae011: String) {
        try {
            val user = try { s.userRow(aae011) } catch (e: Exception) {
                throw SettleRet(-1, "无法计算补退，原因为经办人信息不正确")
            }
            var tc90 = s.single("v_tc90_qpkg", "atc900=?", atc900)
                ?: throw SettleRet(-1, "无法计算补退，原因为查询合同信息出错")
            // 重解析仅 ATC900 字段(源 SELECT ATC900 INTO R_TC90.ATC900, 其余列保持首查值)
            tc90 = HashMap(tc90).also {
                it["atc900"] = s.jdbc.queryForObject(
                    "SELECT atc900 FROM v_tc90_qpkg WHERE atc900=(SELECT MAX(atc900) FROM tc90 " +
                        "WHERE atb930=? AND aac003=? AND aac002=?)",
                    Long::class.java, tc90["atb930"], tc90["aac003"], tc90["aac002"])
            }
            // N_AAE002 = 上月(源 ADD_MONTHS(-1); 非法年月→结算年月格式不正确)
            val nAae002 = try { s.monthAdd(aae002, -1) } catch (e: Exception) {
                throw SettleRet(-1, "无法计算补退，原因为结算年月格式不正确")
            }
            val nAae002Bd = nAae002.toBigDecimal()
            s.jdbc.update(
                "DELETE FROM tc07 WHERE atc07z='0' AND datasource='30' AND aae002=?::numeric AND atc900=?",
                aae002.toBigDecimal(), tc90["atc900"])
            // N_AAE002_BJ 水位(查询异常→上月; NULL→202012)
            val nBj = try {
                s.jdbc.queryForObject(
                    "SELECT MAX(aae002) FROM tc07 WHERE atc07z IN ('0','1') AND datasource='30' " +
                        "AND aae002>=?::numeric AND atc900=?",
                    BigDecimal::class.java, (aae002.substring(0, 4) + "01").toBigDecimal(),
                    tc90["atc900"])?.toPlainString() ?: "202012"
            } catch (e: Exception) { nAae002 }
            val nBjBd = nBj.toBigDecimal()

            // R_TC07 模板(L1832-1866; AAE003=上月) — 全分支共享, 尾部单插
            val row = sup.template(tc90, user, aae002)
            row["aae003"] = nAae002Bd
            val c1 = StringBuilder(); val c2 = StringBuilder(); val c3 = StringBuilder()
            var n1 = 0; var n2 = 0; var n3 = 0; var nJs3 = 0 // N_JS_3 省市公积金段共享(源不重置)

            fun bd(r: Map<String, Any?>, k: String): BigDecimal? = s.num(r[k])
            fun nb(v: Any?): String = s.nvl(v).stripTrailingZeros().toPlainString()
            fun ac10(ks: BigDecimal, js: BigDecimal, cols: List<String>) {
                for (a in s.jdbc.queryForList(
                    "SELECT * FROM ac10 WHERE atc932>=?::numeric AND atc932<=?::numeric AND atc900=?",
                    ks, js, tc90["atc900"]))
                    for (col in cols) row[col] = s.nvl(row[col]).subtract(s.nvl(a[col]))
            }
            fun daYi(r: Map<String, Any?>) { // 大病承担分支(AAA803_TC90 优先, '1'=单位)
                val flag = s.str(tc90["aaa803_tc90"]) ?: s.str(tc90["aaa803_tb93"])
                if (flag == "1") {
                    row["daa001"] = BigDecimal.ZERO
                    row["daa002"] = s.nvl(row["daa002"]).add(s.nvl(r["daa001_zj"])).subtract(s.nvl(r["daa001_gz"]))
                } else {
                    row["daa001"] = s.nvl(row["daa001"]).add(s.nvl(r["daa001_zj"])).subtract(s.nvl(r["daa001_gz"]))
                    row["daa002"] = BigDecimal.ZERO
                }
            }
            fun addDiff(r: Map<String, Any?>, cols: List<String>) {
                for (col in cols)
                    row[col] = s.nvl(row[col]).add(s.nvl(r[col + "_zj"])).subtract(s.nvl(r[col + "_gz"]))
            }
            fun ymStr(v: BigDecimal?): String = v?.toPlainString() ?: "" // Oracle NULL 拼接=空串

            val tAae002 = bd(tc90, "aae002")
            val p = s.str(tc90["atc90p"]); val pa = s.str(tc90["atc90pa"]); val w = s.str(tc90["atc90w"])
            val sub99 = s.str(tc90["aaa110"])?.let { if (it.length >= 3) it.substring(2, minOf(4, it.length)) else null }
            val sub99g = s.str(tc90["aaa120"])?.let { if (it.length >= 3) it.substring(2, minOf(4, it.length)) else null }

            // ══ 社保参保(L1858-1966): ATC90P IN('0','1') AND AAA208=上月 ══
            val a201 = bd(tc90, "aaa201"); val a203 = bd(tc90, "aaa203")
            if ((p == "0" || p == "1") && bd(tc90, "aaa208")?.compareTo(nAae002Bd) == 0) {
                // 参保年月差异(AAA201<AAA203<窗口)
                if (a201 != null && a203 != null && tAae002 != null && a201 < a203 && a201 < tAae002) {
                    val jsny: String? = when {
                        a203 < nAae002Bd -> a203.toPlainString()
                        a203.compareTo(nAae002Bd) == 0 -> s.monthAdd(a203.toPlainString(), -1)
                        else -> null
                    }
                    if (jsny == null) throw SettleRet(-1, "无法计算补退，原因为计算补退起止年月出错")
                    ac10(a201, jsny.toBigDecimal(), sheBaoCols)
                    n1++; c1.append(n1).append("、参保时间差异：申报参保年月为【").append(nb(a201))
                        .append("】，实际参保年月为【").append(nb(a203)).append("】。\r")
                }
                // 参保异议(AAA203>AAE002: tc93 工资有、征缴零/缺 → 退发)
                if (a203 != null && tAae002 != null && a203 > tAae002) {
                    val js = s.monthAdd(a203.toPlainString(), -1)
                    for (r in diffRows("tj0a1", "atj0a7",
                            "COALESCE(bba001,0)+COALESCE(bba003,0)+COALESCE(baa001,0)+COALESCE(baa003,0)",
                            sheBaoCols, false, tAae002, js.toBigDecimal(), tc90["atc900"], true)) {
                        addDiff(r, sheBaoCols)
                        n1++; c1.append(n1).append("、").append(s.str(r["aae002_gz"]))
                            .append("征缴与工资金额差异：征缴金额为【").append(nb(r["zjhj"]))
                            .append("】，工资金额为【").append(nb(r["gzhj"])).append("】。\r")
                    }
                }
                // 农民工(AAC008 含 27)
                if ((s.str(tc90["aac008"]) ?: "").contains("27")) {
                    val ks = a203 ?: throw SettleRet(-1, "无法计算补退，原因为计算补退起止年月出错")
                    ac10(ks, s.monthAdd(nAae002, -1).toBigDecimal(), listOf("baa003"))
                    n1++; c1.append(n1).append("、农村户口：申报参保年月为【").append(nb(a201))
                        .append("】，实际参保年月为【").append(nb(a203)).append("】。\r")
                }
            }
            // ══ 社保当月征缴差异(L1972-2014, 无门) ══
            for (r in diffRows("tj0a1", "atj0a7",
                    "COALESCE(bba001,0)+COALESCE(bba003,0)+COALESCE(baa001,0)+COALESCE(baa003,0)",
                    sheBaoCols, false, nBjBd, nAae002Bd, tc90["atc900"], false)) {
                addDiff(r, sheBaoCols)
                n1++; c1.append(n1).append("、").append(s.str(r["aae002_zj"]))
                    .append("征缴与工资金额差异：征缴金额为【").append(nb(r["zjhj"]))
                    .append("】，工资金额为【").append(nb(r["gzhj"])).append("】。\r")
            }

            // ══ 省医保(L2017-2442): ATC90PA IN('0','1') AND AAA608=上月 AND 省区划99 ══
            val a601 = bd(tc90, "aaa601"); val a603 = bd(tc90, "aaa603")
            val yiBaoDaa = "COALESCE(bba002,0)+COALESCE(bba004,0)+COALESCE(bba005,0)+COALESCE(baa002,0)" +
                "+COALESCE(atb90t,0)+COALESCE(daa001,0)"
            val tc93YiDaa = "COALESCE(bba002,0)+COALESCE(bba004,0)+COALESCE(bba005,0)+COALESCE(baa002,0)" +
                "+COALESCE(atb90t,0)+COALESCE(atc93bc,0)+COALESCE(atc93bd,0)"
            if ((pa == "0" || pa == "1") && bd(tc90, "aaa608")?.compareTo(nAae002Bd) == 0 && sub99 == "99") {
                if (a601 != null && a603 != null && tAae002 != null && a601 < a603 && a601 < tAae002) {
                    val jsny: String? = when {
                        a603 < nAae002Bd -> a603.toPlainString()
                        a603.compareTo(nAae002Bd) == 0 -> s.monthAdd(a603.toPlainString(), -1)
                        else -> null
                    }
                    if (jsny == null) throw SettleRet(-1, "无法计算补退，原因为计算补退起止年月出错")
                    ac10(a601, jsny.toBigDecimal(), yiBaoCols)
                    n2++; c2.append(n2).append("、参保时间差异：申报参保年月为【").append(nb(a601))
                        .append("】，实际参保年月为【").append(nb(a603)).append("】。\r")
                }
                if (a603 != null && tAae002 != null && a603 > tAae002) {
                    val js = s.monthAdd(a603.toPlainString(), -1)
                    for (r in diffRows("tj0b1", yiBaoDaa, tc93YiDaa, yiBaoCols, true,
                            tAae002, js.toBigDecimal(), tc90["atc900"], true)) {
                        addDiff(r, yiBaoCols); daYi(r)
                        n2++; c2.append(n2).append("、").append(s.str(r["aae002_gz"]))
                            .append("征缴与工资金额差异：征缴金额为【").append(nb(r["zjhj"]))
                            .append("】，工资金额为【").append(nb(r["gzhj"])).append("】。\r")
                    }
                }
            }
            // ══ 医保当月征缴差异(L2456-2548, 无门) ══
            for (r in diffRows("tj0b1", yiBaoDaa, tc93YiDaa, yiBaoCols, true,
                    nBjBd, nAae002Bd, tc90["atc900"], false)) {
                addDiff(r, yiBaoCols); daYi(r)
                n2++; c2.append(n2).append("、").append(s.str(r["aae002_zj"]))
                    .append("征缴与工资金额差异：征缴金额为【").append(nb(r["zjhj"]))
                    .append("】，工资金额为【").append(nb(r["gzhj"])).append("】。\r")
            }
            // ══ 市医保(L2553-2746): ATC90PA IN('0','1') AND AAA608=结算月 AND 区划非99;
            //    仅参保异议段(ZJHJ 无 ATB90T), 无参保年月差异段 ══
            val yiBaoShi = "COALESCE(bba002,0)+COALESCE(bba004,0)+COALESCE(bba005,0)+COALESCE(baa002,0)" +
                "+COALESCE(daa001,0)"
            if ((pa == "0" || pa == "1") && bd(tc90, "aaa608")?.compareTo(aae002.toBigDecimal()) == 0 &&
                sub99 != null && sub99 != "99"
            ) {
                if (a603 != null && tAae002 != null && a603 > tAae002) {
                    val js = s.monthAdd(a603.toPlainString(), -1)
                    for (r in diffRows("tj0c1", yiBaoShi, tc93YiDaa,
                            listOf("bba002", "bba004", "bba005", "baa002"), true,
                            tAae002, js.toBigDecimal(), tc90["atc900"], true)) {
                        addDiff(r, listOf("bba002", "bba004", "bba005", "baa002")); daYi(r)
                        n2++; c2.append(n2).append("、").append(s.str(r["aae002_gz"]))
                            .append("征缴与工资金额差异：征缴金额为【").append(nb(r["zjhj"]))
                            .append("】，工资金额为【").append(nb(r["gzhj"])).append("】。\r")
                    }
                }
            }
            // ══ 市医保当月征缴差异(L2758-2846, 无门) ══
            for (r in diffRows("tj0c1", yiBaoShi, tc93YiDaa,
                    listOf("bba002", "bba004", "bba005", "baa002"), true,
                    nBjBd, nAae002Bd, tc90["atc900"], false)) {
                addDiff(r, listOf("bba002", "bba004", "bba005", "baa002")); daYi(r)
                n2++; c2.append(n2).append("、").append(s.str(r["aae002_zj"]))
                    .append("征缴与工资金额差异：征缴金额为【").append(nb(r["zjhj"]))
                    .append("】，工资金额为【").append(nb(r["gzhj"])).append("】。\r")
            }

            // ══ 省公积金(L2849-3061)/市公积金(L3092-3300): ATC90W IN('0','1') AND AAA508=上月
            //    AND 区划99/非99; 三子段: 年月差异/基数差异(ATJ0D5·ATJ0E5)/参保异议 ══
            val a501 = bd(tc90, "aaa501"); val a503 = bd(tc90, "aaa503")
            for ((prov, tjName, atjCol) in listOf(
                Triple(true, "tj0d1", "atj0d5"), Triple(false, "tj0e1", "atj0e5"))) {
                if ((w == "0" || w == "1") && bd(tc90, "aaa508")?.compareTo(nAae002Bd) == 0 &&
                    ((prov && sub99g == "99") || (!prov && sub99g != null && sub99g != "99"))
                ) {
                    if (a501 != null && a503 != null && tAae002 != null && a501 < a503 && a501 < tAae002) {
                        val jsny: String? = when {
                            a503 < nAae002Bd -> a503.toPlainString()
                            a503.compareTo(nAae002Bd) == 0 -> s.monthAdd(a503.toPlainString(), -1)
                            else -> null
                        }
                        if (jsny == null) throw SettleRet(-1, "无法计算补退，原因为计算补退起止年月出错")
                        ac10(a501, jsny.toBigDecimal(), gjjCols)
                        n3++; c3.append(n3).append("、参保时间差异：申报参保年月为【").append(nb(a501))
                            .append("】，实际参保年月为【").append(nb(a503)).append("】。\r")
                    }
                    // 基数差异: TJ0D1/E1(ATJ000='2', 当月) vs AC10 区间和(KSNY=AAA503, JSNY=上月前一月)
                    val ksJs = a503 ?: throw SettleRet(-1, "无法计算补退，原因为计算补退起止年月出错")
                    val jsRows = s.jdbc.queryForList(
                        """SELECT a.caa001 caa001_zj, a.caa002 caa002_zj,
                               b.caa001_gz, b.caa002_gz
                           FROM (SELECT caa001, caa002, $atjCol AS atj_zj FROM $tjName t
                                 WHERE atj000='2' AND aae002=?::numeric AND atc900=?) a,
                                (SELECT COALESCE(SUM(COALESCE(caa001,0)),0) caa001_gz,
                                        COALESCE(SUM(COALESCE(caa002,0)),0) caa002_gz,
                                        COALESCE(SUM(COALESCE(caa001,0)),0)+COALESCE(SUM(COALESCE(caa002,0)),0) atj_gz
                                 FROM ac10 WHERE atc932>=?::numeric AND atc932<=?::numeric AND atc900=?) b
                           WHERE b.atj_gz>0 AND a.atj_zj<>b.atj_gz""",
                        nAae002Bd, tc90["atc900"],
                        ksJs, s.monthAdd(nAae002, -1).toBigDecimal(), tc90["atc900"])
                    for (r in jsRows) {
                        nJs3 = 1
                        // 源无 NVL 于 zj 列: NULL 惰性(CAA001_ZJ NULL → 本列置 NULL)
                        row["caa001"] = if (r["caa001_zj"] == null) null
                        else s.nvl(row["caa001"]).add(s.nvl(r["caa001_zj"])).subtract(s.nvl(r["caa001_gz"]))
                        row["caa002"] = if (r["caa002_zj"] == null) null
                        else s.nvl(row["caa002"]).add(s.nvl(r["caa002_zj"])).subtract(s.nvl(r["caa002_gz"]))
                    }
                    if (nJs3 > 0) { n3++; c3.append(n3).append("、基数不符。\r") }
                    if (a503 != null && tAae002 != null && a503 > tAae002) {
                        val js = s.monthAdd(a503.toPlainString(), -1)
                        for (r in diffRows(tjName, "COALESCE(caa001,0)+COALESCE(caa002,0)",
                                "COALESCE(caa001,0)+COALESCE(caa002,0)", gjjCols, false,
                                tAae002, js.toBigDecimal(), tc90["atc900"], true)) {
                            addDiff(r, gjjCols)
                            n3++; c3.append(n3).append("、").append(s.str(r["aae002_gz"]))
                                .append("征缴与工资金额差异：征缴金额为【").append(nb(r["zjhj"]))
                                .append("】，工资金额为【").append(nb(r["gzhj"])).append("】。\r")
                        }
                    }
                }
                // 公积金当月征缴差异(L3067-3089/L3305-3326, 无门, 省市各一)
                for (r in diffRows(tjName, "COALESCE(caa001,0)+COALESCE(caa002,0)",
                        "COALESCE(caa001,0)+COALESCE(caa002,0)", gjjCols, false,
                        nBjBd, nAae002Bd, tc90["atc900"], false)) {
                    addDiff(r, gjjCols)
                    n3++; c3.append(n3).append("、").append(s.str(r["aae002_zj"]))
                        .append("征缴与工资金额差异：征缴金额为【").append(nb(r["zjhj"]))
                        .append("】，工资金额为【").append(nb(r["gzhj"])).append("】。\r")
                }
            }

            // ══ 离职未参保(L3331-3419): ATB90R='5' AND AAE003=结算月 AND 任一险种停 ══
            if (s.str(tc90["atb90r"]) == "5" && bd(tc90, "aae003")?.compareTo(aae002.toBigDecimal()) == 0 &&
                (p == "2" || pa == "2" || w == "2")
            ) {
                for (a in s.jdbc.queryForList("SELECT * FROM ac10 WHERE atc900=?", tc90["atc900"])) {
                    if (p == "2") for (col in sheBaoCols) row[col] = s.nvl(row[col]).subtract(s.nvl(a[col]))
                    if (pa == "2") for (col in yiBaoCols) row[col] = s.nvl(row[col]).subtract(s.nvl(a[col]))
                    if (w == "2") for (col in gjjCols) row[col] = s.nvl(row[col]).subtract(s.nvl(a[col]))
                }
                // 退按月收取(字面 202012 起, 含未确认行)
                for (a in s.jdbc.queryForList("SELECT * FROM tc93 WHERE atc931>=202012 AND atc900=?", tc90["atc900"])) {
                    if (p == "2") for (col in sheBaoCols) row[col] = s.nvl(row[col]).subtract(s.nvl(a[col]))
                    if (pa == "2") {
                        for (col in yiBaoCols) row[col] = s.nvl(row[col]).subtract(s.nvl(a[col]))
                        row["daa001"] = s.nvl(row["daa001"]).subtract(s.nvl(a["atc93bd"]))
                        row["daa002"] = s.nvl(row["daa002"]).subtract(s.nvl(a["atc93bc"]))
                    }
                    if (w == "2") for (col in gjjCols) row[col] = s.nvl(row[col]).subtract(s.nvl(a[col]))
                }
                if (p == "2") { n1++; c1.append(n1).append("、未参保离职：申报参保年月为【")
                    .append(ymStr(a201)).append("】。\r") }
                if (pa == "2") { n2++; c2.append(n2).append("、未参保离职：申报参保年月为【")
                    .append(ymStr(a601)).append("】。\r") }
                if (w == "2") { n3++; c3.append(n3).append("、未参保离职：申报参保年月为【")
                    .append(ymStr(a501)).append("】。\r") }
            }

            // ══ 尾部(L3420-3437): 合计三列 + ATC9EH<>0 才单插 ══
            row["comm_1"] = if (c1.isEmpty()) null else c1.toString()
            row["comm_2"] = if (c2.isEmpty()) null else c2.toString()
            row["comm_3"] = if (c3.isEmpty()) null else c3.toString()
            row["bba200"] = s.nvl(row["bba200"]).add(s.nvl(row["bba001"])).add(s.nvl(row["bba003"]))
                .add(s.nvl(row["bba002"])).add(s.nvl(row["bba004"])).add(s.nvl(row["bba005"]))
                .add(s.nvl(row["atb90t"])).add(s.nvl(row["caa001"]))
            row["baa200"] = s.nvl(row["baa200"]).add(s.nvl(row["baa001"])).add(s.nvl(row["baa003"]))
                .add(s.nvl(row["baa002"])).add(s.nvl(row["caa002"]))
            row["atc9eh"] = s.nvl(row["bba200"]).add(s.nvl(row["baa200"]))
                .add(s.nvl(row["daa001"])).add(s.nvl(row["daa002"]))
            if (s.nvl(row["atc9eh"]).compareTo(BigDecimal.ZERO) != 0) {
                try {
                    val cols = row.keys.filter { it.lowercase() in s.tableCols("tc07") && it != "atc070" }
                    s.jdbc.update(
                        "INSERT INTO tc07 (atc070, ${cols.joinToString(",")}) " +
                            "VALUES (nextval('seq_tc07_atc070'), ${cols.joinToString(",") { "?" }})",
                        *cols.map { row[it] }.toTypedArray())
                } catch (e: Exception) {
                    if (!s.isDup(e)) throw SettleRet(-1, "生成补退信息出错${e.cause?.message ?: e.message}")
                    // DUP_VAL_ON_INDEX → PRM_CODE:=DEF_OK 忠实静默
                }
            }
        } catch (e: SettleRet) {
            throw e // 子面报错等价 PRM_CODE<>OK 直接返回
        } catch (e: Exception) {
            throw SettleRet(-1, "生成补退信息出错，错误信息：${e.cause?.message ?: e.message}，请将错误信息发给维护工程师")
        }
    }

    /** 征缴(tj0x1)×工资(tc93) 差异行(源 (+) 外连接直译):
     *  bDriven=true 参保异议型(B 驱动 RIGHT JOIN, ZJHJ=0 门, tc93 月窗);
     *  bDriven=false 当月差异型(A 驱动 LEFT JOIN, ZJHJ<>GZHJ, 水位..上月窗) */
    private fun diffRows(
        tj: String, zjhjExpr: String, gzhjExpr: String, cols: List<String>,
        daaGz: Boolean, ksny: BigDecimal, jsny: BigDecimal, atc900: Any?, bDriven: Boolean,
    ): List<Map<String, Any?>> {
        val tjCols = if (daaGz) cols + "daa001" else cols
        val gzCols = if (daaGz) cols + listOf("atc93bc", "atc93bd") else cols
        val zjSel = tjCols.joinToString(", ") { "COALESCE(a.$it,0) ${it}_zj" }
        val gzSel = gzCols.joinToString(", ") { "b.$it ${it}_gz" }
        val daaSel = if (daaGz) ", COALESCE(b.atc93bc,0)+COALESCE(b.atc93bd,0) daa001_gz" else ""
        val tjSub = "(SELECT aae002, atc900, $zjhjExpr AS zjhj, ${tjCols.joinToString(",")}" +
            " FROM $tj WHERE aae002>=?::numeric AND aae002<=?::numeric AND atj000='1' AND atc900=?)"
        val gzSub = "(SELECT atc931, atc900, ${gzCols.joinToString(", ")}, $gzhjExpr AS gzhj FROM tc93" +
            " WHERE atc937='1' AND aae214='1' AND atc931>=?::numeric AND atc931<=?::numeric AND atc900=?)"
        val sql = if (bDriven)
            "SELECT b.atc931 aae002_gz, COALESCE(a.zjhj,0) zjhj, $zjSel$daaSel, $gzSel, b.gzhj gzhj " +
                "FROM $tjSub a RIGHT JOIN $gzSub b ON a.aae002=b.atc931 AND a.atc900=b.atc900 " +
                "WHERE COALESCE(a.zjhj,0)=0 AND COALESCE(a.zjhj,0)<>b.gzhj"
        else
            "SELECT a.aae002 aae002_zj, COALESCE(a.zjhj,0) zjhj, $zjSel$daaSel, $gzSel, COALESCE(b.gzhj,0) gzhj " +
                "FROM $tjSub a LEFT JOIN $gzSub b ON a.aae002=b.atc931 AND a.atc900=b.atc900 " +
                "WHERE COALESCE(a.zjhj,0)<>COALESCE(b.gzhj,0)"
        return s.jdbc.queryForList(sql, ksny, jsny, atc900, ksny, jsny, atc900)
    }
}

/** /proc 注册面: PKG_保险补退.PRC_保险上月补退明细 */
@Component
class TdSupplementLastProc(private val c: TdSupplementLastCases) : ProcHandler {
    override fun procName() = "PKG_保险补退.PRC_保险上月补退明细"
    override fun handle(params: Map<String, Any?>): Map<String, Any?> {
        val aae002 = params["aae002"]?.toString() ?: params["prm_aae002"]?.toString()
            ?: return mapOf("appCode" to -1, "errMsg" to "缺少 aae002")
        val atc900 = (params["atc900"] ?: params["prm_atc900"]
            ?: return mapOf("appCode" to -1, "errMsg" to "缺少 atc900")).toString().toLongOrNull()
            ?: return mapOf("appCode" to -1, "errMsg" to "atc900 非数值")
        val aae011 = params["aae011"]?.toString() ?: params["prm_aae011"]?.toString()
            ?: return mapOf("appCode" to -1, "errMsg" to "缺少 aae011")
        return try {
            c.lastDetail(aae002, atc900, aae011)
            mapOf("appCode" to 1, "errMsg" to null as String?)
        } catch (e: SettleRet) {
            mapOf("appCode" to e.code, "errMsg" to e.msg)
        }
    }
}
