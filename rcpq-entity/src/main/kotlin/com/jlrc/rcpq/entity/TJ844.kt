package com.jlrc.rcpq.entity

import jakarta.persistence.*
import java.io.Serializable
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "TJ844")
open class TJ844 {
    @Id
    @Column(name = "TJ8440")
    var tj8440: Long? = null

    @Column(name = "TJ0000")
    var tj0000: Long? = null

    @Column(name = "AAA020")
    var aaa020: String? = null

    @Column(name = "AAA021")
    var aaa021: String? = null

    @Column(name = "TJ0002")
    var tj0002: LocalDate? = null

    @Column(name = "TJ0003")
    var tj0003: LocalDate? = null

    @Column(name = "AAC001")
    var aac001: Long? = null

    @Column(name = "AAC002")
    var aac002: String? = null

    @Column(name = "AAC003")
    var aac003: String? = null

    @Column(name = "AC0009")
    var ac0009: Long? = null

    @Column(name = "TJ8441")
    var tj8441: Long? = null

    @Column(name = "TJ8442")
    var tj8442: Long? = null

    @Column(name = "TJ8443")
    var tj8443: Long? = null

    @Column(name = "TJ8444")
    var tj8444: Long? = null

    @Column(name = "TJ8445")
    var tj8445: Long? = null

    @Column(name = "TJ8446")
    var tj8446: Long? = null

    @Column(name = "TJ8447")
    var tj8447: Long? = null

    @Column(name = "TJ8448")
    var tj8448: Long? = null

    @Column(name = "TJ8449")
    var tj8449: Long? = null

    @Column(name = "TJ844A")
    var tj844a: Long? = null

    @Column(name = "TJ844B")
    var tj844b: Long? = null

    @Column(name = "TJ844C")
    var tj844c: Long? = null

    @Column(name = "TJ844D")
    var tj844d: Long? = null

    @Column(name = "TJ844E")
    var tj844e: Long? = null

    @Column(name = "TJ844F")
    var tj844f: Long? = null

    @Column(name = "TJ844G")
    var tj844g: Long? = null

    @Column(name = "TJ844H")
    var tj844h: Long? = null

    @Column(name = "TJ000X")
    var tj000x: String? = null

    @Column(name = "TJ000W")
    var tj000w: String? = null

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

    @Column(name = "BAZ001")
    var baz001: Long? = null

    @Column(name = "BAZ002")
    var baz002: Long? = null

}
