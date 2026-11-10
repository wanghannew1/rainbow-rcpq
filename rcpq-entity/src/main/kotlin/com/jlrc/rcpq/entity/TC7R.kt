package com.jlrc.rcpq.entity

import jakarta.persistence.*
import java.io.Serializable
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "TC7R")
open class TC7R {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_TC7R_ATC7R0")
    @SequenceGenerator(name = "SEQ_TC7R_ATC7R0", sequenceName = "SEQ_TC7R_ATC7R0", allocationSize = 1)
    @Column(name = "ATC7R0")
    var atc7r0: Long? = null

    @Column(name = "BAZ001")
    var baz001: Long? = null

    @Column(name = "BAZ002")
    var baz002: Long? = null

    @Column(name = "ATC7R1")
    var atc7r1: LocalDate? = null

    @Column(name = "AAC003")
    var aac003: String? = null

    @Column(name = "AAC002")
    var aac002: String? = null

    @Column(name = "AAC004")
    var aac004: String? = null

    @Column(name = "ATC7R2")
    var atc7r2: String? = null

    @Column(name = "ATC7R3")
    var atc7r3: String? = null

    @Column(name = "ATC7O2")
    var atc7o2: String? = null

    @Column(name = "ATC7R4")
    var atc7r4: String? = null

    @Column(name = "ATC7R5")
    var atc7r5: String? = null

    @Column(name = "ATC7R6")
    var atc7r6: String? = null

    @Column(name = "ATC7R7")
    var atc7r7: BigDecimal? = null

    @Column(name = "ATC7R8")
    var atc7r8: String? = null

    @Column(name = "ATC7R9")
    var atc7r9: LocalDate? = null

    @Column(name = "ATC7RA")
    var atc7ra: String? = null

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

    @Column(name = "AAE100")
    var aae100: String? = null

    @Column(name = "AAC006")
    var aac006: LocalDate? = null

    @Column(name = "ATC700")
    var atc700: BigDecimal? = null
}
