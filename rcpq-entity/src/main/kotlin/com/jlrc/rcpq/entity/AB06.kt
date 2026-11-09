package com.jlrc.rcpq.entity

import jakarta.persistence.*
import java.io.Serializable
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "AB06")
open class AB06 {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_AB06_ID")
    @SequenceGenerator(name = "SEQ_AB06_ID", sequenceName = "SEQ_AB06_ID", allocationSize = 1)
    @Column(name = "ID")
    var id: Long? = null

    @Column(name = "BAZ001")
    var baz001: Long? = null

    @Column(name = "BAZ002")
    var baz002: Long? = null

    @Column(name = "AAB001")
    var aab001: Long? = null

    @Column(name = "AAE035")
    var aae035: LocalDate? = null

    @Column(name = "AAE0B1")
    var aae0b1: String? = null

    @Column(name = "AAE0B2")
    var aae0b2: String? = null

    @Column(name = "AAE0B3")
    var aae0b3: String? = null

    @Column(name = "AAE0B4")
    var aae0b4: String? = null

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

}
