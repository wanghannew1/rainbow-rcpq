package com.jlrc.rcpq.entity

import jakarta.persistence.*
import java.io.Serializable
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "V_AB01_TREE")
open class V_AB01_TREE {
    @Id
    @Column(name = "AAB001")
    var aab001: Long? = null

    @Column(name = "ID")
    var id: String? = null

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

    @Column(name = "ORDERNUM")
    var ordernum: String? = null

}
