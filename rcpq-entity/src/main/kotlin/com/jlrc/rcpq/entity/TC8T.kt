package com.jlrc.rcpq.entity

import jakarta.persistence.*
import java.io.Serializable
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "TC8T")
open class TC8T {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_TC8T_ATC8T0")
    @SequenceGenerator(name = "SEQ_TC8T_ATC8T0", sequenceName = "SEQ_TC8T_ATC8T0", allocationSize = 1)
    @Column(name = "ATC8T0")
    var atc8t0: Long? = null

    @Column(name = "ATB930")
    var atb930: Long? = null

    @Column(name = "ATB931")
    var atb931: String? = null

    @Column(name = "AAB001")
    var aab001: Long? = null

    @Column(name = "AAB004")
    var aab004: String? = null

    @Column(name = "ATC8T1")
    var atc8t1: BigDecimal? = null

    @Column(name = "ATC8T2")
    var atc8t2: BigDecimal? = null

    @Column(name = "ATC8T3")
    var atc8t3: BigDecimal? = null

    @Column(name = "ATC8T4")
    var atc8t4: LocalDate? = null

    @Column(name = "ATC8T5")
    var atc8t5: String? = null

    @Column(name = "ATC8T6")
    var atc8t6: String? = null

    @Column(name = "ATC8T7")
    var atc8t7: String? = null

    @Column(name = "AAE114")
    var aae114: String? = null

    @Column(name = "AAE111")
    var aae111: String? = null

    @Column(name = "AAE117")
    var aae117: String? = null

    @Column(name = "AAE136")
    var aae136: LocalDate? = null

    @Column(name = "AAE119")
    var aae119: String? = null

    @Column(name = "AAE120")
    var aae120: String? = null

    @Column(name = "AAE122")
    var aae122: String? = null

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

}
