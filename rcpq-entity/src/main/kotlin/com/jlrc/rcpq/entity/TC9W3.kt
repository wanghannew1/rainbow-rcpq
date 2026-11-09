package com.jlrc.rcpq.entity

import jakarta.persistence.*
import java.io.Serializable
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "TC9W3")
open class TC9W3 {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_TC9W3_ATC9W0")
    @SequenceGenerator(name = "SEQ_TC9W3_ATC9W0", sequenceName = "SEQ_TC9W3_ATC9W0", allocationSize = 1)
    @Column(name = "ATC9W0")
    var atc9w0: Long? = null

    @Column(name = "BAZ001")
    var baz001: Long? = null

    @Column(name = "BAZ002")
    var baz002: Long? = null

    @Column(name = "AAE002")
    var aae002: Long? = null

    @Column(name = "ATC900")
    var atc900: Long? = null

    @Column(name = "AAA100")
    var aaa100: String? = null

    @Column(name = "AAA101")
    var aaa101: String? = null

    @Column(name = "AAC001")
    var aac001: String? = null

    @Column(name = "ATB900")
    var atb900: Long? = null

    @Column(name = "AAB001")
    var aab001: Long? = null

    @Column(name = "AAB004")
    var aab004: String? = null

    @Column(name = "ATB90X")
    var atb90x: String? = null

    @Column(name = "ATC9W1")
    var atc9w1: String? = null

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

    @Column(name = "ATC9W8")
    var atc9w8: String? = null

    @Column(name = "ATC9W2")
    var atc9w2: String? = null

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

    @Column(name = "AAA005")
    var aaa005: String? = null

    @Column(name = "ABP014")
    var abp014: String? = null

    @Column(name = "ABP015")
    var abp015: String? = null

    @Column(name = "AAA90Y")
    var aaa90y: String? = null

    @Column(name = "ATC904")
    var atc904: Long? = null

    @Column(name = "AAA302_1")
    var aaa302_1: BigDecimal? = null

    @Column(name = "AAA302_2")
    var aaa302_2: BigDecimal? = null

    @Column(name = "ATC93BD")
    var atc93bd: BigDecimal? = null

    @Column(name = "AAE003")
    var aae003: Long? = null

    @Column(name = "ABPZZZ")
    var abpzzz: String? = null

    @Column(name = "ATC9W0_1")
    var atc9w0_1: Long? = null

    @Column(name = "ZYNUM")
    var zynum: String? = null

    @Column(name = "JYNUM")
    var jynum: String? = null

}
