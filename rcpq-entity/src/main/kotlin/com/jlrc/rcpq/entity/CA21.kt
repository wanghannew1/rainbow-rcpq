package com.jlrc.rcpq.entity

import jakarta.persistence.*
import java.io.Serializable
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "CA21")
open class CA21 {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_CA21_ACA210")
    @SequenceGenerator(name = "SEQ_CA21_ACA210", sequenceName = "SEQ_CA21_ACA210", allocationSize = 1)
    @Column(name = "ACA210")
    var aca210: Long? = null

    @Column(name = "BAZ001")
    var baz001: Long? = null

    @Column(name = "BAZ002")
    var baz002: Long? = null

    @Column(name = "ACA211")
    var aca211: Long? = null

    @Column(name = "ACA212")
    var aca212: Long? = null

    @Column(name = "ACA213")
    var aca213: String? = null

    @Column(name = "ACA214")
    var aca214: BigDecimal? = null

    @Column(name = "ACA215")
    var aca215: BigDecimal? = null

    @Column(name = "ACA216")
    var aca216: BigDecimal? = null

    @Column(name = "ACA217")
    var aca217: BigDecimal? = null

    @Column(name = "ACA218")
    var aca218: BigDecimal? = null

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
