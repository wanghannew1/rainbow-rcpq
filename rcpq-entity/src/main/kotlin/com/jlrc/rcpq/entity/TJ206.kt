package com.jlrc.rcpq.entity

import jakarta.persistence.*
import java.io.Serializable
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "TJ206")
open class TJ206 {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_TJ206_TJ2060")
    @SequenceGenerator(name = "SEQ_TJ206_TJ2060", sequenceName = "SEQ_TJ206_TJ2060", allocationSize = 1)
    @Column(name = "TJ2060")
    var tj2060: Long? = null

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

    @Column(name = "TJ2061")
    var tj2061: String? = null

    @Column(name = "TJ206A")
    var tj206a: BigDecimal? = null

    @Column(name = "TJ206B")
    var tj206b: BigDecimal? = null

    @Column(name = "TJ206C")
    var tj206c: BigDecimal? = null

    @Column(name = "TJ206D")
    var tj206d: BigDecimal? = null

    @Column(name = "TJ206E")
    var tj206e: BigDecimal? = null

    @Column(name = "TJ206F")
    var tj206f: BigDecimal? = null

    @Column(name = "TJ206G")
    var tj206g: BigDecimal? = null

    @Column(name = "TJ206H")
    var tj206h: BigDecimal? = null

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
