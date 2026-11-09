package com.jlrc.rcpq.entity

import jakarta.persistence.*
import java.io.Serializable
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "TJ0Z3")
open class TJ0Z3 {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_TJ0Z3_AAE002")
    @SequenceGenerator(name = "SEQ_TJ0Z3_AAE002", sequenceName = "SEQ_TJ0Z3_AAE002", allocationSize = 1)
    @Column(name = "AAE002")
    var aae002: Long? = null

    @Column(name = "ATJ001")
    var atj001: Long? = null

    @Column(name = "ATJ001_1")
    var atj001_1: Long? = null

    @Column(name = "ATJ001_2")
    var atj001_2: Long? = null

    @Column(name = "ATJ002")
    var atj002: Long? = null

    @Column(name = "ATJ002_1")
    var atj002_1: Long? = null

    @Column(name = "ATJ003")
    var atj003: Long? = null

    @Column(name = "ATJ003_1")
    var atj003_1: Long? = null

    @Column(name = "ATJ004")
    var atj004: Long? = null

    @Column(name = "ATJ004_1")
    var atj004_1: Long? = null

}
