package com.jlrc.rcpq.entity

import jakarta.persistence.*
import java.io.Serializable
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "TJ0D1")
@IdClass(TJ0D1Id::class)
open class TJ0D1 {
    @Id
    @Column(name = "AAE002")
    var aae002: Long? = null

    @Id
    @Column(name = "ATC900")
    var atc900: Long? = null

    @Id
    @Column(name = "ATJ000")
    var atj000: String? = null

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

    @Column(name = "AAA020")
    var aaa020: String? = null

    @Column(name = "AAA021")
    var aaa021: String? = null

    @Column(name = "AAA001")
    var aaa001: String? = null

    @Column(name = "AAC003")
    var aac003: String? = null

    @Column(name = "AAC002")
    var aac002: String? = null

    @Column(name = "AAC009")
    var aac009: String? = null

    @Column(name = "AAC008")
    var aac008: String? = null

    @Column(name = "AAA303")
    var aaa303: BigDecimal? = null

    @Column(name = "ATB90S_DW")
    var atb90s_dw: String? = null

    @Column(name = "ATB90S_GR")
    var atb90s_gr: String? = null

    @Column(name = "CAA001")
    var caa001: BigDecimal? = null

    @Column(name = "CAA002")
    var caa002: BigDecimal? = null

    @Column(name = "ATJ0D5")
    var atj0d5: BigDecimal? = null

    @Column(name = "ATC072")
    var atc072: String? = null

    @Column(name = "ATC074")
    var atc074: String? = null

    @Column(name = "AAE013")
    var aae013: String? = null

    @Column(name = "ATJ0AA")
    var atj0aa: String? = null

}
data class TJ0D1Id(
    val aae002: Long? = null,
    val atc900: Long? = null,
    val atj000: String? = null,
) : Serializable
