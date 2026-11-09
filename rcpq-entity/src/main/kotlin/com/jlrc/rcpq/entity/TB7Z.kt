package com.jlrc.rcpq.entity

import jakarta.persistence.*
import java.io.Serializable
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "TB7Z")
open class TB7Z {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_TB7Z_ATB7Z0")
    @SequenceGenerator(name = "SEQ_TB7Z_ATB7Z0", sequenceName = "SEQ_TB7Z_ATB7Z0", allocationSize = 1)
    @Column(name = "ATB7Z0")
    var atb7z0: Long? = null

    @Column(name = "BAZ001")
    var baz001: Long? = null

    @Column(name = "BAZ002")
    var baz002: Long? = null

    @Column(name = "ATB7Z1")
    var atb7z1: LocalDate? = null

    @Column(name = "ATB7Z2")
    var atb7z2: BigDecimal? = null

    @Column(name = "ATB723")
    var atb723: String? = null

    @Column(name = "AAE011")
    var aae011: String? = null

    @Column(name = "AAE017")
    var aae017: String? = null

    @Column(name = "AAE036")
    var aae036: LocalDateTime? = null

    @Column(name = "AAE013")
    var aae013: String? = null

    @Column(name = "AAE019")
    var aae019: String? = null

    @Column(name = "AAE020")
    var aae020: String? = null

    @Column(name = "AAE022")
    var aae022: String? = null

    @Column(name = "AAEA11")
    var aaea11: String? = null

    @Column(name = "AAEA17")
    var aaea17: String? = null

    @Column(name = "AAEA36")
    var aaea36: LocalDate? = null

    @Column(name = "AAEA19")
    var aaea19: String? = null

    @Column(name = "AAEA20")
    var aaea20: String? = null

    @Column(name = "AAEA22")
    var aaea22: String? = null

    @Column(name = "ATB7Z3")
    var atb7z3: BigDecimal? = null

}
