package com.jlrc.rcpq.entity

import jakarta.persistence.*
import java.io.Serializable
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "TC8R")
open class TC8R {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_TC8R_ATC8R0")
    @SequenceGenerator(name = "SEQ_TC8R_ATC8R0", sequenceName = "SEQ_TC8R_ATC8R0", allocationSize = 1)
    @Column(name = "ATC8R0")
    var atc8r0: Long? = null

    @Column(name = "ATC9Q0")
    var atc9q0: Long? = null

    @Column(name = "ATC8R1")
    var atc8r1: String? = null

    @Column(name = "ATC8R2")
    var atc8r2: LocalDate? = null

    @Column(name = "ATC8R3")
    var atc8r3: String? = null

    @Column(name = "ATC8R4")
    var atc8r4: LocalDate? = null

    @Column(name = "ATC8R5")
    var atc8r5: String? = null

    @Column(name = "ATC8R6")
    var atc8r6: LocalDate? = null

    @Column(name = "ATC8R7")
    var atc8r7: String? = null

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
