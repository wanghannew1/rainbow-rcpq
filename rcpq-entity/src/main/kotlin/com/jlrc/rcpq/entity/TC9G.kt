package com.jlrc.rcpq.entity

import jakarta.persistence.*
import java.io.Serializable
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "TC9G")
open class TC9G {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_TC9G_ATC9G0")
    @SequenceGenerator(name = "SEQ_TC9G_ATC9G0", sequenceName = "SEQ_TC9G_ATC9G0", allocationSize = 1)
    @Column(name = "ATC9G0")
    var atc9g0: Long? = null

    @Column(name = "ATC970")
    var atc970: Long? = null

    @Column(name = "AAC001")
    var aac001: Long? = null

    @Column(name = "ATC9G1")
    var atc9g1: LocalDate? = null

    @Column(name = "ATC9G2")
    var atc9g2: LocalDate? = null

    @Column(name = "AAB004")
    var aab004: String? = null

    @Column(name = "ATC9G3")
    var atc9g3: LocalDate? = null

    @Column(name = "ATC9G4")
    var atc9g4: String? = null

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

    @Column(name = "ATC97A")
    var atc97a: String? = null

    @Column(name = "AAE112")
    var aae112: String? = null

    @Column(name = "AAE137")
    var aae137: LocalDate? = null

    @Column(name = "AAE121")
    var aae121: String? = null

}
