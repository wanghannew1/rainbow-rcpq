package com.jlrc.rcpq.entity

import jakarta.persistence.*
import java.io.Serializable
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "V_TC9A")
open class V_TC9A {
    @Id
    @Column(name = "ATC9A0")
    var atc9a0: Long? = null

    @Column(name = "AAB004")
    var aab004: String? = null

    @Column(name = "AAB004_1")
    var aab004_1: String? = null

    @Column(name = "AAC001")
    var aac001: String? = null

    @Column(name = "AAC002")
    var aac002: String? = null

    @Column(name = "AAC003")
    var aac003: String? = null

    @Column(name = "ATC9A8")
    var atc9a8: String? = null

    @Column(name = "ATC9A9")
    var atc9a9: LocalDate? = null

    @Column(name = "AAE036")
    var aae036: LocalDateTime? = null

    @Column(name = "ATC978")
    var atc978: String? = null

}
