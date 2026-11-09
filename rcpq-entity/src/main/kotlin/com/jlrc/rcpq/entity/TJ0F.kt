package com.jlrc.rcpq.entity

import jakarta.persistence.*
import java.io.Serializable
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "TJ0F")
@IdClass(TJ0FId::class)
open class TJ0F {
    @Id
    @Column(name = "AAE002")
    var aae002: Long? = null

    @Id
    @Column(name = "ATB930")
    var atb930: Long? = null

    @Id
    @Column(name = "AAA020")
    var aaa020: String? = null

    @Id
    @Column(name = "ATJ000")
    var atj000: String? = null

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

    @Column(name = "AAA021")
    var aaa021: String? = null

    @Column(name = "ATJ0F2")
    var atj0f2: Long? = null

    @Column(name = "ATB90L")
    var atb90l: String? = null

    @Column(name = "ATB90N")
    var atb90n: String? = null

    @Column(name = "ATB90O")
    var atb90o: String? = null

    @Column(name = "ATB90Q")
    var atb90q: String? = null

    @Column(name = "AAA399")
    var aaa399: String? = null

    @Column(name = "BAA002")
    var baa002: BigDecimal? = null

    @Column(name = "BBA002")
    var bba002: BigDecimal? = null

    @Column(name = "ATB90T")
    var atb90t: BigDecimal? = null

    @Column(name = "BBA004")
    var bba004: BigDecimal? = null

    @Column(name = "BBA005")
    var bba005: BigDecimal? = null

    @Column(name = "ATJ0F3")
    var atj0f3: BigDecimal? = null

    @Column(name = "DAA001")
    var daa001: BigDecimal? = null

    @Column(name = "ATJ0F5")
    var atj0f5: BigDecimal? = null

    @Column(name = "ATJ0AE")
    var atj0ae: BigDecimal? = null

    @Column(name = "ATJ001")
    var atj001: String? = null

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

    @Column(name = "AAE001")
    var aae001: Long? = null

    @Column(name = "ATJ0F6")
    var atj0f6: BigDecimal? = null

    @Column(name = "ATJ0F2_B1")
    var atj0f2_b1: Long? = null

    @Column(name = "BBA004_B1")
    var bba004_b1: BigDecimal? = null

    @Column(name = "ATJ0F6_B1")
    var atj0f6_b1: BigDecimal? = null

    @Column(name = "ATJ0F5_B1")
    var atj0f5_b1: BigDecimal? = null

    @Column(name = "ATJ0F2_B2")
    var atj0f2_b2: Long? = null

    @Column(name = "BBA004_B2")
    var bba004_b2: BigDecimal? = null

    @Column(name = "ATJ0F5_B2")
    var atj0f5_b2: BigDecimal? = null

    @Column(name = "ATJ0F5_Z")
    var atj0f5_z: BigDecimal? = null

}
data class TJ0FId(
    val aae002: Long? = null,
    val atb930: Long? = null,
    val aaa020: String? = null,
    val atj000: String? = null,
) : Serializable
