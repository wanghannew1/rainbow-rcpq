package com.jlrc.rcpq.entity

import jakarta.persistence.*
import java.io.Serializable
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "TC9P")
open class TC9P {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_TC9P_ATC9P0")
    @SequenceGenerator(name = "SEQ_TC9P_ATC9P0", sequenceName = "SEQ_TC9P_ATC9P0", allocationSize = 1)
    @Column(name = "ATC9P0")
    var atc9p0: Long? = null

    @Column(name = "ATB930")
    var atb930: Long? = null

    @Column(name = "ATB900")
    var atb900: Long? = null

    @Column(name = "AAB001")
    var aab001: Long? = null

    @Column(name = "ATB931")
    var atb931: String? = null

    @Column(name = "AAB004")
    var aab004: String? = null

    @Column(name = "ATC900")
    var atc900: Long? = null

    @Column(name = "AAC001")
    var aac001: Long? = null

    @Column(name = "AAC003")
    var aac003: String? = null

    @Column(name = "AAC002")
    var aac002: String? = null

    @Column(name = "AAC004")
    var aac004: String? = null

    @Column(name = "ATC9P1")
    var atc9p1: String? = null

    @Column(name = "AAE001")
    var aae001: Long? = null

    @Column(name = "AAE002")
    var aae002: Long? = null

    @Column(name = "ATC9P2")
    var atc9p2: BigDecimal? = null

    @Column(name = "ATC9P3")
    var atc9p3: BigDecimal? = null

    @Column(name = "ATC9P4")
    var atc9p4: String? = null

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

    @Column(name = "ATC9P5")
    var atc9p5: BigDecimal? = null

    @Column(name = "ATC9P6")
    var atc9p6: String? = null

}
