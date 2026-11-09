package com.jlrc.rcpq.entity

import jakarta.persistence.*
import java.io.Serializable
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "TJCC4")
open class TJCC4 {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_TJCC4_ID")
    @SequenceGenerator(name = "SEQ_TJCC4_ID", sequenceName = "SEQ_TJCC4_ID", allocationSize = 1)
    @Column(name = "ID")
    var id: Long? = null

    @Column(name = "ACC140")
    var acc140: Long? = null

    @Column(name = "AAE030")
    var aae030: LocalDate? = null

    @Column(name = "AAC001")
    var aac001: Long? = null

    @Column(name = "AAC002")
    var aac002: String? = null

    @Column(name = "AAC003")
    var aac003: String? = null

    @Column(name = "AAC004")
    var aac004: String? = null

    @Column(name = "AAC006")
    var aac006: LocalDate? = null

    @Column(name = "AAC017")
    var aac017: String? = null

    @Column(name = "AAC024")
    var aac024: String? = null

    @Column(name = "AAC026")
    var aac026: String? = null

    @Column(name = "AAA021")
    var aaa021: String? = null

    @Column(name = "AAB301")
    var aab301: String? = null

    @Column(name = "AAC010")
    var aac010: String? = null

    @Column(name = "ACC141")
    var acc141: String? = null

    @Column(name = "ACC142")
    var acc142: String? = null

    @Column(name = "ACC143")
    var acc143: String? = null

    @Column(name = "ACC144")
    var acc144: String? = null

    @Column(name = "AAB052")
    var aab052: String? = null

    @Column(name = "AAB054")
    var aab054: String? = null

    @Column(name = "ACC145")
    var acc145: String? = null

    @Column(name = "ACC146")
    var acc146: String? = null

    @Column(name = "ACC147")
    var acc147: String? = null

    @Column(name = "ACC148")
    var acc148: Long? = null

    @Column(name = "ACC149")
    var acc149: String? = null

    @Column(name = "ACC14A")
    var acc14a: String? = null

    @Column(name = "ACC14B")
    var acc14b: Long? = null

    @Column(name = "ACC14C")
    var acc14c: String? = null

    @Column(name = "ACC14D")
    var acc14d: String? = null

    @Column(name = "ACC14E")
    var acc14e: String? = null

    @Column(name = "ACC14H")
    var acc14h: LocalDate? = null

    @Column(name = "ACC14J")
    var acc14j: BigDecimal? = null

    @Column(name = "ACC14K")
    var acc14k: LocalDate? = null

    @Column(name = "ACC14M")
    var acc14m: BigDecimal? = null

    @Column(name = "ACC14N")
    var acc14n: String? = null

    @Column(name = "ACC14P")
    var acc14p: String? = null

    @Column(name = "ACC14Q")
    var acc14q: String? = null

    @Column(name = "ACC14R")
    var acc14r: String? = null

    @Column(name = "AAE100")
    var aae100: String? = null

    @Column(name = "AAE022")
    var aae022: String? = null

    @Column(name = "ACC14S")
    var acc14s: String? = null

    @Column(name = "ACC14T")
    var acc14t: BigDecimal? = null

    @Column(name = "ACC14U")
    var acc14u: String? = null

    @Column(name = "ACC14V")
    var acc14v: BigDecimal? = null

    @Column(name = "ACC14W")
    var acc14w: String? = null

    @Column(name = "ACC14F")
    var acc14f: String? = null

    @Column(name = "ACC14G")
    var acc14g: String? = null

    @Column(name = "ACC14I")
    var acc14i: String? = null

    @Column(name = "ACC14L")
    var acc14l: Long? = null

    @Column(name = "ACC14O")
    var acc14o: String? = null

    @Column(name = "ACC150")
    var acc150: Long? = null

    @Column(name = "ACC151")
    var acc151: LocalDate? = null

    @Column(name = "ACC15B")
    var acc15b: String? = null

    @Column(name = "ACC15C")
    var acc15c: String? = null

    @Column(name = "ACC15D")
    var acc15d: LocalDate? = null

    @Column(name = "ACC153")
    var acc153: BigDecimal? = null

    @Column(name = "ACC154")
    var acc154: BigDecimal? = null

    @Column(name = "ACC155")
    var acc155: BigDecimal? = null

    @Column(name = "ACC156")
    var acc156: BigDecimal? = null

    @Column(name = "ACC157")
    var acc157: BigDecimal? = null

    @Column(name = "ACC152")
    var acc152: String? = null

    @Column(name = "ACC159")
    var acc159: String? = null

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

}
