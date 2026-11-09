package com.jlrc.rcpq.entity

import jakarta.persistence.*
import java.io.Serializable
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "AT70")
open class AT70 {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_AT70_AAT700")
    @SequenceGenerator(name = "SEQ_AT70_AAT700", sequenceName = "SEQ_AT70_AAT700", allocationSize = 1)
    @Column(name = "AAT700")
    var aat700: Long? = null

    @Column(name = "BAZ001")
    var baz001: Long? = null

    @Column(name = "BAZ002")
    var baz002: Long? = null

    @Column(name = "AAT701")
    var aat701: LocalDate? = null

    @Column(name = "AAT702")
    var aat702: LocalDate? = null

    @Column(name = "AAT703")
    var aat703: BigDecimal? = null

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

    @Column(name = "ATC7Z1")
    var atc7z1: String? = null

    @Column(name = "AAT704")
    var aat704: Long? = null

}
