package com.jlrc.rcpq.entity

import jakarta.persistence.*
import java.io.Serializable
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "TJ202")
open class TJ202 {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_TJ202_TJ2020")
    @SequenceGenerator(name = "SEQ_TJ202_TJ2020", sequenceName = "SEQ_TJ202_TJ2020", allocationSize = 1)
    @Column(name = "TJ2020")
    var tj2020: Long? = null

    @Column(name = "BAZ001")
    var baz001: Long? = null

    @Column(name = "BAZ002")
    var baz002: Long? = null

    @Column(name = "AAA020")
    var aaa020: String? = null

    @Column(name = "YEAR")
    var year: String? = null

    @Column(name = "MONTH")
    var month: String? = null

    @Column(name = "CC2000")
    var cc2000: String? = null

    @Column(name = "TJ2021")
    var tj2021: String? = null

    @Column(name = "TJ2022")
    var tj2022: String? = null

    @Column(name = "TJ2023")
    var tj2023: String? = null

    @Column(name = "TJ2024")
    var tj2024: String? = null

    @Column(name = "TJ2025")
    var tj2025: String? = null

    @Column(name = "TJ2026")
    var tj2026: String? = null

    @Column(name = "TJ2027")
    var tj2027: String? = null

    @Column(name = "TJ2028")
    var tj2028: String? = null

    @Column(name = "TJ2029")
    var tj2029: String? = null

    @Column(name = "TJ202A")
    var tj202a: BigDecimal? = null

    @Column(name = "TJ202B")
    var tj202b: BigDecimal? = null

    @Column(name = "TJ202C")
    var tj202c: BigDecimal? = null

    @Column(name = "TJ202D")
    var tj202d: BigDecimal? = null

    @Column(name = "TJ202E")
    var tj202e: BigDecimal? = null

    @Column(name = "TJ202F")
    var tj202f: BigDecimal? = null

    @Column(name = "TJ202G")
    var tj202g: BigDecimal? = null

    @Column(name = "TJ202H")
    var tj202h: String? = null

    @Column(name = "TJ202I")
    var tj202i: String? = null

    @Column(name = "TJ202J")
    var tj202j: String? = null

    @Column(name = "TJ202K")
    var tj202k: String? = null

    @Column(name = "TJ202L")
    var tj202l: String? = null

    @Column(name = "TJ202M")
    var tj202m: String? = null

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

}
