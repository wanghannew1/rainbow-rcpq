package com.jlrc.rcpq.entity

import jakarta.persistence.*
import java.io.Serializable
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "TB86")
open class TB86 {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_TB86_ATB860")
    @SequenceGenerator(name = "SEQ_TB86_ATB860", sequenceName = "SEQ_TB86_ATB860", allocationSize = 1)
    @Column(name = "ATB860")
    var atb860: Long? = null

    @Column(name = "AAB001")
    var aab001: Long? = null

    @Column(name = "ATB86Q")
    var atb86q: String? = null

    @Column(name = "ATB862")
    var atb862: String? = null

    @Column(name = "ATB863")
    var atb863: String? = null

    @Column(name = "AAE006")
    var aae006: String? = null

    @Column(name = "AAE007")
    var aae007: String? = null

    @Column(name = "AAE005")
    var aae005: String? = null

    @Column(name = "AAB013")
    var aab013: String? = null

    @Column(name = "ATB864")
    var atb864: String? = null

    @Column(name = "ATB865")
    var atb865: String? = null

    @Column(name = "AAE016")
    var aae016: String? = null

    @Column(name = "AAB049")
    var aab049: BigDecimal? = null

    @Column(name = "ATB866")
    var atb866: String? = null

    @Column(name = "ATB867")
    var atb867: BigDecimal? = null

    @Column(name = "ATB868")
    var atb868: BigDecimal? = null

    @Column(name = "ATB869")
    var atb869: BigDecimal? = null

    @Column(name = "ATB86A")
    var atb86a: BigDecimal? = null

    @Column(name = "ATB86B")
    var atb86b: BigDecimal? = null

    @Column(name = "ATB86C")
    var atb86c: Long? = null

    @Column(name = "ATB86D")
    var atb86d: Long? = null

    @Column(name = "ATB86E")
    var atb86e: Long? = null

    @Column(name = "AAB004")
    var aab004: String? = null

    @Column(name = "ATB86F")
    var atb86f: String? = null

    @Column(name = "AAB020")
    var aab020: String? = null

    @Column(name = "ATB86G")
    var atb86g: String? = null

    @Column(name = "ATB86H")
    var atb86h: String? = null

    @Column(name = "ATB86I")
    var atb86i: String? = null

    @Column(name = "ATB86J")
    var atb86j: String? = null

    @Column(name = "ATB86K")
    var atb86k: LocalDate? = null

    @Column(name = "ATB86L")
    var atb86l: LocalDate? = null

    @Column(name = "ATB86M")
    var atb86m: String? = null

    @Column(name = "ATB86N")
    var atb86n: String? = null

    @Column(name = "ATB86X")
    var atb86x: String? = null

    @Column(name = "ATB86P")
    var atb86p: LocalDate? = null

    @Column(name = "ATB861")
    var atb861: String? = null

    @Column(name = "ATB86R")
    var atb86r: String? = null

    @Column(name = "ATB86S")
    var atb86s: String? = null

    @Column(name = "ATB86T")
    var atb86t: String? = null

    @Column(name = "ATB86U")
    var atb86u: LocalDate? = null

    @Column(name = "ATB86V")
    var atb86v: String? = null

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

    @Column(name = "AAE022")
    var aae022: String? = null

    @Column(name = "BAZ001")
    var baz001: Long? = null

    @Column(name = "BAZ002")
    var baz002: Long? = null

    @Column(name = "AAE100")
    var aae100: String? = null

    @Column(name = "ATB86W")
    var atb86w: String? = null

    @Column(name = "AAEG11")
    var aaeg11: String? = null

    @Column(name = "AAEG17")
    var aaeg17: String? = null

    @Column(name = "AAEG36")
    var aaeg36: LocalDate? = null

    @Column(name = "AAEG19")
    var aaeg19: String? = null

    @Column(name = "AAEG20")
    var aaeg20: String? = null

    @Column(name = "AAEG22")
    var aaeg22: String? = null

    @Column(name = "ATB86Y")
    var atb86y: String? = null

    @Column(name = "AAEH11")
    var aaeh11: String? = null

    @Column(name = "AAEH17")
    var aaeh17: String? = null

    @Column(name = "AAEH36")
    var aaeh36: LocalDate? = null

    @Column(name = "AAEH19")
    var aaeh19: String? = null

    @Column(name = "AAEH20")
    var aaeh20: String? = null

    @Column(name = "AAEH22")
    var aaeh22: String? = null

}
