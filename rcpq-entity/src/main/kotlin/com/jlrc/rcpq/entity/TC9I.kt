package com.jlrc.rcpq.entity

import jakarta.persistence.*
import java.io.Serializable
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "TC9I")
open class TC9I {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_TC9I_ATC9I0")
    @SequenceGenerator(name = "SEQ_TC9I_ATC9I0", sequenceName = "SEQ_TC9I_ATC9I0", allocationSize = 1)
    @Column(name = "ATC9I0")
    var atc9i0: Long? = null

    @Column(name = "ATC980")
    var atc980: Long? = null

    @Column(name = "AAC001")
    var aac001: Long? = null

    @Column(name = "ATC9I1")
    var atc9i1: LocalDate? = null

    @Column(name = "ATC9I2")
    var atc9i2: LocalDate? = null

    @Column(name = "ATC9I3")
    var atc9i3: Long? = null

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
