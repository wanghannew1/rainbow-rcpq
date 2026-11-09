package com.jlrc.rcpq.entity

import jakarta.persistence.*
import java.io.Serializable
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "TC0A")
open class TC0A {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_TC0A_ATC0A0")
    @SequenceGenerator(name = "SEQ_TC0A_ATC0A0", sequenceName = "SEQ_TC0A_ATC0A0", allocationSize = 1)
    @Column(name = "ATC0A0")
    var atc0a0: Long? = null

    @Column(name = "AAE002")
    var aae002: Long? = null

    @Column(name = "ATC900")
    var atc900: Long? = null

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

    @Column(name = "AAA020")
    var aaa020: String? = null

    @Column(name = "AAA021")
    var aaa021: String? = null

    @Column(name = "AAA001")
    var aaa001: String? = null

    @Column(name = "AAC003")
    var aac003: String? = null

    @Column(name = "AAC001")
    var aac001: Long? = null

    @Column(name = "AAC002")
    var aac002: String? = null

    @Column(name = "ATC905")
    var atc905: BigDecimal? = null

    @Column(name = "AAA303")
    var aaa303: BigDecimal? = null

    @Column(name = "AAA303_1")
    var aaa303_1: BigDecimal? = null

    @Column(name = "ATC0A1")
    var atc0a1: String? = null

    @Column(name = "ATC0A2")
    var atc0a2: Long? = null

    @Column(name = "ATC0A3")
    var atc0a3: Long? = null

    @Column(name = "ATB90S_DW")
    var atb90s_dw: BigDecimal? = null

    @Column(name = "ATB90S_GR")
    var atb90s_gr: BigDecimal? = null

    @Column(name = "ABP00C")
    var abp00c: String? = null

    @Column(name = "ABP00D")
    var abp00d: String? = null

    @Column(name = "ABP00E")
    var abp00e: String? = null

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

    @Column(name = "AAE114")
    var aae114: String? = null

    @Column(name = "AAE113")
    var aae113: String? = null

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

    @Column(name = "ATC90W")
    var atc90w: String? = null

}
