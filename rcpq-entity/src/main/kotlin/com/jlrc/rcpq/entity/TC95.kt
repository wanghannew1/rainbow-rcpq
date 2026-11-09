package com.jlrc.rcpq.entity

import jakarta.persistence.*
import java.io.Serializable
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "TC95")
open class TC95 {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_TC95_ATC950")
    @SequenceGenerator(name = "SEQ_TC95_ATC950", sequenceName = "SEQ_TC95_ATC950", allocationSize = 1)
    @Column(name = "ATC950")
    var atc950: Long? = null

    @Column(name = "BAZ001")
    var baz001: Long? = null

    @Column(name = "BAZ002")
    var baz002: Long? = null

    @Column(name = "ATB930")
    var atb930: Long? = null

    @Column(name = "ATB931")
    var atb931: String? = null

    @Column(name = "AAE002")
    var aae002: Long? = null

    @Column(name = "ATC937")
    var atc937: String? = null

    @Column(name = "AAC003")
    var aac003: String? = null

    @Column(name = "AAC002")
    var aac002: String? = null

    @Column(name = "ATC93AA")
    var atc93aa: String? = null

    @Column(name = "ATC936")
    var atc936: String? = null

    @Column(name = "ATC93K")
    var atc93k: String? = null

    @Column(name = "ATC93D")
    var atc93d: String? = null

    @Column(name = "ATC93AC")
    var atc93ac: String? = null

    @Column(name = "ATC93X1")
    var atc93x1: String? = null

    @Column(name = "ATC93X3")
    var atc93x3: String? = null

    @Column(name = "ATC93X5")
    var atc93x5: String? = null

    @Column(name = "A1")
    var a1: String? = null

    @Column(name = "A2")
    var a2: String? = null

    @Column(name = "A3")
    var a3: String? = null

    @Column(name = "A4")
    var a4: String? = null

    @Column(name = "A5")
    var a5: String? = null

    @Column(name = "A6")
    var a6: String? = null

    @Column(name = "A7")
    var a7: String? = null

    @Column(name = "A8")
    var a8: String? = null

    @Column(name = "A9")
    var a9: String? = null

    @Column(name = "A10")
    var a10: String? = null

    @Column(name = "A11")
    var a11: String? = null

    @Column(name = "A12")
    var a12: String? = null

    @Column(name = "A13")
    var a13: String? = null

    @Column(name = "A14")
    var a14: String? = null

    @Column(name = "A15")
    var a15: String? = null

    @Column(name = "A16")
    var a16: String? = null

    @Column(name = "A17")
    var a17: String? = null

    @Column(name = "A18")
    var a18: String? = null

    @Column(name = "A19")
    var a19: String? = null

    @Column(name = "A20")
    var a20: String? = null

    @Column(name = "A21")
    var a21: String? = null

    @Column(name = "A22")
    var a22: String? = null

    @Column(name = "A23")
    var a23: String? = null

    @Column(name = "A24")
    var a24: String? = null

    @Column(name = "A25")
    var a25: String? = null

    @Column(name = "A26")
    var a26: String? = null

    @Column(name = "A27")
    var a27: String? = null

    @Column(name = "A28")
    var a28: String? = null

    @Column(name = "A29")
    var a29: String? = null

    @Column(name = "A30")
    var a30: String? = null

    @Column(name = "A31")
    var a31: String? = null

    @Column(name = "A32")
    var a32: String? = null

    @Column(name = "A33")
    var a33: String? = null

    @Column(name = "A34")
    var a34: String? = null

    @Column(name = "A35")
    var a35: String? = null

    @Column(name = "A36")
    var a36: String? = null

    @Column(name = "A37")
    var a37: String? = null

    @Column(name = "A38")
    var a38: String? = null

    @Column(name = "A39")
    var a39: String? = null

    @Column(name = "A40")
    var a40: String? = null

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
