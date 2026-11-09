package com.jlrc.rcpq.entity

import jakarta.persistence.*
import java.io.Serializable
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "TC9E")
open class TC9E {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_TC9E_ATC9E0")
    @SequenceGenerator(name = "SEQ_TC9E_ATC9E0", sequenceName = "SEQ_TC9E_ATC9E0", allocationSize = 1)
    @Column(name = "ATC9E0")
    var atc9e0: Long? = null

    @Column(name = "BAZ001")
    var baz001: Long? = null

    @Column(name = "BAZ002")
    var baz002: Long? = null

    @Column(name = "ATC900")
    var atc900: Long? = null

    @Column(name = "AAC001")
    var aac001: Long? = null

    @Column(name = "AAC003")
    var aac003: String? = null

    @Column(name = "AAC002")
    var aac002: String? = null

    @Column(name = "ATB930")
    var atb930: Long? = null

    @Column(name = "ATB900")
    var atb900: Long? = null

    @Column(name = "AAB001")
    var aab001: Long? = null

    @Column(name = "AAB004")
    var aab004: String? = null

    @Column(name = "ATC9E1")
    var atc9e1: String? = null

    @Column(name = "ATC9E2")
    var atc9e2: String? = null

    @Column(name = "AAE001")
    var aae001: Long? = null

    @Column(name = "AAE002")
    var aae002: Long? = null

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

    @Column(name = "ATB90K")
    var atb90k: BigDecimal? = null

    @Column(name = "ATB90L")
    var atb90l: BigDecimal? = null

    @Column(name = "ATB90M")
    var atb90m: BigDecimal? = null

    @Column(name = "ATB90N")
    var atb90n: BigDecimal? = null

    @Column(name = "ATB90O")
    var atb90o: BigDecimal? = null

    @Column(name = "ATC9V1")
    var atc9v1: String? = null

    @Column(name = "ATC9V2")
    var atc9v2: String? = null

    @Column(name = "ATC9EE")
    var atc9ee: String? = null

    @Column(name = "ATB90S_DW")
    var atb90s_dw: BigDecimal? = null

    @Column(name = "ATB90S_GR")
    var atb90s_gr: BigDecimal? = null

    @Column(name = "ATC9EK")
    var atc9ek: BigDecimal? = null

    @Column(name = "ATC9EH")
    var atc9eh: BigDecimal? = null

    @Column(name = "ATC9EI")
    var atc9ei: String? = null

    @Column(name = "ATC9EJ")
    var atc9ej: String? = null

    @Column(name = "ATB910")
    var atb910: Long? = null

    @Column(name = "ATC9T4")
    var atc9t4: String? = null

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

    @Column(name = "JSNY")
    var jsny: Long? = null

    @Column(name = "DAA001")
    var daa001: BigDecimal? = null

    @Column(name = "ATC9EM")
    var atc9em: String? = null

    @Column(name = "ATC9EN")
    var atc9en: String? = null

    @Column(name = "ATC9ER")
    var atc9er: BigDecimal? = null

    @Column(name = "AAC107")
    var aac107: BigDecimal? = null

    @Column(name = "ATB90A")
    var atb90a: BigDecimal? = null

    @Column(name = "ATB90P")
    var atb90p: BigDecimal? = null

    @Column(name = "ATB90Q")
    var atb90q: BigDecimal? = null

    @Column(name = "ATB90R")
    var atb90r: BigDecimal? = null

    @Column(name = "BBA000")
    var bba000: BigDecimal? = null

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

    @Column(name = "BAA000")
    var baa000: BigDecimal? = null

    @Column(name = "BAA001")
    var baa001: BigDecimal? = null

    @Column(name = "BAA002")
    var baa002: BigDecimal? = null

    @Column(name = "BAA003")
    var baa003: BigDecimal? = null

    @Column(name = "CAA001")
    var caa001: BigDecimal? = null

    @Column(name = "CAA002")
    var caa002: BigDecimal? = null

    @Column(name = "BBA200")
    var bba200: BigDecimal? = null

    @Column(name = "BAA200")
    var baa200: BigDecimal? = null

    @Column(name = "ATC93AF")
    var atc93af: BigDecimal? = null

    @Column(name = "AAC106")
    var aac106: BigDecimal? = null

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

    @Column(name = "ATC9VC")
    var atc9vc: String? = null

    @Column(name = "ATC9EO")
    var atc9eo: String? = null

    @Column(name = "AAA307")
    var aaa307: BigDecimal? = null

    @Column(name = "ATC9EP")
    var atc9ep: BigDecimal? = null

    @Column(name = "ATC9EQ")
    var atc9eq: BigDecimal? = null

    @Column(name = "AAA308")
    var aaa308: BigDecimal? = null

    @Column(name = "AAA399")
    var aaa399: BigDecimal? = null

    @Column(name = "ATB90T")
    var atb90t: BigDecimal? = null

    @Column(name = "ATC9ES")
    var atc9es: String? = null

}
