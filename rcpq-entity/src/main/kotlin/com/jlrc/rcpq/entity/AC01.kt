package com.jlrc.rcpq.entity

import jakarta.persistence.*
import java.io.Serializable
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "AC01")
open class AC01 {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_AC01_AAC001")
    @SequenceGenerator(name = "SEQ_AC01_AAC001", sequenceName = "SEQ_AC01_AAC001", allocationSize = 1)
    @Column(name = "AAC001")
    var aac001: Long? = null

    @Column(name = "BAZ001")
    var baz001: Long? = null

    @Column(name = "BAZ002")
    var baz002: Long? = null

    @Column(name = "AAC002")
    var aac002: String? = null

    @Column(name = "AAC003")
    var aac003: String? = null

    @Column(name = "AAC004")
    var aac004: String? = null

    @Column(name = "AAC005")
    var aac005: String? = null

    @Column(name = "AAC006")
    var aac006: LocalDate? = null

    @Column(name = "AAC009")
    var aac009: String? = null

    @Column(name = "AAC024")
    var aac024: String? = null

    @Column(name = "AAC033")
    var aac033: String? = null

    @Column(name = "AAC011")
    var aac011: String? = null

    @Column(name = "AAC017")
    var aac017: String? = null

    @Column(name = "AAC181")
    var aac181: LocalDate? = null

    @Column(name = "AAC180")
    var aac180: String? = null

    @Column(name = "AAC182")
    var aac182: String? = null

    @Column(name = "AAC183")
    var aac183: String? = null

    @Column(name = "AGE")
    var age: Long? = null

    @Column(name = "AAB299")
    var aab299: String? = null

    @Column(name = "BAB305")
    var bab305: String? = null

    @Column(name = "CAB299")
    var cab299: String? = null

    @Column(name = "CAB305")
    var cab305: String? = null

    @Column(name = "AAE005")
    var aae005: String? = null

    @Column(name = "AAB301")
    var aab301: String? = null

    @Column(name = "AAC010")
    var aac010: String? = null

    @Column(name = "AAE006")
    var aae006: String? = null

    @Column(name = "AAE007")
    var aae007: String? = null

    @Column(name = "AAC007")
    var aac007: LocalDate? = null

    @Column(name = "AAC015")
    var aac015: String? = null

    @Column(name = "AAC184")
    var aac184: LocalDate? = null

    @Column(name = "ACA111")
    var aca111: String? = null

    @Column(name = "ACA112")
    var aca112: String? = null

    @Column(name = "AAC014")
    var aac014: String? = null

    @Column(name = "BAC014")
    var bac014: String? = null

    @Column(name = "AAC185")
    var aac185: LocalDate? = null

    @Column(name = "AAC025")
    var aac025: String? = null

    @Column(name = "AAE008")
    var aae008: Long? = null

    @Column(name = "ATC90R")
    var atc90r: String? = null

    @Column(name = "AAE009")
    var aae009: String? = null

    @Column(name = "AAE00A")
    var aae00a: String? = null

    @Column(name = "AAE00B")
    var aae00b: String? = null

    @Column(name = "AAE00C")
    var aae00c: String? = null

    @Column(name = "AAE00D")
    var aae00d: String? = null

    @Column(name = "AAE00E")
    var aae00e: String? = null

    @Column(name = "AAE00F")
    var aae00f: String? = null

    @Column(name = "AAE00G")
    var aae00g: String? = null

    @Column(name = "AAE00H")
    var aae00h: String? = null

    @Column(name = "AAE00I")
    var aae00i: String? = null

    @Column(name = "AAE00J")
    var aae00j: String? = null

    @Column(name = "AAE00K")
    var aae00k: String? = null

    @Column(name = "ACC029")
    var acc029: String? = null

    @Column(name = "ACC030")
    var acc030: String? = null

    @Column(name = "AAE00M")
    var aae00m: String? = null

    @Column(name = "ACC0M1")
    var acc0m1: String? = null

    @Column(name = "AAE00N")
    var aae00n: String? = null

    @Column(name = "AAC013")
    var aac013: String? = null

    @Column(name = "AAE00P")
    var aae00p: String? = null

    @Column(name = "AAB001")
    var aab001: Long? = null

    @Column(name = "ID")
    var id: String? = null

    @Column(name = "AAB004")
    var aab004: String? = null

    @Column(name = "AAB001_1")
    var aab001_1: Long? = null

    @Column(name = "ID_1")
    var id_1: String? = null

    @Column(name = "AAB004_1")
    var aab004_1: String? = null

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

    @Column(name = "AAE00R")
    var aae00r: String? = null

    @Column(name = "BBB001")
    var bbb001: String? = null

    @Column(name = "BBB002")
    var bbb002: String? = null

}
