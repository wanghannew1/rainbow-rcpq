package com.jlrc.rcpq.entity

import jakarta.persistence.*
import java.io.Serializable
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "TC90_OUTTER")
open class TC90_OUTTER {
    @Id
    @Column(name = "ID")
    var id: Long? = null

    @Column(name = "AAC003")
    var aac003: String? = null

    @Column(name = "ISRED_AAC003")
    var isred_aac003: String? = null

    @Column(name = "AAC002")
    var aac002: String? = null

    @Column(name = "ISRED_AAC002")
    var isred_aac002: String? = null

    @Column(name = "AAA001")
    var aaa001: String? = null

    @Column(name = "ISRED_AAA001")
    var isred_aaa001: String? = null

    @Column(name = "AAA301")
    var aaa301: String? = null

    @Column(name = "ISRED_AAA301")
    var isred_aaa301: String? = null

    @Column(name = "AAA303")
    var aaa303: String? = null

    @Column(name = "ISRED_AAA303")
    var isred_aaa303: String? = null

    @Column(name = "ATB90S")
    var atb90s: String? = null

    @Column(name = "ISRED_ATB90S")
    var isred_atb90s: String? = null

    @Column(name = "ATC9V1")
    var atc9v1: String? = null

    @Column(name = "ISRED_ATC9V1")
    var isred_atc9v1: String? = null

    @Column(name = "AAC009")
    var aac009: String? = null

    @Column(name = "ISRED_AAC009")
    var isred_aac009: String? = null

    @Column(name = "BAA001")
    var baa001: BigDecimal? = null

    @Column(name = "CAA002")
    var caa002: BigDecimal? = null

    @Column(name = "ISRED_CAA002")
    var isred_caa002: String? = null

    @Column(name = "LABEL1")
    var label1: String? = null

    @Column(name = "LABEL2")
    var label2: String? = null

    @Column(name = "LABEL3")
    var label3: String? = null

    @Column(name = "FLAG")
    var flag: String? = null

    @Column(name = "RESULT")
    var result: String? = null

    @Column(name = "ISRED_LABEL1")
    var isred_label1: String? = null

}
