package com.jlrc.rcpq.entity

import jakarta.persistence.*
import java.io.Serializable
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "AA70")
open class AA70 {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_AA70_AAA700")
    @SequenceGenerator(name = "SEQ_AA70_AAA700", sequenceName = "SEQ_AA70_AAA700", allocationSize = 1)
    @Column(name = "AAA700")
    var aaa700: Long? = null

    @Column(name = "AAA701")
    var aaa701: Long? = null

    @Column(name = "AAA702")
    var aaa702: Long? = null

    @Column(name = "AAE011")
    var aae011: String? = null

    @Column(name = "AAE017")
    var aae017: String? = null

    @Column(name = "AAE036")
    var aae036: LocalDateTime? = null

    @Column(name = "AAA703")
    var aaa703: BigDecimal? = null

    @Column(name = "AAA704")
    var aaa704: BigDecimal? = null

    @Column(name = "AAE019")
    var aae019: String? = null

    @Column(name = "AAE020")
    var aae020: String? = null

    @Column(name = "AAE022")
    var aae022: String? = null

    @Column(name = "BAZ001")
    var baz001: Long? = null

    @Column(name = "BAZ002")
    var baz002: Long? = null

}
