package com.jlrc.rcpq.entity

import jakarta.persistence.*
import java.io.Serializable
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "TC9U")
open class TC9U {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_TC9U_ATC9U0")
    @SequenceGenerator(name = "SEQ_TC9U_ATC9U0", sequenceName = "SEQ_TC9U_ATC9U0", allocationSize = 1)
    @Column(name = "ATC9U0")
    var atc9u0: Long? = null

    @Column(name = "BAZ001")
    var baz001: Long? = null

    @Column(name = "BAZ002")
    var baz002: Long? = null

    @Column(name = "AAE002")
    var aae002: Long? = null

    @Column(name = "ATC900")
    var atc900: Long? = null

    @Column(name = "ATB900")
    var atb900: Long? = null

    @Column(name = "AAB001")
    var aab001: Long? = null

    @Column(name = "AAB004")
    var aab004: String? = null

    @Column(name = "AAA100")
    var aaa100: String? = null

    @Column(name = "AAA101")
    var aaa101: String? = null

    @Column(name = "AAC001")
    var aac001: String? = null

    @Column(name = "ATB90X")
    var atb90x: String? = null

    @Column(name = "ATC9U1")
    var atc9u1: String? = null

    @Column(name = "AAA030")
    var aaa030: Long? = null

    @Column(name = "AAA031")
    var aaa031: String? = null

    @Column(name = "ABP001")
    var abp001: String? = null

    @Column(name = "AAC002")
    var aac002: String? = null

    @Column(name = "AAC003")
    var aac003: String? = null

    @Column(name = "ABP004")
    var abp004: String? = null

    @Column(name = "ABP005")
    var abp005: String? = null

    @Column(name = "ABP006")
    var abp006: String? = null

    @Column(name = "ABP007")
    var abp007: String? = null

    @Column(name = "ABP008")
    var abp008: String? = null

    @Column(name = "ABP009")
    var abp009: String? = null

    @Column(name = "ABP00A")
    var abp00a: String? = null

    @Column(name = "ABP00B")
    var abp00b: String? = null

    @Column(name = "ABP00C")
    var abp00c: String? = null

    @Column(name = "ABP00D")
    var abp00d: String? = null

    @Column(name = "ABP00E")
    var abp00e: String? = null

    @Column(name = "ABP00F")
    var abp00f: String? = null

    @Column(name = "ABP00G")
    var abp00g: String? = null

    @Column(name = "ABP00H")
    var abp00h: String? = null

    @Column(name = "ABP00I")
    var abp00i: String? = null

    @Column(name = "ABP00J")
    var abp00j: String? = null

    @Column(name = "ABP00K")
    var abp00k: String? = null

    @Column(name = "ABP00L")
    var abp00l: String? = null

    @Column(name = "ABP00M")
    var abp00m: String? = null

    @Column(name = "ABP00N")
    var abp00n: String? = null

    @Column(name = "ABP00O")
    var abp00o: String? = null

    @Column(name = "ABP00P")
    var abp00p: String? = null

    @Column(name = "ABP00Q")
    var abp00q: String? = null

    @Column(name = "ABP00R")
    var abp00r: String? = null

    @Column(name = "ABP00S")
    var abp00s: String? = null

    @Column(name = "ABP00T")
    var abp00t: String? = null

    @Column(name = "ABP00U")
    var abp00u: String? = null

    @Column(name = "ABP00V")
    var abp00v: String? = null

    @Column(name = "ABP00W")
    var abp00w: String? = null

    @Column(name = "ABP00X")
    var abp00x: String? = null

    @Column(name = "ATC9U8")
    var atc9u8: String? = null

    @Column(name = "ATC9U2")
    var atc9u2: String? = null

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

    @Column(name = "ATC9UZ")
    var atc9uz: String? = null

    @Column(name = "ATC904")
    var atc904: Long? = null

    @Column(name = "ATC9UY")
    var atc9uy: String? = null

    @Column(name = "AAA004")
    var aaa004: String? = null

    @Column(name = "AAE003")
    var aae003: Long? = null

    @Column(name = "ABPZZZ")
    var abpzzz: String? = null

    @Column(name = "ATC9U0_1")
    var atc9u0_1: Long? = null

    @Column(name = "TMP001")
    var tmp001: String? = null

    @Column(name = "TMP002")
    var tmp002: String? = null

    @Column(name = "TMP003")
    var tmp003: String? = null

    @Column(name = "TMP004")
    var tmp004: String? = null

    @Column(name = "TMP005")
    var tmp005: String? = null

    @Column(name = "TMP006")
    var tmp006: String? = null

    @Column(name = "TMP007")
    var tmp007: String? = null

    @Column(name = "TMP008")
    var tmp008: String? = null

    @Column(name = "TMP009")
    var tmp009: String? = null

    @Column(name = "TMP010")
    var tmp010: String? = null

    @Column(name = "TMP011")
    var tmp011: String? = null

    @Column(name = "TMP012")
    var tmp012: String? = null

    @Column(name = "TMP013")
    var tmp013: String? = null

    @Column(name = "TMP014")
    var tmp014: String? = null

    @Column(name = "TMP015")
    var tmp015: String? = null

    @Column(name = "TMP016")
    var tmp016: String? = null

    @Column(name = "ZYNUM")
    var zynum: String? = null

    @Column(name = "JYNUM")
    var jynum: String? = null

}
