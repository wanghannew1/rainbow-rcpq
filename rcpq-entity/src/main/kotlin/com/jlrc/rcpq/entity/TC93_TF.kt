package com.jlrc.rcpq.entity

import jakarta.persistence.*
import java.io.Serializable
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "TC93_TF")
open class TC93_TF {
    @Id
    @Column(name = "ATC900")
    var atc900: Long? = null

    @Column(name = "ATB930")
    var atb930: Long? = null

    @Column(name = "AAE001")
    var aae001: Long? = null

    @Column(name = "ATC930")
    var atc930: String? = null

    @Column(name = "ID")
    var id: String? = null

}
