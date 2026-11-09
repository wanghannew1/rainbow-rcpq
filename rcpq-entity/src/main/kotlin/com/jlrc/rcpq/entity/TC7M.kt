package com.jlrc.rcpq.entity

import jakarta.persistence.*
import java.io.Serializable
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "TC7M")
open class TC7M {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_TC7M_ATC7M0")
    @SequenceGenerator(name = "SEQ_TC7M_ATC7M0", sequenceName = "SEQ_TC7M_ATC7M0", allocationSize = 1)
    @Column(name = "ATC7M0")
    var atc7m0: Long? = null

    @Column(name = "BAZ001")
    var baz001: Long? = null

    @Column(name = "BAZ002")
    var baz002: Long? = null

    @Column(name = "ATC700")
    var atc700: Long? = null

    @Column(name = "ATC7M1")
    var atc7m1: String? = null

    @Column(name = "ATC7L5")
    var atc7l5: String? = null

    @Column(name = "ATC7L6")
    var atc7l6: String? = null

    @Column(name = "AAC003")
    var aac003: String? = null

    @Column(name = "AAC002")
    var aac002: String? = null

    @Column(name = "ATC7M2")
    var atc7m2: String? = null

    @Column(name = "ATC705")
    var atc705: String? = null

    @Column(name = "ATC7LG")
    var atc7lg: String? = null

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

    @Column(name = "ATC702")
    var atc702: String? = null

    @Column(name = "ATC7M3")
    var atc7m3: String? = null

}
