package com.jlrc.rcpq.entity

import jakarta.persistence.*
import java.io.Serializable
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "TJAM3")
open class TJAM3 {
    @Id
    @Column(name = "TJAM30")
    var tjam30: Long? = null

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

    @Column(name = "TJAM31")
    var tjam31: String? = null

    @Column(name = "TJAM32")
    var tjam32: String? = null

    @Column(name = "TJAM33")
    var tjam33: Long? = null

    @Column(name = "TJAM34")
    var tjam34: Long? = null

    @Column(name = "TJAM35")
    var tjam35: Long? = null

    @Column(name = "TJAM36")
    var tjam36: BigDecimal? = null

    @Column(name = "TJAM37")
    var tjam37: Long? = null

    @Column(name = "TJAM38")
    var tjam38: BigDecimal? = null

    @Column(name = "TJAM39")
    var tjam39: Long? = null

    @Column(name = "TJAM3A")
    var tjam3a: Long? = null

    @Column(name = "TJAM3N")
    var tjam3n: String? = null

    @Column(name = "TJAM3P")
    var tjam3p: String? = null

    @Column(name = "TJAM3Q")
    var tjam3q: String? = null

    @Column(name = "TJAM3R")
    var tjam3r: String? = null

    @Column(name = "TJAM3S")
    var tjam3s: LocalDate? = null

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
