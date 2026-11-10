package com.jlrc.rcpq.entity

import jakarta.persistence.*
import java.io.Serializable
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "TC7H")
open class TC7H {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_TC7H_ATC7H0")
    @SequenceGenerator(name = "SEQ_TC7H_ATC7H0", sequenceName = "SEQ_TC7H_ATC7H0", allocationSize = 1)
    @Column(name = "ATC7H0")
    var atc7h0: Long? = null

    @Column(name = "BAZ001")
    var baz001: Long? = null

    @Column(name = "BAZ002")
    var baz002: Long? = null

    @Column(name = "AAC003")
    var aac003: String? = null

    @Column(name = "AAC002")
    var aac002: String? = null

    @Column(name = "ATC702")
    var atc702: String? = null

    @Column(name = "ATC7H1")
    var atc7h1: String? = null

    @Column(name = "ATC7H2")
    var atc7h2: LocalDate? = null

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
