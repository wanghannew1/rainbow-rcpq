package com.jlrc.rcpq.entity

import jakarta.persistence.*
import java.io.Serializable
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "TJDA4")
open class TJDA4 {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_TJDA4_TJDA40")
    @SequenceGenerator(name = "SEQ_TJDA4_TJDA40", sequenceName = "SEQ_TJDA4_TJDA40", allocationSize = 1)
    @Column(name = "TJDA40")
    var tjda40: Long? = null

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

    @Column(name = "TJDA41")
    var tjda41: Long? = null

    @Column(name = "TJDA42")
    var tjda42: Long? = null

    @Column(name = "TJDA43")
    var tjda43: Long? = null

    @Column(name = "TJDA44")
    var tjda44: Long? = null

    @Column(name = "TJDA45")
    var tjda45: BigDecimal? = null

    @Column(name = "TJDA46")
    var tjda46: Long? = null

    @Column(name = "TJDA47")
    var tjda47: Long? = null

}
