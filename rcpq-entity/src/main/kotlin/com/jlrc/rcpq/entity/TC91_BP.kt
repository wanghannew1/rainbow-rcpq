package com.jlrc.rcpq.entity

import jakarta.persistence.*
import java.io.Serializable
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "TC91_BP")
open class TC91_BP {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_TC91_ID")
    @SequenceGenerator(name = "SEQ_TC91_ID", sequenceName = "SEQ_TC91_ID", allocationSize = 1)
    @Column(name = "ID")
    var id: Long? = null

    @Column(name = "ACE100")
    var ace100: Long? = null

    @Column(name = "ATC900")
    var atc900: Long? = null

    @Column(name = "AAC003")
    var aac003: String? = null

    @Column(name = "AAC002")
    var aac002: String? = null

    @Column(name = "ATC911")
    var atc911: String? = null

    @Column(name = "ATC912")
    var atc912: String? = null

    @Column(name = "SEQ")
    var seq: String? = null

    @Column(name = "ATC914")
    var atc914: String? = null

    @Column(name = "ATC912_1")
    var atc912_1: BigDecimal? = null

    @Column(name = "ATC914_1")
    var atc914_1: String? = null

    @Column(name = "AAE013")
    var aae013: String? = null

    @Column(name = "ACE10A")
    var ace10a: String? = null

    @Column(name = "ACE10B")
    var ace10b: String? = null

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

    @Column(name = "ATC915")
    var atc915: String? = null

}
