package com.jlrc.rcpq.entity

import jakarta.persistence.*
import java.io.Serializable
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "V_AB01")
open class V_AB01 {
    @Id
    @Column(name = "AAB001")
    var aab001: Long? = null

    @Column(name = "AAB004")
    var aab004: String? = null

    @Column(name = "AAB00A")
    var aab00a: String? = null

    @Column(name = "AAB00E")
    var aab00e: String? = null

    @Column(name = "ID")
    var id: String? = null

    @Column(name = "LV")
    var lv: Long? = null

}
