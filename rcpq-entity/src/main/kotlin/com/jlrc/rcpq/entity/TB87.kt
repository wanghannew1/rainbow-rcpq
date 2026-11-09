package com.jlrc.rcpq.entity

import jakarta.persistence.*
import java.io.Serializable
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "TB87")
open class TB87 {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_TB87_ATB870")
    @SequenceGenerator(name = "SEQ_TB87_ATB870", sequenceName = "SEQ_TB87_ATB870", allocationSize = 1)
    @Column(name = "ATB870")
    var atb870: Long? = null

    @Column(name = "ATB86Q")
    var atb86q: String? = null

    @Column(name = "ATB86X")
    var atb86x: String? = null

    @Column(name = "ATB861")
    var atb861: String? = null

    @Column(name = "ATB871")
    var atb871: Long? = null

    @Column(name = "ATB872")
    var atb872: String? = null

    @Column(name = "ATB873")
    var atb873: String? = null

    @Column(name = "ATB874")
    var atb874: String? = null

    @Column(name = "ATB875")
    var atb875: LocalDate? = null

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

}
