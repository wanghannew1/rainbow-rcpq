package com.jlrc.rcpq.entity

import jakarta.persistence.*
import java.io.Serializable
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "TJ0Z1")
open class TJ0Z1 {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_TJ0Z1_TJID")
    @SequenceGenerator(name = "SEQ_TJ0Z1_TJID", sequenceName = "SEQ_TJ0Z1_TJID", allocationSize = 1)
    @Column(name = "TJID")
    var tjid: String? = null

    @Column(name = "AAE002")
    var aae002: Long? = null

    @Column(name = "ACCOUNT")
    var account: String? = null

    @Column(name = "AAA030")
    var aaa030: Long? = null

    @Column(name = "AAA031")
    var aaa031: String? = null

    @Column(name = "AAB001")
    var aab001: Long? = null

    @Column(name = "AAB004")
    var aab004: String? = null

    @Column(name = "DTYPE")
    var dtype: String? = null

    @Column(name = "ATJ001")
    var atj001: String? = null

    @Column(name = "ATJ002")
    var atj002: String? = null

    @Column(name = "ATJ003")
    var atj003: String? = null

    @Column(name = "ATJ004")
    var atj004: String? = null

    @Column(name = "ATJ005")
    var atj005: String? = null

    @Column(name = "ATJ006")
    var atj006: String? = null

    @Column(name = "ATJ007")
    var atj007: String? = null

    @Column(name = "ATJ008")
    var atj008: String? = null

    @Column(name = "ATJ009")
    var atj009: String? = null

    @Column(name = "ATJ00A")
    var atj00a: Long? = null

    @Column(name = "ATJ00B")
    var atj00b: Long? = null

    @Column(name = "ATJ00C")
    var atj00c: Long? = null

    @Column(name = "ATJ00D")
    var atj00d: Long? = null

    @Column(name = "ATJ00E")
    var atj00e: Long? = null

    @Column(name = "ATJ00F")
    var atj00f: Long? = null

    @Column(name = "ATJ00G")
    var atj00g: Long? = null

    @Column(name = "ATJ00H")
    var atj00h: Long? = null

    @Column(name = "ATJ00I")
    var atj00i: Long? = null

    @Column(name = "ATJ00J")
    var atj00j: Long? = null

    @Column(name = "ATJ00K")
    var atj00k: String? = null

    @Column(name = "ATJ00L")
    var atj00l: String? = null

    @Column(name = "ATJ00M")
    var atj00m: String? = null

    @Column(name = "ATJ00N")
    var atj00n: Long? = null

    @Column(name = "ATJ00O")
    var atj00o: BigDecimal? = null

    @Column(name = "ATJ00P")
    var atj00p: String? = null

    @Column(name = "ATJ00Q")
    var atj00q: String? = null

    @Column(name = "ATJ00R")
    var atj00r: String? = null

    @Column(name = "ATJ00S")
    var atj00s: Long? = null

    @Column(name = "ATJ00T")
    var atj00t: Long? = null

    @Column(name = "ATJ00U")
    var atj00u: Long? = null

    @Column(name = "ATJ00V")
    var atj00v: String? = null

    @Column(name = "ATJ00W")
    var atj00w: BigDecimal? = null

    @Column(name = "ATJ00X")
    var atj00x: String? = null

    @Column(name = "IFLOCK")
    var iflock: String? = null

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

}
