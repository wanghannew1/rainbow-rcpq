package com.jlrc.rcpq.entity

import jakarta.persistence.*
import java.io.Serializable
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "TC7K")
open class TC7K {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_TC7K_ATC7K0")
    @SequenceGenerator(name = "SEQ_TC7K_ATC7K0", sequenceName = "SEQ_TC7K_ATC7K0", allocationSize = 1)
    @Column(name = "ATC7K0")
    var atc7k0: Long? = null

    @Column(name = "BAZ001")
    var baz001: Long? = null

    @Column(name = "BAZ002")
    var baz002: Long? = null

    @Column(name = "AAC003")
    var aac003: String? = null

    @Column(name = "ATC702")
    var atc702: String? = null

    @Column(name = "ATC7K1")
    var atc7k1: String? = null

    @Column(name = "ATC7K2")
    var atc7k2: LocalDate? = null

    @Column(name = "ATC7K3")
    var atc7k3: String? = null

    @Column(name = "AAE005")
    var aae005: String? = null

    @Column(name = "ATC7K4")
    var atc7k4: String? = null

    @Column(name = "ATC7K5")
    var atc7k5: LocalDate? = null

    @Column(name = "ATC7K6")
    var atc7k6: LocalDate? = null

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

    @Column(name = "ATC7K7")
    var atc7k7: String? = null

    @Column(name = "ATC7K8")
    var atc7k8: String? = null

    @Column(name = "ATC7K9")
    var atc7k9: String? = null

    @Column(name = "AAE013")
    var aae013: String? = null

}
