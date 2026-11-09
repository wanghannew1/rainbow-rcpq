package com.jlrc.rcpq.entity

import jakarta.persistence.*
import java.io.Serializable
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "SYSLOB")
open class SYSLOB {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_SYSLOB_SYSLOBID")
    @SequenceGenerator(name = "SEQ_SYSLOB_SYSLOBID", sequenceName = "SEQ_SYSLOB_SYSLOBID", allocationSize = 1)
    @Column(name = "SYSLOBID")
    var syslobid: Long? = null

    @Column(name = "BAZ001")
    var baz001: Long? = null

    @Column(name = "BAZ002")
    var baz002: Long? = null

    @Column(name = "TABLENAME")
    var tablename: String? = null

    @Column(name = "COLUMNNAME")
    var columnname: String? = null

    @Column(name = "COLUMNVALUE")
    var columnvalue: String? = null

    @Column(name = "ABF01Z")
    var abf01z: String? = null

    @Column(name = "PDF")
    var pdf: String? = null

    @Column(name = "HTML")
    var html: String? = null

    @Column(name = "EXCEL")
    var excel: String? = null

    @Column(name = "WORD")
    var word: String? = null

    @Column(name = "WORDNAME")
    var wordname: String? = null

    @Column(name = "ABZ001")
    var abz001: Long? = null

    @Column(name = "ABB808")
    var abb808: String? = null

}
