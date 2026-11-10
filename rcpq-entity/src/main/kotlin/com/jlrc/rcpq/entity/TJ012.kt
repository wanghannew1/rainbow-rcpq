package com.jlrc.rcpq.entity

import jakarta.persistence.*
import java.io.Serializable
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "TJ012")
open class TJ012 {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_TJ012_TJ0120")
    @SequenceGenerator(name = "SEQ_TJ012_TJ0120", sequenceName = "SEQ_TJ012_TJ0120", allocationSize = 1)
    @Column(name = "TJ0120")
    var tj0120: Long? = null

    @Column(name = "AC0009")
    var ac0009: Long? = null

    @Column(name = "TJ0121")
    var tj0121: String? = null

    @Column(name = "TJ0122")
    var tj0122: String? = null

    @Column(name = "TJ0123")
    var tj0123: Long? = null

    @Column(name = "TJ0124")
    var tj0124: Long? = null

    @Column(name = "TJ0125")
    var tj0125: Long? = null

    @Column(name = "TJ0126")
    var tj0126: Long? = null

    @Column(name = "TJ0127")
    var tj0127: Long? = null

    @Column(name = "TJ0128")
    var tj0128: Long? = null

    @Column(name = "BAZ001")
    var baz001: Long? = null

    @Column(name = "BAZ002")
    var baz002: Long? = null

    @Column(name = "AAB004")
    var aab004: String? = null
    @Column(name = "TJ0110")
    var tj0110: BigDecimal? = null
}
