package com.jlrc.rcpq.entity

import jakarta.persistence.*
import java.io.Serializable
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "AA99")
open class AA99 {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_AA99_AAA990")
    @SequenceGenerator(name = "SEQ_AA99_AAA990", sequenceName = "SEQ_AA99_AAA990", allocationSize = 1)
    @Column(name = "AAA990")
    var aaa990: Long? = null

    @Column(name = "AAA991")
    var aaa991: String? = null

    @Column(name = "AAA992")
    var aaa992: Long? = null

    @Column(name = "AAA993")
    var aaa993: Long? = null

    @Column(name = "AAA994")
    var aaa994: Long? = null

    @Column(name = "AAA995")
    var aaa995: Long? = null

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

    @Column(name = "AAA996")
    var aaa996: Long? = null

    @Column(name = "AAA997")
    var aaa997: Long? = null

    @Column(name = "AAA998")
    var aaa998: Long? = null

    @Column(name = "AAA999")
    var aaa999: Long? = null

}
