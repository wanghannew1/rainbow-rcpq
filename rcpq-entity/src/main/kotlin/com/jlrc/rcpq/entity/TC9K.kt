package com.jlrc.rcpq.entity

import jakarta.persistence.*
import java.io.Serializable
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "TC9K")
open class TC9K {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_TC9K_ATC9K0")
    @SequenceGenerator(name = "SEQ_TC9K_ATC9K0", sequenceName = "SEQ_TC9K_ATC9K0", allocationSize = 1)
    @Column(name = "ATC9K0")
    var atc9k0: Long? = null

    @Column(name = "ATC980")
    var atc980: Long? = null

    @Column(name = "AAC001")
    var aac001: Long? = null

    @Column(name = "ATC9K1")
    var atc9k1: String? = null

    @Column(name = "ATC9K2")
    var atc9k2: LocalDate? = null

    @Column(name = "ATC9K3")
    var atc9k3: String? = null

    @Column(name = "ATC9K4")
    var atc9k4: LocalDate? = null

    @Column(name = "ATC9K5")
    var atc9k5: LocalDate? = null

    @Column(name = "ATC9K6")
    var atc9k6: Long? = null

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
