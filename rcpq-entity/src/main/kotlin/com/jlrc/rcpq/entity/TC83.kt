package com.jlrc.rcpq.entity

import jakarta.persistence.*
import java.io.Serializable
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "TC83")
open class TC83 {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_TC83_ATC830")
    @SequenceGenerator(name = "SEQ_TC83_ATC830", sequenceName = "SEQ_TC83_ATC830", allocationSize = 1)
    @Column(name = "ATC830")
    var atc830: Long? = null

    @Column(name = "AAC001")
    var aac001: Long? = null

    @Column(name = "ATC840")
    var atc840: Long? = null

    @Column(name = "ATC841")
    var atc841: String? = null

    @Column(name = "ATC831")
    var atc831: String? = null

    @Column(name = "AAC002")
    var aac002: String? = null

    @Column(name = "AAC003")
    var aac003: String? = null

    @Column(name = "AAC004")
    var aac004: String? = null

    @Column(name = "AAC006")
    var aac006: LocalDate? = null

    @Column(name = "BAC006")
    var bac006: Long? = null

    @Column(name = "AAC024")
    var aac024: String? = null

    @Column(name = "AAC005")
    var aac005: String? = null

    @Column(name = "ATC012")
    var atc012: String? = null

    @Column(name = "AAC007")
    var aac007: LocalDate? = null

    @Column(name = "AAC017")
    var aac017: String? = null

    @Column(name = "AAC011")
    var aac011: String? = null

    @Column(name = "AAC045")
    var aac045: String? = null

    @Column(name = "ACC01G")
    var acc01g: String? = null

    @Column(name = "AAC046")
    var aac046: LocalDate? = null

    @Column(name = "ATC83N")
    var atc83n: String? = null

    @Column(name = "ATC83B")
    var atc83b: LocalDate? = null

    @Column(name = "ATC83C")
    var atc83c: String? = null

    @Column(name = "ATC83D")
    var atc83d: String? = null

    @Column(name = "ATC83E")
    var atc83e: String? = null

    @Column(name = "AAB001")
    var aab001: Long? = null

    @Column(name = "AAB004")
    var aab004: String? = null

    @Column(name = "AAE006")
    var aae006: String? = null

    @Column(name = "AAB299")
    var aab299: String? = null

    @Column(name = "BAB305")
    var bab305: String? = null

    @Column(name = "ATC832")
    var atc832: LocalDate? = null

    @Column(name = "ATC833")
    var atc833: String? = null

    @Column(name = "AAE005")
    var aae005: String? = null

    @Column(name = "ATC838")
    var atc838: String? = null

    @Column(name = "ATC839")
    var atc839: String? = null

    @Column(name = "ATC83A")
    var atc83a: LocalDate? = null

    @Column(name = "AAE100")
    var aae100: String? = null

    @Column(name = "ATC702")
    var atc702: String? = null

    @Column(name = "ATC011")
    var atc011: String? = null

    @Column(name = "AAB019")
    var aab019: String? = null

    @Column(name = "AAB020")
    var aab020: String? = null

    @Column(name = "ATC834")
    var atc834: String? = null

    @Column(name = "ATC835")
    var atc835: String? = null

    @Column(name = "ATC836")
    var atc836: String? = null

    @Column(name = "ATC837")
    var atc837: String? = null

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

    @Column(name = "BAZ001")
    var baz001: Long? = null

    @Column(name = "BAZ002")
    var baz002: Long? = null

    @Column(name = "ATC83F")
    var atc83f: String? = null

    @Column(name = "ATC83G")
    var atc83g: String? = null

    @Column(name = "AAEA13")
    var aaea13: String? = null

    @Column(name = "ATC83H")
    var atc83h: String? = null

    @Column(name = "AAEA36")
    var aaea36: LocalDate? = null

    @Column(name = "AAEA19")
    var aaea19: String? = null

    @Column(name = "AAEA20")
    var aaea20: String? = null

    @Column(name = "AAEB13")
    var aaeb13: String? = null

    @Column(name = "ATC83I")
    var atc83i: String? = null

    @Column(name = "AAEB36")
    var aaeb36: LocalDate? = null

    @Column(name = "AAEB19")
    var aaeb19: String? = null

    @Column(name = "AAEB20")
    var aaeb20: String? = null

    @Column(name = "AAEC13")
    var aaec13: String? = null

    @Column(name = "ATC83J")
    var atc83j: String? = null

    @Column(name = "AAEC36")
    var aaec36: LocalDate? = null

    @Column(name = "AAEC19")
    var aaec19: String? = null

    @Column(name = "AAEC20")
    var aaec20: String? = null

    @Column(name = "AAED13")
    var aaed13: String? = null

    @Column(name = "ATC83K")
    var atc83k: String? = null

    @Column(name = "AAED36")
    var aaed36: LocalDate? = null

    @Column(name = "AAED19")
    var aaed19: String? = null

    @Column(name = "AAED20")
    var aaed20: String? = null

    @Column(name = "AAEF13")
    var aaef13: String? = null

    @Column(name = "ATC83L")
    var atc83l: String? = null

    @Column(name = "AAEF36")
    var aaef36: LocalDate? = null

    @Column(name = "AAEF19")
    var aaef19: String? = null

    @Column(name = "AAEF20")
    var aaef20: String? = null

    @Column(name = "AAEH36")
    var aaeh36: LocalDate? = null

    @Column(name = "AAEH19")
    var aaeh19: String? = null

    @Column(name = "ATC83M")
    var atc83m: String? = null

    @Column(name = "AAEG11")
    var aaeg11: String? = null

    @Column(name = "AAEG17")
    var aaeg17: String? = null

    @Column(name = "AAEG36")
    var aaeg36: LocalDate? = null

    @Column(name = "AAEG19")
    var aaeg19: String? = null

    @Column(name = "AAEG20")
    var aaeg20: String? = null

    @Column(name = "AAEG22")
    var aaeg22: String? = null

}
