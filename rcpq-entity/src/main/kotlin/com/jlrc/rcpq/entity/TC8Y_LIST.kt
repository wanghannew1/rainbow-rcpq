package com.jlrc.rcpq.entity

import jakarta.persistence.*
import java.io.Serializable
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "TC8Y_LIST")
open class TC8Y_LIST {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_TC8Y_LIST_ID")
    @SequenceGenerator(name = "SEQ_TC8Y_LIST_ID", sequenceName = "SEQ_TC8Y_LIST_ID", allocationSize = 1)
    @Column(name = "ID")
    var id: Long? = null

    @Column(name = "ATC8Y0")
    var atc8y0: Long? = null

    @Column(name = "ATC9Q0")
    var atc9q0: Long? = null

    @Column(name = "ATC900")
    var atc900: Long? = null

    @Column(name = "AAC003")
    var aac003: String? = null

    @Column(name = "AAC002")
    var aac002: String? = null

    @Column(name = "ATB930")
    var atb930: Long? = null

    @Column(name = "ATB931")
    var atb931: String? = null

    @Column(name = "AAB001")
    var aab001: Long? = null

    @Column(name = "AAB004")
    var aab004: String? = null

    @Column(name = "ATC9Q1")
    var atc9q1: String? = null

    @Column(name = "ATC9QA")
    var atc9qa: LocalDate? = null

    @Column(name = "ATC9QB")
    var atc9qb: BigDecimal? = null

    @Column(name = "ATC9QV")
    var atc9qv: BigDecimal? = null

    @Column(name = "ATC9QW")
    var atc9qw: BigDecimal? = null

}
