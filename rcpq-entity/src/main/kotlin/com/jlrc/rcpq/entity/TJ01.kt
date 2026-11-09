package com.jlrc.rcpq.entity

import jakarta.persistence.*
import java.io.Serializable
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "TJ01")
open class TJ01 {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_TJ01_ID")
    @SequenceGenerator(name = "SEQ_TJ01_ID", sequenceName = "SEQ_TJ01_ID", allocationSize = 1)
    @Column(name = "ID")
    var id: Long? = null

    @Column(name = "ATC900")
    var atc900: Long? = null

    @Column(name = "ATB930")
    var atb930: Long? = null

    @Column(name = "ATB900")
    var atb900: Long? = null

    @Column(name = "AAB001")
    var aab001: Long? = null

    @Column(name = "AAB004")
    var aab004: String? = null

    @Column(name = "ATB931")
    var atb931: String? = null

    @Column(name = "AAC001")
    var aac001: Long? = null

    @Column(name = "AAC003")
    var aac003: String? = null

    @Column(name = "AAC002")
    var aac002: String? = null

    @Column(name = "FLAG")
    var flag: String? = null

    @Column(name = "TJ010")
    var tj010: BigDecimal? = null

    @Column(name = "TJ011")
    var tj011: BigDecimal? = null

    @Column(name = "TJ012")
    var tj012: BigDecimal? = null

    @Column(name = "AAE002")
    var aae002: Long? = null

    @Column(name = "AAE00B")
    var aae00b: String? = null

}
