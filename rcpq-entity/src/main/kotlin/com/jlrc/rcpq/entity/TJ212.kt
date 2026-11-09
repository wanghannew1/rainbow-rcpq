package com.jlrc.rcpq.entity

import jakarta.persistence.*
import java.io.Serializable
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "TJ212")
open class TJ212 {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_TJ212_TJ2120")
    @SequenceGenerator(name = "SEQ_TJ212_TJ2120", sequenceName = "SEQ_TJ212_TJ2120", allocationSize = 1)
    @Column(name = "TJ2120")
    var tj2120: String? = null

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

    @Column(name = "AAB001")
    var aab001: Long? = null

    @Column(name = "AAB004")
    var aab004: String? = null

    @Column(name = "TJ2122")
    var tj2122: Long? = null

    @Column(name = "TJ2121")
    var tj2121: String? = null

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
