package com.jlrc.rcpq.entity

import jakarta.persistence.*
import java.io.Serializable
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "TJQC1")
open class TJQC1 {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_TJQC1_TJQC10")
    @SequenceGenerator(name = "SEQ_TJQC1_TJQC10", sequenceName = "SEQ_TJQC1_TJQC10", allocationSize = 1)
    @Column(name = "TJQC10")
    var tjqc10: Long? = null

    @Column(name = "AAA020")
    var aaa020: String? = null

    @Column(name = "AAA021")
    var aaa021: String? = null

    @Column(name = "AAE001")
    var aae001: Long? = null

    @Column(name = "AAE002")
    var aae002: String? = null

    @Column(name = "TJQC11")
    var tjqc11: String? = null

    @Column(name = "TJQC12")
    var tjqc12: String? = null

    @Column(name = "TJQC13")
    var tjqc13: Long? = null

    @Column(name = "TJQC14")
    var tjqc14: Long? = null

    @Column(name = "TJQC15")
    var tjqc15: Long? = null

    @Column(name = "TJQC16")
    var tjqc16: Long? = null

    @Column(name = "TJQC17")
    var tjqc17: Long? = null

    @Column(name = "TJQC18")
    var tjqc18: Long? = null

    @Column(name = "TJQC19")
    var tjqc19: Long? = null

    @Column(name = "TJQC1A")
    var tjqc1a: Long? = null

    @Column(name = "TJQC1B")
    var tjqc1b: Long? = null

    @Column(name = "TJQC1C")
    var tjqc1c: Long? = null

    @Column(name = "TJQC1D")
    var tjqc1d: Long? = null

    @Column(name = "TJQC1E")
    var tjqc1e: Long? = null

    @Column(name = "TJQC1F")
    var tjqc1f: Long? = null

    @Column(name = "TJQC1G")
    var tjqc1g: Long? = null

    @Column(name = "TJQC1H")
    var tjqc1h: Long? = null

    @Column(name = "TJQC1I")
    var tjqc1i: Long? = null

    @Column(name = "TJQC1J")
    var tjqc1j: Long? = null

    @Column(name = "TJQC1K")
    var tjqc1k: Long? = null

    @Column(name = "TJQC1L")
    var tjqc1l: Long? = null

    @Column(name = "TJQC1M")
    var tjqc1m: String? = null

    @Column(name = "TJQC1N")
    var tjqc1n: String? = null

    @Column(name = "TJQC1P")
    var tjqc1p: String? = null

    @Column(name = "TJQC1Q")
    var tjqc1q: String? = null

    @Column(name = "TJQC1R")
    var tjqc1r: LocalDate? = null

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
