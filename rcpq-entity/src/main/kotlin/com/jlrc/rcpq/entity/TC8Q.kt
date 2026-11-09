package com.jlrc.rcpq.entity

import jakarta.persistence.*
import java.io.Serializable
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "TC8Q")
open class TC8Q {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_TC8Q_ATC8Q0")
    @SequenceGenerator(name = "SEQ_TC8Q_ATC8Q0", sequenceName = "SEQ_TC8Q_ATC8Q0", allocationSize = 1)
    @Column(name = "ATC8Q0")
    var atc8q0: Long? = null

    @Column(name = "AAE002")
    var aae002: Long? = null

    @Column(name = "ATC900")
    var atc900: Long? = null

    @Column(name = "AAC003")
    var aac003: String? = null

    @Column(name = "AAC002")
    var aac002: String? = null

    @Column(name = "ATB930")
    var atb930: Long? = null

    @Column(name = "ATB931")
    var atb931: String? = null

    @Column(name = "ATB900")
    var atb900: Long? = null

    @Column(name = "AAB004")
    var aab004: String? = null

    @Column(name = "ATC90C")
    var atc90c: LocalDate? = null

    @Column(name = "ATC90D")
    var atc90d: LocalDate? = null

    @Column(name = "AAE013")
    var aae013: String? = null

    @Column(name = "AAB001")
    var aab001: Long? = null

}
