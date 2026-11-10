package com.jlrc.rcpq.entity

import jakarta.persistence.*
import java.io.Serializable
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "JFSJ02")
open class JFSJ02 {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_JFSJ02_ID")
    @SequenceGenerator(name = "SEQ_JFSJ02_ID", sequenceName = "SEQ_JFSJ02_ID", allocationSize = 1)
    @Column(name = "ID")
    var id: Long? = null

    @Column(name = "ATC930")
    var atc930: Long? = null

    @Column(name = "ATC900")
    var atc900: Long? = null

    @Column(name = "ATB930")
    var atb930: Long? = null

    @Column(name = "AAC003")
    var aac003: String? = null

    @Column(name = "AAC002")
    var aac002: String? = null

    @Column(name = "JFLX")
    var jflx: String? = null

    @Column(name = "AAA301")
    var aaa301: BigDecimal? = null

    @Column(name = "AAA301_")
    var aaa301_: BigDecimal? = null

    @Column(name = "BBA001")
    var bba001: BigDecimal? = null

    @Column(name = "BBA001_")
    var bba001_: BigDecimal? = null

    @Column(name = "JFSJ0201")
    var jfsj0201: BigDecimal? = null

    @Column(name = "BBA002")
    var bba002: BigDecimal? = null

    @Column(name = "BBA002_")
    var bba002_: BigDecimal? = null

    @Column(name = "JFSJ0202")
    var jfsj0202: BigDecimal? = null

    @Column(name = "BBA003")
    var bba003: BigDecimal? = null

    @Column(name = "BBA003_")
    var bba003_: BigDecimal? = null

    @Column(name = "JFSJ0203")
    var jfsj0203: BigDecimal? = null

    @Column(name = "BBA004")
    var bba004: BigDecimal? = null

    @Column(name = "BBA004_")
    var bba004_: BigDecimal? = null

    @Column(name = "JFSJ0204")
    var jfsj0204: BigDecimal? = null

    @Column(name = "BBA005")
    var bba005: BigDecimal? = null

    @Column(name = "BBA005_")
    var bba005_: BigDecimal? = null

    @Column(name = "JFSJ0205")
    var jfsj0205: BigDecimal? = null

    @Column(name = "BAA001")
    var baa001: BigDecimal? = null

    @Column(name = "BAA001_")
    var baa001_: BigDecimal? = null

    @Column(name = "JFSJ0206")
    var jfsj0206: BigDecimal? = null

    @Column(name = "BAA002")
    var baa002: BigDecimal? = null

    @Column(name = "BAA002_")
    var baa002_: BigDecimal? = null

    @Column(name = "JFSJ0207")
    var jfsj0207: BigDecimal? = null

    @Column(name = "BAA003")
    var baa003: BigDecimal? = null

    @Column(name = "BAA003_")
    var baa003_: BigDecimal? = null

    @Column(name = "JFSJ0208")
    var jfsj0208: BigDecimal? = null

    @Column(name = "AAA303")
    var aaa303: BigDecimal? = null

    @Column(name = "AAA303_")
    var aaa303_: BigDecimal? = null

    @Column(name = "CAA001")
    var caa001: BigDecimal? = null

    @Column(name = "CAA001_")
    var caa001_: BigDecimal? = null

    @Column(name = "JFSJ0209")
    var jfsj0209: BigDecimal? = null

    @Column(name = "CAA002")
    var caa002: BigDecimal? = null

    @Column(name = "CAA002_")
    var caa002_: BigDecimal? = null

    @Column(name = "JFSJ020A")
    var jfsj020a: BigDecimal? = null

    @Column(name = "DAA001")
    var daa001: BigDecimal? = null

    @Column(name = "DAA001_")
    var daa001_: BigDecimal? = null

    @Column(name = "JFSJ020B")
    var jfsj020b: BigDecimal? = null

    @Column(name = "ATC93AF")
    var atc93af: BigDecimal? = null

    @Column(name = "ATC93AF_")
    var atc93af_: BigDecimal? = null

    @Column(name = "JFSJ020C")
    var jfsj020c: BigDecimal? = null

    @Column(name = "JFSJ020D")
    var jfsj020d: BigDecimal? = null

    @Column(name = "JFSJ020I")
    var jfsj020i: String? = null

    @Column(name = "AAB001")
    var aab001: Long? = null

    @Column(name = "AAA030")
    var aaa030: Long? = null

    @Column(name = "AAE002")
    var aae002: Long? = null

    @Column(name = "ATC932")
    var atc932: Long? = null

    @Column(name = "ATC933")
    var atc933: Long? = null

    @Column(name = "AAE013")
    var aae013: String? = null

    @Column(name = "JFXZ")
    var jfxz: String? = null
}
