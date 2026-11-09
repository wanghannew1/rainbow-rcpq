package com.jlrc.rcpq.entity

import jakarta.persistence.*
import java.io.Serializable
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "CC78")
open class CC78 {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_CC78_ACC780")
    @SequenceGenerator(name = "SEQ_CC78_ACC780", sequenceName = "SEQ_CC78_ACC780", allocationSize = 1)
    @Column(name = "ACC780")
    var acc780: Long? = null

    @Column(name = "BAZ001")
    var baz001: Long? = null

    @Column(name = "BAZ002")
    var baz002: Long? = null

    @Column(name = "AAA730")
    var aaa730: Long? = null

    @Column(name = "ATC9J0")
    var atc9j0: Long? = null

    @Column(name = "AAA731")
    var aaa731: String? = null

    @Column(name = "ACC781")
    var acc781: LocalDate? = null

    @Column(name = "ACC782")
    var acc782: String? = null

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
