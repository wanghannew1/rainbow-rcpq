package com.jlrc.rcpq.entity

import jakarta.persistence.*
import java.io.Serializable
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "TC7V")
open class TC7V {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_TC7V_ATC7V0")
    @SequenceGenerator(name = "SEQ_TC7V_ATC7V0", sequenceName = "SEQ_TC7V_ATC7V0", allocationSize = 1)
    @Column(name = "ATC7V0")
    var atc7v0: Long? = null

    @Column(name = "BAZ001")
    var baz001: Long? = null

    @Column(name = "BAZ002")
    var baz002: Long? = null

    @Column(name = "ATC702")
    var atc702: String? = null

    @Column(name = "ATC7V1")
    var atc7v1: String? = null

    @Column(name = "AAC003")
    var aac003: String? = null

    @Column(name = "AAC002")
    var aac002: String? = null

    @Column(name = "AAC004")
    var aac004: String? = null

    @Column(name = "AAC005")
    var aac005: String? = null

    @Column(name = "ATC7V2")
    var atc7v2: String? = null

    @Column(name = "AAC006")
    var aac006: LocalDate? = null

    @Column(name = "AAC025")
    var aac025: String? = null

    @Column(name = "AAC011")
    var aac011: String? = null

    @Column(name = "ATC70G")
    var atc70g: String? = null

    @Column(name = "ATC70C")
    var atc70c: LocalDate? = null

    @Column(name = "ATC70D")
    var atc70d: String? = null

    @Column(name = "AAE005")
    var aae005: String? = null

    @Column(name = "ATC7V3")
    var atc7v3: String? = null

    @Column(name = "ATC7V4")
    var atc7v4: LocalDate? = null

    @Column(name = "ATC7V5")
    var atc7v5: String? = null

    @Column(name = "ATC7V6")
    var atc7v6: LocalDate? = null

    @Column(name = "ATC7V7")
    var atc7v7: String? = null

    @Column(name = "ATC7V8")
    var atc7v8: String? = null

    @Column(name = "ATC7V9")
    var atc7v9: String? = null

    @Column(name = "ATC7VA")
    var atc7va: String? = null

    @Column(name = "ATC7VB")
    var atc7vb: String? = null

    @Column(name = "ATC7VC")
    var atc7vc: String? = null

    @Column(name = "ATC7VD")
    var atc7vd: String? = null

    @Column(name = "ATC7VE")
    var atc7ve: String? = null

    @Column(name = "ATC7VF")
    var atc7vf: String? = null

    @Column(name = "ATC7VG")
    var atc7vg: String? = null

    @Column(name = "ATC7VH")
    var atc7vh: LocalDate? = null

    @Column(name = "ATC7VI")
    var atc7vi: String? = null

    @Column(name = "ATC7VJ")
    var atc7vj: LocalDate? = null

    @Column(name = "ATC7VK")
    var atc7vk: String? = null

    @Column(name = "AAE013")
    var aae013: String? = null

    @Column(name = "ATC70I")
    var atc70i: String? = null

    @Column(name = "ATC7VL")
    var atc7vl: LocalDate? = null

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

    @Column(name = "BAE011")
    var bae011: String? = null

    @Column(name = "BAE017")
    var bae017: String? = null

    @Column(name = "BAE036")
    var bae036: LocalDate? = null

    @Column(name = "BAE019")
    var bae019: String? = null

    @Column(name = "BAE020")
    var bae020: String? = null

    @Column(name = "BAE022")
    var bae022: String? = null

    @Column(name = "CAE011")
    var cae011: String? = null

    @Column(name = "CAE017")
    var cae017: String? = null

    @Column(name = "CAE036")
    var cae036: LocalDate? = null

    @Column(name = "CAE019")
    var cae019: String? = null

    @Column(name = "CAE020")
    var cae020: String? = null

    @Column(name = "CAE022")
    var cae022: String? = null

}
