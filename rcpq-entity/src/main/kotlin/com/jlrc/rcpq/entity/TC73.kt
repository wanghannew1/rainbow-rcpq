package com.jlrc.rcpq.entity

import jakarta.persistence.*
import java.io.Serializable
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "TC73")
open class TC73 {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_TC70_ATC700")
    @SequenceGenerator(name = "SEQ_TC70_ATC700", sequenceName = "SEQ_TC70_ATC700", allocationSize = 1)
    @Column(name = "ATC700")
    var atc700: Long? = null

    @Column(name = "BAZ001")
    var baz001: Long? = null

    @Column(name = "BAZ002")
    var baz002: Long? = null

    @Column(name = "AAC003")
    var aac003: String? = null

    @Column(name = "ATC731")
    var atc731: String? = null

    @Column(name = "ATC732")
    var atc732: String? = null

    @Column(name = "ATC734")
    var atc734: LocalDate? = null

    @Column(name = "ATC735")
    var atc735: String? = null

    @Column(name = "ATC736")
    var atc736: String? = null

    @Column(name = "ATC702")
    var atc702: String? = null

    @Column(name = "ATC737")
    var atc737: String? = null

    @Column(name = "ATC704")
    var atc704: LocalDate? = null

    @Column(name = "ATC705")
    var atc705: String? = null

    @Column(name = "ATC707")
    var atc707: String? = null

    @Column(name = "ATC708")
    var atc708: String? = null

    @Column(name = "ATC709")
    var atc709: String? = null

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

    @Column(name = "ATC738")
    var atc738: String? = null

    @Column(name = "ATC70Z")
    var atc70z: String? = null

    @Column(name = "ATC70P")
    var atc70p: String? = null

}
