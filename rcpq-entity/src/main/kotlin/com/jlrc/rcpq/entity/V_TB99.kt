package com.jlrc.rcpq.entity

import jakarta.persistence.*
import java.io.Serializable
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "V_TB99")
open class V_TB99 {
    @Id
    @Column(name = "ATB990")
    var atb990: Long? = null

    @Column(name = "ATB99Z")
    var atb99z: String? = null

    @Column(name = "ATB991")
    var atb991: String? = null

    @Column(name = "ATB992")
    var atb992: String? = null

    @Column(name = "ATB993")
    var atb993: String? = null

    @Column(name = "ATB994_1")
    var atb994_1: BigDecimal? = null

    @Column(name = "ATB994_2")
    var atb994_2: BigDecimal? = null

    @Column(name = "ATB994_3")
    var atb994_3: BigDecimal? = null

    @Column(name = "ATB994_4")
    var atb994_4: BigDecimal? = null

    @Column(name = "ATB994_5")
    var atb994_5: BigDecimal? = null

    @Column(name = "ATB994_6")
    var atb994_6: BigDecimal? = null

    @Column(name = "ATB994_7")
    var atb994_7: BigDecimal? = null

    @Column(name = "ATB994_8")
    var atb994_8: BigDecimal? = null

    @Column(name = "ATB994_9")
    var atb994_9: BigDecimal? = null

    @Column(name = "ATB994_A")
    var atb994_a: BigDecimal? = null

    @Column(name = "ATB994_B")
    var atb994_b: BigDecimal? = null

    @Column(name = "ATB994")
    var atb994: BigDecimal? = null

    @Column(name = "DSTATUS")
    var dstatus: String? = null

    @Column(name = "AAE013")
    var aae013: String? = null

    @Column(name = "ATB995")
    var atb995: String? = null

    @Column(name = "ATB996")
    var atb996: String? = null

    @Column(name = "AAE136")
    var aae136: LocalDate? = null

    @Column(name = "AAE119")
    var aae119: String? = null

    @Column(name = "AAE120")
    var aae120: String? = null

    @Column(name = "AAE236")
    var aae236: LocalDate? = null

    @Column(name = "AAE219")
    var aae219: String? = null

    @Column(name = "AAE220")
    var aae220: String? = null

    @Column(name = "AAE336")
    var aae336: LocalDate? = null

    @Column(name = "AAE319")
    var aae319: String? = null

    @Column(name = "AAE320")
    var aae320: String? = null

    @Column(name = "AAE036")
    var aae036: LocalDateTime? = null

    @Column(name = "AAE011")
    var aae011: String? = null

    @Column(name = "AAE019")
    var aae019: String? = null

    @Column(name = "AAE020")
    var aae020: String? = null

    @Column(name = "ATB99Y")
    var atb99y: String? = null

    @Column(name = "AAA030")
    var aaa030: Long? = null

    @Column(name = "AAA031")
    var aaa031: String? = null

    @Column(name = "AAB004")
    var aab004: String? = null

}
