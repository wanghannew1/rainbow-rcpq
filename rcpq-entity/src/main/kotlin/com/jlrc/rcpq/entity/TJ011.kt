package com.jlrc.rcpq.entity

import jakarta.persistence.*
import java.io.Serializable
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "TJ011")
open class TJ011 {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_TJ011_TJ0110")
    @SequenceGenerator(name = "SEQ_TJ011_TJ0110", sequenceName = "SEQ_TJ011_TJ0110", allocationSize = 1)
    @Column(name = "TJ0110")
    var tj0110: Long? = null

    @Column(name = "AAE002")
    var aae002: String? = null

    @Column(name = "TJ0111")
    var tj0111: String? = null

    @Column(name = "TJ0112")
    var tj0112: String? = null

    @Column(name = "TJ0113")
    var tj0113: Long? = null

    @Column(name = "TJ0114")
    var tj0114: Long? = null

    @Column(name = "TJ0115")
    var tj0115: Long? = null

    @Column(name = "TJ0116")
    var tj0116: Long? = null

    @Column(name = "TJ0117")
    var tj0117: Long? = null

    @Column(name = "TJ0118")
    var tj0118: String? = null

    @Column(name = "TJ0119")
    var tj0119: String? = null

    @Column(name = "TJ011A")
    var tj011a: String? = null

    @Column(name = "TJ011B")
    var tj011b: String? = null

    @Column(name = "TJ011C")
    var tj011c: LocalDate? = null

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

    @Column(name = "BAZ001")
    var baz001: Long? = null

    @Column(name = "BAZ002")
    var baz002: Long? = null

    @Column(name = "AAA030")
    var aaa030: BigDecimal? = null
    @Column(name = "AAA031")
    var aaa031: String? = null
    @Column(name = "TJ01111")
    var tj01111: BigDecimal? = null
    @Column(name = "TJ011D")
    var tj011d: BigDecimal? = null
    @Column(name = "TJ011E")
    var tj011e: BigDecimal? = null
    @Column(name = "TJ011F")
    var tj011f: BigDecimal? = null
    @Column(name = "TJ011G")
    var tj011g: BigDecimal? = null
    @Column(name = "TJ011H")
    var tj011h: BigDecimal? = null
    @Column(name = "TJ011I")
    var tj011i: BigDecimal? = null
    @Column(name = "TJ011J")
    var tj011j: BigDecimal? = null
    @Column(name = "TJ011K")
    var tj011k: BigDecimal? = null
    @Column(name = "TJ011L")
    var tj011l: BigDecimal? = null
    @Column(name = "TJ011M")
    var tj011m: BigDecimal? = null
    @Column(name = "TJ011N")
    var tj011n: BigDecimal? = null
    @Column(name = "TJ011O")
    var tj011o: BigDecimal? = null
    @Column(name = "TJ011P")
    var tj011p: BigDecimal? = null
    @Column(name = "TJ011Q")
    var tj011q: BigDecimal? = null
    @Column(name = "TJ011R")
    var tj011r: BigDecimal? = null
    @Column(name = "TJ011S")
    var tj011s: BigDecimal? = null
    @Column(name = "TJ011T")
    var tj011t: BigDecimal? = null
    @Column(name = "TJ011U")
    var tj011u: BigDecimal? = null
    @Column(name = "TJ011V")
    var tj011v: BigDecimal? = null
    @Column(name = "TJ011W")
    var tj011w: BigDecimal? = null
    @Column(name = "TJ011X")
    var tj011x: BigDecimal? = null
    @Column(name = "TJ011Y")
    var tj011y: BigDecimal? = null
    @Column(name = "TJ011Z")
    var tj011z: BigDecimal? = null
}
