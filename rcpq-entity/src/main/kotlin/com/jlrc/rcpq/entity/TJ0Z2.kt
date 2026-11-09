package com.jlrc.rcpq.entity

import jakarta.persistence.*
import java.io.Serializable
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "TJ0Z2")
open class TJ0Z2 {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_TJ0Z2_TJID")
    @SequenceGenerator(name = "SEQ_TJ0Z2_TJID", sequenceName = "SEQ_TJ0Z2_TJID", allocationSize = 1)
    @Column(name = "TJID")
    var tjid: String? = null

    @Column(name = "ACCOUNT")
    var account: String? = null

    @Column(name = "NAME")
    var name: String? = null

    @Column(name = "AAE002")
    var aae002: Long? = null

    @Column(name = "AAA030")
    var aaa030: Long? = null

    @Column(name = "AAA031")
    var aaa031: String? = null

    @Column(name = "ATJ001")
    var atj001: Long? = null

    @Column(name = "ATJ002")
    var atj002: Long? = null

    @Column(name = "ATJ003")
    var atj003: Long? = null

    @Column(name = "ATJ004")
    var atj004: Long? = null

    @Column(name = "ATJ005")
    var atj005: Long? = null

    @Column(name = "ATJ006")
    var atj006: Long? = null

    @Column(name = "ATJ007")
    var atj007: Long? = null

    @Column(name = "ATJ008")
    var atj008: Long? = null

    @Column(name = "ATJ009")
    var atj009: Long? = null

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
    var atj00k: Long? = null

    @Column(name = "ATJ00L")
    var atj00l: Long? = null

    @Column(name = "ATJ00M")
    var atj00m: Long? = null

    @Column(name = "ATJ00N")
    var atj00n: Long? = null

    @Column(name = "ATJ00O")
    var atj00o: Long? = null

    @Column(name = "ATJ00P")
    var atj00p: Long? = null

    @Column(name = "ATJ00Q")
    var atj00q: Long? = null

    @Column(name = "ATJ00R")
    var atj00r: Long? = null

    @Column(name = "ATJ00S")
    var atj00s: Long? = null

    @Column(name = "ATJ00T")
    var atj00t: Long? = null

    @Column(name = "ATJ00U")
    var atj00u: Long? = null

    @Column(name = "ATJ00V")
    var atj00v: Long? = null

    @Column(name = "ATJ00W")
    var atj00w: Long? = null

    @Column(name = "ATJ00X")
    var atj00x: Long? = null

    @Column(name = "ATJ00Y")
    var atj00y: Long? = null

    @Column(name = "ATJ00Z")
    var atj00z: Long? = null

    @Column(name = "ATJ011")
    var atj011: Long? = null

    @Column(name = "ATJ012")
    var atj012: Long? = null

    @Column(name = "ATJ013")
    var atj013: Long? = null

    @Column(name = "ATJ014")
    var atj014: Long? = null

    @Column(name = "ATJ015")
    var atj015: Long? = null

    @Column(name = "ATJ016")
    var atj016: Long? = null

    @Column(name = "ATJ017")
    var atj017: Long? = null

    @Column(name = "ATJ018")
    var atj018: Long? = null

    @Column(name = "ATJ019")
    var atj019: Long? = null

    @Column(name = "ATJ01A")
    var atj01a: Long? = null

    @Column(name = "ATJ01B")
    var atj01b: Long? = null

    @Column(name = "ATJ01C")
    var atj01c: Long? = null

    @Column(name = "ATJ01D")
    var atj01d: Long? = null

    @Column(name = "AAE011")
    var aae011: String? = null

    @Column(name = "AAE019")
    var aae019: String? = null

    @Column(name = "AAE017")
    var aae017: String? = null

    @Column(name = "AAE036")
    var aae036: LocalDateTime? = null

    @Column(name = "AAE020")
    var aae020: String? = null

    @Column(name = "AAE022")
    var aae022: String? = null

}
