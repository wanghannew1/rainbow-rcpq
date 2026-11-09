package com.jlrc.rcpq.entity

import jakarta.persistence.*
import java.io.Serializable
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "TC90_ZZ")
open class TC90_ZZ {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_TC90_ZZ_ID")
    @SequenceGenerator(name = "SEQ_TC90_ZZ_ID", sequenceName = "SEQ_TC90_ZZ_ID", allocationSize = 1)
    @Column(name = "ID")
    var id: Long? = null

    @Column(name = "AAB001_1")
    var aab001_1: String? = null

    @Column(name = "AAB004_1")
    var aab004_1: String? = null

    @Column(name = "ATC90I")
    var atc90i: LocalDate? = null

    @Column(name = "AAE100")
    var aae100: String? = null

    @Column(name = "ATC900")
    var atc900: Long? = null

    @Column(name = "AAC002")
    var aac002: String? = null

    @Column(name = "AAC003")
    var aac003: String? = null

    @Column(name = "AAE013")
    var aae013: String? = null

    @Column(name = "ATC90AU")
    var atc90au: LocalDate? = null

    @Column(name = "BAE011")
    var bae011: String? = null

    @Column(name = "BAE017")
    var bae017: String? = null

    @Column(name = "BAE036")
    var bae036: LocalDate? = null

    @Column(name = "BAE019")
    var bae019: String? = null

    @Column(name = "BAE020")
    var bae020: String? = null

    @Column(name = "BAE022")
    var bae022: String? = null

    @Column(name = "AAE019")
    var aae019: String? = null

    @Column(name = "AAE011")
    var aae011: String? = null

    @Column(name = "AAE036")
    var aae036: LocalDateTime? = null

}
