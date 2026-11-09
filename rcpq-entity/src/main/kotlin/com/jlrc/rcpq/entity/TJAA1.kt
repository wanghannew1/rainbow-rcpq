package com.jlrc.rcpq.entity

import jakarta.persistence.*
import java.io.Serializable
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "TJAA1")
open class TJAA1 {
    @Id
    @Column(name = "TJAA10")
    var tjaa10: Long? = null

    @Column(name = "TJ0000")
    var tj0000: Long? = null

    @Column(name = "AAA020")
    var aaa020: String? = null

    @Column(name = "AAA021")
    var aaa021: String? = null

    @Column(name = "TJ000Y")
    var tj000y: String? = null

    @Column(name = "AAE001")
    var aae001: Long? = null

    @Column(name = "AAE00A")
    var aae00a: Long? = null

    @Column(name = "AAE002")
    var aae002: String? = null

    @Column(name = "TJ000U")
    var tj000u: Long? = null

    @Column(name = "TJAA11")
    var tjaa11: String? = null

    @Column(name = "TJAA12")
    var tjaa12: String? = null

    @Column(name = "TJAA13")
    var tjaa13: Long? = null

    @Column(name = "TJAA14")
    var tjaa14: Long? = null

    @Column(name = "TJAA15")
    var tjaa15: Long? = null

    @Column(name = "TJAA16")
    var tjaa16: Long? = null

    @Column(name = "TJAA17")
    var tjaa17: Long? = null

    @Column(name = "TJAA18")
    var tjaa18: Long? = null

    @Column(name = "TJAA19")
    var tjaa19: Long? = null

    @Column(name = "TJAA1A")
    var tjaa1a: Long? = null

    @Column(name = "TJAA1B")
    var tjaa1b: Long? = null

    @Column(name = "TJAA1C")
    var tjaa1c: Long? = null

    @Column(name = "TJAA1D")
    var tjaa1d: Long? = null

    @Column(name = "TJAA1E")
    var tjaa1e: Long? = null

    @Column(name = "TJAA1F")
    var tjaa1f: Long? = null

    @Column(name = "TJAA1G")
    var tjaa1g: Long? = null

    @Column(name = "TJAA1H")
    var tjaa1h: Long? = null

    @Column(name = "TJAA1J")
    var tjaa1j: Long? = null

    @Column(name = "TJAA1K")
    var tjaa1k: BigDecimal? = null

    @Column(name = "AAE013")
    var aae013: String? = null

    @Column(name = "TJAA1M")
    var tjaa1m: String? = null

    @Column(name = "TJAA1N")
    var tjaa1n: String? = null

    @Column(name = "TJAA1P")
    var tjaa1p: String? = null

    @Column(name = "TJAA1Q")
    var tjaa1q: String? = null

    @Column(name = "TJAA1R")
    var tjaa1r: LocalDate? = null

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
