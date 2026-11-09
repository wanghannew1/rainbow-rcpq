package com.jlrc.rcpq.entity

import jakarta.persistence.*
import java.io.Serializable
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "TC8Z")
open class TC8Z {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_TC8Z_ID")
    @SequenceGenerator(name = "SEQ_TC8Z_ID", sequenceName = "SEQ_TC8Z_ID", allocationSize = 1)
    @Column(name = "ID")
    var id: Long? = null

    @Column(name = "ATC930")
    var atc930: Long? = null

    @Column(name = "ATB930")
    var atb930: Long? = null

    @Column(name = "AAB004")
    var aab004: String? = null

    @Column(name = "AAB00A")
    var aab00a: String? = null

    @Column(name = "ATC931")
    var atc931: Long? = null

    @Column(name = "ATC932")
    var atc932: Long? = null

    @Column(name = "AAC003")
    var aac003: String? = null

    @Column(name = "AAC002")
    var aac002: String? = null

    @Column(name = "ATC937")
    var atc937: String? = null

    @Column(name = "ATC93R")
    var atc93r: BigDecimal? = null

    @Column(name = "ATC93R_")
    var atc93r_: BigDecimal? = null

    @Column(name = "ATC93S")
    var atc93s: BigDecimal? = null

    @Column(name = "ATC93S_")
    var atc93s_: BigDecimal? = null

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
