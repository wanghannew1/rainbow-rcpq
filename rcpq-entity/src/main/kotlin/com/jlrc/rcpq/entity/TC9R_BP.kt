package com.jlrc.rcpq.entity

import jakarta.persistence.*
import java.io.Serializable
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "TC9R_BP")
open class TC9R_BP {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_TC9R_ATC9R0")
    @SequenceGenerator(name = "SEQ_TC9R_ATC9R0", sequenceName = "SEQ_TC9R_ATC9R0", allocationSize = 1)
    @Column(name = "ATC9R0")
    var atc9r0: Long? = null

    @Column(name = "ATC9Q0")
    var atc9q0: Long? = null

    @Column(name = "AAC003")
    var aac003: String? = null

    @Column(name = "AAC002")
    var aac002: String? = null

    @Column(name = "ATC9R3")
    var atc9r3: String? = null

    @Column(name = "ATC9R4")
    var atc9r4: String? = null

    @Column(name = "ATC9R5")
    var atc9r5: String? = null

    @Column(name = "ATC9R6")
    var atc9r6: String? = null

    @Column(name = "ATC9R7")
    var atc9r7: String? = null

    @Column(name = "ATC9RB")
    var atc9rb: String? = null

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

    @Column(name = "ACE10A")
    var ace10a: String? = null

    @Column(name = "ACE10B")
    var ace10b: String? = null

    @Column(name = "ACE100")
    var ace100: Long? = null

}
