package com.jlrc.rcpq.entity

import jakarta.persistence.*
import java.io.Serializable
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "YTH_GRJF")
open class YTH_GRJF {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_YTH_GRJF_AAC001")
    @SequenceGenerator(name = "SEQ_YTH_GRJF_AAC001", sequenceName = "SEQ_YTH_GRJF_AAC001", allocationSize = 1)
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

    @Column(name = "AAB004")
    var aab004: String? = null

    @Column(name = "AAB019")
    var aab019: String? = null

    @Column(name = "AAE003")
    var aae003: String? = null

    @Column(name = "AAE141")
    var aae141: String? = null

    @Column(name = "AAE143")
    var aae143: String? = null

    @Column(name = "AAC150")
    var aac150: BigDecimal? = null

    @Column(name = "AAC123")
    var aac123: BigDecimal? = null

    @Column(name = "AAC130")
    var aac130: BigDecimal? = null

    @Column(name = "AAE114")
    var aae114: String? = null

    @Column(name = "AAE210")
    var aae210: String? = null

}
