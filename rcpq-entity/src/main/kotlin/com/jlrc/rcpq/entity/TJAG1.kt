package com.jlrc.rcpq.entity

import jakarta.persistence.*
import java.io.Serializable
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "TJAG1")
open class TJAG1 {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_TJAG1_TJAG10")
    @SequenceGenerator(name = "SEQ_TJAG1_TJAG10", sequenceName = "SEQ_TJAG1_TJAG10", allocationSize = 1)
    @Column(name = "TJAG10")
    var tjag10: Long? = null

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

    @Column(name = "TJAG11")
    var tjag11: String? = null

    @Column(name = "TJAG12")
    var tjag12: String? = null

    @Column(name = "TJAG13")
    var tjag13: BigDecimal? = null

    @Column(name = "TJAG14")
    var tjag14: Long? = null

    @Column(name = "TJAG15")
    var tjag15: Long? = null

    @Column(name = "TJAG16")
    var tjag16: Long? = null

    @Column(name = "TJAG17")
    var tjag17: Long? = null

    @Column(name = "TJAG18")
    var tjag18: BigDecimal? = null

    @Column(name = "TJAG19")
    var tjag19: BigDecimal? = null

    @Column(name = "TJAG1A")
    var tjag1a: Long? = null

    @Column(name = "TJAG1B")
    var tjag1b: Long? = null

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
