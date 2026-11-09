package com.jlrc.rcpq.entity

import jakarta.persistence.*
import java.io.Serializable
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "TC9M")
open class TC9M {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_TC9M_ATC9M0")
    @SequenceGenerator(name = "SEQ_TC9M_ATC9M0", sequenceName = "SEQ_TC9M_ATC9M0", allocationSize = 1)
    @Column(name = "ATC9M0")
    var atc9m0: Long? = null

    @Column(name = "ATC9L0")
    var atc9l0: Long? = null

    @Column(name = "AAB001")
    var aab001: Long? = null

    @Column(name = "AAB004")
    var aab004: String? = null

    @Column(name = "AAC001")
    var aac001: Long? = null

    @Column(name = "AAC002")
    var aac002: String? = null

    @Column(name = "AAC003")
    var aac003: String? = null

    @Column(name = "ATC9M1")
    var atc9m1: Long? = null

    @Column(name = "AAC180")
    var aac180: String? = null

    @Column(name = "AAE00C")
    var aae00c: String? = null

    @Column(name = "ATC9M2")
    var atc9m2: String? = null

    @Column(name = "ATC9M3")
    var atc9m3: String? = null

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

}
