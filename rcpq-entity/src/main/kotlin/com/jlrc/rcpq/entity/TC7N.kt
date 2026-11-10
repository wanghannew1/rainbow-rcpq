package com.jlrc.rcpq.entity

import jakarta.persistence.*
import java.io.Serializable
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "TC7N")
open class TC7N {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_TC7N_ATC7N0")
    @SequenceGenerator(name = "SEQ_TC7N_ATC7N0", sequenceName = "SEQ_TC7N_ATC7N0", allocationSize = 1)
    @Column(name = "ATC7N0")
    var atc7n0: Long? = null

    @Column(name = "BAZ001")
    var baz001: Long? = null

    @Column(name = "BAZ002")
    var baz002: Long? = null

    @Column(name = "ATC7L5")
    var atc7l5: String? = null

    @Column(name = "ATC7L6")
    var atc7l6: String? = null

    @Column(name = "AAC003")
    var aac003: String? = null

    @Column(name = "AAC002")
    var aac002: String? = null

    @Column(name = "ATC702")
    var atc702: String? = null

    @Column(name = "AAE005")
    var aae005: String? = null

    @Column(name = "ATC7N1")
    var atc7n1: LocalDate? = null

    @Column(name = "ATC7N2")
    var atc7n2: LocalDate? = null

    @Column(name = "ATC7N3")
    var atc7n3: String? = null

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

    @Column(name = "ATC7L0")
    var atc7l0: BigDecimal? = null
}
