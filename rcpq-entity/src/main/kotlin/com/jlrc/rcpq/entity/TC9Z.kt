package com.jlrc.rcpq.entity

import jakarta.persistence.*
import java.io.Serializable
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "TC9Z")
open class TC9Z {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_TC9Z_ATC9Z0")
    @SequenceGenerator(name = "SEQ_TC9Z_ATC9Z0", sequenceName = "SEQ_TC9Z_ATC9Z0", allocationSize = 1)
    @Column(name = "ATC9Z0")
    var atc9z0: Long? = null

    @Column(name = "BAZ001")
    var baz001: Long? = null

    @Column(name = "BAZ002")
    var baz002: Long? = null

    @Column(name = "ATC900")
    var atc900: Long? = null

    @Column(name = "ATC9Z1")
    var atc9z1: Long? = null

    @Column(name = "ATC9Z2")
    var atc9z2: String? = null

    @Column(name = "ATC9Z3")
    var atc9z3: String? = null

    @Column(name = "ATC9Z4")
    var atc9z4: String? = null

    @Column(name = "ATC9Z5")
    var atc9z5: String? = null

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
