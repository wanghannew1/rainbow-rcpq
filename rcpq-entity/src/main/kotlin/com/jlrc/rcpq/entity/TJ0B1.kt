package com.jlrc.rcpq.entity

import jakarta.persistence.*
import java.io.Serializable
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "TJ0B1")
@IdClass(TJ0B1Id::class)
open class TJ0B1 {
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

    @Column(name = "AAA302")
    var aaa302: BigDecimal? = null

    @Column(name = "AAA305")
    var aaa305: BigDecimal? = null

    @Column(name = "AAA306")
    var aaa306: BigDecimal? = null

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

    @Column(name = "DAA001")
    var daa001: BigDecimal? = null

    @Column(name = "ATJ0B3")
    var atj0b3: BigDecimal? = null

    @Column(name = "ATJ0B5")
    var atj0b5: BigDecimal? = null

    @Column(name = "ATC072")
    var atc072: String? = null

    @Column(name = "ATC074")
    var atc074: String? = null

    @Column(name = "AAE013")
    var aae013: String? = null

    @Column(name = "ATJ0AA")
    var atj0aa: String? = null

    @Column(name = "ATJ0B6")
    var atj0b6: BigDecimal? = null

    @Column(name = "AAC001")
    var aac001: Long? = null

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
data class TJ0B1Id(
    val aae002: Long? = null,
    val atc900: Long? = null,
    val atj000: String? = null,
) : Serializable
