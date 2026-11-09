package com.jlrc.rcpq.entity

import jakarta.persistence.*
import java.io.Serializable
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "TC9D")
open class TC9D {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_TC9D_ATC9D0")
    @SequenceGenerator(name = "SEQ_TC9D_ATC9D0", sequenceName = "SEQ_TC9D_ATC9D0", allocationSize = 1)
    @Column(name = "ATC9D0")
    var atc9d0: Long? = null

    @Column(name = "BAZ001")
    var baz001: Long? = null

    @Column(name = "BAZ002")
    var baz002: Long? = null

    @Column(name = "ATC9D1")
    var atc9d1: String? = null

    @Column(name = "ATC9D2")
    var atc9d2: String? = null

    @Column(name = "ATC9D3")
    var atc9d3: LocalDate? = null

    @Column(name = "ATC9D4")
    var atc9d4: LocalDate? = null

    @Column(name = "ATC9D5")
    var atc9d5: String? = null

    @Column(name = "ATC9D7")
    var atc9d7: Long? = null

    @Column(name = "ATC9D8")
    var atc9d8: Long? = null

    @Column(name = "ATC9D9")
    var atc9d9: Long? = null

    @Column(name = "ATC9DA")
    var atc9da: String? = null

    @Column(name = "ATC9DB")
    var atc9db: String? = null

    @Column(name = "ATC9DC")
    var atc9dc: String? = null

    @Column(name = "ATC9DD")
    var atc9dd: LocalDate? = null

    @Column(name = "ATC9DE")
    var atc9de: LocalDate? = null

    @Column(name = "ATC9DF")
    var atc9df: LocalDate? = null

    @Column(name = "ATC900")
    var atc900: Long? = null

    @Column(name = "AAC001")
    var aac001: Long? = null

    @Column(name = "AAC003")
    var aac003: String? = null

    @Column(name = "AAC002")
    var aac002: String? = null

    @Column(name = "ATC909")
    var atc909: String? = null

    @Column(name = "ATC90A")
    var atc90a: String? = null

    @Column(name = "ATC90B")
    var atc90b: LocalDate? = null

    @Column(name = "ATC90C")
    var atc90c: LocalDate? = null

    @Column(name = "ATC90D")
    var atc90d: LocalDate? = null

    @Column(name = "ATB904")
    var atb904: String? = null

    @Column(name = "ATC90F")
    var atc90f: String? = null

    @Column(name = "ATC90G")
    var atc90g: String? = null

    @Column(name = "ATB900")
    var atb900: Long? = null

    @Column(name = "AAB001")
    var aab001: Long? = null

    @Column(name = "AAB003")
    var aab003: String? = null

    @Column(name = "AAB007")
    var aab007: String? = null

    @Column(name = "AAB004")
    var aab004: String? = null

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
