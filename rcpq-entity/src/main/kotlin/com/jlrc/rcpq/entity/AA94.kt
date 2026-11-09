package com.jlrc.rcpq.entity

import jakarta.persistence.*
import java.io.Serializable
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "AA94")
open class AA94 {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_AA94_AAA940")
    @SequenceGenerator(name = "SEQ_AA94_AAA940", sequenceName = "SEQ_AA94_AAA940", allocationSize = 1)
    @Column(name = "AAA940")
    var aaa940: Long? = null

    @Column(name = "BAZ001")
    var baz001: Long? = null

    @Column(name = "BAZ002")
    var baz002: Long? = null

    @Column(name = "AAA941")
    var aaa941: String? = null

    @Column(name = "AAA100")
    var aaa100: String? = null

    @Column(name = "AAA101")
    var aaa101: String? = null

    @Column(name = "AAA942")
    var aaa942: BigDecimal? = null

    @Column(name = "AAA943")
    var aaa943: String? = null

    @Column(name = "AAA944")
    var aaa944: BigDecimal? = null

    @Column(name = "AAA945")
    var aaa945: BigDecimal? = null

    @Column(name = "AAA946")
    var aaa946: Long? = null

    @Column(name = "AAA947")
    var aaa947: Long? = null

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
