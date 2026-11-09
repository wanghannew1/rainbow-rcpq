package com.jlrc.rcpq.entity

import jakarta.persistence.*
import java.io.Serializable
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "TD04_BP")
open class TD04_BP {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_TD04_ID")
    @SequenceGenerator(name = "SEQ_TD04_ID", sequenceName = "SEQ_TD04_ID", allocationSize = 1)
    @Column(name = "ID")
    var id: Long? = null

    @Column(name = "ACE100")
    var ace100: Long? = null

    @Column(name = "ATB930")
    var atb930: Long? = null

    @Column(name = "ATB931")
    var atb931: String? = null

    @Column(name = "AAC003")
    var aac003: String? = null

    @Column(name = "AAC002")
    var aac002: String? = null

    @Column(name = "ATD049")
    var atd049: String? = null

    @Column(name = "ATD041")
    var atd041: String? = null

    @Column(name = "ATD045")
    var atd045: String? = null

    @Column(name = "ATD042")
    var atd042: String? = null

    @Column(name = "ATD04A")
    var atd04a: String? = null

    @Column(name = "ATD043")
    var atd043: String? = null

    @Column(name = "ATD04B")
    var atd04b: String? = null

    @Column(name = "AAE002")
    var aae002: String? = null

    @Column(name = "ATD04C")
    var atd04c: String? = null

    @Column(name = "ACE10A")
    var ace10a: String? = null

    @Column(name = "ACE10B")
    var ace10b: String? = null

    @Column(name = "AAE011")
    var aae011: String? = null

    @Column(name = "AAE017")
    var aae017: String? = null

    @Column(name = "AAE036")
    var aae036: String? = null

    @Column(name = "AAE019")
    var aae019: String? = null

    @Column(name = "AAE020")
    var aae020: String? = null

    @Column(name = "AAE022")
    var aae022: String? = null

}
