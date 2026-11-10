package com.jlrc.rcpq.entity

import jakarta.persistence.*
import java.io.Serializable
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "AA93")
open class AA93 {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_AA93_AAA930")
    @SequenceGenerator(name = "SEQ_AA93_AAA930", sequenceName = "SEQ_AA93_AAA930", allocationSize = 1)
    @Column(name = "AAA930")
    var aaa930: Long? = null

    @Column(name = "BAZ001")
    var baz001: Long? = null

    @Column(name = "BAZ002")
    var baz002: Long? = null

    @Column(name = "AAA931")
    var aaa931: BigDecimal? = null

    @Column(name = "AAA932")
    var aaa932: BigDecimal? = null

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

    @Column(name = "AAA933")
    var aaa933: BigDecimal? = null

    @Column(name = "AAA934")
    var aaa934: BigDecimal? = null

    @Column(name = "AAE013")
    var aae013: String? = null

    @Column(name = "AAA935")
    var aaa935: String? = null
}
