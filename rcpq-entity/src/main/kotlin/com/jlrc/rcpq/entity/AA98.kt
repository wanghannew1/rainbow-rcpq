package com.jlrc.rcpq.entity

import jakarta.persistence.*
import java.io.Serializable
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "AA98")
open class AA98 {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_AA98_ID")
    @SequenceGenerator(name = "SEQ_AA98_ID", sequenceName = "SEQ_AA98_ID", allocationSize = 1)
    @Column(name = "ID")
    var id: Long? = null

    @Column(name = "AAA981")
    var aaa981: String? = null

    @Column(name = "AAA982")
    var aaa982: BigDecimal? = null

    @Column(name = "AAA983")
    var aaa983: BigDecimal? = null

    @Column(name = "AAA984")
    var aaa984: BigDecimal? = null

    @Column(name = "AAA985")
    var aaa985: BigDecimal? = null

    @Column(name = "AAA986")
    var aaa986: BigDecimal? = null

    @Column(name = "AAA987")
    var aaa987: Long? = null

    @Column(name = "AAA988")
    var aaa988: Long? = null

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
