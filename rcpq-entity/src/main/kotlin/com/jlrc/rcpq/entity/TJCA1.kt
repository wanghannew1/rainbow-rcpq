package com.jlrc.rcpq.entity

import jakarta.persistence.*
import java.io.Serializable
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "TJCA1")
open class TJCA1 {
    @Id
    @Column(name = "TJCA10")
    var tjca10: Long? = null

    @Column(name = "BAZ001")
    var baz001: Long? = null

    @Column(name = "BAZ002")
    var baz002: Long? = null

    @Column(name = "TJ0000")
    var tj0000: Long? = null

    @Column(name = "AAA020")
    var aaa020: String? = null

    @Column(name = "AAA021")
    var aaa021: String? = null

    @Column(name = "AAE001")
    var aae001: Long? = null

    @Column(name = "TJCA11")
    var tjca11: String? = null

    @Column(name = "TJCA12")
    var tjca12: String? = null

    @Column(name = "TJ000U")
    var tj000u: Long? = null

    @Column(name = "TJCA14")
    var tjca14: Long? = null

    @Column(name = "TJCA15")
    var tjca15: Long? = null

    @Column(name = "TJCA16")
    var tjca16: Long? = null

    @Column(name = "TJCA17")
    var tjca17: Long? = null

    @Column(name = "TJCA18")
    var tjca18: Long? = null

    @Column(name = "TJCA19")
    var tjca19: Long? = null

    @Column(name = "TJCA1A")
    var tjca1a: Long? = null

    @Column(name = "TJCA1B")
    var tjca1b: Long? = null

    @Column(name = "TJCA1C")
    var tjca1c: Long? = null

    @Column(name = "TJCA1D")
    var tjca1d: Long? = null

    @Column(name = "TJCA1E")
    var tjca1e: Long? = null

    @Column(name = "TJCA1F")
    var tjca1f: Long? = null

    @Column(name = "TJCA1G")
    var tjca1g: Long? = null

    @Column(name = "AAE013")
    var aae013: String? = null

    @Column(name = "TJ001M")
    var tj001m: String? = null

    @Column(name = "TJ001N")
    var tj001n: String? = null

    @Column(name = "TJ001P")
    var tj001p: String? = null

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

}
