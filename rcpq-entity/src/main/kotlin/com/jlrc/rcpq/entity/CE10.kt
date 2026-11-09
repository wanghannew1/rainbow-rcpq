package com.jlrc.rcpq.entity

import jakarta.persistence.*
import java.io.Serializable
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "CE10")
open class CE10 {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_CE10_ACE100")
    @SequenceGenerator(name = "SEQ_CE10_ACE100", sequenceName = "SEQ_CE10_ACE100", allocationSize = 1)
    @Column(name = "ACE100")
    var ace100: Long? = null

    @Column(name = "BAZ001")
    var baz001: Long? = null

    @Column(name = "BAZ002")
    var baz002: Long? = null

    @Column(name = "ACE101")
    var ace101: Long? = null

    @Column(name = "ACE102")
    var ace102: String? = null

    @Column(name = "ACE103")
    var ace103: String? = null

    @Column(name = "ACE104")
    var ace104: Long? = null

    @Column(name = "ACE105")
    var ace105: Long? = null

    @Column(name = "ACE106")
    var ace106: Long? = null

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
