package com.jlrc.rcpq.entity

import jakarta.persistence.*
import java.io.Serializable
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "WTS_POLICY")
open class WTS_POLICY {
    @Id
    @Column(name = "POLICY_ID")
    var policy_id: String? = null

    @Column(name = "PID")
    var pid: String? = null

    @Column(name = "CONTEXT")
    var context: String? = null

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

    @Column(name = "LABEL")
    var label: String? = null

    @Column(name = "POLICYTYPE")
    var policytype: String? = null

    @Column(name = "ORDERNUM")
    var ordernum: Long? = null

    @Column(name = "CLASSTYPE")
    var classtype: String? = null

    @Column(name = "UPDATEDAY")
    var updateday: LocalDateTime? = null

}
