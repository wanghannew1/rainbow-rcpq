package com.jlrc.rcpq.service.td

import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Component
import java.math.BigDecimal
import java.sql.Timestamp
import java.time.YearMonth
import java.util.Calendar
import java.util.Date
import javax.sql.DataSource

/** 子过程守卫/引擎失败信号: 原样透传 appCode/errMsg(源 IF PRM_CODE<>DEF_OK THEN RETURN 语义) */
class SettleRet(val code: Int, val msg: String) : RuntimeException(msg)

/**
 * PKG_费用结算 计算族共用支撑(桥单 20261127-034): SELECT * INTO 单行语义/动态整行 INSERT/
 * 月份算术/经办人单行。INSERT 按目标表 information_schema 实际列交集(源 INSERT .. VALUES R
 * 全列形态, PG 侧列集以此对齐)。
 */
@Component
class TdSettleSupport(val jdbc: JdbcTemplate, ds: DataSource) {

    private val conn = ds.connection

    /** 表列名缓存(小写) */
    private val cols = HashMap<String, Set<String>>()

    @Synchronized
    fun tableCols(table: String): Set<String> {
        cols[table]?.let { return it }
        conn.use { c ->
            val md = c.metaData.getColumns(null, null, table, "%")
            val s = mutableSetOf<String>()
            while (md.next()) s.add(md.getString("COLUMN_NAME").lowercase())
            cols[table] = s
            return s
        }
    }

    /** SELECT * INTO 单行: 0/多行 → Oracle 异常同判(调用方按上下文转守卫或外壳) */
    fun single(table: String, where: String, vararg args: Any?): Map<String, Any?>? =
        jdbc.queryForList("SELECT * FROM $table WHERE $where", *args).singleOrNull()

    fun singleOrThrow(table: String, where: String, vararg args: Any?): Map<String, Any?> =
        single(table, where, *args) ?: throw SettleRet(-1, "查询${table}出错")

    /** 经办人单行(源 SELECT * INTO SYS_ORG_USER_UNIT, 守卫文案由调用方给) */
    fun userRow(aae011: String): Map<String, Any?> =
        jdbc.queryForList("SELECT * FROM sys_org_user_unit WHERE aae011=?", aae011).singleOrNull()
            ?: throw SettleRet(-1, "查询经办人信息出错，无法结算")

    /** 动态整行 INSERT(列交集; 值按 queryForList 原生类型直绑) */
    fun insertRow(table: String, row: Map<String, Any?>): Int {
        val keys = row.keys.filter { it.lowercase() in tableCols(table) }
        if (keys.isEmpty()) return 0
        val sql = "INSERT INTO $table (${keys.joinToString(",")}) VALUES (${keys.joinToString(",") { "?" }})"
        return jdbc.update(sql, *keys.map { row[it] }.toTypedArray())
    }

    fun isDup(e: Throwable): Boolean {
        var t: Throwable? = e
        while (t != null) {
            if (t is org.springframework.dao.DataIntegrityViolationException ||
                t is org.springframework.dao.DuplicateKeyException) return true
            t = t.cause
        }
        return false
    }

    fun nvl(v: Any?): BigDecimal = when (v) {
        null -> BigDecimal.ZERO
        is BigDecimal -> v
        is Number -> BigDecimal(v.toString())
        is String -> if (v.isBlank()) BigDecimal.ZERO else BigDecimal(v.trim())
        else -> BigDecimal(v.toString())
    }

    fun num(v: Any?): BigDecimal? = when (v) {
        null -> null
        is BigDecimal -> v
        is Number -> BigDecimal(v.toString())
        is String -> if (v.isBlank()) null else v.trim().toBigDecimalOrNull()
        else -> null
    }

    fun str(v: Any?): String? = v?.toString()

    fun long(v: Any?): Long? = when (v) {
        null -> null
        is Number -> v.toLong()
        is String -> v.trim().toLongOrNull()
        else -> null
    }

    /** ym(yyyyMM) ± months → yyyyMM */
    fun monthAdd(ym: String, months: Int): String =
        YearMonth.of(ym.substring(0, 4).toInt(), ym.substring(4, 6).toInt())
            .plusMonths(months.toLong()).format(java.time.format.DateTimeFormatter.ofPattern("yyyyMM"))

    fun prevYm(ym: String): String = monthAdd(ym, -1)

    /** Oracle MONTHS_BETWEEN(整月) */
    fun monthsBetween(a: String, b: String): Long {
        val ya = YearMonth.of(a.substring(0, 4).toInt(), a.substring(4, 6).toInt())
        val yb = YearMonth.of(b.substring(0, 4).toInt(), b.substring(4, 6).toInt())
        return (ya.year * 12L + (ya.monthValue - 1L)) - (yb.year * 12L + (yb.monthValue - 1L))
    }

    fun nowYm(): String = YearMonth.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMM"))

    fun truncNow(): Timestamp {
        val c = Calendar.getInstance()
        c.set(Calendar.HOUR_OF_DAY, 0); c.set(Calendar.MINUTE, 0)
        c.set(Calendar.SECOND, 0); c.set(Calendar.MILLISECOND, 0)
        return Timestamp(c.timeInMillis)
    }

    fun now(): Timestamp = Timestamp(Date().time)

    /** AA92 起征点(AAA921='1'普通'2'劳务) */
    fun qzd(kind: String): BigDecimal =
        jdbc.queryForObject("SELECT aaa922 FROM aa92 WHERE aaa921=?", BigDecimal::class.java, kind)

    /** TC93 虚拟零行(四名单子过程公共模板; 源 L285-346 逐列) */
    fun virtualRow(tc90: Map<String, Any?>, tb93: Map<String, Any?>, user: Map<String, Any?>,
                   atc931: String, atc937: String, nAae002: String,
                   aae013: String?, aae036: Timestamp, atc93m: BigDecimal?): MutableMap<String, Any?> {
        val r = mutableMapOf<String, Any?>(
            "atc930" to jdbc.queryForObject("SELECT nextval('seq_tc93_atc930')", Long::class.java),
            "baz001" to jdbc.queryForObject("SELECT nextval('seq_sys_baz001')", Long::class.java),
            "baz002" to jdbc.queryForObject("SELECT nextval('seq_sys_baz002')", Long::class.java),
            "atb900" to tb93["atb900"], "atc900" to tc90["atc900"],
            "aab001" to tb93["aab001"], "aab004" to tc90["aab004"],
            "atb930" to tb93["atb930"], "atb931" to tb93["atb931"],
            "atc931" to atc931.toLong(), "atc932" to atc931.toLong(), "atc937" to atc937,
            // 源赋值序: 前置 AAE001/AAE002:=P_ATC931/N_AAE002, 但尾部块(各子过程)统一
            // AAE002:=P_ATC931 再赋覆盖 → 四子过程最终 AAE001=AAE002=P_ATC931(逐行核对)
            "aae001" to atc931.toLong(), "aae002" to atc931.toLong(),
            "aac001" to tc90["aac001"], "aac003" to tc90["aac003"], "aac002" to tc90["aac002"],
            "bbb001" to tc90["bbb001"],
            "atb90a" to BigDecimal.ZERO, "atb90z" to BigDecimal.ZERO,
            "atc9p2" to BigDecimal.ZERO, "atc9p3" to BigDecimal.ZERO,
            "atc93g" to "0", "atc93n" to null, "atc93p" to "0", "atc93w" to "0",
            "aae114" to "1", "aae214" to "0", "aae314" to null,
            "atc93x" to BigDecimal.ZERO, "atc933" to BigDecimal.ZERO, "atc934" to BigDecimal.ZERO,
            "atc935" to BigDecimal.ZERO, "atc936" to BigDecimal.ZERO,
            "atc93k" to BigDecimal.ZERO, "atc93l" to BigDecimal.ZERO,
            "atc93m" to atc93m, "atc93y" to BigDecimal.ZERO,
            "atc938" to BigDecimal.ZERO, "atc939" to BigDecimal.ZERO,
            "atc93a" to BigDecimal.ZERO, "atc93b" to BigDecimal.ZERO,
            "atc93h" to BigDecimal.ZERO, "atc93i" to BigDecimal.ZERO,
            "atc93j" to BigDecimal.ZERO, "atc93z" to BigDecimal.ZERO,
            "atc93aa" to BigDecimal.ZERO,
            "aae013" to aae013,
            "aae011" to user["aae011"], "aae019" to user["aae019"], "aae017" to user["aae017"],
            "aae020" to user["aae020"], "aae022" to user["aae022"], "aae036" to aae036,
        )
        return r
    }
}
