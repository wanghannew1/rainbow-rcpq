package com.jlrc.rcpq.entity

import jakarta.persistence.*
import java.io.Serializable
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "TC9N")
open class TC9N {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_TC9N_ATC9N0")
    @SequenceGenerator(name = "SEQ_TC9N_ATC9N0", sequenceName = "SEQ_TC9N_ATC9N0", allocationSize = 1)
    @Column(name = "ATC9N0")
    var atc9n0: Long? = null

    @Column(name = "ATC9L0")
    var atc9l0: Long? = null

    @Column(name = "AAC001")
    var aac001: Long? = null

    @Column(name = "AAC003")
    var aac003: String? = null

    @Column(name = "ATC9N1")
    var atc9n1: String? = null

    @Column(name = "AAC002")
    var aac002: String? = null

    @Column(name = "ATC9N2")
    var atc9n2: String? = null

    @Column(name = "ATC9M1")
    var atc9m1: Long? = null

    @Column(name = "ATC9N3")
    var atc9n3: String? = null

    @Column(name = "AAC180")
    var aac180: String? = null

    @Column(name = "ATC9N4")
    var atc9n4: String? = null

    @Column(name = "AAE00C")
    var aae00c: String? = null

    @Column(name = "ATC9N5")
    var atc9n5: String? = null

    @Column(name = "ATC9N6")
    var atc9n6: String? = null

    @Column(name = "ATC9N7")
    var atc9n7: String? = null

    @Column(name = "AAB001")
    var aab001: Long? = null

    @Column(name = "AAB004")
    var aab004: String? = null

    @Column(name = "ATC9N8")
    var atc9n8: String? = null

    @Column(name = "ATC9N9")
    var atc9n9: String? = null

    @Column(name = "ATC9NA")
    var atc9na: String? = null

    @Column(name = "ATC9NB")
    var atc9nb: LocalDate? = null

    @Column(name = "ATC9NC")
    var atc9nc: String? = null

    @Column(name = "ATC9ND")
    var atc9nd: LocalDate? = null

    @Column(name = "ATC9NE")
    var atc9ne: String? = null

    @Column(name = "ATC9NF")
    var atc9nf: String? = null

    @Column(name = "ATC9NG")
    var atc9ng: String? = null

    @Column(name = "ATC9NH")
    var atc9nh: String? = null

    @Column(name = "ATC9NI")
    var atc9ni: String? = null

    @Column(name = "ATC9NJ")
    var atc9nj: String? = null

    @Column(name = "ATC9NK")
    var atc9nk: String? = null

    @Column(name = "ATC9NL")
    var atc9nl: BigDecimal? = null

    @Column(name = "ATC9NM")
    var atc9nm: String? = null

    @Column(name = "ATC9NN")
    var atc9nn: String? = null

    @Column(name = "ATC9NP")
    var atc9np: String? = null

    @Column(name = "ATC9NQ")
    var atc9nq: String? = null

    @Column(name = "ATC9NR")
    var atc9nr: String? = null

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
