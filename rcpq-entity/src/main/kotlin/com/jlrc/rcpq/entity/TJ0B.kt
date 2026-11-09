package com.jlrc.rcpq.entity

import jakarta.persistence.*
import java.io.Serializable
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "TJ0B")
@IdClass(TJ0BId::class)
open class TJ0B {
    @Id
    @Column(name = "AAE002")
    var aae002: Long? = null

    @Id
    @Column(name = "ATB930")
    var atb930: Long? = null

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

    @Column(name = "AAA020")
    var aaa020: String? = null

    @Column(name = "AAA021")
    var aaa021: String? = null

    @Column(name = "ATJ0B2")
    var atj0b2: Long? = null

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

    @Column(name = "ATJ0B3")
    var atj0b3: BigDecimal? = null

    @Column(name = "DAA001")
    var daa001: BigDecimal? = null

    @Column(name = "ATJ0B5")
    var atj0b5: BigDecimal? = null

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

    @Column(name = "ATJ0B6")
    var atj0b6: BigDecimal? = null

    @Column(name = "ATJ0B2_B")
    var atj0b2_b: Long? = null

    @Column(name = "BAA002_B")
    var baa002_b: BigDecimal? = null

    @Column(name = "BBA002_B")
    var bba002_b: BigDecimal? = null

    @Column(name = "ATB90T_B")
    var atb90t_b: BigDecimal? = null

    @Column(name = "BBA004_B")
    var bba004_b: BigDecimal? = null

    @Column(name = "BBA005_B")
    var bba005_b: BigDecimal? = null

    @Column(name = "DAA001_B")
    var daa001_b: BigDecimal? = null

    @Column(name = "ATJ0B6_B")
    var atj0b6_b: BigDecimal? = null

    @Column(name = "ATJ0B5_B")
    var atj0b5_b: BigDecimal? = null

    @Column(name = "ATJ0B5_Z")
    var atj0b5_z: BigDecimal? = null

    @Column(name = "ATJ0B3_B")
    var atj0b3_b: BigDecimal? = null

}
data class TJ0BId(
    val aae002: Long? = null,
    val atb930: Long? = null,
    val atj000: String? = null,
) : Serializable
