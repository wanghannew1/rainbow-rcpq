package com.jlrc.rcpq.entity

import jakarta.persistence.*
import java.io.Serializable
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "YTH_GRCB")
open class YTH_GRCB {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_YTH_GRCB_AAC001")
    @SequenceGenerator(name = "SEQ_YTH_GRCB_AAC001", sequenceName = "SEQ_YTH_GRCB_AAC001", allocationSize = 1)
    @Column(name = "AAC001")
    var aac001: String? = null

    @Column(name = "AAC002")
    var aac002: String? = null

    @Column(name = "AAC003")
    var aac003: String? = null

    @Column(name = "AAC004")
    var aac004: String? = null

    @Column(name = "AAC006")
    var aac006: LocalDateTime? = null

    @Column(name = "AAB001")
    var aab001: String? = null

    @Column(name = "AAE141")
    var aae141: String? = null

    @Column(name = "AAE041")
    var aae041: String? = null

    @Column(name = "AAE042")
    var aae042: String? = null

}
