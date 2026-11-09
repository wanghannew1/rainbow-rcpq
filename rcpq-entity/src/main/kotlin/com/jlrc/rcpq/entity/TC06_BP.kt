package com.jlrc.rcpq.entity

import jakarta.persistence.*
import java.io.Serializable
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "TC06_BP")
open class TC06_BP {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_TC06_ID")
    @SequenceGenerator(name = "SEQ_TC06_ID", sequenceName = "SEQ_TC06_ID", allocationSize = 1)
    @Column(name = "ID")
    var id: Long? = null

    @Column(name = "ACE100")
    var ace100: Long? = null

    @Column(name = "AAE002")
    var aae002: Long? = null

    @Column(name = "AAC001")
    var aac001: Long? = null

    @Column(name = "AAC003")
    var aac003: String? = null

    @Column(name = "AAC002")
    var aac002: String? = null

    @Column(name = "ATC061")
    var atc061: String? = null

    @Column(name = "ATC062")
    var atc062: String? = null

    @Column(name = "ATC063")
    var atc063: String? = null

    @Column(name = "ATC064")
    var atc064: String? = null

    @Column(name = "ATC065")
    var atc065: String? = null

    @Column(name = "ATC066")
    var atc066: String? = null

    @Column(name = "ATC067")
    var atc067: String? = null

    @Column(name = "ATC06A")
    var atc06a: String? = null

    @Column(name = "ATC06B")
    var atc06b: String? = null

    @Column(name = "ATC06C")
    var atc06c: String? = null

    @Column(name = "ATC06D")
    var atc06d: String? = null

    @Column(name = "ATC06E")
    var atc06e: String? = null

    @Column(name = "ATC06F")
    var atc06f: String? = null

    @Column(name = "AAA301")
    var aaa301: String? = null

    @Column(name = "AAA302")
    var aaa302: String? = null

    @Column(name = "AAA303")
    var aaa303: String? = null

    @Column(name = "AAA304")
    var aaa304: String? = null

    @Column(name = "AAA305")
    var aaa305: String? = null

    @Column(name = "AAA306")
    var aaa306: String? = null

    @Column(name = "AAE013")
    var aae013: String? = null

    @Column(name = "ACE10A")
    var ace10a: String? = null

    @Column(name = "ACE10B")
    var ace10b: String? = null

    @Column(name = "AAE011")
    var aae011: String? = null

    @Column(name = "AAE017")
    var aae017: String? = null

    @Column(name = "AAE036")
    var aae036: String? = null

    @Column(name = "AAE019")
    var aae019: String? = null

    @Column(name = "AAE020")
    var aae020: String? = null

    @Column(name = "AAE022")
    var aae022: String? = null

}
