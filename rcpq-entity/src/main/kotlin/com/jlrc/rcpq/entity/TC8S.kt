package com.jlrc.rcpq.entity

import jakarta.persistence.*
import java.io.Serializable
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "TC8S")
open class TC8S {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_TC8S_ATC8S0")
    @SequenceGenerator(name = "SEQ_TC8S_ATC8S0", sequenceName = "SEQ_TC8S_ATC8S0", allocationSize = 1)
    @Column(name = "ATC8S0")
    var atc8s0: Long? = null

    @Column(name = "ATC900")
    var atc900: Long? = null

    @Column(name = "AAC001")
    var aac001: Long? = null

    @Column(name = "AAC003")
    var aac003: String? = null

    @Column(name = "AAC002")
    var aac002: String? = null

    @Column(name = "AAC004")
    var aac004: String? = null

    @Column(name = "AAC006")
    var aac006: LocalDate? = null

    @Column(name = "AAC024")
    var aac024: String? = null

    @Column(name = "AAC011")
    var aac011: String? = null

    @Column(name = "AAE005")
    var aae005: String? = null

    @Column(name = "AAE006")
    var aae006: String? = null

    @Column(name = "AAE006_1")
    var aae006_1: String? = null

    @Column(name = "AAE006_2")
    var aae006_2: String? = null

    @Column(name = "AAE006_3")
    var aae006_3: String? = null

    @Column(name = "AAE006_4")
    var aae006_4: String? = null

    @Column(name = "AAE006_5")
    var aae006_5: String? = null

    @Column(name = "AAE006_6")
    var aae006_6: String? = null

    @Column(name = "AAE008")
    var aae008: Long? = null

    @Column(name = "CAB305")
    var cab305: String? = null

    @Column(name = "CAB305_1")
    var cab305_1: String? = null

    @Column(name = "CAB305_2")
    var cab305_2: String? = null

    @Column(name = "CAB305_3")
    var cab305_3: String? = null

    @Column(name = "CAB305_4")
    var cab305_4: String? = null

    @Column(name = "CAB305_5")
    var cab305_5: String? = null

    @Column(name = "CAB305_6")
    var cab305_6: String? = null

    @Column(name = "AAE008_1")
    var aae008_1: Long? = null

    @Column(name = "ATC8S1")
    var atc8s1: String? = null

    @Column(name = "ATC8S2")
    var atc8s2: String? = null

    @Column(name = "ATC8S3")
    var atc8s3: String? = null

    @Column(name = "AAB004")
    var aab004: String? = null

    @Column(name = "AAB004_1")
    var aab004_1: String? = null

    @Column(name = "ATC8S4")
    var atc8s4: LocalDate? = null

    @Column(name = "ATC8S5")
    var atc8s5: String? = null

    @Column(name = "ATC8S6")
    var atc8s6: String? = null

    @Column(name = "ATC8S7")
    var atc8s7: String? = null

    @Column(name = "ATC8S8")
    var atc8s8: String? = null

    @Column(name = "ATC8S9")
    var atc8s9: String? = null

    @Column(name = "ATC8SB")
    var atc8sb: String? = null

    @Column(name = "ATC8SC")
    var atc8sc: String? = null

    @Column(name = "ATC8SD")
    var atc8sd: String? = null

    @Column(name = "ATC8SE")
    var atc8se: String? = null

    @Column(name = "ATC8SF")
    var atc8sf: String? = null

    @Column(name = "ATC8SG")
    var atc8sg: String? = null

    @Column(name = "ATC8SH")
    var atc8sh: String? = null

    @Column(name = "ATC8SI")
    var atc8si: String? = null

    @Column(name = "ATC8SJ")
    var atc8sj: Long? = null

    @Column(name = "AAE00M")
    var aae00m: String? = null

    @Column(name = "AAA031")
    var aaa031: String? = null

    @Column(name = "AAA032")
    var aaa032: String? = null

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

    @Column(name = "ATC97A")
    var atc97a: String? = null

    @Column(name = "AAE211")
    var aae211: String? = null

    @Column(name = "AAE217")
    var aae217: String? = null

    @Column(name = "AAE236")
    var aae236: LocalDate? = null

    @Column(name = "AAE219")
    var aae219: String? = null

    @Column(name = "AAE220")
    var aae220: String? = null

    @Column(name = "AAE222")
    var aae222: String? = null

    @Column(name = "ATC8SK")
    var atc8sk: BigDecimal? = null

    @Column(name = "ATB90A")
    var atb90a: BigDecimal? = null

    @Column(name = "AAE002")
    var aae002: Long? = null

    @Column(name = "ATC93G")
    var atc93g: String? = null

    @Column(name = "ATB930")
    var atb930: Long? = null

}
