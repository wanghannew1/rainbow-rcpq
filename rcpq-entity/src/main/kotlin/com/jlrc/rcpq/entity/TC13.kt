package com.jlrc.rcpq.entity

import jakarta.persistence.*
import java.io.Serializable
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "TC13")
open class TC13 {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_TC13_ATC130")
    @SequenceGenerator(name = "SEQ_TC13_ATC130", sequenceName = "SEQ_TC13_ATC130", allocationSize = 1)
    @Column(name = "ATC130")
    var atc130: Long? = null

    @Column(name = "BAZ001")
    var baz001: Long? = null

    @Column(name = "BAZ002")
    var baz002: Long? = null

    @Column(name = "AAC001")
    var aac001: Long? = null

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

    @Column(name = "ATC131")
    var atc131: String? = null

    @Column(name = "ATC132")
    var atc132: String? = null

    @Column(name = "AAC011")
    var aac011: String? = null

    @Column(name = "AAC009")
    var aac009: String? = null

    @Column(name = "AAC017")
    var aac017: String? = null

    @Column(name = "ATC109")
    var atc109: String? = null

    @Column(name = "AAC180")
    var aac180: String? = null

    @Column(name = "AAC182")
    var aac182: String? = null

    @Column(name = "AAC183")
    var aac183: String? = null

    @Column(name = "ATC133")
    var atc133: String? = null

    @Column(name = "ATC134")
    var atc134: String? = null

    @Column(name = "AAC0B1")
    var aac0b1: String? = null

    @Column(name = "ATC135")
    var atc135: String? = null

    @Column(name = "AAE005")
    var aae005: String? = null

    @Column(name = "ATC111")
    var atc111: String? = null

    @Column(name = "AAE006")
    var aae006: String? = null

    @Column(name = "AAE007")
    var aae007: String? = null

    @Column(name = "AAB301")
    var aab301: String? = null

    @Column(name = "AAC010")
    var aac010: String? = null

    @Column(name = "AAB299")
    var aab299: String? = null

    @Column(name = "BAB305")
    var bab305: String? = null

    @Column(name = "ATC105")
    var atc105: String? = null

    @Column(name = "ATC136")
    var atc136: String? = null

    @Column(name = "ATC137")
    var atc137: String? = null

    @Column(name = "ATC138")
    var atc138: String? = null

    @Column(name = "ATC139")
    var atc139: String? = null

    @Column(name = "ATB140")
    var atb140: Long? = null

    @Column(name = "ATB141")
    var atb141: String? = null

    @Column(name = "ATC13A")
    var atc13a: String? = null

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

    @Column(name = "ATC13B")
    var atc13b: BigDecimal? = null

    @Column(name = "ATC13C")
    var atc13c: String? = null

    @Column(name = "AAEA13")
    var aaea13: String? = null

    @Column(name = "AAEA11")
    var aaea11: String? = null

    @Column(name = "AAEA17")
    var aaea17: String? = null

    @Column(name = "AAEA36")
    var aaea36: LocalDate? = null

    @Column(name = "AAEA19")
    var aaea19: String? = null

    @Column(name = "AAEA20")
    var aaea20: String? = null

    @Column(name = "AAEA22")
    var aaea22: String? = null

    @Column(name = "ATB151")
    var atb151: LocalDate? = null

    @Column(name = "ATB152")
    var atb152: String? = null

    @Column(name = "ATB153")
    var atb153: String? = null

    @Column(name = "ATB150")
    var atb150: Long? = null

    @Column(name = "ATB120")
    var atb120: Long? = null

    @Column(name = "ATB121")
    var atb121: String? = null

    @Column(name = "ATB122")
    var atb122: LocalDate? = null

    @Column(name = "ATB123")
    var atb123: LocalDate? = null

    @Column(name = "ATC171")
    var atc171: String? = null

    @Column(name = "ATC13D")
    var atc13d: BigDecimal? = null

}
