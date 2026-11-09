package com.jlrc.rcpq.entity

import jakarta.persistence.*
import java.io.Serializable
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "TJCC1")
open class TJCC1 {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_TJCC1_TJCC10")
    @SequenceGenerator(name = "SEQ_TJCC1_TJCC10", sequenceName = "SEQ_TJCC1_TJCC10", allocationSize = 1)
    @Column(name = "TJCC10")
    var tjcc10: Long? = null

    @Column(name = "BAZ001")
    var baz001: Long? = null

    @Column(name = "BAZ002")
    var baz002: Long? = null

    @Column(name = "TJ0000")
    var tj0000: Long? = null

    @Column(name = "TJCC11")
    var tjcc11: String? = null

    @Column(name = "TJCC12")
    var tjcc12: String? = null

    @Column(name = "TJCC13")
    var tjcc13: Long? = null

    @Column(name = "TJCC14")
    var tjcc14: Long? = null

    @Column(name = "TJCC15")
    var tjcc15: Long? = null

    @Column(name = "TJCC16")
    var tjcc16: Long? = null

    @Column(name = "TJCC17")
    var tjcc17: BigDecimal? = null

    @Column(name = "TJCC18")
    var tjcc18: BigDecimal? = null

    @Column(name = "TJCC19")
    var tjcc19: BigDecimal? = null

    @Column(name = "TJCC1A")
    var tjcc1a: Long? = null

    @Column(name = "TJCC1B")
    var tjcc1b: Long? = null

    @Column(name = "TJCC1C")
    var tjcc1c: String? = null

    @Column(name = "TJCC1D")
    var tjcc1d: String? = null

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
