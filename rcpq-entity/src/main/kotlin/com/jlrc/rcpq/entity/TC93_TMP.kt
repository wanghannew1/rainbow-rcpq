package com.jlrc.rcpq.entity

import jakarta.persistence.*
import java.io.Serializable
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "TC93_TMP")
@IdClass(TC93_TMPId::class)
open class TC93_TMP {
    @Id
    @Column(name = "ATC930")
    var atc930: String? = null

    @Id
    @Column(name = "AAC002")
    var aac002: String? = null

    @Id
    @Column(name = "AAC003")
    var aac003: String? = null

    @Id
    @Column(name = "STR")
    var str: String? = null

    @Id
    @Column(name = "KEY")
    var key: String? = null

}
data class TC93_TMPId(
    val atc930: String? = null,
    val aac002: String? = null,
    val aac003: String? = null,
    val str: String? = null,
    val key: String? = null,
) : Serializable
