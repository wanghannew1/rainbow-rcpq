package com.jlrc.rcpq.entity

import jakarta.persistence.*
import java.io.Serializable
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "TC9Q_GS")
open class TC9Q_GS {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_TC9Q_GS_ID")
    @SequenceGenerator(name = "SEQ_TC9Q_GS_ID", sequenceName = "SEQ_TC9Q_GS_ID", allocationSize = 1)
    @Column(name = "ID")
    var id: Long? = null

    @Column(name = "ATC9Q0")
    var atc9q0: Long? = null

    @Column(name = "ATC9QZ")
    var atc9qz: String? = null

    @Column(name = "ATC9QD")
    var atc9qd: BigDecimal? = null

    @Column(name = "ATC9QV")
    var atc9qv: BigDecimal? = null

    @Column(name = "ATC9QW")
    var atc9qw: BigDecimal? = null

    @Column(name = "ATC9QX")
    var atc9qx: String? = null

    @Column(name = "ATC9QY")
    var atc9qy: String? = null

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

    @Column(name = "ATC9QZX")
    var atc9qzx: String? = null

    @Column(name = "AAEA11")
    var aaea11: String? = null

    @Column(name = "AAEA36")
    var aaea36: LocalDate? = null

    @Column(name = "AAEA19")
    var aaea19: String? = null

    @Column(name = "ATC9QZY")
    var atc9qzy: BigDecimal? = null

    @Column(name = "ATC9QZW")
    var atc9qzw: String? = null

}
