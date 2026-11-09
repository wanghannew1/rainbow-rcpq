package com.jlrc.rcpq.entity

import jakarta.persistence.*
import java.io.Serializable
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "TJ0A")
@IdClass(TJ0AId::class)
open class TJ0A {
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

    @Column(name = "ATJ0A2")
    var atj0a2: Long? = null

    @Column(name = "ATB90K")
    var atb90k: String? = null

    @Column(name = "ATB90M")
    var atb90m: String? = null

    @Column(name = "ATB90P")
    var atb90p: String? = null

    @Column(name = "ATB90R")
    var atb90r: String? = null

    @Column(name = "BBA001")
    var bba001: BigDecimal? = null

    @Column(name = "BAA001")
    var baa001: BigDecimal? = null

    @Column(name = "ATJ0A3")
    var atj0a3: BigDecimal? = null

    @Column(name = "BBA003")
    var bba003: BigDecimal? = null

    @Column(name = "BAA003")
    var baa003: BigDecimal? = null

    @Column(name = "ATJ0A4")
    var atj0a4: BigDecimal? = null

    @Column(name = "ATJ0A5")
    var atj0a5: BigDecimal? = null

    @Column(name = "ATJ0A6")
    var atj0a6: BigDecimal? = null

    @Column(name = "ATJ0A7")
    var atj0a7: BigDecimal? = null

    @Column(name = "ATJ0A2_B")
    var atj0a2_b: Long? = null

    @Column(name = "BBA001_B")
    var bba001_b: BigDecimal? = null

    @Column(name = "BAA001_B")
    var baa001_b: BigDecimal? = null

    @Column(name = "ATJ0A3_B")
    var atj0a3_b: BigDecimal? = null

    @Column(name = "BBA003_B")
    var bba003_b: BigDecimal? = null

    @Column(name = "BAA003_B")
    var baa003_b: BigDecimal? = null

    @Column(name = "ATJ0A4_B")
    var atj0a4_b: BigDecimal? = null

    @Column(name = "ATJ0A7_B")
    var atj0a7_b: BigDecimal? = null

    @Column(name = "ATJ0A3_Z")
    var atj0a3_z: BigDecimal? = null

    @Column(name = "ATJ0A4_Z")
    var atj0a4_z: BigDecimal? = null

    @Column(name = "ATJ0A7_Z")
    var atj0a7_z: BigDecimal? = null

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

}
data class TJ0AId(
    val aae002: Long? = null,
    val atb930: Long? = null,
    val atj000: String? = null,
) : Serializable
