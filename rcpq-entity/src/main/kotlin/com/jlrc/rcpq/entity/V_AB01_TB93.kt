package com.jlrc.rcpq.entity

import jakarta.persistence.*
import java.io.Serializable
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "V_AB01_TB93")
open class V_AB01_TB93 {
    @Id
    @Column(name = "ATB930")
    var atb930: Long? = null

    @Column(name = "AAA031")
    var aaa031: String? = null

    @Column(name = "ATB931")
    var atb931: String? = null

    @Column(name = "AAB00E")
    var aab00e: String? = null

    @Column(name = "AAA032")
    var aaa032: String? = null

}
