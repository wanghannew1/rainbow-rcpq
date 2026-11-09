package com.jlrc.rcpq.entity

import jakarta.persistence.*
import java.io.Serializable
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "AA97_DW")
open class AA97_DW {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_AA97_DW_FID")
    @SequenceGenerator(name = "SEQ_AA97_DW_FID", sequenceName = "SEQ_AA97_DW_FID", allocationSize = 1)
    @Column(name = "FID")
    var fid: Long? = null

    @Column(name = "BAZ001")
    var baz001: Long? = null

    @Column(name = "BAZ002")
    var baz002: Long? = null

    @Column(name = "AAE001")
    var aae001: Long? = null

    @Column(name = "ATB930")
    var atb930: Long? = null

    @Column(name = "ATB931")
    var atb931: String? = null

    @Column(name = "AAB001")
    var aab001: Long? = null

    @Column(name = "AAB004")
    var aab004: String? = null

    @Column(name = "AAA100")
    var aaa100: String? = null

    @Column(name = "AAA101")
    var aaa101: String? = null

    @Column(name = "AAA030")
    var aaa030: Long? = null

    @Column(name = "AAA031")
    var aaa031: String? = null

    @Column(name = "AAE011")
    var aae011: String? = null

    @Column(name = "AAE019")
    var aae019: String? = null

    @Column(name = "XZ")
    var xz: String? = null

    @Column(name = "PSUM")
    var psum: Long? = null

}
