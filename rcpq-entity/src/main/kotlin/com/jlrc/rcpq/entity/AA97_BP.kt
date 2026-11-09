package com.jlrc.rcpq.entity

import jakarta.persistence.*
import java.io.Serializable
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "AA97_BP")
open class AA97_BP {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_AA97_ID")
    @SequenceGenerator(name = "SEQ_AA97_ID", sequenceName = "SEQ_AA97_ID", allocationSize = 1)
    @Column(name = "ID")
    var id: Long? = null

    @Column(name = "ACE100")
    var ace100: Long? = null

    @Column(name = "ATC900")
    var atc900: Long? = null

    @Column(name = "ATB930")
    var atb930: Long? = null

    @Column(name = "ATB931")
    var atb931: String? = null

    @Column(name = "ATB900")
    var atb900: Long? = null

    @Column(name = "AAB001")
    var aab001: Long? = null

    @Column(name = "AAA030")
    var aaa030: Long? = null

    @Column(name = "AAA031")
    var aaa031: String? = null

    @Column(name = "AAC001")
    var aac001: Long? = null

    @Column(name = "AAC003")
    var aac003: String? = null

    @Column(name = "AAC002")
    var aac002: String? = null

    @Column(name = "AAB004")
    var aab004: String? = null

    @Column(name = "AAA100")
    var aaa100: String? = null

    @Column(name = "AAA101")
    var aaa101: String? = null

    @Column(name = "AAA001")
    var aaa001: String? = null

    @Column(name = "AAE001")
    var aae001: Long? = null

    @Column(name = "AAA004")
    var aaa004: String? = null

    @Column(name = "AAA005")
    var aaa005: String? = null

    @Column(name = "AAA300")
    var aaa300: String? = null

    @Column(name = "AAA301")
    var aaa301: String? = null

    @Column(name = "AAA302")
    var aaa302: String? = null

    @Column(name = "AAA303")
    var aaa303: String? = null

    @Column(name = "AAA110")
    var aaa110: String? = null

    @Column(name = "AAA111")
    var aaa111: String? = null

    @Column(name = "AAA120")
    var aaa120: String? = null

    @Column(name = "AAA121")
    var aaa121: String? = null

    @Column(name = "AAA301_1")
    var aaa301_1: String? = null

    @Column(name = "AAA302_1")
    var aaa302_1: String? = null

    @Column(name = "AAA302_2")
    var aaa302_2: String? = null

    @Column(name = "AAA303_1")
    var aaa303_1: String? = null

    @Column(name = "AAA303_2")
    var aaa303_2: String? = null

    @Column(name = "ATB90S_DW")
    var atb90s_dw: String? = null

    @Column(name = "ATB90S_GR")
    var atb90s_gr: String? = null

    @Column(name = "AAB001_1")
    var aab001_1: String? = null

    @Column(name = "AAB001_2")
    var aab001_2: String? = null

    @Column(name = "AAB001_3")
    var aab001_3: String? = null

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

    @Column(name = "AAA009")
    var aaa009: String? = null

    @Column(name = "AAA305")
    var aaa305: String? = null

    @Column(name = "AAA305_1")
    var aaa305_1: String? = null

    @Column(name = "AAA130")
    var aaa130: String? = null

    @Column(name = "AAA131")
    var aaa131: String? = null

    @Column(name = "AAA305_2")
    var aaa305_2: String? = null

}
