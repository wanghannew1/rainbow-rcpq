package com.jlrc.rcpq.entity

import jakarta.persistence.*
import java.io.Serializable
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "WBS_SCREEN")
open class WBS_SCREEN {
    @Id
    @Column(name = "SCREENID")
    var screenid: String? = null

    @Column(name = "DESCRIPTION")
    var description: String? = null

    @Column(name = "LEFT")
    var left: String? = null

    @Column(name = "TOP")
    var top: String? = null

    @Column(name = "WIDTH")
    var width: String? = null

    @Column(name = "HEIGHT")
    var height: String? = null

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

    @Column(name = "PARENTID")
    var parentid: String? = null

    @Column(name = "LEAF")
    var leaf: String? = null

    @Column(name = "ISDISPLAY")
    var isdisplay: String? = null

    @Column(name = "URL")
    var url: String? = null

}
