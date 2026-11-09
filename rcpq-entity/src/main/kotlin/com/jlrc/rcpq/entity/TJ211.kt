package com.jlrc.rcpq.entity

import jakarta.persistence.*
import java.io.Serializable
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "TJ211")
open class TJ211 {
    @Id
    @Column(name = "TJ2110")
    var tj2110: String? = null

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

    @Column(name = "TJ2111")
    var tj2111: Long? = null

    @Column(name = "TJ2112")
    var tj2112: Long? = null

    @Column(name = "TJ2113")
    var tj2113: Long? = null

    @Column(name = "TJ2114")
    var tj2114: Long? = null

    @Column(name = "TJ2115")
    var tj2115: Long? = null

    @Column(name = "TJ2116")
    var tj2116: Long? = null

    @Column(name = "TJ2117")
    var tj2117: Long? = null

    @Column(name = "TJ2118")
    var tj2118: Long? = null

    @Column(name = "TJ2119")
    var tj2119: Long? = null

    @Column(name = "TJ211A")
    var tj211a: Long? = null

    @Column(name = "TJ211B")
    var tj211b: Long? = null

    @Column(name = "TJ211C")
    var tj211c: Long? = null

    @Column(name = "TJ211D")
    var tj211d: Long? = null

    @Column(name = "TJ211E")
    var tj211e: Long? = null

    @Column(name = "TJ211F")
    var tj211f: Long? = null

    @Column(name = "TJ211G")
    var tj211g: Long? = null

    @Column(name = "TJ211H")
    var tj211h: Long? = null

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
