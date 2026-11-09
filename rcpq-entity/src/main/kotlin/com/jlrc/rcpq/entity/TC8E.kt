package com.jlrc.rcpq.entity

import jakarta.persistence.*
import java.io.Serializable
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "TC8E")
open class TC8E {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_TC8E_ATC8E0")
    @SequenceGenerator(name = "SEQ_TC8E_ATC8E0", sequenceName = "SEQ_TC8E_ATC8E0", allocationSize = 1)
    @Column(name = "ATC8E0")
    var atc8e0: Long? = null

    @Column(name = "AAC001")
    var aac001: Long? = null

    @Column(name = "ATB86Q")
    var atb86q: String? = null

    @Column(name = "ATB86X")
    var atb86x: String? = null

    @Column(name = "ATB861")
    var atb861: String? = null

    @Column(name = "AAC002")
    var aac002: String? = null

    @Column(name = "AAC003")
    var aac003: String? = null

    @Column(name = "AAC004")
    var aac004: String? = null

    @Column(name = "AAC006")
    var aac006: LocalDate? = null

    @Column(name = "ATC8E1")
    var atc8e1: String? = null

    @Column(name = "ATC8E2")
    var atc8e2: String? = null

    @Column(name = "AAE005")
    var aae005: String? = null

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

}
