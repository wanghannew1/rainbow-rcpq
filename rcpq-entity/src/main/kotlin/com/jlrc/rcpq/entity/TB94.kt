package com.jlrc.rcpq.entity

import jakarta.persistence.*
import java.io.Serializable
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "TB94")
open class TB94 {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_TB94_ATB940")
    @SequenceGenerator(name = "SEQ_TB94_ATB940", sequenceName = "SEQ_TB94_ATB940", allocationSize = 1)
    @Column(name = "ATB940")
    var atb940: Long? = null

    @Column(name = "AAE001")
    var aae001: Long? = null

    @Column(name = "ATB941")
    var atb941: Long? = null

    @Column(name = "ATB942")
    var atb942: Long? = null

    @Column(name = "ATB94C")
    var atb94c: BigDecimal? = null

    @Column(name = "ATB943")
    var atb943: Long? = null

    @Column(name = "ATB945")
    var atb945: BigDecimal? = null

    @Column(name = "ATB94D")
    var atb94d: BigDecimal? = null

    @Column(name = "ATB946")
    var atb946: BigDecimal? = null

    @Column(name = "ATB947")
    var atb947: BigDecimal? = null

    @Column(name = "ATB948")
    var atb948: String? = null

    @Column(name = "ATB949")
    var atb949: String? = null

    @Column(name = "ATB94A")
    var atb94a: LocalDate? = null

    @Column(name = "ATB94B")
    var atb94b: LocalDate? = null

    @Column(name = "AAE013")
    var aae013: String? = null

    @Column(name = "AAE019")
    var aae019: String? = null

    @Column(name = "AAE011")
    var aae011: String? = null

    @Column(name = "AAE020")
    var aae020: String? = null

    @Column(name = "AAE017")
    var aae017: String? = null

    @Column(name = "AAE036")
    var aae036: LocalDateTime? = null

    @Column(name = "AAE022")
    var aae022: String? = null

}
