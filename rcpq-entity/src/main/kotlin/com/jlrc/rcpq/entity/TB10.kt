package com.jlrc.rcpq.entity

import jakarta.persistence.*
import java.io.Serializable
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "TB10")
open class TB10 {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_TB10_ATB100")
    @SequenceGenerator(name = "SEQ_TB10_ATB100", sequenceName = "SEQ_TB10_ATB100", allocationSize = 1)
    @Column(name = "ATB100")
    var atb100: Long? = null

    @Column(name = "BAZ001")
    var baz001: Long? = null

    @Column(name = "BAZ002")
    var baz002: Long? = null

    @Column(name = "ATB101")
    var atb101: String? = null

    @Column(name = "ATB104")
    var atb104: String? = null

    @Column(name = "ATB103")
    var atb103: String? = null

    @Column(name = "ATB102")
    var atb102: String? = null

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

    @Column(name = "AAE022")
    var aae022: String? = null

}
