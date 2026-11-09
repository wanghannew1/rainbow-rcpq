package com.jlrc.rcpq.entity

import jakarta.persistence.*
import java.io.Serializable
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "V_TC02_TC90")
open class V_TC02_TC90 {
    @Id
    @Column(name = "ATC900")
    var atc900: Long? = null

    @Column(name = "AAC002")
    var aac002: String? = null

    @Column(name = "AAC001")
    var aac001: Long? = null

    @Column(name = "AAC003")
    var aac003: String? = null

    @Column(name = "AAC004")
    var aac004: String? = null

    @Column(name = "AAC006")
    var aac006: LocalDate? = null

    @Column(name = "AAB004")
    var aab004: String? = null

    @Column(name = "AAB001")
    var aab001: Long? = null

    @Column(name = "ATC020")
    var atc020: Long? = null

}
