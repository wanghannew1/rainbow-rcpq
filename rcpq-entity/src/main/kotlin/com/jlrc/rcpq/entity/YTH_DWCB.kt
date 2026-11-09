package com.jlrc.rcpq.entity

import jakarta.persistence.*
import java.io.Serializable
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "YTH_DWCB")
open class YTH_DWCB {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_YTH_DWCB_AAB001")
    @SequenceGenerator(name = "SEQ_YTH_DWCB_AAB001", sequenceName = "SEQ_YTH_DWCB_AAB001", allocationSize = 1)
    @Column(name = "AAB001")
    var aab001: String? = null

    @Column(name = "AAB004")
    var aab004: String? = null

    @Column(name = "AAB003")
    var aab003: String? = null

    @Column(name = "AAB002")
    var aab002: String? = null

    @Column(name = "AAE141")
    var aae141: String? = null

    @Column(name = "AAB050")
    var aab050: LocalDateTime? = null

}
