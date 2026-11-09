package com.jlrc.rcpq.entity

import jakarta.persistence.*
import java.io.Serializable
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "TB91")
open class TB91 {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_TB91_ATB910")
    @SequenceGenerator(name = "SEQ_TB91_ATB910", sequenceName = "SEQ_TB91_ATB910", allocationSize = 1)
    @Column(name = "ATB910")
    var atb910: Long? = null

    @Column(name = "BAZ001")
    var baz001: Long? = null

    @Column(name = "BAZ002")
    var baz002: Long? = null

    @Column(name = "ATB91Z")
    var atb91z: Long? = null

    @Column(name = "ATB911")
    var atb911: String? = null

    @Column(name = "ATB912")
    var atb912: String? = null

    @Column(name = "ATB913")
    var atb913: String? = null

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

    @Column(name = "BAE011")
    var bae011: String? = null

    @Column(name = "BAE017")
    var bae017: String? = null

    @Column(name = "BAE036")
    var bae036: LocalDate? = null

    @Column(name = "BAE019")
    var bae019: String? = null

    @Column(name = "BAE020")
    var bae020: String? = null

    @Column(name = "BAE022")
    var bae022: String? = null

    @Column(name = "AAE013")
    var aae013: String? = null

    @Column(name = "ATB900")
    var atb900: String? = null

    @Column(name = "AAB00E")
    var aab00e: String? = null

    @Column(name = "AAB004")
    var aab004: String? = null

    @Column(name = "ATB931")
    var atb931: String? = null

}
