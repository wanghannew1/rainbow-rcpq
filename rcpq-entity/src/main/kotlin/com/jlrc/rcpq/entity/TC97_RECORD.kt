package com.jlrc.rcpq.entity

import jakarta.persistence.*
import java.io.Serializable
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "TC97_RECORD")
open class TC97_RECORD {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_TC97_RECORD_ID")
    @SequenceGenerator(name = "SEQ_TC97_RECORD_ID", sequenceName = "SEQ_TC97_RECORD_ID", allocationSize = 1)
    @Column(name = "ID")
    var id: Long? = null

    @Column(name = "ATC970")
    var atc970: Long? = null

    @Column(name = "AAC001")
    var aac001: Long? = null

    @Column(name = "TYPE")
    var type: String? = null

    @Column(name = "XH")
    var xh: Long? = null

    @Column(name = "RENURE")
    var renure: LocalDate? = null

    @Column(name = "DEPARTURE")
    var departure: LocalDate? = null

    @Column(name = "DEPARTMENT")
    var department: String? = null

    @Column(name = "JOB")
    var job: String? = null

    @Column(name = "PZWDH")
    var pzwdh: String? = null

    @Column(name = "NAME")
    var name: String? = null

    @Column(name = "ATC970R")
    var atc970r: String? = null

    @Column(name = "XCSJ")
    var xcsj: LocalDate? = null

    @Column(name = "PAGENO")
    var pageno: Long? = null

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
