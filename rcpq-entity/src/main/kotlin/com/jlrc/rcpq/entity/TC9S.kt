package com.jlrc.rcpq.entity

import jakarta.persistence.*
import java.io.Serializable
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "TC9S")
open class TC9S {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_TC9S_ATC9S0")
    @SequenceGenerator(name = "SEQ_TC9S_ATC9S0", sequenceName = "SEQ_TC9S_ATC9S0", allocationSize = 1)
    @Column(name = "ATC9S0")
    var atc9s0: Long? = null

    @Column(name = "BAZ001")
    var baz001: Long? = null

    @Column(name = "BAZ002")
    var baz002: Long? = null

    @Column(name = "ATB900")
    var atb900: Long? = null

    @Column(name = "AAB001")
    var aab001: Long? = null

    @Column(name = "AAB004")
    var aab004: String? = null

    @Column(name = "ATB90X")
    var atb90x: String? = null

    @Column(name = "ATC9S1")
    var atc9s1: Long? = null

    @Column(name = "ATC9S2")
    var atc9s2: Long? = null

    @Column(name = "ATB90A")
    var atb90a: BigDecimal? = null

    @Column(name = "ATC9S3")
    var atc9s3: Long? = null

    @Column(name = "BBA000")
    var bba000: BigDecimal? = null

    @Column(name = "BBA001")
    var bba001: BigDecimal? = null

    @Column(name = "BBA002")
    var bba002: BigDecimal? = null

    @Column(name = "BBA003")
    var bba003: BigDecimal? = null

    @Column(name = "BBA004")
    var bba004: BigDecimal? = null

    @Column(name = "BBA005")
    var bba005: BigDecimal? = null

    @Column(name = "BAA000")
    var baa000: BigDecimal? = null

    @Column(name = "BAA001")
    var baa001: BigDecimal? = null

    @Column(name = "BAA002")
    var baa002: BigDecimal? = null

    @Column(name = "BAA003")
    var baa003: BigDecimal? = null

    @Column(name = "ATC9S4")
    var atc9s4: Long? = null

    @Column(name = "CAA001")
    var caa001: BigDecimal? = null

    @Column(name = "CAA002")
    var caa002: BigDecimal? = null

    @Column(name = "ATC9S5")
    var atc9s5: BigDecimal? = null

    @Column(name = "ATC9S6")
    var atc9s6: BigDecimal? = null

    @Column(name = "ATC9S7")
    var atc9s7: BigDecimal? = null

    @Column(name = "ATC9S8")
    var atc9s8: BigDecimal? = null

    @Column(name = "AAE013")
    var aae013: String? = null

    @Column(name = "ATC9SA")
    var atc9sa: String? = null

    @Column(name = "ATC9SB")
    var atc9sb: String? = null

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

    @Column(name = "ATC9SC")
    var atc9sc: BigDecimal? = null

    @Column(name = "ATC9SD")
    var atc9sd: BigDecimal? = null

}
