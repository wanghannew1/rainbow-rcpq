package com.jlrc.rcpq.entity

import jakarta.persistence.*
import java.io.Serializable
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "V_TC93_TC9AA")
@IdClass(V_TC93_TC9AAId::class)
open class V_TC93_TC9AA {
    @Id
    @Column(name = "AAE002")
    var aae002: Long? = null

    @Id
    @Column(name = "AAB004")
    var aab004: String? = null

    @Id
    @Column(name = "AAC003")
    var aac003: String? = null

    @Id
    @Column(name = "AAC002")
    var aac002: String? = null

    @Column(name = "ATC937")
    var atc937: String? = null

    @Column(name = "ATC93AA")
    var atc93aa: String? = null

    @Column(name = "ATC93C")
    var atc93c: String? = null

    @Column(name = "CAA002")
    var caa002: String? = null

    @Column(name = "ATC9AAB")
    var atc9aab: String? = null

    @Column(name = "LX")
    var lx: String? = null

}
data class V_TC93_TC9AAId(
    val aae002: Long? = null,
    val aab004: String? = null,
    val aac003: String? = null,
    val aac002: String? = null,
) : Serializable
