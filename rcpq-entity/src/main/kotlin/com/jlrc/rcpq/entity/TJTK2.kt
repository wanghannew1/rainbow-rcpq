package com.jlrc.rcpq.entity

import jakarta.persistence.*
import java.io.Serializable
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "TJTK2")
open class TJTK2 {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_TJTK2_TJTK20")
    @SequenceGenerator(name = "SEQ_TJTK2_TJTK20", sequenceName = "SEQ_TJTK2_TJTK20", allocationSize = 1)
    @Column(name = "TJTK20")
    var tjtk20: Long? = null

    @Column(name = "ATB100")
    var atb100: Long? = null

    @Column(name = "ATB101")
    var atb101: String? = null

    @Column(name = "TJTK21")
    var tjtk21: String? = null

    @Column(name = "TJTK22")
    var tjtk22: Long? = null

    @Column(name = "AAE013")
    var aae013: String? = null

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

    @Column(name = "TJTK23")
    var tjtk23: LocalDate? = null

    @Column(name = "TJTK24")
    var tjtk24: LocalDate? = null

    @Column(name = "TJTK25")
    var tjtk25: Long? = null

    @Column(name = "AAE022")
    var aae022: String? = null

    @Column(name = "TJTK26")
    var tjtk26: String? = null

    @Column(name = "TJTK27")
    var tjtk27: String? = null

    @Column(name = "TJTK28")
    var tjtk28: String? = null

    @Column(name = "TJTK29")
    var tjtk29: LocalDate? = null

    @Column(name = "TJTK2A")
    var tjtk2a: Long? = null

}
