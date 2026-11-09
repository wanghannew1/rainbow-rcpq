package com.jlrc.rcpq.entity

import jakarta.persistence.*
import java.io.Serializable
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "TC8U_CS")
open class TC8U_CS {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_TC8U_CS_ID")
    @SequenceGenerator(name = "SEQ_TC8U_CS_ID", sequenceName = "SEQ_TC8U_CS_ID", allocationSize = 1)
    @Column(name = "ID")
    var id: Long? = null

    @Column(name = "ATB930")
    var atb930: Long? = null

    @Column(name = "ATB931")
    var atb931: String? = null

    @Column(name = "AAB001")
    var aab001: Long? = null

    @Column(name = "AAB004")
    var aab004: String? = null

    @Column(name = "MONTH_BEG")
    var month_beg: Long? = null

    @Column(name = "MONTH_END")
    var month_end: Long? = null

    @Column(name = "AAE002")
    var aae002: Long? = null

    @Column(name = "ATC933")
    var atc933: BigDecimal? = null

    @Column(name = "ATJ022")
    var atj022: BigDecimal? = null

    @Column(name = "BAA001")
    var baa001: BigDecimal? = null

    @Column(name = "BBA001")
    var bba001: BigDecimal? = null

    @Column(name = "BAA003")
    var baa003: BigDecimal? = null

    @Column(name = "BBA003")
    var bba003: BigDecimal? = null

    @Column(name = "BAA002")
    var baa002: BigDecimal? = null

    @Column(name = "BBA002")
    var bba002: BigDecimal? = null

    @Column(name = "BBA005")
    var bba005: BigDecimal? = null

    @Column(name = "BBA004")
    var bba004: BigDecimal? = null

    @Column(name = "CAA002")
    var caa002: BigDecimal? = null

    @Column(name = "CAA001")
    var caa001: BigDecimal? = null

    @Column(name = "ATJ023")
    var atj023: BigDecimal? = null

    @Column(name = "ATJ024")
    var atj024: BigDecimal? = null

    @Column(name = "ATJ025")
    var atj025: BigDecimal? = null

    @Column(name = "ATJ026")
    var atj026: BigDecimal? = null

    @Column(name = "ATJ027")
    var atj027: BigDecimal? = null

    @Column(name = "ATC93S")
    var atc93s: BigDecimal? = null

    @Column(name = "ATB90Z")
    var atb90z: BigDecimal? = null

    @Column(name = "ATJ028")
    var atj028: BigDecimal? = null

    @Column(name = "ATJ029")
    var atj029: BigDecimal? = null

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

    @Column(name = "ATC9P5")
    var atc9p5: BigDecimal? = null

    @Column(name = "AAE013")
    var aae013: String? = null

    @Column(name = "FLAG")
    var flag: String? = null

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

    @Column(name = "ATC8U9")
    var atc8u9: String? = null

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

    @Column(name = "JINESUM")
    var jinesum: BigDecimal? = null

}
