package com.jlrc.rcpq.entity

import jakarta.persistence.*
import java.io.Serializable
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "TC8U")
open class TC8U {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_TC8U_ATC8U0")
    @SequenceGenerator(name = "SEQ_TC8U_ATC8U0", sequenceName = "SEQ_TC8U_ATC8U0", allocationSize = 1)
    @Column(name = "ATC8U0")
    var atc8u0: Long? = null

    @Column(name = "ATB930")
    var atb930: Long? = null

    @Column(name = "ATB931")
    var atb931: String? = null

    @Column(name = "AAB001")
    var aab001: Long? = null

    @Column(name = "AAB004")
    var aab004: String? = null

    @Column(name = "ATB934")
    var atb934: String? = null

    @Column(name = "ATB90AC")
    var atb90ac: String? = null

    @Column(name = "ATC931")
    var atc931: Long? = null

    @Column(name = "ATC932")
    var atc932: Long? = null

    @Column(name = "ATC937")
    var atc937: String? = null

    @Column(name = "AAE002")
    var aae002: Long? = null

    @Column(name = "ATJ022")
    var atj022: BigDecimal? = null

    @Column(name = "CAA002")
    var caa002: BigDecimal? = null

    @Column(name = "CAA001")
    var caa001: BigDecimal? = null

    @Column(name = "BBA000")
    var bba000: BigDecimal? = null

    @Column(name = "BAA000")
    var baa000: BigDecimal? = null

    @Column(name = "BBA100")
    var bba100: BigDecimal? = null

    @Column(name = "BAA100")
    var baa100: BigDecimal? = null

    @Column(name = "CAA101")
    var caa101: BigDecimal? = null

    @Column(name = "CAA102")
    var caa102: BigDecimal? = null

    @Column(name = "ATB90Z")
    var atb90z: BigDecimal? = null

    @Column(name = "ATJ028")
    var atj028: BigDecimal? = null

    @Column(name = "ATC9P5")
    var atc9p5: BigDecimal? = null

    @Column(name = "ATJ029")
    var atj029: BigDecimal? = null

    @Column(name = "ATC8U1")
    var atc8u1: String? = null

    @Column(name = "ATC93U")
    var atc93u: String? = null

    @Column(name = "ATC8U2")
    var atc8u2: LocalDate? = null

    @Column(name = "ATC8U3")
    var atc8u3: BigDecimal? = null

    @Column(name = "ATC8U4")
    var atc8u4: BigDecimal? = null

    @Column(name = "ATC8U5")
    var atc8u5: String? = null

    @Column(name = "ATC8U6")
    var atc8u6: LocalDate? = null

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

    @Column(name = "ATC8U7")
    var atc8u7: String? = null

    @Column(name = "ATJ025")
    var atj025: BigDecimal? = null

    @Column(name = "ATC8U8")
    var atc8u8: String? = null

    @Column(name = "ATC8U9")
    var atc8u9: String? = null

}
