package com.jlrc.rcpq.entity

import jakarta.persistence.*
import java.io.Serializable
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "V_TC97_DA")
open class V_TC97_DA {
    @Id
    @Column(name = "ATC970")
    var atc970: Long? = null

    @Column(name = "AAC003")
    var aac003: String? = null

    @Column(name = "AAC002")
    var aac002: String? = null

    @Column(name = "MC")
    var mc: String? = null

    @Column(name = "ATC97E")
    var atc97e: String? = null

    @Column(name = "ATC97F")
    var atc97f: String? = null

    @Column(name = "BAZ001")
    var baz001: Long? = null

    @Column(name = "BAZ002")
    var baz002: Long? = null

    @Column(name = "ID")
    var id: Long? = null

    @Column(name = "AAC001")
    var aac001: Long? = null

    @Column(name = "ATC971")
    var atc971: String? = null

    @Column(name = "ATC972")
    var atc972: LocalDate? = null

    @Column(name = "ATC975")
    var atc975: LocalDate? = null

    @Column(name = "ATC976")
    var atc976: String? = null

    @Column(name = "ATC97C")
    var atc97c: String? = null

    @Column(name = "ATC977")
    var atc977: String? = null

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

    @Column(name = "BAE011")
    var bae011: String? = null

    @Column(name = "BAE017")
    var bae017: String? = null

    @Column(name = "BAE036")
    var bae036: LocalDate? = null

    @Column(name = "BAE019")
    var bae019: String? = null

    @Column(name = "BAE020")
    var bae020: String? = null

    @Column(name = "BAE022")
    var bae022: String? = null

    @Column(name = "AAE013")
    var aae013: String? = null

    @Column(name = "AAA001")
    var aaa001: String? = null

    @Column(name = "AAA004")
    var aaa004: String? = null

    @Column(name = "ATC978")
    var atc978: String? = null

    @Column(name = "ATC979")
    var atc979: String? = null

    @Column(name = "ATC97B")
    var atc97b: String? = null

    @Column(name = "AAE113")
    var aae113: String? = null

    @Column(name = "ATC97A")
    var atc97a: String? = null

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

    @Column(name = "ATC97D")
    var atc97d: String? = null

    @Column(name = "ATC980")
    var atc980: Long? = null

    @Column(name = "AAB004")
    var aab004: String? = null

    @Column(name = "ATB90R")
    var atb90r: String? = null

}
