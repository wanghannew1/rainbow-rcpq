package com.jlrc.rcpq.entity

import jakarta.persistence.*
import java.io.Serializable
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "V_ZDGZFFQD")
open class V_ZDGZFFQD {
    @Id
    @Column(name = "ATC930")
    var atc930: Long? = null

    @Column(name = "XMH")
    var xmh: String? = null

    @Column(name = "AAE00A")
    var aae00a: String? = null

    @Column(name = "AAC003")
    var aac003: String? = null

    @Column(name = "ATC93C")
    var atc93c: BigDecimal? = null

    @Column(name = "ATB931")
    var atb931: String? = null

    @Column(name = "AAB004")
    var aab004: String? = null

    @Column(name = "ATC931")
    var atc931: Long? = null

    @Column(name = "ATC932")
    var atc932: Long? = null

    @Column(name = "RYBDQK")
    var rybdqk: String? = null

    @Column(name = "AAE013")
    var aae013: String? = null

    @Column(name = "ATC937")
    var atc937: String? = null

}
