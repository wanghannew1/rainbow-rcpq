package com.jlrc.rcpq.entity

import jakarta.persistence.*
import java.io.Serializable
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "V_TB98")
open class V_TB98 {
    @Id
    @Column(name = "ATB980")
    var atb980: Long? = null

    @Column(name = "TKEY")
    var tkey: Long? = null

    @Column(name = "ATB930")
    var atb930: Long? = null

    @Column(name = "ATB931")
    var atb931: String? = null

    @Column(name = "AAB001")
    var aab001: Long? = null

    @Column(name = "AAB004")
    var aab004: String? = null

    @Column(name = "AAA030")
    var aaa030: Long? = null

    @Column(name = "AAA031")
    var aaa031: String? = null

    @Column(name = "ATC937")
    var atc937: String? = null

    @Column(name = "AAE002")
    var aae002: Long? = null

    @Column(name = "ATB961")
    var atb961: Long? = null

    @Column(name = "ATB962")
    var atb962: Long? = null

    @Column(name = "ATB963")
    var atb963: Long? = null

    @Column(name = "ATB964")
    var atb964: Long? = null

    @Column(name = "ATB965")
    var atb965: Long? = null

    @Column(name = "ATB96Z")
    var atb96z: String? = null

    @Column(name = "AAE013")
    var aae013: String? = null

    @Column(name = "TB96_AAE036")
    var tb96_aae036: LocalDate? = null

    @Column(name = "TB96_AAE019")
    var tb96_aae019: String? = null

    @Column(name = "TB96_AAE020")
    var tb96_aae020: String? = null

    @Column(name = "ATB981")
    var atb981: BigDecimal? = null

    @Column(name = "ATB982")
    var atb982: String? = null

    @Column(name = "DSTATUS")
    var dstatus: String? = null

    @Column(name = "TB98_AAE013")
    var tb98_aae013: String? = null

    @Column(name = "TB98_AAE036")
    var tb98_aae036: LocalDate? = null

    @Column(name = "TB98_AAE019")
    var tb98_aae019: String? = null

    @Column(name = "TB98_AAE020")
    var tb98_aae020: String? = null

    @Column(name = "ZZZ011")
    var zzz011: String? = null

    @Column(name = "AAE114")
    var aae114: String? = null

    @Column(name = "AAE119")
    var aae119: String? = null

    @Column(name = "AAE136")
    var aae136: LocalDate? = null

    @Column(name = "AAE120")
    var aae120: String? = null

}
