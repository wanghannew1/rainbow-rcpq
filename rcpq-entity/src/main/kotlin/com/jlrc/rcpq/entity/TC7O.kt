package com.jlrc.rcpq.entity

import jakarta.persistence.*
import java.io.Serializable
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "TC7O")
open class TC7O {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_TC7O_ATC7O0")
    @SequenceGenerator(name = "SEQ_TC7O_ATC7O0", sequenceName = "SEQ_TC7O_ATC7O0", allocationSize = 1)
    @Column(name = "ATC7O0")
    var atc7o0: Long? = null

    @Column(name = "BAZ001")
    var baz001: Long? = null

    @Column(name = "BAZ002")
    var baz002: Long? = null

    @Column(name = "ATC702")
    var atc702: String? = null

    @Column(name = "ATC7O1")
    var atc7o1: String? = null

    @Column(name = "ATB705")
    var atb705: String? = null

    @Column(name = "AAC003")
    var aac003: String? = null

    @Column(name = "AAC002")
    var aac002: String? = null

    @Column(name = "AAC004")
    var aac004: String? = null

    @Column(name = "AAC005")
    var aac005: String? = null

    @Column(name = "ATC7O2")
    var atc7o2: String? = null

    @Column(name = "ATC7O3")
    var atc7o3: String? = null

    @Column(name = "ATC7O4")
    var atc7o4: BigDecimal? = null

    @Column(name = "ATC7O5")
    var atc7o5: BigDecimal? = null

    @Column(name = "ATC7O6")
    var atc7o6: String? = null

    @Column(name = "ATC7O7")
    var atc7o7: String? = null

    @Column(name = "ATC7O8")
    var atc7o8: String? = null

    @Column(name = "ATC7O9")
    var atc7o9: BigDecimal? = null

    @Column(name = "ATC7OA")
    var atc7oa: String? = null

    @Column(name = "ATC7OB")
    var atc7ob: String? = null

    @Column(name = "ATC7OC")
    var atc7oc: String? = null

    @Column(name = "ATC7OD")
    var atc7od: BigDecimal? = null

    @Column(name = "ATC7OE")
    var atc7oe: BigDecimal? = null

    @Column(name = "AAE013")
    var aae013: String? = null

    @Column(name = "ATC7OF")
    var atc7of: BigDecimal? = null

    @Column(name = "ATC7OG")
    var atc7og: BigDecimal? = null

    @Column(name = "AAE006")
    var aae006: String? = null

    @Column(name = "AAE007")
    var aae007: String? = null

    @Column(name = "ATC7OH")
    var atc7oh: LocalDate? = null

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

    @Column(name = "AAE100")
    var aae100: String? = null

    @Column(name = "ATC7OI")
    var atc7oi: String? = null

    @Column(name = "ATC7OJ")
    var atc7oj: String? = null

    @Column(name = "ATC701")
    var atc701: String? = null

    @Column(name = "ATC7OK")
    var atc7ok: String? = null

    @Column(name = "ATC7OL")
    var atc7ol: String? = null

    @Column(name = "ATC7OM")
    var atc7om: String? = null

    @Column(name = "ATC7ON")
    var atc7on: String? = null

    @Column(name = "ATC7OP")
    var atc7op: BigDecimal? = null

    @Column(name = "ATC7OQ")
    var atc7oq: String? = null

    @Column(name = "AAC006")
    var aac006: LocalDate? = null

    @Column(name = "ATC7OR")
    var atc7or: String? = null

}
