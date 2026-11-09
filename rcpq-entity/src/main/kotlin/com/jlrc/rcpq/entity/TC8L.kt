package com.jlrc.rcpq.entity

import jakarta.persistence.*
import java.io.Serializable
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "TC8L")
open class TC8L {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_TC8L_ATC8L0")
    @SequenceGenerator(name = "SEQ_TC8L_ATC8L0", sequenceName = "SEQ_TC8L_ATC8L0", allocationSize = 1)
    @Column(name = "ATC8L0")
    var atc8l0: Long? = null

    @Column(name = "ATC9Q0")
    var atc9q0: Long? = null

    @Column(name = "AAC001")
    var aac001: Long? = null

    @Column(name = "AAC003")
    var aac003: String? = null

    @Column(name = "AAC002")
    var aac002: String? = null

    @Column(name = "AAB001")
    var aab001: Long? = null

    @Column(name = "AAB004")
    var aab004: String? = null

    @Column(name = "BBB001")
    var bbb001: String? = null

    @Column(name = "BBB002")
    var bbb002: String? = null

    @Column(name = "ATC9Q1")
    var atc9q1: String? = null

    @Column(name = "ATC8L1")
    var atc8l1: BigDecimal? = null

    @Column(name = "AAE013")
    var aae013: String? = null

    @Column(name = "ATC8L2")
    var atc8l2: BigDecimal? = null

    @Column(name = "ATC8G4")
    var atc8g4: String? = null

    @Column(name = "ATC8G5")
    var atc8g5: String? = null

    @Column(name = "ATC8L3")
    var atc8l3: BigDecimal? = null

    @Column(name = "ATC9QA")
    var atc9qa: LocalDate? = null

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

    @Column(name = "ZH")
    var zh: String? = null

    @Column(name = "MC")
    var mc: String? = null

    @Column(name = "ATB931")
    var atb931: String? = null

    @Column(name = "ATC8G7")
    var atc8g7: Long? = null

    @Column(name = "ATC8G9")
    var atc8g9: LocalDate? = null

    @Column(name = "ATC8GA")
    var atc8ga: LocalDate? = null

    @Column(name = "ATC8GB")
    var atc8gb: String? = null

    @Column(name = "ATC8GC")
    var atc8gc: String? = null

    @Column(name = "ATC8GD")
    var atc8gd: LocalDate? = null

    @Column(name = "BATCH")
    var batch: Long? = null

    @Column(name = "KEYS")
    var keys: String? = null

    @Column(name = "DC")
    var dc: String? = null

    @Column(name = "DCTIME")
    var dctime: LocalDate? = null

    @Column(name = "DC2")
    var dc2: String? = null

    @Column(name = "DC2TIME")
    var dc2time: LocalDate? = null

}
