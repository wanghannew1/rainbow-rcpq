package com.jlrc.rcpq.entity

import jakarta.persistence.*
import java.io.Serializable
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "AA03")
open class AA03 {
    @Id
    @Column(name = "AAA030")
    var aaa030: Long? = null

    @Column(name = "AAA031")
    var aaa031: String? = null

    @Column(name = "AAA032")
    var aaa032: String? = null

    @Column(name = "AAA033")
    var aaa033: String? = null

    @Column(name = "AAB301")
    var aab301: String? = null

    @Column(name = "AAA021")
    var aaa021: String? = null

    @Column(name = "AAA034")
    var aaa034: BigDecimal? = null

    @Column(name = "AAA035")
    var aaa035: BigDecimal? = null

    @Column(name = "AAA036")
    var aaa036: String? = null

    @Column(name = "AAA037")
    var aaa037: String? = null

    @Column(name = "AAA032_1")
    var aaa032_1: String? = null

    @Column(name = "AAA032_2")
    var aaa032_2: String? = null

    @Column(name = "AAA033_1")
    var aaa033_1: String? = null

    @Column(name = "AAA033_2")
    var aaa033_2: String? = null

    @Column(name = "AAA037_1")
    var aaa037_1: String? = null

    @Column(name = "AAA037_2")
    var aaa037_2: String? = null

    @Column(name = "AAB301_1")
    var aab301_1: String? = null

    @Column(name = "AAA021_1")
    var aaa021_1: String? = null

    @Column(name = "AAA034_1")
    var aaa034_1: BigDecimal? = null

    @Column(name = "AAA035_1")
    var aaa035_1: BigDecimal? = null

    @Column(name = "AAA032_3")
    var aaa032_3: String? = null

    @Column(name = "AAA033_3")
    var aaa033_3: String? = null

    @Column(name = "AAA036_3")
    var aaa036_3: String? = null

    @Column(name = "AAA037_3")
    var aaa037_3: String? = null

    @Column(name = "AAA032_4")
    var aaa032_4: String? = null

    @Column(name = "AAA032_5")
    var aaa032_5: String? = null

    @Column(name = "AAA033_4")
    var aaa033_4: String? = null

    @Column(name = "AAA033_5")
    var aaa033_5: String? = null

    @Column(name = "AAA037_4")
    var aaa037_4: String? = null

    @Column(name = "AAA037_5")
    var aaa037_5: String? = null

    @Column(name = "AAE013")
    var aae013: String? = null

}
