package com.jlrc.rcpq.entity

import jakarta.persistence.*
import java.io.Serializable
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "V_TC93_AVG")
@IdClass(V_TC93_AVGId::class)
open class V_TC93_AVG {
    @Id
    @Column(name = "AAC002")
    var aac002: String? = null

    @Id
    @Column(name = "AAC003")
    var aac003: String? = null

    @Id
    @Column(name = "ATC931")
    var atc931: Long? = null

    @Id
    @Column(name = "ATC933")
    var atc933: Long? = null

    @Id
    @Column(name = "ATC933_AVG")
    var atc933_avg: Long? = null

}
data class V_TC93_AVGId(
    val aac002: String? = null,
    val aac003: String? = null,
    val atc931: Long? = null,
    val atc933: Long? = null,
    val atc933_avg: Long? = null,
) : Serializable
