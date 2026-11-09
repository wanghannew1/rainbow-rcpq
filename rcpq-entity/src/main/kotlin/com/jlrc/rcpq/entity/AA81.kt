package com.jlrc.rcpq.entity

import jakarta.persistence.*
import java.io.Serializable
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "AA81")
open class AA81 {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_AA81_AAA810")
    @SequenceGenerator(name = "SEQ_AA81_AAA810", sequenceName = "SEQ_AA81_AAA810", allocationSize = 1)
    @Column(name = "AAA810")
    var aaa810: Long? = null

    @Column(name = "BAZ001")
    var baz001: Long? = null

    @Column(name = "BAZ002")
    var baz002: Long? = null

    @Column(name = "AAA800")
    var aaa800: Long? = null

    @Column(name = "AAA901")
    var aaa901: String? = null

    @Column(name = "AAA902")
    var aaa902: String? = null

    @Column(name = "ACB208")
    var acb208: String? = null

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
