package com.jlrc.rcpq.entity

import jakarta.persistence.*
import java.io.Serializable
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "TJ843")
open class TJ843 {
    @Id
    @Column(name = "TJ8430")
    var tj8430: Long? = null

    @Column(name = "TJ0000")
    var tj0000: Long? = null

    @Column(name = "AAA020")
    var aaa020: String? = null

    @Column(name = "AAA021")
    var aaa021: String? = null

    @Column(name = "TJ0002")
    var tj0002: LocalDate? = null

    @Column(name = "TJ0003")
    var tj0003: LocalDate? = null

    @Column(name = "AC0009")
    var ac0009: Long? = null

    @Column(name = "TJ8431")
    var tj8431: Long? = null

    @Column(name = "TJ8432")
    var tj8432: Long? = null

    @Column(name = "TJ8433")
    var tj8433: Long? = null

    @Column(name = "TJ8434")
    var tj8434: Long? = null

    @Column(name = "TJ8435")
    var tj8435: Long? = null

    @Column(name = "TJ8436")
    var tj8436: Long? = null

    @Column(name = "TJ8437")
    var tj8437: Long? = null

    @Column(name = "TJ8438")
    var tj8438: Long? = null

    @Column(name = "TJ8439")
    var tj8439: Long? = null

    @Column(name = "TJ843A")
    var tj843a: Long? = null

    @Column(name = "TJ843B")
    var tj843b: Long? = null

    @Column(name = "TJ843C")
    var tj843c: Long? = null

    @Column(name = "TJ843D")
    var tj843d: Long? = null

    @Column(name = "TJ843E")
    var tj843e: Long? = null

    @Column(name = "TJ843F")
    var tj843f: Long? = null

    @Column(name = "TJ843G")
    var tj843g: Long? = null

    @Column(name = "TJ843H")
    var tj843h: Long? = null

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
