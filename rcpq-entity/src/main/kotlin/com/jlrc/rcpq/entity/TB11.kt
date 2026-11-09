package com.jlrc.rcpq.entity

import jakarta.persistence.*
import java.io.Serializable
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "TB11")
open class TB11 {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_TB11_ATB110")
    @SequenceGenerator(name = "SEQ_TB11_ATB110", sequenceName = "SEQ_TB11_ATB110", allocationSize = 1)
    @Column(name = "ATB110")
    var atb110: Long? = null

    @Column(name = "BAZ001")
    var baz001: Long? = null

    @Column(name = "BAZ002")
    var baz002: Long? = null

    @Column(name = "ATB111")
    var atb111: String? = null

    @Column(name = "ATB112")
    var atb112: LocalDate? = null

    @Column(name = "ATB113")
    var atb113: LocalDate? = null

    @Column(name = "ATB114")
    var atb114: Long? = null

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
