package com.jlrc.rcpq.entity

import jakarta.persistence.*
import java.io.Serializable
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "TC8H")
open class TC8H {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_TC8H_ATC8H0")
    @SequenceGenerator(name = "SEQ_TC8H_ATC8H0", sequenceName = "SEQ_TC8H_ATC8H0", allocationSize = 1)
    @Column(name = "ATC8H0")
    var atc8h0: Long? = null

    @Column(name = "AAB001")
    var aab001: Long? = null

    @Column(name = "AAB004")
    var aab004: String? = null

    @Column(name = "AAB00A")
    var aab00a: String? = null

    @Column(name = "ATC931")
    var atc931: Long? = null

    @Column(name = "BBA000")
    var bba000: BigDecimal? = null

    @Column(name = "BAA000")
    var baa000: BigDecimal? = null

    @Column(name = "CAA001")
    var caa001: BigDecimal? = null

    @Column(name = "CAA002")
    var caa002: BigDecimal? = null

    @Column(name = "ATC8H1")
    var atc8h1: BigDecimal? = null

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

    @Column(name = "ATC8H2")
    var atc8h2: BigDecimal? = null

    @Column(name = "ATC8H3")
    var atc8h3: String? = null

    @Column(name = "ATC8H4")
    var atc8h4: String? = null

    @Column(name = "ALEVEL")
    var alevel: Long? = null

    @Column(name = "ATB931")
    var atb931: String? = null

    @Column(name = "ATB930")
    var atb930: Long? = null

    @Column(name = "XH")
    var xh: Long? = null

    @Column(name = "ATC8H5")
    var atc8h5: BigDecimal? = null

    @Column(name = "ATC8H6")
    var atc8h6: BigDecimal? = null

}
