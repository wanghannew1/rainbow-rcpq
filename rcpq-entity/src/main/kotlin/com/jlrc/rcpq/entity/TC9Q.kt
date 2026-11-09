package com.jlrc.rcpq.entity

import jakarta.persistence.*
import java.io.Serializable
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "TC9Q")
open class TC9Q {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_TC9Q_ATC9Q0")
    @SequenceGenerator(name = "SEQ_TC9Q_ATC9Q0", sequenceName = "SEQ_TC9Q_ATC9Q0", allocationSize = 1)
    @Column(name = "ATC9Q0")
    var atc9q0: Long? = null

    @Column(name = "ATC900")
    var atc900: Long? = null

    @Column(name = "AAC001")
    var aac001: Long? = null

    @Column(name = "AAC003")
    var aac003: String? = null

    @Column(name = "AAC002")
    var aac002: String? = null

    @Column(name = "ATB900")
    var atb900: Long? = null

    @Column(name = "ATB930")
    var atb930: Long? = null

    @Column(name = "ATB931")
    var atb931: String? = null

    @Column(name = "ATB903")
    var atb903: String? = null

    @Column(name = "AAB001")
    var aab001: Long? = null

    @Column(name = "AAB004")
    var aab004: String? = null

    @Column(name = "ATC9Q1")
    var atc9q1: String? = null

    @Column(name = "ATC9Q2")
    var atc9q2: String? = null

    @Column(name = "ATC9Q3")
    var atc9q3: String? = null

    @Column(name = "BBB001")
    var bbb001: String? = null

    @Column(name = "BBB002")
    var bbb002: String? = null

    @Column(name = "ATC9Q4")
    var atc9q4: String? = null

    @Column(name = "ATC9Q5")
    var atc9q5: LocalDate? = null

    @Column(name = "ATC9Q6")
    var atc9q6: LocalDate? = null

    @Column(name = "ATC9Q7")
    var atc9q7: BigDecimal? = null

    @Column(name = "ATC9Q8")
    var atc9q8: LocalDate? = null

    @Column(name = "ATC9Q9")
    var atc9q9: BigDecimal? = null

    @Column(name = "ATC9QA")
    var atc9qa: LocalDate? = null

    @Column(name = "ATC9QB")
    var atc9qb: BigDecimal? = null

    @Column(name = "ATC9QC")
    var atc9qc: LocalDate? = null

    @Column(name = "ATC9QD")
    var atc9qd: BigDecimal? = null

    @Column(name = "ATC9QE")
    var atc9qe: String? = null

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

    @Column(name = "AAEA11")
    var aaea11: String? = null

    @Column(name = "AAEA36")
    var aaea36: LocalDate? = null

    @Column(name = "AAEB11")
    var aaeb11: String? = null

    @Column(name = "AAEB36")
    var aaeb36: LocalDate? = null

    @Column(name = "AAEC11")
    var aaec11: String? = null

    @Column(name = "AAEC36")
    var aaec36: LocalDate? = null

    @Column(name = "AAED11")
    var aaed11: String? = null

    @Column(name = "AAED36")
    var aaed36: LocalDate? = null

    @Column(name = "AAEE11")
    var aaee11: String? = null

    @Column(name = "AAEE36")
    var aaee36: LocalDate? = null

    @Column(name = "AAC004")
    var aac004: String? = null

    @Column(name = "ATC9QF")
    var atc9qf: BigDecimal? = null

    @Column(name = "ATC9QG")
    var atc9qg: BigDecimal? = null

    @Column(name = "ATC9QH")
    var atc9qh: BigDecimal? = null

    @Column(name = "ATC9QI")
    var atc9qi: BigDecimal? = null

    @Column(name = "ATC9QJ")
    var atc9qj: String? = null

    @Column(name = "ATC9QK")
    var atc9qk: BigDecimal? = null

    @Column(name = "ATC9QL")
    var atc9ql: BigDecimal? = null

    @Column(name = "ATC9QM")
    var atc9qm: BigDecimal? = null

    @Column(name = "ATC9QN")
    var atc9qn: BigDecimal? = null

    @Column(name = "ATC9QP")
    var atc9qp: BigDecimal? = null

    @Column(name = "ATC9QQ")
    var atc9qq: BigDecimal? = null

    @Column(name = "ATC9QR")
    var atc9qr: BigDecimal? = null

    @Column(name = "ATC9QS")
    var atc9qs: BigDecimal? = null

    @Column(name = "ATC9QT")
    var atc9qt: BigDecimal? = null

    @Column(name = "ATC9QU")
    var atc9qu: BigDecimal? = null

    @Column(name = "AAA031")
    var aaa031: String? = null

    @Column(name = "ATC9QV")
    var atc9qv: BigDecimal? = null

    @Column(name = "ATC9QW")
    var atc9qw: BigDecimal? = null

    @Column(name = "ATC9QX")
    var atc9qx: String? = null

    @Column(name = "ATC9QY")
    var atc9qy: String? = null

    @Column(name = "ATC9QZA")
    var atc9qza: String? = null

    @Column(name = "ATC9QZB")
    var atc9qzb: LocalDate? = null

    @Column(name = "ATC9QZG")
    var atc9qzg: LocalDate? = null

    @Column(name = "AAA032")
    var aaa032: String? = null

    @Column(name = "ATC9QZC")
    var atc9qzc: LocalDate? = null

    @Column(name = "ATC9QZD")
    var atc9qzd: String? = null

    @Column(name = "ATC9QZF")
    var atc9qzf: LocalDate? = null

    @Column(name = "ATC9QSH")
    var atc9qsh: String? = null

    @Column(name = "ATC9QSP")
    var atc9qsp: String? = null

    @Column(name = "ATC9QSD")
    var atc9qsd: LocalDate? = null

    @Column(name = "ATC9QSH2")
    var atc9qsh2: String? = null

    @Column(name = "ATC9QSP2")
    var atc9qsp2: String? = null

    @Column(name = "ATC9QSD2")
    var atc9qsd2: LocalDate? = null

    @Column(name = "ATC9QSA")
    var atc9qsa: String? = null

    @Column(name = "ATC9QSA2")
    var atc9qsa2: String? = null

    @Column(name = "AAB00E")
    var aab00e: String? = null

    @Column(name = "AAE005")
    var aae005: String? = null

}
