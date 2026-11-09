package com.jlrc.rcpq.entity

import jakarta.persistence.*
import java.io.Serializable
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "AC0D")
open class AC0D {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_AC0D_AAC0D0")
    @SequenceGenerator(name = "SEQ_AC0D_AAC0D0", sequenceName = "SEQ_AC0D_AAC0D0", allocationSize = 1)
    @Column(name = "AAC0D0")
    var aac0d0: Long? = null

    @Column(name = "BAZ001")
    var baz001: Long? = null

    @Column(name = "BAZ002")
    var baz002: Long? = null

    @Column(name = "AAC001")
    var aac001: Long? = null

    @Column(name = "AAC045")
    var aac045: String? = null

    @Column(name = "AAC0D2")
    var aac0d2: String? = null

    @Column(name = "AAC0D3")
    var aac0d3: String? = null

    @Column(name = "AAC046")
    var aac046: LocalDate? = null

    @Column(name = "AAC0D5")
    var aac0d5: String? = null

    @Column(name = "AAC0D6")
    var aac0d6: BigDecimal? = null

    @Column(name = "ATC011")
    var atc011: String? = null

    @Column(name = "AAC037")
    var aac037: String? = null

    @Column(name = "ACC01G")
    var acc01g: String? = null

    @Column(name = "AAC0D4")
    var aac0d4: String? = null

    @Column(name = "ATC01G")
    var atc01g: String? = null

    @Column(name = "AAC038")
    var aac038: String? = null

    @Column(name = "AAC039")
    var aac039: String? = null

    @Column(name = "AAC041")
    var aac041: String? = null

    @Column(name = "AAC042")
    var aac042: String? = null

    @Column(name = "AAC0D7")
    var aac0d7: String? = null

    @Column(name = "AAC043")
    var aac043: String? = null

    @Column(name = "AAC0D1")
    var aac0d1: String? = null

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

    @Column(name = "AAE030")
    var aae030: LocalDate? = null

}
