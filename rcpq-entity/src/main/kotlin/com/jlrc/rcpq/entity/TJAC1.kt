package com.jlrc.rcpq.entity

import jakarta.persistence.*
import java.io.Serializable
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "TJAC1")
open class TJAC1 {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_TJAC1_TJAC10")
    @SequenceGenerator(name = "SEQ_TJAC1_TJAC10", sequenceName = "SEQ_TJAC1_TJAC10", allocationSize = 1)
    @Column(name = "TJAC10")
    var tjac10: Long? = null

    @Column(name = "BAZ001")
    var baz001: Long? = null

    @Column(name = "BAZ002")
    var baz002: Long? = null

    @Column(name = "TJ0000")
    var tj0000: Long? = null

    @Column(name = "AHB400")
    var ahb400: Long? = null

    @Column(name = "AHB401")
    var ahb401: String? = null

    @Column(name = "AAE001")
    var aae001: Long? = null

    @Column(name = "TJAC11")
    var tjac11: String? = null

    @Column(name = "TJAC12")
    var tjac12: String? = null

    @Column(name = "TJ000U")
    var tj000u: Long? = null

    @Column(name = "TJAC14")
    var tjac14: Long? = null

    @Column(name = "TJAC15")
    var tjac15: Long? = null

    @Column(name = "TJAC16")
    var tjac16: Long? = null

    @Column(name = "TJAC17")
    var tjac17: Long? = null

    @Column(name = "TJAC18")
    var tjac18: LocalDate? = null

    @Column(name = "TJAC19")
    var tjac19: Long? = null

    @Column(name = "TJAC1A")
    var tjac1a: Long? = null

    @Column(name = "AAE013")
    var aae013: String? = null

    @Column(name = "AAC003")
    var aac003: String? = null

    @Column(name = "TJ001Q")
    var tj001q: String? = null

    @Column(name = "TJ001R")
    var tj001r: LocalDate? = null

    @Column(name = "TJ000X")
    var tj000x: String? = null

    @Column(name = "TJ000W")
    var tj000w: String? = null

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

    @Column(name = "TJAC1B")
    var tjac1b: Long? = null

    @Column(name = "TJAC1C")
    var tjac1c: Long? = null

    @Column(name = "TJAC1D")
    var tjac1d: Long? = null

}
