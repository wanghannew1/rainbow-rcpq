package com.jlrc.rcpq.entity

import jakarta.persistence.*
import java.io.Serializable
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "TD03")
open class TD03 {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_TD03_ATD030")
    @SequenceGenerator(name = "SEQ_TD03_ATD030", sequenceName = "SEQ_TD03_ATD030", allocationSize = 1)
    @Column(name = "ATD030")
    var atd030: Long? = null

    @Column(name = "ATC900")
    var atc900: Long? = null

    @Column(name = "AAC001")
    var aac001: Long? = null

    @Column(name = "AAC003")
    var aac003: String? = null

    @Column(name = "AAC002")
    var aac002: String? = null

    @Column(name = "ATB930")
    var atb930: Long? = null

    @Column(name = "ATB931")
    var atb931: String? = null

    @Column(name = "AAB004")
    var aab004: String? = null

    @Column(name = "AAE002")
    var aae002: Long? = null

    @Column(name = "AAE003")
    var aae003: Long? = null

    @Column(name = "AAE001")
    var aae001: Long? = null

    @Column(name = "ATD031")
    var atd031: BigDecimal? = null

    @Column(name = "ATD032")
    var atd032: BigDecimal? = null

    @Column(name = "ATD033")
    var atd033: Long? = null

    @Column(name = "XH")
    var xh: Long? = null

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

    @Column(name = "AAE013")
    var aae013: String? = null

    @Column(name = "FLAG")
    var flag: String? = null

}
