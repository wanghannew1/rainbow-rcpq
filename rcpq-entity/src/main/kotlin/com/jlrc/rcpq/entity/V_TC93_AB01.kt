package com.jlrc.rcpq.entity

import jakarta.persistence.*
import java.io.Serializable
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "V_TC93_AB01")
open class V_TC93_AB01 {
    @Id
    @Column(name = "ATC930")
    var atc930: Long? = null

    @Column(name = "QFJE")
    var qfje: Long? = null

    @Column(name = "BAZ001")
    var baz001: Long? = null

    @Column(name = "BAZ002")
    var baz002: Long? = null

    @Column(name = "ATB900")
    var atb900: Long? = null

    @Column(name = "ATC900")
    var atc900: Long? = null

    @Column(name = "AAB001")
    var aab001: Long? = null

    @Column(name = "AAB004")
    var aab004: String? = null

    @Column(name = "ATB930")
    var atb930: Long? = null

    @Column(name = "ATB931")
    var atb931: String? = null

    @Column(name = "ATC931")
    var atc931: Long? = null

    @Column(name = "ATC932")
    var atc932: Long? = null

    @Column(name = "ATC937")
    var atc937: String? = null

    @Column(name = "AAC001")
    var aac001: Long? = null

    @Column(name = "AAC003")
    var aac003: String? = null

    @Column(name = "AAC002")
    var aac002: String? = null

    @Column(name = "AAE005")
    var aae005: String? = null

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

    @Column(name = "ATC938")
    var atc938: String? = null

    @Column(name = "ATC939")
    var atc939: String? = null

    @Column(name = "ATC93A")
    var atc93a: String? = null

    @Column(name = "ATC93B")
    var atc93b: String? = null

    @Column(name = "ATC93H")
    var atc93h: String? = null

    @Column(name = "ATC93I")
    var atc93i: String? = null

    @Column(name = "ATC93J")
    var atc93j: String? = null

    @Column(name = "ATC93Z")
    var atc93z: String? = null

    @Column(name = "ATC93AA")
    var atc93aa: BigDecimal? = null

    @Column(name = "ATC93AB")
    var atc93ab: BigDecimal? = null

    @Column(name = "ATC93D")
    var atc93d: BigDecimal? = null

    @Column(name = "ATC93AC")
    var atc93ac: BigDecimal? = null

    @Column(name = "ATC93C")
    var atc93c: BigDecimal? = null

    @Column(name = "ATC93E")
    var atc93e: BigDecimal? = null

    @Column(name = "ATC93G")
    var atc93g: String? = null

    @Column(name = "ATC93N")
    var atc93n: String? = null

    @Column(name = "ATC93P")
    var atc93p: String? = null

    @Column(name = "ATC93R")
    var atc93r: BigDecimal? = null

    @Column(name = "ATC93S")
    var atc93s: BigDecimal? = null

    @Column(name = "ATC93T")
    var atc93t: String? = null

    @Column(name = "ATC93U")
    var atc93u: String? = null

    @Column(name = "ATC9P2")
    var atc9p2: BigDecimal? = null

    @Column(name = "ATC9P3")
    var atc9p3: BigDecimal? = null

    @Column(name = "KEYS")
    var keys: String? = null

    @Column(name = "KEYS_1")
    var keys_1: String? = null

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

    @Column(name = "AAE022")
    var aae022: String? = null

    @Column(name = "ATC93AD")
    var atc93ad: BigDecimal? = null

    @Column(name = "KEYS_2")
    var keys_2: String? = null

    @Column(name = "ATC93AE")
    var atc93ae: String? = null

    @Column(name = "BBA200")
    var bba200: BigDecimal? = null

    @Column(name = "BAA200")
    var baa200: BigDecimal? = null

    @Column(name = "BBA300")
    var bba300: BigDecimal? = null

    @Column(name = "BAA300")
    var baa300: BigDecimal? = null

    @Column(name = "AAE013")
    var aae013: String? = null

    @Column(name = "AAE002")
    var aae002: Long? = null

    @Column(name = "AAE001")
    var aae001: Long? = null

    @Column(name = "ATC93AG")
    var atc93ag: BigDecimal? = null

    @Column(name = "AAA301")
    var aaa301: BigDecimal? = null

    @Column(name = "AAA302")
    var aaa302: BigDecimal? = null

    @Column(name = "AAA303")
    var aaa303: BigDecimal? = null

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

    @Column(name = "CAA001")
    var caa001: BigDecimal? = null

    @Column(name = "ATB90T")
    var atb90t: BigDecimal? = null

    @Column(name = "ATC93BA")
    var atc93ba: BigDecimal? = null

    @Column(name = "ATC93BB")
    var atc93bb: BigDecimal? = null

    @Column(name = "ATC93BC")
    var atc93bc: BigDecimal? = null

    @Column(name = "BAA001")
    var baa001: BigDecimal? = null

    @Column(name = "BAA002")
    var baa002: BigDecimal? = null

    @Column(name = "BAA003")
    var baa003: BigDecimal? = null

    @Column(name = "CAA002")
    var caa002: BigDecimal? = null

    @Column(name = "ATC93BD")
    var atc93bd: BigDecimal? = null

    @Column(name = "ATC93BE")
    var atc93be: BigDecimal? = null

    @Column(name = "ATC93BF")
    var atc93bf: BigDecimal? = null

    @Column(name = "ATC93BG")
    var atc93bg: BigDecimal? = null

    @Column(name = "ATC301")
    var atc301: BigDecimal? = null

    @Column(name = "ATC302")
    var atc302: BigDecimal? = null

    @Column(name = "ATC303")
    var atc303: BigDecimal? = null

    @Column(name = "ATC304")
    var atc304: BigDecimal? = null

    @Column(name = "ATC305")
    var atc305: BigDecimal? = null

    @Column(name = "ATC306")
    var atc306: BigDecimal? = null

    @Column(name = "ATC93Y1")
    var atc93y1: BigDecimal? = null

    @Column(name = "ATC93Y2")
    var atc93y2: BigDecimal? = null

    @Column(name = "ATC93Y3")
    var atc93y3: BigDecimal? = null

    @Column(name = "ATC93YZ")
    var atc93yz: BigDecimal? = null

    @Column(name = "ATC93Z1")
    var atc93z1: BigDecimal? = null

    @Column(name = "ATC93Z2")
    var atc93z2: BigDecimal? = null

    @Column(name = "ATC93Z3")
    var atc93z3: BigDecimal? = null

    @Column(name = "AAE00B")
    var aae00b: String? = null

    @Column(name = "ATC93Z4")
    var atc93z4: String? = null

    @Column(name = "ATC93X1")
    var atc93x1: BigDecimal? = null

    @Column(name = "ATC93X2")
    var atc93x2: String? = null

    @Column(name = "ATC93X3")
    var atc93x3: BigDecimal? = null

    @Column(name = "ATC93X4")
    var atc93x4: String? = null

    @Column(name = "ATC93X5")
    var atc93x5: BigDecimal? = null

    @Column(name = "ATC93X6")
    var atc93x6: String? = null

    @Column(name = "AAB004_1")
    var aab004_1: String? = null

    @Column(name = "ATC908")
    var atc908: String? = null

    @Column(name = "ATC93W1")
    var atc93w1: BigDecimal? = null

    @Column(name = "ATC93W2")
    var atc93w2: BigDecimal? = null

    @Column(name = "ATC93W3")
    var atc93w3: BigDecimal? = null

    @Column(name = "ATC93W4")
    var atc93w4: BigDecimal? = null

    @Column(name = "ATC93W5")
    var atc93w5: BigDecimal? = null

    @Column(name = "ATC93W6")
    var atc93w6: BigDecimal? = null

    @Column(name = "ATC93W7")
    var atc93w7: BigDecimal? = null

    @Column(name = "ATC93W8")
    var atc93w8: BigDecimal? = null

    @Column(name = "ATC93W9")
    var atc93w9: BigDecimal? = null

    @Column(name = "ATC93W10")
    var atc93w10: BigDecimal? = null

    @Column(name = "ATC93W11")
    var atc93w11: BigDecimal? = null

    @Column(name = "ATC93W12")
    var atc93w12: BigDecimal? = null

    @Column(name = "ATC93W13")
    var atc93w13: BigDecimal? = null

    @Column(name = "ATC93W14")
    var atc93w14: BigDecimal? = null

    @Column(name = "ATC93W15")
    var atc93w15: BigDecimal? = null

    @Column(name = "ATC93W16")
    var atc93w16: BigDecimal? = null

    @Column(name = "ATC93W17")
    var atc93w17: BigDecimal? = null

    @Column(name = "ATC93W18")
    var atc93w18: BigDecimal? = null

    @Column(name = "ATC93W19")
    var atc93w19: BigDecimal? = null

    @Column(name = "ATC93W20")
    var atc93w20: BigDecimal? = null

    @Column(name = "ATC93W21")
    var atc93w21: BigDecimal? = null

    @Column(name = "ATC93W22")
    var atc93w22: BigDecimal? = null

    @Column(name = "ATC93W23")
    var atc93w23: BigDecimal? = null

    @Column(name = "ATC93W24")
    var atc93w24: BigDecimal? = null

    @Column(name = "ATC93W25")
    var atc93w25: BigDecimal? = null

    @Column(name = "ATC93W26")
    var atc93w26: BigDecimal? = null

    @Column(name = "ATC93W27")
    var atc93w27: BigDecimal? = null

    @Column(name = "ATC93W28")
    var atc93w28: BigDecimal? = null

    @Column(name = "ATC93BH")
    var atc93bh: BigDecimal? = null

    @Column(name = "ATC93W29")
    var atc93w29: BigDecimal? = null

    @Column(name = "ATC93W30")
    var atc93w30: BigDecimal? = null

    @Column(name = "ATC93W31")
    var atc93w31: BigDecimal? = null

    @Column(name = "ATC93W32")
    var atc93w32: BigDecimal? = null

    @Column(name = "ATC93W33")
    var atc93w33: BigDecimal? = null

    @Column(name = "ATC93W34")
    var atc93w34: BigDecimal? = null

    @Column(name = "ATC93AR")
    var atc93ar: BigDecimal? = null

    @Column(name = "ZZZ011")
    var zzz011: String? = null

    @Column(name = "AAA030")
    var aaa030: Long? = null

    @Column(name = "AAA031")
    var aaa031: String? = null

    @Column(name = "QQJE")
    var qqje: Long? = null

}
