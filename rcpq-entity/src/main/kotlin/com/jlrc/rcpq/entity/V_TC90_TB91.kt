package com.jlrc.rcpq.entity

import jakarta.persistence.*
import java.io.Serializable
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "V_TC90_TB91")
open class V_TC90_TB91 {
    @Id
    @Column(name = "ATB910")
    var atb910: Long? = null

    @Column(name = "ATC900")
    var atc900: Long? = null

    @Column(name = "AAC002")
    var aac002: String? = null

    @Column(name = "AAC003")
    var aac003: String? = null

    @Column(name = "AAE00A")
    var aae00a: String? = null

    @Column(name = "AAC012")
    var aac012: String? = null

    @Column(name = "ATC90L")
    var atc90l: String? = null

    @Column(name = "BAZ001")
    var baz001: Long? = null

    @Column(name = "BAZ002")
    var baz002: Long? = null

    @Column(name = "ATB91Z")
    var atb91z: Long? = null

    @Column(name = "ATB911")
    var atb911: String? = null

    @Column(name = "ATB912")
    var atb912: String? = null

    @Column(name = "ATB913")
    var atb913: String? = null

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

    @Column(name = "BAE011")
    var bae011: String? = null

    @Column(name = "BAE017")
    var bae017: String? = null

    @Column(name = "BAE036")
    var bae036: LocalDate? = null

    @Column(name = "BAE019")
    var bae019: String? = null

    @Column(name = "BAE020")
    var bae020: String? = null

    @Column(name = "BAE022")
    var bae022: String? = null

    @Column(name = "AAE013")
    var aae013: String? = null

    @Column(name = "ATB900")
    var atb900: String? = null

    @Column(name = "AAB00E")
    var aab00e: String? = null

    @Column(name = "AAB004")
    var aab004: String? = null

    @Column(name = "ATB931")
    var atb931: String? = null

    @Column(name = "AAC004")
    var aac004: String? = null

    @Column(name = "ATC904")
    var atc904: Long? = null

    @Column(name = "ATC9W2")
    var atc9w2: Long? = null

    @Column(name = "ATC9WC")
    var atc9wc: String? = null

    @Column(name = "ATC909")
    var atc909: String? = null

    @Column(name = "AAC009")
    var aac009: String? = null

    @Column(name = "ATC902")
    var atc902: String? = null

    @Column(name = "AAE008")
    var aae008: Long? = null

    @Column(name = "AAE005")
    var aae005: String? = null

    @Column(name = "AAE00D")
    var aae00d: String? = null

    @Column(name = "ATC90G")
    var atc90g: String? = null

    @Column(name = "AAA030")
    var aaa030: Long? = null

    @Column(name = "AAA031")
    var aaa031: String? = null

    @Column(name = "ATC908")
    var atc908: String? = null

    @Column(name = "ATC90C")
    var atc90c: LocalDate? = null

    @Column(name = "ATC90D")
    var atc90d: LocalDate? = null

    @Column(name = "AAA201")
    var aaa201: Long? = null

    @Column(name = "AAA100")
    var aaa100: String? = null

    @Column(name = "AAA101")
    var aaa101: String? = null

    @Column(name = "ATC905")
    var atc905: String? = null

    @Column(name = "AAA301")
    var aaa301: String? = null

    @Column(name = "AAA501")
    var aaa501: Long? = null

    @Column(name = "AAA120")
    var aaa120: String? = null

    @Column(name = "AAA121")
    var aaa121: String? = null

    @Column(name = "ATB90S")
    var atb90s: String? = null

    @Column(name = "AAA303")
    var aaa303: String? = null

    @Column(name = "AAA601")
    var aaa601: Long? = null

    @Column(name = "AAA110")
    var aaa110: String? = null

    @Column(name = "AAA111")
    var aaa111: String? = null

    @Column(name = "ZYNUM")
    var zynum: String? = null

    @Column(name = "JYNUM")
    var jynum: String? = null

    @Column(name = "AAA302")
    var aaa302: String? = null

}
