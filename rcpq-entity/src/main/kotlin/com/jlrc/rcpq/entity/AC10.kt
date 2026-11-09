package com.jlrc.rcpq.entity

import jakarta.persistence.*
import java.io.Serializable
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "AC10")
open class AC10 {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_AC10_ID")
    @SequenceGenerator(name = "SEQ_AC10_ID", sequenceName = "SEQ_AC10_ID", allocationSize = 1)
    @Column(name = "ID")
    var id: Long? = null

    @Column(name = "AAC001")
    var aac001: Long? = null

    @Column(name = "ATC900")
    var atc900: Long? = null

    @Column(name = "AAB001")
    var aab001: Long? = null

    @Column(name = "ATB930")
    var atb930: Long? = null

    @Column(name = "ATB931")
    var atb931: String? = null

    @Column(name = "ATC930")
    var atc930: Long? = null

    @Column(name = "AAE001")
    var aae001: Long? = null

    @Column(name = "ATC932")
    var atc932: Long? = null

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

    @Column(name = "ATB90P")
    var atb90p: BigDecimal? = null

    @Column(name = "ATB90Q")
    var atb90q: BigDecimal? = null

    @Column(name = "ATB90R")
    var atb90r: BigDecimal? = null

    @Column(name = "ATB90S_DW")
    var atb90s_dw: BigDecimal? = null

    @Column(name = "ATB90S_GR")
    var atb90s_gr: BigDecimal? = null

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

    @Column(name = "DAA001")
    var daa001: BigDecimal? = null

    @Column(name = "ATC93AF")
    var atc93af: BigDecimal? = null

    @Column(name = "AAC101")
    var aac101: String? = null

    @Column(name = "AAC102")
    var aac102: String? = null

    @Column(name = "AAC103")
    var aac103: String? = null

    @Column(name = "AAC104")
    var aac104: String? = null

    @Column(name = "AAC105")
    var aac105: String? = null

    @Column(name = "AAC106")
    var aac106: BigDecimal? = null

    @Column(name = "AAC107")
    var aac107: BigDecimal? = null

    @Column(name = "ATB90A")
    var atb90a: BigDecimal? = null

    @Column(name = "ATC9E0")
    var atc9e0: Long? = null

    @Column(name = "ATC9EN")
    var atc9en: String? = null

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

    @Column(name = "ATC9EH")
    var atc9eh: BigDecimal? = null

    @Column(name = "ATC9T0")
    var atc9t0: Long? = null

    @Column(name = "AAC108")
    var aac108: String? = null

    @Column(name = "ATC9EO")
    var atc9eo: String? = null

    @Column(name = "ATC9T4")
    var atc9t4: String? = null

    @Column(name = "AAC109")
    var aac109: String? = null

    @Column(name = "BAZ001")
    var baz001: Long? = null

    @Column(name = "AAC10A")
    var aac10a: String? = null

    @Column(name = "AAA308")
    var aaa308: BigDecimal? = null

    @Column(name = "AAA399")
    var aaa399: BigDecimal? = null

    @Column(name = "ATB90T")
    var atb90t: BigDecimal? = null

    @Column(name = "AAC110")
    var aac110: String? = null

}
