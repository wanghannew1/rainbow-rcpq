package com.jlrc.rcpq.entity

import jakarta.persistence.*
import java.io.Serializable
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "TC72")
open class TC72 {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_TC72_ATC720")
    @SequenceGenerator(name = "SEQ_TC72_ATC720", sequenceName = "SEQ_TC72_ATC720", allocationSize = 1)
    @Column(name = "ATC720")
    var atc720: Long? = null

    @Column(name = "BAZ001")
    var baz001: Long? = null

    @Column(name = "BAZ002")
    var baz002: Long? = null

    @Column(name = "ATC721")
    var atc721: String? = null

    @Column(name = "ATC722")
    var atc722: String? = null

    @Column(name = "ATC723")
    var atc723: String? = null

    @Column(name = "ATC724")
    var atc724: LocalDate? = null

    @Column(name = "ATC725")
    var atc725: String? = null

    @Column(name = "ATC726")
    var atc726: LocalDate? = null

    @Column(name = "ATC727")
    var atc727: LocalDate? = null

    @Column(name = "ATC728")
    var atc728: String? = null

    @Column(name = "ATC729")
    var atc729: String? = null

    @Column(name = "ATC72A")
    var atc72a: String? = null

    @Column(name = "ATC72B")
    var atc72b: String? = null

    @Column(name = "ATC72C")
    var atc72c: String? = null

    @Column(name = "ATC72D")
    var atc72d: LocalDate? = null

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

    @Column(name = "ATC72E")
    var atc72e: String? = null

    @Column(name = "AAE013")
    var aae013: String? = null

}
