package com.jlrc.rcpq.entity

import jakarta.persistence.*
import java.io.Serializable
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "TJ201")
open class TJ201 {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_TJ201_TJ2010")
    @SequenceGenerator(name = "SEQ_TJ201_TJ2010", sequenceName = "SEQ_TJ201_TJ2010", allocationSize = 1)
    @Column(name = "TJ2010")
    var tj2010: Long? = null

    @Column(name = "BAZ001")
    var baz001: Long? = null

    @Column(name = "BAZ002")
    var baz002: Long? = null

    @Column(name = "AAA020")
    var aaa020: String? = null

    @Column(name = "YEAR")
    var year: String? = null

    @Column(name = "QUARTER")
    var quarter: String? = null

    @Column(name = "CC2000")
    var cc2000: String? = null

    @Column(name = "TJ2011")
    var tj2011: String? = null

    @Column(name = "TJ2012")
    var tj2012: String? = null

    @Column(name = "TJ2013")
    var tj2013: String? = null

    @Column(name = "TJ2014")
    var tj2014: String? = null

    @Column(name = "TJ2015")
    var tj2015: String? = null

    @Column(name = "TJ2016")
    var tj2016: String? = null

    @Column(name = "TJ2017")
    var tj2017: String? = null

    @Column(name = "TJ2018")
    var tj2018: String? = null

    @Column(name = "TJ2019")
    var tj2019: String? = null

    @Column(name = "TJ201A")
    var tj201a: BigDecimal? = null

    @Column(name = "TJ201B")
    var tj201b: BigDecimal? = null

    @Column(name = "TJ201C")
    var tj201c: BigDecimal? = null

    @Column(name = "TJ201D")
    var tj201d: BigDecimal? = null

    @Column(name = "TJ201E")
    var tj201e: BigDecimal? = null

    @Column(name = "TJ201F")
    var tj201f: BigDecimal? = null

    @Column(name = "TJ201G")
    var tj201g: BigDecimal? = null

    @Column(name = "TJ201H")
    var tj201h: BigDecimal? = null

    @Column(name = "TJ201I")
    var tj201i: BigDecimal? = null

    @Column(name = "TJ201J")
    var tj201j: BigDecimal? = null

    @Column(name = "TJ201K")
    var tj201k: BigDecimal? = null

    @Column(name = "TJ201L")
    var tj201l: BigDecimal? = null

    @Column(name = "TJ201M")
    var tj201m: BigDecimal? = null

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
