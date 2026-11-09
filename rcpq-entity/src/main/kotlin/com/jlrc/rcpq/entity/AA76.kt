package com.jlrc.rcpq.entity

import jakarta.persistence.*
import java.io.Serializable
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "AA76")
open class AA76 {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_AA76_AAA760")
    @SequenceGenerator(name = "SEQ_AA76_AAA760", sequenceName = "SEQ_AA76_AAA760", allocationSize = 1)
    @Column(name = "AAA760")
    var aaa760: Long? = null

    @Column(name = "BAZ001")
    var baz001: Long? = null

    @Column(name = "BAZ002")
    var baz002: Long? = null

    @Column(name = "AAA761")
    var aaa761: String? = null

    @Column(name = "AAA762")
    var aaa762: String? = null

    @Column(name = "AAA763")
    var aaa763: String? = null

    @Column(name = "BCC70M")
    var bcc70m: String? = null

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

    @Column(name = "AAA764")
    var aaa764: String? = null

}
