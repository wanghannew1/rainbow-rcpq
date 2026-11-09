package com.jlrc.rcpq.entity

import jakarta.persistence.*
import java.io.Serializable
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "TC9Q_LOG")
open class TC9Q_LOG {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_TC9Q_LOG_ID")
    @SequenceGenerator(name = "SEQ_TC9Q_LOG_ID", sequenceName = "SEQ_TC9Q_LOG_ID", allocationSize = 1)
    @Column(name = "ID")
    var id: Long? = null

    @Column(name = "ATC9Q0")
    var atc9q0: Long? = null

    @Column(name = "ATC9Q1")
    var atc9q1: String? = null

    @Column(name = "ATC9QE")
    var atc9qe: String? = null

    @Column(name = "ATC9QD")
    var atc9qd: BigDecimal? = null

    @Column(name = "AAE002")
    var aae002: Long? = null

    @Column(name = "AAE013")
    var aae013: String? = null

}
