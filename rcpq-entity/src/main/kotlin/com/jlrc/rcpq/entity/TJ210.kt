package com.jlrc.rcpq.entity

import jakarta.persistence.*
import java.io.Serializable
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "TJ210")
open class TJ210 {
    @Id
    @Column(name = "TJ2100")
    var tj2100: String? = null

    @Column(name = "BAZ001")
    var baz001: Long? = null

    @Column(name = "BAZ002")
    var baz002: Long? = null

    @Column(name = "TJ0000")
    var tj0000: String? = null

    @Column(name = "AAA020")
    var aaa020: String? = null

    @Column(name = "AC0004")
    var ac0004: String? = null

    @Column(name = "AC0005")
    var ac0005: String? = null

    @Column(name = "AC0007")
    var ac0007: String? = null

    @Column(name = "AC0008")
    var ac0008: String? = null

    @Column(name = "AC0009")
    var ac0009: Long? = null

    @Column(name = "TJ2101")
    var tj2101: Long? = null

    @Column(name = "TJ2102")
    var tj2102: Long? = null

    @Column(name = "TJ2103")
    var tj2103: Long? = null

    @Column(name = "TJ2104")
    var tj2104: Long? = null

    @Column(name = "TJ2105")
    var tj2105: Long? = null

    @Column(name = "TJ2106")
    var tj2106: Long? = null

    @Column(name = "TJ2107")
    var tj2107: Long? = null

    @Column(name = "TJ2108")
    var tj2108: Long? = null

    @Column(name = "TJ2109")
    var tj2109: Long? = null

    @Column(name = "TJ210A")
    var tj210a: Long? = null

    @Column(name = "TJ210B")
    var tj210b: Long? = null

    @Column(name = "TJ210C")
    var tj210c: Long? = null

    @Column(name = "TJ210D")
    var tj210d: Long? = null

    @Column(name = "TJ210E")
    var tj210e: Long? = null

    @Column(name = "TJ210F")
    var tj210f: Long? = null

    @Column(name = "TJ210G")
    var tj210g: Long? = null

    @Column(name = "TJ210H")
    var tj210h: Long? = null

    @Column(name = "TJ210I")
    var tj210i: Long? = null

    @Column(name = "TJ210J")
    var tj210j: Long? = null

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
