package com.jlrc.rcpq.entity

import jakarta.persistence.*
import java.io.Serializable
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "TB89")
open class TB89 {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_TB89_ATB890")
    @SequenceGenerator(name = "SEQ_TB89_ATB890", sequenceName = "SEQ_TB89_ATB890", allocationSize = 1)
    @Column(name = "ATB890")
    var atb890: Long? = null

    @Column(name = "ATB891")
    var atb891: String? = null

    @Column(name = "ATB892")
    var atb892: LocalDate? = null

    @Column(name = "ATB893")
    var atb893: String? = null

    @Column(name = "ATB894")
    var atb894: String? = null

    @Column(name = "ATB895")
    var atb895: String? = null

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

    @Column(name = "BAZ001")
    var baz001: Long? = null

    @Column(name = "BAZ002")
    var baz002: Long? = null

    @Column(name = "ATB860")
    var atb860: BigDecimal? = null
}
