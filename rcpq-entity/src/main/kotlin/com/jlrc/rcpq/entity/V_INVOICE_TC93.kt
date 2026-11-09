package com.jlrc.rcpq.entity

import jakarta.persistence.*
import java.io.Serializable
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "V_INVOICE_TC93")
@IdClass(V_INVOICE_TC93Id::class)
open class V_INVOICE_TC93 {
    @Id
    @Column(name = "ATB930")
    var atb930: Long? = null

    @Id
    @Column(name = "ATC937")
    var atc937: String? = null

    @Id
    @Column(name = "AAE002")
    var aae002: Long? = null

    @Column(name = "AAB001")
    var aab001: Long? = null

    @Column(name = "ATC933")
    var atc933: Long? = null

    @Column(name = "ATB931")
    var atb931: String? = null

    @Column(name = "ATB934")
    var atb934: String? = null

    @Column(name = "ATB935")
    var atb935: String? = null

    @Column(name = "AAB004")
    var aab004: String? = null

    @Column(name = "AAB00E")
    var aab00e: String? = null

}
data class V_INVOICE_TC93Id(
    val atb930: Long? = null,
    val atc937: String? = null,
    val aae002: Long? = null,
) : Serializable
