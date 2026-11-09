package com.jlrc.rcpq.entity

import jakarta.persistence.*
import java.io.Serializable
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "TC40")
open class TC40 {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_TC40_ATC400")
    @SequenceGenerator(name = "SEQ_TC40_ATC400", sequenceName = "SEQ_TC40_ATC400", allocationSize = 1)
    @Column(name = "ATC400")
    var atc400: Long? = null

    @Column(name = "BAZ001")
    var baz001: Long? = null

    @Column(name = "BAZ002")
    var baz002: Long? = null

    @Column(name = "ATC900")
    var atc900: Long? = null

    @Column(name = "ATB930")
    var atb930: Long? = null

    @Column(name = "ATB931")
    var atb931: String? = null

    @Column(name = "ATB900")
    var atb900: Long? = null

    @Column(name = "AAB001")
    var aab001: Long? = null

    @Column(name = "AAB004")
    var aab004: String? = null

    @Column(name = "AAA030")
    var aaa030: Long? = null

    @Column(name = "AAA031")
    var aaa031: String? = null

    @Column(name = "AAC001")
    var aac001: Long? = null

    @Column(name = "AAC003")
    var aac003: String? = null

    @Column(name = "AAC002")
    var aac002: String? = null

    @Column(name = "AAC004")
    var aac004: String? = null

    @Column(name = "ATC401")
    var atc401: BigDecimal? = null

    @Column(name = "ATC402")
    var atc402: BigDecimal? = null

    @Column(name = "ATC403")
    var atc403: BigDecimal? = null

    @Column(name = "ATC404")
    var atc404: BigDecimal? = null

    @Column(name = "ATC405")
    var atc405: BigDecimal? = null

    @Column(name = "ATC406")
    var atc406: BigDecimal? = null

    @Column(name = "ATC407")
    var atc407: BigDecimal? = null

    @Column(name = "ATC408")
    var atc408: BigDecimal? = null

    @Column(name = "ATC409")
    var atc409: BigDecimal? = null

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

}
