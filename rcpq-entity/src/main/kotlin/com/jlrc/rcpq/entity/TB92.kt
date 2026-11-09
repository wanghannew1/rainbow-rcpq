package com.jlrc.rcpq.entity

import jakarta.persistence.*
import java.io.Serializable
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "TB92")
open class TB92 {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_TB92_ATB920")
    @SequenceGenerator(name = "SEQ_TB92_ATB920", sequenceName = "SEQ_TB92_ATB920", allocationSize = 1)
    @Column(name = "ATB920")
    var atb920: Long? = null

    @Column(name = "BAZ001")
    var baz001: Long? = null

    @Column(name = "BAZ002")
    var baz002: Long? = null

    @Column(name = "ATB900")
    var atb900: Long? = null

    @Column(name = "ATB921")
    var atb921: LocalDate? = null

    @Column(name = "ATB904")
    var atb904: String? = null

    @Column(name = "ATB904_")
    var atb904_: String? = null

    @Column(name = "ATB905")
    var atb905: LocalDate? = null

    @Column(name = "ATB905_")
    var atb905_: LocalDate? = null

    @Column(name = "ATB906")
    var atb906: LocalDate? = null

    @Column(name = "ATB906_")
    var atb906_: LocalDate? = null

    @Column(name = "ATB907")
    var atb907: LocalDate? = null

    @Column(name = "ATB907_")
    var atb907_: LocalDate? = null

    @Column(name = "ATB90A")
    var atb90a: BigDecimal? = null

    @Column(name = "ATB90A_")
    var atb90a_: BigDecimal? = null

    @Column(name = "ATB90B")
    var atb90b: BigDecimal? = null

    @Column(name = "ATB90B_")
    var atb90b_: BigDecimal? = null

    @Column(name = "ATB90C")
    var atb90c: String? = null

    @Column(name = "ATB90C_")
    var atb90c_: String? = null

    @Column(name = "ATB90D")
    var atb90d: String? = null

    @Column(name = "ATB90D_")
    var atb90d_: String? = null

    @Column(name = "ATB90E")
    var atb90e: String? = null

    @Column(name = "ATB90E_")
    var atb90e_: String? = null

    @Column(name = "ATB90F")
    var atb90f: String? = null

    @Column(name = "ATB90F_")
    var atb90f_: String? = null

    @Column(name = "ATB90G")
    var atb90g: String? = null

    @Column(name = "ATB90G_")
    var atb90g_: String? = null

    @Column(name = "ATB90H")
    var atb90h: String? = null

    @Column(name = "ATB90H_")
    var atb90h_: String? = null

    @Column(name = "ATB90I")
    var atb90i: String? = null

    @Column(name = "ATB90I_")
    var atb90i_: String? = null

    @Column(name = "ATB90J")
    var atb90j: String? = null

    @Column(name = "ATB90J_")
    var atb90j_: String? = null

    @Column(name = "AAB301")
    var aab301: String? = null

    @Column(name = "AAB301_")
    var aab301_: String? = null

    @Column(name = "AAA021")
    var aaa021: String? = null

    @Column(name = "AAA021_")
    var aaa021_: String? = null

    @Column(name = "ATB90K")
    var atb90k: BigDecimal? = null

    @Column(name = "ATB90K_")
    var atb90k_: BigDecimal? = null

    @Column(name = "ATB90L")
    var atb90l: BigDecimal? = null

    @Column(name = "ATB90L_")
    var atb90l_: BigDecimal? = null

    @Column(name = "ATB90M")
    var atb90m: BigDecimal? = null

    @Column(name = "ATB90M_")
    var atb90m_: BigDecimal? = null

    @Column(name = "ATB90N")
    var atb90n: BigDecimal? = null

    @Column(name = "ATB90N_")
    var atb90n_: BigDecimal? = null

    @Column(name = "ATB90O")
    var atb90o: BigDecimal? = null

    @Column(name = "ATB90O_")
    var atb90o_: BigDecimal? = null

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

    @Column(name = "ATB910")
    var atb910: Long? = null

    @Column(name = "ATB90Y")
    var atb90y: String? = null

    @Column(name = "ATB90Y_")
    var atb90y_: String? = null

    @Column(name = "ATB90Q")
    var atb90q: String? = null

    @Column(name = "ATB90Q_")
    var atb90q_: String? = null

    @Column(name = "ATB90Z")
    var atb90z: BigDecimal? = null

    @Column(name = "ATB90Z_")
    var atb90z_: BigDecimal? = null

    @Column(name = "ATB90W")
    var atb90w: String? = null

    @Column(name = "ATB90W_")
    var atb90w_: String? = null

    @Column(name = "ATB90S")
    var atb90s: BigDecimal? = null

    @Column(name = "ATB90S_")
    var atb90s_: BigDecimal? = null

    @Column(name = "ATB90AA")
    var atb90aa: String? = null

    @Column(name = "ATB90AA_")
    var atb90aa_: String? = null

    @Column(name = "ATB90AB")
    var atb90ab: Long? = null

    @Column(name = "ATB90AB_")
    var atb90ab_: Long? = null

    @Column(name = "AAB004")
    var aab004: String? = null

    @Column(name = "AAB004_")
    var aab004_: String? = null

}
