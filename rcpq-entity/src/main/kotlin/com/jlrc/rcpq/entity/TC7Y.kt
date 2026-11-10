package com.jlrc.rcpq.entity

import jakarta.persistence.*
import java.io.Serializable
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "TC7Y")
open class TC7Y {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_TC7Y_ATC7Y0")
    @SequenceGenerator(name = "SEQ_TC7Y_ATC7Y0", sequenceName = "SEQ_TC7Y_ATC7Y0", allocationSize = 1)
    @Column(name = "ATC7Y0")
    var atc7y0: Long? = null

    @Column(name = "BAZ001")
    var baz001: Long? = null

    @Column(name = "BAZ002")
    var baz002: Long? = null

    @Column(name = "ATC7Y1")
    var atc7y1: String? = null

    @Column(name = "AAC003")
    var aac003: String? = null

    @Column(name = "AAC002")
    var aac002: String? = null

    @Column(name = "AAC004")
    var aac004: String? = null

    @Column(name = "ATC7VF")
    var atc7vf: String? = null

    @Column(name = "ATC7V4")
    var atc7v4: LocalDate? = null

    @Column(name = "ATC7W2")
    var atc7w2: Long? = null

    @Column(name = "ATC7Y2")
    var atc7y2: LocalDate? = null

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

    @Column(name = "ATC7V0")
    var atc7v0: BigDecimal? = null
}
