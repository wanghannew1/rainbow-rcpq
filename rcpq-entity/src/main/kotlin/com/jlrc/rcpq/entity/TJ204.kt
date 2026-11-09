package com.jlrc.rcpq.entity

import jakarta.persistence.*
import java.io.Serializable
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "TJ204")
open class TJ204 {
    @Id
    @Column(name = "TJ2040")
    var tj2040: String? = null

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

    @Column(name = "TJ2041")
    var tj2041: Long? = null

    @Column(name = "TJ2042")
    var tj2042: Long? = null

    @Column(name = "TJ2043")
    var tj2043: Long? = null

    @Column(name = "TJ2044")
    var tj2044: Long? = null

    @Column(name = "TJ2045")
    var tj2045: Long? = null

    @Column(name = "TJ2046")
    var tj2046: Long? = null

    @Column(name = "TJ2047")
    var tj2047: Long? = null

    @Column(name = "TJ2048")
    var tj2048: Long? = null

    @Column(name = "TJ2049")
    var tj2049: Long? = null

    @Column(name = "TJ204A")
    var tj204a: Long? = null

    @Column(name = "TJ204B")
    var tj204b: Long? = null

    @Column(name = "TJ204C")
    var tj204c: Long? = null

    @Column(name = "TJ204D")
    var tj204d: Long? = null

    @Column(name = "TJ204E")
    var tj204e: Long? = null

    @Column(name = "TJ204F")
    var tj204f: Long? = null

    @Column(name = "TJ204G")
    var tj204g: Long? = null

    @Column(name = "TJ204H")
    var tj204h: Long? = null

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
