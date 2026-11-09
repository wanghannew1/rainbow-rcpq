package com.jlrc.rcpq.entity

import jakarta.persistence.*
import java.io.Serializable
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "TC74")
open class TC74 {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_TC74_ATC740")
    @SequenceGenerator(name = "SEQ_TC74_ATC740", sequenceName = "SEQ_TC74_ATC740", allocationSize = 1)
    @Column(name = "ATC740")
    var atc740: Long? = null

    @Column(name = "BAZ001")
    var baz001: Long? = null

    @Column(name = "BAZ002")
    var baz002: Long? = null

    @Column(name = "AAC003")
    var aac003: String? = null

    @Column(name = "ATC741")
    var atc741: LocalDate? = null

    @Column(name = "ATC735")
    var atc735: String? = null

    @Column(name = "ATC704")
    var atc704: LocalDate? = null

    @Column(name = "ATC742")
    var atc742: String? = null

    @Column(name = "ATC743")
    var atc743: String? = null

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

    @Column(name = "ATC744")
    var atc744: String? = null

    @Column(name = "ATC745")
    var atc745: String? = null

    @Column(name = "ATC746")
    var atc746: BigDecimal? = null

    @Column(name = "ATC732")
    var atc732: String? = null

}
