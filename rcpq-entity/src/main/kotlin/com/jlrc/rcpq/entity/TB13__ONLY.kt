package com.jlrc.rcpq.entity

import jakarta.persistence.*
import java.io.Serializable
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "TB13")
open class TB13__ONLY {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_TB13_ATB130")
    @SequenceGenerator(name = "SEQ_TB13_ATB130", sequenceName = "SEQ_TB13_ATB130", allocationSize = 1)
    @Column(name = "ATB130")
    var atb130: Long? = null

    @Column(name = "BAZ001")
    var baz001: Long? = null

    @Column(name = "BAZ002")
    var baz002: Long? = null

    @Column(name = "ATB120")
    var atb120: Long? = null

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

    @Column(name = "ATB131")
    var atb131: String? = null

    @Column(name = "ATB132")
    var atb132: String? = null

    @Column(name = "ATB133")
    var atb133: LocalDate? = null

    @Column(name = "ATB134")
    var atb134: LocalDate? = null

    @Column(name = "ATB135")
    var atb135: Long? = null

    @Column(name = "ATB136")
    var atb136: Long? = null

    @Column(name = "ATB137")
    var atb137: String? = null

    @Column(name = "ATB138")
    var atb138: String? = null

    @Column(name = "ATB139")
    var atb139: LocalDate? = null

    @Column(name = "ATB13A")
    var atb13a: String? = null

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
