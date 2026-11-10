package com.jlrc.rcpq.entity

import jakarta.persistence.*
import java.io.Serializable
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "V_AB01_TB93")
open class V_AB01_TB93 {
    @Id
    @Column(name = "ATB930")
    var atb930: Long? = null

    @Column(name = "AAA031")
    var aaa031: String? = null

    @Column(name = "ATB931")
    var atb931: String? = null

    @Column(name = "AAA021")
    var aaa021: String? = null
    @Column(name = "AAA030")
    var aaa030: BigDecimal? = null
    @Column(name = "AAA121")
    var aaa121: String? = null
    @Column(name = "AAA221")
    var aaa221: String? = null
    @Column(name = "AAA803")
    var aaa803: String? = null
    @Column(name = "AAB001")
    var aab001: BigDecimal? = null
    @Column(name = "AAB301")
    var aab301: String? = null
    @Column(name = "AAB311")
    var aab311: String? = null
    @Column(name = "AAB321")
    var aab321: String? = null
    @Column(name = "AAB998")
    var aab998: String? = null
    @Column(name = "AAE006")
    var aae006: String? = null
    @Column(name = "AAE011")
    var aae011: String? = null
    @Column(name = "AAE013")
    var aae013: String? = null
    @Column(name = "AAE017")
    var aae017: String? = null
    @Column(name = "AAE019")
    var aae019: String? = null
    @Column(name = "AAE020")
    var aae020: String? = null
    @Column(name = "AAE022")
    var aae022: String? = null
    @Column(name = "AAE036")
    var aae036: LocalDateTime? = null
    @Column(name = "ATB900")
    var atb900: BigDecimal? = null
    @Column(name = "ATB900_1")
    var atb900_1: BigDecimal? = null
    @Column(name = "ATB90A")
    var atb90a: BigDecimal? = null
    @Column(name = "ATB90AA")
    var atb90aa: String? = null
    @Column(name = "ATB90AB")
    var atb90ab: BigDecimal? = null
    @Column(name = "ATB90AC")
    var atb90ac: String? = null
    @Column(name = "ATB90AD")
    var atb90ad: String? = null
    @Column(name = "ATB90AE")
    var atb90ae: String? = null
    @Column(name = "ATB90AF")
    var atb90af: String? = null
    @Column(name = "ATB90B")
    var atb90b: BigDecimal? = null
    @Column(name = "ATB90C")
    var atb90c: String? = null
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
    @Column(name = "ATB90S")
    var atb90s: String? = null
    @Column(name = "ATB90Y")
    var atb90y: String? = null
    @Column(name = "ATB90Z")
    var atb90z: BigDecimal? = null
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
    @Column(name = "ATB939")
    var atb939: BigDecimal? = null
    @Column(name = "ATB93T")
    var atb93t: String? = null
    @Column(name = "ATB93U")
    var atb93u: BigDecimal? = null
    @Column(name = "BAE011")
    var bae011: String? = null
    @Column(name = "BAZ001")
    var baz001: BigDecimal? = null
    @Column(name = "BAZ002")
    var baz002: BigDecimal? = null
    @Column(name = "BBB002")
    var bbb002: String? = null
}
