package com.jlrc.rcpq.entity

import jakarta.persistence.*
import java.io.Serializable
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "AA82")
open class AA82 {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_AA82_AAA820")
    @SequenceGenerator(name = "SEQ_AA82_AAA820", sequenceName = "SEQ_AA82_AAA820", allocationSize = 1)
    @Column(name = "AAA820")
    var aaa820: Long? = null

    @Column(name = "AAA821")
    var aaa821: BigDecimal? = null

    @Column(name = "AAA822")
    var aaa822: BigDecimal? = null

    @Column(name = "AAA823")
    var aaa823: BigDecimal? = null

    @Column(name = "AAA824")
    var aaa824: BigDecimal? = null

    @Column(name = "AAA825")
    var aaa825: BigDecimal? = null

    @Column(name = "AAA826")
    var aaa826: BigDecimal? = null

    @Column(name = "AAA827")
    var aaa827: BigDecimal? = null

    @Column(name = "AAA828")
    var aaa828: BigDecimal? = null

    @Column(name = "AAA829")
    var aaa829: BigDecimal? = null

    @Column(name = "AAA82A")
    var aaa82a: BigDecimal? = null

    @Column(name = "AAA82B")
    var aaa82b: BigDecimal? = null

    @Column(name = "AAA82C")
    var aaa82c: BigDecimal? = null

    @Column(name = "AAA82D")
    var aaa82d: BigDecimal? = null

    @Column(name = "AAA82F")
    var aaa82f: BigDecimal? = null

    @Column(name = "AAA82G")
    var aaa82g: BigDecimal? = null

    @Column(name = "AAA82H")
    var aaa82h: BigDecimal? = null

    @Column(name = "AAA82I")
    var aaa82i: BigDecimal? = null

    @Column(name = "AAA82J")
    var aaa82j: BigDecimal? = null

    @Column(name = "AAA82K")
    var aaa82k: BigDecimal? = null

    @Column(name = "AAA82L")
    var aaa82l: BigDecimal? = null

    @Column(name = "AAA82M")
    var aaa82m: BigDecimal? = null

    @Column(name = "AAA82N")
    var aaa82n: BigDecimal? = null

    @Column(name = "AAA82P")
    var aaa82p: BigDecimal? = null

    @Column(name = "AAA82Q")
    var aaa82q: BigDecimal? = null

    @Column(name = "AAA82R")
    var aaa82r: BigDecimal? = null

    @Column(name = "AAA82S")
    var aaa82s: BigDecimal? = null

    @Column(name = "AAA82T")
    var aaa82t: BigDecimal? = null

    @Column(name = "AAA82U")
    var aaa82u: BigDecimal? = null

    @Column(name = "AAA82V")
    var aaa82v: BigDecimal? = null

    @Column(name = "AAA82W")
    var aaa82w: BigDecimal? = null

    @Column(name = "AAA82X")
    var aaa82x: BigDecimal? = null

    @Column(name = "AAA82Y")
    var aaa82y: BigDecimal? = null

    @Column(name = "AAA82Z")
    var aaa82z: BigDecimal? = null

    @Column(name = "AAA82O")
    var aaa82o: BigDecimal? = null

}
