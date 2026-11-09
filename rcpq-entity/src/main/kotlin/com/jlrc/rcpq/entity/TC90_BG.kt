package com.jlrc.rcpq.entity

import jakarta.persistence.*
import java.io.Serializable
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "TC90_BG")
open class TC90_BG {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_TC90_BG_ID")
    @SequenceGenerator(name = "SEQ_TC90_BG_ID", sequenceName = "SEQ_TC90_BG_ID", allocationSize = 1)
    @Column(name = "ID")
    var id: Long? = null

    @Column(name = "ATC900")
    var atc900: Long? = null

    @Column(name = "COLUMNNAME")
    var columnname: String? = null

}
