package com.jlrc.rcpq.entity

import jakarta.persistence.*
import java.io.Serializable
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "TC92")
open class TC92 {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_TC92_ATC920")
    @SequenceGenerator(name = "SEQ_TC92_ATC920", sequenceName = "SEQ_TC92_ATC920", allocationSize = 1)
    @Column(name = "ATC920")
    var atc920: Long? = null

    @Column(name = "BAZ001")
    var baz001: Long? = null

    @Column(name = "BAZ002")
    var baz002: Long? = null

    @Column(name = "ATC900")
    var atc900: Long? = null

    @Column(name = "ATC921")
    var atc921: LocalDate? = null

    @Column(name = "AAC009")
    var aac009: String? = null

    @Column(name = "AAC009_")
    var aac009_: String? = null

    @Column(name = "ATC909")
    var atc909: String? = null

    @Column(name = "ATC909_")
    var atc909_: String? = null

    @Column(name = "ATC90A")
    var atc90a: String? = null

    @Column(name = "ATC90A_")
    var atc90a_: String? = null

    @Column(name = "ATC90B")
    var atc90b: LocalDate? = null

    @Column(name = "ATC90B_")
    var atc90b_: LocalDate? = null

    @Column(name = "ATC90C")
    var atc90c: LocalDate? = null

    @Column(name = "ATC90C_")
    var atc90c_: LocalDate? = null

    @Column(name = "ATC90D")
    var atc90d: LocalDate? = null

    @Column(name = "ATC90D_")
    var atc90d_: LocalDate? = null

    @Column(name = "ATC90G")
    var atc90g: String? = null

    @Column(name = "ATC90G_")
    var atc90g_: String? = null

    @Column(name = "ATC90H")
    var atc90h: String? = null

    @Column(name = "ATC90H_")
    var atc90h_: String? = null

    @Column(name = "AAA001")
    var aaa001: String? = null

    @Column(name = "AAA001_")
    var aaa001_: String? = null

    @Column(name = "AAA002")
    var aaa002: String? = null

    @Column(name = "AAA002_")
    var aaa002_: String? = null

    @Column(name = "AAA003")
    var aaa003: String? = null

    @Column(name = "AAA003_")
    var aaa003_: String? = null

    @Column(name = "AAA004")
    var aaa004: String? = null

    @Column(name = "AAA004_")
    var aaa004_: String? = null

    @Column(name = "AAA100")
    var aaa100: String? = null

    @Column(name = "AAA101")
    var aaa101: String? = null

    @Column(name = "AAA100_")
    var aaa100_: String? = null

    @Column(name = "AAA101_")
    var aaa101_: String? = null

    @Column(name = "AAA301")
    var aaa301: BigDecimal? = null

    @Column(name = "AAA301_")
    var aaa301_: BigDecimal? = null

    @Column(name = "AAA302")
    var aaa302: BigDecimal? = null

    @Column(name = "AAA302_")
    var aaa302_: BigDecimal? = null

    @Column(name = "AAA303")
    var aaa303: BigDecimal? = null

    @Column(name = "AAA303_")
    var aaa303_: BigDecimal? = null

    @Column(name = "AAA406")
    var aaa406: String? = null

    @Column(name = "AAA406_")
    var aaa406_: String? = null

    @Column(name = "BBB001")
    var bbb001: String? = null

    @Column(name = "BBB001_")
    var bbb001_: String? = null

    @Column(name = "BBB002")
    var bbb002: String? = null

    @Column(name = "BBB002_")
    var bbb002_: String? = null

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

    @Column(name = "ATB910")
    var atb910: Long? = null

    @Column(name = "ATC90R")
    var atc90r: String? = null

    @Column(name = "ATC90R_")
    var atc90r_: String? = null

    @Column(name = "ATC90U")
    var atc90u: String? = null

    @Column(name = "ATC90U_")
    var atc90u_: String? = null

    @Column(name = "AAE005")
    var aae005: String? = null

    @Column(name = "AAE005_")
    var aae005_: String? = null

    @Column(name = "ATC902")
    var atc902: String? = null

    @Column(name = "ATC902_")
    var atc902_: String? = null

    @Column(name = "AAA407")
    var aaa407: String? = null

    @Column(name = "AAA407_")
    var aaa407_: String? = null

    @Column(name = "ATB911")
    var atb911: String? = null

    @Column(name = "AAC002")
    var aac002: String? = null

    @Column(name = "AAC002_")
    var aac002_: String? = null

    @Column(name = "AAC003")
    var aac003: String? = null

    @Column(name = "AAC003_")
    var aac003_: String? = null

    @Column(name = "AAC004")
    var aac004: String? = null

    @Column(name = "AAC004_")
    var aac004_: String? = null

    @Column(name = "AAC005")
    var aac005: String? = null

    @Column(name = "AAC005_")
    var aac005_: String? = null

    @Column(name = "AAC006")
    var aac006: LocalDate? = null

    @Column(name = "AAC006_")
    var aac006_: LocalDate? = null

    @Column(name = "AAC001")
    var aac001: Long? = null

    @Column(name = "AAA304")
    var aaa304: BigDecimal? = null

    @Column(name = "AAA304_")
    var aaa304_: BigDecimal? = null

    @Column(name = "AAA305")
    var aaa305: BigDecimal? = null

    @Column(name = "AAA305_")
    var aaa305_: BigDecimal? = null

    @Column(name = "AAA306")
    var aaa306: BigDecimal? = null

    @Column(name = "AAA306_")
    var aaa306_: BigDecimal? = null

    @Column(name = "ATB90S")
    var atb90s: String? = null

    @Column(name = "ATB90S_")
    var atb90s_: String? = null

    @Column(name = "ATC90AA")
    var atc90aa: String? = null

    @Column(name = "ATC90AA_")
    var atc90aa_: String? = null

    @Column(name = "ATC90AB")
    var atc90ab: LocalDate? = null

    @Column(name = "ATC90AB_")
    var atc90ab_: LocalDate? = null

    @Column(name = "ATC90AC")
    var atc90ac: LocalDate? = null

    @Column(name = "ATC90AC_")
    var atc90ac_: LocalDate? = null

    @Column(name = "ATC90AD")
    var atc90ad: BigDecimal? = null

    @Column(name = "ATC90AD_")
    var atc90ad_: BigDecimal? = null

    @Column(name = "ATC90AE")
    var atc90ae: LocalDate? = null

    @Column(name = "ATC90AE_")
    var atc90ae_: LocalDate? = null

    @Column(name = "ATC908")
    var atc908: String? = null

    @Column(name = "ATC908_")
    var atc908_: String? = null

    @Column(name = "ATC907")
    var atc907: String? = null

    @Column(name = "ATC907_")
    var atc907_: String? = null

    @Column(name = "ATB90AB")
    var atb90ab: Long? = null

    @Column(name = "ATB90AB_")
    var atb90ab_: Long? = null

    @Column(name = "ATC905")
    var atc905: BigDecimal? = null

    @Column(name = "ATC905_")
    var atc905_: BigDecimal? = null

    @Column(name = "ATC90AH")
    var atc90ah: String? = null

    @Column(name = "ATC90AH_")
    var atc90ah_: String? = null

    @Column(name = "ATC90AJ")
    var atc90aj: String? = null

    @Column(name = "ATC90AJ_")
    var atc90aj_: String? = null

    @Column(name = "ATC90AK")
    var atc90ak: LocalDate? = null

    @Column(name = "ATC90AK_")
    var atc90ak_: LocalDate? = null

    @Column(name = "ATC90AL")
    var atc90al: LocalDate? = null

    @Column(name = "ATC90AL_")
    var atc90al_: LocalDate? = null

    @Column(name = "ATC90AM")
    var atc90am: String? = null

    @Column(name = "ATC90AM_")
    var atc90am_: String? = null

    @Column(name = "ATC90AN")
    var atc90an: BigDecimal? = null

    @Column(name = "ATC90AN_")
    var atc90an_: BigDecimal? = null

    @Column(name = "ATB90AD")
    var atb90ad: String? = null

    @Column(name = "ATB90AD_")
    var atb90ad_: String? = null

    @Column(name = "AAA401")
    var aaa401: String? = null

    @Column(name = "AAA401_")
    var aaa401_: String? = null

    @Column(name = "AAA402")
    var aaa402: String? = null

    @Column(name = "AAA402_")
    var aaa402_: String? = null

    @Column(name = "AAA403")
    var aaa403: String? = null

    @Column(name = "AAA403_")
    var aaa403_: String? = null

    @Column(name = "AAA404")
    var aaa404: String? = null

    @Column(name = "AAA404_")
    var aaa404_: String? = null

    @Column(name = "AAA405")
    var aaa405: String? = null

    @Column(name = "AAA405_")
    var aaa405_: String? = null

    @Column(name = "ATC904")
    var atc904: Long? = null

    @Column(name = "ATC904_")
    var atc904_: Long? = null

    @Column(name = "AAB001_1")
    var aab001_1: Long? = null

    @Column(name = "ID_1")
    var id_1: String? = null

    @Column(name = "AAB004_1")
    var aab004_1: String? = null

    @Column(name = "AAB001_1_")
    var aab001_1_: Long? = null

    @Column(name = "ID_1_")
    var id_1_: String? = null

    @Column(name = "AAB004_1_")
    var aab004_1_: String? = null

    @Column(name = "AAE013")
    var aae013: String? = null

    @Column(name = "AAE013_")
    var aae013_: String? = null

    @Column(name = "AAA110")
    var aaa110: String? = null

    @Column(name = "AAA111")
    var aaa111: String? = null

    @Column(name = "AAA110_")
    var aaa110_: String? = null

    @Column(name = "AAA111_")
    var aaa111_: String? = null

    @Column(name = "AAA120")
    var aaa120: String? = null

    @Column(name = "AAA121")
    var aaa121: String? = null

    @Column(name = "AAA120_")
    var aaa120_: String? = null

    @Column(name = "AAA121_")
    var aaa121_: String? = null

    @Column(name = "AAC012")
    var aac012: String? = null

    @Column(name = "AAC012_")
    var aac012_: String? = null

    @Column(name = "AAA201")
    var aaa201: Long? = null

    @Column(name = "AAA201_")
    var aaa201_: Long? = null

    @Column(name = "AAA501")
    var aaa501: Long? = null

    @Column(name = "AAA501_")
    var aaa501_: Long? = null

    @Column(name = "AAA601")
    var aaa601: Long? = null

    @Column(name = "AAA601_")
    var aaa601_: Long? = null

    @Column(name = "ATB90A")
    var atb90a: BigDecimal? = null

    @Column(name = "ATB90A_")
    var atb90a_: BigDecimal? = null

    @Column(name = "ATB90B")
    var atb90b: BigDecimal? = null

    @Column(name = "ATB90B_")
    var atb90b_: BigDecimal? = null

    @Column(name = "AAE006")
    var aae006: String? = null

    @Column(name = "AAE006_")
    var aae006_: String? = null

    @Column(name = "ATB90C")
    var atb90c: String? = null

    @Column(name = "ATB90C_")
    var atb90c_: String? = null

    @Column(name = "AAE00P")
    var aae00p: String? = null

    @Column(name = "AAE00P_")
    var aae00p_: String? = null

    @Column(name = "AAA803")
    var aaa803: String? = null

    @Column(name = "AAA803_")
    var aaa803_: String? = null

    @Column(name = "AAE00B")
    var aae00b: String? = null

    @Column(name = "AAE00B_")
    var aae00b_: String? = null

    @Column(name = "AAA005")
    var aaa005: String? = null

    @Column(name = "AAA005_")
    var aaa005_: String? = null

    @Column(name = "ATC90AV")
    var atc90av: Long? = null

    @Column(name = "ATC90AV_")
    var atc90av_: Long? = null

    @Column(name = "AAA202")
    var aaa202: Long? = null

    @Column(name = "AAA202_")
    var aaa202_: Long? = null

    @Column(name = "AAA502")
    var aaa502: Long? = null

    @Column(name = "AAA502_")
    var aaa502_: Long? = null

    @Column(name = "AAA602")
    var aaa602: Long? = null

    @Column(name = "AAA602_")
    var aaa602_: Long? = null

    @Column(name = "AAA130")
    var aaa130: String? = null

    @Column(name = "AAA131")
    var aaa131: String? = null

    @Column(name = "AAA130_")
    var aaa130_: String? = null

    @Column(name = "AAA131_")
    var aaa131_: String? = null

    @Column(name = "AAA901")
    var aaa901: Long? = null

    @Column(name = "AAA901_")
    var aaa901_: Long? = null

    @Column(name = "AAA902")
    var aaa902: Long? = null

    @Column(name = "AAA902_")
    var aaa902_: Long? = null

    @Column(name = "ATC90P")
    var atc90p: String? = null

    @Column(name = "ATC90P_")
    var atc90p_: String? = null

    @Column(name = "AAA203")
    var aaa203: Long? = null

    @Column(name = "AAA203_")
    var aaa203_: Long? = null

    @Column(name = "AAA208")
    var aaa208: Long? = null

    @Column(name = "AAA208_")
    var aaa208_: Long? = null

    @Column(name = "ATC90PA")
    var atc90pa: String? = null

    @Column(name = "ATC90PA_")
    var atc90pa_: String? = null

    @Column(name = "AAA603")
    var aaa603: Long? = null

    @Column(name = "AAA603_")
    var aaa603_: Long? = null

    @Column(name = "AAA608")
    var aaa608: Long? = null

    @Column(name = "AAA608_")
    var aaa608_: Long? = null

    @Column(name = "ATC90W")
    var atc90w: String? = null

    @Column(name = "ATC90W_")
    var atc90w_: String? = null

    @Column(name = "AAA503")
    var aaa503: Long? = null

    @Column(name = "AAA503_")
    var aaa503_: Long? = null

    @Column(name = "AAA508")
    var aaa508: Long? = null

    @Column(name = "AAA508_")
    var aaa508_: Long? = null

    @Column(name = "ATC90PB")
    var atc90pb: String? = null

    @Column(name = "ATC90PB_")
    var atc90pb_: String? = null

    @Column(name = "AAA903")
    var aaa903: Long? = null

    @Column(name = "AAA903_")
    var aaa903_: Long? = null

    @Column(name = "AAA908")
    var aaa908: Long? = null

    @Column(name = "AAA908_")
    var aaa908_: Long? = null

}
