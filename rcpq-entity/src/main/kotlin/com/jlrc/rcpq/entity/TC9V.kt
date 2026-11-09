package com.jlrc.rcpq.entity

import jakarta.persistence.*
import java.io.Serializable
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "TC9V")
open class TC9V {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_TC9V_ATC9V0")
    @SequenceGenerator(name = "SEQ_TC9V_ATC9V0", sequenceName = "SEQ_TC9V_ATC9V0", allocationSize = 1)
    @Column(name = "ATC9V0")
    var atc9v0: Long? = null

    @Column(name = "BAZ001")
    var baz001: Long? = null

    @Column(name = "BAZ002")
    var baz002: Long? = null

    @Column(name = "ATC900")
    var atc900: Long? = null

    @Column(name = "AAC001")
    var aac001: Long? = null

    @Column(name = "AAC003")
    var aac003: String? = null

    @Column(name = "AAC002")
    var aac002: String? = null

    @Column(name = "ATC9V1")
    var atc9v1: String? = null

    @Column(name = "ATC9V2")
    var atc9v2: String? = null

    @Column(name = "ATC9V3")
    var atc9v3: Long? = null

    @Column(name = "ATC9U1")
    var atc9u1: BigDecimal? = null

    @Column(name = "ATC9U2")
    var atc9u2: BigDecimal? = null

    @Column(name = "ATC9U5")
    var atc9u5: BigDecimal? = null

    @Column(name = "ATC9V5")
    var atc9v5: BigDecimal? = null

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

    @Column(name = "ATC9V6")
    var atc9v6: String? = null

    @Column(name = "ATB910")
    var atb910: Long? = null

    @Column(name = "ATB90X")
    var atb90x: String? = null

    @Column(name = "AAB004")
    var aab004: String? = null

    @Column(name = "ATB900")
    var atb900: Long? = null

    @Column(name = "ATC9V7")
    var atc9v7: String? = null

    @Column(name = "AAA301")
    var aaa301: BigDecimal? = null

    @Column(name = "AAA301_")
    var aaa301_: BigDecimal? = null

    @Column(name = "ATC9V8")
    var atc9v8: Long? = null

    @Column(name = "ATC9V4")
    var atc9v4: Long? = null

    @Column(name = "ATC9V9")
    var atc9v9: BigDecimal? = null

    @Column(name = "ATC9TE")
    var atc9te: String? = null

    @Column(name = "ATC9E0")
    var atc9e0: Long? = null

    @Column(name = "ATC9VA")
    var atc9va: BigDecimal? = null

    @Column(name = "ATC9VB")
    var atc9vb: BigDecimal? = null

    @Column(name = "ATC9VC")
    var atc9vc: String? = null

    @Column(name = "JSNY")
    var jsny: Long? = null

    @Column(name = "ATC9VD")
    var atc9vd: String? = null

    @Column(name = "ATC93G")
    var atc93g: String? = null

    @Column(name = "AAA030")
    var aaa030: Long? = null

    @Column(name = "AAA031")
    var aaa031: String? = null

    @Column(name = "AAA033")
    var aaa033: String? = null

}
