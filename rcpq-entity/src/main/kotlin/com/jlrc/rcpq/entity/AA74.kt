package com.jlrc.rcpq.entity

import jakarta.persistence.*
import java.io.Serializable
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "AA74")
open class AA74 {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_AA74_AAA740")
    @SequenceGenerator(name = "SEQ_AA74_AAA740", sequenceName = "SEQ_AA74_AAA740", allocationSize = 1)
    @Column(name = "AAA740")
    var aaa740: Long? = null

    @Column(name = "BAZ001")
    var baz001: Long? = null

    @Column(name = "BAZ002")
    var baz002: Long? = null

    @Column(name = "AAA741")
    var aaa741: String? = null

    @Column(name = "AAA742")
    var aaa742: String? = null

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

    @Column(name = "AAA743")
    var aaa743: String? = null

    @Column(name = "AAE022")
    var aae022: String? = null

}
