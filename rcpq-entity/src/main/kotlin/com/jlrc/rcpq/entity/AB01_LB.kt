package com.jlrc.rcpq.entity

import jakarta.persistence.*
import java.io.Serializable
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "AB01_LB")
open class AB01_LB {
    @Id
    @Column(name = "AAB001")
    var aab001: String? = null

    @Column(name = "AAB004")
    var aab004: String? = null

    @Column(name = "AAB019")
    var aab019: String? = null

    @Column(name = "AAB003")
    var aab003: String? = null

    @Column(name = "AAB020")
    var aab020: String? = null

    @Column(name = "AAB002")
    var aab002: String? = null

    @Column(name = "AAB036")
    var aab036: LocalDateTime? = null

    @Column(name = "AAB021")
    var aab021: String? = null

    @Column(name = "AAB022")
    var aab022: String? = null

    @Column(name = "AAB013")
    var aab013: String? = null

    @Column(name = "AAB014")
    var aab014: String? = null

    @Column(name = "AAB015")
    var aab015: String? = null

    @Column(name = "AAE006")
    var aae006: String? = null

    @Column(name = "AAE007")
    var aae007: String? = null

    @Column(name = "AAB006")
    var aab006: String? = null

    @Column(name = "AAB007")
    var aab007: String? = null

    @Column(name = "AAB008")
    var aab008: LocalDateTime? = null

    @Column(name = "AAB009")
    var aab009: Long? = null

    @Column(name = "AAB051")
    var aab051: String? = null

    @Column(name = "AAE119")
    var aae119: String? = null

    @Column(name = "AAB000")
    var aab000: Long? = null

}
