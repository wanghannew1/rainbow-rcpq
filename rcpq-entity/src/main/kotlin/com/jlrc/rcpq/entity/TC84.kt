package com.jlrc.rcpq.entity

import jakarta.persistence.*
import java.io.Serializable
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "TC84")
open class TC84 {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_TC84_ATC840")
    @SequenceGenerator(name = "SEQ_TC84_ATC840", sequenceName = "SEQ_TC84_ATC840", allocationSize = 1)
    @Column(name = "ATC840")
    var atc840: Long? = null

    @Column(name = "AAB001")
    var aab001: Long? = null

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

    @Column(name = "ATC012")
    var atc012: String? = null

    @Column(name = "AAC024")
    var aac024: String? = null

    @Column(name = "ATC841")
    var atc841: String? = null

    @Column(name = "AAC007")
    var aac007: LocalDate? = null

    @Column(name = "AAC045")
    var aac045: String? = null

    @Column(name = "ACC01G")
    var acc01g: String? = null

    @Column(name = "ATC011")
    var atc011: String? = null

    @Column(name = "AAB004")
    var aab004: String? = null

    @Column(name = "AAB019")
    var aab019: String? = null

    @Column(name = "AAB020")
    var aab020: String? = null

    @Column(name = "ATC845")
    var atc845: String? = null

    @Column(name = "ATC846")
    var atc846: String? = null

    @Column(name = "ATC847")
    var atc847: LocalDate? = null

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

    @Column(name = "BAZ001")
    var baz001: Long? = null

    @Column(name = "BAZ002")
    var baz002: Long? = null

    @Column(name = "ATC848")
    var atc848: String? = null

    @Column(name = "AAB301")
    var aab301: String? = null

    @Column(name = "AAA021")
    var aaa021: String? = null

    @Column(name = "ATC849")
    var atc849: String? = null

    @Column(name = "ATC84A")
    var atc84a: String? = null

    @Column(name = "ATC84B")
    var atc84b: LocalDate? = null

    @Column(name = "ATC84C")
    var atc84c: String? = null

}
