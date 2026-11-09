package com.jlrc.rcpq.entity

import jakarta.persistence.*
import java.io.Serializable
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "V_CBJ")
@IdClass(V_CBJId::class)
open class V_CBJ {
    @Id
    @Column(name = "ATB930")
    var atb930: Long? = null

    @Id
    @Column(name = "AAE001")
    var aae001: Long? = null

    @Column(name = "ATB931")
    var atb931: String? = null

    @Column(name = "ATB90Z_1")
    var atb90z_1: Long? = null

    @Column(name = "ATB90Z_2")
    var atb90z_2: Long? = null

}
data class V_CBJId(
    val atb930: Long? = null,
    val aae001: Long? = null,
) : Serializable
