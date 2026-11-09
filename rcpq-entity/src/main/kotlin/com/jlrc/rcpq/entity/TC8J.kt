package com.jlrc.rcpq.entity

import jakarta.persistence.*
import java.io.Serializable
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "TC8J")
open class TC8J {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_TC8J_ATC8J0")
    @SequenceGenerator(name = "SEQ_TC8J_ATC8J0", sequenceName = "SEQ_TC8J_ATC8J0", allocationSize = 1)
    @Column(name = "ATC8J0")
    var atc8j0: Long? = null

    @Column(name = "AAB001")
    var aab001: Long? = null

    @Column(name = "AAB004")
    var aab004: String? = null

    @Column(name = "ATB930")
    var atb930: Long? = null

    @Column(name = "ATB931")
    var atb931: String? = null

    @Column(name = "ATC931")
    var atc931: Long? = null

    @Column(name = "ATC8J1")
    var atc8j1: BigDecimal? = null

    @Column(name = "ATC8J2")
    var atc8j2: BigDecimal? = null

    @Column(name = "ATC8JM")
    var atc8jm: BigDecimal? = null

    @Column(name = "ATC8J3")
    var atc8j3: BigDecimal? = null

    @Column(name = "ATC8J4")
    var atc8j4: BigDecimal? = null

    @Column(name = "ATC8J5")
    var atc8j5: BigDecimal? = null

    @Column(name = "ATC8J6")
    var atc8j6: BigDecimal? = null

    @Column(name = "ATC8J7")
    var atc8j7: BigDecimal? = null

    @Column(name = "ATC8JC")
    var atc8jc: BigDecimal? = null

    @Column(name = "ATC8J8")
    var atc8j8: BigDecimal? = null

    @Column(name = "ATC8J9")
    var atc8j9: BigDecimal? = null

    @Column(name = "ATC8JA")
    var atc8ja: BigDecimal? = null

    @Column(name = "ATC8JD")
    var atc8jd: BigDecimal? = null

    @Column(name = "ATC8JE")
    var atc8je: BigDecimal? = null

    @Column(name = "ATC8JF")
    var atc8jf: BigDecimal? = null

    @Column(name = "ATC8JG")
    var atc8jg: BigDecimal? = null

    @Column(name = "ATC8JH")
    var atc8jh: BigDecimal? = null

    @Column(name = "ATC8JI")
    var atc8ji: BigDecimal? = null

    @Column(name = "ATC8JK")
    var atc8jk: BigDecimal? = null

    @Column(name = "ATC8JL")
    var atc8jl: BigDecimal? = null

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
