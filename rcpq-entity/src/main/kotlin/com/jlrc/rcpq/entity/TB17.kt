package com.jlrc.rcpq.entity

import jakarta.persistence.*
import java.io.Serializable
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "TB17")
open class TB17 {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_TB17_ATB170")
    @SequenceGenerator(name = "SEQ_TB17_ATB170", sequenceName = "SEQ_TB17_ATB170", allocationSize = 1)
    @Column(name = "ATB170")
    var atb170: Long? = null

    @Column(name = "BAZ001")
    var baz001: Long? = null

    @Column(name = "BAZ002")
    var baz002: Long? = null

    @Column(name = "AAC001")
    var aac001: Long? = null

    @Column(name = "AAC002")
    var aac002: String? = null

    @Column(name = "AAC003")
    var aac003: String? = null

    @Column(name = "AAC004")
    var aac004: String? = null

    @Column(name = "AAC005")
    var aac005: String? = null

    @Column(name = "AAC006")
    var aac006: LocalDate? = null

    @Column(name = "ATB140")
    var atb140: Long? = null

    @Column(name = "ATB141")
    var atb141: String? = null

    @Column(name = "ATB171")
    var atb171: String? = null

    @Column(name = "ATC109")
    var atc109: String? = null

    @Column(name = "ATB172")
    var atb172: String? = null

    @Column(name = "ATB173")
    var atb173: String? = null

    @Column(name = "ATB151")
    var atb151: LocalDate? = null

    @Column(name = "ATB152")
    var atb152: String? = null

    @Column(name = "ATB153")
    var atb153: String? = null

    @Column(name = "ATB150")
    var atb150: Long? = null

    @Column(name = "ATB160")
    var atb160: Long? = null

    @Column(name = "ATB161")
    var atb161: String? = null

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
