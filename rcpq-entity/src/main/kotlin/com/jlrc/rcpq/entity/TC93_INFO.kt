package com.jlrc.rcpq.entity

import jakarta.persistence.*
import java.io.Serializable
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "TC93_INFO")
open class TC93_INFO {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_TC93_INFO_ID")
    @SequenceGenerator(name = "SEQ_TC93_INFO_ID", sequenceName = "SEQ_TC93_INFO_ID", allocationSize = 1)
    @Column(name = "ID")
    var id: Long? = null

    @Column(name = "ATC930")
    var atc930: Long? = null

    @Column(name = "ATC900")
    var atc900: Long? = null

    @Column(name = "AAC001")
    var aac001: Long? = null

    @Column(name = "FLAG")
    var flag: String? = null

    @Column(name = "KEY")
    var key: Long? = null

    @Column(name = "AAE013")
    var aae013: String? = null

    @Column(name = "ATB930")
    var atb930: Long? = null

    @Column(name = "ATC931")
    var atc931: Long? = null

    @Column(name = "ATC932")
    var atc932: Long? = null

    @Column(name = "ATC937")
    var atc937: String? = null

}
