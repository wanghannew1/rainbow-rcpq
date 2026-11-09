package com.jlrc.rcpq.entity

import jakarta.persistence.*
import java.io.Serializable
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "TC8C")
open class TC8C {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_TC8C_ATC8C0")
    @SequenceGenerator(name = "SEQ_TC8C_ATC8C0", sequenceName = "SEQ_TC8C_ATC8C0", allocationSize = 1)
    @Column(name = "ATC8C0")
    var atc8c0: Long? = null

    @Column(name = "ATC830")
    var atc830: Long? = null

    @Column(name = "ATC831")
    var atc831: String? = null

    @Column(name = "ATC8CC")
    var atc8cc: String? = null

    @Column(name = "AAE006")
    var aae006: String? = null

    @Column(name = "ATC8C3")
    var atc8c3: String? = null

    @Column(name = "AAC004")
    var aac004: String? = null

    @Column(name = "ATC8C4")
    var atc8c4: LocalDate? = null

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

    @Column(name = "AAC003")
    var aac003: String? = null

    @Column(name = "BAB305")
    var bab305: String? = null

    @Column(name = "AAB299")
    var aab299: String? = null

}
