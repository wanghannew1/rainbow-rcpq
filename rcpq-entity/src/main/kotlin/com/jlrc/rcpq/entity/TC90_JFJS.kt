package com.jlrc.rcpq.entity

import jakarta.persistence.*
import java.io.Serializable
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "TC90_JFJS")
open class TC90_JFJS {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_TC90_JFJS_ID")
    @SequenceGenerator(name = "SEQ_TC90_JFJS_ID", sequenceName = "SEQ_TC90_JFJS_ID", allocationSize = 1)
    @Column(name = "ID")
    var id: Long? = null

    @Column(name = "ATC900")
    var atc900: Long? = null

    @Column(name = "AAA301")
    var aaa301: BigDecimal? = null

    @Column(name = "AAA301_1")
    var aaa301_1: BigDecimal? = null

    @Column(name = "AAE013")
    var aae013: String? = null

    @Column(name = "AAE011")
    var aae011: String? = null

    @Column(name = "AAE036")
    var aae036: LocalDateTime? = null

    @Column(name = "AAE019")
    var aae019: String? = null

}
