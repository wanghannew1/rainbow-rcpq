package com.jlrc.rcpq.entity

import jakarta.persistence.*
import java.io.Serializable
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "TC93_SS")
open class TC93_SS {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_TC93_SS_ID")
    @SequenceGenerator(name = "SEQ_TC93_SS_ID", sequenceName = "SEQ_TC93_SS_ID", allocationSize = 1)
    @Column(name = "ID")
    var id: Long? = null

    @Column(name = "AAC003")
    var aac003: String? = null

    @Column(name = "AAC002")
    var aac002: String? = null

    @Column(name = "YEAR")
    var year: String? = null

    @Column(name = "MONTH")
    var month: String? = null

    @Column(name = "SSLB")
    var sslb: String? = null

    @Column(name = "SSJE")
    var ssje: BigDecimal? = null

    @Column(name = "AAC001")
    var aac001: Long? = null

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

    @Column(name = "AAE214")
    var aae214: String? = null

}
