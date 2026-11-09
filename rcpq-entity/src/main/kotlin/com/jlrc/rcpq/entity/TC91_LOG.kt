package com.jlrc.rcpq.entity

import jakarta.persistence.*
import java.io.Serializable
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "TC91_LOG")
open class TC91_LOG {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_TC91_LOG_ID")
    @SequenceGenerator(name = "SEQ_TC91_LOG_ID", sequenceName = "SEQ_TC91_LOG_ID", allocationSize = 1)
    @Column(name = "ID")
    var id: Long? = null

    @Column(name = "ATC930")
    var atc930: Long? = null

    @Column(name = "ATC910")
    var atc910: Long? = null

    @Column(name = "ATC913")
    var atc913: BigDecimal? = null

    @Column(name = "ATC916")
    var atc916: BigDecimal? = null

    @Column(name = "MONEY")
    var money: BigDecimal? = null

}
