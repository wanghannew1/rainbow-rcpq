package com.jlrc.rcpq.entity

import jakarta.persistence.*
import java.io.Serializable
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "TC8UMX")
open class TC8UMX {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_TC8UMX_ID")
    @SequenceGenerator(name = "SEQ_TC8UMX_ID", sequenceName = "SEQ_TC8UMX_ID", allocationSize = 1)
    @Column(name = "ID")
    var id: Long? = null

    @Column(name = "ATC8U0")
    var atc8u0: Long? = null

    @Column(name = "ATB930")
    var atb930: Long? = null

    @Column(name = "ATB931")
    var atb931: String? = null

    @Column(name = "AAB001")
    var aab001: Long? = null

    @Column(name = "AAB004")
    var aab004: String? = null

    @Column(name = "ATC931")
    var atc931: Long? = null

    @Column(name = "ATC932")
    var atc932: Long? = null

    @Column(name = "ATC937")
    var atc937: String? = null

    @Column(name = "AAE002")
    var aae002: Long? = null

    @Column(name = "FYLB")
    var fylb: String? = null

    @Column(name = "TC8UMX01")
    var tc8umx01: BigDecimal? = null

    @Column(name = "TC8UMX02")
    var tc8umx02: BigDecimal? = null

    @Column(name = "TC8UMX03")
    var tc8umx03: BigDecimal? = null

    @Column(name = "TC8UMX04")
    var tc8umx04: BigDecimal? = null

}
