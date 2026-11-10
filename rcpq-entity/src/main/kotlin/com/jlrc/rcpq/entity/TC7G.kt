package com.jlrc.rcpq.entity

import jakarta.persistence.*
import java.io.Serializable
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "TC7G")
open class TC7G {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_TC7G_ATC7G0")
    @SequenceGenerator(name = "SEQ_TC7G_ATC7G0", sequenceName = "SEQ_TC7G_ATC7G0", allocationSize = 1)
    @Column(name = "ATC7G0")
    var atc7g0: Long? = null

    @Column(name = "BAZ001")
    var baz001: Long? = null

    @Column(name = "BAZ002")
    var baz002: Long? = null

    @Column(name = "ATC7G1")
    var atc7g1: LocalDate? = null

    @Column(name = "ATC7G2")
    var atc7g2: String? = null

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

    @Column(name = "ATC7G3")
    var atc7g3: LocalDate? = null

    @Column(name = "ATC7G4")
    var atc7g4: String? = null

    @Column(name = "AAE111")
    var aae111: String? = null

    @Column(name = "AAE117")
    var aae117: String? = null

    @Column(name = "AAE136")
    var aae136: LocalDate? = null

    @Column(name = "AAE119")
    var aae119: String? = null

    @Column(name = "AAE120")
    var aae120: String? = null

    @Column(name = "AAE122")
    var aae122: String? = null

    @Column(name = "ATC7G5")
    var atc7g5: String? = null

    @Column(name = "ATC7G6")
    var atc7g6: String? = null

    @Column(name = "ATC700")
    var atc700: BigDecimal? = null
}
