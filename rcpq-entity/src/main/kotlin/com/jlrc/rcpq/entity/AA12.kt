package com.jlrc.rcpq.entity

import jakarta.persistence.*
import java.io.Serializable
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime
@Entity
@Table(name = "AA12")
open class AA12 {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "AA12_SEQ")
    // TODO: 序列名待与 PG DDL 对齐(原 Oracle sequence)
    @SequenceGenerator(name = "AA12_SEQ", sequenceName = "AA12_SEQ", allocationSize = 1)
    @Column(name = "ID")
    var id: Long? = null

    @Column(name = "PARENTID")
    var parentid: String? = null

    @Column(name = "HASCHILD")
    var haschild: String? = null

    @Column(name = "LABEL")
    var label: String? = null

    @Column(name = "SPELL")
    var spell: String? = null

    @Column(name = "ISCOMMON")
    var iscommon: String? = null

    @Column(name = "ISVALID")
    var isvalid: String? = null

    @Column(name = "BAZ001")
    var baz001: Long? = null

    @Column(name = "BAZ002")
    var baz002: Long? = null

    @Column(name = "ORDNUMBER")
    var ordnumber: String? = null

    @Column(name = "FLOOR")
    var floor: String? = null

}
