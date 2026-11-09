package com.jlrc.rcpq.entity

import jakarta.persistence.*
import java.io.Serializable
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "TC90_INNER")
@IdClass(TC90_INNERId::class)
open class TC90_INNER {
    @Id
    @Column(name = "ATC900")
    var atc900: Long? = null

    @Id
    @Column(name = "FLAG")
    var flag: String? = null

    @Column(name = "AAC003")
    var aac003: String? = null

    @Column(name = "ISRED_AAC003")
    var isred_aac003: String? = null

    @Column(name = "AAC002")
    var aac002: String? = null

    @Column(name = "ISRED_AAC002")
    var isred_aac002: String? = null

    @Column(name = "AAA001")
    var aaa001: String? = null

    @Column(name = "ISRED_AAA001")
    var isred_aaa001: String? = null

    @Column(name = "AAA301")
    var aaa301: String? = null

    @Column(name = "ISRED_AAA301")
    var isred_aaa301: String? = null

    @Column(name = "AAA303")
    var aaa303: String? = null

    @Column(name = "ISRED_AAA303")
    var isred_aaa303: String? = null

    @Column(name = "ATB90S")
    var atb90s: String? = null

    @Column(name = "ISRED_ATB90S")
    var isred_atb90s: String? = null

    @Column(name = "ATC9V1")
    var atc9v1: String? = null

    @Column(name = "ISRED_ATC9V1")
    var isred_atc9v1: String? = null

    @Column(name = "AAA003")
    var aaa003: String? = null

    @Column(name = "ISRED_AAA003")
    var isred_aaa003: String? = null

    @Column(name = "BBB001")
    var bbb001: String? = null

    @Column(name = "ISRED_BBB001")
    var isred_bbb001: String? = null

    @Column(name = "AAB004")
    var aab004: String? = null

    @Column(name = "AAC009")
    var aac009: String? = null

    @Column(name = "ISRED_AAC009")
    var isred_aac009: String? = null

    @Column(name = "RESULT")
    var result: String? = null

    @Column(name = "BAA001")
    var baa001: String? = null

    @Column(name = "CAA002")
    var caa002: String? = null

    @Column(name = "ISRED_CAA002")
    var isred_caa002: String? = null

    @Column(name = "ATC90P")
    var atc90p: String? = null

    @Column(name = "ATC90W")
    var atc90w: String? = null

    @Column(name = "ATC904")
    var atc904: Long? = null

    @Column(name = "ISRED_ATC904")
    var isred_atc904: String? = null

}
data class TC90_INNERId(
    val atc900: Long? = null,
    val flag: String? = null,
) : Serializable
