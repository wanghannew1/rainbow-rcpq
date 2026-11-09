package com.jlrc.rcpq.entity

import jakarta.persistence.*
import java.io.Serializable
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "TJ0E")
@IdClass(TJ0EId::class)
open class TJ0E {
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

    @Column(name = "ATJ0E2")
    var atj0e2: Long? = null

    @Column(name = "ATB90S_DW")
    var atb90s_dw: String? = null

    @Column(name = "ATB90S_GR")
    var atb90s_gr: String? = null

    @Column(name = "CAA001")
    var caa001: BigDecimal? = null

    @Column(name = "CAA002")
    var caa002: BigDecimal? = null

    @Column(name = "ATJ0E5")
    var atj0e5: BigDecimal? = null

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
data class TJ0EId(
    val aae002: Long? = null,
    val atb930: Long? = null,
    val atj000: String? = null,
) : Serializable
