package com.jlrc.rcpq.entity

import jakarta.persistence.*
import java.io.Serializable
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "V_TC90_TC97")
open class V_TC90_TC97 {
    @Id
    @Column(name = "ATC900")
    var atc900: Long? = null

    @Column(name = "BAZ001")
    var baz001: Long? = null

    @Column(name = "BAZ002")
    var baz002: Long? = null

    @Column(name = "ATB930")
    var atb930: Long? = null

    @Column(name = "ATB900")
    var atb900: Long? = null

    @Column(name = "AAB001")
    var aab001: Long? = null

    @Column(name = "ID")
    var id: String? = null

    @Column(name = "AAB004")
    var aab004: String? = null

    @Column(name = "AAB001_1")
    var aab001_1: Long? = null

    @Column(name = "ID_1")
    var id_1: String? = null

    @Column(name = "AAB004_1")
    var aab004_1: String? = null

    @Column(name = "AAC001")
    var aac001: Long? = null

    @Column(name = "AAC003")
    var aac003: String? = null

    @Column(name = "AAC002")
    var aac002: String? = null

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

    @Column(name = "AAE006")
    var aae006: String? = null

    @Column(name = "AAC009")
    var aac009: String? = null

    @Column(name = "AAC005")
    var aac005: String? = null

    @Column(name = "AAC033")
    var aac033: String? = null

    @Column(name = "AAE005")
    var aae005: String? = null

    @Column(name = "AAC181")
    var aac181: LocalDate? = null

    @Column(name = "AAC180")
    var aac180: String? = null

    @Column(name = "AAC182")
    var aac182: String? = null

    @Column(name = "AAC183")
    var aac183: String? = null

    @Column(name = "AAC007")
    var aac007: LocalDate? = null

    @Column(name = "AAC015")
    var aac015: String? = null

    @Column(name = "AAC184")
    var aac184: LocalDate? = null

    @Column(name = "ACA111")
    var aca111: String? = null

    @Column(name = "ACA112")
    var aca112: String? = null

    @Column(name = "AAC014")
    var aac014: String? = null

    @Column(name = "BAC014")
    var bac014: String? = null

    @Column(name = "AAC185")
    var aac185: LocalDate? = null

    @Column(name = "AAC025")
    var aac025: String? = null

    @Column(name = "AAE008")
    var aae008: Long? = null

    @Column(name = "AAE00D")
    var aae00d: String? = null

    @Column(name = "ATC902")
    var atc902: String? = null

    @Column(name = "ATC90R")
    var atc90r: String? = null

    @Column(name = "AAE009")
    var aae009: String? = null

    @Column(name = "AAE00A")
    var aae00a: String? = null

    @Column(name = "AAC013")
    var aac013: String? = null

    @Column(name = "AAE00P")
    var aae00p: String? = null

    @Column(name = "ATC903")
    var atc903: String? = null

    @Column(name = "ATC904")
    var atc904: Long? = null

    @Column(name = "ATC905")
    var atc905: BigDecimal? = null

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

    @Column(name = "ATC90E")
    var atc90e: LocalDate? = null

    @Column(name = "ATB904")
    var atb904: String? = null

    @Column(name = "ATC90F")
    var atc90f: String? = null

    @Column(name = "ATC90G")
    var atc90g: String? = null

    @Column(name = "ATC90H")
    var atc90h: String? = null

    @Column(name = "AAA001")
    var aaa001: String? = null

    @Column(name = "AAA002")
    var aaa002: String? = null

    @Column(name = "AAA003")
    var aaa003: String? = null

    @Column(name = "AAA004")
    var aaa004: String? = null

    @Column(name = "AAA100")
    var aaa100: String? = null

    @Column(name = "AAA101")
    var aaa101: String? = null

    @Column(name = "AAA201")
    var aaa201: Long? = null

    @Column(name = "AAA202")
    var aaa202: Long? = null

    @Column(name = "AAA301")
    var aaa301: BigDecimal? = null

    @Column(name = "AAA302")
    var aaa302: BigDecimal? = null

    @Column(name = "AAA303")
    var aaa303: BigDecimal? = null

    @Column(name = "AAA304")
    var aaa304: BigDecimal? = null

    @Column(name = "AAA305")
    var aaa305: BigDecimal? = null

    @Column(name = "AAA306")
    var aaa306: BigDecimal? = null

    @Column(name = "ATB90S")
    var atb90s: BigDecimal? = null

    @Column(name = "AAA401")
    var aaa401: String? = null

    @Column(name = "AAA402")
    var aaa402: String? = null

    @Column(name = "AAA403")
    var aaa403: String? = null

    @Column(name = "AAA404")
    var aaa404: String? = null

    @Column(name = "AAA405")
    var aaa405: String? = null

    @Column(name = "AAA406")
    var aaa406: String? = null

    @Column(name = "AAA407")
    var aaa407: String? = null

    @Column(name = "BBB001")
    var bbb001: String? = null

    @Column(name = "BBB002")
    var bbb002: String? = null

    @Column(name = "AAE100")
    var aae100: String? = null

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

    @Column(name = "ATC90I")
    var atc90i: LocalDate? = null

    @Column(name = "ATC90J")
    var atc90j: LocalDate? = null

    @Column(name = "ATC90K")
    var atc90k: String? = null

    @Column(name = "ATC90L")
    var atc90l: String? = null

    @Column(name = "ATC90M")
    var atc90m: String? = null

    @Column(name = "ATC90N")
    var atc90n: String? = null

    @Column(name = "ATC90O")
    var atc90o: BigDecimal? = null

    @Column(name = "AAE013")
    var aae013: String? = null

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

    @Column(name = "CCC001")
    var ccc001: String? = null

    @Column(name = "ATC90X")
    var atc90x: String? = null

    @Column(name = "ATB903")
    var atb903: String? = null

    @Column(name = "ATC90P")
    var atc90p: String? = null

    @Column(name = "ATC90Q")
    var atc90q: LocalDate? = null

    @Column(name = "ATC90T1")
    var atc90t1: String? = null

    @Column(name = "ATC90T2")
    var atc90t2: String? = null

    @Column(name = "ATC90T3")
    var atc90t3: String? = null

    @Column(name = "ATC90T4")
    var atc90t4: String? = null

    @Column(name = "ATC90T5")
    var atc90t5: String? = null

    @Column(name = "ATC90T6")
    var atc90t6: String? = null

    @Column(name = "ATC90U")
    var atc90u: String? = null

    @Column(name = "ATB910")
    var atb910: Long? = null

    @Column(name = "ATB90R")
    var atb90r: String? = null

    @Column(name = "AAA501")
    var aaa501: Long? = null

    @Column(name = "AAA502")
    var aaa502: Long? = null

    @Column(name = "ATC90W")
    var atc90w: String? = null

    @Column(name = "ATC9V1")
    var atc9v1: String? = null

    @Column(name = "ATB90AB")
    var atb90ab: Long? = null

    @Column(name = "ATC90AA")
    var atc90aa: String? = null

    @Column(name = "ATC90AB")
    var atc90ab: LocalDate? = null

    @Column(name = "ATC90AC")
    var atc90ac: LocalDate? = null

    @Column(name = "ATC90AD")
    var atc90ad: BigDecimal? = null

    @Column(name = "ATC90AE")
    var atc90ae: LocalDate? = null

    @Column(name = "ATC90AF")
    var atc90af: String? = null

    @Column(name = "ATC90AG")
    var atc90ag: String? = null

    @Column(name = "ATC90AH")
    var atc90ah: String? = null

    @Column(name = "ATC90AI")
    var atc90ai: String? = null

    @Column(name = "ATC90AJ")
    var atc90aj: String? = null

    @Column(name = "ATC90AK")
    var atc90ak: LocalDate? = null

    @Column(name = "ATC90AL")
    var atc90al: LocalDate? = null

    @Column(name = "ATC90AM")
    var atc90am: String? = null

    @Column(name = "ATC90AN")
    var atc90an: BigDecimal? = null

    @Column(name = "ATC90AO")
    var atc90ao: BigDecimal? = null

    @Column(name = "ATC90AP")
    var atc90ap: BigDecimal? = null

    @Column(name = "ATC90AQ")
    var atc90aq: BigDecimal? = null

    @Column(name = "ATC90AR")
    var atc90ar: Long? = null

    @Column(name = "ATB90AD")
    var atb90ad: String? = null

    @Column(name = "ATC90AS")
    var atc90as: String? = null

    @Column(name = "ATC90AT")
    var atc90at: String? = null

    @Column(name = "ATC977")
    var atc977: String? = null

    @Column(name = "ATC970")
    var atc970: Long? = null

    @Column(name = "ATC978")
    var atc978: String? = null

    @Column(name = "ATC97A")
    var atc97a: String? = null

    @Column(name = "AAE00B")
    var aae00b: String? = null

    @Column(name = "AAE013B")
    var aae013b: String? = null

}
