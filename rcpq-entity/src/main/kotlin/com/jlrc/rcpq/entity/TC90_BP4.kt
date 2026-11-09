package com.jlrc.rcpq.entity

import jakarta.persistence.*
import java.io.Serializable
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "TC90_BP4")
open class TC90_BP4 {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_TC90_BP4_ID")
    @SequenceGenerator(name = "SEQ_TC90_BP4_ID", sequenceName = "SEQ_TC90_BP4_ID", allocationSize = 1)
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

    @Column(name = "ATC90C")
    var atc90c: String? = null

    @Column(name = "AAA100")
    var aaa100: String? = null

    @Column(name = "AAA201")
    var aaa201: String? = null

    @Column(name = "AAA301")
    var aaa301: String? = null

    @Column(name = "AAA110")
    var aaa110: String? = null

    @Column(name = "AAA601")
    var aaa601: String? = null

    @Column(name = "AAA302")
    var aaa302: String? = null

    @Column(name = "AAA120")
    var aaa120: String? = null

    @Column(name = "ATB90S")
    var atb90s: String? = null

    @Column(name = "AAA501")
    var aaa501: String? = null

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
