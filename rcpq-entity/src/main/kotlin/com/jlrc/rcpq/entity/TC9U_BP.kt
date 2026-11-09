package com.jlrc.rcpq.entity

import jakarta.persistence.*
import java.io.Serializable
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "TC9U_BP")
open class TC9U_BP {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_TC9U_ID")
    @SequenceGenerator(name = "SEQ_TC9U_ID", sequenceName = "SEQ_TC9U_ID", allocationSize = 1)
    @Column(name = "ID")
    var id: Long? = null

    @Column(name = "ATC9U0")
    var atc9u0: Long? = null

    @Column(name = "AAC003")
    var aac003: String? = null

    @Column(name = "AAC002")
    var aac002: String? = null

    @Column(name = "ATC9U1")
    var atc9u1: String? = null

    @Column(name = "ATC9U2")
    var atc9u2: String? = null

    @Column(name = "ATC9U3")
    var atc9u3: String? = null

    @Column(name = "ATC9U4")
    var atc9u4: String? = null

    @Column(name = "ATC9V1")
    var atc9v1: String? = null

    @Column(name = "ATC90K")
    var atc90k: String? = null

    @Column(name = "BP1")
    var bp1: String? = null

    @Column(name = "BP2")
    var bp2: String? = null

    @Column(name = "BP3")
    var bp3: String? = null

    @Column(name = "BP4")
    var bp4: String? = null

    @Column(name = "BP5")
    var bp5: String? = null

    @Column(name = "ACE10A")
    var ace10a: String? = null

    @Column(name = "ACE10B")
    var ace10b: String? = null

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

    @Column(name = "ATC9U5")
    var atc9u5: String? = null

    @Column(name = "ACE100")
    var ace100: Long? = null

    @Column(name = "ATC9V2")
    var atc9v2: String? = null

    @Column(name = "ATC9U8")
    var atc9u8: String? = null

    @Column(name = "AAE005")
    var aae005: String? = null

}
