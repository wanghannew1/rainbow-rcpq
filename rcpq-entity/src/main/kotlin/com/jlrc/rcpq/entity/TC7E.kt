package com.jlrc.rcpq.entity

import jakarta.persistence.*
import java.io.Serializable
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "TC7E")
open class TC7E {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_TC7E_ATC7E0")
    @SequenceGenerator(name = "SEQ_TC7E_ATC7E0", sequenceName = "SEQ_TC7E_ATC7E0", allocationSize = 1)
    @Column(name = "ATC7E0")
    var atc7e0: Long? = null

    @Column(name = "BAZ001")
    var baz001: Long? = null

    @Column(name = "BAZ002")
    var baz002: Long? = null

    @Column(name = "ATC7E1")
    var atc7e1: String? = null

    @Column(name = "ATC7E2")
    var atc7e2: String? = null

    @Column(name = "ATC7E3")
    var atc7e3: String? = null

    @Column(name = "ATC7E4")
    var atc7e4: String? = null

    @Column(name = "ATC7E5")
    var atc7e5: LocalDate? = null

    @Column(name = "ATC7E6")
    var atc7e6: Long? = null

    @Column(name = "AAE013")
    var aae013: String? = null

    @Column(name = "ATC7E7")
    var atc7e7: String? = null

    @Column(name = "ATC7E8")
    var atc7e8: String? = null

    @Column(name = "ATC7E9")
    var atc7e9: String? = null

    @Column(name = "ATC7EA")
    var atc7ea: LocalDate? = null

    @Column(name = "ATC7EB")
    var atc7eb: String? = null

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

    @Column(name = "AAE111")
    var aae111: String? = null

    @Column(name = "AAE117")
    var aae117: String? = null

    @Column(name = "AAE136")
    var aae136: LocalDate? = null

    @Column(name = "AAE119")
    var aae119: String? = null

    @Column(name = "AAE120")
    var aae120: String? = null

    @Column(name = "AAE122")
    var aae122: String? = null

    @Column(name = "ATC7EC")
    var atc7ec: String? = null

}
