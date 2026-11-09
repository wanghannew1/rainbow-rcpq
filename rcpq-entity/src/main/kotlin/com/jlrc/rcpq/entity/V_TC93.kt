package com.jlrc.rcpq.entity

import jakarta.persistence.*
import java.io.Serializable
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "V_TC93")
@IdClass(V_TC93Id::class)
open class V_TC93 {
    @Id
    @Column(name = "ATB930")
    var atb930: Long? = null

    @Id
    @Column(name = "AAB004")
    var aab004: String? = null

    @Id
    @Column(name = "ATC931")
    var atc931: Long? = null

    @Id
    @Column(name = "ATC937")
    var atc937: String? = null

    @Column(name = "ATB931")
    var atb931: String? = null

    @Column(name = "ATC93X")
    var atc93x: BigDecimal? = null

    @Column(name = "ATC933")
    var atc933: BigDecimal? = null

    @Column(name = "ATC934")
    var atc934: BigDecimal? = null

    @Column(name = "ATC935")
    var atc935: BigDecimal? = null

    @Column(name = "ATC936")
    var atc936: BigDecimal? = null

    @Column(name = "ATC93K")
    var atc93k: BigDecimal? = null

    @Column(name = "ATC93L")
    var atc93l: BigDecimal? = null

    @Column(name = "ATC93M")
    var atc93m: BigDecimal? = null

    @Column(name = "ATC93Y")
    var atc93y: BigDecimal? = null

    @Column(name = "ATC93AA")
    var atc93aa: BigDecimal? = null

    @Column(name = "ATC93AB")
    var atc93ab: BigDecimal? = null

    @Column(name = "ATC93D")
    var atc93d: BigDecimal? = null

    @Column(name = "ATC93C")
    var atc93c: BigDecimal? = null

    @Column(name = "DWSB")
    var dwsb: BigDecimal? = null

    @Column(name = "GRSB")
    var grsb: BigDecimal? = null

    @Column(name = "CAA001")
    var caa001: BigDecimal? = null

    @Column(name = "CAA002")
    var caa002: BigDecimal? = null

    @Column(name = "ATB90A")
    var atb90a: BigDecimal? = null

    @Column(name = "ATB90B")
    var atb90b: BigDecimal? = null

    @Column(name = "DAA001")
    var daa001: BigDecimal? = null

    @Column(name = "EAA001")
    var eaa001: BigDecimal? = null

    @Column(name = "BBA100")
    var bba100: BigDecimal? = null

    @Column(name = "BAA100")
    var baa100: BigDecimal? = null

    @Column(name = "CAA101")
    var caa101: BigDecimal? = null

    @Column(name = "CAA102")
    var caa102: BigDecimal? = null

    @Column(name = "BBA200")
    var bba200: BigDecimal? = null

    @Column(name = "BAA200")
    var baa200: BigDecimal? = null

    @Column(name = "BBA300")
    var bba300: BigDecimal? = null

    @Column(name = "BAA300")
    var baa300: BigDecimal? = null

    @Column(name = "ATC93V")
    var atc93v: String? = null

    @Column(name = "AAE114")
    var aae114: String? = null

    @Column(name = "AAE214")
    var aae214: String? = null

    @Column(name = "AAE314")
    var aae314: String? = null

}
data class V_TC93Id(
    val atb930: Long? = null,
    val aab004: String? = null,
    val atc931: Long? = null,
    val atc937: String? = null,
) : Serializable
