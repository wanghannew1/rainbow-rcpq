package com.jlrc.rcpq.entity

import jakarta.persistence.*
import java.io.Serializable
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "TJ841")
open class TJ841 {
    @Id
    @Column(name = "TJ8410")
    var tj8410: Long? = null

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

    @Column(name = "AC0009")
    var ac0009: Long? = null

    @Column(name = "TJ8411")
    var tj8411: Long? = null

    @Column(name = "TJ8412")
    var tj8412: Long? = null

    @Column(name = "TJ8413")
    var tj8413: Long? = null

    @Column(name = "TJ8414")
    var tj8414: Long? = null

    @Column(name = "TJ8415")
    var tj8415: Long? = null

    @Column(name = "TJ8416")
    var tj8416: Long? = null

    @Column(name = "TJ8417")
    var tj8417: Long? = null

    @Column(name = "TJ8418")
    var tj8418: Long? = null

    @Column(name = "TJ8419")
    var tj8419: Long? = null

    @Column(name = "TJ841A")
    var tj841a: Long? = null

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
