package com.jlrc.rcpq.entity

import jakarta.persistence.*
import java.io.Serializable
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "AA97")
open class AA97 {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_AA97_AAA970")
    @SequenceGenerator(name = "SEQ_AA97_AAA970", sequenceName = "SEQ_AA97_AAA970", allocationSize = 1)
    @Column(name = "AAA970")
    var aaa970: Long? = null

    @Column(name = "BAZ001")
    var baz001: Long? = null

    @Column(name = "BAZ002")
    var baz002: Long? = null

    @Column(name = "ATB930")
    var atb930: Long? = null

    @Column(name = "ATB931")
    var atb931: String? = null

    @Column(name = "AAB001")
    var aab001: Long? = null

    @Column(name = "AAB004")
    var aab004: String? = null

    @Column(name = "AAA030")
    var aaa030: Long? = null

    @Column(name = "AAA031")
    var aaa031: String? = null

    @Column(name = "ATC900")
    var atc900: Long? = null

    @Column(name = "AAC001")
    var aac001: Long? = null

    @Column(name = "AAC003")
    var aac003: String? = null

    @Column(name = "AAC002")
    var aac002: String? = null

    @Column(name = "AAE001")
    var aae001: Long? = null

    @Column(name = "AAA001")
    var aaa001: String? = null

    @Column(name = "AAA004")
    var aaa004: String? = null

    @Column(name = "AAA005")
    var aaa005: String? = null

    @Column(name = "AAA009")
    var aaa009: String? = null

    @Column(name = "AAA100")
    var aaa100: String? = null

    @Column(name = "AAA101")
    var aaa101: String? = null

    @Column(name = "AAA110")
    var aaa110: String? = null

    @Column(name = "AAA111")
    var aaa111: String? = null

    @Column(name = "AAA120")
    var aaa120: String? = null

    @Column(name = "AAA121")
    var aaa121: String? = null

    @Column(name = "AAA130")
    var aaa130: String? = null

    @Column(name = "AAA131")
    var aaa131: String? = null

    @Column(name = "AAA300")
    var aaa300: BigDecimal? = null

    @Column(name = "AAA301")
    var aaa301: BigDecimal? = null

    @Column(name = "AAA302")
    var aaa302: BigDecimal? = null

    @Column(name = "AAA303")
    var aaa303: BigDecimal? = null

    @Column(name = "AAA304")
    var aaa304: BigDecimal? = null

    @Column(name = "AAA305")
    var aaa305: BigDecimal? = null

    @Column(name = "AAA306")
    var aaa306: BigDecimal? = null

    @Column(name = "ATB90S")
    var atb90s: String? = null

    @Column(name = "AAA301_1")
    var aaa301_1: BigDecimal? = null

    @Column(name = "AAA302_1")
    var aaa302_1: BigDecimal? = null

    @Column(name = "AAA303_1")
    var aaa303_1: BigDecimal? = null

    @Column(name = "AAA304_1")
    var aaa304_1: BigDecimal? = null

    @Column(name = "AAA305_1")
    var aaa305_1: BigDecimal? = null

    @Column(name = "AAA306_1")
    var aaa306_1: BigDecimal? = null

    @Column(name = "ATB90S_DW")
    var atb90s_dw: BigDecimal? = null

    @Column(name = "ATB90S_GR")
    var atb90s_gr: BigDecimal? = null

    @Column(name = "ABP00C")
    var abp00c: String? = null

    @Column(name = "ABP00D")
    var abp00d: String? = null

    @Column(name = "ABP00E")
    var abp00e: String? = null

    @Column(name = "ATC90P")
    var atc90p: String? = null

    @Column(name = "ATC90W")
    var atc90w: String? = null

    @Column(name = "ATC90PA")
    var atc90pa: String? = null

    @Column(name = "ATC90PB")
    var atc90pb: String? = null

    @Column(name = "AAE100")
    var aae100: String? = null

    @Column(name = "AAE013")
    var aae013: String? = null

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

    @Column(name = "AAE002_1")
    var aae002_1: Long? = null

    @Column(name = "AAE214_1")
    var aae214_1: String? = null

    @Column(name = "AAE211_1")
    var aae211_1: String? = null

    @Column(name = "AAE217_1")
    var aae217_1: String? = null

    @Column(name = "AAE236_1")
    var aae236_1: LocalDate? = null

    @Column(name = "AAE219_1")
    var aae219_1: String? = null

    @Column(name = "AAE220_1")
    var aae220_1: String? = null

    @Column(name = "AAE222_1")
    var aae222_1: String? = null

    @Column(name = "AAE002_2")
    var aae002_2: Long? = null

    @Column(name = "AAE214_2")
    var aae214_2: String? = null

    @Column(name = "AAE211_2")
    var aae211_2: String? = null

    @Column(name = "AAE217_2")
    var aae217_2: String? = null

    @Column(name = "AAE236_2")
    var aae236_2: LocalDate? = null

    @Column(name = "AAE219_2")
    var aae219_2: String? = null

    @Column(name = "AAE220_2")
    var aae220_2: String? = null

    @Column(name = "AAE222_2")
    var aae222_2: String? = null

    @Column(name = "AAE002_3")
    var aae002_3: Long? = null

    @Column(name = "AAE214_3")
    var aae214_3: String? = null

    @Column(name = "AAE211_3")
    var aae211_3: String? = null

    @Column(name = "AAE217_3")
    var aae217_3: String? = null

    @Column(name = "AAE236_3")
    var aae236_3: LocalDate? = null

    @Column(name = "AAE219_3")
    var aae219_3: String? = null

    @Column(name = "AAE220_3")
    var aae220_3: String? = null

    @Column(name = "AAE222_3")
    var aae222_3: String? = null

    @Column(name = "AAE002_4")
    var aae002_4: Long? = null

    @Column(name = "AAE214_4")
    var aae214_4: String? = null

    @Column(name = "AAE211_4")
    var aae211_4: String? = null

    @Column(name = "AAE217_4")
    var aae217_4: String? = null

    @Column(name = "AAE236_4")
    var aae236_4: LocalDate? = null

    @Column(name = "AAE219_4")
    var aae219_4: String? = null

    @Column(name = "AAE220_4")
    var aae220_4: String? = null

    @Column(name = "AAE222_4")
    var aae222_4: String? = null

    @Column(name = "AA97SOUCE")
    var aa97souce: String? = null

}
