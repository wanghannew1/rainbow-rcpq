package com.jlrc.rcpq.entity

import jakarta.persistence.*
import java.io.Serializable
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "TD02")
open class TD02 {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_TD02_ID")
    @SequenceGenerator(name = "SEQ_TD02_ID", sequenceName = "SEQ_TD02_ID", allocationSize = 1)
    @Column(name = "ID")
    var id: Long? = null

    @Column(name = "ATC900")
    var atc900: Long? = null

    @Column(name = "ATB930")
    var atb930: Long? = null

    @Column(name = "ATB931")
    var atb931: String? = null

    @Column(name = "AAB001")
    var aab001: Long? = null

    @Column(name = "AAB004")
    var aab004: String? = null

    @Column(name = "AAC001")
    var aac001: Long? = null

    @Column(name = "AAC003")
    var aac003: String? = null

    @Column(name = "AAC002")
    var aac002: String? = null

    @Column(name = "AAE006")
    var aae006: String? = null

    @Column(name = "AAE005")
    var aae005: String? = null

    @Column(name = "AAE008")
    var aae008: Long? = null

    @Column(name = "ATD021")
    var atd021: String? = null

    @Column(name = "ATD022")
    var atd022: LocalDate? = null

    @Column(name = "ATD023")
    var atd023: String? = null

    @Column(name = "ATD024")
    var atd024: String? = null

    @Column(name = "ATD025")
    var atd025: Long? = null

    @Column(name = "ATD026")
    var atd026: Long? = null

    @Column(name = "ATD027")
    var atd027: LocalDate? = null

    @Column(name = "ATD028")
    var atd028: LocalDate? = null

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
