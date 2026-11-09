package com.jlrc.rcpq.entity

import jakarta.persistence.*
import java.io.Serializable
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "TB95")
open class TB95 {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_TB95_ATB950")
    @SequenceGenerator(name = "SEQ_TB95_ATB950", sequenceName = "SEQ_TB95_ATB950", allocationSize = 1)
    @Column(name = "ATB950")
    var atb950: Long? = null

    @Column(name = "BAZ001")
    var baz001: Long? = null

    @Column(name = "BAZ002")
    var baz002: Long? = null

    @Column(name = "AAE002")
    var aae002: Long? = null

    @Column(name = "AAA030")
    var aaa030: Long? = null

    @Column(name = "AAA031")
    var aaa031: String? = null

    @Column(name = "AAA100")
    var aaa100: String? = null

    @Column(name = "AAA101")
    var aaa101: String? = null

    @Column(name = "ATB951")
    var atb951: String? = null

    @Column(name = "AAE003")
    var aae003: Long? = null

    @Column(name = "AAE004")
    var aae004: Long? = null

    @Column(name = "ATB952")
    var atb952: String? = null

    @Column(name = "ATB953")
    var atb953: Long? = null

    @Column(name = "ATB954")
    var atb954: BigDecimal? = null

    @Column(name = "ATB955")
    var atb955: BigDecimal? = null

    @Column(name = "ATB956")
    var atb956: String? = null

    @Column(name = "ATB957")
    var atb957: Long? = null

    @Column(name = "ATB958")
    var atb958: BigDecimal? = null

    @Column(name = "ATB959")
    var atb959: BigDecimal? = null

    @Column(name = "ATB95A")
    var atb95a: Long? = null

    @Column(name = "ATB95B")
    var atb95b: Long? = null

    @Column(name = "ATB95C")
    var atb95c: Long? = null

    @Column(name = "ATB95D")
    var atb95d: Long? = null

    @Column(name = "ATB95E")
    var atb95e: Long? = null

}
