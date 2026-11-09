package com.jlrc.rcpq.entity

import jakarta.persistence.*
import java.io.Serializable
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "TC99")
open class TC99 {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_TC99_ATC990")
    @SequenceGenerator(name = "SEQ_TC99_ATC990", sequenceName = "SEQ_TC99_ATC990", allocationSize = 1)
    @Column(name = "ATC990")
    var atc990: Long? = null

    @Column(name = "BAZ001")
    var baz001: Long? = null

    @Column(name = "BAZ002")
    var baz002: Long? = null

    @Column(name = "ATC970")
    var atc970: Long? = null

    @Column(name = "ATC991")
    var atc991: String? = null

    @Column(name = "ATC992")
    var atc992: LocalDate? = null

    @Column(name = "ATC993")
    var atc993: LocalDate? = null

    @Column(name = "ATC994")
    var atc994: String? = null

    @Column(name = "ATC995")
    var atc995: String? = null

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

    @Column(name = "ATC996")
    var atc996: String? = null

    @Column(name = "ATC997")
    var atc997: String? = null

}
