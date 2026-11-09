package com.jlrc.rcpq.entity

import jakarta.persistence.*
import java.io.Serializable
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "V_TC90_ZB")
open class V_TC90_ZB {
    @Id
    @Column(name = "ATC900")
    var atc900: Long? = null

    @Column(name = "AAB00E")
    var aab00e: String? = null

    @Column(name = "ATB931")
    var atb931: String? = null

}
