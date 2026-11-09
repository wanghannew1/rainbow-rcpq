package com.jlrc.rcpq.entity

import jakarta.persistence.*
import java.io.Serializable
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "TJ011")
open class TJ011 {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_TJ011_TJ0110")
    @SequenceGenerator(name = "SEQ_TJ011_TJ0110", sequenceName = "SEQ_TJ011_TJ0110", allocationSize = 1)
    @Column(name = "TJ0110")
    var tj0110: Long? = null

    @Column(name = "TJ0000")
    var tj0000: Long? = null

    @Column(name = "AAA020")
    var aaa020: String? = null

    @Column(name = "AAA021")
    var aaa021: String? = null

    @Column(name = "AAE002")
    var aae002: String? = null

    @Column(name = "AAE00A")
    var aae00a: Long? = null

    @Column(name = "AC0009")
    var ac0009: Long? = null

    @Column(name = "TJ0111")
    var tj0111: String? = null

    @Column(name = "TJ0112")
    var tj0112: String? = null

    @Column(name = "TJ0113")
    var tj0113: Long? = null

    @Column(name = "TJ0114")
    var tj0114: Long? = null

    @Column(name = "TJ0115")
    var tj0115: Long? = null

    @Column(name = "TJ0116")
    var tj0116: Long? = null

    @Column(name = "TJ0117")
    var tj0117: Long? = null

    @Column(name = "AAE013")
    var aae013: String? = null

    @Column(name = "TJ0118")
    var tj0118: String? = null

    @Column(name = "TJ0119")
    var tj0119: String? = null

    @Column(name = "TJ011A")
    var tj011a: String? = null

    @Column(name = "TJ011B")
    var tj011b: String? = null

    @Column(name = "TJ011C")
    var tj011c: LocalDate? = null

    @Column(name = "TJ000X")
    var tj000x: String? = null

    @Column(name = "TJ000W")
    var tj000w: String? = null

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
