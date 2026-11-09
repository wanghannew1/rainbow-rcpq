package com.jlrc.rcpq.entity

import jakarta.persistence.*
import java.io.Serializable
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "TC90_TMP")
open class TC90_TMP {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_TC90_TMP_ID")
    @SequenceGenerator(name = "SEQ_TC90_TMP_ID", sequenceName = "SEQ_TC90_TMP_ID", allocationSize = 1)
    @Column(name = "ID")
    var id: Long? = null

    @Column(name = "ATC900")
    var atc900: Long? = null

    @Column(name = "AAC003")
    var aac003: String? = null

    @Column(name = "AAC002")
    var aac002: String? = null

    @Column(name = "AAA001")
    var aaa001: String? = null

    @Column(name = "AAA301")
    var aaa301: String? = null

    @Column(name = "AAA303")
    var aaa303: String? = null

    @Column(name = "ATB90S")
    var atb90s: String? = null

    @Column(name = "ATC9V1")
    var atc9v1: String? = null

    @Column(name = "AAA003")
    var aaa003: String? = null

    @Column(name = "BBB001")
    var bbb001: String? = null

    @Column(name = "AAB004")
    var aab004: String? = null

    @Column(name = "AAC009")
    var aac009: String? = null

    @Column(name = "BAA001")
    var baa001: BigDecimal? = null

    @Column(name = "CAA002")
    var caa002: BigDecimal? = null

    @Column(name = "PINYIN")
    var pinyin: String? = null

    @Column(name = "ORGIN")
    var orgin: String? = null

    @Column(name = "FLAG")
    var flag: String? = null

    @Column(name = "LABEL1")
    var label1: String? = null

    @Column(name = "LABEL2")
    var label2: String? = null

    @Column(name = "LABEL3")
    var label3: String? = null

    @Column(name = "ACE100")
    var ace100: Long? = null

    @Column(name = "ATC90P")
    var atc90p: String? = null

    @Column(name = "ATC90W")
    var atc90w: String? = null

    @Column(name = "ATC904")
    var atc904: Long? = null

}
