package com.jlrc.rcpq.entity

import jakarta.persistence.*
import java.io.Serializable
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "TJ071")
open class TJ071 {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_TJ071_TJ0710")
    @SequenceGenerator(name = "SEQ_TJ071_TJ0710", sequenceName = "SEQ_TJ071_TJ0710", allocationSize = 1)
    @Column(name = "TJ0710")
    var tj0710: Long? = null

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

    @Column(name = "TJ0711")
    var tj0711: BigDecimal? = null

    @Column(name = "TJ0712")
    var tj0712: BigDecimal? = null

    @Column(name = "TJ0713")
    var tj0713: BigDecimal? = null

    @Column(name = "TJ0714")
    var tj0714: BigDecimal? = null

    @Column(name = "TJ0715")
    var tj0715: BigDecimal? = null

    @Column(name = "TJ0716")
    var tj0716: BigDecimal? = null

    @Column(name = "TJ0717")
    var tj0717: BigDecimal? = null

    @Column(name = "TJ0718")
    var tj0718: BigDecimal? = null

    @Column(name = "TJ0719")
    var tj0719: BigDecimal? = null

    @Column(name = "TJ071A")
    var tj071a: BigDecimal? = null

    @Column(name = "TJ071B")
    var tj071b: BigDecimal? = null

    @Column(name = "TJ071C")
    var tj071c: BigDecimal? = null

    @Column(name = "TJ071D")
    var tj071d: BigDecimal? = null

    @Column(name = "TJ071E")
    var tj071e: BigDecimal? = null

    @Column(name = "TJ071F")
    var tj071f: BigDecimal? = null

    @Column(name = "AC000A")
    var ac000a: String? = null

    @Column(name = "AC000B")
    var ac000b: String? = null

    @Column(name = "AC000C")
    var ac000c: String? = null

    @Column(name = "AC000D")
    var ac000d: String? = null

    @Column(name = "AC000E")
    var ac000e: LocalDate? = null

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
