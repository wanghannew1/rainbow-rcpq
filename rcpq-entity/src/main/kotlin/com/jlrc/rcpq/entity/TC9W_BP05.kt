package com.jlrc.rcpq.entity

import jakarta.persistence.*
import java.io.Serializable
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "TC9W_BP05")
@IdClass(TC9W_BP05Id::class)
open class TC9W_BP05 {
    @Id
    @Column(name = "AAE002")
    var aae002: Long? = null

    @Id
    @Column(name = "ATC900")
    var atc900: Long? = null

    @Id
    @Column(name = "ABP001")
    var abp001: String? = null

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

    @Column(name = "AAC002")
    var aac002: String? = null

    @Column(name = "AAC003")
    var aac003: String? = null

    @Column(name = "AAC009")
    var aac009: String? = null

    @Column(name = "ABP002")
    var abp002: String? = null

    @Column(name = "ABP001_1")
    var abp001_1: String? = null

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

    @Column(name = "ABP00A")
    var abp00a: String? = null

    @Column(name = "ABP00B")
    var abp00b: String? = null

    @Column(name = "ABP00C")
    var abp00c: String? = null

    @Column(name = "ABP00D")
    var abp00d: String? = null

}
data class TC9W_BP05Id(
    val aae002: Long? = null,
    val atc900: Long? = null,
    val abp001: String? = null,
) : Serializable
