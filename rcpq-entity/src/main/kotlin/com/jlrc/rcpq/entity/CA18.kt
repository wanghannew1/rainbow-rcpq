package com.jlrc.rcpq.entity

import jakarta.persistence.*
import java.io.Serializable
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "CA18")
open class CA18 {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_CA18_ID")
    @SequenceGenerator(name = "SEQ_CA18_ID", sequenceName = "SEQ_CA18_ID", allocationSize = 1)
    @Column(name = "ID")
    var id: String? = null

    @Column(name = "ACA110")
    var aca110: String? = null

    @Column(name = "ACE110")
    var ace110: String? = null

    @Column(name = "LABEL")
    var label: String? = null

    @Column(name = "ACA113")
    var aca113: String? = null

    @Column(name = "ACA114")
    var aca114: BigDecimal? = null

    @Column(name = "ACA115")
    var aca115: BigDecimal? = null

    @Column(name = "ACA116")
    var aca116: String? = null

    @Column(name = "ACA117")
    var aca117: LocalDate? = null

    @Column(name = "ACA118")
    var aca118: String? = null

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

}
