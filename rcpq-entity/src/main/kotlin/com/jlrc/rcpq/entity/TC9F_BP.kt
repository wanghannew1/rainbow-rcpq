package com.jlrc.rcpq.entity

import jakarta.persistence.*
import java.io.Serializable
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "TC9F_BP")
open class TC9F_BP {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_TC9F_ID")
    @SequenceGenerator(name = "SEQ_TC9F_ID", sequenceName = "SEQ_TC9F_ID", allocationSize = 1)
    @Column(name = "ID")
    var id: Long? = null

    @Column(name = "ACE100")
    var ace100: Long? = null

    @Column(name = "AAC001")
    var aac001: Long? = null

    @Column(name = "AAC002")
    var aac002: String? = null

    @Column(name = "AAC003")
    var aac003: String? = null

    @Column(name = "AAC011")
    var aac011: String? = null

    @Column(name = "AAC181")
    var aac181: String? = null

    @Column(name = "AAC180")
    var aac180: String? = null

    @Column(name = "AAC183")
    var aac183: String? = null

    @Column(name = "AAC182")
    var aac182: String? = null

    @Column(name = "AAE00C")
    var aae00c: String? = null

    @Column(name = "ATC9F2")
    var atc9f2: String? = null

    @Column(name = "ATC9F3")
    var atc9f3: String? = null

    @Column(name = "AAE00B")
    var aae00b: String? = null

    @Column(name = "ATC9F4")
    var atc9f4: String? = null

    @Column(name = "ATC9F5")
    var atc9f5: String? = null

    @Column(name = "ATC9F6")
    var atc9f6: String? = null

    @Column(name = "ATC9F7")
    var atc9f7: String? = null

    @Column(name = "ATC9F8")
    var atc9f8: String? = null

    @Column(name = "ATC9F9")
    var atc9f9: String? = null

    @Column(name = "ATC9FA")
    var atc9fa: String? = null

    @Column(name = "ATC9FB")
    var atc9fb: String? = null

    @Column(name = "ATC9FC")
    var atc9fc: String? = null

    @Column(name = "ATC9FD")
    var atc9fd: String? = null

    @Column(name = "ATC9FE")
    var atc9fe: String? = null

    @Column(name = "ATC9FF")
    var atc9ff: String? = null

    @Column(name = "ATC9FG")
    var atc9fg: String? = null

    @Column(name = "ATC9FH")
    var atc9fh: String? = null

    @Column(name = "ATC9FI")
    var atc9fi: String? = null

    @Column(name = "ATC9FJ")
    var atc9fj: String? = null

    @Column(name = "ATC9FK")
    var atc9fk: String? = null

    @Column(name = "ATC9FL")
    var atc9fl: String? = null

    @Column(name = "ATC9FM")
    var atc9fm: String? = null

    @Column(name = "ATC9FN")
    var atc9fn: String? = null

    @Column(name = "ATC9FO")
    var atc9fo: String? = null

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

    @Column(name = "AAE00M")
    var aae00m: String? = null

    @Column(name = "ATC9FP")
    var atc9fp: String? = null

    @Column(name = "AAC004")
    var aac004: String? = null

    @Column(name = "AAC006")
    var aac006: String? = null

    @Column(name = "ATC9FQ")
    var atc9fq: String? = null

    @Column(name = "ATC9FR")
    var atc9fr: LocalDate? = null

    @Column(name = "ATC9FS")
    var atc9fs: Long? = null

}
