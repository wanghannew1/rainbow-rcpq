package com.jlrc.rcpq.entity

import jakarta.persistence.*
import java.io.Serializable
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "TC15")
open class TC15 {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_TC15_ATC150")
    @SequenceGenerator(name = "SEQ_TC15_ATC150", sequenceName = "SEQ_TC15_ATC150", allocationSize = 1)
    @Column(name = "ATC150")
    var atc150: Long? = null

    @Column(name = "BAZ001")
    var baz001: Long? = null

    @Column(name = "BAZ002")
    var baz002: Long? = null

    @Column(name = "AAC001")
    var aac001: Long? = null

    @Column(name = "AAC002")
    var aac002: String? = null

    @Column(name = "AAC003")
    var aac003: String? = null

    @Column(name = "AAC004")
    var aac004: String? = null

    @Column(name = "AAC005")
    var aac005: String? = null

    @Column(name = "AAC006")
    var aac006: LocalDate? = null

    @Column(name = "ATC151")
    var atc151: String? = null

    @Column(name = "ATC152")
    var atc152: String? = null

    @Column(name = "ATC153")
    var atc153: LocalDate? = null

    @Column(name = "ATC154")
    var atc154: String? = null

    @Column(name = "ATC155")
    var atc155: String? = null

    @Column(name = "ATC156")
    var atc156: BigDecimal? = null

    @Column(name = "ATC157")
    var atc157: String? = null

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
