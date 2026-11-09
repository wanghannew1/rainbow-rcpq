package com.jlrc.rcpq.entity

import jakarta.persistence.*
import java.io.Serializable
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "TJAJ1")
open class TJAJ1 {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_TJAJ1_TJAJ10")
    @SequenceGenerator(name = "SEQ_TJAJ1_TJAJ10", sequenceName = "SEQ_TJAJ1_TJAJ10", allocationSize = 1)
    @Column(name = "TJAJ10")
    var tjaj10: Long? = null

    @Column(name = "BAZ001")
    var baz001: Long? = null

    @Column(name = "BAZ002")
    var baz002: Long? = null

    @Column(name = "TJ0000")
    var tj0000: Long? = null

    @Column(name = "TJKSSJ")
    var tjkssj: LocalDate? = null

    @Column(name = "TJJSSJ")
    var tjjssj: LocalDate? = null

    @Column(name = "AAA020")
    var aaa020: String? = null

    @Column(name = "AAA021")
    var aaa021: String? = null

    @Column(name = "TJAJ11")
    var tjaj11: String? = null

    @Column(name = "TJAJ12")
    var tjaj12: String? = null

    @Column(name = "TJAJ13")
    var tjaj13: Long? = null

    @Column(name = "TJAJ14")
    var tjaj14: Long? = null

    @Column(name = "TJAJ15")
    var tjaj15: Long? = null

    @Column(name = "TJAJ16")
    var tjaj16: Long? = null

    @Column(name = "TJAJ17")
    var tjaj17: Long? = null

    @Column(name = "TJAJ18")
    var tjaj18: Long? = null

    @Column(name = "TJAJ19")
    var tjaj19: Long? = null

    @Column(name = "TJAJ1A")
    var tjaj1a: Long? = null

    @Column(name = "TJAJ1B")
    var tjaj1b: Long? = null

    @Column(name = "TJAJ1C")
    var tjaj1c: Long? = null

    @Column(name = "TJAJ1D")
    var tjaj1d: Long? = null

    @Column(name = "TJAJ1E")
    var tjaj1e: Long? = null

    @Column(name = "TJAJ1F")
    var tjaj1f: Long? = null

    @Column(name = "TJAJ1G")
    var tjaj1g: Long? = null

    @Column(name = "TJAJ1H")
    var tjaj1h: Long? = null

    @Column(name = "TJAJ1I")
    var tjaj1i: Long? = null

    @Column(name = "TJAJ1J")
    var tjaj1j: Long? = null

    @Column(name = "TJAJ1K")
    var tjaj1k: Long? = null

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
