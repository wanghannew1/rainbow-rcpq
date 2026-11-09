package com.jlrc.rcpq.entity

import jakarta.persistence.*
import java.io.Serializable
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "V_DW_EXPIRE_TX")
@IdClass(V_DW_EXPIRE_TXId::class)
open class V_DW_EXPIRE_TX {
    @Id
    @Column(name = "AAE002")
    var aae002: Long? = null

    @Id
    @Column(name = "AAB001_1")
    var aab001_1: Long? = null

    @Column(name = "AAB001")
    var aab001: Long? = null

    @Column(name = "AAB004")
    var aab004: String? = null

    @Column(name = "AAB00A")
    var aab00a: String? = null

    @Column(name = "ATB904")
    var atb904: String? = null

    @Column(name = "AAE007")
    var aae007: String? = null

    @Column(name = "AAE006")
    var aae006: String? = null

    @Column(name = "ATB930")
    var atb930: Long? = null

    @Column(name = "ATB931")
    var atb931: String? = null

    @Column(name = "ATB900")
    var atb900: Long? = null

    @Column(name = "RS")
    var rs: Long? = null

    @Column(name = "AAB00E")
    var aab00e: String? = null

    @Column(name = "AAB004_1")
    var aab004_1: String? = null

}
data class V_DW_EXPIRE_TXId(
    val aae002: Long? = null,
    val aab001_1: Long? = null,
) : Serializable
