package com.jlrc.rcpq.entity

import jakarta.persistence.*
import java.io.Serializable
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "AA77")
open class AA77 {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_AA77_AAA770")
    @SequenceGenerator(name = "SEQ_AA77_AAA770", sequenceName = "SEQ_AA77_AAA770", allocationSize = 1)
    @Column(name = "AAA770")
    var aaa770: Long? = null

    @Column(name = "BAZ001")
    var baz001: Long? = null

    @Column(name = "BAZ002")
    var baz002: Long? = null

    @Column(name = "AAA771")
    var aaa771: Long? = null

    @Column(name = "AAA772")
    var aaa772: Long? = null

    @Column(name = "AAA773")
    var aaa773: String? = null

    @Column(name = "AAA774")
    var aaa774: BigDecimal? = null

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
