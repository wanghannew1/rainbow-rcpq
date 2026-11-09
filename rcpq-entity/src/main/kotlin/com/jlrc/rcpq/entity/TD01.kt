package com.jlrc.rcpq.entity

import jakarta.persistence.*
import java.io.Serializable
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "TD01")
open class TD01 {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_TD01_ID")
    @SequenceGenerator(name = "SEQ_TD01_ID", sequenceName = "SEQ_TD01_ID", allocationSize = 1)
    @Column(name = "ID")
    var id: Long? = null

    @Column(name = "ATB930")
    var atb930: Long? = null

    @Column(name = "ATB931")
    var atb931: String? = null

    @Column(name = "ATC900")
    var atc900: Long? = null

    @Column(name = "AAC001")
    var aac001: Long? = null

    @Column(name = "AAC003")
    var aac003: String? = null

    @Column(name = "AAC002")
    var aac002: String? = null

    @Column(name = "ATD005")
    var atd005: String? = null

    @Column(name = "AAA001")
    var aaa001: String? = null

    @Column(name = "ATD001")
    var atd001: LocalDate? = null

    @Column(name = "ATD002")
    var atd002: String? = null

    @Column(name = "ATD003")
    var atd003: LocalDate? = null

    @Column(name = "AAE013")
    var aae013: String? = null

    @Column(name = "ATD004")
    var atd004: String? = null

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
