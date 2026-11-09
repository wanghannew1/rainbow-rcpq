package com.jlrc.rcpq.entity

import jakarta.persistence.*
import java.io.Serializable
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "TJCA3")
open class TJCA3 {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_TJCA3_TJCA30")
    @SequenceGenerator(name = "SEQ_TJCA3_TJCA30", sequenceName = "SEQ_TJCA3_TJCA30", allocationSize = 1)
    @Column(name = "TJCA30")
    var tjca30: Long? = null

    @Column(name = "BAZ001")
    var baz001: Long? = null

    @Column(name = "BAZ002")
    var baz002: Long? = null

    @Column(name = "TJ0000")
    var tj0000: Long? = null

    @Column(name = "AAA020")
    var aaa020: String? = null

    @Column(name = "AAA021")
    var aaa021: String? = null

    @Column(name = "AAE001")
    var aae001: Long? = null

    @Column(name = "AAE00A")
    var aae00a: Long? = null

    @Column(name = "TJCA31")
    var tjca31: Long? = null

    @Column(name = "TJCA32")
    var tjca32: Long? = null

    @Column(name = "TJCA33")
    var tjca33: Long? = null

    @Column(name = "TJCA34")
    var tjca34: Long? = null

    @Column(name = "TJCA35")
    var tjca35: Long? = null

    @Column(name = "TJCA36")
    var tjca36: Long? = null

    @Column(name = "TJCA37")
    var tjca37: Long? = null

    @Column(name = "TJCA38")
    var tjca38: Long? = null

    @Column(name = "TJCA39")
    var tjca39: Long? = null

    @Column(name = "TJCA3A")
    var tjca3a: Long? = null

    @Column(name = "TJCA3B")
    var tjca3b: Long? = null

    @Column(name = "TJCA3C")
    var tjca3c: Long? = null

    @Column(name = "TJCA3D")
    var tjca3d: Long? = null

    @Column(name = "TJCA3E")
    var tjca3e: Long? = null

    @Column(name = "TJCA3F")
    var tjca3f: Long? = null

    @Column(name = "TJCA3G")
    var tjca3g: Long? = null

    @Column(name = "TJCA3H")
    var tjca3h: Long? = null

    @Column(name = "TJCA3I")
    var tjca3i: Long? = null

    @Column(name = "TJCA3J")
    var tjca3j: Long? = null

    @Column(name = "TJCA3K")
    var tjca3k: Long? = null

    @Column(name = "TJCAA1")
    var tjcaa1: Long? = null

    @Column(name = "TJCAA2")
    var tjcaa2: Long? = null

    @Column(name = "TJCAA3")
    var tjcaa3: Long? = null

    @Column(name = "TJCAA4")
    var tjcaa4: Long? = null

    @Column(name = "TJCAA5")
    var tjcaa5: Long? = null

    @Column(name = "TJCAA6")
    var tjcaa6: Long? = null

    @Column(name = "TJCAA7")
    var tjcaa7: Long? = null

    @Column(name = "TJCAA8")
    var tjcaa8: Long? = null

    @Column(name = "TJCAA9")
    var tjcaa9: Long? = null

    @Column(name = "TJCAAA")
    var tjcaaa: Long? = null

    @Column(name = "TJCAAB")
    var tjcaab: Long? = null

    @Column(name = "TJCAAC")
    var tjcaac: Long? = null

    @Column(name = "TJCAAD")
    var tjcaad: Long? = null

    @Column(name = "TJCAAE")
    var tjcaae: Long? = null

    @Column(name = "TJCAAF")
    var tjcaaf: Long? = null

    @Column(name = "TJCAAG")
    var tjcaag: Long? = null

    @Column(name = "TJCAAH")
    var tjcaah: Long? = null

    @Column(name = "TJCAAI")
    var tjcaai: Long? = null

    @Column(name = "TJCAAJ")
    var tjcaaj: Long? = null

    @Column(name = "TJCAAK")
    var tjcaak: Long? = null

    @Column(name = "TJCAB1")
    var tjcab1: Long? = null

    @Column(name = "TJCAB2")
    var tjcab2: Long? = null

    @Column(name = "TJCAB3")
    var tjcab3: Long? = null

    @Column(name = "TJCAB4")
    var tjcab4: Long? = null

    @Column(name = "TJCAB5")
    var tjcab5: Long? = null

    @Column(name = "TJCAB6")
    var tjcab6: Long? = null

    @Column(name = "TJCAB7")
    var tjcab7: Long? = null

    @Column(name = "TJCAB8")
    var tjcab8: Long? = null

    @Column(name = "TJCAB9")
    var tjcab9: Long? = null

    @Column(name = "TJCABA")
    var tjcaba: Long? = null

    @Column(name = "TJCABB")
    var tjcabb: Long? = null

    @Column(name = "TJCABC")
    var tjcabc: Long? = null

    @Column(name = "TJCABD")
    var tjcabd: Long? = null

    @Column(name = "TJCABE")
    var tjcabe: Long? = null

    @Column(name = "TJCABF")
    var tjcabf: Long? = null

    @Column(name = "TJCABG")
    var tjcabg: Long? = null

    @Column(name = "TJCABH")
    var tjcabh: Long? = null

    @Column(name = "TJCABI")
    var tjcabi: Long? = null

    @Column(name = "TJCABJ")
    var tjcabj: Long? = null

    @Column(name = "TJCABK")
    var tjcabk: Long? = null

    @Column(name = "AAE013")
    var aae013: String? = null

    @Column(name = "TJ001M")
    var tj001m: String? = null

    @Column(name = "TJ001N")
    var tj001n: String? = null

    @Column(name = "TJ001P")
    var tj001p: String? = null

    @Column(name = "TJ001Q")
    var tj001q: String? = null

    @Column(name = "TJ001R")
    var tj001r: LocalDateTime? = null

    @Column(name = "TJ000X")
    var tj000x: String? = null

    @Column(name = "TJ000W")
    var tj000w: String? = null

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
