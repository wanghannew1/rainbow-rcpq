package com.jlrc.rcpq.entity

import jakarta.persistence.*
import java.io.Serializable
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "TC9C")
open class TC9C {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_TC9C_ATC9C0")
    @SequenceGenerator(name = "SEQ_TC9C_ATC9C0", sequenceName = "SEQ_TC9C_ATC9C0", allocationSize = 1)
    @Column(name = "ATC9C0")
    var atc9c0: Long? = null

    @Column(name = "BAZ001")
    var baz001: Long? = null

    @Column(name = "BAZ002")
    var baz002: Long? = null

    @Column(name = "ATC970")
    var atc970: Long? = null

    @Column(name = "ATC990")
    var atc990: Long? = null

    @Column(name = "ATC9C1")
    var atc9c1: Long? = null

    @Column(name = "ATC9C2")
    var atc9c2: Long? = null

    @Column(name = "ATC9C3")
    var atc9c3: BigDecimal? = null

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

    @Column(name = "AAE003")
    var aae003: Long? = null

    @Column(name = "AAE013")
    var aae013: String? = null

}
