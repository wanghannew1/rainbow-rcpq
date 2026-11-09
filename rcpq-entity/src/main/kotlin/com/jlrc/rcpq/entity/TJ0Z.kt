package com.jlrc.rcpq.entity

import jakarta.persistence.*
import java.io.Serializable
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "TJ0Z")
@IdClass(TJ0ZId::class)
open class TJ0Z {
    @Id
    @Column(name = "AAE002")
    var aae002: Long? = null

    @Id
    @Column(name = "ATB930")
    var atb930: Long? = null

    @Id
    @Column(name = "TTYPE")
    var ttype: String? = null

    @Column(name = "XH")
    var xh: Long? = null

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

    @Column(name = "AAA020")
    var aaa020: String? = null

    @Column(name = "AAA021")
    var aaa021: String? = null

    @Column(name = "PNUM_Z")
    var pnum_z: Long? = null

    @Column(name = "MNUM_SUM_Z")
    var mnum_sum_z: BigDecimal? = null

    @Column(name = "PNUM_B")
    var pnum_b: Long? = null

    @Column(name = "MNUM_SUM_B")
    var mnum_sum_b: BigDecimal? = null

    @Column(name = "MNUM_SUM")
    var mnum_sum: BigDecimal? = null

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

    @Column(name = "AAEZZZ")
    var aaezzz: String? = null

    @Column(name = "AAE111")
    var aae111: String? = null

    @Column(name = "AAE117")
    var aae117: String? = null

    @Column(name = "AAE136")
    var aae136: LocalDate? = null

    @Column(name = "AAE119")
    var aae119: String? = null

    @Column(name = "AAE120")
    var aae120: String? = null

    @Column(name = "AAE122")
    var aae122: String? = null

    @Column(name = "DFSIGN")
    var dfsign: String? = null

    @Column(name = "ZZZ011")
    var zzz011: String? = null

    @Column(name = "ZZZ019")
    var zzz019: String? = null

    @Column(name = "DFMONEY")
    var dfmoney: BigDecimal? = null

    @Column(name = "DFMSG")
    var dfmsg: String? = null

    @Column(name = "DFTIME")
    var dftime: String? = null

    @Column(name = "AAE311")
    var aae311: String? = null

    @Column(name = "AAE317")
    var aae317: String? = null

    @Column(name = "AAE336")
    var aae336: LocalDate? = null

    @Column(name = "AAE319")
    var aae319: String? = null

    @Column(name = "AAE320")
    var aae320: String? = null

    @Column(name = "AAE322")
    var aae322: String? = null

}
data class TJ0ZId(
    val aae002: Long? = null,
    val atb930: Long? = null,
    val ttype: String? = null,
) : Serializable
