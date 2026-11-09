package com.jlrc.rcpq.entity

import jakarta.persistence.*
import java.io.Serializable
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "TC07")
open class TC07 {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_TC07_ATC070")
    @SequenceGenerator(name = "SEQ_TC07_ATC070", sequenceName = "SEQ_TC07_ATC070", allocationSize = 1)
    @Column(name = "ATC070")
    var atc070: Long? = null

    @Column(name = "BAZ001")
    var baz001: Long? = null

    @Column(name = "BAZ002")
    var baz002: Long? = null

    @Column(name = "AAE002")
    var aae002: Long? = null

    @Column(name = "AAE003")
    var aae003: Long? = null

    @Column(name = "ATC70X")
    var atc70x: String? = null

    @Column(name = "ATC70Y")
    var atc70y: String? = null

    @Column(name = "ATC900")
    var atc900: Long? = null

    @Column(name = "AAC001")
    var aac001: Long? = null

    @Column(name = "AAA001")
    var aaa001: String? = null

    @Column(name = "AAA004")
    var aaa004: String? = null

    @Column(name = "AAA005")
    var aaa005: String? = null

    @Column(name = "AAC003")
    var aac003: String? = null

    @Column(name = "AAC002")
    var aac002: String? = null

    @Column(name = "AAC009")
    var aac009: String? = null

    @Column(name = "AAC008")
    var aac008: String? = null

    @Column(name = "ATB930")
    var atb930: Long? = null

    @Column(name = "ATB931")
    var atb931: String? = null

    @Column(name = "ATB900")
    var atb900: Long? = null

    @Column(name = "AAB001")
    var aab001: Long? = null

    @Column(name = "AAB004")
    var aab004: String? = null

    @Column(name = "AAA030")
    var aaa030: Long? = null

    @Column(name = "AAA031")
    var aaa031: String? = null

    @Column(name = "ATC071")
    var atc071: String? = null

    @Column(name = "ATC072")
    var atc072: String? = null

    @Column(name = "ATC073")
    var atc073: String? = null

    @Column(name = "AAA911")
    var aaa911: Long? = null

    @Column(name = "AAA912")
    var aaa912: Long? = null

    @Column(name = "AAA913")
    var aaa913: Long? = null

    @Column(name = "AAA100")
    var aaa100: String? = null

    @Column(name = "AAA101")
    var aaa101: String? = null

    @Column(name = "AAA301")
    var aaa301: BigDecimal? = null

    @Column(name = "AAA304")
    var aaa304: BigDecimal? = null

    @Column(name = "ATB90K")
    var atb90k: BigDecimal? = null

    @Column(name = "ATB90M")
    var atb90m: BigDecimal? = null

    @Column(name = "ATB90P")
    var atb90p: BigDecimal? = null

    @Column(name = "ATB90R")
    var atb90r: BigDecimal? = null

    @Column(name = "BBA001")
    var bba001: BigDecimal? = null

    @Column(name = "BAA001")
    var baa001: BigDecimal? = null

    @Column(name = "BBA003")
    var bba003: BigDecimal? = null

    @Column(name = "BAA003")
    var baa003: BigDecimal? = null

    @Column(name = "AAA110")
    var aaa110: String? = null

    @Column(name = "AAA111")
    var aaa111: String? = null

    @Column(name = "AAA302")
    var aaa302: BigDecimal? = null

    @Column(name = "AAA305")
    var aaa305: BigDecimal? = null

    @Column(name = "AAA306")
    var aaa306: BigDecimal? = null

    @Column(name = "ATB90L")
    var atb90l: BigDecimal? = null

    @Column(name = "ATB90N")
    var atb90n: BigDecimal? = null

    @Column(name = "ATB90O")
    var atb90o: BigDecimal? = null

    @Column(name = "ATB90Q")
    var atb90q: BigDecimal? = null

    @Column(name = "AAA399")
    var aaa399: BigDecimal? = null

    @Column(name = "BBA002")
    var bba002: BigDecimal? = null

    @Column(name = "BBA004")
    var bba004: BigDecimal? = null

    @Column(name = "BBA005")
    var bba005: BigDecimal? = null

    @Column(name = "BAA002")
    var baa002: BigDecimal? = null

    @Column(name = "ATB90T")
    var atb90t: BigDecimal? = null

    @Column(name = "DAA001")
    var daa001: BigDecimal? = null

    @Column(name = "AAA120")
    var aaa120: String? = null

    @Column(name = "AAA121")
    var aaa121: String? = null

    @Column(name = "AAA303")
    var aaa303: BigDecimal? = null

    @Column(name = "ATB90S_DW")
    var atb90s_dw: BigDecimal? = null

    @Column(name = "ATB90S_GR")
    var atb90s_gr: BigDecimal? = null

    @Column(name = "CAA001")
    var caa001: BigDecimal? = null

    @Column(name = "CAA002")
    var caa002: BigDecimal? = null

    @Column(name = "BBA200")
    var bba200: BigDecimal? = null

    @Column(name = "BAA200")
    var baa200: BigDecimal? = null

    @Column(name = "ATC9EH")
    var atc9eh: BigDecimal? = null

    @Column(name = "ATC07Z")
    var atc07z: String? = null

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

    @Column(name = "AAE013")
    var aae013: String? = null

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

    @Column(name = "DATASOURCE")
    var datasource: String? = null

    @Column(name = "DAA002")
    var daa002: BigDecimal? = null

    @Column(name = "ATB90A")
    var atb90a: BigDecimal? = null

    @Column(name = "ATB90B")
    var atb90b: BigDecimal? = null

    @Column(name = "COMM_1")
    var comm_1: String? = null

    @Column(name = "COMM_2")
    var comm_2: String? = null

    @Column(name = "COMM_3")
    var comm_3: String? = null

}
