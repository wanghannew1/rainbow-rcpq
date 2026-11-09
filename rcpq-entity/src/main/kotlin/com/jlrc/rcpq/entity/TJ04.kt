package com.jlrc.rcpq.entity

import jakarta.persistence.*
import java.io.Serializable
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "TJ04")
@IdClass(TJ04Id::class)
open class TJ04 {
    @Id
    @Column(name = "ATB930")
    var atb930: Long? = null

    @Id
    @Column(name = "ATC932")
    var atc932: Long? = null

    @Id
    @Column(name = "ATC937")
    var atc937: String? = null

    @Column(name = "ATB931")
    var atb931: String? = null

    @Column(name = "ATJ041")
    var atj041: Long? = null

    @Column(name = "ATJ042")
    var atj042: BigDecimal? = null

    @Column(name = "ATC93AA")
    var atc93aa: BigDecimal? = null

    @Column(name = "ATC93C")
    var atc93c: BigDecimal? = null

    @Column(name = "ATC93S")
    var atc93s: BigDecimal? = null

    @Column(name = "BBA200")
    var bba200: BigDecimal? = null

    @Column(name = "BAA200")
    var baa200: BigDecimal? = null

    @Column(name = "BBA300")
    var bba300: BigDecimal? = null

    @Column(name = "BAA300")
    var baa300: BigDecimal? = null

    @Column(name = "BBA000")
    var bba000: BigDecimal? = null

    @Column(name = "BBA001")
    var bba001: BigDecimal? = null

    @Column(name = "BBA002")
    var bba002: BigDecimal? = null

    @Column(name = "BBA003")
    var bba003: BigDecimal? = null

    @Column(name = "BBA004")
    var bba004: BigDecimal? = null

    @Column(name = "BBA005")
    var bba005: BigDecimal? = null

    @Column(name = "BAA000")
    var baa000: BigDecimal? = null

    @Column(name = "BAA001")
    var baa001: BigDecimal? = null

    @Column(name = "BAA002")
    var baa002: BigDecimal? = null

    @Column(name = "BAA003")
    var baa003: BigDecimal? = null

    @Column(name = "CAA001")
    var caa001: BigDecimal? = null

    @Column(name = "CAA002")
    var caa002: BigDecimal? = null

    @Column(name = "ATB90Y")
    var atb90y: String? = null

    @Column(name = "ATB90A")
    var atb90a: BigDecimal? = null

    @Column(name = "ATB90Z")
    var atb90z: BigDecimal? = null

    @Column(name = "AAB00E")
    var aab00e: String? = null

    @Column(name = "AAE002")
    var aae002: Long? = null

    @Column(name = "AAE001")
    var aae001: Long? = null

    @Column(name = "AAE314")
    var aae314: String? = null

    @Column(name = "AAE319")
    var aae319: String? = null

    @Column(name = "AAE336")
    var aae336: LocalDate? = null

    @Column(name = "AAE219")
    var aae219: String? = null

    @Column(name = "AAE236")
    var aae236: LocalDate? = null

}
data class TJ04Id(
    val atb930: Long? = null,
    val atc932: Long? = null,
    val atc937: String? = null,
) : Serializable
