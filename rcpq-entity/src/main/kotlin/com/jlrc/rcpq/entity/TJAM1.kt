package com.jlrc.rcpq.entity

import jakarta.persistence.*
import java.io.Serializable
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "TJAM1")
open class TJAM1 {
    @Id
    @Column(name = "TJAM10")
    var tjam10: Long? = null

    @Column(name = "TJ0000")
    var tj0000: Long? = null

    @Column(name = "AAA020")
    var aaa020: String? = null

    @Column(name = "AAA021")
    var aaa021: String? = null

    @Column(name = "AAE001")
    var aae001: Long? = null

    @Column(name = "AAE00A")
    var aae00a: Long? = null

    @Column(name = "AC0009")
    var ac0009: Long? = null

    @Column(name = "TJAM11")
    var tjam11: String? = null

    @Column(name = "TJAM12")
    var tjam12: String? = null

    @Column(name = "TJAM13")
    var tjam13: Long? = null

    @Column(name = "TJAM14")
    var tjam14: Long? = null

    @Column(name = "TJAM15")
    var tjam15: Long? = null

    @Column(name = "TJAM16")
    var tjam16: Long? = null

    @Column(name = "TJAM17")
    var tjam17: Long? = null

    @Column(name = "TJAM18")
    var tjam18: Long? = null

    @Column(name = "TJAM19")
    var tjam19: Long? = null

    @Column(name = "TJAM1A")
    var tjam1a: Long? = null

    @Column(name = "TJAM1B")
    var tjam1b: BigDecimal? = null

    @Column(name = "TJAM1C")
    var tjam1c: BigDecimal? = null

    @Column(name = "TJAM1D")
    var tjam1d: String? = null

    @Column(name = "TJAM1E")
    var tjam1e: String? = null

    @Column(name = "TJAM1F")
    var tjam1f: String? = null

    @Column(name = "TJAM1G")
    var tjam1g: String? = null

    @Column(name = "TJAM1H")
    var tjam1h: LocalDate? = null

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

    @Column(name = "BAZ001")
    var baz001: Long? = null

    @Column(name = "BAZ002")
    var baz002: Long? = null

}
