package com.jlrc.rcpq.entity

import jakarta.persistence.*
import java.io.Serializable
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "TC8G")
open class TC8G {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_TC8G_ATC8G0")
    @SequenceGenerator(name = "SEQ_TC8G_ATC8G0", sequenceName = "SEQ_TC8G_ATC8G0", allocationSize = 1)
    @Column(name = "ATC8G0")
    var atc8g0: Long? = null

    @Column(name = "ATC8G1")
    var atc8g1: Long? = null

    @Column(name = "ATC930")
    var atc930: Long? = null

    @Column(name = "ATB930")
    var atb930: Long? = null

    @Column(name = "AAB001")
    var aab001: Long? = null

    @Column(name = "AAB004")
    var aab004: String? = null

    @Column(name = "AAC001")
    var aac001: Long? = null

    @Column(name = "ATC931")
    var atc931: Long? = null

    @Column(name = "ATC932")
    var atc932: Long? = null

    @Column(name = "ATC937")
    var atc937: String? = null

    @Column(name = "AAC003")
    var aac003: String? = null

    @Column(name = "AAC002")
    var aac002: String? = null

    @Column(name = "BBB001")
    var bbb001: String? = null

    @Column(name = "BBB002")
    var bbb002: String? = null

    @Column(name = "ATC8G2")
    var atc8g2: BigDecimal? = null

    @Column(name = "AAE013")
    var aae013: String? = null

    @Column(name = "ATC8G3")
    var atc8g3: BigDecimal? = null

    @Column(name = "ATC8G4")
    var atc8g4: String? = null

    @Column(name = "ATC8G5")
    var atc8g5: String? = null

    @Column(name = "ATC8G6")
    var atc8g6: BigDecimal? = null

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

    @Column(name = "ATC8G7")
    var atc8g7: Long? = null

    @Column(name = "ATB90AB")
    var atb90ab: Long? = null

    @Column(name = "ATC8G8")
    var atc8g8: String? = null

    @Column(name = "ATB931")
    var atb931: String? = null

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

    @Column(name = "ATC93U")
    var atc93u: String? = null

    @Column(name = "ATC900")
    var atc900: Long? = null

    @Column(name = "DC")
    var dc: String? = null

    @Column(name = "DC2")
    var dc2: String? = null

    @Column(name = "AAB00E")
    var aab00e: String? = null

}
