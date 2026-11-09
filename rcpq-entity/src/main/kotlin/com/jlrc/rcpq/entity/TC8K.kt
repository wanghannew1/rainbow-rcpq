package com.jlrc.rcpq.entity

import jakarta.persistence.*
import java.io.Serializable
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "TC8K")
open class TC8K {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_TC8K_ATC8K0")
    @SequenceGenerator(name = "SEQ_TC8K_ATC8K0", sequenceName = "SEQ_TC8K_ATC8K0", allocationSize = 1)
    @Column(name = "ATC8K0")
    var atc8k0: Long? = null

    @Column(name = "AAB001")
    var aab001: Long? = null

    @Column(name = "AAB004")
    var aab004: String? = null

    @Column(name = "AAE001")
    var aae001: Long? = null

    @Column(name = "ATC8K1")
    var atc8k1: BigDecimal? = null

    @Column(name = "ATC8K2")
    var atc8k2: BigDecimal? = null

    @Column(name = "ATC8K3")
    var atc8k3: BigDecimal? = null

    @Column(name = "ATC8K4")
    var atc8k4: BigDecimal? = null

    @Column(name = "ATC8K5")
    var atc8k5: BigDecimal? = null

    @Column(name = "ATC8K6")
    var atc8k6: BigDecimal? = null

    @Column(name = "ATC8K7")
    var atc8k7: BigDecimal? = null

    @Column(name = "ATC8K8")
    var atc8k8: BigDecimal? = null

    @Column(name = "ATC8K9")
    var atc8k9: BigDecimal? = null

    @Column(name = "ATC8KA")
    var atc8ka: BigDecimal? = null

    @Column(name = "ATC8KB")
    var atc8kb: BigDecimal? = null

    @Column(name = "ATC8KC")
    var atc8kc: BigDecimal? = null

    @Column(name = "ATC8KD")
    var atc8kd: BigDecimal? = null

    @Column(name = "ATC8KE")
    var atc8ke: BigDecimal? = null

    @Column(name = "ATC8KF")
    var atc8kf: BigDecimal? = null

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

    @Column(name = "ATB930")
    var atb930: Long? = null

    @Column(name = "ATB931")
    var atb931: String? = null

}
