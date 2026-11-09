package com.jlrc.rcpq.entity

import jakarta.persistence.*
import java.io.Serializable
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "TB96")
open class TB96 {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_TB96_ATB960")
    @SequenceGenerator(name = "SEQ_TB96_ATB960", sequenceName = "SEQ_TB96_ATB960", allocationSize = 1)
    @Column(name = "ATB960")
    var atb960: Long? = null

    @Column(name = "BAZ001")
    var baz001: Long? = null

    @Column(name = "BAZ002")
    var baz002: Long? = null

    @Column(name = "ATB930")
    var atb930: Long? = null

    @Column(name = "ATB931")
    var atb931: String? = null

    @Column(name = "AAB001")
    var aab001: Long? = null

    @Column(name = "AAB004")
    var aab004: String? = null

    @Column(name = "AAA030")
    var aaa030: Long? = null

    @Column(name = "AAA031")
    var aaa031: String? = null

    @Column(name = "ATC937")
    var atc937: String? = null

    @Column(name = "AAE002")
    var aae002: Long? = null

    @Column(name = "ATB961")
    var atb961: BigDecimal? = null

    @Column(name = "ATB962")
    var atb962: BigDecimal? = null

    @Column(name = "ATB96Z")
    var atb96z: String? = null

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

    @Column(name = "ATB963")
    var atb963: BigDecimal? = null

    @Column(name = "ATB964")
    var atb964: BigDecimal? = null

    @Column(name = "ATB965")
    var atb965: BigDecimal? = null

    @Column(name = "ATB96Y")
    var atb96y: String? = null

    @Column(name = "ATB96X")
    var atb96x: LocalDate? = null

}
