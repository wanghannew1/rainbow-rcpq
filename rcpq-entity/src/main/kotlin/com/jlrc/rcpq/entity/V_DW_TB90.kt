package com.jlrc.rcpq.entity

import jakarta.persistence.*
import java.io.Serializable
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "V_DW_TB90")
open class V_DW_TB90 {
    @Id
    @Column(name = "ATB900")
    var atb900: Long? = null

    @Column(name = "AAB003")
    var aab003: String? = null

    @Column(name = "AAB007")
    var aab007: String? = null

    @Column(name = "AAB010")
    var aab010: LocalDate? = null

    @Column(name = "AAB011")
    var aab011: LocalDate? = null

    @Column(name = "AAB019")
    var aab019: String? = null

    @Column(name = "AAB020")
    var aab020: String? = null

    @Column(name = "AAB048")
    var aab048: String? = null

    @Column(name = "AAB054")
    var aab054: String? = null

    @Column(name = "AAB022")
    var aab022: String? = null

    @Column(name = "AAB021")
    var aab021: String? = null

    @Column(name = "AAB025")
    var aab025: String? = null

    @Column(name = "AAB026")
    var aab026: String? = null

    @Column(name = "AAB052")
    var aab052: String? = null

    @Column(name = "AAB053")
    var aab053: String? = null

    @Column(name = "AAE006")
    var aae006: String? = null

    @Column(name = "AAE007")
    var aae007: String? = null

    @Column(name = "AAB013")
    var aab013: String? = null

    @Column(name = "AAB014")
    var aab014: String? = null

    @Column(name = "AAB023")
    var aab023: String? = null

    @Column(name = "AAE004")
    var aae004: String? = null

    @Column(name = "AAE005")
    var aae005: String? = null

    @Column(name = "AAE015")
    var aae015: String? = null

    @Column(name = "AAE016")
    var aae016: String? = null

    @Column(name = "AAB01D")
    var aab01d: String? = null

    @Column(name = "BAB002")
    var bab002: String? = null

    @Column(name = "AAB998")
    var aab998: String? = null

    @Column(name = "AAB00B")
    var aab00b: LocalDate? = null

    @Column(name = "AAB00C")
    var aab00c: LocalDate? = null

    @Column(name = "AAB049")
    var aab049: BigDecimal? = null

    @Column(name = "AAB00E")
    var aab00e: String? = null

    @Column(name = "AAZ011")
    var aaz011: String? = null

    @Column(name = "BAZ001")
    var baz001: Long? = null

    @Column(name = "BAZ002")
    var baz002: Long? = null

    @Column(name = "AAB001")
    var aab001: Long? = null

    @Column(name = "AAB004")
    var aab004: String? = null

    @Column(name = "ATB903")
    var atb903: String? = null

    @Column(name = "ATB904")
    var atb904: String? = null

    @Column(name = "ATB905")
    var atb905: LocalDate? = null

    @Column(name = "ATB906")
    var atb906: LocalDate? = null

    @Column(name = "ATB907")
    var atb907: LocalDate? = null

    @Column(name = "ATB908")
    var atb908: String? = null

    @Column(name = "ATB909")
    var atb909: String? = null

    @Column(name = "ATB90A")
    var atb90a: BigDecimal? = null

    @Column(name = "ATB90B")
    var atb90b: BigDecimal? = null

    @Column(name = "ATB90C")
    var atb90c: String? = null

    @Column(name = "AAE100")
    var aae100: String? = null

    @Column(name = "AAE013")
    var aae013: String? = null

    @Column(name = "ATB90D")
    var atb90d: String? = null

    @Column(name = "ATB90E")
    var atb90e: String? = null

    @Column(name = "ATB90F")
    var atb90f: String? = null

    @Column(name = "ATB90G")
    var atb90g: String? = null

    @Column(name = "ATB90H")
    var atb90h: String? = null

    @Column(name = "ATB90I")
    var atb90i: String? = null

    @Column(name = "ATB90J")
    var atb90j: String? = null

    @Column(name = "AAB301")
    var aab301: String? = null

    @Column(name = "AAA021")
    var aaa021: String? = null

    @Column(name = "ATB90K")
    var atb90k: BigDecimal? = null

    @Column(name = "ATB90L")
    var atb90l: BigDecimal? = null

    @Column(name = "ATB90M")
    var atb90m: BigDecimal? = null

    @Column(name = "ATB90N")
    var atb90n: BigDecimal? = null

    @Column(name = "ATB90O")
    var atb90o: BigDecimal? = null

    @Column(name = "ATB90P")
    var atb90p: String? = null

    @Column(name = "AAE011")
    var aae011: String? = null

    @Column(name = "AAE017")
    var aae017: String? = null

    @Column(name = "AAE036")
    var aae036: LocalDateTime? = null

    @Column(name = "AAE019")
    var aae019: String? = null

    @Column(name = "AAE020")
    var aae020: String? = null

    @Column(name = "AAE022")
    var aae022: String? = null

    @Column(name = "ATB90R")
    var atb90r: String? = null

    @Column(name = "ATB90S")
    var atb90s: String? = null

    @Column(name = "ATB90T")
    var atb90t: String? = null

    @Column(name = "ATB90U")
    var atb90u: LocalDate? = null

    @Column(name = "ATB90V")
    var atb90v: String? = null

    @Column(name = "BAE011")
    var bae011: String? = null

    @Column(name = "BAE017")
    var bae017: String? = null

    @Column(name = "BAE036")
    var bae036: LocalDate? = null

    @Column(name = "BAE019")
    var bae019: String? = null

    @Column(name = "BAE020")
    var bae020: String? = null

    @Column(name = "BAE022")
    var bae022: String? = null

    @Column(name = "ATB910")
    var atb910: Long? = null

    @Column(name = "ATB90W")
    var atb90w: String? = null

    @Column(name = "ATB90Y")
    var atb90y: String? = null

    @Column(name = "ATB90Q")
    var atb90q: String? = null

    @Column(name = "ATB90Z")
    var atb90z: BigDecimal? = null

    @Column(name = "AAB00A")
    var aab00a: String? = null

    @Column(name = "ATB90AA")
    var atb90aa: String? = null

    @Column(name = "ATB90AB")
    var atb90ab: Long? = null

    @Column(name = "ATB90AC")
    var atb90ac: String? = null

    @Column(name = "ATB90AD")
    var atb90ad: String? = null

}
