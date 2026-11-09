package com.jlrc.rcpq.entity

import jakarta.persistence.*
import java.io.Serializable
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "TJ012")
open class TJ012 {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_TJ012_TJ0120")
    @SequenceGenerator(name = "SEQ_TJ012_TJ0120", sequenceName = "SEQ_TJ012_TJ0120", allocationSize = 1)
    @Column(name = "TJ0120")
    var tj0120: Long? = null

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

    @Column(name = "TJ0121")
    var tj0121: String? = null

    @Column(name = "TJ0122")
    var tj0122: String? = null

    @Column(name = "TJ0123")
    var tj0123: Long? = null

    @Column(name = "TJ0124")
    var tj0124: Long? = null

    @Column(name = "TJ0125")
    var tj0125: Long? = null

    @Column(name = "TJ0126")
    var tj0126: Long? = null

    @Column(name = "TJ0127")
    var tj0127: Long? = null

    @Column(name = "TJ0128")
    var tj0128: Long? = null

    @Column(name = "TJ0129")
    var tj0129: Long? = null

    @Column(name = "TJ012A")
    var tj012a: Long? = null

    @Column(name = "TJ012B")
    var tj012b: Long? = null

    @Column(name = "TJ012C")
    var tj012c: Long? = null

    @Column(name = "TJ012D")
    var tj012d: Long? = null

    @Column(name = "TJ012E")
    var tj012e: Long? = null

    @Column(name = "TJ012F")
    var tj012f: Long? = null

    @Column(name = "TJ012G")
    var tj012g: Long? = null

    @Column(name = "TJ012H")
    var tj012h: Long? = null

    @Column(name = "TJ012J")
    var tj012j: Long? = null

    @Column(name = "TJ012K")
    var tj012k: Long? = null

    @Column(name = "TJ012M")
    var tj012m: Long? = null

    @Column(name = "TJ012N")
    var tj012n: Long? = null

    @Column(name = "TJ012P")
    var tj012p: BigDecimal? = null

    @Column(name = "AAE013")
    var aae013: String? = null

    @Column(name = "TJ012Q")
    var tj012q: String? = null

    @Column(name = "TJ012R")
    var tj012r: String? = null

    @Column(name = "TJ012S")
    var tj012s: String? = null

    @Column(name = "TJ012T")
    var tj012t: String? = null

    @Column(name = "TJ012U")
    var tj012u: LocalDate? = null

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
