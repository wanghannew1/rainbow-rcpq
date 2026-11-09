package com.jlrc.rcpq.entity

import jakarta.persistence.*
import java.io.Serializable
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "TC96")
open class TC96 {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_TC96_ATC960")
    @SequenceGenerator(name = "SEQ_TC96_ATC960", sequenceName = "SEQ_TC96_ATC960", allocationSize = 1)
    @Column(name = "ATC960")
    var atc960: Long? = null

    @Column(name = "BAZ001")
    var baz001: Long? = null

    @Column(name = "BAZ002")
    var baz002: Long? = null

    @Column(name = "ATC961")
    var atc961: String? = null

    @Column(name = "ATC962")
    var atc962: String? = null

    @Column(name = "ATC963")
    var atc963: String? = null

    @Column(name = "ATC964")
    var atc964: Long? = null

    @Column(name = "ATC965")
    var atc965: BigDecimal? = null

    @Column(name = "ATC966")
    var atc966: BigDecimal? = null

    @Column(name = "ATC967")
    var atc967: BigDecimal? = null

    @Column(name = "ATC968")
    var atc968: BigDecimal? = null

    @Column(name = "ATC969")
    var atc969: BigDecimal? = null

    @Column(name = "ATC96A")
    var atc96a: BigDecimal? = null

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

    @Column(name = "ATC900")
    var atc900: Long? = null

    @Column(name = "AAC003")
    var aac003: String? = null

    @Column(name = "AAC002")
    var aac002: String? = null

    @Column(name = "AAA001")
    var aaa001: String? = null

    @Column(name = "ATC932")
    var atc932: Long? = null

    @Column(name = "AAA301")
    var aaa301: BigDecimal? = null

    @Column(name = "AAA302")
    var aaa302: BigDecimal? = null

    @Column(name = "BBA000")
    var bba000: BigDecimal? = null

    @Column(name = "BAA000")
    var baa000: BigDecimal? = null

    @Column(name = "BBA100")
    var bba100: BigDecimal? = null

    @Column(name = "BAA100")
    var baa100: BigDecimal? = null

    @Column(name = "ATC96B")
    var atc96b: BigDecimal? = null

    @Column(name = "ATC96C")
    var atc96c: BigDecimal? = null

    @Column(name = "ATC96D")
    var atc96d: BigDecimal? = null

    @Column(name = "ATC96E")
    var atc96e: BigDecimal? = null

    @Column(name = "ATC96F")
    var atc96f: String? = null

    @Column(name = "ATC96G")
    var atc96g: String? = null

}
