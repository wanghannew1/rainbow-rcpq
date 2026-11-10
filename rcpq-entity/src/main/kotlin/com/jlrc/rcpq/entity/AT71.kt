package com.jlrc.rcpq.entity

import jakarta.persistence.*
import java.io.Serializable
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "AT71")
open class AT71 {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_AT71_AAT710")
    @SequenceGenerator(name = "SEQ_AT71_AAT710", sequenceName = "SEQ_AT71_AAT710", allocationSize = 1)
    @Column(name = "AAT710")
    var aat710: Long? = null

    @Column(name = "BAZ001")
    var baz001: Long? = null

    @Column(name = "BAZ002")
    var baz002: Long? = null

    @Column(name = "ATC711")
    var atc711: BigDecimal? = null

    @Column(name = "ATC712")
    var atc712: LocalDate? = null

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

    @Column(name = "ATC713")
    var atc713: String? = null

    @Column(name = "ATC700")
    var atc700: BigDecimal? = null
}
