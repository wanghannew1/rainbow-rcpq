package com.jlrc.rcpq.entity

import jakarta.persistence.*
import java.io.Serializable
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "TC9L")
open class TC9L {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_TC9L_ATC9L0")
    @SequenceGenerator(name = "SEQ_TC9L_ATC9L0", sequenceName = "SEQ_TC9L_ATC9L0", allocationSize = 1)
    @Column(name = "ATC9L0")
    var atc9l0: Long? = null

    @Column(name = "ATC9L1")
    var atc9l1: String? = null

    @Column(name = "ATC9L2")
    var atc9l2: String? = null

    @Column(name = "ATC9L3")
    var atc9l3: LocalDate? = null

    @Column(name = "ATC9L4")
    var atc9l4: BigDecimal? = null

    @Column(name = "ATC9L5")
    var atc9l5: BigDecimal? = null

    @Column(name = "ATC9L6")
    var atc9l6: BigDecimal? = null

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
