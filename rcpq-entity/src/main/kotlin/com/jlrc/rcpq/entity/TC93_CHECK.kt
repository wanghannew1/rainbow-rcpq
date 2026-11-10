package com.jlrc.rcpq.entity

import jakarta.persistence.*
import java.io.Serializable
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "TC93_CHECK")
open class TC93_CHECK {
    @Id
    @Column(name = "CHECKID")
    var checkid: Long? = null

    @Column(name = "ATB900")
    var atb900: Long? = null

    @Column(name = "ATC900")
    var atc900: Long? = null

    @Column(name = "AAB001")
    var aab001: Long? = null

    @Column(name = "AAB004")
    var aab004: String? = null

    @Column(name = "ATB930")
    var atb930: Long? = null

    @Column(name = "ATB931")
    var atb931: String? = null

    @Column(name = "ATC931")
    var atc931: Long? = null

    @Column(name = "ATC937")
    var atc937: String? = null

    @Column(name = "AAC001")
    var aac001: Long? = null

    @Column(name = "AAC003")
    var aac003: String? = null

    @Column(name = "AAC002")
    var aac002: String? = null

    @Column(name = "AAC004")
    var aac004: String? = null

    @Column(name = "AAC006")
    var aac006: LocalDate? = null

    @Column(name = "AAB004_1")
    var aab004_1: String? = null

    @Column(name = "AAE00B")
    var aae00b: String? = null

    @Column(name = "ATC908")
    var atc908: String? = null

    @Column(name = "AAE00P")
    var aae00p: String? = null

    @Column(name = "ATC905")
    var atc905: BigDecimal? = null

    @Column(name = "ATB90R")
    var atb90r: String? = null

    @Column(name = "ATC90A")
    var atc90a: String? = null

    @Column(name = "ATC90B")
    var atc90b: LocalDate? = null

    @Column(name = "ATC90C")
    var atc90c: LocalDate? = null

    @Column(name = "ATC90D")
    var atc90d: LocalDate? = null

    @Column(name = "ATC90I")
    var atc90i: LocalDate? = null

    @Column(name = "ATC90AV")
    var atc90av: Long? = null

    @Column(name = "AAA401")
    var aaa401: String? = null

    @Column(name = "ATC90P")
    var atc90p: String? = null

    @Column(name = "AAA001")
    var aaa001: String? = null

    @Column(name = "AAA301")
    var aaa301: BigDecimal? = null

    @Column(name = "AAA201")
    var aaa201: Long? = null

    @Column(name = "AAA202")
    var aaa202: Long? = null

    @Column(name = "AAA100")
    var aaa100: String? = null

    @Column(name = "AAA101")
    var aaa101: String? = null

    @Column(name = "AAA402")
    var aaa402: String? = null

    @Column(name = "ATC90PA")
    var atc90pa: String? = null

    @Column(name = "AAA005")
    var aaa005: String? = null

    @Column(name = "AAA302")
    var aaa302: BigDecimal? = null

    @Column(name = "AAA601")
    var aaa601: Long? = null

    @Column(name = "AAA602")
    var aaa602: Long? = null

    @Column(name = "AAA110")
    var aaa110: String? = null

    @Column(name = "AAA111")
    var aaa111: String? = null

    @Column(name = "AAA407")
    var aaa407: String? = null

    @Column(name = "ATC90W")
    var atc90w: String? = null

    @Column(name = "AAA004")
    var aaa004: String? = null

    @Column(name = "AAA303")
    var aaa303: BigDecimal? = null

    @Column(name = "ATB90S")
    var atb90s: String? = null

    @Column(name = "AAA501")
    var aaa501: Long? = null

    @Column(name = "AAA502")
    var aaa502: Long? = null

    @Column(name = "AAA120")
    var aaa120: String? = null

    @Column(name = "AAA121")
    var aaa121: String? = null

    @Column(name = "BBB001")
    var bbb001: String? = null

    @Column(name = "BBB002")
    var bbb002: String? = null

    @Column(name = "ATB90C")
    var atb90c: String? = null

    @Column(name = "ATB90A")
    var atb90a: BigDecimal? = null

    @Column(name = "ATB90B")
    var atb90b: BigDecimal? = null

    @Column(name = "AAA803")
    var aaa803: String? = null

    @Column(name = "AAE100")
    var aae100: String? = null

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

    @Column(name = "AAE211")
    var aae211: String? = null
    @Column(name = "AAE214")
    var aae214: String? = null
    @Column(name = "AAE217")
    var aae217: String? = null
    @Column(name = "AAE219")
    var aae219: String? = null
    @Column(name = "AAE220")
    var aae220: String? = null
    @Column(name = "AAE222")
    var aae222: String? = null
    @Column(name = "AAE236")
    var aae236: LocalDateTime? = null
}
