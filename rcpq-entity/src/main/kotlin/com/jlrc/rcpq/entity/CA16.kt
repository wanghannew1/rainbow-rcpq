package com.jlrc.rcpq.entity

import jakarta.persistence.*
import java.io.Serializable
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "CA16")
open class CA16 {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_CA16_ACA160")
    @SequenceGenerator(name = "SEQ_CA16_ACA160", sequenceName = "SEQ_CA16_ACA160", allocationSize = 1)
    @Column(name = "ACA160")
    var aca160: Long? = null

    @Column(name = "AAE001")
    var aae001: Long? = null

    @Column(name = "ACA161")
    var aca161: String? = null

    @Column(name = "ACA162")
    var aca162: String? = null

    @Column(name = "ACA163")
    var aca163: String? = null

    @Column(name = "ACA164")
    var aca164: String? = null

    @Column(name = "ACA165")
    var aca165: BigDecimal? = null

    @Column(name = "ACA166")
    var aca166: BigDecimal? = null

    @Column(name = "ACA167")
    var aca167: BigDecimal? = null

    @Column(name = "ACA168")
    var aca168: BigDecimal? = null

    @Column(name = "AAE013")
    var aae013: String? = null

    @Column(name = "AAE011")
    var aae011: String? = null

    @Column(name = "AAE019")
    var aae019: String? = null

    @Column(name = "AAE017")
    var aae017: String? = null

    @Column(name = "AAE020")
    var aae020: String? = null

    @Column(name = "AAE022")
    var aae022: String? = null

    @Column(name = "AAE036")
    var aae036: LocalDateTime? = null

    @Column(name = "BAZ001")
    var baz001: Long? = null

    @Column(name = "BAZ002")
    var baz002: Long? = null

}
