package com.jlrc.rcpq.entity

import jakarta.persistence.*
import java.io.Serializable
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "V_TC8F")
open class V_TC8F {
    @Id
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

    @Column(name = "ATC8F1")
    var atc8f1: String? = null

    @Column(name = "ATC8F2")
    var atc8f2: BigDecimal? = null

    @Column(name = "ATC8F3")
    var atc8f3: LocalDate? = null

    @Column(name = "ATC8F4")
    var atc8f4: String? = null

    @Column(name = "AAE100")
    var aae100: String? = null

    @Column(name = "AAE013")
    var aae013: String? = null

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

    @Column(name = "ATC8F5")
    var atc8f5: String? = null

    @Column(name = "ATC8F6")
    var atc8f6: String? = null

    @Column(name = "ATC8F7")
    var atc8f7: BigDecimal? = null

    @Column(name = "ATC8F8")
    var atc8f8: String? = null

    @Column(name = "ATC8F9")
    var atc8f9: BigDecimal? = null

    @Column(name = "ATC8FA")
    var atc8fa: String? = null

    @Column(name = "ATC8FB")
    var atc8fb: BigDecimal? = null

    @Column(name = "TYPE")
    var type: String? = null

    @Column(name = "TABLENAME")
    var tablename: String? = null

    @Column(name = "FLAG")
    var flag: String? = null

    @Column(name = "AAB00E")
    var aab00e: String? = null

}
