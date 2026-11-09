package com.jlrc.rcpq.entity

import jakarta.persistence.*
import java.io.Serializable
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "TC93_BP")
open class TC93_BP {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_TC93_ID")
    @SequenceGenerator(name = "SEQ_TC93_ID", sequenceName = "SEQ_TC93_ID", allocationSize = 1)
    @Column(name = "ID")
    var id: Long? = null

    @Column(name = "ACE100")
    var ace100: Long? = null

    @Column(name = "AAE002")
    var aae002: Long? = null

    @Column(name = "ATB930")
    var atb930: Long? = null

    @Column(name = "ATC937")
    var atc937: String? = null

    @Column(name = "AAC003")
    var aac003: String? = null

    @Column(name = "AAC002")
    var aac002: String? = null

    @Column(name = "AAB004_1")
    var aab004_1: String? = null

    @Column(name = "ATC908")
    var atc908: String? = null

    @Column(name = "ATC93X")
    var atc93x: String? = null

    @Column(name = "ATC933")
    var atc933: String? = null

    @Column(name = "ATC934")
    var atc934: String? = null

    @Column(name = "ATC935")
    var atc935: String? = null

    @Column(name = "ATC936")
    var atc936: String? = null

    @Column(name = "ATC93K")
    var atc93k: String? = null

    @Column(name = "ATC93L")
    var atc93l: String? = null

    @Column(name = "ATC93M")
    var atc93m: String? = null

    @Column(name = "ATC93Y")
    var atc93y: String? = null

    @Column(name = "ATC93D")
    var atc93d: String? = null

    @Column(name = "ATC93X1")
    var atc93x1: String? = null

    @Column(name = "ATC93X3")
    var atc93x3: String? = null

    @Column(name = "ATC93X5")
    var atc93x5: String? = null

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
