package com.jlrc.rcpq.entity

import jakarta.persistence.*
import java.io.Serializable
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "TB18")
open class TB18 {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_TB18_ATB180")
    @SequenceGenerator(name = "SEQ_TB18_ATB180", sequenceName = "SEQ_TB18_ATB180", allocationSize = 1)
    @Column(name = "ATB180")
    var atb180: Long? = null

    @Column(name = "BAZ001")
    var baz001: Long? = null

    @Column(name = "BAZ002")
    var baz002: Long? = null

    @Column(name = "ATB181")
    var atb181: String? = null

    @Column(name = "ATB182")
    var atb182: String? = null

    @Column(name = "ATB183")
    var atb183: String? = null

    @Column(name = "ATB184")
    var atb184: LocalDate? = null

    @Column(name = "ATB185")
    var atb185: LocalDate? = null

    @Column(name = "ATB186")
    var atb186: BigDecimal? = null

    @Column(name = "ATB187")
    var atb187: Long? = null

    @Column(name = "ATB188")
    var atb188: String? = null

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

}
