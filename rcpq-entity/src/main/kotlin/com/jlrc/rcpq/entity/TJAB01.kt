package com.jlrc.rcpq.entity

import jakarta.persistence.*
import java.io.Serializable
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "TJAB01")
open class TJAB01 {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_TJAB01_TJAB01")
    @SequenceGenerator(name = "SEQ_TJAB01_TJAB01", sequenceName = "SEQ_TJAB01_TJAB01", allocationSize = 1)
    @Column(name = "TJAB01")
    var tjab01: Long? = null

    @Column(name = "BAZ001")
    var baz001: Long? = null

    @Column(name = "BAZ002")
    var baz002: Long? = null

    @Column(name = "ACB330")
    var acb330: Long? = null

    @Column(name = "ACB350")
    var acb350: Long? = null

    @Column(name = "ACB200")
    var acb200: Long? = null

    @Column(name = "AAB001")
    var aab001: Long? = null

    @Column(name = "AAB004")
    var aab004: String? = null

    @Column(name = "ACE210")
    var ace210: Long? = null

    @Column(name = "ACE200")
    var ace200: Long? = null

    @Column(name = "ACE201")
    var ace201: String? = null

    @Column(name = "ACB21R")
    var acb21r: Long? = null

    @Column(name = "AAE013")
    var aae013: String? = null

    @Column(name = "AAE036")
    var aae036: LocalDateTime? = null

    @Column(name = "AAE019")
    var aae019: String? = null

    @Column(name = "AAE011")
    var aae011: String? = null

    @Column(name = "AAE020")
    var aae020: String? = null

    @Column(name = "AAE017")
    var aae017: String? = null

    @Column(name = "AAE022")
    var aae022: String? = null

    @Column(name = "ACB331")
    var acb331: String? = null

    @Column(name = "ACB333")
    var acb333: LocalDate? = null

    @Column(name = "ACB334")
    var acb334: LocalDate? = null

    @Column(name = "ACB335")
    var acb335: String? = null

}
