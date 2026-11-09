package com.jlrc.rcpq.entity

import jakarta.persistence.*
import java.io.Serializable
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "TC93_SSBP")
open class TC93_SSBP {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_TC93_SSBP_ID")
    @SequenceGenerator(name = "SEQ_TC93_SSBP_ID", sequenceName = "SEQ_TC93_SSBP_ID", allocationSize = 1)
    @Column(name = "ID")
    var id: Long? = null

    @Column(name = "ACE100")
    var ace100: Long? = null

    @Column(name = "AAC003")
    var aac003: String? = null

    @Column(name = "AAC002")
    var aac002: String? = null

    @Column(name = "S_DATE")
    var s_date: String? = null

    @Column(name = "D_DATE")
    var d_date: String? = null

    @Column(name = "SSLB")
    var sslb: String? = null

    @Column(name = "SSJE")
    var ssje: BigDecimal? = null

    @Column(name = "ACE10A")
    var ace10a: String? = null

    @Column(name = "ACE10B")
    var ace10b: String? = null

    @Column(name = "AAE022")
    var aae022: String? = null

    @Column(name = "YEAR")
    var year: String? = null

}
