package com.jlrc.rcpq.entity

import jakarta.persistence.*
import java.io.Serializable
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "AA11")
open class AA11 {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_AA11_AAA110")
    @SequenceGenerator(name = "SEQ_AA11_AAA110", sequenceName = "SEQ_AA11_AAA110", allocationSize = 1)
    @Column(name = "AAA110")
    var aaa110: String? = null

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

    @Column(name = "AAA119")
    var aaa119: String? = null

    @Column(name = "AAE100")
    var aae100: String? = null

}
