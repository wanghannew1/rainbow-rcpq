package com.jlrc.rcpq.entity

import jakarta.persistence.*
import java.io.Serializable
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "TC7A")
open class TC7A {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_TC7A_ATC7A0")
    @SequenceGenerator(name = "SEQ_TC7A_ATC7A0", sequenceName = "SEQ_TC7A_ATC7A0", allocationSize = 1)
    @Column(name = "ATC7A0")
    var atc7a0: Long? = null

    @Column(name = "BAZ001")
    var baz001: Long? = null

    @Column(name = "BAZ002")
    var baz002: Long? = null

    @Column(name = "ATC7A1")
    var atc7a1: String? = null

    @Column(name = "ATC7A2")
    var atc7a2: String? = null

    @Column(name = "ATC7A3")
    var atc7a3: String? = null

    @Column(name = "ATC7A4")
    var atc7a4: String? = null

    @Column(name = "ATC7A5")
    var atc7a5: String? = null

    @Column(name = "ATC7A6")
    var atc7a6: String? = null

    @Column(name = "ATC7A7")
    var atc7a7: String? = null

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

    @Column(name = "ATC700")
    var atc700: Long? = null

    @Column(name = "ATC740")
    var atc740: Long? = null

}
