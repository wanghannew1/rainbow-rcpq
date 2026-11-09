package com.jlrc.rcpq.entity

import jakarta.persistence.*
import java.io.Serializable
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "TJDA3")
open class TJDA3 {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_TJDA3_TJDA30")
    @SequenceGenerator(name = "SEQ_TJDA3_TJDA30", sequenceName = "SEQ_TJDA3_TJDA30", allocationSize = 1)
    @Column(name = "TJDA30")
    var tjda30: Long? = null

    @Column(name = "TJ0000")
    var tj0000: Long? = null

    @Column(name = "AAA020")
    var aaa020: String? = null

    @Column(name = "AAA021")
    var aaa021: String? = null

    @Column(name = "TJ0002")
    var tj0002: String? = null

    @Column(name = "TJ0003")
    var tj0003: String? = null

    @Column(name = "TJ0004")
    var tj0004: Long? = null

    @Column(name = "TJ0005")
    var tj0005: String? = null

    @Column(name = "TJ0006")
    var tj0006: String? = null

    @Column(name = "AAE013")
    var aae013: String? = null

    @Column(name = "TJ000A")
    var tj000a: String? = null

    @Column(name = "TJ000B")
    var tj000b: String? = null

    @Column(name = "TJ000C")
    var tj000c: String? = null

    @Column(name = "TJ000D")
    var tj000d: String? = null

    @Column(name = "TJ000E")
    var tj000e: LocalDate? = null

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

    @Column(name = "TJDA31")
    var tjda31: Long? = null

    @Column(name = "TJDA32")
    var tjda32: Long? = null

    @Column(name = "TJDA33")
    var tjda33: BigDecimal? = null

    @Column(name = "TJDA34")
    var tjda34: Long? = null

    @Column(name = "TJDA35")
    var tjda35: Long? = null

    @Column(name = "TJDA36")
    var tjda36: BigDecimal? = null

    @Column(name = "TJDA37")
    var tjda37: Long? = null

    @Column(name = "TJDA38")
    var tjda38: Long? = null

    @Column(name = "TJDA39")
    var tjda39: BigDecimal? = null

    @Column(name = "TJDA3A")
    var tjda3a: Long? = null

    @Column(name = "TJDA3B")
    var tjda3b: Long? = null

    @Column(name = "TJDA3C")
    var tjda3c: BigDecimal? = null

    @Column(name = "AAE001")
    var aae001: Long? = null

}
