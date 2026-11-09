package com.jlrc.rcpq.entity

import jakarta.persistence.*
import java.io.Serializable
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "TB9A")
open class TB9A {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_TB9A_ATB9A0")
    @SequenceGenerator(name = "SEQ_TB9A_ATB9A0", sequenceName = "SEQ_TB9A_ATB9A0", allocationSize = 1)
    @Column(name = "ATB9A0")
    var atb9a0: Long? = null

    @Column(name = "BAZ001")
    var baz001: Long? = null

    @Column(name = "BAZ002")
    var baz002: Long? = null

    @Column(name = "ATB970")
    var atb970: Long? = null

    @Column(name = "UKEY")
    var ukey: Long? = null

    @Column(name = "ATB9A1")
    var atb9a1: BigDecimal? = null

    @Column(name = "DSTATUS")
    var dstatus: String? = null

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

    @Column(name = "AAEA11")
    var aaea11: String? = null

    @Column(name = "AAEA17")
    var aaea17: String? = null

    @Column(name = "AAEA36")
    var aaea36: LocalDate? = null

    @Column(name = "AAEA19")
    var aaea19: String? = null

    @Column(name = "AAEA20")
    var aaea20: String? = null

    @Column(name = "AAEA22")
    var aaea22: String? = null

}
