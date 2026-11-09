package com.jlrc.rcpq.entity

import jakarta.persistence.*
import java.io.Serializable
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "TC8P")
open class TC8P {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_TC8P_ATC8P0")
    @SequenceGenerator(name = "SEQ_TC8P_ATC8P0", sequenceName = "SEQ_TC8P_ATC8P0", allocationSize = 1)
    @Column(name = "ATC8P0")
    var atc8p0: Long? = null

    @Column(name = "ATC8F0")
    var atc8f0: Long? = null

    @Column(name = "ATC8T0")
    var atc8t0: Long? = null

    @Column(name = "AAE100")
    var aae100: String? = null

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

    @Column(name = "ATC8P1")
    var atc8p1: BigDecimal? = null

    @Column(name = "ATC8P2")
    var atc8p2: BigDecimal? = null

    @Column(name = "TABLENAME")
    var tablename: String? = null

}
