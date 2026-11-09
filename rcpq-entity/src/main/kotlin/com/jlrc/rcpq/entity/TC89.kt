package com.jlrc.rcpq.entity

import jakarta.persistence.*
import java.io.Serializable
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "TC89")
open class TC89 {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_TC89_ATC890")
    @SequenceGenerator(name = "SEQ_TC89_ATC890", sequenceName = "SEQ_TC89_ATC890", allocationSize = 1)
    @Column(name = "ATC890")
    var atc890: Long? = null

    @Column(name = "ATC830")
    var atc830: Long? = null

    @Column(name = "AAC001")
    var aac001: Long? = null

    @Column(name = "AAB001")
    var aab001: Long? = null

    @Column(name = "ATC831")
    var atc831: String? = null

    @Column(name = "AAC002")
    var aac002: String? = null

    @Column(name = "AAC003")
    var aac003: String? = null

    @Column(name = "AAC004")
    var aac004: String? = null

    @Column(name = "ATC832")
    var atc832: LocalDate? = null

    @Column(name = "AAB004")
    var aab004: String? = null

    @Column(name = "ATC833")
    var atc833: String? = null

    @Column(name = "ATC894")
    var atc894: String? = null

    @Column(name = "ATC895")
    var atc895: BigDecimal? = null

    @Column(name = "ATC896")
    var atc896: BigDecimal? = null

    @Column(name = "ATC897")
    var atc897: LocalDate? = null

    @Column(name = "ATC898")
    var atc898: String? = null

    @Column(name = "ATC899")
    var atc899: BigDecimal? = null

    @Column(name = "ATC89B")
    var atc89b: BigDecimal? = null

    @Column(name = "ATC89A")
    var atc89a: BigDecimal? = null

    @Column(name = "ATC89C")
    var atc89c: LocalDate? = null

    @Column(name = "ATC89D")
    var atc89d: String? = null

    @Column(name = "ATC89E")
    var atc89e: LocalDate? = null

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
