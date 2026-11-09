package com.jlrc.rcpq.entity

import jakarta.persistence.*
import java.io.Serializable
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "TJ205")
open class TJ205 {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_TJ205_TJ2050")
    @SequenceGenerator(name = "SEQ_TJ205_TJ2050", sequenceName = "SEQ_TJ205_TJ2050", allocationSize = 1)
    @Column(name = "TJ2050")
    var tj2050: Long? = null

    @Column(name = "BAZ001")
    var baz001: Long? = null

    @Column(name = "BAZ002")
    var baz002: Long? = null

    @Column(name = "AAE030")
    var aae030: String? = null

    @Column(name = "AAE031")
    var aae031: String? = null

    @Column(name = "AAA020")
    var aaa020: String? = null

    @Column(name = "TJ2051")
    var tj2051: String? = null

    @Column(name = "TJ2052")
    var tj2052: String? = null

    @Column(name = "TJ205A")
    var tj205a: BigDecimal? = null

    @Column(name = "TJ205B")
    var tj205b: BigDecimal? = null

    @Column(name = "TJ205C")
    var tj205c: BigDecimal? = null

    @Column(name = "TJ205D")
    var tj205d: BigDecimal? = null

    @Column(name = "TJ205E")
    var tj205e: BigDecimal? = null

    @Column(name = "TJ205F")
    var tj205f: BigDecimal? = null

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
