package com.jlrc.rcpq.entity

import jakarta.persistence.*
import java.io.Serializable
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "V_TB9A_YCX")
open class V_TB9A_YCX {
    @Id
    @Column(name = "ATB9A0")
    var atb9a0: Long? = null

    @Column(name = "UKEY")
    var ukey: Long? = null

    @Column(name = "DZZT")
    var dzzt: String? = null

    @Column(name = "ATB970")
    var atb970: Long? = null

    @Column(name = "ATB971")
    var atb971: BigDecimal? = null

    @Column(name = "ATB974")
    var atb974: LocalDate? = null

    @Column(name = "DZZY")
    var dzzy: String? = null

    @Column(name = "ATB973")
    var atb973: BigDecimal? = null

    @Column(name = "ATB9A1")
    var atb9a1: BigDecimal? = null

    @Column(name = "DSTATUS")
    var dstatus: String? = null

    @Column(name = "AAE011")
    var aae011: String? = null

    @Column(name = "AAE019")
    var aae019: String? = null

    @Column(name = "AAE036")
    var aae036: LocalDateTime? = null

    @Column(name = "AAE020")
    var aae020: String? = null

    @Column(name = "AAEA11")
    var aaea11: String? = null

    @Column(name = "AAEA19")
    var aaea19: String? = null

    @Column(name = "AAEA36")
    var aaea36: LocalDate? = null

    @Column(name = "AAEA20")
    var aaea20: String? = null

    @Column(name = "ZZZ011")
    var zzz011: String? = null

    @Column(name = "AAE013")
    var aae013: String? = null

}
