package com.jlrc.rcpq.entity

import jakarta.persistence.*
import java.io.Serializable
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "AA04")
open class AA04 {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_AA04_AAA040")
    @SequenceGenerator(name = "SEQ_AA04_AAA040", sequenceName = "SEQ_AA04_AAA040", allocationSize = 1)
    @Column(name = "AAA040")
    var aaa040: Long? = null

    @Column(name = "AAA041")
    var aaa041: String? = null

    @Column(name = "TYPE")
    var type: String? = null

    @Column(name = "AAA042")
    var aaa042: String? = null

    @Column(name = "AAA043")
    var aaa043: String? = null

    @Column(name = "ATB930")
    var atb930: Long? = null

    @Column(name = "AAA044")
    var aaa044: String? = null

    @Column(name = "AAA045")
    var aaa045: String? = null

    @Column(name = "AAA046")
    var aaa046: String? = null

    @Column(name = "AAA047")
    var aaa047: String? = null

    @Column(name = "AAA048")
    var aaa048: String? = null

    @Column(name = "AAE001")
    var aae001: Long? = null

}
