package com.jlrc.rcpq.entity

import jakarta.persistence.*
import java.io.Serializable
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "TC0A_BP")
open class TC0A_BP {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_TC0A_ID")
    @SequenceGenerator(name = "SEQ_TC0A_ID", sequenceName = "SEQ_TC0A_ID", allocationSize = 1)
    @Column(name = "ID")
    var id: Long? = null

    @Column(name = "ACE100")
    var ace100: Long? = null

    @Column(name = "ATB930")
    var atb930: Long? = null

    @Column(name = "ATB931")
    var atb931: String? = null

    @Column(name = "AAE002")
    var aae002: String? = null

    @Column(name = "AAC003")
    var aac003: String? = null

    @Column(name = "AAC002")
    var aac002: String? = null

    @Column(name = "ATC905")
    var atc905: String? = null

    @Column(name = "AAA303_1")
    var aaa303_1: String? = null

    @Column(name = "AAA303_2")
    var aaa303_2: String? = null

    @Column(name = "ATC0A1")
    var atc0a1: String? = null

    @Column(name = "ATC0A2")
    var atc0a2: String? = null

    @Column(name = "ATC0A3")
    var atc0a3: String? = null

    @Column(name = "ATB90S_DW")
    var atb90s_dw: BigDecimal? = null

    @Column(name = "ATB90S_GR")
    var atb90s_gr: BigDecimal? = null

    @Column(name = "ABP00C")
    var abp00c: String? = null

    @Column(name = "ABP00D")
    var abp00d: String? = null

    @Column(name = "ABP00E")
    var abp00e: String? = null

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

}
