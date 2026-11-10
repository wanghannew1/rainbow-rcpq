package com.jlrc.rcpq.entity

import jakarta.persistence.*
import java.io.Serializable
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "TC30_BP01")
open class TC30_BP01 {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_TC30_BP01_ID")
    @SequenceGenerator(name = "SEQ_TC30_BP01_ID", sequenceName = "SEQ_TC30_BP01_ID", allocationSize = 1)
    @Column(name = "ID")
    var id: Long? = null

    @Column(name = "BAZ001")
    var baz001: Long? = null

    @Column(name = "BAZ002")
    var baz002: Long? = null

    @Column(name = "AAE002")
    var aae002: Long? = null

    @Column(name = "ATC900")
    var atc900: Long? = null

    @Column(name = "ATB90X")
    var atb90x: String? = null

    @Column(name = "ATC30Z")
    var atc30z: String? = null

    @Column(name = "AAA030")
    var aaa030: Long? = null

    @Column(name = "AAA031")
    var aaa031: String? = null

    @Column(name = "ABP001")
    var abp001: String? = null

    @Column(name = "AAC003")
    var aac003: String? = null

    @Column(name = "ABP002")
    var abp002: String? = null

    @Column(name = "AAC002")
    var aac002: String? = null

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

    @Column(name = "ABP015")
    var abp015: String? = null

    @Column(name = "ABP016")
    var abp016: String? = null

    @Column(name = "ABP017")
    var abp017: String? = null

    @Column(name = "ABP018")
    var abp018: String? = null

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

    @Column(name = "ABP999")
    var abp999: String? = null

    @Column(name = "AAC001")
    var aac001: BigDecimal? = null
}
