package com.jlrc.rcpq.entity

import jakarta.persistence.*
import java.io.Serializable
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "TJ207")
open class TJ207 {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_TJ207_TJ2070")
    @SequenceGenerator(name = "SEQ_TJ207_TJ2070", sequenceName = "SEQ_TJ207_TJ2070", allocationSize = 1)
    @Column(name = "TJ2070")
    var tj2070: Long? = null

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

    @Column(name = "TJ2071")
    var tj2071: String? = null

    @Column(name = "TJ207A")
    var tj207a: BigDecimal? = null

    @Column(name = "TJ207B")
    var tj207b: BigDecimal? = null

    @Column(name = "TJ207C")
    var tj207c: BigDecimal? = null

    @Column(name = "TJ207D")
    var tj207d: BigDecimal? = null

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
