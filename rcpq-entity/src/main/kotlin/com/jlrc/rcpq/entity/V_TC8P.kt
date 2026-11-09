package com.jlrc.rcpq.entity

import jakarta.persistence.*
import java.io.Serializable
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "V_TC8P")
open class V_TC8P {
    @Id
    @Column(name = "ATC8P0")
    var atc8p0: Long? = null

    @Column(name = "ATC8F0")
    var atc8f0: Long? = null

    @Column(name = "ATB930")
    var atb930: Long? = null

    @Column(name = "ATB931")
    var atb931: String? = null

    @Column(name = "AAB001")
    var aab001: Long? = null

    @Column(name = "AAB004")
    var aab004: String? = null

    @Column(name = "ATB934")
    var atb934: String? = null

    @Column(name = "ATB935")
    var atb935: String? = null

    @Column(name = "ATC931")
    var atc931: Long? = null

    @Column(name = "ATC932")
    var atc932: Long? = null

    @Column(name = "ATC937")
    var atc937: String? = null

    @Column(name = "ATC8F1")
    var atc8f1: String? = null

    @Column(name = "ATC8F2")
    var atc8f2: BigDecimal? = null

    @Column(name = "ATC8F3")
    var atc8f3: LocalDate? = null

    @Column(name = "ATC8F4")
    var atc8f4: String? = null

    @Column(name = "ATC8F9")
    var atc8f9: BigDecimal? = null

    @Column(name = "ATC8FA")
    var atc8fa: BigDecimal? = null

    @Column(name = "ATC8F5")
    var atc8f5: BigDecimal? = null

    @Column(name = "ATC8F6")
    var atc8f6: LocalDate? = null

    @Column(name = "ATC8F8")
    var atc8f8: String? = null

    @Column(name = "AAE211")
    var aae211: String? = null

    @Column(name = "AAE217")
    var aae217: String? = null

    @Column(name = "AAE236")
    var aae236: LocalDate? = null

    @Column(name = "AAE219")
    var aae219: String? = null

    @Column(name = "AAE220")
    var aae220: String? = null

    @Column(name = "AAE222")
    var aae222: String? = null

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

    @Column(name = "ATC93U")
    var atc93u: String? = null

}
