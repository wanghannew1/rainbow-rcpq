package com.jlrc.rcpq.entity

import jakarta.persistence.*
import java.io.Serializable
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "TC12")
open class TC12 {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_TC12_ATC120")
    @SequenceGenerator(name = "SEQ_TC12_ATC120", sequenceName = "SEQ_TC12_ATC120", allocationSize = 1)
    @Column(name = "ATC120")
    var atc120: Long? = null

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

    @Column(name = "AAC001")
    var aac001: Long? = null

    @Column(name = "AAC002")
    var aac002: String? = null

    @Column(name = "AAC003")
    var aac003: String? = null

    @Column(name = "AAC004")
    var aac004: String? = null

    @Column(name = "AAC006")
    var aac006: LocalDate? = null

    @Column(name = "AAC011")
    var aac011: String? = null

    @Column(name = "AAE005")
    var aae005: String? = null

    @Column(name = "ATC111")
    var atc111: String? = null

    @Column(name = "ATC112")
    var atc112: String? = null

    @Column(name = "AAE006")
    var aae006: String? = null

    @Column(name = "AAE007")
    var aae007: String? = null

    @Column(name = "AAC0B1")
    var aac0b1: String? = null

    @Column(name = "AAB301")
    var aab301: String? = null

    @Column(name = "AAC010")
    var aac010: String? = null

    @Column(name = "AAB004")
    var aab004: String? = null

    @Column(name = "AAC180")
    var aac180: String? = null

    @Column(name = "AAC182")
    var aac182: String? = null

    @Column(name = "AAC183")
    var aac183: String? = null

    @Column(name = "ATC113")
    var atc113: String? = null

    @Column(name = "ATC114")
    var atc114: String? = null

    @Column(name = "ATC115")
    var atc115: String? = null

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

    @Column(name = "ATB110")
    var atb110: BigDecimal? = null
}
