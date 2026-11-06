package com.jlrc.rcpq.entity

import jakarta.persistence.*
import java.io.Serializable
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime
@Entity
@Table(name = "AA01")
open class AA01 {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "AA01_SEQ")
    // TODO: 序列名待与 PG DDL 对齐(原 Oracle sequence)
    @SequenceGenerator(name = "AA01_SEQ", sequenceName = "AA01_SEQ", allocationSize = 1)
    @Column(name = "AAA010")
    var aaa010: Long? = null

    @Column(name = "AAB034")
    var aab034: String? = null

    @Column(name = "AAE140")
    var aae140: String? = null

    @Column(name = "AAA001")
    var aaa001: String? = null

    @Column(name = "AAA002")
    var aaa002: String? = null

    @Column(name = "AAA004")
    var aaa004: String? = null

    @Column(name = "AAA005")
    var aaa005: String? = null

    @Column(name = "AAA006")
    var aaa006: String? = null

    @Column(name = "AAA009")
    var aaa009: String? = null

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

    @Column(name = "AAA003")
    var aaa003: String? = null

}
