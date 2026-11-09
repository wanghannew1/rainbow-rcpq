package com.jlrc.rcpq.entity

import jakarta.persistence.*
import java.io.Serializable
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "TC8Y")
open class TC8Y {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_TC8Y_ATC8Y0")
    @SequenceGenerator(name = "SEQ_TC8Y_ATC8Y0", sequenceName = "SEQ_TC8Y_ATC8Y0", allocationSize = 1)
    @Column(name = "ATC8Y0")
    var atc8y0: Long? = null

    @Column(name = "ATC8Y1")
    var atc8y1: String? = null

    @Column(name = "BBB002")
    var bbb002: String? = null

    @Column(name = "ATC8Y2")
    var atc8y2: BigDecimal? = null

    @Column(name = "ATC8Y3")
    var atc8y3: LocalDate? = null

    @Column(name = "ATC8Y4")
    var atc8y4: String? = null

    @Column(name = "ATC8Y5")
    var atc8y5: BigDecimal? = null

    @Column(name = "ATC8Y6")
    var atc8y6: BigDecimal? = null

    @Column(name = "ATC8Y7")
    var atc8y7: BigDecimal? = null

    @Column(name = "ATC8Y8")
    var atc8y8: BigDecimal? = null

    @Column(name = "AAE114")
    var aae114: String? = null

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

    @Column(name = "AAE002")
    var aae002: String? = null

}
