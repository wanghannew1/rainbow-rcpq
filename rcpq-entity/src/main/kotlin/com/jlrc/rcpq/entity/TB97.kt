package com.jlrc.rcpq.entity

import jakarta.persistence.*
import java.io.Serializable
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "TB97")
open class TB97 {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_TB97_ATB970")
    @SequenceGenerator(name = "SEQ_TB97_ATB970", sequenceName = "SEQ_TB97_ATB970", allocationSize = 1)
    @Column(name = "ATB970")
    var atb970: Long? = null

    @Column(name = "BAZ001")
    var baz001: Long? = null

    @Column(name = "BAZ002")
    var baz002: Long? = null

    @Column(name = "ATB970_1")
    var atb970_1: Long? = null

    @Column(name = "ATB930")
    var atb930: Long? = null

    @Column(name = "ATB931")
    var atb931: String? = null

    @Column(name = "AAB001")
    var aab001: Long? = null

    @Column(name = "AAB004")
    var aab004: String? = null

    @Column(name = "AAA030")
    var aaa030: Long? = null

    @Column(name = "AAA031")
    var aaa031: String? = null

    @Column(name = "AAC001")
    var aac001: Long? = null

    @Column(name = "ATC900")
    var atc900: Long? = null

    @Column(name = "AAC003")
    var aac003: String? = null

    @Column(name = "AAC002")
    var aac002: String? = null

    @Column(name = "ATB971")
    var atb971: BigDecimal? = null

    @Column(name = "ATB972")
    var atb972: BigDecimal? = null

    @Column(name = "ATB973_1")
    var atb973_1: BigDecimal? = null

    @Column(name = "ATB973")
    var atb973: BigDecimal? = null

    @Column(name = "ATB974")
    var atb974: LocalDate? = null

    @Column(name = "ATB975")
    var atb975: String? = null

    @Column(name = "ATB976")
    var atb976: String? = null

    @Column(name = "DSTATUS")
    var dstatus: String? = null

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

    @Column(name = "ATB97Y")
    var atb97y: String? = null

    @Column(name = "ATB97Z")
    var atb97z: String? = null

}
