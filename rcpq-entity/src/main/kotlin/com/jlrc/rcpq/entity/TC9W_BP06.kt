package com.jlrc.rcpq.entity

import jakarta.persistence.*
import java.io.Serializable
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "TC9W_BP06")
open class TC9W_BP06 {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_TC9W_BP06_ID")
    @SequenceGenerator(name = "SEQ_TC9W_BP06_ID", sequenceName = "SEQ_TC9W_BP06_ID", allocationSize = 1)
    @Column(name = "ID")
    var id: Long? = null

    @Column(name = "ACE100")
    var ace100: Long? = null

    @Column(name = "AAA005")
    var aaa005: String? = null

    @Column(name = "AAC003")
    var aac003: String? = null

    @Column(name = "AAE002")
    var aae002: String? = null

    @Column(name = "AAE003")
    var aae003: String? = null

    @Column(name = "ABP001")
    var abp001: String? = null

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

    @Column(name = "ACE10A")
    var ace10a: String? = null

    @Column(name = "ACE10B")
    var ace10b: String? = null

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

    @Column(name = "AAA030")
    var aaa030: Long? = null

    @Column(name = "AAA031")
    var aaa031: String? = null

    @Column(name = "AAA020")
    var aaa020: String? = null

    @Column(name = "AAA021")
    var aaa021: String? = null

}
