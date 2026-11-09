package com.jlrc.rcpq.entity

import jakarta.persistence.*
import java.io.Serializable
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "TJDA5")
open class TJDA5 {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_TJDA5_TJDA50")
    @SequenceGenerator(name = "SEQ_TJDA5_TJDA50", sequenceName = "SEQ_TJDA5_TJDA50", allocationSize = 1)
    @Column(name = "TJDA50")
    var tjda50: Long? = null

    @Column(name = "TJ0000")
    var tj0000: Long? = null

    @Column(name = "AAA020")
    var aaa020: String? = null

    @Column(name = "AAA021")
    var aaa021: String? = null

    @Column(name = "AAE002")
    var aae002: String? = null

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

    @Column(name = "TJDA51")
    var tjda51: Long? = null

    @Column(name = "TJDA52")
    var tjda52: Long? = null

    @Column(name = "TJDA53")
    var tjda53: BigDecimal? = null

    @Column(name = "TJDA54")
    var tjda54: BigDecimal? = null

    @Column(name = "TJDA55")
    var tjda55: BigDecimal? = null

    @Column(name = "TJDA56")
    var tjda56: BigDecimal? = null

}
