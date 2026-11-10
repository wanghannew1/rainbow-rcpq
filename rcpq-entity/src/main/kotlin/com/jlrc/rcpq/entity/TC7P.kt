package com.jlrc.rcpq.entity

import jakarta.persistence.*
import java.io.Serializable
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "TC7P")
open class TC7P {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_TC7P_ATC7P0")
    @SequenceGenerator(name = "SEQ_TC7P_ATC7P0", sequenceName = "SEQ_TC7P_ATC7P0", allocationSize = 1)
    @Column(name = "ATC7P0")
    var atc7p0: Long? = null

    @Column(name = "BAZ001")
    var baz001: Long? = null

    @Column(name = "BAZ002")
    var baz002: Long? = null

    @Column(name = "ATC702")
    var atc702: String? = null

    @Column(name = "ATC7P1")
    var atc7p1: String? = null

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

    @Column(name = "ATC7P2")
    var atc7p2: String? = null

    @Column(name = "ATC7P3")
    var atc7p3: BigDecimal? = null

    @Column(name = "ATC7P4")
    var atc7p4: String? = null

    @Column(name = "ATB705")
    var atb705: String? = null

    @Column(name = "ATC7P5")
    var atc7p5: LocalDate? = null

    @Column(name = "ATC7P6")
    var atc7p6: String? = null

    @Column(name = "ATC7P7")
    var atc7p7: String? = null

    @Column(name = "ATC7P8")
    var atc7p8: String? = null

    @Column(name = "ATC7P9")
    var atc7p9: String? = null

    @Column(name = "ATC7PA")
    var atc7pa: String? = null

    @Column(name = "ATC7PB")
    var atc7pb: String? = null

    @Column(name = "ATC7PC")
    var atc7pc: String? = null

    @Column(name = "ATC7PD")
    var atc7pd: String? = null

    @Column(name = "ATC7PE")
    var atc7pe: String? = null

    @Column(name = "ATC7PF")
    var atc7pf: Long? = null

    @Column(name = "ATC7PG")
    var atc7pg: Long? = null

    @Column(name = "ATC7PH")
    var atc7ph: BigDecimal? = null

    @Column(name = "AAE013")
    var aae013: String? = null

    @Column(name = "ATC7PI")
    var atc7pi: LocalDate? = null

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

    @Column(name = "ATC7PJ")
    var atc7pj: String? = null

    @Column(name = "ATC7O0")
    var atc7o0: Long? = null

    @Column(name = "ATC700")
    var atc700: BigDecimal? = null
}
