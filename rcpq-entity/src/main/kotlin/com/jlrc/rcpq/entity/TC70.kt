package com.jlrc.rcpq.entity

import jakarta.persistence.*
import java.io.Serializable
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "TC70")
open class TC70 {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_TC70_ATC700")
    @SequenceGenerator(name = "SEQ_TC70_ATC700", sequenceName = "SEQ_TC70_ATC700", allocationSize = 1)
    @Column(name = "ATC700")
    var atc700: Long? = null

    @Column(name = "BAZ001")
    var baz001: Long? = null

    @Column(name = "BAZ002")
    var baz002: Long? = null

    @Column(name = "AAC001")
    var aac001: Long? = null

    @Column(name = "AAC003")
    var aac003: String? = null

    @Column(name = "AAC002")
    var aac002: String? = null

    @Column(name = "ATC701")
    var atc701: String? = null

    @Column(name = "ATC702")
    var atc702: String? = null

    @Column(name = "ATC703")
    var atc703: String? = null

    @Column(name = "ATC704")
    var atc704: LocalDate? = null

    @Column(name = "ATC705")
    var atc705: String? = null

    @Column(name = "ATC706")
    var atc706: String? = null

    @Column(name = "ATB700")
    var atb700: Long? = null

    @Column(name = "AAB001")
    var aab001: Long? = null

    @Column(name = "AAB004")
    var aab004: String? = null

    @Column(name = "ATB702")
    var atb702: String? = null

    @Column(name = "ATC707")
    var atc707: String? = null

    @Column(name = "ATC708")
    var atc708: String? = null

    @Column(name = "ATC709")
    var atc709: String? = null

    @Column(name = "AAE013")
    var aae013: String? = null

    @Column(name = "ATC70A")
    var atc70a: String? = null

    @Column(name = "ATC70B")
    var atc70b: LocalDate? = null

    @Column(name = "ATC70C")
    var atc70c: LocalDate? = null

    @Column(name = "ATC70D")
    var atc70d: String? = null

    @Column(name = "ATC70E")
    var atc70e: String? = null

    @Column(name = "ATC70F")
    var atc70f: String? = null

    @Column(name = "ATC70G")
    var atc70g: String? = null

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

    @Column(name = "AAC004")
    var aac004: String? = null

    @Column(name = "AAC006")
    var aac006: LocalDate? = null

    @Column(name = "AAE005")
    var aae005: String? = null

    @Column(name = "ATC70H")
    var atc70h: String? = null

    @Column(name = "ATC70I")
    var atc70i: String? = null

    @Column(name = "ATC70J")
    var atc70j: String? = null

    @Column(name = "ATC70K")
    var atc70k: String? = null

    @Column(name = "ATC70L")
    var atc70l: String? = null

    @Column(name = "ATC70Z")
    var atc70z: String? = null

    @Column(name = "ATC70M")
    var atc70m: String? = null

    @Column(name = "ATC70P")
    var atc70p: String? = null

    @Column(name = "AAC007")
    var aac007: LocalDate? = null

    @Column(name = "AAC024")
    var aac024: String? = null

}
