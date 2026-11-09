package com.jlrc.rcpq.entity

import jakarta.persistence.*
import java.io.Serializable
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "V_TC93_BALANCE")
@IdClass(V_TC93_BALANCEId::class)
open class V_TC93_BALANCE {
    @Id
    @Column(name = "ATB930")
    var atb930: Long? = null

    @Id
    @Column(name = "ATC937")
    var atc937: String? = null

    @Id
    @Column(name = "ATC931")
    var atc931: Long? = null

    @Column(name = "ATC932")
    var atc932: Long? = null

    @Column(name = "AAE002")
    var aae002: Long? = null

    @Column(name = "ATB931")
    var atb931: String? = null

    @Column(name = "ATB934")
    var atb934: String? = null

    @Column(name = "ATB935")
    var atb935: String? = null

    @Column(name = "ATB90Y")
    var atb90y: String? = null

    @Column(name = "ATB90AA")
    var atb90aa: String? = null

    @Column(name = "AAB00E")
    var aab00e: String? = null

}
data class V_TC93_BALANCEId(
    val atb930: Long? = null,
    val atc937: String? = null,
    val atc931: Long? = null,
) : Serializable
