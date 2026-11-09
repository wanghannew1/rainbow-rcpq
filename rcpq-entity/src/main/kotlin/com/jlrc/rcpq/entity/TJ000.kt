package com.jlrc.rcpq.entity

import jakarta.persistence.*
import java.io.Serializable
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "TJ000")
open class TJ000 {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_TJ000_TJ0000")
    @SequenceGenerator(name = "SEQ_TJ000_TJ0000", sequenceName = "SEQ_TJ000_TJ0000", allocationSize = 1)
    @Column(name = "TJ0000")
    var tj0000: Long? = null

    @Column(name = "TJ0001")
    var tj0001: String? = null

    @Column(name = "AAA020")
    var aaa020: String? = null

    @Column(name = "AAA021")
    var aaa021: String? = null

    @Column(name = "AAE001")
    var aae001: Long? = null

    @Column(name = "AAE003")
    var aae003: Long? = null

    @Column(name = "AAE00A")
    var aae00a: Long? = null

    @Column(name = "AAE002")
    var aae002: String? = null

    @Column(name = "TJ0002")
    var tj0002: String? = null

    @Column(name = "TJ0003")
    var tj0003: String? = null

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
