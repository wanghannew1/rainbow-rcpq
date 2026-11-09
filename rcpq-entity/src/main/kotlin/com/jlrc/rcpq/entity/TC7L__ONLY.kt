package com.jlrc.rcpq.entity

import jakarta.persistence.*
import java.io.Serializable
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "TC7L")
open class TC7L__ONLY {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_TC7L_ATC7L0")
    @SequenceGenerator(name = "SEQ_TC7L_ATC7L0", sequenceName = "SEQ_TC7L_ATC7L0", allocationSize = 1)
    @Column(name = "ATC7L0")
    var atc7l0: Long? = null

    @Column(name = "BAZ001")
    var baz001: Long? = null

    @Column(name = "BAZ002")
    var baz002: Long? = null

    @Column(name = "ATC700")
    var atc700: Long? = null

    @Column(name = "ATC7L1")
    var atc7l1: String? = null

    @Column(name = "ATC7L2")
    var atc7l2: String? = null

    @Column(name = "ATC7L3")
    var atc7l3: String? = null

    @Column(name = "ATC7L4")
    var atc7l4: String? = null

    @Column(name = "ATC702")
    var atc702: String? = null

    @Column(name = "ATC7L5")
    var atc7l5: String? = null

    @Column(name = "ATC7L6")
    var atc7l6: String? = null

    @Column(name = "ATC7L7")
    var atc7l7: String? = null

    @Column(name = "AAC003")
    var aac003: String? = null

    @Column(name = "AAC002")
    var aac002: String? = null

    @Column(name = "AAC004")
    var aac004: String? = null

    @Column(name = "AAC005")
    var aac005: String? = null

    @Column(name = "AAC006")
    var aac006: LocalDate? = null

    @Column(name = "ATC7L8")
    var atc7l8: LocalDate? = null

    @Column(name = "ATC7L9")
    var atc7l9: String? = null

    @Column(name = "AAE006")
    var aae006: String? = null

    @Column(name = "AAC025")
    var aac025: String? = null

    @Column(name = "AAC011")
    var aac011: String? = null

    @Column(name = "AAC017")
    var aac017: String? = null

    @Column(name = "ATC7LA")
    var atc7la: LocalDate? = null

    @Column(name = "ATC7LB")
    var atc7lb: LocalDate? = null

    @Column(name = "ATC7LC")
    var atc7lc: String? = null

    @Column(name = "ATC7LD")
    var atc7ld: String? = null

    @Column(name = "AAC034")
    var aac034: BigDecimal? = null

    @Column(name = "AAC032")
    var aac032: String? = null

    @Column(name = "ATC7LE")
    var atc7le: String? = null

    @Column(name = "ATC7LF")
    var atc7lf: String? = null

    @Column(name = "ATC7LG")
    var atc7lg: String? = null

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

    @Column(name = "ATC7LH")
    var atc7lh: String? = null

    @Column(name = "ATC7LI")
    var atc7li: String? = null

    @Column(name = "ATC7LJ")
    var atc7lj: LocalDate? = null

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

}
