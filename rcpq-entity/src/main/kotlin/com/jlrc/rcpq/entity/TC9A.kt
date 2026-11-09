package com.jlrc.rcpq.entity

import jakarta.persistence.*
import java.io.Serializable
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "TC9A")
open class TC9A {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_TC9A_ATC9A0")
    @SequenceGenerator(name = "SEQ_TC9A_ATC9A0", sequenceName = "SEQ_TC9A_ATC9A0", allocationSize = 1)
    @Column(name = "ATC9A0")
    var atc9a0: Long? = null

    @Column(name = "BAZ001")
    var baz001: Long? = null

    @Column(name = "BAZ002")
    var baz002: Long? = null

    @Column(name = "ATC970")
    var atc970: Long? = null

    @Column(name = "ATC9A1")
    var atc9a1: String? = null

    @Column(name = "ATC9A2")
    var atc9a2: LocalDate? = null

    @Column(name = "AAC011")
    var aac011: String? = null

    @Column(name = "ATC9A3")
    var atc9a3: String? = null

    @Column(name = "ATC9A4")
    var atc9a4: Long? = null

    @Column(name = "ATC9A5")
    var atc9a5: String? = null

    @Column(name = "ATC9A6")
    var atc9a6: String? = null

    @Column(name = "ATC9A7")
    var atc9a7: String? = null

    @Column(name = "ATC9A8")
    var atc9a8: String? = null

    @Column(name = "ATC9A9")
    var atc9a9: LocalDate? = null

    @Column(name = "ATC9AA")
    var atc9aa: String? = null

    @Column(name = "ATC9AB")
    var atc9ab: String? = null

    @Column(name = "ATC9AC")
    var atc9ac: LocalDate? = null

    @Column(name = "ATC9AD")
    var atc9ad: String? = null

    @Column(name = "ATC9AE")
    var atc9ae: Long? = null

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

    @Column(name = "AAC002")
    var aac002: String? = null

    @Column(name = "AAC003")
    var aac003: String? = null

    @Column(name = "AAC001")
    var aac001: String? = null

    @Column(name = "AAB001")
    var aab001: Long? = null

    @Column(name = "AAB004")
    var aab004: String? = null

    @Column(name = "AAB001_1")
    var aab001_1: Long? = null

    @Column(name = "AAB004_1")
    var aab004_1: String? = null

}
