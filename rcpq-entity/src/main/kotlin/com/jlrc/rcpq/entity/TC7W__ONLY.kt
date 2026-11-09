package com.jlrc.rcpq.entity

import jakarta.persistence.*
import java.io.Serializable
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "TC7W")
open class TC7W__ONLY {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_TC7W_ATC7W0")
    @SequenceGenerator(name = "SEQ_TC7W_ATC7W0", sequenceName = "SEQ_TC7W_ATC7W0", allocationSize = 1)
    @Column(name = "ATC7W0")
    var atc7w0: Long? = null

    @Column(name = "BAZ001")
    var baz001: Long? = null

    @Column(name = "BAZ002")
    var baz002: Long? = null

    @Column(name = "ATC7V0")
    var atc7v0: Long? = null

    @Column(name = "ATC700")
    var atc700: Long? = null

    @Column(name = "ATC7W1")
    var atc7w1: Long? = null

    @Column(name = "ATC7W2")
    var atc7w2: Long? = null

    @Column(name = "ATC7W3")
    var atc7w3: BigDecimal? = null

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
