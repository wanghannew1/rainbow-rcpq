package com.jlrc.rcpq.entity

import jakarta.persistence.*
import java.io.Serializable
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "TB12")
open class TB12 {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_TB12_ATB120")
    @SequenceGenerator(name = "SEQ_TB12_ATB120", sequenceName = "SEQ_TB12_ATB120", allocationSize = 1)
    @Column(name = "ATB120")
    var atb120: Long? = null

    @Column(name = "BAZ001")
    var baz001: Long? = null

    @Column(name = "BAZ002")
    var baz002: Long? = null

    @Column(name = "ATB121")
    var atb121: String? = null

    @Column(name = "ATB122")
    var atb122: LocalDate? = null

    @Column(name = "ATB123")
    var atb123: LocalDate? = null

    @Column(name = "ATB124")
    var atb124: Long? = null

    @Column(name = "ATB125")
    var atb125: Long? = null

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
