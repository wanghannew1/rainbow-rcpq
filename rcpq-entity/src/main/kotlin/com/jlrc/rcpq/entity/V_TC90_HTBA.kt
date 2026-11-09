package com.jlrc.rcpq.entity

import jakarta.persistence.*
import java.io.Serializable
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "V_TC90_HTBA")
open class V_TC90_HTBA {
    @Id
    @Column(name = "ATC900")
    var atc900: Long? = null

    @Column(name = "AAA030")
    var aaa030: Long? = null

    @Column(name = "AAA031")
    var aaa031: String? = null

    @Column(name = "GTZT")
    var gtzt: String? = null

    @Column(name = "ATB90RA")
    var atb90ra: String? = null

    @Column(name = "AAA701")
    var aaa701: Long? = null

    @Column(name = "AAA702")
    var aaa702: Long? = null

    @Column(name = "AAC001")
    var aac001: Long? = null

    @Column(name = "AAC002")
    var aac002: String? = null

    @Column(name = "AAC003")
    var aac003: String? = null

    @Column(name = "AAC005")
    var aac005: String? = null

    @Column(name = "AAC011")
    var aac011: String? = null

    @Column(name = "BAB305")
    var bab305: String? = null

    @Column(name = "AAE008")
    var aae008: String? = null

    @Column(name = "AAE006")
    var aae006: String? = null

    @Column(name = "AAC009")
    var aac009: String? = null

    @Column(name = "ATC908")
    var atc908: String? = null

    @Column(name = "IFZG")
    var ifzg: String? = null

    @Column(name = "IFNMG")
    var ifnmg: String? = null

    @Column(name = "AAC007")
    var aac007: String? = null

    @Column(name = "ATC90B")
    var atc90b: String? = null

    @Column(name = "IFQDHT")
    var ifqdht: String? = null

    @Column(name = "ATC90A")
    var atc90a: String? = null

    @Column(name = "HTQX")
    var htqx: String? = null

    @Column(name = "HTKSRQ")
    var htksrq: LocalDate? = null

    @Column(name = "HTJSRQ")
    var htjsrq: LocalDate? = null

    @Column(name = "ZGLB")
    var zglb: String? = null

    @Column(name = "YGXS")
    var ygxs: String? = null

    @Column(name = "YGKSRQ")
    var ygksrq: LocalDate? = null

    @Column(name = "YGJSRQ")
    var ygjsrq: LocalDate? = null

    @Column(name = "SYKSRQ")
    var syksrq: String? = null

    @Column(name = "SYJSRQ")
    var syjsrq: String? = null

    @Column(name = "HTXQRQ")
    var htxqrq: String? = null

    @Column(name = "IFYANGL")
    var ifyangl: String? = null

    @Column(name = "IFSHIY")
    var ifshiy: String? = null

    @Column(name = "IFGONGS")
    var ifgongs: String? = null

    @Column(name = "IFYIL")
    var ifyil: String? = null

    @Column(name = "IFSHENGY")
    var ifshengy: String? = null

    @Column(name = "AAE005")
    var aae005: String? = null

    @Column(name = "HTMM")
    var htmm: String? = null

    @Column(name = "GZDD")
    var gzdd: String? = null

    @Column(name = "GZBZ")
    var gzbz: String? = null

    @Column(name = "ATB90AB")
    var atb90ab: Long? = null

    @Column(name = "YX")
    var yx: String? = null

    @Column(name = "YXFP")
    var yxfp: String? = null

    @Column(name = "GSZD")
    var gszd: String? = null

    @Column(name = "GZNR")
    var gznr: String? = null

    @Column(name = "DGSHF")
    var dgshf: String? = null

    @Column(name = "ZXFS")
    var zxfs: String? = null

    @Column(name = "LDBC")
    var ldbc: String? = null

    @Column(name = "TGLDTJ")
    var tgldtj: String? = null

    @Column(name = "ATB906")
    var atb906: LocalDate? = null

    @Column(name = "ATB907")
    var atb907: LocalDate? = null

    @Column(name = "AAB004")
    var aab004: String? = null

    @Column(name = "BJZF")
    var bjzf: String? = null

    @Column(name = "SYQGZ")
    var syqgz: String? = null

    @Column(name = "QTFLDY")
    var qtfldy: String? = null

    @Column(name = "GZYD")
    var gzyd: String? = null

    @Column(name = "FWQKSRQ")
    var fwqksrq: String? = null

    @Column(name = "FWQJSRQ")
    var fwqjsrq: String? = null

    @Column(name = "WYJ")
    var wyj: String? = null

    @Column(name = "JYXZKSRQ")
    var jyxzksrq: String? = null

    @Column(name = "JYXZJSRQ")
    var jyxzjsrq: String? = null

    @Column(name = "JYXZFW")
    var jyxzfw: String? = null

    @Column(name = "JYXZBC")
    var jyxzbc: String? = null

    @Column(name = "WFBMXY")
    var wfbmxy: String? = null

    @Column(name = "YDQT")
    var ydqt: String? = null

    @Column(name = "FJXY1")
    var fjxy1: String? = null

    @Column(name = "FJXY2")
    var fjxy2: String? = null

    @Column(name = "FJXY3")
    var fjxy3: String? = null

    @Column(name = "ATC90Q")
    var atc90q: String? = null

    @Column(name = "ATC90K")
    var atc90k: String? = null

    @Column(name = "SBYY")
    var sbyy: String? = null

    @Column(name = "ATB90R")
    var atb90r: String? = null

    @Column(name = "ATC9WC")
    var atc9wc: String? = null

    @Column(name = "AAA110")
    var aaa110: String? = null

    @Column(name = "AAA111")
    var aaa111: String? = null

    @Column(name = "AAE00P")
    var aae00p: String? = null

    @Column(name = "ATC90P")
    var atc90p: String? = null

    @Column(name = "ATC90PA")
    var atc90pa: String? = null

    @Column(name = "ATC90W")
    var atc90w: String? = null

    @Column(name = "AAA100")
    var aaa100: String? = null

    @Column(name = "AAA101")
    var aaa101: String? = null

}
