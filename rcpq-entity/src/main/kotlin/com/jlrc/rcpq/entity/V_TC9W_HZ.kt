package com.jlrc.rcpq.entity

import jakarta.persistence.*
import java.io.Serializable
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "V_TC9W_HZ")
open class V_TC9W_HZ {
    @Id
    @Column(name = "ID")
    var id: Long? = null

    @Column(name = "FLAG")
    var flag: String? = null

    @Column(name = "ATC90X")
    var atc90x: String? = null

    @Column(name = "AAC003")
    var aac003: String? = null

    @Column(name = "AAC002")
    var aac002: String? = null

    @Column(name = "TC9WHZ1")
    var tc9whz1: String? = null

    @Column(name = "AAC009")
    var aac009: String? = null

    @Column(name = "ATC9WA")
    var atc9wa: String? = null

    @Column(name = "TC9WHZ3")
    var tc9whz3: String? = null

    @Column(name = "TC9WHZ4")
    var tc9whz4: String? = null

    @Column(name = "TC9WHZ5")
    var tc9whz5: String? = null

}
