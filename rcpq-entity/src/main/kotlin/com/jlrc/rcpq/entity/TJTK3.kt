package com.jlrc.rcpq.entity

import jakarta.persistence.*
import java.io.Serializable
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "TJTK3")
open class TJTK3 {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_TJTK3_TJTK30")
    @SequenceGenerator(name = "SEQ_TJTK3_TJTK30", sequenceName = "SEQ_TJTK3_TJTK30", allocationSize = 1)
    @Column(name = "TJTK30")
    var tjtk30: Long? = null

    @Column(name = "AAE013")
    var aae013: String? = null

    @Column(name = "AAE001")
    var aae001: Long? = null

    @Column(name = "AAE00A")
    var aae00a: Long? = null

    @Column(name = "TJTK31")
    var tjtk31: String? = null

    @Column(name = "TJTK32")
    var tjtk32: String? = null

    @Column(name = "TJTK33")
    var tjtk33: String? = null

    @Column(name = "TJTK34")
    var tjtk34: String? = null

    @Column(name = "TJTK35")
    var tjtk35: String? = null

    @Column(name = "TJTK36")
    var tjtk36: String? = null

    @Column(name = "TJTK37")
    var tjtk37: String? = null

    @Column(name = "TJTK38")
    var tjtk38: String? = null

    @Column(name = "TJTK39")
    var tjtk39: String? = null

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
