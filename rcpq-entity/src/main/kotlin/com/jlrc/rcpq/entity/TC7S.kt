package com.jlrc.rcpq.entity

import jakarta.persistence.*
import java.io.Serializable
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "TC7S")
open class TC7S {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_TC7S_ATC7S0")
    @SequenceGenerator(name = "SEQ_TC7S_ATC7S0", sequenceName = "SEQ_TC7S_ATC7S0", allocationSize = 1)
    @Column(name = "ATC7S0")
    var atc7s0: Long? = null

    @Column(name = "BAZ001")
    var baz001: Long? = null

    @Column(name = "BAZ002")
    var baz002: Long? = null

    @Column(name = "AAC003")
    var aac003: String? = null

    @Column(name = "AAC002")
    var aac002: String? = null

    @Column(name = "AAC004")
    var aac004: String? = null

    @Column(name = "ATC702")
    var atc702: String? = null

    @Column(name = "ATC7S1")
    var atc7s1: LocalDate? = null

    @Column(name = "ATC7S2")
    var atc7s2: String? = null

    @Column(name = "ATC7S3")
    var atc7s3: String? = null

    @Column(name = "ATC7S4")
    var atc7s4: String? = null

    @Column(name = "ATC7S5")
    var atc7s5: String? = null

    @Column(name = "ATC7S6")
    var atc7s6: LocalDate? = null

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

    @Column(name = "BAE011")
    var bae011: String? = null

    @Column(name = "BAE017")
    var bae017: String? = null

    @Column(name = "BAE036")
    var bae036: LocalDate? = null

    @Column(name = "BAE019")
    var bae019: String? = null

    @Column(name = "BAE020")
    var bae020: String? = null

    @Column(name = "BAE022")
    var bae022: String? = null

    @Column(name = "ATC7S7")
    var atc7s7: String? = null

}
