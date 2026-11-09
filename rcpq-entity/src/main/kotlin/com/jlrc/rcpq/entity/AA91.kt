package com.jlrc.rcpq.entity

import jakarta.persistence.*
import java.io.Serializable
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "AA91")
open class AA91 {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_AA91_AAA910")
    @SequenceGenerator(name = "SEQ_AA91_AAA910", sequenceName = "SEQ_AA91_AAA910", allocationSize = 1)
    @Column(name = "AAA910")
    var aaa910: Long? = null

    @Column(name = "BAZ001")
    var baz001: Long? = null

    @Column(name = "BAZ002")
    var baz002: Long? = null

    @Column(name = "ATB900")
    var atb900: Long? = null

    @Column(name = "AAC003")
    var aac003: String? = null

    @Column(name = "AAC002")
    var aac002: String? = null

    @Column(name = "AAA911")
    var aaa911: LocalDate? = null

    @Column(name = "AAA912")
    var aaa912: String? = null

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

    @Column(name = "AAE100")
    var aae100: String? = null

    @Column(name = "AAA913")
    var aaa913: String? = null

    @Column(name = "AAA914")
    var aaa914: LocalDate? = null

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

    @Column(name = "AAC001")
    var aac001: Long? = null

}
