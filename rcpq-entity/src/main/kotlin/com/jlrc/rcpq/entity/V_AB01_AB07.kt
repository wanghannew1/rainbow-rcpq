package com.jlrc.rcpq.entity

import jakarta.persistence.*
import java.io.Serializable
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "V_AB01_AB07")
open class V_AB01_AB07 {
    @Id
    @Column(name = "ID")
    var id: Long? = null

    @Column(name = "AAB001")
    var aab001: Long? = null

    @Column(name = "AAB004")
    var aab004: String? = null

    @Column(name = "AAE007")
    var aae007: String? = null

    @Column(name = "AAE006")
    var aae006: String? = null

    @Column(name = "AAC003")
    var aac003: String? = null

}
