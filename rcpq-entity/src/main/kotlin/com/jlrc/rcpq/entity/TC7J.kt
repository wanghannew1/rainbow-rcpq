package com.jlrc.rcpq.entity

import jakarta.persistence.*
import java.io.Serializable
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "TC7J")
open class TC7J {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_TC7J_ATC7J0")
    @SequenceGenerator(name = "SEQ_TC7J_ATC7J0", sequenceName = "SEQ_TC7J_ATC7J0", allocationSize = 1)
    @Column(name = "ATC7J0")
    var atc7j0: Long? = null

    @Column(name = "BAZ001")
    var baz001: Long? = null

    @Column(name = "BAZ002")
    var baz002: Long? = null

    @Column(name = "AAC003")
    var aac003: String? = null

    @Column(name = "ATC702")
    var atc702: String? = null

    @Column(name = "ATC7J1")
    var atc7j1: String? = null

    @Column(name = "ATC7J2")
    var atc7j2: String? = null

    @Column(name = "ATC7J3")
    var atc7j3: LocalDate? = null

    @Column(name = "ATC7J4")
    var atc7j4: String? = null

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

    @Column(name = "ATC700")
    var atc700: BigDecimal? = null
}
