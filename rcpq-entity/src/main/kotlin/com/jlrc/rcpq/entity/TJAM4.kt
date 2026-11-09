package com.jlrc.rcpq.entity

import jakarta.persistence.*
import java.io.Serializable
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "TJAM4")
open class TJAM4 {
    @Id
    @Column(name = "TJAM40")
    var tjam40: Long? = null

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

    @Column(name = "TJAM41")
    var tjam41: String? = null

    @Column(name = "TJAM42")
    var tjam42: String? = null

    @Column(name = "TJAM43")
    var tjam43: Long? = null

    @Column(name = "TJAM44")
    var tjam44: Long? = null

    @Column(name = "TJAM45")
    var tjam45: Long? = null

    @Column(name = "TJAM46")
    var tjam46: Long? = null

    @Column(name = "TJAM47")
    var tjam47: Long? = null

    @Column(name = "TJAM48")
    var tjam48: Long? = null

    @Column(name = "TJAM49")
    var tjam49: Long? = null

    @Column(name = "TJAM4N")
    var tjam4n: String? = null

    @Column(name = "TJAM4P")
    var tjam4p: String? = null

    @Column(name = "TJAM4Q")
    var tjam4q: String? = null

    @Column(name = "TJAM4R")
    var tjam4r: String? = null

    @Column(name = "TJAM4S")
    var tjam4s: LocalDate? = null

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
