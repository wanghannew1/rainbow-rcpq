package com.jlrc.rcpq.entity

import jakarta.persistence.*
import java.io.Serializable
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "TC9J")
open class TC9J {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_TC9J_ATC9J0")
    @SequenceGenerator(name = "SEQ_TC9J_ATC9J0", sequenceName = "SEQ_TC9J_ATC9J0", allocationSize = 1)
    @Column(name = "ATC9J0")
    var atc9j0: Long? = null

    @Column(name = "AAC001")
    var aac001: Long? = null

    @Column(name = "AAC002")
    var aac002: String? = null

    @Column(name = "AAC003")
    var aac003: String? = null

    @Column(name = "AAC004")
    var aac004: String? = null

    @Column(name = "AAC005")
    var aac005: String? = null

    @Column(name = "AAC006")
    var aac006: LocalDate? = null

    @Column(name = "AAC007")
    var aac007: LocalDate? = null

    @Column(name = "AAC011")
    var aac011: String? = null

    @Column(name = "AAC017")
    var aac017: String? = null

    @Column(name = "AAC024")
    var aac024: String? = null

    @Column(name = "AAC181")
    var aac181: LocalDate? = null

    @Column(name = "AAC180")
    var aac180: String? = null

    @Column(name = "AAC182")
    var aac182: String? = null

    @Column(name = "AAE005")
    var aae005: String? = null

    @Column(name = "AAE006")
    var aae006: String? = null

    @Column(name = "AAE007")
    var aae007: String? = null

    @Column(name = "AAE036")
    var aae036: LocalDateTime? = null

    @Column(name = "ACC782")
    var acc782: String? = null

    @Column(name = "ACC783")
    var acc783: String? = null

    @Column(name = "AAB004")
    var aab004: String? = null

    @Column(name = "ACC7JE")
    var acc7je: String? = null

    @Column(name = "ATC971")
    var atc971: String? = null

    @Column(name = "ATC972")
    var atc972: LocalDate? = null

    @Column(name = "AAA001")
    var aaa001: String? = null

    @Column(name = "AAA004")
    var aaa004: String? = null

    @Column(name = "ATC978")
    var atc978: String? = null

    @Column(name = "ATC979")
    var atc979: String? = null

    @Column(name = "BAE019")
    var bae019: String? = null

    @Column(name = "BAE036")
    var bae036: LocalDate? = null

    @Column(name = "AAE011")
    var aae011: String? = null

    @Column(name = "AAE017")
    var aae017: String? = null

    @Column(name = "AAE019")
    var aae019: String? = null

    @Column(name = "AAE020")
    var aae020: String? = null

    @Column(name = "AAE022")
    var aae022: String? = null

    @Column(name = "ATC90C")
    var atc90c: LocalDate? = null

    @Column(name = "ATC90D")
    var atc90d: LocalDate? = null

    @Column(name = "AAE001")
    var aae001: Long? = null

    @Column(name = "AAE001E")
    var aae001e: Long? = null

    @Column(name = "MONEY")
    var money: BigDecimal? = null

    @Column(name = "ATC900")
    var atc900: Long? = null

    @Column(name = "AAB001")
    var aab001: Long? = null

    @Column(name = "ATC90I")
    var atc90i: LocalDate? = null

    @Column(name = "AAB004_1")
    var aab004_1: String? = null

    @Column(name = "ATC90X")
    var atc90x: String? = null

    @Column(name = "ATC904")
    var atc904: Long? = null

    @Column(name = "ATC90B")
    var atc90b: LocalDate? = null

}
