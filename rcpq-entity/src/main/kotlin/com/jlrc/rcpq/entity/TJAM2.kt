package com.jlrc.rcpq.entity

import jakarta.persistence.*
import java.io.Serializable
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "TJAM2")
open class TJAM2 {
    @Id
    @Column(name = "TJAM20")
    var tjam20: Long? = null

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

    @Column(name = "TJAM21")
    var tjam21: String? = null

    @Column(name = "TJAM22")
    var tjam22: String? = null

    @Column(name = "TJAM23")
    var tjam23: Long? = null

    @Column(name = "TJAM24")
    var tjam24: Long? = null

    @Column(name = "TJAM25")
    var tjam25: Long? = null

    @Column(name = "TJAM26")
    var tjam26: Long? = null

    @Column(name = "TJAM27")
    var tjam27: Long? = null

    @Column(name = "TJAM28")
    var tjam28: Long? = null

    @Column(name = "TJAM29")
    var tjam29: Long? = null

    @Column(name = "TJAM2A")
    var tjam2a: Long? = null

    @Column(name = "TJAM2B")
    var tjam2b: Long? = null

    @Column(name = "TJAM2C")
    var tjam2c: Long? = null

    @Column(name = "TJAM2D")
    var tjam2d: Long? = null

    @Column(name = "TJAM2E")
    var tjam2e: Long? = null

    @Column(name = "TJAM2F")
    var tjam2f: Long? = null

    @Column(name = "TJAM2G")
    var tjam2g: Long? = null

    @Column(name = "TJAM2H")
    var tjam2h: Long? = null

    @Column(name = "TJAM2J")
    var tjam2j: Long? = null

    @Column(name = "TJAM2K")
    var tjam2k: Long? = null

    @Column(name = "TJAM2M")
    var tjam2m: Long? = null

    @Column(name = "TJAM2N")
    var tjam2n: String? = null

    @Column(name = "TJAM2P")
    var tjam2p: String? = null

    @Column(name = "TJAM2Q")
    var tjam2q: String? = null

    @Column(name = "TJAM2R")
    var tjam2r: String? = null

    @Column(name = "TJAM2S")
    var tjam2s: LocalDate? = null

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
