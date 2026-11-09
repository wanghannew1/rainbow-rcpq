package com.jlrc.rcpq.entity

import jakarta.persistence.*
import java.io.Serializable
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "V_TC9V")
open class V_TC9V {
    @Id
    @Column(name = "KEY")
    var key: String? = null

    @Column(name = "AAC001")
    var aac001: Long? = null

    @Column(name = "AAC002")
    var aac002: String? = null

    @Column(name = "AAC003")
    var aac003: String? = null

    @Column(name = "ATC9V1")
    var atc9v1: String? = null

    @Column(name = "ATC9U1")
    var atc9u1: BigDecimal? = null

    @Column(name = "AAE002_BEGIN")
    var aae002_begin: Long? = null

    @Column(name = "AAE002_END")
    var aae002_end: Long? = null

    @Column(name = "ATC9U2")
    var atc9u2: Long? = null

    @Column(name = "ATC9U5")
    var atc9u5: Long? = null

    @Column(name = "ATC9V5")
    var atc9v5: Long? = null

    @Column(name = "ATC9VA")
    var atc9va: Long? = null

    @Column(name = "ATC9VB")
    var atc9vb: Long? = null

    @Column(name = "ATC9E0")
    var atc9e0: Long? = null

    @Column(name = "ATC9VC")
    var atc9vc: String? = null

    @Column(name = "ATC9V2")
    var atc9v2: String? = null

}
