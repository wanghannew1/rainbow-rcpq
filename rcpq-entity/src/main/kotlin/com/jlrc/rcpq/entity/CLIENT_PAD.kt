package com.jlrc.rcpq.entity

import jakarta.persistence.*
import java.io.Serializable
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "CLIENT_PAD")
open class CLIENT_PAD {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_CLIENT_PAD_CLIENTPAD")
    @SequenceGenerator(name = "SEQ_CLIENT_PAD_CLIENTPAD", sequenceName = "SEQ_CLIENT_PAD_CLIENTPAD", allocationSize = 1)
    @Column(name = "CLIENTPAD")
    var clientpad: Long? = null

    @Column(name = "CLIENT_IP")
    var client_ip: String? = null

    @Column(name = "PAD_IP")
    var pad_ip: String? = null

}
