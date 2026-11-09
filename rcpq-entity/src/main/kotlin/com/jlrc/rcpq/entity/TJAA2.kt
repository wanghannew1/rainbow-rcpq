package com.jlrc.rcpq.entity

import jakarta.persistence.*
import java.io.Serializable
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "TJAA2")
open class TJAA2 {
    @Id
    @Column(name = "TJAA20")
    var tjaa20: Long? = null

    @Column(name = "TJ0000")
    var tj0000: Long? = null

    @Column(name = "AAA020")
    var aaa020: String? = null

    @Column(name = "AAA021")
    var aaa021: String? = null

    @Column(name = "AAE002")
    var aae002: String? = null

    @Column(name = "TJ000U")
    var tj000u: Long? = null

    @Column(name = "TJAA21")
    var tjaa21: String? = null

    @Column(name = "TJAA22")
    var tjaa22: String? = null

    @Column(name = "TJAA23")
    var tjaa23: Long? = null

    @Column(name = "TJAA24")
    var tjaa24: Long? = null

    @Column(name = "TJAA25")
    var tjaa25: Long? = null

    @Column(name = "TJAA26")
    var tjaa26: Long? = null

    @Column(name = "TJAA27")
    var tjaa27: Long? = null

    @Column(name = "TJAA28")
    var tjaa28: Long? = null

    @Column(name = "TJAA29")
    var tjaa29: Long? = null

    @Column(name = "TJAA2A")
    var tjaa2a: Long? = null

    @Column(name = "AAE013")
    var aae013: String? = null

    @Column(name = "TJAA2M")
    var tjaa2m: String? = null

    @Column(name = "TJAA2N")
    var tjaa2n: String? = null

    @Column(name = "TJAA2P")
    var tjaa2p: String? = null

    @Column(name = "TJAA2Q")
    var tjaa2q: String? = null

    @Column(name = "TJAA2R")
    var tjaa2r: LocalDate? = null

    @Column(name = "TJ000X")
    var tj000x: String? = null

    @Column(name = "TJ000W")
    var tj000w: String? = null

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

}
