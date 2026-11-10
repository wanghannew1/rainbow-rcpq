package com.jlrc.rcpq.entity

import jakarta.persistence.*
import java.io.Serializable
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "TC16")
open class TC16 {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_TC16_ATC160")
    @SequenceGenerator(name = "SEQ_TC16_ATC160", sequenceName = "SEQ_TC16_ATC160", allocationSize = 1)
    @Column(name = "ATC160")
    var atc160: Long? = null

    @Column(name = "BAZ001")
    var baz001: Long? = null

    @Column(name = "BAZ002")
    var baz002: Long? = null

    @Column(name = "ATC161")
    var atc161: String? = null

    @Column(name = "ATC162")
    var atc162: String? = null

    @Column(name = "ATC163")
    var atc163: String? = null

    @Column(name = "ATC164")
    var atc164: String? = null

    @Column(name = "ATC165")
    var atc165: LocalDate? = null

    @Column(name = "ATC166")
    var atc166: Long? = null

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

    @Column(name = "ATB191")
    var atb191: String? = null

    @Column(name = "ATB190")
    var atb190: BigDecimal? = null
}
