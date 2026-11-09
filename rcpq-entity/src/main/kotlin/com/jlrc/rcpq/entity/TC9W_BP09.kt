package com.jlrc.rcpq.entity

import jakarta.persistence.*
import java.io.Serializable
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "TC9W_BP09")
open class TC9W_BP09 {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_TC9W_BP09_ID")
    @SequenceGenerator(name = "SEQ_TC9W_BP09_ID", sequenceName = "SEQ_TC9W_BP09_ID", allocationSize = 1)
    @Column(name = "ID")
    var id: Long? = null

    @Column(name = "BAZ001")
    var baz001: Long? = null

    @Column(name = "BAZ002")
    var baz002: Long? = null

    @Column(name = "AAE002")
    var aae002: Long? = null

    @Column(name = "ATC9W1")
    var atc9w1: String? = null

    @Column(name = "AAA90W")
    var aaa90w: String? = null

    @Column(name = "AAA030")
    var aaa030: Long? = null

    @Column(name = "AAA031")
    var aaa031: String? = null

    @Column(name = "AAA020")
    var aaa020: String? = null

    @Column(name = "AAA021")
    var aaa021: String? = null

    @Column(name = "AAA001")
    var aaa001: String? = null

    @Column(name = "AAC002")
    var aac002: String? = null

    @Column(name = "AAC003")
    var aac003: String? = null

    @Column(name = "ABP001")
    var abp001: String? = null

    @Column(name = "ABO002")
    var abo002: String? = null

    @Column(name = "ABO003")
    var abo003: String? = null

    @Column(name = "AAE013")
    var aae013: String? = null

    @Column(name = "ACE10A")
    var ace10a: String? = null

    @Column(name = "ACE10B")
    var ace10b: String? = null

    @Column(name = "ATC900")
    var atc900: Long? = null

    @Column(name = "ATB930")
    var atb930: Long? = null

    @Column(name = "ATB931")
    var atb931: String? = null

    @Column(name = "AAB001")
    var aab001: Long? = null

    @Column(name = "AAB004")
    var aab004: String? = null

    @Column(name = "AAA90V")
    var aaa90v: String? = null

    @Column(name = "AAE001")
    var aae001: Long? = null

    @Column(name = "ATC93BD_1")
    var atc93bd_1: BigDecimal? = null

    @Column(name = "ATC93BD_2")
    var atc93bd_2: BigDecimal? = null

    @Column(name = "AAA201_1")
    var aaa201_1: Long? = null

    @Column(name = "AAA201_2")
    var aaa201_2: Long? = null

    @Column(name = "AAC008_1")
    var aac008_1: String? = null

    @Column(name = "AAC008_2")
    var aac008_2: String? = null

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

    @Column(name = "AAE211")
    var aae211: String? = null

    @Column(name = "AAE217")
    var aae217: String? = null

    @Column(name = "AAE236")
    var aae236: LocalDate? = null

    @Column(name = "AAE219")
    var aae219: String? = null

    @Column(name = "AAE220")
    var aae220: String? = null

    @Column(name = "AAE222")
    var aae222: String? = null

    @Column(name = "ABO004")
    var abo004: String? = null

    @Column(name = "ABO005")
    var abo005: String? = null

}
