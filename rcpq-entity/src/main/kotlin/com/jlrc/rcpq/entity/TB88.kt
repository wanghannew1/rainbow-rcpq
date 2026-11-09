package com.jlrc.rcpq.entity

import jakarta.persistence.*
import java.io.Serializable
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "TB88")
open class TB88 {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_TB88_ATB880")
    @SequenceGenerator(name = "SEQ_TB88_ATB880", sequenceName = "SEQ_TB88_ATB880", allocationSize = 1)
    @Column(name = "ATB880")
    var atb880: Long? = null

    @Column(name = "ATB881")
    var atb881: String? = null

    @Column(name = "ATB882")
    var atb882: LocalDate? = null

    @Column(name = "ATB883")
    var atb883: String? = null

    @Column(name = "ATB884")
    var atb884: String? = null

    @Column(name = "ATB885")
    var atb885: String? = null

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

    @Column(name = "BAZ001")
    var baz001: Long? = null

    @Column(name = "BAZ002")
    var baz002: Long? = null

}
