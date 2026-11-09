package com.jlrc.rcpq.entity

import jakarta.persistence.*
import java.io.Serializable
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "V_TD04_TB93")
@IdClass(V_TD04_TB93Id::class)
open class V_TD04_TB93 {
    @Id
    @Column(name = "ATB930")
    var atb930: Long? = null

    @Id
    @Column(name = "AAE002")
    var aae002: Long? = null

    @Column(name = "CNT")
    var cnt: Long? = null

    @Column(name = "ATD043")
    var atd043: BigDecimal? = null

    @Column(name = "ATB90A")
    var atb90a: BigDecimal? = null

    @Column(name = "ATD047")
    var atd047: BigDecimal? = null

    @Column(name = "ATB931")
    var atb931: String? = null

}
data class V_TD04_TB93Id(
    val atb930: Long? = null,
    val aae002: Long? = null,
) : Serializable
