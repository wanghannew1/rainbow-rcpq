package com.jlrc.rcpq.entity

import jakarta.persistence.*
import java.io.Serializable
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "TC94")
open class TC94 {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_TC94_ATC940")
    @SequenceGenerator(name = "SEQ_TC94_ATC940", sequenceName = "SEQ_TC94_ATC940", allocationSize = 1)
    @Column(name = "ATC940")
    var atc940: Long? = null

    @Column(name = "BAZ001")
    var baz001: Long? = null

    @Column(name = "BAZ002")
    var baz002: Long? = null

    @Column(name = "ATC930")
    var atc930: Long? = null

    @Column(name = "AAA901")
    var aaa901: String? = null

    @Column(name = "AAA902")
    var aaa902: String? = null

    @Column(name = "ATC941")
    var atc941: BigDecimal? = null

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

    @Column(name = "AAA900")
    var aaa900: Long? = null

    @Column(name = "ATC931")
    var atc931: Long? = null

    @Column(name = "ATC932")
    var atc932: Long? = null

    @Column(name = "ATC937")
    var atc937: String? = null

    @Column(name = "ATB930")
    var atb930: Long? = null

}
