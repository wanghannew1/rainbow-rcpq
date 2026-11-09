package com.jlrc.rcpq.entity

import jakarta.persistence.*
import java.io.Serializable
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "AA75")
open class AA75 {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_AA75_AAA750")
    @SequenceGenerator(name = "SEQ_AA75_AAA750", sequenceName = "SEQ_AA75_AAA750", allocationSize = 1)
    @Column(name = "AAA750")
    var aaa750: Long? = null

    @Column(name = "BAZ001")
    var baz001: Long? = null

    @Column(name = "BAZ002")
    var baz002: Long? = null

    @Column(name = "AAA730")
    var aaa730: Long? = null

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

    @Column(name = "AAA731")
    var aaa731: String? = null

    @Column(name = "ACC700")
    var acc700: Long? = null

    @Column(name = "AAC002")
    var aac002: String? = null

    @Column(name = "AAC003")
    var aac003: String? = null

    @Column(name = "AAC004")
    var aac004: String? = null

    @Column(name = "ACC740")
    var acc740: String? = null

    @Column(name = "ACC704")
    var acc704: String? = null

    @Column(name = "ACC70A")
    var acc70a: String? = null

}
