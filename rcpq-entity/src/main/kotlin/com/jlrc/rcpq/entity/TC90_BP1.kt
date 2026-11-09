package com.jlrc.rcpq.entity

import jakarta.persistence.*
import java.io.Serializable
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "TC90_BP1")
open class TC90_BP1 {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_TC90_BP1_ID")
    @SequenceGenerator(name = "SEQ_TC90_BP1_ID", sequenceName = "SEQ_TC90_BP1_ID", allocationSize = 1)
    @Column(name = "ID")
    var id: Long? = null

    @Column(name = "ACE100")
    var ace100: Long? = null

    @Column(name = "ATB930")
    var atb930: Long? = null

    @Column(name = "ATB900")
    var atb900: Long? = null

    @Column(name = "AAB001")
    var aab001: Long? = null

    @Column(name = "AAB004")
    var aab004: String? = null

    @Column(name = "AAC001")
    var aac001: Long? = null

    @Column(name = "AAC003")
    var aac003: String? = null

    @Column(name = "AAC002")
    var aac002: String? = null

    @Column(name = "ATC90I")
    var atc90i: String? = null

    @Column(name = "AAE003")
    var aae003: String? = null

    @Column(name = "ATC90K")
    var atc90k: String? = null

    @Column(name = "ATC90L")
    var atc90l: String? = null

    @Column(name = "ATC90AV")
    var atc90av: String? = null

    @Column(name = "ATC90N")
    var atc90n: String? = null

    @Column(name = "AAE013")
    var aae013: String? = null

    @Column(name = "ACE10A")
    var ace10a: String? = null

    @Column(name = "ACE10B")
    var ace10b: String? = null

    @Column(name = "BAE011")
    var bae011: String? = null

    @Column(name = "BAE017")
    var bae017: String? = null

    @Column(name = "BAE036")
    var bae036: String? = null

    @Column(name = "BAE019")
    var bae019: String? = null

    @Column(name = "BAE020")
    var bae020: String? = null

    @Column(name = "BAE022")
    var bae022: String? = null

    @Column(name = "ATC90P")
    var atc90p: String? = null

    @Column(name = "ATC90W")
    var atc90w: String? = null

    @Column(name = "ATC90PA")
    var atc90pa: String? = null

    @Column(name = "AAA202")
    var aaa202: String? = null

    @Column(name = "AAA502")
    var aaa502: String? = null

    @Column(name = "AAA602")
    var aaa602: String? = null

    @Column(name = "AAA902")
    var aaa902: String? = null

}
