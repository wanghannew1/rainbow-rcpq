package com.jlrc.rcpq.entity

import jakarta.persistence.*
import java.io.Serializable
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "AC0B")
open class AC0B {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_AC0B_AAC0B0")
    @SequenceGenerator(name = "SEQ_AC0B_AAC0B0", sequenceName = "SEQ_AC0B_AAC0B0", allocationSize = 1)
    @Column(name = "AAC0B0")
    var aac0b0: Long? = null

    @Column(name = "BAZ001")
    var baz001: Long? = null

    @Column(name = "BAZ002")
    var baz002: Long? = null

    @Column(name = "AAC001")
    var aac001: Long? = null

    @Column(name = "AAC045")
    var aac045: String? = null

    @Column(name = "AAE030")
    var aae030: LocalDate? = null

    @Column(name = "AAE031")
    var aae031: LocalDate? = null

    @Column(name = "AAC0B4")
    var aac0b4: String? = null

    @Column(name = "AAC217")
    var aac217: BigDecimal? = null

    @Column(name = "AAC014")
    var aac014: String? = null

    @Column(name = "AAC0B1")
    var aac0b1: String? = null

    @Column(name = "AAC015")
    var aac015: String? = null

    @Column(name = "AAC0B2")
    var aac0b2: String? = null

    @Column(name = "AAC0B3")
    var aac0b3: String? = null

    @Column(name = "AAC0B5")
    var aac0b5: String? = null

    @Column(name = "AAC0B6")
    var aac0b6: String? = null

    @Column(name = "AAC0B7")
    var aac0b7: String? = null

    @Column(name = "AAC0B8")
    var aac0b8: String? = null

    @Column(name = "AAC0BA")
    var aac0ba: LocalDate? = null

    @Column(name = "AAE013")
    var aae013: String? = null

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

    @Column(name = "BAC014")
    var bac014: String? = null

}
