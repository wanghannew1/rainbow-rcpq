package com.jlrc.rcpq.entity

import jakarta.persistence.*
import java.io.Serializable
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "TC79")
open class TC79 {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_TC79_ATC790")
    @SequenceGenerator(name = "SEQ_TC79_ATC790", sequenceName = "SEQ_TC79_ATC790", allocationSize = 1)
    @Column(name = "ATC790")
    var atc790: Long? = null

    @Column(name = "BAZ001")
    var baz001: Long? = null

    @Column(name = "BAZ002")
    var baz002: Long? = null

    @Column(name = "AAC001")
    var aac001: Long? = null

    @Column(name = "ATB700")
    var atb700: Long? = null

    @Column(name = "AAB001")
    var aab001: Long? = null

    @Column(name = "AAB004")
    var aab004: String? = null

    @Column(name = "ATC791")
    var atc791: String? = null

    @Column(name = "ATC792")
    var atc792: LocalDate? = null

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

    @Column(name = "ATB702")
    var atb702: String? = null

    @Column(name = "ATC700")
    var atc700: BigDecimal? = null
}
