package com.jlrc.rcpq.entity

import jakarta.persistence.*
import java.io.Serializable
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "TC90_BP")
open class TC90_BP {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_TC90_ID")
    @SequenceGenerator(name = "SEQ_TC90_ID", sequenceName = "SEQ_TC90_ID", allocationSize = 1)
    @Column(name = "ID")
    var id: Long? = null

    @Column(name = "ACE100")
    var ace100: Long? = null

    @Column(name = "ATB930")
    var atb930: Long? = null

    @Column(name = "ATB900")
    var atb900: Long? = null

    @Column(name = "AAB001")
    var aab001: Long? = null

    @Column(name = "AAB004")
    var aab004: String? = null

    @Column(name = "AAC001")
    var aac001: Long? = null

    @Column(name = "AAC003")
    var aac003: String? = null

    @Column(name = "AAC002")
    var aac002: String? = null

    @Column(name = "AAE00B")
    var aae00b: String? = null

    @Column(name = "AAB299")
    var aab299: String? = null

    @Column(name = "AAC009")
    var aac009: String? = null

    @Column(name = "AAC013")
    var aac013: String? = null

    @Column(name = "AAE00P")
    var aae00p: String? = null

    @Column(name = "AAE005")
    var aae005: String? = null

    @Column(name = "AAB004_1")
    var aab004_1: String? = null

    @Column(name = "ATC909")
    var atc909: String? = null

    @Column(name = "ATC90B")
    var atc90b: String? = null

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

    @Column(name = "ATC90AH")
    var atc90ah: String? = null

    @Column(name = "AAE002")
    var aae002: String? = null

    @Column(name = "ATB90C")
    var atb90c: String? = null

    @Column(name = "ATB90A")
    var atb90a: String? = null

    @Column(name = "ATB90B")
    var atb90b: String? = null

    @Column(name = "ATC90ZB")
    var atc90zb: String? = null

    @Column(name = "ATC90ZA")
    var atc90za: String? = null

    @Column(name = "AAA401")
    var aaa401: String? = null

    @Column(name = "AAA403")
    var aaa403: String? = null

    @Column(name = "AAA100")
    var aaa100: String? = null

    @Column(name = "AAA402")
    var aaa402: String? = null

    @Column(name = "AAA404")
    var aaa404: String? = null

    @Column(name = "AAA405")
    var aaa405: String? = null

    @Column(name = "AAA110")
    var aaa110: String? = null

    @Column(name = "AAA407")
    var aaa407: String? = null

    @Column(name = "AAA120")
    var aaa120: String? = null

    @Column(name = "ATB90S")
    var atb90s: String? = null

    @Column(name = "AAA301")
    var aaa301: String? = null

    @Column(name = "AAA304")
    var aaa304: String? = null

    @Column(name = "AAA302")
    var aaa302: String? = null

    @Column(name = "AAA305")
    var aaa305: String? = null

    @Column(name = "AAA306")
    var aaa306: String? = null

    @Column(name = "AAA303")
    var aaa303: String? = null

    @Column(name = "AAE013")
    var aae013: String? = null

    @Column(name = "ACE10A")
    var ace10a: String? = null

    @Column(name = "ACE10B")
    var ace10b: String? = null

    @Column(name = "AAE011")
    var aae011: String? = null

    @Column(name = "AAE017")
    var aae017: String? = null

    @Column(name = "AAE036")
    var aae036: String? = null

    @Column(name = "AAE019")
    var aae019: String? = null

    @Column(name = "AAE020")
    var aae020: String? = null

    @Column(name = "AAE022")
    var aae022: String? = null

    @Column(name = "AAC005")
    var aac005: String? = null

    @Column(name = "AAC011")
    var aac011: String? = null

    @Column(name = "ATC902")
    var atc902: String? = null

    @Column(name = "AAC012")
    var aac012: String? = null

    @Column(name = "AAA605")
    var aaa605: String? = null

    @Column(name = "AAA203")
    var aaa203: String? = null

    @Column(name = "AAA503")
    var aaa503: String? = null

    @Column(name = "AAA603")
    var aaa603: String? = null

    @Column(name = "ATC90AX")
    var atc90ax: String? = null

    @Column(name = "ATC905")
    var atc905: String? = null

    @Column(name = "AAE00A")
    var aae00a: String? = null

    @Column(name = "ATC90P")
    var atc90p: String? = null

    @Column(name = "ATC90W")
    var atc90w: String? = null

    @Column(name = "ATC90PA")
    var atc90pa: String? = null

    @Column(name = "BBB001")
    var bbb001: String? = null

    @Column(name = "BBB002")
    var bbb002: String? = null

    @Column(name = "ATC051")
    var atc051: String? = null

    @Column(name = "ATC052")
    var atc052: String? = null

    @Column(name = "ATC053")
    var atc053: String? = null

    @Column(name = "ATC900_BGQ")
    var atc900_bgq: Long? = null

    @Column(name = "ATC9WC")
    var atc9wc: String? = null

    @Column(name = "ATC90R")
    var atc90r: String? = null

    @Column(name = "ABP001")
    var abp001: String? = null

}
