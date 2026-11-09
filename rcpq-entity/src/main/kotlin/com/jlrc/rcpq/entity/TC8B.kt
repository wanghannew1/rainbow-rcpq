package com.jlrc.rcpq.entity

import jakarta.persistence.*
import java.io.Serializable
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "TC8B")
open class TC8B {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_TC8B_ATC8B0")
    @SequenceGenerator(name = "SEQ_TC8B_ATC8B0", sequenceName = "SEQ_TC8B_ATC8B0", allocationSize = 1)
    @Column(name = "ATC8B0")
    var atc8b0: Long? = null

    @Column(name = "AAC002")
    var aac002: String? = null

    @Column(name = "AAC003")
    var aac003: String? = null

    @Column(name = "ATC011")
    var atc011: String? = null

    @Column(name = "ATC8BC")
    var atc8bc: String? = null

    @Column(name = "AAC045")
    var aac045: String? = null

    @Column(name = "ATC8B1")
    var atc8b1: String? = null

    @Column(name = "AAE006")
    var aae006: String? = null

    @Column(name = "AAC004")
    var aac004: String? = null

    @Column(name = "AAC006")
    var aac006: LocalDate? = null

    @Column(name = "AAC024")
    var aac024: String? = null

    @Column(name = "ATC8B7")
    var atc8b7: String? = null

    @Column(name = "ATC8B8")
    var atc8b8: LocalDate? = null

    @Column(name = "ATC8B9")
    var atc8b9: String? = null

    @Column(name = "ATC8BA")
    var atc8ba: LocalDate? = null

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

    @Column(name = "BAZ001")
    var baz001: Long? = null

    @Column(name = "BAZ002")
    var baz002: Long? = null

    @Column(name = "AAC005")
    var aac005: String? = null

    @Column(name = "AAB299")
    var aab299: String? = null

    @Column(name = "BAB305")
    var bab305: String? = null

    @Column(name = "ATC830")
    var atc830: Long? = null

    @Column(name = "AAC001")
    var aac001: Long? = null

    @Column(name = "ATC831")
    var atc831: String? = null

    @Column(name = "ATC8D0")
    var atc8d0: Long? = null

}
