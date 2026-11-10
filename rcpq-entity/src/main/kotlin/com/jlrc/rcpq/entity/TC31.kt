package com.jlrc.rcpq.entity

import jakarta.persistence.*
import java.io.Serializable
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "TC31")
open class TC31 {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_TC31_ATC310")
    @SequenceGenerator(name = "SEQ_TC31_ATC310", sequenceName = "SEQ_TC31_ATC310", allocationSize = 1)
    @Column(name = "ATC310")
    var atc310: String? = null

    @Column(name = "AAE002")
    var aae002: Long? = null

    @Column(name = "AAA030")
    var aaa030: Long? = null

    @Column(name = "AAA031")
    var aaa031: String? = null

    @Column(name = "AAC001")
    var aac001: Long? = null

    @Column(name = "AAC003")
    var aac003: String? = null

    @Column(name = "AAC002")
    var aac002: String? = null

    @Column(name = "ATC311")
    var atc311: BigDecimal? = null

    @Column(name = "ATC312")
    var atc312: BigDecimal? = null

    @Column(name = "ATC313")
    var atc313: BigDecimal? = null

    @Column(name = "ATC31Z_1")
    var atc31z_1: BigDecimal? = null

    @Column(name = "ATC31Z_2")
    var atc31z_2: BigDecimal? = null

    @Column(name = "ATC93BG")
    var atc93bg: BigDecimal? = null

    @Column(name = "AAE001")
    var aae001: Long? = null

    @Column(name = "AAE100")
    var aae100: String? = null

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

    @Column(name = "ATB930")
    var atb930: BigDecimal? = null
}
