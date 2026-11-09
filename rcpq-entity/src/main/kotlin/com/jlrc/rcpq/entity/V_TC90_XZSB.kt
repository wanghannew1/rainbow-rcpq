package com.jlrc.rcpq.entity

import jakarta.persistence.*
import java.io.Serializable
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "V_TC90_XZSB")
open class V_TC90_XZSB {
    @Id
    @Column(name = "ATC900")
    var atc900: Long? = null

    @Column(name = "AAE002")
    var aae002: Long? = null

    @Column(name = "AAE003")
    var aae003: Long? = null

    @Column(name = "AAB004")
    var aab004: String? = null

    @Column(name = "AAC003")
    var aac003: String? = null

    @Column(name = "AAC002")
    var aac002: String? = null

    @Column(name = "AAC004")
    var aac004: String? = null

    @Column(name = "AAC008")
    var aac008: String? = null

    @Column(name = "AAA001")
    var aaa001: String? = null

    @Column(name = "ATC90P")
    var atc90p: String? = null

    @Column(name = "AAA201")
    var aaa201: Long? = null

    @Column(name = "AAA202")
    var aaa202: Long? = null

    @Column(name = "AAA100")
    var aaa100: String? = null

    @Column(name = "AAA101")
    var aaa101: String? = null

    @Column(name = "AAA005")
    var aaa005: String? = null

    @Column(name = "ATC90PA")
    var atc90pa: String? = null

    @Column(name = "AAA601")
    var aaa601: Long? = null

    @Column(name = "AAA602")
    var aaa602: Long? = null

    @Column(name = "AAA110")
    var aaa110: String? = null

    @Column(name = "AAA111")
    var aaa111: String? = null

    @Column(name = "AAA004")
    var aaa004: String? = null

    @Column(name = "ATC90W")
    var atc90w: String? = null

    @Column(name = "AAA501")
    var aaa501: Long? = null

    @Column(name = "AAA502")
    var aaa502: Long? = null

    @Column(name = "AAA120")
    var aaa120: String? = null

    @Column(name = "AAA121")
    var aaa121: String? = null

    @Column(name = "AAA301")
    var aaa301: BigDecimal? = null

    @Column(name = "AAA302")
    var aaa302: BigDecimal? = null

    @Column(name = "AAA303")
    var aaa303: BigDecimal? = null

    @Column(name = "AAA304")
    var aaa304: BigDecimal? = null

    @Column(name = "AAA305")
    var aaa305: BigDecimal? = null

    @Column(name = "AAA306")
    var aaa306: BigDecimal? = null

    @Column(name = "ATB90S")
    var atb90s: String? = null

    @Column(name = "AAE036")
    var aae036: LocalDateTime? = null

    @Column(name = "AAE019")
    var aae019: String? = null

    @Column(name = "AAE020")
    var aae020: String? = null

    @Column(name = "AAE013_1")
    var aae013_1: String? = null

}
