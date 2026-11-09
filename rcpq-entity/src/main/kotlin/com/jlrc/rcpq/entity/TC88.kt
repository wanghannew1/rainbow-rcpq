package com.jlrc.rcpq.entity

import jakarta.persistence.*
import java.io.Serializable
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "TC88")
open class TC88 {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_TC88_ATC880")
    @SequenceGenerator(name = "SEQ_TC88_ATC880", sequenceName = "SEQ_TC88_ATC880", allocationSize = 1)
    @Column(name = "ATC880")
    var atc880: Long? = null

    @Column(name = "ATC830")
    var atc830: Long? = null

    @Column(name = "AAC001")
    var aac001: Long? = null

    @Column(name = "ATC831")
    var atc831: String? = null

    @Column(name = "AAC002")
    var aac002: String? = null

    @Column(name = "AAC003")
    var aac003: String? = null

    @Column(name = "AAC004")
    var aac004: String? = null

    @Column(name = "AAB001")
    var aab001: Long? = null

    @Column(name = "AAB004")
    var aab004: String? = null

    @Column(name = "ATC832")
    var atc832: LocalDate? = null

    @Column(name = "ATC833")
    var atc833: String? = null

    @Column(name = "AAE005")
    var aae005: String? = null

    @Column(name = "ATC884")
    var atc884: String? = null

    @Column(name = "ATC885")
    var atc885: String? = null

    @Column(name = "ATC886")
    var atc886: BigDecimal? = null

    @Column(name = "ATC887")
    var atc887: String? = null

    @Column(name = "ATC888")
    var atc888: LocalDate? = null

    @Column(name = "ATC889")
    var atc889: BigDecimal? = null

    @Column(name = "ATC88A")
    var atc88a: BigDecimal? = null

    @Column(name = "ATC88C")
    var atc88c: LocalDate? = null

    @Column(name = "ATC88D")
    var atc88d: String? = null

    @Column(name = "ATC88E")
    var atc88e: LocalDate? = null

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

    @Column(name = "BAZ001")
    var baz001: Long? = null

    @Column(name = "BAZ002")
    var baz002: Long? = null

}
