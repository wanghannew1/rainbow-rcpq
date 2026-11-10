package com.jlrc.rcpq.entity

import jakarta.persistence.*
import java.io.Serializable
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "TC9W_BP02")
open class TC9W_BP02 {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_TC9W_BP02_ATC9U0")
    @SequenceGenerator(name = "SEQ_TC9W_BP02_ATC9U0", sequenceName = "SEQ_TC9W_BP02_ATC9U0", allocationSize = 1)
    @Column(name = "ID")
    var id: Long? = null

    @Column(name = "BAZ001")
    var baz001: Long? = null

    @Column(name = "BAZ002")
    var baz002: Long? = null

    @Column(name = "AAE002")
    var aae002: Long? = null

    @Column(name = "AAA030")
    var aaa030: Long? = null

    @Column(name = "AAA031")
    var aaa031: String? = null

    @Column(name = "AAA020")
    var aaa020: String? = null

    @Column(name = "AAA021")
    var aaa021: String? = null

    @Column(name = "AAA001")
    var aaa001: String? = null

    @Column(name = "AAC003")
    var aac003: String? = null

    @Column(name = "ABP0011")
    var abp0011: String? = null

    @Column(name = "AAC002")
    var aac002: String? = null

    @Column(name = "ABP002")
    var abp002: String? = null

    @Column(name = "ABP003")
    var abp003: String? = null

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

    @Column(name = "ABP010")
    var abp010: String? = null

    @Column(name = "ABP011")
    var abp011: String? = null

    @Column(name = "ABP012")
    var abp012: String? = null

    @Column(name = "ABP013")
    var abp013: String? = null

    @Column(name = "ABP014")
    var abp014: String? = null

    @Column(name = "ACE10A")
    var ace10a: String? = null

    @Column(name = "ACE10B")
    var ace10b: String? = null

    @Column(name = "ATC900")
    var atc900: Long? = null

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

    @Column(name = "AAA303_1")
    var aaa303_1: BigDecimal? = null
    @Column(name = "AAA303_2")
    var aaa303_2: BigDecimal? = null
    @Column(name = "AAA90Z")
    var aaa90z: String? = null
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
    @Column(name = "ABP00Y")
    var abp00y: String? = null
    @Column(name = "ABP00Z")
    var abp00z: String? = null
    @Column(name = "ABP01A")
    var abp01a: String? = null
    @Column(name = "ABP01B")
    var abp01b: String? = null
    @Column(name = "ABP01C")
    var abp01c: String? = null
    @Column(name = "ABP01D")
    var abp01d: String? = null
    @Column(name = "ABP01E")
    var abp01e: String? = null
    @Column(name = "ABP01F")
    var abp01f: String? = null
    @Column(name = "ABP01G")
    var abp01g: String? = null
    @Column(name = "ABP01H")
    var abp01h: String? = null
    @Column(name = "ABP01I")
    var abp01i: String? = null
    @Column(name = "ABP01J")
    var abp01j: String? = null
}
