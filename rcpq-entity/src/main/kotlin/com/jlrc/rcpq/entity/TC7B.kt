package com.jlrc.rcpq.entity

import jakarta.persistence.*
import java.io.Serializable
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "TC7B")
open class TC7B {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_TC7B_ATC7B0")
    @SequenceGenerator(name = "SEQ_TC7B_ATC7B0", sequenceName = "SEQ_TC7B_ATC7B0", allocationSize = 1)
    @Column(name = "ATC7B0")
    var atc7b0: Long? = null

    @Column(name = "BAZ001")
    var baz001: Long? = null

    @Column(name = "BAZ002")
    var baz002: Long? = null

    @Column(name = "AAC003")
    var aac003: String? = null

    @Column(name = "ATC7B1")
    var atc7b1: String? = null

    @Column(name = "ATC7B2")
    var atc7b2: String? = null

    @Column(name = "ATC7B3")
    var atc7b3: String? = null

    @Column(name = "ATC7B4")
    var atc7b4: LocalDate? = null

    @Column(name = "ATC7B5")
    var atc7b5: LocalDate? = null

    @Column(name = "ATC7B6")
    var atc7b6: String? = null

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

    @Column(name = "ATC7B7")
    var atc7b7: String? = null

    @Column(name = "ATC7B8")
    var atc7b8: LocalDate? = null

    @Column(name = "ATC7B9")
    var atc7b9: String? = null

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

    @Column(name = "AAE100")
    var aae100: String? = null

    @Column(name = "ATC7BA")
    var atc7ba: String? = null

}
