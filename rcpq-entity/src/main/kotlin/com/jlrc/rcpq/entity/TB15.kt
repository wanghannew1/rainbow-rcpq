package com.jlrc.rcpq.entity

import jakarta.persistence.*
import java.io.Serializable
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "TB15")
open class TB15 {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_TB15_ATB150")
    @SequenceGenerator(name = "SEQ_TB15_ATB150", sequenceName = "SEQ_TB15_ATB150", allocationSize = 1)
    @Column(name = "ATB150")
    var atb150: Long? = null

    @Column(name = "BAZ001")
    var baz001: Long? = null

    @Column(name = "BAZ002")
    var baz002: Long? = null

    @Column(name = "ATB140")
    var atb140: Long? = null

    @Column(name = "ATB141")
    var atb141: String? = null

    @Column(name = "ATB151")
    var atb151: LocalDate? = null

    @Column(name = "ATB152")
    var atb152: String? = null

    @Column(name = "ATB153")
    var atb153: String? = null

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
