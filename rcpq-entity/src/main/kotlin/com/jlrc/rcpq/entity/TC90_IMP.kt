package com.jlrc.rcpq.entity

import jakarta.persistence.*
import java.io.Serializable
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "TC90_IMP")
open class TC90_IMP {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_TC90_IMP_ID")
    @SequenceGenerator(name = "SEQ_TC90_IMP_ID", sequenceName = "SEQ_TC90_IMP_ID", allocationSize = 1)
    @Column(name = "ID")
    var id: Long? = null

    @Column(name = "ACE100")
    var ace100: Long? = null

    @Column(name = "ACE10A")
    var ace10a: String? = null

    @Column(name = "ACE10B")
    var ace10b: String? = null

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

    @Column(name = "ATB930")
    var atb930: Long? = null

    @Column(name = "AAB001_1")
    var aab001_1: Long? = null

    @Column(name = "AAC002")
    var aac002: String? = null

    @Column(name = "AAC003")
    var aac003: String? = null

    @Column(name = "AAC009")
    var aac009: String? = null

    @Column(name = "AAE008")
    var aae008: String? = null

    @Column(name = "AAB299")
    var aab299: String? = null

    @Column(name = "AAE006")
    var aae006: String? = null

    @Column(name = "AAE005")
    var aae005: String? = null

    @Column(name = "AAE00P")
    var aae00p: String? = null

    @Column(name = "AAC013")
    var aac013: String? = null

    @Column(name = "AAE002")
    var aae002: String? = null

    @Column(name = "ATC909")
    var atc909: String? = null

    @Column(name = "ATC90A")
    var atc90a: String? = null

    @Column(name = "ATC90C")
    var atc90c: String? = null

    @Column(name = "ATC90D")
    var atc90d: String? = null

    @Column(name = "ATC907")
    var atc907: String? = null

    @Column(name = "ATC908")
    var atc908: String? = null

    @Column(name = "ATC90AA")
    var atc90aa: String? = null

    @Column(name = "ATC90AB")
    var atc90ab: String? = null

    @Column(name = "ATC90AC")
    var atc90ac: String? = null

    @Column(name = "ATC90AD")
    var atc90ad: String? = null

    @Column(name = "ATC90AE")
    var atc90ae: String? = null

    @Column(name = "ATC90AH")
    var atc90ah: String? = null

    @Column(name = "ATC90AJ")
    var atc90aj: String? = null

    @Column(name = "ATC90AK")
    var atc90ak: String? = null

    @Column(name = "ATC90AL")
    var atc90al: String? = null

    @Column(name = "ATC90AM")
    var atc90am: String? = null

    @Column(name = "ATC90AN")
    var atc90an: String? = null

    @Column(name = "ATC90G")
    var atc90g: String? = null

    @Column(name = "ATC90H")
    var atc90h: String? = null

    @Column(name = "AAA301")
    var aaa301: String? = null

    @Column(name = "ATC904")
    var atc904: String? = null

    @Column(name = "ATC9WC")
    var atc9wc: String? = null

    @Column(name = "AAA407")
    var aaa407: String? = null

    @Column(name = "AAA303")
    var aaa303: String? = null

    @Column(name = "AAA406")
    var aaa406: String? = null

    @Column(name = "BBB001")
    var bbb001: String? = null

    @Column(name = "AAC005")
    var aac005: String? = null

    @Column(name = "AAC024")
    var aac024: String? = null

    @Column(name = "AAC011")
    var aac011: String? = null

    @Column(name = "AAC017")
    var aac017: String? = null

    @Column(name = "AAC181")
    var aac181: String? = null

    @Column(name = "AAC180")
    var aac180: String? = null

    @Column(name = "AAC182")
    var aac182: String? = null

    @Column(name = "AAE00A")
    var aae00a: String? = null

    @Column(name = "AAE00D")
    var aae00d: String? = null

    @Column(name = "ATB900")
    var atb900: Long? = null

    @Column(name = "AAB001")
    var aab001: Long? = null

    @Column(name = "ID_")
    var id_: String? = null

    @Column(name = "AAB004")
    var aab004: String? = null

    @Column(name = "ID_1")
    var id_1: String? = null

    @Column(name = "AAB004_1")
    var aab004_1: String? = null

    @Column(name = "BAB305")
    var bab305: String? = null

    @Column(name = "AAC001")
    var aac001: Long? = null

    @Column(name = "ATC90X")
    var atc90x: String? = null

    @Column(name = "AAC004")
    var aac004: String? = null

    @Column(name = "AAC006")
    var aac006: LocalDate? = null

    @Column(name = "ATC90C_")
    var atc90c_: LocalDate? = null

    @Column(name = "ATC90D_")
    var atc90d_: LocalDate? = null

    @Column(name = "ATC90AB_")
    var atc90ab_: LocalDate? = null

    @Column(name = "ATC90AC_")
    var atc90ac_: LocalDate? = null

    @Column(name = "ATC90AE_")
    var atc90ae_: LocalDate? = null

    @Column(name = "ATC90AK_")
    var atc90ak_: LocalDate? = null

    @Column(name = "ATC90AL_")
    var atc90al_: LocalDate? = null

    @Column(name = "ATC90AA_")
    var atc90aa_: String? = null

    @Column(name = "ATC90AD_")
    var atc90ad_: BigDecimal? = null

    @Column(name = "ATC90AH_")
    var atc90ah_: String? = null

    @Column(name = "ATC90AJ_")
    var atc90aj_: String? = null

    @Column(name = "ATC90AM_")
    var atc90am_: String? = null

    @Column(name = "ATC90AN_")
    var atc90an_: BigDecimal? = null

    @Column(name = "AAC009_")
    var aac009_: String? = null

    @Column(name = "AAE00P_")
    var aae00p_: String? = null

    @Column(name = "AAC013_")
    var aac013_: String? = null

    @Column(name = "ATC909_")
    var atc909_: String? = null

    @Column(name = "ATC90A_")
    var atc90a_: String? = null

    @Column(name = "ATC90G_")
    var atc90g_: String? = null

    @Column(name = "ATC907_")
    var atc907_: String? = null

    @Column(name = "ATC90H_")
    var atc90h_: String? = null

    @Column(name = "AAA301_")
    var aaa301_: BigDecimal? = null

    @Column(name = "ATC9WC_")
    var atc9wc_: String? = null

    @Column(name = "AAA407_")
    var aaa407_: String? = null

    @Column(name = "AAA303_")
    var aaa303_: BigDecimal? = null

    @Column(name = "AAA406_")
    var aaa406_: String? = null

    @Column(name = "ATC900")
    var atc900: Long? = null

    @Column(name = "AAC005_")
    var aac005_: String? = null

    @Column(name = "AAC024_")
    var aac024_: String? = null

    @Column(name = "AAC011_")
    var aac011_: String? = null

    @Column(name = "AAC017_")
    var aac017_: String? = null

    @Column(name = "AAC181_")
    var aac181_: LocalDate? = null

    @Column(name = "AAC180_")
    var aac180_: String? = null

    @Column(name = "AAC182_")
    var aac182_: String? = null

    @Column(name = "AAE00A_")
    var aae00a_: String? = null

    @Column(name = "AAE00D_")
    var aae00d_: String? = null

}
