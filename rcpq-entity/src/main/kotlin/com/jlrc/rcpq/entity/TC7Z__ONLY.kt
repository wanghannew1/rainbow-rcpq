package com.jlrc.rcpq.entity

import jakarta.persistence.*
import java.io.Serializable
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "TC7Z")
open class TC7Z__ONLY {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_TC7Z_ATC7Z0")
    @SequenceGenerator(name = "SEQ_TC7Z_ATC7Z0", sequenceName = "SEQ_TC7Z_ATC7Z0", allocationSize = 1)
    @Column(name = "ATC7Z0")
    var atc7z0: Long? = null

    @Column(name = "BAZ001")
    var baz001: Long? = null

    @Column(name = "BAZ002")
    var baz002: Long? = null

    @Column(name = "ATC7Z1")
    var atc7z1: String? = null

    @Column(name = "ATB7Z0")
    var atb7z0: Long? = null

    @Column(name = "ATC7Z2")
    var atc7z2: String? = null

    @Column(name = "ATC7Z3")
    var atc7z3: LocalDate? = null

    @Column(name = "ATC7Z4")
    var atc7z4: LocalDate? = null

    @Column(name = "ATC7Z5")
    var atc7z5: BigDecimal? = null

    @Column(name = "ATC7Z6")
    var atc7z6: String? = null

    @Column(name = "ATC7Z7")
    var atc7z7: Long? = null

    @Column(name = "ATC7Z8")
    var atc7z8: BigDecimal? = null

    @Column(name = "ATC7Z9")
    var atc7z9: LocalDate? = null

    @Column(name = "ATC7ZA")
    var atc7za: String? = null

    @Column(name = "ATC7ZB")
    var atc7zb: LocalDate? = null

    @Column(name = "ATC7ZC")
    var atc7zc: String? = null

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

    @Column(name = "AAEA11")
    var aaea11: String? = null

    @Column(name = "AAEA17")
    var aaea17: String? = null

    @Column(name = "AAEA36")
    var aaea36: LocalDate? = null

    @Column(name = "AAEA19")
    var aaea19: String? = null

    @Column(name = "AAEA20")
    var aaea20: String? = null

    @Column(name = "AAEA22")
    var aaea22: String? = null

    @Column(name = "ATC700")
    var atc700: Long? = null

    @Column(name = "ATC7ZZ")
    var atc7zz: String? = null

    @Column(name = "ATC711")
    var atc711: BigDecimal? = null

    @Column(name = "ATC712")
    var atc712: String? = null

}
