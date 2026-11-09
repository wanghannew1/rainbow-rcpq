package com.jlrc.rcpq.entity

import jakarta.persistence.*
import java.io.Serializable
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "TC7C")
open class TC7C {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_TC7C_ATC7C0")
    @SequenceGenerator(name = "SEQ_TC7C_ATC7C0", sequenceName = "SEQ_TC7C_ATC7C0", allocationSize = 1)
    @Column(name = "ATC7C0")
    var atc7c0: Long? = null

    @Column(name = "BAZ001")
    var baz001: Long? = null

    @Column(name = "BAZ002")
    var baz002: Long? = null

    @Column(name = "AAC003")
    var aac003: String? = null

    @Column(name = "ATC7C1")
    var atc7c1: String? = null

    @Column(name = "ATC7C2")
    var atc7c2: String? = null

    @Column(name = "ATC7C3")
    var atc7c3: LocalDate? = null

    @Column(name = "ATC7C4")
    var atc7c4: LocalDate? = null

    @Column(name = "ATC7C5")
    var atc7c5: String? = null

    @Column(name = "ATC7C7")
    var atc7c7: String? = null

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

    @Column(name = "ATC7C6")
    var atc7c6: String? = null

}
