package com.jlrc.rcpq.entity

import jakarta.persistence.*
import java.io.Serializable
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "V_TB93_AB07")
open class V_TB93_AB07 {
    @Id
    @Column(name = "ATB930")
    var atb930: Long? = null

    @Column(name = "BAZ001")
    var baz001: Long? = null

    @Column(name = "BAZ002")
    var baz002: Long? = null

    @Column(name = "ATB900")
    var atb900: Long? = null

    @Column(name = "AAB001")
    var aab001: Long? = null

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

    @Column(name = "AAB998")
    var aab998: String? = null

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

    @Column(name = "AAE013")
    var aae013: String? = null

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

    @Column(name = "ATB900_1")
    var atb900_1: Long? = null

    @Column(name = "AAC003")
    var aac003: String? = null

    @Column(name = "AAB073")
    var aab073: String? = null

    @Column(name = "AAB075")
    var aab075: String? = null

    @Column(name = "AAB00A")
    var aab00a: String? = null

    @Column(name = "ATB904")
    var atb904: String? = null

    @Column(name = "AAB00E")
    var aab00e: String? = null

}
