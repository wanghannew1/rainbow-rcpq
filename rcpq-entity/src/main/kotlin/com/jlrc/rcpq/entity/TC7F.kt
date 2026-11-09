package com.jlrc.rcpq.entity

import jakarta.persistence.*
import java.io.Serializable
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "TC7F")
open class TC7F {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_TC7F_ATC7F0")
    @SequenceGenerator(name = "SEQ_TC7F_ATC7F0", sequenceName = "SEQ_TC7F_ATC7F0", allocationSize = 1)
    @Column(name = "ATC7F0")
    var atc7f0: Long? = null

    @Column(name = "BAZ001")
    var baz001: Long? = null

    @Column(name = "BAZ002")
    var baz002: Long? = null

    @Column(name = "ATC7F1")
    var atc7f1: LocalDate? = null

    @Column(name = "ATC7F2")
    var atc7f2: String? = null

    @Column(name = "AAC003")
    var aac003: String? = null

    @Column(name = "AAC004")
    var aac004: String? = null

    @Column(name = "AAC006")
    var aac006: LocalDate? = null

    @Column(name = "ATC70E")
    var atc70e: String? = null

    @Column(name = "ATC70F")
    var atc70f: String? = null

    @Column(name = "ATC70C")
    var atc70c: LocalDate? = null

    @Column(name = "ATC70D")
    var atc70d: String? = null

    @Column(name = "ATC70A")
    var atc70a: String? = null

    @Column(name = "ATC7F3")
    var atc7f3: Long? = null

    @Column(name = "ATC7F4")
    var atc7f4: Long? = null

    @Column(name = "ATC7F5")
    var atc7f5: Long? = null

    @Column(name = "ATC7F6")
    var atc7f6: String? = null

    @Column(name = "ATC7F7")
    var atc7f7: String? = null

    @Column(name = "ATC7F8")
    var atc7f8: BigDecimal? = null

    @Column(name = "ATC7F9")
    var atc7f9: BigDecimal? = null

    @Column(name = "ATC7FA")
    var atc7fa: Long? = null

    @Column(name = "ATC7FB")
    var atc7fb: Long? = null

    @Column(name = "ATC7FC")
    var atc7fc: BigDecimal? = null

    @Column(name = "ATC7FD")
    var atc7fd: BigDecimal? = null

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

    @Column(name = "ATC7FE")
    var atc7fe: String? = null

}
