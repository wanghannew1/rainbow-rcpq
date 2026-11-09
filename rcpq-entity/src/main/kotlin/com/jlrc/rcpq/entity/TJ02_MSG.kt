package com.jlrc.rcpq.entity

import jakarta.persistence.*
import java.io.Serializable
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "TJ02_MSG")
open class TJ02_MSG {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_TJ02_MSG_ID")
    @SequenceGenerator(name = "SEQ_TJ02_MSG_ID", sequenceName = "SEQ_TJ02_MSG_ID", allocationSize = 1)
    @Column(name = "ID")
    var id: Long? = null

    @Column(name = "ATB930")
    var atb930: Long? = null

    @Column(name = "ATC931")
    var atc931: Long? = null

    @Column(name = "ATC932")
    var atc932: Long? = null

    @Column(name = "AAE002")
    var aae002: Long? = null

    @Column(name = "ATC937")
    var atc937: String? = null

    @Column(name = "SEQ")
    var seq: Long? = null

    @Column(name = "MSG")
    var msg: String? = null

    @Column(name = "TJ02_MSG1")
    var tj02_msg1: String? = null

}
