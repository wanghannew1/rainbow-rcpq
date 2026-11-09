package com.jlrc.rcpq.entity

import jakarta.persistence.*
import java.io.Serializable
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "JFSJ")
open class JFSJ {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_JFSJ_ID")
    @SequenceGenerator(name = "SEQ_JFSJ_ID", sequenceName = "SEQ_JFSJ_ID", allocationSize = 1)
    @Column(name = "ID")
    var id: Long? = null

    @Column(name = "AAA030")
    var aaa030: Long? = null

    @Column(name = "AAA031")
    var aaa031: String? = null

    @Column(name = "AAE002")
    var aae002: Long? = null

    @Column(name = "JFLX")
    var jflx: String? = null

    @Column(name = "JFXZ")
    var jfxz: String? = null

    @Column(name = "SEQ")
    var seq: Long? = null

    @Column(name = "JFSJ001")
    var jfsj001: String? = null

    @Column(name = "JFSJ002")
    var jfsj002: String? = null

    @Column(name = "JFSJ003")
    var jfsj003: String? = null

    @Column(name = "JFSJ004")
    var jfsj004: String? = null

    @Column(name = "JFSJ005")
    var jfsj005: String? = null

    @Column(name = "JFSJ006")
    var jfsj006: String? = null

    @Column(name = "JFSJ007")
    var jfsj007: String? = null

    @Column(name = "JFSJ008")
    var jfsj008: String? = null

    @Column(name = "JFSJ009")
    var jfsj009: String? = null

    @Column(name = "JFSJ00A")
    var jfsj00a: String? = null

    @Column(name = "JFSJ00B")
    var jfsj00b: String? = null

    @Column(name = "JFSJ00C")
    var jfsj00c: String? = null

    @Column(name = "ATC930")
    var atc930: Long? = null

    @Column(name = "ATC900")
    var atc900: Long? = null

    @Column(name = "ATB930")
    var atb930: Long? = null

    @Column(name = "ACE100")
    var ace100: Long? = null

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

    @Column(name = "JFSJ00D")
    var jfsj00d: String? = null

}
