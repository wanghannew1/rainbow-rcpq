package com.jlrc.rcpq.entity

import jakarta.persistence.*
import java.io.Serializable
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime
@Entity
@Table(name = "AA10")
@IdClass(AA10Id::class)
open class AA10 {
    @Id
    @Column(name = "AAA100")
    var aaa100: String? = null

    @Id
    @Column(name = "AAA102")
    var aaa102: String? = null

    @Column(name = "AAA101")
    var aaa101: String? = null

    @Column(name = "AAA103")
    var aaa103: String? = null

    @Column(name = "AAA104")
    var aaa104: String? = null

    @Column(name = "AAA105")
    var aaa105: String? = null

    @Column(name = "AAA106")
    var aaa106: String? = null

    @Column(name = "AAA107")
    var aaa107: String? = null

    @Column(name = "AAA108")
    var aaa108: String? = null

}
data class AA10Id(
    val aaa100: Long? = null,
    val aaa102: Long? = null,
) : Serializable
