package com.jlrc.rcpq.entity

import jakarta.persistence.*
import java.io.Serializable
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "TC02")
open class TC02 {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_TC02_ATC020")
    @SequenceGenerator(name = "SEQ_TC02_ATC020", sequenceName = "SEQ_TC02_ATC020", allocationSize = 1)
    @Column(name = "ATC020")
    var atc020: Long? = null

    @Column(name = "BAZ001")
    var baz001: Long? = null

    @Column(name = "BAZ002")
    var baz002: Long? = null

    @Column(name = "ATC900")
    var atc900: Long? = null

    @Column(name = "AAC001")
    var aac001: Long? = null

    @Column(name = "AAC003")
    var aac003: String? = null

    @Column(name = "AAC002")
    var aac002: String? = null

    @Column(name = "AAB001")
    var aab001: Long? = null

    @Column(name = "AAB004")
    var aab004: String? = null

    @Column(name = "AAC004")
    var aac004: String? = null

    @Column(name = "AAC006")
    var aac006: LocalDate? = null

    @Column(name = "ATC021")
    var atc021: LocalDate? = null

    @Column(name = "ATC022")
    var atc022: LocalDate? = null

    @Column(name = "ATC023")
    var atc023: LocalDate? = null

    @Column(name = "ATC024")
    var atc024: BigDecimal? = null

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

    @Column(name = "ATC031")
    var atc031: LocalDate? = null

    @Column(name = "ATC032")
    var atc032: String? = null

    @Column(name = "ATC033")
    var atc033: BigDecimal? = null

    @Column(name = "AAE034")
    var aae034: String? = null

    @Column(name = "ATC041")
    var atc041: LocalDate? = null

    @Column(name = "ATC042")
    var atc042: BigDecimal? = null

    @Column(name = "ATC043")
    var atc043: BigDecimal? = null

    @Column(name = "AAE044")
    var aae044: String? = null

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

}
