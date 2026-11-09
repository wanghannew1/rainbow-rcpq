package com.jlrc.rcpq.entity

import jakarta.persistence.*
import java.io.Serializable
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "V_TJ02")
@IdClass(V_TJ02Id::class)
open class V_TJ02 {
    @Id
    @Column(name = "ATC930")
    var atc930: Long? = null

    @Id
    @Column(name = "ATB930")
    var atb930: Long? = null

    @Id
    @Column(name = "ATC932")
    var atc932: Long? = null

    @Id
    @Column(name = "ATC937")
    var atc937: String? = null

    @Id
    @Column(name = "AAE002")
    var aae002: Long? = null

    @Id
    @Column(name = "TJ02LB")
    var tj02lb: String? = null

    @Column(name = "ATB931")
    var atb931: String? = null

    @Column(name = "ATC931")
    var atc931: Long? = null

    @Column(name = "AAC001")
    var aac001: Long? = null

    @Column(name = "AAC003")
    var aac003: String? = null

    @Column(name = "AAC002")
    var aac002: String? = null

    @Column(name = "ATC933")
    var atc933: BigDecimal? = null

    @Column(name = "ATJ021")
    var atj021: String? = null

    @Column(name = "ATJ022")
    var atj022: BigDecimal? = null

    @Column(name = "AAA301")
    var aaa301: BigDecimal? = null

    @Column(name = "BAA001")
    var baa001: BigDecimal? = null

    @Column(name = "BBA001")
    var bba001: BigDecimal? = null

    @Column(name = "BAA003")
    var baa003: BigDecimal? = null

    @Column(name = "BBA003")
    var bba003: BigDecimal? = null

    @Column(name = "BAA002")
    var baa002: BigDecimal? = null

    @Column(name = "BBA002")
    var bba002: BigDecimal? = null

    @Column(name = "BBA005")
    var bba005: BigDecimal? = null

    @Column(name = "BBA004")
    var bba004: BigDecimal? = null

    @Column(name = "AAA303")
    var aaa303: BigDecimal? = null

    @Column(name = "CAA002")
    var caa002: BigDecimal? = null

    @Column(name = "CAA001")
    var caa001: BigDecimal? = null

    @Column(name = "ATJ023")
    var atj023: BigDecimal? = null

    @Column(name = "ATJ024")
    var atj024: BigDecimal? = null

    @Column(name = "ATJ025")
    var atj025: BigDecimal? = null

    @Column(name = "ATJ026")
    var atj026: BigDecimal? = null

    @Column(name = "ATJ027")
    var atj027: BigDecimal? = null

    @Column(name = "ATC93S")
    var atc93s: BigDecimal? = null

    @Column(name = "ATB90Z")
    var atb90z: BigDecimal? = null

    @Column(name = "ATJ028")
    var atj028: BigDecimal? = null

    @Column(name = "ATJ029")
    var atj029: BigDecimal? = null

    @Column(name = "AAB004")
    var aab004: String? = null

    @Column(name = "ATB90K")
    var atb90k: String? = null

    @Column(name = "ATB90L")
    var atb90l: String? = null

    @Column(name = "ATB90M")
    var atb90m: String? = null

    @Column(name = "ATB90N")
    var atb90n: String? = null

    @Column(name = "ATB90O")
    var atb90o: String? = null

    @Column(name = "ATB90S")
    var atb90s: String? = null

    @Column(name = "ATB90K_")
    var atb90k_: String? = null

    @Column(name = "ATB90L_")
    var atb90l_: String? = null

    @Column(name = "ATB90M_")
    var atb90m_: String? = null

    @Column(name = "ATB90S_")
    var atb90s_: String? = null

    @Column(name = "ATJ02A")
    var atj02a: String? = null

    @Column(name = "ATJ02B")
    var atj02b: String? = null

    @Column(name = "ATJ02C")
    var atj02c: String? = null

    @Column(name = "ATJ02D")
    var atj02d: String? = null

    @Column(name = "ISLOCK")
    var islock: String? = null

    @Column(name = "ATJ02E")
    var atj02e: String? = null

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

    @Column(name = "AAE217")
    var aae217: String? = null

    @Column(name = "AAE236")
    var aae236: LocalDate? = null

    @Column(name = "AAE219")
    var aae219: String? = null

    @Column(name = "AAE220")
    var aae220: String? = null

    @Column(name = "AAE222")
    var aae222: String? = null

    @Column(name = "BBA000")
    var bba000: BigDecimal? = null

    @Column(name = "BAA000")
    var baa000: BigDecimal? = null

    @Column(name = "BBA100")
    var bba100: BigDecimal? = null

    @Column(name = "BAA100")
    var baa100: BigDecimal? = null

    @Column(name = "CAA101")
    var caa101: BigDecimal? = null

    @Column(name = "CAA102")
    var caa102: BigDecimal? = null

    @Column(name = "ATC9P5")
    var atc9p5: BigDecimal? = null

    @Column(name = "TYPE")
    var type: String? = null

    @Column(name = "AAE00A")
    var aae00a: String? = null

    @Column(name = "AAE013")
    var aae013: String? = null

    @Column(name = "AAB00E")
    var aab00e: String? = null

}
data class V_TJ02Id(
    val atc930: Long? = null,
    val atb930: Long? = null,
    val atc932: Long? = null,
    val atc937: String? = null,
    val aae002: Long? = null,
    val tj02lb: String? = null,
) : Serializable
