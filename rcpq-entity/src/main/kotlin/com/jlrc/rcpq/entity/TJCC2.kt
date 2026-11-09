package com.jlrc.rcpq.entity

import jakarta.persistence.*
import java.io.Serializable
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "TJCC2")
open class TJCC2 {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_TJCC2_TJCC20")
    @SequenceGenerator(name = "SEQ_TJCC2_TJCC20", sequenceName = "SEQ_TJCC2_TJCC20", allocationSize = 1)
    @Column(name = "TJCC20")
    var tjcc20: Long? = null

    @Column(name = "BAZ001")
    var baz001: Long? = null

    @Column(name = "BAZ002")
    var baz002: Long? = null

    @Column(name = "TJ0000")
    var tj0000: Long? = null

    @Column(name = "TJCC21")
    var tjcc21: String? = null

    @Column(name = "TJCC22")
    var tjcc22: String? = null

    @Column(name = "TJCC23")
    var tjcc23: Long? = null

    @Column(name = "TJCC24")
    var tjcc24: Long? = null

    @Column(name = "TJCC25")
    var tjcc25: String? = null

    @Column(name = "TJCC26")
    var tjcc26: Long? = null

    @Column(name = "TJCC27")
    var tjcc27: BigDecimal? = null

    @Column(name = "TJCC28")
    var tjcc28: BigDecimal? = null

    @Column(name = "TJCC29")
    var tjcc29: BigDecimal? = null

    @Column(name = "TJCC2A")
    var tjcc2a: Long? = null

    @Column(name = "TJCC2B")
    var tjcc2b: Long? = null

    @Column(name = "TJCC2C")
    var tjcc2c: String? = null

    @Column(name = "TJCC2D")
    var tjcc2d: String? = null

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
