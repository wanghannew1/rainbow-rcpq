package com.jlrc.rcpq.entity

import jakarta.persistence.*
import java.io.Serializable
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "V_TB93_TC93")
open class V_TB93_TC93 {
    @Id
    @Column(name = "ATC930")
    var atc930: Long? = null

    @Column(name = "ATB930")
    var atb930: Long? = null

    @Column(name = "AAB004")
    var aab004: String? = null

    @Column(name = "ATC931")
    var atc931: Long? = null

    @Column(name = "ATC932")
    var atc932: Long? = null

    @Column(name = "ATC937")
    var atc937: String? = null

    @Column(name = "ATB931")
    var atb931: String? = null

    @Column(name = "AAC002")
    var aac002: String? = null

    @Column(name = "AAC003")
    var aac003: String? = null

    @Column(name = "BBB001")
    var bbb001: String? = null

    @Column(name = "BBB002")
    var bbb002: String? = null

    @Column(name = "ATB90A")
    var atb90a: BigDecimal? = null

    @Column(name = "ATB90Z")
    var atb90z: BigDecimal? = null

    @Column(name = "ATC93X")
    var atc93x: BigDecimal? = null

    @Column(name = "ATC933")
    var atc933: BigDecimal? = null

    @Column(name = "ATC934")
    var atc934: BigDecimal? = null

    @Column(name = "ATC935")
    var atc935: BigDecimal? = null

    @Column(name = "ATC936")
    var atc936: BigDecimal? = null

    @Column(name = "ATC93K")
    var atc93k: BigDecimal? = null

    @Column(name = "ATC93L")
    var atc93l: BigDecimal? = null

    @Column(name = "ATC93M")
    var atc93m: BigDecimal? = null

    @Column(name = "ATC93Y")
    var atc93y: BigDecimal? = null

    @Column(name = "ATC93AA")
    var atc93aa: BigDecimal? = null

    @Column(name = "ATC93AB")
    var atc93ab: BigDecimal? = null

    @Column(name = "ATC93D")
    var atc93d: BigDecimal? = null

    @Column(name = "ATC93AC")
    var atc93ac: BigDecimal? = null

    @Column(name = "ATC93AD")
    var atc93ad: BigDecimal? = null

    @Column(name = "ATC93C")
    var atc93c: BigDecimal? = null

    @Column(name = "ATC93G")
    var atc93g: String? = null

    @Column(name = "ATC93N")
    var atc93n: String? = null

    @Column(name = "ATC93P")
    var atc93p: String? = null

    @Column(name = "ATC93S")
    var atc93s: BigDecimal? = null

    @Column(name = "ATC93T")
    var atc93t: String? = null

    @Column(name = "ATC93U")
    var atc93u: String? = null

    @Column(name = "ATC93V")
    var atc93v: String? = null

    @Column(name = "ATC93W")
    var atc93w: String? = null

    @Column(name = "AAE114")
    var aae114: String? = null

    @Column(name = "AAE111")
    var aae111: String? = null

    @Column(name = "AAE117")
    var aae117: String? = null

    @Column(name = "AAE136")
    var aae136: LocalDate? = null

    @Column(name = "AAE119")
    var aae119: String? = null

    @Column(name = "AAE120")
    var aae120: String? = null

    @Column(name = "AAE122")
    var aae122: String? = null

    @Column(name = "AAE214")
    var aae214: String? = null

    @Column(name = "AAE211")
    var aae211: String? = null

    @Column(name = "AAE217")
    var aae217: String? = null

    @Column(name = "AAE236")
    var aae236: LocalDate? = null

    @Column(name = "AAE219")
    var aae219: String? = null

    @Column(name = "AAE220")
    var aae220: String? = null

    @Column(name = "AAE222")
    var aae222: String? = null

    @Column(name = "AAE314")
    var aae314: String? = null

    @Column(name = "AAE311")
    var aae311: String? = null

    @Column(name = "AAE317")
    var aae317: String? = null

    @Column(name = "AAE336")
    var aae336: LocalDate? = null

    @Column(name = "AAE319")
    var aae319: String? = null

    @Column(name = "AAE320")
    var aae320: String? = null

    @Column(name = "AAE322")
    var aae322: String? = null

    @Column(name = "ATC910")
    var atc910: Long? = null

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

    @Column(name = "AAE013")
    var aae013: String? = null

    @Column(name = "AAE002")
    var aae002: Long? = null

    @Column(name = "AAE001")
    var aae001: Long? = null

    @Column(name = "ATC900")
    var atc900: Long? = null

    @Column(name = "BAA000")
    var baa000: BigDecimal? = null

    @Column(name = "BAA001")
    var baa001: BigDecimal? = null

    @Column(name = "BAA002")
    var baa002: BigDecimal? = null

    @Column(name = "BAA003")
    var baa003: BigDecimal? = null

    @Column(name = "BBA001")
    var bba001: BigDecimal? = null

    @Column(name = "BBA002")
    var bba002: BigDecimal? = null

    @Column(name = "BBA003")
    var bba003: BigDecimal? = null

    @Column(name = "BBA004")
    var bba004: BigDecimal? = null

    @Column(name = "BBA005")
    var bba005: BigDecimal? = null

    @Column(name = "BBA000")
    var bba000: BigDecimal? = null

    @Column(name = "CAA001")
    var caa001: BigDecimal? = null

    @Column(name = "CAA002")
    var caa002: BigDecimal? = null

    @Column(name = "BBA200")
    var bba200: BigDecimal? = null

    @Column(name = "BAA200")
    var baa200: BigDecimal? = null

    @Column(name = "DAA001")
    var daa001: BigDecimal? = null

    @Column(name = "ATC93AF")
    var atc93af: BigDecimal? = null

    @Column(name = "BBA100")
    var bba100: BigDecimal? = null

    @Column(name = "BAA100")
    var baa100: BigDecimal? = null

    @Column(name = "CAA101")
    var caa101: BigDecimal? = null

    @Column(name = "CAA102")
    var caa102: BigDecimal? = null

    @Column(name = "BBA300")
    var bba300: BigDecimal? = null

    @Column(name = "BAA300")
    var baa300: BigDecimal? = null

    @Column(name = "BAA101")
    var baa101: BigDecimal? = null

    @Column(name = "BAA102")
    var baa102: BigDecimal? = null

    @Column(name = "BAA103")
    var baa103: BigDecimal? = null

    @Column(name = "ATC9P2")
    var atc9p2: BigDecimal? = null

    @Column(name = "ATC9P3")
    var atc9p3: BigDecimal? = null

    @Column(name = "AAE005")
    var aae005: String? = null

    @Column(name = "AAE00B")
    var aae00b: String? = null

    @Column(name = "ATB90Y")
    var atb90y: String? = null

    @Column(name = "ATC8G0")
    var atc8g0: Long? = null

    @Column(name = "AAE00A")
    var aae00a: String? = null

    @Column(name = "ATC904")
    var atc904: Long? = null

    @Column(name = "ATC93AG")
    var atc93ag: BigDecimal? = null

    @Column(name = "ATC301")
    var atc301: String? = null

    @Column(name = "ATC302")
    var atc302: String? = null

    @Column(name = "ATC303")
    var atc303: String? = null

    @Column(name = "ATC304")
    var atc304: String? = null

    @Column(name = "ATC305")
    var atc305: String? = null

    @Column(name = "ATC306")
    var atc306: String? = null

}
