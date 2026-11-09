package com.jlrc.rcpq.entity

import jakarta.persistence.*
import java.io.Serializable
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "TC9AA")
open class TC9AA {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_TC9AA_ID")
    @SequenceGenerator(name = "SEQ_TC9AA_ID", sequenceName = "SEQ_TC9AA_ID", allocationSize = 1)
    @Column(name = "ID")
    var id: Long? = null

    @Column(name = "ACE100")
    var ace100: Long? = null

    @Column(name = "ATC9AA1")
    var atc9aa1: String? = null

    @Column(name = "ATC9AA2")
    var atc9aa2: String? = null

    @Column(name = "AAB004")
    var aab004: String? = null

    @Column(name = "AAC003")
    var aac003: String? = null

    @Column(name = "AAC002")
    var aac002: String? = null

    @Column(name = "ATC9AA3")
    var atc9aa3: String? = null

    @Column(name = "ATC9AA4")
    var atc9aa4: String? = null

    @Column(name = "ATC9AA5")
    var atc9aa5: String? = null

    @Column(name = "ATC9AA6")
    var atc9aa6: String? = null

    @Column(name = "ATC9AA7")
    var atc9aa7: String? = null

    @Column(name = "ATC9AA8")
    var atc9aa8: String? = null

    @Column(name = "ATC9AA9")
    var atc9aa9: String? = null

    @Column(name = "ATC9AAA")
    var atc9aaa: String? = null

    @Column(name = "ATC9AAB")
    var atc9aab: String? = null

    @Column(name = "ATC9AAC")
    var atc9aac: String? = null

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
