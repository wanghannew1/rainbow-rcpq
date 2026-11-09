package com.jlrc.rcpq.entity

import jakarta.persistence.*
import java.io.Serializable
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "AA97_BG")
open class AA97_BG {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_AA97_BG_ID")
    @SequenceGenerator(name = "SEQ_AA97_BG_ID", sequenceName = "SEQ_AA97_BG_ID", allocationSize = 1)
    @Column(name = "ID")
    var id: Long? = null

    @Column(name = "BAZ001")
    var baz001: Long? = null

    @Column(name = "BAZ002")
    var baz002: Long? = null

    @Column(name = "TNAME")
    var tname: String? = null

    @Column(name = "TKEY")
    var tkey: Long? = null

    @Column(name = "ATC900")
    var atc900: Long? = null

    @Column(name = "AA97TYPE")
    var aa97type: String? = null

    @Column(name = "AAA300_BGQ")
    var aaa300_bgq: BigDecimal? = null

    @Column(name = "AAA300_BGH")
    var aaa300_bgh: BigDecimal? = null

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
