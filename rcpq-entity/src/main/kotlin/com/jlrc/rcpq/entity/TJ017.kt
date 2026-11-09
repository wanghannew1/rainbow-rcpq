package com.jlrc.rcpq.entity

import jakarta.persistence.*
import java.io.Serializable
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "TJ017")
@IdClass(TJ017Id::class)
open class TJ017 {
    @Id
    @Column(name = "ID")
    var id: Long? = null

    @Id
    @Column(name = "ATJ0171")
    var atj0171: String? = null

    @Id
    @Column(name = "TABLENAME")
    var tablename: String? = null

}
data class TJ017Id(
    val id: Long? = null,
    val atj0171: String? = null,
    val tablename: String? = null,
) : Serializable
