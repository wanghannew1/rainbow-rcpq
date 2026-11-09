package com.jlrc.rcpq.entity

import jakarta.persistence.*
import java.io.Serializable
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "WTS_RESOURCE")
open class WTS_RESOURCE {
    @Id
    @Column(name = "ID")
    var id: String? = null

    @Column(name = "BAZ001")
    var baz001: Long? = null

    @Column(name = "BAZ002")
    var baz002: Long? = null

    @Column(name = "URL")
    var url: String? = null

    @Column(name = "LABLE")
    var lable: String? = null

    @Column(name = "RESOURCETYPE")
    var resourcetype: String? = null

    @Column(name = "OWNERSHIP")
    var ownership: String? = null

    @Column(name = "DESCRIPTION")
    var description: String? = null

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
