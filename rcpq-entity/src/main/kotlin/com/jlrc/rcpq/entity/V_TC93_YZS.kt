package com.jlrc.rcpq.entity

import jakarta.persistence.*
import java.io.Serializable
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "V_TC93_YZS")
@IdClass(V_TC93_YZSId::class)
open class V_TC93_YZS {
    @Id
    @Column(name = "ATB900")
    var atb900: Long? = null

    @Id
    @Column(name = "ATC932")
    var atc932: Long? = null

    @Column(name = "AAB004")
    var aab004: String? = null

    @Column(name = "DWSB")
    var dwsb: Long? = null

    @Column(name = "BBA001")
    var bba001: Long? = null

    @Column(name = "BBA002")
    var bba002: Long? = null

    @Column(name = "BBA003")
    var bba003: Long? = null

    @Column(name = "BBA004")
    var bba004: Long? = null

    @Column(name = "BBA005")
    var bba005: Long? = null

    @Column(name = "GRSB")
    var grsb: Long? = null

    @Column(name = "BAA001")
    var baa001: Long? = null

    @Column(name = "BAA002")
    var baa002: Long? = null

    @Column(name = "BAA003")
    var baa003: Long? = null

    @Column(name = "CAA001")
    var caa001: Long? = null

    @Column(name = "CAA002")
    var caa002: Long? = null

    @Column(name = "ATB90A")
    var atb90a: Long? = null

}
data class V_TC93_YZSId(
    val atb900: Long? = null,
    val atc932: Long? = null,
) : Serializable
