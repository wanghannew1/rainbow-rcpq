package com.jlrc.rcpq.entity

import jakarta.persistence.*
import java.io.Serializable
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "TC90_BP2")
open class TC90_BP2 {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_TC90_BP2_ID")
    @SequenceGenerator(name = "SEQ_TC90_BP2_ID", sequenceName = "SEQ_TC90_BP2_ID", allocationSize = 1)
    @Column(name = "ID")
    var id: Long? = null

    @Column(name = "ACE100")
    var ace100: Long? = null

    @Column(name = "ATB930")
    var atb930: Long? = null

    @Column(name = "ATB900")
    var atb900: Long? = null

    @Column(name = "AAB001")
    var aab001: Long? = null

    @Column(name = "AAB004")
    var aab004: String? = null

    @Column(name = "AAC003")
    var aac003: String? = null

    @Column(name = "AAC002")
    var aac002: String? = null

    @Column(name = "AAE005")
    var aae005: String? = null

    @Column(name = "AAE00B")
    var aae00b: String? = null

    @Column(name = "AAB004_1")
    var aab004_1: String? = null

    @Column(name = "ATC908")
    var atc908: String? = null

    @Column(name = "AAA605")
    var aaa605: String? = null

    @Column(name = "ATB90C")
    var atb90c: String? = null

    @Column(name = "ATB90A")
    var atb90a: String? = null

    @Column(name = "ATB90B")
    var atb90b: String? = null

    @Column(name = "ATC90ZB")
    var atc90zb: String? = null

    @Column(name = "ATC90ZA")
    var atc90za: String? = null

    @Column(name = "ATC90AX")
    var atc90ax: String? = null

    @Column(name = "BBB001")
    var bbb001: String? = null

    @Column(name = "BBB002")
    var bbb002: String? = null

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
