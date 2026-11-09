package com.jlrc.rcpq.entity

import jakarta.persistence.*
import java.io.Serializable
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "V_AA97_NF")
open class V_AA97_NF {
    @Id
    @Column(name = "AAA971")
    var aaa971: Long? = null

}
