package com.jlrc.rcpq.entity

import jakarta.persistence.*
import java.io.Serializable
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "TC7I")
open class TC7I {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_TC7I_ATC7I0")
    @SequenceGenerator(name = "SEQ_TC7I_ATC7I0", sequenceName = "SEQ_TC7I_ATC7I0", allocationSize = 1)
    @Column(name = "ATC7I0")
    var atc7i0: Long? = null

    @Column(name = "BAZ001")
    var baz001: Long? = null

    @Column(name = "BAZ002")
    var baz002: Long? = null

    @Column(name = "ATC7I1")
    var atc7i1: Long? = null

    @Column(name = "ATC7I2")
    var atc7i2: String? = null

    @Column(name = "ATC7I3")
    var atc7i3: String? = null

    @Column(name = "ATC7I4")
    var atc7i4: String? = null

    @Column(name = "ATC7I5")
    var atc7i5: String? = null

    @Column(name = "AAC003")
    var aac003: String? = null

    @Column(name = "ATC7I6")
    var atc7i6: String? = null

    @Column(name = "AAB004")
    var aab004: String? = null

    @Column(name = "AAE005")
    var aae005: String? = null

    @Column(name = "ATC7I7")
    var atc7i7: LocalDate? = null

    @Column(name = "ATC7I8")
    var atc7i8: LocalDate? = null

    @Column(name = "ATC7I9")
    var atc7i9: String? = null

    @Column(name = "ATC702")
    var atc702: String? = null

    @Column(name = "ATC7IA")
    var atc7ia: LocalDate? = null

    @Column(name = "ATC7IB")
    var atc7ib: String? = null

    @Column(name = "ATC7IC")
    var atc7ic: BigDecimal? = null

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

    @Column(name = "AAE013")
    var aae013: String? = null

    @Column(name = "ATC7ID")
    var atc7id: String? = null

    @Column(name = "ATC700")
    var atc700: BigDecimal? = null
}
