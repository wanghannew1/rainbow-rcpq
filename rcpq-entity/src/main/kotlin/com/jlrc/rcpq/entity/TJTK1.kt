package com.jlrc.rcpq.entity

import jakarta.persistence.*
import java.io.Serializable
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "TJTK1")
open class TJTK1 {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_TJTK1_TJTK10")
    @SequenceGenerator(name = "SEQ_TJTK1_TJTK10", sequenceName = "SEQ_TJTK1_TJTK10", allocationSize = 1)
    @Column(name = "TJTK10")
    var tjtk10: Long? = null

    @Column(name = "ATB100")
    var atb100: Long? = null

    @Column(name = "ATB101")
    var atb101: String? = null

    @Column(name = "TJTK12")
    var tjtk12: LocalDate? = null

    @Column(name = "TJTK13")
    var tjtk13: LocalDate? = null

    @Column(name = "ATC10C")
    var atc10c: String? = null

    @Column(name = "TJTK11")
    var tjtk11: String? = null

    @Column(name = "AAC001")
    var aac001: Long? = null

    @Column(name = "AAC002")
    var aac002: String? = null

    @Column(name = "AAC003")
    var aac003: String? = null

    @Column(name = "AAC004")
    var aac004: String? = null

    @Column(name = "AAC006")
    var aac006: Long? = null

    @Column(name = "AAC005")
    var aac005: String? = null

    @Column(name = "ATC109")
    var atc109: String? = null

    @Column(name = "AAC181")
    var aac181: LocalDate? = null

    @Column(name = "AAC182")
    var aac182: String? = null

    @Column(name = "AAE013")
    var aae013: String? = null

    @Column(name = "AAE011")
    var aae011: String? = null

    @Column(name = "AAE017")
    var aae017: String? = null

    @Column(name = "AAE036")
    var aae036: LocalDateTime? = null

    @Column(name = "AAE019")
    var aae019: String? = null

    @Column(name = "AAE020")
    var aae020: String? = null

    @Column(name = "AAE022")
    var aae022: String? = null

    @Column(name = "AAC180")
    var aac180: String? = null
}
