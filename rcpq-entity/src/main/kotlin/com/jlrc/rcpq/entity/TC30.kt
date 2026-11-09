package com.jlrc.rcpq.entity

import jakarta.persistence.*
import java.io.Serializable
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "TC30")
open class TC30 {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_TC30_ATC300")
    @SequenceGenerator(name = "SEQ_TC30_ATC300", sequenceName = "SEQ_TC30_ATC300", allocationSize = 1)
    @Column(name = "ATC300")
    var atc300: Long? = null

    @Column(name = "ATC900")
    var atc900: Long? = null

    @Column(name = "AAC002")
    var aac002: String? = null

    @Column(name = "AAC003")
    var aac003: String? = null

    @Column(name = "AAB001")
    var aab001: Long? = null

    @Column(name = "AAB004")
    var aab004: String? = null

    @Column(name = "ATB930")
    var atb930: Long? = null

    @Column(name = "ATB931")
    var atb931: String? = null

    @Column(name = "AAA030")
    var aaa030: Long? = null

    @Column(name = "AAA031")
    var aaa031: String? = null

    @Column(name = "AAE001")
    var aae001: Long? = null

    @Column(name = "ATC301")
    var atc301: BigDecimal? = null

    @Column(name = "ATC302")
    var atc302: BigDecimal? = null

    @Column(name = "ATC303")
    var atc303: BigDecimal? = null

    @Column(name = "ATC304")
    var atc304: BigDecimal? = null

    @Column(name = "ATC305")
    var atc305: BigDecimal? = null

    @Column(name = "ATC306")
    var atc306: BigDecimal? = null

    @Column(name = "ATC307")
    var atc307: BigDecimal? = null

    @Column(name = "ATC308")
    var atc308: BigDecimal? = null

    @Column(name = "ATC309")
    var atc309: BigDecimal? = null

    @Column(name = "ATC30A")
    var atc30a: BigDecimal? = null

    @Column(name = "ATC30B")
    var atc30b: BigDecimal? = null

    @Column(name = "ATC30C")
    var atc30c: BigDecimal? = null

    @Column(name = "ATC30D")
    var atc30d: BigDecimal? = null

    @Column(name = "ATC30E")
    var atc30e: BigDecimal? = null

    @Column(name = "ATC30F")
    var atc30f: BigDecimal? = null

    @Column(name = "ATC30G")
    var atc30g: BigDecimal? = null

    @Column(name = "AAC001")
    var aac001: Long? = null

    @Column(name = "ATC30H")
    var atc30h: BigDecimal? = null

    @Column(name = "ATC30I")
    var atc30i: BigDecimal? = null

}
