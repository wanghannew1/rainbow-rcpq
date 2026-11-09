package com.jlrc.rcpq.entity

import jakarta.persistence.*
import java.io.Serializable
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "TC96_TMP")
open class TC96_TMP {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_TC96_TMP_ATC96T")
    @SequenceGenerator(name = "SEQ_TC96_TMP_ATC96T", sequenceName = "SEQ_TC96_TMP_ATC96T", allocationSize = 1)
    @Column(name = "ATC96T")
    var atc96t: Long? = null

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

}
