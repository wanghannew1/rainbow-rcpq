package com.jlrc.rcpq.entity

import jakarta.persistence.*
import java.io.Serializable
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "V_TC90_ADW")
open class V_TC90_ADW {
    @Id
    @Column(name = "ATC900")
    var atc900: Long? = null

    @Column(name = "AAA032")
    var aaa032: String? = null

    @Column(name = "AAA001")
    var aaa001: String? = null

    @Column(name = "AAC002")
    var aac002: String? = null

    @Column(name = "AAB001")
    var aab001: Long? = null

    @Column(name = "ATB930")
    var atb930: Long? = null

    @Column(name = "AAC003")
    var aac003: String? = null

    @Column(name = "AAA301")
    var aaa301: BigDecimal? = null

    @Column(name = "ATC90A")
    var atc90a: String? = null

    @Column(name = "ATC90B")
    var atc90b: LocalDate? = null

    @Column(name = "ATC90C")
    var atc90c: LocalDate? = null

    @Column(name = "ATC90D")
    var atc90d: LocalDate? = null

    @Column(name = "AAE100")
    var aae100: String? = null

    @Column(name = "ATB90R")
    var atb90r: String? = null

    @Column(name = "AAE036")
    var aae036: LocalDateTime? = null

    @Column(name = "ATB931")
    var atb931: String? = null

    @Column(name = "AAB004")
    var aab004: String? = null

    @Column(name = "ATC904")
    var atc904: Long? = null

    @Column(name = "AAC004")
    var aac004: String? = null

    @Column(name = "AAA004")
    var aaa004: String? = null

    @Column(name = "AAA003")
    var aaa003: String? = null

    @Column(name = "AAE005")
    var aae005: String? = null

    @Column(name = "ATC902")
    var atc902: String? = null

    @Column(name = "AAE008")
    var aae008: Long? = null

    @Column(name = "AAE013")
    var aae013: String? = null

    @Column(name = "AAA202")
    var aaa202: Long? = null

    @Column(name = "AAC013")
    var aac013: String? = null

    @Column(name = "AAC009")
    var aac009: String? = null

    @Column(name = "AAA303")
    var aaa303: BigDecimal? = null

}
