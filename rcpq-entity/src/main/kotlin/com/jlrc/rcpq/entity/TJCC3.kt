package com.jlrc.rcpq.entity

import jakarta.persistence.*
import java.io.Serializable
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "TJCC3")
open class TJCC3 {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_TJCC3_TJCC30")
    @SequenceGenerator(name = "SEQ_TJCC3_TJCC30", sequenceName = "SEQ_TJCC3_TJCC30", allocationSize = 1)
    @Column(name = "TJCC30")
    var tjcc30: Long? = null

    @Column(name = "BAZ001")
    var baz001: Long? = null

    @Column(name = "BAZ002")
    var baz002: Long? = null

    @Column(name = "TJ0000")
    var tj0000: Long? = null

    @Column(name = "TJCC31")
    var tjcc31: String? = null

    @Column(name = "TJCC32")
    var tjcc32: String? = null

    @Column(name = "TJCC33")
    var tjcc33: Long? = null

    @Column(name = "TJCC34")
    var tjcc34: Long? = null

    @Column(name = "TJCC35")
    var tjcc35: Long? = null

    @Column(name = "TJCC36")
    var tjcc36: Long? = null

    @Column(name = "TJCC37")
    var tjcc37: Long? = null

    @Column(name = "TJCC38")
    var tjcc38: BigDecimal? = null

    @Column(name = "TJCC39")
    var tjcc39: BigDecimal? = null

    @Column(name = "TJCC3A")
    var tjcc3a: BigDecimal? = null

    @Column(name = "TJCC3B")
    var tjcc3b: BigDecimal? = null

    @Column(name = "TJCC3C")
    var tjcc3c: BigDecimal? = null

    @Column(name = "TJCC3D")
    var tjcc3d: Long? = null

    @Column(name = "TJCC3E")
    var tjcc3e: Long? = null

    @Column(name = "TJCC3F")
    var tjcc3f: Long? = null

    @Column(name = "TJCC3G")
    var tjcc3g: Long? = null

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
