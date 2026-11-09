package com.jlrc.rcpq.entity

import jakarta.persistence.*
import java.io.Serializable
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "TC06")
open class TC06 {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_TC06_ATC060")
    @SequenceGenerator(name = "SEQ_TC06_ATC060", sequenceName = "SEQ_TC06_ATC060", allocationSize = 1)
    @Column(name = "ATC060")
    var atc060: Long? = null

    @Column(name = "BAZ001")
    var baz001: Long? = null

    @Column(name = "BAZ002")
    var baz002: Long? = null

    @Column(name = "AAC001")
    var aac001: Long? = null

    @Column(name = "AAC003")
    var aac003: String? = null

    @Column(name = "AAC002")
    var aac002: String? = null

    @Column(name = "ATC061")
    var atc061: String? = null

    @Column(name = "AAE002")
    var aae002: Long? = null

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
    var atc06a: Long? = null

    @Column(name = "ATC06B")
    var atc06b: Long? = null

    @Column(name = "ATC06C")
    var atc06c: Long? = null

    @Column(name = "ATC06D")
    var atc06d: Long? = null

    @Column(name = "ATC06E")
    var atc06e: Long? = null

    @Column(name = "ATC06F")
    var atc06f: Long? = null

    @Column(name = "ATC06Z")
    var atc06z: String? = null

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

    @Column(name = "KEY1")
    var key1: Long? = null

    @Column(name = "KEY2")
    var key2: Long? = null

    @Column(name = "KEY3")
    var key3: Long? = null

    @Column(name = "KEY4")
    var key4: Long? = null

    @Column(name = "KEY5")
    var key5: Long? = null

    @Column(name = "KEY6")
    var key6: Long? = null

    @Column(name = "AAE013")
    var aae013: String? = null

    @Column(name = "AAE100")
    var aae100: String? = null

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

    @Column(name = "ATB90S")
    var atb90s: String? = null

    @Column(name = "AAA030")
    var aaa030: Long? = null

    @Column(name = "AAA031")
    var aaa031: String? = null

    @Column(name = "ATC900")
    var atc900: Long? = null

    @Column(name = "ATB930")
    var atb930: Long? = null

    @Column(name = "ATB931")
    var atb931: String? = null

    @Column(name = "AAB001")
    var aab001: Long? = null

    @Column(name = "AAB004")
    var aab004: String? = null

}
