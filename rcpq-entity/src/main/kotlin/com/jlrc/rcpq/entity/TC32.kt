package com.jlrc.rcpq.entity

import jakarta.persistence.*
import java.io.Serializable
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "TC32")
open class TC32 {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_TC32_ATC320")
    @SequenceGenerator(name = "SEQ_TC32_ATC320", sequenceName = "SEQ_TC32_ATC320", allocationSize = 1)
    @Column(name = "ATC320")
    var atc320: Long? = null

    @Column(name = "AAA030")
    var aaa030: Long? = null

    @Column(name = "AAA031")
    var aaa031: String? = null

    @Column(name = "AAE002")
    var aae002: Long? = null

    @Column(name = "AAC001")
    var aac001: Long? = null

    @Column(name = "AAC002")
    var aac002: String? = null

    @Column(name = "AAC003")
    var aac003: String? = null

    @Column(name = "RYZT")
    var ryzt: String? = null

}
