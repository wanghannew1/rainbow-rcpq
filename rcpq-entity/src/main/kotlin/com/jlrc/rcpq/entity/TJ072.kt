package com.jlrc.rcpq.entity

import jakarta.persistence.*
import java.io.Serializable
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "TJ072")
open class TJ072 {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_TJ072_TJ0720")
    @SequenceGenerator(name = "SEQ_TJ072_TJ0720", sequenceName = "SEQ_TJ072_TJ0720", allocationSize = 1)
    @Column(name = "TJ0720")
    var tj0720: Long? = null

    @Column(name = "TJ0000")
    var tj0000: Long? = null

    @Column(name = "AAA020")
    var aaa020: String? = null

    @Column(name = "AAA021")
    var aaa021: String? = null

    @Column(name = "AAE001")
    var aae001: Long? = null

    @Column(name = "AAE00A")
    var aae00a: Long? = null

    @Column(name = "AC0009")
    var ac0009: Long? = null

    @Column(name = "TJ0721")
    var tj0721: Long? = null

    @Column(name = "TJ0722")
    var tj0722: Long? = null

    @Column(name = "TJ0723")
    var tj0723: Long? = null

    @Column(name = "TJ0724")
    var tj0724: Long? = null

    @Column(name = "TJ0725")
    var tj0725: Long? = null

    @Column(name = "TJ0726")
    var tj0726: Long? = null

    @Column(name = "TJ0727")
    var tj0727: Long? = null

    @Column(name = "TJ0728")
    var tj0728: Long? = null

    @Column(name = "TJ0729")
    var tj0729: Long? = null

    @Column(name = "TJ072A")
    var tj072a: Long? = null

    @Column(name = "TJ072B")
    var tj072b: Long? = null

    @Column(name = "AC000A")
    var ac000a: String? = null

    @Column(name = "AC000B")
    var ac000b: String? = null

    @Column(name = "AC000C")
    var ac000c: String? = null

    @Column(name = "AC000D")
    var ac000d: String? = null

    @Column(name = "AC000E")
    var ac000e: LocalDate? = null

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
