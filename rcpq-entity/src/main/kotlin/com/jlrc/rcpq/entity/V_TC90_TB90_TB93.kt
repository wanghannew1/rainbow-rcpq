package com.jlrc.rcpq.entity

import jakarta.persistence.*
import java.io.Serializable
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "V_TC90_TB90_TB93")
open class V_TC90_TB90_TB93 {
    @Id
    @Column(name = "ATC900")
    var atc900: Long? = null

    @Column(name = "AAC001")
    var aac001: Long? = null

    @Column(name = "AAC002")
    var aac002: String? = null

    @Column(name = "AAC003")
    var aac003: String? = null

    @Column(name = "AAC004")
    var aac004: String? = null

    @Column(name = "AAC006")
    var aac006: LocalDate? = null

    @Column(name = "ATC901")
    var atc901: Long? = null

    @Column(name = "AAB299")
    var aab299: String? = null

    @Column(name = "BAB305")
    var bab305: String? = null

    @Column(name = "AAC017")
    var aac017: String? = null

    @Column(name = "AAC011")
    var aac011: String? = null

    @Column(name = "AAC024")
    var aac024: String? = null

    @Column(name = "AAC009")
    var aac009: String? = null

    @Column(name = "AAC005")
    var aac005: String? = null

    @Column(name = "AAE005")
    var aae005: String? = null

    @Column(name = "AAC007")
    var aac007: LocalDate? = null

    @Column(name = "ATC902")
    var atc902: String? = null

    @Column(name = "AAE009")
    var aae009: String? = null

    @Column(name = "AAE00A")
    var aae00a: String? = null

    @Column(name = "AAE00P")
    var aae00p: String? = null

    @Column(name = "ATC904")
    var atc904: Long? = null

    @Column(name = "ATC907")
    var atc907: String? = null

    @Column(name = "ATC908")
    var atc908: String? = null

    @Column(name = "ATC909")
    var atc909: String? = null

    @Column(name = "ATC90A")
    var atc90a: String? = null

    @Column(name = "ATC90B")
    var atc90b: LocalDate? = null

    @Column(name = "ATC90C")
    var atc90c: LocalDate? = null

    @Column(name = "ATC90D")
    var atc90d: LocalDate? = null

    @Column(name = "ATC90F")
    var atc90f: String? = null

    @Column(name = "AAE002")
    var aae002: Long? = null

    @Column(name = "ATB90R_1")
    var atb90r_1: String? = null

    @Column(name = "AAB004")
    var aab004: String? = null

    @Column(name = "AAB001")
    var aab001: Long? = null

    @Column(name = "ATB900")
    var atb900: Long? = null

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

    @Column(name = "AAE100")
    var aae100: String? = null

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

    @Column(name = "AAB311")
    var aab311: String? = null

    @Column(name = "AAA121")
    var aaa121: String? = null

    @Column(name = "AAB321")
    var aab321: String? = null

    @Column(name = "AAA221")
    var aaa221: String? = null

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

    @Column(name = "ATB90R")
    var atb90r: String? = null

    @Column(name = "ATB90S")
    var atb90s: String? = null

    @Column(name = "ATB90T")
    var atb90t: String? = null

    @Column(name = "ATB930")
    var atb930: Long? = null

    @Column(name = "ATB931")
    var atb931: String? = null

    @Column(name = "BBB002")
    var bbb002: String? = null

    @Column(name = "ATB932")
    var atb932: String? = null

    @Column(name = "ATB933")
    var atb933: String? = null

    @Column(name = "ATB934")
    var atb934: String? = null

    @Column(name = "ATB935")
    var atb935: String? = null

    @Column(name = "ATB936")
    var atb936: String? = null

    @Column(name = "ATB937")
    var atb937: String? = null

    @Column(name = "ATB938")
    var atb938: String? = null

    @Column(name = "AAE006")
    var aae006: String? = null

    @Column(name = "ATB90AC")
    var atb90ac: String? = null

    @Column(name = "ATB939")
    var atb939: Long? = null

    @Column(name = "ATB90Y")
    var atb90y: String? = null

    @Column(name = "ATB90AA")
    var atb90aa: String? = null

    @Column(name = "ATB90AB")
    var atb90ab: Long? = null

    @Column(name = "ATB90C")
    var atb90c: String? = null

    @Column(name = "ATB90A")
    var atb90a: BigDecimal? = null

    @Column(name = "ATB90B")
    var atb90b: BigDecimal? = null

    @Column(name = "ATB90Z")
    var atb90z: BigDecimal? = null

    @Column(name = "ATB90AD")
    var atb90ad: String? = null

    @Column(name = "ATB90AE")
    var atb90ae: String? = null

    @Column(name = "ATB90AF")
    var atb90af: String? = null

    @Column(name = "AAA030")
    var aaa030: Long? = null

    @Column(name = "AAA031")
    var aaa031: String? = null

    @Column(name = "ZZZ011")
    var zzz011: String? = null

    @Column(name = "ATC90P")
    var atc90p: String? = null

    @Column(name = "ATC90PA")
    var atc90pa: String? = null

    @Column(name = "ATC90PB")
    var atc90pb: String? = null

    @Column(name = "ATC90W")
    var atc90w: String? = null

}
