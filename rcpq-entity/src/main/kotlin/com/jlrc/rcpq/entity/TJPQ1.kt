package com.jlrc.rcpq.entity

import jakarta.persistence.*
import java.io.Serializable
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "TJPQ1")
open class TJPQ1 {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_TJPQ1_TJPQ10")
    @SequenceGenerator(name = "SEQ_TJPQ1_TJPQ10", sequenceName = "SEQ_TJPQ1_TJPQ10", allocationSize = 1)
    @Column(name = "TJPQ10")
    var tjpq10: Long? = null

    @Column(name = "BAZ001")
    var baz001: Long? = null

    @Column(name = "BAZ002")
    var baz002: Long? = null

    @Column(name = "AAC003")
    var aac003: String? = null

    @Column(name = "AAC002")
    var aac002: String? = null

    @Column(name = "ATC933")
    var atc933: BigDecimal? = null

    @Column(name = "ATC933_AVG")
    var atc933_avg: BigDecimal? = null

}
