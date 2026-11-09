package com.jlrc.rcpq.entity

import jakarta.persistence.*
import java.io.Serializable
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "TJ203")
open class TJ203 {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_TJ203_TJ2030")
    @SequenceGenerator(name = "SEQ_TJ203_TJ2030", sequenceName = "SEQ_TJ203_TJ2030", allocationSize = 1)
    @Column(name = "TJ2030")
    var tj2030: Long? = null

    @Column(name = "BAZ001")
    var baz001: Long? = null

    @Column(name = "BAZ002")
    var baz002: Long? = null

    @Column(name = "AAA020")
    var aaa020: String? = null

    @Column(name = "AAE030")
    var aae030: String? = null

    @Column(name = "AAE031")
    var aae031: String? = null

    @Column(name = "CC2000")
    var cc2000: String? = null

    @Column(name = "TJ2031")
    var tj2031: String? = null

    @Column(name = "TJ2032")
    var tj2032: String? = null

    @Column(name = "TJ2033")
    var tj2033: String? = null

    @Column(name = "TJ2034")
    var tj2034: String? = null

    @Column(name = "TJ2035")
    var tj2035: String? = null

    @Column(name = "TJ2036")
    var tj2036: String? = null

    @Column(name = "TJ2037")
    var tj2037: String? = null

    @Column(name = "TJ2038")
    var tj2038: String? = null

    @Column(name = "TJ2039")
    var tj2039: String? = null

    @Column(name = "TJ203A")
    var tj203a: BigDecimal? = null

    @Column(name = "TJ203B")
    var tj203b: BigDecimal? = null

    @Column(name = "TJ203C")
    var tj203c: BigDecimal? = null

    @Column(name = "TJ203D")
    var tj203d: BigDecimal? = null

    @Column(name = "TJ203E")
    var tj203e: BigDecimal? = null

    @Column(name = "TJ203F")
    var tj203f: BigDecimal? = null

    @Column(name = "TJ203G")
    var tj203g: BigDecimal? = null

    @Column(name = "TJ203H")
    var tj203h: BigDecimal? = null

    @Column(name = "TJ203I")
    var tj203i: BigDecimal? = null

    @Column(name = "TJ203J")
    var tj203j: BigDecimal? = null

    @Column(name = "TJ203K")
    var tj203k: BigDecimal? = null

    @Column(name = "TJ203L")
    var tj203l: BigDecimal? = null

    @Column(name = "TJ203M")
    var tj203m: BigDecimal? = null

    @Column(name = "TJ203N")
    var tj203n: BigDecimal? = null

    @Column(name = "TJ203O")
    var tj203o: BigDecimal? = null

    @Column(name = "TJ203P")
    var tj203p: BigDecimal? = null

    @Column(name = "TJ203Q")
    var tj203q: BigDecimal? = null

    @Column(name = "TJ203R")
    var tj203r: BigDecimal? = null

    @Column(name = "TJ203S")
    var tj203s: BigDecimal? = null

    @Column(name = "TJ203T")
    var tj203t: BigDecimal? = null

    @Column(name = "TJ203U")
    var tj203u: BigDecimal? = null

    @Column(name = "TJ203V")
    var tj203v: BigDecimal? = null

    @Column(name = "TJ203W")
    var tj203w: BigDecimal? = null

    @Column(name = "TJ203X")
    var tj203x: BigDecimal? = null

    @Column(name = "TJ203Y")
    var tj203y: BigDecimal? = null

    @Column(name = "TJ203Z")
    var tj203z: BigDecimal? = null

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
