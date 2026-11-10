package com.jlrc.rcpq.entity

import jakarta.persistence.*
import java.io.Serializable
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "TC8I_LIST")
open class TC8I_LIST {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_TC8I_LIST_ID")
    @SequenceGenerator(name = "SEQ_TC8I_LIST_ID", sequenceName = "SEQ_TC8I_LIST_ID", allocationSize = 1)
    @Column(name = "ID")
    var id: Long? = null

    @Column(name = "ATC8I0")
    var atc8i0: Long? = null

    @Column(name = "ID_1")
    var id_1: Long? = null

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

    @Column(name = "MONEY")
    var money: BigDecimal? = null
}
