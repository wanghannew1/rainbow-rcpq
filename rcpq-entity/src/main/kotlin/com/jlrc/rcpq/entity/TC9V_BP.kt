package com.jlrc.rcpq.entity

import jakarta.persistence.*
import java.io.Serializable
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "TC9V_BP")
open class TC9V_BP {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_TC9V_ID")
    @SequenceGenerator(name = "SEQ_TC9V_ID", sequenceName = "SEQ_TC9V_ID", allocationSize = 1)
    @Column(name = "ID")
    var id: Long? = null

    @Column(name = "ATC900")
    var atc900: Long? = null

    @Column(name = "AAC001")
    var aac001: Long? = null

    @Column(name = "AAC003")
    var aac003: String? = null

    @Column(name = "AAC002")
    var aac002: String? = null

    @Column(name = "ATC9V1")
    var atc9v1: String? = null

    @Column(name = "ATC9V2")
    var atc9v2: String? = null

    @Column(name = "ATC9V3")
    var atc9v3: String? = null

    @Column(name = "ATC9V4")
    var atc9v4: String? = null

    @Column(name = "ATC9U1")
    var atc9u1: String? = null

    @Column(name = "ATC9U2")
    var atc9u2: String? = null

    @Column(name = "ATC9U5")
    var atc9u5: String? = null

    @Column(name = "ATC9V5")
    var atc9v5: String? = null

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

    @Column(name = "ATC9VA")
    var atc9va: String? = null

    @Column(name = "ATC9VB")
    var atc9vb: String? = null

    @Column(name = "ATC9VH")
    var atc9vh: String? = null

    @Column(name = "BP1")
    var bp1: String? = null

    @Column(name = "BP2")
    var bp2: String? = null

    @Column(name = "BP3")
    var bp3: String? = null

    @Column(name = "BP4")
    var bp4: String? = null

    @Column(name = "ATC9V0")
    var atc9v0: String? = null

    @Column(name = "JSNY")
    var jsny: Long? = null

}
