package com.jlrc.rcpq.entity

import jakarta.persistence.*
import java.io.Serializable
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "TJ031")
open class TJ031 {
    @Id
    @Column(name = "TJ0310")
    var tj0310: Long? = null

    @Column(name = "BAZ001")
    var baz001: Long? = null

    @Column(name = "BAZ002")
    var baz002: Long? = null

    @Column(name = "TJ0000")
    var tj0000: Long? = null

    @Column(name = "AAA020")
    var aaa020: String? = null

    @Column(name = "AAA021")
    var aaa021: String? = null

    @Column(name = "AAE001")
    var aae001: Long? = null

    @Column(name = "TJ0004")
    var tj0004: Long? = null

    @Column(name = "TJ0005")
    var tj0005: String? = null

    @Column(name = "TJ0006")
    var tj0006: String? = null

    @Column(name = "T03101")
    var t03101: Long? = null

    @Column(name = "T03102")
    var t03102: Long? = null

    @Column(name = "T03103")
    var t03103: Long? = null

    @Column(name = "T03104")
    var t03104: Long? = null

    @Column(name = "T03105")
    var t03105: Long? = null

    @Column(name = "T03106")
    var t03106: BigDecimal? = null

    @Column(name = "T03107")
    var t03107: BigDecimal? = null

    @Column(name = "T03108")
    var t03108: BigDecimal? = null

    @Column(name = "T03109")
    var t03109: BigDecimal? = null

    @Column(name = "T03110")
    var t03110: BigDecimal? = null

    @Column(name = "T03111")
    var t03111: Long? = null

    @Column(name = "T03112")
    var t03112: Long? = null

    @Column(name = "T03113")
    var t03113: Long? = null

    @Column(name = "T03114")
    var t03114: Long? = null

    @Column(name = "T03115")
    var t03115: Long? = null

    @Column(name = "T03116")
    var t03116: Long? = null

    @Column(name = "T03117")
    var t03117: Long? = null

    @Column(name = "T03118")
    var t03118: Long? = null

    @Column(name = "T03119")
    var t03119: Long? = null

    @Column(name = "T03120")
    var t03120: Long? = null

    @Column(name = "T03121")
    var t03121: Long? = null

    @Column(name = "T03122")
    var t03122: Long? = null

    @Column(name = "T03123")
    var t03123: Long? = null

    @Column(name = "T03124")
    var t03124: Long? = null

    @Column(name = "T03125")
    var t03125: Long? = null

    @Column(name = "T03126")
    var t03126: Long? = null

    @Column(name = "T03127")
    var t03127: Long? = null

    @Column(name = "T03128")
    var t03128: Long? = null

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
