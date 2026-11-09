package com.jlrc.rcpq.entity

import jakarta.persistence.*
import java.io.Serializable
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "AC01_GX")
open class AC01_GX {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_AC01_GX_ID")
    @SequenceGenerator(name = "SEQ_AC01_GX_ID", sequenceName = "SEQ_AC01_GX_ID", allocationSize = 1)
    @Column(name = "ID")
    var id: Long? = null

    @Column(name = "ACE100")
    var ace100: Long? = null

    @Column(name = "ACE10A")
    var ace10a: String? = null

    @Column(name = "ACE10B")
    var ace10b: String? = null

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

    @Column(name = "AAC002")
    var aac002: String? = null

    @Column(name = "AAC003")
    var aac003: String? = null

    @Column(name = "ACC030")
    var acc030: String? = null

    @Column(name = "ACC0M1")
    var acc0m1: String? = null

    @Column(name = "AAE00N")
    var aae00n: String? = null

    @Column(name = "AAE007")
    var aae007: String? = null

    @Column(name = "AAE00D")
    var aae00d: String? = null

    @Column(name = "AAE00A")
    var aae00a: String? = null

    @Column(name = "AAE00H")
    var aae00h: String? = null

    @Column(name = "AAE00I")
    var aae00i: String? = null

    @Column(name = "ATC90AQ")
    var atc90aq: String? = null

    @Column(name = "AAC001")
    var aac001: Long? = null

    @Column(name = "ATC900")
    var atc900: Long? = null

}
