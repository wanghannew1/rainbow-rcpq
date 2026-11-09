package com.jlrc.rcpq.entity

import jakarta.persistence.*
import java.io.Serializable
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "TJQC5")
open class TJQC5 {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_TJQC5_TJQC10")
    @SequenceGenerator(name = "SEQ_TJQC5_TJQC10", sequenceName = "SEQ_TJQC5_TJQC10", allocationSize = 1)
    @Column(name = "TJQC10")
    var tjqc10: Long? = null

    @Column(name = "AAA020")
    var aaa020: String? = null

    @Column(name = "AAA021")
    var aaa021: String? = null

    @Column(name = "AAE001")
    var aae001: LocalDate? = null

    @Column(name = "AAE002")
    var aae002: LocalDate? = null

    @Column(name = "TJQC11")
    var tjqc11: Long? = null

    @Column(name = "TJQC12")
    var tjqc12: String? = null

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

    @Column(name = "TJQC13")
    var tjqc13: String? = null

}
