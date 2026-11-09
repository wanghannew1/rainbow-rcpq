package com.jlrc.rcpq.entity

import jakarta.persistence.*
import java.io.Serializable
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "TJ208")
open class TJ208 {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_TJ208_TJ2080")
    @SequenceGenerator(name = "SEQ_TJ208_TJ2080", sequenceName = "SEQ_TJ208_TJ2080", allocationSize = 1)
    @Column(name = "TJ2080")
    var tj2080: Long? = null

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

    @Column(name = "TJ2081")
    var tj2081: String? = null

    @Column(name = "TJ2082")
    var tj2082: String? = null

    @Column(name = "TJ2083")
    var tj2083: String? = null

    @Column(name = "TJ208A")
    var tj208a: BigDecimal? = null

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
