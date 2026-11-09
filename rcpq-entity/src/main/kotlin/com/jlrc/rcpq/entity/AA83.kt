package com.jlrc.rcpq.entity

import jakarta.persistence.*
import java.io.Serializable
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "AA83")
open class AA83 {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_AA83_ID")
    @SequenceGenerator(name = "SEQ_AA83_ID", sequenceName = "SEQ_AA83_ID", allocationSize = 1)
    @Column(name = "ID")
    var id: Long? = null

    @Column(name = "ATC930")
    var atc930: Long? = null

    @Column(name = "ATB930")
    var atb930: Long? = null

    @Column(name = "ATC900")
    var atc900: Long? = null

    @Column(name = "AAC003")
    var aac003: String? = null

    @Column(name = "AAC002")
    var aac002: String? = null

    @Column(name = "AAE001")
    var aae001: Long? = null

    @Column(name = "AAE002")
    var aae002: Long? = null

    @Column(name = "AAA831")
    var aaa831: BigDecimal? = null

    @Column(name = "ATC93AA")
    var atc93aa: BigDecimal? = null

    @Column(name = "ATJ029")
    var atj029: BigDecimal? = null

    @Column(name = "BBA000")
    var bba000: BigDecimal? = null

    @Column(name = "BAA000")
    var baa000: BigDecimal? = null

    @Column(name = "CAA001")
    var caa001: BigDecimal? = null

    @Column(name = "CAA002")
    var caa002: BigDecimal? = null

    @Column(name = "BBA200")
    var bba200: BigDecimal? = null

    @Column(name = "BAA200")
    var baa200: BigDecimal? = null

    @Column(name = "BBA300")
    var bba300: BigDecimal? = null

    @Column(name = "BAA300")
    var baa300: BigDecimal? = null

    @Column(name = "AAA832")
    var aaa832: BigDecimal? = null

    @Column(name = "AAA833")
    var aaa833: String? = null

    @Column(name = "AAA834")
    var aaa834: String? = null

    @Column(name = "AAA835")
    var aaa835: String? = null

    @Column(name = "AAA836")
    var aaa836: BigDecimal? = null

    @Column(name = "AAA837")
    var aaa837: BigDecimal? = null

    @Column(name = "AAA838")
    var aaa838: BigDecimal? = null

    @Column(name = "ATC937")
    var atc937: String? = null

}
