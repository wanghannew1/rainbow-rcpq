package com.jlrc.rcpq.entity

import jakarta.persistence.*
import java.io.Serializable
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "TJ01_EXPENDITURE")
open class TJ01_EXPENDITURE {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_TJ01_EXPENDITURE_ID")
    @SequenceGenerator(name = "SEQ_TJ01_EXPENDITURE_ID", sequenceName = "SEQ_TJ01_EXPENDITURE_ID", allocationSize = 1)
    @Column(name = "ID")
    var id: Long? = null

    @Column(name = "AAE001")
    var aae001: Long? = null

    @Column(name = "SEQ")
    var seq: Long? = null

    @Column(name = "MONTH")
    var month: Long? = null

    @Column(name = "EXPENDITURE1")
    var expenditure1: String? = null

    @Column(name = "EXPENDITURE2")
    var expenditure2: BigDecimal? = null

    @Column(name = "EXPENDITURE3")
    var expenditure3: BigDecimal? = null

    @Column(name = "EXPENDITURE4")
    var expenditure4: BigDecimal? = null

    @Column(name = "EXPENDITURE5")
    var expenditure5: BigDecimal? = null

    @Column(name = "EXPENDITURE6")
    var expenditure6: BigDecimal? = null

    @Column(name = "EXPENDITURE7")
    var expenditure7: BigDecimal? = null

    @Column(name = "EXPENDITURE8")
    var expenditure8: BigDecimal? = null

    @Column(name = "EXPENDITURE9")
    var expenditure9: BigDecimal? = null

    @Column(name = "EXPENDITUREA")
    var expenditurea: BigDecimal? = null

    @Column(name = "EXPENDITUREB")
    var expenditureb: BigDecimal? = null

    @Column(name = "EXPENDITUREC")
    var expenditurec: BigDecimal? = null

    @Column(name = "EXPENDITURED")
    var expenditured: BigDecimal? = null

    @Column(name = "EXPENDITUREE")
    var expendituree: BigDecimal? = null

    @Column(name = "EXPENDITUREF")
    var expendituref: BigDecimal? = null

    @Column(name = "EXPENDITUREG")
    var expenditureg: BigDecimal? = null

    @Column(name = "EXPENDITUREH")
    var expenditureh: BigDecimal? = null

    @Column(name = "EXPENDITUREI")
    var expenditurei: BigDecimal? = null

    @Column(name = "EXPENDITUREJ")
    var expenditurej: BigDecimal? = null

    @Column(name = "EXPENDITUREK")
    var expenditurek: BigDecimal? = null

    @Column(name = "EXPENDITUREL")
    var expenditurel: BigDecimal? = null

    @Column(name = "EXPENDITUREM")
    var expenditurem: BigDecimal? = null

    @Column(name = "EXPENDITUREAJ")
    var expenditureaj: BigDecimal? = null

    @Column(name = "EXPENDITUREAK")
    var expenditureak: BigDecimal? = null

    @Column(name = "EXPENDITUREAL")
    var expenditureal: BigDecimal? = null

    @Column(name = "EXPENDITUREAM")
    var expendituream: BigDecimal? = null

    @Column(name = "EXPENDITUREAN")
    var expenditurean: BigDecimal? = null

    @Column(name = "EXPENDITUREAP")
    var expenditureap: BigDecimal? = null

    @Column(name = "EXPENDITUREAQ")
    var expenditureaq: BigDecimal? = null

    @Column(name = "EXPENDITUREAR")
    var expenditurear: BigDecimal? = null

    @Column(name = "EXPENDITUREAS")
    var expenditureas: BigDecimal? = null

    @Column(name = "EXPENDITUREAT")
    var expenditureat: BigDecimal? = null

    @Column(name = "EXPENDITUREAU")
    var expenditureau: BigDecimal? = null

    @Column(name = "EXPENDITUREN")
    var expendituren: BigDecimal? = null

    @Column(name = "EXPENDITUREO")
    var expenditureo: BigDecimal? = null

    @Column(name = "EXPENDITUREP")
    var expenditurep: BigDecimal? = null

    @Column(name = "EXPENDITUREQ")
    var expenditureq: BigDecimal? = null

    @Column(name = "EXPENDITURER")
    var expenditurer: BigDecimal? = null

    @Column(name = "EXPENDITURES")
    var expenditures: BigDecimal? = null

    @Column(name = "EXPENDITURET")
    var expendituret: BigDecimal? = null

    @Column(name = "EXPENDITUREU")
    var expenditureu: BigDecimal? = null

    @Column(name = "EXPENDITUREV")
    var expenditurev: BigDecimal? = null

    @Column(name = "EXPENDITUREW")
    var expenditurew: BigDecimal? = null

    @Column(name = "EXPENDITUREX")
    var expenditurex: BigDecimal? = null

    @Column(name = "EXPENDITUREY")
    var expenditurey: BigDecimal? = null

    @Column(name = "EXPENDITUREAA")
    var expenditureaa: BigDecimal? = null

    @Column(name = "EXPENDITUREAB")
    var expenditureab: BigDecimal? = null

    @Column(name = "EXPENDITUREAC")
    var expenditureac: BigDecimal? = null

    @Column(name = "EXPENDITUREAD")
    var expendituread: BigDecimal? = null

    @Column(name = "EXPENDITUREAE")
    var expenditureae: BigDecimal? = null

    @Column(name = "EXPENDITUREAF")
    var expenditureaf: BigDecimal? = null

    @Column(name = "EXPENDITUREAG")
    var expenditureag: BigDecimal? = null

    @Column(name = "EXPENDITUREAH")
    var expenditureah: BigDecimal? = null

    @Column(name = "EXPENDITUREAI")
    var expenditureai: BigDecimal? = null

    @Column(name = "EXPENDITUREAV")
    var expenditureav: BigDecimal? = null

}
