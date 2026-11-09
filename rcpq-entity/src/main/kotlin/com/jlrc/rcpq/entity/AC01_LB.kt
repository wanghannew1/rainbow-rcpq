package com.jlrc.rcpq.entity

import jakarta.persistence.*
import java.io.Serializable
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "AC01_LB")
open class AC01_LB {
    @Id
    @Column(name = "AAC001")
    var aac001: String? = null

    @Column(name = "AKC020")
    var akc020: String? = null

    @Column(name = "AAC002")
    var aac002: String? = null

    @Column(name = "AAC003")
    var aac003: String? = null

    @Column(name = "AAC004")
    var aac004: String? = null

    @Column(name = "AAC005")
    var aac005: String? = null

    @Column(name = "AAC006")
    var aac006: LocalDateTime? = null

    @Column(name = "AAC009")
    var aac009: String? = null

    @Column(name = "AAC011")
    var aac011: String? = null

    @Column(name = "AAE005")
    var aae005: String? = null

    @Column(name = "AAE006")
    var aae006: String? = null

    @Column(name = "AAE007")
    var aae007: String? = null

    @Column(name = "AAC031")
    var aac031: String? = null

    @Column(name = "AAC000")
    var aac000: Long? = null

}
