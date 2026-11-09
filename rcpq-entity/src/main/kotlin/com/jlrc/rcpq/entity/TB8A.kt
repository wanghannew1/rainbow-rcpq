package com.jlrc.rcpq.entity

import jakarta.persistence.*
import java.io.Serializable
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "TB8A")
open class TB8A {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_TB8A_ATB8A0")
    @SequenceGenerator(name = "SEQ_TB8A_ATB8A0", sequenceName = "SEQ_TB8A_ATB8A0", allocationSize = 1)
    @Column(name = "ATB8A0")
    var atb8a0: Long? = null

    @Column(name = "ATB8A1")
    var atb8a1: String? = null

    @Column(name = "ATB8A4")
    var atb8a4: String? = null

    @Column(name = "ATB8A6")
    var atb8a6: String? = null

    @Column(name = "ATB8A7")
    var atb8a7: String? = null

    @Column(name = "ATB8A8")
    var atb8a8: LocalDate? = null

    @Column(name = "ATB8A9")
    var atb8a9: String? = null

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

    @Column(name = "BAZ001")
    var baz001: Long? = null

    @Column(name = "BAZ002")
    var baz002: Long? = null

}
