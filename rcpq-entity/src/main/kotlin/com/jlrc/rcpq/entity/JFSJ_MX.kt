package com.jlrc.rcpq.entity

import jakarta.persistence.*
import java.io.Serializable
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "JFSJ_MX")
open class JFSJ_MX {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_JFSJ_MX_ID")
    @SequenceGenerator(name = "SEQ_JFSJ_MX_ID", sequenceName = "SEQ_JFSJ_MX_ID", allocationSize = 1)
    @Column(name = "ID")
    var id: Long? = null

    @Column(name = "AAA030")
    var aaa030: Long? = null

    @Column(name = "AAA031")
    var aaa031: String? = null

    @Column(name = "AAE002")
    var aae002: Long? = null

    @Column(name = "JFLX")
    var jflx: String? = null

    @Column(name = "JFXZ")
    var jfxz: String? = null

    @Column(name = "SEQ")
    var seq: Long? = null

    @Column(name = "JFSJ001")
    var jfsj001: String? = null

    @Column(name = "JFSJ002")
    var jfsj002: String? = null

    @Column(name = "JFSJ003")
    var jfsj003: String? = null

    @Column(name = "AAB001")
    var aab001: Long? = null

    @Column(name = "ATB930")
    var atb930: Long? = null

    @Column(name = "ATB931")
    var atb931: String? = null

    @Column(name = "ATC900")
    var atc900: Long? = null

    @Column(name = "ATC930")
    var atc930: Long? = null

    @Column(name = "ATC932")
    var atc932: Long? = null

    @Column(name = "AAA301")
    var aaa301: BigDecimal? = null

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

    @Column(name = "ATC9U2")
    var atc9u2: BigDecimal? = null

    @Column(name = "ATC9V9")
    var atc9v9: BigDecimal? = null

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

    @Column(name = "DAA001")
    var daa001: BigDecimal? = null

    @Column(name = "ATC93AF")
    var atc93af: BigDecimal? = null

    @Column(name = "ACE100")
    var ace100: Long? = null

    @Column(name = "AAA303")
    var aaa303: BigDecimal? = null

    @Column(name = "ATC933")
    var atc933: BigDecimal? = null
}
