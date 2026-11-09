package com.jlrc.rcpq.entity

import jakarta.persistence.*
import java.io.Serializable
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "TC93_LZ")
open class TC93_LZ {
    @Id
    @Column(name = "ID")
    var id: Long? = null

    @Column(name = "ATC900")
    var atc900: Long? = null

    @Column(name = "ATB930")
    var atb930: Long? = null

    @Column(name = "ATC931")
    var atc931: Long? = null

    @Column(name = "ATC932")
    var atc932: Long? = null

    @Column(name = "ATC937")
    var atc937: String? = null

    @Column(name = "AAC003")
    var aac003: String? = null

    @Column(name = "AAC002")
    var aac002: String? = null

    @Column(name = "ATB90A")
    var atb90a: BigDecimal? = null

    @Column(name = "ATB90Z")
    var atb90z: BigDecimal? = null

    @Column(name = "ATC93X")
    var atc93x: BigDecimal? = null

    @Column(name = "ATC933")
    var atc933: BigDecimal? = null

    @Column(name = "ATC934")
    var atc934: BigDecimal? = null

    @Column(name = "ATC935")
    var atc935: BigDecimal? = null

    @Column(name = "ATC936")
    var atc936: BigDecimal? = null

    @Column(name = "ATC93K")
    var atc93k: BigDecimal? = null

    @Column(name = "ATC93L")
    var atc93l: BigDecimal? = null

    @Column(name = "ATC93M")
    var atc93m: BigDecimal? = null

    @Column(name = "ATC93Y")
    var atc93y: BigDecimal? = null

    @Column(name = "ATC938")
    var atc938: String? = null

    @Column(name = "ATC939")
    var atc939: String? = null

    @Column(name = "ATC93A")
    var atc93a: String? = null

    @Column(name = "ATC93B")
    var atc93b: String? = null

    @Column(name = "ATC93H")
    var atc93h: String? = null

    @Column(name = "ATC93I")
    var atc93i: String? = null

    @Column(name = "ATC93J")
    var atc93j: String? = null

    @Column(name = "ATC93Z")
    var atc93z: String? = null

    @Column(name = "ATC93AA")
    var atc93aa: BigDecimal? = null

    @Column(name = "ATC93AB")
    var atc93ab: BigDecimal? = null

    @Column(name = "ATC93D")
    var atc93d: BigDecimal? = null

    @Column(name = "ATC93AC")
    var atc93ac: BigDecimal? = null

    @Column(name = "ATC93AD")
    var atc93ad: BigDecimal? = null

    @Column(name = "ATC93C")
    var atc93c: BigDecimal? = null

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

    @Column(name = "AAE002")
    var aae002: Long? = null

    @Column(name = "AAE001")
    var aae001: Long? = null

    @Column(name = "ACE10A")
    var ace10a: String? = null

    @Column(name = "ACE10B")
    var ace10b: String? = null

    @Column(name = "ACE100")
    var ace100: Long? = null

}
