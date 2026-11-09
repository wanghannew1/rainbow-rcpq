package com.jlrc.rcpq.entity

import jakarta.persistence.*
import java.io.Serializable
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "SYS_ORG_USER_UNIT")
open class SYS_ORG_USER_UNIT {
    @Id
    @Column(name = "AAE011")
    var aae011: String? = null

    @Column(name = "ACCOUNT")
    var account: String? = null

    @Column(name = "AAE019")
    var aae019: String? = null

    @Column(name = "AAE017")
    var aae017: String? = null

    @Column(name = "AAE020")
    var aae020: String? = null

    @Column(name = "AAE022")
    var aae022: String? = null

    @Column(name = "MOBILE")
    var mobile: String? = null

    @Column(name = "TELEPHONE")
    var telephone: String? = null

}
