package com.jlrc.rcpq.entity

import jakarta.persistence.*
import java.io.Serializable
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "TB70")
open class TB70 {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_TB70_ATB700")
    @SequenceGenerator(name = "SEQ_TB70_ATB700", sequenceName = "SEQ_TB70_ATB700", allocationSize = 1)
    @Column(name = "ATB700")
    var atb700: Long? = null

    @Column(name = "BAZ001")
    var baz001: Long? = null

    @Column(name = "BAZ002")
    var baz002: Long? = null

    @Column(name = "AAB001")
    var aab001: Long? = null

    @Column(name = "AAB004")
    var aab004: String? = null

    @Column(name = "AAB007")
    var aab007: String? = null

    @Column(name = "ATB701")
    var atb701: String? = null

    @Column(name = "ATB705")
    var atb705: String? = null

    @Column(name = "AAE006")
    var aae006: String? = null

    @Column(name = "AAE005")
    var aae005: String? = null

    @Column(name = "AAE007")
    var aae007: String? = null

    @Column(name = "AAE004")
    var aae004: String? = null

    @Column(name = "ATB702")
    var atb702: String? = null

    @Column(name = "AAE100")
    var aae100: String? = null

    @Column(name = "AAE013")
    var aae013: String? = null

    @Column(name = "ATB707")
    var atb707: LocalDate? = null

    @Column(name = "ATB708")
    var atb708: String? = null

    @Column(name = "ATB709")
    var atb709: String? = null

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

    @Column(name = "AAE111")
    var aae111: String? = null

    @Column(name = "AAE117")
    var aae117: String? = null

    @Column(name = "AAE136")
    var aae136: LocalDate? = null

    @Column(name = "AAE119")
    var aae119: String? = null

    @Column(name = "AAE120")
    var aae120: String? = null

    @Column(name = "AAE122")
    var aae122: String? = null

    @Column(name = "ATB703")
    var atb703: LocalDate? = null

}
