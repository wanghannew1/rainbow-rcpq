package com.jlrc.rcpq.entity

import jakarta.persistence.*
import java.io.Serializable
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "TC91")
open class TC91 {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_TC91_ATC910")
    @SequenceGenerator(name = "SEQ_TC91_ATC910", sequenceName = "SEQ_TC91_ATC910", allocationSize = 1)
    @Column(name = "ATC910")
    var atc910: Long? = null

    @Column(name = "ATC900")
    var atc900: Long? = null

    @Column(name = "SEQ")
    var seq: Long? = null

    @Column(name = "ATC911")
    var atc911: String? = null

    @Column(name = "ATC912")
    var atc912: BigDecimal? = null

    @Column(name = "ATC916")
    var atc916: BigDecimal? = null

    @Column(name = "ATC913")
    var atc913: BigDecimal? = null

    @Column(name = "ATC914")
    var atc914: String? = null

    @Column(name = "ATC915")
    var atc915: LocalDate? = null

    @Column(name = "BAE011")
    var bae011: String? = null

    @Column(name = "BAE017")
    var bae017: String? = null

    @Column(name = "BAE036")
    var bae036: LocalDate? = null

    @Column(name = "BAE019")
    var bae019: String? = null

    @Column(name = "BAE020")
    var bae020: String? = null

    @Column(name = "BAE022")
    var bae022: String? = null

    @Column(name = "AAE013")
    var aae013: String? = null

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

    @Column(name = "ATC917")
    var atc917: String? = null

}
