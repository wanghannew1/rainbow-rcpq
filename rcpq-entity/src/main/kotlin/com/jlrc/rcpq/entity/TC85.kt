package com.jlrc.rcpq.entity

import jakarta.persistence.*
import java.io.Serializable
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "TC85")
open class TC85 {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_TC85_ATC850")
    @SequenceGenerator(name = "SEQ_TC85_ATC850", sequenceName = "SEQ_TC85_ATC850", allocationSize = 1)
    @Column(name = "ATC850")
    var atc850: Long? = null

    @Column(name = "ATB840")
    var atb840: Long? = null

    @Column(name = "AAC001")
    var aac001: Long? = null

    @Column(name = "ATC831")
    var atc831: String? = null

    @Column(name = "AAC002")
    var aac002: String? = null

    @Column(name = "AAC003")
    var aac003: String? = null

    @Column(name = "AAC004")
    var aac004: String? = null

    @Column(name = "AAB001")
    var aab001: Long? = null

    @Column(name = "AAB004")
    var aab004: String? = null

    @Column(name = "ATC832")
    var atc832: LocalDate? = null

    @Column(name = "ATC833")
    var atc833: String? = null

    @Column(name = "ATC851")
    var atc851: String? = null

    @Column(name = "ATC852")
    var atc852: String? = null

    @Column(name = "AAE013")
    var aae013: String? = null

    @Column(name = "AAE011")
    var aae011: String? = null

    @Column(name = "AAE017")
    var aae017: String? = null

    @Column(name = "AAE036")
    var aae036: LocalDateTime? = null

    @Column(name = "AAE019")
    var aae019: String? = null

    @Column(name = "AAE020")
    var aae020: String? = null

    @Column(name = "AAE022")
    var aae022: String? = null

    @Column(name = "BAZ001")
    var baz001: Long? = null

    @Column(name = "BAZ002")
    var baz002: Long? = null

    @Column(name = "ATC830")
    var atc830: Long? = null

    @Column(name = "ATC83E")
    var atc83e: String? = null

    @Column(name = "ATC853")
    var atc853: String? = null

    @Column(name = "ATC854")
    var atc854: String? = null

    @Column(name = "ATC855")
    var atc855: String? = null

}
