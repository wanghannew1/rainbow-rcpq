package com.jlrc.rcpq.entity

import jakarta.persistence.*
import java.io.Serializable
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "TC76")
open class TC76 {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_TC76_ATC760")
    @SequenceGenerator(name = "SEQ_TC76_ATC760", sequenceName = "SEQ_TC76_ATC760", allocationSize = 1)
    @Column(name = "ATC760")
    var atc760: Long? = null

    @Column(name = "BAZ001")
    var baz001: Long? = null

    @Column(name = "BAZ002")
    var baz002: Long? = null

    @Column(name = "ATC761")
    var atc761: String? = null

    @Column(name = "ATC762")
    var atc762: String? = null

    @Column(name = "ATC763")
    var atc763: String? = null

    @Column(name = "ATC764")
    var atc764: String? = null

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
