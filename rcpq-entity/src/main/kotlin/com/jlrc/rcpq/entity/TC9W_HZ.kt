package com.jlrc.rcpq.entity

import jakarta.persistence.*
import java.io.Serializable
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "TC9W_HZ")
open class TC9W_HZ {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_TC9W_HZ_ID")
    @SequenceGenerator(name = "SEQ_TC9W_HZ_ID", sequenceName = "SEQ_TC9W_HZ_ID", allocationSize = 1)
    @Column(name = "ID")
    var id: Long? = null

    @Column(name = "ACE100")
    var ace100: Long? = null

    @Column(name = "ATB900")
    var atb900: Long? = null

    @Column(name = "ATC9W0")
    var atc9w0: Long? = null

    @Column(name = "FLAG")
    var flag: String? = null

    @Column(name = "KEY")
    var key: String? = null

    @Column(name = "ATB90X")
    var atb90x: String? = null

    @Column(name = "AAC001")
    var aac001: Long? = null

    @Column(name = "AAC003")
    var aac003: String? = null

    @Column(name = "AAC002")
    var aac002: String? = null

    @Column(name = "ATC9W1")
    var atc9w1: String? = null

    @Column(name = "ATC9W2")
    var atc9w2: String? = null

    @Column(name = "ATC9WC")
    var atc9wc: String? = null

    @Column(name = "AAC009")
    var aac009: String? = null

    @Column(name = "TC9WHZ1")
    var tc9whz1: String? = null

    @Column(name = "ATC9WA")
    var atc9wa: String? = null

    @Column(name = "TC9WHZ2")
    var tc9whz2: String? = null

    @Column(name = "ATC9WE")
    var atc9we: String? = null

    @Column(name = "TC9WHZ3")
    var tc9whz3: String? = null

    @Column(name = "TC9WHZ4")
    var tc9whz4: String? = null

    @Column(name = "TC9WHZ5")
    var tc9whz5: String? = null

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

    @Column(name = "ATC900")
    var atc900: Long? = null

}
