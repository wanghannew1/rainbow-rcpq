package com.jlrc.rcpq.entity

import jakarta.persistence.*
import java.io.Serializable
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "TC04_BP")
open class TC04_BP {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_TC04_ID")
    @SequenceGenerator(name = "SEQ_TC04_ID", sequenceName = "SEQ_TC04_ID", allocationSize = 1)
    @Column(name = "ID")
    var id: Long? = null

    @Column(name = "ACE100")
    var ace100: Long? = null

    @Column(name = "XH")
    var xh: String? = null

    @Column(name = "AAA005")
    var aaa005: String? = null

    @Column(name = "AAC003")
    var aac003: String? = null

    @Column(name = "AAE002")
    var aae002: Long? = null

    @Column(name = "AAE013")
    var aae013: String? = null

    @Column(name = "ATC041")
    var atc041: String? = null

    @Column(name = "ATC042")
    var atc042: String? = null

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

    @Column(name = "ATC900")
    var atc900: Long? = null

}
