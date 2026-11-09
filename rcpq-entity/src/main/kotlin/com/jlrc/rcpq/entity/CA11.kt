package com.jlrc.rcpq.entity

import jakarta.persistence.*
import java.io.Serializable
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "CA11")
open class CA11 {
    @Id
    @Column(name = "ACA110")
    var aca110: String? = null

    @Column(name = "ID")
    var id: String? = null

    @Column(name = "LABEL")
    var label: String? = null

    @Column(name = "SPELL")
    var spell: String? = null

    @Column(name = "HASCHILD")
    var haschild: String? = null

    @Column(name = "ISCOMMON")
    var iscommon: String? = null

    @Column(name = "BAZ001")
    var baz001: Long? = null

    @Column(name = "BAZ002")
    var baz002: Long? = null

    @Column(name = "PARENTID")
    var parentid: String? = null

    @Column(name = "FLOOR")
    var floor: Long? = null

    @Column(name = "ISVALID")
    var isvalid: String? = null

    @Column(name = "ORDNUMBER")
    var ordnumber: Long? = null

    @Column(name = "ACA111")
    var aca111: String? = null

    @Column(name = "ACA112")
    var aca112: String? = null

}
