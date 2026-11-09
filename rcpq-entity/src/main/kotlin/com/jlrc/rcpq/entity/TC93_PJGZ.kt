package com.jlrc.rcpq.entity

import jakarta.persistence.*
import java.io.Serializable
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "TC93_PJGZ")
open class TC93_PJGZ {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_TC93_PJGZ_ID")
    @SequenceGenerator(name = "SEQ_TC93_PJGZ_ID", sequenceName = "SEQ_TC93_PJGZ_ID", allocationSize = 1)
    @Column(name = "ID")
    var id: Long? = null

    @Column(name = "BAZ001")
    var baz001: Long? = null

    @Column(name = "BAZ002")
    var baz002: Long? = null

    @Column(name = "ATB900")
    var atb900: Long? = null

    @Column(name = "ATC900")
    var atc900: Long? = null

    @Column(name = "AAB001")
    var aab001: Long? = null

    @Column(name = "AAB004")
    var aab004: String? = null

    @Column(name = "ATB930")
    var atb930: Long? = null

    @Column(name = "ATB931")
    var atb931: String? = null

    @Column(name = "AAC003")
    var aac003: String? = null

    @Column(name = "AAC002")
    var aac002: String? = null

    @Column(name = "DBEGIN")
    var dbegin: Long? = null

    @Column(name = "DEND")
    var dend: Long? = null

    @Column(name = "PJGZ")
    var pjgz: BigDecimal? = null

    @Column(name = "GZZE")
    var gzze: BigDecimal? = null

    @Column(name = "GZYS")
    var gzys: Long? = null

}
