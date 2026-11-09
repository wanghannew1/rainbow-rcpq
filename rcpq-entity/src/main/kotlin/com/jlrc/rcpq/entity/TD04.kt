package com.jlrc.rcpq.entity

import jakarta.persistence.*
import java.io.Serializable
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "TD04")
open class TD04 {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_TD04_ATD040")
    @SequenceGenerator(name = "SEQ_TD04_ATD040", sequenceName = "SEQ_TD04_ATD040", allocationSize = 1)
    @Column(name = "ATD040")
    var atd040: Long? = null

    @Column(name = "ATC900")
    var atc900: Long? = null

    @Column(name = "AAB001")
    var aab001: Long? = null

    @Column(name = "AAB004")
    var aab004: String? = null

    @Column(name = "ATB930")
    var atb930: Long? = null

    @Column(name = "ATB931")
    var atb931: String? = null

    @Column(name = "ATB90Y")
    var atb90y: String? = null

    @Column(name = "AAC001")
    var aac001: Long? = null

    @Column(name = "AAC003")
    var aac003: String? = null

    @Column(name = "AAC002")
    var aac002: String? = null

    @Column(name = "AAC004")
    var aac004: String? = null

    @Column(name = "ATC90B")
    var atc90b: LocalDate? = null

    @Column(name = "ATC90I")
    var atc90i: LocalDate? = null

    @Column(name = "ATD041")
    var atd041: BigDecimal? = null

    @Column(name = "ATD045")
    var atd045: BigDecimal? = null

    @Column(name = "ATD046")
    var atd046: BigDecimal? = null

    @Column(name = "ATD042")
    var atd042: BigDecimal? = null

    @Column(name = "ATD043")
    var atd043: BigDecimal? = null

    @Column(name = "ATD044")
    var atd044: String? = null

    @Column(name = "AAE002")
    var aae002: Long? = null

    @Column(name = "ATC9P4")
    var atc9p4: String? = null

    @Column(name = "AAE114")
    var aae114: String? = null

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

    @Column(name = "ATB90A")
    var atb90a: BigDecimal? = null

    @Column(name = "ATC93D")
    var atc93d: BigDecimal? = null

    @Column(name = "ATD047")
    var atd047: BigDecimal? = null

    @Column(name = "BBB001")
    var bbb001: String? = null

    @Column(name = "ATD049")
    var atd049: String? = null

    @Column(name = "ATD04A")
    var atd04a: BigDecimal? = null

    @Column(name = "ATD04B")
    var atd04b: String? = null

    @Column(name = "ATD04C")
    var atd04c: String? = null

    @Column(name = "BAZ001")
    var baz001: Long? = null

    @Column(name = "BAZ002")
    var baz002: Long? = null

}
