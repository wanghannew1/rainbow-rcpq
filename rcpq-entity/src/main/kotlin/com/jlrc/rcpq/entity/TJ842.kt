package com.jlrc.rcpq.entity

import jakarta.persistence.*
import java.io.Serializable
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "TJ842")
open class TJ842 {
    @Id
    @Column(name = "TJ8420")
    var tj8420: Long? = null

    @Column(name = "TJ0000")
    var tj0000: Long? = null

    @Column(name = "AAA020")
    var aaa020: String? = null

    @Column(name = "AAA021")
    var aaa021: String? = null

    @Column(name = "TJ0002")
    var tj0002: LocalDate? = null

    @Column(name = "TJ0003")
    var tj0003: LocalDate? = null

    @Column(name = "AAB001")
    var aab001: Long? = null

    @Column(name = "AAB004")
    var aab004: String? = null

    @Column(name = "AC0009")
    var ac0009: Long? = null

    @Column(name = "TJ8421")
    var tj8421: Long? = null

    @Column(name = "TJ8422")
    var tj8422: Long? = null

    @Column(name = "TJ8423")
    var tj8423: Long? = null

    @Column(name = "TJ8424")
    var tj8424: Long? = null

    @Column(name = "TJ8425")
    var tj8425: Long? = null

    @Column(name = "TJ8426")
    var tj8426: Long? = null

    @Column(name = "TJ8427")
    var tj8427: Long? = null

    @Column(name = "TJ8428")
    var tj8428: Long? = null

    @Column(name = "TJ8429")
    var tj8429: Long? = null

    @Column(name = "TJ842A")
    var tj842a: Long? = null

    @Column(name = "TJ000X")
    var tj000x: String? = null

    @Column(name = "TJ000W")
    var tj000w: String? = null

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

    @Column(name = "BAZ001")
    var baz001: Long? = null

    @Column(name = "BAZ002")
    var baz002: Long? = null

}
