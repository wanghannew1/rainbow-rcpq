package com.jlrc.rcpq.entity

import jakarta.persistence.*
import java.io.Serializable
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "TC7Q")
open class TC7Q {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_TC7Q_ATC7Q0")
    @SequenceGenerator(name = "SEQ_TC7Q_ATC7Q0", sequenceName = "SEQ_TC7Q_ATC7Q0", allocationSize = 1)
    @Column(name = "ATC7Q0")
    var atc7q0: Long? = null

    @Column(name = "BAZ001")
    var baz001: Long? = null

    @Column(name = "BAZ002")
    var baz002: Long? = null

    @Column(name = "ATC7Q1")
    var atc7q1: LocalDate? = null

    @Column(name = "AAC003")
    var aac003: String? = null

    @Column(name = "AAC002")
    var aac002: String? = null

    @Column(name = "AAC004")
    var aac004: String? = null

    @Column(name = "ATC7Q2")
    var atc7q2: String? = null

    @Column(name = "ATC7Q3")
    var atc7q3: String? = null

    @Column(name = "ATC7Q4")
    var atc7q4: BigDecimal? = null

    @Column(name = "ATC7Q5")
    var atc7q5: LocalDate? = null

    @Column(name = "ATC7Q6")
    var atc7q6: String? = null

    @Column(name = "ATC7Q7")
    var atc7q7: String? = null

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

    @Column(name = "AAE100")
    var aae100: String? = null

    @Column(name = "ATC700")
    var atc700: BigDecimal? = null
}
