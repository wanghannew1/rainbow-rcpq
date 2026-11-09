package com.jlrc.rcpq.entity

import jakarta.persistence.*
import java.io.Serializable
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "V_TC90_XJ")
open class V_TC90_XJ {
    @Id
    @Column(name = "ATC900")
    var atc900: Long? = null

    @Column(name = "AAA030")
    var aaa030: Long? = null

    @Column(name = "AAA031")
    var aaa031: String? = null

    @Column(name = "AAB004")
    var aab004: String? = null

    @Column(name = "AAC003")
    var aac003: String? = null

    @Column(name = "AAC002")
    var aac002: String? = null

    @Column(name = "AAA401")
    var aaa401: String? = null

    @Column(name = "AAA100")
    var aaa100: String? = null

    @Column(name = "AAA101")
    var aaa101: String? = null

    @Column(name = "AAA201")
    var aaa201: Long? = null

    @Column(name = "AAA202")
    var aaa202: Long? = null

    @Column(name = "AAA301")
    var aaa301: BigDecimal? = null

    @Column(name = "BBA001")
    var bba001: String? = null

    @Column(name = "BAA001")
    var baa001: String? = null

    @Column(name = "BAA100")
    var baa100: Long? = null

    @Column(name = "AAA403")
    var aaa403: String? = null

    @Column(name = "AAA304")
    var aaa304: BigDecimal? = null

    @Column(name = "BBA003")
    var bba003: String? = null

    @Column(name = "BAA003")
    var baa003: String? = null

    @Column(name = "BAA300")
    var baa300: Long? = null

    @Column(name = "BAA309")
    var baa309: Long? = null

    @Column(name = "AAA402")
    var aaa402: String? = null

    @Column(name = "AAA110")
    var aaa110: String? = null

    @Column(name = "AAA111")
    var aaa111: String? = null

    @Column(name = "AAA601")
    var aaa601: Long? = null

    @Column(name = "AAA602")
    var aaa602: Long? = null

    @Column(name = "AAA302")
    var aaa302: BigDecimal? = null

    @Column(name = "BBA002")
    var bba002: String? = null

    @Column(name = "BAA002")
    var baa002: String? = null

    @Column(name = "AAA404")
    var aaa404: String? = null

    @Column(name = "AAA305")
    var aaa305: BigDecimal? = null

    @Column(name = "BBA004")
    var bba004: String? = null

    @Column(name = "AAA405")
    var aaa405: String? = null

    @Column(name = "AAA306")
    var aaa306: BigDecimal? = null

    @Column(name = "BBA005")
    var bba005: String? = null

    @Column(name = "ATB90T")
    var atb90t: String? = null

    @Column(name = "ATC93BD")
    var atc93bd: Long? = null

    @Column(name = "BAA209")
    var baa209: Long? = null

    @Column(name = "AAA407")
    var aaa407: String? = null

    @Column(name = "AAA120")
    var aaa120: String? = null

    @Column(name = "AAA121")
    var aaa121: String? = null

    @Column(name = "AAA501")
    var aaa501: Long? = null

    @Column(name = "AAA502")
    var aaa502: Long? = null

    @Column(name = "AAA303")
    var aaa303: BigDecimal? = null

    @Column(name = "ATB90S")
    var atb90s: String? = null

    @Column(name = "CAA001")
    var caa001: String? = null

    @Column(name = "CAA002")
    var caa002: String? = null

    @Column(name = "CAA009")
    var caa009: Long? = null

    @Column(name = "AAE002")
    var aae002: Long? = null

    @Column(name = "AAE003")
    var aae003: Long? = null

}
