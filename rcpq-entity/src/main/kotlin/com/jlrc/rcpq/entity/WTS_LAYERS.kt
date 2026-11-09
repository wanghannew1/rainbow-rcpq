package com.jlrc.rcpq.entity

import jakarta.persistence.*
import java.io.Serializable
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "WTS_LAYERS")
@IdClass(WTS_LAYERSId::class)
open class WTS_LAYERS {
    @Id
    @Column(name = "LAYER_ID")
    var layer_id: String? = null

    @Id
    @Column(name = "LAYER_TID")
    var layer_tid: String? = null

    @Column(name = "LAYER_PID")
    var layer_pid: String? = null

    @Column(name = "LAYER_XML")
    var layer_xml: String? = null

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

    @Column(name = "LAYER_LABEL")
    var layer_label: String? = null

    @Column(name = "LAYER_TYPE")
    var layer_type: String? = null

    @Column(name = "LAYER_INDEX")
    var layer_index: Long? = null

}
data class WTS_LAYERSId(
    val layer_id: String? = null,
    val layer_tid: String? = null,
) : Serializable
