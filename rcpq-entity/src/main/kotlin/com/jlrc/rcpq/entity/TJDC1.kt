package com.jlrc.rcpq.entity

import jakarta.persistence.*
import java.io.Serializable
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "TJDC1")
open class TJDC1 {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_TJDC1_TJDC10")
    @SequenceGenerator(name = "SEQ_TJDC1_TJDC10", sequenceName = "SEQ_TJDC1_TJDC10", allocationSize = 1)
    @Column(name = "TJDC10")
    var tjdc10: Long? = null

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

    @Column(name = "TJDC11")
    var tjdc11: Long? = null

    @Column(name = "TJDC12")
    var tjdc12: Long? = null

    @Column(name = "TJDC13")
    var tjdc13: Long? = null

    @Column(name = "TJDC14")
    var tjdc14: Long? = null

    @Column(name = "TJDC15")
    var tjdc15: Long? = null

    @Column(name = "TJDC16")
    var tjdc16: Long? = null

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

}
