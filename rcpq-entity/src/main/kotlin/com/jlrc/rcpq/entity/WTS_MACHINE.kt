package com.jlrc.rcpq.entity

import jakarta.persistence.*
import java.io.Serializable
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "WTS_MACHINE")
open class WTS_MACHINE {
    @Id
    @Column(name = "MACHINE_ID")
    var machine_id: String? = null

    @Column(name = "NAVIGATION_ID")
    var navigation_id: String? = null

    @Column(name = "MACHINE_NAME")
    var machine_name: String? = null

    @Column(name = "MAC_ADDRESS")
    var mac_address: String? = null

    @Column(name = "PASSWORD")
    var password: String? = null

    @Column(name = "ENABLE")
    var enable: String? = null

    @Column(name = "DISABLE_MSG")
    var disable_msg: String? = null

    @Column(name = "DESCRIPTION")
    var description: String? = null

    @Column(name = "BAZ001")
    var baz001: Long? = null

    @Column(name = "BAZ002")
    var baz002: Long? = null

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

    @Column(name = "PASSWORDC")
    var passwordc: String? = null

}
