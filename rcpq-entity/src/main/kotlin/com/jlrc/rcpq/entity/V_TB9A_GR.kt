package com.jlrc.rcpq.entity

import jakarta.persistence.*
import java.io.Serializable
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "V_TB9A_GR")
open class V_TB9A_GR {
    @Id
    @Column(name = "ATB9A0")
    var atb9a0: Long? = null

    @Column(name = "UKEY")
    var ukey: Long? = null

    @Column(name = "ATC930")
    var atc930: Long? = null

    @Column(name = "ATB930")
    var atb930: Long? = null

    @Column(name = "ATB931")
    var atb931: String? = null

    @Column(name = "AAB001")
    var aab001: Long? = null

    @Column(name = "AAB004")
    var aab004: String? = null

    @Column(name = "ATC900")
    var atc900: Long? = null

    @Column(name = "AAC001")
    var aac001: Long? = null

    @Column(name = "AAC002")
    var aac002: String? = null

    @Column(name = "AAC003")
    var aac003: String? = null

    @Column(name = "AAA030")
    var aaa030: Long? = null

    @Column(name = "AAA031")
    var aaa031: String? = null

    @Column(name = "ATC937")
    var atc937: String? = null

    @Column(name = "ATC931")
    var atc931: Long? = null

    @Column(name = "ATC93X3")
    var atc93x3: BigDecimal? = null

    @Column(name = "ATC93E")
    var atc93e: BigDecimal? = null

    @Column(name = "ATC93W")
    var atc93w: String? = null

    @Column(name = "ATB970")
    var atb970: Long? = null

    @Column(name = "ATB971")
    var atb971: BigDecimal? = null

    @Column(name = "ATB974")
    var atb974: LocalDate? = null

    @Column(name = "DZZY")
    var dzzy: String? = null

    @Column(name = "ATB973")
    var atb973: BigDecimal? = null

    @Column(name = "ATB9A1")
    var atb9a1: BigDecimal? = null

    @Column(name = "DSTATUS")
    var dstatus: String? = null

    @Column(name = "AAE011")
    var aae011: String? = null

    @Column(name = "AAE019")
    var aae019: String? = null

    @Column(name = "AAE036")
    var aae036: LocalDateTime? = null

    @Column(name = "AAE020")
    var aae020: String? = null

    @Column(name = "AAEA11")
    var aaea11: String? = null

    @Column(name = "AAEA19")
    var aaea19: String? = null

    @Column(name = "AAEA36")
    var aaea36: LocalDate? = null

    @Column(name = "AAEA20")
    var aaea20: String? = null

    @Column(name = "ZZZ011")
    var zzz011: String? = null

}
