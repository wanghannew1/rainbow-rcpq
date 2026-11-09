package com.jlrc.rcpq.entity

import jakarta.persistence.*
import java.io.Serializable
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "TC8I")
open class TC8I {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_TC8I_ATC8I0")
    @SequenceGenerator(name = "SEQ_TC8I_ATC8I0", sequenceName = "SEQ_TC8I_ATC8I0", allocationSize = 1)
    @Column(name = "ATC8I0")
    var atc8i0: Long? = null

    @Column(name = "ATC8I1")
    var atc8i1: String? = null

    @Column(name = "ATC8I6")
    var atc8i6: BigDecimal? = null

    @Column(name = "AAE136")
    var aae136: LocalDate? = null

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

    @Column(name = "BBB002")
    var bbb002: String? = null

    @Column(name = "ATC8I2")
    var atc8i2: BigDecimal? = null

    @Column(name = "ATC8I3")
    var atc8i3: LocalDate? = null

    @Column(name = "ATC8I4")
    var atc8i4: String? = null

    @Column(name = "ATC8I5")
    var atc8i5: BigDecimal? = null

    @Column(name = "AAE114")
    var aae114: String? = null

    @Column(name = "AAE111")
    var aae111: String? = null

    @Column(name = "AAE117")
    var aae117: String? = null

    @Column(name = "AAE119")
    var aae119: String? = null

    @Column(name = "AAE120")
    var aae120: String? = null

    @Column(name = "AAE122")
    var aae122: String? = null

    @Column(name = "AAE013")
    var aae013: String? = null

    @Column(name = "AAE002")
    var aae002: String? = null

}
