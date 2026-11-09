package com.jlrc.rcpq.entity

import jakarta.persistence.*
import java.io.Serializable
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "TB16")
open class TB16 {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_TB16_ATB160")
    @SequenceGenerator(name = "SEQ_TB16_ATB160", sequenceName = "SEQ_TB16_ATB160", allocationSize = 1)
    @Column(name = "ATB160")
    var atb160: Long? = null

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

    @Column(name = "ATB161")
    var atb161: String? = null

    @Column(name = "ATB162")
    var atb162: Long? = null

    @Column(name = "ATB163")
    var atb163: Long? = null

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
