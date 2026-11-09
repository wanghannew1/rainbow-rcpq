package com.jlrc.rcpq.entity

import jakarta.persistence.*
import java.io.Serializable
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "TJ013")
open class TJ013 {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_TJ013_TJ0130")
    @SequenceGenerator(name = "SEQ_TJ013_TJ0130", sequenceName = "SEQ_TJ013_TJ0130", allocationSize = 1)
    @Column(name = "TJ0130")
    var tj0130: Long? = null

    @Column(name = "TJ0000")
    var tj0000: Long? = null

    @Column(name = "AAA020")
    var aaa020: String? = null

    @Column(name = "AAA021")
    var aaa021: String? = null

    @Column(name = "AAE001")
    var aae001: Long? = null

    @Column(name = "AAE00A")
    var aae00a: Long? = null

    @Column(name = "AC0009")
    var ac0009: Long? = null

    @Column(name = "TJ0131")
    var tj0131: String? = null

    @Column(name = "TJ0132")
    var tj0132: String? = null

    @Column(name = "TJ0133")
    var tj0133: Long? = null

    @Column(name = "TJ0134")
    var tj0134: Long? = null

    @Column(name = "TJ0135")
    var tj0135: Long? = null

    @Column(name = "TJ0136")
    var tj0136: Long? = null

    @Column(name = "TJ0137")
    var tj0137: Long? = null

    @Column(name = "TJ0138")
    var tj0138: Long? = null

    @Column(name = "TJ0139")
    var tj0139: Long? = null

    @Column(name = "TJ013A")
    var tj013a: Long? = null

    @Column(name = "TJ013B")
    var tj013b: Long? = null

    @Column(name = "TJ013C")
    var tj013c: Long? = null

    @Column(name = "TJ013D")
    var tj013d: Long? = null

    @Column(name = "TJ013E")
    var tj013e: Long? = null

    @Column(name = "TJ013F")
    var tj013f: Long? = null

    @Column(name = "AAE013")
    var aae013: String? = null

    @Column(name = "TJ013Q")
    var tj013q: String? = null

    @Column(name = "TJ013R")
    var tj013r: String? = null

    @Column(name = "TJ013S")
    var tj013s: String? = null

    @Column(name = "TJ013T")
    var tj013t: String? = null

    @Column(name = "TJ013U")
    var tj013u: LocalDate? = null

    @Column(name = "TJ000X")
    var tj000x: String? = null

    @Column(name = "TJ000W")
    var tj000w: String? = null

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
