package com.jlrc.rcpq.entity

import jakarta.persistence.*
import java.io.Serializable
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "TC8M")
open class TC8M {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_TC8M_ATC8M0")
    @SequenceGenerator(name = "SEQ_TC8M_ATC8M0", sequenceName = "SEQ_TC8M_ATC8M0", allocationSize = 1)
    @Column(name = "ATC8M0")
    var atc8m0: Long? = null

    @Column(name = "BAZ002")
    var baz002: Long? = null

    @Column(name = "ATB930")
    var atb930: Long? = null

    @Column(name = "ATB931")
    var atb931: String? = null

    @Column(name = "AAA030")
    var aaa030: Long? = null

    @Column(name = "AAA031")
    var aaa031: String? = null

    @Column(name = "AAB001")
    var aab001: Long? = null

    @Column(name = "AAB004")
    var aab004: String? = null

    @Column(name = "BBB002")
    var bbb002: String? = null

    @Column(name = "ATB90AB")
    var atb90ab: Long? = null

    @Column(name = "ATC8G7")
    var atc8g7: Long? = null

    @Column(name = "ATC931")
    var atc931: Long? = null

    @Column(name = "ISEMPTY")
    var isempty: Long? = null

    @Column(name = "AAB00E")
    var aab00e: String? = null

    @Column(name = "ATC937")
    var atc937: String? = null

    @Column(name = "ATC8G1")
    var atc8g1: String? = null

    @Column(name = "ATC8M1")
    var atc8m1: Long? = null

    @Column(name = "ATC8M2")
    var atc8m2: BigDecimal? = null

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

    @Column(name = "ATC8M3")
    var atc8m3: String? = null

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

    @Column(name = "AAE211")
    var aae211: String? = null

    @Column(name = "AAE217")
    var aae217: String? = null

    @Column(name = "AAE236")
    var aae236: LocalDate? = null

    @Column(name = "AAE219")
    var aae219: String? = null

    @Column(name = "AAE220")
    var aae220: String? = null

    @Column(name = "AAE222")
    var aae222: String? = null

    @Column(name = "ATC8M4")
    var atc8m4: String? = null

    @Column(name = "ATC8M5")
    var atc8m5: Long? = null

}
