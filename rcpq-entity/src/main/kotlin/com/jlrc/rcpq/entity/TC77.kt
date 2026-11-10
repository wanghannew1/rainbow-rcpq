package com.jlrc.rcpq.entity

import jakarta.persistence.*
import java.io.Serializable
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "TC77")
open class TC77 {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_TC77_ATC770")
    @SequenceGenerator(name = "SEQ_TC77_ATC770", sequenceName = "SEQ_TC77_ATC770", allocationSize = 1)
    @Column(name = "ATC770")
    var atc770: Long? = null

    @Column(name = "BAZ001")
    var baz001: Long? = null

    @Column(name = "BAZ002")
    var baz002: Long? = null

    @Column(name = "ATC771")
    var atc771: Long? = null

    @Column(name = "ATC772")
    var atc772: String? = null

    @Column(name = "ATC773")
    var atc773: String? = null

    @Column(name = "ATC774")
    var atc774: String? = null

    @Column(name = "ATC775")
    var atc775: String? = null

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

    @Column(name = "ATC700")
    var atc700: BigDecimal? = null
}
