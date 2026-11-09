package com.jlrc.rcpq.entity

import jakarta.persistence.*
import java.io.Serializable
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "TJ014")
open class TJ014 {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_TJ014_TJ0140")
    @SequenceGenerator(name = "SEQ_TJ014_TJ0140", sequenceName = "SEQ_TJ014_TJ0140", allocationSize = 1)
    @Column(name = "TJ0140")
    var tj0140: Long? = null

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

    @Column(name = "TJ0141")
    var tj0141: String? = null

    @Column(name = "TJ0142")
    var tj0142: String? = null

    @Column(name = "TJ0143")
    var tj0143: Long? = null

    @Column(name = "TJ0144")
    var tj0144: Long? = null

    @Column(name = "TJ0145")
    var tj0145: Long? = null

    @Column(name = "TJ0146")
    var tj0146: Long? = null

    @Column(name = "TJ0147")
    var tj0147: Long? = null

    @Column(name = "TJ0148")
    var tj0148: Long? = null

    @Column(name = "TJ0149")
    var tj0149: Long? = null

    @Column(name = "TJ014A")
    var tj014a: Long? = null

    @Column(name = "TJ014B")
    var tj014b: Long? = null

    @Column(name = "TJ014C")
    var tj014c: Long? = null

    @Column(name = "TJ014D")
    var tj014d: Long? = null

    @Column(name = "TJ014E")
    var tj014e: Long? = null

    @Column(name = "TJ014F")
    var tj014f: Long? = null

    @Column(name = "TJ014G")
    var tj014g: Long? = null

    @Column(name = "TJ014H")
    var tj014h: Long? = null

    @Column(name = "TJ014J")
    var tj014j: Long? = null

    @Column(name = "TJ014K")
    var tj014k: Long? = null

    @Column(name = "TJ014M")
    var tj014m: Long? = null

    @Column(name = "TJ014N")
    var tj014n: Long? = null

    @Column(name = "TJ014P")
    var tj014p: Long? = null

    @Column(name = "TJ014Q")
    var tj014q: Long? = null

    @Column(name = "AAE013")
    var aae013: String? = null

    @Column(name = "TJ014U")
    var tj014u: String? = null

    @Column(name = "TJ014V")
    var tj014v: String? = null

    @Column(name = "TJ014W")
    var tj014w: String? = null

    @Column(name = "TJ014X")
    var tj014x: String? = null

    @Column(name = "TJ014Y")
    var tj014y: LocalDateTime? = null

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
