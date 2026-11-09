package com.jlrc.rcpq.entity

import jakarta.persistence.*
import java.io.Serializable
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "TC8G_BP")
open class TC8G_BP {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_TC8G_ATC8G0")
    @SequenceGenerator(name = "SEQ_TC8G_ATC8G0", sequenceName = "SEQ_TC8G_ATC8G0", allocationSize = 1)
    @Column(name = "ATC8G0")
    var atc8g0: Long? = null

    @Column(name = "ATC8G1")
    var atc8g1: String? = null

    @Column(name = "ATC8G2")
    var atc8g2: String? = null

    @Column(name = "ATC8G3")
    var atc8g3: String? = null

    @Column(name = "ATC8G4")
    var atc8g4: String? = null

    @Column(name = "ATC8G5")
    var atc8g5: String? = null

    @Column(name = "ATC8G6")
    var atc8g6: String? = null

    @Column(name = "ATC8G7")
    var atc8g7: String? = null

    @Column(name = "ATC8G8")
    var atc8g8: String? = null

    @Column(name = "ATC8G9")
    var atc8g9: String? = null

    @Column(name = "ATC8GA")
    var atc8ga: String? = null

    @Column(name = "ACE10A")
    var ace10a: String? = null

    @Column(name = "ACE10B")
    var ace10b: String? = null

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

    @Column(name = "ATC8G0_1")
    var atc8g0_1: Long? = null

    @Column(name = "ATC931")
    var atc931: Long? = null

    @Column(name = "ATC937")
    var atc937: String? = null

    @Column(name = "ACE100")
    var ace100: Long? = null

    @Column(name = "BBB002")
    var bbb002: String? = null

}
