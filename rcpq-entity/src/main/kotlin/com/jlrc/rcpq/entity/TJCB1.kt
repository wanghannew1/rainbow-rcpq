package com.jlrc.rcpq.entity

import jakarta.persistence.*
import java.io.Serializable
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "TJCB1")
open class TJCB1 {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_TJCB1_TJCB10")
    @SequenceGenerator(name = "SEQ_TJCB1_TJCB10", sequenceName = "SEQ_TJCB1_TJCB10", allocationSize = 1)
    @Column(name = "TJCB10")
    var tjcb10: Long? = null

    @Column(name = "BAZ001")
    var baz001: Long? = null

    @Column(name = "BAZ002")
    var baz002: Long? = null

    @Column(name = "TJ0000")
    var tj0000: Long? = null

    @Column(name = "ACB118")
    var acb118: Long? = null

    @Column(name = "ACB119")
    var acb119: Long? = null

    @Column(name = "AAB001")
    var aab001: Long? = null

    @Column(name = "AAB004")
    var aab004: String? = null

    @Column(name = "TJCB11")
    var tjcb11: BigDecimal? = null

    @Column(name = "TJCB12")
    var tjcb12: String? = null

    @Column(name = "TJCB13")
    var tjcb13: String? = null

    @Column(name = "TJCB14")
    var tjcb14: String? = null

    @Column(name = "TJCB15")
    var tjcb15: String? = null

    @Column(name = "TJCB16")
    var tjcb16: BigDecimal? = null

    @Column(name = "TJCB17")
    var tjcb17: Long? = null

    @Column(name = "TJCB18")
    var tjcb18: BigDecimal? = null

    @Column(name = "TJCB19")
    var tjcb19: Long? = null

    @Column(name = "TJCB1A")
    var tjcb1a: BigDecimal? = null

    @Column(name = "TJCB1B")
    var tjcb1b: Long? = null

    @Column(name = "TJCB1C")
    var tjcb1c: BigDecimal? = null

    @Column(name = "TJCB1D")
    var tjcb1d: Long? = null

    @Column(name = "TJCB1E")
    var tjcb1e: BigDecimal? = null

    @Column(name = "TJCB1F")
    var tjcb1f: Long? = null

    @Column(name = "TJCB1G")
    var tjcb1g: BigDecimal? = null

    @Column(name = "TJCB1H")
    var tjcb1h: Long? = null

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

    @Column(name = "ACB142")
    var acb142: String? = null

    @Column(name = "ACB143")
    var acb143: String? = null

    @Column(name = "ACB144")
    var acb144: String? = null

    @Column(name = "ACB145")
    var acb145: String? = null

}
