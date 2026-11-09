package com.jlrc.rcpq.entity

import jakarta.persistence.*
import java.io.Serializable
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "TJAC2")
open class TJAC2 {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_TJAC2_TJAC20")
    @SequenceGenerator(name = "SEQ_TJAC2_TJAC20", sequenceName = "SEQ_TJAC2_TJAC20", allocationSize = 1)
    @Column(name = "TJAC20")
    var tjac20: Long? = null

    @Column(name = "AAA021")
    var aaa021: String? = null

    @Column(name = "AAE019")
    var aae019: String? = null

    @Column(name = "AAE011")
    var aae011: String? = null

    @Column(name = "AAE017")
    var aae017: String? = null

    @Column(name = "AAE022")
    var aae022: String? = null

    @Column(name = "AAA020")
    var aaa020: String? = null

    @Column(name = "AAE020")
    var aae020: String? = null

    @Column(name = "TJAC22")
    var tjac22: Long? = null

    @Column(name = "TJAC23")
    var tjac23: Long? = null

    @Column(name = "TJAC24")
    var tjac24: Long? = null

    @Column(name = "TJAC25")
    var tjac25: Long? = null

    @Column(name = "TJAC29")
    var tjac29: Long? = null

    @Column(name = "TJAC2A")
    var tjac2a: Long? = null

    @Column(name = "TJAC2B")
    var tjac2b: Long? = null

    @Column(name = "TJAC2C")
    var tjac2c: Long? = null

    @Column(name = "TJAC2D")
    var tjac2d: Long? = null

    @Column(name = "TJAC2E")
    var tjac2e: Long? = null

    @Column(name = "TJAC2F")
    var tjac2f: Long? = null

    @Column(name = "TJAC2G")
    var tjac2g: Long? = null

    @Column(name = "TJAC2H")
    var tjac2h: Long? = null

    @Column(name = "TJAC2I")
    var tjac2i: Long? = null

    @Column(name = "TJAC2J")
    var tjac2j: Long? = null

    @Column(name = "TJAC2K")
    var tjac2k: Long? = null

    @Column(name = "TJAC2L")
    var tjac2l: Long? = null

    @Column(name = "TJAC2M")
    var tjac2m: Long? = null

    @Column(name = "TJAC2N")
    var tjac2n: Long? = null

    @Column(name = "TJAC2O")
    var tjac2o: Long? = null

    @Column(name = "TJAC2P")
    var tjac2p: Long? = null

    @Column(name = "AAE001")
    var aae001: Long? = null

    @Column(name = "BAZ001")
    var baz001: Long? = null

    @Column(name = "BAZ002")
    var baz002: Long? = null

    @Column(name = "TJAC26")
    var tjac26: BigDecimal? = null

    @Column(name = "TJAC27")
    var tjac27: BigDecimal? = null

    @Column(name = "TJAC28")
    var tjac28: BigDecimal? = null

    @Column(name = "AAE036")
    var aae036: LocalDateTime? = null

}
