package com.jlrc.rcpq.entity

import jakarta.persistence.*
import java.io.Serializable
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "TC8A")
open class TC8A {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_TC8A_ATC8A0")
    @SequenceGenerator(name = "SEQ_TC8A_ATC8A0", sequenceName = "SEQ_TC8A_ATC8A0", allocationSize = 1)
    @Column(name = "ATC8A0")
    var atc8a0: Long? = null

    @Column(name = "ATC830")
    var atc830: Long? = null

    @Column(name = "AAC001")
    var aac001: Long? = null

    @Column(name = "ATC8A9")
    var atc8a9: Long? = null

    @Column(name = "ATC831")
    var atc831: String? = null

    @Column(name = "AAC002")
    var aac002: String? = null

    @Column(name = "AAC003")
    var aac003: String? = null

    @Column(name = "AAE006")
    var aae006: String? = null

    @Column(name = "AAC026")
    var aac026: String? = null

    @Column(name = "AAE005")
    var aae005: String? = null

    @Column(name = "AAE138")
    var aae138: String? = null

    @Column(name = "ATC8A1")
    var atc8a1: String? = null

    @Column(name = "AAC004")
    var aac004: String? = null

    @Column(name = "ATC8A2")
    var atc8a2: LocalDate? = null

    @Column(name = "ATC8A3")
    var atc8a3: String? = null

    @Column(name = "ATC8A4")
    var atc8a4: String? = null

    @Column(name = "ATC8A5")
    var atc8a5: String? = null

    @Column(name = "ATC8A6")
    var atc8a6: LocalDate? = null

    @Column(name = "ATC8A7")
    var atc8a7: String? = null

    @Column(name = "ATC8A8")
    var atc8a8: LocalDate? = null

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

    @Column(name = "BAZ001")
    var baz001: Long? = null

    @Column(name = "BAZ002")
    var baz002: Long? = null

    @Column(name = "AAB001")
    var aab001: Long? = null

    @Column(name = "AAB004")
    var aab004: String? = null

    @Column(name = "ATC832")
    var atc832: LocalDate? = null

    @Column(name = "ATC833")
    var atc833: String? = null

}
