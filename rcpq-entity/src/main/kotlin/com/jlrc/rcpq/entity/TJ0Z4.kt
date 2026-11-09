package com.jlrc.rcpq.entity

import jakarta.persistence.*
import java.io.Serializable
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "TJ0Z4")
@IdClass(TJ0Z4Id::class)
open class TJ0Z4 {
    @Id
    @Column(name = "AAE002")
    var aae002: Long? = null

    @Id
    @Column(name = "ATJ001")
    var atj001: String? = null

    @Id
    @Column(name = "ATB930")
    var atb930: Long? = null

    @Column(name = "ATB931")
    var atb931: String? = null

    @Column(name = "AAB004")
    var aab004: String? = null

}
data class TJ0Z4Id(
    val aae002: Long? = null,
    val atj001: String? = null,
    val atb930: Long? = null,
) : Serializable
