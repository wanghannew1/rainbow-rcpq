package com.jlrc.rcpq.entity

import jakarta.persistence.*
import java.io.Serializable
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "TC9X")
open class TC9X {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_TC9X_ATC9X0")
    @SequenceGenerator(name = "SEQ_TC9X_ATC9X0", sequenceName = "SEQ_TC9X_ATC9X0", allocationSize = 1)
    @Column(name = "ATC9X0")
    var atc9x0: Long? = null

    @Column(name = "BAZ001")
    var baz001: Long? = null

    @Column(name = "BAZ002")
    var baz002: Long? = null

    @Column(name = "AAC001")
    var aac001: Long? = null

    @Column(name = "ATC900")
    var atc900: Long? = null

    @Column(name = "ATC9X1")
    var atc9x1: String? = null

    @Column(name = "ATC9X2")
    var atc9x2: LocalDate? = null

    @Column(name = "ATC9X3")
    var atc9x3: LocalDate? = null

    @Column(name = "ATC9X4")
    var atc9x4: String? = null

    @Column(name = "ATC9X5")
    var atc9x5: String? = null

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

    @Column(name = "AAE013")
    var aae013: String? = null

}
