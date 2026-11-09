package com.jlrc.rcpq.entity

import jakarta.persistence.*
import java.io.Serializable
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "TC92_LIST")
open class TC92_LIST {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_TC92_LIST_ID")
    @SequenceGenerator(name = "SEQ_TC92_LIST_ID", sequenceName = "SEQ_TC92_LIST_ID", allocationSize = 1)
    @Column(name = "ID")
    var id: Long? = null

    @Column(name = "ATC900")
    var atc900: Long? = null

    @Column(name = "ATC920")
    var atc920: Long? = null

    @Column(name = "BGSJ")
    var bgsj: LocalDate? = null

    @Column(name = "BGZD")
    var bgzd: String? = null

    @Column(name = "BGQ")
    var bgq: String? = null

    @Column(name = "BGH")
    var bgh: String? = null

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
