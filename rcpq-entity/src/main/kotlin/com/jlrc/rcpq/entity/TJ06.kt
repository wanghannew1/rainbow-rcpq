package com.jlrc.rcpq.entity

import jakarta.persistence.*
import java.io.Serializable
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "TJ06")
@IdClass(TJ06Id::class)
open class TJ06 {
    @Id
    @Column(name = "AAE002")
    var aae002: Long? = null

    @Id
    @Column(name = "ATC900")
    var atc900: Long? = null

    @Column(name = "ATB930")
    var atb930: String? = null

    @Column(name = "ATB931")
    var atb931: String? = null

    @Column(name = "AAC002")
    var aac002: String? = null

    @Column(name = "AAC003")
    var aac003: String? = null

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

    @Column(name = "BBA200")
    var bba200: BigDecimal? = null

    @Column(name = "BAA200")
    var baa200: BigDecimal? = null

    @Column(name = "ATC9EH")
    var atc9eh: BigDecimal? = null

    @Column(name = "ATC8H3")
    var atc8h3: String? = null

    @Column(name = "AAA031")
    var aaa031: String? = null

    @Column(name = "AAA032")
    var aaa032: String? = null

}
data class TJ06Id(
    val aae002: Long? = null,
    val atc900: Long? = null,
) : Serializable
